// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.BlobContainerItem
import com.azure.storage.blob.models.LeaseStateType
import com.azure.storage.blob.models.ListBlobContainersOptions
import com.azure.storage.blob.specialized.BlobLeaseClient
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.MINUTES)
class APISpec extends Specification {

    @Shared
    ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    // Prefixes for blobs and containers
    String containerPrefix = "jtc" // java test container

    String blobPrefix = "javablob"

    public static final String defaultEndpointTemplate = "https://%s.blob.core.windows.net/"

    static def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    static def PRIMARY_STORAGE = "PRIMARY_STORAGE_"

    protected static StorageSharedKeyCredential primaryCredential
    static TestMode testMode

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient

    InterceptorManager interceptorManager
    boolean recordLiveMode
    protected TestResourceNamer resourceNamer
    protected String testName
    String containerName

    def setupSpec() {
        testMode = setupTestMode()
        primaryCredential = getCredential(PRIMARY_STORAGE)
        // The property is to limit flapMap buffer size of concurrency
        // in case the upload or download open too many connections.
        System.setProperty("reactor.bufferSize.x", "16")
        System.setProperty("reactor.bufferSize.small", "100")
        System.out.println(String.format("--------%s---------", testMode))
    }

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        int iterationIndex = fullTestName.lastIndexOf("[")
        int substringIndex = (int) Math.min((iterationIndex != -1) ? iterationIndex : fullTestName.length(), 50)
        this.testName = fullTestName.substring(0, substringIndex)
        this.interceptorManager = new InterceptorManager(className + fullTestName, testMode)
        this.resourceNamer = new TestResourceNamer(className + testName, testMode, interceptorManager.getRecordedData())

        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)

        // If the test doesn't have the Requires tag record it in live mode.
        recordLiveMode = specificationContext.getCurrentIteration().getDescription().getAnnotation(Requires.class) != null

        primaryBlobServiceClient = setClient(primaryCredential)
        primaryBlobServiceAsyncClient = getServiceAsyncClient(primaryCredential)

        containerName = generateContainerName()
    }

    def cleanup() {
        def options = new ListBlobContainersOptions().setPrefix(containerPrefix + testName)
        for (BlobContainerItem container : primaryBlobServiceClient.listBlobContainers(options, Duration.ofSeconds(120))) {
            BlobContainerClient containerClient = primaryBlobServiceClient.getBlobContainerClient(container.getName())

            if (container.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                createLeaseClient(containerClient).breakLeaseWithResponse(0, null, null, null)
            }

            containerClient.delete()
        }

        interceptorManager.close()
    }

    static TestMode setupTestMode() {
        String testMode = Configuration.getGlobalConfiguration().get(AZURE_TEST_MODE)

        if (testMode != null) {
            try {
                return TestMode.valueOf(testMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException ignore) {
                return TestMode.PLAYBACK
            }
        }

        return TestMode.PLAYBACK
    }

    static boolean playbackMode() {
        return setupTestMode() == TestMode.PLAYBACK
    }

    private StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode == TestMode.RECORD || testMode == TestMode.LIVE) {
            accountName = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_NAME")
            accountKey = Configuration.getGlobalConfiguration().get(accountType + "ACCOUNT_KEY")
        } else {
            accountName = "azstoragesdkaccount"
            accountKey = "astorageaccountkey"
        }

        if (accountName == null || accountKey == null) {
            logger.warning("Account name or key for the {} account was null. Test's requiring these credentials will fail.", accountType)
            return null
        }

        return new StorageSharedKeyCredential(accountName, accountKey)
    }

    HttpClient getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
        if (testMode == TestMode.RECORD || testMode == TestMode.LIVE) {
            builder.wiretap(true)

            if (Boolean.parseBoolean(Configuration.getGlobalConfiguration().get("AZURE_TEST_DEBUGGING"))) {
                builder.proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
            }

            return builder.build()
        } else {
            return interceptorManager.getPlaybackClient()
        }
    }

    def generateContainerName() {
        generateResourceName(containerPrefix, entityNo++)
    }

    def generateBlobName() {
        generateResourceName(blobPrefix, entityNo++)
    }

    private String generateResourceName(String prefix, int entityNo) {
        return resourceNamer.randomName(prefix + testName + entityNo, 63)
    }

    BlobServiceClient setClient(StorageSharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ignore) {
            return null
        }
    }

    BlobServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential) {
        return getServiceClient(credential, String.format(defaultEndpointTemplate, credential.getAccountName()), null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
                                       HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    BlobServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, null).sasToken(sasToken).buildClient()
    }

    BlobServiceAsyncClient getServiceAsyncClient(StorageSharedKeyCredential credential) {
        return getServiceClientBuilder(credential, String.format(defaultEndpointTemplate, credential.getAccountName()))
            .buildAsyncClient()
    }

    BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        if (credential != null) {
            builder.credential(credential)
        }

        return builder
    }

    static BlobLeaseClient createLeaseClient(BlobContainerClient containerClient) {
        return createLeaseClient(containerClient, null)
    }

    static BlobLeaseClient createLeaseClient(BlobContainerClient containerClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .containerClient(containerClient)
            .leaseId(leaseId)
            .buildClient()
    }

    byte[] getRandomByteArray(int size) {
        long seed = UUID.fromString(resourceNamer.randomUuid()).getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }

    /*
     Size must be an int because ByteBuffer sizes can only be an int. Long is not supported.
     */
    ByteBuffer getRandomData(int size) {
        return ByteBuffer.wrap(getRandomByteArray(size))
    }

}
