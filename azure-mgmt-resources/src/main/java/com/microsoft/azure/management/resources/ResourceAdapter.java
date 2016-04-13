package com.microsoft.azure.management.resources;

import com.microsoft.rest.credentials.ServiceClientCredentials;

public interface ResourceAdapter<T extends ResourceAdapter> {
    interface Builder<T> {
        T create(ServiceClientCredentials credentials, String resourceGroupName);
    }
}
