// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Codesnippets for {@link BinaryData}.
 */
public class BinaryDateJavaDocCodeSnippet {

    /**
     * Codesnippets for {@link BinaryData#fromBytes(byte[])}.
     */
    public void createFromBytes() {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.from#bytes
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromBytes(data);
        System.out.println(new String(binaryData.toBytes(), StandardCharsets.UTF_8));
        // END: com.azure.core.experimental.util.BinaryDocument.from#bytes
    }

    /**
     * Codesnippets for {@link BinaryData#fromString(String)}.
     */
    public void createFromString() {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.from#String
        final String data = "Some Data";
        // Following will use default character set as StandardCharsets.UTF_8
        BinaryData binaryData = BinaryData.fromString(data);
        System.out.println(binaryData.toString());
        // END: com.azure.core.experimental.util.BinaryDocument.from#String
    }

    /**
     * Codesnippets for {@link BinaryData#fromStream(InputStream)}.
     */
    public void createFromStream() {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.from#Stream
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromStream(new ByteArrayInputStream(data));
        System.out.println(binaryData.toString());
        // END: com.azure.core.experimental.util.BinaryDocument.from#Stream
    }

    /**
     * Codesnippets for {@link BinaryData#fromStream(InputStream)}.
     */
    public void createFromFlux() throws InterruptedException {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.from#Flux
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        final Flux<ByteBuffer> dataFlux = Flux.just(ByteBuffer.wrap(data));

        Mono<BinaryData> binaryDataMono = BinaryData.fromFlux(dataFlux);

        // Lets print the value of BinaryData
        Disposable subscriber = binaryDataMono
            .map(binaryData -> {
                System.out.println(binaryData.toString());
                return true;
            })
            .subscribe();

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.experimental.util.BinaryDocument.from#Flux
    }

    /**
     * Codesnippets for {@link BinaryData#toStream()}.
     */
    public void toStream() throws IOException {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.to#Stream
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromStream(new ByteArrayInputStream(data));
        final byte[] bytes = new byte[data.length];
        (binaryData.toStream()).read(bytes, 0, data.length);
        System.out.println(new String(bytes));
        // END: com.azure.core.experimental.util.BinaryDocument.to#Stream
    }

    /**
     * Codesnippets for {@link BinaryData#toObjectAsync(Class, ObjectSerializer)}.
     */
    public void createToObjectAsync() throws InterruptedException {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.to#ObjectAsync
        // Lets say we have Person object which could be serialized into json.
        class Person {
            @JsonProperty
            private String name;

            @JsonSetter
            public Person setName(String name) {
                this.name = name;
                return this;
            }

            @JsonGetter
            public String getName() {
                return name;
            }
        }
        final Person data = new Person().setName("John");

        // Ensure your classpath have the Serializer to use to serialize object. For example you can use one of
        // following library.
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-jackson or
        // https://mvnrepository.com/artifact/com.azure/azure-core-serializer-json-gson

        final ObjectSerializer serializer =
            new MyJsonSerializer(); // Replace this with your Serializer
        BinaryData binaryData = BinaryData.fromObject(data, serializer);

        // Lets print the value of BinaryData
        Disposable subscriber = binaryData
            .toObjectAsync(Person.class, serializer)
            .map(person -> {
                System.out.println(person.getName());
                return true;
            })
            .subscribe();

        // So that your program wait for above subscribe to complete.
        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.experimental.util.BinaryDocument.to#ObjectAsync
    }

    public static class MyJsonSerializer implements JsonSerializer {
        private final ClientLogger logger = new ClientLogger(BinaryDataTest.MyJsonSerializer.class);
        private final ObjectMapper mapper;
        private final TypeFactory typeFactory;

        public MyJsonSerializer() {
            this.mapper = new ObjectMapper();
            this.typeFactory = mapper.getTypeFactory();
        }

        @Override
        public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
            if (stream == null) {
                return null;
            }

            try {
                return mapper.readValue(stream, typeFactory.constructType(typeReference.getJavaType()));
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
            return Mono.fromCallable(() -> deserialize(stream, typeReference));
        }


        @Override
        public void serialize(OutputStream stream, Object value) {
            try {
                mapper.writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        }

        @Override
        public Mono<Void> serializeAsync(OutputStream stream, Object value) {
            return Mono.fromRunnable(() -> serialize(stream, value));
        }
    }
}

