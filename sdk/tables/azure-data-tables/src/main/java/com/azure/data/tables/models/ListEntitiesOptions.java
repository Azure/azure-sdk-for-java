// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

/**
 * Options to modify the data returned from the {@code listEntities} family of methods.
 */
@Fluent
public final class ListEntitiesOptions {
    private Integer top;
    private String select;
    private String filter;

    /**
     * Gets the value of the `top` OData query option which limits the number of returned entities.
     *
     * @return The value of the `top` OData query option.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Sets the value of the `top` OData query option which limits the number of returned entities.
     *
     * @param top The value of the `top` OData query option.
     * @return The updated {@code ListEntitiesOptions}.
     */
    public ListEntitiesOptions setTop(Integer top) {
        this.top = top;
        return this;
    }

    /**
     * Gets the value of the `select` OData query option which limits the properties returned on each entity.
     *
     * @return The value of the `select` OData query option.
     */
    public String getSelect() {
        return this.select;
    }

    /**
     * Sets the value of the `select` OData query option which limits the properties returned on each entity.
     *
     * @param select The value of the `select` OData query option.
     * @return The updated {@code ListEntitiesOptions}.
     */
    public ListEntitiesOptions setSelect(String select) {
        this.select = select;
        return this;
    }

    /**
     * Gets the value of the `filter` OData query option which filters the set of returned entities, excluding those
     * that do not match the filter expression.
     *
     * @return The value of the `filter` OData query option.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Sets the value of the `filter` OData query option which filters the set of returned entities, excluding those
     * that do not match the filter expression.
     *
     * @param filter The value of the `filter` OData query option.
     * @return The updated {@code ListEntitiesOptions}.
     */
    public ListEntitiesOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }
}

