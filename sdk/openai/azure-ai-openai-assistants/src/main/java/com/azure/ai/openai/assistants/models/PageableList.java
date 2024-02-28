// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
 
package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The response data for a requested list of items.
 *
 * @param <T> the type of the items in the list.
 */
@Immutable
public final class PageableList<T> {

    /*
     * The object type, which is always list.
     */
    @JsonProperty(value = "object")
    private String object = "list";

    /*
     * The requested list of items.
     */
    @JsonProperty(value = "data")
    private List<T> data;

    /*
     * The first ID represented in this list.
     */
    @JsonProperty(value = "first_id")
    private String firstId;

    /*
     * The last ID represented in this list.
     */
    @JsonProperty(value = "last_id")
    private String lastId;

    /*
     * A value indicating whether there are additional values available not captured in this list.
     */
    @JsonProperty(value = "has_more")
    private boolean hasMore;

    /**
     * Creates an instance of PageableList class.
     *
     * @param data the data value to set.
     * @param firstId the firstId value to set.
     * @param lastId the lastId value to set.
     * @param hasMore the hasMore value to set.
     */
    @JsonCreator
    private PageableList(@JsonProperty(value = "data") List<T> data,
                         @JsonProperty(value = "first_id") String firstId,
                         @JsonProperty(value = "last_id") String lastId,
                         @JsonProperty(value = "has_more") boolean hasMore) {
        this.data = data;
        this.firstId = firstId;
        this.lastId = lastId;
        this.hasMore = hasMore;
    }

    /**
     * Get the object property: The object type, which is always list.
     *
     * @return the object value.
     */
    public String getObject() {
        return this.object;
    }

    /**
     * Get the data property: The requested list of items.
     *
     * @return the data value.
     */
    public List<T> getData() {
        return this.data;
    }

    /**
     * Get the firstId property: The first ID represented in this list.
     *
     * @return the firstId value.
     */
    public String getFirstId() {
        return this.firstId;
    }

    /**
     * Get the lastId property: The last ID represented in this list.
     *
     * @return the lastId value.
     */
    public String getLastId() {
        return this.lastId;
    }

    /**
     * Get the hasMore property: A value indicating whether there are additional values available not captured in this
     * list.
     *
     * @return the hasMore value.
     */
    public boolean isHasMore() {
        return this.hasMore;
    }
}
