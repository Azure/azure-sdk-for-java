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
    /** @return the sku name */
    SkuName name();
    /** @return the sku tier */
    SkuTier tier();
    /** @return the storage resource type that the sku describes */
    StorageResourceType resourceType();
    /** @return the regions that the sku is available */
    List<Region> regions();
    /** @return the capability information in the specified sku */
    List<SkuCapability> capabilities();
    /** @return restrictions because of which sku cannot be used */
    List<Restriction> restrictions();
    /** @return the storage account kind if the sku describes a storage account resource */
    Kind storageAccountKind();
    /** @return the storage account sku type if the sku describes a storage account resource */
    StorageAccountSkuType storageAccountSku();
}
