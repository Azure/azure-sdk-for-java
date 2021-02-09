// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

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
import com.azure.core.util.CoreUtils
import com.azure.core.util.DateTimeRfc1123
import com.azure.core.util.Header
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.common.StorageSharedKeyCredential
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import com.azure.storage.file.datalake.implementation.util.BuilderHelper
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Matcher
import java.util.regex.Pattern

class BuilderHelperTest extends Specification {
    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.blob.windows.core.net/"
    static def requestRetryOptions = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1000, 4000, null)
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-file-datalake.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty())
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    def "Fresh date applied on retry"() {
        when:
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, null,
            endpoint, requestRetryOptions, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(),
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
        def serviceClient = new DataLakeServiceClientBuilder()
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
     * Tests that a new date will be applied to every retry when using the file system client builder's default pipeline.
     */
    def "File system client fresh date on retry"() {
        when:
        def fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .fileSystemName("fileSystem")
            .credential(credentials)
            .httpClient(new FreshDateTestClient())
            .retryOptions(requestRetryOptions)
            .buildClient()

        then:
        StepVerifier.create(fileSystemClient.getHttpPipeline().send(request(fileSystemClient.getFileSystemUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    /**
     * Tests that a new date will be applied to every retry when using the path client builder's default pipeline.
     */
    def "Path client fresh date on retry"() {
        setup:
        def pathClientBuilder = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .fileSystemName("fileSystem")
            .pathName("path")
            .credential(credentials)
            .retryOptions(requestRetryOptions)
            .httpClient(new FreshDateTestClient())

        when:
        def directoryClient = pathClientBuilder.buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = pathClientBuilder.buildFileClient()

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
        def pipeline = BuilderHelper.buildPipeline(credentials, null, null, null,
            endpoint, new RequestRetryOptions(), new HttpLogOptions().setApplicationId(logOptionsUA), new ClientOptions().setApplicationId(clientOptionsUA),
            new ApplicationIdUAStringTestClient(expectedUA), new ArrayList<>(), new ArrayList<>(), null, new ClientLogger(BuilderHelperTest.class))

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
        def serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .buildClient()

        then:
        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the file system client builder's default pipeline.
     */
    @Unroll
    def "File system client custom application id in UA string"() {
        when:
        def fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .fileSystemName("fileSystem")
            .credential(credentials)
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .buildClient()

        then:
        StepVerifier.create(fileSystemClient.getHttpPipeline().send(request(fileSystemClient.getFileSystemUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        logOptionsUA     | clientOptionsUA     || expectedUA
        "log-options-id" | null                || "log-options-id"
        null             | "client-options-id" || "client-options-id"
        "log-options-id" | "client-options-id" || "client-options-id"   // Client options preferred over log options
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the path client builder's default pipeline.
     */
    @Unroll
    def "Path client custom application id in UA string"() {
        setup:
        def pathClientBuilder = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .fileSystemName("fileSystem")
            .pathName("path")
            .credential(credentials)
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))

        when:
        def directoryClient = pathClientBuilder.buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = pathClientBuilder.buildFileClient()

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

    def "Does not throw on ambiguous credentials, without AzureSasCredential"(){
        when:
        new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(Mock(TokenCredential.class))
            .sasToken("foo")
            .buildClient()

        then:
        noExceptionThrown()

        when:
        new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .pathName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(Mock(TokenCredential.class))
            .sasToken("foo")
            .buildDirectoryClient()

        then:
        noExceptionThrown()

        when:
        new DataLakeServiceClientBuilder()
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
        new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .credential(Mock(TokenCredential.class))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .pathName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .pathName("foo")
            .credential(Mock(TokenCredential.class))
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .pathName("foo")
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakePathClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .pathName("foo")
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(Mock(TokenCredential.class))
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)

        when:
        new DataLakeServiceClientBuilder()
            .endpoint(endpoint + "?sig=foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient()

        then:
        thrown(IllegalStateException.class)
    }

    def "Service client BlobUserAgentModificationPolicy"() {
        when:
        def serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
            .httpClient(new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*"))
            .buildClient()

        then:
        StepVerifier.create(serviceClient.blobServiceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    def "File system client BlobUserAgentModificationPolicy"() {
        when:
        def fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(endpoint)
            .fileSystemName("fileSystem")
            .credential(credentials)
            .httpClient(new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*"))
            .buildClient()

        then:
        StepVerifier.create(fileSystemClient.blobContainerClient.getHttpPipeline().send(request(fileSystemClient.getFileSystemUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    def "Path client BlobUserAgentModificationPolicy"() {
        setup:
        def pathClientBuilder = new DataLakePathClientBuilder()
            .endpoint(endpoint)
            .fileSystemName("fileSystem")
            .pathName("path")
            .credential(credentials)
            .httpClient(new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*"))

        when:
        def directoryClient = pathClientBuilder.buildDirectoryClient()

        then:
        StepVerifier.create(directoryClient.blockBlobClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        when:
        def fileClient = pathClientBuilder.buildFileClient()

        then:
        StepVerifier.create(fileClient.blockBlobClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
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

    private static final class UAStringTestClient implements HttpClient {

        private final Pattern pattern

        UAStringTestClient(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue("User-Agent"))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.")
            }
            Matcher matcher = pattern.matcher(request.getHeaders().getValue("User-Agent"));
            assert matcher.matches()
            return Mono.just(new MockHttpResponse(request, 200))
        }
    }
}
