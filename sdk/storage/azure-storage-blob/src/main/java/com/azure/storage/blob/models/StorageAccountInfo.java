// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.models;

public class StorageAccountInfo {
    private final SkuName skuName;
    private final AccountKind accountKind;

    public StorageAccountInfo(BlobGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.skuName();
        this.accountKind = generatedResponseHeaders.accountKind();
    }

    public StorageAccountInfo(ContainerGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.skuName();
        this.accountKind = generatedResponseHeaders.accountKind();
    }

    public StorageAccountInfo(ServiceGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.skuName();
        this.accountKind = generatedResponseHeaders.accountKind();
    }

    /**
     * @return the SKU of the account
     */
    public SkuName skuName() {
        return skuName;
    }

    /**
     * @return the type of the account
     */
    public AccountKind accountKind() {
        return accountKind;
    }
}
