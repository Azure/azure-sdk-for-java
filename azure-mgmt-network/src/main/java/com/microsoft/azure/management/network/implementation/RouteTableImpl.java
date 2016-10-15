/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.RouteTable;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import rx.Observable;

/**
 * Implementation for RouteTable.
 */
@LangDefinition
class RouteTableImpl
    extends GroupableParentResourceImpl<
        RouteTable,
        RouteTableInner,
        RouteTableImpl,
        NetworkManager>
    implements
        RouteTable,
        RouteTable.Definition,
        RouteTable.Update {

	final RouteTablesInner innerCollection;
	
    RouteTableImpl(String name,
            final RouteTableInner innerModel,
            final RouteTablesInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
    }

    @Override
    protected void initializeChildrenFromInner() {
    }

    // Verbs

    @Override
    public RouteTableImpl refresh() {

        RouteTableInner inner = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        initializeChildrenFromInner();
        return this;
    }

    // Helpers

    NetworkManager manager() {
        return super.myManager;
    }

    // Setters (fluent)

    // Getters

    @Override
    protected void beforeCreating() {
    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
    }

    @Override
    protected Observable<RouteTableInner> createInner() {
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }
}
