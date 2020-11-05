// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.support;

/**
 * Util class to generate user agent.
 */
public class UserAgent {
    /**
     * Generate UserAgent string for given service.
     * @param serviceName Name of the service from which called this method.
     * @param allowTelemetry Whether allows telemtry
     * @return generated UserAgent string
     */
    public static String getUserAgent(String serviceName, boolean allowTelemetry) {
        String macAddress = "Not Collected";
        if (allowTelemetry) {
            macAddress = GetHashMac.getHashMac();
        }

        return String.format(serviceName + " MacAddressHash:%s", macAddress);
    }
}
