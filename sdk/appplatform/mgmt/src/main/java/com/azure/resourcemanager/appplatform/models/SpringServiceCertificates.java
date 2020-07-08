// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.CertificatesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** Entry point for Spring Service Certificates API. */
@Fluent
@Beta
public interface SpringServiceCertificates
    extends HasManager<AppPlatformManager>,
    HasInner<CertificatesClient>,
    HasParent<SpringService>,
    SupportsGettingById<SpringServiceCertificate>,
    SupportsGettingByName<SpringServiceCertificate>,
    SupportsListing<SpringServiceCertificate>,
    SupportsDeletingById,
    SupportsDeletingByName {
}
