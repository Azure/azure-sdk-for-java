// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.v2.util.serializer.JacksonAdapter;
import com.azure.core.v2.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class AdditionalPropertiesSerializerWithJacksonAnnotationTests {
    @Test
    public void canSerializeAdditionalProperties() throws Exception {
        NewFoo foo = new NewFoo();
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
            "{\"$type\":\"newfoo\",\"bar\":\"baz\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }

    @Test
    public void canDeserializeAdditionalProperties() throws Exception {
        String wireValue
            = "{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        NewFoo deserialized = new JacksonAdapter().deserialize(wireValue, NewFoo.class, SerializerEncoding.JSON);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
    }

    @Test
    public void canSerializeAdditionalPropertiesThroughInheritance() throws Exception {
        NewFoo foo = new NewFooChild();
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
            "{\"$type\":\"newfoochild\",\"bar\":\"baz\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesThroughInheritance() throws Exception {
        String wireValue
            = "{\"$type\":\"newfoochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        NewFoo deserialized = new JacksonAdapter().deserialize(wireValue, NewFoo.class, SerializerEncoding.JSON);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        Assertions.assertTrue(deserialized instanceof NewFooChild);
    }

    @Test
    public void canSerializeAdditionalPropertiesWithNestedAdditionalProperties() throws Exception {
        NewFoo foo = new NewFoo();
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
        NewFoo nestedNewFoo = new NewFoo();
        nestedNewFoo.bar("bye.world");
        nestedNewFoo.additionalProperties(new HashMap<>());
        nestedNewFoo.additionalProperties().put("name", "Sushi");
        foo.additionalProperties().put("foo", nestedNewFoo);

        String serialized = new JacksonAdapter().serialize(foo, SerializerEncoding.JSON);
        Assertions.assertEquals(
            "{\"$type\":\"newfoo\",\"bar\":\"baz\",\"foo\":{\"name\":\"Sushi\",\"properties\":{\"bar\":\"bye.world\"}},\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }

    @Test
    public void canSerializeAdditionalPropertiesWithConflictProperty() throws Exception {
        NewFoo foo = new NewFoo();
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
        foo.additionalPropertiesProperty(new HashMap<>());
        foo.additionalPropertiesProperty().put("age", 73);

        String serialized = new JacksonAdapter().serialize(foo, SerializerEncoding.JSON);
        Assertions.assertEquals(
            "{\"$type\":\"newfoo\",\"additionalProperties\":{\"age\":73},\"bar\":\"baz\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}",
            serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesWithConflictProperty() throws Exception {
        String wireValue
            = "{\"$type\":\"newfoo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\",\"additionalProperties\":{\"age\":73}}";
        NewFoo deserialized = new JacksonAdapter().deserialize(wireValue, NewFoo.class, SerializerEncoding.JSON);
        Assertions.assertNotNull(deserialized.additionalProperties());
        Assertions.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assertions.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assertions.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        Assertions.assertEquals(1, deserialized.additionalPropertiesProperty().size());
        Assertions.assertEquals(73, deserialized.additionalPropertiesProperty().get("age"));
    }
}
