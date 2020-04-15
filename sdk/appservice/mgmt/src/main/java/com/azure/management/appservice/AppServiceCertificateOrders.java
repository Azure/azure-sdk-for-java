// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.appservice;

import com.azure.core.annotation.Fluent;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.appservice.models.AppServiceCertificateOrdersInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/** Entry point for app service certificate order management API. */
@Fluent
public interface AppServiceCertificateOrders
    extends SupportsCreating<AppServiceCertificateOrder.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByResourceGroup<AppServiceCertificateOrder>,
        SupportsGettingByResourceGroup<AppServiceCertificateOrder>,
        SupportsListing<AppServiceCertificateOrder>,
        SupportsGettingById<AppServiceCertificateOrder>,
        SupportsDeletingByResourceGroup,
        HasManager<AppServiceManager>,
        HasInner<AppServiceCertificateOrdersInner> {
}
