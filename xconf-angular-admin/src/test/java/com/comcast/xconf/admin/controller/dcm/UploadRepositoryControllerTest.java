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

package com.comcast.xconf.admin.controller.dcm;

import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.UploadRepository;
import com.comcast.xconf.search.SearchFields;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadRepositoryControllerTest extends BaseControllerTest{

    @Test
    public void getUploadRepository() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);

        mockMvc.perform(get("/dcm/uploadRepository/" + uploadRepository.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(uploadRepository)));

        mockMvc.perform(get("/dcm/uploadRepository")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(uploadRepository))));
    }

    @Test
    public void exportOne() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);

        performExportRequestAndVerifyResponse("/dcm/uploadRepository/" + uploadRepository.getId(), Lists.newArrayList(uploadRepository), ApplicationType.STB);
    }

    @Test
    public void exportAll() throws Exception {
        UploadRepository uploadRepository1 = createUploadRepository("uploadRepository1");
        uploadRepository1.setName("uploadRepository1");
        uploadRepositoryDAO.setOne(uploadRepository1.getId(), uploadRepository1);

        UploadRepository uploadRepository2 = createUploadRepository("uploadRepository2");
        uploadRepository2.setName("uploadRepository2");
        uploadRepositoryDAO.setOne(uploadRepository2.getId(), uploadRepository2);

        ArrayList<UploadRepository> list = Lists.newArrayList(uploadRepository1, uploadRepository2);
        performExportRequestAndVerifyResponse("/dcm/uploadRepository", list, ApplicationType.STB);
    }

    @Test
    public void create() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        mockMvc.perform(post("/dcm/uploadRepository")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(uploadRepository)))
                .andExpect(status().isCreated())
                .andExpect(content().json(CoreUtil.toJSON(uploadRepository)));
    }

    @Test
    public void deleteUploadRepository() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        mockMvc.perform(delete("/dcm/uploadRepository/" + uploadRepository.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/dcm/uploadRepository/" + uploadRepository.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getLogUploadSettingsSize() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        mockMvc.perform(get("/dcm/uploadRepository/size"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON("1")));
    }

    @Test
    public void getPage() throws Exception {
        UploadRepository uploadRepository = createUploadRepository();
        uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
        mockMvc.perform(get("/dcm/uploadRepository/page").contentType(MediaType.APPLICATION_JSON)
                .param("pageSize", "10").param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Lists.newArrayList(uploadRepository))));
    }

    @Test
    public void checkSorting() throws Exception {
        List<UploadRepository> uploadRepositories = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            UploadRepository uploadRepository = changeIdAndName(createUploadRepository(), "uploadRepositoryName" + i, "uploadRepositoryId" + i);
            uploadRepositoryDAO.setOne(uploadRepository.getId(), uploadRepository);
            uploadRepositories.add(uploadRepository);
        }

        mockMvc.perform(get("/dcm/uploadRepository/page")
                .param("pageSize", "10").param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(uploadRepositories)));
    }

    @Test
    public void getFiltered() throws Exception {
        UploadRepository uploadRepository1 = createUploadRepository("uploadRepository1");
        uploadRepository1.setName("uploadRepository1");
        uploadRepositoryDAO.setOne(uploadRepository1.getId(), uploadRepository1);

        UploadRepository uploadRepository2 = createUploadRepository("uploadRepository2");
        uploadRepository2.setName("uploadRepository2");
        uploadRepositoryDAO.setOne(uploadRepository2.getId(), uploadRepository2);

        mockMvc.perform(post("/dcm/uploadRepository/filtered")
                .contentType(MediaType.APPLICATION_JSON)
                .content(CoreUtil.toJSON(Collections.singletonMap(SearchFields.NAME, uploadRepository1.getName())))
                .param("pageNumber", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json(CoreUtil.toJSON(Collections.singletonList(uploadRepository1))));
    }

    private UploadRepository changeIdAndName(UploadRepository uploadRepository, String id, String name) {
        uploadRepository.setId(id);
        uploadRepository.setName(name);
        return uploadRepository;
    }
}