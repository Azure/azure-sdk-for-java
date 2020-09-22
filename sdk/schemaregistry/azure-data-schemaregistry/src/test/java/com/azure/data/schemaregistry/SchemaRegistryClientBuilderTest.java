// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new SchemaRegistryClientBuilder()
                .endpoint("invalidEndpoint")
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
            .endpoint("https://localhost")
            .buildAsyncClient());
    }
}
