// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.springframework.util.Assert;

public class SimpleReactiveCosmosEntityMetadata<T> implements ReactiveCosmosEntityMetadata<T> {

    private final Class<T> type;
    private final CosmosEntityInformation<T, String> entityInformation;

    public SimpleReactiveCosmosEntityMetadata(Class<T> type, CosmosEntityInformation<T,
                                                                            String> entityInformation) {
        Assert.notNull(type, "type must not be null!");
        Assert.notNull(entityInformation, "entityInformation must not be null!");

        this.type = type;
        this.entityInformation = entityInformation;
    }

    public Class<T> getJavaType() {
        return type;
    }

    public String getCollectionName() {
        return entityInformation.getContainerName();
    }

    @Override
    public String getContainerName() {
        return entityInformation.getContainerName();
    }
}
