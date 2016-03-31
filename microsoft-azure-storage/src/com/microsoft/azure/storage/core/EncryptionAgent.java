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
 * RESERVED FOR INTERNAL USE. Represents the encryption agent stored on the service. It consists of the encryption
 * protocol version and encryption algorithm used.
 */
public class EncryptionAgent {

    /**
     * The algorithm used for encryption.
     */
    public EncryptionAlgorithm encryptionAlgorithm;

    /**
     * The protocol version used for encryption.
     */
    public String protocol;

    /**
     * Initializes a new instance of the {@link EncryptionAgent} class.
     */
    public EncryptionAgent() {}
    
    /**
     * Initializes a new instance of the {@link EncryptionAgent} class using the specified protocol version and the
     * algorithm.
     * 
     * @param protocol
     *            The encryption protocol version.
     * @param algorithm
     *            The encryption algorithm.
     */
    public EncryptionAgent(String protocol, EncryptionAlgorithm algorithm) {
        this.protocol = protocol;
        this.encryptionAlgorithm = algorithm;
    }

    /**
     * Gets the algorithm used for encryption.
     * 
     * @return The algorithm used for encryption.
     */
    public EncryptionAlgorithm getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    /**
     * Gets the protocol version used for encryption.
     * 
     * @return The protocol version used for encryption.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the algorithm used for encryption.
     * 
     * @param encryptionAlgorithm
     *            The algorithm used for encryption.
     */
    public void setEncryptionAlgorithm(EncryptionAlgorithm encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    /**
     * Sets the protocol version used for encryption.
     * 
     * @param protocol
     *            The protocol version used for encryption.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public void serialize(JsonGenerator generator) throws IOException {
        
        // write protocol
        generator.writeStringField("Protocol", this.getProtocol());
        
        // write encryption algorithm
        generator.writeStringField("EncryptionAlgorithm", this.getEncryptionAlgorithm().toString());
    }
    
    public static EncryptionAgent deserialize(JsonParser parser) throws JsonParseException, IOException {
        JsonUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();
        
        EncryptionAgent agent = new EncryptionAgent();
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            parser.nextToken();
            
            if (name.equals("Protocol")) {
                agent.setProtocol(parser.getValueAsString());
            } 
            else if (name.equals("EncryptionAlgorithm")) {
                agent.setEncryptionAlgorithm(EncryptionAlgorithm.valueOf(parser.getValueAsString()));
            }
            parser.nextToken();
        }
        
        JsonUtilities.assertIsEndObjectJsonToken(parser);
        
        return agent;
    }
}
