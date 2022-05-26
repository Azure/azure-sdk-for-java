// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/**
 * Model used for testing JSON flattening.
 */
@Immutable
public final class ClassWithFlattenedProperties implements JsonSerializable<ClassWithFlattenedProperties> {
    private final String odataType;
    private final String odataETag;

    public ClassWithFlattenedProperties(String odataType, String odataETag) {
        this.odataType = odataType;
        this.odataETag = odataETag;
    }

    public String getOdataType() {
        return odataType;
    }

    public String getOdataETag() {
        return odataETag;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (odataType != null) {
            jsonWriter.writeStartObject("@odata")
                .writeStringField("type", odataType)
                .writeEndObject();
        }

        return jsonWriter.writeStringField("@odata.etag", odataETag, false)
            .writeEndObject()
            .flush();
    }

    public static ClassWithFlattenedProperties fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            String odataType = null;
            String odataEtag = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("@odata.etag".equals(fieldName)) {
                    odataEtag = reader.getStringValue();
                } else if ("@odata".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("type".equals(fieldName)) {
                            odataType = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new ClassWithFlattenedProperties(odataType, odataEtag);
        });
    }
}
