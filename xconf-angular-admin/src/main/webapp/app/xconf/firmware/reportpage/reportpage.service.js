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
 * Created: 06.11.15  15:07
 */
(function() {
    'use strict';

    angular
        .module('app.firmwareReportPage')
        .factory('firmwareReportPageService', service);

    service.$inject=['$http'];

    function service($http) {
        var urlMapping = 'firmwarerule/reportpage/';

        return {
            getReport: getReport,
            getMacRulesNames: getMacRulesNames
        };

        function getReport(macRulesIds) {
            return $http({
                url: urlMapping,
                method: "POST",
                data: macRulesIds,
                headers: {
                    'Content-type': 'application/json'
                },
                responseType: 'arraybuffer'
            });
        }

        function getMacRulesNames() {
            return $http.get('firmwarerule/MAC_RULE/names');
        }
    }
})();

