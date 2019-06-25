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


    public SkuName skuName() {
        return skuName;
    }

    public AccountKind accountKind() {
        return accountKind;
    }
}
