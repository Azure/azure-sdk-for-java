/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * RESERVED FOR INTERNAL USE. Represents the envelope key details stored on the service.
 */
public class WrappedContentKey {

    /**
     * The algorithm used for wrapping.
     */
    public String algorithm;

    /**
     * The encrypted content encryption key.
     */
    public byte[] encryptedKey;

    /**
     * The key identifier string.
     */
    public String keyId;

    /**
     * Initializes a new instance of the {@link WrappedContentKey} class.
     */
    public WrappedContentKey() {
    }

    /**
     * Initializes a new instance of the {@link WrappedContentKey} class using the specified key id, encrypted key and
     * the algorithm.
     * 
     * @param keyId
     *            The key identifier string.
     * @param encryptedKey
     *            The encrypted content encryption key.
     * @param algorithm
     *            The algorithm used for wrapping.
     */
    public WrappedContentKey(String keyId, byte[] encryptedKey, String algorithm) {
        this.keyId = keyId;
        this.encryptedKey = encryptedKey;
        this.algorithm = algorithm;
    }

    /**
     * Gets the algorithm used for wrapping.
     * 
     * @return The algorithm used for wrapping.
     */
    public String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Gets the encrypted content encryption key.
     * 
     * @return The encrypted content encryption key.
     */
    public byte[] getEncryptedKey() {
        return this.encryptedKey;
    }

    /**
     * Gets the key identifier. This identifier is used to identify the key that is used to wrap/unwrap the content
     * encryption key.
     * 
     * @return The key identifier string.
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Sets the algorithm used for wrapping.
     * 
     * @param algorithm
     *            The algorithm used for wrapping.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Sets the encrypted content encryption key.
     * 
     * @param encryptedKey
     *            The encrypted content encryption key.
     */
    public void setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    /**
     * Sets the key identifier. This identifier is used to identify the key that is used to wrap/unwrap the content
     * encryption key.
     * 
     * @param keyId
     *            The key identifier string.
     */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void serialize(JsonGenerator generator) throws IOException {

        // write key id
        generator.writeStringField("KeyId", this.getKeyId());

        // write encrypted key
        generator.writeBinaryField("EncryptedKey", this.getEncryptedKey());

        // write algorithm
        generator.writeStringField("Algorithm", this.getAlgorithm());
    }

    public static WrappedContentKey deserialize(JsonParser parser) throws JsonParseException, IOException {
        JsonUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();

        WrappedContentKey wrappedContentKey = new WrappedContentKey();
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            parser.nextToken();

            if (name.equals("KeyId")) {
                wrappedContentKey.setKeyId(parser.getValueAsString());
            }
            else if (name.equals("EncryptedKey")) {
                wrappedContentKey.setEncryptedKey(parser.getBinaryValue());
            }
            else if (name.equals("Algorithm")) {
                wrappedContentKey.setAlgorithm(parser.getValueAsString());
            }
            parser.nextToken();
        }

        JsonUtilities.assertIsEndObjectJsonToken(parser);

        return wrappedContentKey;
    }
}
