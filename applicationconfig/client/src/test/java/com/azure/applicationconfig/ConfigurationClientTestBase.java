// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestBase;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class ConfigurationClientTestBase extends TestBase {
    private static final String KEY_PREFIX = "key";
    private static final String LABEL_PREFIX = "label";
    private static final int PREFIX_LENGTH = 8;
    private static final int RESOURCE_LENGTH = 16;

    private final Logger logger = LoggerFactory.getLogger(ConfigurationClientTestBase.class);

    String keyPrefix;
    String labelPrefix;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
        return testName.getMethodName();
    }

    void beforeTestSetup() {
        keyPrefix = testResourceNamer.randomName(KEY_PREFIX, PREFIX_LENGTH);
        labelPrefix = testResourceNamer.randomName(LABEL_PREFIX, PREFIX_LENGTH);
    }

    <T> T clientSetup(Function<ConfigurationClientCredentials, T> clientBuilder) {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw"
            : System.getenv("AZCONFIG_CONNECTION_STRING");

        Objects.requireNonNull(connectionString, "AZCONFIG_CONNECTION_STRING expected to be set.");

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

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    @Test
    public abstract void addSetting();

    void addSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting newConfiguration = new ConfigurationSetting()
            .key(getKey())
            .value("myNewValue")
            .tags(tags)
            .contentType("text");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label(getLabel()));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @Test
    public abstract void addSettingEmptyKey();

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @Test
    public abstract void addSettingEmptyValue();

    void addSettingEmptyValueRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        ConfigurationSetting setting = new ConfigurationSetting().key(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(key + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public abstract void addSettingNullKey();

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @Test
    public abstract void addExistingSetting();

    void addExistingSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(getKey()).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label(getLabel()));
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public abstract void setSetting();

    void setSettingRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting setConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdatedValue");

        testRunner.accept(setConfiguration, updateConfiguration);
        testRunner.accept(setConfiguration.label(label), updateConfiguration.label(label));
    }

    /**
     * Tests that when an etag is passed to set it will only set if the current representation of the setting has the
     * etag. If the set etag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    @Test
    public abstract void setSettingIfEtag();

    void setSettingIfEtagRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.label(label), updateConfiguration.label(label));
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    @Test
    public abstract void setSettingEmptyKey();

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @Test
    public abstract void setSettingEmptyValue();

    void setSettingEmptyValueRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        ConfigurationSetting setting = new ConfigurationSetting().key(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(key + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public abstract void setSettingNullKey();

    /**
     * Tests that update cannot be done to a non-existent configuration, this will result in a 412.
     * Unlike set update isn't able to create the configuration.
     */
    @Test
    public abstract void updateNoExistingSetting();

    void updateNoExistingSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting expectedFail = new ConfigurationSetting().key(getKey()).value("myFailingUpdate");

        testRunner.accept(expectedFail);
        testRunner.accept(expectedFail.label(getLabel()));
    }

    /**
     * Tests that a configuration is able to be updated when it exists.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public abstract void updateSetting();

    void updateSettingRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final Map<String, String> tags = new HashMap<>();
        tags.put("first tag", "first value");
        tags.put("second tag", "second value");
        final ConfigurationSetting original = new ConfigurationSetting()
            .key(key)
            .value("myNewValue")
            .tags(tags)
            .contentType("json");

        final Map<String, String> updatedTags = new HashMap<>(tags);
        final ConfigurationSetting updated = new ConfigurationSetting()
            .key(original.key())
            .value("myUpdatedValue")
            .tags(updatedTags)
            .contentType("text");

        testRunner.accept(original, updated);
        testRunner.accept(original.label(label), updated.label(label));
    }

    /**
     * Tests that a configuration is able to be updated when it exists with the convenience overload.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public abstract void updateSettingOverload();

    void updateSettingOverloadRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();

        ConfigurationSetting original = new ConfigurationSetting().key(key).value("A Value");
        ConfigurationSetting updated = new ConfigurationSetting().key(key).value("A New Value");

        testRunner.accept(original, updated);
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public abstract void updateSettingNullKey();

    /**
     * Tests that when an etag is passed to update it will only update if the current representation of the setting has the etag.
     * If the update etag doesn't match anything the update won't happen, this will result in a 412.
     */
    @Test
    public abstract void updateSettingIfEtag();

    void updateSettingIfEtagRunner(Consumer<List<ConfigurationSetting>> testRunner) {
        final String key = getKey();
        final String label = getLabel();
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdateValue");
        final ConfigurationSetting finalConfiguration = new ConfigurationSetting().key(key).value("myFinalValue");

        testRunner.accept(Arrays.asList(newConfiguration, updateConfiguration, finalConfiguration));
        testRunner.accept(Arrays.asList(newConfiguration.label(label), updateConfiguration.label(label), finalConfiguration.label(label)));
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is locked.
     */
    @Test
    public abstract void getSetting();

    void getSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label("myLabel"));
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @Test
    public abstract void getSettingNotFound();

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    @Test
    public abstract void deleteSetting();

    void deleteSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting deletableConfiguration = new ConfigurationSetting().key(key).value("myValue");

        testRunner.accept(deletableConfiguration);
        testRunner.accept(deletableConfiguration.label(label));
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @Test
    public abstract void deleteSettingNotFound();

    /**
     * Tests that when an etag is passed to delete it will only delete if the current representation of the setting has the etag.
     * If the delete etag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @Test
    public abstract void deleteSettingWithETag();

    void deleteSettingWithETagRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(newConfiguration.key()).value("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.label(label), updateConfiguration.label(label));
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    @Test
    public abstract void deleteSettingNullKey();

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */
    @Test
    public abstract void listWithKeyAndLabel();

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    @Test
    public abstract void listWithMultipleKeys();

    void listWithMultipleKeysRunner(String key, String key2, BiFunction<ConfigurationSetting, ConfigurationSetting, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value");
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key2).value("value");
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(Arrays.asList(setting, setting2));

        for (ConfigurationSetting actual : testRunner.apply(setting, setting2)) {
            expectedSelection.removeIf(expected -> expected.equals(cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    /**
     * Verifies that ConfigurationSettings can be added with different labels and that we can fetch those ConfigurationSettings
     * from the service when filtering by their labels.
     */
    @Test
    public abstract void listWithMultipleLabels();

    void listWithMultipleLabelsRunner(String key, String label, String label2, BiFunction<ConfigurationSetting, ConfigurationSetting, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value").label(label);
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key).value("value").label(label2);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(Arrays.asList(setting, setting2));

        for (ConfigurationSetting actual : testRunner.apply(setting, setting2)) {
            expectedSelection.removeIf(expected -> expected.equals(cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    @Test
    public abstract void listSettingsSelectFields();

    void listSettingsSelectFieldsRunner(BiFunction<List<ConfigurationSetting>, SettingSelector, List<ConfigurationSetting>> testRunner) {
        final String label = "my-first-mylabel";
        final String label2 = "my-second-mylabel";
        final int numberToCreate = 8;
        final Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        final SettingSelector selector = new SettingSelector()
            .labels("*-second*")
            .keys(keyPrefix + "-fetch-*")
            .fields(SettingFields.KEY, SettingFields.ETAG, SettingFields.CONTENT_TYPE, SettingFields.TAGS);

        List<ConfigurationSetting> settings = new ArrayList<>(numberToCreate);
        for (int value = 0; value < numberToCreate; value++) {
            String key = value % 2 == 0 ? keyPrefix + "-" + value : keyPrefix + "-fetch-" + value;
            String lbl = value / 4 == 0 ? label : label2;
            settings.add(new ConfigurationSetting().key(key).value("myValue2").label(lbl).tags(tags));
        }

        for (ConfigurationSetting setting : testRunner.apply(settings, selector)) {
            assertNotNull(setting.etag());
            assertNotNull(setting.key());
            assertTrue(setting.key().contains(keyPrefix));
            assertNotNull(setting.tags());
            assertEquals(tags.size(), setting.tags().size());

            assertNull(setting.lastModified());
            assertNull(setting.contentType());
            assertNull(setting.label());
        }
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @Test
    public abstract void listSettingsAcceptDateTime();

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @Test
    public abstract void listRevisions();

    static void validateListRevisions(ConfigurationSetting expected, ConfigurationSetting actual) {
        assertEquals(expected.key(), actual.key());
        assertNotNull(actual.etag());
        assertNull(actual.value());
        assertNull(actual.lastModified());
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @Test
    public abstract void listRevisionsWithMultipleKeys();

    void listRevisionsWithMultipleKeysRunner(String key, String key2, Function<List<ConfigurationSetting>, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value");
        final ConfigurationSetting settingUpdate = new ConfigurationSetting().key(setting.key()).value("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key2).value("value");
        final ConfigurationSetting setting2Update = new ConfigurationSetting().key(setting2.key()).value("updatedValue");
        final List<ConfigurationSetting> testInput = Arrays.asList(setting, settingUpdate, setting2, setting2Update);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(testInput);

        for (ConfigurationSetting actual : testRunner.apply(testInput)) {
            expectedSelection.removeIf(expected -> expected.equals(cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    /**
     * Verifies that we can get all revisions for all settings with the specified labels.
     */
    @Test
    public abstract void listRevisionsWithMultipleLabels();

    void listRevisionsWithMultipleLabelsRunner(String key, String label, String label2, Function<List<ConfigurationSetting>, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value").label(label);
        final ConfigurationSetting settingUpdate = new ConfigurationSetting().key(setting.key()).label(setting.label()).value("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key).value("value").label(label2);
        final ConfigurationSetting setting2Update = new ConfigurationSetting().key(setting2.key()).label(setting2.label()).value("updatedValue");
        final List<ConfigurationSetting> testInput = Arrays.asList(setting, settingUpdate, setting2, setting2Update);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(testInput);

        for (ConfigurationSetting actual : testRunner.apply(testInput)) {
            expectedSelection.removeIf(expected -> expected.equals(cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    /**
     * Verifies that the range header for revision selections returns the expected values.
     */
    @Test
    public abstract void listRevisionsWithRange();

    /**
     * Verifies that an exception will be thrown from the service if it cannot satisfy the range request.
     */
    @Test
    public abstract void listRevisionsInvalidRange();

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    @Test
    public abstract void listRevisionsAcceptDateTime();

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public abstract void listRevisionsWithPagination();

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.
     */
    @Test
    public abstract void listSettingsWithPagination();

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the etag provided does not match the one of the current setting.
     */
    @Ignore("Getting a configuration setting only when the value has changed is not a common scenario.")
    @Test
    public abstract void getSettingWhenValueNotUpdated();

    /**
     * Deletes all settings in the AppConfig store.
     */
    @Ignore("This test exists to clean up resources missed due to 429s.")
    @Test
    public abstract void deleteAllSettings();

    /**
     * Helper method to verify that the RestResponse matches what was expected. This method assumes a response status of 200.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned by the service, the body should contain a ConfigurationSetting
     */
    static void assertConfigurationEquals(ConfigurationSetting expected, Response<ConfigurationSetting> response) {
        assertConfigurationEquals(expected, response, 200);
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
        assertEquals(expectedStatusCode, response.statusCode());

        assertConfigurationEquals(expected, response.value());
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
        }

        assertEquals(expected, actual);
    }

    /**
     * The ConfigurationSetting has some fields that are only manipulated by the service,
     * this helper method cleans those fields on the setting returned by the service so tests are able to pass.
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param actual ConfigurationSetting returned by the service.
     */
    private static ConfigurationSetting cleanResponse(ConfigurationSetting expected, ConfigurationSetting actual) {
        ConfigurationSetting cleanedActual = new ConfigurationSetting()
            .key(actual.key())
            .label(actual.label())
            .value(actual.value())
            .tags(actual.tags())
            .contentType(actual.contentType())
            .etag(expected.etag());

        try {
            Field lastModified = ConfigurationSetting.class.getDeclaredField("lastModified");
            lastModified.setAccessible(true);
            lastModified.set(actual, expected.lastModified());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // Shouldn't happen.
        }

        if (ConfigurationSetting.NO_LABEL.equals(expected.label()) && actual.label() == null) {
            cleanedActual.label(ConfigurationSetting.NO_LABEL);
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
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).response().statusCode());
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
}
