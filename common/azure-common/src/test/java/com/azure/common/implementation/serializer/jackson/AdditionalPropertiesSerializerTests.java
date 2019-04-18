// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation.serializer.jackson;

import com.azure.common.implementation.serializer.SerializerEncoding;
import com.azure.common.implementation.util.Foo;
import com.azure.common.implementation.util.FooChild;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalProperties() throws Exception {
        String wireValue = "{\"$type\":\"foo\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        Foo deserialized = new JacksonAdapter().deserialize(wireValue, Foo.class, SerializerEncoding.JSON);
        Assert.assertNotNull(deserialized.additionalProperties());
        Assert.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assert.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assert.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
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
        Assert.assertEquals("{\"$type\":\"foochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}", serialized);
    }

    @Test
    public void canDeserializeAdditionalPropertiesThroughInheritance() throws Exception {
        String wireValue = "{\"$type\":\"foochild\",\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"hello\":\"world\",\"a.b\":\"c.d\",\"bar.b\":\"uuzz\",\"bar.a\":\"ttyy\"}}}},\"bar\":\"baz\",\"a.b\":\"c.d\",\"properties.bar\":\"barbar\"}";
        Foo deserialized = new JacksonAdapter().deserialize(wireValue, Foo.class, SerializerEncoding.JSON);
        Assert.assertNotNull(deserialized.additionalProperties());
        Assert.assertEquals("baz", deserialized.additionalProperties().get("bar"));
        Assert.assertEquals("c.d", deserialized.additionalProperties().get("a.b"));
        Assert.assertEquals("barbar", deserialized.additionalProperties().get("properties.bar"));
        Assert.assertTrue(deserialized instanceof FooChild);
    }
}
