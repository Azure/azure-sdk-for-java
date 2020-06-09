// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.core.mapping;

import com.microsoft.azure.spring.data.cosmosdb.Constants;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;


public class BasicCosmosPersistentProperty extends AnnotationBasedPersistentProperty<CosmosPersistentProperty>
        implements CosmosPersistentProperty {

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
