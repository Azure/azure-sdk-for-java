// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ConfigurationSettingJsonSerializerTest}
 */
public class ConfigurationSettingJsonSerializerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void configurationSettingJsonSerializerTest() throws IOException {
        final ConfigurationSettingJsonSerializer configurationSettingJsonSerializer =
            new ConfigurationSettingJsonSerializer();
        final String key = ".appconfig.featureflag/hello";
        final String featureID = "hello";
        final FeatureFlagConfigurationSetting featureSetting =
            getFeatureFlagConfigurationSetting(featureID, "Feature Flag X");
        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        final SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        configurationSettingJsonSerializer.serialize(featureSetting, jsonGenerator, serializerProvider);
        jsonGenerator.close();

        final ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertEquals(objectNode.get("key").asText(), key);
        assertEquals(objectNode.get("value").asText(),
            "{\"id\":\"" + featureID + "\",\"description\":null,\"display_name\":\"Feature Flag X\",\"enabled\""
                + ":false,\"conditions\":{\"client_filters\":[{\"name\":\"Microsoft.Percentage\",\"parameters\""
                + ":{\"Value\":\"30\"}}]}}");
    }

    @Test
    public void secretReferenceConfigurationSettingJsonSerializerTest() throws IOException {
        final ConfigurationSettingJsonSerializer configurationSettingJsonSerializer =
            new ConfigurationSettingJsonSerializer();
        final String uriValue = "https://localhost/";
        final String key = "hello";
        final SecretReferenceConfigurationSetting setting = new SecretReferenceConfigurationSetting(key, uriValue);
        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        final SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        configurationSettingJsonSerializer.serialize(setting, jsonGenerator, serializerProvider);
        jsonGenerator.close();

        final ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertEquals(key, objectNode.get("key").asText());
        assertEquals("{\"uri\":\"" + uriValue + "\"}", objectNode.get("value").asText());
    }

    private FeatureFlagConfigurationSetting getFeatureFlagConfigurationSetting(String key, String displayName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Value", "30");
        final List<FeatureFlagFilter> filters = new ArrayList<>();
        filters.add(new FeatureFlagFilter("Microsoft.Percentage")
                        .setParameters(parameters));
        return new FeatureFlagConfigurationSetting(key, false)
                   .setDisplayName(displayName)
                   .setClientFilters(filters);
    }
}
