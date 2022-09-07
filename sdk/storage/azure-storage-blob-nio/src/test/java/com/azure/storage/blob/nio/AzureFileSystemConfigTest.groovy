package com.azure.storage.blob.nio

import com.azure.core.credential.AzureSasCredential
import com.azure.core.http.HttpClient
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.utils.TestConfigurationSource
import com.azure.core.util.ConfigurationBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.policy.RetryPolicyType
import com.azure.storage.common.test.shared.policy.NoOpHttpPipelinePolicy
import reactor.core.publisher.Mono
import spock.lang.Specification

class AzureFileSystemConfigTest extends Specification {
    byte[] getBytes(int length) {
        def r = new byte[length]
        new Random().nextBytes(r)
        r
    }

    def accountName = "myaccount"
    def accountKey = Base64.encoder.encodeToString(getBytes(64))
    def httpClient = new HttpClient() {
        @Override
        Mono<HttpResponse> send(HttpRequest httpRequest) {
            return null
        }
    }

    def "AzureFileSystemConfig from Map"() {
        setup:
        def credential = new StorageSharedKeyCredential(accountName, accountKey)
        def sasToken = new AzureSasCredential("?sig=12345678")
        def logLevel = HttpLogDetailLevel.BASIC
        def retryType = RetryPolicyType.EXPONENTIAL
        def maxRetries = 1 as Integer
        def timeout = 2 as Integer
        def retryDelay = 3 as Long
        def maxRetryDelay = 4 as Long
        def secondaryEndpoint = "https://foo-secondary.blob.core.windows.net"
        def httpClient = httpClient
        def policies = [NoOpHttpPipelinePolicy.INSTANCE]
        def blockSize = 5 as Long
        def putBlob = 6 as Long
        def concurrency = 7 as Integer
        def resume = 8 as Integer
        def fileStores = "container1,container2,container3"
        def skipCheck = true
        def configuration = [
            (AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL)       : credential,
            (AzureFileSystem.AZURE_STORAGE_SAS_TOKEN_CREDENTIAL)        : sasToken,
            (AzureFileSystem.AZURE_STORAGE_HTTP_LOG_DETAIL_LEVEL)       : logLevel,
            (AzureFileSystem.AZURE_STORAGE_RETRY_POLICY_TYPE)           : retryType,
            (AzureFileSystem.AZURE_STORAGE_MAX_TRIES)                   : maxRetries,
            (AzureFileSystem.AZURE_STORAGE_TRY_TIMEOUT)                 : timeout,
            (AzureFileSystem.AZURE_STORAGE_RETRY_DELAY_IN_MS)           : retryDelay,
            (AzureFileSystem.AZURE_STORAGE_MAX_RETRY_DELAY_IN_MS)       : maxRetryDelay,
            (AzureFileSystem.AZURE_STORAGE_SECONDARY_HOST)              : secondaryEndpoint,
            (AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT)                 : httpClient,
            (AzureFileSystem.AZURE_STORAGE_HTTP_POLICIES)               : policies as HttpPipelinePolicy[],
            (AzureFileSystem.AZURE_STORAGE_UPLOAD_BLOCK_SIZE)           : blockSize,
            (AzureFileSystem.AZURE_STORAGE_PUT_BLOB_THRESHOLD)          : putBlob,
            (AzureFileSystem.AZURE_STORAGE_MAX_CONCURRENCY_PER_REQUEST) : concurrency,
            (AzureFileSystem.AZURE_STORAGE_DOWNLOAD_RESUME_RETRIES)     : resume,
            (AzureFileSystem.AZURE_STORAGE_FILE_STORES)                 : fileStores,
            (AzureFileSystem.AZURE_STORAGE_SKIP_INITIAL_CONTAINER_CHECK): skipCheck
        ] as Map<String, Object>

        when:
        def c = new AzureFileSystemConfig(configuration)

        then:
        c.sharedKeyCredential == credential
        c.sasCredential == sasToken
        c.logOptions.logLevel == logLevel
        c.retryOptions.getMaxTries() == maxRetries
        c.retryOptions.getTryTimeout() == timeout
        c.retryOptions.getRetryDelayInMs() == retryDelay
        c.retryOptions.getMaxRetryDelayInMs() == maxRetryDelay
        c.retryOptions.getSecondaryHost() == secondaryEndpoint
        c.httpClient == httpClient
        c.policyList == policies as List<HttpPipelinePolicy>
        c.blockSize == blockSize
        c.putBlobThreshold == putBlob
        c.maxConcurrencyPerRequest == concurrency
        c.downloadResumeRetries == resume
        c.fileStoreNames == fileStores.split(",") as List<String>
        c.skipInitialContainerCheck == skipCheck
    }

    def "AzureFileSystemConfig from Configuration"() {
        setup:
        def endpoint = "https://foo.blob.core.windows.net"
        def sasToken = "?sig=12345678"
        def retryType = "exponential"
        def maxRetries = 1
        def timeout = 2
        def retryDelay = 3
        def maxRetryDelay = 4
        def secondaryEndpoint = "https://foo-secondary.blob.core.windows.net"
        def blockSize = 5
        def putBlob = 6
        def concurrency = 7
        def resume = 8
        def fileStores = "container1,container2,container3"
        def skipCheck = true
        def configSource = new TestConfigurationSource()
        [
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_BLOB_ENDPOINT)          : endpoint,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_ACCOUNT_NAME)           : accountName,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_ACCOUNT_KEY)            : accountKey,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_SAS_TOKEN)              : sasToken,
            (Constants.ConfigurationConstants.RETRY_OPTIONS_RETRY_STRATEGY)                   : retryType,
            (Constants.ConfigurationConstants.RETRY_OPTIONS_MAX_RETRY)                        : maxRetries,
            (Constants.ConfigurationConstants.RETRY_OPTIONS_TIMEOUT_SECONDS)                  : timeout,
            (Constants.ConfigurationConstants.RETRY_OPTIONS_DELAY_MS)                         : retryDelay,
            (Constants.ConfigurationConstants.RETRY_OPTIONS_MAX_DELAY_MS)                     : maxRetryDelay,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_BLOB_ENDPOINT_SECONDARY): secondaryEndpoint,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_BLOCK_SIZE)             : blockSize,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_PUT_BLOB_THRESHOLD)     : putBlob,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_PER_REQUEST_CONCURRENCY): concurrency,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_RESUME_RETRIES)         : resume,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_FILE_STORES)            : fileStores,
            (Constants.ConfigurationConstants.Nio.ENVIRONMENT_DEFAULT_SKIP_CONTAINER_CHECK)   : skipCheck
        ].each {configSource.put(it.key, it.value as String)}

        when:
        def c = new AzureFileSystemConfig(new ConfigurationBuilder(
            new TestConfigurationSource(), new TestConfigurationSource(), configSource).build())

        then:
        c.sharedKeyCredential.accountName == accountName
        // can't access key from object, so compare strings the key would sign
        c.sharedKeyCredential.computeHmac256("foo") ==
            new StorageSharedKeyCredential(accountName, accountKey).computeHmac256("foo")
        c.sasCredential.signature == sasToken
        c.retryOptions.getMaxTries() == maxRetries
        c.retryOptions.getTryTimeout() == timeout
        c.retryOptions.getRetryDelayInMs() == retryDelay
        c.retryOptions.getMaxRetryDelayInMs() == maxRetryDelay
        c.retryOptions.getSecondaryHost() == secondaryEndpoint
        c.blockSize == blockSize
        c.putBlobThreshold == putBlob
        c.maxConcurrencyPerRequest == concurrency
        c.downloadResumeRetries == resume
        c.fileStoreNames == fileStores.split(",") as List<String>
        c.skipInitialContainerCheck == skipCheck
    }
}
