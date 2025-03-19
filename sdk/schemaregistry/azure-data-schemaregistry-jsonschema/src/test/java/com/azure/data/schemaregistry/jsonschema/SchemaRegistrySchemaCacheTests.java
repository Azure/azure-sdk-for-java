// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchemaRegistrySchemaCacheTests {
    private static final String SCHEMA_GROUP = "mock-schema-group";
    private static final String SCHEMA_ID = "mock-schema-id";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final String schemaString = Address.JSON_SCHEMA;
    private final String schemaName = Address.class.getName();

    @Mock
    private SchemaRegistryAsyncClient client;
    @Mock
    private SchemaRegistryClient schemaRegistryClient;

    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void beforeEach() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Tests that it fetches a schema.
     */
    @Test
    public void getSchemaAsyncIdDefinitionAsync() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.JSON))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.JSON)));

        // Act
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Tests that it fetches a schema but uses the cached version.  Also, checks it works for getSchema call.
     */
    @Test
    public void getSchemaAsyncIdDefinitionAsyncCachedVersion() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.JSON))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.JSON)));

        // Act & Assert
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Fetch the second time. Should use cached version.
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Should use the cached version we just fetched even though we're using id.
        StepVerifier.create(cache.getSchemaDefinitionAsync(SCHEMA_ID))
            .expectNext(schemaString)
            .expectComplete()
            .verify(TIMEOUT);

        // We only want one invocation.
        verify(client).getSchemaProperties(anyString(), anyString(), anyString(), eq(SchemaFormat.JSON));

        assertEquals(1, cache.getSize());
        assertEquals(schemaString.length(), cache.getTotalLength());
    }

    /**
     * Verifies that an error is returned if no schema group is set.
     */
    @Test
    public void getSchemaIdNoSchemaAsyncGroupAsync() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, null,
            autoRegisterSchemas, capacity);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.JSON))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.JSON)));

        // Act
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectError(IllegalStateException.class)
            .verify(TIMEOUT);
    }

    /**
     * Verify that auto-register schemas is possible.
     */
    @Test
    public void getSchemaIdAutoRegisterSchemaAsyncAsync() {
        // Arrange
        final boolean autoRegisterSchemas = true;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.registerSchema(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.JSON))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.JSON)));

        // Act
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that auto-register schemas is used. Verifies that subsequent calls to getSchemaId and getSchema use the
     * cached version.
     */
    @Test
    public void getSchemaIdAutoRegisterSchemaAsyncCachedAsync() {
        // Arrange
        final boolean autoRegisterSchemas = true;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.registerSchema(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.JSON))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.JSON)));

        // Act
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Fetch the second time. Should use cached version.
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Verify that this also works for the other network call.
        StepVerifier.create(cache.getSchemaDefinitionAsync(SCHEMA_ID))
            .expectNext(schemaString)
            .expectComplete()
            .verify(TIMEOUT);

        // We only want one invocation.
        verify(client).registerSchema(anyString(), anyString(), anyString(), eq(SchemaFormat.JSON));

        verify(client, never()).getSchemaProperties(anyString(), anyString(), anyString(), eq(SchemaFormat.JSON));

        assertEquals(1, cache.getSize());
        assertEquals(schemaString.length(), cache.getTotalLength());
    }

    /**
     * Verifies that we can fetch a schema id, and if the id is queried again, we use the cached one instead of another
     * network call.
     */
    @Test
    public void getSchemaDefinitionAsync() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);
        final SchemaProperties registryProperties = new SchemaProperties(SCHEMA_ID, SchemaFormat.JSON);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.JSON))
            .thenReturn(Mono.just(registryProperties));

        // Act
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Fetch the second time. Should use cached version.
        StepVerifier.create(cache.getSchemaIdAsync(schemaName, schemaString))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // We only want one invocation.
        verify(client).getSchemaProperties(anyString(), anyString(), anyString(), eq(SchemaFormat.JSON));

        verify(client, never()).registerSchema(anyString(), anyString(), anyString(), eq(SchemaFormat.JSON));
    }

    /**
     * Verifies that old cache entries are rotated out.
     */
    @Test
    public void removesOldEntries() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, schemaRegistryClient, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        final String schemaName1 = "name1";
        final String schemaName2 = "name2";
        final String schemaName3 = "name3";
        final String schemaName4 = "name4";

        final String schema1 = "the-first-schema-1";
        final String schema2 = "the-second-schema-11";
        final String schema3 = "the-third-schema-111";

        final String schema4 = "the-fourth-schema-1111";

        final String schemaId1 = "schema-id1";
        final String schemaId2 = "schema-id2";
        final String schemaId3 = "schema-id3";
        final String schemaId4 = "schema-id4";

        final HashSet<String> emittedSchemas = new HashSet<>();

        when(client.getSchemaProperties(eq(SCHEMA_GROUP), anyString(), anyString(), eq(SchemaFormat.JSON)))
            .thenAnswer(invocation -> {
                final String schemaName = invocation.getArgument(1);
                final SchemaFormat format = invocation.getArgument(3);

                final String schemaIdToReturn;

                if (schemaName1.equals(schemaName)) {
                    schemaIdToReturn = schemaId1;
                } else if (schemaName2.equals(schemaName)) {
                    schemaIdToReturn = schemaId2;
                } else if (schemaName3.equals(schemaName)) {
                    schemaIdToReturn = schemaId3;
                } else if (schemaName4.equals(schemaName)) {
                    schemaIdToReturn = schemaId4;
                } else {
                    return Mono.error(new IllegalArgumentException("Did not match any known names. Name:" + schemaName));
                }

                if (emittedSchemas.contains(schemaIdToReturn)) {
                    return Mono.error(new IllegalStateException("Should not have to fetch schema again. Id:" + schemaIdToReturn));
                }

                emittedSchemas.add(schemaIdToReturn);
                return Mono.just(new SchemaProperties(schemaIdToReturn, format));
            });

        StepVerifier.create(cache.getSchemaIdAsync(schemaName1, schema1))
            .expectNext(schemaId1)
            .verifyComplete();
        StepVerifier.create(cache.getSchemaIdAsync(schemaName2, schema2))
            .expectNext(schemaId2)
            .verifyComplete();
        StepVerifier.create(cache.getSchemaIdAsync(schemaName3, schema3))
            .expectNext(schemaId3)
            .verifyComplete();

        // Should be at capacity now.
        assertEquals(capacity, cache.getSize());

        final int expectedLength = schema1.length() + schema2.length() + schema3.length();
        assertEquals(expectedLength, cache.getTotalLength());

        // Get schema1 so it is no longer the eldest.
        StepVerifier.create(cache.getSchemaIdAsync(schemaName1, schema1))
            .expectNext(schemaId1)
            .verifyComplete();

        // Schema2 should be removed after this because it is the oldest.
        StepVerifier.create(cache.getSchemaIdAsync(schemaName4, schema4))
            .expectNext(schemaId4)
            .verifyComplete();

        assertEquals(capacity, cache.getSize());

        final int expectedLength2 = schema1.length() + schema3.length() + schema4.length();
        assertEquals(expectedLength2, cache.getTotalLength());
    }
}
