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

package com.microsoft.azure.storage.queue;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.azure.storage.core.EncryptionData;
import com.microsoft.azure.storage.core.JsonUtilities;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents the encrypted message that is stored on the service.
 */
class CloudQueueEncryptedMessage {

    /**
     * The encrypted message.
     */
    private String encryptedMessageContents;

    /**
     * The encryption related metadata for queue messages.
     */
    private EncryptionData encryptionData;

    /**
     * Gets the encryption related metadata for queue messages.
     * 
     * @return The encryption related metadata for queue messages.
     */
    public String getEncryptedMessageContents() {
        return this.encryptedMessageContents;
    }

    /**
     * Gets the encryption related metadata for queue messages.
     * 
     * @return The encrypted message.
     */
    public EncryptionData getEncryptionData() {
        return this.encryptionData;
    }

    /**
     * Sets the encryption related metadata for queue messages.
     * 
     * @param encryptedMessageContents
     *            The encryption related metadata for queue messages
     */
    public void setEncryptedMessageContents(String encryptedMessageContents) {
        this.encryptedMessageContents = encryptedMessageContents;
    }

    /**
     * Sets the encryption related metadata for queue messages.
     * 
     * @param encryptionData
     *            The encrypted message.
     */
    public void setEncryptionData(EncryptionData encryptionData) {
        this.encryptionData = encryptionData;
    }

    public String serialize() throws IOException {
        final StringWriter strWriter = new StringWriter();
        JsonGenerator generator = Utility.getJsonGenerator(strWriter);

        try {
            // start object
            generator.writeStartObject();

            // write message contents
            generator.writeStringField("EncryptedMessageContents", this.getEncryptedMessageContents());

            // write encryption data
            generator.writeObjectFieldStart("EncryptionData");
            this.getEncryptionData().serialize(generator);
            generator.writeEndObject();

            // end object
            generator.writeEndObject();
        }
        finally {
            generator.close();
        }

        return strWriter.toString();
    }

    public static CloudQueueEncryptedMessage deserialize(String inputMessage) throws JsonProcessingException,
            IOException {
        JsonParser parser = Utility.getJsonParser(inputMessage);
        CloudQueueEncryptedMessage message = new CloudQueueEncryptedMessage();
        try {
            if (!parser.hasCurrentToken()) {
                parser.nextToken();
            }

            JsonUtilities.assertIsStartObjectJsonToken(parser);

            parser.nextToken();

            while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                parser.nextToken();

                if (name.equals("EncryptedMessageContents")) {
                    message.setEncryptedMessageContents(parser.getValueAsString());
                }
                else if (name.equals("EncryptionData")) {
                    message.setEncryptionData(EncryptionData.deserialize(parser));
                }
                parser.nextToken();
            }

            JsonUtilities.assertIsEndObjectJsonToken(parser);
        }
        finally {
            parser.close();
        }

        return message;
    }
}
