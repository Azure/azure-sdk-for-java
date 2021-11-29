// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to tenant management API.
 */
@Fluent
public interface PolicyDefinitions extends
        SupportsListing<PolicyDefinition>,
        SupportsGettingByName<PolicyDefinition>,
        SupportsCreating<PolicyDefinition.DefinitionStages.Blank>,
        SupportsDeletingByName {
}
