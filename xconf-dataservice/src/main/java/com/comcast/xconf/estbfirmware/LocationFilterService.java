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
package com.comcast.xconf.estbfirmware;

import com.comcast.xconf.logupload.UploadProtocol;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LocationFilterService {


    public boolean isValidUrl(String url) {
        List<String> schemas = new ArrayList<String>();

        for (UploadProtocol protocol : UploadProtocol.values()) {
            schemas.add(protocol.name().toLowerCase());
        }

        final UrlValidator urlValidator = new UrlValidator(schemas.toArray(new String[schemas.size()]));

        return urlValidator.isValid(url);
    }

    public void normalizeFirmwareVersions(DownloadLocationRoundRobinFilterValue filter) {
        Set<String> versions = new HashSet<String>();
        if (StringUtils.isNotBlank(filter.getFirmwareVersions())) {
            String[] a = filter.getFirmwareVersions().split("\\s+");
            if (a != null) {
                for (String version : a) {
                    if (!version.isEmpty()) {
                        versions.add(version);
                    }
                }
            }
        }
        String firmwareVersions = "";
        if (versions.size() > 0)  {
            for (String version : versions) {
                firmwareVersions += version + "\n";
            }
            firmwareVersions = firmwareVersions.substring(0, firmwareVersions.length() - 1);
        }

        filter.setFirmwareVersions(firmwareVersions);
    }
}
