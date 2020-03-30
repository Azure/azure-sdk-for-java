/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.annotation.Fluent;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.arm.models.HasParent;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point for Azure web app deployment slot management API.
 */
@Fluent
public interface DeploymentSlots extends
        SupportsCreating<DeploymentSlot.DefinitionStages.Blank>,
        SupportsListing<DeploymentSlot>,
        SupportsGettingByName<DeploymentSlot>,
        SupportsGettingById<DeploymentSlot>,
        SupportsDeletingById,
        SupportsDeletingByName,
        HasManager<AppServiceManager>,
        HasParent<WebApp> {
}