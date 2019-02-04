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

package com.microsoft.windowsazure.services.media;

import java.io.InputStream;

import com.microsoft.windowsazure.core.pipeline.jersey.JerseyFilterableService;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;

/**
 * The Interface WritableBlobContainerContract.
 */
public interface WritableBlobContainerContract extends
        JerseyFilterableService<WritableBlobContainerContract> {

    /**
     * Creates the block blob.
     * 
     * @param blob
     *            the blob
     * @param contentStream
     *            the content stream
     * @return the creates the blob result
     * @throws ServiceException
     *             the service exception
     */
    CreateBlobResult createBlockBlob(String blob, InputStream contentStream)
            throws ServiceException;

    /**
     * Creates the block blob.
     * 
     * @param blob
     *            the blob
     * @param contentStream
     *            the content stream
     * @param options
     *            the options
     * @return the creates the blob result
     * @throws ServiceException
     *             the service exception
     */
    CreateBlobResult createBlockBlob(String blob, InputStream contentStream,
            CreateBlobOptions options) throws ServiceException;

    /**
     * Creates the blob block.
     * 
     * @param blob
     *            the blob
     * @param blockId
     *            the block id
     * @param contentStream
     *            the content stream
     * @throws ServiceException
     *             the service exception
     */
    void createBlobBlock(String blob, String blockId, InputStream contentStream)
            throws ServiceException;

    /**
     * Creates the blob block.
     * 
     * @param blob
     *            the blob
     * @param blockId
     *            the block id
     * @param contentStream
     *            the content stream
     * @param options
     *            the options
     * @throws ServiceException
     *             the service exception
     */
    void createBlobBlock(String blob, String blockId,
            InputStream contentStream, CreateBlobBlockOptions options)
            throws ServiceException;

    /**
     * Commit blob blocks.
     * 
     * @param blob
     *            the blob
     * @param blockList
     *            the block list
     * @throws ServiceException
     *             the service exception
     */
    void commitBlobBlocks(String blob, BlockList blockList)
            throws ServiceException;

    /**
     * Commit blob blocks.
     * 
     * @param blob
     *            the blob
     * @param blockList
     *            the block list
     * @param options
     *            the options
     * @throws ServiceException
     *             the service exception
     */
    void commitBlobBlocks(String blob, BlockList blockList,
            CommitBlobBlocksOptions options) throws ServiceException;
}
