// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.resourcemanager.appplatform.fluent.inner.BindingResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** An immutable client-side representation of an Azure Spring App Service Binding. */
public interface SpringAppServiceBinding
    extends ExternalChildResource<SpringAppServiceBinding, SpringApp>,
    HasInner<BindingResourceInner> {
    /** @return the properties of the service binding */
    BindingResourceProperties properties();
}
