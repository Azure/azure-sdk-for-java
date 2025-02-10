// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents the encryption agent stored on the service. It consists of the encryption protocol version and encryption
 * algorithm used.
 */
final class EncryptionAgent implements JsonSerializable<EncryptionAgent> {

    /**
     * The protocol version used for encryption.
     */
    private String protocol;

    /**
     * The algorithm used for encryption.
     */
    private EncryptionAlgorithm algorithm;

    /**
     * Initializes a new instance of the {@link EncryptionAgent} class.
     */
    EncryptionAgent() {
    }

    /**
     * Initializes a new instance of the {@link EncryptionAgent} class using the specified protocol version and the
     * algorithm.
     *
     * @param protocol The encryption protocol version.
     * @param algorithm The encryption algorithm.
     */
    EncryptionAgent(String protocol, EncryptionAlgorithm algorithm) {
        this.protocol = protocol;
        this.algorithm = algorithm;
    }

    /**
     * Gets the protocol version used for encryption.
     *
     * @return The protocol version used for encryption.
     */
    String getProtocol() {
        return protocol;
    }

    /**
     * Gets the algorithm used for encryption.
     *
     * @return The algorithm used for encryption.
     */
    EncryptionAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the protocol version used for encryption.
     *
     * @param protocol The protocol version used for encryption.
     *
     * @return this
     */
    public EncryptionAgent setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Sets the algorithm used for encryption.
     *
     * @param algorithm The algorithm used for encryption.
     *
     * @return this
     */
    public EncryptionAgent setAlgorithm(EncryptionAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Protocol", protocol)
            .writeStringField("EncryptionAlgorithm", algorithm == null ? null : algorithm.toString())
            .writeEndObject();
    }

    /**
     * Reads an instance of EncryptionAgent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return The EncryptionAgent read from the JsonReader.
     * @throws IOException If an I/O error occurs.
     */
    public static EncryptionAgent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            EncryptionAgent encryptionAgent = new EncryptionAgent();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Protocol".equals(fieldName)) {
                    encryptionAgent.protocol = reader.getString();
                } else if ("EncryptionAlgorithm".equals(fieldName)) {
                    encryptionAgent.algorithm = EncryptionAlgorithm.valueOf(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return encryptionAgent;
        });
    }
}
