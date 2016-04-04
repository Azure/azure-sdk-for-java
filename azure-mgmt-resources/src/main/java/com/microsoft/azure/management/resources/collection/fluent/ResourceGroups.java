package com.microsoft.azure.management.resources.collection.fluent;

import com.microsoft.azure.management.resources.fluentcore.collection.*;
import com.microsoft.azure.management.resources.models.fluent.ResourceGroup;

public interface ResourceGroups extends
        SupportsListing<ResourceGroup>,
        SupportsGetting<ResourceGroup>,
        SupportsCreating<ResourceGroup.DefinitionBlank>,
        SupportsDeleting,
        SupportsUpdating<ResourceGroup.UpdateBlank> {
}
