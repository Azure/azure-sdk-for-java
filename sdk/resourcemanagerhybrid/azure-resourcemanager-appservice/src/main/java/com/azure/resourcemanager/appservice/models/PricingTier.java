// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collection;

/** Defines App service pricing tiers. */
@Fluent
public final class PricingTier {
    private static final AttributeCollection<PricingTier> COLLECTION = new AttributeCollection<>();

    /** Basic pricing tier with a small size. */
    public static final PricingTier BASIC_B1 =
        COLLECTION.addValue(new PricingTier(SkuName.BASIC.toString(), "B1"));

    /** Basic pricing tier with a medium size. */
    public static final PricingTier BASIC_B2 =
        COLLECTION.addValue(new PricingTier(SkuName.BASIC.toString(), "B2"));

    /** Basic pricing tier with a large size. */
    public static final PricingTier BASIC_B3 =
        COLLECTION.addValue(new PricingTier(SkuName.BASIC.toString(), "B3"));

    /** Standard pricing tier with a small size. */
    public static final PricingTier STANDARD_S1 =
        COLLECTION.addValue(new PricingTier(SkuName.STANDARD.toString(), "S1"));

    /** Standard pricing tier with a medium size. */
    public static final PricingTier STANDARD_S2 =
        COLLECTION.addValue(new PricingTier(SkuName.STANDARD.toString(), "S2"));

    /** Standard pricing tier with a large size. */
    public static final PricingTier STANDARD_S3 =
        COLLECTION.addValue(new PricingTier(SkuName.STANDARD.toString(), "S3"));

    /** Premium pricing tier with a small size. */
    public static final PricingTier PREMIUM_P1 =
        COLLECTION.addValue(new PricingTier(SkuName.PREMIUM.toString(), "P1"));

    /** Premium pricing tier with a medium size. */
    public static final PricingTier PREMIUM_P2 =
        COLLECTION.addValue(new PricingTier(SkuName.PREMIUM.toString(), "P2"));

    /** Premium pricing tier with a large size. */
    public static final PricingTier PREMIUM_P3 =
        COLLECTION.addValue(new PricingTier(SkuName.PREMIUM.toString(), "P3"));

    /** V2 Premium pricing tier with a small size. */
    public static final PricingTier PREMIUM_P1V2 =
        COLLECTION.addValue(new PricingTier(SkuName.PREMIUM_V2.toString(), "P1v2"));

    /** V2 Premium pricing tier with a medium size. */
    public static final PricingTier PREMIUM_P2V2 =
        COLLECTION.addValue(new PricingTier(SkuName.PREMIUM_V2.toString(), "P2v2"));

    /** V2 Premium pricing tier with a large size. */
    public static final PricingTier PREMIUM_P3V2 =
        COLLECTION.addValue(new PricingTier(SkuName.PREMIUM_V2.toString(), "P3v2"));

    /** V3 Premium pricing tier with a small size. */
    public static final PricingTier PREMIUM_P1V3 =
        COLLECTION.addValue(new PricingTier("PremiumV3", "P1v3"));

    /** V3 Premium pricing tier with a medium size. */
    public static final PricingTier PREMIUM_P2V3 =
        COLLECTION.addValue(new PricingTier("PremiumV3", "P2v3"));

    /** V3 Premium pricing tier with a large size. */
    public static final PricingTier PREMIUM_P3V3 =
        COLLECTION.addValue(new PricingTier("PremiumV3", "P3v3"));

    /** Free pricing tier. This does not work with Linux web apps, host name bindings, and SSL bindings. */
    public static final PricingTier FREE_F1 =
        COLLECTION.addValue(new PricingTier(SkuName.FREE.toString(), "F1"));

    /** Shared pricing tier. This does not work with Linux web apps, host name bindings, and SSL bindings. */
    public static final PricingTier SHARED_D1 =
        COLLECTION.addValue(new PricingTier(SkuName.SHARED.toString(), "D1"));

    /** The actual serialized value for a SiteAvailabilityState instance. */
    private final SkuDescription skuDescription;

    /**
     * Creates a custom app service pricing tier.
     *
     * @param tier the tier name
     * @param size the size of the plan
     */
    public PricingTier(String tier, String size) {
        this.skuDescription = new SkuDescription().withName(size).withTier(tier).withSize(size);
    }

    /**
     * Parses a serialized value to an AppServicePricingTier instance.
     *
     * @param skuDescription the serialized value to parse.
     * @return the parsed AppServicePricingTier object, or null if unable to parse.
     */
    public static PricingTier fromSkuDescription(SkuDescription skuDescription) {
        if (skuDescription == null) {
            return null;
        }
        return new PricingTier(skuDescription.tier(), skuDescription.size());
    }

    /**
     * Lists the pre-defined app service pricing tiers.
     *
     * @return immutable collection of the pre-defined app service pricing tiers
     */
    public static Collection<PricingTier> getAll() {
        return COLLECTION.getAllValues();
    }

    @Override
    public String toString() {
        return skuDescription.tier() + "_" + skuDescription.size();
    }

    /** @return the underneath sku description */
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
        if (!(obj instanceof PricingTier)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        PricingTier rhs = (PricingTier) obj;
        return toString().equalsIgnoreCase(rhs.toString());
    }
}
