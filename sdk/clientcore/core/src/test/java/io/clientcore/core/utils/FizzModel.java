package io.clientcore.core.utils;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

public class FizzModel implements JsonSerializable<FizzModel> {
    private String fizzName;
    private String fizzId;

    public String getFizzName() {
        return fizzName;
    }

    public FizzModel setFizzName(String fizzName) {
        this.fizzName = fizzName;
        return this;
    }

    public String getFizzId() {
        return fizzId;
    }

    public FizzModel setFizzId(String fizzId) {
        this.fizzId = fizzId;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("fizzName", fizzName);
        jsonWriter.writeStringField("fizzId", fizzId);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static FizzModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FizzModel model = new FizzModel();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                if ("fizzName".equals(fieldName)) {
                    model.setFizzName(reader.getString());
                } else if ("fizzId".equals(fieldName)) {
                    model.setFizzId(reader.getString());
                }
            }
            return model;
        });
    }
}
