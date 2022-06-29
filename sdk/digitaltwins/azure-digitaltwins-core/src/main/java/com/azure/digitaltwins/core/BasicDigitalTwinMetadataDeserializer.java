// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * An optional helper class for deserializing twin metadata.
 */
public final class BasicDigitalTwinMetadataDeserializer extends StdDeserializer<BasicDigitalTwinMetadata> {
    private static final long serialVersionUID = 1L;
    
    public BasicDigitalTwinMetadataDeserializer() { 
        this(null); 
    } 

    public BasicDigitalTwinMetadataDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public BasicDigitalTwinMetadata deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        Iterator<Entry<String, JsonNode>> iter = node.fields();
        
        BasicDigitalTwinMetadata metadata = new BasicDigitalTwinMetadata();

        while (iter.hasNext()) {
            Entry<String, JsonNode> nextField = iter.next();
            
            String propertyName = nextField.getKey();
            JsonNode value = nextField.getValue();
            
            if (propertyName.equals(DigitalTwinsJsonPropertyNames.METADATA_MODEL)) {
                metadata.setModelId(value.asText());
            }
            else if (propertyName.equals(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME)) {
                // Do nothing
            }
            else {
                metadata.addPropertyMetadata(propertyName, mapper.convertValue(value, DigitalTwinPropertyMetadata.class));
            }
        }
        
        return metadata;
    }
}
