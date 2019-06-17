// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

public final class TrackingUtil {
    public static final String TRACKING_ID_TOKEN_SEPARATOR = "_";

    private TrackingUtil() {
    }

    /**
     * parses ServiceBus role identifiers from trackingId
     * @return null if no roleIdentifier found
     */
    static String parseRoleIdentifier(final String trackingId) {
        if (StringUtil.isNullOrWhiteSpace(trackingId) || !trackingId.contains(TRACKING_ID_TOKEN_SEPARATOR)) {
            return null;
        }

        return trackingId.substring(trackingId.indexOf(TRACKING_ID_TOKEN_SEPARATOR));
    }
}
