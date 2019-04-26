// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import com.azure.common.test.InterceptorManager;
import com.azure.common.test.utils.ResourceNamerFactory;

/**
 * The TestResourceNamerFactory to generate TestResourceNamer.
 */
public class TestResourceNamerFactory extends ResourceNamerFactory {

    private final InterceptorManager interceptorManager;

    /**
     * TestResourceNamerFactory constructor with mock integrationTestBase.
     *
     * @param mockIntegrationTestBase A mock integration test base.
     */
    public TestResourceNamerFactory(InterceptorManager mockIntegrationTestBase) {
        this.interceptorManager = mockIntegrationTestBase;
    }

    /**
     * Test namer method to generate instance of TestResourceNamer.
     * @param name prefix for the names.
     * @return instance of ResourceNamer
     */
    @Override
    public TestResourceNamer createResourceNamer(String name) {
        return new TestResourceNamer(name, interceptorManager.getTestMode(), interceptorManager.getRecordedData());
    }
}
