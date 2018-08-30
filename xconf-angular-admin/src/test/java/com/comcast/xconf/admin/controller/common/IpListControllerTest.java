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
 * Author: IpListControllerTest
 * Created: 7:34 PM
 */
package com.comcast.xconf.admin.controller.common;

import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.hesperius.dataaccess.core.exception.EntityConflictException;
import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IpListControllerTest extends BaseControllerTest {
    @Test
    public void getList() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        mockMvc.perform(get("/genericnamespacedlist/"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList))));
    }

    @Test
    public void getLists() throws Exception {
        GenericNamespacedList list1 = saveGenericList(createIpList("id1"));
        GenericNamespacedList list2 = saveGenericList(createIpList("id2"));
        GenericNamespacedList list3 = saveGenericList(createIpList("id3"));
        String expectedNumberOfItems = "3";
        List<GenericNamespacedList> expectedResult = Arrays.asList(list1, list2);

        MockHttpServletResponse response = mockMvc.perform(post("/genericnamespacedlist/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(new HashMap<String, String>()))
                .param("type", GenericNamespacedListTypes.IP_LIST)
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void getById() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        mockMvc.perform(get("/genericnamespacedlist/" + ipList.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(ipList)));
    }

    @Test
    public void getByType() throws Exception {
//        when(commonPermissionService.getPermissions()).thenReturn(Sets.newHashSet("read-common-stb", "write-common-stb", "read-firmware-stb", "write-firmware-stb", "read-dcm-stb", "write-dcm-stb", "read-telemetry-stb", "write-telemetry-stb"));

        GenericNamespacedList macList = createMacList();
        genericNamespacedListDAO.setOne(macList.getId(), macList);
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        Map<String, String> searchContext = Collections.singletonMap(SearchFields.TYPE, GenericNamespacedListTypes.IP_LIST);

        mockMvc.perform(post("/genericnamespacedlist/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(searchContext))
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Collections.singletonList(ipList))));
    }

    @Test
    public void getListIdsByType() throws Exception {
        GenericNamespacedList macList = createMacList();
        genericNamespacedListDAO.setOne(macList.getId(), macList);
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        mockMvc.perform(get("/genericnamespacedlist/" + GenericNamespacedListTypes.IP_LIST + "/ids"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList.getId()))));
    }

    @Test
    public void getAllListIds() throws Exception {
        GenericNamespacedList macList = createMacList();
        genericNamespacedListDAO.setOne(macList.getId(), macList);
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        mockMvc.perform(get("/genericnamespacedlist/ids"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(macList.getId(), ipList.getId()))));
    }

    @Test
    public void createList() throws Exception {
        GenericNamespacedList ipList = createIpList();

        mockMvc.perform(post("/genericnamespacedlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(ipList)))
                .andExpect(status().isCreated())
                .andExpect(content().json(CoreUtil.toJSON(ipList)));

        assertEquals(ipList, genericNamespacedListDAO.getOne(ipList.getId()));
    }

    @Test
    public void createListWithDuplicateIps() throws Exception {
        GenericNamespacedList ipList1 = createIpList();
        genericNamespacedListDAO.setOne(ipList1.getId(), ipList1);

        GenericNamespacedList ipList2 = createIpList();
        ipList2.setId("ipList2");

        mockMvc.perform(post("/genericnamespacedlist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(ipList2)))
                .andExpect(status().isCreated())
                .andExpect(content().json(CoreUtil.toJSON(ipList2)));
    }

    @Test
    public void updateList() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);
        ipList.setData(Sets.newHashSet("11.11.11.11"));

        mockMvc.perform(put("/genericnamespacedlist/" + ipList.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(ipList)))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(QueriesHelper.nullifyUnwantedFields(ipList))));

        assertEquals(QueriesHelper.nullifyUnwantedFields(ipList), QueriesHelper.nullifyUnwantedFields(genericNamespacedListDAO.getOne(ipList.getId())));
    }

    @Test
    public void deleteList() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        mockMvc.perform(delete("/genericnamespacedlist/" + ipList.getId()))
                .andExpect(status().isNoContent());
        assertNull(genericNamespacedListDAO.getOne(ipList.getId()));
    }

    @Test
    public void unableToDeleteListBecauseFormulaIsUsingIt() throws Exception {
        GenericNamespacedList ipList = saveGenericList(createIpList());
        String listId = ipList.getId();
        DCMGenericRule rule = saveFormula(createFormula(createCondition(listId, StbContext.IP_ADDRESS, RuleFactory.IN_LIST)));

        ResultActions resultActions = mockMvc.perform(delete("/genericnamespacedlist/" + ipList.getId()));

        assertException(resultActions, EntityConflictException.class, "List is used by Formula " + rule.getName());
    }

    @Test
    public void unableToDeleteListBecauseTelemetryRuleIsUsingIt() throws Exception {
        GenericNamespacedList ipList = saveGenericList(createIpList());
        String listId = ipList.getId();
        TelemetryRule rule = saveTelemetryRule(createTelemetryRule(createCondition(listId, StbContext.IP_ADDRESS, RuleFactory.IN_LIST)));

        ResultActions resultActions = mockMvc.perform(delete("/genericnamespacedlist/" + ipList.getId()));

        assertException(resultActions, EntityConflictException.class, "List is used by TelemetryRule " + rule.getName());
    }

    @Test
    public void unableToDeleteListBecauseFirmwareRuleIsUsingIt() throws Exception {
        GenericNamespacedList ipList = saveGenericList(createIpList());
        String listId = ipList.getId();
        FirmwareRule rule = saveFirmwareRule(createFirmwareRule(createCondition(listId, StbContext.IP_ADDRESS, RuleFactory.IN_LIST), TemplateNames.MAC_RULE));

        ResultActions resultActions = mockMvc.perform(delete("/genericnamespacedlist/" + ipList.getId()));

        assertException(resultActions, EntityConflictException.class, "List is used by FirmwareRule " + rule.getName());
    }

    @Test
    public void exportOne() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        MockHttpServletResponse response = mockMvc.perform(get("/genericnamespacedlist/" + ipList.getId()).param("export", "export"))
                .andExpect(status().isOk()).andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList)))).andReturn().getResponse();

        assertEquals(Sets.newHashSet("Content-Disposition", "Content-Type"), response.getHeaderNames());
    }

    @Test
    public void exportAll() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        MockHttpServletResponse response = mockMvc.perform(get("/genericnamespacedlist/all/" + GenericNamespacedListTypes.IP_LIST).param("export", "export"))
                .andReturn().getResponse();
        assertEquals(Sets.newHashSet("Content-Disposition", "Content-Type"), response.getHeaderNames());
    }

    @Test
    public void checkSorting() throws Exception {
        List<XMLPersistable> namespacedLists = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            GenericNamespacedList ipList = changeNamespacedListId(createIpList(), "ipListId" + i);
            genericNamespacedListDAO.setOne(ipList.getId(), ipList);
            namespacedLists.add(QueriesHelper.nullifyUnwantedFields(ipList));
        }
        mockMvc.perform(post("/genericnamespacedlist/filtered")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(new HashMap<String, String>()))
                        .param("type", GenericNamespacedListTypes.IP_LIST)
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(content().string(CoreUtil.toJSON(namespacedLists)))
                .andExpect(status().isOk());
    }

    @Test
    public void searchByContext() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);
        GenericNamespacedList ipList1 = createIpList();
        ipList1.setId("testId2");
        ipList1.setData(Sets.newHashSet("10.10.78.78"));
        genericNamespacedListDAO.setOne(ipList1.getId(), ipList1);

        //search by data
        String ipPart = "10.10";
        Map<String, String> context = new HashMap<>();
        context.put(SearchFields.DATA, ipPart);

        mockMvc.perform(post("/genericnamespacedlist/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(context))
                .param("type", ipList.getTypeName())
                .param("pageSize", "1")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList))));

        //search by name
        context.clear();
        context.put(SearchFields.NAME, "ip");

        mockMvc.perform(post("/genericnamespacedlist/filtered")
                .contentType(MediaType.APPLICATION_JSON).content(CoreUtil.toJSON(context))
                .param("type", ipList.getTypeName())
                .param("pageSize", "1")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList))));

        //search by name and data
        context.put(SearchFields.NAME, "test");
        context.put(SearchFields.DATA, "78.78");

        mockMvc.perform(post("/genericnamespacedlist/filtered")
                .contentType(MediaType.APPLICATION_JSON).content(CoreUtil.toJSON(context))
                .param("type", ipList.getTypeName())
                .param("pageSize", "1")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList1))));

        //search by name and data, returns two ips
        context.put(SearchFields.NAME, "t");
        context.put(SearchFields.DATA, "10");

        mockMvc.perform(post("/genericnamespacedlist/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("type", ipList.getTypeName()).content(CoreUtil.toJSON(context))
                .param("pageSize", "2")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(ipList, ipList1))));
    }

    @Test
    public void getIpAddressGroups() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        mockMvc.perform(get("/genericnamespacedlist/ipAddressGroups"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(GenericNamespacedListsConverter.convertToIpAddressGroup(ipList)))));
    }

    @Test
    public void renameIpList() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        FirmwareRuleTemplate ipRuleTemplate = createIpRuleTemplate(ipList.getId());
        firmwareRuleTemplateDao.setOne(ipRuleTemplate.getId(), ipRuleTemplate);

        FirmwareRule envModelRule = createEnvModelRule("envModelRuleName");
        firmwareRuleDao.setOne(envModelRule.getId(), envModelRule);

        FirmwareRule ipRule = createIpRule("ipRuleName", ipList.getId());
        ipRule.setId("ipRuleId");
        firmwareRuleDao.setOne(ipRule.getId(), ipRule);

        SettingProfile settingProfile = createSettingProfile("settingProfileName");
        settingProfileDao.setOne(settingProfile.getId(), settingProfile);

        SettingRule settingRule = createSettingIpRule("settingRuleName", settingProfile.getId(), ipList.getId());
        settingRuleDAO.setOne(settingRule.getId(), settingRule);

        TelemetryRule telemetryRule = createTelemetryIpRule("telemetryRuleName", ipList.getId());
        telemetryRuleDAO.setOne(telemetryRule.getId(), telemetryRule);

        DCMGenericRule dcmRule = createFormula(new Condition(RuleFactory.IP, RuleFactory.IN_LIST, FixedArg.from(ipList.getId())));
        dcmRuleDAO.setOne(dcmRule.getId(), dcmRule);

        String newIpListId = "newId";

        String savedIpListStr = mockMvc.perform(put("/genericnamespacedlist/" + newIpListId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(ipList)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        GenericNamespacedList savedIpList = mapper.readValue(savedIpListStr, GenericNamespacedList.class);
        savedIpList.setUpdated(null);
        ipList.setId(newIpListId);

        assertEquals(ipList, savedIpList);
        assertTrue(RuleUtil.isExistConditionByFixedArgValue(firmwareRuleDao.getOne(ipRule.getId()).getRule(), newIpListId));
        assertTrue(RuleUtil.isExistConditionByFixedArgValue(telemetryRuleDAO.getOne(telemetryRule.getId()), newIpListId));
        assertTrue(RuleUtil.isExistConditionByFixedArgValue(settingRuleDAO.getOne(settingRule.getId()).getRule(), newIpListId));
        assertTrue(RuleUtil.isExistConditionByFixedArgValue(dcmRuleDAO.getOne(dcmRule.getId()), newIpListId));
        assertTrue(RuleUtil.isExistConditionByFixedArgValue(firmwareRuleTemplateDao.getOne(ipRuleTemplate.getId()).getRule(), newIpListId));
    }

}
