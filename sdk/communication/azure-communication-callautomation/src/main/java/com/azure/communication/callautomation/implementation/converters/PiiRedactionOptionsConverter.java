// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.implementation.converters;

import java.io.IOException;

import com.azure.communication.callautomation.models.RedactionType;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

/**
 * PII redaction configuration options.
 */
public final class PiiRedactionOptionsConverter {
    /*
    * Gets or sets a value indicating whether PII redaction is enabled.
    */
    private Boolean enable;

    /*
     * Gets or sets the type of PII redaction to be used.
     */
    private RedactionType redactionType;

    /**
     * Get the enable property: Gets or sets a value indicating whether PII redaction is enabled.
     * 
     * @return the enable value.
     */
    public Boolean getEnable() {
        return this.enable;
    }

    /**
     * Get the redactionType property: Gets or sets the type of PII redaction to be used.
     * 
     * @return the redactionType value.
     */
    public RedactionType getRedactionType() {
        return this.redactionType;
    }

    /**
     * Reads an instance of PiiRedactionOptionsConverter from the JsonReader.
     *<p>
     * Note: PiiRedactionOptionsConverter does not have to implement JsonSerializable, model is only used in deserialization
     * context internally by {@link StreamingDataParser} and not serialized.
     *</p>
     * @param jsonReader The JsonReader being read.
     * @return An instance of FileSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FileSource.
     */
    public static PiiRedactionOptionsConverter fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final PiiRedactionOptionsConverter converter = new PiiRedactionOptionsConverter();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("enable".equals(fieldName)) {
                    converter.enable = reader.getBoolean();
                } else if ("redactionType".equals(fieldName)) {
                    converter.redactionType = RedactionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return converter;
        });
    }
}
