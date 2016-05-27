package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

public interface Providers extends
        SupportsListing<Provider>,
        SupportsGettingByName<Provider> {
    Provider unregister(String resourceProviderNamespace) throws CloudException, IOException;
    Provider register(String resourceProviderNamespace) throws CloudException, IOException;
}
