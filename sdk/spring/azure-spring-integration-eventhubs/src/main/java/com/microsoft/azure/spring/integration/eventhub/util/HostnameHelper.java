// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameHelper {

    private static final String UNKNOWN_HOST_NAME = "Unknown-HostName";

    private static final String HOST_NAME = getNetworkHostname();

    private static String getNetworkHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignore) {
            return UNKNOWN_HOST_NAME;
        }
    }

    public static String getHostname() {
        return HOST_NAME;
    }
}
