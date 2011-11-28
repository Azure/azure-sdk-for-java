package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class BrokerPropertiesMapper {

    public BrokerProperties fromString(String value) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(value.getBytes(), BrokerProperties.class);
        }
        catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
    }

    public String toString(BrokerProperties value) {
        ObjectMapper mapper = new ObjectMapper();
        Writer writer = new StringWriter();
        try {
            mapper.writeValue(writer, value);
        }
        catch (JsonGenerationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

}
