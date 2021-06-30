// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAccessControlClientBuilderTest {
    private String vaultUrl;
    private String roleDefinitionId;
    private String principalId;
    private KeyVaultAdministrationServiceVersion serviceVersion;

    @BeforeEach
    public void setUp() {
        vaultUrl = "https://key-vault-url.vault.azure.net/";
        roleDefinitionId = "RoleDefinitionId";
        principalId = "PrincipalId";
        serviceVersion = KeyVaultAdministrationServiceVersion.V7_2;
    }

    @Test
    public void buildSyncClientTest() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(), keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void buildSyncClientUsingDefaultApiVersionTest() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .buildClient();

        assertNotNull(keyVaultAccessControlClient);
        assertEquals(KeyVaultAccessControlClient.class.getSimpleName(), keyVaultAccessControlClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientTest() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .serviceVersion(serviceVersion)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(keyVaultAccessControlAsyncClient);
        assertEquals(KeyVaultAccessControlAsyncClient.class.getSimpleName(), keyVaultAccessControlAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void buildAsyncClientUsingDefaultApiVersionTest() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .buildAsyncClient();

        assertNotNull(keyVaultAccessControlAsyncClient);
        assertEquals(KeyVaultAccessControlAsyncClient.class.getSimpleName(), keyVaultAccessControlAsyncClient.getClass().getSimpleName());
    }

    @Test
    public void emptyVaultUrlThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new KeyVaultAccessControlClientBuilder().vaultUrl(""));
    }

    @Test
    public void nullCredentialThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new KeyVaultAccessControlClientBuilder().credential(null));
    }

    @Test
    public void clientOptionsIsPreferredOverLogOptions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .clientOptions(new ClientOptions().setApplicationId("aNewApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("aNewApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () ->
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId, principalId));
    }

    @Test
    public void applicationIdFallsBackToLogOptions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .httpLogOptions(new HttpLogOptions().setApplicationId("anOldApplication"))
            .httpClient(httpRequest -> {
                assertTrue(httpRequest.getHeaders().getValue("User-Agent").contains("anOldApplication"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () ->
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId, principalId));
    }

    @Test
    public void clientOptionHeadersAreAddedLast() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom"))))
            .httpClient(httpRequest -> {
                assertEquals("custom", httpRequest.getHeaders().getValue("User-Agent"));
                return Mono.error(new HttpResponseException(new MockHttpResponse(httpRequest, 400)));
            })
            .buildClient();

        assertThrows(RuntimeException.class, () ->
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId, principalId));
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void addPerCallPolicy() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new TestUtils.TestCredential())
            .addPolicy(new TestUtils.PerCallPolicy())
            .addPolicy(new TestUtils.PerRetryPolicy())
            .buildAsyncClient();

        HttpPipeline pipeline = keyVaultAccessControlAsyncClient.getHttpPipeline();

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
