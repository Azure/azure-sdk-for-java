// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;

import java.util.Locale;

/**
 * Fields in {@link ConfigurationSetting} that can be returned from GET queries.
 *
 * @see SettingSelector
 * @see ConfigurationAsyncClient
 */
public enum SettingFields {
    /**
     * Populates the {@link ConfigurationSetting#getKey()} from the service.
     */
    KEY,
    /**
     * Populates the {@link ConfigurationSetting#getLabel()} from the service.
     */
    LABEL,
    /**
     * Populates the {@link ConfigurationSetting#getValue()} from the service.
     */
    VALUE,
    /**
     * Populates the {@link ConfigurationSetting#getContentType()} from the service.
     */
    CONTENT_TYPE,
    /**
     * Populates the {@link ConfigurationSetting#getETag()} from the service.
     */
    ETAG,
    /**
     * Populates the {@link ConfigurationSetting#getLastModified()} from the service.
     */
    LAST_MODIFIED,
    /**
     * Populates the {@link ConfigurationSetting#isLocked()} from the service.
     */
    LOCKED,
    /**
     * Populates the {@link ConfigurationSetting#getTags()} from the service.
     */
    TAGS;

    /**
     * Converts the SettingFields to a string that is usable for HTTP requests and logging.
     * @param field SettingFields to map.
     * @return SettingFields as a lowercase string in the US locale.
     */
    public static String toStringMapper(SettingFields field) {
        return field.toString().toLowerCase(Locale.US);
    }
}
