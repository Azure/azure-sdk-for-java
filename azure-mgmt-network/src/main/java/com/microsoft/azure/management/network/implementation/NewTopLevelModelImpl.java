/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.AddressSpace;
import com.microsoft.azure.management.network.DhcpOptions;
import com.microsoft.azure.management.network.NewChildModel;
import com.microsoft.azure.management.network.NewTopLevelModel;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for Foo and its create and update interfaces.
 */
@LangDefinition
class NewTopLevelModelImpl
    extends GroupableParentResourceImpl<
        NewTopLevelModel,
        VirtualNetworkInner,
        NewTopLevelModelImpl,
        NetworkManager>
    implements
        NewTopLevelModel,
        NewTopLevelModel.Definition,
        NewTopLevelModel.Update {

    private Map<String, NewChildModel> childModels;

    NewTopLevelModelImpl(String name,
            final VirtualNetworkInner innerModel,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.childModels = new HashMap<>();
        List<SubnetInner> inners = this.inner().subnets();
        if (inners != null) {
            for (SubnetInner inner : inners) {
                NewChildModelImpl child = new NewChildModelImpl(inner, this);
                this.childModels.put(inner.name(), child);
            }
        }
    }

    // Verbs

    @Override
    public Observable<NewTopLevelModel> refreshAsync() {
        return super.refreshAsync().map(new Func1<NewTopLevelModel, NewTopLevelModel>() {
            @Override
            public NewTopLevelModel call(NewTopLevelModel foo) {
                NewTopLevelModelImpl impl = (NewTopLevelModelImpl) foo;
                impl.initializeChildrenFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<VirtualNetworkInner> getInnerAsync() {
        return this.manager().inner().virtualNetworks().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    // Helpers

    // Setters (fluent)

    NewTopLevelModelImpl withNewChildModel(NewChildModelImpl child) {
        this.childModels.put(child.name(), child);
        return this;
    }

    // Getters

    @Override
    protected void beforeCreating() {
        if (isInCreateMode()) {
            // TODO something
        }

        // Reset and update subnets
        this.inner().withSubnets(innersFromWrappers(this.childModels.values()));
    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
    }

    @Override
    protected Observable<VirtualNetworkInner> createInner() {
        return this.manager().inner().virtualNetworks().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    public NewTopLevelModelImpl withAddressSpace(String cidr) {
        AddressSpace addressSpace = this.inner().addressSpace();
        if (addressSpace == null) {
            addressSpace = new AddressSpace();
            this.inner().withAddressSpace(addressSpace);
        }

        List<String> cidrs = addressSpace.addressPrefixes();
        if (cidrs == null) {
            cidrs = new ArrayList<>();
            addressSpace.withAddressPrefixes(cidrs);
        }

        cidrs.add(cidr);
        return this;
    }

    @Override
    public NewTopLevelModelImpl withDhcpOptions(DhcpOptions options) {
        this.inner().withDhcpOptions(options);
        return this;
    }

    @Override
    public NewTopLevelModelImpl withAddressSpaces(String... cidrs) {
        if (cidrs != null) {
            for (String cidr : cidrs) {
                withAddressSpace(cidr);
            }
        }
        return this;
    }

    @Override
    public Set<String> addressSpaces() {
        Set<String> addressSpaces = new HashSet<>();
        if (this.inner().addressSpace() == null) {
            return addressSpaces;
        } else if (this.inner().addressSpace().addressPrefixes() == null) {
            return addressSpaces;
        } else {
            return new HashSet<>(this.inner().addressSpace().addressPrefixes());
        }
    }

    @Override
    public DhcpOptions dhcpOptions() {
        return this.inner().dhcpOptions();
    }

    @Override
    public NewTopLevelModelImpl withoutAddressSpace(String cidr) {
        if (this.inner().addressSpace() == null) {
            return this;
        } else if (this.inner().addressSpace().addressPrefixes() == null) {
            return this;
        } else {
            this.inner().addressSpace().addressPrefixes().remove(cidr);
            return this;
        }
    }

    @Override
    public NewChildModelImpl defineChildModel(String name) {
        SubnetInner inner = new SubnetInner().withName(name);
        return new NewChildModelImpl(inner, this);
    }

    @Override
    public NewChildModelImpl updateChildModel(String name) {
        return (NewChildModelImpl) this.childModels.get(name);
    }

    @Override
    public NewTopLevelModelImpl withoutChildModel(String name) {
        this.childModels.remove(name);
        return this;
    }

    @Override
    public Map<String, NewChildModel> childModels() {
        return Collections.unmodifiableMap(this.childModels);
    }
}
