// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.query;

import com.azure.spring.data.gremlin.query.GremlinEntityMetadata;
import com.azure.spring.data.gremlin.query.SimpleGremlinEntityMetadata;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

public class GremlinQueryMethod extends QueryMethod {

    private GremlinEntityMetadata<?> metadata;

    public GremlinQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
    }

    @Override
    public EntityMetadata<?> getEntityInformation() {
        @SuppressWarnings("unchecked") final Class<Object> domainClass = (Class<Object>) super.getDomainClass();

        this.metadata = new SimpleGremlinEntityMetadata<>(domainClass);

        return this.metadata;
    }
}
