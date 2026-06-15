// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

/**
 * Output of {@link MetadataHedgingStrategy#evaluateEligibility}.
 * <p>
 * Java port of the .NET {@code MetadataHedgingStrategy.MetadataHedgeEligibility}
 * (Azure/azure-cosmos-dotnet-v3#5923).
 */
public final class MetadataHedgeEligibility {

    private final boolean isEligible;
    private final MetadataHedgeSkipReason skipReason;

    private MetadataHedgeEligibility(boolean isEligible, MetadataHedgeSkipReason skipReason) {
        this.isEligible = isEligible;
        this.skipReason = skipReason;
    }

    public boolean isEligible() {
        return this.isEligible;
    }

    public MetadataHedgeSkipReason getSkipReason() {
        return this.skipReason;
    }

    public static MetadataHedgeEligibility eligible() {
        return new MetadataHedgeEligibility(true, MetadataHedgeSkipReason.NONE);
    }

    public static MetadataHedgeEligibility skip(MetadataHedgeSkipReason reason) {
        return new MetadataHedgeEligibility(false, reason);
    }
}
