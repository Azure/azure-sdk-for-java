// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.json.DefaultJsonReader;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Function;

public class AdditionalPropertiesSerializerWithJacksonAnnotationTests {
    @Test
    public void canSerializeAdditionalProperties() {
        NewFoo foo = new NewFoo();
        foo.bar("hello.world");
        foo.baz(new ArrayList<>());
        foo.baz().add("hello");
        foo.baz().add("hello.world");
        foo.qux(new LinkedHashMap<>());
        foo.qux().put("hello", "world");
        foo.qux().put("a.b", "c.d");
        foo.qux().put("bar.a", "ttyy");
        foo.qux().put("bar.b", "uuzz");
        foo.additionalProperties(new LinkedHashMap<>());
        foo.additionalProperties().put("bar", "baz");
        foo.additionalProperties().put("a.b", "c.d");
        foo.additionalProperties().put("properties.bar", "barbar");

        String serialized = writeJson(foo);
        Assertions.assertEquals("{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalProperties() {
        String wireValue = "{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        NewFoo deserialized = readJson(wireValue, NewFoo::fromJson);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
    }

    @Test
    public void canSerializeAdditionalPropertiesThroughInheritance() {
        NewFoo foo = new NewFooChild();
        foo.bar("hello.world");
        foo.baz(new ArrayList<>());
        foo.baz().add("hello");
        foo.baz().add("hello.world");
        foo.qux(new LinkedHashMap<>());
        foo.qux().put("hello", "world");
        foo.qux().put("a.b", "c.d");
        foo.qux().put("bar.a", "ttyy");
        foo.qux().put("bar.b", "uuzz");
        foo.additionalProperties(new LinkedHashMap<>());
        foo.additionalProperties().put("bar", "baz");
        foo.additionalProperties().put("a.b", "c.d");
        foo.additionalProperties().put("properties.bar", "barbar");

        String serialized = writeJson(foo);
        Assertions.assertEquals("{\"$type\":\"newfoochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesThroughInheritance() {
        String wireValue = "{\"$type\":\"newfoochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        NewFoo deserialized = readJson(wireValue, NewFoo::fromJson);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        Assertions.assertTrue(deserialized instanceof NewFooChild);
    }

    @Test
    public void canSerializeAdditionalPropertiesWithNestedAdditionalProperties() {
        NewFoo foo = new NewFoo();
        foo.bar("hello.world");
        foo.baz(new ArrayList<>());
        foo.baz().add("hello");
        foo.baz().add("hello.world");
        foo.qux(new LinkedHashMap<>());
        foo.qux().put("hello", "world");
        foo.qux().put("a.b", "c.d");
        foo.qux().put("bar.a", "ttyy");
        foo.qux().put("bar.b", "uuzz");
        foo.additionalProperties(new LinkedHashMap<>());
        foo.additionalProperties().put("bar", "baz");
        foo.additionalProperties().put("a.b", "c.d");
        foo.additionalProperties().put("properties.bar", "barbar");
        NewFoo nestedNewFoo = new NewFoo();
        nestedNewFoo.bar("bye.world");
        nestedNewFoo.additionalProperties(new LinkedHashMap<>());
        nestedNewFoo.additionalProperties().put("name", "Sushi");
        foo.additionalProperties().put("foo", nestedNewFoo);

        String serialized = writeJson(foo);
        Assertions.assertEquals("{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\",\"foo\":{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"bye.world\"},\"name\":\"Sushi\"}}", serialized);
    }

    @Test
    public void canSerializeAdditionalPropertiesWithConflictProperty() {
        NewFoo foo = new NewFoo();
        foo.bar("hello.world");
        foo.baz(new ArrayList<>());
        foo.baz().add("hello");
        foo.baz().add("hello.world");
        foo.qux(new LinkedHashMap<>());
        foo.qux().put("hello", "world");
        foo.qux().put("a.b", "c.d");
        foo.qux().put("bar.a", "ttyy");
        foo.qux().put("bar.b", "uuzz");
        foo.additionalProperties(new LinkedHashMap<>());
        foo.additionalProperties().put("bar", "baz");
        foo.additionalProperties().put("a.b", "c.d");
        foo.additionalProperties().put("properties.bar", "barbar");
        foo.additionalPropertiesProperty(new LinkedHashMap<>());
        foo.additionalPropertiesProperty().put("age", 73);

        String serialized = writeJson(foo);
        Assertions.assertEquals("{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\"}}}},\"additionalProperties\":{\"age\":73},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesWithConflictProperty() {
        String wireValue = "{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\",\"additionalProperties\":{\"age\":73}}";
        NewFoo deserialized = readJson(wireValue, NewFoo::fromJson);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        Assertions.assertEquals(1, deserialized.additionalPropertiesProperty().size());
        Assertions.assertEquals(73, deserialized.additionalPropertiesProperty().get("age"));
    }

    private static String writeJson(JsonSerializable<?> jsonSerializable) {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(outputStream);
        jsonSerializable.toJson(writer);

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static <T> T readJson(String json, Function<JsonReader, T> reader) {
        return reader.apply(DefaultJsonReader.fromString(json));
    }
}
