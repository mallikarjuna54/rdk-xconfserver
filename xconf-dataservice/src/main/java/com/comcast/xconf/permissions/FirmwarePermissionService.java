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
 * <p>
 * Author: mdolina
 * Created: 9/6/17  14:35 PM
 */
package com.comcast.xconf.permissions;

import org.springframework.stereotype.Service;

@Service
public class FirmwarePermissionService extends PermissionService {
    @Override
    EntityPermission getEntityPermission() {
        return new EntityPermission.Builder(
                Permissions.READ_FIRMWARE_ALL,
                Permissions.READ_FIRMWARE_STB,
                Permissions.READ_FIRMWARE_XHOME,
                Permissions.WRITE_FIRMWARE_ALL,
                Permissions.WRITE_FIRMWARE_STB,
                Permissions.WRITE_FIRMWARE_XHOME
        ).build();
    }
}
