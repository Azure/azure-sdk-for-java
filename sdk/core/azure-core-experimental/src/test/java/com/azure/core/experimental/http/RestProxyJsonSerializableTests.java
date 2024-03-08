// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonSerializable;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link JsonSerializable} is supported by {@code RestProxy} when {@code azure-json} is included as a
 * dependency.
 */
public class RestProxyJsonSerializableTests {
    @Host("http://localhost")
    @ServiceInterface(name = "JsonSerializable")
    public interface SimpleJsonSerializableProxy {
        @Put("sendJsonSerializable")
        @ExpectedResponses({ 200 })
        void sendJsonSerializable(@BodyParam("application/json") SimpleJsonSerializable simpleJsonSerializable);

        @Get("getJsonSerializable")
        @ExpectedResponses({ 200 })
        SimpleJsonSerializable getJsonSerializable();

        @Get("getInvalidJsonSerializable")
        @ExpectedResponses({ 200 })
        SimpleJsonSerializable getInvalidJsonSerializable();
    }

    @Test
    public void sendJsonSerializableRequest() {
        SimpleJsonSerializable jsonSerializable = new SimpleJsonSerializable(true, 10, 10.0, "10");
        String expectedBody = "{\"boolean\":true,\"int\":10,\"decimal\":10.0,\"string\":\"10\"}";

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertEquals(expectedBody, request.getBodyAsBinaryData().toString());
            return Mono.just(new MockHttpResponse(request, 200, null, SerializerEncoding.JSON));
        }).build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);
        proxy.sendJsonSerializable(jsonSerializable);
    }

    @Test
    public void receiveJsonSerializableResponse() {
        String response = "{\"boolean\":true,\"int\":10,\"decimal\":10.0,\"string\":\"10\"}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(
                new MockHttpResponse(request, 200, response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.JSON)))
            .build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);

        SimpleJsonSerializable jsonSerializable = proxy.getJsonSerializable();
        assertEquals(true, jsonSerializable.isABoolean());
        assertEquals(10, jsonSerializable.getAnInt());
        assertEquals(10.0D, jsonSerializable.getADecimal());
        assertEquals("10", jsonSerializable.getAString());
    }

    @Test
    public void invalidJsonSerializableResponse() {
        String response = "{\"boolean\":true,\"int\":10}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(
                new MockHttpResponse(request, 200, response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.JSON)))
            .build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);
        assertThrows(HttpResponseException.class, proxy::getInvalidJsonSerializable);
    }
}
