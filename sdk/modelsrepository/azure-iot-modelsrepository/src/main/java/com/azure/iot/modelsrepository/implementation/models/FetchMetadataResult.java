// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The FetchMetadataResult class is used for storing the result of the
 * fetch repository metadata operation. It contains the metadata definition
 * and path
 */
@Fluent

public class FetchMetadataResult {

    private ModelsRepositoryMetadata definition;
    private String path;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Gets the model repository's metadata definition
     *
     * @return Repository metadata definition
     */
    public ModelsRepositoryMetadata getDefinition() {
        return this.definition;
    }

    /**
     * Sets the model repository's metadata definition
     *
     * @param definition the model repository's metadata definition
     * @return the {@link FetchMetadataResult}  object itself
     */
    public FetchMetadataResult setDefinition(String definition) throws JsonProcessingException {
        this.definition = mapper.readValue(definition, ModelsRepositoryMetadata.class);
        return this;
    }

    /**
     * Gets the model repository's metadata path.
     *
     * @return model repository's metadata path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Sets the model repository's metadata path.
     *
     * @param path the model repository's metadata path.
     * @return the {@link FetchMetadataResult} object itself
     */
    public FetchMetadataResult setPath(String path) {
        this.path = path;
        return this;
    }

}
