/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb.implementation;

import com.azure.management.cosmosdb.DatabaseAccountListReadOnlyKeysResult;
import com.azure.management.cosmosdb.models.DatabaseAccountListReadOnlyKeysResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for DatabaseAccountListReadOnlyKeysResult.
 */
public class DatabaseAccountListReadOnlyKeysResultImpl extends WrapperImpl<DatabaseAccountListReadOnlyKeysResultInner>
    implements DatabaseAccountListReadOnlyKeysResult {
    DatabaseAccountListReadOnlyKeysResultImpl(DatabaseAccountListReadOnlyKeysResultInner innerObject) {
        super(innerObject);
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
