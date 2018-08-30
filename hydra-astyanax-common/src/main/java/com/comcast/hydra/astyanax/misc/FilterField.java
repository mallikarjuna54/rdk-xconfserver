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
package com.comcast.hydra.astyanax.misc;

import org.apache.commons.lang.StringUtils;

/**
 * User: anatoliy
 * Date: 9/13/12
 * Time: 1:06 PM
 */
public enum FilterField implements IFilterField {
    id,
    type,
    entityId,
    entityUrl,
    accountId,
    userId,
    locator,
    title,
    ;

    @Override
    public String getQueryParamName() {
        return AND_PREFIX + StringUtils.capitalize(name());
    }

    @Override
    public String getOrQueryParamName() {
        return OR_PREFIX + StringUtils.capitalize(name());
    }

    @Override
    public String getName() {
        return this.name();
    }
}
