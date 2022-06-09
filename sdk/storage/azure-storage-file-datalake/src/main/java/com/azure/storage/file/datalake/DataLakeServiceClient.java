// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.file.datalake.implementation.util.DataLakeImplUtils;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;


/**
 * Client to a storage account. It may only be instantiated through a {@link DataLakeServiceClientBuilder}. This class
 * does not hold any state about a particular storage account but is instead a convenient way of sending off appropriate
 * requests to the resource on the service. It may also be used to construct URLs to file systems, files and
 * directories.
 *
 * <p>
 * This client contains operations on a data lake service account. Operations on a file system are available on
 * {@link DataLakeFileSystemClient} through {@link #getFileSystemClient(String)}, and operations on a file or directory
 * are available on {@link DataLakeFileClient} and {@link DataLakeDirectoryClient} respectively.
 */
@ServiceClient(builder = DataLakeServiceClientBuilder.class)
public class DataLakeServiceClient {

    private static final ClientLogger LOGGER = new ClientLogger(DataLakeServiceClient.class);
    private final DataLakeServiceAsyncClient dataLakeServiceAsyncClient;
    private final BlobServiceClient blobServiceClient;

    /**
     * Package-private constructor for use by {@link DataLakeServiceClientBuilder}.
     *
     * @param dataLakeServiceAsyncClient the async data lake service client.
     * @param blobServiceClient the sync blob service client.
     */
    DataLakeServiceClient(DataLakeServiceAsyncClient dataLakeServiceAsyncClient, BlobServiceClient blobServiceClient) {
        this.dataLakeServiceAsyncClient = dataLakeServiceAsyncClient;
        this.blobServiceClient = blobServiceClient;
    }

    /**
     * Initializes a {@link DataLakeFileSystemClient} object pointing to the specified file system. This method does
     * not create a file system. It simply constructs the URL to the file system and offers access to methods relevant
     * to file systems.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.getFileSystemClient#String -->
     * <pre>
     * DataLakeFileSystemClient dataLakeFileSystemClient = client.getFileSystemClient&#40;&quot;fileSystemName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.getFileSystemClient#String -->
     *
     * @param fileSystemName The name of the file system to point to.
     * @return A {@link DataLakeFileSystemClient} object pointing to the specified file system
     */
    public DataLakeFileSystemClient getFileSystemClient(String fileSystemName) {
        return new DataLakeFileSystemClient(dataLakeServiceAsyncClient.getFileSystemAsyncClient(fileSystemName),
            blobServiceClient.getBlobContainerClient(fileSystemName));
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    public HttpPipeline getHttpPipeline() {
        return dataLakeServiceAsyncClient.getHttpPipeline();
    }

    /**
     * Gets the service version the client is using.
     *
     * @return the service version the client is using.
     */
    public DataLakeServiceVersion getServiceVersion() {
        return this.dataLakeServiceAsyncClient.getServiceVersion();
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystem#String -->
     * <pre>
     * DataLakeFileSystemClient dataLakeFileSystemClient = client.createFileSystem&#40;&quot;fileSystemName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystem#String -->
     *
     * @param fileSystemName Name of the file system to create
     * @return The {@link DataLakeFileSystemClient} used to interact with the file system created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileSystemClient createFileSystem(String fileSystemName) {
        return createFileSystemWithResponse(fileSystemName, null, null, Context.NONE).getValue();
    }

    /**
     * Creates a new file system within a storage account. If a file system with the same name already exists, the
     * operation fails. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/create-container">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystemWithResponse#String-Map-PublicAccessType-Context -->
     * <pre>
     * Map&lt;String, String&gt; metadata = Collections.singletonMap&#40;&quot;metadata&quot;, &quot;value&quot;&#41;;
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * DataLakeFileSystemClient dataLakeFileSystemClient = client.createFileSystemWithResponse&#40;
     *     &quot;fileSystemName&quot;,
     *     metadata,
     *     PublicAccessType.CONTAINER,
     *     context&#41;.getValue&#40;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.createFileSystemWithResponse#String-Map-PublicAccessType-Context -->
     *
     * @param fileSystemName Name of the file system to create
     * @param metadata Metadata to associate with the file system. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @param accessType Specifies how the data in this file system is available to the public. See the
     * x-ms-blob-public-access header in the Azure Docs for more information. Pass null for no public access.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileSystemClient}
     * used to interact with the file system created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileSystemClient> createFileSystemWithResponse(String fileSystemName,
        Map<String, String> metadata, PublicAccessType accessType, Context context) {
        DataLakeFileSystemClient client = getFileSystemClient(fileSystemName);

        return new SimpleResponse<>(client.createWithResponse(metadata, accessType, null, context), client);
    }

    /**
     * Deletes the specified file system in the storage account. If the file system doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystem#String -->
     * <pre>
     * try &#123;
     *     client.deleteFileSystem&#40;&quot;fileSystemName&quot;&#41;;
     *     System.out.printf&#40;&quot;Delete file system completed with status %n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Delete file system failed: %s%n&quot;, error&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystem#String -->
     *
     * @param fileSystemName Name of the file system to delete
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteFileSystem(String fileSystemName) {
        deleteFileSystemWithResponse(fileSystemName, null, Context.NONE).getValue();
    }

    /**
     * Deletes the specified file system in the storage account. If the file system doesn't exist the operation fails.
     * For more information see the <a href="https://docs.microsoft.com/rest/api/storageservices/delete-container">Azure
     * Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * System.out.printf&#40;&quot;Delete file system completed with status %d%n&quot;,
     *     client.deleteFileSystemWithResponse&#40;&quot;fileSystemName&quot;, new DataLakeRequestConditions&#40;&#41;, context&#41;
     *         .getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.deleteFileSystemWithResponse#String-DataLakeRequestConditions-Context -->
     *
     * @param fileSystemName Name of the file system to delete
     * @param requestConditions {@link DataLakeRequestConditions}
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteFileSystemWithResponse(String fileSystemName,
        DataLakeRequestConditions requestConditions, Context context) {
        return getFileSystemClient(fileSystemName).deleteWithResponse(requestConditions, null, context);
    }

    /**
     * Gets the URL of the storage account represented by this client.
     *
     * @return the URL.
     */
    public String getAccountUrl() {
        return dataLakeServiceAsyncClient.getAccountUrl();
    }

    /**
     * Returns a lazy loaded list of file systems in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems -->
     * <pre>
     * client.listFileSystems&#40;&#41;.forEach&#40;fileSystem -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;, fileSystem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems -->
     *
     * @return The list of file systems.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FileSystemItem> listFileSystems() {
        return this.listFileSystems(new ListFileSystemsOptions(), null);
    }

    /**
     * Returns a lazy loaded list of file systems in this account. The returned {@link PagedIterable} can be consumed
     * while new items are automatically retrieved as needed. For more information, see the <a
     * href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems#ListFileSystemsOptions-Duration -->
     * <pre>
     * ListFileSystemsOptions options = new ListFileSystemsOptions&#40;&#41;
     *     .setPrefix&#40;&quot;filSystemNamePrefixToMatch&quot;&#41;
     *     .setDetails&#40;new FileSystemListDetails&#40;&#41;.setRetrieveMetadata&#40;true&#41;&#41;;
     *
     * client.listFileSystems&#40;options, timeout&#41;.forEach&#40;fileSystem -&gt; System.out.printf&#40;&quot;Name: %s%n&quot;,
     *     fileSystem.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.listFileSystems#ListFileSystemsOptions-Duration -->
     *
     * @param options A {@link ListFileSystemsOptions} which specifies what data should be returned by the service.
     * If iterating by page, the page size passed to byPage methods such as
     * {@link PagedIterable#iterableByPage(int)} will be preferred over the value set on these options.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @return The list of file systems.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<FileSystemItem> listFileSystems(ListFileSystemsOptions options, Duration timeout) {
        return new PagedIterable<>(dataLakeServiceAsyncClient.listFileSystemsWithOptionalTimeout(options, timeout));
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.getProperties -->
     * <pre>
     * DataLakeServiceProperties properties = client.getProperties&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b%n&quot;,
     *     properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *     properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.getProperties -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @return The resource properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeServiceProperties getProperties() {
        return getPropertiesWithResponse(null, Context.NONE).getValue();
    }

    /**
     * Returns the resource's metadata and properties.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.getPropertiesWithResponse#Duration-Context -->
     * <pre>
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     * properties = client.getPropertiesWithResponse&#40;timeout, context&#41;.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Hour metrics enabled: %b, Minute metrics enabled: %b%n&quot;,
     *     properties.getHourMetrics&#40;&#41;.isEnabled&#40;&#41;,
     *     properties.getMinuteMetrics&#40;&#41;.isEnabled&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.getPropertiesWithResponse#Duration-Context -->
     *
     * <p>For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/get-blob-properties">Azure Docs</a></p>
     *
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing the resource properties and metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeServiceProperties> getPropertiesWithResponse(Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<BlobServiceProperties> response = blobServiceClient.getPropertiesWithResponse(timeout, context);
            return new SimpleResponse<>(response, Transforms.toDataLakeServiceProperties(response.getValue()));
        }, LOGGER);
    }

    /**
     * Sets properties for a storage account's DataLake service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.setProperties#DataLakeServiceProperties -->
     * <pre>
     * DataLakeRetentionPolicy loggingRetentionPolicy = new DataLakeRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;3&#41;;
     * DataLakeRetentionPolicy metricsRetentionPolicy = new DataLakeRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;1&#41;;
     *
     * DataLakeServiceProperties properties = new DataLakeServiceProperties&#40;&#41;
     *     .setLogging&#40;new DataLakeAnalyticsLogging&#40;&#41;
     *         .setWrite&#40;true&#41;
     *         .setDelete&#40;true&#41;
     *         .setRetentionPolicy&#40;loggingRetentionPolicy&#41;&#41;
     *     .setHourMetrics&#40;new DataLakeMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;
     *     .setMinuteMetrics&#40;new DataLakeMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;;
     *
     * try &#123;
     *     client.setProperties&#40;properties&#41;;
     *     System.out.printf&#40;&quot;Setting properties completed%n&quot;&#41;;
     * &#125; catch &#40;UnsupportedOperationException error&#41; &#123;
     *     System.out.printf&#40;&quot;Setting properties failed: %s%n&quot;, error&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.setProperties#DataLakeServiceProperties -->
     *
     * @param properties Configures the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void setProperties(DataLakeServiceProperties properties) {
        setPropertiesWithResponse(properties, null, Context.NONE);
    }

    /**
     * Sets properties for a storage account's DataLake service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     * <p>This method checks to ensure the properties being sent follow the specifications indicated in the Azure Docs.
     * If CORS policies are set, CORS parameters that are not set default to the empty string.</p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.setPropertiesWithResponse#DataLakeServiceProperties-Duration-Context -->
     * <pre>
     * loggingRetentionPolicy = new DataLakeRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;3&#41;;
     * metricsRetentionPolicy = new DataLakeRetentionPolicy&#40;&#41;.setEnabled&#40;true&#41;.setDays&#40;1&#41;;
     *
     * properties = new DataLakeServiceProperties&#40;&#41;
     *     .setLogging&#40;new DataLakeAnalyticsLogging&#40;&#41;
     *         .setWrite&#40;true&#41;
     *         .setDelete&#40;true&#41;
     *         .setRetentionPolicy&#40;loggingRetentionPolicy&#41;&#41;
     *     .setHourMetrics&#40;new DataLakeMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;
     *     .setMinuteMetrics&#40;new DataLakeMetrics&#40;&#41;
     *         .setEnabled&#40;true&#41;
     *         .setRetentionPolicy&#40;metricsRetentionPolicy&#41;&#41;;
     *
     * Context context = new Context&#40;&quot;Key&quot;, &quot;Value&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Setting properties completed with status %d%n&quot;,
     *     client.setPropertiesWithResponse&#40;properties, timeout, context&#41;.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.setPropertiesWithResponse#DataLakeServiceProperties-Duration-Context -->
     *
     * @param properties Configures the service.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The storage account properties.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> setPropertiesWithResponse(DataLakeServiceProperties properties, Duration timeout,
        Context context) {
        return DataLakeImplUtils.returnOrConvertException(() ->
            blobServiceClient.setPropertiesWithResponse(Transforms.toBlobServiceProperties(properties),
                timeout, context), LOGGER);
    }

    /**
     * Gets a user delegation key for use with this account's data lake storage. Note: This method call is only valid
     * when using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime -->
     * <pre>
     * System.out.printf&#40;&quot;User delegation key: %s%n&quot;,
     *     client.getUserDelegationKey&#40;delegationKeyStartTime, delegationKeyExpiryTime&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKey#OffsetDateTime-OffsetDateTime -->
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @return The user delegation key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UserDelegationKey getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry) {
        return getUserDelegationKeyWithResponse(start, expiry, null, Context.NONE).getValue();
    }

    /**
     * Gets a user delegation key for use with this account's data lake storage. Note: This method call is only valid
     * when using {@link TokenCredential} in this object's {@link HttpPipeline}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context -->
     * <pre>
     * System.out.printf&#40;&quot;User delegation key: %s%n&quot;,
     *     client.getUserDelegationKeyWithResponse&#40;delegationKeyStartTime, delegationKeyExpiryTime, timeout, context&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.getUserDelegationKeyWithResponse#OffsetDateTime-OffsetDateTime-Duration-Context -->
     *
     * @param start Start time for the key's validity. Null indicates immediate start.
     * @param expiry Expiration of the key's validity.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the user delegation key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UserDelegationKey> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry,
        Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<com.azure.storage.blob.models.UserDelegationKey> response = blobServiceClient
                .getUserDelegationKeyWithResponse(start, expiry, timeout, context);
            return new SimpleResponse<>(response, Transforms.toDataLakeUserDelegationKey(response.getValue()));
        }, LOGGER);
    }

    /**
     * Get associated account name.
     *
     * @return account name associated with this storage resource.
     */
    public String getAccountName() {
        return this.dataLakeServiceAsyncClient.getAccountName();
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to file
     * systems and file shares.</p>
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setBlobAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = client.generateAccountSas&#40;sasValues&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues) {
        return dataLakeServiceAsyncClient.generateAccountSas(accountSasSignatureValues);
    }

    /**
     * Generates an account SAS for the Azure Storage account using the specified {@link AccountSasSignatureValues}.
     * <p>Note : The client must be authenticated via {@link StorageSharedKeyCredential}
     * <p>See {@link AccountSasSignatureValues} for more information on how to construct an account SAS.</p>
     *
     * <p>The snippet below generates a SAS that lasts for two days and gives the user read and list access to file
     * systems and file shares.</p>
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues-Context -->
     * <pre>
     * AccountSasPermission permissions = new AccountSasPermission&#40;&#41;
     *     .setListPermission&#40;true&#41;
     *     .setReadPermission&#40;true&#41;;
     * AccountSasResourceType resourceTypes = new AccountSasResourceType&#40;&#41;.setContainer&#40;true&#41;;
     * AccountSasService services = new AccountSasService&#40;&#41;.setBlobAccess&#40;true&#41;.setFileAccess&#40;true&#41;;
     * OffsetDateTime expiryTime = OffsetDateTime.now&#40;&#41;.plus&#40;Duration.ofDays&#40;2&#41;&#41;;
     *
     * AccountSasSignatureValues sasValues =
     *     new AccountSasSignatureValues&#40;expiryTime, permissions, services, resourceTypes&#41;;
     *
     * &#47;&#47; Client must be authenticated via StorageSharedKeyCredential
     * String sas = client.generateAccountSas&#40;sasValues, new Context&#40;&quot;key&quot;, &quot;value&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.generateAccountSas#AccountSasSignatureValues-Context -->
     *
     * @param accountSasSignatureValues {@link AccountSasSignatureValues}
     * @param context Additional context that is passed through the code when generating a SAS.
     *
     * @return A {@code String} representing the SAS query parameters.
     */
    public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context) {
        return dataLakeServiceAsyncClient.generateAccountSas(accountSasSignatureValues, context);
    }

    /**
     * Restores a previously deleted file system.
     * If the file system associated with provided <code>deletedFileSystemName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the file system.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystem#String-String -->
     * <pre>
     * ListFileSystemsOptions listFileSystemsOptions = new ListFileSystemsOptions&#40;&#41;;
     * listFileSystemsOptions.getDetails&#40;&#41;.setRetrieveDeleted&#40;true&#41;;
     * client.listFileSystems&#40;listFileSystemsOptions, null&#41;.forEach&#40;
     *     deletedFileSystem -&gt; &#123;
     *         DataLakeFileSystemClient fileSystemClient = client.undeleteFileSystem&#40;
     *             deletedFileSystem.getName&#40;&#41;, deletedFileSystem.getVersion&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystem#String-String -->
     *
     * @param deletedFileSystemName The name of the previously deleted file system.
     * @param deletedFileSystemVersion The version of the previously deleted file system.
     * @return The {@link DataLakeFileSystemClient} used to interact with the restored file system.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataLakeFileSystemClient undeleteFileSystem(String deletedFileSystemName, String deletedFileSystemVersion) {
        return this.undeleteFileSystemWithResponse(
            new FileSystemUndeleteOptions(deletedFileSystemName, deletedFileSystemVersion), null,
            Context.NONE).getValue();
    }

    /**
     * Restores a previously deleted file system. The restored file system
     * will be renamed to the <code>destinationFileSystemName</code> if provided in <code>options</code>.
     * Otherwise <code>deletedFileSystemName</code> is used as destination file system name.
     * If the file system associated with provided <code>destinationFileSystemName</code>
     * already exists, this call will result in a 409 (conflict).
     * This API is only functional if Container Soft Delete is enabled
     * for the storage account associated with the file system.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystemWithResponse#FileSystemUndeleteOptions-Duration-Context -->
     * <pre>
     * ListFileSystemsOptions listFileSystemsOptions = new ListFileSystemsOptions&#40;&#41;;
     * listFileSystemsOptions.getDetails&#40;&#41;.setRetrieveDeleted&#40;true&#41;;
     * client.listFileSystems&#40;listFileSystemsOptions, null&#41;.forEach&#40;
     *     deletedFileSystem -&gt; &#123;
     *         DataLakeFileSystemClient fileSystemClient = client.undeleteFileSystemWithResponse&#40;
     *             new FileSystemUndeleteOptions&#40;deletedFileSystem.getName&#40;&#41;, deletedFileSystem.getVersion&#40;&#41;&#41;, timeout,
     *             context&#41;.getValue&#40;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.undeleteFileSystemWithResponse#FileSystemUndeleteOptions-Duration-Context -->
     *
     * @param options {@link FileSystemUndeleteOptions}.
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DataLakeFileSystemClient}
     * used to interact with the restored file system.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataLakeFileSystemClient> undeleteFileSystemWithResponse(
        FileSystemUndeleteOptions options, Duration timeout, Context context) {
        return DataLakeImplUtils.returnOrConvertException(() -> {
            Response<com.azure.storage.blob.BlobContainerClient> response = blobServiceClient
                .undeleteBlobContainerWithResponse(Transforms.toBlobContainerUndeleteOptions(options), timeout,
                    context);
            return new SimpleResponse<>(response, getFileSystemClient(response.getValue().getBlobContainerName()));
        }, LOGGER);
    }

//    /**
//     * Renames an existing file system.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystem#String-String -->
//     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystem#String-String -->
//     *
//     * @param sourceFileSystemName The current name of the file system.
//     * @param destinationFileSystemName The new name of the file system.
//     * @return A {@link DataLakeFileSystemClient} used to interact with the renamed file system.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public DataLakeFileSystemClient renameFileSystem(String sourceFileSystemName, String destinationFileSystemName) {
//        return this.renameFileSystemWithResponse(sourceFileSystemName,
//            new FileSystemRenameOptions(destinationFileSystemName), null, Context.NONE).getValue();
//    }
//
//    /**
//     * Renames an existing file system.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <!-- src_embed com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystemWithResponse#String-FileSystemRenameOptions-Duration-Context -->
//     * <!-- end com.azure.storage.file.datalake.DataLakeServiceClient.renameFileSystemWithResponse#String-FileSystemRenameOptions-Duration-Context -->
//     *
//     * @param sourceFileSystemName The current name of the file system.
//     * @param options {@link FileSystemRenameOptions}
//     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
//     * @param context Additional context that is passed through the Http pipeline during the service call.
//     * @return A {@link Response} whose {@link Response#getValue() value} contains a
//     * {@link DataLakeFileSystemClient} used to interact with the renamed file system.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Response<DataLakeFileSystemClient> renameFileSystemWithResponse(String sourceFileSystemName,
//        FileSystemRenameOptions options, Duration timeout, Context context) {
//        return DataLakeImplUtils.returnOrConvertException(() -> {
//            Response<com.azure.storage.blob.BlobContainerClient> response = blobServiceClient
//                .renameBlobContainerWithResponse(sourceFileSystemName,
//                    Transforms.toBlobContainerRenameOptions(options), timeout, context);
//            return new SimpleResponse<>(response, getFileSystemClient(options.getDestinationFileSystemName()));
//        }, logger);
//    }
}
