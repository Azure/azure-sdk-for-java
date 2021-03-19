// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryConstants;

import java.util.Locale;

/**
 * The FetchResult class has the purpose of containing key elements of
 * an IModelFetcher Fetch() operation including model definition, path and whether
 * it was from an expanded (pre-calculated) fetch.
 */
@Fluent
public class FetchResult {

    private String definition;
    private String path;

    /**
     * Gets the dtmi model definition
     *
     * @return Model definition
     */
    public String getDefinition() {
        return this.definition;
    }

    /**
     * Sets the model definition
     *
     * @param definition the model definition
     * @return the FetchResult object itself
     */
    public FetchResult setDefinition(String definition) {
        this.definition = definition;
        return this;
    }

    /**
     * Gets the dtmi path.
     *
     * @return dtmi path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Sets the dtmi path.
     *
     * @param path the dtmi path.
     * @return the {@link FetchResult} object itself
     */
    public FetchResult setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Whether the result is from an expanded (pre-calculated) fetch or not.
     *
     * @return true: if result is from an expanded fetch operation.
     * false: if path is null or result is not from an expanded fetch operation.
     */
    public boolean isFromExpanded() {
        if (this.path == null) {
            return false;
        }

        return this.path.toLowerCase(Locale.getDefault()).endsWith(ModelsRepositoryConstants.JSON_EXPANDED_EXTENSION);
    }
}
