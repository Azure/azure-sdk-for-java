// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.*
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.blob.implementation.util.BuilderHelper
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class BuilderHelperTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.blob.core.windows.net/"
    static def requestRetryOptions = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1000, 4000, null)

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty())
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    def "Fresh date applied on retry"() {
        when:
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, endpoint, requestRetryOptions, BuilderHelper.getDefaultHttpLogOptions(),
            new FreshDateTestClient(), new ArrayList<>(), new ArrayList<>(), null, new ClientLogger(BuilderHelperTest.class))

        then:
        StepVerifier.create(pipeline.send(request(endpoint)))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the service client builder's default pipeline.
     */
    def "Service client fresh date on retry"() {
        when:
        def serviceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the container client builder's default pipeline.
     */
    def "Container client fresh date on retry"() {
        when:
        def containerClient = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildClient()

        then:
        StepVerifier.create(containerClient.getHttpPipeline().send(request(containerClient.getBlobContainerUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the blob client builder's default pipeline.
     */
    def "Blob client fresh date on retry"() {
        when:
        def blobClient = new BlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildClient()

        then:
        StepVerifier.create(blobClient.getHttpPipeline().send(request(blobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the specialized blob client builder's default
     * pipeline.
     */
    def "Specialized blob client fresh date on retry"() {
        setup:
        def specializedBlobClientBuilder = new SpecializedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .retryOptions(requestRetryOptions)
            .httpClient(new FreshDateTestClient())

        when:
        def appendBlobClient = specializedBlobClientBuilder
            .buildAppendBlobClient()

        then:
        StepVerifier.create(appendBlobClient.getHttpPipeline().send(request(appendBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def blockBlobClient = specializedBlobClientBuilder
            .buildBlockBlobClient()

        then:
        StepVerifier.create(blockBlobClient.getHttpPipeline().send(request(blockBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def pageBlobClient = specializedBlobClientBuilder
            .buildPageBlobClient()

        then:
        StepVerifier.create(pageBlobClient.getHttpPipeline().send(request(pageBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    def "Custom application id in UA string"() {
        when:
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, endpoint, new RequestRetryOptions(), new HttpLogOptions().setApplicationId("custom-id"),
            new ApplicationIdUAStringTestClient(), new ArrayList<>(), new ArrayList<>(), null, new ClientLogger(BuilderHelperTest.class))

        then:
        StepVerifier.create(pipeline.send(request(endpoint)))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default pipeline.
     */
    def "Service client custom application id in UA string"() {
        when:
        def serviceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId("custom-id"))
            .httpClient(new ApplicationIdUAStringTestClient())
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the container client builder's default pipeline.
     */
    def "Container client custom application id in UA string"() {
        when:
        def containerClient = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId("custom-id"))
            .httpClient(new ApplicationIdUAStringTestClient())
            .buildClient()

        then:
        StepVerifier.create(containerClient.getHttpPipeline().send(request(containerClient.getBlobContainerUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the blob client builder's default pipeline.
     */
    def "Blob client custom application id in UA string"() {
        when:
        def blobClient = new BlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId("custom-id"))
            .httpClient(new ApplicationIdUAStringTestClient())
            .buildClient()

        then:
        StepVerifier.create(blobClient.getHttpPipeline().send(request(blobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the specialized blob client builder's default
     * pipeline.
     */
    def "Specialized blob client custom application id in UA string"() {
        setup:
        def specializedBlobClientBuilder = new SpecializedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId("custom-id"))
            .httpClient(new ApplicationIdUAStringTestClient())

        when:
        def appendBlobClient = specializedBlobClientBuilder
            .buildAppendBlobClient()

        then:
        StepVerifier.create(appendBlobClient.getHttpPipeline().send(request(appendBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def blockBlobClient = specializedBlobClientBuilder
            .buildBlockBlobClient()

        then:
        StepVerifier.create(blockBlobClient.getHttpPipeline().send(request(blockBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def pageBlobClient = specializedBlobClientBuilder
            .buildPageBlobClient()

        then:
        StepVerifier.create(pageBlobClient.getHttpPipeline().send(request(pageBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    private static final class FreshDateTestClient implements HttpClient {
        private DateTimeRfc1123 firstDate

        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = convertToDateObject(request.getHeaders().getValue("Date"))
                return Mono.error(new IOException("IOException!"))
            }

            assert firstDate != convertToDateObject(request.getHeaders().getValue("Date"))
            return Mono.just(new MockHttpResponse(request, 200))
        }

        private static DateTimeRfc1123 convertToDateObject(String dateHeader) {
            if (CoreUtils.isNullOrEmpty(dateHeader)) {
                throw new RuntimeException("Failed to set 'Date' header.")
            }

            return new DateTimeRfc1123(dateHeader)
        }
    }

    private static final class ApplicationIdUAStringTestClient implements HttpClient {
        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue("User-Agent"))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.")
            }
            assert request.getHeaders().getValue("User-Agent").startsWith("custom-id")
            return Mono.just(new MockHttpResponse(request, 200))
        }
    }
}
