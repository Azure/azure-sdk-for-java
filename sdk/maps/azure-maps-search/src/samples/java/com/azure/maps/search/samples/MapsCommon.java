package com.azure.maps.search.samples;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MapsCommon {

    public static String getUid(String url) {
        Pattern pattern = Pattern.compile("[0-9A-Fa-f\\-]{36}");
        Matcher matcher = pattern.matcher(url);
        matcher.find();
        return matcher.group();
    }

    public static void print(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String json;
        json = ow.writeValueAsString(object);
        System.out.println(json);
    }

    public static InputStream getResource(String path) {
        return new MapsCommon().getClass().getResourceAsStream(path);
    }

    public static String readContent(InputStream stream) {
        Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static <T> T readJson(String content, Class<T> valueType)
            throws JsonMappingException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(content, valueType);
    }
}
