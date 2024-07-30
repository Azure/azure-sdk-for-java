// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.implementation.models.CopyFileSmbInfo;
import com.azure.storage.file.share.implementation.models.DestinationLeaseAccessConditions;
import com.azure.storage.file.share.implementation.models.DirectoriesCreateHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesForceCloseHandlesHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesListHandlesHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesSetMetadataHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesSetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.ListFilesAndDirectoriesSegmentResponse;
import com.azure.storage.file.share.implementation.models.ListFilesIncludeType;
import com.azure.storage.file.share.implementation.models.ListHandlesResponse;
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
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareDirectoryCreateOptions;
import com.azure.storage.file.share.options.ShareFileRenameOptions;
import com.azure.storage.file.share.options.ShareListFilesAndDirectoriesOptions;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.storage.common.implementation.StorageImplUtils.sendRequest;

/**
 * This class provides a client that contains all the operations for interacting with directory in Azure Storage File
 * Service. Operations allowed by the client are creating, deleting and listing subdirectory and file, retrieving
 * properties, setting metadata and list or force close handles of the directory or file.
 *
 * <p><strong>Instantiating an Synchronous Directory Client</strong></p>
 *
 * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.instantiation -->
 * <pre>
 * ShareDirectoryClient client = new ShareFileClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;$&#123;connectionString&#125;&quot;&#41;
 *     .endpoint&#40;&quot;$&#123;endpoint&#125;&quot;&#41;
 *     .buildDirectoryClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.file.share.ShareDirectoryClient.instantiation -->
 *
 * <p>View {@link ShareFileClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ShareFileClientBuilder
 * @see ShareDirectoryClient
 * @see StorageSharedKeyCredential
 */
@ServiceClient(builder = ShareFileClientBuilder.class)
public class ShareDirectoryClient {

    private static final ClientLogger LOGGER = new ClientLogger(ShareDirectoryClient.class);

    private final AzureFileStorageImpl azureFileStorageClient;
    private final String shareName;
    private final String directoryPath;
    private final String snapshot;
    private final String accountName;
    private final ShareServiceVersion serviceVersion;
    private final AzureSasCredential sasToken;
    private final String directoryUrl;

    /**
     * Creates a ShareDirectoryClient.
     * @param azureFileStorageClient Client that interacts with the service interfaces
     * @param shareName Name of the share
     * @param directoryPath Name of the directory
     * @param snapshot The snapshot of the share
     * @param accountName Name of the account
     * @param serviceVersion The version of the service to be used when making requests.
     * @param sasToken The SAS token used to authenticate the request
     */
    ShareDirectoryClient(AzureFileStorageImpl azureFileStorageClient, String shareName, String directoryPath,
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

        StringBuilder directoryUrlString = new StringBuilder(azureFileStorageClient.getUrl()).append("/")
            .append(shareName).append("/").append(directoryPath);
        if (snapshot != null) {
            directoryUrlString.append("?sharesnapshot=").append(snapshot);
        }
        this.directoryUrl = directoryUrlString.toString();
    }

    /**
     * Get the url of the storage directory client.
     *
     * @return the URL of the storage directory client.
     */
    public String getDirectoryUrl() {
        return this.directoryUrl;
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public ShareServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Constructs a ShareFileClient that interacts with the specified file.
     *
     * <p>If the file doesn't exist in this directory {@link ShareFileClient#create(long)} create} in the client will
     * need to be called before interaction with the file can happen.</p>
     *
     * @param fileName Name of the file
     * @return a ShareFileClient that interacts with the specified share
     */
    public ShareFileClient getFileClient(String fileName) {
        String filePath = directoryPath + "/" + fileName;
        // Support for root directory
        if (directoryPath.isEmpty()) {
            filePath = fileName;
        }
        return new ShareFileClient(
            new ShareFileAsyncClient(azureFileStorageClient, shareName, filePath, null, accountName, serviceVersion, sasToken),
            azureFileStorageClient, shareName, filePath, null, accountName, serviceVersion, sasToken);
    }

    /**
     * Constructs a ShareDirectoryClient that interacts with the specified directory.
     *
     * <p>If the file doesn't exist in this directory {@link ShareDirectoryClient#create()} create} in the client will
     * need to be called before interaction with the directory can happen.</p>
     *
     * @param subdirectoryName Name of the directory
     * @return a ShareDirectoryClient that interacts with the specified directory
     */
    public ShareDirectoryClient getSubdirectoryClient(String subdirectoryName) {
        boolean needPathDelimiter = !this.directoryPath.isEmpty() && !this.directoryPath.endsWith("/");
        String subDirectoryPath = this.directoryPath + (needPathDelimiter ? "/" : "") + subdirectoryName;
        return new ShareDirectoryClient(azureFileStorageClient, shareName, subDirectoryPath, snapshot, accountName,
            serviceVersion, sasToken);
    }

    /**
     * Determines if the directory this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.exists -->
     * <pre>
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.exists&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.exists -->
     *
     * @return Flag indicating existence of the directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Boolean exists() {
        return existsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Determines if the directory this client represents exists in the cloud.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.existsWithResponse#Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * System.out.printf&#40;&quot;Exists? %b%n&quot;, client.existsWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.existsWithResponse#Duration-Context -->
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Flag indicating existence of the directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> existsWithResponse(Duration timeout, Context context) {
        try {
            Response<ShareDirectoryProperties> response = getPropertiesWithResponse(timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (RuntimeException e) {
            if (ModelHelper.checkDoesNotExistStatusCode(e) && e instanceof HttpResponseException) {
                HttpResponse response = ((HttpResponseException) e).getResponse();
                return new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                    response.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Creates a directory in the file share and returns a response of {@link ShareDirectoryInfo} to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createDirectory -->
     * <pre>
     * shareDirectoryClient.create&#40;&#41;;
     * System.out.println&#40;&quot;Completed creating the directory. &quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createDirectory -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return The {@link ShareDirectoryInfo directory info}.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryInfo create() {
        return createWithResponse(null, null, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a directory in the file share and returns a response of ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#FileSmbProperties-String-Map-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Response&lt;ShareDirectoryInfo&gt; response = shareDirectoryClient.createWithResponse&#40;smbProperties, filePermission,
     *     Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed creating the directory with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#FileSmbProperties-String-Map-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryInfo> createWithResponse(FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Duration timeout, Context context) {
        return createWithResponse(new ShareDirectoryCreateOptions()
            .setSmbProperties(smbProperties)
            .setFilePermission(filePermission)
            .setMetadata(metadata), timeout, context);
    }

    /**
     * Creates a directory in the file share and returns a response of ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#ShareDirectoryCreateOptions-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;.setMetadata&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;;
     * Response&lt;ShareDirectoryInfo&gt; response = shareDirectoryClient.createWithResponse&#40;options, Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed creating the directory with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createWithResponse#ShareDirectoryCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDirectoryCreateOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * directory name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryInfo> createWithResponse(ShareDirectoryCreateOptions options, Duration timeout,
        Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        ShareDirectoryCreateOptions finalOptions = options == null ? new ShareDirectoryCreateOptions() : options;
        FileSmbProperties properties = finalOptions.getSmbProperties() == null ? new FileSmbProperties()
            : finalOptions.getSmbProperties();

        // Checks that file permission and file permission key are valid
        ModelHelper.validateFilePermissionAndKey(finalOptions.getFilePermission(), properties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        String finalFilePermission = properties.setFilePermission(finalOptions.getFilePermission(),
            FileConstants.FILE_PERMISSION_INHERIT);
        String filePermissionKey = properties.getFilePermissionKey();

        String fileAttributes = properties.setNtfsFileAttributes(FileConstants.FILE_ATTRIBUTES_NONE);
        String fileCreationTime = properties.setFileCreationTime(FileConstants.FILE_TIME_NOW);
        String fileLastWriteTime = properties.setFileLastWriteTime(FileConstants.FILE_TIME_NOW);
        String fileChangeTime = properties.getFileChangeTimeString();

        Callable<ResponseBase<DirectoriesCreateHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getDirectories()
                .createWithResponse(shareName, directoryPath, fileAttributes, null, finalOptions.getMetadata(),
                    finalFilePermission,
                    filePermissionKey, fileCreationTime, fileLastWriteTime, fileChangeTime, finalContext);

        return ModelHelper.mapShareDirectoryInfo(sendRequest(operation, timeout, ShareStorageException.class));
    }

    /**
     * Creates a directory in the file share if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createIfNotExists -->
     * <pre>
     * ShareDirectoryClient shareDirectoryClient = createClientWithSASToken&#40;&#41;;
     * ShareDirectoryInfo shareDirectoryInfo = shareDirectoryClient.createIfNotExists&#40;&#41;;
     * System.out.printf&#40;&quot;Last Modified Time:%s&quot;, shareDirectoryInfo.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createIfNotExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @return A {@link ShareDirectoryInfo} that contains information about the created directory.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryInfo createIfNotExists() {
        return createIfNotExistsWithResponse(new ShareDirectoryCreateOptions(), null, null).getValue();
    }

    /**
     * Creates a directory in the file share if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createIfNotExistsWithResponse#ShareDirectoryCreateOptions-Duration-Context -->
     * <pre>
     * ShareDirectoryClient directoryClient = createClientWithSASToken&#40;&#41;;
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;.setMetadata&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;;
     *
     * Response&lt;ShareDirectoryInfo&gt; response = directoryClient.createIfNotExistsWithResponse&#40;options,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     *
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createIfNotExistsWithResponse#ShareDirectoryCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param options {@link ShareDirectoryCreateOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A reactive {@link Response} signaling completion, whose {@link Response#getValue() value} contains a
     * {@link ShareDirectoryInfo} containing information about the directory. If {@link Response}'s status code is 201,
     * a new directory was successfully created. If status code is 409, a directory already existed at this location.
     * */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryInfo> createIfNotExistsWithResponse(ShareDirectoryCreateOptions options,
        Duration timeout, Context context) {
        try {
            return createWithResponse(options, timeout, context);
        } catch (ShareStorageException e) {
            if (e.getStatusCode() == 409 && e.getErrorCode().equals(ShareErrorCode.RESOURCE_ALREADY_EXISTS)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), null);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Deletes the directory in the file share. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.delete -->
     * <pre>
     * shareDirectoryClient.delete&#40;&#41;;
     * System.out.println&#40;&quot;Completed deleting the file.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.delete -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @throws ShareStorageException If the share doesn't exist
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(null, Context.NONE);
    }

    /**
     * Deletes the directory in the file share. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteWithResponse#duration-context -->
     * <pre>
     * Response&lt;Void&gt; response = shareDirectoryClient.deleteWithResponse&#40;Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed deleting the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteWithResponse#duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the share doesn't exist
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<Response<Void>> operation = () -> this.azureFileStorageClient.getDirectories()
            .deleteNoCustomHeadersWithResponse(shareName, directoryPath, null, finalContext);

        return sendRequest(operation, timeout, ShareStorageException.class);
    }

    /**
     * Deletes the directory in the file share if it exists. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteIfExists -->
     * <pre>
     * ShareDirectoryClient shareDirectoryClient = createClientWithSASToken&#40;&#41;;
     * boolean result = shareDirectoryClient.deleteIfExists&#40;&#41;;
     * System.out.println&#40;&quot;Directory deleted: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteIfExists -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     * @return {@code true} if the directory is successfully deleted, {@code false} if the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Deletes the directory in the file share if it exists. The directory must be empty before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteIfExistsWithResponse#duration-context -->
     * <pre>
     * Response&lt;Boolean&gt; response = shareDirectoryClient.deleteIfExistsWithResponse&#40;Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteIfExistsWithResponse#duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the directory
     * was successfully deleted. If status code is 404, the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(Duration timeout, Context context) {
        try {
            Response<Void> response = this.deleteWithResponse(timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (ShareStorageException e) {
            if (e.getStatusCode() == 404 && e.getErrorCode().equals(ShareErrorCode.RESOURCE_NOT_FOUND)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.getProperties -->
     * <pre>
     * ShareDirectoryProperties response = shareDirectoryClient.getProperties&#40;&#41;;
     * System.out.printf&#40;&quot;Directory latest modified date is %s.&quot;, response.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @return Storage directory properties
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Retrieves the properties of this directory. The properties includes directory metadata, last modified date, is
     * server encrypted, and eTag.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.getPropertiesWithResponse#duration-Context -->
     * <pre>
     * Response&lt;ShareDirectoryProperties&gt; response = shareDirectoryClient.getPropertiesWithResponse&#40;
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Directory latest modified date is %s.&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.getPropertiesWithResponse#duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-directory-properties">Azure Docs</a>.</p>
     *
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the storage directory properties with response status code and headers
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<DirectoriesGetPropertiesHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getDirectories().getPropertiesWithResponse(shareName, directoryPath,
                snapshot, null, finalContext);

        return ModelHelper.mapShareDirectoryPropertiesResponse(sendRequest(operation, timeout,
            ShareStorageException.class));
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.setProperties#FileSmbProperties-String -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * ShareDirectoryInfo response = shareDirectoryClient.setProperties&#40;smbProperties, filePermission&#41;;
     * System.out.printf&#40;&quot;Directory latest modified date is %s.&quot;, response.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.setProperties#FileSmbProperties-String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @return The storage directory SMB properties
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryInfo setProperties(FileSmbProperties smbProperties, String filePermission) {
        return setPropertiesWithResponse(smbProperties, filePermission, null, Context.NONE).getValue();
    }

    /**
     * Sets the properties of this directory. The properties include the file SMB properties and the file permission.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set directory properties</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.setPropertiesWithResponse#FileSmbProperties-String-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Response&lt;ShareDirectoryInfo&gt; response = shareDirectoryClient.setPropertiesWithResponse&#40;smbProperties, filePermission,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Directory latest modified date is %s.&quot;, response.getValue&#40;&#41;.getLastModified&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.setPropertiesWithResponse#FileSmbProperties-String-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-properties">Azure Docs</a>.</p>
     *
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the storage directory smb properties with headers and response status code
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryInfo> setPropertiesWithResponse(FileSmbProperties smbProperties,
        String filePermission, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        smbProperties = smbProperties == null ? new FileSmbProperties() : smbProperties;

        // Checks that file permission and file permission key are valid
        ModelHelper.validateFilePermissionAndKey(filePermission, smbProperties.getFilePermissionKey());

        // If file permission and file permission key are both not set then set default value
        String finalFilePermission = smbProperties.setFilePermission(filePermission, FileConstants.PRESERVE);
        String filePermissionKey = smbProperties.getFilePermissionKey();

        String fileAttributes = smbProperties.setNtfsFileAttributes(FileConstants.PRESERVE);
        String fileCreationTime = smbProperties.setFileCreationTime(FileConstants.PRESERVE);
        String fileLastWriteTime = smbProperties.setFileLastWriteTime(FileConstants.PRESERVE);
        String fileChangeTime = smbProperties.getFileChangeTimeString();
        Callable<ResponseBase<DirectoriesSetPropertiesHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getDirectories().setPropertiesWithResponse(shareName, directoryPath,
                fileAttributes, null, finalFilePermission, filePermissionKey, fileCreationTime, fileLastWriteTime,
                fileChangeTime, finalContext);

        return ModelHelper.mapSetPropertiesResponse(sendRequest(operation, timeout, ShareStorageException.class));
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
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map -->
     * <pre>
     * ShareDirectorySetMetadataInfo response =
     *     shareDirectoryClient.setMetadata&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;updatedMetadata&quot;&#41;&#41;;
     * System.out.printf&#40;&quot;Setting the directory metadata completed with updated etag %s&quot;, response.getETag&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map -->
     *
     * <p>Clear the metadata of the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map.clearMetadata -->
     * <pre>
     * ShareDirectorySetMetadataInfo response = shareDirectoryClient.setMetadata&#40;null&#41;;
     * System.out.printf&#40;&quot;Cleared metadata.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.setMetadata#map.clearMetadata -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @return The information about the directory
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectorySetMetadataInfo setMetadata(Map<String, String> metadata) {
        return setMetadataWithResponse(metadata, null, Context.NONE).getValue();
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
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context -->
     * <pre>
     * Response&lt;ShareDirectorySetMetadataInfo&gt; response =
     *     shareDirectoryClient.setMetadataWithResponse&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;updatedMetadata&quot;&#41;,
     *         Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Setting the directory metadata completed with updated etag %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context -->
     *
     * <p>Clear the metadata of the directory</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata -->
     * <pre>
     * Response&lt;ShareDirectorySetMetadataInfo&gt; response = shareDirectoryClient.setMetadataWithResponse&#40;null,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Directory latest modified date is %s.&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.setMetadataWithResponse#map-duration-context.clearMetadata -->
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-directory-metadata">Azure Docs</a>.</p>
     *
     * @param metadata Optional metadata to set on the directory, if null is passed the metadata for the directory is
     * cleared
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the information about the directory and response status code
     * @throws ShareStorageException If the directory doesn't exist or the metadata contains invalid keys
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectorySetMetadataInfo> setMetadataWithResponse(Map<String, String> metadata,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Callable<ResponseBase<DirectoriesSetMetadataHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getDirectories().setMetadataWithResponse(shareName, directoryPath, null,
                metadata, finalContext);

        return ModelHelper.setShareDirectoryMetadataResponse(sendRequest(operation, timeout,
            ShareStorageException.class));

    }

    /**
     * Lists all sub-directories and files in this directory without their prefix or maxResult in single page.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in the account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories -->
     * <pre>
     * shareDirectoryClient.listFilesAndDirectories&#40;&#41;.forEach&#40;
     *     fileRef -&gt; System.out.printf&#40;&quot;Is the resource a directory? %b. The resource name is: %s.&quot;,
     *         fileRef.isDirectory&#40;&#41;, fileRef.getName&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @return {@link ShareFileItem File info} in the storage directory
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileItem> listFilesAndDirectories() {
        return listFilesAndDirectories(null, null, null, Context.NONE);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in this directory with "subdir" prefix and return 10 results in the
     * account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#string-integer-duration-context -->
     * <pre>
     * shareDirectoryClient.listFilesAndDirectories&#40;&quot;subdir&quot;, 10, Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;.forEach&#40;
     *         fileRef -&gt; System.out.printf&#40;&quot;Is the resource a directory? %b. The resource name is: %s.&quot;,
     *             fileRef.isDirectory&#40;&#41;, fileRef.getName&#40;&#41;&#41;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#string-integer-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param prefix Optional prefix which filters the results to return only files and directories whose name begins
     * with.
     * @param maxResultsPerPage Optional maximum number of files and/or directories to return per page.
     * If the request does not specify maxResultsPerPage or specifies a value greater than 5,000,
     * the server will return up to 5,000 items. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over this value.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileItem> listFilesAndDirectories(String prefix, Integer maxResultsPerPage,
                                                                Duration timeout, Context context) {
        return listFilesAndDirectories(new ShareListFilesAndDirectoriesOptions().setPrefix(prefix)
            .setMaxResultsPerPage(maxResultsPerPage), timeout, context);
    }

    /**
     * Lists all sub-directories and files in this directory with their prefix or snapshots.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>List all sub-directories and files in this directory with "subdir" prefix and return 10 results in the
     * account</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions-duration-context -->
     * <pre>
     * shareDirectoryClient.listFilesAndDirectories&#40;new ShareListFilesAndDirectoriesOptions&#40;&#41;
     *         .setPrefix&#40;&quot;subdir&quot;&#41;.setMaxResultsPerPage&#40;10&#41;, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;
     *     .forEach&#40;fileRef -&gt; System.out.printf&#40;&quot;Is the resource a directory? %b. The resource name is: %s.&quot;,
     *         fileRef.isDirectory&#40;&#41;, fileRef.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.listFilesAndDirectories#ShareListFilesAndDirectoriesOptions-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-directories-and-files">Azure
     * Docs</a>.</p>
     *
     * @param options Optional parameters.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link ShareFileItem File info} in this directory with prefix and max number of return results.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ShareFileItem> listFilesAndDirectories(ShareListFilesAndDirectoriesOptions options,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;

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

        BiFunction<String, Integer, PagedResponse<ShareFileItem>> retriever = (marker, pageSize) -> {
            Callable<Response<ListFilesAndDirectoriesSegmentResponse>> operation = () -> this.azureFileStorageClient
                .getDirectories().listFilesAndDirectoriesSegmentNoCustomHeadersWithResponse(shareName, directoryPath,
                    modifiedOptions.getPrefix(), snapshot, marker,
                    pageSize == null ? modifiedOptions.getMaxResultsPerPage() : pageSize, null, finalIncludeTypes,
                    modifiedOptions.includeExtendedInfo(), finalContext);

            Response<ListFilesAndDirectoriesSegmentResponse> response
                = sendRequest(operation, timeout, ShareStorageException.class);

            return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                ModelHelper.convertResponseAndGetNumOfResults(response), response.getValue().getNextMarker(), null);
        };

        return new PagedIterable<>(pageSize -> retriever.apply(null, pageSize), retriever);
    }

    /**
     * List of open handles on a directory or a file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get 10 handles with recursive call.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.listHandles#Integer-boolean-duration-context -->
     * <pre>
     * Iterable&lt;HandleItem&gt; result = shareDirectoryClient.listHandles&#40;10, true, Duration.ofSeconds&#40;1&#41;,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Get handles completed with handle id %s&quot;, result.iterator&#40;&#41;.next&#40;&#41;.getHandleId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.listHandles#Integer-boolean-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-handles">Azure Docs</a>.</p>
     *
     * @param maxResultsPerPage Optional maximum number of results will return per page
     * @param recursive Specifies operation should apply to the directory specified in the URI, its files, its
     * subdirectories and their files.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link HandleItem handles} in the directory that satisfy the requirements
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<HandleItem> listHandles(Integer maxResultsPerPage, boolean recursive, Duration timeout,
        Context context) {
        return listHandlesWithOptionalTimeout(maxResultsPerPage, recursive, timeout, context);
    }

    PagedIterable<HandleItem> listHandlesWithOptionalTimeout(Integer maxResultPerPage, boolean recursive,
        Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;
        Function<String, PagedResponse<HandleItem>> retriever = (marker) -> {
            Callable<ResponseBase<DirectoriesListHandlesHeaders, ListHandlesResponse>> operation =
                () -> this.azureFileStorageClient.getDirectories().listHandlesWithResponse(shareName, directoryPath,
                    marker, maxResultPerPage, null, snapshot, recursive, finalContext);

            ResponseBase<DirectoriesListHandlesHeaders, ListHandlesResponse> response
                = sendRequest(operation, timeout, ShareStorageException.class);

            return new PagedResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                ModelHelper.transformHandleItems(response.getValue().getHandleList()),
                response.getValue().getNextMarker(), response.getDeserializedHeaders());

        };
        return new PagedIterable<>(() -> retriever.apply(null), retriever);
    }

    /**
     * Closes a handle on the directory at the service. This is intended to be used alongside {@link
     * #listHandles(Integer, boolean, Duration, Context)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandle#String -->
     * <pre>
     * shareDirectoryClient.listHandles&#40;null, true, Duration.ofSeconds&#40;30&#41;, Context.NONE&#41;.forEach&#40;handleItem -&gt; &#123;
     *     shareDirectoryClient.forceCloseHandle&#40;handleItem.getHandleId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Closed handle %s on resource %s%n&quot;, handleItem.getHandleId&#40;&#41;, handleItem.getPath&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandle#String -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be closed.
     * @return Information about the closed handles.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CloseHandlesInfo forceCloseHandle(String handleId) {
        return forceCloseHandleWithResponse(handleId, null, Context.NONE).getValue();
    }

    /**
     * Closes a handle on the directory at the service. This is intended to be used alongside {@link
     * #listHandles(Integer, boolean, Duration, Context)}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close handles returned by list handles.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandleWithResponse#String-Duration-Context -->
     * <pre>
     * shareDirectoryClient.listHandles&#40;null, true, Duration.ofSeconds&#40;30&#41;, Context.NONE&#41;.forEach&#40;handleItem -&gt; &#123;
     *     Response&lt;CloseHandlesInfo&gt; closeResponse = shareDirectoryClient.forceCloseHandleWithResponse&#40;
     *         handleItem.getHandleId&#40;&#41;, Duration.ofSeconds&#40;30&#41;, Context.NONE&#41;;
     *     System.out.printf&#40;&quot;Closing handle %s on resource %s completed with status code %d%n&quot;,
     *         handleItem.getHandleId&#40;&#41;, handleItem.getPath&#40;&#41;, closeResponse.getStatusCode&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.forceCloseHandleWithResponse#String-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param handleId Handle ID to be clsoed.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that contains information about the closed handles, headers and response status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CloseHandlesInfo> forceCloseHandleWithResponse(String handleId, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;

        Callable<ResponseBase<DirectoriesForceCloseHandlesHeaders, Void>> operation = () ->
            this.azureFileStorageClient.getDirectories().forceCloseHandlesWithResponse(shareName, directoryPath,
                handleId, null, null, snapshot, false, finalContext);

        ResponseBase<DirectoriesForceCloseHandlesHeaders, Void> response
            = sendRequest(operation, timeout, ShareStorageException.class);

        return new SimpleResponse<>(response,
            new CloseHandlesInfo(response.getDeserializedHeaders().getXMsNumberOfHandlesClosed(),
                response.getDeserializedHeaders().getXMsNumberOfHandlesFailed()));
    }

    /**
     * Closes all handles opened on the directory at the service.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Force close all handles recursively.</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.forceCloseAllHandles#boolean-Duration-Context -->
     * <pre>
     * CloseHandlesInfo closeHandlesInfo = shareDirectoryClient.forceCloseAllHandles&#40;true, Duration.ofSeconds&#40;30&#41;,
     *     Context.NONE&#41;;
     * System.out.printf&#40;&quot;Closed %d open handles on the directory%n&quot;, closeHandlesInfo.getClosedHandles&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Failed to close %d open handles on the directory%n&quot;, closeHandlesInfo.getFailedHandles&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.forceCloseAllHandles#boolean-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/force-close-handles">Azure Docs</a>.</p>
     *
     * @param recursive Flag indicating if the operation should apply to all subdirectories and files contained in the
     * directory.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return Information about the closed handles
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CloseHandlesInfo forceCloseAllHandles(boolean recursive, Duration timeout, Context context) {
        Context finalContext = context == null ? Context.NONE : context;

        Function<String, PagedResponse<CloseHandlesInfo>> retriever = (marker) -> {
            Callable<ResponseBase<DirectoriesForceCloseHandlesHeaders, Void>> operation =
                () -> this.azureFileStorageClient.getDirectories()
                    .forceCloseHandlesWithResponse(shareName, directoryPath, "*", null, marker, snapshot,
                        recursive, finalContext);

            ResponseBase<DirectoriesForceCloseHandlesHeaders, Void> response
                = sendRequest(operation, timeout, ShareStorageException.class);

            return new PagedResponseBase<>(response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                Collections.singletonList(
                    new CloseHandlesInfo(response.getDeserializedHeaders().getXMsNumberOfHandlesClosed(),
                        response.getDeserializedHeaders().getXMsNumberOfHandlesFailed())),
                response.getDeserializedHeaders().getXMsMarker(),
                response.getDeserializedHeaders());
        };

        return new PagedIterable<>(() -> retriever.apply(null), retriever).stream().reduce(new CloseHandlesInfo(0, 0),
            (accu, next) -> new CloseHandlesInfo(accu.getClosedHandles() + next.getClosedHandles(),
                accu.getFailedHandles() + next.getFailedHandles()));
    }

    /**
     * Moves the directory to another location within the share.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/rename-directory">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.rename#String -->
     * <pre>
     * ShareDirectoryClient renamedClient = client.rename&#40;destinationPath&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.rename#String -->
     *
     * @param destinationPath Relative path from the share to rename the directory to.
     * @return A {@link ShareDirectoryClient} used to interact with the new file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryClient rename(String destinationPath) {
        return renameWithResponse(new ShareFileRenameOptions(destinationPath), null, Context.NONE).getValue();
    }

    /**
     * Moves the directory to another location within the share.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/rename-directory">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.renameWithResponse#ShareFileRenameOptions-Duration-Context -->
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
     * ShareDirectoryClient newRenamedClient = client.renameWithResponse&#40;options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.renameWithResponse#ShareFileRenameOptions-Duration-Context -->
     *
     * @param options {@link ShareFileRenameOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A  {@link Response} whose {@link Response#getValue() value} contains a {@link ShareDirectoryClient} used
     * to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryClient> renameWithResponse(ShareFileRenameOptions options, Duration timeout,
        Context context) {
        StorageImplUtils.assertNotNull("options", options);
        Context finalContext = context == null ? Context.NONE : context;

        ShareRequestConditions sourceRequestConditions = options.getSourceRequestConditions() == null
            ? new ShareRequestConditions() : options.getSourceRequestConditions();
        ShareRequestConditions destinationRequestConditions = options.getDestinationRequestConditions() == null
            ? new ShareRequestConditions() : options.getDestinationRequestConditions();

        // We want to hide the SourceAccessConditions type from the user for consistency's sake, so we convert here.
        SourceLeaseAccessConditions sourceConditions = new SourceLeaseAccessConditions()
            .setSourceLeaseId(sourceRequestConditions.getLeaseId());
        DestinationLeaseAccessConditions destinationConditions = new DestinationLeaseAccessConditions()
            .setDestinationLeaseId(destinationRequestConditions.getLeaseId());

        CopyFileSmbInfo smbInfo;
        String filePermissionKey;
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
        } else {
            smbInfo = null;
            filePermissionKey = null;
        }

        ShareDirectoryClient destinationDirectoryClient = getDirectoryClient(options.getDestinationPath());

        String renameSource = this.sasToken != null ? this.getDirectoryUrl() + "?" + this.sasToken.getSignature()
            : this.getDirectoryUrl();

        Callable<Response<Void>> operation = () -> destinationDirectoryClient.azureFileStorageClient.getDirectories()
            .renameNoCustomHeadersWithResponse(destinationDirectoryClient.getShareName(),
                destinationDirectoryClient.getDirectoryPath(), renameSource, null /* timeout */,
                options.getReplaceIfExists(), options.isIgnoreReadOnly(), options.getFilePermission(),
                filePermissionKey, options.getMetadata(), sourceConditions, destinationConditions, smbInfo,
                finalContext);

        return new SimpleResponse<>(sendRequest(operation, timeout, ShareStorageException.class),
            destinationDirectoryClient);
    }

    /**
     * Takes in a destination and creates a ShareDirectoryClient with a new path
     * @param destinationPath The destination path
     * @return A ShareDirectoryClient
     */
    ShareDirectoryClient getDirectoryClient(String destinationPath) {
        if (CoreUtils.isNullOrEmpty(destinationPath)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'destinationPath' can not be set to null"));
        }

        return new ShareDirectoryClient(this.azureFileStorageClient, getShareName(), destinationPath, null,
            this.getAccountName(), this.getServiceVersion(), sasToken);
    }

    /**
     * Creates a subdirectory under current directory with specific name and returns a response of ShareDirectoryClient
     * to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createSubdirectory#string -->
     * <pre>
     * shareDirectoryClient.createSubdirectory&#40;&quot;subdir&quot;&#41;;
     * System.out.println&#40;&quot;Completed creating the subdirectory.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createSubdirectory#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return The subdirectory client.
     * @throws ShareStorageException If the subdirectory has already existed, the parent directory does not exist or
     * directory is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryClient createSubdirectory(String subdirectoryName) {
        return createSubdirectoryWithResponse(subdirectoryName, null, null, null,
            null, Context.NONE).getValue();
    }

    /**
     * Creates a subdirectory under current directory with specific name , metadata and returns a response of
     * ShareDirectoryClient to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * Response&lt;ShareDirectoryClient&gt; response = shareDirectoryClient.createSubdirectoryWithResponse&#40;&quot;subdir&quot;,
     *     smbProperties, filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Creating the sub directory completed with status code %d&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryWithResponse#String-FileSmbProperties-String-Map-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param smbProperties The SMB properties of the directory.
     * @param filePermission The file permission of the directory.
     * @param metadata Optional metadata to associate with the subdirectory
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the subdirectory client and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or
     * subdirectory is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryClient> createSubdirectoryWithResponse(String subdirectoryName,
        FileSmbProperties smbProperties, String filePermission, Map<String, String> metadata, Duration timeout,
        Context context) {
        ShareDirectoryClient shareDirectoryClient = getSubdirectoryClient(subdirectoryName);
        return new SimpleResponse<>(shareDirectoryClient
            .createWithResponse(smbProperties, filePermission, metadata, timeout, context), shareDirectoryClient);
    }

    /**
     * Creates a subdirectory under current directory with specified name if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the sub directory "subdir" </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryIfNotExists#string -->
     * <pre>
     * ShareDirectoryClient subdirectoryClient = shareDirectoryClient.createSubdirectoryIfNotExists&#40;&quot;subdir&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryIfNotExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return A {@link ShareDirectoryClient} used to interact with the subdirectory created.
     * */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareDirectoryClient createSubdirectoryIfNotExists(String subdirectoryName) {
        return createSubdirectoryIfNotExistsWithResponse(subdirectoryName, new ShareDirectoryCreateOptions(), null,
            Context.NONE).getValue();
    }

    /**
     * Creates a subdirectory under current directory with specific name and metadata if it does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the subdirectory named "subdir", with metadata</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions-Duration-Context -->
     * <pre>
     * FileSmbProperties smbProperties = new FileSmbProperties&#40;&#41;;
     * String filePermission = &quot;filePermission&quot;;
     * ShareDirectoryCreateOptions options = new ShareDirectoryCreateOptions&#40;&#41;.setSmbProperties&#40;smbProperties&#41;
     *     .setFilePermission&#40;filePermission&#41;.setMetadata&#40;Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;&#41;;
     *
     * Response&lt;ShareDirectoryClient&gt; response = shareDirectoryClient
     *     .createSubdirectoryIfNotExistsWithResponse&#40;&quot;subdir&quot;, options, Duration.ofSeconds&#40;1&#41;,
     *         new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createSubdirectoryIfNotExistsWithResponse#String-ShareDirectoryCreateOptions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param options {@link ShareDirectoryCreateOptions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link ShareDirectoryClient} used
     * to interact with the subdirectory created. If {@link Response}'s status code is 201, a new subdirectory was
     * successfully created. If status code is 409, a subdirectory with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareDirectoryClient> createSubdirectoryIfNotExistsWithResponse(String subdirectoryName,
        ShareDirectoryCreateOptions options, Duration timeout, Context context) {
        ShareDirectoryClient shareDirectoryClient = getSubdirectoryClient(subdirectoryName);
        Response<ShareDirectoryInfo> response = shareDirectoryClient.createIfNotExistsWithResponse(options,
            timeout, context);
        return new SimpleResponse<>(response, shareDirectoryClient);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be
     * deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectory#string -->
     * <pre>
     * shareDirectoryClient.deleteSubdirectory&#40;&quot;mysubdirectory&quot;&#41;;
     * System.out.println&#40;&quot;Complete deleting the subdirectory.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectory#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSubdirectory(String subdirectoryName) {
        deleteSubdirectoryWithResponse(subdirectoryName, null, Context.NONE);
    }

    /**
     * Deletes the subdirectory with specific name in this directory. The directory must be empty before it can be
     * deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryWithResponse#string-duration-context -->
     * <pre>
     * Response&lt;Void&gt; response = shareDirectoryClient.deleteSubdirectoryWithResponse&#40;&quot;mysubdirectory&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed deleting the subdirectory with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryWithResponse#string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the subdirectory doesn't exist, the parent directory does not exist or
     * subdirectory name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSubdirectoryWithResponse(String subdirectoryName, Duration timeout, Context context) {
        return getSubdirectoryClient(subdirectoryName).deleteWithResponse(timeout, context);
    }

    /**
     * Deletes the subdirectory with specific name in this directory if it exists. The directory must be empty before
     * it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "subdir"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryIfExists#string -->
     * <pre>
     * boolean result = shareDirectoryClient.deleteSubdirectoryIfExists&#40;&quot;mysubdirectory&quot;&#41;;
     * System.out.println&#40;&quot;Subdirectory deleted: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @return {@code true} if subdirectory is successfully deleted, {@code false} if subdirectory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteSubdirectoryIfExists(String subdirectoryName) {
        return deleteSubdirectoryIfExistsWithResponse(subdirectoryName, null, Context.NONE).getValue();
    }

    /**
     * Deletes the subdirectory with specific name in this directory if it exists. The directory must be empty
     * before it can be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the subdirectory named "mysubdirectory"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryIfExistsWithResponse#string-duration-context -->
     * <pre>
     * Response&lt;Boolean&gt; response = shareDirectoryClient.deleteSubdirectoryIfExistsWithResponse&#40;&quot;mysubdirectory&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteSubdirectoryIfExistsWithResponse#string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-directory">Azure Docs</a>.</p>
     *
     * @param subdirectoryName Name of the subdirectory
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the
     * subdirectory was successfully deleted. If status code is 404, the subdirectory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteSubdirectoryIfExistsWithResponse(String subdirectoryName, Duration timeout,
        Context context) {
        return getSubdirectoryClient(subdirectoryName).deleteIfExistsWithResponse(timeout, context);
    }

    /**
     * Creates a file in this directory with specific name, max number of results and returns a response of
     * ShareDirectoryInfo to interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create 1k file with named "myFile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createFile#string-long -->
     * <pre>
     * ShareFileClient response = shareDirectoryClient.createFile&#40;&quot;myfile&quot;, 1024&#41;;
     * System.out.println&#40;&quot;Completed creating the file: &quot; + response&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createFile#string-long -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Size of the file
     * @return The ShareFileClient
     * @throws ShareStorageException If the parent directory does not exist or file name is an invalid resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ShareFileClient createFile(String fileName, long maxSize) {
        return createFileWithResponse(fileName, maxSize, null, null, null,
            null, null, Context.NONE).getValue();
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-duration-context -->
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
     * Response&lt;ShareFileClient&gt; response = shareDirectoryClient.createFileWithResponse&#40;&quot;myFile&quot;, 1024,
     *     httpHeaders, smbProperties, filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed creating the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission THe file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the parent directory does not exist or file name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileClient> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, Duration timeout, Context context) {
        return this.createFileWithResponse(fileName, maxSize, httpHeaders, smbProperties, filePermission, metadata,
            null, timeout, context);
    }

    /**
     * Creates a file in this directory with specific name and returns a response of ShareDirectoryInfo to
     * interact with it.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create the file named "myFile"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-duration-context -->
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
     * Response&lt;ShareFileClient&gt; response = shareDirectoryClient.createFileWithResponse&#40;&quot;myFile&quot;, 1024,
     *     httpHeaders, smbProperties, filePermission, Collections.singletonMap&#40;&quot;directory&quot;, &quot;metadata&quot;&#41;,
     *     requestConditions, Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed creating the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.createFile#String-long-ShareFileHttpHeaders-FileSmbProperties-String-Map-ShareRequestConditions-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-file">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param maxSize Max size of the file
     * @param httpHeaders The user settable file http headers.
     * @param smbProperties The user settable file smb properties.
     * @param filePermission THe file permission of the file.
     * @param metadata Optional name-value pairs associated with the file as metadata.
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the directory info and the status of creating the directory.
     * @throws ShareStorageException If the directory has already existed, the parent directory does not exist or file
     * name is an invalid resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ShareFileClient> createFileWithResponse(String fileName, long maxSize,
        ShareFileHttpHeaders httpHeaders, FileSmbProperties smbProperties, String filePermission,
        Map<String, String> metadata, ShareRequestConditions requestConditions, Duration timeout, Context context) {
        ShareFileClient shareFileClient = getFileClient(fileName);
        Response<ShareFileInfo> response = shareFileClient.createWithResponse(maxSize, httpHeaders, smbProperties,
            filePermission, metadata, requestConditions, timeout, context);
        return new SimpleResponse<>(response, shareFileClient);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteFile#string -->
     * <pre>
     * shareDirectoryClient.deleteFile&#40;&quot;myfile&quot;&#41;;
     * System.out.println&#40;&quot;Completed deleting the file.&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteFile#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, Context.NONE);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-duration-context -->
     * <pre>
     * Response&lt;Void&gt; response = shareDirectoryClient.deleteFileWithResponse&#40;&quot;myfile&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed deleting the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, Duration timeout, Context context) {
        return this.deleteFileWithResponse(fileName, null, timeout, context);
    }

    /**
     * Deletes the file with specific name in this directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     * Response&lt;Void&gt; response = shareDirectoryClient.deleteFileWithResponse&#40;&quot;myfile&quot;, requestConditions,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Completed deleting the file with status code: &quot; + response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteFileWithResponse#string-ShareRequestConditions-duration-context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response that only contains headers and response status code
     * @throws ShareStorageException If the directory doesn't exist or the file doesn't exist or file name is an invalid
     * resource name.
     * @throws RuntimeException if the operation doesn't complete before the timeout concludes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, timeout, context);
    }

    /**
     * Deletes the file with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteFileIfExists#string -->
     * <pre>
     * boolean result = shareDirectoryClient.deleteFileIfExists&#40;&quot;myfile&quot;&#41;;
     * System.out.println&#40;&quot;File deleted: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteFileIfExists#string -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @return {@code true} if the file is successfully deleted, {@code false} if the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, null, null, Context.NONE).getValue();
    }

    /**
     * Deletes the file with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteFileIfExistsWithResponse#String-Duration-Context -->
     * <pre>
     * Response&lt;Boolean&gt; response = shareDirectoryClient.deleteFileIfExistsWithResponse&#40;&quot;myfile&quot;,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteFileIfExistsWithResponse#String-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the file
     * was successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteFileIfExistsWithResponse(String fileName, Duration timeout, Context context) {
        return this.deleteFileIfExistsWithResponse(fileName, null, timeout, context);
    }

    /**
     * Deletes the file with specific name in this directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the file "filetest"</p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.deleteFileIfExistsWithResponse#String-ShareRequestConditions-Duration-Context -->
     * <pre>
     * ShareRequestConditions requestConditions = new ShareRequestConditions&#40;&#41;.setLeaseId&#40;leaseId&#41;;
     *
     * Response&lt;Boolean&gt; fileResponse = shareDirectoryClient.deleteFileIfExistsWithResponse&#40;&quot;myfile&quot;, requestConditions,
     *     Duration.ofSeconds&#40;1&#41;, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;fileResponse.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.deleteFileIfExistsWithResponse#String-ShareRequestConditions-Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/delete-file2">Azure Docs</a>.</p>
     *
     * @param fileName Name of the file
     * @param requestConditions {@link ShareRequestConditions}
     * @param timeout An optional timeout applied to the operation. If a response is not returned before the timeout
     * concludes a {@link RuntimeException} will be thrown.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 202, the file
     * was successfully deleted. If status code is 404, the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteFileIfExistsWithResponse(String fileName, ShareRequestConditions requestConditions,
        Duration timeout, Context context) {
        requestConditions = requestConditions == null ? new ShareRequestConditions() : requestConditions;
        try {
            Response<Void> response = deleteFileWithResponse(fileName, requestConditions, timeout, context);
            return new SimpleResponse<>(response, true);
        } catch (ShareStorageException e) {
            if (e.getStatusCode() == 404 && e.getErrorCode().equals(ShareErrorCode.RESOURCE_NOT_FOUND)) {
                HttpResponse res = e.getResponse();
                return new SimpleResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), false);
            } else {
                throw LOGGER.logExceptionAsError(e);
            }
        }
    }

    /**
     * Get snapshot id which attached to {@link ShareDirectoryClient}. Return {@code null} if no snapshot id attached.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Get the share snapshot id. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.getShareSnapshotId -->
     * <pre>
     * OffsetDateTime currentTime = OffsetDateTime.of&#40;LocalDateTime.now&#40;&#41;, ZoneOffset.UTC&#41;;
     * ShareDirectoryClient shareDirectoryClient = new ShareFileClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;https:&#47;&#47;$&#123;accountName&#125;.file.core.windows.net&quot;&#41;
     *     .sasToken&#40;&quot;$&#123;SASToken&#125;&quot;&#41;
     *     .shareName&#40;&quot;myshare&quot;&#41;
     *     .resourcePath&#40;&quot;mydirectory&quot;&#41;
     *     .snapshot&#40;currentTime.toString&#40;&#41;&#41;
     *     .buildDirectoryClient&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Snapshot ID: %s%n&quot;, shareDirectoryClient.getShareSnapshotId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.getShareSnapshotId -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.getShareName -->
     * <pre>
     * String shareName = directoryAsyncClient.getShareName&#40;&#41;;
     * System.out.println&#40;&quot;The share name of the directory is &quot; + shareName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.getShareName -->
     *
     * @return The share name of the directory.
     */
    public String getShareName() {
        return this.shareName;
    }

    /**
     * Get the directory path of the client.
     *
     * <p>Get directory path. </p>
     *
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.getDirectoryPath -->
     * <pre>
     * String directoryPath = shareDirectoryClient.getDirectoryPath&#40;&#41;;
     * System.out.println&#40;&quot;The name of the directory is &quot; + directoryPath&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.getDirectoryPath -->
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return this.directoryPath;
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
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareFileSasPermission permission = new ShareFileSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * shareDirectoryClient.generateSas&#40;values&#41;; &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues -->
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
     * <!-- src_embed com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues-Context -->
     * <pre>
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;;
     * ShareFileSasPermission permission = new ShareFileSasPermission&#40;&#41;.setReadPermission&#40;true&#41;;
     *
     * ShareServiceSasSignatureValues values = new ShareServiceSasSignatureValues&#40;expiryTime, permission&#41;
     *     .setStartTime&#40;OffsetDateTime.now&#40;&#41;&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * shareDirectoryClient.generateSas&#40;values, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.share.ShareDirectoryClient.generateSas#ShareServiceSasSignatureValues-Context -->
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateSas(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return new ShareSasImplUtil(shareServiceSasSignatureValues, getShareName(), getDirectoryPath())
            .generateSas(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }

    /**
     * For debugging purposes only.
     * Returns the string to sign that will be used to generate the signature for the SAS URL.
     *
     * @param shareServiceSasSignatureValues {@link ShareServiceSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return The string to sign that will be used to generate the signature for the SAS URL.
     */
   public String generateSasStringToSign(ShareServiceSasSignatureValues shareServiceSasSignatureValues, Context context) {
        return new ShareSasImplUtil(shareServiceSasSignatureValues, getShareName(), getDirectoryPath())
            .generateSasStringToSign(SasImplUtils.extractSharedKeyCredential(getHttpPipeline()), context);
    }
}
