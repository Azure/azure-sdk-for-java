// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.util.Beta;
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
@Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public JsonNode getPrevious() {
        return previous;
    }

    /**
     * Gets the change feed metadata.
     *
     * @return change feed metadata.
     */
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_37_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
