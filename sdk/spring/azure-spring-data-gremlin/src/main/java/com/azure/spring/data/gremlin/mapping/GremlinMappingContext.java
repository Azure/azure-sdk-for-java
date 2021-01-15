// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.mapping;

import org.springframework.context.ApplicationContext;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

public class GremlinMappingContext
        extends AbstractMappingContext<BasicGremlinPersistentEntity<?>, GremlinPersistentProperty> {
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public GremlinPersistentProperty createPersistentProperty(Property property,
                                                              BasicGremlinPersistentEntity<?> owner,
                                                              SimpleTypeHolder holder) {
        return new BasicGremlinPersistentProperty(property, owner, holder);
    }

    @Override
    protected <T> BasicGremlinPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        final BasicGremlinPersistentEntity<T> entity = new BasicGremlinPersistentEntity<>(typeInformation);

        if (this.context != null) {
            entity.setApplicationContext(this.context);
        }

        return entity;
    }

}
