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
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.hesperius.dataaccess.core.exception.EntityConflictException;
import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static com.comcast.xconf.queries.QueriesHelper.nullifyUnwantedFields;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FirmwareConfigControllerTest extends BaseControllerTest {

    @Test
    public void testGetAllFirmwareConfigs() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        performRequestAndVerifyResponse(FirmwareConfigController.URL_MAPPING, Collections.singleton(firmwareConfig));
    }

    @Test
    public void getFirmwareConfigs() throws Exception {
        FirmwareConfig config1 = saveFirmwareConfig(createFirmwareConfig("id1", "a"));
        FirmwareConfig config2 = saveFirmwareConfig(createFirmwareConfig("id2", "b"));
        FirmwareConfig config3 = saveFirmwareConfig(createFirmwareConfig("id3", "c"));
        String expectedNumberOfItems = "3";
        List<FirmwareConfig> expectedResult = Arrays.asList(config1, config2);

        MockHttpServletResponse response = performGetRequestAndVerifyResponse("/" + FirmwareConfigController.URL_MAPPING + "/page",
            new HashMap<String, String>(){{
                put("pageNumber", "1");
                put("pageSize", "2");
            }}, expectedResult).andReturn().getResponse();

        final Object actualNumberOfItems = response.getHeaderValue("numberOfItems");
        assertEquals(expectedNumberOfItems, actualNumberOfItems);
    }

    @Test
    public void testGetFirmwareConfigById() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        nullifyUnwantedFields(firmwareConfig);

        performRequestAndVerifyResponse(FirmwareConfigController.URL_MAPPING + "/" + firmwareConfig.getId(), firmwareConfig);
    }

    @Test
    public void testGetFirmwareConfigsByModel() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);
        String modelId = firmwareConfig.getSupportedModelIds().iterator().next();

        performRequestAndVerifyResponse(FirmwareConfigController.URL_MAPPING + "/model/{modelId}", modelId, Collections.singleton(firmwareConfig));
    }

    @Test
    public void testCreateFirmwareConfig() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);

        mockMvc.perform(
                post("/" + FirmwareConfigController.URL_MAPPING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CoreUtil.toJSON(firmwareConfig)))
                .andExpect(status().isCreated());

        assertEquals(firmwareConfig, firmwareConfigDAO.getOne(firmwareConfig.getId()));
    }

    @Test
    public void testUpdateFirmwareConfig() throws Exception {
        Model model = createModel();
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig existingFirmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(existingFirmwareConfig.getId(), existingFirmwareConfig);
        FirmwareConfig firmwareConfigToUpdate = createFirmwareConfig();
        firmwareConfigToUpdate.setDescription("updated description");
        nullifyUnwantedFields(firmwareConfigToUpdate);

        mockMvc.perform(
                put("/" + FirmwareConfigController.URL_MAPPING)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CoreUtil.toJSON(firmwareConfigToUpdate)))
                .andExpect(status().isOk());

        assertEquals(firmwareConfigToUpdate, firmwareConfigDAO.getOne(existingFirmwareConfig.getId()));
    }

    @Test
    public void testDeleteFirmwareConfig() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        firmwareConfig = nullifyUnwantedFields(firmwareConfig);

        mockMvc.perform(
                delete("/" + FirmwareConfigController.URL_MAPPING + "/" + firmwareConfig.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CoreUtil.toJSON(firmwareConfig)))
                .andExpect(status().isNoContent());

        assertEquals(null, firmwareConfigDAO.getOne(firmwareConfig.getId()));
    }

    @Test
    public void deletionIsForbidden_ConfigIsUsedInFirmwareRule() throws Exception {
        String configId = "configId";
        saveFirmwareConfig(createFirmwareConfig(configId));
        FirmwareRule ruleWithConfigId = saveFirmwareRule(createFirmwareRule(configId));

        MvcResult actualResult = mockMvc.perform(
                delete("/" + FirmwareConfigController.URL_MAPPING + "/" + configId)
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isConflict()).andReturn();

        final Exception actualException = actualResult.getResolvedException();
        assertEquals(EntityConflictException.class, actualException.getClass());
        assertEquals("FirmwareConfig is used by " + ruleWithConfigId.getName() + " firmware rule", actualException.getMessage());
    }

    @Test
    public void testGetFirmwareConfigsBySupportedModels() throws Exception{
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        mockMvc.perform(
                post("/" + FirmwareConfigController.URL_MAPPING + "/bySupportedModels")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(CoreUtil.toJSON(firmwareConfig.getSupportedModelIds())))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreUtil.toJSON(Lists.newArrayList(firmwareConfig))));
    }

    @Test
    public void testExportOne() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        MockHttpServletResponse result = mockMvc.perform(get("/" + FirmwareConfigController.URL_MAPPING + "/" + firmwareConfig.getId())
                .param("export", "export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rebootImmediately").doesNotExist())
                .andExpect(jsonPath("$[0].firmwareDownloadProtocol").doesNotExist())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(nullifyUnwantedFields(firmwareConfig)))))
                .andReturn().getResponse();

        assertEquals(Sets.newHashSet("Content-Disposition", "Content-Type"), result.getHeaderNames());

    }

    @Test
    public void testExportAll() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);

        MockHttpServletResponse result = mockMvc.perform(get("/" + FirmwareConfigController.URL_MAPPING)
                .param("exportAll", "exportAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rebootImmediately").doesNotExist())
                .andExpect(jsonPath("$[0].firmwareDownloadProtocol").doesNotExist())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(nullifyUnwantedFields(firmwareConfig)))))
                .andReturn().getResponse();

        assertEquals(Sets.newHashSet("Content-Disposition", "Content-Type"), result.getHeaderNames());
    }

    @Test
    public void checkSorting() throws Exception {
        List<FirmwareConfig> firmwareConfigs = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            FirmwareConfig firmwareConfig = changeFirmwareConfigIdAndDescription(createFirmwareConfig(), "description" + i, "firmwareConfig" + i);
            firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
            firmwareConfigs.add(QueriesHelper.nullifyUnwantedFields(firmwareConfig));
        }

        mockMvc.perform(get("/" + FirmwareConfigController.URL_MAPPING))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreUtil.toJSON(firmwareConfigs)));
    }

    @Test
    public void getSupportedFirmwareConfigVersionsByEnvModelRuleName() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        Model model = createModel();
        Condition condition = createCondition(model.getId(), "model", StandardOperation.IS);
        FirmwareRule firmwareRule = createFirmwareRule(condition, firmwareConfig.getId(), TemplateNames.ENV_MODEL_RULE);
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        mockMvc.perform(get("/" + FirmwareConfigController.URL_MAPPING + "/supportedConfigsByEnvModelRuleName/" + firmwareRule.getName()))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreUtil.toJSON(Sets.newHashSet(firmwareConfig))));
    }

    @Test
    public void getFirmwareConfigByEnvModelRuleName() throws Exception {
        FirmwareConfig firmwareConfig = createFirmwareConfig();
        firmwareConfigDAO.setOne(firmwareConfig.getId(), firmwareConfig);
        FirmwareRule firmwareRule = createEnvModelRule("envModelRuleName", firmwareConfig.getId());
        firmwareRule.setApplicableAction(createRuleAction(firmwareConfig.getId()));
        firmwareRuleDao.setOne(firmwareRule.getId(), firmwareRule);

        mockMvc.perform(get("/" + FirmwareConfigController.URL_MAPPING + "/byEnvModelRuleName/" + firmwareRule.getName()))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(firmwareConfig)));
    }

    @Test
    public void searchByContext() throws Exception {
        FirmwareConfig firmwareConfig1 = createFirmwareConfig("id123", "firmwareVersion1", Sets.newHashSet("MODEL1", "MODEL2"), "description1");
        firmwareConfigDAO.setOne(firmwareConfig1.getId(), firmwareConfig1);

        FirmwareConfig firmwareConfig2 = createFirmwareConfig("id456", "firmwareVersion2", Sets.newHashSet("MODEL3", "MODEL4"), "description2");
        firmwareConfigDAO.setOne(firmwareConfig2.getId(), firmwareConfig2);

        FirmwareConfig firmwareConfig3 = createFirmwareConfig("id789", "firmwareVersion3", Sets.newHashSet("MODEL4", "MODEL5"), "description2");
        firmwareConfigDAO.setOne(firmwareConfig3.getId(), firmwareConfig3);

        Map<String, String> searchContext = new HashMap<>();
        searchContext.put(SearchFields.MODEL, "model1");
        searchContext.put(SearchFields.FIRMWARE_VERSION, "firmwareVersion1");
        searchContext.put(SearchFields.DESCRIPTION, "description1");

        assertSearchByContext(searchContext, Collections.singletonList(firmwareConfig1));

        searchContext.clear();
        searchContext.put(SearchFields.MODEL, "model4");

        assertSearchByContext(searchContext, Lists.newArrayList(firmwareConfig2, firmwareConfig3));

        searchContext.clear();
        searchContext.put(SearchFields.FIRMWARE_VERSION, "firmwareVersion2");

        assertSearchByContext(searchContext, Collections.singletonList(firmwareConfig2));

        searchContext.clear();
        searchContext.put(SearchFields.DESCRIPTION, "description1");

        assertSearchByContext(searchContext, Collections.singletonList(firmwareConfig1));
    }

    private void assertSearchByContext(Map<String, String> searchContext, List<FirmwareConfig> expectedFirmwareConfigs) throws Exception {
        mockMvc.perform(post("/" + FirmwareConfigController.URL_MAPPING + "/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(searchContext))
                .param("pageSize", "10")
                .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(expectedFirmwareConfigs)));
    }

    protected FirmwareConfig changeFirmwareConfigIdAndDescription(FirmwareConfig firmwareConfig,  String id, String description) {
        firmwareConfig.setId(id);
        firmwareConfig.setDescription(description);
        return firmwareConfig;
    }
}
