// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import org.springframework.context.ApplicationContext;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * Class to build mapping metadata and thus create instances of {@link BasicCosmosPersistentEntity} and
 * {@link CosmosPersistentProperty}.
 */
public class CosmosMappingContext
        extends AbstractMappingContext<BasicCosmosPersistentEntity<?>, CosmosPersistentProperty> {

    private ApplicationContext context;

    @Override
    protected <T> BasicCosmosPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        final BasicCosmosPersistentEntity<T> entity = new BasicCosmosPersistentEntity<>(typeInformation);

        if (context != null) {
            entity.setApplicationContext(context);
        }
        return entity;
    }

    @Override
    public CosmosPersistentProperty createPersistentProperty(Property property,
                                                             BasicCosmosPersistentEntity<?> owner,
                                                             SimpleTypeHolder simpleTypeHolder) {
        return new BasicCosmosPersistentProperty(property, owner, simpleTypeHolder);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }
}
