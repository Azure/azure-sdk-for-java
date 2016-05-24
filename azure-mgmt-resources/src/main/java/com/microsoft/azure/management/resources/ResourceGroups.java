package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsUpdating;

import java.io.IOException;

/**
 * Defines an instance for accessing resource groups in Azure.
 */
public interface ResourceGroups extends
        SupportsListing<ResourceGroup>,
        SupportsGetting<ResourceGroup>,
        SupportsCreating<ResourceGroup.DefinitionBlank>,
        SupportsDeleting,
        SupportsUpdating<ResourceGroup.Update> {
    /**
     * Checks whether resource group exists.
     *
     * @param name The name of the resource group to check. The name is case insensitive.
     * @throws CloudException exception thrown from REST call.
     * @throws IOException exception thrown from serialization/deserialization.
     * @return true if the resource group exists; false otherwise.
     */
    boolean checkExistence(String name) throws CloudException, IOException;
}
