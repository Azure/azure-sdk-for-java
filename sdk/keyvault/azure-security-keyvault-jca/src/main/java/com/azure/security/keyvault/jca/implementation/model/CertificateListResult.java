// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * The CertificateItem REST model.
 */
public class CertificateListResult implements JsonSerializable<CertificateListResult> {
    /**
     * Stores the list of certificates.
     */
    private List<CertificateItem> value;

    /**
     * Stores the link to the next certificates page.
     */
    private String nextLink;

    /**
     * Get the list of certificates in this page.
     *
     * @return The list of certificates in this page.
     */
    public List<CertificateItem> getValue() {
        return value;
    }

    /**
     * Set the list of certificates in this page.
     *
     * @param value the list of certificates in this page.
     */
    public void setValue(List<CertificateItem> value) {
        this.value = value;
    }

    /**
     * Get the link to the next certificates page.
     *
     * @return The link to the next certificates page.
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * Set the link to the next certificates page.
     *
     * @param nextLink The link to the next certificates page.
     */
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("value", value, JsonWriter::writeJson);
        jsonWriter.writeStringField("nextLink", nextLink);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link CertificateListResult} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link CertificateListResult} if the {@link JsonReader} was pointing to an instance of it,
     * or {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link CertificateListResult}.
     */
    public static CertificateListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CertificateListResult deserializedCertificateListResult = new CertificateListResult();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("value".equals(fieldName)) {
                    deserializedCertificateListResult.value = reader.readArray(CertificateItem::fromJson);
                } else if ("nextLink".equals(fieldName)) {
                    deserializedCertificateListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCertificateListResult;
        });
    }
}
