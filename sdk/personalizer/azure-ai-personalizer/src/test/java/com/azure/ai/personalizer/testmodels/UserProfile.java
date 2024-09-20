// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class UserProfile implements JsonSerializable<UserProfile> {
    public String getProfileType() {
        return profileType;
    }

    public UserProfile setProfileType(String profileType) {
        this.profileType = profileType;
        return this;
    }

    public String getLatLong() {
        return latLong;
    }

    public UserProfile setLatLong(String latLong) {
        this.latLong = latLong;
        return this;
    }

    String profileType;
    String latLong;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("profileType", profileType)
            .writeStringField("latLong", latLong)
            .writeEndObject();
    }

    public static UserProfile fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UserProfile userProfile = new UserProfile();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("profileType".equals(fieldName)) {
                    userProfile.profileType = reader.getString();
                } else if ("latLong".equals(fieldName)) {
                    userProfile.latLong = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return userProfile;
        });
    }
}
