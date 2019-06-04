package com.microsoft.azure.cosmosdb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.io.Serializable;

import org.testng.annotations.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;

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
        document.set("intValue", Integer.MAX_VALUE);
        document.set("doubleValue", Double.MAX_VALUE);
        document.set("longValue", Long.MAX_VALUE);

        assertThat(document.getObject("intValue", Integer.class).intValue()).isEqualTo(Integer.MAX_VALUE);
        assertThat(document.getObject("doubleValue", Double.class).doubleValue()).isEqualTo(Double.MAX_VALUE);
        assertThat(document.getObject("longValue", Long.class).longValue()).isEqualTo(Long.MAX_VALUE);

        // string
        document.set("stringValue", "stringField");
        assertThat(document.getObject("stringValue", String.class)).isEqualTo("stringField");

        // boolean
        document.set("boolValue", true);
        assertThat(document.getObject("boolValue", Boolean.class)).isEqualTo(true);

        // enum
        document.set("enumValue", "third");
        assertThat(document.getObject("enumValue", enums.class)).isEqualTo(enums.third);

        // Pojo
        Pojo pojo = new Pojo(1, 2);
        document.set("pojoValue", pojo);
        Pojo readPojo = document.getObject("pojoValue", Pojo.class);
        assertThat(readPojo.getA()).isEqualTo(pojo.getA());
        assertThat(readPojo.getB()).isEqualTo(pojo.getB());

        // JsonSerializable
        Document innerDocument = new Document();
        innerDocument.setId("innerDocument");
        document.set("innerDocument", innerDocument);
        Document readInnerDocument = document.getObject("innerDocument", Document.class);
        assertThat(readInnerDocument.getId()).isEqualTo(innerDocument.getId());
    }

    @Test(groups = { "unit" })
    public void objectMapperInvalidJsonNoQuotesForFieldAndValue() {
        // Invalid Json - field and value must be quoted
        try {
            new Document("{ field: value }");
            fail("failure expected");
        } catch (Exception e) {
            assertThat(e.getCause() instanceof JsonParseException).isTrue();
        }
    }

    @Test(groups = { "unit" })
    public void objectMapperInvalidJsonNoQuotesForField() {
        // Invalid Json - field must be quoted
        try {
            new Document("{ field: 'value' }");
            fail("failure expected");
        } catch (Exception e) {
            assertThat(e.getCause() instanceof JsonParseException).isTrue();
        }
    }

    @Test(groups = { "unit" })
    public void objectMapperInvalidJsonNoDuplicatesAllowed() {
        // Invalid Json - duplicates must not exist in Json string
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
