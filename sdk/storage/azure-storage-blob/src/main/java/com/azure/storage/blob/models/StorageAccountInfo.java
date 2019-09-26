// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.models;

import com.azure.storage.blob.implementation.BlobGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.ContainerGetAccountInfoHeaders;
import com.azure.storage.blob.implementation.ServiceGetAccountInfoHeaders;

public class StorageAccountInfo {
    private final SkuName skuName;
    private final AccountKind accountKind;

    public StorageAccountInfo(BlobGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.getSkuName();
        this.accountKind = generatedResponseHeaders.getAccountKind();
    }

    public StorageAccountInfo(ContainerGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.getSkuName();
        this.accountKind = generatedResponseHeaders.getAccountKind();
    }

    public StorageAccountInfo(ServiceGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.getSkuName();
        this.accountKind = generatedResponseHeaders.getAccountKind();
    }

    /**
     * @return the SKU of the account
     */
    public SkuName getSkuName() {
        return skuName;
    }

    /**
     * @return the type of the account
     */
    public AccountKind getAccountKind() {
        return accountKind;
    }
}
