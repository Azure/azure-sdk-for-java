// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.v2.annotation.BodyParam;
import com.azure.core.v2.annotation.ExpectedResponses;
import com.azure.core.v2.annotation.Get;
import com.azure.core.v2.annotation.Host;
import com.azure.core.v2.annotation.Put;
import com.azure.core.v2.annotation.ServiceInterface;
import com.azure.core.v2.exception.HttpResponseException;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.rest.RestProxy;
import com.azure.json.JsonSerializable;
import org.junit.jupiter.api.Test;

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
            return new MockHttpResponse(request, 200));
        }).build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);
        proxy.sendJsonSerializable(jsonSerializable);
    }

    @Test
    public void receiveJsonSerializableResponse() {
        String response = "{\"boolean\":true,\"int\":10,\"decimal\":10.0,\"string\":\"10\"}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono
                .just(new MockHttpResponse(request, 200, new HttpHeaders(), response.getBytes(StandardCharsets.UTF_8))))
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
            .httpClient(request -> Mono
                .just(new MockHttpResponse(request, 200, new HttpHeaders(), response.getBytes(StandardCharsets.UTF_8))))
            .build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);
        assertThrows(HttpResponseException.class, proxy::getInvalidJsonSerializable);
    }
}
