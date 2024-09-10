// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

final class EncryptedRegionInfo implements JsonSerializable<EncryptedRegionInfo> {
    /**
     * The cipher text length.
     */
    private long dataLength;

    /**
     * The nonce length.
     */
    private int nonceLength;

    EncryptedRegionInfo() {
    }

    /**
     * Creates a new AuthenticationBlockInfo.
     *
     * @param ciphertextLength The length of the cipher text.
     * @param nonceLength The length of the nonce.
     */
    EncryptedRegionInfo(long ciphertextLength, int nonceLength) {
        this.dataLength = ciphertextLength;
        this.nonceLength = nonceLength;
    }

    /**
     * Gets the ciphertextLength property.
     *
     * @return The ciphertextLength property.
     */
    public long getDataLength() {
        return dataLength;
    }

    /**
     * Gets the nonceLength property.
     *
     * @return The nonceLength property.
     */
    public int getNonceLength() {
        return nonceLength;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeNumberField("DataLength", dataLength)
            .writeNumberField("NonceLength", nonceLength)
            .writeEndObject();
    }

    /**
     * Reads an instance of EncryptedRegionInfo from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return The EncryptedRegionInfo read from the JsonReader.
     * @throws IOException If an I/O error occurs.
     */
    public static EncryptedRegionInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            EncryptedRegionInfo encryptedRegionInfo = new EncryptedRegionInfo();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("DataLength".equals(fieldName)) {
                    encryptedRegionInfo.dataLength = reader.getInt();
                } else if ("NonceLength".equals(fieldName)) {
                    encryptedRegionInfo.nonceLength = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }
            return encryptedRegionInfo;
        });
    }
}
