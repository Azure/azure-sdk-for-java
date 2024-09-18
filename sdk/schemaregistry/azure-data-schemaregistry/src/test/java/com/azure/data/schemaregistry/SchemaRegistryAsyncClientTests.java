// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.data.schemaregistry.models.SchemaProperties;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;

import static com.azure.data.schemaregistry.Constants.PLAYBACK_ENDPOINT;
import static com.azure.data.schemaregistry.Constants.PLAYBACK_TEST_GROUP;
import static com.azure.data.schemaregistry.Constants.RESOURCE_LENGTH;
import static com.azure.data.schemaregistry.Constants.SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE;
import static com.azure.data.schemaregistry.Constants.SCHEMA_REGISTRY_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SchemaFormat#AVRO} using {@link SchemaRegistryAsyncClient}.
 */
public class SchemaRegistryAsyncClientTests extends TestProxyTestBase {
    static final String SCHEMA_CONTENT = "{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\", \"type\" : \"int\" }]}";

    private String schemaGroup;
    private SchemaRegistryClientBuilder builder;
    private SchemaRegistryAsyncClientTestsBase testBase;

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
            endpoint = System.getenv(SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE);
            schemaGroup = System.getenv(SCHEMA_REGISTRY_GROUP);

            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
            assertNotNull(schemaGroup, "'schemaGroup' cannot be null in LIVE/RECORD mode.");
        }

        builder = new SchemaRegistryClientBuilder()
            .credential(tokenCredential)
            .fullyQualifiedNamespace(endpoint);

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(buildAsyncAssertingClient(interceptorManager.getPlaybackClient()));
        } else if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        testBase = new SchemaRegistryAsyncClientTestsBase(schemaGroup, SchemaFormat.AVRO);
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .skipRequest((httpRequest, context) -> false)
            .build();
    }

    @Override
    protected void afterTest() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    @Disabled("Can't apply sanitizer in the tests, disable this test temperately for patch release")
    public void registerAndGetSchema() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();

        // Act & Assert
        testBase.registerAndGetSchema(client1, client2, schemaName, SCHEMA_CONTENT);
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

        // Act & Assert
        testBase.registerAndGetSchemaTwice(client1, client2, schemaName, SCHEMA_CONTENT, schemaContentModified);
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

        // Act & Assert
        testBase.registerAndGetSchemaId(client1, client2, schemaName, SCHEMA_CONTENT);
    }

    /**
     * Verifies that a 4xx is returned if we use an invalid schema format.
     */
    @Test
    @RecordWithoutRequestBody
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
    @RecordWithoutRequestBody
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
                assertEquals(404, responseException.getResponse().getStatusCode());
            }).verify();
    }

    /**
     * Verifies that an error is returned if we try to register an invalid schema.
     */
    @Test
    public void registerBadRequest() {
        // Arrange
        final String invalidContent = "\"{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\" }]}\"";
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        testBase.registerBadRequest(client1, schemaName, invalidContent);
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
    @Disabled
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

}
