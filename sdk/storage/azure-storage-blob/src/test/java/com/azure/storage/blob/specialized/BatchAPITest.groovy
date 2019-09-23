package com.azure.storage.blob.specialized


import com.azure.core.http.HttpPipeline
import com.azure.core.http.HttpPipelineBuilder
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.models.StorageException

class BatchAPITest extends APISpec {
    def setupCustomPolicy(HttpPipeline pipeline, HttpPipelinePolicy policy) {
        def policies = new HttpPipelinePolicy[pipeline.getPolicyCount()]

        for (def i = 0; i < pipeline.getPolicyCount(); i++) {
            policies[i] = pipeline.getPolicy(i)
        }

        policies[pipeline.getPolicyCount()] = policy

        return policies
    }

    def "Empty batch"() {
        when:

        then:
        thrown(StorageException)
    }

    def "Mixed batch"() {
        when:

        then:
        thrown(StorageException)
    }

    def "Incorrect content length"() {
        setup:
        def httpPipeline = cc.getHttpPipeline()

        def pipline = new HttpPipelineBuilder()
            .policies(setupCustomPolicy(httpPipeline, null)) // replace null with custom policy
            .httpClient(httpPipeline.getHttpClient())
            .build()

        def batch = new BlobBatch(null, pipline)

        // Needs to use a custom pipeline policy
    }

    def "Sub-request has version"() {
        // Needs to use a custom pipeline policy
    }

    def "Incorrect batch boundary"() {
        // Needs to use a custom pipeline policy
    }

    def "Set tier all succeed"() {
        setup:
        def batch = new BlobBatch(primaryBlobServiceClient)

        when:
        def operation1 = batch.delete("container", "blob", null, null)
        def result = primaryBlobServiceClient.submitBatch(batch)

        then:
        notThrown(StorageException)
        result.getRawResponse(operation1).getStatusCode() == 202
    }

    def "Set tier some succeed"() {

    }

    def "Set tier none succeed"() {

    }

    def "Delete blob all succeed"() {

    }

    def "Delete blob some succeed"() {

    }

    def "Delete blob none succeed"() {

    }
}
