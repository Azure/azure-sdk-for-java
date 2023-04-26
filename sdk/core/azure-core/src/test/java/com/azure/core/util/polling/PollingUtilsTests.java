// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.implementation.serializer.DefaultJsonSerializer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.implementation.PollingUtils;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PollingUtilsTests {
    private static final ClientLogger LOGGER = new ClientLogger(PollingUtilsTests.class);
    private static final String FORWARD_SLASH = "/";
    private static final String LOCAL_HOST = "http://localhost";

    @Test
    public void invalidPathTest() {
        String invalidPath = "`file";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> getAbsolutePath(invalidPath, LOCAL_HOST, LOGGER));
        assertTrue("'path' must be a valid URI.".equals(exception.getMessage()));
        assertThrows(NullPointerException.class, () -> getAbsolutePath(null, LOCAL_HOST, LOGGER));
    }

    @Test
    public void relativePathTest() {
        String relativePath = "/file";
        assertEquals(LOCAL_HOST + relativePath, getAbsolutePath(relativePath, LOCAL_HOST, null));
        assertEquals(LOCAL_HOST + relativePath, getAbsolutePath(relativePath, LOCAL_HOST, LOGGER));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                                                      getAbsolutePath(relativePath, null, LOGGER));
        assertTrue("Relative path requires endpoint to be non-null and non-empty to create an absolute path."
                       .equals(exception.getMessage()));

        assertThrows(NullPointerException.class, () -> getAbsolutePath(relativePath, null, null));
        assertThrows(NullPointerException.class, () -> getAbsolutePath(relativePath, "", null));
    }

    @Test
    public void absolutePathTest() {
        String absolutePath = "http://localhost";
        assertEquals(absolutePath, getAbsolutePath(absolutePath, null, null));
        assertEquals(absolutePath, getAbsolutePath(absolutePath, LOCAL_HOST, null));
        assertEquals(absolutePath, getAbsolutePath(absolutePath, LOCAL_HOST, LOGGER));
    }

    @Test
    public void missingOrDuplicateSlashInPath() {
        final String endpoint = "http://localhost";
        final String relativePath = "path";
        final String expectedPath = endpoint + FORWARD_SLASH + relativePath;
        // Case 1: without slash in between
        assertEquals(expectedPath, getAbsolutePath(relativePath, endpoint, null));
        // Case 2: endpoint with slash only
        final String endpointWithSlash = endpoint + FORWARD_SLASH;
        assertEquals(expectedPath, getAbsolutePath(relativePath, endpointWithSlash, null));
        // Case 3: relative path with slash only
        final String relativePathWithSlash = FORWARD_SLASH + relativePath;
        assertEquals(expectedPath, getAbsolutePath(relativePathWithSlash, endpoint, null));
        // Case 4: both of endpoint and relative path have slash
        assertEquals(expectedPath, getAbsolutePath(relativePathWithSlash, endpointWithSlash, null));
    }

    @Test
    public void testConvertResponse() {
        String status = "Succeeded";

        Resource convertOrigin = new Resource().setName("name").setStatus(status);

        PollResult convertResult = PollingUtils.convertResponseSync(convertOrigin,
            new DefaultJsonSerializer(),
            TypeReference.createInstance(PollResult.class));

        assertEquals(status, convertResult.getStatus());
    }

    @Test
    public void testSerializeResponse() {
        String status = "Succeeded";

        Resource convertOrigin = new Resource().setName("name").setStatus(status);

        JsonSerializer customizedJsonSerializer = new JsonSerializer() {
            private static final ClientLogger LOGGER = new ClientLogger(DefaultJsonSerializer.class);

            private final static SerializerAdapter JACKSON_ADAPTER = new JacksonAdapter((outerMapper, innerMapper) -> {
                JacksonAnnotationIntrospector annotationIntrospector =
                    new JacksonAnnotationIntrospector() {
                        @Override
                        public JsonProperty.Access findPropertyAccess(Annotated annotated) {
                            JsonProperty.Access access = super.findPropertyAccess(annotated);
                            if (access == JsonProperty.Access.WRITE_ONLY) {
                                return JsonProperty.Access.AUTO;
                            }
                            return access;
                        }
                    };
                outerMapper.setAnnotationIntrospector(annotationIntrospector);
            });

            @Override
            public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
                try {
                    return JACKSON_ADAPTER.deserialize(data, typeReference.getJavaType(), SerializerEncoding.JSON);
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
            }

            @Override
            public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
                try {
                    return JACKSON_ADAPTER.deserialize(stream, typeReference.getJavaType(), SerializerEncoding.JSON);
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
            }

            @Override
            public <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
                return Mono.fromCallable(() -> deserializeFromBytes(data, typeReference));
            }

            @Override
            public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
                return Mono.fromCallable(() -> deserialize(stream, typeReference));
            }

            @Override
            public byte[] serializeToBytes(Object value) {
                try {
                    return JACKSON_ADAPTER.serializeToBytes(value, SerializerEncoding.JSON);
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
            }

            @Override
            public void serialize(OutputStream stream, Object value) {
                try {
                    JACKSON_ADAPTER.serialize(value, SerializerEncoding.JSON, stream);
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
                }
            }

            @Override
            public Mono<byte[]> serializeToBytesAsync(Object value) {
                return Mono.fromCallable(() -> serializeToBytes(value));
            }

            @Override
            public Mono<Void> serializeAsync(OutputStream stream, Object value) {
                return Mono.fromRunnable(() -> serialize(stream, value));
            }
        };

        BinaryData jsonAsBinaryData = PollingUtils.serializeResponseSync(convertOrigin, customizedJsonSerializer);

        assertTrue(jsonAsBinaryData.toString().contains(status));
    }

    private static class PollResult {
        @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
        private String status;

        public String getStatus() {
            return status;
        }

        public PollResult setStatus(String status) {
            this.status = status;
            return this;
        }
    }

    private static class Resource {
        @JsonProperty(value = "name", required = true)
        private String name;

        @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
        private String status;

        public String getName() {
            return name;
        }

        public Resource setName(String name) {
            this.name = name;
            return this;
        }

        public String getStatus() {
            return status;
        }

        public Resource setStatus(String status) {
            this.status = status;
            return this;
        }
    }
}
