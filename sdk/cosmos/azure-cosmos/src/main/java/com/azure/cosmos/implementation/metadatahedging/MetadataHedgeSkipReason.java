// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

/**
 * Reason a metadata hedge was not dispatched. Recorded in {@link MetadataHedgeDiagnostics}
 * for supportability.
 * <p>
 * Java port of the .NET {@code MetadataHedgingStrategy.MetadataHedgeSkipReason} enum
 * (Azure/azure-cosmos-dotnet-v3#5923).
 */
public enum MetadataHedgeSkipReason {
    NONE,
    OPT_IN_DISABLED,
    PPAF_DISABLED,
    GATEWAY_KILL_SWITCH_ON,
    SINGLE_REGION,
    NOT_COLD_START,
    RESOURCE_TYPE_NOT_SUPPORTED,
    NOT_FIRST_READ_FEED_PAGE,
    BUDGET_EXHAUSTED,
    ALREADY_HEDGED_THIS_OPERATION,
    EXCLUDED_REGION_LEAVES_NO_TARGET
}
