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
 *  Created: 11/20/15 7:04 PM
 */

package com.comcast.xconf.admin.controller.common;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.service.common.ModelService;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.shared.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ModelController.URL_MAPPING)
public class ModelController extends AbstractController<Model> {
    public static final String URL_MAPPING = "model";

    @Autowired
    private ModelService modelService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.MODEL.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_MODELS.getName();
    }

    @Override
    public AbstractService<Model> getService() {
        return modelService;
    }
}
