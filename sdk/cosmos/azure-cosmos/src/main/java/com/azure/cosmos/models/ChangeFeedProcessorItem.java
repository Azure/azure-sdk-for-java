// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Change Feed processor item.
 */
@Beta(value = Beta.SinceVersion.V4_34_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class ChangeFeedProcessorItem {
    @JsonProperty("current")
    private JsonNode current;
    @JsonProperty("previous")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private JsonNode previous;
    @JsonProperty("metadata")
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ChangeFeedProcessorItem{current=")
            .append(current.toPrettyString())
            .append(", ");
        if (previous != null) {
            stringBuilder.append("previous=")
                .append(previous.toPrettyString())
                .append(", ");
        }
        stringBuilder.append("changeFeedMetaData=")
            .append(changeFeedMetaData)
            .append("}");
        return stringBuilder.toString();
    }
}
