package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;

public interface ResourceConnector<T extends ResourceConnector> {
    interface Builder<T> {
        T create(ResourceManagementClientImpl credentials, String resourceGroupName);
    }
}
