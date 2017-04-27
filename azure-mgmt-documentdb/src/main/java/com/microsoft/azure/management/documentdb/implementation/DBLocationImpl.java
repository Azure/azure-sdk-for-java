/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.documentdb.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.documentdb.DBLocation;
import com.microsoft.azure.management.documentdb.DatabaseAccount;
import com.microsoft.azure.management.documentdb.Location;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

@LangDefinition
class DBLocationImpl
    extends ChildResourceImpl<Location,
        DatabaseAccountImpl,
        DatabaseAccount>
    implements
        DBLocation,
        DBLocation.Definition<DatabaseAccount.DefinitionStages.WithCreate>,
        DBLocation.Update {

    DBLocationImpl(Location inner, DatabaseAccountImpl parent) {
        super(inner, parent);
    }

    @Override
    public String documentEndpoint() {
        return this.inner().documentEndpoint();
    }

    @Override
    public int failoverPriority() {
        return this.inner().failoverPriority();
    }

    @Override
    public DBLocationImpl withFailoverPriority(int param0) {
        this.inner().withFailoverPriority(param0);
        return this;        
    }


    @Override
    public String name() {
        return this.inner().locationName();
    }

    public void withLocation(String name) {
        this.inner().withLocationName(name);
    }

    @Override
    public DatabaseAccount.Definition attach() {
        this.parent().addDBLocation(this);
        return this.parent();
    }
}
