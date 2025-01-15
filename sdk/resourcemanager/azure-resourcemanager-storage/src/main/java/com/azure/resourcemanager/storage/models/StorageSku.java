// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.storage.fluent.models.SkuInformationInner;
import java.util.List;

/** Type representing sku for an Azure storage resource. */
@Fluent
public interface StorageSku extends HasInnerModel<SkuInformationInner> {
    /**
     * Gets the sku name.
     *
     * @return the sku name
     */
    SkuName name();

    /**
     * Gets the sku tier.
     *
     * @return the sku tier
     */
    SkuTier tier();

    /**
     * Gets the storage resource type that the sku describes.
     *
     * @return the storage resource type that the sku describes
     */
    StorageResourceType resourceType();

    /**
     * Gets the regions that the sku is available.
     *
     * @return the regions that the sku is available
     */
    List<Region> regions();

    /**
     * Gets the capability information in the specified sku.
     *
     * @return the capability information in the specified sku
     */
    List<SkuCapability> capabilities();

    /**
     * Gets restrictions because of which sku cannot be used.
     *
     * @return restrictions because of which sku cannot be used
     */
    List<Restriction> restrictions();

    /**
     * Gets the storage account kind if the sku describes a storage account resource.
     *
     * @return the storage account kind if the sku describes a storage account resource
     */
    Kind storageAccountKind();

    /**
     * Gets the storage account sku type.
     *
     * @return the storage account sku type if the sku describes a storage account resource
     */
    StorageAccountSkuType storageAccountSku();
}
