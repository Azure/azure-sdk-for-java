/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.implementation;

import java.io.InputStream;

import com.microsoft.windowsazure.services.blob.BlobContract;
import com.microsoft.windowsazure.services.blob.implementation.BlobExceptionProcessor;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.media.WritableBlobContainerContract;
import com.sun.jersey.api.client.Client;

/**
 * Implementation of WritableBlobContainerContract, used to upload blobs to the
 * Media Services storage.
 * 
 */
public class MediaBlobContainerWriter implements WritableBlobContainerContract {

    private final BlobContract blobService;
    private final BlobContract restProxy;
    private final String containerName;

    /**
     * 
     */
    public MediaBlobContainerWriter(Client client, String accountName, String blobServiceUri, String containerName,
            String sasToken) {
        this.containerName = containerName;
        this.restProxy = new MediaBlobRestProxy(client, accountName, blobServiceUri, new SASTokenFilter(sasToken));
        this.blobService = new BlobExceptionProcessor(this.restProxy);
    }

    private MediaBlobContainerWriter(MediaBlobContainerWriter baseWriter, ServiceFilter filter) {
        this.containerName = baseWriter.containerName;
        this.restProxy = baseWriter.restProxy.withFilter(filter);
        this.blobService = new BlobExceptionProcessor(this.restProxy);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.FilterableService#withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public WritableBlobContainerContract withFilter(ServiceFilter filter) {
        return new MediaBlobContainerWriter(this, filter);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.WritableBlobContainerContract#createBlockBlob(java.lang.String, java.io.InputStream)
     */
    @Override
    public CreateBlobResult createBlockBlob(String blob, InputStream contentStream) throws ServiceException {
        return blobService.createBlockBlob(containerName, blob, contentStream);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.WritableBlobContainerContract#createBlockBlob(java.lang.String, java.io.InputStream, com.microsoft.windowsazure.services.blob.models.CreateBlobOptions)
     */
    @Override
    public CreateBlobResult createBlockBlob(String blob, InputStream contentStream, CreateBlobOptions options)
            throws ServiceException {
        return blobService.createBlockBlob(containerName, blob, contentStream, options);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.WritableBlobContainerContract#createBlobBlock(java.lang.String, java.lang.String, java.io.InputStream)
     */
    @Override
    public void createBlobBlock(String blob, String blockId, InputStream contentStream) throws ServiceException {
        blobService.createBlobBlock(containerName, blob, blockId, contentStream);

    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.WritableBlobContainerContract#createBlobBlock(java.lang.String, java.lang.String, java.io.InputStream, com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions)
     */
    @Override
    public void createBlobBlock(String blob, String blockId, InputStream contentStream, CreateBlobBlockOptions options)
            throws ServiceException {
        blobService.createBlobBlock(containerName, blob, blockId, contentStream, options);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.WritableBlobContainerContract#commitBlobBlocks(java.lang.String, com.microsoft.windowsazure.services.blob.models.BlockList)
     */
    @Override
    public void commitBlobBlocks(String blob, BlockList blockList) throws ServiceException {
        blobService.commitBlobBlocks(containerName, blob, blockList);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.WritableBlobContainerContract#commitBlobBlocks(java.lang.String, com.microsoft.windowsazure.services.blob.models.BlockList, com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions)
     */
    @Override
    public void commitBlobBlocks(String blob, BlockList blockList, CommitBlobBlocksOptions options)
            throws ServiceException {
        blobService.commitBlobBlocks(containerName, blob, blockList, options);
    }
}
