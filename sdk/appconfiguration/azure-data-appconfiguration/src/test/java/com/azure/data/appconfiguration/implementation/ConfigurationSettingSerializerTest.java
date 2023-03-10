// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingSerializationHelper.writeFeatureFlagConfigurationSetting;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingSerializationHelper.writeSecretReferenceConfigurationSetting;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ConfigurationSettingSerializationHelper}
 */
public class ConfigurationSettingSerializerTest {
    private static final String FEATURE_FLAG_VALUE_JSON =
        "{\"id\":\"hello\",\"description\":null,\"display_name\":\"Feature Flag X\",\"enabled\":false,"
            + "\"conditions\":{\"client_filters\":[{\"name\":\"Microsoft.Percentage\",\"parameters\":"
            + "{\"Value\":\"30\"}}]}}";
    private static final String SECRET_REFERENCE_VALUE_JSON = "{\"uri\":\"https://localhost\"}";

    @Test
    public void writeFeatureFlagConfigurationSettingTest() throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Value", "30");
        final List<FeatureFlagFilter> filters = new ArrayList<>();
        filters.add(new FeatureFlagFilter("Microsoft.Percentage")
                        .setParameters(parameters));
        final FeatureFlagConfigurationSetting setting = new FeatureFlagConfigurationSetting("hello", false)
                   .setDisplayName("Feature Flag X")
                   .setClientFilters(filters);
        assertEquals(FEATURE_FLAG_VALUE_JSON, writeFeatureFlagConfigurationSetting(setting));
    }

    @Test
    public void writeSecretReferenceConfigurationSettingTest() throws IOException {
        final String uriValue = "https://localhost";
        final String key = "hello";
        final SecretReferenceConfigurationSetting setting = new SecretReferenceConfigurationSetting(key, uriValue);
        assertEquals(SECRET_REFERENCE_VALUE_JSON, writeSecretReferenceConfigurationSetting(setting));
    }
}
