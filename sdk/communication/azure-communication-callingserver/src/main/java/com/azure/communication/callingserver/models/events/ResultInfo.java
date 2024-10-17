// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The ResultInfo model. */
@Immutable
public final class ResultInfo implements JsonSerializable<ResultInfo> {
    /*
     * The code property.
     */
    private Integer code;

    /*
     * The subCode property.
     */
    private Integer subCode;

    /*
     * The message property.
     */
    private String message;

    private ResultInfo() {
    }

    /**
     * Get the code property: The code property.
     *
     * @return the code value.
     */
    public Integer getCode() {
        return this.code;
    }

    /**
     * Get the subCode property: The subCode property.
     *
     * @return the subCode value.
     */
    public Integer getSubCode() {
        return this.subCode;
    }

    /**
     * Get the message property: The message property.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeNumberField("code", code)
            .writeNumberField("subCode", subCode)
            .writeStringField("message", message)
            .writeEndObject();
    }

    /**
     * Reads an instance of {@link ResultInfo} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read.
     * @return An instance of {@link ResultInfo}, or null if the {@link JsonReader} was pointing to
     * {@link JsonToken#NULL}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     */
    public static ResultInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResultInfo info = new ResultInfo();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equals(fieldName)) {
                    info.code = reader.getNullable(JsonReader::getInt);
                } else if ("subCode".equals(fieldName)) {
                    info.subCode = reader.getNullable(JsonReader::getInt);
                } else if ("message".equals(fieldName)) {
                    info.message = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return info;
        });
    }
}
