package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

import java.io.IOException;

public interface GenericResources extends
        SupportsListing<GenericResource>,
        SupportsListingByGroup<GenericResource>,
        SupportsGettingByGroup<GenericResource>,
        SupportsCreating<GenericResource.DefinitionBlank>,
        SupportsDeleting,
        SupportsDeletingByGroup {
    boolean checkExistence(String resourceGroupName, String resourceProviderNamespace, String parentResourcePath, String resourceType, String resourceName, String apiVersion) throws IOException, CloudException;

    interface InGroup extends
            SupportsListing<GenericResource>,
            SupportsGettingByName<GenericResource> {
    }
}
