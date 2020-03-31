/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network;


import com.azure.core.implementation.annotation.Beta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines values for PublicIPSkuType.
 */
public final class PublicIPSkuType {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, PublicIPSkuType> VALUES_BY_NAME = new HashMap<>();

    /** Static value Basic for PublicIPSkuType. */
    public static final PublicIPSkuType BASIC = new PublicIPSkuType(PublicIPAddressSkuName.BASIC);

    /** Static value Standard for PublicIPSkuType. */
    public static final PublicIPSkuType STANDARD = new PublicIPSkuType(PublicIPAddressSkuName.STANDARD);

    /** The actual serialized value for a PublicIPSkuType instance. */
    private PublicIPAddressSkuName skuName;

    /**
     * @return predefined publicIP SKU types
     */
    public static PublicIPSkuType[] values() {
        Collection<PublicIPSkuType> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new PublicIPSkuType[valuesCollection.size()]);
    }

    /**
     * Creates a PublicIPSkuType from sku name.
     * @param skuName the sku name
     */
    private PublicIPSkuType(PublicIPAddressSkuName skuName) {
        this.skuName = skuName;
        if (skuName != null) {
            VALUES_BY_NAME.put(skuName.toString().toLowerCase(), this);
        }
    }

    /**
     * Parses a value into a SKU type and creates a new PublicIPSkuType instance if not
     * found among the existing ones.
     *
     * @param pipSku a sku
     * @return the PublicIPSkuType
     */
    public static PublicIPSkuType fromSku(PublicIPAddressSku pipSku) {
        if (pipSku == null) {
            return null;
        }
        if (pipSku.name() == null) {
            return null;
        }
        PublicIPSkuType result = VALUES_BY_NAME.get(pipSku.name().toString().toLowerCase());
        if (result != null) {
            return result;
        } else {
            return new PublicIPSkuType(pipSku.name());
        }
    }

    /**
     * @return the PublicIPAddressSku associated with the PublicIPAddressSkuType.
     */
    public PublicIPAddressSku sku() {
        return (new PublicIPAddressSku()).withName(this.skuName);
    }

    @Override
    public int hashCode() {
        return skuName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PublicIPSkuType)) {
            return false;
        } else if (obj == this) {
            return true;
        } else  if (skuName == null) {
            return ((PublicIPSkuType) obj).skuName == null;
        } else {
            return skuName.equals(((PublicIPSkuType) obj).skuName);
        }
    }
}
