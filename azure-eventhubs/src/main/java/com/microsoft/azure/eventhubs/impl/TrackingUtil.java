/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Session;

import java.time.Instant;

public final class TrackingUtil {
    public static final String TRACKING_ID_TOKEN_SEPARATOR = "_";

    private TrackingUtil() {
    }

    /**
     * parses ServiceBus role identifiers from trackingId
     *
     * @return null if no roleIdentifier found
     */
    static String parseRoleIdentifier(final String trackingId) {
        if (StringUtil.isNullOrWhiteSpace(trackingId) || !trackingId.contains(TRACKING_ID_TOKEN_SEPARATOR)) {
            return null;
        }

        return trackingId.substring(trackingId.indexOf(TRACKING_ID_TOKEN_SEPARATOR));
    }

    public static String getLinkName(final Session session) {
        // returned linkName lookslike: ea9cac_8b_G27_1479943074829
        final String linkNamePrefix = StringUtil.getRandomString();
        final String linkNameWithServiceRoleTracker = session.getConnection() != null && !StringUtil.isNullOrEmpty(session.getConnection().getRemoteContainer()) ?
                linkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(session.getConnection().getRemoteContainer()
                        .substring(Math.max(session.getConnection().getRemoteContainer().length() - 7, 0), session.getConnection().getRemoteContainer().length())) :
                linkNamePrefix;
        return linkNameWithServiceRoleTracker.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(String.valueOf(Instant.now().toEpochMilli()));
    }
}
