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
 * Author: Igor Kostrov
 * Created: 3/16/2016
 */
(function() {
    'use strict';

    angular
        .module('app.settingrule')
        .factory('settingRuleService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {
        var API_URL = 'setting/rule/';

        return {
            getAll: getAll,
            getRules: getRules,
            getRule: getRule,
            createRule: createRule,
            updateRule: updateRule,
            deleteRule: deleteRule,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities
        };

        function getAll() {
            return $http.get(API_URL);
        }

        function getRules(pageNumber, pageSize, searchContext) {
            var url = API_URL + 'filtered?pageNumber=' + pageNumber + '&pageSize=' + pageSize;
            return $http.post(url, searchContext);
        }

        function getRule(id) {
            return $http.get(API_URL + id);
        }

        function createRule(firmwareRule) {
            return $http.post(API_URL, firmwareRule);
        }

        function updateRule(firmwareRule) {
            return $http.put(API_URL, firmwareRule);
        }

        function deleteRule(id) {
            return $http.delete(API_URL + id);
        }

        function updateSyncEntities(firmwareRules) {
            var requests = utilsService.generateRequestList(firmwareRules, {url: API_URL + 'entities', method: 'PUT'});
           return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(firmwareRules) {
            var requests = utilsService.generateRequestList(firmwareRules, {url: API_URL + 'entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }
    }
})();