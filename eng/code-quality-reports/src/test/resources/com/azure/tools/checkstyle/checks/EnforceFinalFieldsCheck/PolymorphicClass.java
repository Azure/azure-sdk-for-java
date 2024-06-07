package com.azure.tools.checkstyle.checks;
import com.azure.json.JsonReader;
import java.io.IOException;

public class PolymorphicClass {

    private String type = "PolymorphicClass";

    public PolymorphicClass() {
    }

    public static PolymorphicClass fromJson(JsonReader jsonReader) throws IOException {
        PolymorphicClass deserializedPolymorphicClass = new PolymorphicClass();
        deserializedPolymorphicClass.type = reader.getString();
        return deserializedPolymorphicClass;
    }
}
