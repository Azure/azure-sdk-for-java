// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import org.springframework.data.mapping.PersistentEntity;


public interface CosmosPersistentEntity<T> extends PersistentEntity<T, CosmosPersistentProperty> {

    @Deprecated
    String getCollection();

    String getContainer();

    String getLanguage();
}
