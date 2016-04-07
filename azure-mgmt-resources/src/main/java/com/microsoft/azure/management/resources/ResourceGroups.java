package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.collection.*;
import com.microsoft.azure.management.resources.models.ResourceGroup;

public interface ResourceGroups extends
        SupportsListing<ResourceGroup>,
        SupportsGetting<ResourceGroup>,
        SupportsCreating<ResourceGroup.DefinitionBlank>,
        SupportsDeleting,
        SupportsUpdating<ResourceGroup.UpdateBlank> {
}
