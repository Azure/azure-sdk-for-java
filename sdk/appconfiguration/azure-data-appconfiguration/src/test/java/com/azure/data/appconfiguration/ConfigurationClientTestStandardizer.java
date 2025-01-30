// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.http.HttpClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingsFilter;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ConfigurationClientTestStandardizer extends ConfigurationClientTestBase {
    @Test
    public abstract void addConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void addConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void addConfigurationSettingRunner(Consumer<ConfigurationSetting> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting newConfiguration
            = new ConfigurationSetting().setKey(getKey()).setValue("myNewValue").setTags(tags).setContentType("text");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.setLabel(getLabel()));
    }

    ConfigurationSetting addConfigurationSettingWithTagsRunner(Consumer<ConfigurationSetting> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting newConfiguration
            = new ConfigurationSetting().setKey(getKey()).setValue("myNewValue").setContentType("text");

        testRunner.accept(newConfiguration);
        testRunner.accept(newConfiguration.setLabel(getLabel()).setTags(tags));
        return newConfiguration;
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
        final ConfigurationSetting newConfiguration
            = new ConfigurationSetting().setKey(getKey()).setValue("myNewValue");

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
        final ConfigurationSetting updateConfiguration
            = new ConfigurationSetting().setKey(key).setValue("myUpdatedValue");

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
    public abstract void featureFlagConfigurationSettingUnknownAttributesArePreserved(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void featureFlagConfigurationSettingUnknownAttributesArePreservedRunner(
        Consumer<FeatureFlagConfigurationSetting> testRunner) {
        String key = getKey();
        FeatureFlagConfigurationSetting featureFlagX = getFeatureFlagConfigurationSetting(key, "Feature Flag X");
        String valueWithAdditionalFieldAtFirstLayer = String.format(
            "{\"id\":\"%s\",\"k1\":\"v1\",\"description\":\"%s\",\"display_name\":\"%s\",\"enabled\":%s,"
                + "\"conditions\":{\"requirement_type\":\"All\",\"client_filters\":"
                + "[{\"name\":\"Microsoft.Percentage\",\"parameters\":{\"Value\":30}}]"
                + "},\"additional_field\":\"additional_value\"}",
            featureFlagX.getFeatureId(), featureFlagX.getDescription(), featureFlagX.getDisplayName(),
            featureFlagX.isEnabled());
        featureFlagX.setValue(valueWithAdditionalFieldAtFirstLayer);
        testRunner.accept(featureFlagX);
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
    public abstract void secretReferenceConfigurationSettingUnknownAttributesArePreserved(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void secretReferenceConfigurationSettingUnknownAttributesArePreservedRunner(
        Consumer<SecretReferenceConfigurationSetting> testRunner) {
        String key = getKey();
        String valueWithAdditionalFields
            = "{\"uri\":\"uriValue\",\"objectFiledName\":{\"unknown\":\"unknown\",\"unknown2\":\"unknown2\"},"
            + "\"arrayFieldName\":[{\"name\":\"Microsoft.Percentage\",\"parameters\":{\"Value\":30}}]}";

        testRunner.accept(new SecretReferenceConfigurationSetting(key, valueWithAdditionalFields));
    }

    @Test
    public abstract void setConfigurationSettingIfETag(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void setConfigurationSettingIfETagRunner(BiConsumer<ConfigurationSetting, ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();

        final ConfigurationSetting newConfiguration = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting updateConfiguration
            = new ConfigurationSetting().setKey(key).setValue("myUpdateValue");

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

    @Test
    public abstract void setConfigurationSettingNullKey(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void getConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

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
        final ConfigurationSetting updateConfiguration
            = new ConfigurationSetting().setKey(newConfiguration.getKey()).setValue("myUpdateValue");

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
    public abstract void listConfigurationSettingsWithNullSelector(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    void listWithMultipleKeysRunner(String key, String key2,
        BiFunction<ConfigurationSetting, ConfigurationSetting, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key2).setValue("value");
        final Set<ConfigurationSetting> expectedSelection = new HashSet<>(Arrays.asList(setting, setting2));
        testRunner.apply(setting, setting2)
            .forEach(
                actual -> expectedSelection.removeIf(expected -> equals(expected, cleanResponse(expected, actual))));
        assertTrue(expectedSelection.isEmpty());
    }

    @Test
    public abstract void listWithMultipleLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    void listWithMultipleLabelsRunner(String key, String label, String label2,
        BiFunction<ConfigurationSetting, ConfigurationSetting, Iterable<ConfigurationSetting>> testRunner) {
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

    void listConfigurationSettingsSelectFieldsRunner(
        BiFunction<List<ConfigurationSetting>, SettingSelector, Iterable<ConfigurationSetting>> testRunner) {
        final String label = "my-first-mylabel";
        final String label2 = "my-second-mylabel";
        final int numberToCreate = 8;
        final Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        final SettingSelector selector = new SettingSelector().setLabelFilter("my-second*")
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

    void listConfigurationSettingsSelectFieldsWithNotSupportedFilterRunner(String keyFilter, String labelFilter,
        Consumer<SettingSelector> testRunner) {
        final Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        final SettingSelector selector = new SettingSelector().setKeyFilter(keyFilter)
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

    void listRevisionsWithMultipleKeysRunner(String key, String key2,
        Function<List<ConfigurationSetting>, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value");
        final ConfigurationSetting settingUpdate
            = new ConfigurationSetting().setKey(setting.getKey()).setValue("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key2).setValue("value");
        final ConfigurationSetting setting2Update
            = new ConfigurationSetting().setKey(setting2.getKey()).setValue("updatedValue");
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

    void listRevisionsWithMultipleLabelsRunner(String key, String label, String label2,
        Function<List<ConfigurationSetting>, Iterable<ConfigurationSetting>> testRunner) {
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label);
        final ConfigurationSetting settingUpdate
            = new ConfigurationSetting().setKey(setting.getKey()).setLabel(setting.getLabel()).setValue("updatedValue");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label2);
        final ConfigurationSetting setting2Update = new ConfigurationSetting().setKey(setting2.getKey())
            .setLabel(setting2.getLabel())
            .setValue("updatedValue");
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

    @Test
    public abstract void createSnapshot(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    void createSnapshotRunner(BiConsumer<String, List<ConfigurationSettingsFilter>> testRunner) {
        String snapshotName = getKey();
        List<ConfigurationSettingsFilter> filters = new ArrayList<>();
        filters.add(new ConfigurationSettingsFilter(snapshotName + "-*"));
        testRunner.accept(snapshotName, filters);
    }

    @Test
    public abstract void getSnapshot(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void getSnapshotConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void archiveSnapshot(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void archiveSnapshotConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void recoverSnapshot(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void recoverSnapshotConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listSnapshots(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listSnapshotsWithFields(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listSettingFromSnapshot(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listSettingFromSnapshotWithFields(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listSettingsWithPageETag(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    @Test
    public abstract void listLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    List<ConfigurationSetting> listLabelsRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label);
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key).setValue("value").setLabel(label2);
        testRunner.accept(setting);
        testRunner.accept(setting2);
        List<ConfigurationSetting> result = new ArrayList<>();
        result.add(setting);
        result.add(setting2);
        return result;
    }

    @Test
    public abstract void listSettingByTagsFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    List<ConfigurationSetting> listSettingByTagsFilterRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        String key2 = getKey();
        Map<String, String> tags = new HashMap<>();
        tags.put(key, "tagValue");
        Map<String, String> tags2 = new HashMap<>();
        tags2.put(key, "tagValue");
        tags2.put(key2, "tagValue");
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value").setTags(tags);
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key2).setValue("value").setTags(tags2);

        testRunner.accept(setting);
        testRunner.accept(setting2);

        List<ConfigurationSetting> result = new ArrayList<>();
        result.add(setting);
        result.add(setting2);
        return result;
    }

    @Test
    public abstract void listRevisionsWithTagsFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion);

    List<ConfigurationSetting> listRevisionsWithTagsFilterRunner(Consumer<ConfigurationSetting> testRunner) {
        final String keyName = getKey();
        final Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting original
            = new ConfigurationSetting().setKey(keyName).setValue("myValue").setTags(tags);
        final ConfigurationSetting updated
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        testRunner.accept(original);
        testRunner.accept(updated);
        testRunner.accept(updated2);

        List<ConfigurationSetting> result = new ArrayList<>();
        result.add(original);
        result.add(updated);
        result.add(updated2);
        return result;
    }

    @Test
    public abstract void createSnapshotWithTagsFilter(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion);

    List<ConfigurationSetting> createSnapshotWithTagsFilterPrepareRunner(Consumer<ConfigurationSetting> testRunner) {
        String key = getKey();
        String key2 = getKey();
        Map<String, String> tags = new HashMap<>();
        tags.put("MyTag", "TagValue");
        tags.put("AnotherTag", "AnotherTagValue");

        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("value");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(key2).setValue("value").setTags(tags);

        testRunner.accept(setting);
        testRunner.accept(setting2);

        List<ConfigurationSetting> result = new ArrayList<>();
        result.add(setting);
        result.add(setting2);
        return result;
    }

    void createSnapshotWithTagsFilterRunner(BiConsumer<String, List<ConfigurationSettingsFilter>> testRunner) {
        String snapshotName = getKey();
        List<String> tagsFilter = new ArrayList<>();
        tagsFilter.add("MyTag=TagValue");
        tagsFilter.add("AnotherTag=AnotherTagValue");

        List<ConfigurationSettingsFilter> filters = new ArrayList<>();
        filters.add(new ConfigurationSettingsFilter(KEY_PREFIX + "*").setTags(tagsFilter));
        testRunner.accept(snapshotName, filters);
    }
}
