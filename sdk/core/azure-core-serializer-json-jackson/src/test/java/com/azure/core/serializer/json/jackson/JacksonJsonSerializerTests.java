// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JacksonJsonSerializer}.
 */
public class JacksonJsonSerializerTests {
    private static final JacksonJsonSerializer DEFAULT_SERIALIZER = new JacksonJsonSerializerBuilder().build();
    private static final JacksonJsonSerializer CUSTOM_SERIALIZER = new JacksonJsonSerializerBuilder()
        .serializer(new ObjectMapper().registerModule(
            new SimpleModule().addSerializer(Person.class, new PersonSerializer())
                .addDeserializer(Person.class, new PersonDeserializer())))
        .build();

    @Test
    public void deserializeNull() {
        StepVerifier.create(DEFAULT_SERIALIZER.deserializeAsync(null, TypeReference.createInstance(Person.class)))
            .verifyComplete();
    }

    @Test
    public void deserializeWithDefaultSerializer() {
        String json = "{\"name\":null,\"age\":50}";
        Person expected = new Person().setAge(50);

        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(DEFAULT_SERIALIZER.deserializeAsync(jsonStream, TypeReference.createInstance(Person.class)))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    @Test
    public void deserializeWithCustomSerializer() {
        String json = "{\"name\":null,\"age\":50}";
        Person expected = new Person().setName("John Doe").setAge(50);

        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(CUSTOM_SERIALIZER.deserializeAsync(jsonStream, TypeReference.createInstance(Person.class)))
            .assertNext(actual -> assertEquals(expected, actual))
            .verifyComplete();
    }

    @Test
    public void deserializeWithDefaultSerializerToObjectNode() {
        String json = "{\"name\":null,\"age\":50}";

        InputStream jsonStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(DEFAULT_SERIALIZER.deserializeAsync(jsonStream, TypeReference.createInstance(ObjectNode.class)))
            .assertNext(actual -> {
                assertEquals(50, actual.get("age").asInt());
                assertTrue(actual.get("name").isNull());
            }).verifyComplete();
    }

    @Test
    public void serializeWithDefaultSerializer() {
        Person person = new Person().setAge(50);
        byte[] expected = "{\"name\":null,\"age\":50}".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        StepVerifier.create(DEFAULT_SERIALIZER.serializeAsync(stream, person))
            .verifyComplete();

        assertArrayEquals(expected, stream.toByteArray());
    }

    @Test
    public void serializeWithCustomSerializer() {
        Person person = new Person().setAge(50);
        byte[] expected = "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        StepVerifier.create(CUSTOM_SERIALIZER.serializeAsync(stream, person))
            .verifyComplete();

        assertArrayEquals(expected, stream.toByteArray());
    }

    private static final class PersonSerializer extends JsonSerializer<Person> {

        @Override
        public void serialize(Person person, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();

            generator.writeFieldName("name");
            generator.writeString(person.getName() == null ? "John Doe" : person.getName());

            generator.writeFieldName("age");
            generator.writeNumber(person.getAge());

            generator.writeEndObject();
        }
    }

    private static final class PersonDeserializer extends JsonDeserializer<Person> {

        @Override
        public Person deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String name = "John Doe";
            int age = 0;

            String fieldName;
            while ((fieldName = parser.nextFieldName()) != null) {
                if (fieldName.equalsIgnoreCase("name") && parser.nextToken() != JsonToken.VALUE_NULL) {
                    name = (String) parser.getCurrentValue();
                } else if (fieldName.equalsIgnoreCase("age")) {
                    age = parser.nextIntValue(0);
                }
            }

            return new Person().setName(name).setAge(age);
        }
    }
}
