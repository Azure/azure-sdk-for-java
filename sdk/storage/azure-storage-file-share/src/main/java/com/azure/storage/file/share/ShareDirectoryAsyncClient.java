// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.CopyFileSmbInfo;
import com.azure.storage.file.share.implementation.models.DestinationLeaseAccessConditions;
import com.azure.storage.file.share.models.FilePermissionFormat;
import com.azure.storage.file.share.implementation.models.ListFilesIncludeType;
import com.azure.storage.file.share.implementation.models.SourceLeaseAccessConditions;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.implementation.util.ShareSasImplUtil;
import com.azure.storage.file.share.models.CloseHandlesInfo;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileHttpHeaders;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareDirectorySetPropertiesOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;
import static com.azure.core.util.FluxUtil.withContext;


/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File
 * Service. Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving
 * properties, setting metadata and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Asynchronous Directory Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation -->
 * <pre>
 * ShareDirectoryAsyncClient client = new ShareFileClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildDirectoryAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.instantiation -->
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareDirectoryClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class, isAsync = true)
public class ShareDirectoryAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ShareDirectoryAsyncClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String directoryPath;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;

    /**
     * Creates a ShareDirectoryAsyncClient that sends requests to the storage directory at {@link
     * AzureFileStorageImpl#getUrl() endpoint}. Each service call goes through the {@link HttpPipeline pipeline} in the
     * {@code client}.
     *
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param directoryPath Name of the directory
     * @param snapshot The snapshot of the share
     */
    ShareDirectoryAsyncClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String directoryPath,
        String snapshot, String accountName, ShareServiceVersion serviceVersion, AzureSasCredential sasToken) {
        Objects.requireNonNull(shareName, "'shareName' cannot be null.");
        Objects.requireNonNull(directoryPath);
        this.shareName = shareName;
        this.directoryPath = directoryPath;
        this.snapshot = snapshot;
        this.azureFileStorageClient = azureFileStorageClient;
        this.accountName = accountName;
        this.serviceVersion = serviceVersion;
        this.sasToken = sasToken;
    }

    ShareDirectoryAsyncClient(ShareDirectoryAsyncClient directoryAsyncClient) {
        this(directoryAsyncClient.azureFileStorageClient, directoryAsyncClient.shareName,
            Utility.urlEncode(directoryAsyncClient.directoryPath), directoryAsyncClient.snapshot,
            directoryAsyncClient.accountName, directoryAsyncClient.serviceVersion, directoryAsyncClient.sasToken);
    }

    /**
     * Get the url of the storage directory client.
     *
     * @return the URL of the storage directory client
     */
    public String getDirectoryUrl() {
        StringBuilder directoryUrlString = new StringBuilder(azureFileStorageClient.getUrl()).append("/")
            .append(shareName).append("/").append(directoryPath);
        if (snapshot != null) {
            directoryUrlString.append("?sharesnapshot=").append(snapshot);
        }
        return directoryUrlString.toString();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Constructs a ShareFileAsyncClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in this directory {@link ShareFileAsyncClient#create(long)} create} in the client
     * will need to be called before interaction with the file can happen.</p>
     *
     * @param fileName Name of the file
     * @return a ShareFileAsyncClient that interacts with the specified share
     */
    public ShareFileAsyncClient getFileClient(String fileName) {
        String filePath = directoryPath + "/" + fileName;
        // Support for root directory
        if (directoryPath.isEmpty()) {
            filePath = fileName;
        }
        return new ShareFileAsyncClient(azureFileStorageClient, shareName, filePath, null, accountName,
            serviceVersion, sasToken);
    }

    /**
     * Constructs a ShareDirectoryAsyncClient that interacts with the specified directory.
     *
     * <p>If the file doesn't exist in this directory {@link ShareDirectoryAsyncClient#create()} create} in the client
     * will need to be called before interaction with the directory can happen.</p>
     *
     * @param subdirectoryName Name of the directory
     * @return a ShareDirectoryAsyncClient that interacts with the specified directory
     */
    public ShareDirectoryAsyncClient getSubdirectoryClient(String subdirectoryName) {
        StringBuilder directoryPathBuilder = new StringBuilder()
            .append(this.directoryPath);
        if (!this.directoryPath.isEmpty() && !this.directoryPath.endsWith("/")) {
            directoryPathBuilder.append("/");
        }
        directoryPathBuilder.append(subdirectoryName);
        return new ShareDirectoryAsyncClient(azureFileStorageClient, shareName, directoryPathBuilder.toString(),
            snapshot, accountName, serviceVersion, sasToken);
    }

    /**
     * Determines if the directory this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.exists -->
     * <pre>
     * client.exists&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.exists -->
     *
     * @return Flag indicating existence of the directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> exists() {
        return existsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Determines if the directory this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.existsWithResponse -->
     * <pre>
     * client.existsWithResponse&#40;&#41;.subscribe&#40;response -&gt; System.out.printf&#40;&quot;Exists? %b%n&quot;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.existsWithResponse -->
     *
     * @return Flag indicating existence of the directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> existsWithResponse() {
        try {
            return withContext(this::existsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> existsWithResponse(Context context) {
        return this.getPropertiesWithResponse(context)
            .map(cp -> (Response<Boolean>) new SimpleResponse<>(cp, true))
            .onErrorResume(this::checkDoesNotExistStatusCode,
                t -> {
                    HttpResponse response = ((ShareStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    private boolean checkDoesNotExistStatusCode(Throwable t) {
        return t instanceof ShareStorageException
            && ((ShareStorageException) t).getStatusCode() == 404
            && (((ShareStorageException) t).getErrorCode() == ShareErrorCode.RESOURCE_NOT_FOUND
            || ((ShareStorageException) t).getErrorCode() == ShareErrorCode.SHARE_NOT_FOUND);
    }

    /**
     * Creates this directory in the file share and returns a response of {@link ShareDirectoryInfo} to interact
     * with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.create -->
     * <pre>
     * shareDirectoryAsyncClient.create&#40;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed creating the directory!&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.create -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return The {@link ShareDirectoryInfo directory info}.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryInfo> create() {
        return createWithResponse(null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a directory in the file share and returns a response of ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#FileSmbProperties-String-Map -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;;
     * shareDirectoryAsyncClient.createWithResponse&#40;smbProperties, filePermission, metadata&#41;.subscribe&#40;
     *     response -&gt;
     *         System.out.println&#40;&quot;Completed creating the directory with status code:&quot; + response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#FileSmbProperties-String-Map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryInfo>> createWithResponse(FileSmbProperties smbProperties, String filePermission,
                                                                 Map<String, String> metadata) {
        try {
            return withContext(context -> createWithResponse(smbProperties, filePermission, null,
                metadata, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a directory in the file share and returns a response of ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#ShareDirectoryCreateOptions -->
     * <pre>
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;
     *         .setSmbProperties&#40;new FileSmbProperties&#40;&#41;&#41;
     *         .setFilePermission&#40;&quot;filePermission&quot;&#41;
     *         .setFilePermissionFormat&#40;FilePermissionFormat.BINARY&#41;
     *         .setMetadata&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;;
     *
     * shareDirectoryAsyncClient.createWithResponse&#40;options&#41;
     *         .subscribe&#40;response -&gt;
     *             System.out.println&#40;&quot;Completed creating the directory with status code:&quot; + response.getStatusCode&#40;&#41;&#41;,
     *             error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createWithResponse#ShareDirectoryCreateOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDirectoryCreateOptions}
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryInfo>> createWithResponse(ShareDirectoryCreateOptions options) {
        try {
            return withContext(context -> createWithResponse(options.getSmbProperties(), options.getFilePermission(),
                options.getFilePermissionFormat(), options.getMetadata(), context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryInfo>> createWithResponse(FileSmbProperties smbProperties, String filePermission,
        FilePermissionFormat filePermissionFormat, Map<String, String> metadata, Context context) {
        FileSmbProperties properties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        ModelHelper.validateFilePermissionAndKey(filePermission, properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = properties.setFilePermission(filePermission, FileConstants.FILE_PERMISSION_INHERIT);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.FILE_ATTRIBUTES_NONE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.FILE_TIME_NOW);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.FILE_TIME_NOW);
        String fileChangeTime = properties.getFileChangeTimeString();
        context = context == null ? Context.NONE : context;

        return azureFileStorageClient.getDirectories()
            .createWithResponseAsync(shareName, directoryPath, fileAttributes, null, metadata, filePermission,
                filePermissionFormat, filePermissionKey, fileCreationTime, fileLastWriteTime, fileChangeTime, context)
            .map(ModelHelper::mapShareDirectoryInfo);
    }

    /**
     * Creates this directory in the file share if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createIfNotExists -->
     * <pre>
     * shareDirectoryAsyncClient.createIfNotExists&#40;&#41;.subscribe&#40;response -&gt;
     *     System.out.printf&#40;&quot;Created at %s%n&quot;, response.getLastModified&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createIfNotExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return A reactive response signaling completion. {@link ShareDirectoryInfo} contains information about the
     * created directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryInfo> createIfNotExists() {
        try {
            return createIfNotExistsWithResponse(new ShareDirectoryCreateOptions(), null).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a directory in the file share if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createIfNotExistsWithResponse#ShareDirectoryCreateOptions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;.setMetadata&#40;metadata&#41;;
     *
     * shareDirectoryAsyncClient.createIfNotExistsWithResponse&#40;options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createIfNotExistsWithResponse#ShareDirectoryCreateOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDirectoryCreateOptions}
     * @return A {@link Mono} containing {@link Response} signaling completion, whose {@link Response#getValue() value}
     * contains a {@link ShareDirectoryInfo} containing information about the directory. If {@link Response}'s status
     * code is 201, a new directory was successfully created. If status code is 409, a directory already existed at this
     * location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryInfo>> createIfNotExistsWithResponse(ShareDirectoryCreateOptions options) {
        try {
            return createIfNotExistsWithResponse(options, null);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryInfo>> createIfNotExistsWithResponse(ShareDirectoryCreateOptions options, Context context) {
        try {
            options = options == null ? new ShareDirectoryCreateOptions() : options;
            return createWithResponse(options.getSmbProperties(), options.getFilePermission(),
                null, options.getMetadata(), context)
                .onErrorResume(t -> t instanceof ShareStorageException && ((ShareStorageException) t)
                    .getStatusCode() == 409, t -> {
                        HttpResponse response = ((ShareStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), null));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.delete -->
     * <pre>
     * shareDirectoryAsyncClient.delete&#40;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return An empty response.
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> delete() {
        return deleteWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the directory in the file share.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteWithResponse -->
     * <pre>
     * shareDirectoryAsyncClient.deleteWithResponse&#40;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteWithResponse() {
        try {
            return withContext(this::deleteWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getDirectories()
            .deleteNoCustomHeadersWithResponseAsync(shareName, directoryPath, null, context);
    }

    /**
     * Deletes the directory in the file share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteIfExists -->
     * <pre>
     * shareDirectoryAsyncClient.deleteIfExists&#40;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return a reactive response signaling completion. {@code true} indicates that the directory was successfully
     * deleted, {@code false} indicates that the directory did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteIfExists() {
        return deleteIfExistsWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the directory in the file share if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteIfExistsWithResponse -->
     * <pre>
     * shareDirectoryAsyncClient.deleteIfExistsWithResponse&#40;&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteIfExistsWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the directory was
     * successfully deleted. If status code is 404, the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteIfExistsWithResponse() {
        try {
            return withContext(this::deleteIfExistsWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteIfExistsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return deleteWithResponse(context)
            .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
            .onErrorResume(t -> t instanceof ShareStorageException && ((ShareStorageException) t).getStatusCode() == 404,
                t -> {
                    HttpResponse response = ((ShareStorageException) t).getResponse();
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                        response.getHeaders(), false));
                });
    }

    /**
     * Retrieves the properties of this directory. The properties include directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.getProperties -->
     * <pre>
     * shareDirectoryAsyncClient.getProperties&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.printf&#40;&quot;Directory latest modified date is %s.&quot;, properties.getLastModified&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return Storage directory properties
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryProperties> getProperties() {
        return getPropertiesWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Retrieves the properties of this directory. The properties include directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.getPropertiesWithResponse -->
     * <pre>
     * shareDirectoryAsyncClient.getPropertiesWithResponse&#40;&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.printf&#40;&quot;Directory latest modified date is %s:&quot;, properties.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.getPropertiesWithResponse -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return A response containing the storage directory properties with headers and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryProperties>> getPropertiesWithResponse() {
        try {
            return withContext(this::getPropertiesWithResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryProperties>> getPropertiesWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getDirectories()
            .getPropertiesWithResponseAsync(shareName, directoryPath, snapshot, null, context)
            .map(ModelHelper::mapShareDirectoryPropertiesResponse);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setProperties#FileSmbProperties-String -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * shareDirectoryAsyncClient.setProperties&#40;smbProperties, filePermission&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.printf&#40;&quot;Directory latest modified date is %s:&quot;, properties.getLastModified&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setProperties#FileSmbProperties-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return The storage directory SMB properties
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryInfo> setProperties(FileSmbProperties smbProperties, String filePermission) {
        return setPropertiesWithResponse(smbProperties, filePermission).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#FileSmbProperties-String -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * shareDirectoryAsyncClient.setPropertiesWithResponse&#40;smbProperties, filePermission&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.printf&#40;&quot;Directory latest modified date is %s:&quot;, properties.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#FileSmbProperties-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryInfo>> setPropertiesWithResponse(FileSmbProperties smbProperties,
                                                                        String filePermission) {
        try {
            return withContext(context -> setPropertiesWithResponse(smbProperties, filePermission, null,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#ShareDirectorySetPropertiesOptions -->
     * <pre>
     * ShareDirectorySetPropertiesOptions options = new ShareDirectorySetPropertiesOptions&#40;&#41;;
     * options.setSmbProperties&#40;new FileSmbProperties&#40;&#41;&#41;;
     * options.setFilePermissions&#40;new ShareFilePermission&#40;&#41;.setPermission&#40;&quot;filePermission&quot;&#41;
     *     .setPermissionFormat&#40;FilePermissionFormat.BINARY&#41;&#41;;
     * shareDirectoryAsyncClient.setPropertiesWithResponse&#40;options&#41;.subscribe&#40;properties -&gt; &#123;
     *     System.out.printf&#40;&quot;Directory latest modified date is %s:&quot;, properties.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setPropertiesWithResponse#ShareDirectorySetPropertiesOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDirectorySetPropertiesOptions}
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryInfo>> setPropertiesWithResponse(ShareDirectorySetPropertiesOptions options) {
        try {
            return withContext(context -> setPropertiesWithResponse(options.getSmbProperties(),
                options.getFilePermissions().getPermission(), options.getFilePermissions().getPermissionFormat(), context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryInfo>> setPropertiesWithResponse(FileSmbProperties smbProperties, String filePermission,
        FilePermissionFormat filePermissionFormat, Context context) {

        FileSmbProperties properties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        ModelHelper.validateFilePermissionAndKey(filePermission, properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        filePermission = properties.setFilePermission(filePermission, FileConstants.PRESERVE);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.PRESERVE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.PRESERVE);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.PRESERVE);
        String fileChangeTime = properties.getFileChangeTimeString();

        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getDirectories()
            .setPropertiesWithResponseAsync(shareName, directoryPath, fileAttributes, null, filePermission,
                filePermissionFormat, filePermissionKey, fileCreationTime, fileLastWriteTime, fileChangeTime, context)
            .map(ModelHelper::mapSetPropertiesResponse);
    }

    /**
     * Sets the user-defined metadata to associate to the directory.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the directory.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "directory:updatedMetadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map -->
     * <pre>
     * shareDirectoryAsyncClient.setMetadata&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;updatedMetadata&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Setting the directory metadata completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map -->
     *
     * <p>Clear the metadata of the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map.clearMetadata -->
     * <pre>
     * shareDirectoryAsyncClient.setMetadata&#40;null&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Clearing the directory metadata completed&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadata#map.clearMetadata -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return information about the directory
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectorySetMetadataInfo> setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata).flatMap(FluxUtil::toMono);
    }

    /**
     * Sets the user-defined metadata to associate to the directory.
     *
     * <p>If {@code null} is passed for the metadata it will clear the metadata associated to the directory.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the metadata to "directory:updatedMetadata"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map -->
     * <pre>
     * shareDirectoryAsyncClient.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;updatedMetadata&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Setting the directory metadata completed with status code:&quot;
     *         + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map -->
     *
     * <p>Clear the metadata of the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map.clearMetadata -->
     * <pre>
     * shareDirectoryAsyncClient.setMetadataWithResponse&#40;null&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Clearing the directory metadata completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.setMetadataWithResponse#map.clearMetadata -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return A response containing the information about the directory with headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectorySetMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata) {
        try {
            return withContext(context -> setMetadataWithResponse(metadata, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectorySetMetadataInfo>> setMetadataWithResponse(Map<String, String> metadata,
        Context context) {
        context = context == null ? Context.NONE : context;
        return azureFileStorageClient.getDirectories()
            .setMetadataWithResponseAsync(shareName, directoryPath, null, metadata, context)
            .map(ModelHelper::setShareDirectoryMetadataResponse);
    }

    /**
     * Lists all sub-directories and files in this directory without their prefix or maxResults in single page.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in the account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories -->
     * <pre>
     * shareDirectoryAsyncClient.listFilesAndDirectories&#40;&#41;.subscribe&#40;
     *     fileRef -&gt; System.out.printf&#40;&quot;Is the resource a directory? %b. The resource name is: %s.&quot;,
     *         fileRef.isDirectory&#40;&#41;, fileRef.getName&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed listing the directories and files.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @return {@link ShareFileItem File info} in the storage directory
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileItem> listFilesAndDirectories() {
        return listFilesAndDirectories(null, null);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories with "subdir" prefix and return 10 results in the account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#string-integer -->
     * <pre>
     * shareDirectoryAsyncClient.listFilesAndDirectories&#40;&quot;subdir&quot;, 10&#41;.subscribe&#40;
     *     fileRef -&gt; System.out.printf&#40;&quot;Is the resource a directory? %b. The resource name is: %s.&quot;,
     *         fileRef.isDirectory&#40;&#41;, fileRef.getName&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed listing the directories and files.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#string-integer -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins
     * with.
     * @param maxResultsPerPage Optional maximum number of files and/or directories to return per page. If the request
     * does not specify maxResultsPerPage or specifies a value greater than 5,000,
     * the server will return up to 5,000 items.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileItem> listFilesAndDirectories(String prefix, Integer maxResultsPerPage) {
        return listFilesAndDirectories(new ShareListFilesAndDirectoriesOptions().setPrefix(prefix)
            .setMaxResultsPerPage(maxResultsPerPage));
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories with "subdir" prefix and return 10 results in the account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions -->
     * <pre>
     * shareDirectoryAsyncClient.listFilesAndDirectories&#40;new ShareListFilesAndDirectoriesOptions&#40;&#41;
     *     .setPrefix&#40;&quot;subdir&quot;&#41;.setMaxResultsPerPage&#40;10&#41;&#41;
     *     .subscribe&#40;fileRef -&gt; System.out.printf&#40;&quot;Is the resource a directory? %b. The resource name is: %s.&quot;,
     *         fileRef.isDirectory&#40;&#41;, fileRef.getName&#40;&#41;&#41;,
     *         error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Completed listing the directories and files.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param options Optional parameters.
     * the server will return up to 5,000 items.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ShareFileItem> listFilesAndDirectories(ShareListFilesAndDirectoriesOptions options) {
        try {
            return listFilesAndDirectoriesWithOptionalTimeout(options, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<ShareFileItem> listFilesAndDirectoriesWithOptionalTimeout(
        ShareListFilesAndDirectoriesOptions options, Duration timeout, Context context) {
        final ShareListFilesAndDirectoriesOptions modifiedOptions = options == null
            ? new ShareListFilesAndDirectoriesOptions() : options;

        List<ListFilesIncludeType> includeTypes = new ArrayList<>();
        if (modifiedOptions.includeAttributes()) {
            includeTypes.add(ListFilesIncludeType.ATTRIBUTES);
        }
        if (modifiedOptions.includeETag()) {
            includeTypes.add(ListFilesIncludeType.ETAG);
        }
        if (modifiedOptions.includeTimestamps()) {
            includeTypes.add(ListFilesIncludeType.TIMESTAMPS);
        }
        if (modifiedOptions.includePermissionKey()) {
            includeTypes.add(ListFilesIncludeType.PERMISSION_KEY);
        }

        // these options must be absent from request if empty or false
        final List<ListFilesIncludeType> finalIncludeTypes = includeTypes.isEmpty() ? null : includeTypes;

        BiFunction<String, Integer, Mono<PagedResponse<ShareFileItem>>> retriever =
            (marker, pageSize) -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.getDirectories()
                .listFilesAndDirectoriesSegmentNoCustomHeadersWithResponseAsync(shareName, directoryPath,
                    modifiedOptions.getPrefix(), snapshot, marker,
                    pageSize == null ? modifiedOptions.getMaxResultsPerPage() : pageSize, null, finalIncludeTypes,
                    modifiedOptions.includeExtendedInfo(), context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), ModelHelper.convertResponseAndGetNumOfResults(response),
                    response.getValue().getNextMarker(), null));

        return new PagedFlux<>(pageSize -> retriever.apply(null, pageSize), retriever);
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.listHandles#integer-boolean -->
     * <pre>
     * shareDirectoryAsyncClient.listHandles&#40;10, true&#41;
     *     .subscribe&#40;handleItem -&gt; System.out.printf&#40;&quot;Get handles completed with handle id %s&quot;,
     *         handleItem.getHandleId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.listHandles#integer-boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResultPerPage Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its
     * subdirectories and their files.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<HandleItem> listHandles(Integer maxResultPerPage, boolean recursive) {
        try {
            return listHandlesWithOptionalTimeout(maxResultPerPage, recursive, null, Context.NONE);
        } catch (RuntimeException ex) {
            return pagedFluxError(LOGGER, ex);
        }
    }

    PagedFlux<HandleItem> listHandlesWithOptionalTimeout(Integer maxResultPerPage, boolean recursive, Duration timeout,
        Context context) {
        Function<String, Mono<PagedResponse<HandleItem>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.getDirectories()
                .listHandlesWithResponseAsync(shareName, directoryPath, marker, maxResultPerPage, null, snapshot,
                    recursive, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    ModelHelper.transformHandleItems(response.getValue().getHandleList()),
                    response.getValue().getNextMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Closes a handle on the directory. This is intended to be used alongside {@link #listHandles(Integer, boolean)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandle#String -->
     * <pre>
     * shareDirectoryAsyncClient.listHandles&#40;null, true&#41;.subscribe&#40;handleItem -&gt;
     *     shareDirectoryAsyncClient.forceCloseHandle&#40;handleItem.getHandleId&#40;&#41;&#41;.subscribe&#40;ignored -&gt;
     *         System.out.printf&#40;&quot;Closed handle %s on resource %s%n&quot;,
     *             handleItem.getHandleId&#40;&#41;, handleItem.getPath&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandle#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseHandlesInfo> forceCloseHandle(String handleId) {
        return forceCloseHandleWithResponse(handleId).flatMap(FluxUtil::toMono);
    }

    /**
     * Closes a handle on the directory. This is intended to be used alongside {@link #listHandles(Integer, boolean)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandleWithResponse#String -->
     * <pre>
     * shareDirectoryAsyncClient.listHandles&#40;null, true&#41;.subscribe&#40;handleItem -&gt;
     *     shareDirectoryAsyncClient.forceCloseHandleWithResponse&#40;handleItem.getHandleId&#40;&#41;&#41;.subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Closing handle %s on resource %s completed with status code %d%n&quot;,
     *             handleItem.getHandleId&#40;&#41;, handleItem.getPath&#40;&#41;, response.getStatusCode&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseHandleWithResponse#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return A response that contains information about the closed handles along with headers and response status
     * code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CloseHandlesInfo>> forceCloseHandleWithResponse(String handleId) {
        try {
            return withContext(context -> forceCloseHandleWithResponse(handleId, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<CloseHandlesInfo>> forceCloseHandleWithResponse(String handleId, Context context) {
        return this.azureFileStorageClient.getDirectories().forceCloseHandlesWithResponseAsync(shareName, directoryPath,
            handleId, null, null, snapshot, false, context)
            .map(response -> new SimpleResponse<>(response,
                new CloseHandlesInfo(response.getDeserializedHeaders().getXMsNumberOfHandlesClosed(),
                    response.getDeserializedHeaders().getXMsNumberOfHandlesFailed())));
    }

    /**
     * Closes all handles opened on the directory at the service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close all handles recursively.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseAllHandles#boolean -->
     * <pre>
     * shareDirectoryAsyncClient.forceCloseAllHandles&#40;true&#41;.subscribe&#40;closeHandlesInfo -&gt;
     *     System.out.printf&#40;&quot;Closed %d open handles on the directory%nFailed to close %d open handles on the &quot;
     *         + &quot;directory%n&quot;, closeHandlesInfo.getClosedHandles&#40;&#41;, closeHandlesInfo.getFailedHandles&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.forceCloseAllHandles#boolean -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param recursive Flag indicating if the operation should apply to all subdirectories and files contained in the
     * directory.
     * @return A response that contains information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CloseHandlesInfo> forceCloseAllHandles(boolean recursive) {
        try {
            return withContext(context -> forceCloseAllHandlesWithTimeout(recursive, null,
                context).reduce(new CloseHandlesInfo(0, 0),
                    (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles(),
                        accu.getFailedHandles() + next.getFailedHandles())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    PagedFlux<CloseHandlesInfo> forceCloseAllHandlesWithTimeout(boolean recursive, Duration timeout, Context context) {
        Function<String, Mono<PagedResponse<CloseHandlesInfo>>> retriever =
            marker -> StorageImplUtils.applyOptionalTimeout(this.azureFileStorageClient.getDirectories()
                .forceCloseHandlesWithResponseAsync(shareName, directoryPath, "*", null, marker, snapshot,
                    recursive, context), timeout)
                .map(response -> new PagedResponseBase<>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    Collections.singletonList(
                        new CloseHandlesInfo(response.getDeserializedHeaders().getXMsNumberOfHandlesClosed(),
                             response.getDeserializedHeaders().getXMsNumberOfHandlesFailed())),
                    response.getDeserializedHeaders().getXMsMarker(),
                    response.getDeserializedHeaders()));

        return new PagedFlux<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Moves the directory to another location within the share.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/rename-directory">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.rename#String -->
     * <pre>
     * ShareDirectoryAsyncClient renamedClient = client.rename&#40;destinationPath&#41;.block&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.rename#String -->
     *
     * @param destinationPath Relative path from the share to rename the directory to.
     * @return A {@link Mono} containing a {@link ShareDirectoryAsyncClient} used to interact with the new file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryAsyncClient> rename(String destinationPath) {
        return renameWithResponse(new ShareFileRenameOptions(destinationPath)).flatMap(FluxUtil::toMono);
    }

    /**
     * Moves the directory to another location within the share.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/rename-directory">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.renameWithResponse#ShareFileRenameOptions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * ShareFileRenameOptions options = new ShareFileRenameOptions&#40;destinationPath&#41;
     *     .setDestinationRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;
     *     .setSourceRequestConditions&#40;new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;&#41;
     *     .setIgnoreReadOnly&#40;false&#41;
     *     .setReplaceIfExists&#40;false&#41;
     *     .setFilePermission&#40;&quot;filePermission&quot;&#41;
     *     .setSmbProperties&#40;smbProperties&#41;;
     *
     * ShareDirectoryAsyncClient newRenamedClient = client.renameWithResponse&#40;options&#41;.block&#40;&#41;.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.renameWithResponse#ShareFileRenameOptions -->
     *
     * @param options {@link ShareFileRenameOptions}
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a {@link
     * ShareDirectoryAsyncClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryAsyncClient>> renameWithResponse(ShareFileRenameOptions options) {
        try {
            return withContext(context -> renameWithResponse(options, context))
                .map(response -> new SimpleResponse<>(response, new ShareDirectoryAsyncClient(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> renameWithResponse(ShareFileRenameOptions options, Context context) {
        StorageImplUtils.assertNotNull("options", options);
        context = context == null ? Context.NONE : context;

        ShareRequestConditions sourceRequestConditions = options.getSourceRequestConditions() == null
            ? new ShareRequestConditions() : options.getSourceRequestConditions();
        ShareRequestConditions destinationRequestConditions = options.getDestinationRequestConditions() == null
            ? new ShareRequestConditions() : options.getDestinationRequestConditions();

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceLeaseAccessConditions sourceConditions = new SourceLeaseAccessConditions()
            .setSourceLeaseId(sourceRequestConditions.getLeaseId());
        DestinationLeaseAccessConditions destinationConditions = new DestinationLeaseAccessConditions()
            .setDestinationLeaseId(destinationRequestConditions.getLeaseId());

        CopyFileSmbInfo smbInfo = null;
        String filePermissionKey = null;
        if (options.getSmbProperties() != null) {
            FileSmbProperties tempSmbProperties = options.getSmbProperties();
            filePermissionKey = tempSmbProperties.getFilePermissionKey();

            String fileAttributes = NtfsFileAttributes.toString(tempSmbProperties.getNtfsFileAttributes());
            String fileCreationTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileCreationTime());
            String fileLastWriteTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileLastWriteTime());
            String fileChangeTime = FileSmbProperties.parseFileSMBDate(tempSmbProperties.getFileChangeTime());
            smbInfo = new CopyFileSmbInfo()
                .setFileAttributes(fileAttributes)
                .setFileCreationTime(fileCreationTime)
                .setFileLastWriteTime(fileLastWriteTime)
                .setFileChangeTime(fileChangeTime)
                .setIgnoreReadOnly(options.isIgnoreReadOnly());
        }

        ShareDirectoryAsyncClient destinationDirectoryClient =
            getDirectoryAsyncClient(options.getDestinationPath());

        String renameSource = this.getDirectoryUrl();

        renameSource = this.sasToken != null ? renameSource + "?" + this.sasToken.getSignature() : renameSource;

        return destinationDirectoryClient.azureFileStorageClient.getDirectories().renameWithResponseAsync(
            destinationDirectoryClient.getShareName(), destinationDirectoryClient.getDirectoryPath(), renameSource,
            null /* timeout */, options.getReplaceIfExists(), options.isIgnoreReadOnly(),
            options.getFilePermission(), options.getFilePermissionFormat(), filePermissionKey, options.getMetadata(),
            sourceConditions, destinationConditions, smbInfo, context)
            .map(response -> new SimpleResponse<>(response, destinationDirectoryClient));
    }

    /**
     * Takes in a destination and creates a ShareFileAsyncClient with a new path
     * @param destinationPath The destination path
     * @return A DataLakePathAsyncClient
     */
    ShareDirectoryAsyncClient getDirectoryAsyncClient(String destinationPath) {
        if (CoreUtils.isNullOrEmpty(destinationPath)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'destinationPath' can not be set to null"));
        }

        return new ShareDirectoryAsyncClient(this.azureFileStorageClient, getShareName(), destinationPath, null,
            this.getAccountName(), this.getServiceVersion(), sasToken);
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of
     * ShareDirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectory#string -->
     * <pre>
     * shareDirectoryAsyncClient.createSubdirectory&#40;&quot;subdir&quot;&#41;
     *     .doOnSuccess&#40;response -&gt; System.out.println&#40;&quot;Completed creating the subdirectory.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectory#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A subdirectory client.
     * @throws ShareStorageException If the subdirectory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryAsyncClient> createSubdirectory(String subdirectoryName) {
        return createSubdirectoryWithResponse(subdirectoryName, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of
     * ShareDirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;;
     * shareDirectoryAsyncClient.createSubdirectoryWithResponse&#40;&quot;subdir&quot;, smbProperties, filePermission, metadata&#41;.subscribe&#40;
     *     response -&gt;
     *         System.out.println&#40;&quot;Successfully creating the subdirectory with status code: &quot;
     *             + response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the subdirectory
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * subdirectory is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata) {
        try {
            return withContext(context ->
                createSubdirectoryWithResponse(subdirectoryName, smbProperties, filePermission, metadata, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> createSubdirectoryWithResponse(String subdirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Context context) {
        ShareDirectoryAsyncClient createSubClient = getSubdirectoryClient(subdirectoryName);
        return createSubClient.createWithResponse(smbProperties, filePermission, null, metadata, context)
            .map(response -> new SimpleResponse<>(response, createSubClient));
    }

    /**
     * Creates a subdirectory under current directory with specific name if it does not exist and returns a response of
     * ShareDirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryIfNotExists#string -->
     * <pre>
     * shareDirectoryAsyncClient.createSubdirectoryIfNotExists&#40;&quot;subdir&quot;&#41;
     *     .switchIfEmpty&#40;Mono.&lt;ShareDirectoryAsyncClient&gt;empty&#40;&#41;
     *         .doOnSuccess&#40;x -&gt; System.out.println&#40;&quot;Already exists.&quot;&#41;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Create completed.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryIfNotExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A {@link Mono} containing a {@link ShareDirectoryAsyncClient} used to interact with the subdirectory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareDirectoryAsyncClient> createSubdirectoryIfNotExists(String subdirectoryName) {
        return createSubdirectoryIfNotExistsWithResponse(subdirectoryName, new ShareDirectoryCreateOptions())
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a subdirectory under current directory with specific name and metadata if it does not exist,
     * and returns a response of ShareDirectoryAsyncClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;.setMetadata&#40;metadata&#41;;
     *
     * shareDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse&#40;&quot;subdir&quot;, options&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *         System.out.println&#40;&quot;Already exists.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully created.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createSubdirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param options {@link ShareDirectoryCreateOptions}
     * @return A reactive response signaling completion. The presence of a {@link Response} item indicates a new
     * subdirectory was created, and {@link Response#getValue() value} contains a {@link ShareDirectoryAsyncClient}
     * which can be used to interact with the newly created directory. An empty {@code Mono} indicates the specified
     * subdirectory already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareDirectoryAsyncClient>> createSubdirectoryIfNotExistsWithResponse(String subdirectoryName,
        ShareDirectoryCreateOptions options) {
        try {
            return withContext(
                context -> createSubdirectoryIfNotExistsWithResponse(subdirectoryName, options, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareDirectoryAsyncClient>> createSubdirectoryIfNotExistsWithResponse(String subdirectoryName,
        ShareDirectoryCreateOptions options, Context context) {
        try {
            options = options == null ? new ShareDirectoryCreateOptions() : options;
            ShareDirectoryAsyncClient createSubClient = getSubdirectoryClient(subdirectoryName);
            return createSubClient.createIfNotExistsWithResponse(options, context)
                .map(response -> new SimpleResponse<>(response, createSubClient));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the subdirectory with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectory#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteSubdirectory&#40;&quot;mysubdirectory&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed deleting the subdirectory.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectory#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return An empty response.
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteSubdirectory(String subdirectoryName) {
        return deleteSubdirectoryWithResponse(subdirectoryName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the subdirectory with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryWithResponse#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteSubdirectoryWithResponse&#40;&quot;mysubdirectory&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete subdirectory completed with status code %d&quot;,
     *         response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed deleting the subdirectory.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryWithResponse#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteSubdirectoryWithResponse(String subdirectoryName) {
        try {
            return withContext(context -> deleteSubdirectoryWithResponse(subdirectoryName, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteSubdirectoryWithResponse(String subdirectoryName, Context context) {
        ShareDirectoryAsyncClient deleteSubClient = getSubdirectoryClient(subdirectoryName);
        return deleteSubClient.deleteWithResponse(context);
    }

    /**
     * Deletes the subdirectory with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryIfExists#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteSubdirectoryIfExists&#40;&quot;mysubdirectory&quot;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return a reactive response signaling completion. {@code true} indicates that the subdirectory was successfully
     * deleted, {@code false} indicates that the subdirectory did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteSubdirectoryIfExists(String subdirectoryName) {
        return deleteSubdirectoryIfExistsWithResponse(subdirectoryName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the subdirectory with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse&#40;&quot;mysubdirectory&quot;&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteSubdirectoryIfExistsWithResponse#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the subdirectory was
     * successfully deleted. If status code is 404, the subdirectory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteSubdirectoryIfExistsWithResponse(String subdirectoryName) {
        try {
            return withContext(context -> deleteSubdirectoryIfExistsWithResponse(subdirectoryName,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteSubdirectoryIfExistsWithResponse(String subdirectoryName, Context context) {
        try {
            return getSubdirectoryClient(subdirectoryName).deleteIfExistsWithResponse(context);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of
     * ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createFile#string-long -->
     * <pre>
     * shareDirectoryAsyncClient.createFile&#40;&quot;myfile&quot;, 1024&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed creating the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createFile#string-long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Size of the file
     * @return The ShareFileAsyncClient.
     * @throws ShareStorageException If the parent directory does not exist or file name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ShareFileAsyncClient> createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     * shareDirectoryAsyncClient.createFileWithResponse&#40;&quot;myFile&quot;, 1024, httpHeaders, smbProperties, filePermission,
     *     Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Completed creating the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the parent directory does not exist or file name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata) {
        return this.createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
            null);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions -->
     * <pre>
     * ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders&#40;&#41;
     *     .setContentType&#40;&quot;text&#47;html&quot;&#41;
     *     .setContentEncoding&#40;&quot;gzip&quot;&#41;
     *     .setContentLanguage&#40;&quot;en&quot;&#41;
     *     .setCacheControl&#40;&quot;no-transform&quot;&#41;
     *     .setContentDisposition&#40;&quot;attachment&quot;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;
     *     .setNtfsFileAttributes&#40;EnumSet.of&#40;NtfsFileAttributes.READ_ONLY&#41;&#41;
     *     .setFileCreationTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFileLastWriteTime&#40;OffsetDateTime.now&#40;&#41;&#41;
     *     .setFilePermissionKey&#40;&quot;filePermissionKey&quot;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * &#47;&#47; NOTE: filePermission and filePermissionKey should never be both set
     *
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * shareDirectoryAsyncClient.createFileWithResponse&#40;&quot;myFile&quot;, 1024, httpHeaders, smbProperties, filePermission,
     *     Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;, requestConditions&#41;.subscribe&#40;
     *         response -&gt; System.out.printf&#40;&quot;Creating the file completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *         error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Completed creating the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.createFileWithResponse#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission The file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the parent directory does not exist or file name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions) {
        try {
            return withContext(context ->
                createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
                    requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<ShareFileAsyncClient>> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions, Context context) {
        ShareFileAsyncClient shareFileAsyncClient = getFileClient(fileName);
        return shareFileAsyncClient
            .createWithResponse(maxSize, httpHeaders, smbProperties, filePermission, null, metadata, requestConditions,
                context).map(response -> new SimpleResponse<>(response, shareFileAsyncClient));
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFile#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteFile&#40;&quot;myfile&quot;&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *     &#125;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFile#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return An empty response.
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteFile(String fileName) {
        return deleteFileWithResponse(fileName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteFileWithResponse&#40;&quot;myfile&quot;&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete file completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteFileWithResponse(String fileName) {
        return this.deleteFileWithResponse(fileName, null);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * shareDirectoryAsyncClient.deleteFileWithResponse&#40;&quot;myfile&quot;, requestConditions&#41;.subscribe&#40;
     *     response -&gt; System.out.printf&#40;&quot;Delete file completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;,
     *     error -&gt; System.err.println&#40;error.toString&#40;&#41;&#41;,
     *     &#40;&#41; -&gt; System.out.println&#40;&quot;Completed deleting the file.&quot;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileWithResponse#string-ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteFileWithResponse(fileName, requestConditions, context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Void>> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions,
        Context context) {
        ShareFileAsyncClient shareFileAsyncClient = getFileClient(fileName);
        return shareFileAsyncClient.deleteWithResponse(requestConditions, context);
    }

    /**
     * Deletes the file with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileIfExists#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteFileIfExists&#40;&quot;myfile&quot;&#41;.subscribe&#40;deleted -&gt; &#123;
     *     if &#40;deleted&#41; &#123;
     *         System.out.println&#40;&quot;Successfully deleted.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return a reactive response signaling completion. {@code true} indicates that the file was successfully
     * deleted, {@code false} indicates that the file did not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the file with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileIfExistsWithResponse#string -->
     * <pre>
     * shareDirectoryAsyncClient.deleteFileIfExistsWithResponse&#40;&quot;myfile&quot;&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileIfExistsWithResponse#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName) {
        try {
            return this.deleteFileIfExistsWithResponse(fileName, null);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Deletes the file with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileIfExistsWithResponse#string-ShareRequestConditions -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * shareDirectoryAsyncClient.deleteFileIfExistsWithResponse&#40;&quot;myfile&quot;, requestConditions&#41;.subscribe&#40;response -&gt; &#123;
     *     if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *         System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     *     &#125; else &#123;
     *         System.out.println&#40;&quot;successfully deleted.&quot;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.deleteFileIfExistsWithResponse#string-ShareRequestConditions -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @return A reactive response signaling completion. If {@link Response}'s status code is 202, the file was
     * successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, ShareRequestConditions requestConditions) {
        try {
            return withContext(context -> deleteFileIfExistsWithResponse(fileName, requestConditions,
                context));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    Mono<Response<Boolean>> deleteFileIfExistsWithResponse(String fileName, ShareRequestConditions requestConditions, Context context) {
        try {
            requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
            return deleteFileWithResponse(fileName, requestConditions, context)
                .map(response -> (Response<Boolean>) new SimpleResponse<>(response, true))
                .onErrorResume(t -> t instanceof ShareStorageException && ((ShareStorageException) t)
                    .getStatusCode() == 404,
                    t -> {
                        HttpResponse response = ((ShareStorageException) t).getResponse();
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                            response.getHeaders(), false));
                    });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    /**
     * Get snapshot id which attached to {@link ShareDirectoryAsyncClient}. Return {@code null} if no snapshot id
     * attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareSnapshotId -->
     * <pre>
     * OffsetDateTime currentTime = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * ShareDirectoryAsyncClient shareDirectoryAsyncClient = new ShareFileClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net&quot;&#41;
     *     .sasToken&#40;&quot;$&#123;SASToken&#125;&quot;&#41;
     *     .shareName&#40;&quot;myshare&quot;&#41;
     *     .resourcePath&#40;&quot;mydirectory&quot;&#41;
     *     .snapshot&#40;currentTime.toString&#40;&#41;&#41;
     *     .buildDirectoryAsyncClient&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Snapshot ID: %s%n&quot;, shareDirectoryAsyncClient.getShareSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareSnapshotId -->
     *
     * @return The snapshot id which is a unique {@code DateTime} value that identifies the share snapshot to its base
     * share.
     */
    public String getShareSnapshotId() {
        return this.snapshot;
    }

    /**
     * Get the share name of directory client.
     *
     * <p>Get the share name. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareName -->
     * <pre>
     * String shareName = shareDirectoryAsyncClient.getShareName&#40;&#41;;
     * System.out.println&#40;&quot;The share name of the directory is &quot; + shareName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.getShareName -->
     *
     * @return The share name of the directory.
     */
    public String getShareName() {
        return shareName;
    }

    /**
     * Get directory path of the client.
     *
     * <p>Get directory path. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.getDirectoryPath -->
     * <pre>
     * String directoryPath = shareDirectoryAsyncClient.getDirectoryPath&#40;&#41;;
     * System.out.println&#40;&quot;The name of the directory is &quot; + directoryPath&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.getDirectoryPath -->
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return directoryPath;
    }


    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.accountName;
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return azureFileStorageClient.getHttpPipeline();
    }

    /**
     * Generates a service SAS for the directory using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareFileSasPermission permission = new ShareFileSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * shareDirectoryAsyncClient.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues -->
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues) {
        return generateSas(shareServiceSasSignatureValues, Context.NONE);
    }

    /**
     * Generates a service SAS for the directory using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareFileSasPermission permission = new ShareFileSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * shareDirectoryAsyncClient.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryAsyncClient.generateSas#ShareServiceSasSignatureValues-Context -->
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return generateSas(shareServiceSasSignatureValues, null, context);
    }

    /**
     * Generates a service SAS for the directory using the specified {@link ShareServiceSasSignatureValues}
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link ShareServiceSasSignatureValues} for more information on how to construct a service SAS.</p>
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param stringToSignHandler For debugging purposes only. Returns the string to sign that was used to generate the
     * signature.
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues,
        Consumer<String> stringToSignHandler, Context context) {
        return new ShareSasImplUtil(shareServiceSasSignatureValues, getShareName(), getDirectoryPath())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), stringToSignHandler, context);
    }
}
