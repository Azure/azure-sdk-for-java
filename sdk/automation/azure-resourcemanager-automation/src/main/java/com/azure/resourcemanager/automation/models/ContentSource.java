// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Definition of the content source. */
@Fluent
public final class ContentSource {
    /*
     * Gets or sets the hash.
     */
    @JsonProperty(value = "hash")
    private ContentHash hash;

    /*
     * Gets or sets the content source type.
     */
    @JsonProperty(value = "type")
    private ContentSourceType type;

    /*
     * Gets or sets the value of the content. This is based on the content source type.
     */
    @JsonProperty(value = "value")
    private String value;

    /*
     * Gets or sets the version of the content.
     */
    @JsonProperty(value = "version")
    private String version;

    /**
     * Get the hash property: Gets or sets the hash.
     *
     * @return the hash value.
     */
    public ContentHash hash() {
        return this.hash;
    }

    /**
     * Set the hash property: Gets or sets the hash.
     *
     * @param hash the hash value to set.
     * @return the ContentSource object itself.
     */
    public ContentSource withHash(ContentHash hash) {
        this.hash = hash;
        return this;
    }

    /**
     * Get the type property: Gets or sets the content source type.
     *
     * @return the type value.
     */
    public ContentSourceType type() {
        return this.type;
    }

    /**
     * Set the type property: Gets or sets the content source type.
     *
     * @param type the type value to set.
     * @return the ContentSource object itself.
     */
    public ContentSource withType(ContentSourceType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the value property: Gets or sets the value of the content. This is based on the content source type.
     *
     * @return the value value.
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value property: Gets or sets the value of the content. This is based on the content source type.
     *
     * @param value the value value to set.
     * @return the ContentSource object itself.
     */
    public ContentSource withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the version property: Gets or sets the version of the content.
     *
     * @return the version value.
     */
    public String version() {
        return this.version;
    }

    /**
     * Set the version property: Gets or sets the version of the content.
     *
     * @param version the version value to set.
     * @return the ContentSource object itself.
     */
    public ContentSource withVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (hash() != null) {
            hash().validate();
        }
    }
}
