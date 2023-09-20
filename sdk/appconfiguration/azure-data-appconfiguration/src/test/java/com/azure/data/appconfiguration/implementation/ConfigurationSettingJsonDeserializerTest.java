// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ConfigurationSettingJsonDeserializerTest}
 */
public class ConfigurationSettingJsonDeserializerTest {
    private static final String FEATURE_FLAG_JSON =
        "{\"key\":\".appconfig.featureflag/hello\",\"value\":\"{\\\"id\\\":\\\"hello\\\",\\\"description\\\":null,"
            + "\\\"display_name\\\":\\\"Feature Flag X\\\",\\\"enabled\\\":false,\\\"conditions\\\":{"
            + "\\\"client_filters\\\":[{\\\"name\\\":\\\"Microsoft.Percentage\\\",\\\"parameters\\\":{"
            + "\\\"Value\\\":\\\"30\\\"}}]}}\",\"content_type\":\"application/vnd.microsoft.appconfig.ff+json;"
            + "charset=utf-8\",\"etag\":null,\"locked\":false,\"tags\":{}}";
    private static final String FEATURE_FLAG_VALUE_JSON =
        "{\"id\":\"hello\",\"description\":null,\"display_name\":\"Feature Flag X\",\"enabled\":false,"
            + "\"conditions\":{\"client_filters\":[{\"name\":\"Microsoft.Percentage\",\"parameters\":"
            + "{\"Value\":\"30\"}}]}}";

    private static final String SECRET_REFERENCE_JSON =
        "{\"key\":\"hello\",\"value\":\"{\\\"uri\\\":\\\"https://localhost\\\"}\","
            + "\"content_type\":\"application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8\","
            + "\"etag\":null,\"locked\":false,\"tags\":{}}";;
    private static final String SECRET_REFERENCE_VALUE_JSON = "{\"uri\":\"https://localhost\"}";

    private static final String CONFIGURATION_SETTING_JSON =
        "{\"key\":\"hello\",\"value\":\"world\",\"etag\":null,\"locked\":false,\"tags\":{}}";

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper().registerModule(ConfigurationSettingJsonDeserializer.getModule());
    }

    @ParameterizedTest
    @MethodSource("deserializeSupplier")
    public <T extends ConfigurationSetting> void deserialize(String json, Class<T> type, T expectedGeo)
        throws IOException {
        if (expectedGeo instanceof FeatureFlagConfigurationSetting) {
            assertFeatureFlagConfigurationSetting(
                (FeatureFlagConfigurationSetting) expectedGeo,
                (FeatureFlagConfigurationSetting) MAPPER.readValue(json, type));
        } else if (expectedGeo instanceof SecretReferenceConfigurationSetting) {
            assertSecretReferenceConfigurationSetting(
                (SecretReferenceConfigurationSetting) expectedGeo,
                (SecretReferenceConfigurationSetting) MAPPER.readValue(json, type));
        } else {
            assertConfigurationSetting(expectedGeo, MAPPER.readValue(json, type));
        }
    }

    public static Stream<Arguments> deserializeSupplier() {
        final String key = "hello";
        return Stream.of(
            Arguments.of(deserializerFeatureFlagConfigurationSettingSupplier(
                getFeatureFlagConfigurationSetting(key, "Feature Flag X"))),
            Arguments.of(deserializerSecretReferenceConfigurationSettingSupplier(
                getSecretReferenceConfigurationSetting(key, "https://localhost"))),
            Arguments.of(deserializerConfigurationSettingSupplier(
                getConfigurationSetting(key, "world")))
        );
    }

    public static Object[] deserializerFeatureFlagConfigurationSettingSupplier(
        FeatureFlagConfigurationSetting featureSetting) {
        return new Object[]{FEATURE_FLAG_JSON, FeatureFlagConfigurationSetting.class, featureSetting};
    }

    public static Object[] deserializerSecretReferenceConfigurationSettingSupplier(
        SecretReferenceConfigurationSetting secretReferenceSetting) {
        return new Object[]{SECRET_REFERENCE_JSON, SecretReferenceConfigurationSetting.class, secretReferenceSetting};
    }

    public static Object[] deserializerConfigurationSettingSupplier(ConfigurationSetting setting) {
        return new Object[]{CONFIGURATION_SETTING_JSON, ConfigurationSetting.class, setting};
    }

    private static FeatureFlagConfigurationSetting getFeatureFlagConfigurationSetting(String key, String displayName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Value", "30");
        final List<FeatureFlagFilter> filters = new ArrayList<>();
        filters.add(new FeatureFlagFilter("Microsoft.Percentage")
                        .setParameters(parameters));

        return new FeatureFlagConfigurationSetting(key, false)
                   .setDisplayName(displayName)
                   .setClientFilters(filters)
                   .setValue(FEATURE_FLAG_VALUE_JSON);
    }

    private static ConfigurationSetting getConfigurationSetting(String key, String value) {
        return new ConfigurationSetting().setKey(key).setValue(value);
    }

    private static SecretReferenceConfigurationSetting getSecretReferenceConfigurationSetting(String key,
        String secretReference) {
        return new SecretReferenceConfigurationSetting(key, secretReference).setValue(SECRET_REFERENCE_VALUE_JSON);
    }

    private static void assertFeatureFlagConfigurationSetting(FeatureFlagConfigurationSetting expect,
        FeatureFlagConfigurationSetting actual) {
        assertConfigurationSetting(expect, actual);
        assertEquals(expect.getFeatureId(), actual.getFeatureId());
        assertEquals(expect.getDescription(), actual.getDescription());
        assertEquals(expect.getDisplayName(), actual.getDisplayName());
        assertClientFilters(expect.getClientFilters(), actual.getClientFilters());
    }

    private static void assertClientFilters(Iterable<FeatureFlagFilter> expect, Iterable<FeatureFlagFilter> actual) {
        if (expect == null || actual == null) {
            assertEquals(expect, actual);
        }

        final List<FeatureFlagFilter> expectList = StreamSupport.stream(expect.spliterator(), false)
                                                       .collect(Collectors.toList());
        final List<FeatureFlagFilter> actualList = StreamSupport.stream(actual.spliterator(), false)
                                                       .collect(Collectors.toList());
        assertEquals(expectList.size(), actualList.size());
        for (int i = 0; i < expectList.size(); i++) {
            final FeatureFlagFilter expectFilter = expectList.get(i);
            final FeatureFlagFilter actualFilter = actualList.get(i);
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
