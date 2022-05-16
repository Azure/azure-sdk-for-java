// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.Person;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCard;
import com.azure.data.schemaregistry.apacheavro.generatedtestsources.PlayingCardSuit;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import org.apache.avro.Schema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchemaRegistrySchemaCache}.
 */
public class SchemaRegistrySchemaCacheTest {
    private static final String SCHEMA_GROUP = "mock-schema-group";
    private static final String SCHEMA_ID = "mock-schema-id";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final Schema schema = Person.getClassSchema();
    private final String schemaString = schema.toString();
    private final String schemaName = schema.getFullName();

    @Mock
    private SchemaRegistryAsyncClient client;

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
    public void getSchemaId() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.AVRO))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.AVRO)));

        // Act
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Tests that it fetches a schema but uses the cached version.  Also, checks it works for getSchema call.
     */
    @Test
    public void getSchemaIdCachedVersion() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.AVRO))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.AVRO)));

        // Act & Assert
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Fetch the second time. Should use cached version.
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Should use the cached version we just fetched even though we're using id.
        StepVerifier.create(cache.getSchema(SCHEMA_ID))
            .expectNext(schema)
            .expectComplete()
            .verify(TIMEOUT);

        // We only want one invocation.
        verify(client).getSchemaProperties(anyString(), anyString(), anyString(), eq(SchemaFormat.AVRO));

        assertEquals(1, cache.getSize());
        assertEquals(schemaString.length(), cache.getTotalLength());
    }

    /**
     * Verifies that an error is returned if no schema group is set.
     */
    @Test
    public void getSchemaIdNoSchemaGroup() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, null,
            autoRegisterSchemas, capacity);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.AVRO))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.AVRO)));

        // Act
        StepVerifier.create(cache.getSchemaId(schema))
            .expectError(IllegalStateException.class)
            .verify(TIMEOUT);
    }

    /**
     * Verify that auto-register schemas is possible.
     */
    @Test
    public void getSchemaIdAutoRegisterSchema() {
        // Arrange
        final boolean autoRegisterSchemas = true;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.registerSchema(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.AVRO))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.AVRO)));

        // Act
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that auto-register schemas is used. Verifies that subsequent calls to getSchemaId and getSchema use the
     * cached version.
     */
    @Test
    public void getSchemaIdAutoRegisterSchemaCached() {
        // Arrange
        final boolean autoRegisterSchemas = true;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        when(client.registerSchema(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.AVRO))
            .thenReturn(Mono.just(new SchemaProperties(SCHEMA_ID, SchemaFormat.AVRO)));

        // Act
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Fetch the second time. Should use cached version.
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Verify that this also works for the other network call.
        StepVerifier.create(cache.getSchema(SCHEMA_ID))
            .expectNext(schema)
            .expectComplete()
            .verify(TIMEOUT);

        // We only want one invocation.
        verify(client).registerSchema(anyString(), anyString(), anyString(), eq(SchemaFormat.AVRO));

        verify(client, never()).getSchemaProperties(anyString(), anyString(), anyString(), eq(SchemaFormat.AVRO));

        assertEquals(1, cache.getSize());
        assertEquals(schemaString.length(), cache.getTotalLength());
    }

    /**
     * Verifies that we can fetch a schema id, and if the id is queried again, we use the cached one instead of another
     * network call.
     */
    @Test
    public void getSchema() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);
        final SchemaProperties registryProperties = new SchemaProperties(SCHEMA_ID, SchemaFormat.AVRO);

        when(client.getSchemaProperties(SCHEMA_GROUP, schemaName, schemaString, SchemaFormat.AVRO))
            .thenReturn(Mono.just(registryProperties));

        // Act
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // Fetch the second time. Should use cached version.
        StepVerifier.create(cache.getSchemaId(schema))
            .expectNext(SCHEMA_ID)
            .expectComplete()
            .verify(TIMEOUT);

        // We only want one invocation.
        verify(client).getSchemaProperties(anyString(), anyString(), anyString(), eq(SchemaFormat.AVRO));

        verify(client, never()).registerSchema(anyString(), anyString(), anyString(), eq(SchemaFormat.AVRO));
    }

    /**
     * Verifies that old cache entries are rotated out.
     */
    @Test
    public void removesOldEntries() {
        // Arrange
        final boolean autoRegisterSchemas = false;
        final int capacity = 3;
        final SchemaRegistrySchemaCache cache = new SchemaRegistrySchemaCache(client, SCHEMA_GROUP,
            autoRegisterSchemas, capacity);

        final Schema schema1 = Person.getClassSchema();
        final Schema schema2 = PlayingCard.getClassSchema();
        final Schema schema3 = PlayingCardSuit.getClassSchema();

        final Schema.Field name = new Schema.Field("name", Schema.create(Schema.Type.STRING), "Name of school");
        final Schema.Field year = new Schema.Field("year", Schema.create(Schema.Type.INT), "Name of school", 1900);

        final Schema schema4 = Schema.createRecord("School", "School schema", "com.test",
            false, Arrays.asList(name, year));

        final String schemaId1 = "schema-id1";
        final String schemaId2 = "schema-id2";
        final String schemaId3 = "schema-id3";
        final String schemaId4 = "schema-id4";

        final HashSet<String> emittedSchemas = new HashSet<>();

        when(client.getSchemaProperties(eq(SCHEMA_GROUP), anyString(), anyString(), eq(SchemaFormat.AVRO)))
            .thenAnswer(invocation -> {
                final String schemaName = invocation.getArgument(1);
                final SchemaFormat format = invocation.getArgument(3);

                final String schemaIdToReturn;
                if (schema1.getFullName().equals(schemaName)) {
                    schemaIdToReturn = schemaId1;
                } else if (schema2.getFullName().equals(schemaName)) {
                    schemaIdToReturn = schemaId2;
                } else if (schema3.getFullName().equals(schemaName)) {
                    schemaIdToReturn = schemaId3;
                } else if (schema4.getFullName().equals(schemaName)) {
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

        StepVerifier.create(cache.getSchemaId(schema1))
            .expectNext(schemaId1)
            .verifyComplete();
        StepVerifier.create(cache.getSchemaId(schema2))
            .expectNext(schemaId2)
            .verifyComplete();
        StepVerifier.create(cache.getSchemaId(schema3))
            .expectNext(schemaId3)
            .verifyComplete();

        // Should be at capacity now.
        assertEquals(capacity, cache.getSize());

        final int expectedLength = schema1.toString().length() + schema2.toString().length() + schema3.toString().length();
        assertEquals(expectedLength, cache.getTotalLength());

        // Get schema1 so it is no longer the eldest.
        StepVerifier.create(cache.getSchemaId(schema1))
            .expectNext(schemaId1)
            .verifyComplete();

        // Schema2 should be removed after this because it is the oldest.
        StepVerifier.create(cache.getSchemaId(schema4))
            .expectNext(schemaId4)
            .verifyComplete();

        assertEquals(capacity, cache.getSize());

        final int expectedLength2 = schema1.toString().length() + schema3.toString().length() + schema4.toString().length();
        assertEquals(expectedLength2, cache.getTotalLength());
    }
}
