// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.rest.Response;
import com.azure.common.implementation.util.ImplUtils;

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

class ConfigurationClientTestBase {
    static final int RESOURCE_LENGTH = 16;

    static void addSetting(String key, String label, Consumer<ConfigurationSetting> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting newConfiguration = new ConfigurationSetting()
            .key(key)
            .value("myNewValue")
            .tags(tags)
            .contentType("text");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label(label));
    }

    static void addSettingEmptyValue(String key, Consumer<ConfigurationSetting> testRunner) {
        ConfigurationSetting setting = new ConfigurationSetting().key(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(key + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    static void addExistingSetting(String key, String label, Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label(label));
    }

    static void setSetting(String key, String label, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        final ConfigurationSetting setConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdatedValue");

        testRunner.accept(setConfiguration, updateConfiguration);
        testRunner.accept(setConfiguration.label(label), updateConfiguration.label(label));
    }

    static void setSettingIfEtag(String key, String label, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.label(label), updateConfiguration.label(label));
    }

    static void setSettingEmptyValue(String key, Consumer<ConfigurationSetting> testRunner) {
        ConfigurationSetting setting = new ConfigurationSetting().key(key);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(key + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    static void updateNoExistingSetting(String key, String label, Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting expectedFail = new ConfigurationSetting().key(key).value("myFailingUpdate");

        testRunner.accept(expectedFail);
        testRunner.accept(expectedFail.label(label));
    }

    static void updateSetting(String key, String label, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
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

    static void updateSettingOverload(String key, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        ConfigurationSetting original = new ConfigurationSetting().key(key).value("A Value");
        ConfigurationSetting updated = new ConfigurationSetting().key(key).value("A New Value");

        testRunner.accept(original, updated);
    }

    static void getSetting(String key, Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label("myLabel"));
    }

    static void deleteSetting(String key, String label, Consumer<ConfigurationSetting> testRunner) {
        final ConfigurationSetting deletableConfiguration = new ConfigurationSetting().key(key).value("myValue");

        testRunner.accept(deletableConfiguration);
        testRunner.accept(deletableConfiguration.label(label));
    }

    static void deleteSettingWithETag(String key, String label, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
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
            expectedSelection.removeIf(expected -> configurationsEqual(expected, actual));
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
            expectedSelection.removeIf(expected -> configurationsEqual(expected, actual));
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
            expectedSelection.removeIf(expected -> configurationsEqual(expected, actual));
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
            expectedSelection.removeIf(expected -> configurationsEqual(expected, actual));
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
        assertTrue(ConfigurationClientTestBase.configurationsEqual(expected, actual));
    }

    /**
     * Helper method to verify that two ConfigurationSettings are datawise equal.
     * @param expected ConfigurationSetting expected to be returned by the service.
     * @param actual ConfigurationSetting returned by the service.
     * @return True if the ConfigurationSettings are the same setting or their member variables are equivalent.
     */
    private static boolean configurationsEqual(ConfigurationSetting expected, ConfigurationSetting actual) {
        if (expected == actual) {
            return true;
        }

        if (expected == null) {
            return false;
        }

        if (!Objects.equals(expected.key(), actual.key())
            || !Objects.equals(expected.value(), actual.value())
            || !Objects.equals(expected.contentType(), actual.contentType())
            || ImplUtils.isNullOrEmpty(expected.tags()) != ImplUtils.isNullOrEmpty(actual.tags())) {
            return false;
        }

        if (!ImplUtils.isNullOrEmpty(expected.tags())) {
            if (!Objects.equals(expected.tags().size(), actual.tags().size())
                || !Objects.equals(expected.tags(), actual.tags())) {
                return false;
            }
        }

        if (ConfigurationSetting.NO_LABEL.equals(expected.label()) && actual.label() != null) {
            return false;
        } else {
            return Objects.equals(expected.label(), actual.label());
        }
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
