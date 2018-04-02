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

/**
 * Resource types in the Azure Cosmos DB database service.
 */
public enum ResourceType {
    Attachment,
    Conflict,
    Database,
    DatabaseAccount,
    Document,
    DocumentCollection,
    Media,  // Media doesn't have a corresponding resource class.
    Offer,
    PartitionKeyRange,
    Permission,
    StoredProcedure,
    Trigger,
    User,
    UserDefinedFunction,
    MasterPartition,
    ServerPartition,
    Topology,
    Schema;

    public boolean isScript() {
        return this == ResourceType.UserDefinedFunction
                || this == ResourceType.Trigger
                || this == ResourceType.StoredProcedure;
    }

    public boolean isPartitioned() {
        return this == ResourceType.Document ||
                this == ResourceType.Attachment ||
                this == ResourceType.Conflict ||
                this == ResourceType.Schema;
    }

    public boolean isMasterResource() {
        return this == ResourceType.Offer ||
                this == ResourceType.Database ||
                this == ResourceType.User ||
                this == ResourceType.Permission ||
                this == ResourceType.Topology ||
                this == ResourceType.PartitionKeyRange ||
                this == ResourceType.DocumentCollection;
    }

    /**
     * @return
     */
    public boolean isCollectionChild() {
        return this == ResourceType.Document ||
                this == ResourceType.Attachment ||
                this == ResourceType.Conflict ||
                this == ResourceType.Schema ||
                this.isScript();
    }
}
