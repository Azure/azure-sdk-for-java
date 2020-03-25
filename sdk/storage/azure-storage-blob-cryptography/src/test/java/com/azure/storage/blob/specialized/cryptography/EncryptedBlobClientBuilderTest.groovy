// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.storage.common.StorageSharedKeyCredential
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.security.SecureRandom

class EncryptedBlobClientBuilderTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.blob.core.windows.net/"

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
}
