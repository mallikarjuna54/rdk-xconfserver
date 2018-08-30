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
package com.comcast.xconf.admin.contextconfig;

import com.comcast.xconf.permissions.DcmPermissionService;
import com.comcast.xconf.permissions.FirmwarePermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

@Configuration
@ImportResource({"classpath:spring-servlet.xml", "classpath:spring-datastore.xml"})
public class TestContextConfig {

    @Bean
    FirmwarePermissionService firmwarePermissionService() {
        FirmwarePermissionService permissionService = mock(FirmwarePermissionService.class, CALLS_REAL_METHODS);
        return permissionService;
    }

    @Bean
    DcmPermissionService dcmPermissionService() {
        DcmPermissionService dcmPermissionService = mock(DcmPermissionService.class, CALLS_REAL_METHODS);
        return dcmPermissionService;
    }

    @Bean
    TelemetryPermissionService telemetryPermissionService() {
        TelemetryPermissionService telemetryPermissionService = mock(TelemetryPermissionService.class, CALLS_REAL_METHODS);
        return telemetryPermissionService;
    }
}
