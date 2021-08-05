// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/**
 * Defines values for NamespaceSku.
 */
public final class NamespaceSku {
    /** Static value NamespaceSku for BASIC. */
    public static final NamespaceSku BASIC = new NamespaceSku(
        new SBSku().withName(SkuName.BASIC).withTier(SkuTier.BASIC));
    /** Static value NamespaceSku for STANDARD. */
    public static final NamespaceSku STANDARD = new NamespaceSku(
        new SBSku().withName(SkuName.STANDARD).withTier(SkuTier.STANDARD));
    /** Static value NamespaceSku for PREMIUM_CAPACITY1. */
    public static final NamespaceSku PREMIUM_CAPACITY1 = new NamespaceSku(
        new SBSku().withCapacity(1).withName(SkuName.PREMIUM).withTier(SkuTier.PREMIUM));
    /** Static value NamespaceSku for PREMIUM_CAPACITY2. */
    public static final NamespaceSku PREMIUM_CAPACITY2 = new NamespaceSku(
        new SBSku().withCapacity(2).withName(SkuName.PREMIUM).withTier(SkuTier.PREMIUM));
    /** Static value NamespaceSku for PREMIUM_CAPACITY4. */
    public static final NamespaceSku PREMIUM_CAPACITY4 = new NamespaceSku(
        new SBSku().withCapacity(4).withName(SkuName.PREMIUM).withTier(SkuTier.PREMIUM));

    private final SBSku sku;

    /**
     * Creates Service Bus namespace sku.
     *
     * @param name sku name
     * @param tier sku tier
     */
    public NamespaceSku(String name, String tier) {
        this(new SBSku()
                .withCapacity(null)
                .withName(SkuName.fromString(name))
                .withTier(SkuTier.fromString(tier)));
    }

    /**
     * Creates Service Bus namespace SKU.
     *
     * @param name sku name
     * @param tier sku tier
     * @param capacity factor of resources allocated to host Service Bus
     */
    public NamespaceSku(String name, String tier, int capacity) {
        this(new SBSku()
                .withCapacity(capacity)
                .withName(SkuName.fromString(name))
                .withTier(SkuTier.fromString(tier)));
    }

    /**
     * Creates Service Bus namespace SKU.
     *
     * @param sku inner sku model instance
     */
    public NamespaceSku(SBSku sku) {
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
        return ResourceManagerUtils.toPrimitiveInt(this.sku.capacity());
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
