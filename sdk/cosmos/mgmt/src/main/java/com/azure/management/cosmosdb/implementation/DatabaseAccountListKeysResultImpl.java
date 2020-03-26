/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb.implementation;


import com.azure.management.cosmosdb.DatabaseAccountListKeysResult;
import com.azure.management.cosmosdb.models.DatabaseAccountListKeysResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for DatabaseAccountListKeysResult.
 */
public class DatabaseAccountListKeysResultImpl extends WrapperImpl<DatabaseAccountListKeysResultInner>
    implements DatabaseAccountListKeysResult {
    DatabaseAccountListKeysResultImpl(DatabaseAccountListKeysResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public String primaryMasterKey() {
        return this.inner().primaryMasterKey();
    }

    @Override
    public String secondaryMasterKey() {
        return this.inner().secondaryMasterKey();
    }

    @Override
    public String primaryReadonlyMasterKey() {
        return this.inner().primaryReadonlyMasterKey();
    }

    @Override
    public String secondaryReadonlyMasterKey() {
        return this.inner().secondaryReadonlyMasterKey();
    }
}
