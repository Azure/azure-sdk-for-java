package io.clientcore.core.utils;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

public class BazModel implements JsonSerializable<BazModel> {

    private String id;
    private String bazId;

    private String bazName;

    public String getBazId() {
        return bazId;
    }

    public BazModel setBazId(String bazId) {
        this.bazId = bazId;
        return this;
    }

    public String getBazName() {
        return bazName;
    }

    public BazModel setBazName(String bazName) {
        this.bazName = bazName;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("bazId", bazId);
        jsonWriter.writeStringField("bazName", bazName);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static BazModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            BazModel bazModel = new BazModel();
            boolean isEmpty = true;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                if("bazId".equals(fieldName)) {
                    isEmpty = false;
                    bazModel.setBazId(reader.getString());
                } else if ("bazName".equals(fieldName)) {
                    isEmpty = false;
                    bazModel.setBazName(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            if (isEmpty) {
                throw new IOException("Not a valid BazModel json");
            }
            return bazModel;
        });
    }
}
