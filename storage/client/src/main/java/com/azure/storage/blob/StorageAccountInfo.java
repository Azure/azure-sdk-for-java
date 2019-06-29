// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.models.AccountKind;
import com.azure.storage.blob.models.BlobGetAccountInfoHeaders;
import com.azure.storage.blob.models.ContainerGetAccountInfoHeaders;
import com.azure.storage.blob.models.ServiceGetAccountInfoHeaders;
import com.azure.storage.blob.models.SkuName;

public class StorageAccountInfo {

    private SkuName skuName;

    private AccountKind accountKind;


    StorageAccountInfo(BlobGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.skuName();
        this.accountKind = generatedResponseHeaders.accountKind();
    }

    StorageAccountInfo(ContainerGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.skuName();
        this.accountKind = generatedResponseHeaders.accountKind();
    }

    StorageAccountInfo(ServiceGetAccountInfoHeaders generatedResponseHeaders) {
        this.skuName = generatedResponseHeaders.skuName();
        this.accountKind = generatedResponseHeaders.accountKind();
    }


    public SkuName skuName() {
        return skuName;
    }

    public AccountKind accountKind() {
        return accountKind;
    }
}
