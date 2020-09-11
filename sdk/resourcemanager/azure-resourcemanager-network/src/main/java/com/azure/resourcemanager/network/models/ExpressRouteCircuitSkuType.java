// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Express route circuit sku type. */
public class ExpressRouteCircuitSkuType {
    private static final Map<String, ExpressRouteCircuitSkuType> VALUES_BY_NAME = new HashMap<>();

    /** Static value for Standard sku tier and MeteredData sku family. */
    public static final ExpressRouteCircuitSkuType STANDARD_METEREDDATA =
        new ExpressRouteCircuitSkuType(ExpressRouteCircuitSkuTier.STANDARD, ExpressRouteCircuitSkuFamily.METERED_DATA);
    /** Static value for Standard sku tier and UnlimitedData sku family. */
    public static final ExpressRouteCircuitSkuType STANDARD_UNLIMITEDDATA =
        new ExpressRouteCircuitSkuType(
            ExpressRouteCircuitSkuTier.STANDARD, ExpressRouteCircuitSkuFamily.UNLIMITED_DATA);
    /** Static value for Premium sku tier and MeteredData sku family. */
    public static final ExpressRouteCircuitSkuType PREMIUM_METEREDDATA =
        new ExpressRouteCircuitSkuType(ExpressRouteCircuitSkuTier.PREMIUM, ExpressRouteCircuitSkuFamily.METERED_DATA);
    /** Static value for Premium sku tier and UnlimitedData sku family. */
    public static final ExpressRouteCircuitSkuType PREMIUM_UNLIMITEDDATA =
        new ExpressRouteCircuitSkuType(ExpressRouteCircuitSkuTier.PREMIUM, ExpressRouteCircuitSkuFamily.UNLIMITED_DATA);

    /** the SKU corresponding to this type. */
    private final ExpressRouteCircuitSku sku;

    /** The string value of the SKU. */
    private final String value;

    /** @return predefined Express Route circuit SKU types */
    public static ExpressRouteCircuitSkuType[] values() {
        Collection<ExpressRouteCircuitSkuType> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new ExpressRouteCircuitSkuType[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for ExpressRouteCircuitSkuType.
     *
     * @param skuTier a SKU tier
     * @param skuFamily an SKU family
     */
    public ExpressRouteCircuitSkuType(ExpressRouteCircuitSkuTier skuTier, ExpressRouteCircuitSkuFamily skuFamily) {
        this(
            new ExpressRouteCircuitSku()
                .withName(
                    (skuTier == null ? "" : skuTier.toString()) + "_" + (skuFamily == null ? "" : skuFamily.toString()))
                .withTier(skuTier)
                .withFamily(skuFamily));
    }

    /**
     * Creates a custom value for ExpressRouteCircuitSkuType.
     *
     * @param sku the SKU
     */
    public ExpressRouteCircuitSkuType(ExpressRouteCircuitSku sku) {
        // Store Sku copy since original user provided sku can be modified
        // by the user.
        //
        this.sku = createCopy(sku);

        this.value = this.sku.name();
        VALUES_BY_NAME.put(this.value.toLowerCase(Locale.ROOT), this);
    }

    /**
     * Searches for an SKU type and creates a new Express Route circuit SKU type instance if not found among the
     * existing ones.
     *
     * @param sku an Express Route circuit SKU
     * @return the parsed or created Express Route circuit SKU type
     */
    public static ExpressRouteCircuitSkuType fromSku(ExpressRouteCircuitSku sku) {
        if (sku == null) {
            return null;
        }

        String nameToLookFor = sku.name();

        ExpressRouteCircuitSkuType result = VALUES_BY_NAME.get(nameToLookFor.toLowerCase(Locale.ROOT));
        if (result != null) {
            return result;
        } else {
            return new ExpressRouteCircuitSkuType(sku);
        }
    }

    /** @return the SKU */
    public ExpressRouteCircuitSku sku() {
        // Return copy of sku to guard ExpressRouteCircuitSkuType from ending up with invalid
        // sku in case consumer changes the returned Sku instance.
        //
        return createCopy(this.sku);
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExpressRouteCircuitSkuType)) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (value == null) {
            return ((ExpressRouteCircuitSkuType) obj).value == null;
        } else {
            return value.equalsIgnoreCase(((ExpressRouteCircuitSkuType) obj).value.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Creates a copy of the given sku.
     *
     * @param sku the sku to create copy of
     * @return the copy
     */
    private static ExpressRouteCircuitSku createCopy(ExpressRouteCircuitSku sku) {
        return new ExpressRouteCircuitSku().withName(sku.name()).withTier(sku.tier()).withFamily(sku.family());
    }
}
