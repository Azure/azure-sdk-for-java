// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.HashSet;
import java.util.Set;

/**
 * Operation types in the Azure Cosmos DB database service.
 */
public enum OperationType {
    AbortPartitionMigration,
    AbortSplit,
    AddComputeGatewayRequestCharges,
    Batch,
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
    QueryPlan,
    Stop,
    Throttle,
    Patch,
    Upsert;

    private static final Set<OperationType> writeOperationTypes = new HashSet<>() {{
        add(Create);
        add(Delete);
        add(Recreate);
        add(ExecuteJavaScript);
        add(Replace);
        add(Upsert);
        add(Patch);
        add(Batch);
    }};

    private static final Set<OperationType> pointOperationTypes = new HashSet<>() {{
        add(Create);
        add(Delete);
        add(Replace);
        add(Upsert);
        add(Patch);
        add(Read);
    }};

    public boolean isWriteOperation() {
        return writeOperationTypes.contains(this);
    }

    public boolean isPointOperation() {
        return pointOperationTypes.contains(this);
    }
}
