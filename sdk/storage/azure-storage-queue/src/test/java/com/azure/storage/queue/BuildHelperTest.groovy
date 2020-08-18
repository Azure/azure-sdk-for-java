// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.Configuration
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import com.azure.storage.queue.implementation.util.BuilderHelper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class BuildHelperTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.queue.core.windows.net/"
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
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, endpoint, requestRetryOptions, null,
            new FreshDateTestClient(), new ArrayList<>(), Configuration.NONE, new ClientLogger(BuildHelperTest.class))

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
        def serviceClient = new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getQueueServiceUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the queue client builder's default pipeline.
     */
    def "Queue client fresh date on retry"() {
        when:
        def queueClient = new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("queue")
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildClient()

        then:
        StepVerifier.create(queueClient.getHttpPipeline().send(request(queueClient.getQueueUrl())))
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

    def "Parse protocol"() {
        when:
        def parts = BuilderHelper.parseEndpoint(endpoint +
            "?sv=2019-12-12&ss=bfqt&srt=s&sp=rwdlacupx&se=2020-08-15T05:43:05Z&st=2020-08-14T21:43:05Z&spr=https,http&sig=sig", null)

        then:
        parts.getSasToken().contains("https%2Chttp")
    }
}
