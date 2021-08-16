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
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.data.appconfiguration.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigurationAsyncClientTest extends ConfigurationClientTestBase {

    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClientTest.class);
    private static final String NO_LABEL = null;
    private ConfigurationAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listConfigurationSettings(new SettingSelector().setKeyFilter(keyPrefix + "*"))
            .flatMap(configurationSetting -> {
                logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
                Mono<Response<ConfigurationSetting>> unlock = configurationSetting.isReadOnly() ? client.setReadOnlyWithResponse(configurationSetting, false) : Mono.empty();
                return unlock.then(client.deleteConfigurationSettingWithResponse(configurationSetting, false));
            })
            .blockLast();

        logger.info("Finished cleaning up values.");
    }

    private ConfigurationAsyncClient getConfigurationAsyncClient(HttpClient httpClient,
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
            return builder.buildAsyncClient();
        });
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label
     * identifier.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        addConfigurationSettingRunner((expected) ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        addConfigurationSettingRunner(
            (expected) ->
                StepVerifier.create(client.addConfigurationSetting(expected))
                    .assertNext(response -> assertConfigurationEquals(expected, response))
                    .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        addFeatureFlagConfigurationSettingRunner(
            (expected) ->
                StepVerifier.create(client.addConfigurationSetting(expected))
                    .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                        (FeatureFlagConfigurationSetting) response))
                    .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        addSecretReferenceConfigurationSettingRunner(
            (expected) ->
                StepVerifier.create(client.addConfigurationSetting(expected))
                    .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                        (SecretReferenceConfigurationSetting) response))
                    .verifyComplete());
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingEmptyKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.addConfigurationSetting("", null, "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingEmptyValue(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        addConfigurationSettingEmptyValueRunner((setting) -> {
            StepVerifier.create(client.addConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSetting(setting.getKey(), setting.getLabel(), null))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addConfigurationSettingNullKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.addConfigurationSetting(null, null, "A Value"))
            .expectError(IllegalArgumentException.class)
            .verify();

        StepVerifier.create(client.addConfigurationSettingWithResponse(null))
            .expectError(NullPointerException.class)
            .verify();
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addExistingSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        addExistingSettingRunner((expected) ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected).then(client.addConfigurationSettingWithResponse(expected)))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceExistsException.class, HttpURLConnection.HTTP_PRECON_FAILED)));
    }

    /**
     * Tests that a configuration is able to be added or updated with set. When the configuration is read-only updates
     * cannot happen, this will result in a 409.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        setConfigurationSettingRunner((expected, update) ->
            StepVerifier.create(client.setConfigurationSettingWithResponse(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        setConfigurationSettingRunner(
            (expected, update) -> StepVerifier.create(client.setConfigurationSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        setFeatureFlagConfigurationSettingRunner(
            (expected, update) -> StepVerifier.create(client.setConfigurationSetting(expected))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(
                    expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        setSecretReferenceConfigurationSettingRunner(
            (expected, update) -> StepVerifier.create(client.setConfigurationSetting(expected))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(
                    expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete());
    }

    /**
     * Tests that when an ETag is passed to set it will only set if the current representation of the setting has the
     * ETag. If the set ETag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingIfETag(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        setConfigurationSettingIfETagRunner((initial, update) -> {
            // This ETag is not the correct format. It is not the correct hash that the service is expecting.
            StepVerifier.create(client.setConfigurationSettingWithResponse(initial.setETag("badEtag"), true))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED));

            final String etag = client.addConfigurationSettingWithResponse(initial).block().getValue().getETag();

            StepVerifier.create(client.setConfigurationSettingWithResponse(update.setETag(etag), true))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();

            StepVerifier.create(client.setConfigurationSettingWithResponse(initial, true))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED));

            StepVerifier.create(client.getConfigurationSettingWithResponse(update, null, false))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingEmptyKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.setConfigurationSetting("", NO_LABEL, "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string. Value is not a required
     * property.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingEmptyValue(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        setConfigurationSettingEmptyValueRunner((setting) -> {
            StepVerifier.create(client.setConfigurationSetting(setting.getKey(), NO_LABEL, setting.getValue()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSetting(setting.getKey(), setting.getLabel(), null))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void setConfigurationSettingNullKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(client.setConfigurationSetting(null, NO_LABEL, "A Value"))
            .verifyError(IllegalArgumentException.class);
        StepVerifier.create(client.setConfigurationSettingWithResponse(null, false))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is read-only.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        getConfigurationSettingRunner((expected) ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected).then(client.getConfigurationSettingWithResponse(expected, null, false)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSettingConvenience(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        getConfigurationSettingRunner(
            (expected) -> StepVerifier.create(
                    client.addConfigurationSetting(expected).then(
                        client.getConfigurationSetting(expected)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        getFeatureFlagConfigurationSettingRunner(
            (expected) -> StepVerifier.create(
                    client.addConfigurationSetting(expected).then(
                        client.getConfigurationSetting(expected)))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        getSecretReferenceConfigurationSettingRunner(
            (expected) -> StepVerifier.create(
                    client.addConfigurationSetting(expected).then(
                        client.getConfigurationSetting(expected)))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete());
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSettingNotFound(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().setKey(key).setValue("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        StepVerifier.create(client.addConfigurationSettingWithResponse(neverRetrievedConfiguration))
            .assertNext(response -> assertConfigurationEquals(neverRetrievedConfiguration, response))
            .verifyComplete();

        StepVerifier.create(client.getConfigurationSetting("myNonExistentKey", null, null))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));


        StepVerifier.create(client.getConfigurationSettingWithResponse(nonExistentLabel, null, false))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that configurations are able to be deleted when they exist. After the configuration has been deleted
     * attempting to get it will result in a 404, the same as if the configuration never existed.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        deleteConfigurationSettingRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected).then(client.getConfigurationSettingWithResponse(expected, null, false)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            StepVerifier.create(client.deleteConfigurationSettingWithResponse(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSettingWithResponse(expected, null, false))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        deleteConfigurationSettingRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSetting(expected).then(client.getConfigurationSetting(expected)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSetting(expected))
                .verifyErrorSatisfies(
                    ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        deleteFeatureFlagConfigurationSettingRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSetting(expected).then(client.getConfigurationSetting(expected)))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete();

            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSetting(expected))
                .verifyErrorSatisfies(
                    ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        deleteSecretReferenceConfigurationSettingRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSetting(expected).then(client.getConfigurationSetting(expected)))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete();

            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSetting(expected))
                .verifyErrorSatisfies(
                    ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingNotFound(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguration = new ConfigurationSetting().setKey(key).setValue("myNeverDeletedValue");

        StepVerifier.create(client.addConfigurationSettingWithResponse(neverDeletedConfiguration))
            .assertNext(response -> assertConfigurationEquals(neverDeletedConfiguration, response))
            .verifyComplete();

        StepVerifier.create(client.deleteConfigurationSettingWithResponse(new ConfigurationSetting().setKey("myNonExistentKey"), false))
            .assertNext(response -> assertConfigurationEquals(null, response, HttpURLConnection.HTTP_NO_CONTENT))
            .verifyComplete();

        StepVerifier.create(client.deleteConfigurationSettingWithResponse(new ConfigurationSetting().setKey(neverDeletedConfiguration.getKey()).setLabel("myNonExistentLabel"), false))
            .assertNext(response -> assertConfigurationEquals(null, response, HttpURLConnection.HTTP_NO_CONTENT))
            .verifyComplete();

        StepVerifier.create(client.getConfigurationSetting(neverDeletedConfiguration.getKey(), neverDeletedConfiguration.getLabel(), null))
            .assertNext(response -> assertConfigurationEquals(neverDeletedConfiguration, response))
            .verifyComplete();
    }

    /**
     * Tests that when an ETag is passed to delete it will only delete if the current representation of the setting has
     * the ETag. If the delete ETag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingWithETag(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        deleteConfigurationSettingWithETagRunner((initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addConfigurationSettingWithResponse(initial).block().getValue();
            final ConfigurationSetting updatedConfig = client.setConfigurationSettingWithResponse(update, true).block().getValue();

            StepVerifier.create(client.getConfigurationSettingWithResponse(initial, null, false))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();

            StepVerifier.create(client.deleteConfigurationSettingWithResponse(initiallyAddedConfig, true))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, HttpURLConnection.HTTP_PRECON_FAILED));

            StepVerifier.create(client.deleteConfigurationSettingWithResponse(updatedConfig, true))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();

            StepVerifier.create(client.getConfigurationSettingWithResponse(initial, null, false))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be
     * thrown.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void deleteConfigurationSettingNullKey(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.deleteConfigurationSetting(null, null))
            .verifyError(IllegalArgumentException.class);
        StepVerifier.create(client.deleteConfigurationSettingWithResponse(null, false))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests assert that the setting can be deleted after clear read-only of the setting.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnly(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);

        lockUnlockRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // read-only setting
            StepVerifier.create(client.setReadOnly(expected.getKey(), expected.getLabel(), true))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // unsuccessfully delete
            StepVerifier.create(client.deleteConfigurationSettingWithResponse(expected, false))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 409));

            // clear read-only of setting and delete
            StepVerifier.create(client.setReadOnly(expected.getKey(), expected.getLabel(), false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // successfully deleted
            StepVerifier.create(client.deleteConfigurationSettingWithResponse(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();
        });
    }

    /**
     * Tests assert that the setting can be deleted after clear read-only of the setting.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithConfigurationSetting(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        lockUnlockRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // read-only setting
            StepVerifier.create(client.setReadOnly(expected.getKey(), expected.getLabel(), true))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // unsuccessfully delete
            StepVerifier.create(client.deleteConfigurationSettingWithResponse(expected, false))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 409));

            // clear read-only setting and delete
            StepVerifier.create(client.setReadOnlyWithResponse(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // successfully deleted
            StepVerifier.create(client.deleteConfigurationSettingWithResponse(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        lockUnlockRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // read-only setting
            StepVerifier.create(client.setReadOnly(expected, true))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // unsuccessfully delete
            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 409));

            // clear read-only setting and delete
            StepVerifier.create(client.setReadOnly(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // successfully deleted
            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithFeatureFlagConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        lockUnlockFeatureFlagRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSetting(expected))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete();

            // read-only setting
            StepVerifier.create(client.setReadOnly(expected, true))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete();

            // unsuccessfully delete
            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 409));

            // clear read-only setting and delete
            StepVerifier.create(client.setReadOnly(expected, false))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete();

            // successfully deleted
            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .assertNext(response -> assertFeatureFlagConfigurationSettingEquals(expected,
                    (FeatureFlagConfigurationSetting) response))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void clearReadOnlyWithSecretReferenceConfigurationSettingConvenience(HttpClient httpClient,
        ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        lockUnlockSecretReferenceRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSetting(expected))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete();

            // read-only setting
            StepVerifier.create(client.setReadOnly(expected, true))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete();

            // unsuccessfully delete
            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 409));

            // clear read-only setting and delete
            StepVerifier.create(client.setReadOnly(expected, false))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete();

            // successfully deleted
            StepVerifier.create(client.deleteConfigurationSetting(expected))
                .assertNext(response -> assertSecretReferenceConfigurationSettingEquals(expected,
                    (SecretReferenceConfigurationSetting) response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listWithKeyAndLabel(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String value = "myValue";
        final String key = testResourceNamer.randomName(keyPrefix, 16);
        final String label = testResourceNamer.randomName("lbl", 8);
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue(value).setLabel(label);

        StepVerifier.create(client.setConfigurationSettingWithResponse(expected, false))
            .assertNext(response -> assertConfigurationEquals(expected, response))
            .verifyComplete();

        StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeyFilter(key).setLabelFilter(label)))
            .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
            .verifyComplete();

        StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeyFilter(key)))
            .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
            .verifyComplete();
    }

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listWithMultipleKeys(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        String key = getKey();
        String key2 = getKey();

        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeyFilter(key + "," + key2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsWithNullSelector(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String key = getKey();
        final String key2 = getKey();

        // Delete all existing settings in the resource
        StepVerifier.create(
                client.listConfigurationSettings(null)
                    .flatMap(setting -> client.deleteConfigurationSettingWithResponse(setting, false))
                    .then())
            .verifyComplete();

        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();
            StepVerifier.create(client.addConfigurationSettingWithResponse(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listConfigurationSettings(null))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();
            assertEquals(2, selected.size());
            return selected;
        });
    }

    /**
     * Verifies that ConfigurationSettings can be added with different labels and that we can fetch those
     * ConfigurationSettings from the service when filtering by their labels.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listWithMultipleLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listWithMultipleLabelsRunner(key, label, label2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeyFilter(key).setLabelFilter(label + "," + label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFields(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        listConfigurationSettingsSelectFieldsRunner((settings, selector) -> {
            final List<Mono<Response<ConfigurationSetting>>> settingsBeingAdded = new ArrayList<>();
            for (ConfigurationSetting setting : settings) {
                settingsBeingAdded.add(client.setConfigurationSettingWithResponse(setting, false));
            }

            // Waiting for all the settings to be added.
            Flux.merge(settingsBeingAdded).blockLast();

            List<ConfigurationSetting> settingsReturned = new ArrayList<>();
            StepVerifier.create(client.listConfigurationSettings(selector))
                .assertNext(settingsReturned::add)
                .assertNext(settingsReturned::add)
                .verifyComplete();

            return settingsReturned;
        });
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a key filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithPrefixStarKeyFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        filterValueTest("*" + getKey(), getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a* key filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithSubstringKeyFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        filterValueTest("*" + getKey() + "*", getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a label filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithPrefixStarLabelFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        filterValueTest(getKey(), "*" + getLabel());
    }

    /**
     * Verifies that throws exception when using SettingSelector with not supported *a* label filter.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsSelectFieldsWithSubstringLabelFilter(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        filterValueTest(getKey(), "*" + getLabel() + "*");
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsAcceptDateTime(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setConfigurationSettingWithResponse(original, false))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();
        StepVerifier.create(client.setConfigurationSettingWithResponse(updated, false).delayElement(Duration.ofSeconds(2)))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .verifyComplete();
        StepVerifier.create(client.setConfigurationSettingWithResponse(updated2, false))
            .assertNext(response -> assertConfigurationEquals(updated2, response))
            .verifyComplete();

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().setKeyFilter(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        StepVerifier.create(client.listConfigurationSettings(options))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .verifyComplete();
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisions(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setConfigurationSettingWithResponse(original, false))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();
        StepVerifier.create(client.setConfigurationSettingWithResponse(updated, false))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .verifyComplete();
        StepVerifier.create(client.setConfigurationSettingWithResponse(updated2, false))
            .assertNext(response -> assertConfigurationEquals(updated2, response))
            .verifyComplete();

        // Get all revisions for a key, they are listed in descending order.
        StepVerifier.create(client.listRevisions(new SettingSelector().setKeyFilter(keyName)))
            .assertNext(response -> assertConfigurationEquals(updated2, response))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();

        // Verifies that we can select specific fields.
        StepVerifier.create(client.listRevisions(new SettingSelector().setKeyFilter(keyName).setFields(SettingFields.KEY, SettingFields.ETAG)))
            .assertNext(response -> validateListRevisions(updated2, response))
            .assertNext(response -> validateListRevisions(updated, response))
            .assertNext(response -> validateListRevisions(original, response))
            .verifyComplete();

        SettingSelector settingSelector = new SettingSelector();
        settingSelector.setKeyFilter("prodDBConnection");
        com.azure.core.util.Context context = new com.azure.core.util.Context("key", "value");

        StepVerifier.create(client.listRevisions(settingSelector, context, "", true))
            .verifyError();

        StepVerifier.create(client.listRevisionsFirstPage(null, context, "", true)
                .then(client.listRevisionsNextPage(connectionString, context, "", true)))
            .verifyError();
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithMultipleKeys(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addConfigurationSettingWithResponse(testInput.get(0)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.setConfigurationSettingWithResponse(testInput.get(1), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(testInput.get(2)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.setConfigurationSettingWithResponse(testInput.get(3), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(3), response))
                .verifyComplete();

            StepVerifier.create(client.listRevisions(new SettingSelector().setKeyFilter(key + "," + key2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    /**
     * Verifies that we can get all revisions for all settings with the specified labels.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithMultipleLabels(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listRevisionsWithMultipleLabelsRunner(key, label, label2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addConfigurationSettingWithResponse(testInput.get(0)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.setConfigurationSettingWithResponse(testInput.get(1), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(testInput.get(2)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.setConfigurationSettingWithResponse(testInput.get(3), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(3), response))
                .verifyComplete();

            StepVerifier.create(client.listRevisions(new SettingSelector().setKeyFilter(key).setLabelFilter(label + "," + label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsAcceptDateTime(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setConfigurationSettingWithResponse(original, false))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();
        StepVerifier.create(client.setConfigurationSettingWithResponse(updated, false).delayElement(Duration.ofSeconds(2)))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .verifyComplete();
        StepVerifier.create(client.setConfigurationSettingWithResponse(updated2, false))
            .assertNext(response -> assertConfigurationEquals(updated2, response))
            .verifyComplete();

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeyFilter(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().setKeyFilter(keyName)
            .setAcceptDatetime(revisions.get(1).getLastModified());
        StepVerifier.create(client.listRevisions(options))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination (ie.
     * where 'nextLink' has a URL pointing to the next page of results.)
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithPagination(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix));
        }

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listRevisions(filter))
            .expectNextCount(numberExpected)
            .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination and
     * stream is invoked multiple times. (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithPaginationAndRepeatStream(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (int value = 0; value < numberExpected; value++) {
            ConfigurationSetting setting = new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix);
            settings.add(setting);
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);

        Flux.merge(results).blockLast();

        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        PagedFlux<ConfigurationSetting> configurationSettingPagedFlux = client.listRevisions(filter);
        configurationSettingPagedFlux.toStream().forEach(configurationSettingList1::add);
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedFlux.toStream().forEach(configurationSettingList2::add);
        assertEquals(numberExpected, configurationSettingList2.size());
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination and
     * stream is invoked multiple times. (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listRevisionsWithPaginationAndRepeatIterator(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (int value = 0; value < numberExpected; value++) {
            ConfigurationSetting setting = new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix);
            settings.add(setting);
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix).setLabelFilter(labelPrefix);

        Flux.merge(results).blockLast();

        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        PagedFlux<ConfigurationSetting> configurationSettingPagedFlux = client.listRevisions(filter);
        configurationSettingPagedFlux.toIterable().forEach(configurationSettingList1::add);
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedFlux.toIterable().forEach(configurationSettingList2::add);
        assertEquals(numberExpected, configurationSettingList2.size());
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination (ie.
     * where 'nextLink' has a URL pointing to the next page of results.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void listConfigurationSettingsWithPagination(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix));
        }

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeyFilter(keyPrefix + "-*").setLabelFilter(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listConfigurationSettings(filter))
            .expectNextCount(numberExpected)
            .verifyComplete();
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the ETag provided does not match the one of the current setting.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void getConfigurationSettingWhenValueNotUpdated(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final String key = getKey();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting block = client.addConfigurationSettingWithResponse(expected).block().getValue();

        assertNotNull(block);

        // conditional get, now the setting has not be updated yet, resulting 304 and null value
        StepVerifier.create(client.getConfigurationSettingWithResponse(block, null, true))
            .assertNext(response -> assertConfigurationEquals(null, response, 304))
            .verifyComplete();

        StepVerifier.create(client.setConfigurationSettingWithResponse(newExpected, false))
            .assertNext(response -> assertConfigurationEquals(newExpected, response))
            .verifyComplete();

        // conditional get, now the setting is updated and we are able to get a new setting with 200 code
        StepVerifier.create(client.getConfigurationSettingWithResponse(block, null, true))
            .assertNext(response -> assertConfigurationEquals(newExpected, response))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    @Disabled
    public void deleteAllSettings(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        client.listConfigurationSettings(new SettingSelector().setKeyFilter("*"))
            .flatMap(configurationSetting -> {
                logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
                return client.deleteConfigurationSettingWithResponse(configurationSetting, false);
            }).blockLast();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.data.appconfiguration.TestHelper#getTestParameters")
    public void addHeadersFromContextPolicyTest(HttpClient httpClient, ConfigurationServiceVersion serviceVersion) {
        client = getConfigurationAsyncClient(httpClient, serviceVersion);
        final HttpHeaders headers = getCustomizedHeaders();
        addHeadersFromContextPolicyRunner(expected ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected)
                    .subscriberContext(Context.of(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers)))
                .assertNext(response -> {
                    final HttpHeaders requestHeaders = response.getRequest().getHeaders();
                    assertContainsHeaders(headers, requestHeaders);
                })
                .verifyComplete());
    }

    /**
     * Test helper that calling list configuration setting with given key and label input
     *
     * @param keyFilter key filter expression
     * @param labelFilter label filter expression
     */
    private void filterValueTest(String keyFilter, String labelFilter) {
        listConfigurationSettingsSelectFieldsWithNotSupportedFilterRunner(keyFilter, labelFilter, selector ->
            StepVerifier.create(client.listConfigurationSettings(selector))
                .verifyError(HttpResponseException.class));
    }
}
