// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpPipelinePolicy
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.Configuration
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy
import com.azure.storage.file.share.implementation.util.BuilderHelper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.util.function.Supplier

class BuilderHelperTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.file.core.windows.net/"

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty())
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    def "Fresh date applied on retry"() {
        when:
        def credentialPolicySupplier = new Supplier<HttpPipelinePolicy>() {
            @Override
            HttpPipelinePolicy get() {
                return new StorageSharedKeyCredentialPolicy(credentials)
            }
        }

        def pipeline = BuilderHelper.buildPipeline(credentialPolicySupplier, new RequestRetryOptions(), null,
            new FreshDateTestClient(), new ArrayList<>(), Configuration.NONE)

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
        def serviceClient = new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getFileServiceUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the share client builder's default pipeline.
     */
    def "Share client fresh date on retry"() {
        when:
        def shareClient = new ShareClientBuilder()
            .endpoint(endpoint)
            .shareName("share")
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .buildClient()

        then:
        StepVerifier.create(shareClient.getHttpPipeline().send(request(shareClient.getShareUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the file client builder's default pipeline.
     */
    def "File client fresh date on retry"() {
        setup:
        def fileClientBuilder = new ShareFileClientBuilder()
            .endpoint(endpoint)
            .shareName("fileSystem")
            .resourcePath("path")
            .credential(credentials)

        when:
        def directoryClient = fileClientBuilder.httpClient(new FreshDateTestClient()).buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = fileClientBuilder.httpClient(new FreshDateTestClient()).buildFileClient()

        then:
        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
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
