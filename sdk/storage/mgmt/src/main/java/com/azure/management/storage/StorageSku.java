/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.models.SkuInformationInner;

import java.util.List;

/**
 * Type representing sku for an Azure storage resource.
 */
@Fluent
public interface StorageSku extends HasInner<SkuInformationInner> {
    /**
     * @return the sku name
     */
    SkuName name();
    /**
     * @return the sku tier
     */
    SkuTier tier();
    /**
     * @return the storage resource type that the sku describes
     */
    StorageResourceType resourceType();
    /**
     * @return the regions that the sku is available
     */
    List<Region> regions();
    /**
     * @return the capability information in the specified sku
     */
    List<SKUCapability> capabilities();
    /**
     * @return restrictions because of which sku cannot be used
     */
    List<Restriction> restrictions();
    /**
     * @return the storage account kind if the sku describes a storage account resource
     */
    Kind storageAccountKind();
    /**
     * @return the storage account sku type if the sku describes a storage account resource
     */
    StorageAccountSkuType storageAccountSku();
}