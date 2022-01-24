// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link SchemaRegistryClientBuilder}.
 */
public class SchemaRegistryClientBuilderTest {
    @Test
    public void testNullCredentials() {
        Assertions.assertThrows(NullPointerException.class,
            () -> new SchemaRegistryClientBuilder().buildAsyncClient());
    }

    @Test
    public void testNullEndpoint() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .tenantId("tenant-id")
            .clientId("client-id")
            .clientSecret("client-secret")
            .build();
        Assertions.assertThrows(NullPointerException.class,
            () -> new SchemaRegistryClientBuilder()
                .credential(credential)
                .buildAsyncClient());
    }

    @Test
    public void testInvalidEndpoint() {
        // Arrange
        final TokenCredential credential = mock(TokenCredential.class);

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new SchemaRegistryClientBuilder()
                .credential(credential)
                .fullyQualifiedNamespace("")
                .buildAsyncClient());
    }

    @Test
    public void testSchemaRegistryClientCreation() {

        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .tenantId("tenant-id")
            .clientId("client-id")
            .clientSecret("client-secret")
            .build();

        Assertions.assertNotNull(new SchemaRegistryClientBuilder()
            .credential(credential)
            .fullyQualifiedNamespace("https://localhost")
            .buildAsyncClient());
    }
}
