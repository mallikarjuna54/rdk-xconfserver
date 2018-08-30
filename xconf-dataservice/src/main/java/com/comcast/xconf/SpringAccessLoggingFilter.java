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
package com.comcast.xconf;

import com.comcast.hesperius.dataaccess.support.AccessLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class SpringAccessLoggingFilter extends AccessLoggingFilter {

    static {
        NOT_LOGGED_HTTP_HEADERS.remove("Content-Length");
    }

    @Override
    protected void addLogEntries(HttpServletRequest request, Map<String, String> additionParams) {
        additionParams.put("payload", readPayload(request));
    }
}
