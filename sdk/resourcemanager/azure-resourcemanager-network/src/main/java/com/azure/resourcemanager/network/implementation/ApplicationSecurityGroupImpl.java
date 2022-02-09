// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.fluent.models.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

/** Implementation for ApplicationSecurityGroup and its create and update interfaces. */
class ApplicationSecurityGroupImpl
    extends GroupableResourceImpl<
        ApplicationSecurityGroup, ApplicationSecurityGroupInner, ApplicationSecurityGroupImpl, NetworkManager>
    implements ApplicationSecurityGroup, ApplicationSecurityGroup.Definition, ApplicationSecurityGroup.Update {

    ApplicationSecurityGroupImpl(
        final String name, final ApplicationSecurityGroupInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected Mono<ApplicationSecurityGroupInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getApplicationSecurityGroups()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<ApplicationSecurityGroup> createResourceAsync() {
        return this
            .manager()
            .serviceClient()
            .getApplicationSecurityGroups()
            .createOrUpdateAsync(resourceGroupName(), name(), innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    public String resourceGuid() {
        return innerModel().resourceGuid();
    }

    @Override
    public String provisioningState() {
        return innerModel().provisioningState().toString();
    }
}
