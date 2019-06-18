package com.azure.storage.blob;

import com.azure.storage.blob.models.*;

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
