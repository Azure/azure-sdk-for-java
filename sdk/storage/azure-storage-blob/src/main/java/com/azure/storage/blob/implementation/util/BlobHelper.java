package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;

public class BlobHelper {

    protected BlobHelper() {
    }

    private static BlobHelper.PropertyAccessor propertyAccessor;

    protected static BlobHelper getHelper(BlobAsyncClientBase asyncClient) {
        BlobHelper helper = propertyAccessor.getHelper(asyncClient);
        return helper;
    }

    /**
     * Sets the property accessor
     *
     * @param newAccessor
     */
    public static void setPropertyAccessor(final BlobHelper.PropertyAccessor newAccessor) {
        if (newAccessor == null) {
            throw new IllegalStateException();
        }
        propertyAccessor = newAccessor;
    }

    /**
     * Gets azureBlobStorage field from the client.
     *
     * @param client the client with private field to retrieve.
     * @return the azureBlobStorage property.
     */
    public static AzureBlobStorageImpl getAzureBlobStorageImpl(BlobAsyncClientBase client) {
        return propertyAccessor.getAzureBlobStorageImpl(client);
    }

    /**
     * Gets serviceVersion field from the client.
     *
     * @param client the client with private field to retrieve.
     * @return the serviceVersion property.
     */
    public static BlobServiceVersion getServiceVersion(BlobAsyncClientBase client) {
        return BlobServiceVersion.getBlobServiceVersion(getAzureBlobStorageImpl(client).getVersion());
    }

    /**
     * Gets customerProvidedKey field from the client.
     *
     * @param client the client with private field to retrieve.
     * @return the customerProvidedKey property.
     */
    public static CpkInfo getCustomerProvidedKey(BlobAsyncClientBase client) {
        return propertyAccessor.getCustomerProvidedKey(client);
    }

    /**
     * The interface to get property value from Property accessor.
     */
    public interface PropertyAccessor {
        BlobHelper getHelper(BlobAsyncClientBase client);
        AzureBlobStorageImpl getAzureBlobStorageImpl(BlobAsyncClientBase client);
        CpkInfo getCustomerProvidedKey(BlobAsyncClientBase client);
    }
}
