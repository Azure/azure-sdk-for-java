package com.azure.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class JsonCreationTests {

    //Part 1: Simple Creation of Objects
    @Test
    public void createJsonObject(){
        JsonObject test = new JsonObject();
        assertTrue(test.isObject());
    }

    @Test
    public void createJsonString(){
        JsonString test = new JsonString("");
        assertTrue(test.isString());
    }

    @Test
    public void createJsonNumber(){
        JsonNumber test = new JsonNumber(0);
        assertTrue(test.isNumber());
    }

    @Test
    public void createJsonBoolean(){
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertTrue(test.isBoolean());
    }

    @Test
    public void createJsonNull(){
        JsonNull test = JsonNull.getInstance();
        assertTrue(test.isNull());
    }

    @Test
    public void createJsonArray(){
        JsonArray test = new JsonArray();
        assertTrue(test.isArray());
    }


    //Part 2: Test that created object is not any of the other types.
    //2.1: Object
    @Test
    public void JsonObjectNotJsonString(){
        JsonObject test = new JsonObject();
        assertFalse(test.isString());
    }

    @Test
    public void JsonObjectNotJsonNumber(){
        JsonObject test = new JsonObject();
        assertFalse(test.isNumber());
    }

    @Test
    public void JsonObjectNotJsonBoolean(){
        JsonObject test = new JsonObject();
        assertFalse(test.isBoolean());
    }

    @Test
    public void JsonObjectNotJsonNull(){
        JsonObject test = new JsonObject();
        assertFalse(test.isNull());
    }

    @Test
    public void JsonObjectNotJsonArray(){
        JsonObject test = new JsonObject();
        assertFalse(test.isArray());
    }



    //2.2: Number
    @Test
    public void JsonNumberNotJsonString(){
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isString());
    }

    @Test
    public void JsonNumberNotJsonObject(){
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isObject());
    }

    @Test
    public void JsonNumberNotJsonBoolean(){
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isBoolean());
    }

    @Test
    public void JsonNumberNotJsonNull(){
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isNull());
    }

    @Test
    public void JsonNumberNotJsonArray(){
        JsonNumber test = new JsonNumber(0);
        assertFalse(test.isArray());
    }

    // Cannot construct JsonNumber from null Number object  
    @Test 
    public void JsonNumberNotNull(){
        assertThrows(
            IllegalArgumentException.class, 
            () -> { 
                Number numberTest = null; 
                JsonNumber test = new JsonNumber(numberTest); 
            }
        );
    }

    // Cannot construct JsonNumber from non-parseable int or float value 
    // NOTE: Strings storing octal representations such as 010 (8 in decimal) 
    // are parseable - they will just be interpreted as decimal with the 
    // preceeding 0s removed and therefore parseable; however, binary (0b or 0B) 
    // and hexadecimal (0x or 0X) representations will not be parseable. 
    @ParameterizedTest
    @ValueSource(strings = {"null", "true", "false", " 0 0 ", "0 1 2 3", "1. 2", "1 . 2", "1a1", "1-", "-1-", "abc", "-a", "0b01010101", "0B10101", "0x10", "0xFF", "0x1A", ".", "-."}) 
    public void JsonNumberNotValidIntOrFloatStrings(String value){
        assertThrows(
            IllegalArgumentException.class, 
            () -> { 
                JsonNumber test = new JsonNumber(value); 
            }
        );
    }

    // Making sure JsonNumber does successfully construct with parseable int and 
    // float String representations 
    @ParameterizedTest
    @ValueSource(strings = {" 123", "123 ", " -123", "-123 ", "00000", "0123", "123", "-1934234", "929.12342", "-1.2345", ".12345", "0.0", "-.12345", ".0", "-.0", " .0", ".0 ", "1000000000000000000000000000000", "-1000000000000000000000000000000"}) 
    public void JsonNumberValidIntOrFloatStrings(String value){
        assertDoesNotThrow(
            () -> { 
                JsonNumber test = new JsonNumber(value); 
            }
        );
    }

    //2.3: Boolean
    @Test
    public void JsonBooleanNotJsonString(){
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isString());
    }

    @Test
    public void JsonBooleanNotJsonNumber(){
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isNumber());
    }

    @Test
    public void JsonBooleanNotJsonObject(){
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isObject());
    }

    @Test
    public void JsonBooleanNotJsonNull(){
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isNull());
    }

    @Test
    public void JsonBooleanNotJsonArray(){
        JsonBoolean test = JsonBoolean.getInstance(true);
        assertFalse(test.isArray());
    }

    //2.4: Null
    @Test
    public void JsonNullNotJsonString(){
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isString());
    }

    @Test
    public void JsonNullNotJsonNumber(){
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isNumber());
    }

    @Test
    public void JsonNullNotJsonBoolean(){
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isBoolean());
    }

    @Test
    public void JsonNullNotJsonObject(){
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isObject());
    }

    @Test
    public void JsonNullNotJsonArray(){
        JsonNull test = JsonNull.getInstance();
        assertFalse(test.isArray());
    }


    //2.5: Array
    @Test
    public void JsonArrayNotJsonString(){
        JsonArray test = new JsonArray();
        assertFalse(test.isString());
    }

    @Test
    public void JsonArrayNotJsonNumber(){
        JsonArray test = new JsonArray();
        assertFalse(test.isNumber());
    }

    @Test
    public void JsonArrayNotJsonBoolean(){
        JsonArray test = new JsonArray();
        assertFalse(test.isBoolean());
    }

    @Test
    public void JsonArrayNotJsonNull(){
        JsonArray test = new JsonArray();
        assertFalse(test.isNull());
    }

    @Test
    public void JsonArrayNotJsonObject(){
        JsonArray test = new JsonArray();
        assertFalse(test.isObject());
    }
}
