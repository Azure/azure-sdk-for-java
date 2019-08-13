package com.azure.storage.blob

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.identity.credential.EnvironmentCredentialBuilder
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.common.credentials.SharedKeyCredential

import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.function.Supplier

class TestCommon {
    private static final Supplier<ProxyOptions> PROXY_OPTIONS = new Supplier<ProxyOptions>() {
        @Override
        ProxyOptions get() {
            return new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
        }
    }

    private final InterceptorManager interceptorManager
    private final TestResourceNamer resourceNamer
    private final TestMode testMode
    private final String testName
    private final String className

    TestCommon(String testName, String className, boolean includeIteration, int iterationNo) {
        this.testName = testName.substring(0, (int) Math.min(testName.length(), 50))
        this.className = className
        this.testMode = ConfigurationManager.getConfiguration().get("AZURE_TEST_MODE", TestMode.PLAYBACK)

        if (includeIteration) {
            this.interceptorManager = new InterceptorManager(String.format("%s[%d]", className + testName, iterationNo), testMode)
        } else {
            this.interceptorManager = new InterceptorManager(className + testName, testMode)
        }

        this.resourceNamer = new TestResourceNamer(className + testName, testMode, interceptorManager.getRecordedData())
    }

    TestMode getTestMode() {
        return testMode
    }

    String getTestName() {
        return testName
    }

    void stopRecording() {
        interceptorManager.close()
    }

    BlobServiceClient setClient(SharedKeyCredential credential) {
        try {
            return getServiceClient(credential)
        } catch (Exception ex) {
            return null
        }
    }

    BlobServiceClient getServiceClient(String endpoint) {
        return getServiceClient(null, endpoint, null)
    }

    BlobServiceClient getServiceClient(SharedKeyCredential credential) {
        return getServiceClient(credential, String.format("https://%s.blob.core.windows.net", credential.accountName()), null)
    }

    BlobServiceClient getServiceClient(SharedKeyCredential credential, String endpoint) {
        return getServiceClient(credential, endpoint, null)
    }

    BlobServiceClient getServiceClient(SharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        if (credential != null) {
            builder.credential(credential)
        }

        return builder.buildClient()
    }

    BlobServiceClient getServiceClient(SASTokenCredential credential, String endpoint) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildClient()
    }

    BlobServiceClient getOAuthServiceClient(String accountName) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(String.format("https://%s.blob.core.windows.net/", accountName))
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
        return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient()
    }

    ContainerClient getContainerClient(SASTokenCredential credential, String endpoint) {
        ContainerClientBuilder builder = new ContainerClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        builder.credential(credential).buildClient()
    }

    BlobAsyncClient getBlobAsyncClient(SharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        builder.credential(credential).buildBlobAsyncClient()
    }

    BlobClient getBlobClient(SASTokenCredential credential, String endpoint, String blobName) {
        return getBlobClient(credential, endpoint, blobName, null)
    }

    BlobClient getBlobClient(SASTokenCredential credential, String endpoint, String blobName, String snapshotId) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildBlobClient()
    }

    BlobClient getBlobClient(SharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildBlobClient()
    }

    BlobClient getBlobClient(SharedKeyCredential credential, String endpoint, String blobName) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.credential(credential).buildBlobClient()
    }

    BlobClient getBlobClient(String endpoint, SASTokenCredential credential) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)

        if (credential != null) {
            builder.credential(credential)
        }

        if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
        }

        return builder.buildBlobClient()
    }

    private HttpClient getHttpClient() {
        HttpClient client
        if (testMode == TestMode.RECORD) {
            client = HttpClient.createDefault().wiretap(true)
        } else {
            client = interceptorManager.getPlaybackClient()
        }

        if (Boolean.parseBoolean(ConfigurationManager.getConfiguration().get("AZURE_TEST_DEBUGGING"))) {
            return client.proxy(PROXY_OPTIONS)
        } else {
            return client
        }
    }

    String generateResourceName(String prefix, int entityNo) {
        return resourceNamer.randomName(prefix + testName + entityNo, 63)
    }

    String getRandomUUID() {
        return resourceNamer.randomUuid()
    }

    String getBlockID() {
        return Base64.encoder.encodeToString(resourceNamer.randomUuid().getBytes(StandardCharsets.UTF_8))
    }

    OffsetDateTime getUTCNow() {
        return resourceNamer.now()
    }

    byte[] getRandomData(int size) {
        long seed = UUID.fromString(getRandomUUID()).getMostSignificantBits() & Long.MAX_VALUE
        Random rand = new Random(seed)
        byte[] data = new byte[size]
        rand.nextBytes(data)
        return data
    }
}
