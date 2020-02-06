/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.support;

public class UserAgent {
    /**
     * Generate UserAgent string for given service.
     *
     * @param serviceName    Name of the service from which called this method.
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
