// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GremlinRepositoryRegistrarUnitTest {

    private GremlinRepositoryRegistrar registrar;

    @Before
    public void setup() {
        this.registrar = new GremlinRepositoryRegistrar();
    }

    @Test
    public void testGremlinRepositoryRegistrarGetters() {
        Assert.assertSame(this.registrar.getAnnotation(), EnableGremlinRepositories.class);
        Assert.assertTrue(this.registrar.getExtension() instanceof GremlinRepositoryConfigurationExtension);
    }
}
