// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.support;

import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.query.GremlinOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class GremlinRepositoryFactoryUnitTest {

    @Mock
    private GremlinOperations operations;

    @Autowired
    private ApplicationContext context;

    private GremlinRepositoryFactory factory;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.factory = new GremlinRepositoryFactory(this.operations, this.context);
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Test
    public void testGetRepositoryBaseClass() {
        Assertions.assertEquals(SimpleGremlinRepository.class, this.factory.getRepositoryBaseClass(null));
    }

    @Test
    public void testGetEntityInformation() {
        final EntityInformation<Person, String> information = this.factory.getEntityInformation(Person.class);

        Assertions.assertNotNull(information);
        Assertions.assertEquals(information.getIdType(), String.class);
    }

    @Test
    public void testGetQueryLookupStrategy() {
        final Optional<QueryLookupStrategy> strategyOptional = this.factory.
                getQueryLookupStrategy(QueryLookupStrategy.Key.CREATE, null);

        Assertions.assertTrue(strategyOptional.isPresent());
    }
}
