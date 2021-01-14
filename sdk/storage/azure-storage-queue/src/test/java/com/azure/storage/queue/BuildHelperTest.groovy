// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue

import com.azure.core.credential.AzureSasCredential
import com.azure.core.credential.TokenCredential
import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.ClientOptions
import com.azure.core.util.Configuration
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.core.util.Header
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import com.azure.storage.queue.implementation.util.BuilderHelper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

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
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, null, endpoint, requestRetryOptions, BuilderHelper.defaultHttpLogOptions,
            new ClientOptions(), new FreshDateTestClient(), new ArrayList<>(), new ArrayList<>(), Configuration.NONE, new ClientLogger(BuildHelperTest.class))

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

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    @Unroll
    def "Custom application id in UA string"() {
        when:
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, null,
            endpoint, new RequestRetryOptions(), new HttpLogOptions().setApplicationId(logOptionsUA), new ClientOptions().setApplicationId(clientOptionsUA),
            new ApplicationIdUAStringTestClient(expectedUA), new ArrayList<>(), new ArrayList<>(), Configuration.NONE, new ClientLogger(BuildHelperTest.class))

        then:
        StepVerifier.create(pipeline.send(request(endpoint)))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default pipeline.
     */
    @Unroll
    def "Service client custom application id in UA string"() {
        when:
        def serviceClient = new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getQueueServiceUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the queue client builder's default pipeline.
     */
    @Unroll
    def "Queue client custom application id in UA string"() {
        when:
        def queueClient = new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("queue")
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient()

        then:
        StepVerifier.create(queueClient.getHttpPipeline().send(request(queueClient.getQueueUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a custom headers will be honored when using the default pipeline builder.
     */
    def "Custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        when:
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, null,
            endpoint, new RequestRetryOptions(), BuilderHelper.defaultHttpLogOptions, new ClientOptions().setHeaders(headers),
            new ClientOptionsHeadersTestClient(headers), new ArrayList<>(), new ArrayList<>(), Configuration.NONE, new ClientLogger(BuildHelperTest.class))

        then:
        StepVerifier.create(pipeline.send(request(endpoint)))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default pipeline.
     */
    def "Service client custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        when:
        def serviceClient = new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getQueueServiceUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the queue client builder's default pipeline.
     */
    def "Queue client custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        when:
        def queueClient = new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("queue")
            .credential(credentials)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient()

        then:
        StepVerifier.create(queueClient.getHttpPipeline().send(request(queueClient.getQueueUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    def "Does not throw on ambiguous credentials, without AzureSasCredential"(){
        when:
        new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(Mock(TokenCredential.class))
            .sasToken("foo")
            .buildClient()

        then:
        noExceptionThrown()

        when:
        new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(Mock(TokenCredential.class))
            .sasToken("foo")
            .buildClient()

        then:
        noExceptionThrown()
    }

    def "Throws on ambiguous credentials, with AzureSasCredential"() {
        when:
        new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("foo")
            .credential(Mock(TokenCredential.class))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueClientBuilder()
            .endpoint(endpoint)
            .queueName("foo")
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .queueName("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .credential(Mock(TokenCredential.class))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueServiceClientBuilder()
            .endpoint(endpoint)
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new QueueServiceClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)
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

    private static final class ApplicationIdUAStringTestClient implements HttpClient {

        private final String expectedUA;

        ApplicationIdUAStringTestClient(String expectedUA) {
            this.expectedUA = expectedUA;
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue("User-Agent"))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.")
            }
            assert request.getHeaders().getValue("User-Agent").startsWith(expectedUA)
            return Mono.just(new MockHttpResponse(request, 200))
        }
    }

    private static final class ClientOptionsHeadersTestClient implements HttpClient {

        private final Iterable<Header> headers;

        ClientOptionsHeadersTestClient(Iterable<Header> headers) {
            this.headers = headers;
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request) {

            headers.forEach({ header ->
                if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(header.getName()))) {
                    throw new RuntimeException("Failed to set custom header " + header.getName())
                }
                // This is meant to not match.
                if (header.getName() == "Authorization") {
                    if (request.getHeaders().getValue(header.getName()) == header.getValue()) {
                        throw new RuntimeException("Custom header " + header.getName() + " did not match expectation.")
                    }
                } else {
                    if (request.getHeaders().getValue(header.getName()) != header.getValue()) {
                        throw new RuntimeException("Custom header " + header.getName() + " did not match expectation.")
                    }
                }

            })
            return Mono.just(new MockHttpResponse(request, 200))
        }
    }
}
