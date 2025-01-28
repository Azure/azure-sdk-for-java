// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class UserFeatures implements JsonSerializable<UserFeatures> {
    private boolean isPayingUser;
    private String favoriteGenre;
    private double hoursOnSite;
    private String lastWatchedType;

    public boolean isPayingUser() {
        return isPayingUser;
    }

    public UserFeatures setPayingUser(boolean payingUser) {
        isPayingUser = payingUser;
        return this;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public UserFeatures setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
        return this;
    }

    public double getHoursOnSite() {
        return hoursOnSite;
    }

    public UserFeatures setHoursOnSite(double hoursOnSite) {
        this.hoursOnSite = hoursOnSite;
        return this;
    }

    public String getLastWatchedType() {
        return lastWatchedType;
    }

    public UserFeatures setLastWatchedType(String lastWatchedType) {
        this.lastWatchedType = lastWatchedType;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeBooleanField("isPayingUser", isPayingUser)
            .writeStringField("favoriteGenre", favoriteGenre)
            .writeDoubleField("hoursOnSite", hoursOnSite)
            .writeStringField("lastWatchedType", lastWatchedType)
            .writeEndObject();
    }

    public static UserFeatures fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, UserFeatures::new, (reader, fieldName, userFeatures) -> {
            if ("isPayingUser".equals(fieldName)) {
                userFeatures.isPayingUser = reader.getBoolean();
            } else if ("favoriteGenre".equals(fieldName)) {
                userFeatures.favoriteGenre = reader.getString();
            } else if ("hoursOnSite".equals(fieldName)) {
                userFeatures.hoursOnSite = reader.getDouble();
            } else if ("lastWatchedType".equals(fieldName)) {
                userFeatures.lastWatchedType = reader.getString();
            } else {
                reader.skipChildren();
            }
        });
    }
}
