// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Path settings within {@link CosmosFullTextPolicy}
 */
@Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosFullTextPath {
    @JsonProperty(Constants.Properties.PATH)
    private String path;
    @JsonProperty(Constants.Properties.LANGUAGE)
    private String language;

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextPath() {}

    /**
     * Gets the path for the cosmosFullText.
     *
     * @return path
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getPath() {
        return path;
    }

    /**
     * Sets the path for the cosmosFullText.
     *
     * @param path the path for the cosmosFullText.
     * @return CosmosFullTextPath
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextPath setPath(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new NullPointerException("Full text search path is either null or empty");
        }

        if (path.charAt(0) != '/') {
            throw new IllegalArgumentException("Path needs to start with '/'");
        }

        this.path = path;
        return this;
    }

    /**
     * Gets the language for the cosmosFullText path.
     * @return language
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language for the cosmosFullText path.
     * @param language the language for the cosmosFullText path.
     * @return CosmosFullTextPath
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextPath setLanguage(String language) {
        this.language = language;
        return this;
    }
}
