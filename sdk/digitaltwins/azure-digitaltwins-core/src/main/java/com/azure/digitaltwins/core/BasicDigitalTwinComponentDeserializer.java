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
 * An optional, helper class for deserializing twin components.
 */
public final class BasicDigitalTwinComponentDeserializer extends StdDeserializer<BasicDigitalTwinComponent> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor for deserializer.
     */
    public BasicDigitalTwinComponentDeserializer() { 
        this(null); 
    } 

    /**
     * Constructor for deserializer allowing one pass in the value class.
     * @param vc The value class.
     */
    public BasicDigitalTwinComponentDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public BasicDigitalTwinComponent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        Iterator<Entry<String, JsonNode>> iter = node.fields();
        
        Map<String, DigitalTwinPropertyMetadata> metadata = new HashMap<>();
        OffsetDateTime lastUpdateTime = null;
        Map<String, Object> contents = new HashMap<>();
        while (iter.hasNext()) {
            Entry<String, JsonNode> nextField = iter.next();
            
            String propertyName = nextField.getKey();
            JsonNode value = nextField.getValue();
            
            if (propertyName.equals(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA)) {
                Iterator<Entry<String, JsonNode>> metadataIter = value.fields();
                
                while (metadataIter.hasNext()) {
                    Entry<String, JsonNode> nextMetadataItem = metadataIter.next();
                    
                    String metadataName = nextMetadataItem.getKey();
                    JsonNode metadataValue = nextMetadataItem.getValue();
                    
                    if (metadataName.equals(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME)) {
                        lastUpdateTime = OffsetDateTime.parse(metadataValue.asText());
                    }
                    else {
                        metadata.put(metadataName, mapper.convertValue(metadataValue, DigitalTwinPropertyMetadata.class));
                    }
                }
            }
            else {
                Object convertedValue;
                
                if (value.isBoolean()) {
                    convertedValue = value.asBoolean();
                }
                else if (value.isDouble()) {
                    convertedValue = value.asDouble();
                }
                else if (value.isInt()) {
                    convertedValue = value.asInt();
                }
                else if (value.isLong()) {
                    convertedValue = value.asLong();
                }
                else if (value.isTextual()) {
                    convertedValue = value.asText();
                }
                else {
                    convertedValue = value;
                }
                
                contents.put(propertyName, convertedValue);
            }
        }
        
        BasicDigitalTwinComponent component = new BasicDigitalTwinComponent(lastUpdateTime);
        
        for (Entry<String, DigitalTwinPropertyMetadata> item : metadata.entrySet()) {
            component.addMetadata(item.getKey(), item.getValue());
        }
        
        for (Entry<String, Object> item : contents.entrySet()) {
            component.addToContents(item.getKey(), item.getValue());
        }
        
        return component;
    }
}
