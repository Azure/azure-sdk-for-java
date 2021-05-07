// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.data.appconfiguration.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationClientTest extends ConfigurationClientTestBase {
    private final ClientLogger logger = new ClientLogger(ConfigurationClientTest.class);

    private ConfigurationClient client;

    @Override
    protected String getTestName() {
        return "";
    }

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listConfigurationSettings(new SettingSelector().setKeyFilter(keyPrefix + "*")).forEach(configurationSetting -> {
            logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
            if (configurationSetting.isReadOnly()) {
                client.setReadOnlyWithResponse(configurationSetting, false, Context.NONE);
            }
            client.deleteConfigurationSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });

        logger.info("Finished cleaning up values.");
    }

    private ConfigurationClient getConfigurationClient(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        return clientSetup(credentials -> {
            ConfigurationClientBuilder builder = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
                .serviceVersion(serviceVersion)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
            if (getTestMode() != TestMode.PLAYBACK) {
                builder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .addPolicy(new RetryPolicy());
            }
            return builder.buildClient();
        });
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected,
            client.addConfigurationSetting(expected)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        addFeatureFlagConfigurationSettingRunner((expected) -> assertFeatureFlagConfigurationSettingEquals(expected,
            (FeatureFlagConfigurationSetting) client.addConfigurationSetting(expected)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        addSecretReferenceConfigurationSettingRunner(
            (expected) -> assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.addConfigurationSetting(expected)));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingEmptyKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        assertRestException(() -> client.addConfigurationSetting("", null, "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingEmptyValue(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        addConfigurationSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting, client.addConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getConfigurationSetting(setting.getKey(), setting.getLabel()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingNullKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.addConfigurationSetting(null, null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addConfigurationSettingWithResponse(null, Context.NONE), NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addExistingSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        addExistingSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertRestException(() -> client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue(),
                ResourceExistsException.class, HttpURLConnection.HTTP_PRECON_FAILED);
        });
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is read-only updates cannot happen, this will result in a 409.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        setConfigurationSettingRunner(
            (expected, update) -> assertConfigurationEquals(expected,
                client.setConfigurationSettingWithResponse(expected, false, Context.NONE).getValue()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        setConfigurationSettingRunner(
            (expected, update) -> assertConfigurationEquals(expected, client.setConfigurationSetting(expected)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        setFeatureFlagConfigurationSettingRunner(
            (expected, update) -> assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.setConfigurationSetting(expected)));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        setSecretReferenceConfigurationSettingRunner(
            (expected, update) -> assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.setConfigurationSetting(expected)));
    }

    /**
     * Tests that when an ETag is passed to set it will only set if the current representation of the setting has the
     * ETag. If the set ETag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingIfETag(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        setConfigurationSettingIfETagRunner((initial, update) -> {
            // This ETag is not the correct format. It is not the correct hash that the service is expecting.
            assertRestException(() -> client.setConfigurationSettingWithResponse(initial.setETag("badETag"), true, Context.NONE).getValue(), HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);

            final String etag = client.addConfigurationSettingWithResponse(initial, Context.NONE).getValue().getETag();

            assertConfigurationEquals(update, client.setConfigurationSettingWithResponse(update.setETag(etag), true, Context.NONE));
            assertRestException(() -> client.setConfigurationSettingWithResponse(initial, true, Context.NONE).getValue(), HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);
            assertConfigurationEquals(update, client.getConfigurationSetting(update.getKey(), update.getLabel()));
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingEmptyKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        assertRestException(() -> client.setConfigurationSetting("", null, "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingEmptyValue(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        setConfigurationSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting, client.setConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getConfigurationSetting(setting.getKey(), setting.getLabel()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingNullKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.setConfigurationSetting(null, null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.setConfigurationSettingWithResponse(null, false, Context.NONE).getValue(), NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is read-only.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        getConfigurationSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSettingConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        getConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        getFeatureFlagConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.getConfigurationSetting(expected));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        getSecretReferenceConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.getConfigurationSetting(expected));
        });
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSettingNotFound(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().setKey(key).setValue("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        assertConfigurationEquals(neverRetrievedConfiguration, client.addConfigurationSettingWithResponse(neverRetrievedConfiguration, Context.NONE).getValue());

        assertRestException(() -> client.getConfigurationSetting("myNonExistentKey", null, null), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        assertRestException(() -> client.getConfigurationSetting(nonExistentLabel.getKey(), nonExistentLabel.getLabel()), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        deleteConfigurationSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));

            assertConfigurationEquals(expected, client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        deleteConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));

            assertConfigurationEquals(expected, client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        deleteFeatureFlagConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.getConfigurationSetting(expected));

            assertFeatureFlagConfigurationSettingEquals(expected,
                (FeatureFlagConfigurationSetting) client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        deleteSecretReferenceConfigurationSettingRunner((expected) -> {
            client.addConfigurationSetting(expected);
            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.getConfigurationSetting(expected));

            assertSecretReferenceConfigurationSettingEquals(expected,
                (SecretReferenceConfigurationSetting) client.deleteConfigurationSetting(expected));
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()),
                ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingNotFound(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguation = new ConfigurationSetting().setKey(key).setValue("myNeverDeletedValue");
        final ConfigurationSetting notFoundDelete = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        assertConfigurationEquals(neverDeletedConfiguation, client.addConfigurationSettingWithResponse(neverDeletedConfiguation, Context.NONE).getValue());

        assertConfigurationEquals(null, client.deleteConfigurationSetting("myNonExistentKey", null));
        assertConfigurationEquals(null, client.deleteConfigurationSettingWithResponse(notFoundDelete, false, Context.NONE), HttpURLConnection.HTTP_NO_CONTENT);

        assertConfigurationEquals(neverDeletedConfiguation, client.getConfigurationSetting(neverDeletedConfiguation.getKey(), neverDeletedConfiguation.getLabel()));
    }

    /**
     * Tests that when an ETag is passed to delete it will only delete if the current representation of the setting has the ETag.
     * If the delete ETag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingWithETag(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        deleteConfigurationSettingWithETagRunner((initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addConfigurationSettingWithResponse(initial, Context.NONE).getValue();
            final ConfigurationSetting updatedConfig = client.setConfigurationSettingWithResponse(update, false, Context.NONE).getValue();

            assertConfigurationEquals(update, client.getConfigurationSetting(initial.getKey(), initial.getLabel()));
            assertRestException(() -> client.deleteConfigurationSettingWithResponse(initiallyAddedConfig, true, Context.NONE).getValue(), HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED);
            assertConfigurationEquals(update, client.deleteConfigurationSettingWithResponse(updatedConfig, true, Context.NONE).getValue());
            assertRestException(() -> client.getConfigurationSetting(initial.getKey(), initial.getLabel()), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingNullKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.deleteConfigurationSetting(null, null), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.deleteConfigurationSettingWithResponse(null, false, Context.NONE).getValue(), NullPointerException.class);
    }

    /**
     * Tests assert that the setting can be deleted after clear read-only of the setting.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnly(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        lockUnlockRunner((expected) -> {
            // read-only setting
            client.addConfigurationSettingWithResponse(expected, Context.NONE);
            client.setReadOnlyWithResponse(expected, true, Context.NONE).getValue();

            // unsuccessfully delete
            assertRestException(() ->
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE),
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        lockUnlockRunner((expected) -> {

            // lock setting
            client.addConfigurationSettingWithResponse(expected, Context.NONE);
            client.setReadOnlyWithResponse(expected, true, Context.NONE);

            // unsuccessfully deleted
            assertRestException(() ->
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE),
                HttpResponseException.class, 409);

            // unlock setting and delete
            client.setReadOnlyWithResponse(expected, false, Context.NONE);

            // successfully deleted
            assertConfigurationEquals(expected,
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
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

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listWithKeyAndLabel(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String value = "myValue";
        final String key = getKey();
        final String label = getLabel();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue(value).setLabel(label);

        assertConfigurationEquals(expected, client.setConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
        assertConfigurationEquals(expected, client.listConfigurationSettings(new SettingSelector().setKeyFilter(key).setLabelFilter(label)).iterator().next());
        assertConfigurationEquals(expected, client.listConfigurationSettings(new SettingSelector().setKeyFilter(key)).iterator().next());
    }

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listWithMultipleKeys(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        String key = getKey();
        String key2 = getKey();

        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            assertConfigurationEquals(setting, client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2, client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            return client.listConfigurationSettings(new SettingSelector().setKeyFilter(key + "," + key2));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsWithNullSelector(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);

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

            final PagedIterable<ConfigurationSetting> configurationSettingIterable =
                client.listConfigurationSettings(new SettingSelector().setKeyFilter(key + "," + key2));
            assertEquals(2, configurationSettingIterable.stream().count());
            return configurationSettingIterable;
        });
    }

    /**
     * Verifies that ConfigurationSettings can be added with different labels and that we can fetch those ConfigurationSettings
     * from the service when filtering by their labels.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listWithMultipleLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listWithMultipleLabelsRunner(key, label, label2, (setting, setting2) -> {
            assertConfigurationEquals(setting, client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2, client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            return client.listConfigurationSettings(new SettingSelector().setKeyFilter(key).setLabelFilter(label + "," + label2));
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFields(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        listConfigurationSettingsSelectFieldsRunner((settings, selector) -> {
            settings.forEach(setting -> client.setConfigurationSettingWithResponse(setting, false, Context.NONE).getValue());
            return client.listConfigurationSettings(selector);
        });
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a key filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithPrefixStarKeyFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        filterValueTest("*" + getKey(), getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a* key filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithSubstringKeyFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        filterValueTest("*" + getKey() + "*", getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a label filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithPrefixStarLabelFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        filterValueTest(getKey(), "*" + getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a* label filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithSubstringLabelFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        filterValueTest(getKey(), "*" + getLabel() + "*");
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsAcceptDateTime(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        try {
            assertConfigurationEquals(original, client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated, client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated2, client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());
        } catch (InterruptedException ex) {
            // Do nothing.
        }

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().setKeyFilter(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        assertConfigurationEquals(updated, (client.listConfigurationSettings(options).stream().collect(Collectors.toList())).get(0));
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisions(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        assertConfigurationEquals(original, client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
        assertConfigurationEquals(updated, client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
        assertConfigurationEquals(updated2, client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());

        // Get all revisions for a key, they are listed in descending order.
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated2, revisions.get(0));
        assertConfigurationEquals(updated, revisions.get(1));
        assertConfigurationEquals(original, revisions.get(2));

        // Verifies that we can select specific fields.
        revisions = client.listRevisions(new SettingSelector().setKeyFilter(keyName).setFields(SettingFields.KEY, SettingFields.ETAG)).stream().collect(Collectors.toList());
        validateListRevisions(updated2, revisions.get(0));
        validateListRevisions(updated, revisions.get(1));
        validateListRevisions(original, revisions.get(2));

        // Verifies that we have revision list size greater than 0. The count number of revision changes.
        assertTrue(client.listRevisions(null).stream().collect(Collectors.toList()).size() > 0);
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithMultipleKeys(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            assertConfigurationEquals(testInput.get(0), client.addConfigurationSettingWithResponse(testInput.get(0), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(1), client.setConfigurationSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2), client.addConfigurationSettingWithResponse(testInput.get(2), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(3), client.setConfigurationSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listRevisions(new SettingSelector().setKeyFilter(key + "," + key2));
        });
    }

    /**
     * Verifies that we can get all revisions for all settings with the specified labels.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithMultipleLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listRevisionsWithMultipleLabelsRunner(key, label, label2, (testInput) -> {
            assertConfigurationEquals(testInput.get(0), client.addConfigurationSettingWithResponse(testInput.get(0), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(1), client.setConfigurationSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2), client.addConfigurationSettingWithResponse(testInput.get(2), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(3), client.setConfigurationSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listRevisions(new SettingSelector().setKeyFilter(key).setLabelFilter(label + "," + label2));
        });
    }

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsAcceptDateTime(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        try {
            assertConfigurationEquals(original, client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated, client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated2, client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());
        } catch (InterruptedException ex) {
            // Do nothing.
        }

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().setKeyFilter(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        revisions = client.listRevisions(options).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated, revisions.get(0));
        assertConfigurationEquals(original, revisions.get(1));
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithPagination(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);
        assertEquals(numberExpected, client.listRevisions(filter).stream().collect(Collectors.toList()).size());
    }

    /**
     * Verifies that, given a ton of revisions, we can process {@link java.util.stream.Stream} multiple time and get same result.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithPaginationAndRepeatStream(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithPaginationAndRepeatIterator(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);

        PagedIterable<ConfigurationSetting> configurationSettingPagedIterable = client.listRevisions(filter);
        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        configurationSettingPagedIterable.iterator().forEachRemaining(configurationSetting -> configurationSettingList1.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedIterable.iterator().forEachRemaining(configurationSetting -> configurationSettingList2.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList2.size());

        equalsArray(configurationSettingList1, configurationSettingList2);
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsWithPagination(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix), false, Context.NONE).getValue();
        }
        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix + "-*").setLabelFilter(labelPrefix);

        assertEquals(numberExpected, client.listConfigurationSettings(filter).stream().count());
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the ETag provided does not match the one of the current setting.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSettingWhenValueNotUpdated(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final String key = getKey();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting block = client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();

        assertNotNull(block);
        assertConfigurationEquals(expected, block);
        // conditional get, now the setting has not be updated yet, resulting 304 and null value
        assertConfigurationEquals(null, client.getConfigurationSettingWithResponse(block, null, true, Context.NONE), 304);
        assertConfigurationEquals(newExpected, client.setConfigurationSettingWithResponse(newExpected, false, Context.NONE).getValue());
        // conditional get, now the setting is updated and we are able to get a new setting with 200 code
        assertConfigurationEquals(newExpected, client.getConfigurationSettingWithResponse(newExpected, null, true, Context.NONE).getValue());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    @Disabled
    public void deleteAllSettings(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);

        client.listConfigurationSettings(new SettingSelector().setKeyFilter("*")).forEach(configurationSetting -> {
            logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
            client.deleteConfigurationSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addHeadersFromContextPolicyTest(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationClient(httpClient, serviceVersion);
        final HttpHeaders headers = getCustomizedHeaders();
        addHeadersFromContextPolicyRunner(expected -> {
                final Response<ConfigurationSetting> response =
                    client.addConfigurationSettingWithResponse(expected,
                        new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
                assertContainsHeaders(headers, response.getRequest().getHeaders());
            }
        );
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
                client.listConfigurationSettings(selector).iterator().forEachRemaining(setting -> setting.getLabel());
                Assertions.fail("Expected to fail");
            } catch (Exception ex) {
                assertRestException(ex, HttpResponseException.class, 400);
            }
        });
    }
}
