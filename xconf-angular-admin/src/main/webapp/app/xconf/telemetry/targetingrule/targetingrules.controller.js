/*******************************************************************************
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
 *******************************************************************************/
(function() {
    'use strict';

    angular
        .module('app.targetingrule')
        .controller('TargetingRulesController', controller);

    controller.$inject = ['$scope', '$controller', 'targetingRuleService', 'alertsService', 'utilsService', 'dialogs', 'PermanentProfileService', 'paginationService', 'RULE_SEARCH_OPTIONS'];

    function controller($scope, $controller, targetingRuleService, alertsService, utilsService, dialogs, PermanentProfileService, paginationService, RULE_SEARCH_OPTIONS) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.rules = [];
        vm.deleteRule = deleteRule;
        vm.profiles = [];
        vm.exportOne = exportOne;
        vm.exportAll = exportAll;
        vm.searchParam = {};
        vm.searchOptions = RULE_SEARCH_OPTIONS;
        vm.getRules = getRules;
        vm.paginationStorageKey = 'targetingRulePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.shiftItems = shiftItems;

        init();

        function init() {
            getRules();
        }

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                init();
            }
        });

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            getRules();
        });

        function getRules() {
            targetingRuleService.getPage(vm.pageSize, vm.pageNumber, vm.searchParam)
                .then(function (result) {
                    vm.rules = result.data;
                    vm.generalItemsNumber = result.headers['numberofitems'];
                    paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                },
                function (error) {
                    alertsService.showError({title: 'Error', message: 'Error by loading targeting rule list.'});
                }
            );
        }

        PermanentProfileService.getAll()
            .then(function(resp) {
                vm.profiles = resp.data;
            }, function(error) {
                alertsService.showError({title: 'Error', message: 'Error by loading profiles.'});
            });

        function deleteRule(rule) {
            if (rule && rule.id) {
                var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Targeting rule ' + rule.name + ' ? </span>');
                dialog.result.then(function (btn) {
                    targetingRuleService.deleteTargetingRule(rule.id).then(function () {
                        utilsService.removeItemFromArray(vm.rules, rule);
                        alertsService.successfullyDeleted(rule.name);
                        shiftItems();
                    }, function (error) {
                        alertsService.showError({title: 'Error', message: error.data});
                    });
                });
            }
        }

        function exportOne(id) {
            window.open('telemetry/rule/' + id + '?export');
        }

        function exportAll() {
            window.open('telemetry/rule' + '?export');
        }

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getGeneralItemsNumber() - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getRules();
        }

        function startParse() {
            return getGeneralItemsNumber() > 0;
        }

        function getGeneralItemsNumber() {
            return vm.generalItemsNumber;
        }

        function searchTelemetryRulesByContext() {
            targetingRuleService.searchTelemetryRulesByContext(vm.pageSize,vm.pageNumber, vm.searchContext).then(function (resp) {
                vm.rules = resp.data;
                console.log(resp);
                vm.generalItemsNumber = resp.headers['numberofitems'];
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            }, function (error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }
    }
})();