// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.query;

import com.azure.spring.data.gremlin.query.criteria.Criteria;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class GremlinQuery {

    private final Criteria criteria;

    public GremlinQuery(@NonNull Criteria criteria) {
        Assert.notNull(criteria, "criteria should not be null");
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }
}
