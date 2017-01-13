/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;

/**
 * Entry point for certificate management API.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface AppServiceCertificates extends
        SupportsCreating<AppServiceCertificate.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsListingByGroup<AppServiceCertificate>,
        SupportsGettingByGroup<AppServiceCertificate>,
        SupportsGettingById<AppServiceCertificate>,
        SupportsDeletingByGroup,
        HasManager<AppServiceManager> {
}