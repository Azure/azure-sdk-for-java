// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.annotation.ServiceClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;

/**
 * This class provides a client that contains generic blob operations for Azure Storage Blobs. Operations allowed by
 * the client are downloading and copying a blob, retrieving and setting metadata, retrieving and setting HTTP headers,
 * and deleting and un-deleting a blob.
 *
 * <p>
 * This client is instantiated through {@link BlobClientBuilder} or retrieved via
 * {@link BlobContainerClient#getBlobClient(String) getBlobClient}.
 *
 * <p>
 * For operations on a specific blob type (i.e append, block, or page) use
 * {@link #getAppendBlobClient() asAppendBlobClient}, {@link #getBlockBlobClient() asBlockBlobClient}, or
 * {@link #getPageBlobClient() asPageBlobClient} to construct a client that allows blob specific operations.
 *
 * <p>
 * Please refer to the <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/understanding-block-blobs--append-blobs--and-page-blobs>Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = BlobClientBuilder.class)
public class BlobClient extends BlobClientBase {
    private final BlobAsyncClient client;

    /**
     * Package-private constructor for use by {@link BlobClientBuilder}.
     *
     * @param client the async blob client
     */
    BlobClient(BlobAsyncClient client) {
        super(client);
        this.client = client;
    }

    /**
     * Creates a new {@link BlobClient} linked to the {@code snapshot} of this blob resource.
     *
     * @param snapshot the identifier for a specific snapshot of this blob
     * @return a {@link BlobClient} used to interact with the specific snapshot.
     */
    @Override
    public BlobClient getSnapshotClient(String snapshot) {
        return new BlobClient(client.getSnapshotClient(snapshot));
    }

    /**
     * Creates a new {@link AppendBlobClient} associated to this blob.
     *
     * @return a {@link AppendBlobClient} associated to this blob.
     */
    public AppendBlobClient getAppendBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildAppendBlobClient();
    }

    /**
     * Creates a new {@link BlockBlobClient} associated to this blob.
     *
     * @return a {@link BlockBlobClient} associated to this blob.
     */
    public BlockBlobClient getBlockBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildBlockBlobClient();
    }

    /**
     * Creates a new {@link PageBlobClient} associated to this blob.
     *
     * @return a {@link PageBlobClient} associated to this blob.
     */
    public PageBlobClient getPageBlobClient() {
        return new SpecializedBlobClientBuilder()
            .blobClient(this)
            .buildPageBlobClient();
    }
}
