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
 *  Created: 11/30/15 4:15 PM
 */

package com.comcast.xconf.admin.controller;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.main.api.*;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.hesperius.dataaccess.core.ValidationException;
import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.*;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.settings.SettingType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.mockito.internal.matchers.EndsWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;
import java.util.*;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.IS;
import static com.comcast.xconf.estbfirmware.factory.RuleFactory.ENV;
import static com.comcast.xconf.firmware.ApplicationType.STB;
import static com.comcast.xconf.firmware.ApplicationType.XHOME;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class BaseControllerTest extends BaseIntegrationTest {

    protected Cookie stbCookie = new Cookie("applicationType", STB);
    protected Cookie xhomeCookie = new Cookie("applicationType", ApplicationType.XHOME);

    protected void performRequestAndVerifyResponse(String path, Object obj) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String expectedContent = CoreUtil.toJSON(obj);
        mockMvc.perform(
                get(path).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedContent)).andReturn();
    }

    protected void performRequestAndVerifyResponse(String path, String uriVariable, Object obj) throws Exception {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String expectedContent = CoreUtil.toJSON(obj);
        mockMvc.perform(
                get(path, uriVariable).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedContent));
    }

    protected ResultActions performGetRequest(final String path, final Map<String, String> params) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = get(path).contentType(MediaType.APPLICATION_JSON);
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                requestBuilder.param(param.getKey(), param.getValue());
            }
        }

        return mockMvc.perform(requestBuilder);
    }

    protected ResultActions performGetRequestAndVerifyResponse(final String path, final Map<String, String> params, final Object obj) throws Exception {
        return performGetRequest(path, params).andExpect(content().string(CoreUtil.toJSON(obj)));
    }

    protected ResultActions performExportRequestAndVerifyResponse(final String path, final Object obj, String applicationType) throws Exception {
        String applicationTypeSuffix = "_" + applicationType + ".json";
        Map<String, String> params = Collections.singletonMap("export", "export");
        return performGetRequest(path, params)
                .andExpect(header().string("Content-Disposition", new EndsWith(applicationTypeSuffix)))
                .andExpect(content().string(CoreUtil.toJSON(obj)));
    }

    protected ResultActions performPostRequest(String path, Object obj) throws Exception {
        return mockMvc.perform(post("/" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(obj)));
    }

    protected ResultActions performPostRequest(String path, Cookie cookie, Object obj) throws Exception {
        return mockMvc.perform(post("/" + path)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(obj)));
    }

    protected void performPostRequestAndVerify(String path, Object obj) throws Exception {
        mockMvc.perform(post("/" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(obj)))
                .andExpect(status().isCreated());
    }

    protected ResultActions performPutRequest(String path, Object obj) throws Exception {
        return mockMvc.perform(put("/" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(obj)));
    }

    protected void performPutRequestAndVerify(String path, Object obj) throws Exception {
        performPutRequest(path, obj).andExpect(status().isOk());
    }

    protected ResultActions performDeleteRequest(String path) throws Exception {
        return mockMvc.perform(delete("/" + path)
                .contentType(MediaType.APPLICATION_JSON));
    }

    protected void performDeleteRequestAndVerify(String path) throws Exception {
        performDeleteRequest(path).andExpect(status().isNoContent());
    }

    protected void validateNotFoundException(ResultActions resultActions, String id) throws Exception {
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("type").value("EntityNotFoundException"))
                .andExpect(jsonPath("message").value("Entity with id: " + id + " does not exist"));

    }

    protected void validateEntityExistsException(ResultActions resultActions, String id) throws Exception {
        resultActions.andExpect(status().isConflict())
                .andExpect(jsonPath("status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("type").value("EntityExistsException"))
                .andExpect(jsonPath("message").value("Entity with id: " + id + " already exists"));
    }

    protected Environment createEnvironment() {
        return createEnvironment("environmentId");
    }

    protected Environment createEnvironment(String id) {
        Environment environment = new Environment();
        environment.setId(id);
        environment.setDescription("test description");
        return environment;
    }

    protected Environment saveEnvironment(Environment environment) throws ValidationException {
        environmentDAO.setOne(environment.getId(), environment);

        return environment;
    }

    protected Model createModel() {
        return createModel("modelId");
    }

    protected Model createModel(String id) {
        Model model = new Model();
        model.setId(id);
        model.setDescription("test description");
        return model;
    }

    protected Model saveModel(Model model) throws ValidationException {
        modelDAO.setOne(model.getId(), model);

        return model;
    }

    protected LogFile createLogFile() {
        LogFile logFile = new LogFile();
        logFile.setId("testLogFileId");
        logFile.setName("logFileName");
        logFile.setDeleteOnUpload(true);
        return logFile;
    }

    protected UploadRepository createUploadRepository() {
        UploadRepository uploadRepository = new UploadRepository();
        uploadRepository.setId("testId");
        uploadRepository.setName("testName");
        uploadRepository.setDescription("testDescription");
        uploadRepository.setProtocol(UploadProtocol.HTTP);
        uploadRepository.setUrl("http://test.com");
        return uploadRepository;
    }

    protected UploadRepository createUploadRepository(String id) {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepository.setId(id);
        return uploadRepository;
    }

    protected DeviceSettings createDeviceSettings(String id) {
        DeviceSettings deviceSettings = createDeviceSettings();
        deviceSettings.setId(id);
        return deviceSettings;
    }

    protected DeviceSettings createDeviceSettings() {
        DeviceSettings deviceSettings = new DeviceSettings();
        deviceSettings.setId("testId");
        deviceSettings.setName("testName");
        deviceSettings.setSettingsAreActive(true);
        deviceSettings.setSchedule(createSchedule());
        deviceSettings.setCheckOnReboot(true);
        deviceSettings.setConfigurationServiceURL(createConfigurationServiceURL());
        return deviceSettings;
    }

    protected ConfigurationServiceURL createConfigurationServiceURL() {
        ConfigurationServiceURL configurationServiceURL = new ConfigurationServiceURL("configId", "name", "description", "http://test.com");
        return configurationServiceURL;
    }

    protected LogUploadSettings createLogUploadSettings(String id) {
        LogUploadSettings logUploadSettings = createLogUploadSettings();
        logUploadSettings.setId(id);
        return logUploadSettings;
    }

    protected LogUploadSettings createLogUploadSettings() {
        LogUploadSettings logUploadSettings = new LogUploadSettings();
        logUploadSettings.setId("testId");
        logUploadSettings.setName("logUploadSettingsName");
        logUploadSettings.setActiveDateTimeRange(false);
        logUploadSettings.setUploadOnReboot(false);
        logUploadSettings.setAreSettingsActive(true);
        logUploadSettings.setUploadRepositoryId(createUploadRepository().getId());
        logUploadSettings.setNumberOfDays(2);
        logUploadSettings.setSchedule(createSchedule());
        return logUploadSettings;
    }

    protected Schedule createSchedule() {
        Schedule schedule = new Schedule();
        schedule.setType("ActNow");
        schedule.setExpression("expression");
        schedule.setExpressionL1("expressionL1");
        schedule.setExpressionL2("expressionL2");
        schedule.setExpressionL3("expressionL3");
        schedule.setTimeWindowMinutes(0);
        return schedule;
    }

    protected Schedule createSchedule(String cronExpression) {
        Schedule schedule = createSchedule();
        schedule.setExpression(cronExpression);
        return schedule;
    }

    protected FirmwareRuleTemplate createFirmwareRuleTemplate() {
        FirmwareRuleTemplate firmwareRuleTemplate = new FirmwareRuleTemplate();
        firmwareRuleTemplate.setId("testFirmwareRuleTemplate");
        firmwareRuleTemplate.setApplicableAction(createApplicableAction(ApplicableAction.Type.RULE_TEMPLATE));
        firmwareRuleTemplate.setRule(createRule());
        firmwareRuleTemplate.setPriority(1);
        return firmwareRuleTemplate;
    }

    protected FirmwareRuleTemplate createFirmwareRuleTemplate(String id) {
        FirmwareRuleTemplate firmwareRuleTemplate = createFirmwareRuleTemplate();
        firmwareRuleTemplate.setId(id);
        return firmwareRuleTemplate;
    }

    protected FirmwareRuleTemplate createIpRuleTemplate(String ipListId) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(TemplateNames.IP_RULE);
        template.setApplicableAction(createApplicableAction(ApplicableAction.Type.RULE_TEMPLATE));
        template.setPriority(1);
        template.setRule(createIpCompoundRule(ipListId));
        return template;
    }

    protected FirmwareRuleTemplate createMacRuleTemplate(String ipListId) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(TemplateNames.MAC_RULE);
        template.setApplicableAction(createApplicableAction(ApplicableAction.Type.RULE_TEMPLATE));
        template.setPriority(1);
        template.setRule(createIpCompoundRule(ipListId));
        return template;
    }

    private ApplicableAction createApplicableAction(ApplicableAction.Type type) {
        RuleAction ruleAction = new RuleAction();
        ruleAction.setActionType(type);
        ruleAction.setId("testActionId");
        ruleAction.setConfigId(createFirmwareConfig().getId());
        return ruleAction;
    }

    protected ApplicableAction createDefinePropertiesTemplateAction(ApplicableAction.Type type, String propertyKey, String propertyValue, DefinePropertiesTemplateAction.ValidationType validationType) {
        DefinePropertiesTemplateAction definePropertiesTemplateAction = new DefinePropertiesTemplateAction();
        definePropertiesTemplateAction.setId("testActionId");
        definePropertiesTemplateAction.setActionType(type);
        DefinePropertiesTemplateAction.PropertyValue definePropertyValue = DefinePropertiesTemplateAction.PropertyValue.create(propertyValue, false, validationType);
        definePropertiesTemplateAction.setProperties(Collections.singletonMap(propertyKey, definePropertyValue));
        return definePropertiesTemplateAction;
    }

    protected FirmwareConfig createAndSaveFirmwareConfig() throws ValidationException {
        return saveFirmwareConfig(createFirmwareConfig());
    }

    protected FirmwareConfig createFirmwareConfig() {
        return createFirmwareConfig("testFirmwareConfigId");
    }

    protected FirmwareConfig createFirmwareConfig(String id) {
        return createFirmwareConfig(id, "testDescription");
    }

    protected FirmwareConfig createFirmwareConfig(String id, String description) {
        FirmwareConfig firmwareConfig = new FirmwareConfig();
        firmwareConfig.setId(id);
        firmwareConfig.setDescription(description);
        firmwareConfig.setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol.http);
        firmwareConfig.setFirmwareFilename("firmareFileName");
        firmwareConfig.setFirmwareLocation("http://test.com");
        firmwareConfig.setFirmwareVersion("version");
        firmwareConfig.setIpv6FirmwareLocation("2001:0db8:11a3:09d7:1f34:8a2e:07a0:765d");
        firmwareConfig.setRebootImmediately(false);
        firmwareConfig.setApplicationType(STB);
        firmwareConfig.setSupportedModelIds(Sets.newHashSet(createModel().getId()));
        return firmwareConfig;
    }

    protected FirmwareConfig createFirmwareConfig(String id, String firmwareVersion, Set<String> modelIds, String description) {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfig.setId(id);
        firmwareConfig.setFirmwareVersion(firmwareVersion);
        firmwareConfig.setSupportedModelIds(modelIds);
        firmwareConfig.setDescription(description);
        return firmwareConfig;
    }

    protected FirmwareConfig saveFirmwareConfig(FirmwareConfig config) throws ValidationException {
        firmwareConfigDAO.setOne(config.getId(), config);

        return config;
    }

    protected DCMGenericRule createFormula() {
        return createFormula(createCondition());
    }

    protected DCMRuleWithSettings createAndSaveDcmRuleWithSettings(DCMGenericRule dcmRule) throws ValidationException {
        DeviceSettings deviceSettings = createDeviceSettings(dcmRule.getId());
        deviceSettingsDAO.setOne(deviceSettings.getId(), deviceSettings);

        LogUploadSettings logUploadSettings = createLogUploadSettings(dcmRule.getId());
        logUploadSettingsDAO.setOne(logUploadSettings.getId(), logUploadSettings);

        VodSettings vodSettings = createVodSettings(dcmRule.getId());
        vodSettingsDAO.setOne(vodSettings.getId(), vodSettings);


        DCMRuleWithSettings dcmRuleWithSettings = new DCMRuleWithSettings(dcmRule,
                deviceSettings,
                logUploadSettings,
                vodSettings);

        return dcmRuleWithSettings;
    }

    protected DCMGenericRule createFormula(Condition condition) {
        DCMGenericRule formula = new DCMGenericRule();
        formula.setId("formulaId");
        formula.setName("name");
        formula.setDescription("description");
        formula.setPriority(1);
        formula.setPercentage(100);
        formula.setPercentageL1(0);
        formula.setPercentageL2(0);
        formula.setPercentageL3(0);
        formula.setRuleExpression("env");
        formula.setCondition(condition);

        return formula;
    }

    protected DCMGenericRule saveFormula(DCMGenericRule formula) throws Exception {
        dcmRuleDAO.setOne(formula.getId(), formula);

        return formula;
    }

    protected Condition createCondition() {
        Condition condition = new Condition();
        condition.setFreeArg(new FreeArg(FreeArgType.forName("String"), "host"));
        condition.setFixedArg(FixedArg.from("localhost"));
        condition.setOperation(RuleFactory.IN_LIST);
        return condition;
    }

    protected Condition createCondition(String value) {
        return new Condition(RuleFactory.VERSION, IS, FixedArg.from(value));
    }

    protected Rule createRule(String value) {
        return createRule(createCondition(value));
    }

    protected Rule createRule() {
        return createRule(createCondition());
    }

    protected Rule createRule(Condition condition) {
        Rule rule = new Rule();
        rule.setCondition(condition);
        return rule;
    }

    protected FirmwareRule createFirmwareRule() throws ValidationException {
        return createFirmwareRule(createCondition(), "firmwarerule");
    }

    protected FirmwareRule createFirmwareRule(Condition condition, String templateType) throws ValidationException {
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig();
        FirmwareRule firmwareRule = createFirmwareRule(firmwareConfig.getId());
        firmwareRule.getRule().setCondition(condition);
        firmwareRule.setType(templateType);
        return firmwareRule;
    }

    protected FirmwareRule createFirmwareRule(Condition condition, String firmwareConfigId, String templateType) {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setName("testName");
        firmwareRule.setId("testFirmwareRuleId");
        firmwareRule.setRule(createRule(condition));
        firmwareRule.setType(templateType);
        firmwareRule.setApplicationType(STB);
        RuleAction ruleAction = new RuleAction();
        ruleAction.setActionType(ApplicableAction.Type.RULE);
        ruleAction.setId(firmwareConfigId);
        ruleAction.setConfigId(firmwareConfigId);
        firmwareRule.setApplicableAction(ruleAction);

        return firmwareRule;
    }

    protected FirmwareRule createFirmwareRule(String configId) throws ValidationException {
        return createFirmwareRule(createCondition(), configId, TemplateNames.ENV_MODEL_RULE);
    }

    protected FirmwareRule saveFirmwareRule(FirmwareRule rule) throws Exception {
        firmwareRuleDao.setOne(rule.getId(), rule);

        return rule;
    }

    protected VodSettings createVodSettings(String id) {
        VodSettings vodSettings = createVodSettings();
        vodSettings.setId(id);
        return vodSettings;
    }

    protected VodSettings createVodSettings() {
        VodSettings vodSettings = new VodSettings();
        vodSettings.setId("testVodSettingId");
        vodSettings.setName("testVodSettingsName");
        vodSettings.setIpList(Lists.newArrayList("10.10.10.10"));
        vodSettings.setIpNames(Lists.newArrayList("testIpName"));
        vodSettings.setLocationsURL("http://test.com");
        Map<String, String> srmIpList = new HashMap<>();
        srmIpList.put("testIpName", "10.10.10.10");
        vodSettings.setSrmIPList(srmIpList);
        return vodSettings;
    }

    protected PermanentTelemetryProfile createTelemetryProfile() {
        return createTelemetryProfile("permanentTelemetryProfileId");
    }

    protected PermanentTelemetryProfile createTelemetryProfile(String id) {
        return createTelemetryProfile(id, "ProfileName");
    }

    protected PermanentTelemetryProfile createTelemetryProfile(String id, String name) {
        PermanentTelemetryProfile profile = new PermanentTelemetryProfile();
        profile.setId(id);
        profile.setName(name);
        profile.setTelemetryProfile(Lists.newArrayList(createTelemetryElement()));
        profile.setUploadProtocol(UploadProtocol.HTTPS);
        profile.setUploadRepository("https://test.com");
        profile.setApplicationType(STB);
        return profile;
    }

    protected SettingProfile createSettingProfile(String name, SettingType type, Map<String, String> map) {
        SettingProfile profile = new SettingProfile();
        profile.setId(UUID.randomUUID().toString());
        profile.setSettingProfileId(name);
        profile.setSettingType(type);
        profile.setProperties(map);
        profile.setApplicationType(STB);
        return profile;
    }

    protected SettingProfile createSettingProfile(String name, Map<String, String> map) {
        return createSettingProfile(name, SettingType.EPON, map);
    }

    protected SettingProfile createSettingProfile(String name) {
        return createSettingProfile(name, new HashMap<>(Collections.singletonMap("key1", "value1")));
    }

    protected SettingProfile createAndSaveSettingProfile(String name) throws ValidationException {
        SettingProfile profile = createSettingProfile(name, Collections.singletonMap("key1", "value1"));
        settingProfileDao.setOne(profile.getId(), profile);
        return profile;
    }

    protected SettingProfile createAndSaveSettingProfile(String name, SettingType type) throws ValidationException {
        SettingProfile profile = createSettingProfile(name, type, Collections.singletonMap("key1", "value1"));
        settingProfileDao.setOne(profile.getId(), profile);
        return profile;
    }

    protected SettingRule createSettingRule(String name, String boundSettingId) {
        SettingRule rule = new SettingRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(name);
        rule.setBoundSettingId(boundSettingId);
        rule.setRule(createRule(name));
        rule.setApplicationType(STB);
        return rule;
    }

    protected SettingRule createSettingRule(FreeArg ruleFactory, String name, String boundSettingId) {
        SettingRule settingRule = new SettingRule();
        settingRule.setId(UUID.randomUUID().toString());
        settingRule.setName(name);
        settingRule.setBoundSettingId(boundSettingId);

        Condition condition = new Condition(ruleFactory, IS, FixedArg.from(name));
        Rule rule = createRule(condition);
        settingRule.setRule(rule);

        return settingRule;
    }

    protected SettingRule createAndSaveSettingRule(FreeArg ruleFactory, String name, String boundSettingId) throws ValidationException {
        SettingRule rule = createSettingRule(ruleFactory, name, boundSettingId);
        settingRuleDAO.setOne(rule.getId(), rule);
        return rule;
    }

    protected SettingRule createAndSaveSettingRule(String name, String boundSettingId) throws ValidationException {
        SettingRule rule = createSettingRule(name, boundSettingId);
        settingRuleDAO.setOne(rule.getId(), rule);
        return rule;
    }

    protected PermanentTelemetryProfile saveTelemetryProfile(PermanentTelemetryProfile profile) throws ValidationException {
        permanentTelemetryDAO.setOne(profile.getId(), profile);

        return profile;
    }

    protected TelemetryProfile.TelemetryElement createTelemetryElement() {
        TelemetryProfile.TelemetryElement telemetryElement = new TelemetryProfile.TelemetryElement();
        telemetryElement.setHeader("testHeader");
        telemetryElement.setContent("testContent");
        telemetryElement.setType("testType");
        telemetryElement.setPollingFrequency("10");
        return telemetryElement;
    }

    protected TelemetryRule createTelemetryRule() throws Exception {
        return createTelemetryRule(createCondition());
    }

    protected TelemetryRule createTelemetryRule(Condition condition) throws Exception {
        return createTelemetryRule("1-2-3-4-5", condition);
    }

    protected TelemetryRule createTelemetryRule(String id, String name) throws Exception {
        return createTelemetryRule(id, name, createCondition());
    }

    protected TelemetryRule createTelemetryRule(String id, Condition condition) throws Exception {
        return createTelemetryRule(id, "Rule name", condition);
    }

    protected TelemetryRule createTelemetryRule(String id, String name, Condition condition) throws Exception {
        PermanentTelemetryProfile permanentTelemetryProfile = createTelemetryProfile();
        permanentTelemetryDAO.setOne(permanentTelemetryProfile.getId(), permanentTelemetryProfile);
        TelemetryRule telemetryRule = new TelemetryRule();
        telemetryRule.setName(name);
        telemetryRule.setId(UUID.fromString(id).toString());
        telemetryRule.setBoundTelemetryId(createTelemetryProfile().getId());
        telemetryRule.setCondition(condition);
        telemetryRule.setApplicationType(STB);
        return telemetryRule;
    }

    protected TelemetryRule saveTelemetryRule(TelemetryRule rule) throws Exception {
        telemetryRuleDAO.setOne(rule.getId(), rule);

        return rule;
    }

    protected LogFilesGroup createLogFilesGroup() {
        LogFilesGroup logFilesGroup = new LogFilesGroup();
        logFilesGroup.setId("testId");
        logFilesGroup.setGroupName("groupName");
        List<String> logFileIds = new ArrayList<>();
        for (LogFile logFile : createLogFileList()) {
            logFileIds.add(logFile.getId());
        }
        logFilesGroup.setLogFileIds(logFileIds);
        return logFilesGroup;
    }

    protected List<LogFile> createLogFileList() {
        List<LogFile> logFiles = new ArrayList<>();
        logFiles.add(createLogFile());
        LogFile logFile = createLogFile();
        logFile.setId("testLogFileId2");
        logFile.setName("testLogFileName2");
        logFiles.add(logFile);
        return logFiles;
    }

    protected DownloadLocationRoundRobinFilterValue createDownloadLocationFilter() throws ValidationException {
        return createDownloadLocationFilter(STB);
    }

    protected DownloadLocationRoundRobinFilterValue createDownloadLocationFilter(String applicationType) throws ValidationException {
        DownloadLocationRoundRobinFilterValue filter = new DownloadLocationRoundRobinFilterValue();
        filter.setId(DownloadLocationRoundRobinFilterValue.SINGLETON_ID);
        filter.setApplicationType(applicationType);
        filter.setFirmwareVersions("firmwareVersion");
        filter.setHttpLocation("test.com");
        filter.setHttpFullUrlLocation("http://test.com");
        filter.setRogueModels(Lists.newArrayList(saveModel(createModel())));
        filter.setLocations(Lists.newArrayList(createIpV4Location("10.10.10.10", 100)));
        filter.setIpv6locations(Lists.newArrayList(createIpV4Location("2001:0db8:11a3:09d7:1f34:8a2e:07a0:765d", 100)));
        return filter;
    }

    protected DownloadLocationRoundRobinFilterValue.Location createIpV4Location(String ip, double percentage) {
        DownloadLocationRoundRobinFilterValue.Location location = new DownloadLocationRoundRobinFilterValue.Location();
        location.setLocationIp(new IpAddress(ip));
        location.setPercentage(percentage);
        return location;
    }

    protected GenericNamespacedList createMacList() {
        return createMacList("macList");
    }

    protected GenericNamespacedList createMacList(String id) {
        GenericNamespacedList macList = new GenericNamespacedList();
        macList.setId(id);
        macList.setTypeName(GenericNamespacedListTypes.MAC_LIST);
        macList.setData(Sets.newHashSet("AA:AA:AA:AA:AA:AA"));
        return macList;
    }

    protected GenericNamespacedList saveGenericList(GenericNamespacedList list) throws Exception {
        genericNamespacedListDAO.setOne(list.getId(), list);

        return list;
    }

    protected GenericNamespacedList createIpList() {
        return createIpList("ipList");
    }

    protected GenericNamespacedList createIpList(String id) {
        GenericNamespacedList ipList = new GenericNamespacedList();
        ipList.setId(id);
        ipList.setTypeName(GenericNamespacedListTypes.IP_LIST);
        ipList.setData(Sets.newHashSet("10.10.10.10"));
        return ipList;
    }

    protected Condition createCondition(String fixedArg, String freeArgName, Operation operation) {
        return new Condition(new FreeArg(FreeArgType.forName("STRING"), freeArgName), operation, FixedArg.from(fixedArg));
    }

    protected void assertException(ResultActions resultActions, Class expectedExceptionClass, String expectedMessage) {
        Exception exception = resultActions.andReturn().getResolvedException();
        assertEquals(expectedExceptionClass, exception.getClass());
        assertEquals(expectedMessage, exception.getMessage());
    }

    protected GenericNamespacedList changeNamespacedListId(GenericNamespacedList namespacedList, String id) {
        namespacedList.setId(id);
        return namespacedList;
    }

    protected <T extends IPersistable> void setIdAndAssertEqualsEntities(String id, T expectedEntity, T actualEntity) {
        actualEntity.setId(id);
        assertEquals(expectedEntity, actualEntity);
    }

    protected PercentFilterValue createPercentFilter(String ipListId, FirmwareRule envModelRule) throws ValidationException {
        PercentFilterValue filter = new PercentFilterValue();
        filter.setId(PercentFilterValue.SINGLETON_ID);
        filter.setPercent(100);
        filter.setPercentage(100);
        filter.setWhitelist(createIpAddressGroup(ipListId));
        Map<String, EnvModelPercentage> envModelPercentageMap = new HashMap<>();
        EnvModelPercentage envModelPercentage = new EnvModelPercentage();
        envModelPercentage.setWhitelist(createIpAddressGroup(ipListId));
        envModelPercentage.setPercentage(100);
        envModelPercentage.setActive(true);
        envModelPercentage.setFirmwareCheckRequired(false);
        envModelPercentageMap.put(envModelRule.getName(), envModelPercentage);
        filter.setEnvModelPercentages(envModelPercentageMap);
        return filter;
    }

    protected PercentFilterValue createPercentFilterWithLkgAndIntermediateVersion(String ipListId, FirmwareRule envModelRule) throws ValidationException {
        PercentFilterValue percentFilter = createPercentFilter(ipListId, envModelRule);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        for (EnvModelPercentage envModelPercentage : percentFilter.getEnvModelPercentages().values()) {
            envModelPercentage.setPercentage(90);
            envModelPercentage.setFirmwareCheckRequired(true);
            envModelPercentage.setLastKnownGood(firmwareConfig.getId());
            envModelPercentage.setIntermediateVersion(firmwareConfig.getId());
        }
        return percentFilter;
    }

    protected IpAddressGroupExtended createIpAddressGroup(String ipListId) {
        IpAddressGroupExtended ipAddressGroup = new IpAddressGroupExtended();
        ipAddressGroup.setId(ipListId);
        ipAddressGroup.setName(ipListId);
        ipAddressGroup.setIpAddresses(Sets.newHashSet(new IpAddress("10.10.10.10")));
        return ipAddressGroup;
    }

    protected FirmwareRule createEnvModelRule(String name, String configId) throws ValidationException {
        FirmwareRule firmwareRule = createFirmwareRule(configId);
        firmwareRule.setName(name);
        firmwareRule.setType(TemplateNames.ENV_MODEL_RULE);
        firmwareRule.setRule(createEnvModelCompoundRule());
        return firmwareRule;
    }

    protected FirmwareRule createEnvModelRule(String name) throws ValidationException {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setName(name);
        firmwareRule.setType(TemplateNames.ENV_MODEL_RULE);
        firmwareRule.setRule(createEnvModelCompoundRule());
        return firmwareRule;
    }

    protected FirmwareRule createMacRule(String name) throws ValidationException {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setName(name);
        firmwareRule.setType(TemplateNames.MAC_RULE);
        Rule rule = new Rule();
        rule.setCondition(new Condition(RuleFactory.MAC, RuleFactory.IN_LIST, FixedArg.from(createMacList().getId())));
        firmwareRule.setRule(rule);
        return firmwareRule;
    }

    protected Rule createEnvModelCompoundRule() {
        List<Rule> compoundRules = new ArrayList<>();

        Rule envRule = new Rule();
        envRule.setCondition(new Condition(ENV, IS, FixedArg.from("ENVID")));
        compoundRules.add(envRule);

        Rule modelRule = new Rule();
        modelRule.setCondition(new Condition(RuleFactory.MODEL, IS, FixedArg.from("MODELID")));
        modelRule.setRelation(Relation.AND);
        compoundRules.add(modelRule);

        Rule resultRule = new Rule();
        resultRule.setCompoundParts(compoundRules);

        return resultRule;
    }

    protected FirmwareRule createIpRule(String name, String ipListId) throws ValidationException {
        FirmwareRule firmwareRule = createFirmwareRule();
        firmwareRule.setName(name);
        firmwareRule.setType(TemplateNames.IP_RULE);
        firmwareRule.setRule(createIpCompoundRule(ipListId));
        return firmwareRule;
    }

    protected Rule createIpCompoundRule(String ipListId) {
        Rule resultRule = createEnvModelCompoundRule();
        Rule ipRule = new Rule();
        Condition condition = new Condition(RuleFactory.IP, RuleFactory.IN_LIST, FixedArg.from(ipListId));
        ipRule.setCondition(condition);
        ipRule.setRelation(Relation.AND);
        resultRule.getCompoundParts().add(ipRule);
        return resultRule;
    }

    protected SettingRule createSettingIpRule(String name, String boundConfigId, String ipListId) {
        SettingRule settingRule = createSettingRule(name, boundConfigId);
        settingRule.setName(name);
        settingRule.setRule(createIpCompoundRule(ipListId));
        return settingRule;
    }

    protected TelemetryRule createTelemetryIpRule(String name, String ipListId) throws Exception {
        TelemetryRule telemetryRule = createTelemetryRule(
                UUID.randomUUID().toString(),
                name,
                new Condition(RuleFactory.IP, RuleFactory.IN_LIST, FixedArg.from(ipListId)));
        return telemetryRule;
    }

    protected TelemetryRule createTelemetryMacRule(String name, String macListId) throws Exception {
        TelemetryRule telemetryRule = createTelemetryRule(
                UUID.randomUUID().toString(),
                name,
                new Condition(RuleFactory.MAC, RuleFactory.IN_LIST, FixedArg.from(macListId)));
        return telemetryRule;
    }

    protected Formula createOldFormula() {
        Formula formula = new Formula();
        formula.setId(UUID.randomUUID().toString());
        formula.setName("formulaName");
        formula.setPercentage(100);
        formula.setRuleExpression("eStbMac AND env");
        formula.setEnv(Sets.newHashSet(createEnvironment().getId()));
        formula.setEstbMacAddress("AA:AA:AA:AA:AA:AA");
        return formula;
    }

    protected NamespacedList createNamespacedList() {
        NamespacedList namespacedList = new NamespacedList();
        namespacedList.setId("namespacedListId");
        namespacedList.setData(Sets.newHashSet("AA:AA:BB:BB:CC:CC"));
        return namespacedList;
    }

    protected com.comcast.xconf.estbfirmware.FirmwareRule createOldFirmwareRule() {
        com.comcast.xconf.estbfirmware.FirmwareRule firmwareRule = new com.comcast.xconf.estbfirmware.FirmwareRule();
        firmwareRule.setBoundConfigId("configId");
        firmwareRule.setId(UUID.randomUUID().toString());
        firmwareRule.setName("firmwareRuleName");
        firmwareRule.setCondition(new Condition(RuleFactory.IP, RuleFactory.IN_LIST, FixedArg.from("ipList")));
        firmwareRule.setNegated(false);
        firmwareRule.setType(com.comcast.xconf.estbfirmware.FirmwareRule.RuleType.IP_RULE);
        firmwareRule.setTargetedModelIds(Sets.newHashSet(createModel().getId()));
        return firmwareRule;
    }

    protected RuleAction createRuleAction(String firmwareConfigId) {
        RuleAction ruleAction = new RuleAction();
        ruleAction.setConfigId(firmwareConfigId);
        ruleAction.setId(UUID.randomUUID().toString());
        ruleAction.setActionType(ApplicableAction.Type.RULE);
        return ruleAction;
    }

    protected void verifySearchResult(String url, Object context, Object expectedResult) throws Exception {
        mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(CoreUtil.toJSON(context))
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(expectedResult)));
    }

    protected FirmwareRule createFirmwareRule(FirmwareRuleTemplate template, String firmwareConfigId, String applicationType) {
        FirmwareRule firmwareRule = createFirmwareRule(template.getRule().getCondition(), firmwareConfigId, template.getId());
        firmwareRule.setId(UUID.randomUUID().toString());
        firmwareRule.setName(applicationType + "_name");
        firmwareRule.setApplicationType(applicationType);
        return firmwareRule;
    }

    protected Map<String, FirmwareRule> createAndSaveFirmwareRules(FirmwareRuleTemplate template) throws ValidationException {
        Map<String, FirmwareRule> firmwareRules = new HashMap();
        FirmwareConfig stbFirmwareConfig = createAndSaveFirmwareConfig(STB);
        FirmwareRule stbFirmwareRule = createFirmwareRule(template, stbFirmwareConfig.getId(), STB);
        stbFirmwareRule.setName(STB);
        firmwareRuleDao.setOne(stbFirmwareRule.getId(), stbFirmwareRule);
        firmwareRules.put(STB, stbFirmwareRule);

        FirmwareConfig xhomeFirmwareConfig = createAndSaveFirmwareConfig(XHOME);
        FirmwareRule xhomeFirmwareRule = createFirmwareRule(template, xhomeFirmwareConfig.getId(), XHOME);
        xhomeFirmwareRule.setName(XHOME);
        firmwareRuleDao.setOne(xhomeFirmwareRule.getId(), xhomeFirmwareRule);
        firmwareRules.put(XHOME, xhomeFirmwareRule);

        return firmwareRules;
    }

    protected FirmwareConfig createAndSaveFirmwareConfig(String applicationType) {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfig.setId(UUID.randomUUID().toString());
        firmwareConfig.setApplicationType(applicationType);
        firmwareConfig.setDescription(firmwareConfig.getId() + "_" + "description");
        firmwareConfig.setFirmwareVersion(firmwareConfig.getId() + "_" + "version");
        return firmwareConfig;
    }

    protected ResultMatcher errorMessageMatcher(final String errorMessage) {
        return new ResultMatcher() {
            @Override
            public void match(MvcResult mvcResult) throws Exception {
                assertEquals(errorMessage, mvcResult.getResolvedException().getMessage());
            }
        };
    }

    protected Rule createEnvPartnerRule(String envId, String partnerId) {
        Rule normalizedRule = new Rule();
        Rule envRule = Rule.Builder.of(new Condition(ENV, IS, FixedArg.from(envId))).build();
        envRule.setRelation(Relation.AND);
        Rule partnerRule = Rule.Builder.of(new Condition(RuleFactory.PARTNER_ID, IS, FixedArg.from(partnerId))).build();
        normalizedRule.setCompoundParts(Lists.newArrayList(envRule, partnerRule));

        return normalizedRule;
    }
}
