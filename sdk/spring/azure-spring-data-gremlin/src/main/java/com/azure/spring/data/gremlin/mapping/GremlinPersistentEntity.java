// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.mapping;

import org.springframework.data.mapping.PersistentEntity;

public interface GremlinPersistentEntity<T> extends PersistentEntity<T, GremlinPersistentProperty> {

}
