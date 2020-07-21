// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.resourcemanager.appplatform.fluent.inner.CustomDomainResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** An immutable client-side representation of an Azure Spring App Custom Domain. */
public interface SpringAppDomain
    extends ExternalChildResource<SpringAppDomain, SpringApp>,
    HasInner<CustomDomainResourceInner> {
    /** @return the properties of the spring app custom domain */
    CustomDomainProperties properties();
}
