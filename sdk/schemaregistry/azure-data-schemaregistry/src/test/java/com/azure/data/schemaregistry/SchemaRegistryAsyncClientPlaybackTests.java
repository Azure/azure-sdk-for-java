// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.data.schemaregistry.models.SchemaFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

import static com.azure.data.schemaregistry.SchemaRegistryAsyncClientTests.PLAYBACK_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that can only be played-back because they use a recording from the Portal or a back-compat issue that cannot
 * be reproduced with the latest client.
 */
public class SchemaRegistryAsyncClientPlaybackTests {
    private TokenCredential tokenCredential;
    private String endpoint;
    private TestContextManager testContextManager;
    private InterceptorManager interceptorManager;

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        if (!testInfo.getTestMethod().isPresent()) {
            throw new IllegalStateException(
                "Expected testInfo.getTestMethod() not be empty since we need a method for TestContextManager.");
        }

        this.testContextManager = new TestContextManager(testInfo.getTestMethod().get(), TestMode.PLAYBACK);

        try {
            interceptorManager = new InterceptorManager(testContextManager);
        } catch (UncheckedIOException e) {
            Assertions.fail(e);
        }

        this.tokenCredential = mock(TokenCredential.class);
        this.endpoint = PLAYBACK_ENDPOINT;

        // Sometimes it throws an "NotAMockException", so we had to change from thenReturn to thenAnswer.
        when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
            return Mono.fromCallable(() -> {
                return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
            });
        });
    }

    @AfterEach
    public void teardownTest() {
        if (testContextManager != null && testContextManager.didTestRun()) {
            interceptorManager.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * This is run in playback mode because the GUID is unique for each schema. This is a schema that was previously
     * registered in Azure Portal.
     */
    @Test
    public void getSchemaByIdFromPortal() {
        // Arrange
        final SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace(endpoint)
            .credential(tokenCredential)
            .httpClient(interceptorManager.getPlaybackClient())
            .buildAsyncClient();
        final String schemaId = "f45b841fcb88401e961ca45477906be9";

        // Act & Assert
        StepVerifier.create(client.getSchema(schemaId))
            .assertNext(schema -> {
                assertNotNull(schema.getProperties());
                assertEquals(schemaId, schema.getProperties().getId());
                assertEquals(SchemaFormat.AVRO, schema.getProperties().getFormat());
            })
            .verifyComplete();
    }

    /**
     * Verifies that the new serializer works with 1.0.0 schema registry client.
     * https://search.maven.org/artifact/com.azure/azure-data-schemaregistry/1.0.0/
     */
    @Test
    public void getSchemaBackCompatibility() {
        // Arrange
        final SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace(endpoint)
            .credential(tokenCredential)
            .httpClient(interceptorManager.getPlaybackClient())
            .buildAsyncClient();
        final String schemaId = "e5691f79e3964309ac712ec52abcccca";

        // Act & Assert
        StepVerifier.create(client.getSchema(schemaId))
            .assertNext(schema -> {
                assertNotNull(schema.getProperties());
                assertEquals(schemaId, schema.getProperties().getId());
                assertEquals(SchemaFormat.AVRO, schema.getProperties().getFormat());
            })
            .verifyComplete();
    }
}
