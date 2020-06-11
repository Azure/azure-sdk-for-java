// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityMetadata;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

public class CosmosQueryMethod extends QueryMethod {

    private CosmosEntityMetadata<?> metadata;

    public CosmosQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {
        super(method, metadata, factory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EntityMetadata<?> getEntityInformation() {
        final Class<Object> domainType = (Class<Object>) getDomainClass();
        final CosmosEntityInformation<Object, String> entityInformation =
                new CosmosEntityInformation<Object, String>(domainType);

        this.metadata = new SimpleCosmosEntityMetadata<Object>(domainType, entityInformation);
        return this.metadata;
    }
}
