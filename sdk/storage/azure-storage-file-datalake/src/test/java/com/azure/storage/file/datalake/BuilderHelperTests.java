// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.file.datalake.implementation.util.BuilderHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuilderHelperTests {
    private static final StorageSharedKeyCredential CREDENTIALS = new StorageSharedKeyCredential("accountName",
        "accountKey");
    private static final String ENDPOINT = "https://account.blob.core.windows.net/";
    private static final RequestRetryOptions REQUEST_RETRY_OPTIONS = new RequestRetryOptions(RetryPolicyType.FIXED, 2,
        2, 1000L, 4000L, null);
    private static final RetryOptions CORE_RETRY_OPTIONS = new RetryOptions(new FixedDelayOptions(1,
        Duration.ofMillis(1000)));
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-file-datalake.properties");
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault("name", "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");
    private static final String UA_PATTERN =
        "azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + CLIENT_NAME + "/"
        + CLIENT_VERSION + " " + "(.)*";

    private static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, url)
            .setHeader(HttpHeaderName.CONTENT_LENGTH, "0")
            .setBody(Flux.empty());
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    @Test
    public void freshDateAppliedOnRetry() {
        HttpPipeline pipeline = BuilderHelper.buildPipeline(CREDENTIALS, null, null, ENDPOINT, REQUEST_RETRY_OPTIONS,
            null, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(), new FreshDateTestClient(),
            new ArrayList<>(), new ArrayList<>(), null, new ClientLogger(BuilderHelperTests.class));

        StepVerifier.create(pipeline.send(request(ENDPOINT)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the service client builder's default pipeline.
     */
    @Test
    public void serviceClientFreshDateOnRetry() {
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the file system client builder's default pipeline.
     */
    @Test
    public void fileSystemClientFreshDateOnRetry() {
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .fileSystemName("fileSystem")
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        StepVerifier.create(fileSystemClient.getHttpPipeline().send(request(fileSystemClient.getFileSystemUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the path client builder's default pipeline.
     */
    @Test
    public void pathClientFreshDateOnRetry() {
        DataLakePathClientBuilder pathClientBuilder = new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .fileSystemName("fileSystem")
            .pathName("path")
            .credential(CREDENTIALS)
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .httpClient(new FreshDateTestClient());

        DataLakeDirectoryClient directoryClient = pathClientBuilder.buildDirectoryClient();

        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        DataLakeFileClient fileClient = pathClientBuilder.buildFileClient();

        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private static Stream<Arguments> clientAndLogOptions() {
        return Stream.of(
            // logOptionsUA | clientOptionsUA | expectedUA
            Arguments.of("log-options-id", null, "log-options-id"),
            Arguments.of(null, "client-options-id", "client-options-id"),
            // Client options preferred over log options
            Arguments.of("log-options-id", "client-options-id", "client-options-id")
        );
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("clientAndLogOptions")
    public void customApplicationIdInUAString(String logOptionsUA, String clientOptionsUA, String expectedUA) {
        HttpPipeline pipeline = BuilderHelper.buildPipeline(CREDENTIALS, null, null, ENDPOINT,
            new RequestRetryOptions(), null, new HttpLogOptions().setApplicationId(logOptionsUA),
            new ClientOptions().setApplicationId(clientOptionsUA), new ApplicationIdUAStringTestClient(expectedUA),
            new ArrayList<>(), new ArrayList<>(), null, new ClientLogger(BuilderHelperTests.class));

        StepVerifier.create(pipeline.send(request(ENDPOINT)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("clientAndLogOptions")
    public void serviceClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the file system client builder's default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("clientAndLogOptions")
    public void fileSystemClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .fileSystemName("fileSystem")
            .credential(CREDENTIALS)
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .buildClient();

        StepVerifier.create(fileSystemClient.getHttpPipeline().send(request(fileSystemClient.getFileSystemUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the path client builder's default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("clientAndLogOptions")
    public void pathClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        DataLakePathClientBuilder pathClientBuilder = new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .fileSystemName("fileSystem")
            .pathName("path")
            .credential(CREDENTIALS)
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA));

        DataLakeDirectoryClient directoryClient = pathClientBuilder.buildDirectoryClient();

        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        DataLakeFileClient fileClient = pathClientBuilder.buildFileClient();

        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void doesNotThrowOnAmbiguousCredentialsWithoutAzureSasCredential() {
        assertDoesNotThrow(() -> new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .buildClient());

        assertDoesNotThrow(() -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .pathName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .buildDirectoryClient());

        assertDoesNotThrow(() -> new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .buildClient());
    }

    @Test
    public void throwsOnAmbiguousCredentialsWithAzureSasCredential() {
        assertThrows(IllegalStateException.class, () -> new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT + "?sig=foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .pathName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .pathName("foo")
            .credential(new MockTokenCredential())
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .pathName("foo")
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT + "?sig=foo")
            .pathName("foo")
            .credential(new AzureSasCredential("foo"))
            .buildDirectoryClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT + "?sig=foo")
            .credential(new AzureSasCredential("foo"))
            .buildClient());
    }

    @Test
    public void onlyOneRetryOptionsCanBeApplied() {
        assertThrows(IllegalStateException.class, () -> new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .retryOptions(CORE_RETRY_OPTIONS)
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .fileSystemName("foo")
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .retryOptions(CORE_RETRY_OPTIONS)
            .buildClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .fileSystemName("foo")
            .pathName("foo")
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .retryOptions(CORE_RETRY_OPTIONS)
            .buildFileClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .fileSystemName("foo")
            .pathName("foo")
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .retryOptions(CORE_RETRY_OPTIONS)
            .buildFileClient());

        assertThrows(IllegalStateException.class, () -> new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .fileSystemName("foo")
            .pathName("foo")
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .retryOptions(CORE_RETRY_OPTIONS)
            .buildDirectoryClient());
    }

    @Test
    public void serviceClientBlobUserAgentModificationPolicy() {
        DataLakeServiceClient serviceClient = new DataLakeServiceClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpClient(new UAStringTestClient(UA_PATTERN))
            .buildClient();

        StepVerifier.create(serviceClient.blobServiceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void fileSystemClientBlobUserAgentModificationPolicy() {
        DataLakeFileSystemClient fileSystemClient = new DataLakeFileSystemClientBuilder()
            .endpoint(ENDPOINT)
            .fileSystemName("fileSystem")
            .credential(CREDENTIALS)
            .httpClient(new UAStringTestClient(UA_PATTERN))
            .buildClient();

        StepVerifier.create(fileSystemClient.blobContainerClient.getHttpPipeline().send(request(fileSystemClient.getFileSystemUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void pathClientBlobUserAgentModificationPolicy() {
        DataLakePathClientBuilder pathClientBuilder = new DataLakePathClientBuilder()
            .endpoint(ENDPOINT)
            .fileSystemName("fileSystem")
            .pathName("path")
            .credential(CREDENTIALS)
            .httpClient(new UAStringTestClient(UA_PATTERN));

        DataLakeDirectoryClient directoryClient = pathClientBuilder.buildDirectoryClient();

        StepVerifier.create(directoryClient.blockBlobClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        DataLakeFileClient fileClient = pathClientBuilder.buildFileClient();

        StepVerifier.create(fileClient.blockBlobClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private static final class FreshDateTestClient implements HttpClient {
        private String firstDate;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = validateDate(request.getHeaders().getValue(HttpHeaderName.DATE));
                return Mono.error(new IOException("IOException!"));
            }

            assertNotEquals(firstDate, validateDate(request.getHeaders().getValue(HttpHeaderName.DATE)));
            return Mono.just(new MockHttpResponse(request, 200));
        }

        private static String validateDate(String dateHeader) {
            if (CoreUtils.isNullOrEmpty(dateHeader)) {
                throw new RuntimeException("Failed to set 'Date' header.");
            }

            return dateHeader;
        }
    }

    private static final class ApplicationIdUAStringTestClient implements HttpClient {

        private final String expectedUA;

        ApplicationIdUAStringTestClient(String expectedUA) {
            this.expectedUA = expectedUA;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(HttpHeaderName.USER_AGENT))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.");
            }

            assertTrue(request.getHeaders().getValue(HttpHeaderName.USER_AGENT).startsWith(expectedUA));

            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    private static final class UAStringTestClient implements HttpClient {

        private final Pattern pattern;

        UAStringTestClient(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(HttpHeaderName.USER_AGENT))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.");
            }

            Matcher matcher = pattern.matcher(request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
            assertTrue(matcher.matches());

            return Mono.just(new MockHttpResponse(request, 200));
        }
    }
}
