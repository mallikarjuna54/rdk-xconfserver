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
 * Created: 4/28/15
 */

(function() {

    'use strict';
})();
angular
    .module('app.config', ['ui.router'])

    .constant('PERMISSION', {
        'PERMIT_ALL': 'permitAll',

        'READ_COMMON': 'read-common',
        'WRITE_COMMON': 'write-common',


        'READ_FIRMWARE': 'read-firmware',
        'READ_FIRMWARE_ALL': 'read-firmware-*',
        'READ_FIRMWARE_STB': 'read-firmware-stb',
        'READ_FIRMWARE_XHOME': 'read-firmware-xhome',

        'WRITE_FIRMWARE': 'write-firmware',
        'WRITE_FIRMWARE_ALL': 'write-firmware-*',
        'WRITE_FIRMWARE_STB': 'write-firmware-stb',
        'WRITE_FIRMWARE_XHOME': 'write-firmware-xhome',

        'READ_DCM': 'read-dcm',
        'READ_DCM_ALL': 'read-dcm-*',
        'READ_DCM_STB': 'read-dcm-stb',
        'READ_DCM_XHOME': 'read-dcm-xhome',

        'WRITE_DCM': 'write-dcm',
        'WRITE_DCM_ALL': 'write-dcm-*',
        'WRITE_DCM_STB': 'write-dcm-stb',
        'WRITE_DCM_XHOME': 'write-dcm-xhome',

        'READ_TELEMETRY': 'read-telemetry',
        'READ_TELEMETRY_ALL': 'read-telemetry-*',
        'READ_TELEMETRY_STB': 'read-telemetry-stb',
        'READ_TELEMETRY_XHOME': 'read-telemetry-xhome',

        'WRITE_TELEMETRY': 'write-telemetry',
        'WRITE_TELEMETRY_ALL': 'write-telemetry-*',
        'WRITE_TELEMETRY_STB': 'write-telemetry-stb',
        'WRITE_TELEMETRY_XHOME': 'write-telemetry-xhome',

        'VIEW_TOOLS': 'view-tools',
        'WRITE_TOOLS': 'write-tools',

        'READ_FIRMWARE_RULE_TEMPLATES': 'read-firmware-rule-templates',
        'WRITE_FIRMWARE_RULE_TEMPLATES': 'write-firmware-rule-templates'
    })

    .constant('ENTITY_TYPE', {
        'MODEL': 'Model',
        'ENVIRONMENT': 'Environment',
        'MAC_LIST': 'MAC_LIST',
        'IP_LIST': 'IP_LIST',
        'NS_LIST': 'NS_LIST',
        'PERCENT_FILTER':'PERCENT_FILTER'
    })

    .constant('SETTING_TYPE', {
        'EPON': 'EPON',
        'PARTNER_SETTINGS': 'PARTNER_SETTINGS'
    })

    .constant('PROTOCOL', {
        'TFTP': 'TFTP',
        'SFTP': 'SFTP',
        'HTTP': 'HTTP',
        'HTTPS': 'HTTPS',
        'SCP': 'SCP',
        'S3': 'S3'
    })

    .constant('RELATION', {
        'AND': 'AND',
        'OR': 'OR'
    })

    .constant('OPERATION', {
        'IS': 'IS',
        'IN': 'IN',
        'IN_LIST': 'IN_LIST',
        'LTE': 'LTE',
        'GTE': 'GTE',
        'LIKE': 'LIKE',
        'PERCENT': 'PERCENT',
        'EXISTS': 'EXISTS',
        'MATCH': 'MATCH'
    })

    .constant('OPERATION_ARRAY', [
        'IS',
        'IN',
        'IN_LIST',
        'LTE',
        'GTE',
        'LIKE',
        'PERCENT',
        'EXISTS',
        'MATCH'
    ])

    .constant('FIRMWARE_RULE_OPERATION_ARRAY', [
        'IS',
        'IN',
        'IN_LIST',
        'LIKE',
        'PERCENT',
        'EXISTS'
    ])

    .constant('TARGETING_RULE_OPERATION_ARRAY', [
        'IS',
        'IN_LIST',
        'LIKE',
        'PERCENT'
    ])

    .constant('RFC_RULE_OPERATION_ARRAY', [
            'IS',
            'IN_LIST',
            'LIKE',
            'PERCENT',
            'EXISTS'
        ])

        .constant('TIME_FREE_ARG_OPERATION_ARRAY', [
        'LTE',
        'GTE'
    ])

    .constant('FIRMWARE_RULE_TYPE', {
        'MAC_RULE': 'MAC_RULE',
        'IP_RULE': 'IP_RULE',
        'ENV_MODEL_RULE': 'ENV_MODEL_RULE',

        'DOWNLOAD_LOCATION_FILTER': 'DOWNLOAD_LOCATION_FILTER',
        'TIME_FILTER': 'TIME_FILTER',
        'IP_FILTER': 'IP_FILTER',
        'REBOOT_IMMEDIATELY_FILTER': 'REBOOT_IMMEDIATELY_FILTER'
    })

    .constant('APPLICABLE_ACTION_TYPE', {
        'RULE': {
            name : 'RULE',
            class : 'com.comcast.xconf.firmware.RuleAction'
        },
        'DEFINE_PROPERTIES': {
            name : 'DEFINE_PROPERTIES',
            class : 'com.comcast.xconf.firmware.DefinePropertiesAction'
        },
        'BLOCKING_FILTER': {
            name : 'BLOCKING_FILTER',
            class : 'com.comcast.xconf.firmware.BlockingFilterAction'
        },

        'RULE_TEMPLATE': {
            name : 'RULE_TEMPLATE',
            class : 'com.comcast.xconf.firmware.RuleAction'
        },
        'DEFINE_PROPERTIES_TEMPLATE': {
            name : 'DEFINE_PROPERTIES_TEMPLATE',
            class : 'com.comcast.xconf.firmware.DefinePropertiesTemplateAction'
        },
        'BLOCKING_FILTER_TEMPLATE': {
            name : 'BLOCKING_FILTER_TEMPLATE',
            class : 'com.comcast.xconf.firmware.BlockingFilterAction'
        },

        'getActionTypeByName': function(actionTypeName) {
            switch(actionTypeName) {
                case this.RULE.name:
                    return this.RULE;
                case this.DEFINE_PROPERTIES.name:
                    return this.DEFINE_PROPERTIES;
                case this.BLOCKING_FILTER.name:
                    return this.BLOCKING_FILTER;

                case this.RULE_TEMPLATE.name:
                    return this.RULE_TEMPLATE;
                case this.DEFINE_PROPERTIES_TEMPLATE.name:
                    return this.DEFINE_PROPERTIES_TEMPLATE;
                case this.BLOCKING_FILTER_TEMPLATE.name:
                    return this.BLOCKING_FILTER_TEMPLATE;
            }
        }
    })

    .constant('SETTINGS_AVAILABILITY_KEYS', {
        DEVICE_SETTINGS: 'deviceSettings',
        VOD_SETTINGS: 'vodSettings',
        LOG_UPLOAD_SETTINGS: 'logUploadSettings'
    })

    .constant('EDIT_MODE', {
        'CREATE': 'CREATE',
        'UPDATE': 'UPDATE'
    })

    .constant('NAMESPACED_LIST_TYPE', {
        IP_LIST: 'IP_LIST',
        MAC_LIST: 'MAC_LIST',
        RI_MAC_LIST: 'RI_MAC_LIST'
    })

    .constant('MODES_TO_GET_LOG_FILES', {
        LOG_FILES: 'LogFiles',
        LOG_FILES_GROUP: 'LogFilesGroup',
        ALL_LOG_FILES: 'AllLogFiles'
    })

    .constant('SCHEDULE_TYPE', {
        ACT_NOW: 'ActNow',
        CRON_EXPRESSION: 'CronExpression',
        WHOLE_DAY_RANDOMIZED: 'WholeDayRandomized'
    })

    .constant('FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE', [
        'eStbMac',
        'env',
        'model',
        'firmwareVersion',
        'eCMMac',
        'receiverId',
        'controllerId',
        'channelMapId',
        'vodId',
        'timeZone',
        'time',
        'ipAddress'
    ])

    .constant('LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE', [
        'estbIP',
        'estbMacAddress',
        'ecmMacAddress',
        'env',
        'model',
        'firmwareVersion',
        'controllerId',
        'channelMapId',
        'vodId'
    ])

    .constant('FREE_ARG_NAME', {
        ESTB_MAC: 'eStbMac',
        ESTB_MAC_ADDRESS: 'estbMacAddress',
        ECM_MAC: 'eCMMac',
        ECM_MAC_ADDRESS: 'ecmMacAddress'
    })

    .constant('CAPABILITIES', [
        'RCDL',
        'supportsFullHttpUrl',
        'rebootDecoupled'
    ])

    .constant('RULE_SEARCH_OPTIONS', {
        data: [
            {
                "name": {
                    friendlyName: "Name",
                    apiArgs: ["NAME"]
                }
            },
            {
                "name": {
                    friendlyName: 'Key',
                    apiArgs: ['FREE_ARG']
                }
            },
            {
                "name": {
                    friendlyName: 'Value',
                    apiArgs: ['FIXED_ARG']
                }
            },
            {
                "name": {
                    friendlyName: 'Key and Value',
                    apiArgs: ['FREE_ARG', 'FIXED_ARG']
                }
            }
        ]

    }).constant('TIME_ZONES', [
    'UTC',
    'Local time'
])

    .constant('SEARCH_OPTIONS', {
        TEMPLATE_ID: 'TEMPLATE_ID',
        NAME: 'NAME',
        APPLICABLE_ACTION_TYPE: 'APPLICABLE_ACTION_TYPE'
    })

    .constant('APPLICATION_TYPES', [
        'xhome',
        'stb'
    ])

    .constant('APPLICATION_TYPE', {
        XHOME: 'xhome',
        STB: 'stb'
    });
