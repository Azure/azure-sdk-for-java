/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.core.utils;

import com.microsoft.windowsazure.core.pipeline.ConnectionStringField;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class ParsedConnectionStringTest {

    private static class OneField extends ParsedConnectionString {
        private String aField;

        public OneField(String connectionString)
                throws ConnectionStringSyntaxException {
            super(connectionString);
        }

        public String getAField() {
            return aField;
        }

        @SuppressWarnings("unused")
        protected void setAField(String aField) {
            this.aField = aField;
        }
    }

    @Test
    public void shouldSuccessfullyParseValidStringWithOneField()
            throws Exception {
        OneField cs = new OneField("AField=avalue");

        assertEquals("avalue", cs.getAField());
    }

    private static class ThreeFields extends ParsedConnectionString {
        private String fieldOne;
        private String fieldTwo;
        private int fieldThree;

        public ThreeFields(String connectionString)
                throws ConnectionStringSyntaxException {
            super(connectionString);
        }

        public String getFieldOne() {
            return fieldOne;
        }

        @SuppressWarnings("unused")
        protected void setFieldOne(String fieldOne) {
            this.fieldOne = fieldOne;
        }

        public String getFieldTwo() {
            return fieldTwo;
        }

        @SuppressWarnings("unused")
        protected void setFieldTwo(String fieldTwo) {
            this.fieldTwo = fieldTwo;
        }

        public int getFieldThree() {
            return fieldThree;
        }

        @ConnectionStringField(name = "fieldthree")
        protected void setNumericField(String fieldThree) {
            this.fieldThree = Integer.parseInt(fieldThree);
        }
    }

    @Test
    public void shouldSuccessfullyParseValidStringWithMultipleFields()
            throws Exception {
        ThreeFields cs = new ThreeFields(
                "FieldOne=hello;FieldTwo=world;FieldThree=27");

        assertEquals("hello", cs.getFieldOne());
        assertEquals("world", cs.getFieldTwo());
        assertEquals(27, cs.getFieldThree());
    }

    @Test
    public void shouldSuccessFullyParseValisStringWithQuotedKeysAndValues()
            throws Exception {
        ThreeFields cs = new ThreeFields(
                "FieldOne=hello;'FieldTwo'=world;FieldThree='27'");

        assertEquals("hello", cs.getFieldOne());
        assertEquals("world", cs.getFieldTwo());
        assertEquals(27, cs.getFieldThree());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldThrowForFieldThatDoesntExist() throws Exception {
        exception.expect(ConnectionStringSyntaxException.class);
        new OneField("nosuchfield=nothing");
    }

    @Test
    public void shouldNotThrowIfValueMissing() throws Exception {
        ThreeFields cs = new ThreeFields(
                "  FieldOne=  hello; FieldTwo  =;FieldThree=19 ");
        assertEquals("", cs.getFieldTwo());
    }

    @Test
    public void shouldIgnoreEmptyPairsAndExtraSemicolons() throws Exception {
        ThreeFields cs = new ThreeFields(
                "FieldOne=hello;;  ; 'FieldTwo'=world;FieldThree='27';");

        assertEquals("hello", cs.getFieldOne());
        assertEquals("world", cs.getFieldTwo());
        assertEquals(27, cs.getFieldThree());
    }
}
