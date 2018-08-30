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
 *  Created: 11/9/15 6:34 PM
 */

(function() {
    'use strict';

    angular
        .module('app.roundrobinfilter')
        .controller('RoundRobinFilterController', controller);

    controller.$inject = ['$rootScope', '$scope', '$controller', 'roundRobinFilterService', 'alertsService'];

    function controller($rootScope, $scope, $controller, roundRobinFilterService, alertsService) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.filter = null;
        vm.firmwareVersions = {
            firstPart: [],
            lastPart: []
        };
        vm.exportFilter = roundRobinFilterService.exportFilter;
        vm.showMore = false;

        init();

        function init() {
            roundRobinFilterService.getFilter($rootScope.applicationType).then(function(resp) {
                vm.filter = resp.data;
                vm.showMore = false;
                if (vm.filter.firmwareVersions) {
                    var firmwareVersionsArray = vm.filter.firmwareVersions.split('\n');
                    firmwareVersionsArray.sort();
                    if (firmwareVersionsArray.length > 10) {
                        vm.firmwareVersions.firstPart = firmwareVersionsArray.slice(0, 10);
                        vm.firmwareVersions.lastPart = firmwareVersionsArray.slice(10, firmwareVersionsArray.length);
                    } else {
                        vm.firmwareVersions.firstPart = firmwareVersionsArray;
                    }
                }
            }, alertsService.errorHandler);
        }
    }

})();