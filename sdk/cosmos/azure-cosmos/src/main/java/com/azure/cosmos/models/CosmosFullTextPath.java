// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path settings within {@link CosmosFullTextPolicy}
 */
public class CosmosFullTextPath {
    @JsonProperty(Constants.Properties.LANGUAGE)
    private String path;
    @JsonProperty(Constants.Properties.PATH)
    private String language;
    private JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosFullTextPath() { this.jsonSerializable = new JsonSerializable(); }

    /**
     * Gets the path for the cosmosFullText.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path for the cosmosFullText.
     *
     * @param path the path for the cosmosFullText.
     * @return CosmosFullTextPath
     */
    public CosmosFullTextPath setPath(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new NullPointerException("full text search path is either null or empty");
        }

        if (path.charAt(0) != '/' || path.lastIndexOf('/') != 0) {
            throw new IllegalArgumentException("");
        }

        this.path = path;
        return this;
    }

    /**
     * Gets the language for the cosmosFullText path.
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language for the cosmosFullText path.
     * @param language the language for the cosmosFullText path.
     * @return CosmosFullTextPath
     */
    public CosmosFullTextPath setLanguage(String language) {
        this.language = language;
        return this;
    }
}
