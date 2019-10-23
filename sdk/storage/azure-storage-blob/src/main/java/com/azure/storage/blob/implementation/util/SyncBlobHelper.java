package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.specialized.BlobClientBase;

public class SyncBlobHelper {
    protected SyncBlobHelper() {
    }

    private static SyncBlobHelper.PropertyAccessor propertyAccessor;

    protected static SyncBlobHelper getHelper(BlobClientBase syncClient) {
        SyncBlobHelper helper = propertyAccessor.getHelper(syncClient);
        return helper;
    }

    public static void setNodeAccessor(final SyncBlobHelper.PropertyAccessor newAccessor) {
        if (propertyAccessor != null) {
            throw new IllegalStateException();
        }
        propertyAccessor = newAccessor;
    }

    public static AzureBlobStorageImpl getAzureBlobStorageImpl(BlobClientBase client) {
        return propertyAccessor.getAzureBlobStorageImpl(client);
    }

    public static CpkInfo getCustomerProvidedKey(BlobClientBase client) {
        return propertyAccessor.getCustomerProvidedKey(client);
    }

    public interface PropertyAccessor {
        SyncBlobHelper getHelper(BlobClientBase client);
        AzureBlobStorageImpl getAzureBlobStorageImpl(BlobClientBase client);
        CpkInfo getCustomerProvidedKey(BlobClientBase client);
    }
}
