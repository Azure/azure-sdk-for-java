// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import org.springframework.data.repository.core.EntityMetadata;

public interface ReactiveCosmosEntityMetadata<T> extends EntityMetadata<T> {
    @Deprecated
    String getCollectionName();

    String getContainerName();
}
