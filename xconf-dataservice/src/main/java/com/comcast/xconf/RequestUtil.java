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
package com.comcast.xconf;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.hesperius.dataaccess.core.config.ConfigurationProvider;
import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * User: ikostrov
 * Date: 09.10.14
 * Time: 18:00
 */
public class RequestUtil {

    private static final Logger log = LoggerFactory.getLogger(RequestUtil.class);

    public static final ImmutableList<String> XFF_HEADERS;
    public static final String XFF_HEADER_NAME = "X-Forwarded-For";

    public static final String PARTNER_PARAM = "partnerId";
    public static final String[] PARTNER_HEADERS = {"X-Partner","X-PARTNER-ID", "X-PartnerId"};

    static {
        XconfSpecificConfig specificConfig = ConfigurationProvider.getConfiguration().getSpecificConfig();
        String configHeaderName = specificConfig.getHaProxyHeaderName();

        // order is important here: first should be XFF, then HAFF
        final ArrayList<String> xff = new ArrayList<>();
        xff.add(XFF_HEADER_NAME);
        if (StringUtils.isNotBlank(configHeaderName)) {
            xff.add(configHeaderName);
        }
        XFF_HEADERS = ImmutableList.copyOf(xff);
        log.info("Will use such headers for identifying the originating IP address: " + XFF_HEADERS);
    }

    /**
     * First we check 'X-Forwarded-For' header, then 'HA-Forwarded-For' if it exists and contains valid ip address.
     * Usually format of header is 'X-Forwarded-For: client, proxy1, proxy2' so we split string by "[,]" and take first part.
     * @param req http request info
     * @return valid ip address or null
     */
    public static String grepIpAddressFromXFF(HttpServletRequest req) {
        for (String xffHeaderName : XFF_HEADERS) {
            String header = req.getHeader(xffHeaderName);
            if (StringUtils.isNotBlank(header)) {
                String[] split = header.split("[,]");
                if (split.length > 0
                        && StringUtils.isNotBlank(split[0])
                        && IpAddress.isValid(split[0].trim())) {

                    return split[0].trim();
                }
            }
        }

        return null;
    }

    /**
     * Most important is IP from 'X-Forwarded-For' or 'HA-Forwarded-For' header. If it's valid we use it.
     * If not then check value from context. If valid - use it.
     * If not then read remote address from request info. If valid - use it.
     * At edge case when nothing above is a correct IP address then we fallback to '0.0.0.0'
     * @param contextIpAddress ip address from request context
     * @param req http request meta info
     */
    public static String findValidIpAddress(HttpServletRequest req, String contextIpAddress) {

        // check XFF/HAFF header if it contains IpAddress
        String ipFromHeader = RequestUtil.grepIpAddressFromXFF(req);

        if (IpAddress.isValid(contextIpAddress)) {

            if (log.isDebugEnabled()) {
                log.debug("supplied valid IP in context: " + contextIpAddress);
            }
            return contextIpAddress;

        } else if (IpAddress.isValid(ipFromHeader)) {

            if (log.isDebugEnabled()) {
                log.debug("supplied valid IP address in XFF header: " + ipFromHeader);
            }
            return ipFromHeader;

        } else if (IpAddress.isValid(req.getRemoteAddr())) {

            if (ipFromHeader != null) {
                log.warn("invalid IP is specified in XFF header: '%s'", ipFromHeader);
            }
            if (contextIpAddress != null) {
                log.warn("invalid IP is specified in context: '%s'", contextIpAddress);
            }

            if (log.isDebugEnabled()) {
                log.debug("using IP from request remote address: " + req.getRemoteAddr());
            }

            return req.getRemoteAddr();
        } else {

            log.warn("using 0.0.0.0 because IP was invalid in XFF, context, request remote address");
            return "0.0.0.0";
        }
    }

    public static void normalizeContext(LogUploaderContext context) {
        if (context.getEnv() != null) {
            context.setEnv(context.getEnv().toUpperCase());
        }

        if (context.getModel() != null) {
            context.setModel(context.getModel().toUpperCase());
        }

        if (context.getPartner() != null) {
            context.setPartner(context.getPartner().toUpperCase());
        }

        if (context.getEstbMacAddress() == null) {
            context.setEstbMacAddress(MacAddressUtil.normalizeMacAddress("11:11:11:11:11:11"));
        } else {
            context.setEstbMacAddress(MacAddressUtil.normalizeMacAddress(context.getEstbMacAddress()));
        }
        if (context.getEcmMacAddress() != null) {
            context.setEcmMacAddress(MacAddressUtil.normalizeMacAddress(context.getEcmMacAddress()));
        }
    }
}
