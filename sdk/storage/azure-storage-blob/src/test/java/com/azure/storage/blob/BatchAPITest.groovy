package com.azure.storage.blob


import com.azure.core.http.HttpPipeline
import com.azure.core.http.HttpPipelineBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobBatch
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.StorageException

class BatchAPITest extends APISpec {
    static def setupCustomPolicy(HttpPipeline pipeline, HttpPipelinePolicy policy) {
        def policies = new HttpPipelinePolicy[pipeline.getPolicyCount()]

        for (def i = 0; i < pipeline.getPolicyCount(); i++) {
            policies[i] = pipeline.getPolicy(i)
        }

        policies[pipeline.getPolicyCount()] = policy

        return policies
    }

    def "Empty batch"() {
        when:
        def batch = new BlobBatch(primaryBlobServiceClient)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        thrown(StorageException)
    }

    def "Mixed batch"() {
        when:
        def batch = new BlobBatch(primaryBlobServiceAsyncClient)
        batch.delete("container", "blob", null, null)
        batch.setTier("container", "blob2", null, null)

        then:
        thrown(UnsupportedOperationException)

        when:
        batch = new BlobBatch(primaryBlobServiceAsyncClient)
        batch.setTier("container", "blob", null, null)
        batch.delete("container", "blob2", null, null)

        then:
        thrown(UnsupportedOperationException)
    }

    def "Incorrect content length"() {
        setup:
        def httpPipeline = cc.getHttpPipeline()

        def pipeline = new HttpPipelineBuilder()
            .policies(setupCustomPolicy(httpPipeline, null)) // replace null with custom policy
            .httpClient(httpPipeline.getHttpClient())
            .build()

        def batch = new BlobBatch(null, pipeline)

        // Needs to use a custom pipeline policy
    }

    def "Sub-request has version"() {
        setup:
        def httpPipeline = cc.getHttpPipeline()

        def pipeline = new HttpPipelineBuilder()
            .policies(setupCustomPolicy(httpPipeline, null)) // replace null with custom policy
            .httpClient(httpPipeline.getHttpClient())
            .build()

        def batch = new BlobBatch(null, pipeline)
    }

    def "Incorrect batch boundary"() {
        setup:
        def httpPipeline = cc.getHttpPipeline()

        def pipeline = new HttpPipelineBuilder()
            .policies(setupCustomPolicy(httpPipeline, null)) // replace null with custom policy
            .httpClient(httpPipeline.getHttpClient())
            .build()

        def batch = new BlobBatch(null, pipeline)
    }

    def "Set tier all succeed"() {
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
        def response1 = batch.setTier(containerName, blobName1, AccessTier.HOT, null)
        def response2 = batch.setTier(containerName, blobName2, AccessTier.COOL, null)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        notThrown(StorageException)
        response1.getStatusCode() == 202
        response2.getStatusCode() == 202
    }

    def "Set tier some succeed throw on any error"() {

    }

    def "Set tier some succeed do not throw on any error"() {

    }

    def "Set tier none succeed"() {

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
        def response1 = batch.delete(containerName, blobName1, null, null)
        def response2 = batch.delete(containerName, blobName2, null, null)
        primaryBlobServiceClient.submitBatch(batch)

        then:
        notThrown(StorageException)
        response1.getStatusCode() == 202
        response2.getStatusCode() == 202
    }

    def "Delete blob some succeed throw on any error"() {

    }

    def "Delete blob some succeed do not throw on any error"() {

    }

    def "Delete blob none succeed"() {

    }

    def "Accessing batch request before submission throws"() {
        setup:
        def batch = new BlobBatch(primaryBlobServiceAsyncClient)

        when:
        def batchRequest = batch.delete("blob", "container", null, null)
        batchRequest.getStatusCode()

        then:
        thrown(UnsupportedOperationException)
    }
}
