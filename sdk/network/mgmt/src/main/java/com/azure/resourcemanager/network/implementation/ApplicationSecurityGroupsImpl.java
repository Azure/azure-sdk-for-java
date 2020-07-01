// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.ApplicationSecurityGroupsClient;
import com.azure.resourcemanager.network.fluent.inner.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroup;
import com.azure.resourcemanager.network.models.ApplicationSecurityGroups;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for ApplicationSecurityGroups. */
public class ApplicationSecurityGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        ApplicationSecurityGroup,
        ApplicationSecurityGroupImpl,
        ApplicationSecurityGroupInner,
    ApplicationSecurityGroupsClient,
    NetworkManager>
    implements ApplicationSecurityGroups {

    public ApplicationSecurityGroupsImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getApplicationSecurityGroups(), networkManager);
    }

    @Override
    public ApplicationSecurityGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ApplicationSecurityGroupImpl wrapModel(String name) {
        ApplicationSecurityGroupInner inner = new ApplicationSecurityGroupInner();
        return new ApplicationSecurityGroupImpl(name, inner, super.manager());
    }

    @Override
    protected ApplicationSecurityGroupImpl wrapModel(ApplicationSecurityGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new ApplicationSecurityGroupImpl(inner.name(), inner, this.manager());
    }
}
