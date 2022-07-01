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
public class SampleResource implements JsonSerializable<SampleResource> {
    private String namePropertiesName;
    private String registrationTtl;

    public SampleResource withNamePropertiesName(String namePropertiesName) {
        this.namePropertiesName = namePropertiesName;
        return this;
    }

    public SampleResource withRegistrationTtl(String registrationTtl) {
        this.registrationTtl = registrationTtl;
        return this;
    }

    public String getNamePropertiesName() {
        return namePropertiesName;
    }

    public String getRegistrationTtl() {
        return registrationTtl;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        jsonWriter.writeStartObject();

        if (namePropertiesName == null && registrationTtl == null) {
            return jsonWriter.writeEndObject().flush();
        }

        return jsonWriter.writeStartObject("properties")
            .writeStringField("name", namePropertiesName, false)
            .writeStringField("registrationTtl", registrationTtl, false)
            .writeEndObject()
            .writeEndObject()
            .flush();
    }

    public static SampleResource fromJson(JsonReader jsonReader) {
        return jsonReader.readObject(reader -> {
            String namePropertiesName = null;
            String registrationTtl = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("properties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("name".equals(fieldName)) {
                            namePropertiesName = reader.getStringValue();
                        } else if ("registrationTtl".equals(fieldName)) {
                            registrationTtl = reader.getStringValue();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new SampleResource().withNamePropertiesName(namePropertiesName).withRegistrationTtl(registrationTtl);
        });
    }
}

