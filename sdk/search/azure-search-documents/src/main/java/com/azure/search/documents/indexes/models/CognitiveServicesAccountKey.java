// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A cognitive service resource provisioned with a key that is attached to a
 * skillset.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.CognitiveServicesByKey")
@Fluent
public final class CognitiveServicesAccountKey extends CognitiveServicesAccount {
    /*
     * The key used to provision the cognitive service resource attached to a
     * skillset.
     */
    @JsonProperty(value = "key", required = true)
    private String key;

    /**
     * Get the key property: The key used to provision the cognitive service
     * resource attached to a skillset.
     *
     * @return the key value.
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Set the key property: The key used to provision the cognitive service
     * resource attached to a skillset.
     *
     * @param key the key value to set.
     * @return the CognitiveServicesAccountKey object itself.
     */
    public CognitiveServicesAccountKey setKey(String key) {
        this.key = key;
        return this;
    }
}
