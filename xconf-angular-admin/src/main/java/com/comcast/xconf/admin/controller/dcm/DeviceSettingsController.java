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
 * Author: Stanislav Menshykov
 * Created: 17.10.15  11:157
 */
package com.comcast.xconf.admin.controller.dcm;

import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.dcm.DeviceSettingsService;
import com.comcast.xconf.logupload.DeviceSettings;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.shared.service.CrudService;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/dcm/deviceSettings", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeviceSettingsController extends AbstractController<DeviceSettings> {

    @Autowired
    private DeviceSettingsService deviceSettingsService;

    @RequestMapping(value = "/names", method = RequestMethod.GET)
    public ResponseEntity getDeviceSettingsNames() {
        return new ResponseEntity<>(deviceSettingsService.getDeviceSettingsNames(), HttpStatus.OK);
    }

    @RequestMapping(value = "/size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getDeviceSettingsSize() {
        return Integer.toString(deviceSettingsService.getAll().size());
    }

    @RequestMapping(value = "/filtered", method = RequestMethod.POST)
    public ResponseEntity getFiltered(
            @RequestBody Map<String, String> searchContext,
            @RequestParam final Integer pageSize,
            @RequestParam final Integer pageNumber
    ) {
        List<DeviceSettings> deviceSettingsByName = deviceSettingsService.findByContext(searchContext);
        return new ResponseEntity<>(PageUtils.getPage(deviceSettingsByName, pageNumber, pageSize),
                Utils.createNumberOfItemsHttpHeaders(deviceSettingsByName),
                HttpStatus.OK);
    }

    @Override
    public String getOneEntityExportName() {
        return null;
    }

    @Override
    public String getAllEntitiesExportName() {
        return null;
    }

    @Override
    public AbstractService<DeviceSettings> getService() {
        return deviceSettingsService;
    }
}


