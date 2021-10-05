// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptographyClientBuilderTest {
    private String keyIdentifier;
    private CryptographyServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        keyIdentifier = "https://key-vault-url.vault.azure.net/keys/TestKey/someVersion";
        serviceVersion = CryptographyServiceVersion.V7_2;
    }

    @Test
    public void buildSyncClientTest() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(cryptographyClient);
        assertEquals(CryptographyClient.class.getSimpleName(), cryptographyClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(cryptographyClient);
        assertEquals(CryptographyClient.class.getSimpleName(), cryptographyClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientWithoutKeyVersionTest() {
        String versionlessKeyIdentifier = "https://key-vault-url.vault.azure.net/keys/TestKey";

        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier(versionlessKeyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(cryptographyClient);
        assertEquals(CryptographyClient.class.getSimpleName(), cryptographyClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(cryptographyAsyncClient);
        assertEquals(CryptographyAsyncClient.class.getSimpleName(), cryptographyAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(cryptographyAsyncClient);
        assertEquals(CryptographyAsyncClient.class.getSimpleName(), cryptographyAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientWithoutKeyVersionTest() {
        String versionlessKeyIdentifier = "https://key-vault-url.vault.azure.net/keys/TestKey";

        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier(versionlessKeyIdentifier)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(cryptographyAsyncClient);
        assertEquals(CryptographyAsyncClient.class.getSimpleName(), cryptographyAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new KeyClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyClientBuilder().credential(null));
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, cryptographyClient::getKey);
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, cryptographyClient::getKey);
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        CryptographyClient cryptographyClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, cryptographyClient::getKey);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void addPerCallPolicy() {
        CryptographyAsyncClient cryptographyAsyncClient = new CryptographyClientBuilder()
            .keyIdentifier(keyIdentifier)
            .credential(new TestUtils.TestCredential())
            .addPolicy(new TestUtils.PerCallPolicy())
            .addPolicy(new TestUtils.PerRetryPolicy())
            .buildAsyncClient();

        HttpPipeline pipeline = cryptographyAsyncClient.getHttpPipeline();

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
}
