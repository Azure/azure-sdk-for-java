/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.scheduler.implementation.JobCollectionsInner;
import com.microsoft.azure.management.scheduler.implementation.ScheduleServiceManager;

/**
 * Entry point to Job Collection management API for Scheduler service in Azure.
 */
@Fluent()
@Beta(Beta.SinceVersion.V1_2_0)
public interface JobCollections extends
    SupportsCreating<JobCollection.DefinitionStages.Blank>,
    HasManager<ScheduleServiceManager>,
    HasInner<JobCollectionsInner>,
    SupportsBatchCreation<JobCollection>,
    SupportsGettingById<JobCollection>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsListingByResourceGroup<JobCollection>,
    SupportsGettingByResourceGroup<JobCollection>,
    SupportsListing<JobCollection> {
}
