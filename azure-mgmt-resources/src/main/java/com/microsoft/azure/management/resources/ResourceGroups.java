package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.collection.*;

import java.io.IOException;

public interface ResourceGroups extends
        SupportsListing<ResourceGroup>,
        SupportsGetting<ResourceGroup>,
        SupportsCreating<ResourceGroup.DefinitionBlank>,
        SupportsDeleting,
        SupportsUpdating<ResourceGroup.Update> {
    boolean checkExistence(String name) throws CloudException, IOException;
}
