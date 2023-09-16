package com.azure.json;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonBooleanTests {
    @Test
    public void booleanElementTrue(){
        JsonElement test = JsonBoolean.getInstance(true);

        assertEquals("true", test.toString());
        assertTrue(test.isBoolean());
        // assertTrue(test.isTrue());
        // assertFalse(test.isFalse());
    }

    @Test
    public void booleanElementFalse(){
        JsonElement test = JsonBoolean.getInstance(false);

        assertEquals("false", test.toString());
        assertTrue(test.isBoolean());
        // assertTrue(test.isFalse());
        // assertFalse(test.isTrue());
    }

  @Test
    public void booleanTrue(){
        JsonBoolean test = JsonBoolean.getInstance(true);

        assertTrue(test.isTrue());
        assertFalse(test.isFalse());
    }

   @Test
    public void booleanInstanceTests(){
        assertTrue(JsonBoolean.getInstance(true).isTrue());
        assertTrue(JsonBoolean.getInstance(false).isFalse());
    }

    @Test
    public void booleanComparisonsTests(){
        assertEquals(JsonBoolean.getInstance(true), JsonBoolean.getInstance(true));
        assertEquals(JsonBoolean.getInstance(false), JsonBoolean.getInstance(false));
        assertFalse(JsonBoolean.getInstance(true) == JsonBoolean.getInstance(false));
    }
  
    @Test
    public void booleanFalse(){
        JsonBoolean test = JsonBoolean.getInstance(false);
        assertTrue(test.isFalse());
        assertFalse(test.isTrue());
    }
}