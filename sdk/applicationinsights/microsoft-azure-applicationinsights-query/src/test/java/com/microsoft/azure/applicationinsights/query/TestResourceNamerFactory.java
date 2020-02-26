// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.applicationinsights.query;

import com.microsoft.azure.arm.utils.ResourceNamer;
import com.microsoft.azure.arm.utils.ResourceNamerFactory;

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
