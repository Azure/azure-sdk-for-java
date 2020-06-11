// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.resourcemanager.cosmos.models.DatabaseAccountListKeysResult;
import com.azure.resourcemanager.cosmos.fluent.inner.DatabaseAccountListKeysResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The implementation for DatabaseAccountListKeysResult. */
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
