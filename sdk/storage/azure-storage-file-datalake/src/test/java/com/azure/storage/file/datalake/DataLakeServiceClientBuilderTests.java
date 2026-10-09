// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.ContainerSessionProvider;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionOptions.SessionMode;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataLakeServiceClientBuilderTests {

    private static final String ENDPOINT = "https://account.blob.core.windows.net/";

    @Test
    public void independentSyncClientsForwardTheSharedProviderToBlob() {
        AtomicInteger acquisitions = new AtomicInteger();
        HttpClient transport = sessionTransport(acquisitions);
        ContainerSessionProvider provider
            = new ContainerSessionProvider(new BlobServiceClientBuilder().endpoint(ENDPOINT)
                .credential(new MockTokenCredential())
                .httpClient(transport)
                .buildClient());
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.ENABLED).setSessionProvider(provider);
        for (int i = 0; i < 3; i++) {
            DataLakeServiceClient client
                = new DataLakeServiceClientBuilder().endpoint("https://account.dfs.core.windows.net")
                    .credential(new MockTokenCredential())
                    .httpClient(transport)
                    .sessionOptions(options)
                    .buildClient();
            assertFalse(hasPolicyOfType(client.getHttpPipeline(), "SessionAuthenticationPolicy"));
            HttpPipeline pipeline = client.getFileSystemClient("container").getBlobContainerClient().getHttpPipeline();
            HttpRequest request = new HttpRequest(HttpMethod.GET, ENDPOINT + "container/blob");
            try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
                assertEquals(200, response.getStatusCode());
                assertTrue(response.getRequest()
                    .getHeaders()
                    .getValue(HttpHeaderName.AUTHORIZATION)
                    .startsWith("Session shared:"));
            }
        }
        assertSame(provider, options.getSessionProvider());
        assertEquals(1, acquisitions.get());
    }

    @Test
    public void independentAsyncClientsForwardTheSharedProviderToBlob() {
        AtomicInteger acquisitions = new AtomicInteger();
        HttpClient transport = sessionTransport(acquisitions);
        ContainerSessionProvider provider
            = new ContainerSessionProvider(new BlobServiceClientBuilder().endpoint(ENDPOINT)
                .credential(new MockTokenCredential())
                .httpClient(transport)
                .buildClient());
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.ENABLED).setSessionProvider(provider);
        for (int i = 0; i < 3; i++) {
            DataLakeServiceAsyncClient client
                = new DataLakeServiceClientBuilder().endpoint("https://account.dfs.core.windows.net")
                    .credential(new MockTokenCredential())
                    .httpClient(transport)
                    .sessionOptions(options)
                    .buildAsyncClient();
            assertFalse(hasPolicyOfType(client.getHttpPipeline(), "SessionAuthenticationPolicy"));
            HttpPipeline pipeline
                = client.getFileSystemAsyncClient("container").getBlobContainerAsyncClient().getHttpPipeline();
            HttpRequest request = new HttpRequest(HttpMethod.GET, ENDPOINT + "container/blob");
            StepVerifier.create(pipeline.send(request)).assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertTrue(response.getRequest()
                    .getHeaders()
                    .getValue(HttpHeaderName.AUTHORIZATION)
                    .startsWith("Session shared:"));
                response.close();
            }).verifyComplete();
        }
        assertSame(provider, options.getSessionProvider());
        assertEquals(1, acquisitions.get());
    }

    private static HttpClient sessionTransport(AtomicInteger acquisitions) {
        return request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                acquisitions.incrementAndGet();
                String body = "<CreateSessionResult><Id>id</Id><AuthenticationType>HMAC</AuthenticationType>"
                    + "<Credentials><SessionToken>shared</SessionToken><SessionKey>a2V5</SessionKey>"
                    + "</Credentials></CreateSessionResult>";
                return Mono.just(new MockHttpResponse(request, 201, body.getBytes(StandardCharsets.UTF_8))
                    .addHeader("Content-Type", "application/xml"));
            }
            return Mono.just(new MockHttpResponse(request, 200));
        };
    }

    @Test
    public void defaultTokenCredentialClientsUseBearerPolicyWithoutSessions() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .buildClient();

        HttpPipeline pipeline = client.blobServiceClient.getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    @Test
    public void disablingSessionsRemovesSessionPolicyButKeepsBearerPolicy() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(new SessionOptions().setSessionMode(SessionMode.DISABLED))
            .buildClient();

        HttpPipeline pipeline = client.blobServiceClient.getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    @Test
    public void fileSystemClientsReuseTheServiceSessionPipeline() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(new SessionOptions().setSessionMode(SessionMode.ENABLED))
            .buildClient();

        DataLakeFileSystemClient fileSystemClient = client.getFileSystemClient("filesystem");

        HttpPipeline pipeline = fileSystemClient.getBlobContainerClient().getHttpPipeline();
        assertSame(client.blobServiceClient.getHttpPipeline(), pipeline);
        assertTrue(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
    }

    @Test
    public void sharedKeyCredentialDoesNotUseBearerOrSessionPolicies() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("account", "accountKey"))
            .httpClient(new NoOpHttpClient())
            .buildClient();

        HttpPipeline pipeline = client.blobServiceClient.getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertFalse(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    @Test
    public void nullSessionOptionsUseBearerPolicyWithoutSessions() {
        DataLakeServiceClient client = assertDoesNotThrow(() -> new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(null)
            .buildClient());

        HttpPipeline pipeline = client.blobServiceClient.getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    @Test
    public void defaultSessionOptionsUseBearerPolicyWithoutSessions() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(new SessionOptions())
            .buildClient();

        HttpPipeline pipeline = client.blobServiceClient.getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    @Test
    public void sessionOptionsOnlyEnableSessionsOnTheBlobPipeline() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(new SessionOptions().setSessionMode(SessionMode.ENABLED))
            .buildClient();

        assertFalse(hasPolicyOfType(client.getHttpPipeline(), "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(client.getHttpPipeline(), "StorageBearerTokenChallengeAuthorizationPolicy"));
        assertTrue(hasPolicyOfType(client.blobServiceClient.getHttpPipeline(), "SessionAuthenticationPolicy"));
    }

    @Test
    public void nullSessionModeUsesBearerPolicyWithoutSessions() {
        DataLakeServiceClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(new SessionOptions().setSessionMode(SessionMode.ENABLED).setSessionMode(null))
            .buildClient();

        HttpPipeline pipeline = client.blobServiceClient.getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    @Test
    public void nullSessionModeAsyncUsesBearerPolicyWithoutSessions() {
        DataLakeServiceAsyncClient client = new DataLakeServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(new SessionOptions().setSessionMode(SessionMode.ENABLED).setSessionMode(null))
            .buildAsyncClient();

        HttpPipeline pipeline
            = client.getFileSystemAsyncClient("filesystem").getBlobContainerAsyncClient().getHttpPipeline();
        assertFalse(hasPolicyOfType(pipeline, "SessionAuthenticationPolicy"));
        assertTrue(hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"));
    }

    private static boolean hasPolicyOfType(HttpPipeline pipeline, String simpleClassName) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass().getSimpleName().equals(simpleClassName)) {
                return true;
            }
        }

        return false;
    }
}
