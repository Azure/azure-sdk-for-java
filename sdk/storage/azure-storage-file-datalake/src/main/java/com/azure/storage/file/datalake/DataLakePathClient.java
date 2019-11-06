// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathRenameMode;
import com.azure.storage.file.datalake.implementation.models.SourceModifiedAccessConditions;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathProperties;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
public class DataLakePathClient {
    private final ClientLogger logger = new ClientLogger(DataLakePathClient.class);

    protected final DataLakePathAsyncClient dataLakePathAsyncClient;
    protected final BlockBlobClient blockBlobClient;

    DataLakePathClient(DataLakePathAsyncClient dataLakePathAsyncClient, BlockBlobClient blockBlobClient) {
        this.dataLakePathAsyncClient = dataLakePathAsyncClient;
        this.blockBlobClient = blockBlobClient;
    }

    /**
     * Gets the URL of the object represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    String getPathUrl() {
        return dataLakePathAsyncClient.getPathUrl();
    }

    /**
     * Gets the associated account name.
     *
     * @return Account name associated with this storage resource.
     */
    public String getAccountName() {
        return dataLakePathAsyncClient.getAccountName();
    }

    /**
     * Gets the name of the File System in which this object lives.
     *
     * @return The name of the File System.
     */
    public String getFileSystemName() {
        return dataLakePathAsyncClient.getFileSystemName();
    }

    /**
     * Gets the path of this object, not including the name of the resource itself.
     *
     * @return The path of the object.
     */
    String getObjectPath() {
        return dataLakePathAsyncClient.getObjectPath();
    }

    /**
     * Gets the name of this object, not including its full path.
     *
     * @return The name of the object.
     */
    String getObjectName() {
        return dataLakePathAsyncClient.getObjectName();
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakePathAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return dataLakePathAsyncClient.getServiceVersion();
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return Information about the created resource.
     */
    public PathInfo create() {
        return createWithResponse(null, null, null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param permissions POSIX access permissions for the resource owner, the resource owning group, and others.
     * @param umask Restricts permissions of the resource to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing information about the created resource
     */
    public Response<PathInfo> createWithResponse(String permissions, String umask, PathHttpHeaders headers,
        Map<String, String> metadata, DataLakeRequestConditions accessConditions, Duration timeout,
        Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.createWithResponse(
            permissions, umask, dataLakePathAsyncClient.pathResourceType, headers, metadata, accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     */
    public void setMetadata(Map<String, String> metadata) {
        setMetadataWithResponse(metadata, null, null, Context.NONE);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setMetadata#Map-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions accessConditions, Duration timeout, Context context) {
        return blockBlobClient.setMetadataWithResponse(metadata, Transforms.toBlobRequestConditions(accessConditions),
            timeout, context);
    }

    /**
     * Changes a resources's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setHttpHeaders#PathHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     */
    public void setHttpHeaders(PathHttpHeaders headers) {
        setHttpHeadersWithResponse(headers, null, null, Context.NONE);
    }

    /**
     * Changes a resources's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-file-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setHttpHeadersWithResponse(PathHttpHeaders headers,
        DataLakeRequestConditions accessConditions, Duration timeout, Context context) {
        return blockBlobClient.setHttpHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
            Transforms.toBlobRequestConditions(accessConditions), timeout, context);
    }

    /**
     * Changes the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setAccessControl#PathAccessControl}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControl {@link PathAccessControl}
     * @return The resource info.
     */
    public PathInfo setAccessControl(PathAccessControl accessControl) {
        return setAccessControlWithResponse(accessControl, null, null, Context.NONE).getValue();
    }

    /**
     * Changes the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setAccessControlWithResponse#PathAccessControl-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControl {@link PathAccessControl}
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource info.
     */
    public Response<PathInfo> setAccessControlWithResponse(PathAccessControl accessControl,
        DataLakeRequestConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.setAccessControlWithResponse(accessControl,
            accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.getAccessControl}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @return The resource access control.
     */
    public PathAccessControl getAccessControl() {
        return getAccessControlWithResponse(false, null, null, Context.NONE).getValue();
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @param returnUpn When true, user identity values returned as User Principal Names. When false, user identity
     * values returned as Azure Active Directory Object IDs. Default value is false.
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource access control.
     */
    public Response<PathAccessControl> getAccessControlWithResponse(boolean returnUpn,
        DataLakeRequestConditions accessConditions, Duration timeout, Context context) {
        Mono<Response<PathAccessControl>> response = dataLakePathAsyncClient.getAccessControlWithResponse(returnUpn,
            accessConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Returns the resources's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return The resource properties and metadata.
     */
    public PathProperties getProperties() {
        return getPropertiesWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.getPropertiesWithResponse#DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param accessConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource properties and metadata.
     */
    public Response<PathProperties> getPropertiesWithResponse(DataLakeRequestConditions accessConditions,
        Duration timeout, Context context) {
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(
            Transforms.toBlobRequestConditions(accessConditions), timeout, context);
        return new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue()));
    }

    /**
     * Package-private rename method for use by {@link DataLakeFileClient} and {@link DataLakeDirectoryClient}
     *
     * @param destinationPath The path of the destination relative to the file system name
     * @param sourceAccessConditions {@link DataLakeRequestConditions} against the source.
     * @param destAccessConditions {@link DataLakeRequestConditions} against the destination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakePathClient} used to interact with the path created.
     */
    Mono<Response<DataLakePathClient>> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceAccessConditions, DataLakeRequestConditions destAccessConditions,
        Context context) {

        destAccessConditions = destAccessConditions == null ? new DataLakeRequestConditions() : destAccessConditions;
        sourceAccessConditions = sourceAccessConditions == null ? new DataLakeRequestConditions()
            : sourceAccessConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceAccessConditions.getIfModifiedSince())
            .setSourceIfUnmodifiedSince(sourceAccessConditions.getIfUnmodifiedSince())
            .setSourceIfMatch(sourceAccessConditions.getIfMatch())
            .setSourceIfNoneMatch(sourceAccessConditions.getIfNoneMatch());

        LeaseAccessConditions destLac = new LeaseAccessConditions().setLeaseId(destAccessConditions.getLeaseId());
        ModifiedAccessConditions destMac = new ModifiedAccessConditions()
            .setIfMatch(destAccessConditions.getIfMatch())
            .setIfNoneMatch(destAccessConditions.getIfNoneMatch())
            .setIfModifiedSince(destAccessConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(destAccessConditions.getIfUnmodifiedSince());

        DataLakePathClient dataLakePathClient = getPathClient(destinationPath);

        String renameSource = "/" + dataLakePathAsyncClient.getFileSystemName() + "/"
            + dataLakePathAsyncClient.getObjectPath();

        return dataLakePathClient.dataLakePathAsyncClient.dataLakeStorage.paths().createWithRestResponseAsync(
            null /* pathResourceType */, null /* continuation */, PathRenameMode.LEGACY, renameSource,
            sourceAccessConditions.getLeaseId(), null /* properties */, null /* permissions */, null /* umask */,
            null /* request id */, null /* timeout */, null /* headers */, destLac, destMac, sourceConditions, context)
            .map(response -> new SimpleResponse<>(response, dataLakePathClient));
    }

    private DataLakePathClient getPathClient(String destinationPath) {
        return new DataLakePathClient(dataLakePathAsyncClient.getPathAsyncClient(destinationPath),
            dataLakePathAsyncClient.prepareBuilderReplacePath(destinationPath).buildBlockBlobClient());
    }

    BlockBlobClient getBlockBlobClient() {
        return blockBlobClient;
    }

}
