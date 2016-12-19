/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;
import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Defines App service pricing tiers.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public class AppServicePricingTier {
    /** Free app service plan. */
    public static final AppServicePricingTier FREE_F1 = new AppServicePricingTier("Free", "F1");

    /** App service plan with shared infrastructure. */
    public static final AppServicePricingTier SHARED_D1 = new AppServicePricingTier("Shared", "D1");

    /** Basic pricing tier with a small size. */
    public static final AppServicePricingTier BASIC_B1 = new AppServicePricingTier("Basic", "B1");

    /** Basic pricing tier with a medium size. */
    public static final AppServicePricingTier BASIC_B2 = new AppServicePricingTier("Basic", "B2");

    /** Basic pricing tier with a large size. */
    public static final AppServicePricingTier BASIC_B3 = new AppServicePricingTier("Basic", "B3");

    /** Standard pricing tier with a small size. */
    public static final AppServicePricingTier STANDARD_S1 = new AppServicePricingTier("Standard", "S1");

    /** Standard pricing tier with a medium size. */
    public static final AppServicePricingTier STANDARD_S2 = new AppServicePricingTier("Standard", "S2");

    /** Standard pricing tier with a large size. */
    public static final AppServicePricingTier STANDARD_S3 = new AppServicePricingTier("Standard", "S3");

    /** Premium pricing tier with a small size. */
    public static final AppServicePricingTier PREMIUM_P1 = new AppServicePricingTier("Premium", "P1");

    /** Premium pricing tier with a medium size. */
    public static final AppServicePricingTier PREMIUM_P2 = new AppServicePricingTier("Premium", "P2");

    /** Premium pricing tier with a large size. */
    public static final AppServicePricingTier PREMIUM_P3 = new AppServicePricingTier("Premium", "P3");

    /** The actual serialized value for a SiteAvailabilityState instance. */
    private SkuDescription skuDescription;

    /**
     * Creates a custom app service pricing tier.
     * @param tier the tier name
     * @param size the size of the plan
     */
    public AppServicePricingTier(String tier, String size) {
        this.skuDescription = new SkuDescription()
                .withName(size)
                .withTier(tier)
                .withSize(size);
    }

    /**
     * Parses a serialized value to an AppServicePricingTier instance.
     *
     * @param skuDescription the serialized value to parse.
     * @return the parsed AppServicePricingTier object, or null if unable to parse.
     */
    public static AppServicePricingTier fromSkuDescription(SkuDescription skuDescription) {
        if (skuDescription == null) {
            return null;
        }
        return new AppServicePricingTier(skuDescription.tier(), skuDescription.size());
    }

    @Override
    public String toString() {
        return skuDescription.tier() + "_" + skuDescription.size();
    }

    /**
     * @return the underneath sku description
     */
    @JsonValue
    public SkuDescription toSkuDescription() {
        return this.skuDescription;
    }

    @Override
    public int hashCode() {
        return skuDescription.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AppServicePricingTier)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        AppServicePricingTier rhs = (AppServicePricingTier) obj;
        return toString().equalsIgnoreCase(rhs.toString());
    }
}