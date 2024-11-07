// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Full Text Search Policy
 */
@Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CosmosFullTextPolicy {
    @JsonProperty(Constants.Properties.DEFAULT_LANGUAGE)
    private String defaultLanguage;
    @JsonProperty(Constants.Properties.FULL_TEXT_PATHS)
    private List<CosmosFullTextPath> paths;

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextPolicy() {
    }

    /**
     * Gets the default language for cosmosFullText.
     *
     * @return the default language for cosmosFullText.
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Sets the default language for cosmosFullText.
     * @param defaultLanguage the default language for cosmosFullText.
     * @return CosmosFullTextPolicy
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextPolicy setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }

    /**
     * Gets the paths for cosmosFulltext.
     * @return the paths for cosmosFulltext.
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public List<CosmosFullTextPath> getPaths() {
        return paths;
    }

    /**
     * Sets the paths for cosmosFulltext.
     * @param paths the paths for cosmosFulltext.
     * @return CosmosFullTextPolicy
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextPolicy setPaths(List<CosmosFullTextPath> paths) {
        for (CosmosFullTextPath cosmosFullTextPath : paths) {
            if (cosmosFullTextPath.getLanguage().isEmpty()) {
                throw new IllegalArgumentException("Language needs to specified for the path in the Full text policy.");
            }
        }
        this.paths = paths;
        return this;
    }
}
