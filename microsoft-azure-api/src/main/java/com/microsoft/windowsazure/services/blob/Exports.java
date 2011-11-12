package com.microsoft.windowsazure.services.blob;

import com.microsoft.windowsazure.configuration.builder.Builder;
import com.microsoft.windowsazure.services.blob.implementation.BlobServiceForJersey;
import com.microsoft.windowsazure.services.blob.implementation.BlobServiceImpl;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {
        registry.add(BlobServiceContract.class, BlobServiceImpl.class);
        registry.add(BlobServiceForJersey.class);
        registry.add(SharedKeyLiteFilter.class);
    }
}
