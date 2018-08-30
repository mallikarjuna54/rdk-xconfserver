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
 *  Created: 12/18/15 1:00 PM
 */

package com.comcast.xconf.queries.controllers;


import com.comcast.hesperius.dataaccess.core.dao.ICompositeDAO;
import com.comcast.hesperius.dataaccess.core.dao.ISimpleCachedDAO;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.logupload.LogFile;
import com.comcast.xconf.logupload.LogUploadSettings;
import com.comcast.xconf.logupload.Schedule;
import com.comcast.xconf.queries.QueryConstants;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(QueryConstants.UPDATES_LOG_UPLOAD_SETTINGS)
public class LogUploadSettingsQueriesController {

    @Autowired
    private ISimpleCachedDAO<String, LogFile> logFileDAO;

    @Autowired
    ICompositeDAO<String, LogFile> indexesLogFilesDAO;

    @Autowired
    private ISimpleCachedDAO<String, LogUploadSettings> logUploadSettingsDAO;

    private static final Logger log = LoggerFactory.getLogger(LogUploadSettingsQueriesController.class);

    @RequestMapping(method = RequestMethod.POST, value = "/{timezone}/{scheduleTimezone}")
    public ResponseEntity save(@RequestBody LogUploadSettings logUploadSettings,
                               @PathVariable String timezone,
                               @PathVariable String scheduleTimezone) {

        Schedule schedule = logUploadSettings.getSchedule();
        if (schedule == null) {
            return new ResponseEntity<>("Schedule is empty", HttpStatus.BAD_REQUEST);
        }
        if (schedule != null && checkDateStrLength(schedule.getStartDate()) && checkDateStrLength(schedule.getEndDate())) {
            boolean startValid = Utils.isValidDate(schedule.getStartDate());
            if (!startValid) {
                return new ResponseEntity("Start date is invalid", HttpStatus.BAD_REQUEST);
            }
            boolean endValid = Utils.isValidDate(schedule.getEndDate());
            if (!endValid) {
                return new ResponseEntity<>("End date is invalid", HttpStatus.BAD_REQUEST);
            }
            if (startValid && endValid && Utils.compareDates(schedule.getStartDate(), schedule.getEndDate()) >= 0) {
                return new ResponseEntity<>("Start date is greater/equal to End date", HttpStatus.BAD_REQUEST);
            }
        }

        if (StringUtils.isBlank(logUploadSettings.getFromDateTime())) {
            return new ResponseEntity("Start date is blank", HttpStatus.BAD_REQUEST);
        }
        if (StringUtils.isBlank(logUploadSettings.getToDateTime())) {
            return new ResponseEntity("End date is blank", HttpStatus.BAD_REQUEST);
        }

        if (checkDateStrLength(logUploadSettings.getFromDateTime()) && checkDateStrLength(logUploadSettings.getToDateTime())) {
            boolean startValid = Utils.isValidDate(logUploadSettings.getFromDateTime());
            if (!startValid) {
                return new ResponseEntity("Start date is invalid", HttpStatus.BAD_REQUEST);
            }
            boolean endValid = Utils.isValidDate(logUploadSettings.getToDateTime());
            if (!endValid) {
                return new ResponseEntity<>("End date is invalid", HttpStatus.BAD_REQUEST);
            }
            if (startValid && endValid && Utils.compareDates(logUploadSettings.getFromDateTime(), logUploadSettings.getToDateTime()) >= 0) {
                return new ResponseEntity<>("Start date is greater/equal to End date", HttpStatus.BAD_REQUEST);
            }
        }

        if(logUploadSettings.getModeToGetLogFiles() == null) {
            return new ResponseEntity<>("File mode is empty", HttpStatus.BAD_REQUEST);
        }

        if ((logUploadSettings.getLogFileIds() == null) && (logUploadSettings.getModeToGetLogFiles().equals(LogUploadSettings.MODE_TO_GET_LOG_FILES[0])))  {
            return new ResponseEntity<>("At least log file should be specified", HttpStatus.BAD_REQUEST);
        }

        String nameErrorMessage = validateName(logUploadSettings);
        if (StringUtils.isNotBlank(nameErrorMessage)) {
            return new ResponseEntity<>(nameErrorMessage, HttpStatus.BAD_REQUEST);
        }

        try {
            if (logUploadSettings.getModeToGetLogFiles().equals(LogUploadSettings.MODE_TO_GET_LOG_FILES[0])) {
                Set<String> keys = new HashSet<String>();
                keys.addAll(logUploadSettings.getLogFileIds());

                // TODO: get rid of code duplication
                Map<String, Optional<LogFile>> logFiles = logFileDAO.getAllAsMap(keys);
                Map<String, LogFile> logFilesMap = Maps.transformEntries(logFiles, LogFile.LOG_FILE_TRANSFORMER);

                indexesLogFilesDAO.deleteAll(logUploadSettings.getId());
                indexesLogFilesDAO.setMultiple(logUploadSettings.getId(), Lists.newArrayList(
                        Iterables.filter(
                                Iterables.transform(logFilesMap.values(), new Function<LogFile, LogFile>() {
                                    @Nullable
                                    @Override
                                    public LogFile apply(@Nullable LogFile input) {
                                        return input;
                                    }
                                }),
                                Predicates.notNull()
                        )
                ));
            }

            if (!checkDateStrLength(logUploadSettings.getFromDateTime()) || !checkDateStrLength(logUploadSettings.getToDateTime())) {
                logUploadSettings.setFromDateTime("");
                logUploadSettings.setToDateTime("");
            } else
            if ((timezone!=null) && (!timezone.equals("UTC"))) {
                logUploadSettings.setFromDateTime(Utils.converterDateTimeToUTC(logUploadSettings.getFromDateTime(),timezone));
                logUploadSettings.setToDateTime(Utils.converterDateTimeToUTC(logUploadSettings.getToDateTime(),timezone));
            }

            if (!checkDateStrLength(schedule.getStartDate()) || !checkDateStrLength(schedule.getEndDate())) {
                schedule.setStartDate("");
                schedule.setEndDate("");
            } else
            if ((scheduleTimezone != null) && (!scheduleTimezone.equals("UTC"))) {
                schedule.setStartDate(Utils.converterDateTimeToUTC(schedule.getStartDate(),scheduleTimezone));
                schedule.setEndDate(Utils.converterDateTimeToUTC(schedule.getEndDate(),scheduleTimezone));
            }
            logUploadSettings.setSchedule(schedule);
            logUploadSettingsDAO.setOne(logUploadSettings.getId(),logUploadSettings);

        } catch (Exception e) {
            log.error(e.toString());
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(logUploadSettings, HttpStatus.CREATED);
    }

    private String validateName(final LogUploadSettings logUploadSettings) {
        final LogUploadSettings lus = getLogUploadSettingsByName(StringUtils.trim(logUploadSettings.getName()));
        if (lus != null && !lus.getId().equals(logUploadSettings.getId())) {
            return "Name is already used";
        }
        return null;
    }

    private LogUploadSettings getLogUploadSettingsByName(final String name) {
        for (final Optional<LogUploadSettings> entity : logUploadSettingsDAO.asLoadingCache().asMap().values()) {
            if (!entity.isPresent()) {
                continue;
            }
            final LogUploadSettings logUploadSettings = entity.get();
            if (logUploadSettings.getName().equals(name)) {
                return logUploadSettings;
            }
        }
        return null;
    }

    private boolean checkDateStrLength(String dateStr) {
        return StringUtils.isNotBlank(dateStr) && dateStr.length() == 19;
    }

}
