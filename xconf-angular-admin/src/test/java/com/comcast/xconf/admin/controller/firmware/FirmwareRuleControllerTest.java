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
 *  Author: mdolina
 *  Created: 12/9/15 4:35 PM
 */

package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Relation;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hesperius.dataaccess.core.ValidationException;
import com.comcast.hesperius.dataaccess.core.exception.EntityConflictException;
import com.comcast.hesperius.dataaccess.core.exception.EntityNotFoundException;
import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.estbfirmware.factory.TemplateFactory;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FirmwareRuleControllerTest extends BaseControllerTest {

    @Autowired
    private TemplateFactory templateFactory;

    @Test
    public void getFirmwareRule() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        mockMvc.perform(get("/firmwarerule/" + firmwareRule.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(firmwareRule)));
    }

    @Test
    public void testCreateUpdate() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setType(firmwareRuleTemplate.getId());
        mockMvc.perform(post("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRule)))
                .andExpect(status().isCreated());
    }

    @Test
    public void creatingReturnsValidationException() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule("id", ApplicableAction.Type.RULE);
        firmwareRule.getRule().getCondition().setOperation(null);

        ResultActions resultActions = mockMvc.perform(post("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRule)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void createRuleNormalizeCondition() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setRule(createEnvPartnerRule("   envFixedArg  ", "partnerId"));

        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate(firmwareRule.getType());
        firmwareRuleTemplate.setRule(createEnvPartnerRule("envFixedArg", "partnerId"));
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

         String actualResult = mockMvc.perform(post("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRule))).andReturn().getResponse().getContentAsString();

        assertConditionWasNormalized(actualResult);
    }

    @Test
    public void createRuleValidateActionReturnsException() throws Exception {
        FirmwareRule firmwareRule = createRebootImmediatelyFirmwareRuleAndTemplate("true");

        MvcResult mvcResult = mockMvc.perform(post("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRule))).andReturn();

        assertValidationException("ipAddress is empty", mvcResult);
    }

    @Test
    public void update() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);

        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        firmwareRule.getRule().getCondition().setFixedArg(FixedArg.from("changedValue"));

        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate(firmwareRule.getType());
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        mockMvc.perform(put("/firmwarerule").contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRule)))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(firmwareRule)));
    }

    @Test
    public void updateWithoutId() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setId(null);
        mockMvc.perform(put("/firmwarerule").contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRule)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("FirmwareRule id is empty"));
    }

    @Test
    public void updatingReturnsValidationException() throws Exception {
        String id = "id";
        FirmwareRule firmwareRule = createAndSaveFirmwareRule(id, ApplicableAction.Type.RULE);
        FirmwareRule firmwareRuleToUpdate = createFirmwareRule(id, ApplicableAction.Type.RULE);
        firmwareRuleToUpdate.getRule().getCondition().setOperation(null);

        ResultActions resultActions = mockMvc.perform(put("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRuleToUpdate)))
                .andExpect(status().isBadRequest());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(RuleValidationException.class, exception.getClass());
        assertEquals("Operation is null", exception.getMessage());
    }

    @Test
    public void updatingReturnsEntityConflictException() throws Exception {
        String id = "id";
        String notUniqueName = "notUniqueName";
        FirmwareRule firmwareRule = createAndSaveFirmwareRule(id, ApplicableAction.Type.RULE);
        FirmwareRule firmwareRuleNotUniqueName = createAndSaveFirmwareRule("someId", ApplicableAction.Type.RULE);
        firmwareRuleNotUniqueName.setName(notUniqueName);
        firmwareRuleNotUniqueName.getRule().getCondition().setFixedArg(FixedArg.from("someUniqueFixedArg"));
        FirmwareRule firmwareRuleToUpdate = createFirmwareRule(id, ApplicableAction.Type.RULE);
        firmwareRuleToUpdate.setName(notUniqueName);

        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate(firmwareRuleToUpdate.getType());
        firmwareRuleTemplate.setRule(firmwareRuleToUpdate.getRule());
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        ResultActions resultActions = mockMvc.perform(put("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRuleToUpdate)))
                .andExpect(status().isConflict());

        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(EntityConflictException.class, exception.getClass());
        assertEquals("Name is already used", exception.getMessage());
    }

    @Test
    public void updateRuleNormalizeCondition() throws Exception {
        Rule normalizedRule = createEnvPartnerRule("ENVFIXEDARG", "PARTNERID");
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setRule(normalizedRule);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate("firmwarerule");
        firmwareRuleTemplate.setRule(normalizedRule);
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        FirmwareRule firmwareRuleToUpdate = createFirmwareRule();
        firmwareRuleToUpdate.setType(firmwareRuleTemplate.getId());
        firmwareRuleToUpdate.setRule(createEnvPartnerRule("  envFixedArg ", "partnerId"));

        String actualResult = mockMvc.perform(put("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRuleToUpdate))).andReturn().getResponse().getContentAsString();

        assertConditionWasNormalized(actualResult);
    }

    @Test
    public void updateRuleValidateActionReturnsException() throws Exception {
        FirmwareRule firmwareRule = createRebootImmediatelyFirmwareRuleAndTemplate("false");
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        FirmwareRule firmwareRuleToUpdate = createRebootImmediatelyFirmwareRuleAndTemplate("true");

        MvcResult mvcResult = mockMvc.perform(put("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(firmwareRuleToUpdate))).andReturn();

        assertValidationException("ipAddress is empty", mvcResult);
    }

    @Test
    public void deleteFirmwareRule() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        mockMvc.perform(delete("/firmwarerule/" + firmwareRule.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/firmwarerule/" + firmwareRule.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getIdToNamesMapByType() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        Map<String, String> firmwareRuleIdName = new HashMap<>();
        firmwareRuleIdName.put(firmwareRule.getId(), firmwareRule.getName());
        mockMvc.perform(get("/firmwarerule/" + firmwareRule.getType() + "/names"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(firmwareRuleIdName)));
    }

    @Test
    public void getAllFirmwareRules() throws Exception {
        Integer numberOfRulesOfRuleType = 4;
        Integer numberOfRulesOfDefinePropertiesType = 2;
        Integer numberOfRulesOfBlockingFilterType = 8;
        ApplicableAction.Type typeForQuery = ApplicableAction.Type.BLOCKING_FILTER;
        createAndSaveFirmwareRulesOfRuleType(numberOfRulesOfRuleType);
        createAndSaveFirmwareRulesOfDefinePropertiesType(numberOfRulesOfDefinePropertiesType);
        List<FirmwareRule> rulesOfBlockingFilterType = createAndSaveFirmwareRulesOfBlockingFilterTypeType(numberOfRulesOfBlockingFilterType);
        Map<String, String> searchContext = Collections.singletonMap(SearchFields.APPLICABLE_ACTION_TYPE, typeForQuery.name());
        MockHttpServletResponse result = mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON).content(CoreUtil.toJSON(searchContext))
                .param("pageSize", "100")
                .param("pageNumber", "1"))
                .andExpect(status().isOk()).andReturn().getResponse();

        String actualContent = result.getContentAsString();
        String expectedContent = CoreUtil.toJSON(rulesOfBlockingFilterType);

        JSONAssert.assertEquals(expectedContent, actualContent, false);
        assertEquals(numberOfRulesOfRuleType.toString(), result.getHeaderValue(ApplicableAction.Type.RULE.toString()).toString());
        assertEquals(numberOfRulesOfDefinePropertiesType.toString(), result.getHeaderValue(ApplicableAction.Type.DEFINE_PROPERTIES.toString()).toString());
        assertEquals(numberOfRulesOfBlockingFilterType.toString(), result.getHeaderValue(ApplicableAction.Type.BLOCKING_FILTER.toString()).toString());
    }

    @Test
    public void getAllFirmwareRulesFilteredByTemplate() throws Exception {
        String templateNameToFilterBy = "templateToFilterBy";
        createAndSaveFirmwareRule("ruleTypeRule", ApplicableAction.Type.RULE);
        createAndSaveFirmwareRule("ruleTypeDefineProperties", ApplicableAction.Type.DEFINE_PROPERTIES);
        createAndSaveFirmwareRule("ruleTypeBlockingFilter", ApplicableAction.Type.BLOCKING_FILTER);
        FirmwareRule ruleTypeRuleTemplateToFilterBy = createAndSaveFirmwareRule("ruleToFind", ApplicableAction.Type.RULE, templateNameToFilterBy);

        Map<String, String> searchContext = Collections.singletonMap(SearchFields.TEMPLATE_ID, templateNameToFilterBy);

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(searchContext))
                .param("pageSize", "100")
                .param("pageNumber", "1")).andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Collections.singletonList(ruleTypeRuleTemplateToFilterBy))));
    }

    @Test
    public void checkSorting() throws Exception {
        List<FirmwareRule> firmwareRules = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            FirmwareRule firmwareRule = changeIdAndName(createFirmwareRule(), "firmwareRuleId" + i, "firmwareRuleName" + i);
            firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
            firmwareRules.add(firmwareRule);
        }

        mockMvc.perform(post("/firmwarerule/filtered")
                .param("pageSize", "100")
                .param("pageNumber", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(firmwareRules)));
    }

    @Test
    public void exportFirmwareRule() throws Exception {
        final String name = "forExport";
        final String id = "id";
        final FirmwareRule ruleToBeExported = createAndSaveFirmwareRule(id, name);
        final List<FirmwareRule> expectedResult = Lists.newArrayList(ruleToBeExported);

        final MockHttpServletResponse response = mockMvc.perform(get("/firmwarerule/" + id).param("export", ""))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(expectedResult))).andReturn().getResponse();

        final Object actualContentDisposition = response.getHeaderValue("Content-Disposition");
        final String expectedContentDisposition = "attachment; filename=firmwareRule_" + id + "_" + ApplicationType.get(ruleToBeExported.getApplicationType()) +".json";

        assertEquals(expectedContentDisposition, actualContentDisposition);
    }

    @Test
    public void exportFirmwareRuleNegative() throws Exception {
        final String nonExistentId = "someId";

        final ResultActions resultActions = mockMvc.perform(get("/firmwarerule/" + nonExistentId).param("export", ""));
        resultActions.andExpect(status().isNotFound());

        final Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals("Entity with id: " + nonExistentId + " does not exist", exception.getMessage());
        assertEquals(EntityNotFoundException.class, exception.getClass());
    }

    @Test
    public void exportAllFirmwareRules() throws Exception {
        final String name = "forExport";
        final List<FirmwareRule> expectedResult = Lists.newArrayList(createAndSaveFirmwareRules(name, 42));

        final MockHttpServletResponse response = mockMvc.perform(get("/firmwarerule/").param("export", ""))
                .andExpect(status().isOk()).andReturn().getResponse();

        final String expectedContentDisposition = "attachment; filename=allFirmwareRules.json";
        final String actualResult = response.getContentAsString();
        final Object actualContentDisposition = response.getHeaderValue("Content-Disposition");
        JSONAssert.assertEquals(CoreUtil.toJSON(expectedResult), actualResult, false);
        assertEquals(expectedContentDisposition, actualContentDisposition);
    }

    @Test
    public void exportAllFirmwareRulesByType() throws Exception {
        final String name = "forExport";
        final List<FirmwareRule> expectedResult = Lists.newArrayList(createAndSaveFirmwareRules(name, 42));

        final MockHttpServletResponse response = mockMvc.perform(get("/firmwarerule/export/byType")
                .param("exportAll", "exportAll")
                .param("type", "RULE"))
                .andExpect(status().isOk()).andReturn().getResponse();

        final String expectedContentDisposition = "attachment; filename=allFirmwareRules_RULE_ACTION_stb.json";
        final String actualResult = response.getContentAsString();
        final Object actualContentDisposition = response.getHeaderValue("Content-Disposition");
        JSONAssert.assertEquals(CoreUtil.toJSON(expectedResult), actualResult, false);
        assertEquals(expectedContentDisposition, actualContentDisposition);
    }

    @Test
    public void getFirmwareRuleNamesByType() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule("id123", ApplicableAction.Type.RULE);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);
        mockMvc.perform(get("/firmwarerule/byTemplate/{templateId}/names", firmwareRule.getType()))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(firmwareRule.getName()))));
    }

    @Test
    public void searchFirmwareRuleByFreeAndFixedArg() throws Exception {
        FirmwareRule firmwareRule = createFirmwareRule("id123", ApplicableAction.Type.RULE);
        Rule rule = new Rule();
        List<Rule> compoundParts = new ArrayList<>();
        compoundParts.add(createRule(createCondition("ABCD", "model", StandardOperation.IS)));
        Rule rule1 = createRule(createCondition("ABCD", "env", StandardOperation.IS));
        rule1.setRelation(Relation.AND);
        compoundParts.add(rule1);
        rule.setCompoundParts(compoundParts);
        firmwareRule.setRule(rule);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        FirmwareRule firmwareRule1 = createFirmwareRule("id1234", ApplicableAction.Type.RULE);
        firmwareRule1.getRule().setCondition(createCondition("EFGH", "model", StandardOperation.IS));
        firmwareRuleDao.setOne(firmwareRule1.getId(), firmwareRule1);

        Map<String, String> searchContext = new HashMap<>();
        searchContext.put(SearchFields.FREE_ARG, "model");

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(CoreUtil.toJSON(searchContext)))
                .andExpect(status().isOk());

        searchContext.put(SearchFields.FIXED_ARG, "ABCD");

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(CoreUtil.toJSON(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(firmwareRule))));

        searchContext.remove(SearchFields.FREE_ARG);

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(CoreUtil.toJSON(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(firmwareRule))));

        searchContext.put(SearchFields.FREE_ARG, "model");
        searchContext.put(SearchFields.FIXED_ARG, "wrongModelId");

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "1")
                .param("pageNumber", "1")
                .content(CoreUtil.toJSON(searchContext)))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList())));

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(firmwareRule, firmwareRule1))));
    }

    @Test
    public void searchFirmwareRuleByFirmwareConfigDescription() throws Exception {
        FirmwareConfig firmwareConfig1 = createFirmwareConfig(UUID.randomUUID().toString(), "version1", Sets.newHashSet("MODELID"), "description1");
        firmwareConfigDAO.setOne(firmwareConfig1.getId(), firmwareConfig1);
        FirmwareRule firmwareRule1 = createFirmwareRule(createCondition("MODELID", "model", StandardOperation.IS), "templateId");
        firmwareRule1.setId(UUID.randomUUID().toString());
        firmwareRule1.setApplicableAction(createRuleAction(firmwareConfig1.getId()));
        firmwareRuleDao.setOne(firmwareRule1.getId(), firmwareRule1);

        FirmwareConfig firmwareConfig2 = createFirmwareConfig(UUID.randomUUID().toString(), "version2", Sets.newHashSet("MODELID"), "description2");
        firmwareConfigDAO.setOne(firmwareConfig2.getId(), firmwareConfig2);
        FirmwareRule firmwareRule2 = createFirmwareRule(createCondition("ENVID", "env", StandardOperation.IS), "templateId");
        firmwareRule2.setId(UUID.randomUUID().toString());
        firmwareRule2.setApplicableAction(createRuleAction(firmwareConfig2.getId()));
        firmwareRuleDao.setOne(firmwareRule2.getId(), firmwareRule2);

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(CoreUtil.toJSON(Collections.singletonMap(SearchFields.FIRMWARE_VERSION, firmwareConfig1.getDescription()))))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(firmwareRule1))));

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10")
                .param("pageNumber", "1")
                .content(CoreUtil.toJSON(Collections.singletonMap(SearchFields.FIRMWARE_VERSION, "wrongDescription"))))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(new ArrayList<>())));
    }

    private FirmwareRule createRebootImmediatelyFirmwareRuleAndTemplate(final String rebootImmediatelyValue) throws ValidationException {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setType(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
        FirmwareRuleTemplate rebootImmediatelyFilterTemplate = createAndSaveRebootImmediatelyFilterTemplate();
        firmwareRule.setRule(rebootImmediatelyFilterTemplate.getRule());
        DefinePropertiesAction action = new DefinePropertiesAction();
        action.setActionType(ApplicableAction.Type.DEFINE_PROPERTIES);
        action.setProperties(new HashMap<>(Collections.singletonMap(ConfigNames.REBOOT_IMMEDIATELY, rebootImmediatelyValue)));
        firmwareRule.setApplicableAction(action);
        return firmwareRule;
    }

    private FirmwareRuleTemplate createAndSaveRebootImmediatelyFilterTemplate() throws ValidationException {
        FirmwareRuleTemplate template = templateFactory.createRiFilterTemplate();
        firmwareRuleTemplateDao.setOne(template.getId(), template);

        return template;
    }

    @Test
    public void createRulesWithTheSameNamesButDifferentTypes() throws Exception {
        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);
        String ruleName = "ruleName";
        FirmwareRule firmwareRule = createFirmwareRule(new Condition(RuleFactory.IP, RuleFactory.IN_LIST, FixedArg.from(ipList.getId())), TemplateNames.IP_RULE);
        firmwareRule.setName(ruleName);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate(firmwareRule.getType());
        firmwareRuleTemplate.setRule(firmwareRule.getRule());
        firmwareRuleTemplateDao.setOne(firmwareRuleTemplate.getId(), firmwareRuleTemplate);

        FirmwareRule customRule = createFirmwareRule(new Condition(RuleFactory.ENV, StandardOperation.IS, FixedArg.from("testEnv")), "CUSTOM_TEMPLATE");
        customRule.setId("anotherRuleId");
        customRule.setName(ruleName);

        FirmwareRuleTemplate templateForCustomRule = createFirmwareRuleTemplate(customRule.getType());
        templateForCustomRule.setRule(customRule.getRule());
        firmwareRuleTemplateDao.setOne(templateForCustomRule.getId(), templateForCustomRule);

        mockMvc.perform(post("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(customRule)))
                .andExpect(status().isCreated());
    }

    @Test
    public void updateAndRenameFirmwareRule() throws Exception {
        String envModelRuleName = "envModelRuleName";
        FirmwareRule envModelRule = createEnvModelRule(envModelRuleName);
        firmwareRuleDao.setOne(envModelRule.getId(), envModelRule);

        FirmwareRuleTemplate envModelTemplate = createFirmwareRuleTemplate(envModelRule.getType());
        envModelTemplate.setRule(envModelRule.getRule());
        firmwareRuleTemplateDao.setOne(envModelTemplate.getId(), envModelTemplate);

        GenericNamespacedList ipList = createIpList();
        genericNamespacedListDAO.setOne(ipList.getId(), ipList);

        String newRuleName = "newName";
        envModelRule = createEnvModelRule(newRuleName);

        mockMvc.perform(put("/firmwarerule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(envModelRule)))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(envModelRule)));
    }

    @Test
    public void findFirmwareRuleByTemplate() throws Exception {
        FirmwareRule envModelRule = createAndSaveFirmwareRule(UUID.randomUUID().toString(), ApplicableAction.Type.RULE, TemplateNames.ENV_MODEL_RULE);
        createAndSaveFirmwareRule(UUID.randomUUID().toString(), ApplicableAction.Type.RULE, "test_" + TemplateNames.ENV_MODEL_RULE);
        Map<String, String> searchContext = Collections.singletonMap(SearchFields.TEMPLATE_ID, TemplateNames.ENV_MODEL_RULE);

        mockMvc.perform(post("/firmwarerule/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(searchContext))
                .param("pageSize", "100")
                .param("pageNumber", "1")).andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Collections.singletonList(envModelRule))));
    }

    private FirmwareRule createAndSaveFirmwareRule(String id, ApplicableAction.Type type) throws ValidationException {
        FirmwareRule result = createFirmwareRule(id, type);
        firmwareRuleDao.setOne(result.getId(), result);

        return result;
    }

    private FirmwareRule createAndSaveFirmwareRule(String id, String name) throws ValidationException {
        FirmwareRule result = createFirmwareRule(id, ApplicableAction.Type.RULE);
        result.setName(name);
        firmwareRuleDao.setOne(result.getId(), result);

        return result;
    }

    private List<FirmwareRule> createAndSaveFirmwareRules(String namePattern, int number) throws ValidationException {
        List<FirmwareRule> result = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            result.add(createAndSaveFirmwareRule(String.valueOf(i), namePattern + i));
        }

        return result;
    }

    private FirmwareRule createAndSaveFirmwareRule(String id, ApplicableAction.Type actionType, String templateName) throws ValidationException {
        FirmwareRule result = createFirmwareRule(id, actionType);
        result.setType(templateName);
        firmwareRuleDao.setOne(result.getId(), result);

        return result;
    }

    private FirmwareRule createFirmwareRule(String id, ApplicableAction.Type type) throws ValidationException {
        FirmwareRule result = createFirmwareRule();
        result.setId(id);
        result.setName(id + "name");
        result.getApplicableAction().setActionType(type);

        return result;
    }

    private List<FirmwareRule> createAndSaveFirmwareRules(String idPart, ApplicableAction.Type type, Integer numberOfRulesToCreate) throws ValidationException {
        List<FirmwareRule> result = new ArrayList<>();
        for (int i = 0; i < numberOfRulesToCreate; i++) {
            result.add(createAndSaveFirmwareRule(idPart + i, type));
        }

        return result;
    }

    private List<FirmwareRule> createAndSaveFirmwareRulesOfRuleType(Integer numberOfRulesToCreate) throws ValidationException {
        return createAndSaveFirmwareRules("ruleType", ApplicableAction.Type.RULE, numberOfRulesToCreate);
    }

    private List<FirmwareRule> createAndSaveFirmwareRulesOfDefinePropertiesType(Integer numberOfRulesToCreate) throws ValidationException {
        return createAndSaveFirmwareRules("definePropertiesType", ApplicableAction.Type.DEFINE_PROPERTIES, numberOfRulesToCreate);
    }

    private List<FirmwareRule> createAndSaveFirmwareRulesOfBlockingFilterTypeType(Integer numberOfRulesToCreate) throws ValidationException {
        return createAndSaveFirmwareRules("blockingFilterType", ApplicableAction.Type.BLOCKING_FILTER, numberOfRulesToCreate);
    }

    private FirmwareRule changeIdAndName(FirmwareRule firmwareRule, String id, String name) {
        firmwareRule.setId(id);
        firmwareRule.setName(name);
        return firmwareRule;
    }

    private void assertConditionWasNormalized(String actualResult) throws Exception {
        FirmwareRule expectedResult = createFirmwareRule();
        expectedResult.setId(null);
        expectedResult.setRule(createEnvPartnerRule("ENVFIXEDARG", "PARTNERID"));
        JSONAssert.assertEquals(CoreUtil.toJSON(expectedResult), actualResult, false);
    }

    private void assertValidationException(String expectedMessage, MvcResult mvcResult) {
        assertEquals(RuleValidationException.class, mvcResult.getResolvedException().getClass());
        assertEquals(expectedMessage, mvcResult.getResolvedException().getMessage());
    }
 }