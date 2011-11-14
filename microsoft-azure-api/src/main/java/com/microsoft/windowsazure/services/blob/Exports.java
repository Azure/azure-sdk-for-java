package com.microsoft.windowsazure.services.blob;

import com.microsoft.windowsazure.configuration.builder.Builder;
import com.microsoft.windowsazure.services.blob.implementation.BlobRestProxy;
import com.microsoft.windowsazure.services.blob.implementation.BlobExceptionProcessor;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(BlobContract.class, BlobExceptionProcessor.class);
        registry.add(BlobRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
    }
}
