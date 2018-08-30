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
 * Author: phoenix
 * Created: 04/03/2015  15:17
 */
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.hesperius.ruleengine.RuleEngine;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.IRuleProcessor;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hesperius.dataaccess.core.ValidationException;
import com.comcast.hesperius.dataaccess.core.config.ConfigurationProvider;
import com.comcast.hesperius.dataaccess.core.dao.ISimpleCachedDAO;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TimestampedRule;
import com.comcast.xconf.util.EvaluatorHelper;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class TelemetryProfileService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryProfileService.class);

    @Autowired
    private ISimpleCachedDAO<TimestampedRule, TelemetryProfile> temporaryTelemetryProfileDAO;
    @Autowired
    private ISimpleCachedDAO<String, PermanentTelemetryProfile> permanentTelemetryDAO;
    @Autowired
    private ISimpleCachedDAO<String, TelemetryRule> telemetryRuleDAO;

    private static final IRuleProcessor<Condition, Rule> processor = RuleEngine.getRuleProcessor();

    private static final long cacheUpdateWindowSize = ConfigurationProvider.getConfiguration().getCacheConfiguration().getTickDuration();

    public static final Function<TelemetryRule, TelemetryRule.PermanentTelemetryRuleDescriptor> convertToDescriptor = new Function<TelemetryRule, TelemetryRule.PermanentTelemetryRuleDescriptor>() {
        @Override
        public TelemetryRule.PermanentTelemetryRuleDescriptor apply(TelemetryRule input) {
            return new TelemetryRule.PermanentTelemetryRuleDescriptor(input.getId(), input.getName());
        }
    };

    public static final Function<TelemetryProfile, PermanentTelemetryProfile.TelemetryProfileDescriptor> convertToprofileDescriptor = new Function<TelemetryProfile, PermanentTelemetryProfile.TelemetryProfileDescriptor>() {
        @Override
        public PermanentTelemetryProfile.TelemetryProfileDescriptor apply(TelemetryProfile input) {
            return new PermanentTelemetryProfile.TelemetryProfileDescriptor(input.getName(), input.getId());
        }
    };

    public static long expireTime = 60000L;

//    @Scheduled(fixedRate = 60000L)
    @PostConstruct
    public void expireTemporaryTelemetryRules() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (final TimestampedRule expiredRule : Maps.filterValues(temporaryTelemetryProfileDAO.asLoadingCache().asMap(),
                        new Predicate<Optional<TelemetryProfile>>() {
                            @Override
                            public boolean apply(Optional<TelemetryProfile> input) {
                                return input.isPresent() && input.get().getExpires() + cacheUpdateWindowSize <= DateTime.now(DateTimeZone.UTC).getMillis();
                            }
                        }).keySet()) {
                    log.info("{} is expired, removing", expiredRule);
                    temporaryTelemetryProfileDAO.deleteOne(expiredRule);
                }
            }
        }, expireTime, expireTime);
    }


    public TimestampedRule createTelemetryProfile(final String contextAttribute, final String expectedValue, final TelemetryProfile telemetry) throws ValidationException {

        final TimestampedRule telemetryRule = createRuleForAttribute(contextAttribute, expectedValue);
        temporaryTelemetryProfileDAO.setOne(telemetryRule, telemetry);
        return telemetryRule;
    }

    public List<TelemetryProfile> dropTelemetryFor(final String contextAttribute, final String expectedValue) {
        Map<String, String> context = new HashMap<>();
        context.put(contextAttribute, expectedValue);
        final Set<TimestampedRule> matchedRules = getMatchedRules(context);
        List<TelemetryProfile> telemetry = new ArrayList<>();
        for (final TimestampedRule rule : matchedRules) {
            TelemetryProfile p = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(rule).orNull();
            if (p != null) {
                telemetry.add(p);
                log.info("removing temporary rule: {}", rule);
                temporaryTelemetryProfileDAO.deleteOne(rule);
            }
        }
        return telemetry;
    }

    public TelemetryProfile getTemporaryProfileForContext(final Map<String, String> context) {

        final Set<TimestampedRule> rules = Sets.newTreeSet(Ordering.from(new Comparator<TimestampedRule>() {
            @Override
            public int compare(TimestampedRule r1, TimestampedRule r2) {

                return Longs.compare(r1.getTimestamp(), r2.getTimestamp());
            }
        }).reverse());
        Iterables.addAll(rules, processor.filter(temporaryTelemetryProfileDAO.asLoadingCache().asMap().keySet(), context));

        if (rules.isEmpty()) return null;

        final Iterator<TimestampedRule> sortedRulesIterator = rules.iterator();
        TelemetryProfile telemetry = null;
        while (sortedRulesIterator.hasNext()) {
            final TimestampedRule rule = sortedRulesIterator.next();
            telemetry = temporaryTelemetryProfileDAO.asLoadingCache().asMap().get(rule).orNull();
            if (telemetry != null && ((telemetry.getExpires() + cacheUpdateWindowSize) > DateTime.now(DateTimeZone.UTC).getMillis())) {
                break;
            }
        }

        for(final TimestampedRule rule : rules) {
            temporaryTelemetryProfileDAO.deleteOne(rule);       // deleting everything found as per XAPPS-2536
        }

        return telemetry;
    }

    private HashSet<TimestampedRule> getMatchedRules(Map<String, String> context) {
        return Sets.newHashSet(processor.filter(temporaryTelemetryProfileDAO.asLoadingCache().asMap().keySet(), context));
    }

    public PermanentTelemetryProfile getPermanentProfileForContext(final Map<String, String> context) {
        TelemetryRule telemetryRule = getTelemetryRuleForContext(context);
        return getPermanentProfileByTelemetryRule(telemetryRule);
    }

    public TelemetryRule getTelemetryRuleForContext(final Map<String, String> context) {
        return EvaluatorHelper.getEntityRuleForContext(Optional
                .presentInstances(telemetryRuleDAO.asLoadingCache().asMap().values()), context);
    }

    public PermanentTelemetryProfile getPermanentProfileByTelemetryRule(TelemetryRule telemetryRule) {
        return telemetryRule != null && StringUtils.isNotBlank(telemetryRule.getBoundTelemetryId())
                ? permanentTelemetryDAO.getOne(telemetryRule.getBoundTelemetryId()) : null;
    }

    public TelemetryProfile getTelemetryForContext(final Map<String, String> context) {
        return ObjectUtils.firstNonNull(getTemporaryProfileForContext(context), getPermanentProfileForContext(context));
    }

    public List<TelemetryRule.PermanentTelemetryRuleDescriptor> getAvailableDescriptors(String applicationType) {
        final LinkedList<TelemetryRule.PermanentTelemetryRuleDescriptor> descriptors = new LinkedList<>();
        for (final Optional<TelemetryRule> rule : telemetryRuleDAO.asLoadingCache().asMap().values()) {
            if (rule.isPresent() && ApplicationType.equals(rule.get().getApplicationType(), applicationType)) {
                descriptors.add(convertToDescriptor.apply(rule.get()));
            }
        }
        return descriptors;
    }

    public List<PermanentTelemetryProfile.TelemetryProfileDescriptor> getAvailableProfileDescriptors(String applicationType) {
        final LinkedList<PermanentTelemetryProfile.TelemetryProfileDescriptor> descriptors = new LinkedList<>();
        for (final Optional<PermanentTelemetryProfile> telemetry : permanentTelemetryDAO.asLoadingCache().asMap().values()) {
            if (telemetry.isPresent() && ApplicationType.equals(telemetry.get().getApplicationType(), applicationType)) {
                descriptors.add(convertToprofileDescriptor.apply(telemetry.get()));
            }
        }
        return descriptors;
    }

    public TimestampedRule createRuleForAttribute(final String contextAttribute, final String expectedValue) {
        return Rule.Builder.of(
                new Condition(
                        new FreeArg(StandardFreeArgType.STRING, contextAttribute),
                        StandardOperation.IS,
                        FixedArg.from(expectedValue)))
                .copyTo(new TimestampedRule());
    }
}
