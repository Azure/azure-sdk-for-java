// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.base.profile.AzureProfile;
import com.azure.resourcemanager.base.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/**
 * Generic base class for Azure resource managers.
 *
 * @param <T> specific manager type
 * @param <InnerT> inner management client implementation type
 */
public abstract class Manager<T, InnerT> extends ManagerBase implements HasInner<InnerT> {

    protected final InnerT innerManagementClient;

    protected Manager(HttpPipeline httpPipeline, AzureProfile profile,
                      InnerT innerManagementClient, SdkContext sdkContext) {
        super(httpPipeline, profile, sdkContext);
        this.innerManagementClient = innerManagementClient;
    }

    @Override
    public InnerT inner() {
        return this.innerManagementClient;
    }
}
