// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

/**
 * Identifies the branch (primary or hedge) that produced a candidate metadata-hedge winner.
 * Used to compose the per-branch overlay in
 * {@link MetadataHedgingStrategy#isAcceptableWinner}.
 * <p>
 * Java port of the .NET {@code MetadataHedgingStrategy.HedgeBranch} enum
 * (Azure/azure-cosmos-dotnet-v3#5923).
 */
public enum HedgeBranch {
    PRIMARY,
    HEDGE
}
