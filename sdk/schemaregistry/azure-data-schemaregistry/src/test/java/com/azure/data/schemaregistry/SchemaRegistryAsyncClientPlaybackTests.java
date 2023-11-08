// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.data.schemaregistry.models.SchemaFormat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;

import static com.azure.data.schemaregistry.Constants.PLAYBACK_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that can only be played-back because they use a recording from the Portal or a back-compat issue that cannot be
 * reproduced with the latest client.
 */
public class SchemaRegistryAsyncClientPlaybackTests {
    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();
    private TokenCredential tokenCredential;
    private String endpoint;
    private RecordedData recordedData;

    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        if (!testInfo.getTestMethod().isPresent()) {
            throw new IllegalStateException(
                "Expected testInfo.getTestMethod() not be empty since we need a method for TestContextManager.");
        }

        this.tokenCredential = mock(TokenCredential.class);
        this.endpoint = PLAYBACK_ENDPOINT;

        final String resourceName = "/compat/" + testInfo.getTestMethod().get().getName() + ".json";

        try (InputStream stream = SchemaRegistryAsyncClientPlaybackTests.class.getResourceAsStream(resourceName)) {
            this.recordedData = SERIALIZER.deserialize(stream, RecordedData.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to get resource input stream for: " + resourceName, e);
        }

        assertNotNull(recordedData, "RecordedData should not be null. Resource: " + resourceName);

        // Sometimes it throws an "NotAMockException", so we had to change from thenReturn to thenAnswer.
        when(tokenCredential.getToken(any(TokenRequestContext.class))).thenAnswer(invocationOnMock -> {
            return Mono.fromCallable(() -> {
                return new AccessToken("foo", OffsetDateTime.now().plusMinutes(20));
            });
        });
    }

    @AfterEach
    public void teardownTest() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * This is run in playback mode because the GUID is unique for each schema. This is a schema that was previously
     * registered in Azure Portal.
     */
    @Test
    public void getSchemaByIdFromPortal() {

        // Arrange
        final HttpClient httpClientMock = setupHttpClient(recordedData);
        final SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace(endpoint)
            .credential(tokenCredential)
            .httpClient(httpClientMock)
            .serviceVersion(SchemaRegistryVersion.V2021_10)
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
     * https://contral.sonatype.com/artifact/com.azure/azure-data-schemaregistry/1.0.0
     */
    @Test
    public void getSchemaBackCompatibility() {
        // Arrange
        final HttpClient httpClientMock = setupHttpClient(recordedData);

        final SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace(endpoint)
            .credential(tokenCredential)
            .httpClient(httpClientMock)
            .serviceVersion(SchemaRegistryVersion.V2021_10)
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


    private static HttpClient setupHttpClient(RecordedData recordedData) {
        final HttpClient httpClientMock = mock(HttpClient.class);
        final NetworkCallRecord networkRecord = recordedData.findFirstAndRemoveNetworkCall(e -> true);

        when(httpClientMock.send(any(), any(Context.class))).thenAnswer(invocationOnMock -> {
            final HttpRequest request = invocationOnMock.getArgument(0);

            final String body = networkRecord.getResponse().remove("Body");
            final String statusCodeMessage = networkRecord.getResponse().remove("StatusCode");
            final int statusCode = Integer.parseInt(statusCodeMessage);

            assertNotNull(body, "Body cannot be null");

            final HttpHeaders headers = new HttpHeaders(networkRecord.getResponse());

            final HttpResponse response = new MockHttpResponse(request, statusCode, headers, body);
            return Mono.just(response);
        });

        return httpClientMock;
    }
}
