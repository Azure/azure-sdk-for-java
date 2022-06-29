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
 * An optional helper class for deserializing a digital twin.
 */
public final class BasicDigitalTwinDeserializer extends StdDeserializer<BasicDigitalTwin> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor for deserializer.
     */
    public BasicDigitalTwinDeserializer() { 
        this(null); 
    } 

    /**
     * Constructor for deserializer allowing one pass in the value class.
     * @param vc The value class.
     */
    public BasicDigitalTwinDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public BasicDigitalTwin deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        Iterator<Entry<String, JsonNode>> iter = node.fields();
        
        String id = null;
        BasicDigitalTwinMetadata metadata = null;
        OffsetDateTime lastUpdateTime = null;
        String etag = null;
        Map<String, Object> contents = new HashMap<String, Object>();
        while (iter.hasNext()) {
            Entry<String, JsonNode> nextField = iter.next();
            
            String propertyName = nextField.getKey();
            JsonNode value = nextField.getValue();
            
            if (propertyName.equals(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID)) {
                id = value.asText();
            }
            else if (propertyName.equals(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG)) {
                etag = value.asText();
            }
            else if (propertyName.equals(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA)) {
                metadata = mapper.convertValue(value, BasicDigitalTwinMetadata.class);
                
                JsonNode lastUpdateTimeNode = value.get(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME);
                if (lastUpdateTimeNode != null) {
                    lastUpdateTime = OffsetDateTime.parse(lastUpdateTimeNode.asText());
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
        
        BasicDigitalTwin twin = new BasicDigitalTwin(id, lastUpdateTime);
        twin.setETag(etag);
        twin.setMetadata(metadata);
        
        for (Entry<String, Object> item : contents.entrySet()) {
            twin.addToContents(item.getKey(), item.getValue());
        }
        
        return twin;
    }
}
