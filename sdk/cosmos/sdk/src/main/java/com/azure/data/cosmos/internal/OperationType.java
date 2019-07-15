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

package com.azure.data.cosmos.internal;

/**
 * Operation types in the Azure Cosmos DB database service.
 */
public enum OperationType {
    AbortPartitionMigration,
    AbortSplit,
    AddComputeGatewayRequestCharges,
    BatchApply,
    BatchReportThroughputUtilization,
    CompletePartitionMigration,
    CompleteSplit,
    Crash,
    Create,
    Delete,
    ExecuteJavaScript,
    ForceConfigRefresh,
    GetSplitPoint,
    Head,
    HeadFeed,
    MigratePartition,
    Pause,
    PreCreateValidation,
    OfferPreGrowValidation,
    OfferUpdateOperation,
    PreReplaceValidation,
    Query,
    Read,
    ReadFeed,
    Recreate,
    Recycle,
    Replace,
    Resume,
    SqlQuery,
    Stop,
    Throttle,
    Update,
    Upsert;

    public boolean isWriteOperation() {
        return this == Create ||
                this == Delete ||
                this == Recreate ||
                this == ExecuteJavaScript ||
                this == Replace ||
                this == Upsert ||
                this == Update;
    }
}