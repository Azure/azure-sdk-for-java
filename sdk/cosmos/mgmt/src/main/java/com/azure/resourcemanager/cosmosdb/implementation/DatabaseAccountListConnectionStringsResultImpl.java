// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmosdb.implementation;

import com.azure.resourcemanager.cosmosdb.DatabaseAccountConnectionString;
import com.azure.resourcemanager.cosmosdb.DatabaseAccountListConnectionStringsResult;
import com.azure.resourcemanager.cosmosdb.models.DatabaseAccountListConnectionStringsResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import java.util.List;

/** The implementation for DatabaseAccountListConnectionStringsResult. */
public class DatabaseAccountListConnectionStringsResultImpl
    extends WrapperImpl<DatabaseAccountListConnectionStringsResultInner>
    implements DatabaseAccountListConnectionStringsResult {
    DatabaseAccountListConnectionStringsResultImpl(DatabaseAccountListConnectionStringsResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public List<DatabaseAccountConnectionString> connectionStrings() {
        return this.inner().connectionStrings();
    }
}
