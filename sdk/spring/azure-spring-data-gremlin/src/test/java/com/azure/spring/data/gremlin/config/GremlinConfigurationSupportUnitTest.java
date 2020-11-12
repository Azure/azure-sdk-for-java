// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.config;

import com.azure.spring.data.gremlin.common.domain.AdvancedUser;
import com.azure.spring.data.gremlin.common.domain.Book;
import com.azure.spring.data.gremlin.common.domain.BookReference;
import com.azure.spring.data.gremlin.common.domain.Dependency;
import com.azure.spring.data.gremlin.common.domain.Group;
import com.azure.spring.data.gremlin.common.domain.GroupOwner;
import com.azure.spring.data.gremlin.common.domain.InvalidDependency;
import com.azure.spring.data.gremlin.common.domain.Library;
import com.azure.spring.data.gremlin.common.domain.Master;
import com.azure.spring.data.gremlin.common.domain.Neighbor;
import com.azure.spring.data.gremlin.common.domain.Network;
import com.azure.spring.data.gremlin.common.domain.Orange;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Project;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.common.domain.Roadmap;
import com.azure.spring.data.gremlin.common.domain.Service;
import com.azure.spring.data.gremlin.common.domain.SimpleDependency;
import com.azure.spring.data.gremlin.common.domain.Student;
import com.azure.spring.data.gremlin.common.domain.UserDomain;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GremlinConfigurationSupportUnitTest {

    private static final String TEST_CONFIG_PACKAGE_NAME = "com.azure.spring.data.gremlin.config";
    private static final String TEST_DOMAIN_PACKAGE_NAME = "com.azure.spring.data.gremlin.common.domain";
    private TestConfig config;

    @Before
    public void setup() {
        this.config = new TestConfig();
    }

    @Test
    public void testGetMappingBasePackages() {
        final Collection<String> basePackages = this.config.getMappingBasePackages();

        Assert.assertNotNull(basePackages);
        Assert.assertEquals(basePackages.size(), 1);
        Assert.assertEquals(basePackages.toArray()[0], TEST_CONFIG_PACKAGE_NAME);
    }

    @Test
    public void testGremlinMappingContext() throws ClassNotFoundException {
        Assert.assertNotNull(this.config.gremlinMappingContext());
    }

    @Test
    public void testScanEntity() throws ClassNotFoundException {
        final Set<Class<?>> entities = this.config.scanEntities(TEST_DOMAIN_PACKAGE_NAME);
        final Set<Class<?>> references = new HashSet<>(Arrays.asList(
                Dependency.class, Library.class, Network.class, Person.class, Project.class,
                Relationship.class, Roadmap.class, Service.class, SimpleDependency.class, InvalidDependency.class,
                UserDomain.class, AdvancedUser.class, Student.class, Book.class, BookReference.class,
                Neighbor.class, Master.class, Group.class, GroupOwner.class, Orange.class)
        );

        Assert.assertNotNull(entities);
        Assert.assertEquals(entities.size(), references.size());

        references.forEach(entity -> Assert.assertTrue(entities.contains(entity)));
    }

    @Test
    public void testScanEntityEmpty() throws ClassNotFoundException {
        final Set<Class<?>> entities = this.config.scanEntities("");

        Assert.assertTrue(entities.isEmpty());
    }

    private class TestConfig extends GremlinConfigurationSupport {

    }
}
