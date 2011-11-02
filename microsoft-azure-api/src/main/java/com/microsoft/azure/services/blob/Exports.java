package com.microsoft.azure.services.blob;

import com.microsoft.azure.configuration.builder.Builder;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(BlobContract.class, BlobContractImpl.class);
        registry.add(BlobSharedKeyLiteFilter.class);
    }
}
