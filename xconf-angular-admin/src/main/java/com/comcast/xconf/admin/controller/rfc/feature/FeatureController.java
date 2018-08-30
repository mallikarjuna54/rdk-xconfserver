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
 * Author: Yury Stagit
 * Created: 11/02/16  12:00 PM
 */

package com.comcast.xconf.admin.controller.rfc.feature;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.service.rfc.FeatureService;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.shared.controller.ApplicationTypeAwayController;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(FeatureController.URL_MAPPING)
public class FeatureController extends ApplicationTypeAwayController<Feature> {
    public static final String URL_MAPPING = "rfc/feature";

    @Autowired
    private FeatureService featureService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FEATURE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FEATURES.getName();
    }

    @Override
    public AbstractApplicationTypeAwareService<Feature> getService() {
        return featureService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/byIdList")
    public ResponseEntity getFeatureRulesByIdList(@RequestBody List<String> featureIdList) {
        return new ResponseEntity<>(featureService.getFeaturesByIdList(featureIdList), HttpStatus.OK);
    }
}
