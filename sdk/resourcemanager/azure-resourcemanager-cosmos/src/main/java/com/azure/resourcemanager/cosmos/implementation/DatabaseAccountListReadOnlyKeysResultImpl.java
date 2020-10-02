// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.implementation;

import com.azure.resourcemanager.cosmos.models.DatabaseAccountListReadOnlyKeysResult;
import com.azure.resourcemanager.cosmos.fluent.models.DatabaseAccountListReadOnlyKeysResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The implementation for DatabaseAccountListReadOnlyKeysResult. */
public class DatabaseAccountListReadOnlyKeysResultImpl extends WrapperImpl<DatabaseAccountListReadOnlyKeysResultInner>
    implements DatabaseAccountListReadOnlyKeysResult {
    DatabaseAccountListReadOnlyKeysResultImpl(DatabaseAccountListReadOnlyKeysResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public String primaryReadonlyMasterKey() {
        return this.innerModel().primaryReadonlyMasterKey();
    }

    @Override
    public String secondaryReadonlyMasterKey() {
        return this.innerModel().secondaryReadonlyMasterKey();
    }
}
