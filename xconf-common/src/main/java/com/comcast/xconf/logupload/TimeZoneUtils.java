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
 *  Created: 4:29 PM
 */
package com.comcast.xconf.logupload;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

public class TimeZoneUtils {

    private static final Logger log = LoggerFactory.getLogger(TimeZoneUtils.class);

    private final static String DEFAULT_TIME_ZONE = "US/Eastern";
    public static final Integer ONE_HOUR_MILLIS = 3600000;
    public static final Integer DEFAULT_OFFSET_ROW = -5;


    public static TimeZone matchTimeZone(String timeZoneStr) {
        DateTimeZone dateTimeZone;
        try {
            dateTimeZone = DateTimeZone.forID(timeZoneStr);
        } catch (IllegalArgumentException e) {
            log.error("TimeZoneMatchingException: ", e);
            dateTimeZone = DateTimeZone.forID(DEFAULT_TIME_ZONE);
        }
        return dateTimeZone.toTimeZone();
    }
}



