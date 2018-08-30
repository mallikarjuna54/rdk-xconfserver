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
        .module('app.testpage')
        .controller('TestPageController', controller);

    controller.$inject = ['testPageService', 'alertsService', 'LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE', 'testPageUtils'];

    function controller(testPageService, alertsService, LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE, testPageUtils) {
        var vm = this;
        vm.formula = {};
        vm.matchedSettings = null;
        vm.matchedRuleIds = null;
        vm.matchedRuleType = null;
        vm.parameters = [{key: '', value: ''}];
        vm.autoCompleteValues = LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.context = null;

        vm.matchRule = matchRule;

        function matchRule() {
            if (testPageUtils.validateInput(vm.parameters)) {
                var objParams = testPageUtils.getParametersAsObject(vm.parameters);
                testPageService.matchRule(objParams).then(function (resp) {
                    vm.matchedSettings = resp.data.settings;
                    vm.matchedRuleType = resp.data.ruleType;
                    vm.matchedRuleIds = resp.data.ruleIds;
                    vm.context = resp.data.context;
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }
    }
})();