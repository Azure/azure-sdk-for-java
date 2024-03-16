package com.azure.json;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

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

    URL jsonTestFileB = getClass().getClassLoader().getResource("jsonTestFileB.json");
    URL jsonArrayFileA = getClass().getClassLoader().getResource("jsonArrayFileA.json");
    URL jsonArrayFileB = getClass().getClassLoader().getResource("jsonArrayFileB.json");
    URL jsonArrayFileC = getClass().getClassLoader().getResource("jsonArrayFileC.json");
    URL jsonArrayFileD = getClass().getClassLoader().getResource("jsonArrayFileD.json");
    URL jsonArrayFileE = getClass().getClassLoader().getResource("jsonArrayFileE.json");
    URL jsonArrayFileF = getClass().getClassLoader().getResource("jsonArrayFileF.json");
    URL jsonArrayFileG = getClass().getClassLoader().getResource("jsonArrayFileG.json");
    URL jsonArrayFileEmpty = getClass().getClassLoader().getResource("jsonArrayFileEmpty.json");


    @Test
    public void builderDeserializeString() throws IOException {
        String content = Files.readString(Path.of(jsonTestFileB.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    @Test
    public void builderDeserializeBytes() throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonTestFileB.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    @Test
    public void builderDeserializeInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonTestFileB.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    @Test
    public void builderDeserializeReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonTestFileB.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"json\":{\"name\":\"Dave\",\"age\":22}}", result.toJson());
    }

    //----------------------------------------------------------------------------------
    @Test
    public void builderDeserializeOnlyArrayString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileA.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataPriorString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileB.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataAfterString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileC.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileD.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileE.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileF.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileG.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJson());
    }

    @Test
    public void builderDeserializeEmptyArrayString() throws IOException {
        String content = Files.readString(Path.of(jsonArrayFileEmpty.getPath()));
        JsonElement result = JsonElement.fromString(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJson());
    }

    //----------------------------------------------------------------------------------

    @Test
    public void builderDeserializeOnlyArrayBytes() throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileA.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataPriorBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileB.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataAfterBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileC.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileD.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileE.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileF.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileG.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJson());
    }

    @Test
    public void builderDeserializeEmptyArrayBytes () throws IOException {
        byte[] content = Files.readAllBytes(Path.of(jsonArrayFileEmpty.getPath()));
        JsonElement result = JsonElement.fromBytes(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJson());
    }

    //----------------------------------------------------------------------------------

    @Test
    public void builderDeserializeOnlyArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(String.valueOf(Path.of(jsonArrayFileA.getPath())));
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataPriorInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileB.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataAfterInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileC.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileD.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileE.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileF.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileG.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJson());
    }

    @Test
    public void builderDeserializeEmptyArrayInputStream() throws IOException {
        InputStream content = new FileInputStream(jsonArrayFileEmpty.getPath());
        JsonElement result = JsonElement.fromStream(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJson());
    }

    //----------------------------------------------------------------------------------

    @Test
    public void builderDeserializeOnlyArrayReader() throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileA.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"soloArray\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataPriorReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileB.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"priorJson\":\"This is before the array\",\"boolean\":true,\"array\":[1,2,3]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayDataAfterReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileC.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"Array\":[1,2,3],\"JsonAfter\":\"This is after an array\",\"Number\":5}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileD.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"array\":[1,2,[\"array in\",\"array\"],\"follow-up word\"]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileE.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"Data\":[{\"name\":\"A\",\"age\":1,\"input\":[true,false,true]},{\"name\":\"B\",\"age\":2,\"input\":[true,false,false]},{\"name\":\"C\",\"age\":3,\"input\":[false,true,true],\"favourite fruit\":\"lemons\"}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayInArrayMultiReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileF.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"superDeepObjects\":[{\"layer1\":[{\"layer2\":[{\"layer3\":[{\"layer4\":[1,2,3]},{\"layer4Second\":[4,5,6]}]}]}]}]}", result.toJson());
    }

    @Test
    public void builderDeserializeArrayObjectInArrayMultiReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileG.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"superDeepArray\":[[[[null,[[[\"deep\",[1,2,3],4]]]]],false]]}", result.toJson());
    }

    @Test
    public void builderDeserializeEmptyArrayReader () throws IOException {
        Reader content = new BufferedReader(new InputStreamReader(new FileInputStream(jsonArrayFileEmpty.getPath())));
        JsonElement result = JsonElement.fromReader(content);
        assertEquals("{\"inputA\":\"A\",\"array\":[],\"inputC\":null}", result.toJson());
    }



}
