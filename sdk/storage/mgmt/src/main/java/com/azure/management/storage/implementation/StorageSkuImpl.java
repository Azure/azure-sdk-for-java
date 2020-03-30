/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.storage.Kind;
import com.azure.management.storage.Restriction;
import com.azure.management.storage.SKUCapability;
import com.azure.management.storage.SkuName;
import com.azure.management.storage.SkuTier;
import com.azure.management.storage.StorageAccountSkuType;
import com.azure.management.storage.StorageResourceType;
import com.azure.management.storage.StorageSku;
import com.azure.management.storage.models.SkuInformationInner;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for {@link StorageSku}.
 */
class StorageSkuImpl implements StorageSku {
    private final SkuInformationInner inner;

    StorageSkuImpl(SkuInformationInner skuInner) {
        this.inner = skuInner;
    }

    @Override
    public SkuName name() {
        return this.inner.getName();
    }

    @Override
    public SkuTier tier() {
        return this.inner.getTier();
    }

    @Override
    public StorageResourceType resourceType() {
        if (this.inner.getResourceType() != null) {
            return StorageResourceType.fromString(this.inner.getResourceType());
        } else {
            return null;
        }
    }

    @Override
    public List<Region> regions() {
        List<Region> regions = new ArrayList<>();
        if (this.inner.getLocations() != null) {
            for (String location : this.inner.getLocations()) {
                regions.add(Region.fromName(location));
            }
        }
        return regions;
    }

    @Override
    public List<SKUCapability> capabilities() {
        if (this.inner.getCapabilities() != null) {
            return this.inner.getCapabilities();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Restriction> restrictions() {
        if (this.inner.getRestrictions() != null) {
            return this.inner.getRestrictions();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Kind storageAccountKind() {
        return this.inner.getKind();
    }

    @Override
    public StorageAccountSkuType storageAccountSku() {
        if (this.resourceType() != null && this.resourceType().equals(StorageResourceType.STORAGE_ACCOUNTS)) {
            return StorageAccountSkuType.fromSkuName(this.inner.getName());
        }
        return null;
    }

    @Override
    public SkuInformationInner inner() {
        return this.inner;
    }
}