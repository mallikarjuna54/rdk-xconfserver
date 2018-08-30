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
package com.comcast.hesperius.dataaccess.support.services;

import com.comcast.hesperius.dataaccess.core.CustomConnectionPoolMonitor;
import com.comcast.hesperius.dataaccess.core.ServiceInfo;
import com.comcast.hesperius.dataaccess.core.cache.CacheManager;
import com.comcast.hesperius.dataaccess.core.cache.mbean.CacheInfo;
import com.comcast.hesperius.dataaccess.core.config.ConfigurationProvider;
import com.comcast.hesperius.dataaccess.core.config.DataServiceConfiguration;
import com.comcast.hesperius.dataaccess.support.cache.HashChecker;
import com.comcast.hesperius.dataaccess.support.cache.HashCheckerResult;
import com.comcast.hesperius.dataaccess.support.exception.WebAppException;
import com.netflix.astyanax.connectionpool.ConnectionPoolMonitor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
@CrossOrigin
public class DataServiceInfo {
    private static final Logger log = LoggerFactory.getLogger(DataServiceInfo.class);
    public static final String OK = "Ok";

    protected ServiceInfo serviceInfo;

    @RequestMapping(value = "/version", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ServiceInfo getVersion() {
        if (serviceInfo == null) {
            serviceInfo = obtainServiceInfo();
        }

        return serviceInfo;
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public DataServiceConfiguration getConfig() {
        return ConfigurationProvider.getConfiguration();
    }

    @RequestMapping(value = "/heartBeat", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String checkHeartbeat() {
        return OK;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ConnectionPoolMonitor getStatus() {
        return CustomConnectionPoolMonitor.getInstance();
    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Statistics statistics() {
        return new Statistics(CacheManager.getCacheMBeans());
    }

    @RequestMapping(value = "/refresh/{cfName}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String refresh(@PathVariable(value = "cfName") String cfName) {
        boolean result = CacheManager.refreshAll(cfName);
        if (result) {
            return OK;
        } else {
            throw new WebAppException("Not found CF definition: " + cfName, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/refreshAll", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String refreshAll() {
        List<String> result = CacheManager.refreshAll();
        if (result.isEmpty()) {
            return OK;
        } else {
            throw new WebAppException("Couldn't refresh caches for column families: " + result, HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/hash", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String hash() {
        return Long.toString(CacheManager.calculateHash());
    }

    @RequestMapping(value = "/hash/{cfName}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String hash(@PathVariable(value = "cfName") String cfName) {
        return Long.toString(CacheManager.calculateHash(cfName));
    }

    @RequestMapping(value = "/hash/{cfName}/{itemId}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String hash(@PathVariable(value = "cfName") String cfName, @PathVariable(value = "itemId") String itemId) {
        return Long.toString(CacheManager.calculateHash(cfName, itemId));
    }

    //http://localhost:9090/appdiscoveryDataService/info/hashHosts?hosts[]=a,b
    @RequestMapping(value = "/hashHosts", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HashCheckerResult hashHosts(@RequestParam("hosts[]") String[] hosts) {
        return new HashChecker(hosts).checkHash();
    }

    private ServiceInfo obtainServiceInfo() {

        try {
            Configuration config = new PropertiesConfiguration(ServiceInfo.CONFIG_FILE_NAME);
            return new ServiceInfo(config);
        } catch (ConfigurationException ce) {
            log.warn("Failed to load configuration file: {}", ServiceInfo.CONFIG_FILE_NAME);
        } catch (Exception e) {
            log.warn("Exception appears while configuration file is loading", e);
        }

        throw new WebAppException(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @XmlRootElement
    public static class Statistics {
        public Map<String, CacheInfo> cacheMap = new HashMap<>();

        public Statistics() {}

        public Statistics(Map<String, CacheInfo> cacheMap) {
            this.cacheMap = cacheMap;
        }
    }

}

