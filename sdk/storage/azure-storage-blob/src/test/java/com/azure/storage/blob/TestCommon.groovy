package com.azure.storage.blob

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.identity.credential.EnvironmentCredential
import com.azure.storage.common.credentials.SASTokenCredential
import com.azure.storage.common.credentials.SharedKeyCredential

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

    TestCommon(String testName, boolean includeIteration, int iterationNo) {
        this.testName = testName
        this.testMode = ConfigurationManager.getConfiguration().get("AZURE_TEST_MODE", TestMode.PLAYBACK)

        if (includeIteration) {
            this.interceptorManager = new InterceptorManager(String.format("%s[%d]", testName, iterationNo), testMode)
        } else {
            this.interceptorManager = new InterceptorManager(testName, testMode)
        }

        this.resourceNamer = new TestResourceNamer(testName, testMode, interceptorManager.getRecordedData())
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
            .addPolicy(interceptorManager.getRecordPolicy())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        if (credential != null) {
            builder.credential(credential)
        }

        return builder.buildClient()
    }

    BlobServiceClient getServiceClient(SASTokenCredential credential, String endpoint) {
        return new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(interceptorManager.getRecordPolicy())
            .credential(credential)
            .buildClient()
    }

    BlobServiceClient getOAuthServiceClient(String accountName) {
        return new BlobServiceClientBuilder()
            .endpoint(String.format("https://%s.blob.core.windows.net/", accountName))
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(interceptorManager.getRecordPolicy())
            .credential(new EnvironmentCredential()) // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            .buildClient()
    }

    ContainerClient getContainerClient(SASTokenCredential credential, String endpoint) {
        return new ContainerClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(interceptorManager.getRecordPolicy())
            .credential(credential)
            .buildClient()
    }

    BlobAsyncClient getBlobAsyncClient(SharedKeyCredential credential, String endpoint, String blobName) {
        return new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(interceptorManager.getRecordPolicy())
            .credential(credential)
            .buildBlobAsyncClient()
    }

    BlobClient getBlobClient(SASTokenCredential credential, String endpoint, String blobName) {
        return getBlobClient(credential, endpoint, blobName, null)
    }

    BlobClient getBlobClient(SASTokenCredential credential, String endpoint, String blobName, String snapshotId) {
        return new BlobClientBuilder()
            .endpoint(endpoint)
            .blobName(blobName)
            .snapshot(snapshotId)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(interceptorManager.getRecordPolicy())
            .credential(credential)
            .buildBlobClient()
    }

    BlobClient getBlobClient(SharedKeyCredential credential, String endpoint, HttpPipelinePolicy... policies) {
        BlobClientBuilder builder = new BlobClientBuilder()
            .endpoint(endpoint)
            .httpClient(getHttpClient())
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .addPolicy(interceptorManager.getRecordPolicy())

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        return builder.credential(credential).buildBlobClient()
    }

    private HttpClient getHttpClient() {
        HttpClient client = HttpClient.createDefault().wiretap(true)

        if (Boolean.parseBoolean(ConfigurationManager.getConfiguration().get("AZURE_TEST_DEBUGGING"))) {
            return client.proxy(PROXY_OPTIONS)
        } else {
            return client
        }
    }

    String generateResourceName(String prefix, int entityNo) {
        return resourceNamer.randomName(prefix + testName + System.currentTimeMillis() + entityNo, 63)
    }
}
