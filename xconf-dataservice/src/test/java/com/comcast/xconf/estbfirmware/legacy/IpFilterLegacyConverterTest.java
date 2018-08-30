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
 */
package com.comcast.xconf.estbfirmware.legacy;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.IpFilter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class IpFilterLegacyConverterTest extends BaseQueriesControllerTest {

    @Autowired
    private IpFilterLegacyConverter ipFilterLegacyConverter;

    private String estbMacValue = "AA:AA:AA:AA:AA:AA";
    private String ipListName = "ipList";
    private String ipAddress = "10.10.10.10";

    @Test
    public void convertFirmwareRuleToIpFilterByMultipleRuleConditions() throws Exception {
        GenericNamespacedList ipList = createGenericNamespacedList(ipListName, GenericNamespacedListTypes.IP_LIST, ipAddress);
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);
        FirmwareRule firmwareRule = createIpRule();
        IpFilter ipFilter = ipFilterLegacyConverter.convertFirmwareRuleToIpFilter(createIpRule());

        assertEquals(firmwareRule.getId(), ipFilter.getId());
        assertEquals(firmwareRule.getName(), ipFilter.getName());
        assertEquals(Sets.newHashSet(new IpAddress(ipAddress)),ipFilter.getIpAddressGroup().getIpAddresses());
    }

    private FirmwareRule createIpRule() {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setId("firmwareRuleId");
        firmwareRule.setName("firmwareRuleName");
        firmwareRule.setType(FirmwareRule.RuleType.IP_RULE);
        firmwareRule.setCompoundParts(new ArrayList<Rule>());
        firmwareRule.getCompoundParts().add(createRule(null, RuleFactory.IP, RuleFactory.IN_LIST, ipListName));
        firmwareRule.getCompoundParts().add(createRule(Relation.AND, RuleFactory.MAC, RuleFactory.IN_LIST, estbMacValue));
        return firmwareRule;
    }
}