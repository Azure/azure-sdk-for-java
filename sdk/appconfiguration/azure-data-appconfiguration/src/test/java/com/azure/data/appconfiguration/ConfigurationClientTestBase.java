// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public abstract class ConfigurationClientTestBase extends TestBase {
    private static final String AZURE_APPCONFIG_CONNECTION_STRING = "AZURE_APPCONFIG_CONNECTION_STRING";
    private static final String KEY_PREFIX = "key";
    private static final String LABEL_PREFIX = "label";
    private static final int PREFIX_LENGTH = 8;
    private static final int RESOURCE_LENGTH = 16;

    static String connectionString;

    private final ClientLogger logger = new ClientLogger(ConfigurationClientTestBase.class);

    String keyPrefix;
    String labelPrefix;

    void beforeTestSetup() {
        keyPrefix = testResourceNamer.randomName(KEY_PREFIX, PREFIX_LENGTH);
        labelPrefix = testResourceNamer.randomName(LABEL_PREFIX, PREFIX_LENGTH);
    }

    <T> T clientSetup(Function<ConfigurationClientCredentials, T> clientBuilder) {
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            connectionString = interceptorManager.isPlaybackMode()
                ? "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw"
                : Configuration.getGlobalConfiguration().get(AZURE_APPCONFIG_CONNECTION_STRING);
        }

        Objects.requireNonNull(connectionString, "AZURE_APPCONFIG_CONNECTION_STRING expected to be set.");

        T client;
        try {
            client = clientBuilder.apply(new ConfigurationClientCredentials(connectionString));
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Could not create an configuration client credentials.", e);
            fail();
            client = null;
        }

        return Objects.requireNonNull(client);
    }

    String getKey() {
        return testResourceNamer.randomName(keyPrefix, RESOURCE_LENGTH);
    }

    String getLabel() {
        return testResourceNamer.randomName(labelPrefix, RESOURCE_LENGTH);
    }

    @Test
    public abstract void addConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void addConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void addConfigurationSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting newConfiguration = new ConfigurationSetting()
            .setKey(getKey())
            .setValue("myNewValue")
            .setTags(tags)
            .setContentType("text");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.setLabel(getLabel()));
    }

    @Test
    public abstract void addFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void addFeatureFlagConfigurationSettingRunner(Consumer<FeatureFlagConfigurationSetting> testRunner) {
        testRunner.accept(getFeatureFlagConfigurationSetting(getKey(), "Feature Flag X"));
    }

    @Test
    public abstract void addSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void addSecretReferenceConfigurationSettingRunner(Consumer<SecretReferenceConfigurationSetting> testRunner) {
        testRunner.accept(new SecretReferenceConfigurationSetting(getKey(), "https://localhost"));
    }

    @Test
    public abstract void addConfigurationSettingEmptyKey(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void addConfigurationSettingEmptyValue(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void addConfigurationSettingEmptyValueRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        ConfigurationSetting setting = new ConfigurationSetting().setKey(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key + "-1").setValue("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    @Test
    public abstract void addConfigurationSettingNullKey(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void addExistingSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    void addExistingSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().setKey(getKey()).setValue("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.setLabel(getLabel()));
    }

    @Test
    public abstract void setConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void setConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void setConfigurationSettingRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting setConfiguration = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().setKey(key).setValue("myUpdatedValue");

        testRunner.accept(setConfiguration, updateConfiguration);
        testRunner.accept(setConfiguration.setLabel(label), updateConfiguration.setLabel(label));
    }

    @Test
    public abstract void setFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void setFeatureFlagConfigurationSettingRunner(
        BiConsumer<FeatureFlagConfigurationSetting, FeatureFlagConfigurationSetting> testRunner) {
        String key = getKey();
        testRunner.accept(getFeatureFlagConfigurationSetting(key, "Feature Flag X"),
            getFeatureFlagConfigurationSetting(key, "new Feature Flag X"));
    }

    @Test
    public abstract void setSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void setSecretReferenceConfigurationSettingRunner(
        BiConsumer<SecretReferenceConfigurationSetting, SecretReferenceConfigurationSetting> testRunner) {
        String key = getKey();
        testRunner.accept(new SecretReferenceConfigurationSetting(key, "https://localhost"),
            new SecretReferenceConfigurationSetting(key, "https://localhost/100"));
    }

    @Test
    public abstract void setConfigurationSettingIfETag(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void setConfigurationSettingIfETagRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().setKey(key).setValue("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.setLabel(label), updateConfiguration.setLabel(label));
    }

    @Test
    public abstract void setConfigurationSettingEmptyKey(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void setConfigurationSettingEmptyValue(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void setConfigurationSettingEmptyValueRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        ConfigurationSetting setting = new ConfigurationSetting().setKey(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key + "-1").setValue("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    @Test public abstract void setConfigurationSettingNullKey(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void getConfigurationSetting(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void getConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void getConfigurationSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().setKey(key).setValue("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.setLabel("myLabel"));
    }

    @Test
    public abstract void getFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void getFeatureFlagConfigurationSettingRunner(Consumer<FeatureFlagConfigurationSetting> testRunner) {
        testRunner.accept(getFeatureFlagConfigurationSetting(getKey(), "Feature Flag X"));
    }

    @Test
    public abstract void getSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void getSecretReferenceConfigurationSettingRunner(Consumer<SecretReferenceConfigurationSetting> testRunner) {
        testRunner.accept(new SecretReferenceConfigurationSetting(getKey(), "https://localhost"));
    }

    @Test
    public abstract void getConfigurationSettingNotFound(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void deleteConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void deleteConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void deleteConfigurationSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting deletableConfiguration = new ConfigurationSetting().setKey(key).setValue("myValue");

        testRunner.accept(deletableConfiguration);
        testRunner.accept(deletableConfiguration.setLabel(label));
    }

    @Test
    public abstract void deleteFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void deleteFeatureFlagConfigurationSettingRunner(Consumer<FeatureFlagConfigurationSetting> testRunner) {
        testRunner.accept(getFeatureFlagConfigurationSetting(getKey(), "Feature Flag X"));
    }

    @Test
    public abstract void deleteSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void deleteSecretReferenceConfigurationSettingRunner(Consumer<SecretReferenceConfigurationSetting> testRunner) {
        testRunner.accept(new SecretReferenceConfigurationSetting(getKey(), "https://localhost"));
    }

    @Test
    public abstract void deleteConfigurationSettingNotFound(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void deleteConfigurationSettingWithETag(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void deleteConfigurationSettingWithETagRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().setKey(newConfiguration.getKey()).setValue("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.setLabel(label), updateConfiguration.setLabel(label));
    }

    @Test
    public abstract void deleteConfigurationSettingNullKey(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void clearReadOnly(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void clearReadOnlyWithConfigurationSetting(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void clearReadOnlyWithConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void lockUnlockRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        final ConfigurationSetting lockConfiguration = new ConfigurationSetting().setKey(key).setValue("myValue");
        testRunner.accept(lockConfiguration);
    }

    @Test
    public abstract void clearReadOnlyWithFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void lockUnlockFeatureFlagRunner(Consumer<FeatureFlagConfigurationSetting> testRunner) {
        testRunner.accept(getFeatureFlagConfigurationSetting(getKey(), "Feature Flag X"));
    }

    @Test
    public abstract void clearReadOnlyWithSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void lockUnlockSecretReferenceRunner(Consumer<SecretReferenceConfigurationSetting> testRunner) {
        testRunner.accept(new SecretReferenceConfigurationSetting(getKey(), "https://localhost"));
    }

    @Test
    public abstract void listWithKeyAndLabel(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listWithMultipleKeys(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listConfigurationSettingsWithNullSelector(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    void listWithMultipleKeysRunner(String key, String key2, BiFunction<ConfigurationSetting, ConfigurationSetting, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key2).setValue("value");
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(Arrays.asList(setting, setting2));
        testRunner.apply(setting, setting2).forEach(actual -> expectedSelection.removeIf(expected -> equals(expected, cleanResponse(expected, actual))));
        assertTrue(expectedSelection.isEmpty());
    }

    @Test
    public abstract void listWithMultipleLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    void listWithMultipleLabelsRunner(String key, String label, String label2, BiFunction<ConfigurationSetting, ConfigurationSetting, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label);
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label2);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(Arrays.asList(setting, setting2));

        for (ConfigurationSetting actual : testRunner.apply(setting, setting2)) {
            expectedSelection.removeIf(expected -> equals(expected, cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    @Test
    public abstract void listConfigurationSettingsSelectFields(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void listConfigurationSettingsSelectFieldsRunner(BiFunction<List<ConfigurationSetting>, SettingSelector, Iterable<ConfigurationSetting>> testRunner) {
        final String label = "my-first-mylabel";
        final String label2 = "my-second-mylabel";
        final int numberToCreate = 8;
        final Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        final SettingSelector selector = new SettingSelector()
            .setLabelFilter("my-second*")
            .setKeyFilter(keyPrefix + "-fetch-*")
            .setFields(SettingFields.KEY, SettingFields.ETAG, SettingFields.CONTENT_TYPE, SettingFields.TAGS);

        List<ConfigurationSetting> settings = new ArrayList<>(numberToCreate);
        for (int value = 0; value < numberToCreate; value++) {
            String key = value % 2 == 0 ? keyPrefix + "-" + value : keyPrefix + "-fetch-" + value;
            String lbl = value / 4 == 0 ? label : label2;
            settings.add(new ConfigurationSetting().setKey(key).setValue("myValue2").setLabel(lbl).setTags(tags));
        }

        for (ConfigurationSetting setting : testRunner.apply(settings, selector)) {
            assertNotNull(setting.getETag());
            assertNotNull(setting.getKey());
            assertTrue(setting.getKey().contains(keyPrefix));
            assertNotNull(setting.getTags());
            assertEquals(tags.size(), setting.getTags().size());

            assertNull(setting.getLastModified());
            assertNull(setting.getContentType());
            assertNull(setting.getLabel());
        }
    }

    @Test
    public abstract void listConfigurationSettingsSelectFieldsWithPrefixStarKeyFilter(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listConfigurationSettingsSelectFieldsWithSubstringKeyFilter(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listConfigurationSettingsSelectFieldsWithPrefixStarLabelFilter(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listConfigurationSettingsSelectFieldsWithSubstringLabelFilter(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void listConfigurationSettingsSelectFieldsWithNotSupportedFilterRunner(String keyFilter, String labelFilter, Consumer<SettingSelector> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        final SettingSelector selector = new SettingSelector()
            .setKeyFilter(keyFilter)
            .setLabelFilter(labelFilter)
            .setFields(SettingFields.KEY, SettingFields.ETAG, SettingFields.CONTENT_TYPE, SettingFields.TAGS);
        testRunner.accept(selector);
    }

    @Test
    public abstract void listConfigurationSettingsAcceptDateTime(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listRevisions(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    static void validateListRevisions(ConfigurationSetting expected, ConfigurationSetting actual) {
        assertEquals(expected.getKey(), actual.getKey());
        assertNotNull(actual.getETag());
        assertNull(actual.getValue());
        assertNull(actual.getLastModified());
    }

    @Test
    public abstract void listRevisionsWithMultipleKeys(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void listRevisionsWithMultipleKeysRunner(String key, String key2, Function<List<ConfigurationSetting>, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value");
        final ConfigurationSetting settingUpdate = new ConfigurationSetting().setKey(setting.getKey()).setValue("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key2).setValue("value");
        final ConfigurationSetting setting2Update = new ConfigurationSetting().setKey(setting2.getKey()).setValue("updatedValue");
        final List<ConfigurationSetting> testInput = Arrays.asList(setting, settingUpdate, setting2, setting2Update);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(testInput);

        for (ConfigurationSetting actual : testRunner.apply(testInput)) {
            expectedSelection.removeIf(expected -> equals(expected, cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    @Test
    public abstract void listRevisionsWithMultipleLabels(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void listRevisionsWithMultipleLabelsRunner(String key, String label, String label2, Function<List<ConfigurationSetting>, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label);
        final ConfigurationSetting settingUpdate = new ConfigurationSetting().setKey(setting.getKey()).setLabel(setting.getLabel()).setValue("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label2);
        final ConfigurationSetting setting2Update = new ConfigurationSetting().setKey(setting2.getKey()).setLabel(setting2.getLabel()).setValue("updatedValue");
        final List<ConfigurationSetting> testInput = Arrays.asList(setting, settingUpdate, setting2, setting2Update);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(testInput);

        for (ConfigurationSetting actual : testRunner.apply(testInput)) {
            expectedSelection.removeIf(expected -> equals(expected, cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    @Test
    public abstract void listRevisionsAcceptDateTime(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listRevisionsWithPagination(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listConfigurationSettingsWithPagination(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listRevisionsWithPaginationAndRepeatStream(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listRevisionsWithPaginationAndRepeatIterator(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void getConfigurationSettingWhenValueNotUpdated(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Disabled("This test exists to clean up resources missed due to 429s.")
    @Test
    public abstract void deleteAllSettings(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void addHeadersFromContextPolicyTest(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void addHeadersFromContextPolicyRunner(Consumer<ConfigurationSetting> testRunner) {
        final String key = getKey();
        final String value = "newValue";

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().setKey(key).setValue(value);
        testRunner.accept(newConfiguration);
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
    static void assertConfigurationEquals(ConfigurationSetting expected, Response<ConfigurationSetting> response, final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());

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
            assertFalse(true, "One of input settings is null");
        }

        equals(expected, actual);
    }

    /**
     * The ConfigurationSetting has some fields that are only manipulated by the service,
     * this helper method cleans those fields on the setting returned by the service so tests are able to pass.
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param actual ConfigurationSetting returned by the service.
     */
    private static ConfigurationSetting cleanResponse(ConfigurationSetting expected, ConfigurationSetting actual) {
        ConfigurationSetting cleanedActual = new ConfigurationSetting()
            .setKey(actual.getKey())
            .setLabel(actual.getLabel())
            .setValue(actual.getValue())
            .setTags(actual.getTags())
            .setContentType(actual.getContentType())
            .setETag(expected.getETag());

        try {
            Field lastModified = ConfigurationSetting.class.getDeclaredField("lastModified");
            lastModified.setAccessible(true);
            lastModified.set(actual, expected.getLastModified());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // Shouldn't happen.
        }

        if (ConfigurationSetting.NO_LABEL.equals(expected.getLabel()) && actual.getLabel() == null) {
            cleanedActual.setLabel(ConfigurationSetting.NO_LABEL);
        }

        return cleanedActual;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a HttpResponseException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, HttpResponseException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
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
     * @return boolean value that defines if two ConfigurationSetting lists are equal
     */
    static boolean equalsArray(List<ConfigurationSetting> settings1, List<ConfigurationSetting> settings2) {
        if (settings1 == settings2) {
            return true;
        }

        if (settings1 == null || settings2 == null) {
            return false;
        }

        if (settings1.size() != settings2.size()) {
            return false;
        }

        final int size = settings1.size();
        for (int i = 0; i < size; i++) {
            if (!equals(settings1.get(i), settings2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method that sets up HttpHeaders
     *
     * @return the http headers
     */
    static HttpHeaders getCustomizedHeaders() {
        final String headerOne = "my-header1";
        final String headerTwo = "my-header2";
        final String headerThree = "my-header3";

        final String headerOneValue = "my-header1-value";
        final String headerTwoValue = "my-header2-value";
        final String headerThreeValue = "my-header3-value";

        final HttpHeaders headers = new HttpHeaders();
        headers.put(headerOne, headerOneValue);
        headers.put(headerTwo, headerTwoValue);
        headers.put(headerThree, headerThreeValue);

        return headers;
    }

    /**
     * Helper method that check if the {@code headerContainer} contains {@code headers}.
     *
     * @param headers the headers that been checked
     * @param headerContainer The headers container that check if the {@code headers} exist in it.
     */
    static void assertContainsHeaders(HttpHeaders headers, HttpHeaders headerContainer) {
        headers.stream().forEach(httpHeader ->
            assertEquals(headerContainer.getValue(httpHeader.getName()), httpHeader.getValue()));
    }

    private String getFeatureFlagConfigurationSettingValue(String key) {
        return "{\"id\":\"" + key + "\",\"description\":null,\"display_name\":\"Feature Flag X\""
                   + ",\"enabled\":false,\"conditions\":{\"client_filters\":[{\"name\":"
                   + "\"Microsoft.Percentage\",\"parameters\":{\"Value\":\"30\"}}]}}";
    }

    private FeatureFlagConfigurationSetting getFeatureFlagConfigurationSetting(String key, String displayName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Value", "30");
        final List<FeatureFlagFilter> filters = new ArrayList<>();
        filters.add(new FeatureFlagFilter("Microsoft.Percentage")
                        .setParameters(parameters));

        return new FeatureFlagConfigurationSetting(key, false)
                .setDisplayName(displayName)
                .setClientFilters(filters)
                .setValue(getFeatureFlagConfigurationSettingValue(key));
    }
}
