```java
maven {
    parent : com.azure:azure-client-sdk-parent:1.7.0
    properties : com.azure:azure-storage-blob:12.36.0-beta.2
    name : Microsoft Azure client library for Blob Storage
    description : This module contains client library for Microsoft Azure Blob Storage.
    dependencies {
        // compile scope
        com.azure:azure-xml 1.2.1
        com.azure:azure-core 1.59.0
        com.azure:azure-core-http-netty 1.16.6
        com.azure:azure-storage-common 12.35.0-beta.2
        com.azure:azure-storage-internal-avro 12.21.0-beta.2
        // provided scope
        com.google.code.findbugs:jsr305 3.0.2
    }
}
module com.azure.storage.blob {
    requires transitive com.azure.storage.common;
    requires com.azure.storage.internal.avro;
    exports com.azure.storage.blob;
    exports com.azure.storage.blob.models;
    exports com.azure.storage.blob.options;
    exports com.azure.storage.blob.sas;
    exports com.azure.storage.blob.specialized;
    exports com.azure.storage.blob.implementation to com.azure.storage.blob.cryptography, com.azure.storage.blob.batch, com.azure.storage.file.datalake;
    exports com.azure.storage.blob.implementation.models to com.azure.storage.blob.batch, com.azure.storage.blob.cryptography;
    exports com.azure.storage.blob.implementation.util to com.azure.storage.blob.cryptography, com.azure.storage.file.datalake, com.azure.storage.blob.changefeed, com.azure.core, com.azure.storage.blob.batch, com.azure.storage.blob.nio;
    opens com.azure.storage.blob.models to com.azure.core;
    opens com.azure.storage.blob.implementation to com.azure.core;
    opens com.azure.storage.blob.implementation.models to com.azure.core;
}
package com.azure.storage.blob {
    public class BlobAsyncClient extends BlobAsyncClientBase {
        public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = BlobConstants.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = BlobConstants.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
        public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = BlobConstants.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;
        protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey)
        protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey, EncryptionScope encryptionScope)
        protected BlobAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey, EncryptionScope encryptionScope, String versionId)
        public AppendBlobAsyncClient getAppendBlobAsyncClient()
        public BlockBlobAsyncClient getBlockBlobAsyncClient()
        @Override public BlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey)
        @Override public BlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope)
        public PageBlobAsyncClient getPageBlobAsyncClient()
        @Override public BlobAsyncClient getSnapshotClient(String snapshot)
        public Mono<BlockBlobItem> upload(BinaryData data)
        public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions)
        public Mono<BlockBlobItem> upload(BinaryData data, boolean overwrite)
        public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions, boolean overwrite)
        @Deprecated protected AsynchronousFileChannel uploadFileResourceSupplier(String filePath)
        public Mono<Void> uploadFromFile(String filePath)
        public Mono<Void> uploadFromFile(String filePath, boolean overwrite)
        public Mono<Void> uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions)
        public Mono<Response<BlockBlobItem>> uploadFromFileWithResponse(BlobUploadFromFileOptions options)
        public Mono<Response<BlockBlobItem>> uploadWithResponse(BlobParallelUploadOptions options)
        public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions)
        @Override public BlobAsyncClient getVersionClient(String versionId)
    }
    @ServiceClient(builder  =  BlobClientBuilder)
    public class BlobClient extends BlobClientBase {
        public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = BlobConstants.BLOB_DEFAULT_UPLOAD_BLOCK_SIZE;
        public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = BlobConstants.BLOB_DEFAULT_NUMBER_OF_BUFFERS;
        public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = BlobConstants.BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE;
        protected BlobClient(BlobAsyncClient client)
        protected BlobClient(BlobAsyncClient client, HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey, EncryptionScope encryptionScope, String versionId)
        // Service Methods:
        public void upload(InputStream data)
        public void upload(BinaryData data)
        public void upload(InputStream data, long length)
        public void upload(InputStream data, boolean overwrite)
        public void upload(BinaryData data, boolean overwrite)
        public void upload(InputStream data, long length, boolean overwrite)
        public void uploadFromFile(String filePath)
        public void uploadFromFile(String filePath, boolean overwrite)
        public void uploadFromFile(String filePath, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions, Duration timeout)
        public Response<BlockBlobItem> uploadFromFileWithResponse(BlobUploadFromFileOptions options, Duration timeout, Context context)
        @Deprecated public Response<BlockBlobItem> uploadWithResponse(BlobParallelUploadOptions options, Context context)
        public Response<BlockBlobItem> uploadWithResponse(BlobParallelUploadOptions options, Duration timeout, Context context)
        @Deprecated public void uploadWithResponse(InputStream data, long length, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions, Duration timeout, Context context)
        // Non-Service Methods:
        public AppendBlobClient getAppendBlobClient()
        public BlockBlobClient getBlockBlobClient()
        @Override public BlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey)
        @Override public BlobClient getEncryptionScopeClient(String encryptionScope)
        public PageBlobClient getPageBlobClient()
        @Override public BlobClient getSnapshotClient(String snapshot)
        @Override public BlobClient getVersionClient(String versionId)
    }
    @ServiceClientBuilder(serviceClients  =  { BlobClient, BlobAsyncClient })
    public final class BlobClientBuilder implements TokenCredentialTrait<BlobClientBuilder> , ConnectionStringTrait<BlobClientBuilder> , AzureNamedKeyCredentialTrait<BlobClientBuilder> , AzureSasCredentialTrait<BlobClientBuilder> , HttpTrait<BlobClientBuilder> , ConfigurationTrait<BlobClientBuilder> , EndpointTrait<BlobClientBuilder> {
        public BlobClientBuilder()
        @Override public BlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy)
        public BlobClientBuilder setAnonymousAccess()
        public BlobClientBuilder audience(BlobAudience audience)
        public BlobClientBuilder blobName(String blobName)
        @Override public BlobClientBuilder clientOptions(ClientOptions clientOptions)
        @Override public BlobClientBuilder configuration(Configuration configuration)
        @Override public BlobClientBuilder connectionString(String connectionString)
        public BlobClientBuilder containerName(String containerName)
        public BlobClientBuilder credential(StorageSharedKeyCredential credential)
        @Override public BlobClientBuilder credential(AzureNamedKeyCredential credential)
        @Override public BlobClientBuilder credential(TokenCredential credential)
        @Override public BlobClientBuilder credential(AzureSasCredential credential)
        public BlobClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey)
        public static HttpLogOptions getDefaultHttpLogOptions()
        public BlobClientBuilder encryptionScope(String encryptionScope)
        @Override public BlobClientBuilder endpoint(String endpoint)
        @Override public BlobClientBuilder httpClient(HttpClient httpClient)
        @Override public BlobClientBuilder httpLogOptions(HttpLogOptions logOptions)
        @Override public BlobClientBuilder pipeline(HttpPipeline httpPipeline)
        public BlobClientBuilder retryOptions(RequestRetryOptions retryOptions)
        @Override public BlobClientBuilder retryOptions(RetryOptions retryOptions)
        public BlobClientBuilder sasToken(String sasToken)
        public BlobClientBuilder serviceVersion(BlobServiceVersion version)
        public BlobClientBuilder snapshot(String snapshot)
        public BlobClientBuilder versionId(String versionId)
        public BlobAsyncClient buildAsyncClient()
        public BlobClient buildClient()
    }
    @ServiceClient(builder  =  BlobContainerClientBuilder, isAsync  =  true)
    public final class BlobContainerAsyncClient {
        public static final String ROOT_CONTAINER_NAME = BlobConstants.ROOT_CONTAINER_NAME;
        public static final String STATIC_WEBSITE_CONTAINER_NAME = BlobConstants.STATIC_WEBSITE_CONTAINER_NAME;
        public static final String LOG_CONTAINER_NAME = BlobConstants.LOG_CONTAINER_NAME;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public Mono<BlobContainerAccessPolicies> getAccessPolicy()
        public Mono<Void> setAccessPolicy(PublicAccessType accessType, List<BlobSignedIdentifier> identifiers)
        public Mono<Response<BlobContainerAccessPolicies>> getAccessPolicyWithResponse(String leaseId)
        public Mono<Response<Void>> setAccessPolicyWithResponse(PublicAccessType accessType, List<BlobSignedIdentifier> identifiers, BlobRequestConditions requestConditions)
        public Mono<StorageAccountInfo> getAccountInfo()
        public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse()
        public Mono<Void> create()
        public Mono<Boolean> createIfNotExists()
        public Mono<Response<Boolean>> createIfNotExistsWithResponse(BlobContainerCreateOptions options)
        public Mono<Response<Void>> createWithResponse(Map<String, String> metadata, PublicAccessType accessType)
        public Mono<Void> delete()
        public Mono<Boolean> deleteIfExists()
        public Mono<Response<Boolean>> deleteIfExistsWithResponse(BlobRequestConditions requestConditions)
        public Mono<Response<Void>> deleteWithResponse(BlobRequestConditions requestConditions)
        public Mono<Boolean> exists()
        public Mono<Response<Boolean>> existsWithResponse()
        public PagedFlux<TaggedBlobItem> findBlobsByTags(String query)
        public PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options)
        public PagedFlux<BlobItem> listBlobs()
        public PagedFlux<BlobItem> listBlobs(ListBlobsOptions options)
        public PagedFlux<BlobItem> listBlobs(ListBlobsOptions options, String continuationToken)
        public PagedFlux<BlobItem> listBlobsByHierarchy(String directory)
        public PagedFlux<BlobItem> listBlobsByHierarchy(String delimiter, ListBlobsOptions options)
        public Mono<Void> setMetadata(Map<String, String> metadata)
        public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions)
        public Mono<BlobContainerProperties> getProperties()
        public Mono<Response<BlobContainerProperties>> getPropertiesWithResponse(String leaseId)
        // Non-Service Methods:
        public String getAccountName()
        public String getAccountUrl()
        public BlobAsyncClient getBlobAsyncClient(String blobName)
        public BlobAsyncClient getBlobAsyncClient(String blobName, String snapshot)
        public String getBlobContainerName()
        public String getBlobContainerUrl()
        public BlobAsyncClient getBlobVersionAsyncClient(String blobName, String versionId)
        public CpkInfo getCustomerProvidedKey()
        public String getEncryptionScope()
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Consumer<String> stringToSignHandler, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Consumer<String> stringToSignHandler, Context context)
        public HttpPipeline getHttpPipeline()
        public BlobServiceAsyncClient getServiceAsyncClient()
        public BlobServiceVersion getServiceVersion()
    }
    @ServiceClient(builder  =  BlobContainerClientBuilder)
    public final class BlobContainerClient {
        public static final String ROOT_CONTAINER_NAME = BlobConstants.ROOT_CONTAINER_NAME;
        public static final String STATIC_WEBSITE_CONTAINER_NAME = BlobConstants.STATIC_WEBSITE_CONTAINER_NAME;
        public static final String LOG_CONTAINER_NAME = BlobConstants.LOG_CONTAINER_NAME;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public BlobContainerAccessPolicies getAccessPolicy()
        public void setAccessPolicy(PublicAccessType accessType, List<BlobSignedIdentifier> identifiers)
        public Response<BlobContainerAccessPolicies> getAccessPolicyWithResponse(String leaseId, Duration timeout, Context context)
        public Response<Void> setAccessPolicyWithResponse(PublicAccessType accessType, List<BlobSignedIdentifier> identifiers, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public StorageAccountInfo getAccountInfo(Duration timeout)
        public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context)
        public void create()
        public boolean createIfNotExists()
        public Response<Boolean> createIfNotExistsWithResponse(BlobContainerCreateOptions options, Duration timeout, Context context)
        public Response<Void> createWithResponse(Map<String, String> metadata, PublicAccessType accessType, Duration timeout, Context context)
        public void delete()
        public boolean deleteIfExists()
        public Response<Boolean> deleteIfExistsWithResponse(BlobRequestConditions requestConditions, Duration timeout, Context context)
        public Response<Void> deleteWithResponse(BlobRequestConditions requestConditions, Duration timeout, Context context)
        public boolean exists()
        public Response<Boolean> existsWithResponse(Duration timeout, Context context)
        public PagedIterable<TaggedBlobItem> findBlobsByTags(String query)
        public PagedIterable<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context)
        public PagedIterable<BlobItem> listBlobs()
        public PagedIterable<BlobItem> listBlobs(ListBlobsOptions options, Duration timeout)
        public PagedIterable<BlobItem> listBlobs(ListBlobsOptions options, String continuationToken, Duration timeout)
        public PagedIterable<BlobItem> listBlobsByHierarchy(String directory)
        public PagedIterable<BlobItem> listBlobsByHierarchy(String delimiter, ListBlobsOptions options, Duration timeout)
        public void setMetadata(Map<String, String> metadata)
        public Response<Void> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public BlobContainerProperties getProperties()
        public Response<BlobContainerProperties> getPropertiesWithResponse(String leaseId, Duration timeout, Context context)
        // Non-Service Methods:
        public String getAccountName()
        public String getAccountUrl()
        public BlobClient getBlobClient(String blobName)
        public BlobClient getBlobClient(String blobName, String snapshot)
        public String getBlobContainerName()
        public String getBlobContainerUrl()
        public BlobClient getBlobVersionClient(String blobName, String versionId)
        public CpkInfo getCustomerProvidedKey()
        public String getEncryptionScope()
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Consumer<String> stringToSignHandler, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Consumer<String> stringToSignHandler, Context context)
        public HttpPipeline getHttpPipeline()
        public BlobServiceClient getServiceClient()
        public BlobServiceVersion getServiceVersion()
    }
    @ServiceClientBuilder(serviceClients  =  { BlobContainerClient, BlobContainerAsyncClient })
    public final class BlobContainerClientBuilder implements TokenCredentialTrait<BlobContainerClientBuilder> , ConnectionStringTrait<BlobContainerClientBuilder> , AzureSasCredentialTrait<BlobContainerClientBuilder> , AzureNamedKeyCredentialTrait<BlobContainerClientBuilder> , HttpTrait<BlobContainerClientBuilder> , ConfigurationTrait<BlobContainerClientBuilder> , EndpointTrait<BlobContainerClientBuilder> {
        public BlobContainerClientBuilder()
        @Override public BlobContainerClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy)
        public BlobContainerClientBuilder setAnonymousAccess()
        public BlobContainerClientBuilder audience(BlobAudience audience)
        public BlobContainerClientBuilder blobContainerEncryptionScope(BlobContainerEncryptionScope blobContainerEncryptionScope)
        @Override public BlobContainerClientBuilder clientOptions(ClientOptions clientOptions)
        @Override public BlobContainerClientBuilder configuration(Configuration configuration)
        @Override public BlobContainerClientBuilder connectionString(String connectionString)
        public BlobContainerClientBuilder containerName(String containerName)
        public BlobContainerClientBuilder credential(StorageSharedKeyCredential credential)
        @Override public BlobContainerClientBuilder credential(AzureNamedKeyCredential credential)
        @Override public BlobContainerClientBuilder credential(TokenCredential credential)
        @Override public BlobContainerClientBuilder credential(AzureSasCredential credential)
        public BlobContainerClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey)
        public static HttpLogOptions getDefaultHttpLogOptions()
        public BlobContainerClientBuilder encryptionScope(String encryptionScope)
        @Override public BlobContainerClientBuilder endpoint(String endpoint)
        @Override public BlobContainerClientBuilder httpClient(HttpClient httpClient)
        @Override public BlobContainerClientBuilder httpLogOptions(HttpLogOptions logOptions)
        @Override public BlobContainerClientBuilder pipeline(HttpPipeline httpPipeline)
        public BlobContainerClientBuilder retryOptions(RequestRetryOptions retryOptions)
        @Override public BlobContainerClientBuilder retryOptions(RetryOptions retryOptions)
        public BlobContainerClientBuilder sasToken(String sasToken)
        public BlobContainerClientBuilder serviceVersion(BlobServiceVersion version)
        public BlobContainerAsyncClient buildAsyncClient()
        public BlobContainerClient buildClient()
    }
    @ServiceClient(builder  =  BlobServiceClientBuilder, isAsync  =  true)
    public final class BlobServiceAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public Mono<StorageAccountInfo> getAccountInfo()
        public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse()
        public Mono<BlobContainerAsyncClient> createBlobContainer(String containerName)
        public Mono<BlobContainerAsyncClient> createBlobContainerIfNotExists(String containerName)
        public Mono<Response<BlobContainerAsyncClient>> createBlobContainerIfNotExistsWithResponse(String containerName, BlobContainerCreateOptions options)
        public Mono<Response<BlobContainerAsyncClient>> createBlobContainerWithResponse(String containerName, Map<String, String> metadata, PublicAccessType accessType)
        public Mono<Void> deleteBlobContainer(String containerName)
        public Mono<Boolean> deleteBlobContainerIfExists(String containerName)
        public Mono<Response<Boolean>> deleteBlobContainerIfExistsWithResponse(String containerName)
        public Mono<Response<Void>> deleteBlobContainerWithResponse(String containerName)
        public PagedFlux<TaggedBlobItem> findBlobsByTags(String query)
        public PagedFlux<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options)
        public PagedFlux<BlobContainerItem> listBlobContainers()
        public PagedFlux<BlobContainerItem> listBlobContainers(ListBlobContainersOptions options)
        public Mono<BlobServiceProperties> getProperties()
        public Mono<Void> setProperties(BlobServiceProperties properties)
        public Mono<Response<BlobServiceProperties>> getPropertiesWithResponse()
        public Mono<Response<Void>> setPropertiesWithResponse(BlobServiceProperties properties)
        public Mono<BlobServiceStatistics> getStatistics()
        public Mono<Response<BlobServiceStatistics>> getStatisticsWithResponse()
        public Mono<BlobContainerAsyncClient> undeleteBlobContainer(String deletedContainerName, String deletedContainerVersion)
        public Mono<Response<BlobContainerAsyncClient>> undeleteBlobContainerWithResponse(UndeleteBlobContainerOptions options)
        public Mono<UserDelegationKey> getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry)
        public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(BlobGetUserDelegationKeyOptions options)
        public Mono<Response<UserDelegationKey>> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry)
        // Non-Service Methods:
        public String getAccountName()
        public String getAccountUrl()
        public BlobContainerAsyncClient getBlobContainerAsyncClient(String containerName)
        public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues)
        public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context)
        public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Consumer<String> stringToSignHandler, Context context)
        public HttpPipeline getHttpPipeline()
        public BlobServiceVersion getServiceVersion()
    }
    @ServiceClient(builder  =  BlobServiceClientBuilder)
    public final class BlobServiceClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public StorageAccountInfo getAccountInfo()
        public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context)
        public BlobContainerClient createBlobContainer(String containerName)
        public BlobContainerClient createBlobContainerIfNotExists(String containerName)
        public Response<BlobContainerClient> createBlobContainerIfNotExistsWithResponse(String containerName, BlobContainerCreateOptions options, Context context)
        public Response<BlobContainerClient> createBlobContainerWithResponse(String containerName, Map<String, String> metadata, PublicAccessType accessType, Context context)
        public void deleteBlobContainer(String containerName)
        public boolean deleteBlobContainerIfExists(String containerName)
        public Response<Boolean> deleteBlobContainerIfExistsWithResponse(String containerName, Context context)
        public Response<Void> deleteBlobContainerWithResponse(String containerName, Context context)
        public PagedIterable<TaggedBlobItem> findBlobsByTags(String query)
        public PagedIterable<TaggedBlobItem> findBlobsByTags(FindBlobsOptions options, Duration timeout, Context context)
        public PagedIterable<BlobContainerItem> listBlobContainers()
        public PagedIterable<BlobContainerItem> listBlobContainers(ListBlobContainersOptions options, Duration timeout)
        public BlobServiceProperties getProperties()
        public void setProperties(BlobServiceProperties properties)
        public Response<BlobServiceProperties> getPropertiesWithResponse(Duration timeout, Context context)
        public Response<Void> setPropertiesWithResponse(BlobServiceProperties properties, Duration timeout, Context context)
        public BlobServiceStatistics getStatistics()
        public Response<BlobServiceStatistics> getStatisticsWithResponse(Duration timeout, Context context)
        public BlobContainerClient undeleteBlobContainer(String deletedContainerName, String deletedContainerVersion)
        public Response<BlobContainerClient> undeleteBlobContainerWithResponse(UndeleteBlobContainerOptions options, Duration timeout, Context context)
        public UserDelegationKey getUserDelegationKey(OffsetDateTime start, OffsetDateTime expiry)
        public Response<UserDelegationKey> getUserDelegationKeyWithResponse(BlobGetUserDelegationKeyOptions options, Duration timeout, Context context)
        public Response<UserDelegationKey> getUserDelegationKeyWithResponse(OffsetDateTime start, OffsetDateTime expiry, Duration timeout, Context context)
        // Non-Service Methods:
        public String getAccountName()
        public String getAccountUrl()
        public BlobContainerClient getBlobContainerClient(String containerName)
        public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues)
        public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Context context)
        public String generateAccountSas(AccountSasSignatureValues accountSasSignatureValues, Consumer<String> stringToSignHandler, Context context)
        public HttpPipeline getHttpPipeline()
        public BlobServiceVersion getServiceVersion()
    }
    @ServiceClientBuilder(serviceClients  =  { BlobServiceClient, BlobServiceAsyncClient })
    public final class BlobServiceClientBuilder implements TokenCredentialTrait<BlobServiceClientBuilder> , ConnectionStringTrait<BlobServiceClientBuilder> , AzureNamedKeyCredentialTrait<BlobServiceClientBuilder> , AzureSasCredentialTrait<BlobServiceClientBuilder> , HttpTrait<BlobServiceClientBuilder> , ConfigurationTrait<BlobServiceClientBuilder> , EndpointTrait<BlobServiceClientBuilder> {
        public BlobServiceClientBuilder()
        @Override public BlobServiceClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy)
        public BlobServiceClientBuilder audience(BlobAudience audience)
        public BlobServiceClientBuilder blobContainerEncryptionScope(BlobContainerEncryptionScope blobContainerEncryptionScope)
        @Override public BlobServiceClientBuilder clientOptions(ClientOptions clientOptions)
        @Override public BlobServiceClientBuilder configuration(Configuration configuration)
        @Override public BlobServiceClientBuilder connectionString(String connectionString)
        public BlobServiceClientBuilder credential(StorageSharedKeyCredential credential)
        @Override public BlobServiceClientBuilder credential(AzureNamedKeyCredential credential)
        @Override public BlobServiceClientBuilder credential(TokenCredential credential)
        @Override public BlobServiceClientBuilder credential(AzureSasCredential credential)
        public BlobServiceClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey)
        public static HttpLogOptions getDefaultHttpLogOptions()
        public BlobServiceClientBuilder encryptionScope(String encryptionScope)
        @Override public BlobServiceClientBuilder endpoint(String endpoint)
        @Override public BlobServiceClientBuilder httpClient(HttpClient httpClient)
        @Override public BlobServiceClientBuilder httpLogOptions(HttpLogOptions logOptions)
        @Override public BlobServiceClientBuilder pipeline(HttpPipeline httpPipeline)
        public BlobServiceClientBuilder retryOptions(RequestRetryOptions retryOptions)
        @Override public BlobServiceClientBuilder retryOptions(RetryOptions retryOptions)
        public BlobServiceClientBuilder sasToken(String sasToken)
        public BlobServiceClientBuilder serviceVersion(BlobServiceVersion version)
        public BlobServiceAsyncClient buildAsyncClient()
        public BlobServiceClient buildClient()
    }
    public enum BlobServiceVersion implements ServiceVersion {
        V2019_02_02("2019-02-02"),
        V2019_07_07("2019-07-07"),
        V2019_12_12("2019-12-12"),
        V2020_02_10("2020-02-10"),
        V2020_04_08("2020-04-08"),
        V2020_06_12("2020-06-12"),
        V2020_08_04("2020-08-04"),
        V2020_10_02("2020-10-02"),
        V2020_12_06("2020-12-06"),
        V2021_02_12("2021-02-12"),
        V2021_04_10("2021-04-10"),
        V2021_06_08("2021-06-08"),
        V2021_08_06("2021-08-06"),
        V2021_10_04("2021-10-04"),
        V2021_12_02("2021-12-02"),
        V2022_11_02("2022-11-02"),
        V2023_01_03("2023-01-03"),
        V2023_05_03("2023-05-03"),
        V2023_08_03("2023-08-03"),
        V2023_11_03("2023-11-03"),
        V2024_02_04("2024-02-04"),
        V2024_05_04("2024-05-04"),
        V2024_08_04("2024-08-04"),
        V2024_11_04("2024-11-04"),
        V2025_01_05("2025-01-05"),
        V2025_05_05("2025-05-05"),
        V2025_07_05("2025-07-05"),
        V2025_11_05("2025-11-05"),
        V2026_02_06("2026-02-06"),
        V2026_04_06("2026-04-06"),
        V2026_06_06("2026-06-06"),
        V2026_10_06("2026-10-06");
        public static BlobServiceVersion getLatest(// returns V2026_10_06 )
        @Override public String getVersion()
    }
    public final class BlobUrlParts {
        public BlobUrlParts()
        public String getAccountName()
        public BlobUrlParts setAccountName(String accountName)
        public String getBlobContainerName()
        public String getBlobName()
        public BlobUrlParts setBlobName(String blobName)
        public CommonSasQueryParameters getCommonSasQueryParameters()
        public BlobUrlParts setCommonSasQueryParameters(CommonSasQueryParameters commonSasQueryParameters)
        public BlobUrlParts setContainerName(String containerName)
        public String getHost()
        public BlobUrlParts setHost(String host)
        public static BlobUrlParts parse(String url)
        public static BlobUrlParts parse(URL url)
        public BlobUrlParts parseSasQueryParameters(String queryParams)
        @Deprecated public BlobServiceSasQueryParameters getSasQueryParameters()
        @Deprecated public BlobUrlParts setSasQueryParameters(BlobServiceSasQueryParameters blobServiceSasQueryParameters)
        public String getScheme()
        public BlobUrlParts setScheme(String scheme)
        public String getSnapshot()
        public BlobUrlParts setSnapshot(String snapshot)
        public URL toUrl()
        public Map<String, String[]> getUnparsedParameters()
        public BlobUrlParts setUnparsedParameters(Map<String, String[]> unparsedParameters)
        public String getVersionId()
        public BlobUrlParts setVersionId(String versionId)
    }
    public final class HttpGetterInfo {
        public HttpGetterInfo()
        public Long getCount()
        public HttpGetterInfo setCount(Long count)
        public String getETag()
        public HttpGetterInfo setETag(String eTag)
        public long getOffset()
        public HttpGetterInfo setOffset(long offset)
    }
    @Deprecated
    public interface ProgressReceiver extends ProgressListener {
        @Override default void handleProgress(long bytesTransferred)
        void reportProgress(long bytesTransferred)
    }
    @Deprecated
    public final class ProgressReporter {
        public ProgressReporter()
        @Deprecated public static Flux<ByteBuffer> addParallelProgressReporting(Flux<ByteBuffer> data, ProgressReceiver progressReceiver, Lock lock, AtomicLong totalProgress)
        @Deprecated public static Flux<ByteBuffer> addProgressReporting(Flux<ByteBuffer> data, ProgressReceiver progressReceiver)
    }
}
package com.azure.storage.blob.models {
    public final class AccessTier extends ExpandableStringEnum<AccessTier> {
        @Generated public static final AccessTier P4 = fromString("P4");
        @Generated public static final AccessTier P6 = fromString("P6");
        @Generated public static final AccessTier P10 = fromString("P10");
        @Generated public static final AccessTier P15 = fromString("P15");
        @Generated public static final AccessTier P20 = fromString("P20");
        @Generated public static final AccessTier P30 = fromString("P30");
        @Generated public static final AccessTier P40 = fromString("P40");
        @Generated public static final AccessTier P50 = fromString("P50");
        @Generated public static final AccessTier P60 = fromString("P60");
        @Generated public static final AccessTier P70 = fromString("P70");
        @Generated public static final AccessTier P80 = fromString("P80");
        @Generated public static final AccessTier HOT = fromString("Hot");
        @Generated public static final AccessTier COOL = fromString("Cool");
        @Generated public static final AccessTier ARCHIVE = fromString("Archive");
        @Generated public static final AccessTier PREMIUM = fromString("Premium");
        @Generated public static final AccessTier COLD = fromString("Cold");
        @Generated public static final AccessTier SMART = fromString("Smart");
        @Deprecated @Generated public AccessTier()
        @Generated public static AccessTier fromString(String name)
        @Generated public static Collection<AccessTier> values()
    }
    public enum AccountKind {
        STORAGE("Storage"),
        BLOB_STORAGE("BlobStorage"),
        STORAGE_V2("StorageV2"),
        FILE_STORAGE("FileStorage"),
        BLOCK_BLOB_STORAGE("BlockBlobStorage");
        public static AccountKind fromString(String value)
        @Override public String toString()
    }
    @Immutable
    public class AppendBlobItem {
        public AppendBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted, String encryptionKeySha256, String blobAppendOffset, Integer blobCommittedBlockCount)
        public AppendBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, String blobAppendOffset, Integer blobCommittedBlockCount)
        public AppendBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, String blobAppendOffset, Integer blobCommittedBlockCount, String versionId)
        public String getBlobAppendOffset()
        public Integer getBlobCommittedBlockCount()
        public byte[] getContentCrc64()
        public byte[] getContentMd5()
        public String getEncryptionKeySha256()
        public String getEncryptionScope()
        public String getETag()
        public OffsetDateTime getLastModified()
        public boolean isServerEncrypted()
        public String getVersionId()
    }
    @Fluent
    public final class AppendBlobRequestConditions extends BlobRequestConditions {
        public AppendBlobRequestConditions()
        public Long getAppendPosition()
        public AppendBlobRequestConditions setAppendPosition(Long appendPosition)
        @Override public AppendBlobRequestConditions setIfMatch(String ifMatch)
        @Override public AppendBlobRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public AppendBlobRequestConditions setIfNoneMatch(String ifNoneMatch)
        @Override public AppendBlobRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        @Override public AppendBlobRequestConditions setLeaseId(String leaseId)
        public Long getMaxSize()
        public AppendBlobRequestConditions setMaxSize(Long maxSize)
        @Override public AppendBlobRequestConditions setTagsConditions(String tagsConditions)
    }
    public final class ArchiveStatus extends ExpandableStringEnum<ArchiveStatus> {
        @Generated public static final ArchiveStatus REHYDRATE_PENDING_TO_HOT = fromString("rehydrate-pending-to-hot");
        @Generated public static final ArchiveStatus REHYDRATE_PENDING_TO_COOL = fromString("rehydrate-pending-to-cool");
        @Generated public static final ArchiveStatus REHYDRATE_PENDING_TO_COLD = fromString("rehydrate-pending-to-cold");
        @Generated public static final ArchiveStatus REHYDRATE_PENDING_TO_SMART = fromString("rehydrate-pending-to-smart");
        @Deprecated @Generated public ArchiveStatus()
        @Generated public static ArchiveStatus fromString(String name)
        @Generated public static Collection<ArchiveStatus> values()
    }
    @Fluent
    public final class BlobAccessPolicy implements XmlSerializable<BlobAccessPolicy> {
        @Generated public BlobAccessPolicy()
        @Generated public OffsetDateTime getExpiresOn()
        @Generated public BlobAccessPolicy setExpiresOn(OffsetDateTime expiresOn)
        @Generated public String getPermissions()
        @Generated public BlobAccessPolicy setPermissions(String permissions)
        @Generated public OffsetDateTime getStartsOn()
        @Generated public BlobAccessPolicy setStartsOn(OffsetDateTime startsOn)
    }
    @Fluent
    public final class BlobAnalyticsLogging implements XmlSerializable<BlobAnalyticsLogging> {
        @Generated public BlobAnalyticsLogging()
        @Generated public boolean isDelete()
        @Generated public BlobAnalyticsLogging setDelete(boolean delete)
        @Generated public boolean isRead()
        @Generated public BlobAnalyticsLogging setRead(boolean read)
        @Generated public BlobRetentionPolicy getRetentionPolicy()
        @Generated public BlobAnalyticsLogging setRetentionPolicy(BlobRetentionPolicy retentionPolicy)
        @Generated public String getVersion()
        @Generated public BlobAnalyticsLogging setVersion(String version)
        @Generated public boolean isWrite()
        @Generated public BlobAnalyticsLogging setWrite(boolean write)
    }
    public class BlobAudience extends ExpandableStringEnum<BlobAudience> {
        public static final BlobAudience AZURE_PUBLIC_CLOUD = fromString("https://storage.azure.com/");
        @Deprecated public BlobAudience()
        public static BlobAudience createBlobServiceAccountAudience(String storageAccountName)
        public static BlobAudience fromString(String audience)
        public static Collection<BlobAudience> values()
    }
    public class BlobBeginCopySourceRequestConditions extends RequestConditions {
        public BlobBeginCopySourceRequestConditions()
        @Override public BlobBeginCopySourceRequestConditions setIfMatch(String ifMatch)
        @Override public BlobBeginCopySourceRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public BlobBeginCopySourceRequestConditions setIfNoneMatch(String ifNoneMatch)
        @Override public BlobBeginCopySourceRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        public String getTagsConditions()
        public BlobBeginCopySourceRequestConditions setTagsConditions(String tagsConditions)
    }
    @Immutable
    public class BlobContainerAccessPolicies {
        public BlobContainerAccessPolicies(PublicAccessType blobAccessType, List<BlobSignedIdentifier> identifiers)
        public PublicAccessType getBlobAccessType()
        public List<BlobSignedIdentifier> getIdentifiers()
    }
    @Fluent
    public final class BlobContainerEncryptionScope {
        @Generated public BlobContainerEncryptionScope()
        @Generated public String getDefaultEncryptionScope()
        @Generated public BlobContainerEncryptionScope setDefaultEncryptionScope(String defaultEncryptionScope)
        @Generated public boolean isEncryptionScopeOverridePrevented()
        @Generated public BlobContainerEncryptionScope setEncryptionScopeOverridePrevented(Boolean encryptionScopeOverridePrevented)
    }
    @Fluent
    public final class BlobContainerItem implements XmlSerializable<BlobContainerItem> {
        @Generated public BlobContainerItem()
        @Generated public Boolean isDeleted()
        @Generated public BlobContainerItem setDeleted(Boolean deleted)
        @Generated public Map<String, String> getMetadata()
        @Generated public BlobContainerItem setMetadata(Map<String, String> metadata)
        @Generated public String getName()
        @Generated public BlobContainerItem setName(String name)
        @Generated public BlobContainerItemProperties getProperties()
        @Generated public BlobContainerItem setProperties(BlobContainerItemProperties properties)
        @Generated public String getVersion()
        @Generated public BlobContainerItem setVersion(String version)
    }
    @Fluent
    public final class BlobContainerItemProperties implements XmlSerializable<BlobContainerItemProperties> {
        @Generated public BlobContainerItemProperties()
        @Generated public String getDefaultEncryptionScope()
        @Generated public BlobContainerItemProperties setDefaultEncryptionScope(String defaultEncryptionScope)
        @Generated public OffsetDateTime getDeletedTime()
        @Generated public BlobContainerItemProperties setDeletedTime(OffsetDateTime deletedTime)
        @Generated public boolean isEncryptionScopeOverridePrevented()
        @Generated public BlobContainerItemProperties setEncryptionScopeOverridePrevented(boolean encryptionScopeOverridePrevented)
        @Generated public String getETag()
        @Generated public BlobContainerItemProperties setETag(String eTag)
        @Generated public Boolean isHasImmutabilityPolicy()
        @Generated public BlobContainerItemProperties setHasImmutabilityPolicy(Boolean hasImmutabilityPolicy)
        @Generated public Boolean isHasLegalHold()
        @Generated public BlobContainerItemProperties setHasLegalHold(Boolean hasLegalHold)
        @Generated public Boolean isImmutableStorageWithVersioningEnabled()
        @Generated public BlobContainerItemProperties setImmutableStorageWithVersioningEnabled(Boolean isImmutableStorageWithVersioningEnabled)
        @Generated public OffsetDateTime getLastModified()
        @Generated public BlobContainerItemProperties setLastModified(OffsetDateTime lastModified)
        @Generated public LeaseDurationType getLeaseDuration()
        @Generated public BlobContainerItemProperties setLeaseDuration(LeaseDurationType leaseDuration)
        @Generated public LeaseStateType getLeaseState()
        @Generated public BlobContainerItemProperties setLeaseState(LeaseStateType leaseState)
        @Generated public LeaseStatusType getLeaseStatus()
        @Generated public BlobContainerItemProperties setLeaseStatus(LeaseStatusType leaseStatus)
        @Generated public PublicAccessType getPublicAccess()
        @Generated public BlobContainerItemProperties setPublicAccess(PublicAccessType publicAccess)
        @Generated public Integer getRemainingRetentionDays()
        @Generated public BlobContainerItemProperties setRemainingRetentionDays(Integer remainingRetentionDays)
    }
    @Fluent
    public final class BlobContainerListDetails {
        public BlobContainerListDetails()
        public boolean getRetrieveDeleted()
        public BlobContainerListDetails setRetrieveDeleted(boolean retrieveDeleted)
        public boolean getRetrieveMetadata()
        public BlobContainerListDetails setRetrieveMetadata(boolean retrieveMetadata)
        public boolean getRetrieveSystemContainers()
        public BlobContainerListDetails setRetrieveSystemContainers(boolean retrieveSystemContainers)
        @Deprecated public ListBlobContainersIncludeType toIncludeType()
    }
    @Immutable
    public final class BlobContainerProperties {
        public BlobContainerProperties(Map<String, String> metadata, String eTag, OffsetDateTime lastModified, LeaseDurationType leaseDuration, LeaseStateType leaseState, LeaseStatusType leaseStatus, PublicAccessType blobPublicAccess, boolean hasImmutabilityPolicy, boolean hasLegalHold)
        public BlobContainerProperties(Map<String, String> metadata, String eTag, OffsetDateTime lastModified, LeaseDurationType leaseDuration, LeaseStateType leaseState, LeaseStatusType leaseStatus, PublicAccessType blobPublicAccess, boolean hasImmutabilityPolicy, boolean hasLegalHold, String defaultEncryptionScope, Boolean encryptionScopeOverridePrevented)
        public BlobContainerProperties(Map<String, String> metadata, String eTag, OffsetDateTime lastModified, LeaseDurationType leaseDuration, LeaseStateType leaseState, LeaseStatusType leaseStatus, PublicAccessType blobPublicAccess, boolean hasImmutabilityPolicy, boolean hasLegalHold, String defaultEncryptionScope, Boolean encryptionScopeOverridePrevented, Boolean isImmutableStorageWithVersioningEnabled)
        public PublicAccessType getBlobPublicAccess()
        public String getDefaultEncryptionScope()
        public Boolean isEncryptionScopeOverridePrevented()
        public String getETag()
        public boolean hasImmutabilityPolicy()
        public boolean hasLegalHold()
        public Boolean isImmutableStorageWithVersioningEnabled()
        public OffsetDateTime getLastModified()
        public LeaseDurationType getLeaseDuration()
        public LeaseStateType getLeaseState()
        public LeaseStatusType getLeaseStatus()
        public Map<String, String> getMetadata()
    }
    @Immutable
    public class BlobCopyInfo {
        public BlobCopyInfo(String copySource, String copyId, CopyStatusType copyStatus, String eTag, OffsetDateTime lastModified, String error)
        public BlobCopyInfo(String copySource, String copyId, CopyStatusType copyStatus, String eTag, OffsetDateTime lastModified, String error, String versionId)
        public BlobCopyInfo(String copySource, String copyId, CopyStatusType copyStatus, String eTag, OffsetDateTime lastModified, String error, String versionId, String encryptionScope)
        public String getCopyId()
        public String getCopySourceUrl()
        public CopyStatusType getCopyStatus()
        public String getEncryptionScope()
        public String getError()
        public String getETag()
        public OffsetDateTime getLastModified()
        public String getVersionId()
    }
    public final class BlobCopySourceTagsMode extends ExpandableStringEnum<BlobCopySourceTagsMode> {
        @Generated public static final BlobCopySourceTagsMode REPLACE = fromString("REPLACE");
        @Generated public static final BlobCopySourceTagsMode COPY = fromString("COPY");
        @Deprecated @Generated public BlobCopySourceTagsMode()
        @Generated public static BlobCopySourceTagsMode fromString(String name)
        @Generated public static Collection<BlobCopySourceTagsMode> values()
    }
    @Fluent
    public final class BlobCorsRule implements XmlSerializable<BlobCorsRule> {
        @Generated public BlobCorsRule()
        @Generated public String getAllowedHeaders()
        @Generated public BlobCorsRule setAllowedHeaders(String allowedHeaders)
        @Generated public String getAllowedMethods()
        @Generated public BlobCorsRule setAllowedMethods(String allowedMethods)
        @Generated public String getAllowedOrigins()
        @Generated public BlobCorsRule setAllowedOrigins(String allowedOrigins)
        @Generated public String getExposedHeaders()
        @Generated public BlobCorsRule setExposedHeaders(String exposedHeaders)
        @Generated public int getMaxAgeInSeconds()
        @Generated public BlobCorsRule setMaxAgeInSeconds(int maxAgeInSeconds)
    }
    public final class BlobDownloadAsyncResponse extends ResponseBase<BlobDownloadHeaders, Flux<ByteBuffer>> implements Closeable {
        public BlobDownloadAsyncResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value, BlobDownloadHeaders deserializedHeaders)
        @Override public void close() throws IOException
        public Mono<Void> writeValueToAsync(AsynchronousByteChannel channel, ProgressReporter progressReporter)
    }
    public final class BlobDownloadContentAsyncResponse extends ResponseBase<BlobDownloadHeaders, BinaryData> {
        public BlobDownloadContentAsyncResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData value, BlobDownloadHeaders deserializedHeaders)
    }
    public final class BlobDownloadContentResponse extends ResponseBase<BlobDownloadHeaders, BinaryData> {
        public BlobDownloadContentResponse(BlobDownloadContentAsyncResponse response)
    }
    @Fluent
    public final class BlobDownloadHeaders {
        public BlobDownloadHeaders()
        public String getAcceptRanges()
        public BlobDownloadHeaders setAcceptRanges(String acceptRanges)
        public Integer getBlobCommittedBlockCount()
        public BlobDownloadHeaders setBlobCommittedBlockCount(Integer blobCommittedBlockCount)
        public byte[] getBlobContentMD5()
        public BlobDownloadHeaders setBlobContentMD5(byte[] blobContentMD5)
        public Long getBlobSequenceNumber()
        public BlobDownloadHeaders setBlobSequenceNumber(Long blobSequenceNumber)
        public BlobType getBlobType()
        public BlobDownloadHeaders setBlobType(BlobType blobType)
        public String getCacheControl()
        public BlobDownloadHeaders setCacheControl(String cacheControl)
        public String getClientRequestId()
        public BlobDownloadHeaders setClientRequestId(String clientRequestId)
        public byte[] getContentCrc64()
        public BlobDownloadHeaders setContentCrc64(byte[] contentCrc64)
        public String getContentDisposition()
        public BlobDownloadHeaders setContentDisposition(String contentDisposition)
        public String getContentEncoding()
        public BlobDownloadHeaders setContentEncoding(String contentEncoding)
        public String getContentLanguage()
        public BlobDownloadHeaders setContentLanguage(String contentLanguage)
        public Long getContentLength()
        public BlobDownloadHeaders setContentLength(Long contentLength)
        public byte[] getContentMd5()
        public BlobDownloadHeaders setContentMd5(byte[] contentMd5)
        public String getContentRange()
        public BlobDownloadHeaders setContentRange(String contentRange)
        public String getContentType()
        public BlobDownloadHeaders setContentType(String contentType)
        public OffsetDateTime getCopyCompletionTime()
        public BlobDownloadHeaders setCopyCompletionTime(OffsetDateTime copyCompletionTime)
        public String getCopyId()
        public BlobDownloadHeaders setCopyId(String copyId)
        public String getCopyProgress()
        public BlobDownloadHeaders setCopyProgress(String copyProgress)
        public String getCopySource()
        public BlobDownloadHeaders setCopySource(String copySource)
        public CopyStatusType getCopyStatus()
        public BlobDownloadHeaders setCopyStatus(CopyStatusType copyStatus)
        public String getCopyStatusDescription()
        public BlobDownloadHeaders setCopyStatusDescription(String copyStatusDescription)
        public OffsetDateTime getCreationTime()
        public BlobDownloadHeaders setCreationTime(OffsetDateTime creationTime)
        public Boolean isCurrentVersion()
        public BlobDownloadHeaders setCurrentVersion(Boolean currentVersion)
        public OffsetDateTime getDateProperty()
        public BlobDownloadHeaders setDateProperty(OffsetDateTime dateProperty)
        public String getEncryptionKeySha256()
        public BlobDownloadHeaders setEncryptionKeySha256(String encryptionKeySha256)
        public String getEncryptionScope()
        public BlobDownloadHeaders setEncryptionScope(String encryptionScope)
        public String getErrorCode()
        public BlobDownloadHeaders setErrorCode(String errorCode)
        public String getETag()
        public BlobDownloadHeaders setETag(String eTag)
        public Boolean hasLegalHold()
        public BlobDownloadHeaders setHasLegalHold(Boolean hasLegalHold)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlobDownloadHeaders setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public BlobDownloadHeaders setIsServerEncrypted(Boolean isServerEncrypted)
        public OffsetDateTime getLastAccessedTime()
        public BlobDownloadHeaders setLastAccessedTime(OffsetDateTime lastAccessedTime)
        public OffsetDateTime getLastModified()
        public BlobDownloadHeaders setLastModified(OffsetDateTime lastModified)
        public LeaseDurationType getLeaseDuration()
        public BlobDownloadHeaders setLeaseDuration(LeaseDurationType leaseDuration)
        public LeaseStateType getLeaseState()
        public BlobDownloadHeaders setLeaseState(LeaseStateType leaseState)
        public LeaseStatusType getLeaseStatus()
        public BlobDownloadHeaders setLeaseStatus(LeaseStatusType leaseStatus)
        public Map<String, String> getMetadata()
        public BlobDownloadHeaders setMetadata(Map<String, String> metadata)
        public String getObjectReplicationDestinationPolicyId()
        public BlobDownloadHeaders setObjectReplicationDestinationPolicyId(String objectReplicationDestinationPolicyId)
        public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies()
        public BlobDownloadHeaders setObjectReplicationSourcePolicies(List<ObjectReplicationPolicy> objectReplicationSourcePolicies)
        public String getRequestId()
        public BlobDownloadHeaders setRequestId(String requestId)
        public Boolean isSealed()
        public BlobDownloadHeaders setSealed(Boolean sealed)
        public Boolean isServerEncrypted()
        public Long getTagCount()
        public BlobDownloadHeaders setTagCount(Long tagCount)
        public String getVersion()
        public BlobDownloadHeaders setVersion(String version)
        public String getVersionId()
        public BlobDownloadHeaders setVersionId(String versionId)
    }
    public final class BlobDownloadResponse extends ResponseBase<BlobDownloadHeaders, Void> {
        public BlobDownloadResponse(BlobDownloadAsyncResponse response)
    }
    public final class BlobErrorCode extends ExpandableStringEnum<BlobErrorCode> {
        @Generated public static final BlobErrorCode ACCOUNT_ALREADY_EXISTS = fromString("AccountAlreadyExists");
        @Generated public static final BlobErrorCode ACCOUNT_BEING_CREATED = fromString("AccountBeingCreated");
        @Generated public static final BlobErrorCode ACCOUNT_IS_DISABLED = fromString("AccountIsDisabled");
        @Generated public static final BlobErrorCode AUTHENTICATION_FAILED = fromString("AuthenticationFailed");
        @Generated public static final BlobErrorCode AUTHORIZATION_FAILURE = fromString("AuthorizationFailure");
        @Generated public static final BlobErrorCode CONDITION_HEADERS_NOT_SUPPORTED = fromString("ConditionHeadersNotSupported");
        @Generated public static final BlobErrorCode CONDITION_NOT_MET = fromString("ConditionNotMet");
        @Generated public static final BlobErrorCode EMPTY_METADATA_KEY = fromString("EmptyMetadataKey");
        @Generated public static final BlobErrorCode INSUFFICIENT_ACCOUNT_PERMISSIONS = fromString("InsufficientAccountPermissions");
        @Generated public static final BlobErrorCode INTERNAL_ERROR = fromString("InternalError");
        @Generated public static final BlobErrorCode INVALID_AUTHENTICATION_INFO = fromString("InvalidAuthenticationInfo");
        @Generated public static final BlobErrorCode INVALID_HEADER_VALUE = fromString("InvalidHeaderValue");
        @Generated public static final BlobErrorCode INVALID_HTTP_VERB = fromString("InvalidHttpVerb");
        @Generated public static final BlobErrorCode INVALID_INPUT = fromString("InvalidInput");
        @Generated public static final BlobErrorCode INVALID_MD5 = fromString("InvalidMd5");
        @Generated public static final BlobErrorCode INVALID_METADATA = fromString("InvalidMetadata");
        @Generated public static final BlobErrorCode INVALID_QUERY_PARAMETER_VALUE = fromString("InvalidQueryParameterValue");
        @Generated public static final BlobErrorCode INVALID_RANGE = fromString("InvalidRange");
        @Generated public static final BlobErrorCode INVALID_RESOURCE_NAME = fromString("InvalidResourceName");
        @Generated public static final BlobErrorCode INVALID_URI = fromString("InvalidUri");
        @Generated public static final BlobErrorCode INVALID_XML_DOCUMENT = fromString("InvalidXmlDocument");
        @Generated public static final BlobErrorCode INVALID_XML_NODE_VALUE = fromString("InvalidXmlNodeValue");
        @Generated public static final BlobErrorCode MD5MISMATCH = fromString("Md5Mismatch");
        @Generated public static final BlobErrorCode METADATA_TOO_LARGE = fromString("MetadataTooLarge");
        @Generated public static final BlobErrorCode MISSING_CONTENT_LENGTH_HEADER = fromString("MissingContentLengthHeader");
        @Generated public static final BlobErrorCode MISSING_REQUIRED_QUERY_PARAMETER = fromString("MissingRequiredQueryParameter");
        @Generated public static final BlobErrorCode MISSING_REQUIRED_HEADER = fromString("MissingRequiredHeader");
        @Generated public static final BlobErrorCode MISSING_REQUIRED_XML_NODE = fromString("MissingRequiredXmlNode");
        @Generated public static final BlobErrorCode MULTIPLE_CONDITION_HEADERS_NOT_SUPPORTED = fromString("MultipleConditionHeadersNotSupported");
        @Generated public static final BlobErrorCode OPERATION_TIMED_OUT = fromString("OperationTimedOut");
        @Generated public static final BlobErrorCode OUT_OF_RANGE_INPUT = fromString("OutOfRangeInput");
        @Generated public static final BlobErrorCode OUT_OF_RANGE_QUERY_PARAMETER_VALUE = fromString("OutOfRangeQueryParameterValue");
        @Generated public static final BlobErrorCode REQUEST_BODY_TOO_LARGE = fromString("RequestBodyTooLarge");
        @Generated public static final BlobErrorCode RESOURCE_TYPE_MISMATCH = fromString("ResourceTypeMismatch");
        @Generated public static final BlobErrorCode REQUEST_URL_FAILED_TO_PARSE = fromString("RequestUrlFailedToParse");
        @Generated public static final BlobErrorCode RESOURCE_ALREADY_EXISTS = fromString("ResourceAlreadyExists");
        @Generated public static final BlobErrorCode RESOURCE_NOT_FOUND = fromString("ResourceNotFound");
        @Generated public static final BlobErrorCode SERVER_BUSY = fromString("ServerBusy");
        @Generated public static final BlobErrorCode UNSUPPORTED_HEADER = fromString("UnsupportedHeader");
        @Generated public static final BlobErrorCode UNSUPPORTED_XML_NODE = fromString("UnsupportedXmlNode");
        @Generated public static final BlobErrorCode UNSUPPORTED_QUERY_PARAMETER = fromString("UnsupportedQueryParameter");
        @Generated public static final BlobErrorCode UNSUPPORTED_HTTP_VERB = fromString("UnsupportedHttpVerb");
        @Generated public static final BlobErrorCode APPEND_POSITION_CONDITION_NOT_MET = fromString("AppendPositionConditionNotMet");
        @Generated public static final BlobErrorCode BLOB_ALREADY_EXISTS = fromString("BlobAlreadyExists");
        @Generated public static final BlobErrorCode BLOB_IMMUTABLE_DUE_TO_POLICY = fromString("BlobImmutableDueToPolicy");
        @Generated public static final BlobErrorCode BLOB_NOT_FOUND = fromString("BlobNotFound");
        @Generated public static final BlobErrorCode BLOB_OVERWRITTEN = fromString("BlobOverwritten");
        @Generated public static final BlobErrorCode BLOB_TIER_INADEQUATE_FOR_CONTENT_LENGTH = fromString("BlobTierInadequateForContentLength");
        @Generated public static final BlobErrorCode BLOB_USES_CUSTOMER_SPECIFIED_ENCRYPTION = fromString("BlobUsesCustomerSpecifiedEncryption");
        @Generated public static final BlobErrorCode BLOCK_COUNT_EXCEEDS_LIMIT = fromString("BlockCountExceedsLimit");
        @Generated public static final BlobErrorCode BLOCK_LIST_TOO_LONG = fromString("BlockListTooLong");
        @Generated public static final BlobErrorCode CANNOT_CHANGE_TO_LOWER_TIER = fromString("CannotChangeToLowerTier");
        @Generated public static final BlobErrorCode CANNOT_VERIFY_COPY_SOURCE = fromString("CannotVerifyCopySource");
        @Generated public static final BlobErrorCode CONTAINER_ALREADY_EXISTS = fromString("ContainerAlreadyExists");
        @Generated public static final BlobErrorCode CONTAINER_BEING_DELETED = fromString("ContainerBeingDeleted");
        @Generated public static final BlobErrorCode CONTAINER_DISABLED = fromString("ContainerDisabled");
        @Generated public static final BlobErrorCode CONTAINER_NOT_FOUND = fromString("ContainerNotFound");
        @Generated public static final BlobErrorCode CONTENT_LENGTH_LARGER_THAN_TIER_LIMIT = fromString("ContentLengthLargerThanTierLimit");
        @Generated public static final BlobErrorCode COPY_ACROSS_ACCOUNTS_NOT_SUPPORTED = fromString("CopyAcrossAccountsNotSupported");
        @Generated public static final BlobErrorCode COPY_ID_MISMATCH = fromString("CopyIdMismatch");
        @Generated public static final BlobErrorCode FEATURE_VERSION_MISMATCH = fromString("FeatureVersionMismatch");
        @Generated public static final BlobErrorCode INCREMENTAL_COPY_BLOB_MISMATCH = fromString("IncrementalCopyBlobMismatch");
        @Generated public static final BlobErrorCode INCREMENTAL_COPY_OF_EARLIER_SNAPSHOT_NOT_ALLOWED = fromString("IncrementalCopyOfEarlierSnapshotNotAllowed");
        @Generated public static final BlobErrorCode INCREMENTAL_COPY_SOURCE_MUST_BE_SNAPSHOT = fromString("IncrementalCopySourceMustBeSnapshot");
        @Generated public static final BlobErrorCode INFINITE_LEASE_DURATION_REQUIRED = fromString("InfiniteLeaseDurationRequired");
        @Generated public static final BlobErrorCode INVALID_BLOB_OR_BLOCK = fromString("InvalidBlobOrBlock");
        @Generated public static final BlobErrorCode INVALID_BLOB_TIER = fromString("InvalidBlobTier");
        @Generated public static final BlobErrorCode INVALID_BLOB_TYPE = fromString("InvalidBlobType");
        @Generated public static final BlobErrorCode INVALID_BLOCK_ID = fromString("InvalidBlockId");
        @Generated public static final BlobErrorCode INVALID_BLOCK_LIST = fromString("InvalidBlockList");
        @Generated public static final BlobErrorCode INVALID_OPERATION = fromString("InvalidOperation");
        @Generated public static final BlobErrorCode INVALID_PAGE_RANGE = fromString("InvalidPageRange");
        @Generated public static final BlobErrorCode INVALID_SOURCE_BLOB_TYPE = fromString("InvalidSourceBlobType");
        @Generated public static final BlobErrorCode INVALID_SOURCE_BLOB_URL = fromString("InvalidSourceBlobUrl");
        @Generated public static final BlobErrorCode INVALID_VERSION_FOR_PAGE_BLOB_OPERATION = fromString("InvalidVersionForPageBlobOperation");
        @Generated public static final BlobErrorCode LEASE_ALREADY_PRESENT = fromString("LeaseAlreadyPresent");
        @Generated public static final BlobErrorCode LEASE_ALREADY_BROKEN = fromString("LeaseAlreadyBroken");
        @Generated public static final BlobErrorCode LEASE_ID_MISMATCH_WITH_BLOB_OPERATION = fromString("LeaseIdMismatchWithBlobOperation");
        @Generated public static final BlobErrorCode LEASE_ID_MISMATCH_WITH_CONTAINER_OPERATION = fromString("LeaseIdMismatchWithContainerOperation");
        @Generated public static final BlobErrorCode LEASE_ID_MISMATCH_WITH_LEASE_OPERATION = fromString("LeaseIdMismatchWithLeaseOperation");
        @Generated public static final BlobErrorCode LEASE_ID_MISSING = fromString("LeaseIdMissing");
        @Generated public static final BlobErrorCode LEASE_IS_BREAKING_AND_CANNOT_BE_ACQUIRED = fromString("LeaseIsBreakingAndCannotBeAcquired");
        @Generated public static final BlobErrorCode LEASE_IS_BREAKING_AND_CANNOT_BE_CHANGED = fromString("LeaseIsBreakingAndCannotBeChanged");
        @Generated public static final BlobErrorCode LEASE_IS_BROKEN_AND_CANNOT_BE_RENEWED = fromString("LeaseIsBrokenAndCannotBeRenewed");
        @Generated public static final BlobErrorCode LEASE_LOST = fromString("LeaseLost");
        @Generated public static final BlobErrorCode LEASE_NOT_PRESENT_WITH_BLOB_OPERATION = fromString("LeaseNotPresentWithBlobOperation");
        @Generated public static final BlobErrorCode LEASE_NOT_PRESENT_WITH_CONTAINER_OPERATION = fromString("LeaseNotPresentWithContainerOperation");
        @Generated public static final BlobErrorCode LEASE_NOT_PRESENT_WITH_LEASE_OPERATION = fromString("LeaseNotPresentWithLeaseOperation");
        @Generated public static final BlobErrorCode MAX_BLOB_SIZE_CONDITION_NOT_MET = fromString("MaxBlobSizeConditionNotMet");
        @Generated public static final BlobErrorCode NO_AUTHENTICATION_INFORMATION = fromString("NoAuthenticationInformation");
        @Generated public static final BlobErrorCode NO_PENDING_COPY_OPERATION = fromString("NoPendingCopyOperation");
        @Generated public static final BlobErrorCode OPERATION_NOT_ALLOWED_ON_INCREMENTAL_COPY_BLOB = fromString("OperationNotAllowedOnIncrementalCopyBlob");
        @Generated public static final BlobErrorCode PENDING_COPY_OPERATION = fromString("PendingCopyOperation");
        @Generated public static final BlobErrorCode PREVIOUS_SNAPSHOT_CANNOT_BE_NEWER = fromString("PreviousSnapshotCannotBeNewer");
        @Generated public static final BlobErrorCode PREVIOUS_SNAPSHOT_NOT_FOUND = fromString("PreviousSnapshotNotFound");
        @Generated public static final BlobErrorCode PREVIOUS_SNAPSHOT_OPERATION_NOT_SUPPORTED = fromString("PreviousSnapshotOperationNotSupported");
        @Generated public static final BlobErrorCode SEQUENCE_NUMBER_CONDITION_NOT_MET = fromString("SequenceNumberConditionNotMet");
        @Generated public static final BlobErrorCode SEQUENCE_NUMBER_INCREMENT_TOO_LARGE = fromString("SequenceNumberIncrementTooLarge");
        @Generated public static final BlobErrorCode SNAPSHOT_COUNT_EXCEEDED = fromString("SnapshotCountExceeded");
        @Generated public static final BlobErrorCode SNAPSHOT_OPERATION_RATE_EXCEEDED = fromString("SnapshotOperationRateExceeded");
        @Generated public static final BlobErrorCode SNAPSHOTS_PRESENT = fromString("SnapshotsPresent");
        @Generated public static final BlobErrorCode SOURCE_CONDITION_NOT_MET = fromString("SourceConditionNotMet");
        @Generated public static final BlobErrorCode SYSTEM_IN_USE = fromString("SystemInUse");
        @Generated public static final BlobErrorCode TARGET_CONDITION_NOT_MET = fromString("TargetConditionNotMet");
        @Generated public static final BlobErrorCode UNAUTHORIZED_BLOB_OVERWRITE = fromString("UnauthorizedBlobOverwrite");
        @Generated public static final BlobErrorCode BLOB_BEING_REHYDRATED = fromString("BlobBeingRehydrated");
        @Generated public static final BlobErrorCode BLOB_ARCHIVED = fromString("BlobArchived");
        @Generated public static final BlobErrorCode BLOB_NOT_ARCHIVED = fromString("BlobNotArchived");
        @Generated public static final BlobErrorCode AUTHORIZATION_SOURCE_IPMISMATCH = fromString("AuthorizationSourceIPMismatch");
        @Generated public static final BlobErrorCode AUTHORIZATION_PROTOCOL_MISMATCH = fromString("AuthorizationProtocolMismatch");
        @Generated public static final BlobErrorCode AUTHORIZATION_PERMISSION_MISMATCH = fromString("AuthorizationPermissionMismatch");
        @Generated public static final BlobErrorCode AUTHORIZATION_SERVICE_MISMATCH = fromString("AuthorizationServiceMismatch");
        @Generated public static final BlobErrorCode AUTHORIZATION_RESOURCE_TYPE_MISMATCH = fromString("AuthorizationResourceTypeMismatch");
        @Generated public static final BlobErrorCode BLOB_ACCESS_TIER_NOT_SUPPORTED_FOR_ACCOUNT_TYPE = fromString("BlobAccessTierNotSupportedForAccountType");
        @Deprecated @Generated public static final BlobErrorCode SNAPHOT_OPERATION_RATE_EXCEEDED = fromString("SnapshotOperationRateExceeded");
        @Deprecated @Generated public static final BlobErrorCode INCREMENTAL_COPY_OF_ERALIER_VERSION_SNAPSHOT_NOT_ALLOWED = fromString("IncrementalCopyOfEralierVersionSnapshotNotAllowed");
        @Deprecated @Generated public static final BlobErrorCode INCREMENTAL_COPY_OF_EARLIER_VERSION_SNAPSHOT_NOT_ALLOWED = fromString("IncrementalCopyOfEarlierVersionSnapshotNotAllowed");
        @Deprecated @Generated public BlobErrorCode()
        @Generated public static BlobErrorCode fromString(String name)
        @Generated public static Collection<BlobErrorCode> values()
    }
    @Fluent
    public final class BlobHttpHeaders {
        @Generated public BlobHttpHeaders()
        @Generated public String getCacheControl()
        @Generated public BlobHttpHeaders setCacheControl(String cacheControl)
        @Generated public String getContentDisposition()
        @Generated public BlobHttpHeaders setContentDisposition(String contentDisposition)
        @Generated public String getContentEncoding()
        @Generated public BlobHttpHeaders setContentEncoding(String contentEncoding)
        @Generated public String getContentLanguage()
        @Generated public BlobHttpHeaders setContentLanguage(String contentLanguage)
        @Generated public byte[] getContentMd5()
        @Generated public BlobHttpHeaders setContentMd5(byte[] contentMd5)
        @Generated public String getContentType()
        @Generated public BlobHttpHeaders setContentType(String contentType)
    }
    public final class BlobImmutabilityPolicy {
        public BlobImmutabilityPolicy()
        public OffsetDateTime getExpiryTime()
        public BlobImmutabilityPolicy setExpiryTime(OffsetDateTime expiryTime)
        public BlobImmutabilityPolicyMode getPolicyMode()
        public BlobImmutabilityPolicy setPolicyMode(BlobImmutabilityPolicyMode policyMode)
    }
    public enum BlobImmutabilityPolicyMode {
        MUTABLE("Mutable"),
        UNLOCKED("Unlocked"),
        LOCKED("Locked");
        public static BlobImmutabilityPolicyMode fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class BlobItem {
        public BlobItem()
        public Boolean isCurrentVersion()
        public BlobItem setCurrentVersion(Boolean isCurrentVersion)
        public boolean isDeleted()
        public BlobItem setDeleted(boolean deleted)
        public Boolean hasVersionsOnly()
        public BlobItem setHasVersionsOnly(Boolean hasVersionsOnly)
        public BlobItem setIsPrefix(Boolean isPrefix)
        public Map<String, String> getMetadata()
        public BlobItem setMetadata(Map<String, String> metadata)
        public String getName()
        public BlobItem setName(String name)
        public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies()
        public BlobItem setObjectReplicationSourcePolicies(List<ObjectReplicationPolicy> objectReplicationSourcePolicies)
        public Boolean isPrefix()
        public BlobItemProperties getProperties()
        public BlobItem setProperties(BlobItemProperties properties)
        public String getSnapshot()
        public BlobItem setSnapshot(String snapshot)
        public Map<String, String> getTags()
        public BlobItem setTags(Map<String, String> tags)
        public String getVersionId()
        public BlobItem setVersionId(String versionId)
    }
    @Fluent
    public final class BlobItemProperties {
        public BlobItemProperties()
        public AccessTier getAccessTier()
        public BlobItemProperties setAccessTier(AccessTier accessTier)
        public OffsetDateTime getAccessTierChangeTime()
        public BlobItemProperties setAccessTierChangeTime(OffsetDateTime accessTierChangeTime)
        public Boolean isAccessTierInferred()
        public BlobItemProperties setAccessTierInferred(Boolean accessTierInferred)
        public ArchiveStatus getArchiveStatus()
        public BlobItemProperties setArchiveStatus(ArchiveStatus archiveStatus)
        public Long getBlobSequenceNumber()
        public BlobItemProperties setBlobSequenceNumber(Long blobSequenceNumber)
        public BlobType getBlobType()
        public BlobItemProperties setBlobType(BlobType blobType)
        public String getCacheControl()
        public BlobItemProperties setCacheControl(String cacheControl)
        public String getContentDisposition()
        public BlobItemProperties setContentDisposition(String contentDisposition)
        public String getContentEncoding()
        public BlobItemProperties setContentEncoding(String contentEncoding)
        public String getContentLanguage()
        public BlobItemProperties setContentLanguage(String contentLanguage)
        public Long getContentLength()
        public BlobItemProperties setContentLength(Long contentLength)
        public byte[] getContentMd5()
        public BlobItemProperties setContentMd5(byte[] contentMd5)
        public String getContentType()
        public BlobItemProperties setContentType(String contentType)
        public OffsetDateTime getCopyCompletionTime()
        public BlobItemProperties setCopyCompletionTime(OffsetDateTime copyCompletionTime)
        public String getCopyId()
        public BlobItemProperties setCopyId(String copyId)
        public String getCopyProgress()
        public BlobItemProperties setCopyProgress(String copyProgress)
        public String getCopySource()
        public BlobItemProperties setCopySource(String copySource)
        public CopyStatusType getCopyStatus()
        public BlobItemProperties setCopyStatus(CopyStatusType copyStatus)
        public String getCopyStatusDescription()
        public BlobItemProperties setCopyStatusDescription(String copyStatusDescription)
        public OffsetDateTime getCreationTime()
        public BlobItemProperties setCreationTime(OffsetDateTime creationTime)
        public String getCustomerProvidedKeySha256()
        public BlobItemProperties setCustomerProvidedKeySha256(String customerProvidedKeySha256)
        public OffsetDateTime getDeletedTime()
        public BlobItemProperties setDeletedTime(OffsetDateTime deletedTime)
        public String getDestinationSnapshot()
        public BlobItemProperties setDestinationSnapshot(String destinationSnapshot)
        public String getEncryptionScope()
        public BlobItemProperties setEncryptionScope(String encryptionScope)
        public String getETag()
        public BlobItemProperties setETag(String eTag)
        public OffsetDateTime getExpiryTime()
        public BlobItemProperties setExpiryTime(OffsetDateTime expiryTime)
        public Boolean hasLegalHold()
        public BlobItemProperties setHasLegalHold(Boolean hasLegalHold)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlobItemProperties setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Boolean isIncrementalCopy()
        public BlobItemProperties setIncrementalCopy(Boolean incrementalCopy)
        public OffsetDateTime getLastAccessedTime()
        public BlobItemProperties setLastAccessedTime(OffsetDateTime lastAccessedTime)
        public OffsetDateTime getLastModified()
        public BlobItemProperties setLastModified(OffsetDateTime lastModified)
        public LeaseDurationType getLeaseDuration()
        public BlobItemProperties setLeaseDuration(LeaseDurationType leaseDuration)
        public LeaseStateType getLeaseState()
        public BlobItemProperties setLeaseState(LeaseStateType leaseState)
        public LeaseStatusType getLeaseStatus()
        public BlobItemProperties setLeaseStatus(LeaseStatusType leaseStatus)
        public RehydratePriority getRehydratePriority()
        public BlobItemProperties setRehydratePriority(RehydratePriority rehydratePriority)
        public Integer getRemainingRetentionDays()
        public BlobItemProperties setRemainingRetentionDays(Integer remainingRetentionDays)
        public Boolean isSealed()
        public BlobItemProperties setSealed(Boolean sealed)
        public Boolean isServerEncrypted()
        public BlobItemProperties setServerEncrypted(Boolean serverEncrypted)
        public AccessTier getSmartAccessTier()
        public BlobItemProperties setSmartAccessTier(AccessTier smartAccessTier)
        public Integer getTagCount()
        public BlobItemProperties setTagCount(Integer tagCount)
    }
    public class BlobLeaseRequestConditions extends RequestConditions {
        public BlobLeaseRequestConditions()
        @Override public BlobLeaseRequestConditions setIfMatch(String ifMatch)
        @Override public BlobLeaseRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public BlobLeaseRequestConditions setIfNoneMatch(String ifNoneMatch)
        @Override public BlobLeaseRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        public String getTagsConditions()
        public BlobLeaseRequestConditions setTagsConditions(String tagsConditions)
    }
    public interface BlobLegalHoldResult {
        boolean hasLegalHold()
    }
    @Fluent
    public final class BlobListDetails {
        public BlobListDetails()
        public boolean getRetrieveCopy()
        public BlobListDetails setRetrieveCopy(boolean retrieveCopy)
        public boolean getRetrieveDeletedBlobs()
        public BlobListDetails setRetrieveDeletedBlobs(boolean retrieveDeletedBlobs)
        public boolean getRetrieveDeletedBlobsWithVersions()
        public BlobListDetails setRetrieveDeletedBlobsWithVersions(boolean retrieveDeletedWithVersions)
        public boolean getRetrieveImmutabilityPolicy()
        public BlobListDetails setRetrieveImmutabilityPolicy(boolean retrieveImmutabilityPolicy)
        public boolean getRetrieveLegalHold()
        public BlobListDetails setRetrieveLegalHold(boolean retrieveLegalHold)
        public boolean getRetrieveMetadata()
        public BlobListDetails setRetrieveMetadata(boolean retrieveMetadata)
        public boolean getRetrieveSnapshots()
        public BlobListDetails setRetrieveSnapshots(boolean retrieveSnapshots)
        public boolean getRetrieveTags()
        public BlobListDetails setRetrieveTags(boolean retrieveTags)
        public boolean getRetrieveUncommittedBlobs()
        public BlobListDetails setRetrieveUncommittedBlobs(boolean retrieveUncommittedBlobs)
        public boolean getRetrieveVersions()
        public BlobListDetails setRetrieveVersions(boolean retrieveVersions)
        public ArrayList<ListBlobsIncludeItem> toList()
    }
    @Fluent
    public final class BlobMetrics implements XmlSerializable<BlobMetrics> {
        @Generated public BlobMetrics()
        @Generated public boolean isEnabled()
        @Generated public BlobMetrics setEnabled(boolean enabled)
        @Generated public Boolean isIncludeApis()
        @Generated public BlobMetrics setIncludeApis(Boolean includeApis)
        @Generated public BlobRetentionPolicy getRetentionPolicy()
        @Generated public BlobMetrics setRetentionPolicy(BlobRetentionPolicy retentionPolicy)
        @Generated public String getVersion()
        @Generated public BlobMetrics setVersion(String version)
    }
    @Fluent
    public final class BlobPrefix implements XmlSerializable<BlobPrefix> {
        public BlobPrefix()
        public String getName()
        public BlobPrefix setName(String name)
    }
    @Immutable
    public final class BlobProperties {
        public BlobProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, long blobSize, String contentType, byte[] contentMd5, String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl, Long blobSequenceNumber, BlobType blobType, LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId, CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime, String copyStatusDescription, Boolean isServerEncrypted, Boolean isIncrementalCopy, String copyDestinationSnapshot, AccessTier accessTier, Boolean isAccessTierInferred, ArchiveStatus archiveStatus, String encryptionKeySha256, OffsetDateTime accessTierChangeTime, Map<String, String> metadata, Integer committedBlockCount)
        public BlobProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, long blobSize, String contentType, byte[] contentMd5, String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl, Long blobSequenceNumber, BlobType blobType, LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId, CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime, String copyStatusDescription, Boolean isServerEncrypted, Boolean isIncrementalCopy, String copyDestinationSnapshot, AccessTier accessTier, Boolean isAccessTierInferred, ArchiveStatus archiveStatus, String encryptionKeySha256, String encryptionScope, OffsetDateTime accessTierChangeTime, Map<String, String> metadata, Integer committedBlockCount, Long tagCount, String versionId, Boolean isCurrentVersion, List<ObjectReplicationPolicy> objectReplicationSourcePolicies, String objectReplicationDestinationPolicyId)
        public BlobProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, long blobSize, String contentType, byte[] contentMd5, String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl, Long blobSequenceNumber, BlobType blobType, LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId, CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime, String copyStatusDescription, Boolean isServerEncrypted, Boolean isIncrementalCopy, String copyDestinationSnapshot, AccessTier accessTier, Boolean isAccessTierInferred, ArchiveStatus archiveStatus, String encryptionKeySha256, String encryptionScope, OffsetDateTime accessTierChangeTime, Map<String, String> metadata, Integer committedBlockCount, String versionId, Boolean isCurrentVersion, Long tagCount, Map<String, String> objectReplicationStatus, String rehydratePriority, Boolean isSealed)
        public BlobProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, long blobSize, String contentType, byte[] contentMd5, String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl, Long blobSequenceNumber, BlobType blobType, LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId, CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime, String copyStatusDescription, Boolean isServerEncrypted, Boolean isIncrementalCopy, String copyDestinationSnapshot, AccessTier accessTier, Boolean isAccessTierInferred, ArchiveStatus archiveStatus, String encryptionKeySha256, String encryptionScope, OffsetDateTime accessTierChangeTime, Map<String, String> metadata, Integer committedBlockCount, Long tagCount, String versionId, Boolean isCurrentVersion, List<ObjectReplicationPolicy> objectReplicationSourcePolicies, String objectReplicationDestinationPolicyId, RehydratePriority rehydratePriority, Boolean isSealed, OffsetDateTime lastAccessedTime, OffsetDateTime expiresOn)
        public BlobProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, long blobSize, String contentType, byte[] contentMd5, String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl, Long blobSequenceNumber, BlobType blobType, LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId, CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime, String copyStatusDescription, Boolean isServerEncrypted, Boolean isIncrementalCopy, String copyDestinationSnapshot, AccessTier accessTier, Boolean isAccessTierInferred, ArchiveStatus archiveStatus, String encryptionKeySha256, String encryptionScope, OffsetDateTime accessTierChangeTime, Map<String, String> metadata, Integer committedBlockCount, Long tagCount, String versionId, Boolean isCurrentVersion, List<ObjectReplicationPolicy> objectReplicationSourcePolicies, String objectReplicationDestinationPolicyId, RehydratePriority rehydratePriority, Boolean isSealed, OffsetDateTime lastAccessedTime, OffsetDateTime expiresOn, BlobImmutabilityPolicy immutabilityPolicy, Boolean hasLegalHold)
        public BlobProperties(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag, long blobSize, String contentType, byte[] contentMd5, String contentEncoding, String contentDisposition, String contentLanguage, String cacheControl, Long blobSequenceNumber, BlobType blobType, LeaseStatusType leaseStatus, LeaseStateType leaseState, LeaseDurationType leaseDuration, String copyId, CopyStatusType copyStatus, String copySource, String copyProgress, OffsetDateTime copyCompletionTime, String copyStatusDescription, Boolean isServerEncrypted, Boolean isIncrementalCopy, String copyDestinationSnapshot, AccessTier accessTier, Boolean isAccessTierInferred, ArchiveStatus archiveStatus, String encryptionKeySha256, String encryptionScope, OffsetDateTime accessTierChangeTime, Map<String, String> metadata, Integer committedBlockCount, Long tagCount, String versionId, Boolean isCurrentVersion, List<ObjectReplicationPolicy> objectReplicationSourcePolicies, String objectReplicationDestinationPolicyId, RehydratePriority rehydratePriority, Boolean isSealed, OffsetDateTime lastAccessedTime, OffsetDateTime expiresOn, BlobImmutabilityPolicy immutabilityPolicy, Boolean hasLegalHold, String requestId)
        public AccessTier getAccessTier()
        public OffsetDateTime getAccessTierChangeTime()
        public Boolean isAccessTierInferred()
        public ArchiveStatus getArchiveStatus()
        public Long getBlobSequenceNumber()
        public long getBlobSize()
        public BlobType getBlobType()
        public String getCacheControl()
        public Integer getCommittedBlockCount()
        public String getContentDisposition()
        public String getContentEncoding()
        public String getContentLanguage()
        public byte[] getContentMd5()
        public String getContentType()
        public OffsetDateTime getCopyCompletionTime()
        public String getCopyDestinationSnapshot()
        public String getCopyId()
        public String getCopyProgress()
        public String getCopySource()
        public CopyStatusType getCopyStatus()
        public String getCopyStatusDescription()
        public OffsetDateTime getCreationTime()
        public Boolean isCurrentVersion()
        public String getEncryptionKeySha256()
        public String getEncryptionScope()
        public String getETag()
        public OffsetDateTime getExpiresOn()
        public Boolean hasLegalHold()
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public Boolean isIncrementalCopy()
        public OffsetDateTime getLastAccessedTime()
        public OffsetDateTime getLastModified()
        public LeaseDurationType getLeaseDuration()
        public LeaseStateType getLeaseState()
        public LeaseStatusType getLeaseStatus()
        public Map<String, String> getMetadata()
        public String getObjectReplicationDestinationPolicyId()
        public List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies()
        public RehydratePriority getRehydratePriority()
        public String getRequestId()
        public Boolean isSealed()
        public Boolean isServerEncrypted()
        public AccessTier getSmartAccessTier()
        public Long getTagCount()
        public String getVersionId()
    }
    @Fluent
    public class BlobQueryArrowField {
        public BlobQueryArrowField(BlobQueryArrowFieldType type)
        public String getName()
        public BlobQueryArrowField setName(String name)
        public Integer getPrecision()
        public BlobQueryArrowField setPrecision(Integer precision)
        public Integer getScale()
        public BlobQueryArrowField setScale(Integer scale)
        public BlobQueryArrowFieldType getType()
    }
    public enum BlobQueryArrowFieldType {
        INT64("int64"),
        BOOL("bool"),
        TIMESTAMP("timestamp[ms]"),
        STRING("string"),
        DOUBLE("double"),
        DECIMAL("decimal");
        public static BlobQueryArrowFieldType fromString(String value)
        @Override public String toString()
    }
    public class BlobQueryArrowSerialization implements BlobQuerySerialization {
        public BlobQueryArrowSerialization()
        public List<BlobQueryArrowField> getSchema()
        public BlobQueryArrowSerialization setSchema(List<BlobQueryArrowField> schema)
    }
    public final class BlobQueryAsyncResponse extends ResponseBase<BlobQueryHeaders, Flux<ByteBuffer>> {
        public BlobQueryAsyncResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value, BlobQueryHeaders deserializedHeaders)
    }
    public class BlobQueryDelimitedSerialization implements BlobQuerySerialization {
        public BlobQueryDelimitedSerialization()
        public char getColumnSeparator()
        public BlobQueryDelimitedSerialization setColumnSeparator(char columnSeparator)
        public char getEscapeChar()
        public BlobQueryDelimitedSerialization setEscapeChar(char escapeChar)
        public char getFieldQuote()
        public BlobQueryDelimitedSerialization setFieldQuote(char fieldQuote)
        public boolean isHeadersPresent()
        public BlobQueryDelimitedSerialization setHeadersPresent(boolean headersPresent)
        public char getRecordSeparator()
        public BlobQueryDelimitedSerialization setRecordSeparator(char recordSeparator)
    }
    public class BlobQueryError {
        public BlobQueryError(boolean fatal, String name, String description, long position)
        public String getDescription()
        public boolean isFatal()
        public String getName()
        public long getPosition()
        @Override public String toString()
    }
    @Fluent
    public final class BlobQueryHeaders {
        public BlobQueryHeaders()
        public String getAcceptRanges()
        public BlobQueryHeaders setAcceptRanges(String acceptRanges)
        public Integer getBlobCommittedBlockCount()
        public BlobQueryHeaders setBlobCommittedBlockCount(Integer blobCommittedBlockCount)
        public byte[] getBlobContentMd5()
        public BlobQueryHeaders setBlobContentMd5(byte[] blobContentMd5)
        public Long getBlobSequenceNumber()
        public BlobQueryHeaders setBlobSequenceNumber(Long blobSequenceNumber)
        public BlobType getBlobType()
        public BlobQueryHeaders setBlobType(BlobType blobType)
        public String getCacheControl()
        public BlobQueryHeaders setCacheControl(String cacheControl)
        public String getClientRequestId()
        public BlobQueryHeaders setClientRequestId(String clientRequestId)
        public byte[] getContentCrc64()
        public BlobQueryHeaders setContentCrc64(byte[] contentCrc64)
        public String getContentDisposition()
        public BlobQueryHeaders setContentDisposition(String contentDisposition)
        public String getContentEncoding()
        public BlobQueryHeaders setContentEncoding(String contentEncoding)
        public String getContentLanguage()
        public BlobQueryHeaders setContentLanguage(String contentLanguage)
        public Long getContentLength()
        public BlobQueryHeaders setContentLength(Long contentLength)
        public byte[] getContentMd5()
        public BlobQueryHeaders setContentMd5(byte[] contentMd5)
        public String getContentRange()
        public BlobQueryHeaders setContentRange(String contentRange)
        public String getContentType()
        public BlobQueryHeaders setContentType(String contentType)
        public OffsetDateTime getCopyCompletionTime()
        public BlobQueryHeaders setCopyCompletionTime(OffsetDateTime copyCompletionTime)
        public String getCopyId()
        public BlobQueryHeaders setCopyId(String copyId)
        public String getCopyProgress()
        public BlobQueryHeaders setCopyProgress(String copyProgress)
        public String getCopySource()
        public BlobQueryHeaders setCopySource(String copySource)
        public CopyStatusType getCopyStatus()
        public BlobQueryHeaders setCopyStatus(CopyStatusType copyStatus)
        public String getCopyStatusDescription()
        public BlobQueryHeaders setCopyStatusDescription(String copyStatusDescription)
        public OffsetDateTime getDateProperty()
        public BlobQueryHeaders setDateProperty(OffsetDateTime dateProperty)
        public String getEncryptionKeySha256()
        public BlobQueryHeaders setEncryptionKeySha256(String encryptionKeySha256)
        public String getEncryptionScope()
        public BlobQueryHeaders setEncryptionScope(String encryptionScope)
        public String getErrorCode()
        public BlobQueryHeaders setErrorCode(String errorCode)
        public String getETag()
        public BlobQueryHeaders setETag(String eTag)
        public OffsetDateTime getLastModified()
        public BlobQueryHeaders setLastModified(OffsetDateTime lastModified)
        public LeaseDurationType getLeaseDuration()
        public BlobQueryHeaders setLeaseDuration(LeaseDurationType leaseDuration)
        public LeaseStateType getLeaseState()
        public BlobQueryHeaders setLeaseState(LeaseStateType leaseState)
        public LeaseStatusType getLeaseStatus()
        public BlobQueryHeaders setLeaseStatus(LeaseStatusType leaseStatus)
        public Map<String, String> getMetadata()
        public BlobQueryHeaders setMetadata(Map<String, String> metadata)
        public String getRequestId()
        public BlobQueryHeaders setRequestId(String requestId)
        public Boolean isServerEncrypted()
        public BlobQueryHeaders setServerEncrypted(Boolean serverEncrypted)
        public String getVersion()
        public BlobQueryHeaders setVersion(String version)
    }
    public class BlobQueryJsonSerialization implements BlobQuerySerialization {
        public BlobQueryJsonSerialization()
        public char getRecordSeparator()
        public BlobQueryJsonSerialization setRecordSeparator(char recordSeparator)
    }
    public final class BlobQueryParquetSerialization implements BlobQuerySerialization {
        public BlobQueryParquetSerialization()
    }
    public class BlobQueryProgress {
        public BlobQueryProgress(long bytesScanned, long totalBytes)
        public long getBytesScanned()
        public long getTotalBytes()
    }
    public final class BlobQueryResponse extends ResponseBase<BlobQueryHeaders, Void> {
        public BlobQueryResponse(BlobQueryAsyncResponse response)
    }
    public interface BlobQuerySerialization {
        // This interface does not declare any API.
    }
    @Immutable
    public final class BlobRange {
        public BlobRange(long offset)
        public BlobRange(long offset, Long count)
        public Long getCount()
        public long getOffset()
        public String toHeaderValue()
        @Override public String toString()
    }
    @Fluent
    public class BlobRequestConditions extends BlobLeaseRequestConditions {
        public BlobRequestConditions()
        public OffsetDateTime getAccessTierIfModifiedSince()
        public BlobRequestConditions setAccessTierIfModifiedSince(OffsetDateTime accessTierIfModifiedSince)
        public OffsetDateTime getAccessTierIfUnmodifiedSince()
        public BlobRequestConditions setAccessTierIfUnmodifiedSince(OffsetDateTime accessTierIfUnmodifiedSince)
        @Override public BlobRequestConditions setIfMatch(String ifMatch)
        @Override public BlobRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public BlobRequestConditions setIfNoneMatch(String ifNoneMatch)
        @Override public BlobRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        public String getLeaseId()
        public BlobRequestConditions setLeaseId(String leaseId)
        @Override public BlobRequestConditions setTagsConditions(String tagsConditions)
    }
    @Fluent
    public final class BlobRetentionPolicy implements XmlSerializable<BlobRetentionPolicy> {
        @Generated public BlobRetentionPolicy()
        @Generated public Integer getDays()
        @Generated public BlobRetentionPolicy setDays(Integer days)
        @Generated public boolean isEnabled()
        @Generated public BlobRetentionPolicy setEnabled(boolean enabled)
    }
    public final class BlobSeekableByteChannelReadResult {
        public BlobSeekableByteChannelReadResult(SeekableByteChannel channel, BlobProperties properties)
        public SeekableByteChannel getChannel()
        public BlobProperties getProperties()
    }
    @Fluent
    public final class BlobServiceProperties implements XmlSerializable<BlobServiceProperties> {
        @Generated public BlobServiceProperties()
        @Generated public List<BlobCorsRule> getCors()
        @Generated public BlobServiceProperties setCors(List<BlobCorsRule> cors)
        @Generated public String getDefaultServiceVersion()
        @Generated public BlobServiceProperties setDefaultServiceVersion(String defaultServiceVersion)
        @Generated public BlobRetentionPolicy getDeleteRetentionPolicy()
        @Generated public BlobServiceProperties setDeleteRetentionPolicy(BlobRetentionPolicy deleteRetentionPolicy)
        @Generated public BlobMetrics getHourMetrics()
        @Generated public BlobServiceProperties setHourMetrics(BlobMetrics hourMetrics)
        @Generated public BlobAnalyticsLogging getLogging()
        @Generated public BlobServiceProperties setLogging(BlobAnalyticsLogging logging)
        @Generated public BlobMetrics getMinuteMetrics()
        @Generated public BlobServiceProperties setMinuteMetrics(BlobMetrics minuteMetrics)
        @Generated public StaticWebsite getStaticWebsite()
        @Generated public BlobServiceProperties setStaticWebsite(StaticWebsite staticWebsite)
    }
    @Fluent
    public final class BlobServiceStatistics implements XmlSerializable<BlobServiceStatistics> {
        @Generated public BlobServiceStatistics()
        @Generated public GeoReplication getGeoReplication()
        @Generated public BlobServiceStatistics setGeoReplication(GeoReplication geoReplication)
    }
    @Fluent
    public final class BlobSignedIdentifier implements XmlSerializable<BlobSignedIdentifier> {
        @Generated public BlobSignedIdentifier()
        @Generated public BlobAccessPolicy getAccessPolicy()
        @Generated public BlobSignedIdentifier setAccessPolicy(BlobAccessPolicy accessPolicy)
        @Generated public String getId()
        @Generated public BlobSignedIdentifier setId(String id)
    }
    public final class BlobStorageException extends HttpResponseException {
        public BlobStorageException(String message, HttpResponse response, Object value)
        public BlobErrorCode getErrorCode()
        public String getServiceMessage()
        public int getStatusCode()
    }
    public enum BlobType {
        BLOCK_BLOB("BlockBlob"),
        PAGE_BLOB("PageBlob"),
        APPEND_BLOB("AppendBlob");
        public static BlobType fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class Block implements XmlSerializable<Block> {
        @Generated public Block()
        @Generated public String getName()
        @Generated public Block setName(String name)
        @Deprecated @Generated public int getSize()
        @Deprecated @Generated public Block setSize(int sizeInt)
        @Generated public long getSizeLong()
        @Generated public Block setSizeLong(long sizeLong)
    }
    @Immutable
    public class BlockBlobItem {
        @Deprecated public BlockBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted, String encryptionKeySha256)
        @Deprecated public BlockBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope)
        @Deprecated public BlockBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, String versionId)
        public BlockBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, String versionId)
        public byte[] getContentCrc64()
        public byte[] getContentMd5()
        public String getEncryptionKeySha256()
        public String getEncryptionScope()
        public String getETag()
        public OffsetDateTime getLastModified()
        public Boolean isServerEncrypted()
        public String getVersionId()
    }
    @Fluent
    public final class BlockList implements XmlSerializable<BlockList> {
        @Generated public BlockList()
        @Generated public List<Block> getCommittedBlocks()
        @Generated public BlockList setCommittedBlocks(List<Block> committedBlocks)
        @Generated public List<Block> getUncommittedBlocks()
        @Generated public BlockList setUncommittedBlocks(List<Block> uncommittedBlocks)
    }
    public enum BlockListType {
        COMMITTED("committed"),
        UNCOMMITTED("uncommitted"),
        ALL("all");
        public static BlockListType fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class BlockLookupList implements XmlSerializable<BlockLookupList> {
        @Generated public BlockLookupList()
        @Generated public List<String> getCommitted()
        @Generated public BlockLookupList setCommitted(List<String> committed)
        @Generated public List<String> getLatest()
        @Generated public BlockLookupList setLatest(List<String> latest)
        @Generated public List<String> getUncommitted()
        @Generated public BlockLookupList setUncommitted(List<String> uncommitted)
    }
    @Fluent
    public final class ClearRange implements XmlSerializable<ClearRange> {
        @Generated public ClearRange()
        @Generated public long getEnd()
        @Generated public ClearRange setEnd(long end)
        @Generated public long getStart()
        @Generated public ClearRange setStart(long start)
    }
    public enum ConsistentReadControl {
        NONE,
        ETAG,
        VERSION_ID;
    }
    public enum CopyStatusType {
        PENDING("pending"),
        SUCCESS("success"),
        ABORTED("aborted"),
        FAILED("failed");
        public static CopyStatusType fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class CpkInfo {
        @Generated public CpkInfo()
        @Generated public EncryptionAlgorithmType getEncryptionAlgorithm()
        @Generated public CpkInfo setEncryptionAlgorithm(EncryptionAlgorithmType encryptionAlgorithm)
        @Generated public String getEncryptionKey()
        @Generated public CpkInfo setEncryptionKey(String encryptionKey)
        @Generated public String getEncryptionKeySha256()
        @Generated public CpkInfo setEncryptionKeySha256(String encryptionKeySha256)
    }
    @Immutable
    public class CustomerProvidedKey {
        public CustomerProvidedKey(String key)
        public CustomerProvidedKey(byte[] key)
        public EncryptionAlgorithmType getEncryptionAlgorithm()
        public String getKey()
        public String getKeySha256()
    }
    public enum DeleteSnapshotsOptionType {
        INCLUDE("include"),
        ONLY("only");
        public static DeleteSnapshotsOptionType fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class DownloadRetryOptions {
        public DownloadRetryOptions()
        public int getMaxRetryRequests()
        public DownloadRetryOptions setMaxRetryRequests(int maxRetryRequests)
    }
    public enum EncryptionAlgorithmType {
        AES256("AES256");
        public static EncryptionAlgorithmType fromString(String value)
        @Override public String toString()
    }
    public final class FileShareTokenIntent extends ExpandableStringEnum<FileShareTokenIntent> {
        @Generated public static final FileShareTokenIntent BACKUP = fromString("backup");
        @Deprecated @Generated public FileShareTokenIntent()
        @Generated public static FileShareTokenIntent fromString(String name)
        @Generated public static Collection<FileShareTokenIntent> values()
    }
    @Fluent
    public final class GeoReplication implements XmlSerializable<GeoReplication> {
        @Generated public GeoReplication()
        @Generated public OffsetDateTime getLastSyncTime()
        @Generated public GeoReplication setLastSyncTime(OffsetDateTime lastSyncTime)
        @Generated public GeoReplicationStatus getStatus()
        @Generated public GeoReplication setStatus(GeoReplicationStatus status)
    }
    public final class GeoReplicationStatus extends ExpandableStringEnum<GeoReplicationStatus> {
        @Generated public static final GeoReplicationStatus LIVE = fromString("live");
        @Generated public static final GeoReplicationStatus BOOTSTRAP = fromString("bootstrap");
        @Generated public static final GeoReplicationStatus UNAVAILABLE = fromString("unavailable");
        @Deprecated @Generated public GeoReplicationStatus()
        @Generated public static GeoReplicationStatus fromString(String name)
        @Generated public static Collection<GeoReplicationStatus> values()
    }
    @Fluent
    public final class KeyInfo implements XmlSerializable<KeyInfo> {
        @Generated public KeyInfo()
        @Generated public String getDelegatedUserTenantId()
        @Generated public KeyInfo setDelegatedUserTenantId(String delegatedUserTenantId)
        @Generated public String getExpiry()
        @Generated public KeyInfo setExpiry(String expiry)
        @Generated public String getStart()
        @Generated public KeyInfo setStart(String start)
    }
    public enum LeaseDurationType {
        INFINITE("infinite"),
        FIXED("fixed");
        public static LeaseDurationType fromString(String value)
        @Override public String toString()
    }
    public enum LeaseStateType {
        AVAILABLE("available"),
        LEASED("leased"),
        EXPIRED("expired"),
        BREAKING("breaking"),
        BROKEN("broken");
        public static LeaseStateType fromString(String value)
        @Override public String toString()
    }
    public enum LeaseStatusType {
        LOCKED("locked"),
        UNLOCKED("unlocked");
        public static LeaseStatusType fromString(String value)
        @Override public String toString()
    }
    public enum ListBlobContainersIncludeType {
        METADATA("metadata"),
        DELETED("deleted"),
        SYSTEM("system");
        public static ListBlobContainersIncludeType fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class ListBlobContainersOptions {
        public ListBlobContainersOptions()
        public BlobContainerListDetails getDetails()
        public ListBlobContainersOptions setDetails(BlobContainerListDetails details)
        public Integer getMaxResultsPerPage()
        public ListBlobContainersOptions setMaxResultsPerPage(Integer maxResultsPerPage)
        public String getPrefix()
        public ListBlobContainersOptions setPrefix(String prefix)
    }
    public enum ListBlobsIncludeItem {
        COPY("copy"),
        DELETED("deleted"),
        METADATA("metadata"),
        SNAPSHOTS("snapshots"),
        UNCOMMITTEDBLOBS("uncommittedblobs"),
        VERSIONS("versions"),
        TAGS("tags"),
        IMMUTABILITY_POLICY("immutabilitypolicy"),
        LEGAL_HOLD("legalhold"),
        DELETED_WITH_VERSIONS("deletedwithversions");
        public static ListBlobsIncludeItem fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class ListBlobsOptions {
        public ListBlobsOptions()
        public BlobListDetails getDetails()
        public ListBlobsOptions setDetails(BlobListDetails details)
        public String getEndBefore()
        public ListBlobsOptions setEndBefore(String endBefore)
        public Integer getMaxResultsPerPage()
        public ListBlobsOptions setMaxResultsPerPage(Integer maxResultsPerPage)
        public String getPrefix()
        public ListBlobsOptions setPrefix(String prefix)
        public String getStartFrom()
        public ListBlobsOptions setStartFrom(String startFrom)
        public StorageResponseSerializationFormat getStorageResponseSerializationFormat()
        public ListBlobsOptions setStorageResponseSerializationFormat(StorageResponseSerializationFormat storageResponseSerializationFormat)
    }
    @Immutable
    public class ObjectReplicationPolicy {
        public ObjectReplicationPolicy(String policyId, List<ObjectReplicationRule> rules)
        public String getPolicyId()
        public List<ObjectReplicationRule> getRules()
    }
    public class ObjectReplicationRule {
        public ObjectReplicationRule(String ruleId, ObjectReplicationStatus status)
        public String getRuleId()
        public ObjectReplicationStatus getStatus()
    }
    public final class ObjectReplicationStatus extends ExpandableStringEnum<ObjectReplicationStatus> {
        public static final ObjectReplicationStatus COMPLETE = fromString("complete");
        public static final ObjectReplicationStatus FAILED = fromString("failed");
        @Deprecated public ObjectReplicationStatus()
        public static ObjectReplicationStatus fromString(String name)
        public static Collection<ObjectReplicationStatus> values()
    }
    public class PageBlobCopyIncrementalRequestConditions extends RequestConditions {
        public PageBlobCopyIncrementalRequestConditions()
        @Override public PageBlobCopyIncrementalRequestConditions setIfMatch(String ifMatch)
        @Override public PageBlobCopyIncrementalRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public PageBlobCopyIncrementalRequestConditions setIfNoneMatch(String ifNoneMatch)
        @Override public PageBlobCopyIncrementalRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        public String getTagsConditions()
        public PageBlobCopyIncrementalRequestConditions setTagsConditions(String tagsConditions)
    }
    @Immutable
    public class PageBlobItem {
        public PageBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted, String encryptionKeySha256, Long blobSequenceNumber)
        public PageBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, Long blobSequenceNumber)
        public PageBlobItem(String eTag, OffsetDateTime lastModified, byte[] contentMd5, Boolean isServerEncrypted, String encryptionKeySha256, String encryptionScope, Long blobSequenceNumber, String versionId)
        public Long getBlobSequenceNumber()
        public byte[] getContentCrc64()
        public byte[] getContentMd5()
        public String getEncryptionKeySha256()
        public String getEncryptionScope()
        public String getETag()
        public OffsetDateTime getLastModified()
        public Boolean isServerEncrypted()
        public String getVersionId()
    }
    @Fluent
    public final class PageBlobRequestConditions extends BlobRequestConditions {
        public PageBlobRequestConditions()
        @Override public PageBlobRequestConditions setIfMatch(String ifMatch)
        @Override public PageBlobRequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince)
        @Override public PageBlobRequestConditions setIfNoneMatch(String ifNoneMatch)
        public Long getIfSequenceNumberEqualTo()
        public PageBlobRequestConditions setIfSequenceNumberEqualTo(Long ifSequenceNumberEqualTo)
        public Long getIfSequenceNumberLessThan()
        public PageBlobRequestConditions setIfSequenceNumberLessThan(Long ifSequenceNumberLessThan)
        public Long getIfSequenceNumberLessThanOrEqualTo()
        public PageBlobRequestConditions setIfSequenceNumberLessThanOrEqualTo(Long ifSequenceNumberLessThanOrEqualTo)
        @Override public PageBlobRequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince)
        @Override public PageBlobRequestConditions setLeaseId(String leaseId)
        @Override public PageBlobRequestConditions setTagsConditions(String tagsConditions)
    }
    @Fluent
    public final class PageList implements XmlSerializable<PageList> {
        @Generated public PageList()
        @Generated public List<ClearRange> getClearRange()
        @Generated public PageList setClearRange(List<ClearRange> clearRange)
        @Generated public List<PageRange> getPageRange()
        @Generated public PageList setPageRange(List<PageRange> pageRange)
    }
    @Fluent
    public final class PageRange implements XmlSerializable<PageRange> {
        @Generated public PageRange()
        @Generated public long getEnd()
        @Generated public PageRange setEnd(long end)
        @Generated public long getStart()
        @Generated public PageRange setStart(long start)
    }
    @Immutable
    public final class PageRangeItem {
        public PageRangeItem(HttpRange range, boolean isClear)
        public boolean isClear()
        public HttpRange getRange()
    }
    @Fluent
    public final class ParallelTransferOptions {
        public ParallelTransferOptions()
        @Deprecated public ParallelTransferOptions(Integer blockSize, Integer maxConcurrency, ProgressReceiver progressReceiver)
        @Deprecated public ParallelTransferOptions(Integer blockSize, Integer maxConcurrency, ProgressReceiver progressReceiver, Integer maxSingleUploadSize)
        @Deprecated public Integer getBlockSize()
        public Long getBlockSizeLong()
        public ParallelTransferOptions setBlockSizeLong(Long blockSize)
        public Integer getMaxConcurrency()
        public ParallelTransferOptions setMaxConcurrency(Integer maxConcurrency)
        @Deprecated public Integer getMaxSingleUploadSize()
        public Long getMaxSingleUploadSizeLong()
        public ParallelTransferOptions setMaxSingleUploadSizeLong(Long maxSingleUploadSize)
        @Deprecated public Integer getNumBuffers()
        public ProgressListener getProgressListener()
        public ParallelTransferOptions setProgressListener(ProgressListener progressListener)
        @Deprecated public ProgressReceiver getProgressReceiver()
        @Deprecated public ParallelTransferOptions setProgressReceiver(ProgressReceiver progressReceiver)
    }
    public enum PathRenameMode {
        LEGACY("legacy"),
        POSIX("posix");
        public static PathRenameMode fromString(String value)
        @Override public String toString()
    }
    public final class PublicAccessType extends ExpandableStringEnum<PublicAccessType> {
        @Generated public static final PublicAccessType CONTAINER = fromString("container");
        @Generated public static final PublicAccessType BLOB = fromString("blob");
        @Deprecated @Generated public PublicAccessType()
        @Generated public static PublicAccessType fromString(String name)
        @Generated public static Collection<PublicAccessType> values()
    }
    public final class RehydratePriority extends ExpandableStringEnum<RehydratePriority> {
        @Generated public static final RehydratePriority HIGH = fromString("High");
        @Generated public static final RehydratePriority STANDARD = fromString("Standard");
        @Deprecated @Generated public RehydratePriority()
        @Generated public static RehydratePriority fromString(String name)
        @Generated public static Collection<RehydratePriority> values()
    }
    public enum SequenceNumberActionType {
        MAX("max"),
        UPDATE("update"),
        INCREMENT("increment");
        public static SequenceNumberActionType fromString(String value)
        @Override public String toString()
    }
    public enum SkuName {
        STANDARD_LRS("Standard_LRS"),
        STANDARD_GRS("Standard_GRS"),
        STANDARD_RAGRS("Standard_RAGRS"),
        STANDARD_ZRS("Standard_ZRS"),
        PREMIUM_LRS("Premium_LRS"),
        STANDARD_GZRS("Standard_GZRS"),
        PREMIUM_ZRS("Premium_ZRS"),
        STANDARD_RAGZRS("Standard_RAGZRS");
        public static SkuName fromString(String value)
        @Override public String toString()
    }
    @Fluent
    public final class StaticWebsite implements XmlSerializable<StaticWebsite> {
        @Generated public StaticWebsite()
        @Generated public String getDefaultIndexDocumentPath()
        @Generated public StaticWebsite setDefaultIndexDocumentPath(String defaultIndexDocumentPath)
        @Generated public boolean isEnabled()
        @Generated public StaticWebsite setEnabled(boolean enabled)
        @Generated public String getErrorDocument404Path()
        @Generated public StaticWebsite setErrorDocument404Path(String errorDocument404Path)
        @Generated public String getIndexDocument()
        @Generated public StaticWebsite setIndexDocument(String indexDocument)
    }
    @Immutable
    public class StorageAccountInfo {
        public StorageAccountInfo(SkuName skuName, AccountKind accountKind)
        public StorageAccountInfo(SkuName skuName, AccountKind accountKind, Boolean isHnsEnabled)
        public AccountKind getAccountKind()
        public boolean isHierarchicalNamespaceEnabled()
        public SkuName getSkuName()
    }
    public enum StorageResponseSerializationFormat {
        AUTO,
        XML,
        ARROW;
    }
    public enum SyncCopyStatusType {
        SUCCESS("success");
        public static SyncCopyStatusType fromString(String value)
        @Override public String toString()
    }
    public final class TaggedBlobItem {
        public TaggedBlobItem(String containerName, String name)
        public TaggedBlobItem(String containerName, String name, Map<String, String> tags)
        public String getContainerName()
        public String getName()
        public Map<String, String> getTags()
    }
    @Fluent
    public final class UserDelegationKey implements XmlSerializable<UserDelegationKey> {
        @Generated public UserDelegationKey()
        @Generated public String getSignedDelegatedUserTenantId()
        @Generated public UserDelegationKey setSignedDelegatedUserTenantId(String signedDelegatedUserTenantId)
        @Generated public OffsetDateTime getSignedExpiry()
        @Generated public UserDelegationKey setSignedExpiry(OffsetDateTime signedExpiry)
        @Generated public String getSignedObjectId()
        @Generated public UserDelegationKey setSignedObjectId(String signedObjectId)
        @Generated public String getSignedService()
        @Generated public UserDelegationKey setSignedService(String signedService)
        @Generated public OffsetDateTime getSignedStart()
        @Generated public UserDelegationKey setSignedStart(OffsetDateTime signedStart)
        @Generated public String getSignedTenantId()
        @Generated public UserDelegationKey setSignedTenantId(String signedTenantId)
        @Generated public String getSignedVersion()
        @Generated public UserDelegationKey setSignedVersion(String signedVersion)
        @Generated public String getValue()
        @Generated public UserDelegationKey setValue(String value)
    }
}
package com.azure.storage.blob.options {
    @Fluent
    public final class AppendBlobAppendBlockFromUrlOptions {
        public AppendBlobAppendBlockFromUrlOptions(String sourceUrl)
        public AppendBlobRequestConditions getDestinationRequestConditions()
        public AppendBlobAppendBlockFromUrlOptions setDestinationRequestConditions(AppendBlobRequestConditions destinationRequestConditions)
        public HttpAuthorization getSourceAuthorization()
        public AppendBlobAppendBlockFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization)
        public byte[] getSourceContentMd5()
        public AppendBlobAppendBlockFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5)
        public CustomerProvidedKey getSourceCustomerProvidedKey()
        public AppendBlobAppendBlockFromUrlOptions setSourceCustomerProvidedKey(CustomerProvidedKey sourceCustomerProvidedKey)
        public BlobRange getSourceRange()
        public AppendBlobAppendBlockFromUrlOptions setSourceRange(BlobRange sourceRange)
        public BlobRequestConditions getSourceRequestConditions()
        public AppendBlobAppendBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions)
        public FileShareTokenIntent getSourceShareTokenIntent()
        public AppendBlobAppendBlockFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent)
        public String getSourceUrl()
    }
    @Fluent
    public final class AppendBlobAppendBlockOptions {
        public AppendBlobAppendBlockOptions()
        public byte[] getContentMd5()
        public AppendBlobAppendBlockOptions setContentMd5(byte[] contentMd5)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public AppendBlobAppendBlockOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public AppendBlobRequestConditions getRequestConditions()
        public AppendBlobAppendBlockOptions setRequestConditions(AppendBlobRequestConditions requestConditions)
    }
    @Fluent
    public class AppendBlobCreateOptions {
        public AppendBlobCreateOptions()
        public Boolean hasLegalHold()
        public BlobHttpHeaders getHeaders()
        public AppendBlobCreateOptions setHeaders(BlobHttpHeaders headers)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public AppendBlobCreateOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public AppendBlobCreateOptions setLegalHold(Boolean legalHold)
        public Map<String, String> getMetadata()
        public AppendBlobCreateOptions setMetadata(Map<String, String> metadata)
        public BlobRequestConditions getRequestConditions()
        public AppendBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
        public AppendBlobCreateOptions setTags(Map<String, String> tags)
    }
    @Fluent
    public final class AppendBlobOutputStreamOptions {
        public AppendBlobOutputStreamOptions()
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public AppendBlobOutputStreamOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public AppendBlobRequestConditions getRequestConditions()
        public AppendBlobOutputStreamOptions setRequestConditions(AppendBlobRequestConditions requestConditions)
    }
    @Fluent
    public class AppendBlobSealOptions {
        public AppendBlobSealOptions()
        public AppendBlobRequestConditions getRequestConditions()
        public AppendBlobSealOptions setRequestConditions(AppendBlobRequestConditions requestConditions)
    }
    @Fluent
    public class BlobAcquireLeaseOptions {
        public BlobAcquireLeaseOptions(int durationInSeconds)
        public int getDuration()
        public BlobLeaseRequestConditions getRequestConditions()
        public BlobAcquireLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions)
    }
    @Fluent
    public class BlobBeginCopyOptions {
        public BlobBeginCopyOptions(String sourceUrl)
        public BlobRequestConditions getDestinationRequestConditions()
        public BlobBeginCopyOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlobBeginCopyOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Boolean isLegalHold()
        public BlobBeginCopyOptions setLegalHold(Boolean legalHold)
        public Map<String, String> getMetadata()
        public BlobBeginCopyOptions setMetadata(Map<String, String> metadata)
        public Duration getPollInterval()
        public BlobBeginCopyOptions setPollInterval(Duration pollInterval)
        public RehydratePriority getRehydratePriority()
        public BlobBeginCopyOptions setRehydratePriority(RehydratePriority rehydratePriority)
        public Boolean isSealDestination()
        public BlobBeginCopyOptions setSealDestination(Boolean sealDestination)
        public BlobBeginCopySourceRequestConditions getSourceRequestConditions()
        public BlobBeginCopyOptions setSourceRequestConditions(BlobBeginCopySourceRequestConditions sourceRequestConditions)
        public String getSourceUrl()
        public Map<String, String> getTags()
        public BlobBeginCopyOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlobBeginCopyOptions setTier(AccessTier tier)
    }
    @Fluent
    public class BlobBreakLeaseOptions {
        public BlobBreakLeaseOptions()
        public Duration getBreakPeriod()
        public BlobBreakLeaseOptions setBreakPeriod(Duration breakPeriod)
        public BlobLeaseRequestConditions getRequestConditions()
        public BlobBreakLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions)
    }
    @Fluent
    public class BlobChangeLeaseOptions {
        public BlobChangeLeaseOptions(String proposedId)
        public String getProposedId()
        public BlobLeaseRequestConditions getRequestConditions()
        public BlobChangeLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions)
    }
    @Fluent
    public class BlobContainerCreateOptions {
        public BlobContainerCreateOptions()
        public Map<String, String> getMetadata()
        public BlobContainerCreateOptions setMetadata(Map<String, String> metadata)
        public PublicAccessType getPublicAccessType()
        public BlobContainerCreateOptions setPublicAccessType(PublicAccessType accessType)
    }
    @Fluent
    public class BlobCopyFromUrlOptions {
        public BlobCopyFromUrlOptions(String copySource)
        public String getCopySource()
        public BlobCopySourceTagsMode getCopySourceTagsMode()
        public BlobCopyFromUrlOptions setCopySourceTagsMode(BlobCopySourceTagsMode copySourceTags)
        public BlobRequestConditions getDestinationRequestConditions()
        public BlobCopyFromUrlOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions)
        public Boolean hasLegalHold()
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlobCopyFromUrlOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public BlobCopyFromUrlOptions setLegalHold(Boolean legalHold)
        public Map<String, String> getMetadata()
        public BlobCopyFromUrlOptions setMetadata(Map<String, String> metadata)
        public HttpAuthorization getSourceAuthorization()
        public BlobCopyFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization)
        public RequestConditions getSourceRequestConditions()
        public BlobCopyFromUrlOptions setSourceRequestConditions(RequestConditions sourceRequestConditions)
        public FileShareTokenIntent getSourceShareTokenIntent()
        public BlobCopyFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent)
        public Map<String, String> getTags()
        public BlobCopyFromUrlOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlobCopyFromUrlOptions setTier(AccessTier tier)
    }
    @Fluent
    public final class BlobDownloadContentOptions {
        public BlobDownloadContentOptions()
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobDownloadContentOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public DownloadRetryOptions getDownloadRetryOptions()
        public BlobDownloadContentOptions setDownloadRetryOptions(DownloadRetryOptions downloadRetryOptions)
        public BlobRange getRange()
        public BlobDownloadContentOptions setRange(BlobRange range)
        public BlobRequestConditions getRequestConditions()
        public BlobDownloadContentOptions setRequestConditions(BlobRequestConditions requestConditions)
        public boolean isRetrieveContentRangeMd5()
        public BlobDownloadContentOptions setRetrieveContentRangeMd5(boolean retrieveContentRangeMd5)
    }
    @Fluent
    public final class BlobDownloadStreamOptions {
        public BlobDownloadStreamOptions()
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobDownloadStreamOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public DownloadRetryOptions getDownloadRetryOptions()
        public BlobDownloadStreamOptions setDownloadRetryOptions(DownloadRetryOptions downloadRetryOptions)
        public BlobRange getRange()
        public BlobDownloadStreamOptions setRange(BlobRange range)
        public BlobRequestConditions getRequestConditions()
        public BlobDownloadStreamOptions setRequestConditions(BlobRequestConditions requestConditions)
        public boolean isRetrieveContentRangeMd5()
        public BlobDownloadStreamOptions setRetrieveContentRangeMd5(boolean retrieveContentRangeMd5)
    }
    @Fluent
    public class BlobDownloadToFileOptions {
        public BlobDownloadToFileOptions(String filePath)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobDownloadToFileOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public DownloadRetryOptions getDownloadRetryOptions()
        public BlobDownloadToFileOptions setDownloadRetryOptions(DownloadRetryOptions downloadRetryOptions)
        public String getFilePath()
        public Set<OpenOption> getOpenOptions()
        public BlobDownloadToFileOptions setOpenOptions(Set<OpenOption> openOptions)
        public ParallelTransferOptions getParallelTransferOptions()
        public BlobDownloadToFileOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions)
        public BlobRange getRange()
        public BlobDownloadToFileOptions setRange(BlobRange range)
        public BlobRequestConditions getRequestConditions()
        public BlobDownloadToFileOptions setRequestConditions(BlobRequestConditions requestConditions)
        public boolean isRetrieveContentRangeMd5()
        public BlobDownloadToFileOptions setRetrieveContentRangeMd5(boolean retrieveContentRangeMd5)
    }
    @Fluent
    public class BlobGetTagsOptions {
        public BlobGetTagsOptions()
        public BlobRequestConditions getRequestConditions()
        public BlobGetTagsOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public class BlobGetUserDelegationKeyOptions {
        public BlobGetUserDelegationKeyOptions(OffsetDateTime expiresOn)
        public String getDelegatedUserTenantId()
        public BlobGetUserDelegationKeyOptions setDelegatedUserTenantId(String delegatedUserTenantId)
        public OffsetDateTime getExpiresOn()
        public OffsetDateTime getStartsOn()
        public BlobGetUserDelegationKeyOptions setStartsOn(OffsetDateTime startsOn)
    }
    @Fluent
    public class BlobInputStreamOptions {
        public BlobInputStreamOptions()
        public Integer getBlockSize()
        public BlobInputStreamOptions setBlockSize(Integer blockSize)
        public ConsistentReadControl getConsistentReadControl()
        public BlobInputStreamOptions setConsistentReadControl(ConsistentReadControl consistentReadControl)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobInputStreamOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public BlobRange getRange()
        public BlobInputStreamOptions setRange(BlobRange range)
        public BlobRequestConditions getRequestConditions()
        public BlobInputStreamOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public class BlobParallelUploadOptions {
        public BlobParallelUploadOptions(Flux<ByteBuffer> dataFlux)
        public BlobParallelUploadOptions(InputStream dataStream)
        public BlobParallelUploadOptions(BinaryData data)
        @Deprecated public BlobParallelUploadOptions(InputStream dataStream, long length)
        public boolean isComputeMd5()
        public BlobParallelUploadOptions setComputeMd5(boolean computeMd5)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobParallelUploadOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public Flux<ByteBuffer> getDataFlux()
        public InputStream getDataStream()
        public BlobHttpHeaders getHeaders()
        public BlobParallelUploadOptions setHeaders(BlobHttpHeaders headers)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlobParallelUploadOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Boolean isLegalHold()
        public BlobParallelUploadOptions setLegalHold(Boolean legalHold)
        @Deprecated public long getLength()
        public Map<String, String> getMetadata()
        public BlobParallelUploadOptions setMetadata(Map<String, String> metadata)
        public Long getOptionalLength()
        public ParallelTransferOptions getParallelTransferOptions()
        public BlobParallelUploadOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions)
        public BlobRequestConditions getRequestConditions()
        public BlobParallelUploadOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
        public BlobParallelUploadOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlobParallelUploadOptions setTier(AccessTier tier)
        @Deprecated public Duration getTimeout()
        @Deprecated public BlobParallelUploadOptions setTimeout(Duration timeout)
    }
    @Fluent
    public class BlobQueryOptions {
        public BlobQueryOptions(String expression)
        public BlobQueryOptions(String expression, OutputStream outputStream)
        public Consumer<BlobQueryError> getErrorConsumer()
        public BlobQueryOptions setErrorConsumer(Consumer<BlobQueryError> errorConsumer)
        public String getExpression()
        public BlobQuerySerialization getInputSerialization()
        public BlobQueryOptions setInputSerialization(BlobQuerySerialization inputSerialization)
        public BlobQuerySerialization getOutputSerialization()
        public BlobQueryOptions setOutputSerialization(BlobQuerySerialization outputSerialization)
        public OutputStream getOutputStream()
        public Consumer<BlobQueryProgress> getProgressConsumer()
        public BlobQueryOptions setProgressConsumer(Consumer<BlobQueryProgress> progressConsumer)
        public BlobRequestConditions getRequestConditions()
        public BlobQueryOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public class BlobReleaseLeaseOptions {
        public BlobReleaseLeaseOptions()
        public BlobLeaseRequestConditions getRequestConditions()
        public BlobReleaseLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions)
    }
    @Fluent
    public class BlobRenewLeaseOptions {
        public BlobRenewLeaseOptions()
        public BlobLeaseRequestConditions getRequestConditions()
        public BlobRenewLeaseOptions setRequestConditions(BlobLeaseRequestConditions requestConditions)
    }
    @Fluent
    public final class BlobSeekableByteChannelReadOptions {
        public BlobSeekableByteChannelReadOptions()
        public ConsistentReadControl getConsistentReadControl()
        public BlobSeekableByteChannelReadOptions setConsistentReadControl(ConsistentReadControl consistentReadControl)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobSeekableByteChannelReadOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public Long getInitialPosition()
        public BlobSeekableByteChannelReadOptions setInitialPosition(Long initialPosition)
        public Integer getReadSizeInBytes()
        public BlobSeekableByteChannelReadOptions setReadSizeInBytes(Integer readSizeInBytes)
        public BlobRequestConditions getRequestConditions()
        public BlobSeekableByteChannelReadOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public class BlobSetAccessTierOptions {
        public BlobSetAccessTierOptions(AccessTier tier)
        public String getLeaseId()
        public BlobSetAccessTierOptions setLeaseId(String leaseId)
        public RehydratePriority getPriority()
        public BlobSetAccessTierOptions setPriority(RehydratePriority priority)
        public String getTagsConditions()
        public BlobSetAccessTierOptions setTagsConditions(String tagsConditions)
        public AccessTier getTier()
    }
    @Fluent
    public class BlobSetTagsOptions {
        public BlobSetTagsOptions(Map<String, String> tags)
        public BlobRequestConditions getRequestConditions()
        public BlobSetTagsOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
    }
    @Fluent
    public class BlobUploadFromFileOptions {
        public BlobUploadFromFileOptions(String filePath)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlobUploadFromFileOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public String getFilePath()
        public BlobHttpHeaders getHeaders()
        public BlobUploadFromFileOptions setHeaders(BlobHttpHeaders headers)
        public Map<String, String> getMetadata()
        public BlobUploadFromFileOptions setMetadata(Map<String, String> metadata)
        public ParallelTransferOptions getParallelTransferOptions()
        public BlobUploadFromFileOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions)
        public BlobRequestConditions getRequestConditions()
        public BlobUploadFromFileOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
        public BlobUploadFromFileOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlobUploadFromFileOptions setTier(AccessTier tier)
    }
    public class BlobUploadFromUrlOptions {
        public BlobUploadFromUrlOptions(String sourceUrl)
        public byte[] getContentMd5()
        public BlobUploadFromUrlOptions setContentMd5(byte[] contentMd5)
        public Boolean isCopySourceBlobProperties()
        public BlobUploadFromUrlOptions setCopySourceBlobProperties(Boolean copySourceBlobProperties)
        public BlobCopySourceTagsMode getCopySourceTagsMode()
        public BlobUploadFromUrlOptions setCopySourceTagsMode(BlobCopySourceTagsMode copySourceTags)
        public BlobRequestConditions getDestinationRequestConditions()
        public BlobUploadFromUrlOptions setDestinationRequestConditions(BlobRequestConditions destinationRequestConditions)
        public BlobHttpHeaders getHeaders()
        public BlobUploadFromUrlOptions setHeaders(BlobHttpHeaders headers)
        public HttpAuthorization getSourceAuthorization()
        public BlobUploadFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization)
        public CustomerProvidedKey getSourceCustomerProvidedKey()
        public BlobUploadFromUrlOptions setSourceCustomerProvidedKey(CustomerProvidedKey sourceCustomerProvidedKey)
        public BlobRequestConditions getSourceRequestConditions()
        public BlobUploadFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions)
        public FileShareTokenIntent getSourceShareTokenIntent()
        public BlobUploadFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent)
        public String getSourceUrl()
        public Map<String, String> getTags()
        public BlobUploadFromUrlOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlobUploadFromUrlOptions setTier(AccessTier tier)
    }
    @Fluent
    public class BlockBlobCommitBlockListOptions {
        public BlockBlobCommitBlockListOptions(List<String> base64BlockIds)
        public List<String> getBase64BlockIds()
        public BlobHttpHeaders getHeaders()
        public BlockBlobCommitBlockListOptions setHeaders(BlobHttpHeaders headers)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlockBlobCommitBlockListOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Boolean isLegalHold()
        public BlockBlobCommitBlockListOptions setLegalHold(Boolean legalHold)
        public Map<String, String> getMetadata()
        public BlockBlobCommitBlockListOptions setMetadata(Map<String, String> metadata)
        public BlobRequestConditions getRequestConditions()
        public BlockBlobCommitBlockListOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
        public BlockBlobCommitBlockListOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlockBlobCommitBlockListOptions setTier(AccessTier tier)
    }
    @Fluent
    public class BlockBlobListBlocksOptions {
        public BlockBlobListBlocksOptions(BlockListType type)
        public String getIfTagsMatch()
        public BlockBlobListBlocksOptions setIfTagsMatch(String ifTagsMatch)
        public String getLeaseId()
        public BlockBlobListBlocksOptions setLeaseId(String leaseId)
        public BlockListType getType()
    }
    public class BlockBlobOutputStreamOptions {
        public BlockBlobOutputStreamOptions()
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlockBlobOutputStreamOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public BlobHttpHeaders getHeaders()
        public BlockBlobOutputStreamOptions setHeaders(BlobHttpHeaders headers)
        public Map<String, String> getMetadata()
        public BlockBlobOutputStreamOptions setMetadata(Map<String, String> metadata)
        public ParallelTransferOptions getParallelTransferOptions()
        public BlockBlobOutputStreamOptions setParallelTransferOptions(ParallelTransferOptions parallelTransferOptions)
        public BlobRequestConditions getRequestConditions()
        public BlockBlobOutputStreamOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
        public BlockBlobOutputStreamOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlockBlobOutputStreamOptions setTier(AccessTier tier)
    }
    public final class BlockBlobSeekableByteChannelWriteOptions {
        public BlockBlobSeekableByteChannelWriteOptions(WriteMode mode)
        public Long getBlockSizeInBytes()
        public BlockBlobSeekableByteChannelWriteOptions setBlockSizeInBytes(Long blockSizeInBytes)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlockBlobSeekableByteChannelWriteOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public BlobHttpHeaders getHeaders()
        public BlockBlobSeekableByteChannelWriteOptions setHeaders(BlobHttpHeaders headers)
        public Map<String, String> getMetadata()
        public BlockBlobSeekableByteChannelWriteOptions setMetadata(Map<String, String> metadata)
        public BlobRequestConditions getRequestConditions()
        public BlockBlobSeekableByteChannelWriteOptions setRequestConditions(BlobRequestConditions conditions)
        public Map<String, String> getTags()
        public BlockBlobSeekableByteChannelWriteOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlockBlobSeekableByteChannelWriteOptions setTier(AccessTier tier)
        public WriteMode getWriteMode()
        public static final class WriteMode extends ExpandableStringEnum<WriteMode> {
            public static final WriteMode OVERWRITE = fromString("Overwrite");
            @Deprecated public WriteMode()
            public static WriteMode fromString(String name)
            public static Collection<WriteMode> values()
        }
    }
    public class BlockBlobSimpleUploadOptions {
        public BlockBlobSimpleUploadOptions(BinaryData data)
        public BlockBlobSimpleUploadOptions(Flux<ByteBuffer> data, long length)
        public BlockBlobSimpleUploadOptions(InputStream data, long length)
        public byte[] getContentMd5()
        public BlockBlobSimpleUploadOptions setContentMd5(byte[] contentMd5)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlockBlobSimpleUploadOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public BinaryData getData()
        public Flux<ByteBuffer> getDataFlux()
        public InputStream getDataStream()
        public BlobHttpHeaders getHeaders()
        public BlockBlobSimpleUploadOptions setHeaders(BlobHttpHeaders headers)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public BlockBlobSimpleUploadOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Boolean isLegalHold()
        public BlockBlobSimpleUploadOptions setLegalHold(Boolean legalHold)
        public long getLength()
        public Map<String, String> getMetadata()
        public BlockBlobSimpleUploadOptions setMetadata(Map<String, String> metadata)
        public BlobRequestConditions getRequestConditions()
        public BlockBlobSimpleUploadOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Map<String, String> getTags()
        public BlockBlobSimpleUploadOptions setTags(Map<String, String> tags)
        public AccessTier getTier()
        public BlockBlobSimpleUploadOptions setTier(AccessTier tier)
    }
    @Fluent
    public final class BlockBlobStageBlockFromUrlOptions {
        public BlockBlobStageBlockFromUrlOptions(String base64BlockId, String sourceUrl)
        public String getBase64BlockId()
        public String getLeaseId()
        public BlockBlobStageBlockFromUrlOptions setLeaseId(String leaseId)
        public HttpAuthorization getSourceAuthorization()
        public BlockBlobStageBlockFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization)
        public byte[] getSourceContentMd5()
        public BlockBlobStageBlockFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5)
        public CustomerProvidedKey getSourceCustomerProvidedKey()
        public BlockBlobStageBlockFromUrlOptions setSourceCustomerProvidedKey(CustomerProvidedKey sourceCustomerProvidedKey)
        public BlobRange getSourceRange()
        public BlockBlobStageBlockFromUrlOptions setSourceRange(BlobRange sourceRange)
        public BlobRequestConditions getSourceRequestConditions()
        public BlockBlobStageBlockFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions)
        public FileShareTokenIntent getSourceShareTokenIntent()
        public BlockBlobStageBlockFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent)
        public String getSourceUrl()
    }
    @Fluent
    public final class BlockBlobStageBlockOptions {
        public BlockBlobStageBlockOptions(String base64BlockId, BinaryData data)
        public String getBase64BlockId()
        public byte[] getContentMd5()
        public BlockBlobStageBlockOptions setContentMd5(byte[] contentMd5)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public BlockBlobStageBlockOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public BinaryData getData()
        public String getLeaseId()
        public BlockBlobStageBlockOptions setLeaseId(String leaseId)
    }
    public class FindBlobsOptions {
        public FindBlobsOptions(String query)
        public Integer getMaxResultsPerPage()
        public FindBlobsOptions setMaxResultsPerPage(Integer maxResultsPerPage)
        public String getQuery()
    }
    @Fluent
    public class ListPageRangesDiffOptions {
        public ListPageRangesDiffOptions(BlobRange range, String previousSnapshot)
        public Integer getMaxResultsPerPage()
        public ListPageRangesDiffOptions setMaxResultsPerPage(Integer pageSize)
        public String getPreviousSnapshot()
        public BlobRange getRange()
        public BlobRequestConditions getRequestConditions()
        public ListPageRangesDiffOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public class ListPageRangesOptions {
        public ListPageRangesOptions(BlobRange range)
        public Integer getMaxResultsPerPage()
        public ListPageRangesOptions setMaxResultsPerPage(Integer pageSize)
        public BlobRange getRange()
        public BlobRequestConditions getRequestConditions()
        public ListPageRangesOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public class PageBlobCopyIncrementalOptions {
        public PageBlobCopyIncrementalOptions(String source, String snapshot)
        public PageBlobCopyIncrementalRequestConditions getRequestConditions()
        public PageBlobCopyIncrementalOptions setRequestConditions(PageBlobCopyIncrementalRequestConditions requestConditions)
        public String getSnapshot()
        public String getSource()
    }
    public class PageBlobCreateOptions {
        public PageBlobCreateOptions(long size)
        public BlobHttpHeaders getHeaders()
        public PageBlobCreateOptions setHeaders(BlobHttpHeaders headers)
        public BlobImmutabilityPolicy getImmutabilityPolicy()
        public PageBlobCreateOptions setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Boolean isLegalHold()
        public PageBlobCreateOptions setLegalHold(Boolean legalHold)
        public Map<String, String> getMetadata()
        public PageBlobCreateOptions setMetadata(Map<String, String> metadata)
        public BlobRequestConditions getRequestConditions()
        public PageBlobCreateOptions setRequestConditions(BlobRequestConditions requestConditions)
        public Long getSequenceNumber()
        public PageBlobCreateOptions setSequenceNumber(Long sequenceNumber)
        public long getSize()
        public Map<String, String> getTags()
        public PageBlobCreateOptions setTags(Map<String, String> tags)
    }
    @Fluent
    public final class PageBlobOutputStreamOptions {
        public PageBlobOutputStreamOptions(PageRange pageRange)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public PageBlobOutputStreamOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public PageRange getPageRange()
        public BlobRequestConditions getRequestConditions()
        public PageBlobOutputStreamOptions setRequestConditions(BlobRequestConditions requestConditions)
    }
    @Fluent
    public final class PageBlobUploadPagesFromUrlOptions {
        public PageBlobUploadPagesFromUrlOptions(PageRange range, String sourceUrl)
        public PageBlobRequestConditions getDestinationRequestConditions()
        public PageBlobUploadPagesFromUrlOptions setDestinationRequestConditions(PageBlobRequestConditions destinationRequestConditions)
        public PageRange getRange()
        public HttpAuthorization getSourceAuthorization()
        public PageBlobUploadPagesFromUrlOptions setSourceAuthorization(HttpAuthorization sourceAuthorization)
        public byte[] getSourceContentMd5()
        public PageBlobUploadPagesFromUrlOptions setSourceContentMd5(byte[] sourceContentMd5)
        public CustomerProvidedKey getSourceCustomerProvidedKey()
        public PageBlobUploadPagesFromUrlOptions setSourceCustomerProvidedKey(CustomerProvidedKey sourceCustomerProvidedKey)
        public Long getSourceOffset()
        public PageBlobUploadPagesFromUrlOptions setSourceOffset(Long sourceOffset)
        public BlobRequestConditions getSourceRequestConditions()
        public PageBlobUploadPagesFromUrlOptions setSourceRequestConditions(BlobRequestConditions sourceRequestConditions)
        public FileShareTokenIntent getSourceShareTokenIntent()
        public PageBlobUploadPagesFromUrlOptions setSourceShareTokenIntent(FileShareTokenIntent sourceShareTokenIntent)
        public String getSourceUrl()
    }
    @Fluent
    public final class PageBlobUploadPagesOptions {
        public PageBlobUploadPagesOptions()
        public byte[] getContentMd5()
        public PageBlobUploadPagesOptions setContentMd5(byte[] contentMd5)
        public ContentValidationAlgorithm getContentValidationAlgorithm()
        public PageBlobUploadPagesOptions setContentValidationAlgorithm(ContentValidationAlgorithm contentValidationAlgorithm)
        public PageBlobRequestConditions getRequestConditions()
        public PageBlobUploadPagesOptions setRequestConditions(PageBlobRequestConditions requestConditions)
    }
    @Fluent
    public class UndeleteBlobContainerOptions {
        public UndeleteBlobContainerOptions(String deletedContainerName, String deletedContainerVersion)
        public String getDeletedContainerName()
        public String getDeletedContainerVersion()
        @Deprecated public String getDestinationContainerName()
        @Deprecated public UndeleteBlobContainerOptions setDestinationContainerName(String destinationContainerName)
    }
}
package com.azure.storage.blob.sas {
    public final class BlobContainerSasPermission {
        public BlobContainerSasPermission()
        public BlobContainerSasPermission setAddPermission(boolean hasAddPermission)
        public BlobContainerSasPermission setCreatePermission(boolean hasCreatePermission)
        public BlobContainerSasPermission setDeletePermission(boolean hasDeletePermission)
        public BlobContainerSasPermission setDeleteVersionPermission(boolean hasDeleteVersionPermission)
        public BlobContainerSasPermission setExecutePermission(boolean hasExecutePermission)
        public BlobContainerSasPermission setFilterPermission(boolean hasFilterPermission)
        public boolean hasAddPermission()
        public boolean hasCreatePermission()
        public boolean hasDeletePermission()
        public boolean hasDeleteVersionPermission()
        public boolean hasExecutePermission()
        public boolean hasFilterPermission()
        public boolean hasImmutabilityPolicyPermission()
        public boolean hasListPermission()
        public boolean hasMovePermission()
        public boolean hasReadPermission()
        public boolean hasTagsPermission()
        public boolean hasWritePermission()
        public BlobContainerSasPermission setImmutabilityPolicyPermission(boolean immutabilityPolicyPermission)
        public BlobContainerSasPermission setListPermission(boolean hasListPermission)
        public BlobContainerSasPermission setMovePermission(boolean hasMovePermission)
        public static BlobContainerSasPermission parse(String permissionString)
        public BlobContainerSasPermission setReadPermission(boolean hasReadPermission)
        public BlobContainerSasPermission setTagsPermission(boolean tagsPermission)
        @Override public String toString()
        public BlobContainerSasPermission setWritePermission(boolean hasWritePermission)
    }
    public final class BlobSasPermission {
        public BlobSasPermission()
        public BlobSasPermission setAddPermission(boolean hasAddPermission)
        public BlobSasPermission setCreatePermission(boolean hasCreatePermission)
        public BlobSasPermission setDeletePermission(boolean hasDeletePermission)
        public BlobSasPermission setDeleteVersionPermission(boolean hasDeleteVersionPermission)
        public BlobSasPermission setExecutePermission(boolean hasExecutePermission)
        public boolean hasAddPermission()
        public boolean hasCreatePermission()
        public boolean hasDeletePermission()
        public boolean hasDeleteVersionPermission()
        public boolean hasExecutePermission()
        public boolean hasImmutabilityPolicyPermission()
        public boolean hasListPermission()
        public boolean hasMovePermission()
        public boolean hasPermanentDeletePermission()
        public boolean hasReadPermission()
        public boolean hasTagsPermission()
        public boolean hasWritePermission()
        public BlobSasPermission setImmutabilityPolicyPermission(boolean immutabilityPolicyPermission)
        public BlobSasPermission setListPermission(boolean hasListPermission)
        public BlobSasPermission setMovePermission(boolean hasMovePermission)
        public static BlobSasPermission parse(String permissionString)
        public BlobSasPermission setPermanentDeletePermission(boolean permanentDeletePermission)
        public BlobSasPermission setReadPermission(boolean hasReadPermission)
        public BlobSasPermission setTagsPermission(boolean tagsPermission)
        @Override public String toString()
        public BlobSasPermission setWritePermission(boolean hasWritePermission)
    }
    @Deprecated
    public enum BlobSasServiceVersion implements ServiceVersion {
        V2019_02_02("2019-02-02"),
        V2019_07_07("2019-07-07"),
        V2019_12_12("2019-12-12"),
        V2020_02_10("2020-02-10"),
        V2020_04_08("2020-04-08"),
        V2020_06_12("2020-06-12"),
        V2020_08_04("2020-08-04");
        public static BlobSasServiceVersion getLatest(// returns V2020_08_04 )
        @Override public String getVersion()
    }
    @Deprecated
    public final class BlobServiceSasQueryParameters extends BaseSasQueryParameters {
        @Deprecated public BlobServiceSasQueryParameters(Map<String, String[]> queryParamsMap, boolean removeSasParametersFromMap)
        @Deprecated public String getCacheControl()
        @Deprecated public String getContentDisposition()
        @Deprecated public String getContentEncoding()
        @Deprecated public String getContentLanguage()
        @Deprecated public String getContentType()
        @Deprecated public String encode()
        @Deprecated public String getIdentifier()
        @Deprecated public OffsetDateTime getKeyExpiry()
        @Deprecated public String getKeyObjectId()
        @Deprecated public String getKeyService()
        @Deprecated public OffsetDateTime getKeyStart()
        @Deprecated public String getKeyTenantId()
        @Deprecated public String getKeyVersion()
        @Deprecated public String getResource()
    }
    public final class BlobServiceSasSignatureValues {
        @Deprecated public BlobServiceSasSignatureValues()
        public BlobServiceSasSignatureValues(String identifier)
        public BlobServiceSasSignatureValues(OffsetDateTime expiryTime, BlobContainerSasPermission permissions)
        public BlobServiceSasSignatureValues(OffsetDateTime expiryTime, BlobSasPermission permissions)
        @Deprecated public BlobServiceSasSignatureValues(String version, SasProtocol sasProtocol, OffsetDateTime startTime, OffsetDateTime expiryTime, String permission, SasIpRange sasIpRange, String identifier, String cacheControl, String contentDisposition, String contentEncoding, String contentLanguage, String contentType)
        @Deprecated public String getBlobName()
        @Deprecated public BlobServiceSasSignatureValues setBlobName(String blobName)
        public String getCacheControl()
        public BlobServiceSasSignatureValues setCacheControl(String cacheControl)
        @Deprecated public String getContainerName()
        @Deprecated public BlobServiceSasSignatureValues setContainerName(String containerName)
        public String getContentDisposition()
        public BlobServiceSasSignatureValues setContentDisposition(String contentDisposition)
        public String getContentEncoding()
        public BlobServiceSasSignatureValues setContentEncoding(String contentEncoding)
        public String getContentLanguage()
        public BlobServiceSasSignatureValues setContentLanguage(String contentLanguage)
        public String getContentType()
        public BlobServiceSasSignatureValues setContentType(String contentType)
        public String getCorrelationId()
        public BlobServiceSasSignatureValues setCorrelationId(String correlationId)
        public String getDelegatedUserObjectId()
        public BlobServiceSasSignatureValues setDelegatedUserObjectId(String delegatedUserObjectId)
        public Boolean isDirectory()
        public BlobServiceSasSignatureValues setDirectory(Boolean isDirectory)
        public OffsetDateTime getExpiryTime()
        public BlobServiceSasSignatureValues setExpiryTime(OffsetDateTime expiryTime)
        @Deprecated public BlobServiceSasQueryParameters generateSasQueryParameters(StorageSharedKeyCredential storageSharedKeyCredentials)
        @Deprecated public BlobServiceSasQueryParameters generateSasQueryParameters(UserDelegationKey delegationKey, String accountName)
        public String getIdentifier()
        public BlobServiceSasSignatureValues setIdentifier(String identifier)
        public String getPermissions()
        public BlobServiceSasSignatureValues setPermissions(BlobSasPermission permissions)
        public BlobServiceSasSignatureValues setPermissions(BlobContainerSasPermission permissions)
        public String getPreauthorizedAgentObjectId()
        public BlobServiceSasSignatureValues setPreauthorizedAgentObjectId(String preauthorizedAgentObjectId)
        public SasProtocol getProtocol()
        public BlobServiceSasSignatureValues setProtocol(SasProtocol protocol)
        public Map<String, String> getRequestHeaders()
        public BlobServiceSasSignatureValues setRequestHeaders(Map<String, String> requestHeaders)
        public Map<String, String> getRequestQueryParameters()
        public BlobServiceSasSignatureValues setRequestQueryParameters(Map<String, String> requestQueryParameters)
        public SasIpRange getSasIpRange()
        public BlobServiceSasSignatureValues setSasIpRange(SasIpRange sasIpRange)
        @Deprecated public String getSnapshotId()
        @Deprecated public BlobServiceSasSignatureValues setSnapshotId(String snapshotId)
        public OffsetDateTime getStartTime()
        public BlobServiceSasSignatureValues setStartTime(OffsetDateTime startTime)
        public String getVersion()
        @Deprecated public BlobServiceSasSignatureValues setVersion(String version)
    }
}
package com.azure.storage.blob.specialized {
    @ServiceClient(builder  =  SpecializedBlobClientBuilder, isAsync  =  true)
    public final class AppendBlobAsyncClient extends BlobAsyncClientBase {
        @Deprecated public static final int MAX_APPEND_BLOCK_BYTES = 4 *  Constants.MB;
        @Deprecated public static final int MAX_BLOCKS = 50000;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public Mono<AppendBlobItem> appendBlock(Flux<ByteBuffer> data, long length)
        public Mono<AppendBlobItem> appendBlockFromUrl(String sourceUrl, BlobRange sourceRange)
        public Mono<Response<AppendBlobItem>> appendBlockFromUrlWithResponse(AppendBlobAppendBlockFromUrlOptions options)
        public Mono<Response<AppendBlobItem>> appendBlockFromUrlWithResponse(String sourceUrl, BlobRange sourceRange, byte[] sourceContentMD5, AppendBlobRequestConditions destRequestConditions, BlobRequestConditions sourceRequestConditions)
        public Mono<Response<AppendBlobItem>> appendBlockWithResponse(Flux<ByteBuffer> data, long length, AppendBlobAppendBlockOptions options)
        @Deprecated public Mono<Response<AppendBlobItem>> appendBlockWithResponse(Flux<ByteBuffer> data, long length, byte[] contentMd5, AppendBlobRequestConditions appendBlobRequestConditions)
        public Mono<AppendBlobItem> create()
        public Mono<AppendBlobItem> create(boolean overwrite)
        public Mono<AppendBlobItem> createIfNotExists()
        public Mono<Response<AppendBlobItem>> createIfNotExistsWithResponse(AppendBlobCreateOptions options)
        public Mono<Response<AppendBlobItem>> createWithResponse(AppendBlobCreateOptions options)
        public Mono<Response<AppendBlobItem>> createWithResponse(BlobHttpHeaders headers, Map<String, String> metadata, BlobRequestConditions requestConditions)
        public Mono<Void> seal()
        public Mono<Response<Void>> sealWithResponse(AppendBlobSealOptions options)
        // Non-Service Methods:
        @Override public AppendBlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey)
        @Override public AppendBlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope)
        public int getMaxAppendBlockBytes()
        public int getMaxBlocks()
    }
    @ServiceClient(builder  =  SpecializedBlobClientBuilder)
    public final class AppendBlobClient extends BlobClientBase {
        @Deprecated public static final int MAX_APPEND_BLOCK_BYTES = 4 *  Constants.MB;
        @Deprecated public static final int MAX_BLOCKS = 50000;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public AppendBlobItem appendBlock(InputStream data, long length)
        public AppendBlobItem appendBlockFromUrl(String sourceUrl, BlobRange sourceRange)
        public Response<AppendBlobItem> appendBlockFromUrlWithResponse(AppendBlobAppendBlockFromUrlOptions options, Duration timeout, Context context)
        public Response<AppendBlobItem> appendBlockFromUrlWithResponse(String sourceUrl, BlobRange sourceRange, byte[] sourceContentMd5, AppendBlobRequestConditions destRequestConditions, BlobRequestConditions sourceRequestConditions, Duration timeout, Context context)
        public Response<AppendBlobItem> appendBlockWithResponse(InputStream data, long length, AppendBlobAppendBlockOptions options, Duration timeout, Context context)
        @Deprecated public Response<AppendBlobItem> appendBlockWithResponse(InputStream data, long length, byte[] contentMd5, AppendBlobRequestConditions appendBlobRequestConditions, Duration timeout, Context context)
        public AppendBlobItem create()
        public AppendBlobItem create(boolean overwrite)
        public AppendBlobItem createIfNotExists()
        public Response<AppendBlobItem> createIfNotExistsWithResponse(AppendBlobCreateOptions options, Duration timeout, Context context)
        public Response<AppendBlobItem> createWithResponse(AppendBlobCreateOptions options, Duration timeout, Context context)
        public Response<AppendBlobItem> createWithResponse(BlobHttpHeaders headers, Map<String, String> metadata, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public void seal()
        public Response<Void> sealWithResponse(AppendBlobSealOptions options, Duration timeout, Context context)
        // Non-Service Methods:
        public BlobOutputStream getBlobOutputStream()
        public BlobOutputStream getBlobOutputStream(boolean overwrite)
        public BlobOutputStream getBlobOutputStream(AppendBlobRequestConditions requestConditions)
        public BlobOutputStream getBlobOutputStream(AppendBlobOutputStreamOptions options)
        @Override public AppendBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey)
        @Override public AppendBlobClient getEncryptionScopeClient(String encryptionScope)
        public int getMaxAppendBlockBytes()
        public int getMaxBlocks()
    }
    public class BlobAsyncClientBase {
        protected final AzureBlobStorageImpl azureBlobStorage ;
        protected final EncryptionScope encryptionScope ;
        protected final String accountName ;
        protected final String containerName ;
        protected final String blobName ;
        protected final BlobServiceVersion serviceVersion ;
        protected BlobAsyncClientBase(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey)
        protected BlobAsyncClientBase(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey, EncryptionScope encryptionScope)
        protected BlobAsyncClientBase(HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey, EncryptionScope encryptionScope, String versionId)
        public Mono<Void> abortCopyFromUrl(String copyId)
        public Mono<Response<Void>> abortCopyFromUrlWithResponse(String copyId, String leaseId)
        public Mono<Void> setAccessTier(AccessTier tier)
        public Mono<Response<Void>> setAccessTierWithResponse(BlobSetAccessTierOptions options)
        public Mono<Response<Void>> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId)
        public Mono<StorageAccountInfo> getAccountInfo()
        public Mono<Response<StorageAccountInfo>> getAccountInfoWithResponse()
        public String getAccountName()
        public String getAccountUrl()
        public PollerFlux<BlobCopyInfo, Void> beginCopy(BlobBeginCopyOptions options)
        public PollerFlux<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval)
        public PollerFlux<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier, RehydratePriority priority, RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions, Duration pollInterval)
        public final String getBlobName()
        public String getBlobUrl()
        public BlobContainerAsyncClient getContainerAsyncClient()
        public final String getContainerName()
        public Mono<String> copyFromUrl(String copySource)
        public Mono<Response<String>> copyFromUrlWithResponse(BlobCopyFromUrlOptions options)
        public Mono<Response<String>> copyFromUrlWithResponse(String copySource, Map<String, String> metadata, AccessTier tier, RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions)
        public Mono<BlobAsyncClientBase> createSnapshot()
        public Mono<Response<BlobAsyncClientBase>> createSnapshotWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions)
        public CpkInfo getCustomerProvidedKey()
        public BlobAsyncClientBase getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey)
        public Mono<Void> delete()
        public Mono<Boolean> deleteIfExists()
        public Mono<Response<Boolean>> deleteIfExistsWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobRequestConditions requestConditions)
        public Mono<Void> deleteImmutabilityPolicy()
        public Mono<Response<Void>> deleteImmutabilityPolicyWithResponse()
        public Mono<Response<Void>> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobRequestConditions requestConditions)
        @Deprecated public Flux<ByteBuffer> download()
        public Mono<BinaryData> downloadContent()
        public Mono<BlobDownloadContentAsyncResponse> downloadContentWithResponse(BlobDownloadContentOptions options)
        public Mono<BlobDownloadContentAsyncResponse> downloadContentWithResponse(DownloadRetryOptions options, BlobRequestConditions requestConditions)
        public Flux<ByteBuffer> downloadStream()
        public Mono<BlobDownloadAsyncResponse> downloadStreamWithResponse(BlobDownloadStreamOptions options)
        public Mono<BlobDownloadAsyncResponse> downloadStreamWithResponse(BlobRange range, DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5)
        public Mono<BlobProperties> downloadToFile(String filePath)
        public Mono<BlobProperties> downloadToFile(String filePath, boolean overwrite)
        public Mono<Response<BlobProperties>> downloadToFileWithResponse(BlobDownloadToFileOptions options)
        public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range, ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean rangeGetContentMd5)
        public Mono<Response<BlobProperties>> downloadToFileWithResponse(String filePath, BlobRange range, ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions)
        @Deprecated public Mono<BlobDownloadAsyncResponse> downloadWithResponse(BlobRange range, DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5)
        protected String getEncryptionScope()
        public BlobAsyncClientBase getEncryptionScopeAsyncClient(String encryptionScope)
        public Mono<Boolean> exists()
        public Mono<Response<Boolean>> existsWithResponse()
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Consumer<String> stringToSignHandler, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Consumer<String> stringToSignHandler, Context context)
        public Mono<Void> setHttpHeaders(BlobHttpHeaders headers)
        public Mono<Response<Void>> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions requestConditions)
        public HttpPipeline getHttpPipeline()
        public Mono<BlobImmutabilityPolicy> setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Mono<Response<BlobImmutabilityPolicy>> setImmutabilityPolicyWithResponse(BlobImmutabilityPolicy immutabilityPolicy, BlobRequestConditions requestConditions)
        public Mono<BlobLegalHoldResult> setLegalHold(boolean legalHold)
        public Mono<Response<BlobLegalHoldResult>> setLegalHoldWithResponse(boolean legalHold)
        public Mono<Void> setMetadata(Map<String, String> metadata)
        public Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions)
        public Mono<BlobProperties> getProperties()
        public Mono<Response<BlobProperties>> getPropertiesWithResponse(BlobRequestConditions requestConditions)
        public Flux<ByteBuffer> query(String expression)
        public Mono<BlobQueryAsyncResponse> queryWithResponse(BlobQueryOptions queryOptions)
        public BlobServiceVersion getServiceVersion()
        public boolean isSnapshot()
        public BlobAsyncClientBase getSnapshotClient(String snapshot)
        public String getSnapshotId()
        public Mono<Map<String, String>> getTags()
        public Mono<Void> setTags(Map<String, String> tags)
        public Mono<Response<Map<String, String>>> getTagsWithResponse(BlobGetTagsOptions options)
        public Mono<Response<Void>> setTagsWithResponse(BlobSetTagsOptions options)
        public Mono<Void> undelete()
        public Mono<Response<Void>> undeleteWithResponse()
        public BlobAsyncClientBase getVersionClient(String versionId)
        public String getVersionId()
    }
    public class BlobClientBase {
        protected final String accountName ;
        protected final String containerName ;
        protected final String blobName ;
        protected final BlobServiceVersion serviceVersion ;
        protected BlobClientBase(BlobAsyncClientBase client)
        protected BlobClientBase(BlobAsyncClientBase client, HttpPipeline pipeline, String url, BlobServiceVersion serviceVersion, String accountName, String containerName, String blobName, String snapshot, CpkInfo customerProvidedKey, EncryptionScope encryptionScope, String versionId)
        public void abortCopyFromUrl(String copyId)
        public Response<Void> abortCopyFromUrlWithResponse(String copyId, String leaseId, Duration timeout, Context context)
        public void setAccessTier(AccessTier tier)
        public Response<Void> setAccessTierWithResponse(BlobSetAccessTierOptions options, Duration timeout, Context context)
        public Response<Void> setAccessTierWithResponse(AccessTier tier, RehydratePriority priority, String leaseId, Duration timeout, Context context)
        public StorageAccountInfo getAccountInfo()
        public Response<StorageAccountInfo> getAccountInfoWithResponse(Duration timeout, Context context)
        public String getAccountName()
        public String getAccountUrl()
        public SyncPoller<BlobCopyInfo, Void> beginCopy(BlobBeginCopyOptions options)
        public SyncPoller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Duration pollInterval)
        public SyncPoller<BlobCopyInfo, Void> beginCopy(String sourceUrl, Map<String, String> metadata, AccessTier tier, RehydratePriority priority, RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions, Duration pollInterval)
        public final String getBlobName()
        public String getBlobUrl()
        public BlobContainerClient getContainerClient()
        public final String getContainerName()
        public String copyFromUrl(String copySource)
        public Response<String> copyFromUrlWithResponse(BlobCopyFromUrlOptions options, Duration timeout, Context context)
        public Response<String> copyFromUrlWithResponse(String copySource, Map<String, String> metadata, AccessTier tier, RequestConditions sourceModifiedRequestConditions, BlobRequestConditions destRequestConditions, Duration timeout, Context context)
        public BlobClientBase createSnapshot()
        public Response<BlobClientBase> createSnapshotWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public CpkInfo getCustomerProvidedKey()
        public BlobClientBase getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey)
        public void delete()
        public boolean deleteIfExists()
        public Response<Boolean> deleteIfExistsWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public void deleteImmutabilityPolicy()
        public Response<Void> deleteImmutabilityPolicyWithResponse(Duration timeout, Context context)
        public Response<Void> deleteWithResponse(DeleteSnapshotsOptionType deleteBlobSnapshotOptions, BlobRequestConditions requestConditions, Duration timeout, Context context)
        @Deprecated public void download(OutputStream stream)
        public BinaryData downloadContent()
        public BlobDownloadContentResponse downloadContentWithResponse(BlobDownloadContentOptions options, Duration timeout, Context context)
        public BlobDownloadContentResponse downloadContentWithResponse(DownloadRetryOptions options, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public BlobDownloadContentResponse downloadContentWithResponse(DownloadRetryOptions options, BlobRequestConditions requestConditions, BlobRange range, boolean getRangeContentMd5, Duration timeout, Context context)
        public void downloadStream(OutputStream stream)
        public BlobDownloadResponse downloadStreamWithResponse(OutputStream stream, BlobDownloadStreamOptions options, Duration timeout, Context context)
        public BlobDownloadResponse downloadStreamWithResponse(OutputStream stream, BlobRange range, DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5, Duration timeout, Context context)
        public BlobProperties downloadToFile(String filePath)
        public BlobProperties downloadToFile(String filePath, boolean overwrite)
        public Response<BlobProperties> downloadToFileWithResponse(BlobDownloadToFileOptions options, Duration timeout, Context context)
        public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range, ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions, BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Duration timeout, Context context)
        public Response<BlobProperties> downloadToFileWithResponse(String filePath, BlobRange range, ParallelTransferOptions parallelTransferOptions, DownloadRetryOptions downloadRetryOptions, BlobRequestConditions requestConditions, boolean rangeGetContentMd5, Set<OpenOption> openOptions, Duration timeout, Context context)
        @Deprecated public BlobDownloadResponse downloadWithResponse(OutputStream stream, BlobRange range, DownloadRetryOptions options, BlobRequestConditions requestConditions, boolean getRangeContentMd5, Duration timeout, Context context)
        public String getEncryptionScope()
        public BlobClientBase getEncryptionScopeClient(String encryptionScope)
        public Boolean exists()
        public Response<Boolean> existsWithResponse(Duration timeout, Context context)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Context context)
        public String generateSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, Consumer<String> stringToSignHandler, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Context context)
        public String generateUserDelegationSas(BlobServiceSasSignatureValues blobServiceSasSignatureValues, UserDelegationKey userDelegationKey, String accountName, Consumer<String> stringToSignHandler, Context context)
        public void setHttpHeaders(BlobHttpHeaders headers)
        public Response<Void> setHttpHeadersWithResponse(BlobHttpHeaders headers, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public HttpPipeline getHttpPipeline()
        public BlobImmutabilityPolicy setImmutabilityPolicy(BlobImmutabilityPolicy immutabilityPolicy)
        public Response<BlobImmutabilityPolicy> setImmutabilityPolicyWithResponse(BlobImmutabilityPolicy immutabilityPolicy, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public BlobLegalHoldResult setLegalHold(boolean legalHold)
        public Response<BlobLegalHoldResult> setLegalHoldWithResponse(boolean legalHold, Duration timeout, Context context)
        public void setMetadata(Map<String, String> metadata)
        public Response<Void> setMetadataWithResponse(Map<String, String> metadata, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public BlobInputStream openInputStream()
        public BlobInputStream openInputStream(BlobInputStreamOptions options)
        public BlobInputStream openInputStream(BlobRange range, BlobRequestConditions requestConditions)
        public BlobInputStream openInputStream(BlobInputStreamOptions options, Context context)
        public InputStream openQueryInputStream(String expression)
        public Response<InputStream> openQueryInputStreamWithResponse(BlobQueryOptions queryOptions)
        public BlobSeekableByteChannelReadResult openSeekableByteChannelRead(BlobSeekableByteChannelReadOptions options, Context context)
        public BlobProperties getProperties()
        public Response<BlobProperties> getPropertiesWithResponse(BlobRequestConditions requestConditions, Duration timeout, Context context)
        public void query(OutputStream stream, String expression)
        public BlobQueryResponse queryWithResponse(BlobQueryOptions queryOptions, Duration timeout, Context context)
        public BlobServiceVersion getServiceVersion()
        public boolean isSnapshot()
        public BlobClientBase getSnapshotClient(String snapshot)
        public String getSnapshotId()
        public Map<String, String> getTags()
        public void setTags(Map<String, String> tags)
        public Response<Map<String, String>> getTagsWithResponse(BlobGetTagsOptions options, Duration timeout, Context context)
        public Response<Void> setTagsWithResponse(BlobSetTagsOptions options, Duration timeout, Context context)
        public void undelete()
        public Response<Void> undeleteWithResponse(Duration timeout, Context context)
        public BlobClientBase getVersionClient(String versionId)
        public String getVersionId()
    }
    public final class BlobInputStream extends StorageInputStream {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        @Override protected synchronized ByteBuffer dispatchRead(int readLength, long offset) throws IOException
        public BlobProperties getProperties()
    }
    @ServiceClient(builder  =  BlobLeaseClientBuilder, isAsync  =  true)
    public final class BlobLeaseAsyncClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public Mono<String> acquireLease(int durationInSeconds)
        public Mono<Response<String>> acquireLeaseWithResponse(BlobAcquireLeaseOptions options)
        public Mono<Response<String>> acquireLeaseWithResponse(int durationInSeconds, RequestConditions modifiedRequestConditions)
        public Mono<Integer> breakLease()
        public Mono<Response<Integer>> breakLeaseWithResponse(BlobBreakLeaseOptions options)
        public Mono<Response<Integer>> breakLeaseWithResponse(Integer breakPeriodInSeconds, RequestConditions modifiedRequestConditions)
        public Mono<String> changeLease(String proposedId)
        public Mono<Response<String>> changeLeaseWithResponse(BlobChangeLeaseOptions options)
        public Mono<Response<String>> changeLeaseWithResponse(String proposedId, RequestConditions modifiedRequestConditions)
        public Mono<Void> releaseLease()
        public Mono<Response<Void>> releaseLeaseWithResponse(RequestConditions modifiedRequestConditions)
        public Mono<Response<Void>> releaseLeaseWithResponse(BlobReleaseLeaseOptions options)
        public Mono<String> renewLease()
        public Mono<Response<String>> renewLeaseWithResponse(RequestConditions modifiedRequestConditions)
        public Mono<Response<String>> renewLeaseWithResponse(BlobRenewLeaseOptions options)
        // Non-Service Methods:
        public String getAccountName()
        public String getLeaseId()
        public String getResourceUrl()
    }
    @ServiceClient(builder  =  BlobLeaseClientBuilder)
    public final class BlobLeaseClient {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public String acquireLease(int durationInSeconds)
        public Response<String> acquireLeaseWithResponse(BlobAcquireLeaseOptions options, Duration timeout, Context context)
        public Response<String> acquireLeaseWithResponse(int durationInSeconds, RequestConditions modifiedRequestConditions, Duration timeout, Context context)
        public Integer breakLease()
        public Response<Integer> breakLeaseWithResponse(BlobBreakLeaseOptions options, Duration timeout, Context context)
        public Response<Integer> breakLeaseWithResponse(Integer breakPeriodInSeconds, RequestConditions modifiedRequestConditions, Duration timeout, Context context)
        public String changeLease(String proposedId)
        public Response<String> changeLeaseWithResponse(BlobChangeLeaseOptions options, Duration timeout, Context context)
        public Response<String> changeLeaseWithResponse(String proposedId, RequestConditions modifiedRequestConditions, Duration timeout, Context context)
        public void releaseLease()
        public Response<Void> releaseLeaseWithResponse(RequestConditions modifiedRequestConditions, Duration timeout, Context context)
        public Response<Void> releaseLeaseWithResponse(BlobReleaseLeaseOptions options, Duration timeout, Context context)
        public String renewLease()
        public Response<String> renewLeaseWithResponse(RequestConditions modifiedRequestConditions, Duration timeout, Context context)
        public Response<String> renewLeaseWithResponse(BlobRenewLeaseOptions options, Duration timeout, Context context)
        // Non-Service Methods:
        public String getAccountName()
        public String getLeaseId()
        public String getResourceUrl()
    }
    @ServiceClientBuilder(serviceClients  =  { BlobLeaseClient, BlobLeaseAsyncClient })
    public final class BlobLeaseClientBuilder {
        public BlobLeaseClientBuilder()
        public BlobLeaseClientBuilder blobAsyncClient(BlobAsyncClientBase blobAsyncClient)
        public BlobLeaseClientBuilder blobClient(BlobClientBase blobClient)
        public BlobLeaseClientBuilder containerAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient)
        public BlobLeaseClientBuilder containerClient(BlobContainerClient blobContainerClient)
        public BlobLeaseClientBuilder leaseId(String leaseId)
        public BlobLeaseAsyncClient buildAsyncClient()
        public BlobLeaseClient buildClient()
    }
    public abstract class BlobOutputStream extends StorageOutputStream {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        public static BlobOutputStream blockBlobOutputStream(BlobAsyncClient client, BlockBlobOutputStreamOptions options, Context context)
        public static BlobOutputStream blockBlobOutputStream(BlobAsyncClient client, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions)
        public static BlobOutputStream blockBlobOutputStream(BlobAsyncClient client, ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions, Context context)
        @Override public synchronized void close() throws IOException
    }
    @ServiceClient(builder  =  SpecializedBlobClientBuilder, isAsync  =  true)
    public final class BlockBlobAsyncClient extends BlobAsyncClientBase {
        @Deprecated public static final int MAX_UPLOAD_BLOB_BYTES = 256 *  Constants.MB;
        public static final long MAX_UPLOAD_BLOB_BYTES_LONG = BlobConstants.MAX_UPLOAD_BLOB_BYTES_LONG;
        @Deprecated public static final int MAX_STAGE_BLOCK_BYTES = 100 *  Constants.MB;
        public static final long MAX_STAGE_BLOCK_BYTES_LONG = BlobConstants.MAX_STAGE_BLOCK_BYTES_LONG;
        public static final int MAX_BLOCKS = BlobConstants.MAX_BLOCKS;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public Mono<BlockBlobItem> commitBlockList(List<String> base64BlockIds)
        public Mono<BlockBlobItem> commitBlockList(List<String> base64BlockIds, boolean overwrite)
        public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(BlockBlobCommitBlockListOptions options)
        public Mono<Response<BlockBlobItem>> commitBlockListWithResponse(List<String> base64BlockIds, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions)
        public Mono<BlockList> listBlocks(BlockListType listType)
        public Mono<Response<BlockList>> listBlocksWithResponse(BlockBlobListBlocksOptions options)
        public Mono<Response<BlockList>> listBlocksWithResponse(BlockListType listType, String leaseId)
        public Mono<Void> stageBlock(String base64BlockId, BinaryData data)
        public Mono<Void> stageBlock(String base64BlockId, Flux<ByteBuffer> data, long length)
        public Mono<Void> stageBlockFromUrl(String base64BlockId, String sourceUrl, BlobRange sourceRange)
        public Mono<Response<Void>> stageBlockFromUrlWithResponse(BlockBlobStageBlockFromUrlOptions options)
        public Mono<Response<Void>> stageBlockFromUrlWithResponse(String base64BlockId, String sourceUrl, BlobRange sourceRange, byte[] sourceContentMd5, String leaseId, BlobRequestConditions sourceRequestConditions)
        public Mono<Response<Void>> stageBlockWithResponse(BlockBlobStageBlockOptions options)
        public Mono<Response<Void>> stageBlockWithResponse(String base64BlockId, Flux<ByteBuffer> data, long length, byte[] contentMd5, String leaseId)
        public Mono<BlockBlobItem> upload(BinaryData data)
        public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, long length)
        public Mono<BlockBlobItem> upload(BinaryData data, boolean overwrite)
        public Mono<BlockBlobItem> upload(Flux<ByteBuffer> data, long length, boolean overwrite)
        public Mono<BlockBlobItem> uploadFromUrl(String sourceUrl)
        public Mono<BlockBlobItem> uploadFromUrl(String sourceUrl, boolean overwrite)
        public Mono<Response<BlockBlobItem>> uploadFromUrlWithResponse(BlobUploadFromUrlOptions options)
        public Mono<Response<BlockBlobItem>> uploadWithResponse(BlockBlobSimpleUploadOptions options)
        public Mono<Response<BlockBlobItem>> uploadWithResponse(Flux<ByteBuffer> data, long length, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, byte[] contentMd5, BlobRequestConditions requestConditions)
        // Non-Service Methods:
        @Override public BlockBlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey)
        @Override public BlockBlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope)
    }
    @ServiceClient(builder  =  SpecializedBlobClientBuilder)
    public final class BlockBlobClient extends BlobClientBase {
        @Deprecated public static final int MAX_UPLOAD_BLOB_BYTES = 256 *  Constants.MB;
        public static final long MAX_UPLOAD_BLOB_BYTES_LONG = BlobConstants.MAX_UPLOAD_BLOB_BYTES_LONG;
        @Deprecated public static final int MAX_STAGE_BLOCK_BYTES = 100 *  Constants.MB;
        public static final long MAX_STAGE_BLOCK_BYTES_LONG = BlobConstants.MAX_STAGE_BLOCK_BYTES_LONG;
        public static final int MAX_BLOCKS = BlobConstants.MAX_BLOCKS;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public BlockBlobItem commitBlockList(List<String> base64BlockIds)
        public BlockBlobItem commitBlockList(List<String> base64BlockIds, boolean overwrite)
        public Response<BlockBlobItem> commitBlockListWithResponse(BlockBlobCommitBlockListOptions options, Duration timeout, Context context)
        public Response<BlockBlobItem> commitBlockListWithResponse(List<String> base64BlockIds, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public BlockList listBlocks(BlockListType listType)
        public Response<BlockList> listBlocksWithResponse(BlockBlobListBlocksOptions options, Duration timeout, Context context)
        public Response<BlockList> listBlocksWithResponse(BlockListType listType, String leaseId, Duration timeout, Context context)
        public void stageBlock(String base64BlockId, BinaryData data)
        public void stageBlock(String base64BlockId, InputStream data, long length)
        public void stageBlockFromUrl(String base64BlockId, String sourceUrl, BlobRange sourceRange)
        public Response<Void> stageBlockFromUrlWithResponse(BlockBlobStageBlockFromUrlOptions options, Duration timeout, Context context)
        public Response<Void> stageBlockFromUrlWithResponse(String base64BlockId, String sourceUrl, BlobRange sourceRange, byte[] sourceContentMd5, String leaseId, BlobRequestConditions sourceRequestConditions, Duration timeout, Context context)
        public Response<Void> stageBlockWithResponse(BlockBlobStageBlockOptions options, Duration timeout, Context context)
        public Response<Void> stageBlockWithResponse(String base64BlockId, InputStream data, long length, byte[] contentMd5, String leaseId, Duration timeout, Context context)
        public BlockBlobItem upload(BinaryData data)
        public BlockBlobItem upload(InputStream data, long length)
        public BlockBlobItem upload(BinaryData data, boolean overwrite)
        public BlockBlobItem upload(InputStream data, long length, boolean overwrite)
        public BlockBlobItem uploadFromUrl(String sourceUrl)
        public BlockBlobItem uploadFromUrl(String sourceUrl, boolean overwrite)
        public Response<BlockBlobItem> uploadFromUrlWithResponse(BlobUploadFromUrlOptions options, Duration timeout, Context context)
        public Response<BlockBlobItem> uploadWithResponse(BlockBlobSimpleUploadOptions options, Duration timeout, Context context)
        public Response<BlockBlobItem> uploadWithResponse(InputStream data, long length, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, byte[] contentMd5, BlobRequestConditions requestConditions, Duration timeout, Context context)
        // Non-Service Methods:
        public BlobOutputStream getBlobOutputStream()
        public BlobOutputStream getBlobOutputStream(boolean overwrite)
        public BlobOutputStream getBlobOutputStream(BlobRequestConditions requestConditions)
        public BlobOutputStream getBlobOutputStream(BlockBlobOutputStreamOptions options)
        public BlobOutputStream getBlobOutputStream(BlockBlobOutputStreamOptions options, Context context)
        public BlobOutputStream getBlobOutputStream(ParallelTransferOptions parallelTransferOptions, BlobHttpHeaders headers, Map<String, String> metadata, AccessTier tier, BlobRequestConditions requestConditions)
        @Override public BlockBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey)
        @Override public BlockBlobClient getEncryptionScopeClient(String encryptionScope)
        public SeekableByteChannel openSeekableByteChannelWrite(BlockBlobSeekableByteChannelWriteOptions options)
    }
    @ServiceClient(builder  =  SpecializedBlobClientBuilder, isAsync  =  true)
    public final class PageBlobAsyncClient extends BlobAsyncClientBase {
        public static final int PAGE_BYTES = BlobConstants.PAGE_BYTES;
        public static final int MAX_PUT_PAGES_BYTES = BlobConstants.MAX_PUT_PAGES_BYTES;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public Mono<PageBlobItem> clearPages(PageRange pageRange)
        public Mono<Response<PageBlobItem>> clearPagesWithResponse(PageRange pageRange, PageBlobRequestConditions pageBlobRequestConditions)
        public Mono<CopyStatusType> copyIncremental(String source, String snapshot)
        public Mono<Response<CopyStatusType>> copyIncrementalWithResponse(PageBlobCopyIncrementalOptions options)
        public Mono<Response<CopyStatusType>> copyIncrementalWithResponse(String source, String snapshot, RequestConditions modifiedRequestConditions)
        public Mono<PageBlobItem> create(long size)
        public Mono<PageBlobItem> create(long size, boolean overwrite)
        public Mono<PageBlobItem> createIfNotExists(long size)
        public Mono<Response<PageBlobItem>> createIfNotExistsWithResponse(PageBlobCreateOptions options)
        public Mono<Response<PageBlobItem>> createWithResponse(PageBlobCreateOptions options)
        public Mono<Response<PageBlobItem>> createWithResponse(long size, Long sequenceNumber, BlobHttpHeaders headers, Map<String, String> metadata, BlobRequestConditions requestConditions)
        public PagedFlux<PageRangeItem> listPageRanges(BlobRange blobRange)
        public PagedFlux<PageRangeItem> listPageRanges(ListPageRangesOptions options)
        public PagedFlux<PageRangeItem> listPageRangesDiff(ListPageRangesDiffOptions options)
        public PagedFlux<PageRangeItem> listPageRangesDiff(BlobRange blobRange, String prevSnapshot)
        public Mono<PageList> getManagedDiskPageRangesDiff(BlobRange blobRange, String prevSnapshotUrl)
        public Mono<Response<PageList>> getManagedDiskPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshotUrl, BlobRequestConditions requestConditions)
        @Deprecated public Mono<PageList> getPageRanges(BlobRange blobRange)
        @Deprecated public Mono<PageList> getPageRangesDiff(BlobRange blobRange, String prevSnapshot)
        @Deprecated public Mono<Response<PageList>> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot, BlobRequestConditions requestConditions)
        @Deprecated public Mono<Response<PageList>> getPageRangesWithResponse(BlobRange blobRange, BlobRequestConditions requestConditions)
        public Mono<PageBlobItem> resize(long size)
        public Mono<Response<PageBlobItem>> resizeWithResponse(long size, BlobRequestConditions requestConditions)
        public Mono<PageBlobItem> updateSequenceNumber(SequenceNumberActionType action, Long sequenceNumber)
        public Mono<Response<PageBlobItem>> updateSequenceNumberWithResponse(SequenceNumberActionType action, Long sequenceNumber, BlobRequestConditions requestConditions)
        public Mono<PageBlobItem> uploadPages(PageRange pageRange, Flux<ByteBuffer> body)
        public Mono<PageBlobItem> uploadPagesFromUrl(PageRange range, String sourceUrl, Long sourceOffset)
        public Mono<Response<PageBlobItem>> uploadPagesFromUrlWithResponse(PageBlobUploadPagesFromUrlOptions options)
        public Mono<Response<PageBlobItem>> uploadPagesFromUrlWithResponse(PageRange range, String sourceUrl, Long sourceOffset, byte[] sourceContentMd5, PageBlobRequestConditions destRequestConditions, BlobRequestConditions sourceRequestConditions)
        public Mono<Response<PageBlobItem>> uploadPagesWithResponse(PageRange pageRange, Flux<ByteBuffer> body, PageBlobUploadPagesOptions options)
        @Deprecated public Mono<Response<PageBlobItem>> uploadPagesWithResponse(PageRange pageRange, Flux<ByteBuffer> body, byte[] contentMd5, PageBlobRequestConditions pageBlobRequestConditions)
        // Non-Service Methods:
        @Override public PageBlobAsyncClient getCustomerProvidedKeyAsyncClient(CustomerProvidedKey customerProvidedKey)
        @Override public PageBlobAsyncClient getEncryptionScopeAsyncClient(String encryptionScope)
    }
    @ServiceClient(builder  =  SpecializedBlobClientBuilder)
    public final class PageBlobClient extends BlobClientBase {
        public static final int PAGE_BYTES = BlobConstants.PAGE_BYTES;
        public static final int MAX_PUT_PAGES_BYTES = BlobConstants.MAX_PUT_PAGES_BYTES;
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
        // Service Methods:
        public PageBlobItem clearPages(PageRange pageRange)
        public Response<PageBlobItem> clearPagesWithResponse(PageRange pageRange, PageBlobRequestConditions pageBlobRequestConditions, Duration timeout, Context context)
        public CopyStatusType copyIncremental(String source, String snapshot)
        public Response<CopyStatusType> copyIncrementalWithResponse(PageBlobCopyIncrementalOptions options, Duration timeout, Context context)
        public Response<CopyStatusType> copyIncrementalWithResponse(String source, String snapshot, RequestConditions modifiedRequestConditions, Duration timeout, Context context)
        public PageBlobItem create(long size)
        public PageBlobItem create(long size, boolean overwrite)
        public PageBlobItem createIfNotExists(long size)
        public Response<PageBlobItem> createIfNotExistsWithResponse(PageBlobCreateOptions options, Duration timeout, Context context)
        public Response<PageBlobItem> createWithResponse(PageBlobCreateOptions options, Duration timeout, Context context)
        public Response<PageBlobItem> createWithResponse(long size, Long sequenceNumber, BlobHttpHeaders headers, Map<String, String> metadata, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public PagedIterable<PageRangeItem> listPageRanges(BlobRange blobRange)
        public PagedIterable<PageRangeItem> listPageRanges(ListPageRangesOptions options, Duration timeout, Context context)
        public PagedIterable<PageRangeItem> listPageRangesDiff(BlobRange blobRange, String prevSnapshot)
        public PagedIterable<PageRangeItem> listPageRangesDiff(ListPageRangesDiffOptions options, Duration timeout, Context context)
        public PageList getManagedDiskPageRangesDiff(BlobRange blobRange, String prevSnapshotUrl)
        public Response<PageList> getManagedDiskPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshotUrl, BlobRequestConditions requestConditions, Duration timeout, Context context)
        @Deprecated public PageList getPageRanges(BlobRange blobRange)
        @Deprecated public PageList getPageRangesDiff(BlobRange blobRange, String prevSnapshot)
        @Deprecated public Response<PageList> getPageRangesDiffWithResponse(BlobRange blobRange, String prevSnapshot, BlobRequestConditions requestConditions, Duration timeout, Context context)
        @Deprecated public Response<PageList> getPageRangesWithResponse(BlobRange blobRange, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public PageBlobItem resize(long size)
        public Response<PageBlobItem> resizeWithResponse(long size, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public PageBlobItem updateSequenceNumber(SequenceNumberActionType action, Long sequenceNumber)
        public Response<PageBlobItem> updateSequenceNumberWithResponse(SequenceNumberActionType action, Long sequenceNumber, BlobRequestConditions requestConditions, Duration timeout, Context context)
        public PageBlobItem uploadPages(PageRange pageRange, InputStream body)
        public PageBlobItem uploadPagesFromUrl(PageRange range, String sourceUrl, Long sourceOffset)
        public Response<PageBlobItem> uploadPagesFromUrlWithResponse(PageBlobUploadPagesFromUrlOptions options, Duration timeout, Context context)
        public Response<PageBlobItem> uploadPagesFromUrlWithResponse(PageRange range, String sourceUrl, Long sourceOffset, byte[] sourceContentMd5, PageBlobRequestConditions destRequestConditions, BlobRequestConditions sourceRequestConditions, Duration timeout, Context context)
        public Response<PageBlobItem> uploadPagesWithResponse(PageRange pageRange, InputStream body, PageBlobUploadPagesOptions options, Duration timeout, Context context)
        @Deprecated public Response<PageBlobItem> uploadPagesWithResponse(PageRange pageRange, InputStream body, byte[] contentMd5, PageBlobRequestConditions pageBlobRequestConditions, Duration timeout, Context context)
        // Non-Service Methods:
        public BlobOutputStream getBlobOutputStream(PageRange pageRange)
        public BlobOutputStream getBlobOutputStream(PageBlobOutputStreamOptions options)
        public BlobOutputStream getBlobOutputStream(PageRange pageRange, BlobRequestConditions requestConditions)
        @Override public PageBlobClient getCustomerProvidedKeyClient(CustomerProvidedKey customerProvidedKey)
        @Override public PageBlobClient getEncryptionScopeClient(String encryptionScope)
    }
    @ServiceClientBuilder(serviceClients  =  { AppendBlobClient, AppendBlobAsyncClient, BlockBlobClient, BlockBlobAsyncClient, PageBlobClient, PageBlobAsyncClient })
    public final class SpecializedBlobClientBuilder implements TokenCredentialTrait<SpecializedBlobClientBuilder> , ConnectionStringTrait<SpecializedBlobClientBuilder> , AzureNamedKeyCredentialTrait<SpecializedBlobClientBuilder> , AzureSasCredentialTrait<SpecializedBlobClientBuilder> , HttpTrait<SpecializedBlobClientBuilder> , ConfigurationTrait<SpecializedBlobClientBuilder> , EndpointTrait<SpecializedBlobClientBuilder> {
        public SpecializedBlobClientBuilder()
        @Override public SpecializedBlobClientBuilder addPolicy(HttpPipelinePolicy pipelinePolicy)
        public SpecializedBlobClientBuilder setAnonymousAccess()
        public SpecializedBlobClientBuilder audience(BlobAudience audience)
        public SpecializedBlobClientBuilder blobAsyncClient(BlobAsyncClientBase blobAsyncClient)
        public SpecializedBlobClientBuilder blobClient(BlobClientBase blobClient)
        public SpecializedBlobClientBuilder blobName(String blobName)
        @Override public SpecializedBlobClientBuilder clientOptions(ClientOptions clientOptions)
        @Override public SpecializedBlobClientBuilder configuration(Configuration configuration)
        @Override public SpecializedBlobClientBuilder connectionString(String connectionString)
        public SpecializedBlobClientBuilder containerAsyncClient(BlobContainerAsyncClient blobContainerAsyncClient, String blobName)
        public SpecializedBlobClientBuilder containerClient(BlobContainerClient blobContainerClient, String blobName)
        public SpecializedBlobClientBuilder containerName(String containerName)
        public SpecializedBlobClientBuilder credential(StorageSharedKeyCredential credential)
        @Override public SpecializedBlobClientBuilder credential(AzureNamedKeyCredential credential)
        @Override public SpecializedBlobClientBuilder credential(TokenCredential credential)
        @Override public SpecializedBlobClientBuilder credential(AzureSasCredential credential)
        public SpecializedBlobClientBuilder customerProvidedKey(CustomerProvidedKey customerProvidedKey)
        public static HttpLogOptions getDefaultHttpLogOptions()
        public SpecializedBlobClientBuilder encryptionScope(String encryptionScope)
        @Override public SpecializedBlobClientBuilder endpoint(String endpoint)
        @Override public SpecializedBlobClientBuilder httpClient(HttpClient httpClient)
        @Override public SpecializedBlobClientBuilder httpLogOptions(HttpLogOptions logOptions)
        @Override public SpecializedBlobClientBuilder pipeline(HttpPipeline httpPipeline)
        public SpecializedBlobClientBuilder retryOptions(RequestRetryOptions retryOptions)
        @Override public SpecializedBlobClientBuilder retryOptions(RetryOptions retryOptions)
        public SpecializedBlobClientBuilder sasToken(String sasToken)
        public SpecializedBlobClientBuilder serviceVersion(BlobServiceVersion version)
        public SpecializedBlobClientBuilder snapshot(String snapshot)
        public SpecializedBlobClientBuilder versionId(String versionId)
        public AppendBlobAsyncClient buildAppendBlobAsyncClient()
        public AppendBlobClient buildAppendBlobClient()
        public BlockBlobAsyncClient buildBlockBlobAsyncClient()
        public BlockBlobClient buildBlockBlobClient()
        public PageBlobAsyncClient buildPageBlobAsyncClient()
        public PageBlobClient buildPageBlobClient()
    }
}
```