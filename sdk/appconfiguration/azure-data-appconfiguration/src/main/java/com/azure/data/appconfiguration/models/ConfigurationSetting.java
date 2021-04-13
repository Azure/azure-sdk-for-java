// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * ConfigurationSetting is a resource identified by unique combination of {@link #getKey() key} and {@link #getLabel()
 * label}. By default, the label is {@code null}. To explicitly reference the default label use {@link #NO_LABEL}.
 */
@Fluent
public class ConfigurationSetting {
    /**
     * The default label for configuration settings is the label, "\0". Users use this value when they want to
     * explicitly reference a configuration setting that has no label. This gets URL encoded as "%00".
     */
    public static final String NO_LABEL = "\0";

    @JsonProperty(value = "key", required = true)
    private String key;

    @JsonProperty(value = "label")
    private String label;

    @JsonProperty(value = "value", required = true)
    private String value;

    @JsonProperty(value = "content_type")
    private String contentType;

    @JsonProperty(value = "etag")
    private String etag;

    @JsonProperty(value = "last_modified")
    private OffsetDateTime lastModified;

    @JsonProperty(value = "locked")
    private boolean readOnly;

    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * Creates an instance of the configuration setting.
     */
    public ConfigurationSetting() {
    }

    /**
     * Gets the key name for this configuration setting.
     *
     * @return The key for this configuration setting.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of this configuration setting.
     *
     * @param key The name of the configuration key.
     * @return ConfigurationSetting object itself.
     */
    public ConfigurationSetting setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets the label of this configuration setting.
     *
     * @return The label of this setting.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this configuration setting. {@link #NO_LABEL} is the default label used when this value is not
     * set.
     *
     * @param label The label of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the value of this configuration setting.
     *
     * @return The value of this configuration setting.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this setting.
     *
     * @param value The value to associate with this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the content type of this configuration setting. By default, this content type is null.
     *
     * @return The content type of this setting.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type. By default, the content type is null.
     *
     * @param contentType The content type of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * The ETag for this configuration setting.
     *
     * @return etag The ETag for the setting.
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets the ETag for this configuration setting.
     *
     * @param etag The ETag for the configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * The time when the configuration setting was last modified.
     *
     * @return The time when the configuration was last modified.
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets whether or not the configuration setting is read-only. If it is, then no modifications can be
     * made to this setting.
     *
     * This is a <b>readonly</b> property. It is populated from responses from the Azure App Configuration service.
     *
     * @return true if read-only; false otherwise.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Gets tags associated with this configuration setting.
     *
     * @return tags Gets tags for this configuration setting.
     */
    public Map<String, String> getTags() {
        return tags == null ? Collections.emptyMap() : tags;
    }

    /**
     * Sets the tags for this configuration setting.
     *
     * @param tags The tags to add to this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public String toString() {
        return String.format("ConfigurationSetting(key=%s, label=%s, value=%s, etag=%s)",
            this.key,
            this.label,
            this.value,
            this.etag);
    }
}
