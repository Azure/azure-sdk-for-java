// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.tree.JsonArray;
import com.azure.json.tree.JsonElement;
import com.azure.json.tree.JsonObject;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JsonBuilderTests {

    //Section 1:Testing parsing Singular JSON Objects -----------------------------------------------------------------
    //Section 1.1:Object - Single String
    @Test
    public void objectSingleStringCorrectType() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleStringCorrectInputType() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isString());
    }

    @Test
    public void objectSingleStringCorrectInputValue() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertEquals("Value", result.toString());
    }

    @Test
    public void objectSingleStringCorrectToJSON() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 1.2:Object - Single Number
    @Test
    public void objectSingleNumberCorrectType() throws IOException {
        String input = "{\"Key\":1}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":1}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":1}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertEquals("1", result.toString());
    }

    @Test
    public void objectSingleNumberCorrectToJSON() throws IOException {
        String input = "{\"Key:\":1}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 1.2.2:Object - Decimal Number
    @Test
    public void objectSingleDecimalNumberCorrectType() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleDecimalNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleDecimalNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertEquals("1.23", result.toString());
    }

    @Test
    public void objectSingleDecimalNumberCorrectToJSON() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 1.2.3:Object - Negative Number
    @Test
    public void objectSingleNegativeNumberCorrectType() throws IOException {
        String input = "{\"Key\":-1}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleNegativeNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":-1}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleNegativeNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":-1}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertEquals("-1", result.toString());
    }

    @Test
    public void objectSingleNegativeNumberCorrectToJSON() throws IOException {
        String input = "{\"Key:\":-1}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    /*Section 1.2.4:Object - Infinite Number
    //Not entirely sure how to input infinite numbers. Might be using the wrong input.
    @Test
    public void objectSingleInfiniteNumberCorrectType() throws IOException {
        String input = "{\"Key\":inf}";
        JsonElement output = builder.deserialize(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleInfiniteNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":inf}";
        JsonElement output = builder.deserialize(input);
        JsonElement result = ((JsonObject)output).getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleInfiniteNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":inf}";
        JsonElement output = builder.deserialize(input);
        JsonElement result = ((JsonObject)output).getProperty("Key");
        assertEquals("inf", result.toString());
    }

    @Test
    public void objectSingleInfiniteNumberCorrectToJSON() throws IOException {
        String input = "{\"Key\":inf}";
        JsonElement output = builder.deserialize(input);
        assertEquals(input, ((JsonObject)output).toJson());
    }
    */

    //Section 1.3:Object - Single Boolean
    @Test
    public void objectSingleBooleanCorrectType() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleBooleanCorrectInputType() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isBoolean());
    }

    @Test
    public void objectSingleBooleanCorrectInputValue() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertEquals("true", result.toString());
    }

    @Test
    public void objectSingleBooleanCorrectToJSON() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 1.4:Object - Single NULL
    @Test
    public void objectSingleNullCorrectType() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleNullCorrectInputType() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isNull());
    }

    @Test
    public void objectSingleNullCorrectInputValue() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertEquals("null", result.toString());
    }

    @Test
    public void objectSingleNullCorrectToJSON() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 1.5:Object - Inner Object With String
    @Test
    public void objectObjectStringCorrectType() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectObjectStringCorrectInputType() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isObject());
    }

    @Test
    public void objectObjectStringCorrectInnerType() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement outerKey = ((JsonObject) output).getProperty("Key");
        JsonElement result = ((JsonObject) outerKey).getProperty("InnerKey");
        assertTrue(result.isString());
    }

    @Test
    public void objectObjectStringCorrectInputValue() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement outerKey = ((JsonObject) output).getProperty("Key");
        JsonElement result = ((JsonObject) outerKey).getProperty("InnerKey");
        assertEquals("Value", result.toString());
    }

    @Test
    public void objectObjectStringCorrectToJSON() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 1.6:Object - Inner Array With String
    @Test
    public void objectArrayStringCorrectType() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectArrayStringCorrectInputType() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonObject) output).getProperty("Key");
        assertTrue(result.isArray());
    }

    @Test
    public void objectArrayStringCorrectInnerType() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement innerArray = ((JsonObject) output).getProperty("Key");
        JsonElement result = ((JsonArray) innerArray).getElement(0);
        assertTrue(result.isString());
    }

    @Test
    public void objectArrayStringCorrectInputValue() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = JsonElement.fromString(input);
        JsonElement innerArray = ((JsonObject) output).getProperty("Key");
        JsonElement result = ((JsonArray) innerArray).getElement(0);
        assertEquals("Value", result.toString());
    }

    @Test
    public void objectArrayStringCorrectToJSON() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //////////////////////////////////////////////////////////////////////////////

    //Section 2:Parsing Single Json Arrays ----------------------------------------------------------------------------
    //Section 2.1:Array - Single String
    @Test
    public void arraySingleStringCorrectType() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleStringCorrectInputType() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isString());
    }

    @Test
    public void arraySingleStringCorrectValue() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertEquals("Value1", result.toString());
    }

    @Test
    public void arraySingleStringCorrectToJSON() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.2: Array - Single Number
    @Test
    public void arraySingleNumberCorrectType() throws IOException {
        String input = "[1]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleNumberCorrectInputType() throws IOException {
        String input = "[1]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleNumberCorrectValue() throws IOException {
        String input = "[1]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertEquals("1", result.toString());
    }

    @Test
    public void arraySingleNumberCorrectToJSON() throws IOException {
        String input = "[1]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.2.1: Array - Number with Decimal
    @Test
    public void arraySingleDecimalNumberCorrectType() throws IOException {
        String input = "[1.23]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleDecimalNumberCorrectInputType() throws IOException {
        String input = "[1.23]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleDecimalNumberCorrectValue() throws IOException {
        String input = "[1.23]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertEquals("1.23", result.toString());
    }

    @Test
    public void arraySingleDecimalNumberCorrectToJSON() throws IOException {
        String input = "[1.23]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.2.2: Array - Negative number
    @Test
    public void arraySingleNegativeNumberCorrectType() throws IOException {
        String input = "[-1]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleNegativeNumberCorrectInputType() throws IOException {
        String input = "[-1]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleNegativeNumberCorrectValue() throws IOException {
        String input = "[-1]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertEquals("-1", result.toString());
    }

    @Test
    public void arraySingleNegativeNumberCorrectToJSON() throws IOException {
        String input = "[-1]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.2.3: Array - Infinite number
    //Still not sure how to handle the input for this.
    /*
    @Test
    public void arraySingleInfiniteNumberCorrectType() throws IOException {
        String input = "[inf]";
        JsonElement output = builder.deserialize(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleInfiniteNumberCorrectInputType() throws IOException {
        String input = "[nf]";
        JsonElement output = builder.deserialize(input);
        JsonElement result = ((JsonArray)output).getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleInfiniteNumberCorrectValue() throws IOException {
        String input = "[inf]";
        JsonElement output = builder.deserialize(input);
        JsonElement result = ((JsonArray)output).getElement(0);
        assertEquals("inf", result.toString());
    }

    @Test
    public void arraySingleInfiniteNumberCorrectToJSON() throws IOException {
        String input = "[inf]";
        JsonElement output = builder.deserialize(input);
        assertEquals(input, ((JsonArray)output).toJson());
    }
    */

    //Section 2.3:Array - Single Boolean
    @Test
    public void arraySingleBooleanCorrectType() throws IOException {
        String input = "[false]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleBooleanCorrectInputType() throws IOException {
        String input = "[false]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isBoolean());
    }

    @Test
    public void arraySingleBooleanCorrectValue() throws IOException {
        String input = "[false]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertEquals("false", result.toString());
    }

    @Test
    public void arraySingleBooleanCorrectToJSON() throws IOException {
        String input = "[false]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.4:Array - Single Null
    @Test
    public void arraySingleNullCorrectType() throws IOException {
        String input = "[null]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleNullCorrectInputType() throws IOException {
        String input = "[null]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isNull());
    }

    @Test
    public void arraySingleNullCorrectValue() throws IOException {
        String input = "[null]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertEquals("null", result.toString());
    }

    @Test
    public void arraySingleNullCorrectToJSON() throws IOException {
        String input = "[null]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.5:Array - Inner Object With String
    @Test
    public void arrayObjectStringCorrectType() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arrayObjectStringCorrectInputType() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isObject());
    }

    @Test
    public void arrayObjectStringCorrectInnerType() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement innerArray = ((JsonArray) output).getElement(0);
        JsonElement result = ((JsonObject) innerArray).getProperty("Key");
        assertTrue(result.isString());
    }

    @Test
    public void arrayObjectStringCorrectValue() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement innerArray = ((JsonArray) output).getElement(0);
        JsonElement result = ((JsonObject) innerArray).getProperty("Key");
        assertEquals("Value1", result.toString());
    }

    @Test
    public void arrayObjectStringCorrectToJSON() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 2.6:Array - Inner Array With String
    @Test
    public void arrayArrayStringCorrectType() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = JsonElement.fromString(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arrayArrayStringCorrectInputType() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement result = ((JsonArray) output).getElement(0);
        assertTrue(result.isArray());
    }

    @Test
    public void arrayArrayStringCorrectInnerType() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement outerArray = ((JsonArray) output).getElement(0);
        JsonElement result = ((JsonArray) outerArray).getElement(0);
        assertTrue(result.isString());
    }

    @Test
    public void arrayArrayStringCorrectValue() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = JsonElement.fromString(input);
        JsonElement outerArray = ((JsonArray) output).getElement(0);
        JsonElement result = ((JsonArray) outerArray).getElement(0);
        assertEquals("Value1", result.toString());
    }

    @Test
    public void arrayArrayStringCorrectToJSON() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 3: Longer JSON Inputs - Object
    @Test
    public void objectMultiSameType() throws IOException {
        String input = "{\"Key\":\"Value\",\"Key2\":\"Value2\"}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    @Test
    public void objectMultiDifferentType() throws IOException {
        String input = "{\"Key\":\"Value\",\"Key2\":1}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    @Test
    public void objectDoubleLayerObjects() throws IOException {
        String input = "{\"Key1\":{\"KeyInner1\":\"Value\"},\"Key2\":{\"InnerKey2\":1}}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    @Test
    public void objectDoubleDoubleLayerObjects() throws IOException {
        String input = "{\"Key1\":{\"KeyInner1\":\"Value\"},\"Key2\":{\"InnerKey2\":1}}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonObject) output).toJsonString());
    }

    //Section 4: Longer JSON Inputs - Array
    @Test
    public void arrayMultiSameType() throws IOException {
        String input = "[\"Value1\",\"Value2\"]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    @Test
    public void arrayMultiDifferentType() throws IOException {
        String input = "[\"Value1\",1]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals(input, ((JsonArray) output).toJsonString());
    }

    //Section 5: Invalid values
    @Test
    public void notJSONInput() {
        String input = "OH NO";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonNoKey() {
        String input = "{\"Text\"}";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonNoValue() {
        String input = "{\"Key\":}";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonInvalidEntry() {
        String input = "{\"Key\": OHNOWHATISTHIS!!!}";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonInvalidArrayEntry() {
        String input = "[OHNOWHATISTHIS!!!]";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonObjectMissingEnd() {
        String input = "{\"Key\":\"Value\"";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonArrayMissingEnd() {
        String input = "[\"Value1\"";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonArrayAdditionalEnd() {
        String input = "[\"Value1\"]]";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    @Test
    public void jsonArrayAdditionalStart() {
        String input = "[[\"Value1\"]";
        assertThrows(IOException.class, () -> JsonElement.fromString(input));
    }

    //----Testing different spacing in jsonInput

    @Test
    public void jsonObjectSpacingInput() throws IOException {
        String input = "{\"Key\": \"Value\", \"Key2\": 5}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("{\"Key\":\"Value\",\"Key2\":5}", output.toJsonString());
    }

    @Test
    public void jsonObjectExcessiveSpacingInput() throws IOException {
        String input
            = "{                        \"Key\":                                     \"Value\",                          \"Key2\":                                         5}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("{\"Key\":\"Value\",\"Key2\":5}", output.toJsonString());
    }

    @Test
    public void jsonObjectFancyFormatInput() throws IOException {
        String input = "{\n\t\"Key\": \"Value\",\n\t\"Key2\": 5\n}"; //Would look something like the spaced format some JSON files use.
        JsonElement output = JsonElement.fromString(input);
        assertEquals("{\"Key\":\"Value\",\"Key2\":5}", output.toJsonString());
    }

    @Test
    public void jsonArraySpacingInput() throws IOException {
        String input = "[\"Word\", 1, null, true]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("[\"Word\",1,null,true]", output.toJsonString());
    }

    @Test
    public void jsonArrayExcessiveSpacingInput() throws IOException {
        String input
            = "[\"Word\"                   ,                          1,               null,              true]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("[\"Word\",1,null,true]", output.toJsonString());
    }

    @Test
    public void jsonArrayFancyFormatInput() throws IOException {
        String input = "[\n\t\"Word\",\n\t 1,\n\t null,\n\t true]"; //Would look something like the spaced format some JSON files use.
        JsonElement output = JsonElement.fromString(input);
        assertEquals("[\"Word\",1,null,true]", output.toJsonString());
    }

    // Section 5: Partial and Complete JSON parsing tests. This is to ascertain that the library can handle situations where the streams hold
    // multiple JSON objects, or start part way through

    @Test
    public void jsonPartialObject() throws IOException {
        String input = "{\"Key\":\"Value\"} {\"Key2\":\"Value2\"}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("{\"Key\":\"Value\"}", output.toJsonString());
    }

    @Test
    public void jsonPartialArray() throws IOException {
        String input = "[\"Value1\"] [\"Value2\"]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("[\"Value1\"]", output.toJsonString());
    }

    @Test
    public void jsonPartialObjectArray() throws IOException {
        String input = "{\"Key\":\"Value\"} [\"Value2\"]";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("{\"Key\":\"Value\"}", output.toJsonString());
    }

    @Test
    public void jsonPartialArrayObject() throws IOException {
        String input = "[\"Value1\"] {\"Key2\":\"Value2\"}";
        JsonElement output = JsonElement.fromString(input);
        assertEquals("[\"Value1\"]", output.toJsonString());
    }

}
