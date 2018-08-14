package com.csc.test_agent.util;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);
    public static String DEFAULT_LOCAL_HOST_ADDRESS = getLocalHostAddress();
    public static String DEFAULT_LOCAL_HOST_NAME = getLocalHostName();

    static String getLocalHostAddress() {
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (Exception e) {
            LOGGER.error("Error while get localhost address", e);
        }
        if (localHost != null && !localHost.isLoopbackAddress()) {
            return localHost.getHostAddress();
        }
        return null;
    }



    public static String getLocalHostName() {
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            LOGGER.error("Error while get localhost name", e);
        }
        if (hostName != null && !"localhost".equals(hostName)) {
            return hostName;
        }
        return hostName;
    }
}
