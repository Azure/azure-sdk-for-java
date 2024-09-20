// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class RecentActivity implements JsonSerializable<RecentActivity> {
    public Integer getItemsInCart() {
        return itemsInCart;
    }

    public RecentActivity setItemsInCart(Integer itemsInCart) {
        this.itemsInCart = itemsInCart;
        return this;
    }

    Integer itemsInCart;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeNumberField("itemsInCart", itemsInCart)
            .writeEndObject();
    }

    public static RecentActivity fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RecentActivity recentActivity = new RecentActivity();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("itemsInCart".equals(fieldName)) {
                    recentActivity.itemsInCart = reader.getNullable(JsonReader::getInt);
                } else {
                    reader.skipChildren();
                }
            }

            return recentActivity;
        });
    }
}
