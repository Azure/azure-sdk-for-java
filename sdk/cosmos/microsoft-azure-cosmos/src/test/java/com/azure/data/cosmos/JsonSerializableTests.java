// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Document;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import org.testng.annotations.Test;

import java.io.Serializable;

import static com.azure.data.cosmos.BridgeInternal.setProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

public class JsonSerializableTests {

    public static class Pojo implements Serializable {
        int a;
        int b;

        public Pojo(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @JsonCreator
        public Pojo(@JsonProperty("a") String a, @JsonProperty("b") String b) {
            this.a = Integer.parseInt(a);
            this.b = Integer.parseInt(b);
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public void setA(int a) {
            this.a = a;
        }

        public void setB(int b) {
            this.b = b;
        }

    }

    public enum enums {
        first, second, third
    }

    @Test(groups = { "unit" })
    public void getObjectAndCastToClass() {
        Document document = new Document();
        // numeric values
        setProperty(document, "intValue", Integer.MAX_VALUE);
        setProperty(document, "doubleValue", Double.MAX_VALUE);
        setProperty(document, "longValue", Long.MAX_VALUE);

        assertThat(document.getObject("intValue", Integer.class).intValue()).isEqualTo(Integer.MAX_VALUE);
        assertThat(document.getObject("doubleValue", Double.class).doubleValue()).isEqualTo(Double.MAX_VALUE);
        assertThat(document.getObject("longValue", Long.class).longValue()).isEqualTo(Long.MAX_VALUE);

        // string
        setProperty(document, "stringValue", "stringField");
        assertThat(document.getObject("stringValue", String.class)).isEqualTo("stringField");

        // boolean
        setProperty(document, "boolValue", true);
        assertThat(document.getObject("boolValue", Boolean.class)).isEqualTo(true);

        // enum
        setProperty(document, "enumValue", "third");
        assertThat(document.getObject("enumValue", enums.class)).isEqualTo(enums.third);

        // Pojo
        Pojo pojo = new Pojo(1, 2);
        setProperty(document, "pojoValue", pojo);
        Pojo readPojo = document.getObject("pojoValue", Pojo.class);
        assertThat(readPojo.getA()).isEqualTo(pojo.getA());
        assertThat(readPojo.getB()).isEqualTo(pojo.getB());

        // JsonSerializable
        Document innerDocument = new Document();
        innerDocument.id("innerDocument");
        setProperty(document, "innerDocument", innerDocument);
        Document readInnerDocument = document.getObject("innerDocument", Document.class);
        assertThat(readInnerDocument.id()).isEqualTo(innerDocument.id());
    }

    @Test(groups = { "unit" })
    public void objectMapperInvalidJsonNoQuotesForFieldAndValue() {
        // INVALID Json - field and value must be quoted
        try {
            new Document("{ field: value }");
            fail("failure expected");
        } catch (Exception e) {
            assertThat(e.getCause() instanceof JsonParseException).isTrue();
        }
    }

    @Test(groups = { "unit" })
    public void objectMapperInvalidJsonNoQuotesForField() {
        // INVALID Json - field must be quoted
        try {
            new Document("{ field: 'value' }");
            fail("failure expected");
        } catch (Exception e) {
            assertThat(e.getCause() instanceof JsonParseException).isTrue();
        }
    }

    @Test(groups = { "unit" })
    public void objectMapperInvalidJsonNoDuplicatesAllowed() {
        // INVALID Json - duplicates must not exist in Json string
        try {
            new Document("{ 'field': 'value1', 'field': 'value2' }");
            fail("failure expected");
        } catch (Exception e) {
            assertThat(e.getCause() instanceof JsonParseException).isTrue();
        }
    }

    @Test(groups = { "unit" })
    public void objectMapperValidJsonWithSingleQuotesAndTrailingComma() {
        Document document = null;

        // Valid Json - Single quotes and trailing commas allowed in Json string
        document = new Document("{ 'field1': 'value1', 'field2': 'value2', }");
        assertThat(document.toJson().equals("{\"field1\":\"value1\",\"field2\":\"value2\"}")).isEqualTo(true);
    }
}
