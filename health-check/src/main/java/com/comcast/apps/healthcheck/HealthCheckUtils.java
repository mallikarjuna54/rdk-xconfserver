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
 * Author: rdolomansky
 * Created: 11/17/14  4:56 PM
 */
package com.comcast.apps.healthcheck;

public abstract class HealthCheckUtils {

    public static final String OK = "Ok";

    public static final String FAILURE = "Failure";


    public static String checkHealth(final IMetricManager metricManager, final IHeartBeat heartBeat) {
        String beat = OK;
        if (metricManager != null) {
            if (!metricManager.hasCurrent()) {
                beat = heartBeat.checkHeartBeat();
            }
            if (!metricManager.isHealthy()) {
                beat = FAILURE;
            }
        }
        return beat;
    }

    public static boolean isOk(final String beat) {
        return OK.equalsIgnoreCase(beat);
    }

}
