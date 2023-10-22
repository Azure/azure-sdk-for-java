package com.azure.json;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class JsonInputTests {
    File file = new File("src/test/resources/JsonTestFileB.json");

    @Test
    public void builderDeserializeString() throws IOException {
        String content = Files.readString(file.toPath());
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    @Test
    public void builderDeserializeBytes() throws IOException {
        byte[] content = Files.readAllBytes(file.toPath());
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    @Test
    public void builderDeserializeInputStream() throws IOException {
        InputStream content = new FileInputStream("src/test/resources/JsonTestFileB.json");
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    @Test
    public void builderDeserializeReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/JsonTestFileB.json")));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }



}
