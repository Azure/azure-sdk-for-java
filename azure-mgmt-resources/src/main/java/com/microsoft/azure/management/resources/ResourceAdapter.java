package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;

public interface ResourceAdapter<T extends ResourceAdapter> {
    interface Builder<T> {
        T create(ResourceManagementClientImpl credentials, String resourceGroupName);
    }
}
