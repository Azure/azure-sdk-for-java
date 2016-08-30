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
import java.io.StringWriter;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.azure.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. Represents the encryption data that is stored on the service.
 */
public class EncryptionData {

    /**
     * The content encryption IV.
     */
    private byte[] contentEncryptionIV;

    /**
     * The encryption agent.
     */
    private EncryptionAgent encryptionAgent;

    /**
     * A {@link WrappedContentKey} object that stores the wrapping algorithm, key identifier and the encrypted key
     * bytes.
     */
    private WrappedContentKey wrappedContentKey;
    
    /**
     * Metadata for encryption.  Currently used only for storing the encryption library, but may contain other data.
     */
    private HashMap<String, String> keyWrappingMetadata;
    
    /**
     * Gets the content encryption IV.
     * 
     * @return The content encryption IV.
     */
    public byte[] getContentEncryptionIV() {
        return this.contentEncryptionIV;
    }

    /**
     * Gets the encryption agent that is used to identify the encryption protocol version and encryption algorithm.
     * 
     * @return The encryption agent.
     */
    public EncryptionAgent getEncryptionAgent() {
        return this.encryptionAgent;
    }

    /**
     * Gets the wrapped key that is used to store the wrapping algorithm, key identifier and the encrypted key bytes.
     * 
     * @return A {@link WrappedContentKey} object that stores the wrapping algorithm, key identifier and the encrypted
     *         key bytes.
     */
    public WrappedContentKey getWrappedContentKey() {
        return this.wrappedContentKey;
    }
    
    /**
     * Gets the metadata for encryption.
     * 
     * @return A HashMap containing the encryption metadata in a key-value format.
     */
    public HashMap<String, String> getKeyWrappingMetadata() {
        return this.keyWrappingMetadata;
    }

    /**
     * Sets the content encryption IV.
     * 
     * @param contentEncryptionIV
     *            The content encryption IV.
     */
    public void setContentEncryptionIV(byte[] contentEncryptionIV) {
        this.contentEncryptionIV = contentEncryptionIV;
    }

    /**
     * Sets the encryption agent that is used to identify the encryption protocol version and encryption algorithm.
     * 
     * @param encryptionAgent
     *            The encryption agent.
     */
    public void setEncryptionAgent(EncryptionAgent encryptionAgent) {
        this.encryptionAgent = encryptionAgent;
    }

    /**
     * Sets the wrapped key that is used to store the wrapping algorithm, key identifier and the encrypted key bytes.
     * 
     * @param wrappedContentKey
     *            A {@link WrappedContentKey} object that stores the wrapping algorithm, key identifier and the
     *            encrypted key bytes.
     */
    public void setWrappedContentKey(WrappedContentKey wrappedContentKey) {
        this.wrappedContentKey = wrappedContentKey;
    }
    
    /**
     * Sets the metadata for encryption.
     * 
     * @param keyWrappingMetadata A HashMap containing the encryption metadata in a key-value format.
     */
    public void setKeyWrappingMetadata(HashMap<String, String> keyWrappingMetadata) {
        this.keyWrappingMetadata = keyWrappingMetadata;
    }
    
    public String serialize() throws IOException {
        
        final StringWriter strWriter = new StringWriter();
        JsonGenerator generator = Utility.getJsonGenerator(strWriter);
        
        try {
            // start object
            generator.writeStartObject();
            
            this.serialize(generator);

            // end object
            generator.writeEndObject();
        }
        finally {
            generator.close();
        }
        
        return strWriter.toString();
    }
    
    public void serialize(JsonGenerator generator) throws IOException {
        
        // write wrapped content key
        generator.writeObjectFieldStart(Constants.EncryptionConstants.WRAPPED_CONTENT_KEY);
        this.getWrappedContentKey().serialize(generator);
        generator.writeEndObject();
        
        // write encryption agent
        generator.writeObjectFieldStart(Constants.EncryptionConstants.ENCRYPTION_AGENT);
        this.getEncryptionAgent().serialize(generator);
        generator.writeEndObject();
        
        // write content encryption IV
        generator.writeBinaryField(Constants.EncryptionConstants.CONTENT_ENCRYPTION_IV, this.getContentEncryptionIV());
        
        // write key wrapping metadata
        generator.writeObjectFieldStart(Constants.EncryptionConstants.KEY_WRAPPING_METADATA);
        for (String key : this.keyWrappingMetadata.keySet()) {
            generator.writeStringField(key, this.keyWrappingMetadata.get(key));
        }
        generator.writeEndObject();

    }
    
    public void copyValues(EncryptionData data) throws JsonProcessingException, IOException {
        this.setWrappedContentKey(data.getWrappedContentKey());
        this.setEncryptionAgent(data.getEncryptionAgent());
        this.setContentEncryptionIV(data.getContentEncryptionIV());
        this.setKeyWrappingMetadata(data.getKeyWrappingMetadata());
    }

    public static EncryptionData deserialize(String inputData) 
            throws JsonProcessingException, IOException  {
        JsonParser parser = Utility.getJsonParser(inputData);
        try {
            if (!parser.hasCurrentToken()) {
                parser.nextToken();
            }

            return EncryptionData.deserialize(parser);
        }
        finally {
            parser.close();
        }
    }
    
    public static EncryptionData deserialize(JsonParser parser) throws JsonParseException, IOException {
        JsonUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();
        
        EncryptionData data = new EncryptionData();
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            parser.nextToken();
            
            if (name.equals(Constants.EncryptionConstants.WRAPPED_CONTENT_KEY)) {
                data.setWrappedContentKey(WrappedContentKey.deserialize(parser));
            } 
            else if (name.equals(Constants.EncryptionConstants.ENCRYPTION_AGENT)) {
                data.setEncryptionAgent(EncryptionAgent.deserialize(parser));
            }
            else if (name.equals(Constants.EncryptionConstants.CONTENT_ENCRYPTION_IV)) {
                data.setContentEncryptionIV(parser.getBinaryValue());
            }
            else if (name.equals(Constants.EncryptionConstants.KEY_WRAPPING_METADATA)) {
                data.setKeyWrappingMetadata(deserializeKeyWrappingMetadata(parser));
            }
            else {
                consumeJsonObject(parser);
            }
            parser.nextToken();
        }
        
        JsonUtilities.assertIsEndObjectJsonToken(parser);
        
        return data;
    }
    
    public static HashMap<String, String> deserializeKeyWrappingMetadata(JsonParser parser) throws JsonParseException, IOException {
        JsonUtilities.assertIsStartObjectJsonToken(parser);

        parser.nextToken();
        
        HashMap<String, String> keyWrappingMetadata = new HashMap<String, String>();
        while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            String name = parser.getCurrentName();
            parser.nextToken();            
            keyWrappingMetadata.put(name, parser.getValueAsString());
            parser.nextToken();
        }
        
        JsonUtilities.assertIsEndObjectJsonToken(parser);
        
        return keyWrappingMetadata;
    }
    
    public static void consumeJsonObject(JsonParser parser) throws IOException {
        JsonUtilities.assertIsStartObjectJsonToken(parser);
        parser.nextToken();
        if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            consumeJsonObject(parser);
        }
    }
}
