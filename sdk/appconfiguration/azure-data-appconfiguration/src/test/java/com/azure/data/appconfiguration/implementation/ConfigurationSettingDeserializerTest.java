// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.SECRET_REFERENCE_CONTENT_TYPE;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.parseFeatureFlagValue;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.parseSecretReferenceFieldValue;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.toConfigurationSetting;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ConfigurationSettingDeserializationHelper}
 */
public class ConfigurationSettingDeserializerTest {
    private static final String KEY = "hello";
    private static final String FEATURE_FLAG_DISPLAY_NAME = "Feature Flag X";
    private static final String SECRET_REFERENCE_URI_VALUE = "https://localhost";
    private static final String SETTING_VALUE = "world";
    private static final String FEATURE_FLAG_VALUE_JSON
        = "{\"id\":\"hello\",\"display_name\":\"Feature Flag X\",\"enabled\":false,"
            + "\"conditions\":{\"client_filters\":[{\"name\":\"Microsoft.Percentage\",\"parameters\":"
            + "{\"Value\":30}}]}}";

    private static final String SECRET_REFERENCE_JSON
        = "{\"key\":\"hello\",\"value\":\"{\\\"uri\\\":\\\"https://localhost\\\"}\","
            + "\"content_type\":\"application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8\","
            + "\"etag\":null,\"locked\":false,\"tags\":{}}";
    private static final String SECRET_REFERENCE_VALUE_JSON = "{\"uri\":\"https://localhost\"}";

    private static final String CONFIGURATION_SETTING_JSON
        = "{\"key\":\"hello\",\"value\":\"world\",\"etag\":null,\"locked\":false,\"tags\":{}}";

    @Test
    public void parseFeatureFlagValueTest() {
        assertFeatureFlagConfigurationSetting(getFeatureFlagConfigurationSetting(),
            parseFeatureFlagValue(FEATURE_FLAG_VALUE_JSON));
    }

    @Test
    public void parseSecretReferenceFieldValueTest() {
        assertSecretReferenceConfigurationSetting(getSecretReferenceConfigurationSetting(),
            parseSecretReferenceFieldValue(KEY, SECRET_REFERENCE_VALUE_JSON));
    }

    @ParameterizedTest
    @MethodSource("deserializeSupplier")
    public <T extends ConfigurationSetting> void deserialize(String json, T expectedGeo) {
        if (expectedGeo instanceof FeatureFlagConfigurationSetting) {
            final KeyValue mockFeatureFlagSetting = new KeyValue().setKey(".appconfig.featureflag/hello")
                .setValue(FEATURE_FLAG_VALUE_JSON)
                .setContentType(FEATURE_FLAG_CONTENT_TYPE);
            assertFeatureFlagConfigurationSetting((FeatureFlagConfigurationSetting) expectedGeo,
                (FeatureFlagConfigurationSetting) toConfigurationSetting(mockFeatureFlagSetting));
        } else if (expectedGeo instanceof SecretReferenceConfigurationSetting) {
            final KeyValue mockSecretReferenceSetting = new KeyValue().setKey(KEY)
                .setValue(SECRET_REFERENCE_VALUE_JSON)
                .setContentType(SECRET_REFERENCE_CONTENT_TYPE);
            assertSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) expectedGeo,
                (SecretReferenceConfigurationSetting) toConfigurationSetting(mockSecretReferenceSetting));
        } else {
            final KeyValue mockSetting = new KeyValue().setKey(KEY).setValue(SETTING_VALUE);
            assertConfigurationSetting(expectedGeo, toConfigurationSetting(mockSetting));
        }
    }

    public static Stream<Arguments> deserializeSupplier() {
        return Stream.of(Arguments.of(FEATURE_FLAG_VALUE_JSON, getFeatureFlagConfigurationSetting()),
            Arguments.of(SECRET_REFERENCE_JSON, getSecretReferenceConfigurationSetting()),
            Arguments.of(CONFIGURATION_SETTING_JSON, getConfigurationSetting()));
    }

    private static FeatureFlagConfigurationSetting getFeatureFlagConfigurationSetting() {
        List<FeatureFlagFilter> filters = Collections.singletonList(
            new FeatureFlagFilter("Microsoft.Percentage").setParameters(Collections.singletonMap("Value", 30)));

        return new FeatureFlagConfigurationSetting(KEY, false).setDisplayName(FEATURE_FLAG_DISPLAY_NAME)
            .setClientFilters(filters)
            .setValue(FEATURE_FLAG_VALUE_JSON);
    }

    private static ConfigurationSetting getConfigurationSetting() {
        return new ConfigurationSetting().setKey(KEY).setValue(SETTING_VALUE);
    }

    private static SecretReferenceConfigurationSetting getSecretReferenceConfigurationSetting() {
        return new SecretReferenceConfigurationSetting(KEY, SECRET_REFERENCE_URI_VALUE)
            .setValue(SECRET_REFERENCE_VALUE_JSON);
    }

    private static void assertFeatureFlagConfigurationSetting(FeatureFlagConfigurationSetting expect,
        FeatureFlagConfigurationSetting actual) {
        assertConfigurationSetting(expect, actual);
        assertEquals(expect.getFeatureId(), actual.getFeatureId());
        assertEquals(expect.getDescription(), actual.getDescription());
        assertEquals(expect.getDisplayName(), actual.getDisplayName());
        assertClientFilters(expect.getClientFilters(), actual.getClientFilters());
    }

    private static void assertClientFilters(List<FeatureFlagFilter> expect, List<FeatureFlagFilter> actual) {
        if (expect == null || actual == null) {
            assertEquals(expect, actual);
            return;
        }

        assertEquals(expect.size(), actual.size());
        for (int i = 0; i < expect.size(); i++) {
            FeatureFlagFilter expectFilter = expect.get(i);
            FeatureFlagFilter actualFilter = actual.get(i);
            assertEquals(expectFilter.getName(), actualFilter.getName());
            assertEquals(expectFilter.getParameters(), actualFilter.getParameters());
        }
    }

    private static void assertSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting expect,
        SecretReferenceConfigurationSetting actual) {
        assertConfigurationSetting(expect, actual);
        assertEquals(expect.getSecretId(), actual.getSecretId());
    }

    private static void assertConfigurationSetting(ConfigurationSetting expect, ConfigurationSetting actual) {
        assertEquals(expect.getKey(), actual.getKey());
        assertEquals(expect.getValue(), actual.getValue());
        assertEquals(expect.getLabel(), actual.getLabel());
        assertEquals(expect.getETag(), actual.getETag());
        assertEquals(expect.getContentType(), actual.getContentType());
        assertEquals(expect.getLastModified(), actual.getLastModified());
        assertEquals(expect.getTags(), actual.getTags());
    }
}
