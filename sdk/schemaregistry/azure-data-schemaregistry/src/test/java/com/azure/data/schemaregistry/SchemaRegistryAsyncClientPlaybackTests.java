// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static com.azure.data.schemaregistry.Constants.PLAYBACK_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that can only be played-back because they use a recording from the Portal or a back-compat issue that cannot be
 * reproduced with the latest client.
 */
public class SchemaRegistryAsyncClientPlaybackTests {
    private final TokenCredential tokenCredential = new MockTokenCredential();
    private final String endpoint = PLAYBACK_ENDPOINT;

    /**
     * This is run in playback mode because the GUID is unique for each schema. This is a schema that was previously
     * registered in Azure Portal.
     */
    @Test
    public void getSchemaByIdFromPortal() {
        // Arrange
        final String schemaId = "f45b841fcb88401e961ca45477906be9";
        final HttpClient httpClientMock = setupHttpClient("test", schemaId,
            "{\"namespace\":\"SampleSchemaNameSpace\",\"type\":\"record\",\"name\":\"Person\","
                + "\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"favourite_number\","
                + "\"type\":[\"int\",\"null\"]},{\"name\":\"favourite_colour\",\"type\":[\"string\",\"null\"]}]}");
        final SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder().fullyQualifiedNamespace(endpoint)
            .credential(tokenCredential)
            .httpClient(httpClientMock)
            .serviceVersion(SchemaRegistryVersion.V2021_10)
            .buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getSchema(schemaId)).assertNext(schema -> {
            assertNotNull(schema.getProperties());
            assertEquals(schemaId, schema.getProperties().getId());
            assertEquals(SchemaFormat.AVRO, schema.getProperties().getFormat());
        }).verifyComplete();
    }

    /**
     * Verifies that the new serializer works with 1.0.0 schema registry client.
     * https://contral.sonatype.com/artifact/com.azure/azure-data-schemaregistry/1.0.0
     */
    @Test
    public void getSchemaBackCompatibility() {
        // Arrange
        final String schemaId = "e5691f79e3964309ac712ec52abcccca";
        final HttpClient httpClientMock = setupHttpClient("sch17568204e", schemaId,
            "\"{\\\"type\\\" : \\\"record\\\",\\\"namespace\\\" : \\\"TestSchema\\\",\\\"name\\\" : \\\"Employee\\\","
                + "\\\"fields\\\" : [{ \\\"name\\\" : \\\"Name\\\" , \\\"type\\\" : \\\"string\\\" },"
                + "{ \\\"name\\\" : \\\"Age\\\", \\\"type\\\" : \\\"int\\\" }]}\"");

        final SchemaRegistryAsyncClient client = new SchemaRegistryClientBuilder().fullyQualifiedNamespace(endpoint)
            .credential(tokenCredential)
            .httpClient(httpClientMock)
            .serviceVersion(SchemaRegistryVersion.V2021_10)
            .buildAsyncClient();

        // Act & Assert
        StepVerifier.create(client.getSchema(schemaId)).assertNext(schema -> {
            assertNotNull(schema.getProperties());
            assertEquals(schemaId, schema.getProperties().getId());
            assertEquals(SchemaFormat.AVRO, schema.getProperties().getFormat());
        }).verifyComplete();
    }

    @SuppressWarnings("deprecation")
    private static HttpClient setupHttpClient(String schemaName, String schemaId, String body) {
        return request -> {
            HttpHeaders headers = new HttpHeaders().add(HttpHeaderName.TRANSFER_ENCODING, "chunked")
                .add(HttpHeaderName.SERVER, "Microsoft-HTTPAPI/2.0")
                .add(HttpHeaderName.DATE, "Thu, 11 Nov 2021 02:50:18 GMT")
                .add(HttpHeaderName.STRICT_TRANSPORT_SECURITY, "max-age=31536000")
                .add(HttpHeaderName.CONTENT_TYPE, "application/json;serialization=Avro")
                .add("Schema-Version", "1")
                .add("Schema-Name", schemaName)
                .add("Schema-Id", schemaId)
                .add("Schema-Id-Location",
                    String.format(
                        "https://registry.servicebus.windows.net:443/$schemagroups/$schemas/%s?api-version=2021-10",
                        schemaId))
                .add("Schema-Group-Name", "mygroup")
                .add("Schema-Versions-Location", String.format(
                    "https://registry.servicebus.windows.net:443/$schemagroups/mygroup/schemas/%s/versions/1?api-version=2021-10",
                    schemaName))
                .add(HttpHeaderName.LOCATION, String.format(
                    "https://registry.servicebus.windows.net:443/$schemagroups/mygroup/schemas/%s/versions/1?api-version=2021-10",
                    schemaName));

            return Mono.just(new MockHttpResponse(request, 200, headers, body.getBytes(StandardCharsets.UTF_8)));
        };
    }
}
