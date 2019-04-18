// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

public class ValidatorTests {
    @Test
    public void validateInt() {
        IntWrapper body = new IntWrapper();
        body.value(2);
        body.nullable(null);
        Validator.validate(body); // pass
    }

    @Test
    public void validateInteger() {
        IntegerWrapper body = new IntegerWrapper();
        body.value(3);
        Validator.validate(body); // pass
        try {
            body.value(null);
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("value is required"));
        }
    }

    @Test
    public void validateString() {
        StringWrapper body = new StringWrapper();
        body.value("");
        Validator.validate(body); // pass
        try {
            body.value(null);
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("value is required"));
        }
    }

    @Test
    public void validateLocalDate() {
        LocalDateWrapper body = new LocalDateWrapper();
        body.value(LocalDate.of(1, 2, 3));
        Validator.validate(body); // pass
        try {
            body.value(null);
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("value is required"));
        }
    }

    @Test
    public void validateList() {
        ListWrapper body = new ListWrapper();
        try {
            body.list(null);
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("list is required"));
        }
        body.list(new ArrayList<>());
        Validator.validate(body); // pass
        StringWrapper wrapper = new StringWrapper();
        wrapper.value("valid");
        body.list.add(wrapper);
        Validator.validate(body); // pass
        body.list.add(null);
        Validator.validate(body); // pass
        body.list.add(new StringWrapper());
        try {
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("list.value is required"));
        }
    }

    @Test
    public void validateMap() {
        MapWrapper body = new MapWrapper();
        try {
            body.map(null);
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("map is required"));
        }
        body.map(new HashMap<>());
        Validator.validate(body); // pass
        StringWrapper wrapper = new StringWrapper();
        wrapper.value("valid");
        body.map.put(LocalDate.of(1, 2, 3), wrapper);
        Validator.validate(body); // pass
        body.map.put(LocalDate.of(1, 2, 3), null);
        Validator.validate(body); // pass
        body.map.put(LocalDate.of(1, 2, 3), new StringWrapper());
        try {
            Validator.validate(body); // fail
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("map.value is required"));
        }
    }

    @Test
    public void validateObject() {
        Product product = new Product();
        Validator.validate(product);
    }

    @Test
    public void validateRecursive() {
        TextNode textNode = new TextNode("\"\"");
        Validator.validate(textNode);
    }

    public final class IntWrapper {
        @JsonProperty(required = true)
        private int value;
        private Object nullable;

        public int value() {
            return value;
        }

        public void value(int value) {
            this.value = value;
        }

        public Object nullable() {
            return nullable;
        }

        public void nullable(Object nullable) {
            this.nullable = nullable;
        }
    }

    public final class IntegerWrapper {
        @JsonProperty(required = true)
        private Integer value;

        public Integer value() {
            return value;
        }

        public void value(Integer value) {
            this.value = value;
        }
    }

    public final class StringWrapper {
        @JsonProperty(required = true)
        private String value;

        public String value() {
            return value;
        }

        public void value(String value) {
            this.value = value;
        }
    }

    public final class LocalDateWrapper {
        @JsonProperty(required = true)
        private LocalDate value;

        public LocalDate value() {
            return value;
        }

        public void value(LocalDate value) {
            this.value = value;
        }
    }

    public final class ListWrapper {
        @JsonProperty(required = true)
        private List<StringWrapper> list;

        public List<StringWrapper> list() {
            return list;
        }

        public void list(List<StringWrapper> list) {
            this.list = list;
        }
    }

    public final class MapWrapper {
        @JsonProperty(required = true)
        private Map<LocalDate, StringWrapper> map;

        public Map<LocalDate, StringWrapper> map() {
            return map;
        }

        public void map(Map<LocalDate, StringWrapper> map) {
            this.map = map;
        }
    }

    public enum Color {
        RED,
        GREEN,
        Blue
    }

    public final class EnumWrapper {
        @JsonProperty(required = true)
        private Color color;

        public Color color() {
            return color;
        }

        public void color(Color color) {
            this.color = color;
        }
    }

    public final class Product {
        private String id;
        private String tag;

        public String id() {
            return id;
        }

        public void id(String id) {
            this.id = id;
        }

        public String tag() {
            return tag;
        }

        public void tag(String tag) {
            this.tag = tag;
        }
    }
}
