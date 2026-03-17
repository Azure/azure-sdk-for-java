// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConfigurationParserTest {

    private static final String JSON_CONTENT_TYPE_DATA = "src/test/resources/jsonContentTypeData.json";

    ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void isJsonContentType() {
        // Basic valid JSON content types
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/api+json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json+activity"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/vnd.xxxx+json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/vnd.microsoft.appconfig.document+json"));
        
        // Invalid content types
        assertFalse(JsonConfigurationParser.isJsonContentType("application"));
        assertFalse(JsonConfigurationParser.isJsonContentType("app/json"));
        assertFalse(JsonConfigurationParser.isJsonContentType("app/config"));
        assertFalse(JsonConfigurationParser.isJsonContentType("application/config"));
        assertFalse(JsonConfigurationParser.isJsonContentType(""));
        assertFalse(JsonConfigurationParser.isJsonContentType(null));
    }

    @Test
    public void isJsonContentTypeWithParameters() {
        // Content types with charset and other parameters
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json; charset=utf-8"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json;charset=utf-8"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json ; charset=utf-8"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/vnd.api+json; charset=utf-8"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json; charset=ISO-8859-1"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json;boundary=something"));
    }

    @Test
    public void isJsonContentTypeWithWhitespace() {
        // Content types with various whitespace
        assertTrue(JsonConfigurationParser.isJsonContentType(" application/json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json "));
        assertTrue(JsonConfigurationParser.isJsonContentType(" application/json "));
        assertTrue(JsonConfigurationParser.isJsonContentType("application / json")); // Note: This might fail if strict parsing is needed
        assertTrue(JsonConfigurationParser.isJsonContentType("application/vnd.api + json")); // Whitespace around +
    }

    @Test
    public void isJsonContentTypeCaseInsensitive() {
        // Case variations
        assertTrue(JsonConfigurationParser.isJsonContentType("Application/Json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("APPLICATION/JSON"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/JSON"));
        assertTrue(JsonConfigurationParser.isJsonContentType("Application/vnd.api+JSON"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/API+JSON"));
    }

    @Test
    public void isJsonContentTypeEdgeCases() {
        // Edge cases and boundary conditions
        assertFalse(JsonConfigurationParser.isJsonContentType("application/")); // Empty subtype
        assertFalse(JsonConfigurationParser.isJsonContentType("/json")); // Empty main type
        assertFalse(JsonConfigurationParser.isJsonContentType("/")); // Both empty
        assertFalse(JsonConfigurationParser.isJsonContentType("application/xml"));
        assertFalse(JsonConfigurationParser.isJsonContentType("text/json")); // Wrong main type
        assertFalse(JsonConfigurationParser.isJsonContentType("application/json+xml")); // json not as suffix
        assertFalse(JsonConfigurationParser.isJsonContentType("application/xml+html")); // No json at all
        assertFalse(JsonConfigurationParser.isJsonContentType("   ")); // Only whitespace
    }

    @Test
    public void isJsonContentTypeComplexStructuredSyntax() {
        // Complex structured syntax suffixes (RFC 6839)
        assertTrue(JsonConfigurationParser.isJsonContentType("application/problem+json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/merge-patch+json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/json-patch+json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/ld+json")); // JSON-LD
        assertTrue(JsonConfigurationParser.isJsonContentType("application/hal+json"));
        assertTrue(JsonConfigurationParser.isJsonContentType("application/vnd.geo+json"));
    }

    @Test
    public void parseJsonSettingTest() throws IOException {
        String key = "config.object";

        JsonNode json = jsonMapper.readValue(new File(JSON_CONTENT_TYPE_DATA), JsonNode.class);
        String jsonText = json.toPrettyString();

        ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue(jsonText);

        Map<String, Object> settings = JsonConfigurationParser.parseJsonSetting(setting);
        assertEquals(13, settings.size());
    }

    @Test
    public void parseSettingTest() throws IOException {
        String currentKey = "config.object";
        JsonNode json = jsonMapper.readValue(new File(JSON_CONTENT_TYPE_DATA), JsonNode.class);
        HashMap<String, Object> settings = new HashMap<>();

        JsonConfigurationParser.parseSetting(currentKey, json, settings);

        assertEquals(13, settings.size());
    }

}
