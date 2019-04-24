// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class ConfigurationClientTestBase {
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
}
