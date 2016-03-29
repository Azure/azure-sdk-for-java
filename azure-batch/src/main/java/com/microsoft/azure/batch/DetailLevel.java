/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

public class DetailLevel {

    private String filterClause;

    private String selectClause;

    private String expandClause;

    public static class Builder {

        private String filterClause;

        private String selectClause;

        private String expandClause;

        public Builder() {}

        public Builder filterClause(String filter) {
            this.filterClause = filter;
            return this;
        }

        public Builder selectClause(String select) {
            this.selectClause = select;
            return this;
        }

        public Builder expandClause(String expand) {
            this.expandClause = expand;
            return this;
        }

        public DetailLevel build() {
            return new DetailLevel(this);
        }
    }

    public String getFilterClause() {
        return filterClause;
    }

    public String getSelectClause() {
        return selectClause;
    }

    public String getExpandClause() {
        return expandClause;
    }

    private DetailLevel(Builder b) {
        this.selectClause = b.selectClause;
        this.expandClause = b.expandClause;
        this.filterClause = b.filterClause;
    }
}
