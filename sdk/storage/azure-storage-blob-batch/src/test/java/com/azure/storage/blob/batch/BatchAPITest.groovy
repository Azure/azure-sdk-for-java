package com.azure.storage.blob.batch

import com.azure.core.http.HttpPipelineBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.util.Context

import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.StorageException

class BatchAPITest extends APISpec {
    static def setupCustomPolicyBatch(BlobServiceAsyncClient blobServiceAsyncClient, HttpPipelinePolicy customPolicy) {
        def clientPipeline = blobServiceAsyncClient.getHttpPipeline()

        def policies = new HttpPipelinePolicy[clientPipeline.getPolicyCount() + 1]
        for (def i = 0; i < clientPipeline.getPolicyCount(); i++) {
            policies[i] = clientPipeline.getPolicy(i)
        }

        policies[clientPipeline.getPolicyCount()] = customPolicy

        return new BlobBatch(blobServiceAsyncClient.getAccountUrl(), new HttpPipelineBuilder()
            .policies(policies)
            .httpClient(clientPipeline.getHttpClient())
            .build())
    }

    def "Empty batch"() {
        when:
        def batch = new BlobBatch(primaryBlobServiceClient)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        thrown(UnsupportedOperationException)
    }

    def "Mixed batch"() {
        when:
        def batch = new BlobBatch(primaryBlobServiceAsyncClient)
        batch.delete("container", "blob")
        batch.setTier("container", "blob2", AccessTier.HOT)

        then:
        thrown(UnsupportedOperationException)

        when:
        batch = new BlobBatch(primaryBlobServiceAsyncClient)
        batch.setTier("container", "blob", AccessTier.HOT)
        batch.delete("container", "blob2")

        then:
        thrown(UnsupportedOperationException)
    }

    def "Set tier all succeed"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(APISpec.defaultInputStream.get(), APISpec.defaultDataSize)
        containerClient.getBlobClient(blobName2).getBlockBlobClient().upload(APISpec.defaultInputStream.get(), APISpec.defaultDataSize)

        when:
        def response1 = batch.setTier(containerName, blobName1, AccessTier.HOT)
        def response2 = batch.setTier(containerName, blobName2, AccessTier.COOL)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        notThrown(StorageException)
        response1.getStatusCode() == 200
        response2.getStatusCode() == 200
    }

    def "Set tier some succeed throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(APISpec.defaultInputStream.get(), APISpec.defaultDataSize)

        when:
        def response1 = batch.setTier(containerName, blobName1, AccessTier.HOT)
        def response2 = batch.setTier(containerName, blobName2, AccessTier.COOL)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        thrown(StorageException)
        response1.getStatusCode() == 200

        when:
        response2.getStatusCode()

        then:
        thrown(StorageException)
    }

    def "Set tier some succeed do not throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(APISpec.defaultInputStream.get(), APISpec.defaultDataSize)

        when:
        def response1 = batch.setTier(containerName, blobName1, AccessTier.HOT)
        def response2 = batch.setTier(containerName, blobName2, AccessTier.COOL)
        primaryBlobServiceClient.submitBatchWithResponse(batch, false, null, Context.NONE)

        then:
        notThrown(StorageException)
        response1.getStatusCode() == 200

        when:
        response2.getStatusCode()

        then:
        thrown(StorageException)
    }

    def "Set tier none succeed throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()

        when:
        def response1 = batch.setTier(containerName, blobName1, AccessTier.HOT)
        def response2 = batch.setTier(containerName, blobName2, AccessTier.COOL)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        thrown(StorageException)

        when:
        response1.getStatusCode()

        then:
        thrown(StorageException)

        when:
        response2.getStatusCode()

        then:
        thrown(UnsupportedOperationException)
    }

    def "Set tier none succeed do not throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()

        when:
        def response1 = batch.setTier(containerName, blobName1, AccessTier.HOT)
        def response2 = batch.setTier(containerName, blobName2, AccessTier.COOL)
        primaryBlobServiceClient.submitBatchWithResponse(batch, false, null, Context.NONE)

        then:
        notThrown(StorageException)

        when:
        response1.getStatusCode()

        then:
        thrown(StorageException)

        when:
        response2.getStatusCode()

        then:
        thrown(StorageException)
    }

    def "Delete blob all succeed"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0)
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0)

        when:
        def response1 = batch.delete(containerName, blobName1)
        def response2 = batch.delete(containerName, blobName2)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        notThrown(StorageException)
        response1.getStatusCode() == 202
        response2.getStatusCode() == 202
    }

    def "Delete blob some succeed throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0)

        when:
        def response1 = batch.delete(containerName, blobName1)
        def response2 = batch.delete(containerName, blobName2)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        thrown(StorageException)
        response1.getStatusCode() == 202

        when:
        response2.getStatusCode()

        then:
        thrown(StorageException)
    }

    def "Delete blob some succeed do not throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0)

        when:
        def response1 = batch.delete(containerName, blobName1)
        def response2 = batch.delete(containerName, blobName2)
        primaryBlobServiceClient.submitBatchWithResponse(batch, false, null, Context.NONE)

        then:
        notThrown(StorageException)
        response1.getStatusCode() == 202

        when:
        response2.getStatusCode()

        then:
        thrown(StorageException)
    }

    def "Delete blob none succeed throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()

        when:
        def response1 = batch.delete(containerName, blobName1)
        def response2 = batch.delete(containerName, blobName2)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        thrown(StorageException)

        when:
        response1.getStatusCode()

        then:
        thrown(StorageException)

        when:
        response2.getStatusCode()

        then:
        thrown(UnsupportedOperationException)
    }

    def "Delete blob none succeed do not throw on any error"() {
        setup:
        def containerName = generateContainerName()
        def blobName1 = generateBlobName()
        def blobName2 = generateBlobName()
        def batch = new BlobBatch(primaryBlobServiceClient)
        def containerClient = primaryBlobServiceClient.getBlobContainerClient(containerName)
        containerClient.create()

        when:
        def response1 = batch.delete(containerName, blobName1)
        def response2 = batch.delete(containerName, blobName2)
        primaryBlobServiceClient.submitBatchWithResponse(batch, false, null, Context.NONE)

        then:
        notThrown(StorageException)

        when:
        response1.getStatusCode()

        then:
        thrown(StorageException)

        when:
        response2.getStatusCode()

        then:
        thrown(StorageException)
    }

    def "Accessing batch request before submission throws"() {
        setup:
        def batch = new BlobBatch(primaryBlobServiceAsyncClient)

        when:
        def batchRequest = batch.delete("blob", "container")
        batchRequest.getStatusCode()

        then:
        thrown(UnsupportedOperationException)
    }
}
