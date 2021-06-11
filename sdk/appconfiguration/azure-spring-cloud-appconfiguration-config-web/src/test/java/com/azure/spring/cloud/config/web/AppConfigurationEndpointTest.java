// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.azure.spring.cloud.config.properties.ConfigStore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AppConfigurationEndpointTest {

    private static final String GET_TEST_VALIDATION = "src/test/resources/webHookValidation.json";

    private static final String GET_TEST_REFRESH = "src/test/resources/webHookRefresh.json";

    private static final String GET_TEST_INVALID = "src/test/resources/webHookInvalid.json";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void validationParsing() throws JsonGenerationException, JsonMappingException, IOException {
        JsonNode request = mapper.readValue(new File(GET_TEST_VALIDATION), JsonNode.class);
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        Map<String, String> allRequestParams = new HashMap<String, String>();

        AppConfigurationEndpoint endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        assertEquals("https://testConfig.azconfig.io", endpoint.getEndpoint());
        assertEquals("testConfig", endpoint.getStore());

        request = mapper.readValue(new File(GET_TEST_REFRESH), JsonNode.class);
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        assertEquals("https://testConfig.azconfig.io", endpoint.getEndpoint());
        assertEquals("testConfig", endpoint.getStore());
    }

    @Test
    public void validationInvalidParsing() throws JsonGenerationException, JsonMappingException, IOException {
        JsonNode request = mapper.readValue(new File(GET_TEST_INVALID), JsonNode.class);
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        Map<String, String> allRequestParams = new HashMap<String, String>();
        expected.expect(IllegalArgumentException.class);

        new AppConfigurationEndpoint(request, configStores, allRequestParams);
    }

    @Test
    public void authenticate() throws JsonParseException, JsonMappingException, IOException {
        JsonNode request = mapper.readValue(new File(GET_TEST_VALIDATION), JsonNode.class);
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        Map<String, String> allRequestParams = new HashMap<String, String>();

        AppConfigurationEndpoint endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // No Config Stores Set Up.
        assertFalse(endpoint.authenticate());

        ConfigStore invalidConfigStore = new ConfigStore();
        invalidConfigStore.setEndpoint("https://invalidConfigStore.azconfig.io");
        configStores.add(invalidConfigStore);
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // There is a config Store, but not the right one
        assertFalse(endpoint.authenticate());

        ConfigStore validConfigStore = new ConfigStore();
        validConfigStore.setEndpoint("https://testConfig.azconfig.io");
        configStores.add(validConfigStore);
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, no secrets
        assertFalse(endpoint.authenticate());

        validConfigStore.getMonitoring().getPushNotification().getPrimaryToken().setName("token");
        validConfigStore.getMonitoring().getPushNotification().getPrimaryToken().setSecret("secret");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, no authentication secrets
        assertFalse(endpoint.authenticate());

        allRequestParams.put("token", "bad");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, invalid secret
        assertFalse(endpoint.authenticate());

        allRequestParams.put("token", "secret");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, invalid secret
        assertTrue(endpoint.authenticate());

        // Reseting Primary
        allRequestParams.remove("token");
        validConfigStore.getMonitoring().getPushNotification().getPrimaryToken().setName("");
        validConfigStore.getMonitoring().getPushNotification().getPrimaryToken().setSecret("");

        validConfigStore.getMonitoring().getPushNotification().getSecondaryToken().setName("token");
        validConfigStore.getMonitoring().getPushNotification().getSecondaryToken().setSecret("secret");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, no authentication secrets
        assertFalse(endpoint.authenticate());

        allRequestParams.put("token", "bad");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, invalid secret
        assertFalse(endpoint.authenticate());

        allRequestParams.put("token", "secret");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Valid Config Store, invalid secret
        assertTrue(endpoint.authenticate());

        validConfigStore.getMonitoring().getPushNotification().getPrimaryToken().setName("token");
        validConfigStore.getMonitoring().getPushNotification().getPrimaryToken().setSecret("primary");
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Both Primary and Secondary Set
        assertTrue(endpoint.authenticate());
    }

    @Test
    public void triggerRefresh() throws JsonParseException, JsonMappingException, IOException {
        JsonNode request = mapper.readValue(new File(GET_TEST_VALIDATION), JsonNode.class);
        List<ConfigStore> configStores = new ArrayList<ConfigStore>();
        Map<String, String> allRequestParams = new HashMap<String, String>();

        ConfigStore invalidConfigStore = new ConfigStore();
        invalidConfigStore.setEndpoint("https://invalidConfigStore.azconfig.io");
        configStores.add(invalidConfigStore);
        AppConfigurationEndpoint endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Invalid Store
        assertFalse(endpoint.authenticate());

        ConfigStore validConfigStore = new ConfigStore();
        validConfigStore.setEndpoint("https://testConfig.azconfig.io");

        configStores.add(validConfigStore);
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Monitoring Disabled
        assertFalse(endpoint.triggerRefresh());

        validConfigStore.getMonitoring().setEnabled(true);
        configStores.add(validConfigStore);
        endpoint = new AppConfigurationEndpoint(request, configStores, allRequestParams);
        // Monitoring Enabled
        assertTrue(endpoint.triggerRefresh());
    }

}
