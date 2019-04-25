// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.rest.Response;
import com.azure.common.test.TestBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
        keyPrefix = sdkContext.randomResourceName(KEY_PREFIX, PREFIX_LENGTH);
        labelPrefix = sdkContext.randomResourceName(LABEL_PREFIX, PREFIX_LENGTH);
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
        return sdkContext.randomResourceName(keyPrefix, RESOURCE_LENGTH);
    }

    String getLabel() {
        return sdkContext.randomResourceName(labelPrefix, RESOURCE_LENGTH);
    }

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

    @Test
    public abstract void addSettingEmptyValue();

    void addSettingEmptyValueRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        ConfigurationSetting setting = new ConfigurationSetting().key(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(key + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    @Test
    public abstract void addExistingSetting();

    void addExistingSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(getKey()).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label(getLabel()));
    }

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

    @Test
    public abstract void setSettingEmptyValue();

    void setSettingEmptyValueRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        ConfigurationSetting setting = new ConfigurationSetting().key(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(key + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    @Test
    public abstract void updateNoExistingSetting();

    void updateNoExistingSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting expectedFail = new ConfigurationSetting().key(getKey()).value("myFailingUpdate");

        testRunner.accept(expectedFail);
        testRunner.accept(expectedFail.label(getLabel()));
    }

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
        final ConfigurationSetting updated = new ConfigurationSetting(original)
            .value("myUpdatedValue")
            .tags(updatedTags)
            .contentType("text");

        testRunner.accept(original, updated);
        testRunner.accept(original.label(label), updated.label(label));
    }

    @Test
    public abstract void updateSettingOverload();

    void updateSettingOverloadRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();

        ConfigurationSetting original = new ConfigurationSetting().key(key).value("A Value");
        ConfigurationSetting updated = new ConfigurationSetting().key(key).value("A New Value");

        testRunner.accept(original, updated);
    }

    @Test
    public abstract void getSetting();

    void getSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label("myLabel"));
    }

    @Test
    public abstract void deleteSetting();

    void deleteSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting deletableConfiguration = new ConfigurationSetting().key(key).value("myValue");

        testRunner.accept(deletableConfiguration);
        testRunner.accept(deletableConfiguration.label(label));
    }

    @Test
    public abstract void deleteSettingWithETag();

    void deleteSettingWithETagRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting(newConfiguration).value("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.label(label), updateConfiguration.label(label));
    }

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    static void listWithMultipleKeys(String key, String key2, BiFunction<ConfigurationSetting, ConfigurationSetting, List<ConfigurationSetting>> testRunner) {
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
    static void listWithMultipleLabels(String key, String label, String label2, BiFunction<ConfigurationSetting, ConfigurationSetting, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value").label(label);
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key).value("value").label(label2);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(Arrays.asList(setting, setting2));

        for (ConfigurationSetting actual : testRunner.apply(setting, setting2)) {
            expectedSelection.removeIf(expected -> expected.equals(cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    static void listRevisionsWithMultipleKeys(String key, String key2, Function<List<ConfigurationSetting>, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value");
        final ConfigurationSetting settingUpdate = new ConfigurationSetting(setting).value("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key2).value("value");
        final ConfigurationSetting setting2Update = new ConfigurationSetting(setting2).value("updatedValue");
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
    static void listRevisionsWithMultipleLabels(String key, String label, String label2, Function<List<ConfigurationSetting>, List<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().key(key).value("value").label(label);
        final ConfigurationSetting settingUpdate = new ConfigurationSetting(setting).value("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().key(key).value("value").label(label2);
        final ConfigurationSetting setting2Update = new ConfigurationSetting(setting2).value("updatedValue");
        final List<ConfigurationSetting> testInput = Arrays.asList(setting, settingUpdate, setting2, setting2Update);
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(testInput);

        for (ConfigurationSetting actual : testRunner.apply(testInput)) {
            expectedSelection.removeIf(expected -> expected.equals(cleanResponse(expected, actual)));
        }

        assertTrue(expectedSelection.isEmpty());
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
        ConfigurationSetting cleanedActual = new ConfigurationSetting(actual)
            .lastModified(expected.lastModified())
            .isLocked(expected.isLocked())
            .etag(expected.etag());

        if (ConfigurationSetting.NO_LABEL.equals(expected.label()) && actual.label() == null) {
            cleanedActual.label(ConfigurationSetting.NO_LABEL);
        }

        return cleanedActual;
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
     * Helper method to verify the error was a RestException and it has a specific HTTP response code.
     *
     * @param ex Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    static void assertRestException(Throwable ex, int expectedStatusCode) {
        assertTrue(ex instanceof ServiceRequestException);
        assertEquals(expectedStatusCode, ((ServiceRequestException) ex).response().statusCode());
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
