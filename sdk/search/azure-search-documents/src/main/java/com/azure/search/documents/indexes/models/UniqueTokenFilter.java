// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Filters out tokens with same text as the previous token. This token filter
 * is implemented using Apache Lucene.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@odata.type")
@JsonTypeName("#Microsoft.Azure.Search.UniqueTokenFilter")
@Fluent
public final class UniqueTokenFilter extends TokenFilter {
    /*
     * A value indicating whether to remove duplicates only at the same
     * position. Default is false.
     */
    @JsonProperty(value = "onlyOnSamePosition")
    private Boolean onlyOnSamePosition;

    /**
     * Get the onlyOnSamePosition property: A value indicating whether to
     * remove duplicates only at the same position. Default is false.
     *
     * @return the onlyOnSamePosition value.
     */
    public Boolean isOnlyOnSamePosition() {
        return this.onlyOnSamePosition;
    }

    /**
     * Set the onlyOnSamePosition property: A value indicating whether to
     * remove duplicates only at the same position. Default is false.
     *
     * @param onlyOnSamePosition the onlyOnSamePosition value to set.
     * @return the UniqueTokenFilter object itself.
     */
    public UniqueTokenFilter setOnlyOnSamePosition(Boolean onlyOnSamePosition) {
        this.onlyOnSamePosition = onlyOnSamePosition;
        return this;
    }
}
