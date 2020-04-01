/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.core;

import com.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.azure.management.resources.fluentcore.utils.ResourceNamerFactory;

public class TestResourceNamerFactory extends ResourceNamerFactory {

    private final InterceptorManager interceptorManager;

    TestResourceNamerFactory(InterceptorManager mockIntegrationTestBase) {
        this.interceptorManager = mockIntegrationTestBase;
    }
    @Override
    public ResourceNamer createResourceNamer(String name) {
        return new TestResourceNamer(name, interceptorManager);
    }
}
