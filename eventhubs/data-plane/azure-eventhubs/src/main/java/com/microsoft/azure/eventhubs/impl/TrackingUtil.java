// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Session;

public final class TrackingUtil {
    public static final String TRACKING_ID_TOKEN_SEPARATOR = "_";

    private TrackingUtil() {
    }

    public static String getLinkName(final Session session) {
        // LN_1479943074829_ea9cac_8b_G27
        final String linkNamePrefix = StringUtil.getRandomString("LN");
        return session.getConnection() != null && !StringUtil.isNullOrEmpty(session.getConnection().getRemoteContainer())
                ? linkNamePrefix.concat(TrackingUtil.TRACKING_ID_TOKEN_SEPARATOR).concat(session.getConnection().getRemoteContainer()
                        .substring(Math.max(session.getConnection().getRemoteContainer().length() - 7, 0)))
                : linkNamePrefix;
    }
}
