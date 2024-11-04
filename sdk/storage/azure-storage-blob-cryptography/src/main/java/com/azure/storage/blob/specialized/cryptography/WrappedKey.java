// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents the envelope key details stored on the service.
 */
final class WrappedKey implements JsonSerializable<WrappedKey> {

    /**
     * The key identifier string.
     */
    private String keyId;

    /**
     * The encrypted content encryption key.
     */
    private byte[] encryptedKey;

    /**
     * The algorithm used for wrapping.
     */
    private String algorithm;

    /**
     * Initializes a new instance of the {@link WrappedKey} class.
     */
    WrappedKey() {
    }

    /**
     * Initializes a new instance of the {@link WrappedKey} class using the specified key id, encrypted key and
     * the algorithm.
     *
     * @param keyId The key identifier string.
     * @param encryptedKey The encrypted content encryption key.
     * @param algorithm The algorithm used for wrapping.
     */
    WrappedKey(String keyId, byte[] encryptedKey, String algorithm) {
        this.keyId = keyId;
        this.encryptedKey = encryptedKey;
        this.algorithm = algorithm;
    }

    /**
     * Gets the key identifier. This identifier is used to identify the key that is used to wrap/unwrap the content
     * encryption key.
     *
     * @return The key identifier string.
     */
    String getKeyId() {
        return keyId;
    }

    /**
     * Gets the encrypted content encryption key.
     *
     * @return The encrypted content encryption key.
     */
    byte[] getEncryptedKey() {
        return encryptedKey;
    }

    /**
     * Gets the algorithm used for wrapping.
     *
     * @return The algorithm used for wrapping.
     */
    String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the key identifier. This identifier is used to identify the key that is used to wrap/unwrap the content
     * encryption key.
     *
     * @param keyId The key identifier string.
     *
     * @return this
     */
    WrappedKey setKeyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Sets the encrypted content encryption key.
     *
     * @param encryptedKey The encrypted content encryption key.
     *
     * @return this
     */
    WrappedKey setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
        return this;
    }

    /**
     * Sets the algorithm used for wrapping.
     *
     * @param algorithm The algorithm used for wrapping.
     *
     * @return this
     */
    WrappedKey setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("KeyId", keyId)
            .writeBinaryField("EncryptedKey", encryptedKey)
            .writeStringField("Algorithm", algorithm)
            .writeEndObject();
    }

    /**
     * Reads an instance of WrappedKey from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return The WrappedKey read from the JsonReader.
     * @throws IOException If an I/O error occurs.
     */
    public static WrappedKey fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            WrappedKey wrappedKey = new WrappedKey();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("KeyId".equals(fieldName)) {
                    wrappedKey.keyId = reader.getString();
                } else if ("EncryptedKey".equals(fieldName)) {
                    wrappedKey.encryptedKey = reader.getBinary();
                } else if ("Algorithm".equals(fieldName)) {
                    wrappedKey.algorithm = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return wrappedKey;
        });
    }
}
