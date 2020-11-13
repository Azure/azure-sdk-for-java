// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.mapping;

import com.azure.spring.data.gremlin.common.Constants;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

public class BasicGremlinPersistentProperty extends AnnotationBasedPersistentProperty<GremlinPersistentProperty>
        implements GremlinPersistentProperty {

    public BasicGremlinPersistentProperty(Property property, GremlinPersistentEntity<?> owner,
                                          SimpleTypeHolder holder) {
        super(property, owner, holder);
    }

    @Override
    protected Association<GremlinPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    @Override
    public boolean isIdProperty() {
        return super.isIdProperty() || getName().equals(Constants.PROPERTY_ID);
    }
}
