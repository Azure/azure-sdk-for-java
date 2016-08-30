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
package com.microsoft.azure.storage.blob;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.core.EncryptionAgent;
import com.microsoft.azure.storage.core.EncryptionData;
import com.microsoft.azure.storage.core.JsonUtilities;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azure.storage.core.WrappedContentKey;

/**
 * Represents the blob encryption data that is stored on the service.
 */
class BlobEncryptionData extends EncryptionData {
    
    /**
     * The blob encryption mode.
     */
    private String encryptionMode;

    /**
     * Gets the blob client encryption mode.
     * 
     * @return The blob encryption mode
     */
    public String getEncryptionMode() {
        return this.encryptionMode;
    }

    /**
     * Sets the blob client encryption mode.
     * 
     * @param encryptionMode
     *          The blob encryption mode
     */
    public void setEncryptionMode(String encryptionMode) {
        this.encryptionMode = encryptionMode;
    }
    
    public String serialize() throws IOException {
        
        final StringWriter strWriter = new StringWriter();
        JsonGenerator generator = Utility.getJsonGenerator(strWriter);
        
        try {
            // start object
            generator.writeStartObject();
            
            // write the encryption mode
            generator.writeStringField(Constants.EncryptionConstants.ENCRYPTION_MODE, Constants.EncryptionConstants.FULL_BLOB);
            
            // write the encryption data
            this.serialize(generator);

            // end object
            generator.writeEndObject();
        }
        finally {
            generator.close();
        }
        
        return strWriter.toString();
    }
    
    public static BlobEncryptionData deserialize(String inputData) 
            throws JsonProcessingException, IOException  {
        JsonParser parser = Utility.getJsonParser(inputData);
        BlobEncryptionData data = new BlobEncryptionData();
        try {
            if (!parser.hasCurrentToken()) {
                parser.nextToken();
            }

            JsonUtilities.assertIsStartObjectJsonToken(parser);

            parser.nextToken();
            
            while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                parser.nextToken();
                
                if (name.equals(Constants.EncryptionConstants.ENCRYPTION_MODE)) {
                    data.setEncryptionMode(parser.getValueAsString());
                }
                else if (name.equals(Constants.EncryptionConstants.WRAPPED_CONTENT_KEY)) {
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
        }
        finally {
            parser.close();
        }
        
        return data;
    }
}
