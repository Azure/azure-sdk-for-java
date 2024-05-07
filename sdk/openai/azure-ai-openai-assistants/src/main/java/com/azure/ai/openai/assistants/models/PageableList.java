// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.models;

import com.azure.ai.openai.assistants.implementation.accesshelpers.PageableListAccessHelper;
import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The response data for a requested list of items.
 *
 * @param <T> the type of the items in the list.
 */
@Immutable
public final class PageableList<T>  {

    static {
        PageableListAccessHelper.setAccessor(PageableList::new);
    }

    /*
     * The object type, which is always list.
     */
    private final String object = "list";

    /*
     * The requested list of items.
     */
    private final List<T> data;

    /*
     * The first ID represented in this list.
     */
    private final String firstId;

    /*
     * The last ID represented in this list.
     */
    private final String lastId;

    /*
     * A value indicating whether there are additional values available not captured in this list.
     */
    private final boolean hasMore;

    /**
     * Creates an instance of PageableList class.
     *
     * @param data the data value to set.
     * @param firstId the firstId value to set.
     * @param lastId the lastId value to set.
     * @param hasMore the hasMore value to set.
     */
    private PageableList(List<T> data, String firstId, String lastId, boolean hasMore) {
        this.data = data;
        this.firstId = firstId;
        this.lastId = lastId;
        this.hasMore = hasMore;
    }

    /**
     * Creates an instance of PageableList class.
     */
    public PageableList() {
        this.data = null;
        this.firstId = null;
        this.lastId = null;
        this.hasMore = false;
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
