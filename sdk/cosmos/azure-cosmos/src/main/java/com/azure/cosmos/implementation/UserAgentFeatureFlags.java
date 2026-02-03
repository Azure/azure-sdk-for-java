// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

/**
 * An enum representing various flags whose opt-in context will appear in the user agent suffix.
 * <p>
 * IMPORTANT: When adding a new flag, please ensure the following:
 * <ul>
 *     <li>The value of the flag in the enum should follow the pattern FlagName(1 << (n - 1)) where n is the effective ordinal of the flag in the enum.</li>
 *     <li>The value of the flag should be unique and not overlap with other flags.</li>
 *     <li>Please keep the value consistent with that of other SDKs - (e.g. see - <a href="https://github.com/Azure/azure-cosmos-dotnet-v3/blob/master/Microsoft.Azure.Cosmos/src/Diagnostics/UserAgentFeatureFlags.cs">UserAgentFeatureFlags.cs</a>) for more details.</li>
 * </ul>
 * </p>
 */
public enum UserAgentFeatureFlags {
    PerPartitionAutomaticFailover(1),
    PerPartitionCircuitBreaker(1 << 1),
    ThinClient(1 << 2),
    // BinaryEncoding(1 << 3),
    Http2(1 << 4),
    RegionScopedSessionCapturing(1 << 5);

    private final int value;

    UserAgentFeatureFlags(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
