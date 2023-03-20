// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.exception.HttpResponseException;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class with common tests.
 */
class SchemaRegistryAsyncClientTestsBase {
    static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);

    private final String schemaGroup;
    private final SchemaFormat schemaFormat;

    SchemaRegistryAsyncClientTestsBase(String schemaGroup, SchemaFormat schemaFormat) {
        this.schemaGroup = schemaGroup;
        this.schemaFormat = schemaFormat;
    }

    /**
     * 1. Registers a schema
     * 2. Gets the schema
     * 3. Verifies that the contents are the same.
     */
    void registerAndGetSchema(SchemaRegistryAsyncClient client1, SchemaRegistryAsyncClient client2,
        String schemaName, String schemaContent) {

        // Arrange
        final String schemaContentNoWhitespace = WHITESPACE_PATTERN.matcher(schemaContent).replaceAll("");
        final AtomicReference<String> schemaId = new AtomicReference<>();


        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, schemaContent, schemaFormat))
            .assertNext(response -> {
                assertNotNull(response.getId());
                schemaId.set(response.getId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client2.getSchema(schemaIdToGet))
            .assertNext(schema -> {
                assertNotNull(schema.getProperties());
                assertEquals(schemaIdToGet, schema.getProperties().getId());
                assertEquals(schemaFormat, schema.getProperties().getFormat());

                // Replace white space.
                final String actualContents = WHITESPACE_PATTERN.matcher(schema.getDefinition()).replaceAll("");
                assertEquals(schemaContentNoWhitespace, actualContents);
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId. Then add another version of it, and get
     * that version.
     */
    void registerAndGetSchemaTwice(SchemaRegistryAsyncClient client1, SchemaRegistryAsyncClient client2,
        String schemaName, String schemaContent, String schemaContentModified) {

        final AtomicReference<String> schemaId = new AtomicReference<>();
        final AtomicReference<String> schemaId2 = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, schemaContent, schemaFormat))
            .assertNext(response -> {
                assertEquals(schemaFormat, response.getFormat());
                assertNotNull(response.getId());
                schemaId.set(response.getId());
            }).verifyComplete();

        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, schemaContentModified, schemaFormat))
            .assertNext(response -> {
                assertEquals(schemaFormat, response.getFormat());
                assertNotNull(response.getId());
                schemaId2.set(response.getId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        assertNotEquals(schemaId.get(), schemaId2.get());

        // Act & Assert
        final String schemaIdToGet = schemaId2.get();
        StepVerifier.create(client2.getSchema(schemaIdToGet))
            .assertNext(schema -> assertSchemaRegistrySchema(schema, schemaIdToGet, schemaFormat, schemaContent))
            .verifyComplete();
    }

    /**
     * Verifies that we can register a schema and then get it by its schema group, name, and content.
     */
    void registerAndGetSchemaId(SchemaRegistryAsyncClient client1, SchemaRegistryAsyncClient client2,
        String schemaName, String schemaContent) {
        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, schemaContent, schemaFormat))
            .assertNext(response -> {
                assertSchemaProperties(response, null, schemaFormat, schemaGroup, schemaName);
                assertEquals(1, response.getVersion());
                schemaId.set(response.getId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client2.getSchemaProperties(schemaGroup, schemaName, schemaContent, schemaFormat))
            .assertNext(schema -> {
                assertSchemaProperties(schema, schemaIdToGet, schemaFormat, schemaGroup, schemaName);

                // Should be the same version since we did not register a new one.
                assertEquals(1, schema.getVersion());
            })
            .verifyComplete();
    }

    /**
     * Verifies that an error is returned if we try to register an invalid schema.
     */
    void registerBadRequest(SchemaRegistryAsyncClient client1, String schemaName, String invalidContent) {
        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, invalidContent, schemaFormat))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof HttpResponseException);

                final HttpResponseException exception = (HttpResponseException) error;
                assertEquals(400, exception.getResponse().getStatusCode());
            }).verify();
    }

    /**
     * Verifies the {@link SchemaRegistrySchema}.
     *
     * @param actual Result from service.
     * @param expectedSchemaId Expected schema id.
     * @param format Expected schema format.
     * @param expectedContents Expected contents.
     */
    static void assertSchemaRegistrySchema(SchemaRegistrySchema actual, String expectedSchemaId, SchemaFormat format,
        String expectedContents) {

        assertNotEquals(expectedContents, "'expectedContents' should not be null.");

        assertEquals(format, actual.getProperties().getFormat());

        assertNotNull(actual.getProperties().getId());

        if (expectedSchemaId != null) {
            assertEquals(expectedSchemaId, actual.getProperties().getId());
        }

        // Replace white space.
        final String actualContents = WHITESPACE_PATTERN.matcher(actual.getDefinition()).replaceAll("");
        final String expectedContentsNoWhitespace = WHITESPACE_PATTERN.matcher(actualContents).replaceAll("");

        assertEquals(expectedContentsNoWhitespace, actualContents);
    }

    /**
     * Verifies the {@link SchemaProperties}.
     *
     * @param actual Result from service.
     * @param expectedSchemaId Expected schema id.
     * @param schemaFormat Expected schema format.
     * @param schemaGroup Expected group.
     * @param schemaName  Expected schema name.
     */
    static void assertSchemaProperties(SchemaProperties actual, String expectedSchemaId, SchemaFormat schemaFormat,
        String schemaGroup, String schemaName) {
        assertNotNull(actual);

        if (expectedSchemaId != null) {
            assertEquals(expectedSchemaId, actual.getId());
        }

        assertEquals(schemaGroup, actual.getGroupName());
        assertEquals(schemaName, actual.getName());
        assertEquals(schemaFormat, actual.getFormat());
    }

}
