// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.testmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

import static com.azure.ai.personalizer.TestUtils.deserializationHelper;

public class RecentActivity implements JsonSerializable<RecentActivity> {
    Integer itemsInCart;

    public Integer getItemsInCart() {
        return itemsInCart;
    }

    public RecentActivity setItemsInCart(Integer itemsInCart) {
        this.itemsInCart = itemsInCart;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeNumberField("itemsInCart", itemsInCart).writeEndObject();
    }

    public static RecentActivity fromJson(JsonReader jsonReader) throws IOException {
        return deserializationHelper(jsonReader, RecentActivity::new, (reader, fieldName, recentActivity) -> {
            if ("itemsInCart".equals(fieldName)) {
                recentActivity.itemsInCart = reader.getNullable(JsonReader::getInt);
            } else {
                reader.skipChildren();
            }
        });
    }
}
