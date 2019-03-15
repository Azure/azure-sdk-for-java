// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Keys that exist in the configuration store.
 */
public class Key {
    @JsonProperty(value = "name")
    private String name;

    @JsonProperty(value = "kv_count")
    private int kvCount;

    @JsonProperty(value = "etag")
    private String etag;

    @JsonProperty(value = "last_modified")
    private OffsetDateTime lastModified;

    /**
     * @return label name
     */
    public String name() {
        return name;
    }

    /**
     * Sets the name.
     * @param name key name
     * @return Key object itself
     */
    public Key withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return number of ConfigurationSetting's associated with this key
     */
    public int kvCount() {
        return kvCount;
    }

    /**
     * Sets the number of ConfigurationSetting's associated with this key
     * @param kvCount number of ConfigurationSetting's
     * @return Key object itself
     */
    public Key withKvCount(int kvCount) {
        this.kvCount = kvCount;
        return this;
    }

    /**
     * @return etag
     */
    public String etag() {
        return etag;
    }

    /**
     * Sets the etag.
     * @param etag etag
     * @return Key object itself
     */
    public Key withEtag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * @return the time when last modified
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * Sets when Key was last modified.
     * @param lastModified lastModified
     * @return Key object itself
     */
    public Key withLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }
}
