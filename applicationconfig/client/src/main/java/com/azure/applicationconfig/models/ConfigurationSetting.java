// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * ConfigurationSetting is a resource identified by unique combination of key + label. Label can be null.
 * To explicitly reference null label use "\0" (url encoded as %00).
 */
public class ConfigurationSetting {
    /**
     * The default label for configuration settings is the null label, "\0".
     * This gets URL encoded as "%00".
     */
    public static final String NULL_LABEL = "\0";

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
     * @return key name
     */
    public String key() {
        return key;
    }

    /**
     * Sets the key.
     * @param key key name
     * @return ConfigurationSetting object itself
     */
    public ConfigurationSetting key(String key) {
        this.key = key;
        return this;
    }

    /**
     * @return label
     */
    public String label() {
        return label;
    }

    /**
     * Sets the label.
     * @param label label
     * @return ConfigurationSetting object itself
     */
    public ConfigurationSetting label(String label) {
        this.label = label;
        return this;
    }

    /**
     * @return key value
     */
    public String value() {
        return value;
    }

    /**
     * Sets the value.
     * @param value value
     * @return ConfigurationSetting object itself
     */
    public ConfigurationSetting value(String value) {
        this.value = value;
        return this;
    }

    /**
     * @return content type
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType content type
     * @return ConfigurationSetting object itself
     */
    public ConfigurationSetting contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @return etag
     */
    public String etag() {
        return etag;
    }

    /**
     * @return the time when last modified
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return true if locked; false otherwise
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @return tags
     */
    public Map<String, String> tags() {
        return tags;
    }

    /**
     * Sets the tags.
     * @param tags tags
     * @return ConfigurationSetting object itself
     */
    public ConfigurationSetting withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
