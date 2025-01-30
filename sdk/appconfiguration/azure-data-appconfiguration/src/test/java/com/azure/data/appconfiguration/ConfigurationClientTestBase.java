// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.client.traits.HttpTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingHelper;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingsFilter;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SnapshotComposition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class ConfigurationClientTestBase extends TestProxyTestBase {
    static final String KEY_PREFIX = "key";
    private static final String LABEL_PREFIX = "label";
    private static final int PREFIX_LENGTH = 8;
    private static final int RESOURCE_LENGTH = 16;
    // Disable `("$.key")` and name sanitizer from the list of common sanitizers
    public static final String[] REMOVE_SANITIZER_ID = { "AZSDK3493", "AZSDK3447" };

    public static final String FAKE_CONNECTION_STRING
        = "Endpoint=https://localhost:8080;Id=0000000000000;Secret=fakeSecrePlaceholder";

    static final Duration MINIMUM_RETENTION_PERIOD = Duration.ofHours(1);

    static TokenCredential tokenCredential;

    String keyPrefix;
    String labelPrefix;

    void beforeTestSetup() {
        keyPrefix = testResourceNamer.randomName(KEY_PREFIX, PREFIX_LENGTH);
        labelPrefix = testResourceNamer.randomName(LABEL_PREFIX, PREFIX_LENGTH + 2);
    }

    protected <T extends HttpTrait<ConfigurationClientBuilder> & TokenCredentialTrait<ConfigurationClientBuilder>> T setupBuilder(
        T builder, HttpClient httpClient, ConfigurationServiceVersion serviceVersion, boolean sync) {
        if (tokenCredential == null) {
            tokenCredential = TestHelper.getTokenCredential(interceptorManager);
        }

        String endpoint = interceptorManager.isPlaybackMode()
            ? new ConfigurationClientCredentials(FAKE_CONNECTION_STRING).getBaseUri()
            : Configuration.getGlobalConfiguration().get("AZ_CONFIG_ENDPOINT");

        Objects.requireNonNull(tokenCredential, "Token Credential expected to be set.");
        Objects.requireNonNull(endpoint, "Az Config endpoint expected to be set.");

        builder.credential(tokenCredential)
            .endpoint(endpoint)
            .serviceVersion(serviceVersion);

        httpClient = interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient;
        if (sync) {
            builder.httpClient(new AssertingHttpClientBuilder(httpClient).assertSync().build());
        } else {
            builder.httpClient(new AssertingHttpClientBuilder(httpClient).assertAsync().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Collections.singletonList(
                new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("Sync-Token", "If-Match"))));
        }

        // Disable `$.key` snanitizer
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
        }

        return builder;
    }

    String getKey() {
        return testResourceNamer.randomName(keyPrefix, RESOURCE_LENGTH);
    }

    String getLabel() {
        return testResourceNamer.randomName(labelPrefix, RESOURCE_LENGTH);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected. This method assumes a response status of 200.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned by the service, the body should contain a ConfigurationSetting
     */
    static void assertConfigurationEquals(ConfigurationSetting expected, Response<ConfigurationSetting> response) {
        assertConfigurationEquals(expected, response, 200);
    }

    static void assertFeatureFlagConfigurationSettingEquals(FeatureFlagConfigurationSetting expected,
        FeatureFlagConfigurationSetting actual) {
        assertEquals(expected.getFeatureId(), actual.getFeatureId());
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getDescription(), actual.getDescription());

        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getLabel(), actual.getLabel());
    }

    static void assertSecretReferenceConfigurationSettingEquals(SecretReferenceConfigurationSetting expected,
        SecretReferenceConfigurationSetting actual) {
        assertEquals(expected.getSecretId(), actual.getSecretId());
        assertEquals(expected.getKey(), actual.getKey());
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getLabel(), actual.getLabel());
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
     * @param expectedStatusCode Expected HTTP status code returned by the service
     */
    static void assertConfigurationEquals(ConfigurationSetting expected, Response<ConfigurationSetting> response,
        final int expectedStatusCode) {
        assertNotNull(response);
        assertConfigurationEquals(expected, response.getValue());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param actual ConfigurationSetting contained in the RestResponse body
     */
    static void assertConfigurationEquals(ConfigurationSetting expected, ConfigurationSetting actual) {
        if (expected != null && actual != null) {
            actual = cleanResponse(expected, actual);
        } else if (expected == actual) {
            return;
        } else if (expected == null || actual == null) {
            fail("One of input settings is null");
        }

        equals(expected, actual);
    }

    /**
     * The ConfigurationSetting has some fields that are only manipulated by the service,
     * this helper method cleans those fields on the setting returned by the service so tests are able to pass.
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param actual ConfigurationSetting returned by the service.
     */
    static ConfigurationSetting cleanResponse(ConfigurationSetting expected, ConfigurationSetting actual) {
        ConfigurationSetting cleanedActual = new ConfigurationSetting().setKey(actual.getKey())
            .setLabel(actual.getLabel())
            .setValue(actual.getValue())
            .setTags(actual.getTags())
            .setContentType(actual.getContentType())
            .setETag(expected.getETag());

        ConfigurationSettingHelper.setLastModified(actual, expected.getLastModified());

        if (ConfigurationSetting.NO_LABEL.equals(expected.getLabel()) && actual.getLabel() == null) {
            cleanedActual.setLabel(ConfigurationSetting.NO_LABEL);
        }

        return cleanedActual;
    }

    static void assertRestException(Runnable exceptionThrower) {
        assertRestException(exceptionThrower, java.net.HttpURLConnection.HTTP_BAD_METHOD);
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a HttpResponseException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test
     */
    static void assertRestException(Throwable exception) {
        assertRestException(exception, java.net.HttpURLConnection.HTTP_BAD_METHOD);
    }

    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertEquals(HttpResponseException.class, exception.getClass());
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    /**
     * Helper method to verify that two configuration setting are equal. Users can defined their equal method.
     *
     * @param o1 ConfigurationSetting object 1
     * @param o2 ConfigurationSetting object 2
     * @return boolean value that defines if two ConfigurationSettings are equal
     */
    static boolean equals(ConfigurationSetting o1, ConfigurationSetting o2) {
        if (o1 == o2) {
            return true;
        }

        if (!Objects.equals(o1.getKey(), o2.getKey())
            || !Objects.equals(o1.getLabel(), o2.getLabel())
            || !Objects.equals(o1.getValue(), o2.getValue())
            || !Objects.equals(o1.getETag(), o2.getETag())
            || !Objects.equals(o1.getLastModified(), o2.getLastModified())
            || !Objects.equals(o1.isReadOnly(), o2.isReadOnly())
            || !Objects.equals(o1.getContentType(), o2.getContentType())
            || CoreUtils.isNullOrEmpty(o1.getTags()) != CoreUtils.isNullOrEmpty(o2.getTags())) {
            return false;
        }

        if (!CoreUtils.isNullOrEmpty(o1.getTags())) {
            return Objects.equals(o1.getTags(), o2.getTags());
        }

        return true;
    }

    /**
     * A helper method to verify that two lists of ConfigurationSetting are equal each other.
     *
     * @param settings1 List of ConfigurationSetting
     * @param settings2 Another List of ConfigurationSetting
     */
    static void equalsArray(List<ConfigurationSetting> settings1, List<ConfigurationSetting> settings2) {
        if (settings1 == settings2) {
            return;
        }

        // If one of the not null assertions fail, the rest would've failed.
        assertNotNull(settings1);
        assertNotNull(settings2);

        assertEquals(settings1.size(), settings2.size());

        final int size = settings1.size();
        for (int i = 0; i < size; i++) {
            assertSettingsEqual(settings1.get(i), settings2.get(i));
        }
    }

    static void assertSettingsEqual(ConfigurationSetting o1, ConfigurationSetting o2) {
        if (o1 == o2) {
            return;
        }

        assertEquals(o1.getKey(), o2.getKey());
        assertEquals(o1.getLabel(), o2.getLabel());
        assertEquals(o1.getValue(), o2.getValue());
        assertEquals(o1.isReadOnly(), o2.isReadOnly());
        assertEquals(o1.getContentType(), o2.getContentType());

        if (o1.getETag() != null) {
            assertEquals(o1.getETag(), o2.getETag());
        }

        if (o1.getLastModified() != null) {
            assertEquals(o1.getLastModified(), o2.getLastModified());
        }

        if (!CoreUtils.isNullOrEmpty(o1.getTags())) {
            assertEquals(o1.getTags(), o2.getTags());
        }
    }

    private static final HttpHeaderName MY_HEADER1 = HttpHeaderName.fromString("my-header1");
    private static final HttpHeaderName MY_HEADER2 = HttpHeaderName.fromString("my-header2");
    private static final HttpHeaderName MY_HEADER3 = HttpHeaderName.fromString("my-header3");

    /**
     * Helper method that sets up HttpHeaders
     *
     * @return the http headers
     */
    static HttpHeaders getCustomizedHeaders() {
        final String headerOneValue = "my-header1-value";
        final String headerTwoValue = "my-header2-value";
        final String headerThreeValue = "my-header3-value";

        final HttpHeaders headers = new HttpHeaders();
        headers.set(MY_HEADER1, headerOneValue);
        headers.set(MY_HEADER2, headerTwoValue);
        headers.set(MY_HEADER3, headerThreeValue);

        return headers;
    }

    /**
     * Helper method that check if the {@code headerContainer} contains {@code headers}.
     *
     * @param headers the headers that been checked
     * @param headerContainer The headers container that check if the {@code headers} exist in it.
     */
    static void assertContainsHeaders(HttpHeaders headers, HttpHeaders headerContainer) {
        headers.stream()
            .forEach(httpHeader -> assertEquals(headerContainer.getValue(httpHeader.getName()), httpHeader.getValue()));
    }

    private String getFeatureFlagConfigurationSettingValue(String key) {
        return "{\"id\":\"" + key + "\",\"description\":null,\"display_name\":\"Feature Flag X\""
            + ",\"enabled\":false,\"conditions\":{\"client_filters\":[{\"name\":"
            + "\"Microsoft.Percentage\",\"parameters\":{\"Value\":30}}]}}";
    }

    FeatureFlagConfigurationSetting getFeatureFlagConfigurationSetting(String key, String displayName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Value", 30);
        final List<FeatureFlagFilter> filters = new ArrayList<>();
        filters.add(new FeatureFlagFilter("Microsoft.Percentage").setParameters(parameters));

        return new FeatureFlagConfigurationSetting(key, false).setDisplayName(displayName)
            .setClientFilters(filters)
            .setValue(getFeatureFlagConfigurationSettingValue(key));
    }

    void assertConfigurationSnapshotWithResponse(String name, ConfigurationSnapshotStatus snapshotStatus,
        List<ConfigurationSettingsFilter> filters, Response<ConfigurationSnapshot> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());

        assertEqualsConfigurationSnapshot(name, snapshotStatus, filters, SnapshotComposition.KEY,
            ConfigurationClientTestBase.MINIMUM_RETENTION_PERIOD, 0L, null, response.getValue());
    }

    void assertEqualsConfigurationSnapshot(String name, ConfigurationSnapshotStatus snapshotStatus,
        List<ConfigurationSettingsFilter> filters, SnapshotComposition snapshotComposition, Duration retentionPeriod,
        Long itemCount, Map<String, String> tags, ConfigurationSnapshot actualSnapshot) {
        assertEquals(name, actualSnapshot.getName());
        assertEquals(snapshotStatus, actualSnapshot.getStatus());
        assertEqualsSnapshotFilters(filters, actualSnapshot.getFilters());
        assertEquals(snapshotComposition, actualSnapshot.getSnapshotComposition());
        assertEquals(retentionPeriod, actualSnapshot.getRetentionPeriod());
        assertNotNull(actualSnapshot.getCreatedAt());
        assertEquals(itemCount, actualSnapshot.getItemCount());
        assertNotNull(actualSnapshot.getSizeInBytes());
        assertNotNull(actualSnapshot.getETag());

        if (!CoreUtils.isNullOrEmpty(tags)) {
            assertEquals(tags, actualSnapshot.getTags());
        }
    }

    void assertEqualsSnapshotFilters(List<ConfigurationSettingsFilter> o1, List<ConfigurationSettingsFilter> o2) {
        if (o1 == o2) {
            return;
        }
        assertEquals(o1.size(), o2.size());
        for (int i = 0; i < o1.size(); i++) {
            ConfigurationSettingsFilter expectedFilter = o1.get(i);
            ConfigurationSettingsFilter actualFilter = o2.get(i);
            assertEquals(expectedFilter.getKey(), actualFilter.getKey());
            assertEquals(expectedFilter.getLabel(), actualFilter.getLabel());
        }
    }
}
