// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi.implementation;

import com.azure.resourcemanager.msi.MsiManager;
import com.azure.resourcemanager.msi.models.Identities;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.msi.fluent.models.IdentityInner;
import com.azure.resourcemanager.msi.fluent.UserAssignedIdentitiesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for Identities.
 */
public final class IdentitesImpl
    extends TopLevelModifiableResourcesImpl<Identity,
        IdentityImpl,
        IdentityInner,
    UserAssignedIdentitiesClient,
    MsiManager>
    implements Identities {

    public IdentitesImpl(UserAssignedIdentitiesClient innerCollection, MsiManager manager) {
        super(innerCollection, manager);
    }

    @Override
    protected IdentityImpl wrapModel(String name) {
        return new IdentityImpl(name, new IdentityInner(), this.manager());
    }

    @Override
    protected IdentityImpl wrapModel(IdentityInner inner) {
        if (inner == null) {
            return null;
        } else {
            return new IdentityImpl(inner.name(), inner, this.manager());
        }
    }

    @Override
    public Identity.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }
}
