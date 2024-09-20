// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class ActionFeatures implements JsonSerializable<ActionFeatures> {
    public String getVideoType() {
        return videoType;
    }

    public ActionFeatures setVideoType(String videoType) {
        this.videoType = videoType;
        return this;
    }

    public Integer getVideoLength() {
        return videoLength;
    }

    public ActionFeatures setVideoLength(Integer videoLength) {
        this.videoLength = videoLength;
        return this;
    }

    public String getDirector() {
        return director;
    }

    public ActionFeatures setDirector(String director) {
        this.director = director;
        return this;
    }

    String videoType;
    Integer videoLength;
    String director;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("videoType", videoType)
            .writeNumberField("videoLength", videoLength)
            .writeStringField("director", director)
            .writeEndObject();
    }

    public static ActionFeatures fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ActionFeatures actionFeatures = new ActionFeatures();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("videoType".equals(fieldName)) {
                    actionFeatures.videoType = reader.getString();
                } else if ("videoLength".equals(fieldName)) {
                    actionFeatures.videoLength = reader.getNullable(JsonReader::getInt);
                } else if ("director".equals(fieldName)) {
                    actionFeatures.director = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return actionFeatures;
        });
    }
}
