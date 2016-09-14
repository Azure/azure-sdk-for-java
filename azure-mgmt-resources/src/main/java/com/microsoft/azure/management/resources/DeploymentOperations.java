/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to deployment operation management API.
 */
@LangDefinition(ContainerName = "~/")
public interface DeploymentOperations extends
        SupportsListing<DeploymentOperation>,
        SupportsGettingById<DeploymentOperation> {
}
