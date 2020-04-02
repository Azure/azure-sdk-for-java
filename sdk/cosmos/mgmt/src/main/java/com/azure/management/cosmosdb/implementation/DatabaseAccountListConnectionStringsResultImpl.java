/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.cosmosdb.implementation;


import com.azure.management.cosmosdb.DatabaseAccountConnectionString;
import com.azure.management.cosmosdb.DatabaseAccountListConnectionStringsResult;
import com.azure.management.cosmosdb.models.DatabaseAccountListConnectionStringsResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.List;

/**
 * The implementation for DatabaseAccountListConnectionStringsResult.
 */
public class DatabaseAccountListConnectionStringsResultImpl extends WrapperImpl<DatabaseAccountListConnectionStringsResultInner>
    implements DatabaseAccountListConnectionStringsResult {
    DatabaseAccountListConnectionStringsResultImpl(DatabaseAccountListConnectionStringsResultInner innerObject) {
        super(innerObject);
    }

    @Override
    public List<DatabaseAccountConnectionString> connectionStrings() {
        return this.inner().connectionStrings();
    }
}
