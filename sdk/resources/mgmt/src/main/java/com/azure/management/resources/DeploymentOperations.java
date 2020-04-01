/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to deployment operation management API.
 */
@Fluent
public interface DeploymentOperations extends
        SupportsListing<DeploymentOperation>,
        SupportsGettingById<DeploymentOperation> {
}
