// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import com.azure.json.tree.JsonElement;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonInputTests {

    //    File file = new File("src/test/resources/JsonTestFileB.json");
    //    File jFileA = new File("src/test/resources/jsonArrayFileA.json");
    //    File jFileB = new File("src/test/resources/jsonArrayFileB.json");
    //    File jFileC = new File("src/test/resources/jsonArrayFileC.json");
    //    File jFileD = new File("src/test/resources/jsonArrayFileD.json");
    //    File jFileE = new File("src/test/resources/jsonArrayFileE.json");
    //    File jFileF = new File("src/test/resources/jsonArrayFileF.json");
    //    File jFileG = new File("src/test/resources/jsonArrayFileG.json");
    //    File jFileEmpty = new File("src/test/resources/jsonArrayFileEmpty.json");

    /*
    URL jsonTestFileB = getClass().getClassLoader().getResource("jsonTestFileB.json");
    URL jsonArrayFileA = getClass().getClassLoader().getResource("jsonArrayFileA.json");
    URL jsonArrayFileB = getClass().getClassLoader().getResource("jsonArrayFileB.json");
    URL jsonArrayFileC = getClass().getClassLoader().getResource("jsonArrayFileC.json");
    URL jsonArrayFileD = getClass().getClassLoader().getResource("jsonArrayFileD.json");
    URL jsonArrayFileE = getClass().getClassLoader().getResource("jsonArrayFileE.json");
    URL jsonArrayFileF = getClass().getClassLoader().getResource("jsonArrayFileF.json");
    URL jsonArrayFileG = getClass().getClassLoader().getResource("jsonArrayFileG.json");
    URL jsonArrayFileEmpty = getClass().getClassLoader().getResource("jsonArrayFileEmpty.json");
     */

    URI jsonTestFileB = ClassLoader.getSystemResource("jsonTestFileB.json").toURI();
    URI jsonArrayFileA = ClassLoader.getSystemResource("jsonArrayFileA.json").toURI();
    URI jsonArrayFileB = ClassLoader.getSystemResource("jsonArrayFileB.json").toURI();
    URI jsonArrayFileC = ClassLoader.getSystemResource("jsonArrayFileC.json").toURI();
    URI jsonArrayFileD = ClassLoader.getSystemResource("jsonArrayFileD.json").toURI();
    URI jsonArrayFileE = ClassLoader.getSystemResource("jsonArrayFileE.json").toURI();
    URI jsonArrayFileF = ClassLoader.getSystemResource("jsonArrayFileF.json").toURI();
    URI jsonArrayFileG = ClassLoader.getSystemResource("jsonArrayFileG.json").toURI();
    URI jsonArrayFileEmpty = ClassLoader.getSystemResource("jsonArrayFileEmpty.json").toURI();

    public JsonInputTests() throws URISyntaxException {
    }

    @Test
    public void uriTesting() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileA)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonTestFileB)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJsonString());
    }

    @Test
    public void builderDeserializeBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonTestFileB));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJsonString());
    }

    @Test
    public void builderDeserializeInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonTestFileB.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJsonString());
    }

    @Test
    public void builderDeserializeReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonTestFileB.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJsonString());
    }

    //----------------------------------------------------------------------------------
    @Test
    public void builderDeserializeOnlyArrayString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileA)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataPriorString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileB)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataAfterString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileC)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileD)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileE)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals(
            "{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileF)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals(
            "{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileG)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeEmptyArrayString() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(jsonArrayFileEmpty)), StandardCharsets.UTF_8);
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJsonString());
    }

    //----------------------------------------------------------------------------------

    @Test
    public void builderDeserializeOnlyArrayBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileA));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataPriorBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileB));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataAfterBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileC));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileD));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileE));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals(
            "{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileF));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals(
            "{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileG));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeEmptyArrayBytes() throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(jsonArrayFileEmpty));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJsonString());
    }

    //----------------------------------------------------------------------------------

    @Test
    public void builderDeserializeOnlyArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(String.valueOf(Paths.get(jsonArrayFileA)));
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataPriorInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileB.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataAfterInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileC.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileD.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileE.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals(
            "{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileF.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals(
            "{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileG.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeEmptyArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileEmpty.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJsonString());
    }

    //----------------------------------------------------------------------------------

    @Test
    public void builderDeserializeOnlyArrayReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileA.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataPriorReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileB.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayDataAfterReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileC.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileD.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileE.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals(
            "{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileF.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals(
            "{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}",
            result.toJsonString());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileG.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJsonString());
    }

    @Test
    public void builderDeserializeEmptyArrayReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileEmpty.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJsonString());
    }
}
