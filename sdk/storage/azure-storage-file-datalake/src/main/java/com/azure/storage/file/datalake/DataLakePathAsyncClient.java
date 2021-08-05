// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.AzureDataLakeStorageRestAPIImpl;
import com.azure.storage.file.datalake.implementation.AzureDataLakeStorageRestAPIImplBuilder;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.PathGetPropertiesAction;
import com.azure.storage.file.datalake.implementation.models.PathRenameMode;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.models.PathSetAccessControlRecursiveMode;
import com.azure.storage.file.datalake.implementation.models.PathsSetAccessControlRecursiveResponse;
import com.azure.storage.file.datalake.implementation.models.SourceModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.AccessControlChangeCounters;
import com.azure.storage.file.datalake.models.AccessControlChangeFailure;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessControlChanges;
import com.azure.storage.file.datalake.models.DataLakeAclChangeFailedException;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.PathRemoveAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathSetAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.options.PathUpdateAccessControlRecursiveOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.storage.common.Utility.STORAGE_TRACING_NAMESPACE_VALUE;

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class, isAsync = true)
public class DataLakePathAsyncClient {

    private final ClientLogger logger = new ClientLogger(DataLakePathAsyncClient.class);

    final AzureDataLakeStorageRestAPIImpl dataLakeStorage;
    final AzureDataLakeStorageRestAPIImpl fileSystemDataLakeStorage;
    /**
     * This {@link AzureDataLakeStorageRestAPIImpl} is pointing to blob endpoint instead of dfs
     * in order to expose APIs that are on blob endpoint but are only functional for HNS enabled accounts.
     */
    final AzureDataLakeStorageRestAPIImpl blobDataLakeStorage;
    private final String accountName;
    private final String fileSystemName;
    final String pathName;
    private final DataLakeServiceVersion serviceVersion;

    final PathResourceType pathResourceType;

    final BlockBlobAsyncClient blockBlobAsyncClient;

    /**
     * Package-private constructor for use by {@link DataLakePathClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param serviceVersion The version of the service to receive requests.
     * @param accountName The storage account name.
     * @param fileSystemName The file system name.
     * @param pathName The path name.
     * @param blockBlobAsyncClient The underlying {@link BlobContainerAsyncClient}
     */
    DataLakePathAsyncClient(HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion,
        String accountName, String fileSystemName, String pathName, PathResourceType pathResourceType,
        BlockBlobAsyncClient blockBlobAsyncClient) {
        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
        this.pathName = Utility.urlDecode(pathName);
        this.pathResourceType = pathResourceType;
        this.blockBlobAsyncClient = blockBlobAsyncClient;
        this.dataLakeStorage = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .fileSystem(fileSystemName)
            .path(this.pathName)
            .version(serviceVersion.getVersion())
            .buildClient();
        this.serviceVersion = serviceVersion;

        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(url, "blob", "dfs");
        this.blobDataLakeStorage = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(blobUrl)
            .fileSystem(fileSystemName)
            .path(this.pathName)
            .version(serviceVersion.getVersion())
            .buildClient();

        this.fileSystemDataLakeStorage = new AzureDataLakeStorageRestAPIImplBuilder()
            .pipeline(pipeline)
            .url(url)
            .fileSystem(fileSystemName)
            .version(serviceVersion.getVersion())
            .buildClient();
    }

    /**
     * Converts the metadata into a string of format "key1=value1, key2=value2" and Base64 encodes the values.
     *
     * @param metadata The metadata.
     *
     * @return The metadata represented as a String.
     */
    static String buildMetadataString(Map<String, String> metadata) {
        if (!CoreUtils.isNullOrEmpty(metadata)) {
            StringBuilder sb = new StringBuilder();
            for (final Map.Entry<String, String> entry : metadata.entrySet()) {
                if (Objects.isNull(entry.getKey()) || entry.getKey().isEmpty()) {
                    throw new IllegalArgumentException("The key for one of the metadata key-value pairs is null, "
                        + "empty, or whitespace.");
                } else if (Objects.isNull(entry.getValue()) || entry.getValue().isEmpty()) {
                    throw new IllegalArgumentException("The value for one of the metadata key-value pairs is null, "
                        + "empty, or whitespace.");
                }

                /*
                The service has an internal base64 decode when metadata is copied from ADLS to Storage, so getMetadata
                will work as normal. Doing this encoding for the customers preserves the existing behavior of
                metadata.
                 */
                sb.append(entry.getKey()).append('=')
                    .append(new String(Base64.getEncoder().encode(entry.getValue().getBytes(StandardCharsets.UTF_8)),
                        StandardCharsets.UTF_8)).append(',');
            }
            sb.deleteCharAt(sb.length() - 1); // Remove the extraneous "," after the last element.
            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * Gets the URL of the storage account.
     *
     * @return the URL.
     */
    String getAccountUrl() {
        return dataLakeStorage.getUrl();
    }

    /**
     * Gets the URL of the object represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    String getPathUrl() {
        return dataLakeStorage.getUrl() + "/" + fileSystemName + "/" + Utility.urlEncode(pathName);
    }

    /**
     * Gets the associated account name.
     *
     * @return Account name associated with this storage resource.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Gets the name of the File System in which this object lives.
     *
     * @return The name of the File System.
     */
    public String getFileSystemName() {
        return fileSystemName;
    }

    /**
     * Gets the full path of this object.
     *
     * @return The path of the object.
     */
    String getObjectPath() {
        return pathName;
    }

    /**
     * Gets the name of this object, not including its full path.
     *
     * @return The name of the object.
     */
    String getObjectName() {
        // Split on / in the path
        String[] pathParts = getObjectPath().split("/");
        // Return last part of path
        return pathParts[pathParts.length - 1];
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakeStorage.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Creates a resource. By default this method will not overwrite an existing path.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.create}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response containing information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> create() {
        try {
            return create(false);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.create#boolean}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param overwrite Whether or not to overwrite, should data exist on the file.
     *
     * @return A reactive response containing information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> create(boolean overwrite) {
        try {
            DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
            if (!overwrite) {
                requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
            }
            return createWithResponse(null, null, null, null, requestConditions).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions}
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param permissions POSIX access permissions for the resource owner, the resource owning group, and others.
     * @param umask Restricts permissions of the resource to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * PathItem}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> createWithResponse(String permissions, String umask, PathHttpHeaders headers,
        Map<String, String> metadata, DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> createWithResponse(permissions, umask, pathResourceType,
                headers, metadata, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PathInfo>> createWithResponse(String permissions, String umask, PathResourceType resourceType,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new DataLakeRequestConditions() : requestConditions;

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        context = context == null ? Context.NONE : context;
        return this.dataLakeStorage.getPaths().createWithResponseAsync(null, null, resourceType, null, null, null, null,
            buildMetadataString(metadata), permissions, umask, headers, lac, mac, null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders().getETag(),
                response.getDeserializedHeaders().getLastModified())));
    }

    /**
     * Package-private delete method for use by {@link DataLakeFileAsyncClient} and {@link DataLakeDirectoryAsyncClient}
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} containing containing status code and HTTP headers
     */
    Mono<Response<Void>> deleteWithResponse(Boolean recursive, DataLakeRequestConditions requestConditions,
        Context context) {
        requestConditions = requestConditions == null ? new DataLakeRequestConditions() : requestConditions;

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        context = context == null ? Context.NONE : context;
        return this.dataLakeStorage.getPaths().deleteWithResponseAsync(null, null, recursive, null, lac, mac,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setMetadata(Map<String, String> metadata) {
        try {
            return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-metadata">Azure Docs</a></p>
     *
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
        DataLakeRequestConditions requestConditions) {
        try {
            return this.blockBlobAsyncClient.setMetadataWithResponse(metadata,
                Transforms.toBlobRequestConditions(requestConditions))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes a resource's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeaders#PathHttpHeaders}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setHttpHeaders(PathHttpHeaders headers) {
        try {
            return setHttpHeadersWithResponse(headers, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes a resources's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> setHttpHeadersWithResponse(PathHttpHeaders headers,
        DataLakeRequestConditions requestConditions) {
        try {
            return this.blockBlobAsyncClient.setHttpHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
                Transforms.toBlobRequestConditions(requestConditions))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the resources's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.getProperties}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the resource's properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathProperties> getProperties() {
        try {
            return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.getPropertiesWithResponse#DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the resource's properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathProperties>> getPropertiesWithResponse(DataLakeRequestConditions requestConditions) {
        try {
            return blockBlobAsyncClient.getPropertiesWithResponse(Transforms.toBlobRequestConditions(requestConditions))
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
                .map(response -> new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Determines if the path this client represents exists in the cloud.
     * <p>Note that this method does not guarantee that the path type (file/directory) matches expectations.</p>
     * <p>For example, a DataLakeFileClient representing a path to a datalake directory will return true, and vice
     * versa.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.exists}
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        try {
            return existsWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Determines if the path this client represents exists in the cloud.
     * <p>Note that this method does not guarantee that the path type (file/directory) matches expectations.</p>
     * <p>For example, a DataLakeFileClient representing a path to a datalake directory will return true, and vice
     * versa.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.existsWithResponse}
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return blockBlobAsyncClient.existsWithResponse()
                .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlList#List-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList A list of {@link PathAccessControlEntry} objects.
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @return A reactive response containing the resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> setAccessControlList(List<PathAccessControlEntry> accessControlList, String group,
        String owner) {
        try {
            return setAccessControlListWithResponse(accessControlList, group, owner, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList A list of {@link PathAccessControlEntry} objects.
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> setAccessControlListWithResponse(List<PathAccessControlEntry> accessControlList,
        String group, String owner, DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> setAccessControlWithResponse(accessControlList,
                null, group, owner, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissions#PathPermissions-String-String}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param permissions {@link PathPermissions}
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @return A reactive response containing the resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> setPermissions(PathPermissions permissions, String group, String owner) {
        try {
            return setPermissionsWithResponse(permissions, group, owner, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param permissions {@link PathPermissions}
     * @param group The group of the resource.
     * @param owner The owner of the resource.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the resource info.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> setPermissionsWithResponse(PathPermissions permissions, String group, String owner,
        DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> setAccessControlWithResponse(null, permissions, group, owner,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PathInfo>> setAccessControlWithResponse(List<PathAccessControlEntry> accessControlList,
        PathPermissions permissions, String group, String owner, DataLakeRequestConditions requestConditions,
        Context context) {

        requestConditions = requestConditions == null ? new DataLakeRequestConditions() : requestConditions;

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        String permissionsString = permissions == null ? null : permissions.toString();
        String accessControlListString =
            accessControlList == null
            ? null
            : PathAccessControlEntry.serializeList(accessControlList);

        context = context == null ? Context.NONE : context;
        return this.dataLakeStorage.getPaths().setAccessControlWithResponseAsync(null, owner, group, permissionsString,
            accessControlListString, null, lac, mac,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders().getETag(),
                response.getDeserializedHeaders().getLastModified())));
    }

    /**
     * Recursively sets the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursive#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList The POSIX access control list for the file or directory.
     * @return A reactive response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessControlChangeResult> setAccessControlRecursive(List<PathAccessControlEntry> accessControlList) {
        try {
            return setAccessControlRecursiveWithResponse(new PathSetAccessControlRecursiveOptions(accessControlList))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Recursively sets the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param options {@link PathSetAccessControlRecursiveOptions}
     * @return A reactive response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessControlChangeResult>> setAccessControlRecursiveWithResponse(
        PathSetAccessControlRecursiveOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        try {
            return withContext(context -> setAccessControlRecursiveWithResponse(
                PathAccessControlEntry.serializeList(options.getAccessControlList()), options.getProgressHandler(),
                PathSetAccessControlRecursiveMode.SET, options.getBatchSize(), options.getMaxBatches(),
                options.isContinueOnFailure(), options.getContinuationToken(), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Recursively updates the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursive#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList The POSIX access control list for the file or directory.
     * @return A reactive response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessControlChangeResult> updateAccessControlRecursive(
        List<PathAccessControlEntry> accessControlList) {
        try {
            return updateAccessControlRecursiveWithResponse(
                new PathUpdateAccessControlRecursiveOptions(accessControlList))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Recursively updates the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param options {@link PathUpdateAccessControlRecursiveOptions}
     * @return A reactive response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessControlChangeResult>> updateAccessControlRecursiveWithResponse(
        PathUpdateAccessControlRecursiveOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        try {
            return withContext(context -> setAccessControlRecursiveWithResponse(
                PathAccessControlEntry.serializeList(options.getAccessControlList()), options.getProgressHandler(),
                PathSetAccessControlRecursiveMode.MODIFY, options.getBatchSize(), options.getMaxBatches(),
                options.isContinueOnFailure(), options.getContinuationToken(), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Recursively removes the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursive#List}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param accessControlList The POSIX access control list for the file or directory.
     * @return A reactive response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessControlChangeResult> removeAccessControlRecursive(
        List<PathRemoveAccessControlEntry> accessControlList) {
        try {
            return removeAccessControlRecursiveWithResponse(
                new PathRemoveAccessControlRecursiveOptions(accessControlList))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Recursively removes the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/update">Azure Docs</a></p>
     *
     * @param options {@link PathRemoveAccessControlRecursiveOptions}
     * @return A reactive response containing the result of the operation.
     *
     * @throws DataLakeAclChangeFailedException if a request to storage throws a
     * {@link DataLakeStorageException} or a {@link Exception} to wrap the exception with the continuation token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessControlChangeResult>> removeAccessControlRecursiveWithResponse(
        PathRemoveAccessControlRecursiveOptions options) {
        StorageImplUtils.assertNotNull("options", options);
        try {
            return withContext(context -> setAccessControlRecursiveWithResponse(
                PathRemoveAccessControlEntry.serializeList(options.getAccessControlList()),
                options.getProgressHandler(), PathSetAccessControlRecursiveMode.REMOVE, options.getBatchSize(),
                options.getMaxBatches(), options.isContinueOnFailure(), options.getContinuationToken(), context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AccessControlChangeResult>> setAccessControlRecursiveWithResponse(
        String accessControlList, Consumer<Response<AccessControlChanges>> progressHandler,
        PathSetAccessControlRecursiveMode mode, Integer batchSize, Integer maxBatches, Boolean continueOnFailure,
        String continuationToken, Context context) {
        StorageImplUtils.assertNotNull("accessControlList", accessControlList);

        context = context == null ? Context.NONE : context;
        Context contextFinal = context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE);

        AtomicInteger directoriesSuccessfulCount = new AtomicInteger(0);
        AtomicInteger filesSuccessfulCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger batchesCount = new AtomicInteger(0);

        return this.dataLakeStorage.getPaths().setAccessControlRecursiveWithResponseAsync(mode, null,
            continuationToken, continueOnFailure, batchSize, accessControlList, null, contextFinal)
            .onErrorMap(e -> {
                if (e instanceof DataLakeStorageException) {
                    return logger.logExceptionAsError(ModelHelper.changeAclRequestFailed((DataLakeStorageException) e,
                        continuationToken));
                } else if (e instanceof Exception) {
                    return logger.logExceptionAsError(ModelHelper.changeAclFailed((Exception) e, continuationToken));
                }
                return e;
            })
            .flatMap(response -> setAccessControlRecursiveWithResponseHelper(response, maxBatches,
                directoriesSuccessfulCount, filesSuccessfulCount, failureCount, batchesCount, progressHandler,
                accessControlList, mode, batchSize, continueOnFailure, continuationToken, null, contextFinal));
    }

    Mono<Response<AccessControlChangeResult>> setAccessControlRecursiveWithResponseHelper(
        PathsSetAccessControlRecursiveResponse response, Integer maxBatches, AtomicInteger directoriesSuccessfulCount,
        AtomicInteger filesSuccessfulCount, AtomicInteger failureCount, AtomicInteger batchesCount,
        Consumer<Response<AccessControlChanges>> progressHandler, String accessControlStr,
        PathSetAccessControlRecursiveMode mode, Integer batchSize, Boolean continueOnFailure, String lastToken,
        List<AccessControlChangeFailure> batchFailures, Context context) {

        // We only enter the helper after making a service call, so increment the counter immediately.
        batchesCount.incrementAndGet();

        // Update counters
        directoriesSuccessfulCount.addAndGet(response.getValue().getDirectoriesSuccessful());
        filesSuccessfulCount.addAndGet(response.getValue().getFilesSuccessful());
        failureCount.addAndGet(response.getValue().getFailureCount());

        // Update first batch failures.
        if (failureCount.get() > 0 && batchFailures == null) {
            batchFailures = response.getValue().getFailedEntries()
                .stream()
                .map(aclFailedEntry -> new AccessControlChangeFailure()
                    .setDirectory(aclFailedEntry.getType().equals("DIRECTORY"))
                    .setName(aclFailedEntry.getName())
                    .setErrorMessage(aclFailedEntry.getErrorMessage())
                ).collect(Collectors.toList());
        }
        List<AccessControlChangeFailure> finalBatchFailures = batchFailures;

        /*
        Determine which token we should report/return/use next.
        If there was a token present on the response (still processing and either no errors or forceFlag set),
        use that one.
        If there were no failures or force flag set and still nothing present, we are at the end, so use that.
        If there were failures and no force flag set, use the last token (no token is returned in this case).
         */
        String newToken = response.getDeserializedHeaders().getXMsContinuation();
        String effectiveNextToken;
        if (newToken != null && !newToken.isEmpty()) {
            effectiveNextToken = newToken;
        } else {
            if (failureCount.get() == 0 || (continueOnFailure == null || continueOnFailure)) {
                effectiveNextToken = newToken;
            } else {
                effectiveNextToken = lastToken;
            }
        }

        // Report progress
        if (progressHandler != null) {
            AccessControlChanges changes = new AccessControlChanges();

            changes.setContinuationToken(effectiveNextToken);

            changes.setBatchFailures(
                response.getValue().getFailedEntries()
                    .stream()
                    .map(aclFailedEntry -> new AccessControlChangeFailure()
                        .setDirectory(aclFailedEntry.getType().equals("DIRECTORY"))
                        .setName(aclFailedEntry.getName())
                        .setErrorMessage(aclFailedEntry.getErrorMessage())
                    ).collect(Collectors.toList())
            );

            changes.setBatchCounters(new AccessControlChangeCounters()
                .setChangedDirectoriesCount(response.getValue().getDirectoriesSuccessful())
                .setChangedFilesCount(response.getValue().getFilesSuccessful())
                .setFailedChangesCount(response.getValue().getFailureCount()));

            changes.setAggregateCounters(new AccessControlChangeCounters()
                .setChangedDirectoriesCount(directoriesSuccessfulCount.get())
                .setChangedFilesCount(filesSuccessfulCount.get())
                .setFailedChangesCount(failureCount.get()));

            progressHandler.accept(
                new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), changes,
                    response.getDeserializedHeaders()));
        }

        /*
        Determine if we are finished either because there is no new continuation (failure or finished) token or we have
        hit maxBatches.
         */
        if ((newToken == null || newToken.isEmpty()) || (maxBatches != null && batchesCount.get() >= maxBatches)) {
            AccessControlChangeResult result = new AccessControlChangeResult()
                .setBatchFailures(batchFailures)
                .setContinuationToken(effectiveNextToken)
                .setCounters(new AccessControlChangeCounters()
                    .setChangedDirectoriesCount(directoriesSuccessfulCount.get())
                    .setChangedFilesCount(filesSuccessfulCount.get())
                    .setFailedChangesCount(failureCount.get()));

            return Mono.just(new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                result, response.getDeserializedHeaders()
            ));
        }

        // If we're not finished, issue another request
        return this.dataLakeStorage.getPaths().setAccessControlRecursiveWithResponseAsync(mode, null,
            effectiveNextToken, continueOnFailure, batchSize, accessControlStr, null, context)
            .onErrorMap(e -> {
                if (e instanceof DataLakeStorageException) {
                    return logger.logExceptionAsError(ModelHelper.changeAclRequestFailed((DataLakeStorageException) e,
                        effectiveNextToken));
                } else if (e instanceof Exception) {
                    return logger.logExceptionAsError(ModelHelper.changeAclFailed((Exception) e, effectiveNextToken));
                }
                return e;
            })
            .flatMap(response2 -> setAccessControlRecursiveWithResponseHelper(response2, maxBatches,
                directoriesSuccessfulCount, filesSuccessfulCount, failureCount, batchesCount, progressHandler,
                accessControlStr, mode, batchSize, continueOnFailure, effectiveNextToken, finalBatchFailures, context));
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControl}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @return A reactive response containing the resource access control.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathAccessControl> getAccessControl() {
        try {
            return getAccessControlWithResponse(false, null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions}
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @param userPrincipalNameReturned When true, user identity values returned as User Principal Names. When false,
     * user identity values returned as Azure Active Directory Object IDs. Default value is false.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the resource access control.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathAccessControl>> getAccessControlWithResponse(boolean userPrincipalNameReturned,
        DataLakeRequestConditions requestConditions) {
        try {
            return withContext(context -> getAccessControlWithResponse(userPrincipalNameReturned,
                requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PathAccessControl>> getAccessControlWithResponse(boolean userPrincipalNameReturned,
        DataLakeRequestConditions requestConditions, Context context) {
        requestConditions = requestConditions == null ? new DataLakeRequestConditions() : requestConditions;

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        context = context == null ? Context.NONE : context;
        return this.dataLakeStorage.getPaths().getPropertiesWithResponseAsync(null, null,
            PathGetPropertiesAction.GET_ACCESS_CONTROL, userPrincipalNameReturned, lac, mac,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, new PathAccessControl(
                PathAccessControlEntry.parseList(response.getDeserializedHeaders().getXMsAcl()),
                PathPermissions.parseSymbolic(response.getDeserializedHeaders().getXMsPermissions()),
                response.getDeserializedHeaders().getXMsGroup(), response.getDeserializedHeaders().getXMsOwner())));
    }

    /**
     * Package-private rename method for use by {@link DataLakeFileAsyncClient} and {@link DataLakeDirectoryAsyncClient}
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath The path of the destination relative to the file system name
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * DataLakePathAsyncClient} used to interact with the path created.
     */
    Mono<Response<DataLakePathAsyncClient>> renameWithResponse(String destinationFileSystem, String destinationPath,
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

        DataLakePathAsyncClient dataLakePathAsyncClient = getPathAsyncClient(destinationFileSystem, destinationPath);

        String renameSource = "/" + this.fileSystemName + "/" + Utility.urlEncode(pathName);

        return dataLakePathAsyncClient.dataLakeStorage.getPaths().createWithResponseAsync(
            null /* request id */, null /* timeout */, null /* pathResourceType */,
            null /* continuation */, PathRenameMode.LEGACY, renameSource, sourceRequestConditions.getLeaseId(),
            null /* metadata */, null /* permissions */, null /* umask */,
            null /* pathHttpHeaders */, destLac, destMac, sourceConditions,
            context.addData(AZ_TRACING_NAMESPACE_KEY, STORAGE_TRACING_NAMESPACE_VALUE))
            .map(response -> new SimpleResponse<>(response, dataLakePathAsyncClient));
    }

    /**
     * Takes in a destination and creates a DataLakePathAsyncClient with a new path
     * @param destinationFileSystem The destination file system
     * @param destinationPath The destination path
     * @return A DataLakePathAsyncClient
     */
    DataLakePathAsyncClient getPathAsyncClient(String destinationFileSystem, String destinationPath) {
        if (destinationFileSystem == null) {
            destinationFileSystem = getFileSystemName();
        }
        if (CoreUtils.isNullOrEmpty(destinationPath)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'destinationPath' can not be set to null"));
        }

        return new DataLakePathAsyncClient(getHttpPipeline(), getAccountUrl(), serviceVersion, accountName,
            destinationFileSystem, destinationPath, pathResourceType,
            prepareBuilderReplacePath(destinationFileSystem, destinationPath).buildBlockBlobAsyncClient());
    }

    /**
     * Takes in a destination path and creates a SpecializedBlobClientBuilder with a new path name
     * @param destinationFileSystem The destination file system
     * @param destinationPath The destination path
     * @return An updated SpecializedBlobClientBuilder
     */
    SpecializedBlobClientBuilder prepareBuilderReplacePath(String destinationFileSystem, String destinationPath) {
        if (destinationFileSystem == null) {
            destinationFileSystem = getFileSystemName();
        }
        // Get current Blob URL and replace current path with user provided path
        String newBlobEndpoint = BlobUrlParts.parse(DataLakeImplUtils.endpointToDesiredEndpoint(getPathUrl(),
            "blob", "dfs")).setBlobName(destinationPath).setContainerName(destinationFileSystem).toUrl().toString();

        return new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .endpoint(newBlobEndpoint)
            .serviceVersion(TransformUtils.toBlobServiceVersion(getServiceVersion()));
    }

    BlockBlobAsyncClient getBlockBlobAsyncClient() {
        return this.blockBlobAsyncClient;
    }

    /**
     * Generates a user delegation SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey) {
        return generateUserDelegationSas(dataLakeServiceSasSignatureValues, userDelegationKey, getAccountName(),
            Context.NONE);
    }

    /**
     * Generates a user delegation SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}.
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a user delegation SAS.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param userDelegationKey A {@link UserDelegationKey} object used to sign the SAS values.
     * See {@link DataLakeServiceAsyncClient#getUserDelegationKey(OffsetDateTime, OffsetDateTime)} for more information
     * on how to get a user delegation key.
     * @param accountName The account name.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateUserDelegationSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues,
        UserDelegationKey userDelegationKey, String accountName, Context context) {
        return new DataLakeSasImplUtil(dataLakeServiceSasSignatureValues, getFileSystemName(), getObjectPath(),
            PathResourceType.DIRECTORY.equals(this.pathResourceType))
            .generateUserDelegationSas(userDelegationKey, accountName, context);
    }

    /**
     * Generates a service SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues) {
        return generateSas(dataLakeServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the path using the specified {@link DataLakeServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link DataLakeServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context}
     *
     * @param dataLakeServiceSasSignatureValues {@link DataLakeServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(DataLakeServiceSasSignatureValues dataLakeServiceSasSignatureValues, Context context) {
        return new DataLakeSasImplUtil(dataLakeServiceSasSignatureValues, getFileSystemName(), getObjectPath(),
            PathResourceType.DIRECTORY.equals(this.pathResourceType))
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }
}
