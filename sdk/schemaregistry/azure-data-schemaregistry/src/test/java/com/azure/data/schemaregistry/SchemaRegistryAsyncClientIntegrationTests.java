package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.data.schemaregistry.implementation.models.ServiceErrorResponseException;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemaRegistryAsyncClientIntegrationTests extends TestBase {
    private static final int RESOURCE_LENGTH = 16;
    private static final String AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME = "AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME";
    private static final String SCHEMA_CONTENT = "{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\", \"type\" : \"int\" }]}";
    private static final String SCHEMA_GROUP = "at";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+", Pattern.MULTILINE);
    private static final String SCHEMA_CONTENT_NO_WHITESPACE = WHITESPACE_PATTERN.matcher(SCHEMA_CONTENT).replaceAll("");

    SchemaRegistryClientBuilder builder;

    @Override
    protected void afterTest() {
        Mockito.framework().clearInlineMocks();
        super.afterTest();
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerAndGetSchema() {
        // Arrange
        initializeBuilder();

        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
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
     * Verifies that we can register a schema and then get it by its schema group, name, and content.
     */
    @Test
    public void registerAndGetSchemaId() {
        // Arrange
        initializeBuilder();

        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();
        final SchemaRegistryAsyncClient client2 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(response -> {
                assertEquals(schemaName, response.getSchemaName());
                assertNotNull(response.getSchemaId());
                schemaId.set(response.getSchemaId());
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client2.getSchemaId(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(schema -> assertEquals(schemaIdToGet, schema))
            .verifyComplete();
    }

    /**
     * Verifies that we can register a schema and then get it by its schemaId.
     */
    @Test
    public void registerBadRequest() {
        // Arrange
        initializeBuilder();

        final String invalidContent = "\"{\"type\" : \"record\",\"namespace\" : \"TestSchema\",\"name\" : \"Employee\",\"fields\" : [{ \"name\" : \"Name\" , \"type\" : \"string\" },{ \"name\" : \"Age\" }]}\"";
        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(SCHEMA_GROUP, schemaName, invalidContent, SerializationType.AVRO))
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
        initializeBuilder();

        final String schemaName = testResourceNamer.randomName("sch", RESOURCE_LENGTH);
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        final AtomicReference<String> schemaId = new AtomicReference<>();

        // Act & Assert
        StepVerifier.create(client1.registerSchema(SCHEMA_GROUP, schemaName, SCHEMA_CONTENT, SerializationType.AVRO))
            .assertNext(response -> {
                assertEquals(schemaName, response.getSchemaName());
                assertNotNull(response.getSchemaId());
                schemaId.set(response.getSchemaId());

                final String contents = new String(response.getSchema(), StandardCharsets.UTF_8);
                assertEquals(SCHEMA_CONTENT, contents);
            }).verifyComplete();

        // Assert that we can get a schema based on its id. We registered a schema with client1 and its response is
        // cached, so it won't make a network call when getting the schema. client2 will not have this information.
        final String schemaIdToGet = schemaId.get();
        assertNotNull(schemaIdToGet);

        // Act & Assert
        StepVerifier.create(client1.getSchema(schemaIdToGet))
            .assertNext(schema -> {
                assertEquals(schemaIdToGet, schema.getSchemaId());
                assertEquals(SerializationType.AVRO, schema.getSerializationType());

                final String contents = new String(schema.getSchema(), StandardCharsets.UTF_8);
                assertEquals(SCHEMA_CONTENT, contents);
            })
            .verifyComplete();
    }

    /**
     * Verifies that we get 404 when non-existent schema returned.
     */
    @Test
    public void getSchemaDoesNotExist() {
        // Arrange
        initializeBuilder();

        final String schemaId = "59f112cf-ff02-40e6-aca9-0d30ed7f7f94";
        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client1.getSchema(schemaId))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ResourceNotFoundException);
                assertEquals(404, ((ResourceNotFoundException)error).getResponse().getStatusCode());
            })
            .verify();
    }

    /**
     * Verifies that we get 404 when non-existent schema query is returned.
     */
    @Test
    public void getSchemaIdDoesNotExist() {
        // Arrange
        initializeBuilder();

        final SchemaRegistryAsyncClient client1 = builder.buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client1.getSchemaId("at", "bar", SCHEMA_CONTENT, SerializationType.AVRO))
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof ResourceNotFoundException);
                assertEquals(404, ((ResourceNotFoundException)error).getResponse().getStatusCode());
            })
            .verify();
    }

    void initializeBuilder() {
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
}
