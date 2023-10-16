package com.azure.json;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
