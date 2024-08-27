// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.datalake.implementation.models.CpkInfo;
import com.azure.storage.file.datalake.implementation.models.FileSystemsListPathsHeaders;
import com.azure.storage.file.datalake.implementation.models.PathList;
import com.azure.storage.file.datalake.implementation.models.PathResourceType;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.implementation.util.TransformUtils;
import com.azure.storage.file.datalake.models.CustomerProvidedKey;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * This class provides a client that contains directory operations for Azure Storage Data Lake. Operations provided by
 * this client include creating a directory, deleting a directory, renaming a directory, setting metadata and
 * http headers, setting and retrieving access control, getting properties and creating and deleting files and
 * subdirectories.
 *
 * <p>
 * This client is instantiated through {@link DataLakePathClientBuilder} or retrieved via
 * {@link DataLakeFileSystemClient#getDirectoryClient(String) getDirectoryClient}.
 *
 * <p>
 * Please refer to the
 *
 * <a href="https://docs.microsoft.com/azure/storage/blobs/data-lake-storage-introduction">Azure
 * Docs</a> for more information.
 */
@ServiceClient(builder = DataLakePathClientBuilder.class)
public class DataLakeDirectoryClient extends DataLakePathClient {
    private final DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient;

    DataLakeDirectoryClient(DataLakeDirectoryAsyncClient dataLakeDirectoryAsyncClient, BlockBlobClient blockBlobClient,
        HttpPipeline pipeline, String url, DataLakeServiceVersion serviceVersion, String accountName,
        String fileSystemName, String directoryName, AzureSasCredential sasToken, CpkInfo customerProvidedKey,
        boolean isTokenCredentialAuthenticated) {
        super(dataLakeDirectoryAsyncClient, blockBlobClient, pipeline, url, serviceVersion, accountName, fileSystemName,
            directoryName, PathResourceType.DIRECTORY, sasToken, customerProvidedKey, isTokenCredentialAuthenticated);
        this.dataLakeDirectoryAsyncClient = dataLakeDirectoryAsyncClient;
    }

    DataLakeDirectoryClient(DataLakePathClient dataLakePathClient) {
        super(dataLakePathClient.dataLakePathAsyncClient, dataLakePathClient.blockBlobClient,
            dataLakePathClient.getHttpPipeline(), dataLakePathClient.getAccountUrl(),
            dataLakePathClient.getServiceVersion(), dataLakePathClient.getAccountName(),
            dataLakePathClient.getFileSystemName(), Utility.urlEncode(dataLakePathClient.pathName),
            PathResourceType.DIRECTORY, dataLakePathClient.getSasToken(), dataLakePathClient.getCpkInfo(),
            dataLakePathClient.isTokenCredentialAuthenticated());
        this.dataLakeDirectoryAsyncClient = new DataLakeDirectoryAsyncClient(
            dataLakePathClient.dataLakePathAsyncClient);
    }

    /**
     * Gets the URL of the directory represented by this client on the Data Lake service.
     *
     * @return the URL.
     */
    public String getDirectoryUrl() {
        return getPathUrl();
    }

    /**
     * Gets the path of this directory, not including the name of the resource itself.
     *
     * @return The path of the directory.
     */
    public String getDirectoryPath() {
        return getObjectPath();
    }

    /**
     * Gets the name of this directory, not including its full path.
     *
     * @return The name of the directory.
     */
    public String getDirectoryName() {
        return getObjectName();
    }

    /**
     * Creates a new {@link DataLakeDirectoryClient} with the specified {@code customerProvidedKey}.
     *
     * @param customerProvidedKey the {@link CustomerProvidedKey} for the directory,
     * pass {@code null} to use no customer provided key.
     * @return a {@link DataLakeDirectoryClient} with the specified {@code customerProvidedKey}.
     */
    public DataLakeDirectoryClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey) {
        CpkInfo finalCustomerProvidedKey = null;
        if (customerProvidedKey != null) {
            finalCustomerProvidedKey = new CpkInfo()
                .setEncryptionKey(customerProvidedKey.getKey())
                .setEncryptionKeySha256(customerProvidedKey.getKeySha256())
                .setEncryptionAlgorithm(customerProvidedKey.getEncryptionAlgorithm());
        }
        return new DataLakeDirectoryClient(
            dataLakeDirectoryAsyncClient.getCustomerProvidedKeyAsyncClient(customerProvidedKey),
            blockBlobClient.getCustomerProvidedKeyClient(Transforms.toBlobCustomerProvidedKey(customerProvidedKey)),
            getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(), getFileSystemName(),
            getObjectPath(), getSasToken(), finalCustomerProvidedKey, isTokenCredentialAuthenticated());
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.delete -->
     * <pre>
     * client.delete&#40;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.delete -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete() {
        deleteWithResponse(false, null, null, Context.NONE).getValue();
    }

    /**
     * Recursively deletes a directory and all contents within the directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteRecursively -->
     * <pre>
     * client.deleteRecursively&#40;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteRecursively -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRecursively() {
        deleteRecursivelyWithResponse(null, null, Context.NONE).getValue();
    }

    /**
     * Recursively deletes a directory and all contents within the directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteRecursivelyWithResponse#DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions deleteRequestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * client.deleteRecursivelyWithResponse&#40;deleteRequestConditions, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteRecursivelyWithResponse#DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRecursivelyWithResponse(DataLakeRequestConditions requestConditions, Duration timeout,
        Context context) {
        return deleteWithResponse(true, requestConditions, timeout, context);
    }

    /**
     * Deletes a directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteWithResponse#boolean-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteWithResponse&#40;recursive, requestConditions, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteWithResponse#boolean-DataLakeRequestConditions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param recursive Whether or not to delete all paths beneath the directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(boolean recursive, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        // TODO (rickle-msft): Update for continuation token if we support HNS off
        return super.deleteWithResponse(recursive, requestConditions, timeout, context);
    }

    /**
     * Deletes a directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExists -->
     * <pre>
     * boolean result = client.deleteIfExists&#40;&#41;;
     * System.out.println&#40;&quot;Delete request completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExists -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     * @return {@code true} if directory is successfully deleted, {@code false} if directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteIfExists() {
        return deleteIfExistsWithResponse(new DataLakePathDeleteOptions(), null, Context.NONE).getValue();
    }

    /**
     * Deletes a directory if it exists.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;recursive&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteIfExistsWithResponse&#40;options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteIfExistsWithResponse#DataLakePathDeleteOptions-Duration-Context -->
     *
     * <p>For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a></p>
     *
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 200, the directory
     * was successfully deleted. If status code is 404, the directory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteIfExistsWithResponse(DataLakePathDeleteOptions options, Duration timeout,
        Context context) {
        return super.deleteIfExistsWithResponse(options, timeout, context);
    }

    /**
     * Initializes a new DataLakeFileClient object by concatenating fileName to the end of DataLakeDirectoryClient's
     * URL. The new DataLakeFileClient uses the same request policy pipeline as the DataLakeDirectoryClient.
     *
     * @param fileName A {@code String} representing the name of the file.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.getFileClient#String -->
     * <pre>
     * DataLakeFileClient dataLakeFileClient = client.getFileClient&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.getFileClient#String -->
     *
     * @return A new {@link DataLakeFileClient} object which references the file with the specified name in this
     * directory.
     */
    public DataLakeFileClient getFileClient(String fileName) {
        Objects.requireNonNull(fileName, "'fileName' can not be set to null");

        String pathPrefix = getObjectPath().isEmpty() ? "" : getObjectPath() + "/";
        BlockBlobClient blockBlobClient = dataLakeDirectoryAsyncClient.prepareBuilderAppendPath(pathPrefix + fileName)
            .buildBlockBlobClient();

        return new DataLakeFileClient(dataLakeDirectoryAsyncClient.getFileAsyncClient(fileName), blockBlobClient,
            getHttpPipeline(), getAccountUrl(), getServiceVersion(), getAccountName(), getFileSystemName(),
            pathPrefix + fileName, this.getSasToken(), getCpkInfo(), isTokenCredentialAuthenticated());
    }

    /**
     * Creates a new file within a directory. By default this method will not overwrite an existing file.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String -->
     * <pre>
     * DataLakeFileClient fileClient = client.createFile&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String -->
     *
     * @param fileName Name of the file to create.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient createFile(String fileName) {
        return createFile(fileName, false);
    }

    /**
     * Creates a new file within a directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * DataLakeFileClient fClient = client.createFile&#40;fileName, overwrite&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createFile#String-boolean -->
     *
     * @param fileName Name of the file to create.
     * @param overwrite Whether or not to overwrite, should a file exist.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient createFile(String fileName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createFileWithResponse(fileName, new DataLakePathCreateOptions().setRequestConditions(requestConditions),
            null, null).getValue();
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Response&lt;DataLakeFileClient&gt; newFileClient = client.createFileWithResponse&#40;fileName, permissions, umask, httpHeaders,
     *     Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions,
     *     timeout, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     *
     * @param fileName Name of the file to create.
     * @param permissions POSIX access permissions for the file owner, the file owning group, and others.
     * @param umask Restricts permissions of the file to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the file. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> createFileWithResponse(String fileName, String permissions, String umask,
        PathHttpHeaders headers, Map<String, String> metadata, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        return createFileWithResponse(fileName, options, timeout, context);
    }

    /**
     * Creates a new file within a directory. If a file with the same name already exists, the file will be
     * overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
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
     * String leaseId = CoreUtils.randomUuid&#40;&#41;.toString&#40;&#41;;
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
     * Response&lt;DataLakeFileClient&gt; newFileClient = client.createFileWithResponse&#40;fileName, options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param fileName Name of the file to create.
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileClient} used
     * to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> createFileWithResponse(String fileName, DataLakePathCreateOptions options,
        Duration timeout, Context context) {
        DataLakeFileClient dataLakeFileClient = getFileClient(fileName);
        Response<PathInfo> response = dataLakeFileClient.createWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, dataLakeFileClient);
    }

    /**
     * Creates a new file within a directory if it does not exist.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExists#String -->
     * <pre>
     * DataLakeFileClient fileClient = client.createFileIfNotExists&#40;fileName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExists#String -->
     *
     * @param fileName Name of the file to create.
     * @return A {@link DataLakeFileClient} used to interact with the file created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileClient createFileIfNotExists(String fileName) {
        return createFileIfNotExistsWithResponse(fileName, new DataLakePathCreateOptions(), null, null).getValue();
    }

    /**
     * Creates a new file within a directory if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;.setContentLanguage&#40;&quot;en-US&quot;&#41;.setContentType&#40;&quot;binary&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setPathHttpHeaders&#40;headers&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     *
     * Response&lt;DataLakeFileClient&gt; response = client.createFileIfNotExistsWithResponse&#40;fileName, options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createFileIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param fileName Name of the file to create.
     * @param options {@link DataLakePathCreateOptions}
     * metadata key or value, it must be removed or encoded.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileAsyncClient}
     * used to interact with the file created. If {@link Response}'s status code is 201, a new file was successfully
     * created. If status code is 409, a file with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileClient> createFileIfNotExistsWithResponse(String fileName, DataLakePathCreateOptions
        options, Duration timeout, Context context) {
        DataLakeFileClient dataLakeFileClient = getFileClient(fileName);
        Response<PathInfo> response = dataLakeFileClient.createIfNotExistsWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, dataLakeFileClient);
//        Response<PathInfo> response = StorageImplUtils.blockWithOptionalTimeout(
//            dataLakeFileClient.dataLakePathAsyncClient.createIfNotExistsWithResponse(options, context), timeout);
//        return new SimpleResponse<>(response, dataLakeFileClient);
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFile#String -->
     * <pre>
     * client.deleteFile&#40;fileName&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFile#String -->
     *
     * @param fileName Name of the file to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteFile(String fileName) {
        deleteFileWithResponse(fileName, null, null, Context.NONE);
    }

    /**
     * Deletes the specified file in the directory. If the file doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     *
     * client.deleteFileWithResponse&#40;fileName, requestConditions, timeout, new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileWithResponse#String-DataLakeRequestConditions-Duration-Context -->
     *
     * @param fileName Name of the file to delete.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileWithResponse(String fileName, DataLakeRequestConditions requestConditions,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteWithResponse(requestConditions, timeout, context);
    }

    /**
     * Deletes the specified file in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExists#String -->
     * <pre>
     * boolean result = client.deleteFileIfExists&#40;fileName&#41;;
     * System.out.println&#40;&quot;Delete request completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExists#String -->
     *
     * @param fileName Name of the file to delete.
     * @return {@code true} if the file is successfully deleted, {@code false} if the file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteFileIfExists(String fileName) {
        return deleteFileIfExistsWithResponse(fileName, new DataLakePathDeleteOptions()
            .setRequestConditions(new DataLakeRequestConditions()), null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified file in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;false&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteFileIfExistsWithResponse&#40;fileName, options, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteFileIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     *
     * @param fileName Name of the file to delete.
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 200, the specified
     * file was successfully deleted. If status code is 404, the specified file does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteFileIfExistsWithResponse(String fileName, DataLakePathDeleteOptions options,
        Duration timeout, Context context) {
        return getFileClient(fileName).deleteIfExistsWithResponse(options, timeout, context);
//        return StorageImplUtils.blockWithOptionalTimeout(dataLakeDirectoryAsyncClient
//            .deleteFileIfExistsWithResponse(fileName, options, context), timeout);
    }

    /**
     * Initializes a new DataLakeDirectoryClient object by concatenating directoryName to the end of
     * DataLakeDirectoryClient's URL. The new DataLakeDirectoryClient uses the same request policy pipeline as the
     * DataLakeDirectoryClient.
     *
     * @param subdirectoryName A {@code String} representing the name of the sub-directory.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubdirectoryClient#String -->
     * <pre>
     * DataLakeDirectoryClient dataLakeDirectoryClient = client.getSubdirectoryClient&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.getSubdirectoryClient#String -->
     *
     * @return A new {@link DataLakeDirectoryClient} object which references the sub-directory with the specified name
     * in this directory
     */
    public DataLakeDirectoryClient getSubdirectoryClient(String subdirectoryName) {
        Objects.requireNonNull(subdirectoryName, "'subdirectoryName' can not be set to null");

        String pathPrefix = getObjectPath().isEmpty() ? "" : getObjectPath() + "/";

        BlockBlobClient blockBlobClient = prepareBuilderAppendPath(pathPrefix + subdirectoryName).buildBlockBlobClient();

        return new DataLakeDirectoryClient(dataLakeDirectoryAsyncClient.getSubdirectoryAsyncClient(subdirectoryName),
            blockBlobClient, getHttpPipeline(), getAccountUrl(), getServiceVersion(),
            getAccountName(), getFileSystemName(), pathPrefix + subdirectoryName, this.getSasToken(), getCpkInfo(),
            isTokenCredentialAuthenticated());
    }

    /**
     * Creates a new sub-directory within a directory. By default this method will not overwrite an existing
     * sub-directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String -->
     * <pre>
     * DataLakeDirectoryClient directoryClient = client.createSubdirectory&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @return A {@link DataLakeDirectoryClient} used to interact with the sub-directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient createSubdirectory(String subdirectoryName) {
        return createSubdirectory(subdirectoryName, false);
    }

    /**
     * Creates a new sub-directory within a directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String-boolean -->
     * <pre>
     * boolean overwrite = false; &#47;* Default value. *&#47;
     * DataLakeDirectoryClient dClient = client.createSubdirectory&#40;fileName, overwrite&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectory#String-boolean -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param overwrite Whether or not to overwrite, should the sub-directory exist.
     * @return A {@link DataLakeDirectoryClient} used to interact with the sub-directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient createSubdirectory(String subdirectoryName, boolean overwrite) {
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions();
        if (!overwrite) {
            requestConditions.setIfNoneMatch(Constants.HeaderConstants.ETAG_WILDCARD);
        }
        return createSubdirectoryWithResponse(subdirectoryName, new DataLakePathCreateOptions()
            .setRequestConditions(requestConditions), null, null)
            .getValue();
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * PathHttpHeaders httpHeaders = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * Response&lt;DataLakeDirectoryClient&gt; newDirectoryClient = client.createSubdirectoryWithResponse&#40;directoryName,
     *     permissions, umask, httpHeaders, Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;, requestConditions, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-String-String-PathHttpHeaders-Map-DataLakeRequestConditions-Duration-Context -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param permissions POSIX access permissions for the sub-directory owner, the sub-directory owning group, and
     * others.
     * @param umask Restricts permissions of the sub-directory to be created.
     * @param headers {@link PathHttpHeaders}
     * @param metadata Metadata to associate with the resource. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient}
     * used to interact with the sub-directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> createSubdirectoryWithResponse(String subdirectoryName,
        String permissions, String umask, PathHttpHeaders headers, Map<String, String> metadata,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
            .setPathHttpHeaders(headers)
            .setMetadata(metadata)
            .setRequestConditions(requestConditions);

        return createSubdirectoryWithResponse(subdirectoryName, options, timeout, context);
    }

    /**
     * Creates a new sub-directory within a directory. If a sub-directory with the same name already exists, the
     * sub-directory will be overwritten. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
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
     * String leaseId = CoreUtils.randomUuid&#40;&#41;.toString&#40;&#41;;
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
     * Response&lt;DataLakeDirectoryClient&gt; newDirectoryClient = client.createSubdirectoryWithResponse&#40;directoryName,
     *     options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a {@link DataLakeDirectoryClient}
     * used to interact with the sub-directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> createSubdirectoryWithResponse(String subdirectoryName,
        DataLakePathCreateOptions options, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubdirectoryClient(subdirectoryName);
        Response<PathInfo> response = dataLakeDirectoryClient.createWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, dataLakeDirectoryClient);
    }

    /**
     * Creates a new sub-directory if it does not exist within a directory. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExists#String -->
     * <pre>
     * DataLakeDirectoryClient directoryClient = client.createSubdirectoryIfNotExists&#40;directoryName&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExists#String -->
     *
     * @param subdirectoryName Name of the subdirectory to create.
     * @return A {@link DataLakeDirectoryClient} used to interact with the subdirectory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient createSubdirectoryIfNotExists(String subdirectoryName) {
        return createSubdirectoryIfNotExistsWithResponse(subdirectoryName, new DataLakePathCreateOptions(), null, null)
            .getValue();
    }

    /**
     * Creates a new sub-directory within a directory if it does not exist. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     * <pre>
     * PathHttpHeaders headers = new PathHttpHeaders&#40;&#41;
     *     .setContentLanguage&#40;&quot;en-US&quot;&#41;
     *     .setContentType&#40;&quot;binary&quot;&#41;;
     * String permissions = &quot;permissions&quot;;
     * String umask = &quot;umask&quot;;
     * DataLakePathCreateOptions options = new DataLakePathCreateOptions&#40;&#41;
     *     .setPermissions&#40;permissions&#41;
     *     .setUmask&#40;umask&#41;
     *     .setPathHttpHeaders&#40;headers&#41;
     *     .setMetadata&#40;Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;&#41;;
     *
     * Response&lt;DataLakeDirectoryClient&gt; response = client.createSubdirectoryIfNotExistsWithResponse&#40;directoryName,
     *     options, timeout, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 409&#41; &#123;
     *     System.out.println&#40;&quot;Already existed.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Create completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.createSubdirectoryIfNotExistsWithResponse#String-DataLakePathCreateOptions-Duration-Context -->
     *
     * @param subdirectoryName Name of the sub-directory to create.
     * @param options {@link DataLakePathCreateOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeDirectoryClient}
     * used to interact with the subdirectory created. If {@link Response}'s status code is 201, a new subdirectory was
     * successfully created. If status code is 409, a subdirectory with the same name already existed at this location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> createSubdirectoryIfNotExistsWithResponse(String subdirectoryName,
        DataLakePathCreateOptions options, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubdirectoryClient(subdirectoryName);
        Response<PathInfo> response = dataLakeDirectoryClient.createIfNotExistsWithResponse(options, timeout, context);
        return new SimpleResponse<>(response, dataLakeDirectoryClient);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectory#String -->
     * <pre>
     * client.deleteSubdirectory&#40;directoryName&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectory#String -->
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteSubdirectory(String subdirectoryName) {
        deleteSubdirectoryWithResponse(subdirectoryName, false, null, null, Context.NONE);
    }

    /**
     * Deletes the specified sub-directory in the directory. If the sub-directory doesn't exist or is not empty the
     * operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     *
     * client.deleteSubdirectoryWithResponse&#40;directoryName, recursive, requestConditions, timeout,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.println&#40;&quot;Delete request completed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryWithResponse#String-boolean-DataLakeRequestConditions-Duration-Context -->
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     * @param recursive Whether or not to delete all paths beneath the sub-directory.
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteSubdirectoryWithResponse(String subdirectoryName, boolean recursive,
        DataLakeRequestConditions requestConditions, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = getSubdirectoryClient(subdirectoryName);
        return dataLakeDirectoryClient.deleteWithResponse(recursive, requestConditions, timeout, context);
    }

    /**
     * Deletes the specified sub-directory in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExists#String -->
     * <pre>
     * boolean result = client.deleteSubdirectoryIfExists&#40;directoryName&#41;;
     * System.out.println&#40;&quot;Delete request completed: &quot; + result&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExists#String -->
     *
     * @param subdirectoryName Name of the subdirectory to delete.
     * @return {@code true} if subdirectory is successfully deleted, {@code false} if subdirectory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean deleteSubdirectoryIfExists(String subdirectoryName) {
        return deleteSubdirectoryIfExistsWithResponse(subdirectoryName, new DataLakePathDeleteOptions()
            .setIsRecursive(false).setRequestConditions(new DataLakeRequestConditions()), null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified subdirectory in the directory if it exists.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/delete">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions requestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * boolean recursive = false; &#47;&#47; Default value
     * DataLakePathDeleteOptions options = new DataLakePathDeleteOptions&#40;&#41;.setIsRecursive&#40;recursive&#41;
     *     .setRequestConditions&#40;requestConditions&#41;;
     *
     * Response&lt;Boolean&gt; response = client.deleteSubdirectoryIfExistsWithResponse&#40;directoryName, options,
     *     timeout, new Context&#40;key1, value1&#41;&#41;;
     * if &#40;response.getStatusCode&#40;&#41; == 404&#41; &#123;
     *     System.out.println&#40;&quot;Does not exist.&quot;&#41;;
     * &#125; else &#123;
     *     System.out.printf&#40;&quot;Delete completed with status %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.deleteSubdirectoryIfExistsWithResponse#String-DataLakePathDeleteOptions-Duration-Context -->
     *
     * @param subdirectoryName Name of the sub-directory to delete.
     * @param options {@link DataLakePathDeleteOptions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers. If {@link Response}'s status code is 200, the specified
     * subdirectory was successfully deleted. If status code is 404, the specified subdirectory does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> deleteSubdirectoryIfExistsWithResponse(String subdirectoryName,
        DataLakePathDeleteOptions options, Duration timeout, Context context) {
        DataLakeDirectoryClient dataLakeDirectoryClient = this.getSubdirectoryClient(subdirectoryName);
        return dataLakeDirectoryClient.deleteIfExistsWithResponse(options, timeout, context);
//        DataLakeDirectoryAsyncClient dataLakeDirectoryClient = dataLakeDirectoryAsyncClient
//            .getSubdirectoryAsyncClient(subdirectoryName);
//        return StorageImplUtils.blockWithOptionalTimeout(dataLakeDirectoryClient.deleteIfExistsWithResponse(options,
//            context), timeout);
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String-String -->
     * <pre>
     * DataLakeDirectoryClient renamedClient = client.rename&#40;fileSystemName, destinationPath&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.rename#String-String -->
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @return A {@link DataLakeDirectoryClient} used to interact with the new directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeDirectoryClient rename(String destinationFileSystem, String destinationPath) {
        return renameWithResponse(destinationFileSystem, destinationPath, null, null, null, Context.NONE).getValue();
    }

    /**
     * Moves the directory to another location within the file system.
     * For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/path/create">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context -->
     * <pre>
     * DataLakeRequestConditions sourceRequestConditions = new DataLakeRequestConditions&#40;&#41;
     *     .setLeaseId&#40;leaseId&#41;;
     * DataLakeRequestConditions destinationRequestConditions = new DataLakeRequestConditions&#40;&#41;;
     *
     * DataLakeDirectoryClient newRenamedClient = client.renameWithResponse&#40;fileSystemName, destinationPath,
     *     sourceRequestConditions, destinationRequestConditions, timeout, new Context&#40;key1, value1&#41;&#41;.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Directory Client has been renamed&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.renameWithResponse#String-String-DataLakeRequestConditions-DataLakeRequestConditions-Duration-Context -->
     *
     * @param destinationFileSystem The file system of the destination within the account.
     * {@code null} for the current file system.
     * @param destinationPath Relative path from the file system to rename the directory to, excludes the file system
     * name. For example if you want to move a directory with fileSystem = "myfilesystem", path = "mydir/mysubdir" to
     * another path in myfilesystem (ex: newdir) then set the destinationPath = "newdir"
     * @param sourceRequestConditions {@link DataLakeRequestConditions} against the source.
     * @param destinationRequestConditions {@link DataLakeRequestConditions} against the destination.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} that contains a
     * {@link DataLakeDirectoryClient} used to interact with the directory created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeDirectoryClient> renameWithResponse(String destinationFileSystem, String destinationPath,
        DataLakeRequestConditions sourceRequestConditions, DataLakeRequestConditions destinationRequestConditions,
        Duration timeout, Context context) {
        Response<DataLakePathClient> response = renameWithResponseWithTimeout(destinationFileSystem, destinationPath,
            sourceRequestConditions, destinationRequestConditions, timeout, context);
        return new SimpleResponse<>(response, new DataLakeDirectoryClient(response.getValue()));
    }

    /**
     * Returns a lazy loaded list of files/directories in this directory. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths -->
     * <pre>
     * client.listPaths&#40;&#41;.forEach&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths -->
     *
     * @return The list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PathItem> listPaths() {
        return this.listPaths(false, false, null, null);
    }

    /**
     * Returns a lazy loaded list of files/directories in this directory. The returned {@link PagedIterable} can be
     * consumed while new items are automatically retrieved as needed. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/datalakestoragegen2/filesystem/list#filesystem">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths#boolean-boolean-Integer-Duration -->
     * <pre>
     * client.listPaths&#40;false, false, 10, timeout&#41;
     *     .forEach&#40;path -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, path.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeDirectoryClient.listPaths#boolean-boolean-Integer-Duration -->
     *
     * @param recursive Specifies if the call should recursively include all paths.
     * @param userPrincipleNameReturned If "true", the user identity values returned in the x-ms-owner, x-ms-group,
     * and x-ms-acl response headers will be transformed from Azure Active Directory Object IDs to User Principal Names.
     * If "false", the values will be returned as Azure Active Directory Object IDs.
     * The default value is false. Note that group and application Object IDs are not translated because they do not
     * have unique friendly names.
     * @param maxResults Specifies the maximum number of blobs to return per page, including all BlobPrefix elements. If
     * the request does not specify maxResults or specifies a value greater than 5,000, the server will return up to
     * 5,000 items per page. If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over this value.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of files/directories.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PathItem> listPaths(boolean recursive, boolean userPrincipleNameReturned, Integer maxResults,
        Duration timeout) {
        BiFunction<String, Integer, PagedResponse<PathItem>> retriever = (marker, pageSize) -> {
            Callable<ResponseBase<FileSystemsListPathsHeaders, PathList>> operation
                = () -> this.fileSystemDataLakeStorage.getFileSystems()
                .listPathsWithResponse(recursive, null, null, marker, getDirectoryPath(),
                    pageSize == null ? maxResults : pageSize, userPrincipleNameReturned, Context.NONE);

            ResponseBase<FileSystemsListPathsHeaders, PathList> response = StorageImplUtils.sendRequest(operation,
                timeout, DataLakeStorageException.class);

            List<PathItem> value = response.getValue() == null
                ? Collections.emptyList()
                : response.getValue().getPaths().stream()
                .map(Transforms::toPathItem)
                .collect(Collectors.toList());

            return new PagedResponseBase<>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                value,
                response.getDeserializedHeaders().getXMsContinuation(),
                response.getDeserializedHeaders());
        };


        return new PagedIterable<>(pageSize -> retriever.apply(null, pageSize), retriever);
    }

    /**
     * Prepares a SpecializedBlobClientBuilder with the pathname appended to the end of the current BlockBlobClient's
     * url
     * @param pathName The name of the path to append
     * @return {@link SpecializedBlobClientBuilder}
     */
    SpecializedBlobClientBuilder prepareBuilderAppendPath(String pathName) {
        String blobUrl = DataLakeImplUtils.endpointToDesiredEndpoint(getPathUrl(), "blob", "dfs");

        return new SpecializedBlobClientBuilder()
            .pipeline(getHttpPipeline())
            .serviceVersion(TransformUtils.toBlobServiceVersion(getServiceVersion()))
            .endpoint(blobUrl)
            .blobName(pathName);
    }
}
