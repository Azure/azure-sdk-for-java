package com.azure.search.data.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EntityMapper<T> {
    public Map<String, Object> objectToMap(T object) {
        ObjectMapper mapper = new ObjectMapper();
        String targetJson = null;
        HashMap result = null;

        try {
            targetJson = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e){
            Assert.fail(e.getMessage());
        }

        try {
            result = mapper.readValue(targetJson, HashMap.class);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        return result;
    }
}
