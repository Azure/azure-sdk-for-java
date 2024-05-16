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
package com.microsoft.windowsazure.services.blob;

import java.io.InputStream;

import com.microsoft.windowsazure.core.pipeline.jersey.JerseyFilterableService;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteBlobOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;

/**
 * Defines the methods available on the Windows Azure blob storage service.
 * Construct an object instance implementing <code>BlobContract</code> with one
 * of the static <em>create</em> methods on {@link BlobService}. These methods
 * associate a <code>Configuration</code> with the implementation, so the
 * methods on the instance of <code>BlobContract</code> all work with a
 * particular storage account.
 */
public interface BlobContract extends JerseyFilterableService<BlobContract> {

    /**
     * Marks a blob for deletion.
     * <p>
     * This method marks the properties, metadata, and content of the blob
     * specified by the <em>blob</em> and <em>container</em> parameters for
     * deletion.
     * <p>
     * When a blob is successfully deleted, it is immediately removed from the
     * storage account's index and is no longer accessible to clients. The
     * blob's data is later removed from the service during garbage collection.
     * <p>
     * Note that in order to delete a blob, you must delete all of its
     * snapshots. You can delete an individual snapshot, only the snapshots, or
     * both the blob and its snapshots with the
     * {@link #deleteBlob(String, String, DeleteBlobOptions)} method.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to delete.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteBlob(String container, String blob) throws ServiceException;

    /**
     * Marks a blob or snapshot for deletion, using the specified options.
     * <p>
     * This method marks the properties, metadata, and content of the blob or
     * snapshot specified by the <em>blob</em> and <em>container</em> parameters
     * for deletion. Use the {@link DeleteBlobOptions options} parameter to set
     * an optional server timeout for the operation, a snapshot timestamp to
     * specify an individual snapshot to delete, a blob lease ID to delete a
     * blob with an active lease, a flag indicating whether to delete all
     * snapshots but not the blob, or both the blob and all snapshots, and any
     * access conditions to satisfy.
     * <p>
     * When a blob is successfully deleted, it is immediately removed from the
     * storage account's index and is no longer accessible to clients. The
     * blob's data is later removed from the service during garbage collection.
     * <p>
     * If the blob has an active lease, the client must specify a valid lease ID
     * in the <em>options</em> parameter in order to delete it.
     * <p>
     * If a blob has a large number of snapshots, it's possible that the delete
     * blob operation will time out. If this happens, the client should retry
     * the request.
     * 
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to delete.
     * @param options
     *            A {@link DeleteBlobOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteBlob(String container, String blob, DeleteBlobOptions options)
            throws ServiceException;

    /**
     * Gets a list of the containers in the blob storage account.
     * 
     * @return A {@link ListContainersResult} reference to the result of the
     *         list containers operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListContainersResult listContainers() throws ServiceException;

    /**
     * Gets a list of the containers in the blob storage account using the
     * specified options.
     * <p>
     * Use the {@link ListContainersOptions options} parameter to specify
     * options, including a server response timeout for the request, a container
     * name prefix filter, a marker for continuing requests, the maximum number
     * of results to return in a request, and whether to include container
     * metadata in the results.
     * 
     * @param options
     *            A {@link ListContainersOptions} instance containing options
     *            for the request.
     * @return A {@link ListContainersResult} reference to the result of the
     *         list containers operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListContainersResult listContainers(ListContainersOptions options)
            throws ServiceException;

    /**
     * Creates a container with the specified name.
     * <p>
     * Container names must be unique within a storage account, and must follow
     * the naming rules specified in <a href=
     * "http://msdn.microsoft.com/en-us/library/windowsazure/dd135715.aspx"
     * >Naming and Referencing Containers, Blobs, and Metadata</a>.
     * 
     * @param container
     *            A {@link String} containing the name of the container to
     *            create.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void createContainer(String container) throws ServiceException;

    /**
     * Creates a container with the specified name, using the specified options.
     * <p>
     * Use the {@link CreateContainerOptions options} parameter to specify
     * options, including a server response timeout for the request, metadata to
     * set on the container, and the public access level for container and blob
     * data. Container names must be unique within a storage account, and must
     * follow the naming rules specified in <a href=
     * "http://msdn.microsoft.com/en-us/library/windowsazure/dd135715.aspx"
     * >Naming and Referencing Containers, Blobs, and Metadata</a>.
     * 
     * @param container
     *            A {@link String} containing the name of the container to
     *            create.
     * @param options
     *            A {@link CreateContainerOptions} instance containing options
     *            for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void createContainer(String container, CreateContainerOptions options)
            throws ServiceException;

    /**
     * Marks a container for deletion. The container and any blobs contained
     * within it are later deleted during garbage collection.
     * <p>
     * When a container is deleted, a container with the same name cannot be
     * created for at least 30 seconds; the container may not be available for
     * more than 30 seconds if the service is still processing the request.
     * 
     * @param container
     *            A {@link String} containing the name of the container to
     *            delete.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteContainer(String container) throws ServiceException;

    /**
     * Marks a container for deletion, using the specified options. The
     * container and any blobs contained within it are later deleted during
     * garbage collection.
     * <p>
     * Use the {@link DeleteContainerOptions options} parameter to specify the
     * server response timeout and any access conditions for the container
     * deletion operation. Access conditions can be used to make the operation
     * conditional on the value of the Etag or last modified time of the
     * container.
     * <p>
     * When a container is deleted, a container with the same name cannot be
     * created for at least 30 seconds; the container may not be available for
     * more than 30 seconds if the service is still processing the request.
     * 
     * @param container
     *            A {@link String} containing the name of the container to
     *            delete.
     * @param options
     *            A {@link DeleteContainerOptions} instance containing options
     *            for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void deleteContainer(String container, DeleteContainerOptions options)
            throws ServiceException;

    /**
     * Creates a block blob from a content stream.
     * 
     * @param container
     *            A {@link String} containing the name of the container to
     *            create the blob in.
     * @param blob
     *            A {@link String} containing the name of the blob to create. A
     *            blob name can contain any combination of characters, but
     *            reserved URL characters must be properly escaped. A blob name
     *            must be at least one character long and cannot be more than
     *            1,024 characters long, and must be unique within the
     *            container.
     * @param contentStream
     *            An {@link InputStream} reference to the content stream to
     *            upload to the new blob.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobResult createBlockBlob(String container, String blob,
            InputStream contentStream) throws ServiceException;

    /**
     * Creates a block blob from a content stream, using the specified options.
     * <p>
     * Use the {@link CreateBlobOptions options} parameter to optionally specify
     * the server timeout for the operation, the MIME content type and content
     * encoding for the blob, the content language, the MD5 hash, a cache
     * control value, and blob metadata.
     * 
     * @param container
     *            A {@link String} containing the name of the container to
     *            create the blob in.
     * @param blob
     *            A {@link String} containing the name of the blob to create. A
     *            blob name can contain any combination of characters, but
     *            reserved URL characters must be properly escaped. A blob name
     *            must be at least one character long and cannot be more than
     *            1,024 characters long, and must be unique within the
     *            container.
     * @param contentStream
     *            An {@link InputStream} reference to the content to upload to
     *            the new blob.
     * @param options
     *            A {@link CreateBlobOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobResult createBlockBlob(String container, String blob,
            InputStream contentStream, CreateBlobOptions options)
            throws ServiceException;

    /**
     * Creates a new uncommited block from a content stream.
     * <p>
     * This method creates an uncommitted block for a block blob specified by
     * the <em>blob</em> and <em>container</em> parameters. The <em>blockId</em>
     * parameter is a client-specified ID for the block, which must be less than
     * or equal to 64 bytes in size. For a given blob, the length of the value
     * specified for the <em>blockId</em> parameter must be the same size for
     * each block. The <em>contentStream</em> parameter specifies the content to
     * be copied to the block. The content for the block must be less than or
     * equal to 4 MB in size.
     * <p>
     * To create or update a block blob, the blocks that have been successfully
     * written to the server with this method must be committed using a call to
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList, CommitBlobBlocksOptions)}.
     * 
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to create the
     *            block for.
     * @param blockId
     *            A {@link String} containing a client-specified ID for the
     *            block.
     * @param contentStream
     *            An {@link InputStream} reference to the content to copy to the
     *            block.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void createBlobBlock(String container, String blob, String blockId,
            InputStream contentStream) throws ServiceException;

    /**
     * Creates a new uncommitted block from a content stream, using the
     * specified options.
     * <p>
     * This method creates an uncommitted block for a block blob specified by
     * the <em>blob</em> and <em>container</em> parameters. The <em>blockId</em>
     * parameter is a client-specified ID for the block, which must be less than
     * or equal to 64 bytes in size. For a given blob, the length of the value
     * specified for the <em>blockId</em> parameter must be the same size for
     * each block. The <em>contentStream</em> parameter specifies the content to
     * be copied to the block. The content for the block must be less than or
     * equal to 4 MB in size. Use the {@link CreateBlobBlockOptions options}
     * parameter to optionally specify the server timeout for the operation, the
     * lease ID if the blob has an active lease, and the MD5 hash value for the
     * block content.
     * <p>
     * To create or update a block blob, the blocks that have been successfully
     * written to the server with this method must be committed using a call to
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList)} or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList, CommitBlobBlocksOptions)}.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to create the
     *            block for.
     * @param blockId
     *            A {@link String} containing a client-specified ID for the
     *            block.
     * @param contentStream
     *            An {@link InputStream} reference to the content to copy to the
     *            block.
     * @param options
     *            A {@link CreateBlobBlockOptions} instance containing options
     *            for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void createBlobBlock(String container, String blob, String blockId,
            InputStream contentStream, CreateBlobBlockOptions options)
            throws ServiceException;

    /**
     * Commits a list of blocks to a block blob.
     * <p>
     * This method creates or updates the block blob specified by the
     * <em>blob</em> and <em>container</em> parameters. You can call this method
     * to update a blob by uploading only those blocks that have changed, then
     * committing the new and existing blocks together. You can do this with the
     * <em>blockList</em> parameter by specifying whether to commit a block from
     * the committed block list or from the uncommitted block list, or to commit
     * the most recently uploaded version of the block, whichever list it may
     * belong to.
     * <p>
     * In order to be written as part of a blob, each block in the list must
     * have been successfully written to the server with a call to
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobBlock(String, String, String, InputStream)}
     * or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobBlock(String, String, String, InputStream, CreateBlobBlockOptions)}.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the block blob to
     *            create or update.
     * @param blockList
     *            A {@link BlockList} containing the list of blocks to commit to
     *            the block blob.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void commitBlobBlocks(String container, String blob, BlockList blockList)
            throws ServiceException;

    /**
     * Commits a block list to a block blob, using the specified options.
     * <p>
     * This method creates or updates the block blob specified by the
     * <em>blob</em> and <em>container</em> parameters. You can call this method
     * to update a blob by uploading only those blocks that have changed, then
     * committing the new and existing blocks together. You can do this with the
     * <em>blockList</em> parameter by specifying whether to commit a block from
     * the committed block list or from the uncommitted block list, or to commit
     * the most recently uploaded version of the block, whichever list it may
     * belong to. Use the {@link CommitBlobBlocksOptions options} parameter to
     * optionally specify the server timeout for the operation, the MIME content
     * type and content encoding for the blob, the content language, the MD5
     * hash, a cache control value, blob metadata, the lease ID if the blob has
     * an active lease, and any access conditions for the operation.
     * <p>
     * In order to be written as part of a blob, each block in the list must
     * have been successfully written to the server with a call to
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobBlock(String, String, String, InputStream)}
     * or
     * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobBlock(String, String, String, InputStream, CreateBlobBlockOptions)}.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the block blob to
     *            create or update.
     * @param blockList
     *            A {@link BlockList} containing the list of blocks to commit to
     *            the block blob.
     * @param options
     *            A {@link CommitBlobBlocksOptions} instance containing options
     *            for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void commitBlobBlocks(String container, String blob, BlockList blockList,
            CommitBlobBlocksOptions options) throws ServiceException;

    /**
     * Lists the blocks of a blob.
     * <p>
     * This method lists the committed blocks of the block blob specified by the
     * <em>blob</em> and <em>container</em> parameters.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the block blob to
     *            list.
     * @return A {@link ListBlobBlocksResult} instance containing the list of
     *         blocks returned for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListBlobBlocksResult listBlobBlocks(String container, String blob)
            throws ServiceException;

    /**
     * Lists the blocks of a blob, using the specified options.
     * <p>
     * This method lists the committed blocks, uncommitted blocks, or both, of
     * the block blob specified by the <em>blob</em> and <em>container</em>
     * parameters. Use the {@link ListBlobBlocksOptions options} parameter to
     * specify an optional server timeout for the operation, the lease ID if the
     * blob has an active lease, the snapshot timestamp to get the committed
     * blocks of a snapshot, whether to return the committed block list, and
     * whether to return the uncommitted block list. By default, only the
     * committed blocks of the blob are returned.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the block blob to
     *            list.
     * @param options
     *            A {@link ListBlobBlocksOptions} instance containing options
     *            for the request.
     * @return A {@link ListBlobBlocksResult} instance containing the list of
     *         blocks returned for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListBlobBlocksResult listBlobBlocks(String container, String blob,
            ListBlobBlocksOptions options) throws ServiceException;

    /**
     * Gets the properties of a blob.
     * <p>
     * This method lists the properties of the blob specified by the
     * <em>blob</em> and <em>container</em> parameters.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get
     *            properties for.
     * @return A {@link GetBlobPropertiesResult} instance containing the blob
     *         properties returned for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetBlobPropertiesResult getBlobProperties(String container, String blob)
            throws ServiceException;

    /**
     * Gets the properties of a blob, using the specified options.
     * <p>
     * This method lists the properties of the blob specified by the
     * <em>blob</em> and <em>container</em> parameters. Use the
     * {@link GetBlobPropertiesOptions options} parameter to set an optional
     * server timeout for the operation, the lease ID if the blob has an active
     * lease, the snapshot timestamp to get the properties of a snapshot, and
     * any access conditions for the request.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get
     *            properties for.
     * @param options
     *            A {@link GetBlobPropertiesOptions} instance containing options
     *            for the request.
     * @return A {@link GetBlobPropertiesResult} instance containing the blob
     *         properties returned for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetBlobPropertiesResult getBlobProperties(String container, String blob,
            GetBlobPropertiesOptions options) throws ServiceException;

    /**
     * Gets the properties, metadata, and content of a blob.
     * <p>
     * This method gets the properties, metadata, and content of the blob
     * specified by the <em>blob</em> and <em>container</em> parameters.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get.
     * @return A {@link GetBlobResult} instance containing the properties,
     *         metadata, and content of the blob from the server response to the
     *         request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetBlobResult getBlob(String container, String blob)
            throws ServiceException;

    /**
     * Gets the properties, metadata, and content of a blob or blob snapshot,
     * using the specified options.
     * <p>
     * This method gets the properties, metadata, and content of the blob
     * specified by the <em>blob</em> and <em>container</em> parameters. Use the
     * {@link GetBlobOptions options} parameter to set an optional server
     * timeout for the operation, a snapshot timestamp to specify a snapshot, a
     * blob lease ID to get a blob with an active lease, an optional start and
     * end range for blob content to return, and any access conditions to
     * satisfy.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get.
     * @param options
     *            A {@link GetBlobOptions} instance containing options for the
     *            request.
     * @return A {@link GetBlobResult} instance containing the properties,
     *         metadata, and content of the blob from the server response to the
     *         request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetBlobResult getBlob(String container, String blob, GetBlobOptions options)
            throws ServiceException;

}
