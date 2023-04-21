// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.data.schemaregistry.models.SchemaRegistrySchema;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
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

    // When we regenerate recordings, make sure that the schema group matches what we are persisting.
    static final String PLAYBACK_TEST_GROUP = "mygroup";
    static final String PLAYBACK_ENDPOINT = "https://foo.servicebus.windows.net";

    private String schemaGroup;
    private SchemaRegistryClientBuilder builder;

    @Override
    protected void beforeTest() {
        TokenCredential tokenCredential;
        String endpoint;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = mock(TokenCredential.class);
            schemaGroup = PLAYBACK_TEST_GROUP;

            // Sometimes it throws an "NotAMockException", so we had to change from thenReturn to thenAnswer.
            when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
                return Mono.fromCallable(() -> {
                    return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
                });
            });

            endpoint = PLAYBACK_ENDPOINT;
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(SCHEMA_REGISTRY_ENDPOINT);
            schemaGroup = System.getenv(SCHEMA_REGISTRY_GROUP);

            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
            assertNotNull(schemaGroup, "'schemaGroup' cannot be null in LIVE/RECORD mode.");
        }

        builder = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.addPolicy(new RetryPolicy())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SchemaFormat.AVRO))
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
                assertEquals(SchemaFormat.AVRO, schema.getProperties().getFormat());

                // Replace white space.
                final String actualContents = WHITESPACE_PATTERN.matcher(schema.getDefinition()).replaceAll("");
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
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SchemaFormat.AVRO))
            .assertNext(response -> {
                assertEquals(SchemaFormat.AVRO, response.getFormat());
                assertNotNull(response.getId());
                schemaId.set(response.getId());
            }).verifyComplete();

        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, schemaContentModified, SchemaFormat.AVRO))
            .assertNext(response -> {
                assertEquals(SchemaFormat.AVRO, response.getFormat());
                assertNotNull(response.getId());
                schemaId2.set(response.getId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        assertNotEquals(schemaId.get(), schemaId2.get());

        // Act & Assert
        final String schemaIdToGet = schemaId2.get();
        StepVerifier.create(client2.getSchema(schemaIdToGet))
            .assertNext(schema -> assertSchemaRegistrySchema(schema, schemaIdToGet, SchemaFormat.AVRO, SCHEMA_CONTENT))
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
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT, SchemaFormat.AVRO))
            .assertNext(response -> {
                assertSchemaProperties(response, null, SchemaFormat.AVRO, schemaGroup, schemaName);
                assertEquals(1, response.getVersion());
                schemaId.set(response.getId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client2.getSchemaProperties(schemaGroup, schemaName, SCHEMA_CONTENT, SchemaFormat.AVRO))
            .assertNext(schema -> {
                assertSchemaProperties(schema, schemaIdToGet, SchemaFormat.AVRO, schemaGroup, schemaName);

                // Should be the same version since we did not register a new one.
                assertEquals(1, schema.getVersion());
            })
            .verifyComplete();
    }

    /**
     * Verifies that a 415 is returned if we use an invalid schema format.
     */
    @Test
    public void registerSchemaInvalidFormat() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client = builder.buildAsyncClient();
        final SchemaFormat unknownSchemaFormat = SchemaFormat.fromString("protobuf");

        // Act & Assert
        StepVerifier.create(client.registerSchemaWithResponse(schemaGroup, schemaName, SCHEMA_CONTENT, unknownSchemaFormat))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof HttpResponseException);

                final HttpResponseException responseException = ((HttpResponseException) error);
                assertEquals(415, responseException.getResponse().getStatusCode());
            })
            .verify();
    }

    /**
     * Verifies that if we register a schema and try to fetch it using an invalid schema format, an error is returned.
     */
    @Test
    public void registerAndGetSchemaPropertiesWithInvalidFormat() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();
        final SchemaFormat invalidFormat = SchemaFormat.fromString("protobuf");

        final SchemaProperties schemaProperties = client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT,
            SchemaFormat.AVRO).block(Duration.ofSeconds(10));

        assertNotNull(schemaProperties);

        // Act & Assert
        StepVerifier.create(client2.getSchemaProperties(schemaGroup, schemaName, SCHEMA_CONTENT, invalidFormat))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof HttpResponseException);

                final HttpResponseException responseException = ((HttpResponseException) error);
                assertEquals(415, responseException.getResponse().getStatusCode());
            }).verify();
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
        StepVerifier.create(client1.registerSchema(schemaGroup, schemaName, invalidContent, SchemaFormat.AVRO))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof HttpResponseException);

                final HttpResponseException exception = (HttpResponseException) error;
                assertEquals(400, exception.getResponse().getStatusCode());
            }).verify();
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
        StepVerifier.create(client1.getSchemaProperties(schemaGroup, "bar", SCHEMA_CONTENT, SchemaFormat.AVRO))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ResourceNotFoundException);
                assertEquals(404, ((ResourceNotFoundException) error).getResponse().getStatusCode());
            })
            .verify();
    }

    @Test
    public void getSchemaByGroupNameVersion() {
        // Arrange
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);

        // Register a schema first.
        final SchemaProperties registeredSchema = client1.registerSchema(schemaGroup, schemaName, SCHEMA_CONTENT,
            SchemaFormat.AVRO).block(Duration.ofSeconds(10));

        assertNotNull(registeredSchema);

        // Act & Assert
        StepVerifier.create(client1.getSchema(schemaGroup, schemaName, registeredSchema.getVersion()))
            .assertNext(actual -> {
                SchemaProperties properties = actual.getProperties();
                assertNotNull(properties);

                assertEquals(registeredSchema.getVersion(), properties.getVersion());
                assertEquals(schemaGroup, registeredSchema.getGroupName());
                assertEquals(schemaName, registeredSchema.getName());
                assertEquals(registeredSchema.getId(), registeredSchema.getId());
            })
            .expectComplete()
            .verify();
    }

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
