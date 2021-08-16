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

import java.time.OffsetDateTime;

import static com.azure.data.schemaregistry.SchemaRegistryAsyncClientTests.AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME;
import static com.azure.data.schemaregistry.SchemaRegistryAsyncClientTests.RESOURCE_LENGTH;
import static com.azure.data.schemaregistry.SchemaRegistryAsyncClientTests.SCHEMA_CONTENT;
import static com.azure.data.schemaregistry.SchemaRegistryAsyncClientTests.SCHEMA_GROUP;
import static com.azure.data.schemaregistry.SchemaRegistryAsyncClientTests.assertSchemaProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SchemaRegistryClient}.
 */
public class SchemaRegistryClientTests extends TestBase {

    private SchemaRegistryClientBuilder builder;

    @Override
    protected void beforeTest() {
        final String endpoint;
        TokenCredential tokenCredential;
        if (interceptorManager.isPlaybackMode()) {
            tokenCredential = mock(TokenCredential.class);
            when(tokenCredential.getToken(any(TokenRequestContext.class)))
                .thenReturn(Mono.fromCallable(() -> {
                    return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
                }));
            endpoint = "https://foo.servicebus.windows.net";
        } else {
            tokenCredential = new DefaultAzureCredentialBuilder().build();
            endpoint = System.getenv(AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME);

            assertNotNull(endpoint, "'endpoint' cannot be null in LIVE/RECORD mode.");
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
        Mockito.framework().clearInlineMocks();
    }


    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerAndGetSchema() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryClient client1 = builder.buildClient();
        final SchemaRegistryClient client2 = builder.buildClient();

        // Act
        final SchemaProperties response = client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT, SerializationType.AVRO);

        // Assert
        assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = response.getSchemaId();

        // Act
        final SchemaProperties schema1 = client2.getSchema(schemaIdToGet);

        // Assert
        assertSchemaProperties(schema1, schemaIdToGet, schemaName, SCHEMA_CONTENT);
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
        final SchemaRegistryClient client1 = builder.buildClient();
        final SchemaRegistryClient client2 = builder.buildClient();

        // Act & Assert
        final SchemaProperties response = client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT,
            SerializationType.AVRO);
        assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);

        // Expected that the second time we call this method, it will return a different schema because the contents
        // are different.
        final SchemaProperties response2 = client1.registerSchema(SCHEMA_GROUP, schemaName, schemaContentModified,
            SerializationType.AVRO);
        assertSchemaProperties(response2, null, schemaName, schemaContentModified);

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        assertNotEquals(response.getSchemaId(), response2.getSchemaId());

        // Act & Assert
        final SchemaProperties response3 = client2.getSchema(response2.getSchemaId());
        assertSchemaProperties(response3, response2.getSchemaId(), schemaName, schemaContentModified);
    }

    /**
     * Verifies that we can register a schema and then get it by its schema group, name, and content.
     */
    @Test
    public void registerAndGetSchemaId() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryClient client1 = builder.buildClient();
        final SchemaRegistryClient client2 = builder.buildClient();

        // Act & Assert
        final SchemaProperties response = client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT,
            SerializationType.AVRO);
        assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = response.getSchemaId();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        final String schemaId = client2.getSchemaId(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT, SerializationType.AVRO);
        assertEquals(schemaIdToGet, schemaId);
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerBadRequest() {
        // Arrange
        final String invalidContent = "\"{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\" }]}\"";
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryClient client1 = builder.buildClient();

        // Act
        final ServiceErrorResponseException exception = assertThrows(ServiceErrorResponseException.class,
            () -> client1.registerSchema(SCHEMA_GROUP, schemaName, invalidContent, SerializationType.AVRO));

        // Assert
        assertEquals(400, exception.getResponse().getStatusCode());
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerAndGetCachedSchema() {
        // Arrange
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryClient client1 = builder.buildClient();

        // Act & Assert
        final SchemaProperties response = client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT,
            SerializationType.AVRO);
        assertSchemaProperties(response, null, schemaName, SCHEMA_CONTENT);

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema.
        final String schemaIdToGet = response.getSchemaId();

        // Act & Assert
        final SchemaProperties response2 = client1.getSchema(schemaIdToGet);
        assertSchemaProperties(response2, schemaIdToGet, schemaName, SCHEMA_CONTENT);
    }

    /**
     * Verifies that we get 404 when non-existent schema returned.
     */
    @Test
    public void getSchemaDoesNotExist() {
        // Arrange
        final String schemaId = "59f112cf-ff02-40e6-aca9-0d30ed7f7f94";
        final SchemaRegistryClient client1 = builder.buildClient();

        // Act & Assert
        final ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class,
            () -> client1.getSchema(schemaId));

        assertEquals(404, error.getResponse().getStatusCode());
    }

    /**
     * Verifies that we get 404 when non-existent schema query is returned.
     */
    @Test
    public void getSchemaIdDoesNotExist() {
        // Arrange
        final SchemaRegistryClient client1 = builder.buildClient();

        // Act & Assert
        final ResourceNotFoundException error = assertThrows(ResourceNotFoundException.class,
            () -> client1.getSchemaId("at", "bar", SCHEMA_CONTENT, SerializationType.AVRO));

        assertEquals(404, error.getResponse().getStatusCode());
    }
}
