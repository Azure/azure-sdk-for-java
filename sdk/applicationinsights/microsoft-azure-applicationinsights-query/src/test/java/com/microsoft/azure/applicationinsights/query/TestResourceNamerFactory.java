// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.applicationinsights.query;

import com.microsoft.azure.arm.utils.ResourceNamer;
import com.microsoft.azure.arm.utils.ResourceNamerFactory;

/**
 * From:
 * https://github.com/Azure/autorest-clientruntime-for-java/blob/master/azure-arm-client-runtime/src/test/java/com/microsoft/azure/arm/core/TestResourceNamerFactory.java
 */
class TestResourceNamerFactory extends ResourceNamerFactory {
    private final InterceptorManager interceptorManager;

    TestResourceNamerFactory(InterceptorManager interceptorManager) {
        super();
        this.interceptorManager = interceptorManager;
    }

    @Override
    public ResourceNamer createResourceNamer(String name) {
        return new TestResourceNamer(name, interceptorManager);
    }
}
