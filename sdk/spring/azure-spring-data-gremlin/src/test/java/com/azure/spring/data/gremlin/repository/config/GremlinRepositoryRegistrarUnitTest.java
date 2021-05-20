// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class GremlinRepositoryRegistrarUnitTest {

    private GremlinRepositoryRegistrar registrar;

    @BeforeEach
    public void setup() {
        this.registrar = new GremlinRepositoryRegistrar();
    }

    @Test
    public void testGremlinRepositoryRegistrarGetters() {
        Assertions.assertSame(this.registrar.getAnnotation(), EnableGremlinRepositories.class);
        Assertions.assertTrue(this.registrar.getExtension() instanceof GremlinRepositoryConfigurationExtension);
    }
}
