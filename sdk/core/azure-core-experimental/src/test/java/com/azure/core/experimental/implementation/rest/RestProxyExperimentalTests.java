// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.rest;

import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class RestProxyExperimentalTests {
    private static final String GLOBAL_SETTING_KEY = "AZURE_HTTP_REST_PROXY_EXPERIMENTAL_ENABLED";
    private static final String INSTANCE_SETTING_KEY = "com.azure.core.http.restproxy.experimental.enable";

    private static final String EXPECTED_BODY = "{\"first\":\"firstProperty\",\"second\":42,\"third\":true}";
    private static final Something EXPECTED_RESULT = new Something()
        .setFirstProperty("firstProperty")
        .setSecondProperty(42)
        .setThirdProperty(true);

    @Host("https://portal.azure.com")
    @ServiceInterface(name = "ExperimentalRestProxy")
    interface ExperimentalRestProxy {
        @Get("test")
        Something getSomethingWithContext(@BodyParam(ContentType.APPLICATION_JSON) Something something,
            Context context);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void globalRestProxyExperimental() {
        boolean wasSet = Configuration.getGlobalConfiguration().contains(GLOBAL_SETTING_KEY);
        String initialSetting = Configuration.getGlobalConfiguration().get(GLOBAL_SETTING_KEY);
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                assertEquals(EXPECTED_BODY, request.getBodyAsBinaryData().toString());
                return Mono.just(new MockHttpResponse(request));
            })
            .build();
        SerializerAdapter adapter = new ThrowingSerializerAdapter();
        try {
            Configuration.getGlobalConfiguration().put(GLOBAL_SETTING_KEY, "true");
            ExperimentalRestProxy restProxy = RestProxy.create(ExperimentalRestProxy.class, pipeline, adapter);
            Something actual = restProxy.getSomethingWithContext(EXPECTED_RESULT, Context.NONE);

            assertEquals(actual.getFirstProperty(), EXPECTED_RESULT.getFirstProperty());
            assertEquals(actual.getSecondProperty(), EXPECTED_RESULT.getSecondProperty());
            assertEquals(actual.isThirdProperty(), EXPECTED_RESULT.isThirdProperty());
        } finally {
            if (wasSet) {
                Configuration.getGlobalConfiguration().put(GLOBAL_SETTING_KEY, initialSetting);
            } else {
                Configuration.getGlobalConfiguration().remove(GLOBAL_SETTING_KEY);
            }
        }
    }

    @Test
    public void instanceRestProxyExperimental() {
        Context context = new Context(INSTANCE_SETTING_KEY, true);
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(request -> {
                assertEquals(EXPECTED_BODY, request.getBodyAsBinaryData().toString());
                return Mono.just(new MockHttpResponse(request));
            })
            .build();
        SerializerAdapter adapter = new ThrowingSerializerAdapter();
        ExperimentalRestProxy restProxy = RestProxy.create(ExperimentalRestProxy.class, pipeline, adapter);
        Something actual = restProxy.getSomethingWithContext(EXPECTED_RESULT, context);

        assertEquals(actual.getFirstProperty(), EXPECTED_RESULT.getFirstProperty());
        assertEquals(actual.getSecondProperty(), EXPECTED_RESULT.getSecondProperty());
        assertEquals(actual.isThirdProperty(), EXPECTED_RESULT.isThirdProperty());
    }

    private static final class Something implements JsonSerializable<Something> {
        private String firstProperty;
        private int secondProperty;
        private boolean thirdProperty;

        public String getFirstProperty() {
            return firstProperty;
        }

        public Something setFirstProperty(String firstProperty) {
            this.firstProperty = firstProperty;
            return this;
        }

        public int getSecondProperty() {
            return secondProperty;
        }

        public Something setSecondProperty(int secondProperty) {
            this.secondProperty = secondProperty;
            return this;
        }

        public boolean isThirdProperty() {
            return thirdProperty;
        }

        public Something setThirdProperty(boolean thirdProperty) {
            this.thirdProperty = thirdProperty;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) {
            return jsonWriter.writeStartObject()
                .writeStringField("first", firstProperty)
                .writeIntField("second", secondProperty)
                .writeBooleanField("third", thirdProperty)
                .writeEndObject();
        }

        public static Something fromJson(JsonReader jsonReader) {
            return jsonReader.readObject(reader -> {
                Something result = new Something();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("first".equals(fieldName)) {
                        result.firstProperty = reader.getString();
                    } else if ("second".equals(fieldName)) {
                        result.secondProperty = reader.getInt();
                    } else if ("third".equals(fieldName)) {
                        result.thirdProperty = reader.getBoolean();
                    } else {
                        reader.skipChildren();
                    }
                }

                return result;
            });
        }
    }

    private static final class ThrowingSerializerAdapter implements SerializerAdapter {

        @Override
        public String serialize(Object object, SerializerEncoding encoding) throws IOException {
            throw new RuntimeException("Should not be called.");
        }

        @Override
        public String serializeRaw(Object object) {
            throw new RuntimeException("Should not be called.");
        }

        @Override
        public String serializeList(List<?> list, CollectionFormat format) {
            throw new RuntimeException("Should not be called.");
        }

        @Override
        public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
            throw new RuntimeException("Should not be called.");
        }

        @Override
        public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
            throw new RuntimeException("Should not be called.");
        }
    }

    private static final class MockHttpResponse extends HttpResponse {
        MockHttpResponse(HttpRequest httpRequest) {
            super(httpRequest);
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return getBodyAsByteArray().map(ByteBuffer::wrap).flux();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.fromSupplier(() -> EXPECTED_BODY.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Mono<String> getBodyAsString() {
            return getBodyAsString(null);
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return Mono.fromSupplier(() -> EXPECTED_BODY);
        }
    }
}
