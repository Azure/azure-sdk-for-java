// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotComposition;
import com.azure.data.appconfiguration.models.SnapshotFields;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.data.appconfiguration.implementation.Utility.getTagsFilterInString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ParameterizedClass
@MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
public class ConfigurationClientTest extends ConfigurationClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationClientTest.class);

    private final HttpClient httpClient;
    private final ConfigurationServiceVersion serviceVersion;
    private ConfigurationClient client;

    public ConfigurationClientTest(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        this.httpClient = httpClient;
        this.serviceVersion = serviceVersion;
    }

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        client = getConfigurationClient(httpClient, serviceVersion);
    }

    @Override
    protected void afterTest() {
        LOGGER.info("Cleaning up created key values.");
        client.listConfigurationSettings(new SettingSelector().setKeyFilter(keyPrefix + "*"))
            .forEach(configurationSetting -> {
                LOGGER.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(),
                    configurationSetting.getLabel(), configurationSetting.isReadOnly());
                if (configurationSetting.isReadOnly()) {
                    client.setReadOnlyWithResponse(configurationSetting, false, Context.NONE);
                }
                client.deleteConfigurationSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
            });

        LOGGER.info("Finished cleaning up values.");
    }

    private ConfigurationClient getConfigurationClient(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        return clientSetup((credentials, endpoint) -> {
            ConfigurationClientBuilder builder = new ConfigurationClientBuilder().credential(credentials)
                .endpoint(endpoint)
                .serviceVersion(serviceVersion);

            builder = setHttpClient(httpClient, builder);

            if (interceptorManager.isRecordMode()) {
                builder.addPolicy(interceptorManager.getRecordPolicy());
            } else if (interceptorManager.isPlaybackMode()) {
                interceptorManager.addMatchers(Collections.singletonList(
                    new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("Sync-Token", "If-Match"))));
            }

            // Disable `$.key` sanitizer
            if (!interceptorManager.isLiveMode()) {
                interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
            }
            return builder.buildClient();
        });
    }

    private ConfigurationClientBuilder setHttpClient(HttpClient httpClient, ConfigurationClientBuilder builder) {
        if (interceptorManager.isPlaybackMode()) {
            return builder.httpClient(buildSyncAssertingClient(interceptorManager.getPlaybackClient()));
        }
        return builder.httpClient(buildSyncAssertingClient(httpClient));
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient).assertSync().build();
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    @Test
    public void addConfigurationSetting() {
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));
    }

    @Test
    public void addConfigurationSettingConvenience() {
        addConfigurationSettingRunner(
            (expected) -> assertConfigurationEquals(expected, client.addConfigurationSetting(expected)));
    }

    @Test
    public void addFeatureFlagConfigurationSettingConvenience() {
        addFeatureFlagConfigurationSettingRunner((expected) -> assertFeatureFlagConfigurationSettingEquals(expected,
            (FeatureFlagConfigurationSetting) client.addConfigurationSetting(expected)));
    }

    @Test
    public void addSecretReferenceConfigurationSettingConvenience() {
        addSecretReferenceConfigurationSettingRunner((expected) -> assertSecretReferenceConfigurationSettingEquals(
            expected, (SecretReferenceConfigurationSetting) client.addConfigurationSetting(expected)));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @Test
    public void addConfigurationSettingEmptyKey() {
        assertRestException(() -> client.addConfigurationSetting("", null, "A value"),
            HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @Test
    public void addConfigurationSettingEmptyValue() {
        addConfigurationSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting,
                client.addConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getConfigurationSetting(setting.getKey(), setting.getLabel()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void addConfigurationSettingNullKey() {
        assertRunnableThrowsException(() -> client.addConfigurationSetting(null, null, "A Value"),
            IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addConfigurationSettingWithResponse(null, Context.NONE),
            NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @Test
    public void addExistingSetting() {
        addExistingSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertRestException(() -> client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue(),
                HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);
        });
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is read-only updates cannot happen, this will result in a 409.
     */
    @Test
    public void setConfigurationSetting() {
        setConfigurationSettingRunner((expected, update) -> assertConfigurationEquals(expected,
            client.setConfigurationSettingWithResponse(expected, false, Context.NONE).getValue()));
    }

    @Test
    public void setConfigurationSettingConvenience() {
        setConfigurationSettingRunner(
            (expected, update) -> assertConfigurationEquals(expected, client.setConfigurationSetting(expected)));
    }

    @Test
    public void setFeatureFlagConfigurationSettingConvenience() {
        setFeatureFlagConfigurationSettingRunner((expected, update) -> assertFeatureFlagConfigurationSettingEquals(
            expected, (FeatureFlagConfigurationSetting) client.setConfigurationSetting(expected)));
    }

    @Test
    public void featureFlagConfigurationSettingUnknownAttributesArePreserved() {
        featureFlagConfigurationSettingUnknownAttributesArePreservedRunner((expected) -> {
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.addConfigurationSetting(expected));
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.setConfigurationSetting(expected));
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.getConfigurationSetting(expected));
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @Test
    public void setSecretReferenceConfigurationSettingConvenience() {
        setSecretReferenceConfigurationSettingRunner(
            (expected, update) -> assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.setConfigurationSetting(expected)));
    }

    @Test
    public void secretReferenceConfigurationSettingUnknownAttributesArePreserved() {
        secretReferenceConfigurationSettingUnknownAttributesArePreservedRunner((expected) -> {
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.addConfigurationSetting(expected));
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.setConfigurationSetting(expected));
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.getConfigurationSetting(expected));
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Tests that when an ETag is passed to set it will only set if the current representation of the setting has the
     * ETag. If the set ETag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    @Test
    public void setConfigurationSettingIfETag() {
        setConfigurationSettingIfETagRunner((initial, update) -> {
            // This ETag is not the correct format. It is not the correct hash that the service is expecting.
            assertRestException(
                () -> client.setConfigurationSettingWithResponse(initial.setETag("badETag"), true, Context.NONE)
                    .getValue(),
                HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);

            final String etag = client.addConfigurationSettingWithResponse(initial, Context.NONE).getValue().getETag();

            assertConfigurationEquals(update,
                client.setConfigurationSettingWithResponse(update.setETag(etag), true, Context.NONE));
            assertRestException(
                () -> client.setConfigurationSettingWithResponse(initial, true, Context.NONE).getValue(),
                HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);
            assertConfigurationEquals(update, client.getConfigurationSetting(update.getKey(), update.getLabel()));
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    @Test
    public void setConfigurationSettingEmptyKey() {
        assertRestException(() -> client.setConfigurationSetting("", null, "A value"),
            HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @Test
    public void setConfigurationSettingEmptyValue() {
        setConfigurationSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting,
                client.setConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getConfigurationSetting(setting.getKey(), setting.getLabel()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void setConfigurationSettingNullKey() {
        assertRunnableThrowsException(() -> client.setConfigurationSetting(null, null, "A Value"),
            IllegalArgumentException.class);
        assertRunnableThrowsException(
            () -> client.setConfigurationSettingWithResponse(null, false, Context.NONE).getValue(),
            NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is read-only.
     */
    @Test
    public void getConfigurationSetting() {
        getConfigurationSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));
        });
    }

    @Test
    public void getConfigurationSettingConvenience() {
        getConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected));
        });
    }

    @Test
    public void getFeatureFlagConfigurationSettingConvenience() {
        getFeatureFlagConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.getConfigurationSetting(expected));
        });
    }

    @Test
    public void getSecretReferenceConfigurationSettingConvenience() {
        getSecretReferenceConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.getConfigurationSetting(expected));
        });
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @Test
    public void getConfigurationSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration
            = new ConfigurationSetting().setKey(key).setValue("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel
            = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        assertConfigurationEquals(neverRetrievedConfiguration,
            client.addConfigurationSettingWithResponse(neverRetrievedConfiguration, Context.NONE).getValue());

        assertRestException(() -> client.getConfigurationSetting("myNonExistentKey", null, null),
            HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        assertRestException(
            () -> client.getConfigurationSetting(nonExistentLabel.getKey(), nonExistentLabel.getLabel()),
            HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    @Test
    public void deleteConfigurationSetting() {
        deleteConfigurationSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));

            assertConfigurationEquals(expected,
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @Test
    public void deleteConfigurationSettingConvenience() {
        deleteConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));

            assertConfigurationEquals(expected, client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @Test
    public void deleteFeatureFlagConfigurationSettingConvenience() {
        deleteFeatureFlagConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.getConfigurationSetting(expected));

            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @Test
    public void deleteSecretReferenceConfigurationSettingConvenience() {
        deleteSecretReferenceConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.getConfigurationSetting(expected));

            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @Test
    public void deleteConfigurationSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguation
            = new ConfigurationSetting().setKey(key).setValue("myNeverDeletedValue");
        final ConfigurationSetting notFoundDelete
            = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        assertConfigurationEquals(neverDeletedConfiguation,
            client.addConfigurationSettingWithResponse(neverDeletedConfiguation, Context.NONE).getValue());

        assertConfigurationEquals(null, client.deleteConfigurationSetting("myNonExistentKey", null));
        assertConfigurationEquals(null,
            client.deleteConfigurationSettingWithResponse(notFoundDelete, false, Context.NONE),
            HttpURLConnection.HTTP_NO_CONTENT);

        assertConfigurationEquals(neverDeletedConfiguation,
            client.getConfigurationSetting(neverDeletedConfiguation.getKey(), neverDeletedConfiguation.getLabel()));
    }

    /**
     * Tests that when an ETag is passed to delete it will only delete if the current representation of the setting has the ETag.
     * If the delete ETag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @Test
    public void deleteConfigurationSettingWithETag() {
        deleteConfigurationSettingWithETagRunner((initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig
                = client.addConfigurationSettingWithResponse(initial, Context.NONE).getValue();
            final ConfigurationSetting updatedConfig
                = client.setConfigurationSettingWithResponse(update, false, Context.NONE).getValue();

            assertConfigurationEquals(update, client.getConfigurationSetting(initial.getKey(), initial.getLabel()));
            assertRestException(
                () -> client.deleteConfigurationSettingWithResponse(initiallyAddedConfig, true, Context.NONE)
                    .getValue(),
                HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);
            assertConfigurationEquals(update,
                client.deleteConfigurationSettingWithResponse(updatedConfig, true, Context.NONE).getValue());
            assertRestException(() -> client.getConfigurationSetting(initial.getKey(), initial.getLabel()),
                HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    @Test
    public void deleteConfigurationSettingNullKey() {
        assertRunnableThrowsException(() -> client.deleteConfigurationSetting(null, null),
            IllegalArgumentException.class);
        assertRunnableThrowsException(
            () -> client.deleteConfigurationSettingWithResponse(null, false, Context.NONE).getValue(),
            NullPointerException.class);
    }

    /**
     * Tests assert that the setting can be deleted after clear read-only of the setting.
     */
    @Test
    public void clearReadOnly() {
        lockUnlockRunner((expected) -> {
            // read-only setting
            client.addConfigurationSettingWithResponse(expected, Context.NONE);
            client.setReadOnlyWithResponse(expected, true, Context.NONE).getValue();

            // unsuccessfully delete
            assertRestException(() -> client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE),
                HttpResponseException.class, 409);

            // clear read-only setting and delete
            client.setReadOnly(expected.getKey(), expected.getLabel(), false);

            // successfully deleted
            assertConfigurationEquals(expected,
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
        });
    }

    /**
     * Tests assert that the setting can be deleted after unlock the setting.
     */
    @Test
    public void clearReadOnlyWithConfigurationSetting() {
        lockUnlockRunner((expected) -> {

            // lock setting
            client.addConfigurationSettingWithResponse(expected, Context.NONE);
            client.setReadOnlyWithResponse(expected, true, Context.NONE);

            // unsuccessfully deleted
            assertRestException(() -> client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE),
                HttpResponseException.class, 409);

            // unlock setting and delete
            client.setReadOnlyWithResponse(expected, false, Context.NONE);

            // successfully deleted
            assertConfigurationEquals(expected,
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
        });
    }

    @Test
    public void clearReadOnlyWithConfigurationSettingConvenience() {
        lockUnlockRunner((expected) -> {

            // lock setting
            client.addConfigurationSetting(expected);
            client.setReadOnly(expected, true);

            // unsuccessfully deleted
            assertRestException(() -> client.deleteConfigurationSetting(expected), HttpResponseException.class, 409);

            // unlock setting and delete
            client.setReadOnly(expected, false);

            // successfully deleted
            assertConfigurationEquals(expected, client.deleteConfigurationSetting(expected));
        });
    }

    @Test
    public void clearReadOnlyWithFeatureFlagConfigurationSettingConvenience() {
        lockUnlockFeatureFlagRunner((expected) -> {

            // lock setting
            client.addConfigurationSetting(expected);
            client.setReadOnly(expected, true);

            // unsuccessfully deleted
            assertRestException(() -> client.deleteConfigurationSetting(expected), HttpResponseException.class, 409);

            // unlock setting and delete
            client.setReadOnly(expected, false);

            // successfully deleted
            assertConfigurationEquals(expected, client.deleteConfigurationSetting(expected));
        });
    }

    @Test
    public void clearReadOnlyWithSecretReferenceConfigurationSettingConvenience() {
        lockUnlockSecretReferenceRunner((expected) -> {

            // lock setting
            client.addConfigurationSetting(expected);
            client.setReadOnly(expected, true);

            // unsuccessfully deleted
            assertRestException(() -> client.deleteConfigurationSetting(expected), HttpResponseException.class, 409);

            // unlock setting and delete
            client.setReadOnly(expected, false);

            // successfully deleted
            assertConfigurationEquals(expected, client.deleteConfigurationSetting(expected));
        });
    }

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */
    @Test
    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = getKey();
        final String label = getLabel();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue(value).setLabel(label);

        assertConfigurationEquals(expected,
            client.setConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
        assertConfigurationEquals(expected,
            client.listConfigurationSettings(new SettingSelector().setKeyFilter(key).setLabelFilter(label))
                .iterator()
                .next());
        assertConfigurationEquals(expected,
            client.listConfigurationSettings(new SettingSelector().setKeyFilter(key)).iterator().next());
    }

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    @Test
    public void listWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            assertConfigurationEquals(setting,
                client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2,
                client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            return client.listConfigurationSettings(new SettingSelector().setKeyFilter(key + "," + key2));
        });
    }

    @Test
    public void listConfigurationSettingsWithNullSelector() {

        String key = getKey();
        String key2 = getKey();
        // Delete all existing settings in the resource
        final PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(null);
        for (ConfigurationSetting setting : configurationSettings) {
            assertConfigurationEquals(setting,
                client.deleteConfigurationSettingWithResponse(setting, false, Context.NONE));
        }
        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            assertConfigurationEquals(setting,
                client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2,
                client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            final PagedIterable<ConfigurationSetting> configurationSettingIterable
                = client.listConfigurationSettings(null);
            assertEquals(2, configurationSettingIterable.stream().count());
            return configurationSettingIterable;
        });
    }

    /**
     * Verifies that ConfigurationSettings can be added with different labels and that we can fetch those ConfigurationSettings
     * from the service when filtering by their labels.
     */
    @Test
    public void listWithMultipleLabels() {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listWithMultipleLabelsRunner(key, label, label2, (setting, setting2) -> {
            assertConfigurationEquals(setting,
                client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2,
                client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            return client.listConfigurationSettings(
                new SettingSelector().setKeyFilter(key).setLabelFilter(label + "," + label2));
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    @Test
    public void listConfigurationSettingsSelectFields() {
        listConfigurationSettingsSelectFieldsRunner((settings, selector) -> {
            settings.forEach(
                setting -> client.setConfigurationSettingWithResponse(setting, false, Context.NONE).getValue());
            return client.listConfigurationSettings(selector);
        });
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a key filter.
     */
    @Test
    public void listConfigurationSettingsSelectFieldsWithPrefixStarKeyFilter() {
        filterValueTest("*" + getKey(), getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a* key filter.
     */
    @Test
    public void listConfigurationSettingsSelectFieldsWithSubstringKeyFilter() {
        filterValueTest("*" + getKey() + "*", getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a label filter.
     */
    @Test
    public void listConfigurationSettingsSelectFieldsWithPrefixStarLabelFilter() {
        filterValueTest(getKey(), "*" + getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a* label filter.
     */
    @Test
    public void listConfigurationSettingsSelectFieldsWithSubstringLabelFilter() {
        filterValueTest(getKey(), "*" + getLabel() + "*");
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @Test
    public void listConfigurationSettingsAcceptDateTime() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        assertConfigurationEquals(original,
            client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
        sleepIfRunningAgainstService(2000);
        assertConfigurationEquals(updated,
            client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
        sleepIfRunningAgainstService(2000);
        assertConfigurationEquals(updated2,
            client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions
            = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options
            = new SettingSelector().setKeyFilter(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        assertConfigurationEquals(updated,
            (client.listConfigurationSettings(options).stream().collect(Collectors.toList())).get(0));
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @Test
    public void listRevisions() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        assertConfigurationEquals(original,
            client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
        assertConfigurationEquals(updated,
            client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
        assertConfigurationEquals(updated2,
            client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());

        // Get all revisions for a key, they are listed in descending order.
        List<ConfigurationSetting> revisions
            = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated2, revisions.get(0));
        assertConfigurationEquals(updated, revisions.get(1));
        assertConfigurationEquals(original, revisions.get(2));

        // Verifies that we can select specific fields.
        revisions = client
            .listRevisions(new SettingSelector().setKeyFilter(keyName).setFields(SettingFields.KEY, SettingFields.ETAG))
            .stream()
            .collect(Collectors.toList());
        validateListRevisions(updated2, revisions.get(0));
        validateListRevisions(updated, revisions.get(1));
        validateListRevisions(original, revisions.get(2));

        // Verifies that we have revision list size greater than 0. The count number of revision changes.
        assertTrue(client.listRevisions(null).stream().count() > 0);
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @Test
    public void listRevisionsWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            assertConfigurationEquals(testInput.get(0),
                client.addConfigurationSettingWithResponse(testInput.get(0), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(1),
                client.setConfigurationSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2),
                client.addConfigurationSettingWithResponse(testInput.get(2), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(3),
                client.setConfigurationSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listRevisions(new SettingSelector().setKeyFilter(key + "," + key2));
        });
    }

    /**
     * Verifies that we can get all revisions for all settings with the specified labels.
     */
    @Test
    public void listRevisionsWithMultipleLabels() {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listRevisionsWithMultipleLabelsRunner(key, label, label2, (testInput) -> {
            assertConfigurationEquals(testInput.get(0),
                client.addConfigurationSettingWithResponse(testInput.get(0), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(1),
                client.setConfigurationSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2),
                client.addConfigurationSettingWithResponse(testInput.get(2), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(3),
                client.setConfigurationSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listRevisions(new SettingSelector().setKeyFilter(key).setLabelFilter(label + "," + label2));
        });
    }

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    @Test
    public void listRevisionsAcceptDateTime() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2
            = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        assertConfigurationEquals(original,
            client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
        sleepIfRunningAgainstService(2000);
        assertConfigurationEquals(updated,
            client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
        sleepIfRunningAgainstService(2000);
        assertConfigurationEquals(updated2,
            client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions
            = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options
            = new SettingSelector().setKeyFilter(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        revisions = client.listRevisions(options).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated, revisions.get(0));
        assertConfigurationEquals(original, revisions.get(1));
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listRevisionsWithPagination() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client
                .setConfigurationSettingWithResponse(
                    new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix),
                    false, Context.NONE)
                .getValue();
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);
        assertEquals(numberExpected, client.listRevisions(filter).stream().collect(Collectors.toList()).size());
    }

    /**
     * Verifies that, given a ton of revisions, we can process {@link java.util.stream.Stream} multiple time and get same result.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listRevisionsWithPaginationAndRepeatStream() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client
                .setConfigurationSettingWithResponse(
                    new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix),
                    false, Context.NONE)
                .getValue();
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);
        PagedIterable<ConfigurationSetting> configurationSettingPagedIterable = client.listRevisions(filter);
        assertEquals(numberExpected, configurationSettingPagedIterable.stream().collect(Collectors.toList()).size());

        assertEquals(numberExpected, configurationSettingPagedIterable.stream().collect(Collectors.toList()).size());
    }

    /**
     * Verifies that, given a ton of revisions, we can iterate over multiple time and get same result.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listRevisionsWithPaginationAndRepeatIterator() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client
                .setConfigurationSettingWithResponse(
                    new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix),
                    false, Context.NONE)
                .getValue();
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);

        PagedIterable<ConfigurationSetting> configurationSettingPagedIterable = client.listRevisions(filter);
        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        configurationSettingPagedIterable.iterator().forEachRemaining(configurationSettingList1::add);
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedIterable.iterator().forEachRemaining(configurationSettingList2::add);
        assertEquals(numberExpected, configurationSettingList2.size());

        equalsArray(configurationSettingList1, configurationSettingList2);
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Disabled("Error code 403 TOO_MANY_REQUESTS https://github.com/Azure/azure-sdk-for-java/issues/36602")
    @Test
    public void listConfigurationSettingsWithPagination() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setConfigurationSettingWithResponse(
                new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix),
                false, Context.NONE).getValue();
        }
        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix + "-*").setLabelFilter(labelPrefix);

        assertEquals(numberExpected, client.listConfigurationSettings(filter).stream().count());
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the ETag provided does not match the one of the current setting.
     */
    @Test
    public void getConfigurationSettingWhenValueNotUpdated() {
        final String key = getKey();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting block
            = client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();

        assertNotNull(block);
        assertConfigurationEquals(expected, block);
        // conditional get, now the setting has not be updated yet, resulting 304 and null value
        assertConfigurationEquals(null, client.getConfigurationSettingWithResponse(block, null, true, Context.NONE),
            304);
        assertConfigurationEquals(newExpected,
            client.setConfigurationSettingWithResponse(newExpected, false, Context.NONE).getValue());
        // conditional get, now the setting is updated and we are able to get a new setting with 200 code
        assertConfigurationEquals(newExpected,
            client.getConfigurationSettingWithResponse(newExpected, null, true, Context.NONE).getValue());
    }

    @Test
    public void deleteAllSettings() {

        client.listConfigurationSettings(new SettingSelector().setKeyFilter("*")).forEach(configurationSetting -> {
            LOGGER.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(),
                configurationSetting.getLabel(), configurationSetting.isReadOnly());
            client.deleteConfigurationSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });
    }

    @Test
    public void addHeadersFromContextPolicyTest() {
        final HttpHeaders headers = getCustomizedHeaders();
        addHeadersFromContextPolicyRunner(expected -> {
            final Response<ConfigurationSetting> response = client.addConfigurationSettingWithResponse(expected,
                new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
            assertContainsHeaders(headers, response.getRequest().getHeaders());
        });
    }

    @Test
    public void createSnapshot() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });
    }

    @Test
    public void getSnapshot() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Retrieve a snapshot after creation
            Response<ConfigurationSnapshot> getSnapshot = client.getSnapshotWithResponse(name,
                Arrays.asList(SnapshotFields.NAME, SnapshotFields.STATUS, SnapshotFields.FILTERS), Context.NONE);

            assertEquals(200, getSnapshot.getStatusCode());
            ConfigurationSnapshot actualSnapshot = getSnapshot.getValue();
            assertEquals(name, actualSnapshot.getName());
            assertEquals(ConfigurationSnapshotStatus.READY, actualSnapshot.getStatus());
            assertEqualsSnapshotFilters(filters, actualSnapshot.getFilters());
            assertNull(actualSnapshot.getSnapshotComposition());
            assertNull(actualSnapshot.getRetentionPeriod());
            assertNull(actualSnapshot.getCreatedAt());
            assertNull(actualSnapshot.getItemCount());
            assertNull(actualSnapshot.getSizeInBytes());
            assertNull(actualSnapshot.getETag());

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            ConfigurationSnapshot archivedSnapshot = client.archiveSnapshot(name);
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, archivedSnapshot.getStatus());
        });
    }

    @Test
    public void getSnapshotConvenience() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Retrieve a snapshot after creation
            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, client.getSnapshot(name));

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            ConfigurationSnapshot archivedSnapshot = client.archiveSnapshot(name);
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, archivedSnapshot.getStatus());
        });
    }

    @Test
    public void archiveSnapshot() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            // Retention period can be setup when creating a snapshot and cannot edit.
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            Response<ConfigurationSnapshot> response
                = client.archiveSnapshotWithResponse(snapshotResult.getName(), null, Context.NONE);
            assertConfigurationSnapshotWithResponse(200, name, ConfigurationSnapshotStatus.ARCHIVED, filters,
                SnapshotComposition.KEY, MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, response);
        });
    }

    @Test
    public void archiveSnapshotConvenience() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            // Retention period can be setup when creating a snapshot and cannot edit.
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            ConfigurationSnapshot archivedSnapshot = client.archiveSnapshot(name);
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, archivedSnapshot.getStatus());
        });
    }

    @Test
    public void recoverSnapshot() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot
            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.ARCHIVED, filters,
                SnapshotComposition.KEY, MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, client.archiveSnapshot(name));

            // Recover the snapshot, it will be deleted automatically when retention period expires.
            Response<ConfigurationSnapshot> configurationSnapshotResponse
                = client.recoverSnapshotWithResponse(snapshotResult.getName(), null, Context.NONE);
            assertConfigurationSnapshotWithResponse(200, name, ConfigurationSnapshotStatus.READY, filters,
                SnapshotComposition.KEY, MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, configurationSnapshotResponse);

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });
    }

    @Test
    public void recoverSnapshotConvenience() {
        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());

            // Recover the snapshot, it will be deleted automatically when retention period expires.
            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, client.recoverSnapshot(name));

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });
    }

    @Test
    public void listSnapshots() {

        List<ConfigurationSnapshot> allExistingSnapshots = new ArrayList<>();
        client.listSnapshots(new SnapshotSelector().setStatus(ConfigurationSnapshotStatus.READY))
            .stream()
            .map(snapshot -> allExistingSnapshots.add(snapshot));

        // Clean all ready snapshots
        for (ConfigurationSnapshot existSnapshot : allExistingSnapshots) {
            client.archiveSnapshot(existSnapshot.getName());
        }

        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        List<ConfigurationSnapshot> readySnapshots = new ArrayList<>();

        // Create first snapshot
        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            readySnapshots.add(snapshotResult);
        });

        // Create second snapshot
        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });

        // readySnapshots contains only 1 snapshot
        ConfigurationSnapshot readySnapshot = readySnapshots.get(0);
        // List only the snapshot with a specific name
        client.listSnapshots(new SnapshotSelector().setNameFilter(readySnapshot.getName())).forEach(response -> {
            assertEquals(readySnapshot.getName(), response.getName());
            assertEquals(readySnapshot.getStatus(), response.getStatus());
        });
        // Archived the snapshot, it will be deleted automatically when retention period expires.
        assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(readySnapshot.getName()).getStatus());
    }

    @Test
    public void listSnapshotsWithFields() {

        List<ConfigurationSnapshot> allExistingSnapshots = new ArrayList<>();
        client.listSnapshots(new SnapshotSelector().setStatus(ConfigurationSnapshotStatus.READY))
            .stream()
            .map(snapshot -> allExistingSnapshots.add(snapshot));

        // Clean all ready snapshots
        for (ConfigurationSnapshot existSnapshot : allExistingSnapshots) {
            client.archiveSnapshot(existSnapshot.getName());
        }

        // Prepare a setting before creating a snapshot
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));

        List<ConfigurationSnapshot> readySnapshots = new ArrayList<>();

        // Create first snapshot
        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            readySnapshots.add(snapshotResult);
        });

        // Create second snapshot
        createSnapshotRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 1000L, 0L, null, snapshotResult);

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });

        // readySnapshots contains only 1 snapshot
        ConfigurationSnapshot readySnapshot = readySnapshots.get(0);
        // List only the snapshot with a specific name
        PagedIterable<ConfigurationSnapshot> configurationSnapshots
            = client.listSnapshots(new SnapshotSelector().setNameFilter(readySnapshot.getName())
                .setFields(SnapshotFields.NAME, SnapshotFields.FILTERS, SnapshotFields.STATUS));

        for (ConfigurationSnapshot snapshotFieldFiltered : configurationSnapshots) {
            assertEquals(readySnapshot.getName(), snapshotFieldFiltered.getName());
            assertNotNull(snapshotFieldFiltered.getFilters());
            assertEquals(readySnapshot.getStatus(), snapshotFieldFiltered.getStatus());
            assertNull(snapshotFieldFiltered.getETag());
            assertNull(snapshotFieldFiltered.getSnapshotComposition());
            assertNull(snapshotFieldFiltered.getItemCount());
            assertNull(snapshotFieldFiltered.getRetentionPeriod());
            assertNull(snapshotFieldFiltered.getSizeInBytes());
            assertNull(snapshotFieldFiltered.getCreatedAt());
            assertNull(snapshotFieldFiltered.getExpiresAt());
            assertNull(snapshotFieldFiltered.getTags());

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED,
                client.archiveSnapshot(snapshotFieldFiltered.getName()).getStatus());
        }
    }

    @Test
    public void listSettingFromSnapshot() {

        // Create a snapshot
        createSnapshotRunner((name, filters) -> {
            // Prepare 5 settings before creating a snapshot
            final int numberExpected = 5;
            List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
            for (int value = 0; value < numberExpected; value++) {
                settings.add(new ConfigurationSetting().setKey(name + "-" + value));
            }
            for (ConfigurationSetting setting : settings) {
                client.setConfigurationSetting(setting);
            }
            SettingSelector filter = new SettingSelector().setKeyFilter(name + "-*");
            assertEquals(numberExpected, client.listConfigurationSettings(filter).stream().count());

            // Retention period can be setup when creating a snapshot and cannot edit.
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 15000L, (long) numberExpected, null, snapshotResult);

            assertEquals(numberExpected, client.listConfigurationSettingsForSnapshot(name).stream().count());

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });
    }

    @Test
    public void listSettingFromSnapshotWithFields() {
        // Create a snapshot
        createSnapshotRunner((name, filters) -> {
            // Prepare 5 settings before creating a snapshot
            final int numberExpected = 5;
            List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
            for (int value = 0; value < numberExpected; value++) {
                settings.add(new ConfigurationSetting().setKey(name + "-" + value).setValue(value + "-" + name));
            }
            for (ConfigurationSetting setting : settings) {
                client.setConfigurationSetting(setting);
            }
            SettingSelector filter = new SettingSelector().setKeyFilter(name + "-*");
            assertEquals(numberExpected, client.listConfigurationSettings(filter).stream().count());

            // Retention period can be setup when creating a snapshot and cannot edit.
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();

            assertEqualsConfigurationSnapshot(name, ConfigurationSnapshotStatus.READY, filters, SnapshotComposition.KEY,
                MINIMUM_RETENTION_PERIOD, 15000L, (long) numberExpected, null, snapshotResult);

            PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettingsForSnapshot(
                name, Arrays.asList(SettingFields.KEY, SettingFields.VALUE), Context.NONE);

            assertEquals(numberExpected, configurationSettings.stream().count());
            for (ConfigurationSetting setting : configurationSettings) {
                assertNotNull(setting.getKey());
                assertNotNull(setting.getValue());
                assertNull(setting.getLabel());
                assertNull(setting.getContentType());
                assertNull(setting.getLastModified());
                assertNull(setting.getETag());
                assertFalse(setting.isReadOnly());
                assertTrue(setting.getTags().isEmpty());
            }

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });
    }

    @Test
    public void listSettingsWithPageETag() {
        // Step 1: Prepare testing data.
        // Clean all existing settings before this test purpose
        client.listConfigurationSettings(null)
            .stream()
            .forEach(configurationSetting -> client.deleteConfigurationSetting(configurationSetting));
        // Add a few setting to form a page of settings
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(getKey()).setValue("value");
        final ConfigurationSetting setting2 = new ConfigurationSetting().setKey(getKey()).setValue("value");
        client.setConfigurationSetting(setting);
        client.setConfigurationSetting(setting2);
        // Get all page ETags
        PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(null);
        List<MatchConditions> matchConditionsList = new ArrayList<>();
        configurationSettings.iterableByPage().forEach(pagedResponse -> {
            matchConditionsList
                .add(new MatchConditions().setIfNoneMatch(pagedResponse.getHeaders().getValue(HttpHeaderName.ETAG)));
        });

        // Step 2: Test list settings with page ETag
        // Validation 1: Validate all pages are not modified and return empty list of settings in each page response.
        // List settings with page ETag
        PagedIterable<ConfigurationSetting> settings
            = client.listConfigurationSettings(new SettingSelector().setMatchConditions(matchConditionsList));
        settings.iterableByPage().forEach(pagedResponse -> {
            // No changes on the server side, so the response should be empty list
            assertEquals(0, pagedResponse.getValue().size());
        });
        // Validation 2: validate the page has the updated setting should be returned
        // Update a setting
        final ConfigurationSetting updatedSetting
            = new ConfigurationSetting().setKey(setting.getKey()).setValue("new value");
        client.setConfigurationSetting(updatedSetting);
        // List settings with expired page ETag
        settings = client.listConfigurationSettings(new SettingSelector().setMatchConditions(matchConditionsList));
        // The page has the updated setting should be returned, so the response should not be empty list
        settings.iterableByPage().forEach(pagedResponse -> {
            assertFalse(pagedResponse.getValue().isEmpty());
            // find the updated setting in the list
            ConfigurationSetting updatedSettingFromResponse = pagedResponse.getValue()
                .stream()
                .filter(s -> s.getKey().equals(updatedSetting.getKey()))
                .findAny()
                .get();
            assertConfigurationEquals(updatedSetting, updatedSettingFromResponse);
        });
    }

    @Test
    public void listLabels() {
        // Clean all existing settings before this test purpose
        client.listConfigurationSettings(null)
            .stream()
            .forEach(configurationSetting -> client.deleteConfigurationSetting(configurationSetting));
        // Prepare two settings with different labels
        List<ConfigurationSetting> preparedSettings = listLabelsRunner(setting -> assertConfigurationEquals(setting,
            client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue()));
        ConfigurationSetting setting = preparedSettings.get(0);
        ConfigurationSetting setting2 = preparedSettings.get(1);

        // List only the first label var, 'label'
        String label = setting.getLabel();
        PagedIterable<SettingLabel> labels
            = client.listLabels(new SettingLabelSelector().setNameFilter(setting.getLabel()));
        assertEquals(1, labels.stream().count());
        assertEquals(label, labels.iterator().next().getName());
        // List labels with wildcard label filter
        String label2 = setting2.getLabel();
        PagedIterable<SettingLabel> wildCardLabels
            = client.listLabels(new SettingLabelSelector().setNameFilter("label*"));
        List<String> collect = wildCardLabels.stream()
            .collect(Collectors.toList())
            .stream()
            .map(SettingLabel::getName)
            .collect(Collectors.toList());
        assertTrue(collect.contains(label));
        assertTrue(collect.contains(label2));
        // List all labels
        PagedIterable<SettingLabel> allLabels = client.listLabels();
        assertTrue(allLabels.stream().count() >= 2);
    }

    @Test
    public void listSettingByTagsFilter() {
        // Clean all existing settings before this test purpose
        client.listConfigurationSettings(null)
            .stream()
            .forEach(configurationSetting -> client.deleteConfigurationSetting(configurationSetting));
        // Prepare two settings with different tags
        List<ConfigurationSetting> preparedSettings
            = listSettingByTagsFilterRunner(setting -> assertConfigurationEquals(setting,
                client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue()));
        ConfigurationSetting setting = preparedSettings.get(0);
        ConfigurationSetting setting2 = preparedSettings.get(1);

        // List setting by first tags filter, it should return all settings
        PagedIterable<ConfigurationSetting> configurationSettings = client
            .listConfigurationSettings(new SettingSelector().setTagsFilter(getTagsFilterInString(setting.getTags())));
        Iterator<ConfigurationSetting> iterator = configurationSettings.iterator();
        assertConfigurationEquals(setting, iterator.next());
        assertConfigurationEquals(setting2, iterator.next());
        // List setting by second tags filter, it should return only one setting
        PagedIterable<ConfigurationSetting> configurationSettings2 = client
            .listConfigurationSettings(new SettingSelector().setTagsFilter(getTagsFilterInString(setting2.getTags())));
        assertEquals(1, configurationSettings2.stream().count());
        assertConfigurationEquals(setting2, configurationSettings2.iterator().next());
    }

    @Test
    public void listRevisionsWithTagsFilter() {
        // Create 3 revisions of the same key.
        List<ConfigurationSetting> configurationSettings = listRevisionsWithTagsFilterRunner(setting -> {
            assertConfigurationEquals(setting,
                client.setConfigurationSettingWithResponse(setting, false, Context.NONE).getValue());
        });
        ConfigurationSetting original = configurationSettings.get(0);
        // Get all revisions for a key with tags filter, they are listed in descending order.
        List<ConfigurationSetting> revisions
            = client.listRevisions(new SettingSelector().setKeyFilter(original.getKey())
                .setTagsFilter(getTagsFilterInString(original.getTags()))).stream().collect(Collectors.toList());
        assertEquals(1, revisions.size());
        assertConfigurationEquals(original, revisions.get(0));
    }

    @Test
    public void createSnapshotWithTagsFilter() {
        // Clean all existing settings before this test purpose
        client.listConfigurationSettings(null)
            .stream()
            .forEach(configurationSetting -> client.deleteConfigurationSetting(configurationSetting));
        // Prepare settings before creating a snapshot
        List<ConfigurationSetting> settings = createSnapshotWithTagsFilterPrepareRunner(
            setting -> assertConfigurationEquals(setting, client.addConfigurationSetting(setting)));
        ConfigurationSetting setting = settings.get(0);
        ConfigurationSetting settingWithTag = settings.get(1);
        assertTrue(setting.getTags().isEmpty());
        assertFalse(settingWithTag.getTags().isEmpty());

        createSnapshotWithTagsFilterRunner((name, filters) -> {
            ConfigurationSnapshot snapshot
                = new ConfigurationSnapshot(filters).setRetentionPeriod(MINIMUM_RETENTION_PERIOD);
            SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller
                = client.beginCreateSnapshot(name, snapshot, Context.NONE);
            poller.setPollInterval(interceptorManager.isPlaybackMode() ? Duration.ofMillis(1) : Duration.ofSeconds(10));
            poller.waitForCompletion();
            ConfigurationSnapshot snapshotResult = poller.getFinalResult();
            assertEquals(name, snapshotResult.getName());
            // The snapshot should only contain the setting with tags
            PagedIterable<ConfigurationSetting> configurationSettings
                = client.listConfigurationSettingsForSnapshot(name, null, Context.NONE);
            List<ConfigurationSetting> list = configurationSettings.stream().collect(Collectors.toList());
            assertEquals(settingWithTag.getTags(), list.get(0).getTags());

            // Archived the snapshot, it will be deleted automatically when retention period expires.
            assertEquals(ConfigurationSnapshotStatus.ARCHIVED, client.archiveSnapshot(name).getStatus());
        });
    }

    /**
     * Test helper that calling list configuration setting with given key and label input
     *
     * @param keyFilter key filter expression
     * @param labelFilter label filter expression
     */
    private void filterValueTest(String keyFilter, String labelFilter) {
        listConfigurationSettingsSelectFieldsWithNotSupportedFilterRunner(keyFilter, labelFilter, selector -> {
            try {
                client.listConfigurationSettings(selector).iterator().forEachRemaining(ConfigurationSetting::getLabel);
                Assertions.fail("Expected to fail");
            } catch (Exception ex) {
                assertRestException(ex, HttpResponseException.class, 400);
            }
        });
    }
}
