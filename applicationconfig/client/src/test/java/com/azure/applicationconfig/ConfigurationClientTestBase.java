package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.microsoft.azure.core.TestMode;
import com.microsoft.azure.utils.SdkContext;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConfigurationClientTestBase {
    static TestMode getTestMode(Logger logger) throws IllegalArgumentException {
        final String azureTestMode = System.getenv("AZURE_TEST_MODE");

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum.", azureTestMode);
                throw e;
            }
        } else {
            logger.info("Environment variable 'AZURE_TEST_MODE' has not been set yet. Using 'Playback' mode.");
            return TestMode.PLAYBACK;
        }
    }

    static void addSetting(String keyPrefix, String labelPrefix, Consumer<ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
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

    static void addSettingEmptyValue(String keyPrefix, Consumer<ConfigurationSetting> testRunner) {
        ConfigurationSetting setting = new ConfigurationSetting().key(keyPrefix);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(keyPrefix + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    static void addExistingSetting(String keyPrefix, String labelPrefix, Consumer<ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label(label));
    }

    static void setSetting(String keyPrefix, String labelPrefix, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting setConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdatedValue");

        testRunner.accept(setConfiguration, updateConfiguration);
        testRunner.accept(setConfiguration.label(label), updateConfiguration.label(label));
    }

    static void setSettingIfEtag(String keyPrefix, String labelPrefix, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.label(label), updateConfiguration.label(label));
    }

    static void setSettingEmptyValue(String keyPrefix, Consumer<ConfigurationSetting> testRunner) {
        ConfigurationSetting setting = new ConfigurationSetting().key(keyPrefix);
        ConfigurationSetting setting2 = new ConfigurationSetting().key(keyPrefix + "-1").value("");

        testRunner.accept(setting);
        testRunner.accept(setting2);
    }

    static void updateNoExistingSetting(String keyPrefix, String labelPrefix, Consumer<ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting expectedFail = new ConfigurationSetting().key(key).value("myFailingUpdate");

        testRunner.accept(expectedFail);
        testRunner.accept(expectedFail.label(label));
    }

    static void updateSetting(String keyPrefix, String labelPrefix, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
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

    static void updateSettingOverload(String keyPrefix, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        ConfigurationSetting original = new ConfigurationSetting().key(keyPrefix).value("A Value");
        ConfigurationSetting updated = new ConfigurationSetting().key(keyPrefix).value("A New Value");

        testRunner.accept(original, updated);
    }

    static void getSetting(String keyPrefix, Consumer<ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.label("myLabel"));
    }

    static void deleteSetting(String keyPrefix, String labelPrefix, Consumer<ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting deletableConfiguration = new ConfigurationSetting().key(key).value("myValue");

        testRunner.accept(deletableConfiguration);
        testRunner.accept(deletableConfiguration.label(label));
    }

    static void deleteSettingWithETag(String keyPrefix, String labelPrefix, BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        final String key = SdkContext.randomResourceName(keyPrefix, 16);
        final String label = SdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting(newConfiguration).value("myUpdateValue");

        testRunner.accept(newConfiguration, updateConfiguration);
        testRunner.accept(newConfiguration.label(label), updateConfiguration.label(label));
    }
}
