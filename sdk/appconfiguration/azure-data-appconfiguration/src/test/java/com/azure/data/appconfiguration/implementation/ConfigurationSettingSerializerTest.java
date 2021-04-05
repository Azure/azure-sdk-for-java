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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ConfigurationSettingSerializerTest}
 */
public class ConfigurationSettingSerializerTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void configurationSettingJsonSerializerTest() throws IOException {
        final ConfigurationSettingJsonSerializer configurationSettingJsonSerializer =
            new ConfigurationSettingJsonSerializer();

        final String featureID = "hello";
        List<FeatureFlagFilter> filters = new ArrayList<>();
        filters.add(new FeatureFlagFilter("Microsoft.Percentage")
                        .setParameters(new HashMap<>() {{
                            put("Value", "30");
                        }}));
        final FeatureFlagConfigurationSetting featureSetting =
            new FeatureFlagConfigurationSetting(featureID, false)
                .setDisplayName("Feature Flag X")
                .setClientFilters(filters)
                .setValue("val1");

        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        final SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        configurationSettingJsonSerializer.serialize(featureSetting, jsonGenerator, serializerProvider);
        jsonGenerator.close();

        final ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertEquals(objectNode.get("key").asText(), ".appconfig.featureflag/hello");
        assertEquals(objectNode.get("value").asText(),
            "{\"id\":\"hello\",\"description\":null,\"display_name\":\"Feature Flag X\",\"enabled\""
                + ":false,\"conditions\":{\"client_filters\":[{\"name\":\"Microsoft.Percentage\",\"parameters\""
                + ":{\"Value\":\"30\"}}]}}");
    }

    @Test
    public void secretReferenceConfigurationSettingJsonSerializerTest() throws IOException {
        final ConfigurationSettingJsonSerializer configurationSettingJsonSerializer =
            new ConfigurationSettingJsonSerializer();

        SecretReferenceConfigurationSetting setting = new SecretReferenceConfigurationSetting("hello",
            "https://localhost/");
        final StringWriter jsonWriter = new StringWriter();
        final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
        final SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        configurationSettingJsonSerializer.serialize(setting, jsonGenerator, serializerProvider);
        jsonGenerator.close();

        final ObjectNode objectNode = (ObjectNode) objectMapper.readTree(jsonWriter.toString());

        assertEquals("hello", objectNode.get("key").asText());
        assertEquals("{\"uri\":\"https://localhost/\"}", objectNode.get("value").asText());
    }
}
