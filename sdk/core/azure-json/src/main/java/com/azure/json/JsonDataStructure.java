package com.azure.json;

import java.io.IOException;

public abstract class JsonDataStructure extends JsonElement {
    @Override
    public String toString() {
        return null;
    }

    public abstract void build(JsonReader reader) throws IOException;


    public abstract String toJson() throws IOException;

}
