package com.azure.json;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JsonBuilderTests {

    JsonBuilder builder = new JsonBuilder();
    @Test
    public void createArray() {
        JsonArray result = builder.createArray();
        assertNotNull(result);
    }

    @Test
    public void createObject() {
        JsonObject result = builder.createObject();
        assertNotNull(result);
    }

    String exampleJson = "{\"name\": \"John\",\"age\": 30,\"scores\": [85, 92, 78, 95]}";
    String complexExampleJson = "{\"person\":{\"name\":\"John Doe\",\"age\":30,\"address\":{\"street\":\"123 Main St\",\"city\":\"Someville\",\"state\":\"Somestate\",\"postalCode\":\"12345\"},\"contacts\":[{\"type\":\"email\",\"value\":\"john.doe@example.com\"},{\"type\":\"phone\",\"value\":\"555-123-4567\"}]},\"hobbies\":[\"reading\",\"painting\",\"gaming\"],\"isStudent\":false,\"grades\":{\"math\":95,\"history\":82,\"science\":89},\"preferences\":{\"colors\":[\"blue\",\"green\"],\"languages\":{\"primary\":\"English\",\"secondary\":\"French\"}}}";

    @Test
    public void parseJson() throws IOException {
        JsonElement result = (JsonElement) builder.deserialize(complexExampleJson);
        assertNotNull(result);
        String resultString = result.toJson();
        // This currently fails because of issues with number parsing (it's turning ints into floats as I haven't implemented any checking)
        assertEquals(complexExampleJson, resultString);
    }

    @Test
    public void createAndSerializeArray() throws IOException {
        JsonArray array = builder.createArray();
        array.addElement(new JsonString("Hello"));
        array.addElement(new JsonString("World"));
        String result = array.toJson();
        assertEquals("[\"Hello\",\"World\"]", result);
    }



}
