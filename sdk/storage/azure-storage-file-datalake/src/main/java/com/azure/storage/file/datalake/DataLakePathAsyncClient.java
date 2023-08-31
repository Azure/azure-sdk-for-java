// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
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
import com.azure.storage.file.datalake.implementation.models.PathExpiryOptions;
import com.azure.storage.file.datalake.implementation.models.PathGetPropertiesAction;
import com.azure.storage.file.datalake.implementation.models.PathRenameMode;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.models.PathSetAccessControlRecursiveMode;
import com.azure.storage.file.datalake.implementation.models.PathsSetAccessControlRecursiveHeaders;
import com.azure.storage.file.datalake.implementation.models.SetAccessControlRecursiveResponse;
import com.azure.storage.file.datalake.implementation.models.SourceModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.DataLakeSasImplUtil;
import com.azure.storage.file.datalake.implementation.util.ModelHelper;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.AccessControlChangeCounters;
import com.azure.storage.file.datalake.models.AccessControlChangeFailure;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessControlChanges;
import com.azure.storage.file.datalake.implementation.models.CpkInfo;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
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
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
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

/**
 * This class provides a client that contains all operations that apply to any path object.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class, isAsync = true)
public class DataLakePathAsyncClient {

    private static final ClientLogger LOGGER = new ClientLogger(DataLakePathAsyncClient.class);

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
    private final CpkInfo customerProvidedKey;

    final PathResourceType pathResourceType;

    final BlockBlobAsyncClient blockBlobAsyncClient;

    private final AzureSasCredential sasToken;

    private final boolean isTokenCredentialAuthenticated;

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
        BlockBlobAsyncClient blockBlobAsyncClient, AzureSasCredential sasToken,
        CpkInfo customerProvidedKey, boolean isTokenCredentialAuthenticated) {
        this.accountName = accountName;
        this.fileSystemName = fileSystemName;
        this.pathName = Utility.urlDecode(pathName);
        this.pathResourceType = pathResourceType;
        this.blockBlobAsyncClient = blockBlobAsyncClient;
        this.sasToken = sasToken;
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

        this.customerProvidedKey = customerProvidedKey;
        this.isTokenCredentialAuthenticated = isTokenCredentialAuthenticated;
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

    AzureSasCredential getSasToken() {
        return this.sasToken;
    }

    /**
     * Gets the {@link CpkInfo} used to encrypt this path's content on the server.
     *
     * @return the customer provided key used for encryption.
     */
    public CustomerProvidedKey getCustomerProvidedKey() {
        return new CustomerProvidedKey(customerProvidedKey.getEncryptionKey());
    }

    CpkInfo getCpkInfo() {
        return this.customerProvidedKey;
    }

    boolean isTokenCredentialAuthenticated() {
        return this.isTokenCredentialAuthenticated;
    }

    /**
     * Creates a new {@link DataLakePathAsyncClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the path,
     * pass {@code null} to use no customer provided key.
     * @return a {@link DataLakePathAsyncClient} with the specified {@code customerProvidedKey}.
     */
    public DataLakePathAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new DataLakePathAsyncClient(getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(),
            getFileSystemName(), getObjectPath(), this.pathResourceType, this.blockBlobAsyncClient, getSasToken(),
            finalCustomerProvidedKey, isTokenCredentialAuthenticated());
    }

    /**
     * Creates a resource. By default, this method will not overwrite an existing path.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.create -->
     * <pre>
     * client.create&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Last Modified Time:%s&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.create -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response containing information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> create() {
        return create(false);
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.create#boolean -->
     * <pre>
     * boolean overwrite = true;
     * client.create&#40;overwrite&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Last Modified Time:%s&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.create#boolean -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param overwrite Whether to overwrite, should data exist on the file.
     *
     * @return A reactive response containing information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> create(boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createWithResponse(new DataLakePathCreateOptions().setRequestConditions(requestConditions)).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     *
     * client.createWithResponse&#40;permissions, umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;,
     *     requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Last Modified Time:%s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#String-String-PathHttpHeaders-Map-DataLakeRequestConditions -->
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
            DataLakePathCreateOptions options = new DataLakePathCreateOptions()
                .setPermissions(permissions)
                .setUmask(umask)
                .setPathHttpHeaders(headers).setMetadata(metadata)
                .setRequestConditions(requestConditions);
            return withContext(context -> createWithResponse(options));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#DataLakePathCreateOptions -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * String owner = &quot;rwx&quot;;
     * String group = &quot;r--&quot;;
     * String leaseId = UUID.randomUUID&#40;&#41;.toString&#40;&#41;;
     * Integer duration = 15;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setOwner&#40;owner&#41;
     *     .setGroup&#40;group&#41;
     *     .setPathHttpHeaders&#40;httpHeaders&#41;
     *     .setRequestConditions&#40;requestConditions&#41;
     *     .setMetadata&#40;metadata&#41;
     *     .setProposedLeaseId&#40;leaseId&#41;
     *     .setLeaseDuration&#40;duration&#41;;
     *
     * client.createWithResponse&#40;options&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Last Modified Time:%s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.createWithResponse#DataLakePathCreateOptions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * PathItem}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> createWithResponse(DataLakePathCreateOptions options) {
        try {
            return withContext(context -> createWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<PathInfo>> createWithResponse(DataLakePathCreateOptions options, Context context) {
        options = options == null ? new DataLakePathCreateOptions() : options;
        DataLakeRequestConditions requestConditions = options.getRequestConditions() == null ? new DataLakeRequestConditions() : options.getRequestConditions();

        LeaseAccessConditions lac = new LeaseAccessConditions().setLeaseId(requestConditions.getLeaseId());
        ModifiedAccessConditions mac = new ModifiedAccessConditions()
            .setIfMatch(requestConditions.getIfMatch())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince());

        String acl = options.getAccessControlList() != null ? PathAccessControlEntry
            .serializeList(options.getAccessControlList()) : null;
        PathExpiryOptions expiryOptions = setFieldsIfNull(options);

        String expiresOnString = null; // maybe return string instead and do check for expiryOptions in here
        if (options.getScheduleDeletionOptions() != null && options.getScheduleDeletionOptions().getExpiresOn() != null) {
            expiresOnString = DateTimeRfc1123.toRfc1123String(options.getScheduleDeletionOptions().getExpiresOn());
        } else if (options.getScheduleDeletionOptions() != null && options.getScheduleDeletionOptions().getTimeToExpire() != null) {
            expiresOnString = Long.toString(options.getScheduleDeletionOptions().getTimeToExpire().toMillis());
        }

        Long leaseDuration = options.getLeaseDuration() != null ? Long.valueOf(options.getLeaseDuration()) : null;

        context = context == null ? Context.NONE : context;
        return this.dataLakeStorage.getPaths().createWithResponseAsync(null, null, pathResourceType, null, null, null,
                options.getSourceLeaseId(), buildMetadataString(options.getMetadata()), options.getPermissions(),
                options.getUmask(), options.getOwner(), options.getGroup(), acl, options.getProposedLeaseId(),
                leaseDuration, expiryOptions, expiresOnString, options.getEncryptionContext(), options.getPathHttpHeaders(),
                lac, mac, null, customerProvidedKey, context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders().getETag(),
                response.getDeserializedHeaders().getLastModified(),
                response.getDeserializedHeaders().isXMsRequestServerEncrypted() != null,
                response.getDeserializedHeaders().getXMsEncryptionKeySha256())));
    }

    PathExpiryOptions setFieldsIfNull(DataLakePathCreateOptions options) {
        if (pathResourceType == PathResourceType.DIRECTORY) {
            if (options.getProposedLeaseId() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("ProposedLeaseId does not apply to directories."));
            }
            if (options.getLeaseDuration() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("LeaseDuration does not apply to directories."));
            }
            if (options.getScheduleDeletionOptions() != null && options.getScheduleDeletionOptions().getTimeToExpire() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("TimeToExpire does not apply to directories."));
            }
            if (options.getScheduleDeletionOptions() != null && options.getScheduleDeletionOptions().getExpiresOn() != null) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("ExpiresOn does not apply to directories."));
            }
        }
        if (options.getScheduleDeletionOptions() == null) {
            return null;
        }
        if (options.getScheduleDeletionOptions().getTimeToExpire() != null && options.getScheduleDeletionOptions().getExpiresOn() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("TimeToExpire and ExpiresOn both cannot be set."));
        }
        if (options.getScheduleDeletionOptions().getTimeToExpire() != null) {
            return PathExpiryOptions.RELATIVE_TO_NOW;
        } else if (options.getScheduleDeletionOptions().getExpiresOn() != null) {
            return PathExpiryOptions.ABSOLUTE;
        }
        return null;
    }

    /**
     * Creates a resource if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.createIfNotExists -->
     * <pre>
     * client.createIfNotExists&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Created at %s%n&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.createIfNotExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return A reactive response signaling completion. {@link PathInfo} contains information about the created resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathInfo> createIfNotExists() {
        return createIfNotExistsWithResponse(new DataLakePathCreateOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a resource if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.createIfNotExistsWithResponse#DataLakePathCreateOptions -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setPathHttpHeaders&#40;headers&#41;
     *     .setMetadata&#40;metadata&#41;;
     *
     * client.createIfNotExistsWithResponse&#40;options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.createIfNotExistsWithResponse#DataLakePathCreateOptions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathCreateOptions}
     * @return A {@link Mono} containing {@link Response} signaling completion, whose {@link Response#getValue() value}
     * contains a {@link PathInfo} containing information about the resource. If {@link Response}'s status code is
     * 201, a new resource was successfully created. If status code is 409, a resource already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathInfo>> createIfNotExistsWithResponse(DataLakePathCreateOptions options) {
        try {
            return withContext(context -> createIfNotExistsWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<PathInfo>> createIfNotExistsWithResponse(DataLakePathCreateOptions options, Context context) {
        try {
            options = options == null ? new DataLakePathCreateOptions() : options;
            options.setRequestConditions(new DataLakeRequestConditions()
                .setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD));
            return createWithResponse(options, context)
                .onErrorResume(t -> t instanceof DataLakeStorageException
                        && ((DataLakeStorageException) t).getStatusCode() == 409,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Package-private delete method for use by {@link DataLakeFileAsyncClient} and {@link DataLakeDirectoryAsyncClient}
     *
     * @param recursive Whether to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Mono} containing status code and HTTP headers
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

        // Pagination only applies to service version 2023-08-03 and later, when using OAuth.
        Boolean paginated = (getServiceVersion().ordinal() >= DataLakeServiceVersion.V2023_08_03.ordinal()
            && Boolean.TRUE.equals(recursive) // only applies to directories
            && isTokenCredentialAuthenticated()) ? true : null;

        Context finalContext = context == null ? Context.NONE : context;
        return this.dataLakeStorage.getPaths()
            .deleteWithResponseAsync(null, null, recursive, null, paginated, lac, mac, context).expand(resp -> {
                String continuation = resp.getHeaders().getValue(Transforms.X_MS_CONTINUATION);
                if (continuation != null && !continuation.isEmpty()) {
                    return this.dataLakeStorage.getPaths()
                        .deleteWithResponseAsync(null, null, recursive, continuation, paginated, lac, mac, finalContext);
                } else {
                    return Mono.empty();
                }
            }).last().map(res -> new SimpleResponse<>(res, null));
    }

    /**
     * Deletes paths under the resource if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.deleteIfExists -->
     * <pre>
     * client.deleteIfExists&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete completed%n&quot;&#41;,
     *     error -&gt; System.out.printf&#40;&quot;Delete failed: %s%n&quot;, error&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.deleteIfExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a></p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the resource under the path was
     * successfully deleted, {@code false} indicates the resource did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions()).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes all paths under the specified resource if exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * client.deleteIfExistsWithResponse&#40;options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathDeleteOptions}
     *
     * @return A reactive response signaling completion. If {@link Response}'s status code is 200, the resource was
     * successfully deleted. If status code is 404, the resource does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse(DataLakePathDeleteOptions options) {
        try {
            return withContext(context -> deleteIfExistsWithResponse(options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteIfExistsWithResponse(DataLakePathDeleteOptions options, Context context) {
        try {
            options = options == null ? new DataLakePathDeleteOptions() : options;
            return deleteWithResponse(options.getIsRecursive(), options.getRequestConditions(), context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof DataLakeStorageException
                        && ((DataLakeStorageException) t).getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((DataLakeStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map -->
     * <pre>
     * client.setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Set metadata completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map -->
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
        return setMetadataWithResponse(metadata, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes a resource's metadata. The specified metadata in this method will replace existing metadata. If old
     * values must be preserved, they must be downloaded and included in the call to this method.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * client.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Set metadata completed with status %d%n&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setMetadata#Map-DataLakeRequestConditions -->
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
        return this.blockBlobAsyncClient.setMetadataWithResponse(metadata,
            Transforms.toBlobRequestConditions(requestConditions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Changes a resource's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeaders#PathHttpHeaders -->
     * <pre>
     * client.setHttpHeaders&#40;new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeaders#PathHttpHeaders -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-properties">Azure Docs</a></p>
     *
     * @param headers {@link PathHttpHeaders}
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> setHttpHeaders(PathHttpHeaders headers) {
        return setHttpHeadersWithResponse(headers, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes a resource's HTTP header properties. If only one HTTP header is updated, the others will all be erased.
     * In order to preserve existing values, they must be passed alongside the header being changed.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * client.setHttpHeadersWithResponse&#40;new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;, requestConditions&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Set HTTP headers completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setHttpHeadersWithResponse#PathHttpHeaders-DataLakeRequestConditions -->
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
        return this.blockBlobAsyncClient.setHttpHeadersWithResponse(Transforms.toBlobHttpHeaders(headers),
                Transforms.toBlobRequestConditions(requestConditions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.getProperties -->
     * <pre>
     * client.getProperties&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Creation Time: %s, Size: %d%n&quot;, response.getCreationTime&#40;&#41;, response.getFileSize&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return A reactive response containing the resource's properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathProperties> getProperties() {
        return getPropertiesWithResponse(null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.getPropertiesWithResponse#DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * client.getPropertiesWithResponse&#40;requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Creation Time: %s, Size: %d%n&quot;, response.getValue&#40;&#41;.getCreationTime&#40;&#41;,
     *         response.getValue&#40;&#41;.getFileSize&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.getPropertiesWithResponse#DataLakeRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @return A reactive response containing the resource's properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PathProperties>> getPropertiesWithResponse(DataLakeRequestConditions requestConditions) {
        return blockBlobAsyncClient.getPropertiesWithResponse(Transforms.toBlobRequestConditions(requestConditions))
            .onErrorMap(DataLakeImplUtils::transformBlobStorageException)
            .map(response -> new SimpleResponse<>(response, Transforms.toPathProperties(response.getValue(), response)));
    }

    /**
     * Determines if the path this client represents exists in the cloud.
     * <p>Note that this method does not guarantee that the path type (file/directory) matches expectations.</p>
     * <p>For example, a DataLakeFileClient representing a path to a datalake directory will return true, and vice
     * versa.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.exists -->
     * <pre>
     * client.exists&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.exists -->
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Determines if the path this client represents exists in the cloud.
     * <p>Note that this method does not guarantee that the path type (file/directory) matches expectations.</p>
     * <p>For example, a DataLakeFileClient representing a path to a datalake directory will return true, and vice
     * versa.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.existsWithResponse -->
     * <pre>
     * client.existsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.existsWithResponse -->
     *
     * @return true if the path exists, false if it doesn't
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        return blockBlobAsyncClient.existsWithResponse().onErrorMap(DataLakeImplUtils::transformBlobStorageException);
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlList#List-String-String -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * client.setAccessControlList&#40;pathAccessControlEntries, group, owner&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Last Modified Time: %s&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlList#List-String-String -->
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
        return setAccessControlListWithResponse(accessControlList, group, owner, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes the access control list, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * client.setAccessControlListWithResponse&#40;pathAccessControlEntries, group, owner, requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Last Modified Time: %s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlListWithResponse#List-String-String-DataLakeRequestConditions -->
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
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissions#PathPermissions-String-String -->
     * <pre>
     * PathPermissions permissions = new PathPermissions&#40;&#41;
     *     .setGroup&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;&#41;
     *     .setOwner&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;.setWritePermission&#40;true&#41;&#41;
     *     .setOther&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * client.setPermissions&#40;permissions, group, owner&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Last Modified Time: %s&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissions#PathPermissions-String-String -->
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
        return setPermissionsWithResponse(permissions, group, owner, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Changes the permissions, group and/or owner for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * PathPermissions permissions = new PathPermissions&#40;&#41;
     *     .setGroup&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;&#41;
     *     .setOwner&#40;new RolePermissions&#40;&#41;.setExecutePermission&#40;true&#41;.setReadPermission&#40;true&#41;.setWritePermission&#40;true&#41;&#41;
     *     .setOther&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * String group = &quot;group&quot;;
     * String owner = &quot;owner&quot;;
     *
     * client.setPermissionsWithResponse&#40;permissions, group, owner, requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Last Modified Time: %s&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setPermissionsWithResponse#PathPermissions-String-String-DataLakeRequestConditions -->
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
            return monoError(LOGGER, ex);
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
            accessControlListString, null, lac, mac, context)
            .map(response -> new SimpleResponse<>(response, new PathInfo(response.getDeserializedHeaders().getETag(),
                response.getDeserializedHeaders().getLastModified())));
    }

    /**
     * Recursively sets the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursive#List -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * client.setAccessControlRecursive&#40;pathAccessControlEntries&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *         response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursive#List -->
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
        return setAccessControlRecursiveWithResponse(new PathSetAccessControlRecursiveOptions(accessControlList))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Recursively sets the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * Integer batchSize = 2;
     * Integer maxBatches = 10;
     * boolean continueOnFailure = false;
     * String continuationToken = null;
     * Consumer&lt;Response&lt;AccessControlChanges&gt;&gt; progressHandler =
     *     response -&gt; System.out.println&#40;&quot;Received response&quot;&#41;;
     *
     * PathSetAccessControlRecursiveOptions options =
     *     new PathSetAccessControlRecursiveOptions&#40;pathAccessControlEntries&#41;
     *         .setBatchSize&#40;batchSize&#41;
     *         .setMaxBatches&#40;maxBatches&#41;
     *         .setContinueOnFailure&#40;continueOnFailure&#41;
     *         .setContinuationToken&#40;continuationToken&#41;
     *         .setProgressHandler&#40;progressHandler&#41;;
     *
     * client.setAccessControlRecursive&#40;pathAccessControlEntries&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *         response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.setAccessControlRecursiveWithResponse#PathSetAccessControlRecursiveOptions -->
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
        try {
            StorageImplUtils.assertNotNull("options", options);
            return withContext(context -> setAccessControlRecursiveWithResponse(
                PathAccessControlEntry.serializeList(options.getAccessControlList()), options.getProgressHandler(),
                PathSetAccessControlRecursiveMode.SET, options.getBatchSize(), options.getMaxBatches(),
                options.isContinueOnFailure(), options.getContinuationToken(), context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Recursively updates the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursive#List -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * client.updateAccessControlRecursive&#40;pathAccessControlEntries&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *         response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursive#List -->
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
        return updateAccessControlRecursiveWithResponse(new PathUpdateAccessControlRecursiveOptions(accessControlList))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Recursively updates the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions -->
     * <pre>
     * PathAccessControlEntry pathAccessControlEntry = new PathAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;
     *     .setPermissions&#40;new RolePermissions&#40;&#41;.setReadPermission&#40;true&#41;&#41;;
     * List&lt;PathAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * Integer batchSize = 2;
     * Integer maxBatches = 10;
     * boolean continueOnFailure = false;
     * String continuationToken = null;
     * Consumer&lt;Response&lt;AccessControlChanges&gt;&gt; progressHandler =
     *     response -&gt; System.out.println&#40;&quot;Received response&quot;&#41;;
     *
     * PathUpdateAccessControlRecursiveOptions options =
     *     new PathUpdateAccessControlRecursiveOptions&#40;pathAccessControlEntries&#41;
     *         .setBatchSize&#40;batchSize&#41;
     *         .setMaxBatches&#40;maxBatches&#41;
     *         .setContinueOnFailure&#40;continueOnFailure&#41;
     *         .setContinuationToken&#40;continuationToken&#41;
     *         .setProgressHandler&#40;progressHandler&#41;;
     *
     * client.updateAccessControlRecursive&#40;pathAccessControlEntries&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *         response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.updateAccessControlRecursiveWithResponse#PathUpdateAccessControlRecursiveOptions -->
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
        try {
            StorageImplUtils.assertNotNull("options", options);
            return withContext(context -> setAccessControlRecursiveWithResponse(
                PathAccessControlEntry.serializeList(options.getAccessControlList()), options.getProgressHandler(),
                PathSetAccessControlRecursiveMode.MODIFY, options.getBatchSize(), options.getMaxBatches(),
                options.isContinueOnFailure(), options.getContinuationToken(), context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Recursively removes the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursive#List -->
     * <pre>
     * PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;;
     * List&lt;PathRemoveAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * client.removeAccessControlRecursive&#40;pathAccessControlEntries&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *         response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursive#List -->
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
        return removeAccessControlRecursiveWithResponse(new PathRemoveAccessControlRecursiveOptions(accessControlList))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Recursively removes the access control on a path and all subpaths.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions -->
     * <pre>
     * PathRemoveAccessControlEntry pathAccessControlEntry = new PathRemoveAccessControlEntry&#40;&#41;
     *     .setEntityId&#40;&quot;entityId&quot;&#41;;
     * List&lt;PathRemoveAccessControlEntry&gt; pathAccessControlEntries = new ArrayList&lt;&gt;&#40;&#41;;
     * pathAccessControlEntries.add&#40;pathAccessControlEntry&#41;;
     *
     * Integer batchSize = 2;
     * Integer maxBatches = 10;
     * boolean continueOnFailure = false;
     * String continuationToken = null;
     * Consumer&lt;Response&lt;AccessControlChanges&gt;&gt; progressHandler =
     *     response -&gt; System.out.println&#40;&quot;Received response&quot;&#41;;
     *
     * PathRemoveAccessControlRecursiveOptions options =
     *     new PathRemoveAccessControlRecursiveOptions&#40;pathAccessControlEntries&#41;
     *         .setBatchSize&#40;batchSize&#41;
     *         .setMaxBatches&#40;maxBatches&#41;
     *         .setContinueOnFailure&#40;continueOnFailure&#41;
     *         .setContinuationToken&#40;continuationToken&#41;
     *         .setProgressHandler&#40;progressHandler&#41;;
     *
     * client.removeAccessControlRecursive&#40;pathAccessControlEntries&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Successful changed file operations: %d&quot;,
     *         response.getCounters&#40;&#41;.getChangedFilesCount&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.removeAccessControlRecursiveWithResponse#PathRemoveAccessControlRecursiveOptions -->
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
        try {
            StorageImplUtils.assertNotNull("options", options);
            return withContext(context -> setAccessControlRecursiveWithResponse(
                PathRemoveAccessControlEntry.serializeList(options.getAccessControlList()),
                options.getProgressHandler(), PathSetAccessControlRecursiveMode.REMOVE, options.getBatchSize(),
                options.getMaxBatches(), options.isContinueOnFailure(), options.getContinuationToken(), context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<AccessControlChangeResult>> setAccessControlRecursiveWithResponse(
        String accessControlList, Consumer<Response<AccessControlChanges>> progressHandler,
        PathSetAccessControlRecursiveMode mode, Integer batchSize, Integer maxBatches, Boolean continueOnFailure,
        String continuationToken, Context context) {
        StorageImplUtils.assertNotNull("accessControlList", accessControlList);

        context = context == null ? Context.NONE : context;
        Context contextFinal = context;

        AtomicInteger directoriesSuccessfulCount = new AtomicInteger(0);
        AtomicInteger filesSuccessfulCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger batchesCount = new AtomicInteger(0);

        return this.dataLakeStorage.getPaths().setAccessControlRecursiveWithResponseAsync(mode, null,
            continuationToken, continueOnFailure, batchSize, accessControlList, null, contextFinal)
            .onErrorMap(e -> {
                if (e instanceof DataLakeStorageException) {
                    return LOGGER.logExceptionAsError(ModelHelper.changeAclRequestFailed((DataLakeStorageException) e,
                        continuationToken));
                } else if (e instanceof Exception) {
                    return LOGGER.logExceptionAsError(ModelHelper.changeAclFailed((Exception) e, continuationToken));
                }
                return e;
            })
            .flatMap(response -> setAccessControlRecursiveWithResponseHelper(response, maxBatches,
                directoriesSuccessfulCount, filesSuccessfulCount, failureCount, batchesCount, progressHandler,
                accessControlList, mode, batchSize, continueOnFailure, continuationToken, null, contextFinal));
    }

    Mono<Response<AccessControlChangeResult>> setAccessControlRecursiveWithResponseHelper(
        ResponseBase<PathsSetAccessControlRecursiveHeaders, SetAccessControlRecursiveResponse> response,
        Integer maxBatches, AtomicInteger directoriesSuccessfulCount, AtomicInteger filesSuccessfulCount,
        AtomicInteger failureCount, AtomicInteger batchesCount,
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
                    return LOGGER.logExceptionAsError(ModelHelper.changeAclRequestFailed((DataLakeStorageException) e,
                        effectiveNextToken));
                } else if (e instanceof Exception) {
                    return LOGGER.logExceptionAsError(ModelHelper.changeAclFailed((Exception) e, effectiveNextToken));
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControl -->
     * <pre>
     * client.getAccessControl&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Access Control List: %s, Group: %s, Owner: %s, Permissions: %s&quot;,
     *         PathAccessControlEntry.serializeList&#40;response.getAccessControlList&#40;&#41;&#41;, response.getGroup&#40;&#41;,
     *         response.getOwner&#40;&#41;, response.getPermissions&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControl -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/getproperties">Azure Docs</a></p>
     *
     * @return A reactive response containing the resource access control.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PathAccessControl> getAccessControl() {
        return getAccessControlWithResponse(false, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Returns the access control for a resource.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * boolean userPrincipalNameReturned = false;
     *
     * client.getAccessControlWithResponse&#40;userPrincipalNameReturned, requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Access Control List: %s, Group: %s, Owner: %s, Permissions: %s&quot;,
     *         PathAccessControlEntry.serializeList&#40;response.getValue&#40;&#41;.getAccessControlList&#40;&#41;&#41;,
     *         response.getValue&#40;&#41;.getGroup&#40;&#41;, response.getValue&#40;&#41;.getOwner&#40;&#41;, response.getValue&#40;&#41;.getPermissions&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.getAccessControlWithResponse#boolean-DataLakeRequestConditions -->
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
            return monoError(LOGGER, ex);
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
            PathGetPropertiesAction.GET_ACCESS_CONTROL, userPrincipalNameReturned, lac, mac, context)
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
        context = context == null ? Context.NONE : context;

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

        String signature = null;
        if (this.sasToken != null) {
            if (this.sasToken.getSignature().startsWith("?")) {
                signature = this.sasToken.getSignature().substring(1);
            } else {
                signature = this.sasToken.getSignature();
            }
        }

        renameSource = signature != null ? renameSource + "?" + signature : renameSource;

        return dataLakePathAsyncClient.dataLakeStorage.getPaths().createWithResponseAsync(
                null /* request id */, null /* timeout */, null /* pathResourceType */,
                null /* continuation */, PathRenameMode.LEGACY, renameSource, sourceRequestConditions.getLeaseId(),
                null /* properties */, null /* permissions */, null /* umask */, null /* owner */,
                null /* group */, null /* acl */, null /* proposedLeaseId */, null /* leaseDuration */,
                null /* expiryOptions */, null /* expiresOn */, null /* encryptionContext */,
                null /* pathHttpHeaders */, destLac, destMac, sourceConditions, null /* cpkInfo */,
                context)
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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'destinationPath' can not be set to null"));
        }

        return new DataLakePathAsyncClient(getHttpPipeline(), getAccountUrl(), serviceVersion, accountName,
            destinationFileSystem, destinationPath, pathResourceType,
            prepareBuilderReplacePath(destinationFileSystem, destinationPath).buildBlockBlobAsyncClient(), sasToken,
            customerProvidedKey, isTokenCredentialAuthenticated());
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission myPermission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey -->
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context -->
     * <pre>
     * OffsetDateTime myExpiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission myPermission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues myValues = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateUserDelegationSas&#40;values, userDelegationKey, accountName, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.generateUserDelegationSas#DataLakeServiceSasSignatureValues-UserDelegationKey-String-Context -->
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission permission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * client.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * PathSasPermission permission = new PathSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * DataLakeServiceSasSignatureValues values = new DataLakeServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * client.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakePathAsyncClient.generateSas#DataLakeServiceSasSignatureValues-Context -->
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
