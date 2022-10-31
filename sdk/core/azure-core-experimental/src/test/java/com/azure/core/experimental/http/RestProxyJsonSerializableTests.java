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
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that {@link JsonSerializable} is supported by {@code RestProxy} when {@code azure-json} is included as a
 * dependency.
 */
public class RestProxyJsonSerializableTests {
    public static final class SimpleJsonSerializable implements JsonSerializable<SimpleJsonSerializable> {
        private final boolean aBoolean;
        private final int anInt;
        private final double aDecimal;
        private final String aString;

        public SimpleJsonSerializable(boolean aBoolean, int anInt, double aDecimal, String aString) {
            this.aBoolean = aBoolean;
            this.anInt = anInt;
            this.aDecimal = aDecimal;
            this.aString = aString;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeBooleanField("aBoolean", aBoolean);
            jsonWriter.writeIntField("anInt", anInt);
            jsonWriter.writeDoubleField("aDecimal", aDecimal);
            jsonWriter.writeStringField("aString", aString);

            return jsonWriter.writeEndObject();
        }

        public static SimpleJsonSerializable fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                boolean aBoolean = false;
                boolean foundABoolean = false;
                double aDecimal = 0.0D;
                boolean foundADecimal = false;
                int anInt = 0;
                boolean foundAnInt = false;
                String aString = null;
                boolean foundAString = false;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("aBoolean".equals(fieldName)) {
                        aBoolean = reader.getBoolean();
                        foundABoolean = true;
                    } else if ("aDecimal".equals(fieldName)) {
                        aDecimal = reader.getDouble();
                        foundADecimal = true;
                    } else if ("anInt".equals(fieldName)) {
                        anInt = reader.getInt();
                        foundAnInt = true;
                    } else if ("aString".equals(fieldName)) {
                        aString = reader.getString();
                        foundAString = true;
                    } else {
                        reader.skipChildren();
                    }
                }

                if (foundABoolean && foundADecimal && foundAnInt && foundAString) {
                    return new SimpleJsonSerializable(aBoolean, anInt, aDecimal, aString);
                }

                throw new IllegalStateException("Missing required properties.");
            });
        }
    }

    @Host("http://localhost")
    @ServiceInterface(name = "JsonSerializable")
    public interface SimpleJsonSerializableProxy {
        @Put("sendJsonSerializable")
        @ExpectedResponses({200})
        void sendJsonSerializable(@BodyParam("application/json") SimpleJsonSerializable simpleJsonSerializable);

        @Get("getJsonSerializable")
        @ExpectedResponses({200})
        SimpleJsonSerializable getJsonSerializable();

        @Get("getInvalidJsonSerializable")
        @ExpectedResponses({200})
        SimpleJsonSerializable getInvalidJsonSerializable();
    }

    @Test
    public void sendJsonSerializableRequest() {
        SimpleJsonSerializable jsonSerializable = new SimpleJsonSerializable(true, 10, 10.0, "10");
        String expectedBody = "{\"aBoolean\":true,\"anInt\":10,\"aDecimal\":10.0,\"aString\":\"10\"}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                assertEquals(expectedBody, request.getBodyAsBinaryData().toString());
                return Mono.just(new MockHttpResponse(request, 200, null, SerializerEncoding.JSON));
            })
            .build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);
        proxy.sendJsonSerializable(jsonSerializable);
    }

    @Test
    public void receiveJsonSerializableResponse() {
        String response = "{\"aBoolean\":true,\"anInt\":10,\"aDecimal\":10.0,\"aString\":\"10\"}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200,
                response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.JSON)))
            .build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);

        SimpleJsonSerializable jsonSerializable = proxy.getJsonSerializable();
        assertEquals(true, jsonSerializable.aBoolean);
        assertEquals(10, jsonSerializable.anInt);
        assertEquals(10.0D, jsonSerializable.aDecimal);
        assertEquals("10", jsonSerializable.aString);
    }

    @Test
    public void invalidJsonSerializableResponse() {
        String response = "{\"aBoolean\":true,\"anInt\":10}";

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200,
                response.getBytes(StandardCharsets.UTF_8), SerializerEncoding.JSON)))
            .build();

        SimpleJsonSerializableProxy proxy = RestProxy.create(SimpleJsonSerializableProxy.class, pipeline);
        assertThrows(HttpResponseException.class, proxy::getInvalidJsonSerializable);
    }
}
