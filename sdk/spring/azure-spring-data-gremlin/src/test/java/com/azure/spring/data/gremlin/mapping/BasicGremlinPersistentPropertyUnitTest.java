// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.mapping;

import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.domain.Network;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class BasicGremlinPersistentPropertyUnitTest {

    private <T> BasicGremlinPersistentProperty createBasicGremlinPersistentProperty(
            BasicGremlinPersistentEntity<T> entity, Field field) {
        return new BasicGremlinPersistentProperty(Property.of(entity.getTypeInformation(), field), entity,
                SimpleTypeHolder.DEFAULT);
    }

    @Test
    public void testVertexPersistentProperty() {
        final BasicGremlinPersistentEntity<Person> entity =
                new BasicGremlinPersistentEntity<>(ClassTypeInformation.from(Person.class));
        Field field = ReflectionUtils.findField(Person.class, TestConstants.PROPERTY_ID);
        BasicGremlinPersistentProperty property = this.createBasicGremlinPersistentProperty(entity, field);

        Assert.assertEquals(property.getName(), TestConstants.PROPERTY_ID);
        Assert.assertTrue(property.isIdProperty());
        Assert.assertNotNull(property.createAssociation());

        field = ReflectionUtils.findField(Person.class, TestConstants.PROPERTY_NAME);
        property = this.createBasicGremlinPersistentProperty(entity, field);

        Assert.assertEquals(property.getName(), TestConstants.PROPERTY_NAME);
        Assert.assertFalse(property.isIdProperty());
        Assert.assertNotNull(property.createAssociation());
    }

    @Test
    public void testEdgePersistentProperty() {
        final BasicGremlinPersistentEntity<Relationship> entity =
                new BasicGremlinPersistentEntity<>(ClassTypeInformation.from(Relationship.class));
        Field field = ReflectionUtils.findField(Relationship.class, TestConstants.PROPERTY_ID);
        BasicGremlinPersistentProperty property = this.createBasicGremlinPersistentProperty(entity, field);

        Assert.assertEquals(property.getName(), TestConstants.PROPERTY_ID);
        Assert.assertTrue(property.isIdProperty());
        Assert.assertNotNull(property.createAssociation());

        field = ReflectionUtils.findField(Relationship.class, TestConstants.PROPERTY_LOCATION);
        property = this.createBasicGremlinPersistentProperty(entity, field);

        Assert.assertEquals(property.getName(), TestConstants.PROPERTY_LOCATION);
        Assert.assertFalse(property.isIdProperty());
        Assert.assertNotNull(property.createAssociation());
    }

    @Test
    public void testGraphPersistentProperty() {
        final BasicGremlinPersistentEntity<Network> entity =
                new BasicGremlinPersistentEntity<>(ClassTypeInformation.from(Network.class));
        final Field field = ReflectionUtils.findField(Network.class, TestConstants.PROPERTY_ID);
        final BasicGremlinPersistentProperty property = this.createBasicGremlinPersistentProperty(entity, field);

        Assert.assertEquals(property.getName(), TestConstants.PROPERTY_ID);
        Assert.assertTrue(property.isIdProperty());
        Assert.assertNotNull(property.createAssociation());
    }
}
