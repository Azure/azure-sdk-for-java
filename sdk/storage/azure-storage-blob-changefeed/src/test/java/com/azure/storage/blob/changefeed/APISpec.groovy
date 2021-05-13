// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed

import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.TestMode
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
import com.azure.storage.common.test.shared.StorageSpec
import com.azure.storage.common.test.shared.TestAccount
import spock.lang.Timeout

import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.MINUTES)
class APISpec extends StorageSpec {

    Integer entityNo = 0 // Used to generate stable container names for recording tests requiring multiple containers.

    BlobServiceClient primaryBlobServiceClient
    BlobServiceAsyncClient primaryBlobServiceAsyncClient

    String containerName

    def setupSpec() {
        // The property is to limit flapMap buffer size of concurrency
        // in case the upload or download open too many connections.
        System.setProperty("reactor.bufferSize.x", "16")
        System.setProperty("reactor.bufferSize.small", "100")
    }

    def setup() {
        primaryBlobServiceClient = getServiceClient(env.primaryAccount)
        primaryBlobServiceAsyncClient = getServiceAsyncClient(env.primaryAccount)

        containerName = generateContainerName()
    }

    def cleanup() {
        if (env.testMode != TestMode.PLAYBACK) {
            def cleanupClient = new BlobServiceClientBuilder()
                .httpClient(getHttpClient())
                .credential(env.primaryAccount.credential)
                .endpoint(env.primaryAccount.blobEndpoint)
                .buildClient()
            def options = new ListBlobContainersOptions().setPrefix(namer.getResourcePrefix())
            for (BlobContainerItem container : cleanupClient.listBlobContainers(options, Duration.ofSeconds(120))) {
                BlobContainerClient containerClient = cleanupClient.getBlobContainerClient(container.getName())

                if (container.getProperties().getLeaseState() == LeaseStateType.LEASED) {
                    createLeaseClient(containerClient).breakLeaseWithResponse(0, null, null, null)
                }

                containerClient.delete()
            }
        }
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

    BlobServiceClient getServiceClient(TestAccount account) {
        return getServiceClient(account.credential, account.blobEndpoint, null)
    }

    BlobServiceClient getServiceClient(StorageSharedKeyCredential credential, String endpoint,
                                       HttpPipelinePolicy... policies) {
        return getServiceClientBuilder(credential, endpoint, policies).buildClient()
    }

    BlobServiceAsyncClient getServiceAsyncClient(TestAccount account) {
        return getServiceClientBuilder(account.credential, account.blobEndpoint)
            .buildAsyncClient()
    }

    BlobServiceClientBuilder getServiceClientBuilder(StorageSharedKeyCredential credential, String endpoint,
                                                     HttpPipelinePolicy... policies) {
        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .endpoint(endpoint)

        for (HttpPipelinePolicy policy : policies) {
            builder.addPolicy(policy)
        }

        instrument(builder)

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
        long seed = UUID.fromString(namer.getRandomUuid()).getMostSignificantBits() & Long.MAX_VALUE
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
