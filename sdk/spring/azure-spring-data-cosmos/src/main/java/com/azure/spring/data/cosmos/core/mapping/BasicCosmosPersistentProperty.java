// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.Constants;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * Special {@link CosmosPersistentProperty} that takes annotations at a property into account.
 */
public class BasicCosmosPersistentProperty extends AnnotationBasedPersistentProperty<CosmosPersistentProperty>
        implements CosmosPersistentProperty {

    /**
     * Creates a new {@link BasicCosmosPersistentProperty}.
     *
     * @param property must not be {@literal null}.
     * @param owner must not be {@literal null}.
     * @param simpleTypeHolder must not be {@literal null}.
     */
    public BasicCosmosPersistentProperty(Property property, CosmosPersistentEntity<?> owner,
                                         SimpleTypeHolder simpleTypeHolder) {
        super(property, owner, simpleTypeHolder);
    }

    @Override
    protected Association<CosmosPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    @Override
    public boolean isIdProperty() {

        if (super.isIdProperty()) {
            return true;
        }

        return getName().equals(Constants.ID_PROPERTY_NAME);
    }

}
