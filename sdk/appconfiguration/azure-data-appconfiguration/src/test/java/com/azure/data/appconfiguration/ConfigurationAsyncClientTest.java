// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationAsyncClientTest extends ConfigurationClientTestBase {
    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClientTest.class);
    private static final String NO_LABEL = null;
    private ConfigurationAsyncClient client;

    @Override
    protected String getTestName() {
        return "";
    }

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                    .connectionString(connectionString)
                    .httpClient(interceptorManager.getPlaybackClient())
                    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .buildAsyncClient());
        } else {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                    .connectionString(connectionString)
                    .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .addPolicy(new RetryPolicy())
                    .buildAsyncClient());
        }
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listConfigurationSettings(new SettingSelector().setKeys(keyPrefix + "*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
                    Mono<Response<ConfigurationSetting>> unlock = configurationSetting.isReadOnly() ? client.setReadOnlyWithResponse(configurationSetting, false) : Mono.empty();
                    return unlock.then(client.deleteConfigurationSettingWithResponse(configurationSetting, false));
                })
                .blockLast();

        logger.info("Finished cleaning up values.");
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    @Test
    public void addConfigurationSetting() {
        addConfigurationSettingRunner((expected) ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @Test
    public void addConfigurationSettingEmptyKey() {
        StepVerifier.create(client.addConfigurationSetting("", null, "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @Test
    public void addConfigurationSettingEmptyValue() {
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
    @Test
    public void addConfigurationSettingNullKey() {
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
    @Test
    public void addExistingSetting() {
        addExistingSettingRunner((expected) ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected).then(client.addConfigurationSettingWithResponse(expected)))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceExistsException.class, HttpURLConnection.HTTP_PRECON_FAILED)));
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is read-only updates cannot happen, this will result in a 409.
     */
    @Test
    public void setConfigurationSetting() {
        setConfigurationSettingRunner((expected, update) ->
            StepVerifier.create(client.setConfigurationSettingWithResponse(expected, false))
                    .assertNext(response -> assertConfigurationEquals(expected, response))
                    .verifyComplete());
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
    @Test
    public void setConfigurationSettingEmptyKey() {
        StepVerifier.create(client.setConfigurationSetting("", NO_LABEL, "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @Test
    public void setConfigurationSettingEmptyValue() {
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
    @Test
    public void setConfigurationSettingNullKey() {

        StepVerifier.create(client.setConfigurationSetting(null, NO_LABEL, "A Value"))
            .verifyError(IllegalArgumentException.class);
        StepVerifier.create(client.setConfigurationSettingWithResponse(null, false))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is read-only.
     */
    @Test
    public void getConfigurationSetting() {
        getConfigurationSettingRunner((expected) ->
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected).then(client.getConfigurationSettingWithResponse(expected, null, false)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @Test
    public void getConfigurationSettingNotFound() {
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
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    @Test
    public void deleteConfigurationSetting() {
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

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @Test
    public void deleteConfigurationSettingNotFound() {
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
     * Tests that when an ETag is passed to delete it will only delete if the current representation of the setting has the ETag.
     * If the delete ETag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @Test
    public void deleteConfigurationSettingWithETag() {
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
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    @Test
    public void deleteConfigurationSettingNullKey() {
        StepVerifier.create(client.deleteConfigurationSetting(null, null))
            .verifyError(IllegalArgumentException.class);
        StepVerifier.create(client.deleteConfigurationSettingWithResponse(null, false))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests assert that the setting can not be deleted after set the setting to read-only.
     */
    @Test
    public void setReadOnly() {

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
        });
    }

    /**
     * Tests assert that the setting can be deleted after clear read-only of the setting.
     */
    @Test
    public void clearReadOnly() {

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
     * Tests assert that the setting can not be deleted after set the setting to read-only.
     */
    @Test
    public void setReadOnlyWithConfigurationSetting() {
        lockUnlockRunner((expected) -> {
            StepVerifier.create(client.addConfigurationSettingWithResponse(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // read-only setting
            StepVerifier.create(client.setReadOnlyWithResponse(expected, true))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            // unsuccessfully delete
            StepVerifier.create(client.deleteConfigurationSettingWithResponse(expected, false))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, 409));
        });
    }

    /**
     * Tests assert that the setting can be deleted after clear read-only of the setting.
     */
    @Test
    public void clearReadOnlyWithConfigurationSetting() {
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

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */
    @Test
    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = testResourceNamer.randomName(keyPrefix, 16);
        final String label = testResourceNamer.randomName("lbl", 8);
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue(value).setLabel(label);

        StepVerifier.create(client.setConfigurationSettingWithResponse(expected, false))
            .assertNext(response -> assertConfigurationEquals(expected, response))
            .verifyComplete();

        StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeys(key).setLabels(label)))
            .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
            .verifyComplete();

        StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeys(key)))
            .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
            .verifyComplete();
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
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeys(key, key2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
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
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addConfigurationSettingWithResponse(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listConfigurationSettings(new SettingSelector().setKeys(key).setLabels(label, label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    @Test
    public void listConfigurationSettingsSelectFields() {
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
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @Test
    public void listConfigurationSettingsAcceptDateTime() {
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
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        StepVerifier.create(client.listConfigurationSettings(options))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @Test
    public void listRevisions() {
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
        StepVerifier.create(client.listRevisions(new SettingSelector().setKeys(keyName)))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();

        // Verifies that we can select specific fields.
        StepVerifier.create(client.listRevisions(new SettingSelector().setKeys(keyName).setFields(SettingFields.KEY, SettingFields.ETAG)))
                .assertNext(response -> validateListRevisions(updated2, response))
                .assertNext(response -> validateListRevisions(updated, response))
                .assertNext(response -> validateListRevisions(original, response))
                .verifyComplete();
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @Test
    public void listRevisionsWithMultipleKeys() {
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

            StepVerifier.create(client.listRevisions(new SettingSelector().setKeys(key, key2)))
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
    @Test
    public void listRevisionsWithMultipleLabels() {
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

            StepVerifier.create(client.listRevisions(new SettingSelector().setKeys(key).setLabels(label, label2)))
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
    @Test
    public void listRevisionsAcceptDateTime() {
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
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        StepVerifier.create(client.listRevisions(options))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listRevisionsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix));
        }

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listRevisions(filter))
            .expectNextCount(numberExpected)
            .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination and stream is invoked multiple times.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listRevisionsWithPaginationAndRepeatStream() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (int value = 0; value < numberExpected; value++) {
            ConfigurationSetting setting = new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix);
            settings.add(setting);
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        Flux.merge(results).blockLast();

        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        PagedFlux<ConfigurationSetting> configurationSettingPagedFlux = client.listRevisions(filter);
        configurationSettingPagedFlux.toStream().forEach(configurationSetting -> configurationSettingList1.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedFlux.toStream().forEach(configurationSetting -> configurationSettingList2.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList2.size());
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination and stream is invoked multiple times.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listRevisionsWithPaginationAndRepeatIterator() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (int value = 0; value < numberExpected; value++) {
            ConfigurationSetting setting = new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix);
            settings.add(setting);
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        Flux.merge(results).blockLast();

        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        PagedFlux<ConfigurationSetting> configurationSettingPagedFlux = client.listRevisions(filter);
        configurationSettingPagedFlux.toIterable().forEach(configurationSetting -> configurationSettingList1.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedFlux.toIterable().forEach(configurationSetting -> configurationSettingList2.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList2.size());
    }
    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.
     */
    @Test
    public void listConfigurationSettingsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix));
        }

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setConfigurationSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix + "-*").setLabels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listConfigurationSettings(filter))
            .expectNextCount(numberExpected)
            .verifyComplete();
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

    @Test
    @Disabled
    public void deleteAllSettings() {
        client.listConfigurationSettings(new SettingSelector().setKeys("*"))
            .flatMap(configurationSetting -> {
                logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
                return client.deleteConfigurationSettingWithResponse(configurationSetting, false);
            }).blockLast();
    }
}

