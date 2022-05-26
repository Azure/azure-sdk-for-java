// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
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
        return JsonUtils.readObject(jsonReader, reader -> {
            FlattenedProduct product = new FlattenedProduct();

            JsonUtils.readFields(reader, fieldName -> {
                if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    JsonUtils.readFields(reader, fieldName2 -> {
                        if ("p.name".equals(fieldName2)) {
                            product.setProductName(reader.getStringValue());
                        } else if ("type".equals(fieldName2)) {
                            product.setProductType(reader.getStringValue());
                        }
                    });
                }
            });

            return product;
        });
    }
}
