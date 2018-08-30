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
 * Author: obaturynskyi
 * Created: 25.06.2015  17:55
 */
package com.comcast.xconf;

import com.comcast.hesperius.dataaccess.core.cache.CacheManager;
import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.xconf.dcm.converter.DcmRuleConverterTest;
import com.comcast.xconf.dcm.formula.*;
import com.comcast.xconf.dcm.ruleengine.TelemetryProfileControllerTest;
import com.comcast.xconf.dcm.ruleengine.TelemetryProfileService;
import com.comcast.xconf.estbfirmware.EnvironmentServiceTest;
import com.comcast.xconf.estbfirmware.EstbFirmwareControllerTest;
import com.comcast.xconf.estbfirmware.EstbFirmwareRuleBaseTest;
import com.comcast.xconf.estbfirmware.PercentFilterServiceTest;
import com.comcast.xconf.estbfirmware.converter.DownloadLocationFilterConverterTest;
import com.comcast.xconf.estbfirmware.converter.NgRuleConverterTest;
import com.comcast.xconf.estbfirmware.converter.TimeFilterConverterTest;
import com.comcast.xconf.estbfirmware.evaluation.PercentFilterTest;
import com.comcast.xconf.estbfirmware.evaluation.percentfilter.*;
import com.comcast.xconf.estbfirmware.legacy.IpFilterLegacyConverterTest;
import com.comcast.xconf.estbfirmware.legacy.MacRuleLegacyConverterTest;
import com.comcast.xconf.estbfirmware.legacy.RebootImmediatelyLegacyConverterTest;
import com.comcast.xconf.estbfirmware.legacy.TimeFilterLegacyConverterTest;
import com.comcast.xconf.estbfirmware.migration.MigrationControllerTest;
import com.comcast.xconf.featurecontrol.FeatureControlSettingTest;
import com.comcast.xconf.filter.UpdateDeleteApiFilterTest;
import com.comcast.xconf.queries.controllers.*;
import com.comcast.xconf.telemetry.LogUploaderControllerTest;
import com.comcast.xconf.telemetry.TelemetryProfileServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DCMTestCaseFrom1To3.class, DCMTestCaseFrom4To7.class,
        DCMTestCaseFrom8To10.class, DCMTestCaseFrom11To14.class,
        DCMTestCaseForm15to17.class, DCMTestCaseFrom18To21.class,
        DCMTestCase22.class, DCMTestCase23.class,
        DCMTestCase24.class, DCMTestCase25.class,
        DCMTestCase26.class, DCMTestCase27.class,
        DCMTestCase28.class, DCMTestCase29.class,
        DCMTestCase30.class, DCMTestCase31.class,
        DCMTestCase32.class, DCMTestCase33.class,
        DcmMultipleApplicationTypesTest.class,
        EstbFirmwareRuleBaseTest.class, EstbFirmwareControllerTest.class,
        TelemetryProfileServiceTest.class, TelemetryProfileControllerTest.class,
        PercentFilterTest.class, LogUploaderControllerTest.class,
        BlockingFilterQueriesControllerTest.class, EnvModelQueriesControllerTest.class,
        EnvModelRuleQueriesControllerTest.class, FirmwareConfigQueriesControllerTest.class,
        IpAddressGroupsQueriesControllerTest.class, IpRuleQueriesControllerTest.class,
        LocationQueriesControllerTest.class, MacRuleQueriesControllerTest.class,
        NsListQueriesControllerTest.class, RiFilterQueriesControllerTest.class,
        DcmRuleConverterTest.class,
        DownloadLocationFilterConverterTest.class,
        TimeFilterConverterTest.class, TimeFilterLegacyConverterTest.class,
        IpFilterLegacyConverterTest.class, MacRuleLegacyConverterTest.class,
        RebootImmediatelyLegacyConverterTest.class,
        EnvironmentServiceTest.class, PercentFilterServiceTest.class,
        NgRuleConverterTest.class, MigrationControllerTest.class,
        UpdateDeleteApiFilterTest.class,
        PercentFilterTestCase1.class, PercentFilterTestCase2.class, PercentFilterTestCase3.class,
        PercentFilterTestCase4.class, PercentFilterTestCase5.class, PercentFilterTestCase6.class,
        FeatureControlSettingTest.class,
        PercentageBeanQueriesControllerTest.class
})
public class CompleteTestSuite {

    private static EmbCassandra embCassandra;

    public static final long telemetryProfileServiceExpireTimeMs = 1000L;
    public static final long timeToWaitExpiration = telemetryProfileServiceExpireTimeMs + 500L;

    @BeforeClass
    public static void startEmbeddedCassandra() throws IOException {
        embCassandra = new EmbCassandra();
        embCassandra.start();
        CacheManager.initCaches(CoreUtil.CF_DEFINITIONS);
        TelemetryProfileService.expireTime = telemetryProfileServiceExpireTimeMs;
    }

    @AfterClass
    public static void stopEmbeddedCassandra() throws IOException, InterruptedException {
        if (embCassandra != null) {
            embCassandra.stop();
        }
    }
}
