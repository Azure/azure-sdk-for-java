// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CpkInfo;

import java.util.Objects;

/**
 * The blob container helper takes in the blob container sync and async client and
 * has helper methods on getting the properties of the client.
 */
public final class BlobContainerHelper {

    private BlobContainerHelper() {
    }

    private static AsyncPropertyAccessor asyncPropertyAccessor;

    /**
     * Sets the async property accessor
     *
     * @param newAccessor the accessor contains property value.
     */
    public static void setAsyncPropertyAccessor(final AsyncPropertyAccessor newAccessor) {
        Objects.requireNonNull(newAccessor);
        asyncPropertyAccessor = newAccessor;
    }

    /**
     * Gets azureBlobStorage field from the async client.
     *
     * @param client the client with private field to retrieve.
     * @return the azureBlobStorage property.
     */
    public static AzureBlobStorageImpl getAzureBlobStorageImpl(BlobContainerAsyncClient client) {
        return asyncPropertyAccessor.getAzureBlobStorageImpl(client);
    }

    /**
     * Gets serviceVersion field from the async client.
     *
     * @param client the client with private field to retrieve.
     * @return the serviceVersion property.
     */
    public static BlobServiceVersion getServiceVersion(BlobContainerAsyncClient client) {
        return asyncPropertyAccessor.getServiceVersion(client);
    }

    /**
     * Gets customerProvidedKey field from the async client.
     *
     * @param client the client with private field to retrieve.
     * @return the customerProvidedKey property.
     */
    public static CpkInfo getCustomerProvidedKey(BlobContainerAsyncClient client) {
        return asyncPropertyAccessor.getCustomerProvidedKey(client);
    }

    /**
     * The interface to get property value from async property accessor.
     */
    public interface AsyncPropertyAccessor {
        AzureBlobStorageImpl getAzureBlobStorageImpl(BlobContainerAsyncClient client);
        CpkInfo getCustomerProvidedKey(BlobContainerAsyncClient client);
        BlobServiceVersion getServiceVersion(BlobContainerAsyncClient client);
    }

    private static SyncPropertyAccessor syncPropertyAccessor;

    /**
     * Sets the sync property accessor
     *
     * @param newAccessor
     */
    public static void setSyncPropertyAccessor(final SyncPropertyAccessor newAccessor) {
        Objects.requireNonNull(newAccessor);
        syncPropertyAccessor = newAccessor;
    }

    /**
     * Gets azureBlobStorage field from the sync client.
     *
     * @return the azureBlobStorage property.
     */
    public static AzureBlobStorageImpl getAzureBlobStorageImpl() {
        return syncPropertyAccessor.getAzureBlobStorageImpl();
    }

    /**
     * Gets serviceVersion field from the sync client.
     *
     * @return the serviceVersion property.
     */
    public static BlobServiceVersion getServiceVersion() {
        return syncPropertyAccessor.getServiceVersion();
    }

    /**
     * Gets customerProvidedKey field from the sync client.
     *
     * @return the customerProvidedKey property.
     */
    public static CpkInfo getCustomerProvidedKey() {
        return syncPropertyAccessor.getCustomerProvidedKey();
    }

    /**
     * The interface to get property value from sync property accessor.
     */
    public interface SyncPropertyAccessor {
        AzureBlobStorageImpl getAzureBlobStorageImpl();
        CpkInfo getCustomerProvidedKey();
        BlobServiceVersion getServiceVersion();
    }
}
