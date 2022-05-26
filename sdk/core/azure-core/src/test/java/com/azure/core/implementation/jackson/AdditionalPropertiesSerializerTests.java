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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Function;

public class AdditionalPropertiesSerializerTests {
    @Test
    public void canSerializeAdditionalProperties() {
        Foo foo = new Foo();
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
        Assertions.assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalProperties() {
        String wireValue = "{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        Foo deserialized = readJson(wireValue, Foo::fromJson);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
    }

    @Test
    public void canSerializeAdditionalPropertiesThroughInheritance() {
        Foo foo = new FooChild();
        foo.bar("hello.world");
        foo.baz(new ArrayList<>());
        foo.baz().add("hello");
        foo.baz().add("hello.world");
        foo.qux(new HashMap<>());
        foo.qux().put("hello", "world");
        foo.qux().put("a.b", "c.d");
        foo.qux().put("bar.a", "ttyy");
        foo.qux().put("bar.b", "uuzz");
        foo.additionalProperties(new HashMap<>());
        foo.additionalProperties().put("bar", "baz");
        foo.additionalProperties().put("a.b", "c.d");
        foo.additionalProperties().put("properties.bar", "barbar");

        String serialized = writeJson(foo);
        Assertions.assertEquals("{\"$type\":\"foochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesThroughInheritance() {
        String wireValue = "{\"$type\":\"foochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        Foo deserialized = readJson(wireValue, Foo::fromJson);
        // Check additional properties are populated
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        Assertions.assertTrue(deserialized instanceof FooChild);
        // Check typed properties are populated
        Assertions.assertEquals("hello.world", deserialized.bar());
        Assertions.assertNotNull(deserialized.baz());
        Assertions.assertEquals(2, deserialized.baz().size());
        Assertions.assertTrue(deserialized.baz().contains("hello"));
        Assertions.assertTrue(deserialized.baz().contains("hello.world"));
        Assertions.assertNotNull(deserialized.qux());
        Assertions.assertEquals(4, deserialized.qux().size());
        Assertions.assertTrue(deserialized.qux().containsKey("hello"));
        Assertions.assertTrue(deserialized.qux().containsKey("a.b"));
        Assertions.assertTrue(deserialized.qux().containsKey("bar.a"));
        Assertions.assertTrue(deserialized.qux().containsKey("bar.b"));
        Assertions.assertEquals("world", deserialized.qux().get("hello"));
        Assertions.assertEquals("c.d", deserialized.qux().get("a.b"));
        Assertions.assertEquals("ttyy", deserialized.qux().get("bar.a"));
        Assertions.assertEquals("uuzz", deserialized.qux().get("bar.b"));
    }

    @Test
    public void canSerializeAdditionalPropertiesWithNestedAdditionalProperties() {
        Foo foo = new Foo();
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
        Foo nestedFoo = new Foo();
        nestedFoo.bar("bye.world");
        nestedFoo.additionalProperties(new LinkedHashMap<>());
        nestedFoo.additionalProperties().put("name", "Sushi");
        foo.additionalProperties().put("foo", nestedFoo);

        String serialized = writeJson(foo);
        Assertions.assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\",\"foo\":{\"$type\":\"foo\",\"properties\":{\"bar\":\"bye.world\"},\"name\":\"Sushi\"}}", serialized);
    }

    private static String writeJson(JsonSerializable<?> jsonSerializable) {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(outputStream);
        writer.writeJson(jsonSerializable);

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    private static <T> T readJson(String json, Function<JsonReader, T> reader) {
        return reader.apply(DefaultJsonReader.fromString(json));
    }
}
