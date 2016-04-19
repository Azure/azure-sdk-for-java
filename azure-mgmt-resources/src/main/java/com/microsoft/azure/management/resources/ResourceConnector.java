package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public interface ResourceConnector<T extends ResourceConnector> {
    interface Builder<T> {
        T create(ServiceClientCredentials credentials, ResourceGroup resourceGroup);
    }
}
