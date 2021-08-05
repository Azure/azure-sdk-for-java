// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.search.fluent.models.QueryKeyInner;
import com.azure.resourcemanager.search.models.QueryKey;

/**
 * Describes an API key for a given Azure Search service that has permissions for query operations only.
 */
class QueryKeyImpl extends WrapperImpl<QueryKeyInner> implements QueryKey {

    QueryKeyImpl(QueryKeyInner inner) {
        super(inner);
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String key() {
        return this.innerModel().key();
    }
}
