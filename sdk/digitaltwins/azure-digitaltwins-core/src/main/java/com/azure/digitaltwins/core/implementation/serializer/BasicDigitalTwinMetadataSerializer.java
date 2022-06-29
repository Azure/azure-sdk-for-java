// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serializer;

import java.io.IOException;
import java.util.Map.Entry;

import com.azure.digitaltwins.core.BasicDigitalTwinMetadata;
import com.azure.digitaltwins.core.DigitalTwinPropertyMetadata;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * An optional helper class for serializing twin metadata.
 */
public final class BasicDigitalTwinMetadataSerializer extends StdSerializer<BasicDigitalTwinMetadata> {
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor for serializer.
     */
    public BasicDigitalTwinMetadataSerializer() { 
        this(null); 
    } 

    /**
     * Constructor for serializer allowing one pass in the handled type.
     * @param t The handled type.
     */
    public BasicDigitalTwinMetadataSerializer(Class<BasicDigitalTwinMetadata> t) { 
        super(t); 
    }

    @Override
    public void serialize(BasicDigitalTwinMetadata value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        jgen.writeStartObject();
        
        jgen.writeStringField(DigitalTwinsJsonPropertyNames.METADATA_MODEL, value.getModelId());
        
        for (Entry<String, DigitalTwinPropertyMetadata> propertyMetadataEntry : value.getPropertyMetadata().entrySet()) {
            jgen.writeFieldName(propertyMetadataEntry.getKey());
            mapper.writeValue(jgen, propertyMetadataEntry.getValue());
        }
        
        jgen.writeEndObject();
    }
}
