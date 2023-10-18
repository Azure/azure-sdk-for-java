package com.azure.json;

import org.junit.jupiter.api.*;

import java.io.IOException;


import static org.junit.jupiter.api.Assertions.*;

public class JsonBuilderTests {

    JsonBuilder builder = new JsonBuilder();

    //Section 0:Check initialization is correct -----------------------------------------------------------------------
//    TODO these tests become irrelevant
//     @Test
//    public void initializeObject(){
//        JsonElement result = builder.createObject();
//        boolean check = result != null && result.isObject();
//        assertTrue(check);
//    }
//
//    @Test
//    public void initializeArray(){
//        JsonElement result = builder.createArray();
//        boolean check = result != null && result.isArray();
//        assertTrue(check);
//    }


    //Section 1:Testing parsing Singular JSON Objects -----------------------------------------------------------------
    //Section 1.1:Object - Single String
    @Test
    public void objectSingleStringCorrectType() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleStringCorrectInputType() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isString());
    }

    @Test
    public void objectSingleStringCorrectInputValue() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("Value", result.toString());
    }

    @Test
    public void objectSingleStringCorrectToJSON() throws IOException {
        String input = "{\"Key\":\"Value\"}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }


    //Section 1.2:Object - Single Number
    @Test
    public void objectSingleNumberCorrectType() throws IOException {
        String input = "{\"Key\":1}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":1}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":1}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("1", result.toString());
    }

    @Test
    public void objectSingleNumberCorrectToJSON() throws IOException {
        String input = "{\"Key:\":1}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    //Section 1.2.2:Object - Decimal Number
    @Test
    public void objectSingleDecimalNumberCorrectType() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleDecimalNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleDecimalNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("1.23", result.toString());
    }

    @Test
    public void objectSingleDecimalNumberCorrectToJSON() throws IOException {
        String input = "{\"Key\":1.23}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    //Section 1.2.3:Object - Negative Number
    @Test
    public void objectSingleNegativeNumberCorrectType() throws IOException {
        String input = "{\"Key\":-1}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleNegativeNumberCorrectInputType() throws IOException {
        String input = "{\"Key\":-1}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleNegativeNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":-1}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("-1", result.toString());
    }

    @Test
    public void objectSingleNegativeNumberCorrectToJSON() throws IOException {
        String input = "{\"Key:\":-1}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
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
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isNumber());
    }

    @Test
    public void objectSingleInfiniteNumberCorrectInputValue() throws IOException {
        String input = "{\"Key\":inf}";
        JsonElement output = builder.deserialize(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("inf", result.toString());
    }

    @Test
    public void objectSingleInfiniteNumberCorrectToJSON() throws IOException {
        String input = "{\"Key\":inf}";
        JsonElement output = builder.deserialize(input);
        assertEquals(input, output.asObject().toJson());
    }
    */

    //Section 1.3:Object - Single Boolean
    @Test
    public void objectSingleBooleanCorrectType() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleBooleanCorrectInputType() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isBoolean());
    }

    @Test
    public void objectSingleBooleanCorrectInputValue() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("true", result.toString());
    }

    @Test
    public void objectSingleBooleanCorrectToJSON() throws IOException {
        String input = "{\"Key\":true}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    //Section 1.4:Object - Single NULL
    @Test
    public void objectSingleNullCorrectType() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectSingleNullCorrectInputType() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isNull());
    }

    @Test
    public void objectSingleNullCorrectInputValue() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertEquals("null", result.toString());
    }

    @Test
    public void objectSingleNullCorrectToJSON() throws IOException {
        String input = "{\"Key\":null}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    //Section 1.5:Object - Inner Object With String
    @Test
    public void objectObjectStringCorrectType() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectObjectStringCorrectInputType() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isObject());
    }

    @Test
    public void objectObjectStringCorrectInnerType() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = builder.build(input);
        JsonElement outerKey = output.asObject().getProperty("Key");
        JsonElement result = outerKey.asObject().getProperty("InnerKey");
        assertTrue(result.isString());
    }

    @Test
    public void objectObjectStringCorrectInputValue() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = builder.build(input);
        JsonElement outerKey = output.asObject().getProperty("Key");
        JsonElement result = outerKey.asObject().getProperty("InnerKey");
        assertEquals("Value", result.toString());
    }

    @Test
    public void objectObjectStringCorrectToJSON() throws IOException {
        String input = "{\"Key\":{\"InnerKey\":\"Value\"}}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    //Section 1.6:Object - Inner Array With String
    @Test
    public void objectArrayStringCorrectType() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = builder.build(input);
        assertTrue(output.isObject());
    }

    @Test
    public void objectArrayStringCorrectInputType() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = builder.build(input);
        JsonElement result = output.asObject().getProperty("Key");
        assertTrue(result.isArray());
    }

    @Test
    public void objectArrayStringCorrectInnerType() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = builder.build(input);
        JsonElement innerArray = output.asObject().getProperty("Key");
        JsonElement result = innerArray.asArray().getElement(0);
        assertTrue(result.isString());
    }

    @Test
    public void objectArrayStringCorrectInputValue() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = builder.build(input);
        JsonElement innerArray = output.asObject().getProperty("Key");
        JsonElement result = innerArray.asArray().getElement(0);
        assertEquals("Value", result.toString());
    }

    @Test
    public void objectArrayStringCorrectToJSON() throws IOException {
        String input = "{\"Key\":[\"Value\"]}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }


    //////////////////////////////////////////////////////////////////////////////

    //Section 2:Parsing Single Json Arrays ----------------------------------------------------------------------------
    //Section 2.1:Array - Single String
    @Test
    public void arraySingleStringCorrectType() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleStringCorrectInputType() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isString());
    }

    @Test
    public void arraySingleStringCorrectValue() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("Value1", result.toString());
    }


    @Test
    public void arraySingleStringCorrectToJSON() throws IOException {
        String input = "[\"Value1\"]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 2.2: Array - Single Number
    @Test
    public void arraySingleNumberCorrectType() throws IOException {
        String input = "[1]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleNumberCorrectInputType() throws IOException {
        String input = "[1]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleNumberCorrectValue() throws IOException {
        String input = "[1]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("1", result.toString());
    }

    @Test
    public void arraySingleNumberCorrectToJSON() throws IOException {
        String input = "[1]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 2.2.1: Array - Number with Decimal
    @Test
    public void arraySingleDecimalNumberCorrectType() throws IOException {
        String input = "[1.23]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleDecimalNumberCorrectInputType() throws IOException {
        String input = "[1.23]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleDecimalNumberCorrectValue() throws IOException {
        String input = "[1.23]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("1.23", result.toString());
    }

    @Test
    public void arraySingleDecimalNumberCorrectToJSON() throws IOException {
        String input = "[1.23]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 2.2.2: Array - Negative number
    @Test
    public void arraySingleNegativeNumberCorrectType() throws IOException {
        String input = "[-1]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleNegativeNumberCorrectInputType() throws IOException {
        String input = "[-1]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleNegativeNumberCorrectValue() throws IOException {
        String input = "[-1]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("-1", result.toString());
    }

    @Test
    public void arraySingleNegativeNumberCorrectToJSON() throws IOException {
        String input = "[-1]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
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
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isNumber());
    }

    @Test
    public void arraySingleInfiniteNumberCorrectValue() throws IOException {
        String input = "[inf]";
        JsonElement output = builder.deserialize(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("inf", result.toString());
    }

    @Test
    public void arraySingleInfiniteNumberCorrectToJSON() throws IOException {
        String input = "[inf]";
        JsonElement output = builder.deserialize(input);
        assertEquals(input, output.asArray().toJson());
    }
    */


    //Section 2.3:Array - Single Boolean
    @Test
    public void arraySingleBooleanCorrectType() throws IOException {
        String input = "[false]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleBooleanCorrectInputType() throws IOException {
        String input = "[false]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isBoolean());
    }

    @Test
    public void arraySingleBooleanCorrectValue() throws IOException {
        String input = "[false]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("false", result.toString());
    }

    @Test
    public void arraySingleBooleanCorrectToJSON() throws IOException {
        String input = "[false]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 2.4:Array - Single Null
    @Test
    public void arraySingleNullCorrectType() throws IOException {
        String input = "[null]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arraySingleNullCorrectInputType() throws IOException {
        String input = "[null]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isNull());
    }

    @Test
    public void arraySingleNullCorrectValue() throws IOException {
        String input = "[null]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertEquals("null", result.toString());
    }

    @Test
    public void arraySingleNullCorrectToJSON() throws IOException {
        String input = "[null]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 2.5:Array - Inner Object With String
    @Test
    public void arrayObjectStringCorrectType() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arrayObjectStringCorrectInputType() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isObject());
    }


    @Test
    public void arrayObjectStringCorrectInnerType() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = builder.build(input);
        JsonElement innerArray = output.asArray().getElement(0);
        JsonElement result = innerArray.asObject().getProperty("Key");
        assertTrue(result.isString());
    }

    @Test
    public void arrayObjectStringCorrectValue() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = builder.build(input);
        JsonElement innerArray = output.asArray().getElement(0);
        JsonElement result = innerArray.asObject().getProperty("Key");
        assertEquals("Value1", result.toString());
    }

    @Test
    public void arrayObjectStringCorrectToJSON() throws IOException {
        String input = "[{\"Key\":\"Value1\"}]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 2.6:Array - Inner Array With String
    @Test
    public void arrayArrayStringCorrectType() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = builder.build(input);
        assertTrue(output.isArray());
    }

    @Test
    public void arrayArrayStringCorrectInputType() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = builder.build(input);
        JsonElement result = output.asArray().getElement(0);
        assertTrue(result.isArray());
    }

    @Test
    public void arrayArrayStringCorrectInnerType() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = builder.build(input);
        JsonElement outerArray = output.asArray().getElement(0);
        JsonElement result = outerArray.asArray().getElement(0);
        assertTrue(result.isString());
    }

    @Test
    public void arrayArrayStringCorrectValue() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = builder.build(input);
        JsonElement outerArray = output.asArray().getElement(0);
        JsonElement result = outerArray.asArray().getElement(0);
        assertEquals("Value1", result.toString());
    }

    @Test
    public void arrayArrayStringCorrectToJSON() throws IOException {
        String input = "[[\"Value1\"]]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 3: Longer JSON Inputs - Object
    @Test
    public void objectMultiSameType() throws IOException {
        String input = "{\"Key\":\"Value\",\"Key2\":\"Value2\"}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    @Test
    public void objectMultiDifferentType() throws IOException {
        String input = "{\"Key\":\"Value\",\"Key2\":1}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    @Test
    public void objectDoubleLayerObjects() throws IOException {
        String input = "{\"Key1\":{\"KeyInner1\":\"Value\"},\"Key2\":{\"InnerKey2\":1}}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }

    @Test
    public void objectDoubleDoubleLayerObjects() throws IOException {
        String input = "{\"Key1\":{\"KeyInner1\":\"Value\"},\"Key2\":{\"InnerKey2\":1}}";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asObject().toJson());
    }


    //Section 4: Longer JSON Inputs - Array
    @Test
    public void arrayMultiSameType() throws IOException{
        String input = "[\"Value1\",\"Value2\"]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    @Test
    public void arrayMultiDifferentType() throws IOException{
        String input = "[\"Value1\",1]";
        JsonElement output = builder.build(input);
        assertEquals(input, output.asArray().toJson());
    }

    //Section 5: Invalid values
    @Test
    public void notJSONInput() {
        String input = "OH NO";
        assertThrows(IOException.class, ()->builder.build(input));
    }

    @Test
    public void jsonNoKey() throws IOException {
        String input = "{\"Text\"}";
        JsonElement output = builder.build(input);
        assertEquals("{}", output.asObject().toJson());
    }

    @Test
    public void jsonNoValue() throws IOException {
        String input = "{\"Key\":}";
        JsonElement output = builder.build(input);
        assertEquals("{}", output.asObject().toJson());
    }

    @Test
    public void jsonInvalidEntry() throws IOException {
        String input = "{\"Key\": OHNOWHATISTHIS!!!}";
        JsonElement output = builder.build(input);
        assertEquals("{}", output.asObject().toJson());
    }

    @Test
    public void jsonInvalidArrayEntry(){
        String input = "[OHNOWHATISTHIS!!!]";
        assertThrows(IOException.class, ()->builder.build(input));
    }

    @Test
    public void jsonObjectMissingEnd(){
        String input = "{\"Key\":\"Value\"";
        assertThrows(IOException.class, ()->builder.build(input));
    }

    @Test
    public void jsonArrayMissingEnd(){
        String input = "[\"Value1\"";
        assertThrows(IOException.class, ()->builder.build(input));
    }

    @Test
    public void jsonArrayAdditionalEnd(){
        String input = "[\"Value1\"]]";
        assertThrows(IOException.class, ()->builder.build(input));
    }

    @Test
    public void jsonArrayAdditionalStart(){
        String input = "[[\"Value1\"]";
        assertThrows(IOException.class, ()->builder.build(input));
    }


    //----Testing different spacing in jsonInput

    @Test
    public void jsonObjectSpacingInput() throws IOException {
        String input = "{\"Key\": \"Value\", \"Key2\": 5}";
        JsonElement output = builder.build(input);
        assertEquals("{\"Key\":\"Value\",\"Key2\":5}", output.toJson());
    }

    @Test
    public void jsonObjectExcessiveSpacingInput() throws IOException {
        String input = "{                        \"Key\":                                     \"Value\",                          \"Key2\":                                         5}";
        JsonElement output = builder.build(input);
        assertEquals("{\"Key\":\"Value\",\"Key2\":5}", output.toJson());
    }

    @Test
    public void jsonObjectFancyFormatInput() throws IOException {
        String input = "{\n\t\"Key\": \"Value\",\n\t\"Key2\": 5\n}"; //Would look something like the spaced format some JSON files use.
        JsonElement output = builder.build(input);
        assertEquals("{\"Key\":\"Value\",\"Key2\":5}", output.toJson());
    }

    @Test
    public void jsonArraySpacingInput() throws IOException {
        String input = "[\"Word\", 1, null, true]";
        JsonElement output = builder.build(input);
        assertEquals("[\"Word\",1,null,true]", output.toJson());
    }

    @Test
    public void jsonArrayExcessiveSpacingInput() throws IOException {
        String input = "[\"Word\"                   ,                          1,               null,              true]";
        JsonElement output = builder.build(input);
        assertEquals("[\"Word\",1,null,true]", output.toJson());
    }

    @Test
    public void jsonArrayFancyFormatInput() throws IOException {
        String input = "[\n\t\"Word\",\n\t 1,\n\t null,\n\t true]"; //Would look something like the spaced format some JSON files use.
        JsonElement output = builder.build(input);
        assertEquals("[\"Word\",1,null,true]", output.toJson());
    }

}
