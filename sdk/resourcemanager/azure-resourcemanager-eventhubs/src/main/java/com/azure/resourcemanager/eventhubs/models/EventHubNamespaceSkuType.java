// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.models;

import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/**
 * Defines values for EventHubNamespaceSkuType.
 */
public class EventHubNamespaceSkuType implements HasInnerModel<Sku> {
    /** Static value NamespaceSku for BASIC. */
    public static final EventHubNamespaceSkuType BASIC =
        new EventHubNamespaceSkuType(new Sku().withName(SkuName.BASIC).withTier(SkuTier.BASIC));
    /** Static value NamespaceSku for STANDARD. */
    public static final EventHubNamespaceSkuType STANDARD =
        new EventHubNamespaceSkuType(new Sku().withName(SkuName.STANDARD).withTier(SkuTier.STANDARD));

    private final Sku sku;

    /**
     * Creates event hub namespace sku.
     *
     * @param sku inner sku model instance
     */
    public EventHubNamespaceSkuType(Sku sku) {
        this.sku = sku;
    }

    /**
     * Creates event hub namespace sku.
     *
     * @param name sku name
     * @param tier sku tier
     */
    public EventHubNamespaceSkuType(SkuName name, SkuTier tier) {
        this(new Sku().withName(name).withTier(tier).withCapacity(null));
    }

    @Override
    public Sku innerModel() {
        return this.sku;
    }

    /**
     * @return sku tier
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
        return String.format("%s_%s", this.sku.name(), this.sku.tier());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EventHubNamespaceSkuType)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            EventHubNamespaceSkuType rhs = (EventHubNamespaceSkuType) obj;
            if (sku == null) {
                return rhs.sku == null;
            } else {
                return this.toString().equalsIgnoreCase(rhs.toString());
            }
        }
    }
}
