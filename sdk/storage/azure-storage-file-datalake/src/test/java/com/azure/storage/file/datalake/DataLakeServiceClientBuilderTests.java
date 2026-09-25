// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.http.HttpPipeline;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLakeServiceClientBuilderTests {

    private static final String ENDPOINT = "https://account.blob.core.windows.net/";

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

    private static boolean hasPolicyOfType(HttpPipeline pipeline, String simpleClassName) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass().getSimpleName().equals(simpleClassName)) {
                return true;
            }
        }

        return false;
    }
}
