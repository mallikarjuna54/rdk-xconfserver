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
 */
package com.comcast.xconf.admin.contextconfig;

import com.comcast.hesperius.dataaccess.core.cache.support.dao.ChangedKeysProcessingDAO;
import com.comcast.hesperius.dataaccess.core.config.ConfigurationProvider;
import com.comcast.hesperius.dataaccess.core.config.DataServiceConfiguration;
import com.comcast.hesperius.dataaccess.core.util.CoreUtil;
import com.comcast.xconf.contextconfig.DatastoreContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DatastoreContext.class})
public class RootContext extends AdminWebConfig {
    @Bean
    ChangedKeysProcessingDAO changeLogDao() {
        final DataServiceConfiguration.CacheConfiguration cacheConfig = ConfigurationProvider.getConfiguration().getCacheConfiguration();
        String projectName = CoreUtil.dsconfig.getDomainClassesBasePackage();
        projectName = projectName.substring(projectName.lastIndexOf(".") + 1);
        return new ChangedKeysProcessingDAO(StringUtils.capitalize(projectName), cacheConfig.getChangedKeysTimeWindowSize());
    }
}

