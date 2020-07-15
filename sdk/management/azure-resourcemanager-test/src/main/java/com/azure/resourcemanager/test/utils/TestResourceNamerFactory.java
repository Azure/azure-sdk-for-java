package com.azure.resourcemanager.test.utils;

import com.azure.resourcemanager.base.utils.ResourceNamer;
import com.azure.resourcemanager.base.utils.ResourceNamerFactory;

public class TestResourceNamerFactory extends ResourceNamerFactory {

    private final InterceptorManager interceptorManager;

    public TestResourceNamerFactory(InterceptorManager mockIntegrationTestBase) {
        this.interceptorManager = mockIntegrationTestBase;
    }
    @Override
    public ResourceNamer createResourceNamer(String name) {
        return new TestResourceNamer(name, interceptorManager);
    }
}
