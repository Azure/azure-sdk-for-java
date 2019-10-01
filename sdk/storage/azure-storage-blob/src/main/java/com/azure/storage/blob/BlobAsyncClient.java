// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.implementation.AzureBlobStorageBuilder;
import com.azure.storage.blob.implementation.AzureBlobStorageImpl;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are downloading and copying a blob, retrieving and setting metadata, retrieving and setting HTTP headers,
 * and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via
 * {@link BlobContainerAsyncClient#getBlobAsyncClient(String) getBlobClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobAsyncClient() getAppendBlobAsyncClient}, {@link #getBlockBlobAsyncClient()
 * getBlockBlobAsyncClient}, or {@link #getPageBlobAsyncClient() getPageBlobAsyncClient} to construct a client that
 * allows blob specific operations.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
public class BlobAsyncClient extends BlobAsyncClientBase {
    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     *
     * @param azureBlobStorage the API client for blob storage
     */
    BlobAsyncClient(AzureBlobStorageImpl azureBlobStorage, String snapshot, CpkInfo cpk) {
        super(azureBlobStorage, snapshot, cpk);
    }

    /**
     * Creates a new {@link BlobAsyncClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobAsyncClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobAsyncClient getSnapshotClient(String snapshot) {
        return new BlobAsyncClient(new AzureBlobStorageBuilder()
            .url(getBlobUrl().toString())
            .pipeline(azureBlobStorage.getHttpPipeline())
            .build(), getSnapshotId(), getCustomerProvidedKey());
    }

    /**
     * Creates a new {@link AppendBlobAsyncClient} associated to this blob.
     *
     * @return a {@link AppendBlobAsyncClient} associated to this blob.
     */
    public AppendBlobAsyncClient getAppendBlobAsyncClient() {
        return prepareBuilder().buildAppendBlobAsyncClient();
    }

    /**
     * Creates a new {@link BlockBlobAsyncClient} associated to this blob.
     *
     * @return a {@link BlockBlobAsyncClient} associated to this blob.
     */
    public BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return prepareBuilder().buildBlockBlobAsyncClient();
    }

    /**
     * Creates a new {@link PageBlobAsyncClient} associated to this blob.
     *
     * @return a {@link PageBlobAsyncClient} associated to this blob.
     */
    public PageBlobAsyncClient getPageBlobAsyncClient() {
        return prepareBuilder().buildPageBlobAsyncClient();
    }

    private SpecializedBlobClientBuilder prepareBuilder() {
        SpecializedBlobClientBuilder builder = new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(getBlobUrl().toString())
            .snapshot(getSnapshotId());

        CpkInfo cpk = getCustomerProvidedKey();
        if (cpk != null) {
            builder.customerProvidedKey(new CustomerProvidedKey(cpk.getEncryptionKey()));
        }

        return builder;
    }
}
