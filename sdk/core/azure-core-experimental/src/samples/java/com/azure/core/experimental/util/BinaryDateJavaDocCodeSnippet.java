// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.ObjectSerializer;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        // Following will use default StandardCharsets.UTF_8
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
        final Flux<ByteBuffer> expectedFlux = Flux.just(ByteBuffer.wrap(data));

        Mono<BinaryData> binaryDataMono = BinaryData.fromFlux(expectedFlux);

        // Lets print the value of BinaryData
        Disposable subscriber = binaryDataMono
            .map(binaryData -> {
                System.out.println(binaryData.toString());
                return true;
            })
            .subscribe();

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

        // Serializer to use to serialize object.
        final JacksonJsonSerializer serializer = new JacksonJsonSerializerBuilder().build();

        BinaryData binaryData = BinaryData.fromObject(data, new JacksonJsonSerializerBuilder().build());

        // Lets print the value of BinaryData
        Disposable subscriber = binaryData
            .toObjectAsync(Person.class, serializer)
            .map(person -> {
                System.out.println(person.getName());
                return true;
            })
            .subscribe();

        TimeUnit.SECONDS.sleep(5);
        subscriber.dispose();
        // END: com.azure.core.experimental.util.BinaryDocument.to#ObjectAsync
    }

    /**
     * Codesnippets for {@link BinaryData#toStream()}.
     */
    public void toObject() throws IOException {
        // BEGIN: com.azure.core.experimental.util.BinaryDocument.to#Stream
        final byte[] data = "Some Data".getBytes(StandardCharsets.UTF_8);
        BinaryData binaryData = BinaryData.fromStream(new ByteArrayInputStream(data));
        final byte[] bytes = new byte[data.length];
        (binaryData.toStream()).read(bytes, 0, data.length);
        System.out.println(new String(bytes));
        // END: com.azure.core.experimental.util.BinaryDocument.to#Stream
    }

    public static void main(String[] a) throws IOException, InterruptedException {
        //BinaryDateJavaDocCodeSnippet codeSnippet =  new BinaryDateJavaDocCodeSnippet();
        //codeSnippet.createFromStream();
        //codeSnippet.createToStream();
        //codeSnippet.createFromFlux();
        //codeSnippet.createFromString();
        //codeSnippet.createToObjectAsync();
    }
}

