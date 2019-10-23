package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;

public class AsyncBlobHelper {

    protected AsyncBlobHelper() {
    }

    private static AsyncBlobHelper.PropertyAccessor propertyAccessor;

    protected static AsyncBlobHelper getHelper(BlobAsyncClientBase asyncClient) {
        AsyncBlobHelper helper = propertyAccessor.getHelper(asyncClient);
        return helper;
    }

    public static void setNodeAccessor(final AsyncBlobHelper.PropertyAccessor newAccessor) {
        if (propertyAccessor != null) {
            throw new IllegalStateException();
        }
        propertyAccessor = newAccessor;
    }

    public static AzureBlobStorageImpl getAzureBlobStorageImpl(BlobAsyncClientBase client) {
        return propertyAccessor.getAzureBlobStorageImpl(client);
    }

    public static BlobServiceVersion getServiceVersion(BlobAsyncClientBase client) {
        return BlobServiceVersion.valueOf(getAzureBlobStorageImpl(client).getVersion());
    }

    public static CpkInfo getCustomerProvidedKey(BlobAsyncClientBase client) {
        return propertyAccessor.getCustomerProvidedKey(client);
    }

    public interface PropertyAccessor {
        AsyncBlobHelper getHelper(BlobAsyncClientBase client);
        AzureBlobStorageImpl getAzureBlobStorageImpl(BlobAsyncClientBase client);
        CpkInfo getCustomerProvidedKey(BlobAsyncClientBase client);
    }
}
