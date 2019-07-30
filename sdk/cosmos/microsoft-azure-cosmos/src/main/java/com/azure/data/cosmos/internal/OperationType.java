// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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