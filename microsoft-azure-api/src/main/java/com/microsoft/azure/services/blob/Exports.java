package com.microsoft.azure.services.blob;

import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.services.blob.implementation.BlobServiceForJersey;
import com.microsoft.azure.services.blob.implementation.BlobServiceImpl;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(BlobService.class, BlobServiceImpl.class);
        registry.add(BlobServiceForJersey.class);
        registry.add(BlobSharedKeyLiteFilter.class);
    }
}
