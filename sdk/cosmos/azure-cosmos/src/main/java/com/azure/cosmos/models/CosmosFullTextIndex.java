// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.util.Beta;

/**
 * Represents cosmos full text index of the IndexingPolicy in the Azure Cosmos DB database service.
 */
@Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosFullTextIndex {
    private final JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextIndex() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Gets path.
     * @return the path.
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getPath() { return this.jsonSerializable.getString(Constants.Properties.PATH); }

    /**
     * Sets the path.
     * @param path the path.
     * @return the CosmosFullTextIndex.
     */
    @Beta(value = Beta.SinceVersion.V4_65_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosFullTextIndex setPath(String path) {
        this.jsonSerializable.set(
            Constants.Properties.PATH,
            path,
            CosmosItemSerializer.DEFAULT_SERIALIZER);
        return this;
    }

    void populatePropertyBag() { this.jsonSerializable.populatePropertyBag(); }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
