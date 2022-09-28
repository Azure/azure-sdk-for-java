// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Change Feed processor item.
 * Supports current and previous items through {@link JsonNode} structure.
 *
 * Caller is recommended to type cast {@link JsonNode} to cosmos item structure.
 */
public final class ChangeFeedProcessorItem {
    @JsonProperty("current")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JsonNode current;
    @JsonProperty("previous")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JsonNode previous;
    @JsonProperty("metadata")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ChangeFeedMetaData changeFeedMetaData;

    /**
     * Gets the change feed current item.
     *
     * @return change feed current item.
     */
    public JsonNode getCurrent() {
        return current;
    }

    /**
     * Gets the change feed previous item.
     * For delete operations, previous image is always going to be provided.
     * The previous image on replace operations is not going to be exposed by default and requires account-level or container-level opt-in.
     *
     * @return change feed previous item.
     */
    public JsonNode getPrevious() {
        return previous;
    }

    /**
     * Gets the change feed metadata.
     *
     * @return change feed metadata.
     */
    public ChangeFeedMetaData getChangeFeedMetaData() {
        return changeFeedMetaData;
    }

    @Override
    public String toString() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }
}
