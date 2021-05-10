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
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.common.test.shared.TestAccount
import org.spockframework.runtime.model.IterationInfo
import spock.lang.Requires
import spock.lang.Shared

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.function.Supplier

class APISpec extends StorageSpec {
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

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient
    BlobServiceClient versionedBlobServiceClient

    private boolean recordLiveMode

    def setup() {
        // If the test doesn't have the Requires tag record it in live mode.
        recordLiveMode = specificationContext.getCurrentFeature().getFeatureMethod().getAnnotation(Requires.class) == null

        primaryBlobServiceClient = setClient(env.primaryAccount)
        primaryBlobServiceAsyncClient = getServiceAsyncClient(env.primaryAccount)
        versionedBlobServiceClient = setClient(env.versionedAccount)
    }

    BlobServiceClient setClient(TestAccount account) {
        try {
            return getServiceClient(account)
        } catch (Exception ignore) {
            return null
        }
    }

    def getOAuthServiceClient() {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(env.primaryAccount.blobEndpoint)
            .httpClient(getHttpClient())



        if (env.testMode != TestMode.PLAYBACK) {
            builder.addPolicy(getRecordPolicy())

            // AZURE_TENANT_ID, AZURE_CLIENT_ID, AZURE_CLIENT_SECRET
            return builder.credential(new EnvironmentCredentialBuilder().build()).buildClient()
        } else {
            // Running in playback, we don't have access to the AAD environment variables, just use SharedKeyCredential.
            return builder.credential(env.primaryAccount.credential).buildClient()
        }
    }

    BlobServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.credential, account.blobEndpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
        HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    BlobServiceClient getServiceClient(String sasToken, String endpoint) {
        return getServiceClientBuilder(null, endpoint, null).sasToken(sasToken).buildClient()
    }

    BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.credential, account.blobEndpoint)
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

        builder.addPolicy(getRecordPolicy())

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

        builder.addPolicy(getRecordPolicy())

        return builder
    }

    def generateContainerName() {
        generateResourceName(entityNo++)
    }

    def generateBlobName() {
        generateResourceName(entityNo++)
    }

    private String generateResourceName(int entityNo) {
        return namer.getRandomName(namer.getResourcePrefix() + entityNo, 63)
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
