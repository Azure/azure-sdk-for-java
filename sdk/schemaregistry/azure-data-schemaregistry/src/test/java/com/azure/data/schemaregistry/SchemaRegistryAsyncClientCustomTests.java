// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.data.schemaregistry.Constants.PLAYBACK_ENDPOINT;
import static com.azure.data.schemaregistry.Constants.PLAYBACK_TEST_GROUP;
import static com.azure.data.schemaregistry.Constants.RESOURCE_LENGTH;
import static com.azure.data.schemaregistry.Constants.SCHEMA_REGISTRY_CUSTOM_FULLY_QUALIFIED_NAMESPACE;
import static com.azure.data.schemaregistry.Constants.SCHEMA_REGISTRY_GROUP;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SchemaRegistryAsyncClientCustomTests extends TestProxyTestBase {
    static final String SCHEMA_CONTENT = "Person: int, string, decimal";

    private SchemaRegistryClientBuilder builder;
    private SchemaRegistryAsyncClientTestsBase testBase;

    @Override
    protected void beforeTest() {
        TokenCredential tokenCredential;
        String endpoint;
        String schemaGroup;
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
            endpoint = System.getenv(SCHEMA_REGISTRY_CUSTOM_FULLY_QUALIFIED_NAMESPACE);
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

        testBase = new SchemaRegistryAsyncClientTestsBase(schemaGroup, SchemaFormat.CUSTOM);
    }

    @Override
    protected void afterTest() {
        Mockito.framework().clearInlineMock(this);
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .skipRequest((httpRequest, context) -> false)
            .build();
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
        final String schemaContentModified = "Person: int, string, decimal, string";
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
}
