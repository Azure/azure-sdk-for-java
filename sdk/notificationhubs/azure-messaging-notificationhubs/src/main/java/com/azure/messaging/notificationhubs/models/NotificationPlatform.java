// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationPlatform {
    ADM("adm"),
    APPLE("apns"),
    BAIDU("baidu"),
    BROWSER("browser"),
    FCM("gcm"),
    MPNS("mpns"),
    WINDOWS("wns");

    /** The actual serialized value for a NotificationPlatform instance. */
    private final String value;

    NotificationPlatform(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a TimeGranularity instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed TimeGranularity object, or null if unable to parse.
     */
    @JsonCreator
    public static NotificationPlatform fromString(String value) {
        NotificationPlatform[] items = NotificationPlatform.values();
        for (NotificationPlatform item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
