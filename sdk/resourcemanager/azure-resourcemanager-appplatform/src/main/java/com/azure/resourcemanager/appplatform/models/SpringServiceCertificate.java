// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.resourcemanager.appplatform.fluent.models.CertificateResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable client-side representation of an Azure Spring Service Certificate. */
public interface SpringServiceCertificate
    extends ExternalChildResource<SpringServiceCertificate, SpringService>,
        HasInnerModel<CertificateResourceInner> {
    /** @return the properties of the service binding */
    CertificateProperties properties();
}
