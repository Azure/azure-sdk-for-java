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

import com.microsoft.windowsazure.core.pipeline.jersey.JerseyFilterableService;
import java.io.InputStream;
import java.util.HashMap;

import com.microsoft.windowsazure.services.blob.models.AcquireLeaseOptions;
import com.microsoft.windowsazure.services.blob.models.AcquireLeaseResult;
import com.microsoft.windowsazure.services.blob.models.BlobServiceOptions;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.BreakLeaseResult;
import com.microsoft.windowsazure.services.blob.models.CommitBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ContainerACL;
import com.microsoft.windowsazure.services.blob.models.CopyBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CopyBlobResult;
import com.microsoft.windowsazure.services.blob.models.CreateBlobBlockOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobPagesResult;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobSnapshotResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteBlobOptions;
import com.microsoft.windowsazure.services.blob.models.DeleteContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerACLResult;
import com.microsoft.windowsazure.services.blob.models.GetContainerPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetServicePropertiesResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobRegionsResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobsOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobsResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;
import com.microsoft.windowsazure.services.blob.models.PageRange;
import com.microsoft.windowsazure.services.blob.models.ServiceProperties;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobMetadataResult;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesOptions;
import com.microsoft.windowsazure.services.blob.models.SetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.SetContainerMetadataOptions;
import com.microsoft.windowsazure.exception.ServiceException;

/**
 * Defines the methods available on the Windows Azure blob storage service.
 * Construct an object instance implementing <code>BlobContract</code> with one
 * of the static <em>create</em> methods on {@link BlobService}. These methods
 * associate a <code>Configuration</code> with the implementation, so the
 * methods on the instance of <code>BlobContract</code> all work with a
 * particular storage account.
 */
public interface BlobContract extends JerseyFilterableService<BlobContract>
{
    /**
     * Gets the service properties of the blob storage account.
     * 
     * @return A {@link GetServicePropertiesResult} reference to the blob
     *         service properties.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetServicePropertiesResult getServiceProperties() throws ServiceException;

    /**
     * Gets the service properties of the blob storage account, using the
     * specified options.
     * <p>
     * Use the {@link BlobServiceOptions options} parameter to specify a server
     * timeout for the operation.
     * 
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @return A {@link GetServicePropertiesResult} reference to the blob
     *         service properties.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetServicePropertiesResult getServiceProperties(BlobServiceOptions options)
            throws ServiceException;

    /**
     * Sets the service properties of the blob storage account.
     * 
     * @param serviceProperties
     *            A {@link ServiceProperties} instance containing the blob
     *            service properties to set.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setServiceProperties(ServiceProperties serviceProperties)
            throws ServiceException;

    /**
     * Sets the service properties of the blob storage account, using the
     * specified options.
     * <p>
     * Use the {@link BlobServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param serviceProperties
     *            A {@link ServiceProperties} instance containing the blob
     *            service properties to set.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setServiceProperties(ServiceProperties serviceProperties,
            BlobServiceOptions options) throws ServiceException;

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
     * Gets all metadata and system properties for a container.
     * 
     * @param container
     *            A {@link String} containing the name of the container to get
     *            properties and metadata for.
     * @return A {@link GetContainerPropertiesResult} reference to the container
     *         properties and metadata.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetContainerPropertiesResult getContainerProperties(String container)
            throws ServiceException;

    /**
     * Gets all metadata and system properties for a container, using the
     * specified options.
     * <p>
     * Use the {@link BlobServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param container
     *            A {@link String} containing the name of the container to get
     *            properties and metadata for.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @return A {@link GetContainerPropertiesResult} reference to the container
     *         properties and metadata.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetContainerPropertiesResult getContainerProperties(String container,
            BlobServiceOptions options) throws ServiceException;

    /**
     * Gets all metadata for a container.
     * 
     * @param container
     *            A {@link String} containing the name of the container to get
     *            the metadata from.
     * @return A {@link GetContainerPropertiesResult} reference to the container
     *         metadata.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetContainerPropertiesResult getContainerMetadata(String container)
            throws ServiceException;

    /**
     * Gets all metadata for a container, using the specified options.
     * <P>
     * Use the {@link BlobServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param container
     *            A {@link String} containing the name of the container to get
     *            the metadata from.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @return A {@link GetContainerPropertiesResult} reference to the container
     *         metadata.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetContainerPropertiesResult getContainerMetadata(String container,
            BlobServiceOptions options) throws ServiceException;

    /**
     * Gets the public access level and container-level access policies for a
     * container.
     * 
     * @param container
     *            A {@link String} containing the name of the container to get
     *            the public access level and container-level access policies
     *            from.
     * @return A {@link GetContainerACLResult} reference to the container's
     *         public access level and container-level access policies.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetContainerACLResult getContainerACL(String container)
            throws ServiceException;

    /**
     * Gets the public access level and container-level access policies for a
     * container, using the specified options.
     * <p>
     * Use the {@link BlobServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param container
     *            A {@link String} containing the name of the container to get
     *            the public access level and container-level access policies
     *            from.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @return A {@link GetContainerACLResult} reference to the container's
     *         public access level and container-level access policies.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetContainerACLResult getContainerACL(String container,
            BlobServiceOptions options) throws ServiceException;

    /**
     * Sets the public access level and container-level access policies for a
     * container.
     * 
     * @param container
     *            A {@link String} containing the name of the container to set
     *            the public access level and container-level access policies
     *            on.
     * @param acl
     *            A {@link ContainerACL} instance containing the public access
     *            level and container-level access policies to set on the
     *            container.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setContainerACL(String container, ContainerACL acl)
            throws ServiceException;

    /**
     * Sets the public access level and container-level access policies for a
     * container, using the specified options.
     * <p>
     * Use the {@link BlobServiceOptions options} parameter to specify the
     * server timeout for the operation.
     * 
     * @param container
     *            A {@link String} containing the name of the container to set
     *            the public access level and container-level access policies
     *            on.
     * @param acl
     *            A {@link ContainerACL} instance containing the public access
     *            level and container-level access policies to set on the
     *            container.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setContainerACL(String container, ContainerACL acl,
            BlobServiceOptions options) throws ServiceException;

    /**
     * Sets the metadata on a container.
     * <p>
     * Calling this method overwrites all existing metadata that is associated
     * with the container. It is not possible to modify an individual name-value
     * pair. To keep the existing metadata and update or add individual values,
     * make a copy of the metadata, make your changes, and upload the modified
     * copy. Calling this method also updates the Etag and Last-Modified-Time
     * properties for the container.
     * <p>
     * Metadata for a container can also be set at the time it is created.
     * 
     * @param container
     *            A {@link String} containing the name of the container to set
     *            metadata on.
     * @param metadata
     *            A {@link java.util.HashMap} of pairs of {@link String}
     *            containing the metadata name-value pairs to set on the
     *            container.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setContainerMetadata(String container, HashMap<String, String> metadata)
            throws ServiceException;

    /**
     * Sets the metadata on a container, using the specified options.
     * <p>
     * Use the {@link SetContainerMetadataOptions options} parameter to specify
     * the server timeout for the operation, and to set access conditions on
     * whether to update the metadata or not.
     * <p>
     * Calling this method overwrites all existing metadata that is associated
     * with the container. It is not possible to modify an individual name-value
     * pair. To keep the existing metadata and update or add individual values,
     * make a copy of the metadata, make your changes, and upload the modified
     * copy. Calling this method also updates the ETag and last modified time
     * properties for the container.
     * <p>
     * Metadata for a container can also be set at the time it is created.
     * 
     * @param container
     *            A {@link String} containing the name of the container to set
     *            metadata on.
     * @param metadata
     *            A {@link java.util.HashMap} of pairs of {@link String}
     *            containing the metadata name-value pairs to set on the
     *            container.
     * @param options
     *            A {@link SetContainerMetadataOptions} instance containing
     *            options for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void setContainerMetadata(String container,
            HashMap<String, String> metadata,
            SetContainerMetadataOptions options) throws ServiceException;

    /**
     * Lists the blobs in a container.
     * 
     * @param container
     *            A {@link String} containing the name of the container of blobs
     *            to list.
     * @return A {@link ListBlobsResult} reference to the result of the list
     *         blobs operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListBlobsResult listBlobs(String container) throws ServiceException;

    /**
     * Lists the blobs in a container, using the specified options.
     * <p>
     * Use the {@link ListBlobsOptions options} parameter to optionally specify
     * the server timeout for the operation, a prefix for blobs to match, a
     * marker to continue a list operation, a maximum number of results to
     * return with one list operation, a delimiter for structuring virtual blob
     * hierarchies, and whether to include blob metadata, blob snapshots, and
     * uncommitted blobs in the results.
     * 
     * @param container
     *            A {@link String} containing the name of the container of blobs
     *            to list.
     * @param options
     *            A {@link ListBlobsOptions} instance containing options for the
     *            request.
     * @return A {@link ListBlobsResult} reference to the result of the list
     *         blobs operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListBlobsResult listBlobs(String container, ListBlobsOptions options)
            throws ServiceException;

    /**
     * Creates a page blob of the specified maximum length.
     * <p>
     * Note that this method only initializes the page blob. To add content to a
     * page blob, use the
     * {@link BlobContract#createBlobPages(String, String, PageRange, long, InputStream)}
     * or
     * {@link BlobContract#createBlobPages(String, String, PageRange, long, InputStream, CreateBlobPagesOptions)}
     * methods.
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
     * @param length
     *            The length in bytes of the page blob to create. The length
     *            must be a multiple of 512 and may be up to 1 TB.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobResult createPageBlob(String container, String blob, long length)
            throws ServiceException;

    /**
     * Creates a page blob of the specified maximum length, using the specified
     * options.
     * <p>
     * Use the {@link CreateBlobOptions options} parameter to optionally specify
     * the server timeout for the operation, the MIME content type and content
     * encoding for the blob, the content language, the MD5 hash, a cache
     * control value, blob metadata, and a sequence number.
     * <p>
     * Note that this method only initializes the blob. To add content to a page
     * blob, use the
     * {@link BlobContract#createBlobPages(String, String, PageRange, long, InputStream)}
     * or
     * {@link BlobContract#createBlobPages(String, String, PageRange, long, InputStream, CreateBlobPagesOptions)}
     * methods.
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
     * @param length
     *            The length in bytes of the page blob to create. The length
     *            must be a multiple of 512 and may be up to 1 TB.
     * @param options
     *            A {@link CreateBlobOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobResult createPageBlob(String container, String blob, long length,
            CreateBlobOptions options) throws ServiceException;

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
     * Clears a range of pages from a page blob.
     * <p>
     * This method clears and releases the storage for the page range specified
     * by the <em>range</em> parameter within the page blob specified by the
     * <em>blob</em> and <em>container</em> parameters. The <em>range</em>
     * parameter can specify up to the value of the blob's full size.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to clear the
     *            range from.
     * @param range
     *            A {@link PageRange} containing the start and end of the range
     *            to clear in the page blob.
     * @return A {@link CreateBlobPagesResult} reference to the result of the
     *         clear blob pages operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobPagesResult clearBlobPages(String container, String blob,
            PageRange range) throws ServiceException;

    /**
     * Clears a range of pages from a page blob, using the specified options.
     * <p>
     * This method clears and releases the storage for the page range specified
     * by the <em>range</em> parameter within the page blob specified by the
     * <em>blob</em> and <em>container</em> parameters. The <em>range</em>
     * parameter can specify up to the value of the blob's full size. Use the
     * {@link CreateBlobPagesOptions options} parameter to optionally specify
     * the server timeout for the operation, the lease ID if the blob has an
     * active lease, and access conditions for the clear pages operation.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to clear the
     *            range from.
     * @param range
     *            A {@link PageRange} containing the start and end of the range
     *            to clear in the page blob.
     * @param options
     *            A {@link CreateBlobPagesOptions} instance containing options
     *            for the request.
     * @return A {@link CreateBlobPagesResult} reference to the result of the
     *         clear blob pages operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobPagesResult clearBlobPages(String container, String blob,
            PageRange range, CreateBlobPagesOptions options)
            throws ServiceException;

    /**
     * Creates or updates a range of pages in a page blob.
     * <p>
     * This method creates or updates the storage for the page range specified
     * by the <em>range</em> parameter within the page blob specified by the
     * <em>blob</em> and <em>container</em> parameters. The <em>range</em>
     * parameter can specify up to 4MB of data, and its length must match the
     * <em>length</em> parameter. The operation stores <em>length</em> bytes
     * from the <em>contentStream</em> parameter in the specified page range in
     * the blob.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to create or
     *            update the range content of.
     * @param range
     *            A {@link PageRange} containing the start and end of the range
     *            to create or update in the page blob.
     * @param length
     *            The number of bytes to read from the <em>contentStream</em>
     *            parameter into the specified range.
     * @param contentStream
     *            An {@link InputStream} reference to the content to copy to the
     *            page blob.
     * @return A {@link CreateBlobPagesResult} reference to the result of the
     *         create blob pages operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobPagesResult createBlobPages(String container, String blob,
            PageRange range, long length, InputStream contentStream)
            throws ServiceException;

    /**
     * Creates or updates a range of pages in a page blob, using the specified
     * options.
     * <p>
     * This method creates or updates the storage for the page range specified
     * by the <em>range</em> parameter within the page blob specified by the
     * <em>blob</em> and <em>container</em> parameters. The <em>range</em>
     * parameter can specify up to 4MB of data, and its length must match the
     * <em>length</em> parameter. The operation stores <em>length</em> bytes
     * from the <em>contentStream</em> parameter in the specified page range in
     * the blob. Use the {@link CreateBlobPagesOptions options} parameter to
     * optionally specify the server timeout for the operation, the lease ID if
     * the blob has an active lease, an MD5 hash of the content for
     * verification, and access conditions for the create pages operation.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to create or
     *            update the range content of.
     * @param range
     *            A {@link PageRange} containing the start and end of the range
     *            to create or update in the page blob.
     * @param length
     *            The number of bytes to read from the <em>contentStream</em>
     *            parameter into the specified range.
     * @param contentStream
     *            An {@link InputStream} reference to the content to copy to the
     *            page blob.
     * @param options
     *            A {@link CreateBlobPagesOptions} instance containing options
     *            for the request.
     * @return A {@link CreateBlobPagesResult} reference to the result of the
     *         create blob pages operation.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobPagesResult createBlobPages(String container, String blob,
            PageRange range, long length, InputStream contentStream,
            CreateBlobPagesOptions options) throws ServiceException;

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
     * {@link BlobContract#commitBlobBlocks(String, String, BlockList)} or
     * {@link BlobContract#commitBlobBlocks(String, String, BlockList, CommitBlobBlocksOptions)}.
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
     * {@link BlobContract#commitBlobBlocks(String, String, BlockList)} or
     * {@link BlobContract#commitBlobBlocks(String, String, BlockList, CommitBlobBlocksOptions)}.
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
     * {@link BlobContract#createBlobBlock(String, String, String, InputStream)}
     * or
     * {@link BlobContract#createBlobBlock(String, String, String, InputStream, CreateBlobBlockOptions)}.
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
     * {@link BlobContract#createBlobBlock(String, String, String, InputStream)}
     * or
     * {@link BlobContract#createBlobBlock(String, String, String, InputStream, CreateBlobBlockOptions)}.
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
     * Gets the metadata of a blob.
     * <p>
     * This method lists the user-specified metadata of the blob specified by
     * the <em>blob</em> and <em>container</em> parameters.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get
     *            metadata for.
     * @return A {@link GetBlobMetadataResult} instance containing the blob
     *         metadata returned for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetBlobMetadataResult getBlobMetadata(String container, String blob)
            throws ServiceException;

    /**
     * Gets the metadata of a blob, using the specified options.
     * <p>
     * This method lists the user-specified metadata of the blob specified by
     * the <em>blob</em> and <em>container</em> parameters. Use the
     * {@link GetBlobMetadataOptions options} parameter to set an optional
     * server timeout for the operation, the lease ID if the blob has an active
     * lease, the snapshot timestamp to get the properties of a snapshot, and
     * any access conditions for the request.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get
     *            metadata for.
     * @param options
     *            A {@link GetBlobMetadataOptions} instance containing options
     *            for the request.
     * @return A {@link GetBlobMetadataResult} instance containing the blob
     *         metadata returned for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    GetBlobMetadataResult getBlobMetadata(String container, String blob,
            GetBlobMetadataOptions options) throws ServiceException;

    /**
     * Gets the list of valid page ranges for a page blob.
     * <p>
     * This method lists the valid page ranges of the page blob specified by the
     * <em>blob</em> and <em>container</em> parameters.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get page
     *            ranges for.
     * @return A {@link ListBlobRegionsResult} instance containing the valid
     *         page ranges of the blob.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListBlobRegionsResult listBlobRegions(String container, String blob)
            throws ServiceException;

    /**
     * Gets a list of valid page ranges for a page blob or snapshot of a page
     * blob, using the specified options.
     * <p>
     * This method lists the valid page ranges of the page blob specified by the
     * <em>blob</em> and <em>container</em> parameters. Use the
     * {@link ListBlobRegionsOptions options} parameter to set an optional
     * server timeout for the operation, the lease ID if the blob has an active
     * lease, the snapshot timestamp to get the valid page ranges of a snapshot,
     * the start offset and/or end offset to use to narrow the returned valid
     * page range results, and any access conditions for the request.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to get page
     *            ranges for.
     * @param options
     *            A {@link ListBlobRegionsOptions} instance containing options
     *            for the request.
     * @return A {@link ListBlobRegionsResult} instance containing the valid
     *         page ranges of the blob.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    ListBlobRegionsResult listBlobRegions(String container, String blob,
            ListBlobRegionsOptions options) throws ServiceException;

    /**
     * Sets the specified properties on a blob.
     * <p>
     * This method sets properties on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. Use the
     * {@link SetBlobPropertiesOptions options} parameter to set an optional
     * server timeout for the operation, the MIME content type and content
     * encoding for the blob, the content length, the content language, the MD5
     * hash, a cache control value, a blob lease ID, a sequence number and
     * sequence number action value, and any access conditions for the
     * operation.
     * <p>
     * A page blob's sequence number is updated only if the request meets either
     * of the following conditions:
     * <ul>
     * <li>The request sets the sequence number action to <code>max</code> or
     * <code>update</code>, and also specifies a value for the sequence number.</li>
     * <li>The request sets the sequence number action to <code>increment</code>
     * , indicating that the service should increment the sequence number by
     * one.</li>
     * </ul>
     * <p>
     * A page blob's size is modified only if the request specifies a value for
     * the content length.
     * <p>
     * If a request sets only a sequence number and/or content length, and no
     * other properties, then none of the blob's other properties are modified.
     * <p>
     * If any one or more of the following properties is set in the request,
     * then all of these properties are set together. If a value is not provided
     * for a given property when at least one of the properties listed below is
     * set, then that property will be cleared for the blob.
     * <ul>
     * <li>cache control</li>
     * <li>content type</li>
     * <li>MD5 hash value</li>
     * <li>content encoding</li>
     * <li>content language</li>
     * </ul>
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to set
     *            properties on.
     * @param options
     *            A {@link SetBlobPropertiesOptions} instance containing options
     *            for the request.
     * @return A {@link SetBlobPropertiesResult} instance containing the ETag
     *         and last modified time of the blob, and the sequence number, if
     *         applicable.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    SetBlobPropertiesResult setBlobProperties(String container, String blob,
            SetBlobPropertiesOptions options) throws ServiceException;

    /**
     * Sets user-specified metadata on a blob.
     * <p>
     * This method sets the metadata specified in the <em>metadata</em>
     * parameter on the blob specified by the <em>blob</em> and
     * <em>container</em> parameters.
     * <p>
     * Blob metadata is a collection of name-value {@link String} pairs for
     * client use and is opaque to the server. This request replaces all
     * existing metadata associated with the blob. To remove all metadata from
     * the blob, make this request with an empty metadata list. To replace or
     * append a single metadata name-value pair, get the existing blob metadata
     * and replace or append the new item, then make this request with the
     * updated metadata.
     * <p>
     * Metadata names must adhere to the naming rules for <a
     * href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to set
     *            metadata on.
     * @param metadata
     *            A {@link HashMap} of key-value pairs of {@link String}
     *            containing the blob metadata to set.
     * @return A {@link SetBlobMetadataResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    SetBlobMetadataResult setBlobMetadata(String container, String blob,
            HashMap<String, String> metadata) throws ServiceException;

    /**
     * Sets user-specified metadata on a blob, using the specified options.
     * <p>
     * This method sets the metadata specified in the <em>metadata</em>
     * parameter on the blob specified by the <em>blob</em> and
     * <em>container</em> parameters. Use the {@link SetBlobMetadataOptions
     * options} parameter to set an optional server timeout for the operation, a
     * blob lease ID, and any access conditions for the operation.
     * <p>
     * Blob metadata is a collection of name-value {@link String} pairs for
     * client use and is opaque to the server. This request replaces all
     * existing metadata associated with the blob. To remove all metadata from
     * the blob, make this request with an empty metadata list. To replace or
     * append a single metadata name-value pair, get the existing blob metadata
     * and replace or append the new item, then make this request with the
     * updated metadata.
     * <p>
     * Metadata names must adhere to the naming rules for <a
     * href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to set
     *            metadata on.
     * @param metadata
     *            A {@link HashMap} of key-value pairs of {@link String}
     *            containing the blob metadata to set.
     * @param options
     *            A {@link SetBlobMetadataOptions} instance containing options
     *            for the request.
     * @return A {@link SetBlobMetadataResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    SetBlobMetadataResult setBlobMetadata(String container, String blob,
            HashMap<String, String> metadata, SetBlobMetadataOptions options)
            throws ServiceException;

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
     * Creates a read-only snapshot of a blob.
     * <p>
     * This method creates a read-only snapshot of the blob specified by the
     * <em>blob</em> and <em>container</em> parameters as of the time the server
     * processes the request. The current properties and metadata of the blob
     * are copied to the snapshot. The base blob's committed block list is also
     * copied to the snapshot, if the blob is a block blob. Any uncommitted
     * blocks are not copied. Once a snapshot has been created, it can be read,
     * copied, or deleted, but not modified.
     * <p>
     * Use the
     * {@link #createBlobSnapshot(String, String, CreateBlobSnapshotOptions)}
     * overload of this method to make a create blob snapshot request of a blob
     * with an active lease, to change the metadata on the snapshot, or to set
     * access conditions on the request.
     * <p>
     * When you create a snapshot, the Blob service returns a timestamp value
     * that uniquely identifies the snapshot relative to its base blob. You can
     * use this value to perform further operations on the snapshot. Any method
     * on a blob that is valid for a snapshot can be called by specifying the
     * snapshot timestamp as an option. Note that you should treat this
     * timestamp value as opaque.
     * <p>
     * A snapshot provides a convenient way to back up blob data. You can use a
     * snapshot to restore a blob to an earlier version by calling
     * {@link #copyBlob(String, String, String, String, CopyBlobOptions)
     * copyBlob} to overwrite a base blob with its snapshot. The snapshot
     * remains, but the base blob is overwritten with a copy that can be both
     * read and written.
     * <p>
     * Note that each time you make a create blob snapshot request, a new
     * snapshot is created, with a unique timestamp value. A blob can support
     * any number of snapshots. Existing snapshots are never overwritten, but
     * must be deleted explicitly with a
     * {@link #deleteBlob(String, String, DeleteBlobOptions) deleteBlob}
     * request.
     * <p>
     * Note that a lease associated with the base blob is not copied to the
     * snapshot. Snapshots cannot be leased.
     * <p>
     * When a base blob is copied with a
     * {@link #copyBlob(String, String, String, String, CopyBlobOptions)
     * copyBlob} request, any snapshots of the base blob are not copied to the
     * destination blob. When a destination blob is overwritten with a copy, any
     * snapshots associated with the destination blob stay intact under its
     * name.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to create a
     *            snapshot of.
     * @return A {@link CreateBlobSnapshotResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobSnapshotResult createBlobSnapshot(String container, String blob)
            throws ServiceException;

    /**
     * Creates a read-only snapshot of a blob, using the specified options.
     * <p>
     * This method creates a read-only snapshot of the blob specified by the
     * <em>blob</em> and <em>container</em> parameters as of the time the server
     * processes the request. Use the {@link CreateBlobSnapshotOptions options}
     * parameter to set an optional server timeout for the operation, to make a
     * create blob snapshot request of a blob with an active lease, to change
     * the metadata on the snapshot, or to set access conditions on the request.
     * Unless overridden in the <em>options</em> parameter, the current
     * properties and metadata of the blob are copied to the snapshot. The base
     * blob's committed block list is also copied to the snapshot, if the blob
     * is a block blob. Any uncommitted blocks are not copied. Once a snapshot
     * has been created, it can be read, copied, or deleted, but not modified.
     * <p>
     * When you create a snapshot, the Blob service returns a timestamp value
     * that uniquely identifies the snapshot relative to its base blob. You can
     * use this value to perform further operations on the snapshot. Any method
     * on a blob that is valid for a snapshot can be called by specifying the
     * snapshot timestamp as an option. Note that you should treat this
     * timestamp value as opaque.
     * <p>
     * A snapshot provides a convenient way to back up blob data. You can use a
     * snapshot to restore a blob to an earlier version by calling
     * {@link #copyBlob(String, String, String, String, CopyBlobOptions)
     * copyBlob} to overwrite a base blob with its snapshot. The snapshot
     * remains, but the base blob is overwritten with a copy that can be both
     * read and written.
     * <p>
     * Note that each time you make a create blob snapshot request, a new
     * snapshot is created, with a unique timestamp value. A blob can support
     * any number of snapshots. Existing snapshots are never overwritten, but
     * must be deleted explicitly with a
     * {@link #deleteBlob(String, String, DeleteBlobOptions) deleteBlob}
     * request.
     * <p>
     * Note that a lease associated with the base blob is not copied to the
     * snapshot. Snapshots cannot be leased.
     * <p>
     * When a base blob is copied with a
     * {@link #copyBlob(String, String, String, String, CopyBlobOptions)
     * copyBlob} request, any snapshots of the base blob are not copied to the
     * destination blob. When a destination blob is overwritten with a copy, any
     * snapshots associated with the destination blob stay intact under its
     * name.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to create a
     *            snapshot of.
     * @param options
     *            A {@link CreateBlobSnapshotOptions} instance containing
     *            options for the request.
     * @return A {@link CreateBlobSnapshotResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CreateBlobSnapshotResult createBlobSnapshot(String container, String blob,
            CreateBlobSnapshotOptions options) throws ServiceException;

    /**
     * Copies a source blob to a destination within the storage account.
     * <p>
     * This method creates a copy of the properties, metadata, and content of
     * the blob specified by the <em>sourceBlob</em> and
     * <em>sourceContainer</em> in the destination specified by the
     * <em>destinationBlob</em> and <em>destinationContainer</em> parameters.
     * The <em>sourceContainer</em> and <em>destinationContainer</em> parameters
     * may be empty to specify the root container. The destination may be a new
     * or existing blob.
     * <p>
     * Use the
     * {@link #copyBlob(String, String, String, String, CopyBlobOptions)}
     * overload of this method to copy a snapshot, modify the metadata of the
     * destination, copy from or to a blob with an active lease, or to set
     * access conditions on either the source or the destination blob.
     * <p>
     * The source blob for a copy operation may be a block blob or a page blob,
     * or a snapshot of either. If the destination blob already exists, it must
     * be of the same blob type as the source blob.
     * <p>
     * Copying a source blob always copies the entire blob; copying a range of
     * bytes or a set of blocks is not supported.
     * <p>
     * A copy blob request can take any of the following forms:
     * <ul>
     * <li>You can copy a source blob to a destination blob with a different
     * name from that of the source blob. The destination blob can be an
     * existing blob, or a new blob created by the copy operation.</li>
     * <li>You can copy a source blob to a destination blob with the same name,
     * effectively replacing the source blob. Such a copy operation removes any
     * uncommitted blocks and overwrites the blob's metadata.</li>
     * <li>
     * You can copy a snapshot over its base blob. By promoting a snapshot to
     * the position of the base blob, you can restore an earlier version of a
     * blob.</li>
     * <li>
     * You can copy a snapshot to a destination blob with a different name. The
     * resulting destination blob is a writeable blob and not a snapshot.</li>
     * </ul>
     * <p>
     * The source blob's committed block list is also copied to the destination
     * blob, if the blob is a block blob. Any uncommitted blocks are not copied.
     * <p>
     * The destination blob is always the same size as the source blob, so the
     * value of the Content-Length header for the destination blob will be the
     * same as that for the source blob.
     * <p>
     * When the source blob and destination blob are the same, Copy Blob removes
     * any uncommitted blocks.
     * <p>
     * When a source blob is copied, any snapshots of the source blob are not
     * copied to the destination. When a destination blob is overwritten with a
     * copy, any snapshots associated with the destination blob stay intact
     * under its name.
     * 
     * @param destinationContainer
     *            A {@link String} containing the name of the destination blob's
     *            container.
     * @param destinationBlob
     *            A {@link String} containing the name of the destination blob.
     * @param sourceContainer
     *            A {@link String} containing the name of the source blob's
     *            container.
     * @param sourceBlob
     *            A {@link String} containing the name of the source blob.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CopyBlobResult copyBlob(String destinationContainer,
            String destinationBlob, String sourceContainer, String sourceBlob)
            throws ServiceException;

    /**
     * Copies a source blob to a destination within the storage account, using
     * the specified options.
     * <p>
     * This method creates a copy of the properties, metadata, and content of
     * the blob specified by the <em>sourceBlob</em> and
     * <em>sourceContainer</em> in the destination specified by the
     * <em>destinationBlob</em> and <em>destinationContainer</em> parameters.
     * The <em>sourceContainer</em> and <em>destinationContainer</em> parameters
     * may be empty to specify the root container. The destination may be a new
     * or existing blob. Use the {@link CopyBlobOptions options} parameter to
     * set an optional server timeout for the operation, an optional source
     * snapshot timestamp value to copy from a particular snapshot of the source
     * blob, new blob metadata to set on the destination blob, a blob lease ID
     * to overwrite a blob with an active lease, a source lease ID to copy from
     * a source blob with an active lease, any access conditions to satisfy on
     * the destination, and any access conditions to satisfy on the source.
     * <p>
     * The source blob for a copy operation may be a block blob or a page blob,
     * or a snapshot of either. If the destination blob already exists, it must
     * be of the same blob type as the source blob.
     * <p>
     * Copying a source blob always copies the entire blob; copying a range of
     * bytes or a set of blocks is not supported.
     * <p>
     * A copy blob request can take any of the following forms:
     * <ul>
     * <li>You can copy a source blob to a destination blob with a different
     * name from that of the source blob. The destination blob can be an
     * existing blob, or a new blob created by the copy operation.</li>
     * <li>You can copy a source blob to a destination blob with the same name,
     * effectively replacing the source blob. Such a copy operation removes any
     * uncommitted blocks and overwrites the blob's metadata.</li>
     * <li>You can copy a snapshot over its base blob. By promoting a snapshot
     * to the position of the base blob, you can restore an earlier version of a
     * blob.</li>
     * <li>You can copy a snapshot to a destination blob with a different name.
     * The resulting destination blob is a writeable blob and not a snapshot.</li>
     * </ul>
     * <p>
     * The source blob's committed block list is also copied to the destination
     * blob, if the blob is a block blob. Any uncommitted blocks are not copied.
     * <p>
     * The destination blob is always the same size as the source blob, so the
     * value of the Content-Length header for the destination blob will be the
     * same as that for the source blob.
     * <p>
     * You can specify one or more new metadata values for the destination blob
     * by specifying the metadata in the <em>options</em> parameter. If this
     * option is not specified, the metadata associated with the source blob is
     * copied to the destination blob.
     * <p>
     * When the source blob and destination blob are the same, Copy Blob removes
     * any uncommitted blocks. If metadata is specified in this case, the
     * existing metadata is overwritten with the new metadata.
     * <p>
     * You can specify access conditions on the request to copy the blob only if
     * a condition is met. If the specified conditions are not met, the blob is
     * not copied, and the Blob service returns status code 412 (Precondition
     * Failed), which causes a {@link ServiceException} to be thrown.
     * <p>
     * If the destination blob has an active lease, you must specify a valid
     * lease ID for the active lease in order to copy the blob.
     * <p>
     * If the source blob has an active lease, you can optionally specify the
     * lease ID for the source blob to copy the source blob conditionally. In
     * this case, the source blob will be copied only if the lease ID for the
     * source blob matches that specified on the request.
     * <p>
     * Copying a blob does not affect an existing lease on the destination blob.
     * The destination blob's lease is maintained, whether you are copying a
     * blob to a destination blob with a different name from the source, copying
     * the blob to a destination blob with the same name as the source, or
     * copying a snapshot over its base blob.
     * <p>
     * When a source blob is copied, any snapshots of the source blob are not
     * copied to the destination. When a destination blob is overwritten with a
     * copy, any snapshots associated with the destination blob stay intact
     * under its name.
     * 
     * @param destinationContainer
     *            A {@link String} containing the name of the destination blob's
     *            container.
     * @param destinationBlob
     *            A {@link String} containing the name of the destination blob.
     * @param sourceContainer
     *            A {@link String} containing the name of the source blob's
     *            container.
     * @param sourceBlob
     *            A {@link String} containing the name of the source blob.
     * @param options
     *            A {@link CopyBlobOptions} instance containing options for the
     *            request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    CopyBlobResult copyBlob(String destinationContainer,
            String destinationBlob, String sourceContainer, String sourceBlob,
            CopyBlobOptions options) throws ServiceException;

    /**
     * Gets a new lease on a blob.
     * <p>
     * This method requests a lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. If the blob does not have an active
     * lease, the Blob service creates a new one-minute lock for write
     * operations on the blob and returns a new lease ID. Any operation that
     * writes to the blob's properties, metadata, or content for the duration of
     * the lease must include this lease ID to succeed. You may request a new
     * lease only for a blob without an unexpired lease.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to acquire a
     *            lease on.
     * @return An {@link AcquireLeaseResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    AcquireLeaseResult acquireLease(String container, String blob)
            throws ServiceException;

    /**
     * Gets a new lease on a blob, using the specified options.
     * <p>
     * This method requests a lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. Use the {@link AcquireLeaseOptions
     * options} parameter to set an optional server timeout for the operation
     * and any access conditions for the operation. If the blob does not have an
     * active lease, the Blob service creates a new one-minute lock for write
     * operations on the blob and returns a new lease ID. Any operation that
     * writes to the blob's properties, metadata, or content for the duration of
     * the lease must include this lease ID to succeed. You may request a new
     * lease only for a blob without an unexpired lease.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to acquire a
     *            lease on.
     * @param options
     *            An {@link AcquireLeaseOptions} instance containing options for
     *            the request.
     * @return An {@link AcquireLeaseResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    AcquireLeaseResult acquireLease(String container, String blob,
            AcquireLeaseOptions options) throws ServiceException;

    /**
     * Renews a lease on a blob.
     * <p>
     * This method renews the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters with its most recent lease ID specified
     * by the <em>leaseId</em> parameter. Note that the lease may be renewed
     * even if it has expired or been released as long as the blob has not been
     * modified since the expiration or release of that lease.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to renew a
     *            lease on.
     * @param leaseId
     *            A {@link String} containing the lease ID.
     * @return An {@link AcquireLeaseResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    AcquireLeaseResult renewLease(String container, String blob, String leaseId)
            throws ServiceException;

    /**
     * Renews a lease on a blob, using the specified options.
     * <p>
     * This method renews the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters with its most recent lease ID specified
     * by the <em>leaseId</em> parameter. Use the {@link BlobServiceOptions
     * options} parameter to specify an optional server timeout for the
     * operation. Note that the lease may be renewed even if it has expired or
     * been released as long as the blob has not been modified since the
     * expiration or release of that lease.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to renew a
     *            lease on.
     * @param leaseId
     *            A {@link String} containing the lease ID.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @return An {@link AcquireLeaseResult} instance containing the server
     *         response to the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    AcquireLeaseResult renewLease(String container, String blob,
            String leaseId, BlobServiceOptions options) throws ServiceException;

    /**
     * Releases an active or broken lease on a blob.
     * <p>
     * This method releases the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters with a broken or active lease ID
     * specified by the <em>leaseId</em> parameter. Releasing the lease allows
     * another client to immediately acquire the lease for the blob as soon as
     * the release is complete.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to release a
     *            lease on.
     * @param leaseId
     *            A {@link String} containing the lease ID.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void releaseLease(String container, String blob, String leaseId)
            throws ServiceException;

    /**
     * Releases an active or broken lease on a blob, using the specified
     * options.
     * <p>
     * This method releases the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters with a broken or active lease ID
     * specified by the <em>leaseId</em> parameter. Use the
     * {@link BlobServiceOptions options} parameter to specify the server
     * timeout for the operation. Releasing the lease allows another client to
     * immediately acquire the lease for the blob as soon as the release is
     * complete.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to release a
     *            lease on.
     * @param leaseId
     *            A {@link String} containing the lease ID.
     * @param options
     *            A {@link BlobServiceOptions} instance containing options for
     *            the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     */
    void releaseLease(String container, String blob, String leaseId,
            BlobServiceOptions options) throws ServiceException;

    /**
     * Breaks an active lease on a blob.
     * <p>
     * This method breaks the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. The <em>leaseId</em> parameter is not
     * used by the server.
     * <p>
     * Once a lease is broken, it cannot be renewed. Any authorized request can
     * break the lease; the request is not required to specify a matching lease
     * ID. When a lease is broken, the remaining time on the lease is allowed to
     * elapse, during which time no lease operation may be performed on the
     * blob.
     * <p>
     * A lease that has been broken but has not yet expired can also be
     * released, in which case another client may immediately acquire a new
     * lease on the blob.
     * 
     * @deprecated Server ignores the leaseId parameter, replaced by
     *             {@link #breakLease(String, String)} without the useless
     *             parameter.
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to break a
     *            lease on.
     * 
     * @param leaseId
     *            lease id to break. Ignored.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     * 
     * @return result containing time remaining before a new lease can be
     *         acquired
     */
    @Deprecated()
    void breakLease(String container, String blob, String leaseId)
            throws ServiceException;

    /**
     * Breaks an active lease on a blob, using the specified options.
     * <p>
     * This method breaks the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. The <em>leaseId</em> parameter is not
     * used by the server. Use the {@link BlobServiceOptions options} parameter
     * to specify the server timeout for the operation.
     * <p>
     * Once a lease is broken, it cannot be renewed. Any authorized request can
     * break the lease; the request is not required to specify a matching lease
     * ID. When a lease is broken, the remaining time on the lease is allowed to
     * elapse, during which time no lease operation may be performed on the
     * blob.
     * <p>
     * A lease that has been broken but has not yet expired can also be
     * released, in which case another client may immediately acquire a new
     * lease on the blob.
     * 
     * @deprecated Server ignores the leaseId parameter, replaced by
     *             {@link #breakLease(String, String, com.microsoft.windowsazure.services.blob.models.BlobServiceOptions)}
     *             without the useless parameter.
     * 
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to break a
     *            lease on.
     * @param options
     *            A
     *            {@link com.microsoft.windowsazure.services.blob.models.BlobServiceOptions}
     *            instance containing options for the request.
     * @param leaseId
     *            lease id to break. Ignored.
     * 
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     * @return result containing time remaining before a new lease can be
     *         acquired
     */
    @Deprecated()
    void breakLease(String container, String blob, String leaseId,
            BlobServiceOptions options) throws ServiceException;

    /**
     * Breaks an active lease on a blob.
     * <p>
     * This method breaks the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. The <em>leaseId</em> parameter is not
     * used by the server.
     * <p>
     * Once a lease is broken, it cannot be renewed. Any authorized request can
     * break the lease; the request is not required to specify a matching lease
     * ID. When a lease is broken, the remaining time on the lease is allowed to
     * elapse, during which time no lease operation may be performed on the
     * blob.
     * <p>
     * A lease that has been broken but has not yet expired can also be
     * released, in which case another client may immediately acquire a new
     * lease on the blob.
     * 
     * 
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to break a
     *            lease on.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     * 
     * @return result containing time remaining before a new lease can be
     *         acquired
     */
    BreakLeaseResult breakLease(String container, String blob)
            throws ServiceException;

    /**
     * Breaks an active lease on a blob, using the specified options.
     * <p>
     * This method breaks the lease on the blob specified by the <em>blob</em>
     * and <em>container</em> parameters. The <em>leaseId</em> parameter is not
     * used by the server. Use the {@link BlobServiceOptions options} parameter
     * to specify the server timeout for the operation.
     * <p>
     * Once a lease is broken, it cannot be renewed. Any authorized request can
     * break the lease; the request is not required to specify a matching lease
     * ID. When a lease is broken, the remaining time on the lease is allowed to
     * elapse, during which time no lease operation may be performed on the
     * blob.
     * <p>
     * A lease that has been broken but has not yet expired can also be
     * released, in which case another client may immediately acquire a new
     * lease on the blob.
     * 
     * 
     * 
     * @param container
     *            A {@link String} containing the name of the blob's container.
     * @param blob
     *            A {@link String} containing the name of the blob to break a
     *            lease on.
     * @param options
     *            A
     *            {@link com.microsoft.windowsazure.services.blob.models.BlobServiceOptions}
     *            instance containing options for the request.
     * @throws ServiceException
     *             if an error occurs accessing the storage service.
     * @return result containing time remaining before a new lease can be
     *         acquired
     */
    BreakLeaseResult breakLease(String container, String blob,
            BlobServiceOptions options) throws ServiceException;
}
