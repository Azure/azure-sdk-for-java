// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.storage.file.models.FileServiceProperties;
import com.azure.storage.file.models.ListSharesOptions;
import com.azure.storage.file.models.ShareItem;

import java.util.Map;

/**
 * File service client
 */
public final class FileServiceClient {
    private final FileServiceAsyncClient client;

    FileServiceClient(FileServiceAsyncClient client) {
        this.client = client;
    }

    /**
     * @return a new client builder instance
     */
    public static FileServiceClientBuilder builder() {
        return new FileServiceClientBuilder();
    }

    /**
     * @return the URL of the file service
     */
    public String url() {
        return client.url();
    }

    /**
     * Gets a ShareClient that is able to interact with the specified share.
     * @param shareName Name of the share
     * @return a ShareClient that interacts with the specified share
     */
    public ShareClient getShareClient(String shareName) {
        return new ShareClient(client.getShareAsyncClient(shareName));
    }

    /**
     * Lists the shares in the Storage account
     *
     * @return the shares in the Storage account
     */
    public Iterable<ShareItem> listShares() {
        return listShares(null);
    }

    /**
     * Lists the shares in the Storage account that pass the options filter
     *
     * @param options Options used to filter which shares are listed
     * @return the shares in the Storage account that passed the filter
     */
    public Iterable<ShareItem> listShares(ListSharesOptions options) {
        return client.listShares(options).toIterable();
    }

    /**
     * @return the global file properties in the storage account
     */
    public Response<FileServiceProperties> getProperties() {
        return client.getProperties().block();
    }

    /**
     * Sets the global file properties for the storage account
     * @param properties File properties
     * @return an empty response
     */
    public VoidResponse setProperties(FileServiceProperties properties) {
        return client.setProperties(properties).block();
    }

    /**
     * Creates a new share and returns a client to interact with it
     * @param shareName Name of the share
     * @return a ShareClient that interacts with the share that was created
     */
    public Response<ShareClient> createShare(String shareName) {
        return createShare(shareName, null, null);
    }

    /**
     * Creates a new share and returns a client to interact with it
     * @param shareName Name of the share
     * @param metadata Metadata for the new share
     * @param quotaInGB Maximum size the share is allowed to grow to in GB
     * @return a ShareClient that interacts with the share that was created
     */
    public Response<ShareClient> createShare(String shareName, Map<String, String> metadata, Integer quotaInGB) {
        Response<ShareAsyncClient> response = client.createShare(shareName, metadata, quotaInGB).block();
        return FileServiceAsyncClient.mapToResponse(response, new ShareClient(response.value()));
    }

    /**
     * Deletes a share and all of its snapshots
     * @param shareName Name of the share
     * @return an empty response
     */
    public VoidResponse deleteShare(String shareName) {
        return deleteShare(shareName, null);
    }

    /**
     * Deletes a specific snapshot of a share
     * @param shareName Name of the share
     * @param shareSnapshot Identifier of the snapshot
     * @return an empty response
     */
    public VoidResponse deleteShare(String shareName, String shareSnapshot) {
        return client.deleteShare(shareName, shareSnapshot).block();
    }
}
