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
 * Author: ikostrov
 * Created: 28.08.15 16:03
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.hesperius.dataaccess.core.ValidationException;
import com.comcast.hesperius.dataaccess.core.dao.ISimpleCachedDAO;
import com.comcast.xconf.estbfirmware.converter.NgRuleConverter;
import com.comcast.xconf.estbfirmware.legacy.RebootImmediatelyLegacyConverter;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.PredicateManager;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RebootImmediatelyFilterService {

    @Autowired
    private NgRuleConverter ngRuleConverter;

    @Autowired
    private RebootImmediatelyLegacyConverter legacyConverter;

    @Autowired
    private ISimpleCachedDAO<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private PredicateManager predicateManager;

    public void save(RebootImmediatelyFilter filter, String applicationType) throws ValidationException {
        if (StringUtils.isBlank(filter.getId())) {
            String id = UUID.randomUUID().toString();
            filter.setId(id);
        }
        FirmwareRule rule = convertRebootFilterToFirmwareRule(filter);
        if (StringUtils.isNotBlank(applicationType)) {
            rule.setApplicationType(applicationType);
        }
        firmwareRuleDao.setOne(rule.getId(), rule);
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }

    public RebootImmediatelyFilter getOneRebootFilterFromDB(String id) {
        RebootImmediatelyFilter filter = null;
        FirmwareRule rule = firmwareRuleDao.getOne(id);
        if (rule != null) {
            filter = convertFirmwareRuleToRebootFilter(rule);
        }
        return filter;
    }

    public Set<RebootImmediatelyFilter> getAllRebootFiltersFromDB() {
        List<FirmwareRule> allRules = firmwareRuleDao.getAll();
        Set<RebootImmediatelyFilter> ipFilters = new TreeSet<>();

        for (FirmwareRule rule : allRules) {
            if (TemplateNames.REBOOT_IMMEDIATELY_FILTER.equals(rule.getType())) {
                ipFilters.add(convertFirmwareRuleToRebootFilter(rule));
            }
        }
        return ipFilters;
    }

    public Set<RebootImmediatelyFilter> getByApplicationType(String applicationType) {
        List<Predicate<FirmwareRule>> predicates = new ArrayList<>();
        predicates.add(predicateManager.new FirmwareRuleByTemplatePredicate(TemplateNames.REBOOT_IMMEDIATELY_FILTER));
        predicates.add(predicateManager.new ApplicationablePredicate<FirmwareRule>(applicationType));
        List<FirmwareRule> firmwareRules = Lists.newArrayList(Iterables.filter(firmwareRuleDao.getAll(), Predicates.and(predicates)));
        Set<RebootImmediatelyFilter> riFilters = new HashSet<>();
        for (FirmwareRule firmwareRule : firmwareRules) {
            riFilters.add(convertFirmwareRuleToRebootFilter(firmwareRule));
        }
        return riFilters;
    }

    public RebootImmediatelyFilter convertFirmwareRuleToRebootFilter(FirmwareRule firmwareRule) {

        return legacyConverter.convertFirmwareRuleToRebootFilter(ngRuleConverter.convertNew(firmwareRule));
    }

    public FirmwareRule convertRebootFilterToFirmwareRule(RebootImmediatelyFilter filter) {

        return ngRuleConverter.convertOld(legacyConverter.convertRebootFilterToFirmwareRule(filter));
    }

}
