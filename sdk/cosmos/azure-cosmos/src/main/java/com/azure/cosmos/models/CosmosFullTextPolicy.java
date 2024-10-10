// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Full Text Search Policy
 */
public class CosmosFullTextPolicy {
    @JsonProperty(Constants.Properties.DEFAULT_LANGUAGE)
    private String defaultLanguage;
    @JsonProperty(Constants.Properties.PATHS)
    private List<CosmosFullTextPath> paths;
    private JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public CosmosFullTextPolicy() { this.jsonSerializable = new JsonSerializable(); }

    /**
     * Gets the default language for cosmosFullText.
     *
     * @return the default language for cosmosFullText.
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Sets the default language for cosmosFullText.
     * @param defaultLanguage the default language for cosmosFullText.
     * @return CosmosFullTextPolicy
     */
    public CosmosFullTextPolicy setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }

    /**
     * Gets the paths for cosmosFulltext.
     * @return the paths for cosmosFulltext.
     */
    public List<CosmosFullTextPath> getPaths() {
        return paths;
    }

    /**
     * Sets the paths for cosmosFulltext.
     * @param paths the paths for cosmosFulltext.
     * @return CosmosFullTextPolicy
     */
    public CosmosFullTextPolicy setPaths(List<CosmosFullTextPath> paths) {
        this.paths = paths;
        return this;
    }
}
