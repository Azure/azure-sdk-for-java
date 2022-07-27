// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs;

import com.azure.core.util.ServiceVersion;

/** Service version of NotificationHubService. */
public enum NotificationHubServiceVersion implements ServiceVersion {
    /** Enum value 2020-16. */
    V2020_16("2020-06");

    private final String version;

    NotificationHubServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library.
     *
     * @return The latest {@link NotificationHubServiceVersion}.
     */
    public static NotificationHubServiceVersion getLatest() {
        return V2020_16;
    }
}
