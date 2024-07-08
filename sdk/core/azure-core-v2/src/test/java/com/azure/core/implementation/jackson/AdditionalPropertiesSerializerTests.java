// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class AdditionalPropertiesSerializerTests {
    @Test
    public void canSerializeAdditionalProperties() throws Exception {
        Foo foo = new Foo();
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

        String serialized = new JacksonAdapter().serialize(foo, SerializerEncoding.JSON);
        Assertions.assertEquals(
            "{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }

    @Test
    public void canDeserializeAdditionalProperties() throws Exception {
        String wireValue
            = "{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        Foo deserialized = new JacksonAdapter().deserialize(wireValue, Foo.class, SerializerEncoding.JSON);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
    }

    @Test
    public void canSerializeAdditionalPropertiesThroughInheritance() throws Exception {
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

        String serialized = new JacksonAdapter().serialize(foo, SerializerEncoding.JSON);
        Assertions.assertEquals(
            "{\"$type\":\"foochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesThroughInheritance() throws Exception {
        String wireValue
            = "{\"$type\":\"foochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        Foo deserialized = new JacksonAdapter().deserialize(wireValue, Foo.class, SerializerEncoding.JSON);
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
    public void canSerializeAdditionalPropertiesWithNestedAdditionalProperties() throws Exception {
        Foo foo = new Foo();
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
        Foo nestedFoo = new Foo();
        nestedFoo.bar("bye.world");
        nestedFoo.additionalProperties(new HashMap<>());
        nestedFoo.additionalProperties().put("name", "Sushi");
        foo.additionalProperties().put("foo", nestedFoo);

        String serialized = new JacksonAdapter().serialize(foo, SerializerEncoding.JSON);
        Assertions.assertEquals(
            "{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"foo\":{\"properties\":{\"bar\":\"bye.world\"},\"name\":\"Sushi\"},\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }
}
