// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Defines values for LoadBalancerSkuType. */
public final class LoadBalancerSkuType {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, LoadBalancerSkuType> VALUES_BY_NAME = new HashMap<>();

    /** Static value Basic for LoadBalancerSkuType. */
    public static final LoadBalancerSkuType BASIC = new LoadBalancerSkuType(LoadBalancerSkuName.BASIC);

    /** Static value Standard for LoadBalancerSkuType. */
    public static final LoadBalancerSkuType STANDARD = new LoadBalancerSkuType(LoadBalancerSkuName.STANDARD);

    /** The actual serialized value for a LoadBalancerSkuType instance. */
    private final LoadBalancerSkuName skuName;

    /** @return predefined LoadBalancer SKU types */
    public static LoadBalancerSkuType[] values() {
        Collection<LoadBalancerSkuType> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new LoadBalancerSkuType[valuesCollection.size()]);
    }

    /**
     * Creates a LoadBalancerSkuType from sku name.
     *
     * @param skuName the sku name
     */
    private LoadBalancerSkuType(LoadBalancerSkuName skuName) {
        this.skuName = skuName;
        if (skuName != null) {
            VALUES_BY_NAME.put(skuName.toString().toLowerCase(Locale.ROOT), this);
        }
    }

    /**
     * Parses a value into a SKU type and creates a new LoadBalancerSkuType instance if not found among the existing
     * ones.
     *
     * @param lbSku a sku
     * @return the LoadBalancerSkuType
     */
    public static LoadBalancerSkuType fromSku(LoadBalancerSku lbSku) {
        if (lbSku == null) {
            return null;
        }
        if (lbSku.name() == null) {
            return null;
        }
        LoadBalancerSkuType result = VALUES_BY_NAME.get(lbSku.name().toString().toLowerCase(Locale.ROOT));
        if (result != null) {
            return result;
        } else {
            return new LoadBalancerSkuType(lbSku.name());
        }
    }

    /** @return the LoadBalancerSku associated with the LoadBalancerSkuType. */
    public LoadBalancerSku sku() {
        return (new LoadBalancerSku()).withName(this.skuName);
    }

    @Override
    public int hashCode() {
        return skuName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LoadBalancerSkuType)) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (skuName == null) {
            return ((LoadBalancerSkuType) obj).skuName == null;
        } else {
            return skuName.equals(((LoadBalancerSkuType) obj).skuName);
        }
    }
}
