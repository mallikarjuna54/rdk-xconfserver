/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Igor Kostrov
 * Created: 1/14/2016
*/
package com.comcast.xconf.util;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.RuleHelper;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class RuleUtil {
    public static void convertConditions(Rule mainRule, Predicate<Condition> predicate) {
        List<Rule> list = new ArrayList<Rule>();
        list.add(mainRule);
        while (!list.isEmpty()) {
            Rule rule = list.remove(0);

            if (rule.getCondition() != null) {
                predicate.apply(rule.getCondition());
            }

            if (rule.getCompoundParts() != null) {
                list.addAll(rule.getCompoundParts());
            }
        }
    }

    public static List<String> getFixedArgsFromRuleByFreeArgAndOperation(Rule rule, FreeArg freeArg, Operation operation) {
        final List<String> result = new ArrayList<>();
        for (Condition condition : RuleHelper.toConditions(rule)) {
            Object fixedArg = getFixedArgFromConditionByFreeArgAndOperation(condition, freeArg, operation);
            if (fixedArg != null) {
                result.add(fixedArg.toString());
            }
        }

        return result;
    }

    public static List<String> getFixedArgsFromRuleByOperation(Rule rule, Operation operation) {
        final List<String> result = new ArrayList<>();
        for (Condition condition : RuleHelper.toConditions(rule)) {
            Object fixedArg = getFixedArgFromConditionByOperation(condition, operation);
            if (fixedArg != null) {
                result.add(fixedArg.toString());
            }
        }

        return result;
    }

    public static Object getFixedArgFromConditionByOperation(Condition condition, Operation operation) {
        if (operation.equals(condition.getOperation())) {
            return condition.getFixedArg() != null ? condition.getFixedArg().getValue() : null;
        }

        return null;
    }

    public static Object getFixedArgFromConditionByFreeArgAndOperation(Condition condition, FreeArg freeArg, Operation operation) {
        if (operation.equals(condition.getOperation()) && freeArg.equals(condition.getFreeArg())) {
            return condition.getFixedArg() != null ? condition.getFixedArg().getValue() : null;
        }

        return null;
    }

    public static List<Condition> getAllConditions(Rule rule) {
        final List<Condition> result = new ArrayList<>();
        final List<Rule> tmpRulesQueue = new ArrayList<>();
        tmpRulesQueue.add(rule);
        while (!tmpRulesQueue.isEmpty()) {
            Rule currentRule = tmpRulesQueue.remove(0);
            if (currentRule != null) {
                if (currentRule.getCondition() != null) {
                    result.add(currentRule.getCondition());
                }
                if (CollectionUtils.isNotEmpty(currentRule.getCompoundParts())) {
                    tmpRulesQueue.addAll(currentRule.getCompoundParts());
                }
            }
        }

        return result;
    }

    public static List<Rule> getIterableList(Rule rule) {
        final List<Rule> result = new ArrayList<>();
        final List<Rule> tmpRulesQueue = new ArrayList<>();
        tmpRulesQueue.add(rule);
        while (!tmpRulesQueue.isEmpty()) {
            Rule currentRule = tmpRulesQueue.remove(0);
            if (currentRule != null) {
                if (currentRule.getCondition() != null) {
                    result.add(currentRule);
                }
                if (CollectionUtils.isNotEmpty(currentRule.getCompoundParts())) {
                    tmpRulesQueue.addAll(currentRule.getCompoundParts());
                }
            }
        }

        return result;
    }

    public static void normalizeConditions(Rule rule) {
        for (Condition condition : RuleHelper.toConditions(rule)) {
            normalizeCondition(condition);
        }
    }

    public static void normalizeCondition(Condition condition) {
        FreeArg freeArg = condition.getFreeArg();
        if (freeArg != null && freeArg.getName() != null) {
            freeArg.setName(freeArg.getName().trim());
        }

        FixedArg fixedArg = condition.getFixedArg();
        if (fixedArg != null && fixedArg.getValue() != null) {
            fixedArg.setValue(normalizeFixedArgValue(fixedArg.getValue(), freeArg, condition.getOperation()));
        }

        normalizeMacAddress(condition);
        normalizePartnerId(condition);
    }

    public static void normalizePartnerId(Condition condition) {
        if (condition.getFreeArg() != null
                && StringUtils.equals(LogUploaderContext.PARTNER_ID, condition.getFreeArg().getName())
                && condition.getFixedArg() != null
                && condition.getFixedArg().getValue() != null) {
            String newPartnerId = condition.getFixedArg().getValue().toString().toUpperCase();
            condition.getFixedArg().setValue(newPartnerId);
        }
    }

    public static void normalizeMacAddress(Condition condition) {
        ArrayList<String> macAddressNames = Lists.newArrayList(StbContext.ECM_MAC, StbContext.ESTB_MAC, LogUploaderContext.ECM_MAC, LogUploaderContext.ECM_MAC);
        if (condition.getFixedArg() != null
                && condition.getFixedArg().getValue() != null
                && condition.getFreeArg() != null
                && macAddressNames.contains(condition.getFreeArg().getName())) {
            if (StandardOperation.IS.equals(condition.getOperation())) {
                String normalizedMac = MacAddress.normalize((String) condition.getFixedArg().getValue());
                condition.getFixedArg().setValue(normalizedMac);
            } else if(StandardOperation.LIKE.equals(condition.getOperation())) {
                String normalizedMac = MacAddress.normalize((String) condition.getFixedArg().getValue());
                condition.getFixedArg().setValue(normalizedMac);
            } else if(StandardOperation.IN.equals(condition.getOperation())) {
                List<Object> macAddresses = Lists.newArrayList((Collection) condition.getFixedArg().getValue());
                for (int i=0; i < macAddresses.size(); i++) {
                    macAddresses.set(i, MacAddress.normalize((String) macAddresses.get(i)));
                }
                condition.getFixedArg().setValue(macAddresses);
            }
        }
    }

    public static Object normalizeFixedArgValue(Object fixedArgValue, FreeArg freeArg, Operation operation) {
        if (fixedArgValue instanceof Iterable) {
            List<String> normalizedItems = new ArrayList<>();
            for (Object item : (Iterable) fixedArgValue) {
                if (item != null && StringUtils.isNotBlank(item.toString())) {
                    String normalizedItem = item.toString().trim();
                    normalizedItems.add(modifyFixedArgDependingOnFreeArgAndOperation(normalizedItem, freeArg, operation));
                }
            }

            return normalizedItems;
        } else {
            return modifyFixedArgDependingOnFreeArgAndOperation(fixedArgValue.toString().trim(), freeArg, operation);
        }
    }

    private static String modifyFixedArgDependingOnFreeArgAndOperation(String fixedArgValue, FreeArg freeArg, Operation operation) {
        if (fixedArgMustBeUpperCase(freeArg, operation)) {
            fixedArgValue = fixedArgValue.toUpperCase();
        }
        if (fixedArgMustBeNormalizedAsMacAddress(freeArg, operation)) {
            fixedArgValue = MacAddress.normalize(fixedArgValue);
        }

        return fixedArgValue;
    }

    private static Boolean fixedArgMustBeUpperCase(FreeArg freeArg, Operation operation) {
        return (StandardOperation.IS.equals(operation) || StandardOperation.IN.equals(operation)) && isEnvOrModelFreeArg(freeArg);
    }

    private static Boolean fixedArgMustBeNormalizedAsMacAddress(FreeArg freeArg, Operation operation) {
        return (StandardOperation.IS.equals(operation) || StandardOperation.IN.equals(operation)) && isMacAddressFreeArg(freeArg);
    }

    private static Boolean isEnvOrModelFreeArg(FreeArg freeArg) {
        if (freeArg != null && StringUtils.isNotBlank(freeArg.getName())) {
            String freeArgName = freeArg.getName();
            return StbContext.MODEL.equals(freeArgName) ||
                    StbContext.ENVIRONMENT.equals(freeArgName) ||
                    LogUploaderContext.MODEL.equals(freeArgName) ||
                    LogUploaderContext.ENV.equals(freeArgName);
        }

        return false;
    }

    private static Boolean isMacAddressFreeArg(FreeArg freeArg) {
        if (freeArg != null && StringUtils.isNotBlank(freeArg.getName())) {
            String freeArgName = freeArg.getName();
            return StbContext.ESTB_MAC.equals(freeArgName) ||
                    LogUploaderContext.ESTB_MAC.equals(freeArgName);
        }

        return false;
    }

    public static Collection<Condition> getDuplicateConditions(final Rule rule) {
        List<Condition> result = new ArrayList<>();
        Collection<Rule> duplicateRules = getDuplicateNonCompoundRules(flattenRule(rule));

        for (Rule duplicateRule : duplicateRules) {
            result.add(duplicateRule.getCondition());
        }

        return result;
    }

    public static boolean equalComplexRules(Rule rule1, Rule rule2) {
        boolean result;
        List<Rule> flattenedRule1 = flattenRule(rule1);
        List<Rule> flattenedRule2 = flattenRule(rule2);
        if (flattenedRule1.size() != flattenedRule2.size()) {
            return false;
        }
        if (flattenedRule1.size() > 1 &&
                allRulesHaveSameRelation(flattenedRule1) &&
                allRulesHaveSameRelation(flattenedRule2)) {
            Relation sameRelation = flattenedRule1.get(flattenedRule1.size() - 1).getRelation();
            flattenedRule1.get(0).setRelation(sameRelation);
            flattenedRule2.get(0).setRelation(sameRelation);
            result = equalNonCompoundRulesCollections(flattenedRule1, flattenedRule2);
            flattenedRule1.get(0).setRelation(null);
            flattenedRule2.get(0).setRelation(null);
        } else {
            result = equalNonCompoundRulesCollectionsRegardingTheOrder(flattenedRule1, flattenedRule2);
        }

        return result;
    }

    public static Collection<String> getDuplicateFixedArgListItems(Object fixedArgValue) {
        if (fixedArgValue instanceof Collection) {
            Collection<String> items = (Collection) fixedArgValue;
            Set<String> uniqueItems = new TreeSet<>(items);

            if (uniqueItems.size() < items.size()) {
                return CollectionUtils.disjunction(items, uniqueItems);
            }
        }

        return Collections.emptyList();
    }

    @VisibleForTesting
    static boolean equalNonCompoundRules(Rule rule1, Rule rule2) {
        rule1 = rule1 != null ? rule1 : new Rule();
        rule2 = rule2 != null ? rule2 : new Rule();

        Relation relation1 = rule1.getRelation();
        Relation relation2 = rule2.getRelation();
        if ((relation1 != null && !relation1.equals(relation2)) ||
                (relation2 != null && !relation2.equals(relation1))) {
            return false;
        }

        if (rule1.isNegated() != rule2.isNegated()) {
            return false;
        }

        return equalConditions(rule1.getCondition(), rule2.getCondition());
    }

    @VisibleForTesting
    static boolean equalConditions(Condition condition1, Condition condition2) {
        condition1 = condition1 != null ? condition1 : new Condition();
        condition2 = condition2 != null ? condition2 : new Condition();

        FreeArg freeArg1 = condition1.getFreeArg();
        FreeArg freeArg2 = condition2.getFreeArg();
        String freeArg1Name = freeArg1 != null && freeArg1.getName() != null ? freeArg1.getName() : "";
        String freeArg2Name = freeArg2 != null && freeArg2.getName() != null ? freeArg2.getName() : "";
        if (!freeArg1Name.equals(freeArg2Name)) {
            return false;
        }

        Operation operation1 = condition1.getOperation();
        Operation operation2 = condition2.getOperation();
        if ((operation1 != null && !operation1.equals(operation2)) ||
                operation2 != null && !operation2.equals(operation1)) {
            return false;
        }

        FixedArg fixedArg1 = condition1.getFixedArg();
        FixedArg fixedArg2 = condition2.getFixedArg();
        Object fixedArgValue1 = fixedArg1 != null ? fixedArg1.getValue() : "";
        Object fixedArgValue2 = fixedArg2 != null ? fixedArg2.getValue() : "";

        return equalFixedArgValues(fixedArgValue1, fixedArgValue2);
    }

    @VisibleForTesting
    static boolean equalFixedArgValues(Object fixedArgValue1, Object fixedArgValue2) {
        fixedArgValue1 = fixedArgValue1 != null ? fixedArgValue1 : "";
        fixedArgValue2 = fixedArgValue2 != null ? fixedArgValue2 : "";

        if (fixedArgValue1 instanceof Collection && fixedArgValue2 instanceof Collection) {
            return equalCollections((Collection) fixedArgValue1, (Collection) fixedArgValue2);
        } else if(!(fixedArgValue1 instanceof Collection) && !(fixedArgValue2 instanceof Collection)) {
            return fixedArgValue1.toString().equals(fixedArgValue2.toString());
        } else {
            return false;
        }
    }

    private static boolean equalNonCompoundRulesCollectionsRegardingTheOrder(List<Rule> list1, List<Rule> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            if (!equalNonCompoundRules(list1.get(i), list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean equalNonCompoundRulesCollections(List<Rule> list1, List<Rule> list2) {
        if (list1.size() == list2.size()) {
            return intersectionOfNonCompoundRules(list1, list2).size() == list1.size();
        }

        return false;
    }

    private static List<Rule> flattenRule(Rule rootRule) {
        List<Rule> result = new ArrayList<>();
        List<Rule> tmpList = new ArrayList<>();
        tmpList.add(rootRule);
        while (CollectionUtils.isNotEmpty(tmpList)) {
            Rule currentRule = tmpList.remove(0);
            if (currentRule != null) {
                if (currentRule.getCondition() != null) {
                    result.add(currentRule);
                }
                if (CollectionUtils.isNotEmpty(currentRule.getCompoundParts())) {
                    tmpList.addAll(currentRule.getCompoundParts());
                }
            }
        }

        return result;
    }

    private static boolean allRulesHaveSameRelation(List<Rule> rules) {
        if (rules != null && rules.size() > 1) {
            Relation relationToCompare = rules.get(rules.size() - 1).getRelation();
            for (Rule rule : rules) {
                if (rule != null && rule.getRelation() != null && !rule.getRelation().equals(relationToCompare)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static Collection<Rule> intersectionOfNonCompoundRules(Collection<Rule> rules1, Collection<Rule> rules2) {
        rules2 = new ArrayList<>(rules2);
        Collection<Rule> result = new ArrayList<>();
        for (Rule rule1 : rules1) {
            for (Iterator<Rule> iterator = rules2.iterator(); iterator.hasNext();) {
                Rule rule2 = iterator.next();
                if (equalNonCompoundRules(rule1, rule2)) {
                    result.add(rule1);
                    iterator.remove();
                    break;
                }
            }
        }

        return result;
    }

    private static Collection<Rule> getDuplicateNonCompoundRules(final List<Rule> nonCompoundRules) {
        Collection<Rule> result = new ArrayList<>();
        if (nonCompoundRules.size() > 1 && allRulesHaveSameRelation(nonCompoundRules)) {
            Relation sameRelation = nonCompoundRules.get(nonCompoundRules.size() - 1).getRelation();
            nonCompoundRules.get(0).setRelation(sameRelation);
            Set<Rule> uniqueNonCompoundRules = Sets.newTreeSet(new Comparator<Rule>() {
                @Override
                public int compare(Rule o1, Rule o2) {
                    if (equalNonCompoundRules(o1, o2)) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });
            uniqueNonCompoundRules.addAll(nonCompoundRules);
            nonCompoundRules.get(0).setRelation(null);

            if (uniqueNonCompoundRules.size() < nonCompoundRules.size()) {
                result = CollectionUtils.disjunction(nonCompoundRules, uniqueNonCompoundRules);
            }
        }

        return result;
    }

    private static boolean equalCollections(Collection a, Collection b) {
        if (a.size() == b.size()) {
            return CollectionUtils.intersection(a, b).size() == a.size();
        }

        return false;
    }

    public static boolean isExistConditionByFreeArgName(Rule rule, String freeArgName) {
        for(Condition condition : getAllConditions(rule)) {
            if (StringUtils.contains(condition.getFreeArg().getName().toLowerCase(), freeArgName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExistConditionByFixedArgValue(Rule rule, String fixedArgValue) {
        for(Condition condition : getAllConditions(rule)) {
            if (equalFixedArgCondition(condition, fixedArgValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equalFixedArgCondition(Condition condition, String fixedArgValue) {
        if (condition.getFixedArg() != null && condition.getFixedArg().getValue() instanceof Collection) {
            return isExistPartOfSearchValueInFixedArgs((Collection) condition.getFixedArg().getValue(), fixedArgValue);
        }
        if (!StandardOperation.EXISTS.equals(condition.getOperation()) && condition.getFixedArg() != null) {
            return StringUtils.containsIgnoreCase(String.valueOf(condition.getFixedArg().getValue()), fixedArgValue);
        }
        return false;
    }

    public static boolean changeFixedArgToNewValue(String oldFixedArgValue, String newFixedArgValue, Rule rule, Operation operation) {
        boolean isChanged = false;
        for (Condition condition : getAllConditions(rule)) {
            if(condition.getFixedArg() != null
                    && oldFixedArgValue.equals(condition.getFixedArg().getValue())
                    && operation.equals(condition.getOperation())) {
                condition.getFixedArg().setValue(newFixedArgValue);
                isChanged = true;
            }
        }
        return isChanged;
    }

    private static boolean isExistPartOfSearchValueInFixedArgs(Collection<Object> fixedArgs, String searchValue) {
        for (Object fixedArg : fixedArgs) {
            if(StringUtils.contains(fixedArg.toString().toLowerCase(), searchValue)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExistConditionByFreeArgAndFixedArg(Rule rule, String freeArg, String fixedArg) {
        boolean isExist = false;
        for (Condition condition : getAllConditions(rule)) {
            if (condition.getFixedArg() == null
                    || condition.getFreeArg() == null
                    || !StringUtils.equalsIgnoreCase(condition.getFreeArg().getName(), freeArg)) {
                continue;
            }
            if (condition.getFixedArg().getValue() instanceof Collection) {
                isExist = ((Collection) condition.getFixedArg().getValue()).contains(fixedArg);
            } else {
                isExist = StringUtils.equalsIgnoreCase((String) condition.getFixedArg().getValue(), fixedArg);
            }

            if (isExist) {
                break;
            }
        }

        return isExist;
    }

    public static boolean isExistConditionByFreeAndFixedArgParts(Rule rule, String freeArg, String fixedArg) {
        for (Condition condition : getAllConditions(rule)) {
            boolean isExist = false;
            if (condition.getFreeArg() != null
                    && StringUtils.containsIgnoreCase(condition.getFreeArg().getName(), freeArg)) {
                isExist = true;
            }
            if (condition.getFixedArg() == null) {
                return isExist;
            }
            if (condition.getFixedArg().getValue() instanceof Collection) {
                for (Object value : ((Collection) condition.getFixedArg().getValue())) {
                    if (StringUtils.containsIgnoreCase((String) value, fixedArg) && isExist) {
                        isExist = true;
                    }
                }
            } else {
                isExist = isExist && StringUtils.containsIgnoreCase(String.valueOf(condition.getFixedArg().getValue()), fixedArg);
            }

            if (isExist) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getFreeArgNames(Rule rule) {
        List<String> freeArgNames = new ArrayList<>();
        for (Condition condition : getAllConditions(rule)) {
            if (condition.getFreeArg() != null && StringUtils.isNotBlank(condition.getFreeArg().getName())) {
                freeArgNames.add(condition.getFreeArg().getName());
            }
        }
        return freeArgNames;
    }
}
