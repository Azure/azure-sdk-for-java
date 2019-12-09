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
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathRenameMode;
import com.azure.storage.file.datalake.implementation.models.SourceModifiedAccessConditions;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
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
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing information about the created resource
     */
    public Response<PathInfo> createWithResponse(String permissions, String umask, PathHttpHeaders headers,
        Map<String, String> metadata, DataLakeRequestConditions requestConditions, Duration timeout,
        Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.createWithResponse(
            permissions, umask, dataLakePathAsyncClient.pathResourceType, headers, metadata, requestConditions,
            context);

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
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return blockBlobClient.setMetadataWithResponse(metadata, Transforms.toBlobRequestConditions(requestConditions),
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
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     */
    public Response<Void> setHttpHeadersWithResponse(PathHttpHeaders headers,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        return blockBlobClient.setHttpHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
            Transforms.toBlobRequestConditions(requestConditions), timeout, context);
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setAccessControlList#List-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList A list of {@link PathAccessControlEntry} objects.
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @return The resource info.
     */
    public PathInfo setAccessControlList(List<PathAccessControlEntry> accessControlList, String group, String owner) {
        return setAccessControlListWithResponse(accessControlList, group, owner, null, null, Context.NONE).getValue();
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList A list of {@link PathAccessControlEntry} objects.
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource info.
     */
    public Response<PathInfo> setAccessControlListWithResponse(List<PathAccessControlEntry> accessControlList,
        String group, String owner, DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.setAccessControlWithResponse(accessControlList,
            null, group, owner, requestConditions, context);

        return StorageImplUtils.blockWithOptionalTimeout(response, timeout);
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setPermissions#PathPermissions-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param permissions {@link PathPermissions}
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @return The resource info.
     */
    public PathInfo setPermissions(PathPermissions permissions, String group, String owner) {
        return setPermissionsWithResponse(permissions, group, owner, null, null, Context.NONE).getValue();
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions-Duration-Context}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param permissions {@link PathPermissions}
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource info.
     */
    public Response<PathInfo> setPermissionsWithResponse(PathPermissions permissions, String group, String owner,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathInfo>> response = dataLakePathAsyncClient.setAccessControlWithResponse(null, permissions,
            group, owner, requestConditions, context);

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
     * @param userPrincipalNameReturned When true, user identity values returned as User Principal Names. When false,
     * user identity values returned as Azure Active Directory Object IDs. Default value is false.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource access control.
     */
    public Response<PathAccessControl> getAccessControlWithResponse(boolean userPrincipalNameReturned,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        Mono<Response<PathAccessControl>> response = dataLakePathAsyncClient.getAccessControlWithResponse(
            userPrincipalNameReturned, requestConditions, context);

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
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource properties and metadata.
     */
    public Response<PathProperties> getPropertiesWithResponse(DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        Response<BlobProperties> response = blockBlobClient.getPropertiesWithResponse(
            Transforms.toBlobRequestConditions(requestConditions), timeout, context);
        return new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue()));
    }

    /**
     * Package-private rename method for use by {@link DataLakeFileClient} and {@link DataLakeDirectoryClient}
     *
     * @param destinationPath The path of the destination relative to the file system name
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakePathClient} used to interact with the path created.
     */
    Mono<Response<DataLakePathClient>> renameWithResponse(String destinationPath,
        DataLakeRequestConditions sourceRequestConditions, DataLakeRequestConditions destinationRequestConditions,
        Context context) {

        destinationRequestConditions = destinationRequestConditions == null ? new DataLakeRequestConditions()
            : destinationRequestConditions;
        sourceRequestConditions = sourceRequestConditions == null ? new DataLakeRequestConditions()
            : sourceRequestConditions;

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceModifiedAccessConditions sourceConditions = new SourceModifiedAccessConditions()
            .setSourceIfModifiedSince(sourceRequestConditions.getIfModifiedSince())
            .setSourceIfUnmodifiedSince(sourceRequestConditions.getIfUnmodifiedSince())
            .setSourceIfMatch(sourceRequestConditions.getIfMatch())
            .setSourceIfNoneMatch(sourceRequestConditions.getIfNoneMatch());

        LeaseAccessConditions destLac = new LeaseAccessConditions()
            .setLeaseId(destinationRequestConditions.getLeaseId());
        ModifiedAccessConditions destMac = new ModifiedAccessConditions()
            .setIfMatch(destinationRequestConditions.getIfMatch())
            .setIfNoneMatch(destinationRequestConditions.getIfNoneMatch())
            .setIfModifiedSince(destinationRequestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(destinationRequestConditions.getIfUnmodifiedSince());

        DataLakePathClient dataLakePathClient = getPathClient(destinationPath);

        String renameSource = "/" + dataLakePathAsyncClient.getFileSystemName() + "/"
            + dataLakePathAsyncClient.getObjectPath();

        return dataLakePathClient.dataLakePathAsyncClient.dataLakeStorage.paths().createWithRestResponseAsync(
            null /* pathResourceType */, null /* continuation */, PathRenameMode.LEGACY, renameSource,
            sourceRequestConditions.getLeaseId(), null /* properties */, null /* permissions */, null /* umask */,
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

    /**
     * Generates a user delegation SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * @see DataLakeServiceClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime) for more information on how to
     * get a user delegation key.
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return dataLakePathAsyncClient.generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey);
    }

    /**
     * Generates a service SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}
     * Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathClient.generateSas#DataLakeServiceSasSignatureValues}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing all SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return dataLakePathAsyncClient.generateSas(dataLakeServiceSasSignatureValues);
    }

}
