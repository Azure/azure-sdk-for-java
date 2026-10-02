// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    private JsonNode changeFeedMetaData;
    private ChangeFeedMetaData changeFeedMetaDataInternal;
    private JsonNode changeFeedProcessorItemAsJsonNode;


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
     * A previous image is available only when it was captured for the operation under the effective container or
     * account-level retention settings and is still available in the change feed retention window.
     * Replace operations include patch operations and upserts that update an existing item.
     * Creating an item has no previous image. Delete operations do not always include a previous image.
     *
     * @see CosmosContainerProperties#setChangeFeedPreviousImageRetentionMode(CosmosChangeFeedPreviousImageRetentionMode)
     *
     * @return the change feed previous item, or {@code null} if no previous image is included.
     */
    public JsonNode getPrevious() {
        return previous;
    }

    /**
     * Gets the change feed metadata.
     *
     * @return change feed metadata.
     */
    @JsonIgnore
    public ChangeFeedMetaData getChangeFeedMetaData() {

        if (this.changeFeedMetaDataInternal == null) {
            this.changeFeedMetaDataInternal = Utils.getSimpleObjectMapper().convertValue(this.changeFeedMetaData, ChangeFeedMetaData.class);
        }

        return this.changeFeedMetaDataInternal;
    }

    /**
     * Helper API to convert this changeFeedProcessorItem instance to raw JsonNode format.
     *
     * @return jsonNode format of this changeFeedProcessorItem instance.
     *
     * @throws IllegalArgumentException If conversion fails due to incompatible type;
     * if so, root cause will contain underlying checked exception data binding functionality threw
     */
    public JsonNode toJsonNode() {

        if (this.changeFeedProcessorItemAsJsonNode == null) {
            this.changeFeedProcessorItemAsJsonNode = constructChangeFeedProcessorItemAsJsonNode();
        }

        return this.changeFeedProcessorItemAsJsonNode;
    }

    @Override
    public String toString() {
        try {

            if (this.changeFeedProcessorItemAsJsonNode == null) {
                this.changeFeedProcessorItemAsJsonNode = constructChangeFeedProcessorItemAsJsonNode();
            }

            return Utils.getSimpleObjectMapper().writeValueAsString(this.changeFeedProcessorItemAsJsonNode);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }

    private JsonNode constructChangeFeedProcessorItemAsJsonNode() {
        ObjectNode objectNode = Utils.getSimpleObjectMapper().createObjectNode();

        if (this.previous != null) {
            objectNode.set("previous", this.previous);
        }

        if (this.current != null) {
            objectNode.set("current", this.current);
        }

        if (this.changeFeedMetaData != null) {
            objectNode.set("metadata", this.changeFeedMetaData);
        }

        return objectNode;
    }
}
