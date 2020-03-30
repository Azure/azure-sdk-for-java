/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.ApplicationSecurityGroup;
import com.azure.management.network.models.ApplicationSecurityGroupInner;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation for ApplicationSecurityGroup and its create and update interfaces.
 */
class ApplicationSecurityGroupImpl
        extends GroupableResourceImpl<
        ApplicationSecurityGroup,
        ApplicationSecurityGroupInner,
        ApplicationSecurityGroupImpl,
        NetworkManager>
        implements
        ApplicationSecurityGroup,
        ApplicationSecurityGroup.Definition,
        ApplicationSecurityGroup.Update {

    ApplicationSecurityGroupImpl(
            final String name,
            final ApplicationSecurityGroupInner innerModel,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected Mono<ApplicationSecurityGroupInner> getInnerAsync() {
        return this.manager().inner().applicationSecurityGroups().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<ApplicationSecurityGroup> createResourceAsync() {
        return this.manager().inner().applicationSecurityGroups().createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public String resourceGuid() {
        return inner().resourceGuid();
    }

    @Override
    public String provisioningState() {
        return inner().provisioningState();
    }
}
