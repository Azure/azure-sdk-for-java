/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.DatabaseAccount;

/**
 * Used internally to provides functionality to work with database account configuration in the Azure Cosmos DB database service.
 */
public class BaseDatabaseAccountConfigurationProvider implements DatabaseAccountConfigurationProvider {
    private ConsistencyLevel desiredConsistencyLevel;
    private DatabaseAccount databaseAccount;

    public BaseDatabaseAccountConfigurationProvider(DatabaseAccount databaseAccount, ConsistencyLevel desiredConsistencyLevel) {
        this.databaseAccount = databaseAccount;
        this.desiredConsistencyLevel = desiredConsistencyLevel;
    }

    public ConsistencyLevel getStoreConsistencyPolicy() {
        ConsistencyLevel databaseAccountConsistency = this.databaseAccount.getConsistencyPolicy().getDefaultConsistencyLevel();
        if (this.desiredConsistencyLevel == null) {
            return databaseAccountConsistency;
        } else if (!Utils.isValidConsistency(databaseAccountConsistency, this.desiredConsistencyLevel)) {
            throw new IllegalArgumentException(String.format(
                    "ConsistencyLevel %1s specified in the request is invalid when service is configured with consistency level %2s. Ensure the request consistency level is not stronger than the service consistency level.",
                    this.desiredConsistencyLevel.toString(),
                    databaseAccountConsistency.toString()));
        } else {
            return this.desiredConsistencyLevel;
        }
    }

    public int getMaxReplicaSetSize() {
        return this.databaseAccount.getReplicationPolicy().getMaxReplicaSetSize();
    }

    @Override
    public String getQueryEngineConfiguration() {
        return databaseAccount.get("queryEngineConfiguration").toString();
    }
}
