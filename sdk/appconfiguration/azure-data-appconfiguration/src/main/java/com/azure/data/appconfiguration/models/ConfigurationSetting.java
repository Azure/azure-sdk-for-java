// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.implementation.annotation.Fluent;
import com.azure.core.implementation.util.ImplUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * ConfigurationSetting is a resource identified by unique combination of {@link #key() key} and {@link #label() label}.
 * By default, the label is {@code null}. To explicitly reference the default label use {@link #NO_LABEL}.
 */
@Fluent
public class ConfigurationSetting {
    /**
     * The default label for configuration settings is the label, "\0".
     * Users use this value when they want to explicitly reference a configuration setting that has no label.
     * This gets URL encoded as "%00".
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
    private boolean locked;

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
    public String key() {
        return key;
    }

    /**
     * Sets the key of this configuration setting.
     *
     * @param key The name of the configuration key.
     * @return ConfigurationSetting object itself.
     */
    public ConfigurationSetting key(String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets the label of this configuration setting.
     *
     * @return The label of this setting.
     */
    public String label() {
        return label;
    }

    /**
     * Sets the label of this configuration setting. {@link #NO_LABEL} is the default label used when this value is
     * not set.
     *
     * @param label The label of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting label(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the value of this configuration setting.
     *
     * @return The value of this configuration setting.
     */
    public String value() {
        return value;
    }

    /**
     * Sets the value of this setting.
     *
     * @param value The value to associate with this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the content type of this configuration setting. By default, this content type is null.
     *
     * @return The content type of this setting.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Sets the content type. By default, the content type is null.
     *
     * @param contentType The content type of this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * The etag for this configuration setting.
     *
     * @return etag The etag for the setting.
     */
    public String etag() {
        return etag;
    }

    /**
     * Sets the etag for this configuration setting.
     *
     * @param etag The etag for the configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting etag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * The time when the configuration setting was last modified.
     *
     * @return The time when the configuration was last modified.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * Gets whether or not the configuration setting is locked. If the setting is locked, then no modifications can be
     * made to this setting.
     *
     * This is a <b>readonly</b> property. It is populated from responses from the Azure App Configuration
     * service.
     *
     * @return true if locked; false otherwise.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Gets tags associated with this configuration setting.
     *
     * @return tags Gets tags for this configuration setting.
     */
    public Map<String, String> tags() {
        return tags;
    }

    /**
     * Sets the tags for this configuration setting.
     *
     * @param tags The tags to add to this configuration setting.
     * @return The updated ConfigurationSetting object.
     */
    public ConfigurationSetting tags(Map<String, String> tags) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConfigurationSetting)) {
            return false;
        }

        ConfigurationSetting other = (ConfigurationSetting) o;

        if (!Objects.equals(this.key, other.key)
            || !Objects.equals(this.label, other.label)
            || !Objects.equals(this.value, other.value)
            || !Objects.equals(this.etag, other.etag)
            || !Objects.equals(this.lastModified, other.lastModified)
            || !Objects.equals(this.locked, other.locked)
            || !Objects.equals(this.contentType, other.contentType)
            || ImplUtils.isNullOrEmpty(this.tags) != ImplUtils.isNullOrEmpty(other.tags)) {
            return false;
        }

        if (!ImplUtils.isNullOrEmpty(this.tags)) {
            return Objects.equals(this.tags, other.tags);
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key,
                this.label,
                this.value,
                this.etag,
                this.lastModified,
                this.locked,
                this.contentType,
                this.tags);
    }
}
