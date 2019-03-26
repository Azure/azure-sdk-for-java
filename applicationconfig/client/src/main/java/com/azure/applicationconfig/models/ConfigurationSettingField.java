// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

/**
 * Fields in {@link ConfigurationSetting} that can be returned from GET queries.
 */
public enum ConfigurationSettingField {
    /**
     * Populates the {@link ConfigurationSetting#key()} from the service.
     */
    KEY,
    /**
     * Populates the {@link ConfigurationSetting#label()} from the service.
     */
    LABEL,
    /**
     * Populates the {@link ConfigurationSetting#value()} from the service.
     */
    VALUE,
    /**
     * Populates the {@link ConfigurationSetting#contentType()} from the service.
     */
    CONTENT_TYPE,
    /**
     * Populates the {@link ConfigurationSetting#etag()} from the service.
     */
    ETAG,
    /**
     * Populates the {@link ConfigurationSetting#lastModified()} from the service.
     */
    LAST_MODIFIED,
    /**
     * Populates the {@link ConfigurationSetting#isLocked()} ()} from the service.
     */
    LOCKED,
    /**
     * Populates the {@link ConfigurationSetting#tags()} from the service.
     */
    TAGS,
}
