// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.credential.AzureSasCredential
import com.azure.core.credential.TokenCredential
import com.azure.core.http.HttpClient
import com.azure.core.http.HttpHeaders
import com.azure.core.http.HttpMethod
import com.azure.core.http.HttpRequest
import com.azure.core.http.HttpResponse
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.http.policy.HttpPipelinePolicy
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
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy
import com.azure.storage.file.share.implementation.util.BuilderHelper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.Supplier

class BuilderHelperTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.file.core.windows.net/"
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
        def credentialPolicySupplier = new Supplier<HttpPipelinePolicy>() {
            @Override
            HttpPipelinePolicy get() {
                return new StorageSharedKeyCredentialPolicy(credentials)
            }
        }

        def pipeline = BuilderHelper.buildPipeline(credentialPolicySupplier, requestRetryOptions, BuilderHelper.defaultHttpLogOptions,
            new ClientOptions(), new FreshDateTestClient(), new ArrayList<>(), new ArrayList<>(), Configuration.NONE)

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
            .retryOptions(requestRetryOptions)
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
            .retryOptions(requestRetryOptions)
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
            .retryOptions(requestRetryOptions)
            .httpClient(new FreshDateTestClient())

        when:
        def directoryClient = fileClientBuilder.buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = fileClientBuilder.buildFileClient()

        then:
        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    @Unroll
    def "Custom application id in UA string"() {
        when:
        def credentialPolicySupplier = new Supplier<HttpPipelinePolicy>() {
            @Override
            HttpPipelinePolicy get() {
                return new StorageSharedKeyCredentialPolicy(credentials)
            }
        }

        def pipeline = BuilderHelper.buildPipeline(credentialPolicySupplier, new RequestRetryOptions(), new HttpLogOptions().setApplicationId(logOptionsUA), new ClientOptions().setApplicationId(clientOptionsUA),
            new ApplicationIdUAStringTestClient(expectedUA), new ArrayList<>(), new ArrayList<>(), Configuration.NONE)

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
        def serviceClient = new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getFileServiceUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the share client builder's default pipeline.
     */
    @Unroll
    def "Share client custom application id in UA string"() {
        when:
        def shareClient = new ShareClientBuilder()
            .endpoint(endpoint)
            .shareName("share")
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient()

        then:
        StepVerifier.create(shareClient.getHttpPipeline().send(request(shareClient.getShareUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the file client builder's default pipeline.
     */
    @Unroll
    def "File client custom application id in UA string"() {
        setup:
        def fileClientBuilder = new ShareFileClientBuilder()
            .endpoint(endpoint)
            .shareName("fileSystem")
            .resourcePath("path")
            .credential(credentials)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))

        when:
        def directoryClient = fileClientBuilder.buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = fileClientBuilder.buildFileClient()

        then:
        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
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
        def credentialPolicySupplier = new Supplier<HttpPipelinePolicy>() {
            @Override
            HttpPipelinePolicy get() {
                return new StorageSharedKeyCredentialPolicy(credentials)
            }
        }

        def pipeline = BuilderHelper.buildPipeline(credentialPolicySupplier, new RequestRetryOptions(), BuilderHelper.defaultHttpLogOptions, new ClientOptions().setHeaders(headers),
            new ClientOptionsHeadersTestClient(headers), new ArrayList<>(), new ArrayList<>(), Configuration.NONE)

        then:
        StepVerifier.create(pipeline.send(request(endpoint)))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that custom headers will be honored when using the service client builder's default pipeline.
     */
    def "Service client custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        when:
        def serviceClient = new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getFileServiceUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that custom headers will be honored when using the share client builder's default pipeline.
     */
    def "Share client custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        when:
        def shareClient = new ShareClientBuilder()
            .endpoint(endpoint)
            .shareName("share")
            .credential(credentials)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient()

        then:
        StepVerifier.create(shareClient.getHttpPipeline().send(request(shareClient.getShareUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that custom headers will be honored when using the blob client builder's default pipeline.
     */
    def "Blob client custom headers client options"() {
        setup:
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"))
        headers.add(new Header("Authorization", "notthis"))
        headers.add(new Header("User-Agent", "overwritten"))

        def fileClientBuilder = new ShareFileClientBuilder()
            .endpoint(endpoint)
            .shareName("share")
            .resourcePath("blob")
            .credential(credentials)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))

        when:
        def directoryClient = fileClientBuilder.buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = fileClientBuilder.buildFileClient()

        then:
        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

    }

    def "Does not throw on ambiguous credentials, without AzureSasCredential"(){
        when:
        new ShareClientBuilder()
            .endpoint(endpoint)
            .shareName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .sasToken("foo")
            .buildClient()

        then:
        noExceptionThrown()

        when:
        new ShareFileClientBuilder()
            .endpoint(endpoint)
            .shareName("foo")
            .resourcePath("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .sasToken("foo")
            .buildDirectoryClient()

        then:
        noExceptionThrown()

        when:
        new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .sasToken("foo")
            .buildClient()

        then:
        noExceptionThrown()
    }

    def "Throws on ambiguous credentials, with AzureSasCredential"() {
        when:
        new ShareClientBuilder()
            .endpoint(endpoint)
            .shareName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareClientBuilder()
            .endpoint(endpoint)
            .shareName("foo")
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .shareName("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareFileClientBuilder()
            .endpoint(endpoint)
            .shareName("foo")
            .resourcePath("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareFileClientBuilder()
            .endpoint(endpoint)
            .shareName("foo")
            .resourcePath("foo")
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareFileClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .shareName("foo")
            .resourcePath("foo")
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareServiceClientBuilder()
            .endpoint(endpoint)
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new ShareServiceClientBuilder()
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
