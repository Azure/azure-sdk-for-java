// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.serializer.TypeReference;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link GsonJsonSerializer}.
 */
public class GsonJsonSerializerTests {
    private static final GsonJsonSerializer DEFAULT_SERIALIZER = new GsonJsonSerializerBuilder().build();
    private static final GsonJsonSerializer CUSTOM_SERIALIZER = new GsonJsonSerializerBuilder()
        .serializer(new GsonBuilder().registerTypeAdapter(Person.class, new PersonAdapter()).create())
        .build();

    @Test
    public void deserializeNullInputStreamReturnsNull() {
        StepVerifier.create(DEFAULT_SERIALIZER
            .deserializeAsync((InputStream) null, TypeReference.createInstance(Person.class)))
            .verifyComplete();
    }

    @Test
    public void deserializeNullByteArrayReturnsNull() {
        StepVerifier.create(DEFAULT_SERIALIZER
            .deserializeFromBytesAsync((byte[]) null, TypeReference.createInstance(Person.class)))
            .verifyComplete();
    }

    @Test
    public void deserializeWithDefaultSerializer() {
        String json = "{\"name\":null,\"age\":50}";
        Person expected = new Person(null, 50);

        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(DEFAULT_SERIALIZER.deserializeAsync(jsonStream, TypeReference.createInstance(Person.class)))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    @Test
    public void deserializeWithCustomSerializer() {
        String json = "{\"name\":null,\"age\":50}";
        Person expected = new Person("John Doe", 50);

        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(CUSTOM_SERIALIZER.deserializeAsync(jsonStream, TypeReference.createInstance(Person.class)))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    @Test
    public void deserializeWithDefaultSerializerToJsonObject() {
        String json = "{\"name\":null,\"age\":50}";

        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(DEFAULT_SERIALIZER.deserializeAsync(jsonStream, TypeReference.createInstance(JsonObject.class)))
            .assertNext(actual -> {
                assertEquals(50, actual.get("age").getAsInt());
                assertTrue(actual.get("name").isJsonNull());
            }).verifyComplete();
    }

    @Test
    public void serializeWithDefaultSerializer() {
        Person person = new Person(null, 50);
        byte[] expected = "{\"age\":50}".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        StepVerifier.create(DEFAULT_SERIALIZER.serializeAsync(stream, person))
            .verifyComplete();

        assertArrayEquals(expected, stream.toByteArray());
    }

    @Test
    public void serializeWithCustomSerializer() {
        Person person = new Person(null, 50);
        byte[] expected = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        StepVerifier.create(CUSTOM_SERIALIZER.serializeAsync(stream, person))
            .verifyComplete();

        assertArrayEquals(expected, stream.toByteArray());
    }

    public static final class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            Person person = (Person) other;

            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    private static final class PersonAdapter extends TypeAdapter<Person> {
        @Override
        public void write(JsonWriter out, Person value) throws IOException {
            out.beginObject();

            out.name("name");
            out.value(value.getName() == null ? "John Doe" : value.getName());

            out.name("age");
            out.value(value.getAge());

            out.endObject();
        }

        @Override
        public Person read(JsonReader in) throws IOException {
            String name = "John Doe";
            int age = 0;

            in.beginObject();

            while (in.hasNext()) {
                String nodeName = in.nextName();
                if (nodeName.equalsIgnoreCase("name") && in.peek() == JsonToken.STRING) {
                    name = in.nextString();
                } else if (nodeName.equalsIgnoreCase("age")) {
                    age = in.nextInt();
                } else {
                    in.skipValue();
                }
            }

            in.endObject();

            return new Person(name, age);
        }
    }
}
