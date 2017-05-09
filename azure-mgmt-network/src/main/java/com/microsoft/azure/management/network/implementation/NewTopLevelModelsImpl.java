/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NewTopLevelModel;
import com.microsoft.azure.management.network.NewTopLevelModels;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 *  Implementation for Foo.
 */
@LangDefinition
class NewTopLevelModelsImpl
    extends TopLevelModifiableResourcesImpl<
        NewTopLevelModel,
        NewTopLevelModelImpl,
        VirtualNetworkInner,
        VirtualNetworksInner,
        NetworkManager>
    implements NewTopLevelModels {

    NewTopLevelModelsImpl(final NetworkManager networkManager) {
        super(networkManager.inner().virtualNetworks(), networkManager);
    }

    @Override
    public NewTopLevelModelImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NewTopLevelModelImpl wrapModel(String name) {
        VirtualNetworkInner inner = new VirtualNetworkInner();

        return new NewTopLevelModelImpl(name, inner, super.manager());
    }

    @Override
    protected NewTopLevelModelImpl wrapModel(VirtualNetworkInner inner) {
        if (inner == null) {
            return null;
        }
        return new NewTopLevelModelImpl(inner.name(), inner, this.manager());
    }
}
