// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;

import java.util.Locale;

/**
 * Fields in {@link ConfigurationSetting} that can be returned from GET queries.
 *
 * @see SettingSelector
 * @see ConfigurationAsyncClient
 */
public final class SettingFields extends ExpandableStringEnum<SettingFields> {
    /**
     * Populates the {@link ConfigurationSetting#getKey()} from the service.
     */
    public static final SettingFields KEY = fromString("KEY");
    /**
     * Populates the {@link ConfigurationSetting#getLabel()} from the service.
     */
    public static final SettingFields LABEL = fromString("LABEL");
    /**
     * Populates the {@link ConfigurationSetting#getValue()} from the service.
     */
    public static final SettingFields VALUE = fromString("VALUE");
    /**
     * Populates the {@link ConfigurationSetting#getContentType()} from the service.
     */
    public static final SettingFields CONTENT_TYPE = fromString("CONTENT_TYPE");
    /**
     * Populates the {@link ConfigurationSetting#getETag()} from the service.
     */
    public static final SettingFields ETAG = fromString("ETAG");
    /**
     * Populates the {@link ConfigurationSetting#getLastModified()} from the service.
     */
    public static final SettingFields LAST_MODIFIED = fromString("LAST_MODIFIED");
    /**
     * Populates the {@link ConfigurationSetting#isReadOnly()} from the service.
     */
    public static final SettingFields IS_READ_ONLY = fromString("LOCKED");
    /**
     * Populates the {@link ConfigurationSetting#getTags()} from the service.
     */
    public static final SettingFields TAGS = fromString("TAGS");
    /**
     * Converts the SettingFields to a string that is usable for HTTP requests and logging.
     * @param field SettingFields to map.
     * @return SettingFields as a lowercase string in the US locale.
     */
    public static String toStringMapper(SettingFields field) {
        return field.toString().toLowerCase(Locale.US);
    }
    /**
     * Creates or finds a {@link SettingFields} from its string representation.
     * @param name a name to look for
     * @return the corresponding {@link SettingFields}
     */
    public static SettingFields fromString(String name) {
        return fromString(name, SettingFields.class);
    }
}
