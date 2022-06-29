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
 * An optional helper class for serializing a twin components.
 */
public final class BasicDigitalTwinComponentSerializer extends StdSerializer<BasicDigitalTwinComponent> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor for serializer.
     */
    public BasicDigitalTwinComponentSerializer() { 
        this(null); 
    } 

    /**
     * Constructor for serializer allowing one pass in the handled type.
     * @param t The handled type.
     */
    public BasicDigitalTwinComponentSerializer(Class<BasicDigitalTwinComponent> t) { 
        super(t); 
    }

    @Override
    public void serialize(BasicDigitalTwinComponent value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        jgen.writeStartObject();
        
        // Write contents
        for (Entry<String, Object> contentEntry : value.getContents().entrySet()) {
            jgen.writeFieldName(contentEntry.getKey());
            mapper.writeValue(jgen, contentEntry.getValue());
        }
        
        // Write metadata
        jgen.writeFieldName(DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA);
        jgen.writeStartObject();
        
        jgen.writeFieldName(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME);
        mapper.writeValue(jgen, value.getLastUpdatedOn().toString());
        
        for (Entry<String, DigitalTwinPropertyMetadata> propertyMetadataEntry : value.getMetadata().entrySet()) {
            jgen.writeFieldName(propertyMetadataEntry.getKey());
            mapper.writeValue(jgen, propertyMetadataEntry.getValue());
        }
        jgen.writeEndObject();
        
        jgen.writeEndObject();
    }
}
