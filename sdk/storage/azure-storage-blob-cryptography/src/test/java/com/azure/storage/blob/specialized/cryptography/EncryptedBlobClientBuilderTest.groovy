// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.ClientOptions
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.core.util.Header
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.security.SecureRandom

class EncryptedBlobClientBuilderTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.blob.core.windows.net/"
    static def requestRetryOptions = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1000, 4000, null)

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty())
    }

    /**
     * Tests that a new date will be applied to every retry when using the encrypted blob client builder's default
     * pipeline.
     */
    def "Encrypted blob client fresh date on retry"() {
        when:
        def randomData = new byte[256]
        new SecureRandom().nextBytes(randomData)

        def encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildEncryptedBlobClient()

        then:
        StepVerifier.create(encryptedBlobClient.getHttpPipeline().send(request(encryptedBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the encrypted blob client builder's default
     * pipeline.
     */
    @Unroll
    def "Encrypted blob client custom application id in UA string"() {
        when:
        def randomData = new byte[256]
        new SecureRandom().nextBytes(randomData)

        def encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .buildEncryptedBlobClient()

        then:
        StepVerifier.create(encryptedBlobClient.getHttpPipeline().send(request(encryptedBlobClient.getBlobUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that custom headers will be honored when using the encrypted blob client builder's default
     * pipeline.
     */
    @Unroll
    def "Encrypted blob client custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        when:
        def randomData = new byte[256]
        new SecureRandom().nextBytes(randomData)

        def encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(endpoint)
            .containerName("container")
            .blobName("blob")
            .credential(credentials)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .clientOptions(new ClientOptions().setHeaders(headers))
            .buildEncryptedBlobClient()

        then:
        StepVerifier.create(encryptedBlobClient.getHttpPipeline().send(request(encryptedBlobClient.getBlobUrl())))
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
