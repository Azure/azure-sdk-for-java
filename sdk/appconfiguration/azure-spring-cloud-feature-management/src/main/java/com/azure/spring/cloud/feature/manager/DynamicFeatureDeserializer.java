package com.azure.spring.cloud.feature.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.azure.spring.cloud.feature.manager.entities.FeatureVariant;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class DynamicFeatureDeserializer extends StdDeserializer<FeatureVariant> {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public DynamicFeatureDeserializer() {
        this(null);
    }

    protected DynamicFeatureDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public FeatureVariant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);
        FeatureVariant variant = new FeatureVariant();
        Map<String, JsonNode> map = new HashMap<>();

        Iterator<String> fieldNames = node.fieldNames();

        while (fieldNames.hasNext()) {
            String name = fieldNames.next();

            map.put(translateLowerCaseWithSeparator(name), node.get(name));
        }

        variant.setName(map.get("name").asText());

        variant.setConfigurationReference(map.get("configurationReference").asText());
        variant.setAssignmentParameters(
            MAPPER.convertValue(map.get("assignmentParameters"), new TypeReference<LinkedHashMap<String, Object>>() {
            }));

        if (map.containsKey("default")) {
            variant.setDefault(map.get("default").asBoolean());
        }

        return variant;
    }

    private String translateLowerCaseWithSeparator(final String input) {
        if (input == null) {
            return input; // garbage in, garbage out
        }
        final int length = input.length();
        if (length == 0) {
            return input;
        }

        final StringBuilder result = new StringBuilder(length + (length >> 1));

        for (int i = 0; i < length - 1; i++) {
            char ch = input.charAt(i);
            char next = input.charAt(i + 1);
            char uc = Character.toUpperCase(next);

            if ("-".equals(String.valueOf(ch))) {
                result.append(uc);
                i++;
            } else {
                result.append(ch);
            }
        }
        result.append(input.charAt(length - 1));
        return result.toString();
    }

}
