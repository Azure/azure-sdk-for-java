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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * An optional helper class for serializing a digital twin.
 */
public final class BasicDigitalTwinSerializer extends StdSerializer<BasicDigitalTwin> {
    private static final long serialVersionUID = 1L;
    
    public BasicDigitalTwinSerializer() { 
        this(null); 
    } 

    public BasicDigitalTwinSerializer(Class<BasicDigitalTwin> t) { 
        super(t); 
    }

    @Override
    public void serialize(BasicDigitalTwin value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        jgen.writeStartObject();
        jgen.writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID, value.getId());
        jgen.writeStringField(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG, value.getETag());
        
        // Write contents
        for (Entry<String, Object> contentEntry : value.getContents().entrySet()) {
            jgen.writeFieldName(contentEntry.getKey());
            mapper.writeValue(jgen, contentEntry.getValue());
        }
        
        // Write metadata
        jgen.writeFieldName(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA);
        
        String partialMetadata = mapper.writeValueAsString(value.getMetadata());
        JsonNode partialMetadataNode = mapper.readTree(partialMetadata);
        
        jgen.writeStartObject();
        
        Iterator<Entry<String, JsonNode>> iter = partialMetadataNode.fields();
        
        while (iter.hasNext()) {
            Entry<String, JsonNode> nextNode = iter.next();
            
            String metadataPropertyName = nextNode.getKey();
            JsonNode metadataPropertyValue = nextNode.getValue();
            
            jgen.writeFieldName(metadataPropertyName);
            mapper.writeValue(jgen, metadataPropertyValue);
        }
        
        if (value.getLastUpdatedOn() != null) {
            jgen.writeFieldName(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME);
            mapper.writeValue(jgen, value.getLastUpdatedOn().toString());
        }
        
        jgen.writeEndObject();
        
        jgen.writeEndObject();
    }
}
