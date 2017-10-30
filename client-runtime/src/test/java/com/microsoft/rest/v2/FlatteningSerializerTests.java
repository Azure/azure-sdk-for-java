/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.v2.serializer.JacksonAdapter;
import com.microsoft.rest.v2.serializer.JsonFlatten;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatteningSerializerTests {
    @Test
    public void canFlatten() throws Exception {
        Foo foo = new Foo();
        foo.bar = "hello.world";
        foo.baz = new ArrayList<>();
        foo.baz.add("hello");
        foo.baz.add("hello.world");
        foo.qux = new HashMap<>();
        foo.qux.put("hello", "world");
        foo.qux.put("a.b", "c.d");

        String serialized = new JacksonAdapter().serialize(foo);
        Assert.assertEquals("{\"properties\":{\"bar\":\"hello.world\",\"props\":{\"baz\":[\"hello\",\"hello.world\"],\"q\":{\"qux\":{\"a.b\":\"c.d\",\"hello\":\"world\"}}}}}", serialized);
    }

    @JsonFlatten
    private class Foo {
        @JsonProperty(value = "properties.bar")
        private String bar;
        @JsonProperty(value = "properties.props.baz")
        private List<String> baz;
        @JsonProperty(value = "properties.props.q.qux")
        private Map<String, String> qux;
        @JsonProperty(value = "props.empty")
        private Integer empty;
    }
}
