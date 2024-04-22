// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.serializer;

import io.clientcore.core.implementation.http.serializer.DefaultJsonSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdditionalPropertiesSerializerTests {
    @Test
    public void canSerializeAdditionalProperties() throws IOException {
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

        String serialized = new String(new DefaultJsonSerializer().serializeToBytes(foo));

        assertEquals("{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}", serialized);
    }

    @Test
    public void canDeserializeAdditionalProperties() throws IOException {
        String wireValue = "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}";
        Foo deserialized = new DefaultJsonSerializer().deserializeFromBytes(wireValue.getBytes(), Foo.class);

        assertNotNull(deserialized.additionalProperties());
        assertEquals("baz", deserialized.additionalProperties().get("bar"));
        assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
    }

    @Test
    public void canSerializeAdditionalPropertiesThroughInheritance() throws IOException {
        FooChild foo = new FooChild();

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

        String serialized = new String(new DefaultJsonSerializer().serializeToBytes(foo));

        assertEquals("{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}", serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesThroughInheritance() throws IOException {
        String wireValue = "{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}}";
        FooChild deserialized = new DefaultJsonSerializer().deserializeFromBytes(wireValue.getBytes(), FooChild.class);

        // Check additional properties are populated
        assertNotNull(deserialized.additionalProperties());
        assertEquals("baz", deserialized.additionalProperties().get("bar"));
        assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        // Check typed properties are populated
        assertEquals("hello.world", deserialized.bar());
        assertNotNull(deserialized.baz());
        assertEquals(2, deserialized.baz().size());
        assertTrue(deserialized.baz().contains("hello"));
        assertTrue(deserialized.baz().contains("hello.world"));
        assertNotNull(deserialized.qux());
        assertEquals(4, deserialized.qux().size());
        assertTrue(deserialized.qux().containsKey("hello"));
        assertTrue(deserialized.qux().containsKey("a.b"));
        assertTrue(deserialized.qux().containsKey("bar.a"));
        assertTrue(deserialized.qux().containsKey("bar.b"));
        assertEquals("world", deserialized.qux().get("hello"));
        assertEquals("c.d", deserialized.qux().get("a.b"));
        assertEquals("ttyy", deserialized.qux().get("bar.a"));
        assertEquals("uuzz", deserialized.qux().get("bar.b"));
    }

    @Test
    public void canSerializeAdditionalPropertiesWithNestedAdditionalProperties() throws IOException {
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

        String serialized = new String(new DefaultJsonSerializer().serializeToBytes(foo));

        assertEquals("{\"bar\":\"hello.world\",\"baz\":[\"hello\",\"hello.world\"],\"qux\":{\"a.b\":\"c.d\",\"bar.a\":\"ttyy\",\"bar.b\":\"uuzz\",\"hello\":\"world\"},\"additionalProperties\":{\"bar\":\"baz\",\"a.b\":\"c.d\",\"foo\":{\"bar\":\"bye.world\",\"additionalProperties\":{\"name\":\"Sushi\"}},\"properties.bar\":\"barbar\"}}", serialized);
    }
}
