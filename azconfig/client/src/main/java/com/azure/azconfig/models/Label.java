/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.azconfig.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Labels may be assigned to a KeyValue.
 */
public class Label {
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
     * @param name label name
     * @return Label object itself
     */
    public Label withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return number of KeyValue's associated with this label
     */
    public int kvCount() {
        return kvCount;
    }

    /**
     * Sets the number of KeyValue's associated with this label
     * @param kvCount number of KeyValue's
     * @return Label object itself
     */
    public Label withKvCount(int kvCount) {
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
     * @return Label object itself
     */
    public Label withEtag(String etag) {
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
     * Sets when Label was last modified.
     * @param lastModified lastModified
     * @return Label object itself
     */
    public Label withLastModified(OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }
}
