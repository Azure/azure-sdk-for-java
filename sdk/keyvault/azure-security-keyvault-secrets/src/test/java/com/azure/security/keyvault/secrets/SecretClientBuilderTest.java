// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import com.azure.core.util.Header;
import com.azure.security.keyvault.secrets.implementation.SecretClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SecretClientBuilderTest {
    private String vaultUrl;
    private String secretName;
    private SecretServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        secretName = "TestSecret";
        serviceVersion = SecretServiceVersion.V7_3;
    }

    @Test
    public void buildSyncClientTest() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(secretClient);
        assertEquals(SecretClient.class.getSimpleName(), secretClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(secretClient);
        assertEquals(SecretClient.class.getSimpleName(), secretClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(secretAsyncClient);
        assertEquals(SecretAsyncClient.class.getSimpleName(), secretAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(secretAsyncClient);
        assertEquals(SecretAsyncClient.class.getSimpleName(), secretAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SecretClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SecretClientBuilder().credential(null));
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> secretClient.getSecret(secretName));
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> secretClient.getSecret(secretName));
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () -> secretClient.getSecret(secretName));
    }

    @Test
    public void bothRetryOptionsAndRetryPolicySet() {
        assertThrows(IllegalStateException.class, () -> new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions()))
            .retryPolicy(new RetryPolicy())
            .buildClient());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void addPerCallPolicy() {
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .addPolicy(new TestUtils.PerCallPolicy())
            .addPolicy(new TestUtils.PerRetryPolicy())
            .buildAsyncClient();

        HttpPipeline pipeline = secretAsyncClient.getHttpPipeline();

        int retryPolicyPosition = -1, perCallPolicyPosition = -1, perRetryPolicyPosition = -1;

        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass() == RetryPolicy.class) {
                retryPolicyPosition = i;
            }

            if (pipeline.getPolicy(i).getClass() == TestUtils.PerCallPolicy.class) {
                perCallPolicyPosition = i;
            }

            if (pipeline.getPolicy(i).getClass() == TestUtils.PerRetryPolicy.class) {
                perRetryPolicyPosition = i;
            }
        }

        assertTrue(perCallPolicyPosition != -1);
        assertTrue(perCallPolicyPosition < retryPolicyPosition);
        assertTrue(retryPolicyPosition < perRetryPolicyPosition);
    }

    @Test
    public void setPipeline() {
        List<Object> constructorArguments = new ArrayList<>();
        try (MockedConstruction<SecretClientImpl> mocked = mockConstruction(SecretClientImpl.class, (mock, context) -> constructorArguments.addAll(context.arguments()))) {
            HttpPipeline pipeline = new HttpPipelineBuilder().build();
            new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .pipeline(pipeline)
                .buildClient();
            assertEquals(1, mocked.constructed().size());
            assertSame(pipeline, constructorArguments.get(1));
        }
    }

    @Test
    public void getKeyVaultEndpointFromConfiguration() {
        Map<String, String> configurationMap = new HashMap<>();
        configurationMap.put("AZURE_KEYVAULT_ENDPOINT", vaultUrl);
        ConfigurationSource source = source1 -> configurationMap;

        TokenCredential credential = new TestUtils.TestCredential();
        Configuration configurationBadUrl = new ConfigurationBuilder(source, source, source).build();
        assertThrows(NullPointerException.class, () -> new SecretClientBuilder().configuration(configurationBadUrl)
            .credential(credential)
            .buildClient());
    }

    @Test
    public void misconfiguredKeyVaultEndpointThrowsIllegalStateException() {
        Configuration configurationMissingEndpoint = new ConfigurationBuilder().build();
        assertThrows(IllegalStateException.class, () ->
            new SecretClientBuilder().configuration(configurationMissingEndpoint).buildClient());

        Map<String, String> configurationMap = new HashMap<>();
        configurationMap.put("AZURE_KEYVAULT_ENDPOINT", "bad_url");
        ConfigurationSource source = source1 -> configurationMap;

        Configuration configurationBadUrl = new ConfigurationBuilder(source, source, source).build();
        assertThrows(IllegalStateException.class, () ->
            new SecretClientBuilder().configuration(configurationBadUrl).buildClient());

    }

    @Test
    public void getVaultUrl() {
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .buildClient();
        assertEquals(vaultUrl, secretClient.getVaultUrl());
    }

    @Test
    public void vaultUrlCannotBeSetToNull() {
        assertThrows(NullPointerException.class, () ->
            new SecretClientBuilder().vaultUrl(null));
    }

    @Test
    public void policyCannotBeSetToNull() {
        assertThrows(NullPointerException.class, () ->
            new SecretClientBuilder().addPolicy(null));
    }

    @Test
    public void credentialCannotBeSetToNull() {
        assertThrows(IllegalStateException.class, () ->
            new SecretClientBuilder().vaultUrl(vaultUrl).buildClient());
    }
}
