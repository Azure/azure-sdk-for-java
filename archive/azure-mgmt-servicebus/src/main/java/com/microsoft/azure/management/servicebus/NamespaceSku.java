/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 * Defines values for NamespaceSku.
 */
@LangDefinition
public final class NamespaceSku {
    /** Static value NamespaceSku for BASIC. */
    public static final NamespaceSku BASIC = new NamespaceSku(new Sku().withName(SkuName.BASIC).withTier(SkuTier.BASIC));
    /** Static value NamespaceSku for STANDARD. */
    public static final NamespaceSku STANDARD = new NamespaceSku(new Sku().withName(SkuName.STANDARD).withTier(SkuTier.STANDARD));
    /** Static value NamespaceSku for PREMIUM_CAPACITY1. */
    public static final NamespaceSku PREMIUM_CAPACITY1 = new NamespaceSku(new Sku().withCapacity(1).withName(SkuName.PREMIUM).withTier(SkuTier.PREMIUM));
    /** Static value NamespaceSku for PREMIUM_CAPACITY2. */
    public static final NamespaceSku PREMIUM_CAPACITY2 = new NamespaceSku(new Sku().withCapacity(2).withName(SkuName.PREMIUM).withTier(SkuTier.PREMIUM));
    /** Static value NamespaceSku for PREMIUM_CAPACITY4. */
    public static final NamespaceSku PREMIUM_CAPACITY4 = new NamespaceSku(new Sku().withCapacity(4).withName(SkuName.PREMIUM).withTier(SkuTier.PREMIUM));

    private final Sku sku;

    /**
     * Creates Service Bus namespace sku.
     *
     * @param name sku name
     * @param tier sku tier
     */
    public NamespaceSku(String name, String tier) {
        this(new Sku()
                .withCapacity(null)
                .withName(new SkuName(name))
                .withTier(new SkuTier(tier)));
    }

    /**
     * Creates Service Bus namespace SKU.
     *
     * @param name sku name
     * @param tier sku tier
     * @param capacity factor of resources allocated to host Service Bus
     */
    public NamespaceSku(String name, String tier, int capacity) {
        this(new Sku()
                .withCapacity(capacity)
                .withName(new SkuName(name))
                .withTier(new SkuTier(tier)));
    }

    /**
     * Creates Service Bus namespace SKU.
     *
     * @param sku inner sku model instance
     */
    public NamespaceSku(Sku sku) {
        this.sku = sku;
    }

    /**
     * @return sku name
     */
    public SkuName name() {
        return this.sku.name();
    }

    /**
     * @return sku tier
     */
    public SkuTier tier() {
        return this.sku.tier();
    }

    /**
     * @return sku capacity
     */
    public int capacity() {
        return Utils.toPrimitiveInt(this.sku.capacity());
    }

    @Override
    public String toString() {
        if (this.sku.capacity() != null) {
            return String.format("%s_%s_%d", this.sku.name(), this.sku.tier(), this.sku.capacity());
        } else {
            return String.format("%s_%s", this.sku.name(), this.sku.tier());
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamespaceSku)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        NamespaceSku rhs = (NamespaceSku) obj;
        if (sku == null) {
            return rhs.sku == null;
        } else {
            return this.toString().equalsIgnoreCase(rhs.toString());
        }
    }
}