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
 * Created: 3/21/2016
*/
package com.comcast.xconf.admin.service.setting;

import com.comcast.hesperius.dataaccess.core.dao.ISimpleCachedDAO;
import com.comcast.xconf.admin.validator.setting.SettingRuleValidator;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.search.PredicateManager;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SettingRuleService extends AbstractApplicationTypeAwareService<SettingRule> {

    @Autowired
    private ISimpleCachedDAO<String, SettingRule> settingRuleDAO;

    @Autowired
    private SettingRuleValidator validator;

    @Autowired
    private PredicateManager predicateManager;

    @Autowired
    private DcmPermissionService permissionService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public ISimpleCachedDAO<String, SettingRule> getEntityDAO() {
        return settingRuleDAO;
    }

    @Override
    public IValidator<SettingRule> getValidator() {
        return validator;
    }

    @Override
    protected void normalizeOnSave(SettingRule entity) {
        RuleUtil.normalizeConditions(entity.getRule());
    }

    @Override
    protected List<Predicate<SettingRule>> getPredicatesByContext(Map<String, String> context) {
        List<Predicate<SettingRule>> predicates = new ArrayList<>();
        if (context == null || context.isEmpty()) {
            return predicates;
        }

        Predicate<SettingRule> xRulePredicate = predicateManager.getXRulePredicate(context);
        if (xRulePredicate != null) {
            predicates.add(xRulePredicate);
        }
        if (context.containsKey(SearchFields.NAME)) {
            predicates.add(predicateManager.new XRuleNamePredicate<SettingRule>(context.get(SearchFields.NAME)));
        }
        return predicates;
    }
}
