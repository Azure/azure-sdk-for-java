// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.schemaregistry.implementation.models.ServiceErrorResponseException;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SerializationType;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchemaRegistryAsyncClient}.
 */
public class SchemaRegistryAsyncClientTests extends TestBase {
    static final int RESOURCE_LENGTH = 16;
    static final String SCHEMA_REGISTRY_ENDPOINT = "SCHEMA_REGISTRY_ENDPOINT";
    static final String SCHEMA_REGISTRY_GROUP = "SCHEMA_REGISTRY_GROUP";
    static final String SCHEMA_CONTENT = "{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\", \"type\" : \"int\" }]}";
    static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);
    static final String SCHEMA_CONTENT_NO_WHITESPACE = WHITESPACE_PATTERN.matcher(SCHEMA_CONTENT).replaceAll("");

    private String schemaGroup;
    private SchemaRegistryClientBuilder builder;

    @Override
    protected void beforeTest() {
        final String endpoint;
        TokenCredential tokenCredential;

        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = mock(TokenCredential.class);
            schemaGroup = "at";

            // Sometimes it throws an "NotAMockException", so we had to change from thenReturn to thenAnswer.
            when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
                return Mono.fromCallable(() -> {
                    return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
                });
            });

            endpoint = "https://foo.servicebus.windows.net";
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(SCHEMA_REGISTRY_ENDPOINT);
            schemaGroup = System.getenv(SCHEMA_REGISTRY_GROUP);

            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
            assertNotNull(schemaGroup, "'schemaGroup' cannot be null in LIVE/RECORD mode.");
        }

        builder = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .endpoint(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.addPolicy(new RetryPolicy())
                .addPolicy(interceptorManager.getRecordPolicy());
        }
    }

    @Override
    protected void afterTest() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerAndGetSchema() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(response -> {
                assertEquals(schemaName, response.getSchemaName());
                assertNotNull(response.getSchemaId());
                schemaId.set(response.getSchemaId());

                // Replace white space.
                final String contents = new String(response.getSchema(), StandardCharsets.UTF_8);
                final String actualContents = WHITESPACE_PATTERN.matcher(contents).replaceAll("");
                assertEquals(SCHEMA_CONTENT_NO_WHITESPACE, actualContents);
            }).verifyComplete();


        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client2.getSchema(schemaIdToGet))
            .assertNext(schema -> {
                assertEquals(schemaIdToGet, schema.getSchemaId());
                assertEquals(SerializationType.AVRO, schema.getSerializationType());

                // Replace white space.
                final String contents = new String(schema.getSchema(), StandardCharsets.UTF_8);
                final String actualContents = WHITESPACE_PATTERN.matcher(contents).replaceAll("");
                assertEquals(SCHEMA_CONTENT_NO_WHITESPACE, actualContents);
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId. Then add another version of it, and get
     * that version.
     */
    @Test
    public void registerAndGetSchemaTwice() {
        // Arrange
        final String schemaContentModified = "{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\", \"type\" : \"int\" },{ \"name\" : \"Sign\", \"type\" : \"string\" }]}";
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();
        final AtomicReference<String> schemaId2 = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(response -> {
                assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);
                schemaId.set(response.getSchemaId());
            }).verifyComplete();

        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, schemaContentModified, SerializationType.AVRO))
            .assertNext(response -> {
                assertSchemaProperties(response, null, schemaName, schemaContentModified);
                schemaId2.set(response.getSchemaId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        assertNotEquals(schemaId.get(), schemaId2.get());

        // Act & Assert
        final String schemaIdToGet = schemaId2.get();
        StepVerifier.create(client2.getSchema(schemaIdToGet))
            .assertNext(schema -> assertSchemaProperties(schema, schemaIdToGet, schemaName, SCHEMA_CONTENT))
            .verifyComplete();
    }

    /**
     * Verifies that we can register a schema and then get it by its schema group, name, and content.
     */
    @Test
    public void registerAndGetSchemaId() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(response -> {
                assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);
                schemaId.set(response.getSchemaId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client2.getSchemaId(schemaGroup, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(schema -> assertEquals(schemaIdToGet, schema))
            .verifyComplete();
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerBadRequest() {
        // Arrange
        final String invalidContent = "\"{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\" }]}\"";
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, invalidContent, SerializationType.AVRO))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ServiceErrorResponseException);

                final ServiceErrorResponseException exception = (ServiceErrorResponseException) error;
                assertEquals(400, exception.getResponse().getStatusCode());
            }).verify();
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerAndGetCachedSchema() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(response -> {
                assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);
                schemaId.set(response.getSchemaId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client1.getSchema(schemaIdToGet))
            .assertNext(schema -> assertSchemaProperties(schema, schemaIdToGet, schemaName, SCHEMA_CONTENT))
            .verifyComplete();
    }

    /**
     * Verifies that we get 404 when non-existent schema returned.
     */
    @Test
    public void getSchemaDoesNotExist() {
        // Arrange
        final String schemaId = "59f112cf-ff02-40e6-aca9-0d30ed7f7f94";
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client1.getSchema(schemaId))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ResourceNotFoundException);
                assertEquals(404, ((ResourceNotFoundException) error).getResponse().getStatusCode());
            })
            .verify();
    }

    /**
     * Verifies that we get 404 when non-existent schema query is returned.
     */
    @Test
    public void getSchemaIdDoesNotExist() {
        // Arrange
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client1.getSchemaId("at", "bar", SCHEMA_CONTENT, SerializationType.AVRO))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ResourceNotFoundException);
                assertEquals(404, ((ResourceNotFoundException) error).getResponse().getStatusCode());
            })
            .verify();
    }

    static void assertSchemaProperties(SchemaProperties actual, String expectedSchemaId, String expectedSchemaName,
        String expectedContents) {

        assertNotEquals(expectedContents, "'expectedContents' should not be null.");

        assertEquals(expectedSchemaName, actual.getSchemaName());
        assertEquals(SerializationType.AVRO, actual.getSerializationType());

        assertNotNull(actual.getSchemaId());

        if (expectedSchemaId != null) {
            assertEquals(expectedSchemaId, actual.getSchemaId());
        }

        // Replace white space.
        final String contents = new String(actual.getSchema(), StandardCharsets.UTF_8);
        final String actualContents = WHITESPACE_PATTERN.matcher(contents).replaceAll("");

        final String expectedContentsNoWhitespace = WHITESPACE_PATTERN.matcher(actualContents).replaceAll("");
        assertEquals(expectedContentsNoWhitespace, actualContents);
    }
}
