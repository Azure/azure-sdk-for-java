// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Fluent
public class FlattenedProduct implements JsonSerializable<FlattenedProduct> {
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

        return jsonWriter.writeStartObject("properties")
            .writeStringField("p.name", productName, false)
            .writeStringField("type", productType, false)
            .writeEndObject()
            .writeEndObject()
            .flush();
    }

    public static FlattenedProduct fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            FlattenedProduct product = new FlattenedProduct();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("p.name".equals(fieldName)) {
                            product.setProductName(reader.getStringValue());
                        } else if ("type".equals(fieldName)) {
                            product.setProductType(reader.getStringValue());
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return product;
        });
    }
}
