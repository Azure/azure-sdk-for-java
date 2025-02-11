// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class UserProfile implements JsonSerializable<UserProfile> {
    String profileType;
    String latLong;

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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("profileType", profileType)
            .writeStringField("latLong", latLong)
            .writeEndObject();
    }

    public static UserProfile fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, UserProfile::new, (reader, fieldName, userProfile) -> {
            if ("profileType".equals(fieldName)) {
                userProfile.profileType = reader.getString();
            } else if ("latLong".equals(fieldName)) {
                userProfile.latLong = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
