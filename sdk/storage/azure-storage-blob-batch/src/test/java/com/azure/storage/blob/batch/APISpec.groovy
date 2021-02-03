// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.Configuration
import com.azure.core.util.logging.ClientLogger
import com.azure.identity.EnvironmentCredentialBuilder
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobContainerClientBuilder
import com.azure.storage.blob.BlobServiceAsyncClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.blob.specialized.BlobLeaseClient
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends Specification {
    @Shared
    ClientLogger logger = new ClientLogger(APISpec.class)

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    // Fields used for conveniently creating blobs with data.
    static final String defaultText = "default"

    public static final ByteBuffer defaultData = ByteBuffer.wrap(defaultText.getBytes(StandardCharsets.UTF_8))

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>() {
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

    static int defaultDataSize = defaultData.remaining()

    static final String receivedLeaseID = "received"

    static final String garbageLeaseID = UUID.randomUUID().toString()

    // Prefixes for blobs and containers
    String containerPrefix = "jtc" // java test container

    String blobPrefix = "javablob"

    public static final String defaultEndpointTemplate = "https://%s.blob.core.windows.net/"

    static def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    static def PRIMARY_STORAGE = "PRIMARY_STORAGE_"
    static def VERSIONED_STORAGE = "VERSIONED_STORAGE_"

    protected static StorageSharedKeyCredential primaryCredential
    static StorageSharedKeyCredential versionedCredential
    static TestMode testMode

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient
    BlobServiceClient versionedBlobServiceClient

    private InterceptorManager interceptorManager
    private boolean recordLiveMode
    private TestResourceNamer resourceNamer
    protected String testName

    def setupSpec() {
        testMode = setupTestMode()
        primaryCredential = getCredential(PRIMARY_STORAGE)
        versionedCredential = getCredential(VERSIONED_STORAGE)
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
        recordLiveMode = specificationContext.getCurrentIteration().getDescription().getAnnotation(Requires.class) == null

        primaryBlobServiceClient = setClient(primaryCredential)
        primaryBlobServiceAsyncClient = getServiceAsyncClient(primaryCredential)
        versionedBlobServiceClient = setClient(versionedCredential)
    }

    def cleanup() {
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

    static boolean liveMode() {
        return setupTestMode() == TestMode.LIVE
    }

    private StorageSharedKeyCredential getCredential(String accountType) {
        String accountName
        String accountKey

        if (testMode != TestMode.PLAYBACK) {
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

    BlobServiceClient setClient(StorageSharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ignore) {
            return null
        }
    }

    def getOAuthServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
            .httpClient(getHttpClient())

        if (testMode != TestMode.PLAYBACK) {
            if (testMode == TestMode.RECORD) {
                builder.addPolicy(interceptorManager.getRecordPolicy())
            }

            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient()
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(primaryCredential).buildClient()
        }
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential) {
        return getServiceClient(credential, String.format(defaultEndpointTemplate, credential.getAccountName()),
            null)
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

    BlobContainerClient getContainerClient(String sasToken, String endpoint) {
        getContainerClientBuilder(endpoint).sasToken(sasToken).buildClient()
    }

    BlobContainerClientBuilder getContainerClientBuilder(String endpoint) {
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder
    }

    HttpClient getHttpClient() {
        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
        if (testMode != TestMode.PLAYBACK) {
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

    OffsetDateTime getUTCNow() {
        return resourceNamer.now()
    }

    /**
     * This helper method will acquire a lease on a blob to prepare for testing lease Id. We want to test
     * against a valid lease in both the success and failure cases to guarantee that the results actually indicate
     * proper setting of the header. If we pass null, though, we don't want to acquire a lease, as that will interfere
     * with other AC tests.
     *
     * @param bc
     *      The blob on which to acquire a lease.
     * @param leaseID
     *      The signalID. Values should only ever be {@code receivedLeaseID}, {@code garbageLeaseID}, or {@code null}.
     * @return
     * The actual lease Id of the blob if recievedLeaseID is passed, otherwise whatever was passed will be
     * returned.
     */
    def setupBlobLeaseCondition(BlobClientBase bc, String leaseID) {
        String responseLeaseId = null
        if (leaseID == receivedLeaseID || leaseID == garbageLeaseID) {
            responseLeaseId = createLeaseClient(bc).acquireLease(-1)
        }
        if (leaseID == receivedLeaseID) {
            return responseLeaseId
        } else {
            return leaseID
        }
    }

    static BlobLeaseClient createLeaseClient(BlobClientBase blobClient) {
        return createLeaseClient(blobClient, null)
    }

    static BlobLeaseClient createLeaseClient(BlobClientBase blobClient, String leaseId) {
        return new BlobLeaseClientBuilder()
            .blobClient(blobClient)
            .leaseId(leaseId)
            .buildClient()
    }
}
