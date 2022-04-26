// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonCapable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public class FlattenedProduct implements JsonCapable<FlattenedProduct> {
    private String productName;
    private String productType;

    public String getProductName() {
        return this.productName;
    }

    public FlattenedProduct setProductName(String productName) {
        this.productName = productName;
        return this;
    }

    public String getProductType() {
        return this.productType;
    }

    public FlattenedProduct setProductType(String productType) {
        this.productType = productType;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (productName == null && productType == null) {
            return jsonWriter.writeEndObject().flush();
        }

        jsonWriter.writeFieldName("properties").writeStartObject();

        JsonUtils.writeNonNullStringField(jsonWriter, "p.name", productName);
        JsonUtils.writeNonNullStringField(jsonWriter, "type", productType);

        return jsonWriter.writeEndObject().writeEndObject().flush();
    }

    public static FlattenedProduct fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, (reader, token) -> {
            String productName = null;
            String productType = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                token = reader.nextToken();

                if ("properties".equals(fieldName) && token == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("p.name".equals(fieldName)) {
                            productName = reader.getStringValue();
                        } else if ("type".equals(fieldName)) {
                            productType = reader.getStringValue();
                        }
                    }
                }
            }

            return new FlattenedProduct().setProductName(productName).setProductType(productType);
        });
    }
}
