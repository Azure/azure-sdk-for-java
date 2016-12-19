/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

/**
 * Controls the amount of detail requested from the Azure Batch service when listing or
 * retrieving resources.
 */
public class DetailLevel {

    private String filterClause;

    private String selectClause;

    private String expandClause;

    /**
     * The builder class to initiate a {@link DetailLevel} instance.
     */
    public static class Builder {

        private String filterClause;

        private String selectClause;

        private String expandClause;

        /**
         * Initializes a new instance of the Builder class.
         */
        public Builder() {}

        /**
         * Sets the OData filter clause. Used to restrict a list operation to items that match specified criteria.
         *
         * @param filter The filter clause
         * @return The Builder instance
         */
        public Builder withFilterClause(String filter) {
            this.filterClause = filter;
            return this;
        }

        /**
         * Sets the OData select clause. Used to retrieve only specific properties instead of all object properties.
         *
         * @param select The select clause
         * @return The Builder instance
         */
        public Builder withSelectClause(String select) {
            this.selectClause = select;
            return this;
        }

        /**
         * Sets the OData expand clause. Used to retrieve associated entities of the main entity being retrieved.
         *
         * @param expand The expand clause
         * @return The Builder instance
         */
        public Builder withExpandClause(String expand) {
            this.expandClause = expand;
            return this;
        }

        /**
         * Create a DetailLevel class instance.
         *
         * @return A DetailLevel instance.
         */
        public DetailLevel build() {
            return new DetailLevel(this);
        }
    }

    /**
     * Gets the OData filter clause. Used to restrict a list operation to items that match specified criteria.
     *
     * @return The filter clause
     */
    public String filterClause() {
        return filterClause;
    }

    /**
     * Gets the OData select clause. Used to retrieve only specific properties instead of all object properties.
     *
     * @return The select clause
     */
    public String selectClause() {
        return selectClause;
    }

    /**
     * Gets the OData expand clause. Used to retrieve associated entities of the main entity being retrieved.
     *
     * @return The expand clause
     */
    public String expandClause() {
        return expandClause;
    }

    private DetailLevel(Builder b) {
        this.selectClause = b.selectClause;
        this.expandClause = b.expandClause;
        this.filterClause = b.filterClause;
    }
}
