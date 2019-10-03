// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.Range;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigurationAsyncClientTest extends ConfigurationClientTestBase {
    private final ClientLogger logger = new ClientLogger(ConfigurationAsyncClientTest.class);
    private static final String NO_LABEL = null;
    private ConfigurationAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                    .credential(credentials)
                    .httpClient(interceptorManager.getPlaybackClient())
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .buildAsyncClient());
        } else {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                    .credential(credentials)
                    .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .addPolicy(new RetryPolicy())
                    .buildAsyncClient());
        }
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listSettings(new SettingSelector().setKeys(keyPrefix + "*"))
                .flatMap(configurationSetting -> {
                    Mono<Response<ConfigurationSetting>> unlock;
                    if (configurationSetting.isLocked()) {
                        unlock = client.clearReadOnlyWithResponse(configurationSetting);
                    } else {
                        unlock = Mono.empty();
                    }
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isLocked());
                    return unlock.then(client.deleteSettingWithResponse(configurationSetting, false));
                })
                .blockLast();

        logger.info("Finished cleaning up values.");
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    public void addSetting() {
        addSettingRunner((expected) ->
            StepVerifier.create(client.addSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    public void addSettingEmptyKey() {
        StepVerifier.create(client.addSetting("", "A value", null))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    public void addSettingEmptyValue() {
        addSettingEmptyValueRunner((setting) -> {
            StepVerifier.create(client.addSetting(setting.getKey(), setting.getValue(), setting.getLabel()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(setting.getKey()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    public void addSettingNullKey() {
        assertRunnableThrowsException(() -> client.addSetting(null, "A Value", null).block(), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addSetting(null).block(), NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    public void addExistingSetting() {
        addExistingSettingRunner((expected) ->
            StepVerifier.create(client.addSetting(expected).then(client.addSetting(expected)))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_PRECON_FAILED)));
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    public void setSetting() {
        setSettingRunner((expected, update) ->
            StepVerifier.create(client.setSettingWithResponse(expected, false))
                    .assertNext(response -> assertConfigurationEquals(expected, response))
                    .verifyComplete());
    }

    /**
     * Tests that when an etag is passed to set it will only set if the current representation of the setting has the
     * etag. If the set etag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    public void setSettingIfEtag() {
        setSettingIfEtagRunner((initial, update) -> {
            // This etag is not the correct format. It is not the correct hash that the service is expecting.
            StepVerifier.create(client.setSettingWithResponse(initial.setETag("badEtag"), true))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_PRECON_FAILED));

            final String etag = client.addSetting(initial).block().getETag();

            StepVerifier.create(client.setSettingWithResponse(update.setETag(etag), true))
                    .assertNext(response -> assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.setSettingWithResponse(initial, true))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_PRECON_FAILED));

            StepVerifier.create(client.getSetting(update))
                    .assertNext(response -> assertConfigurationEquals(update, response))
                    .verifyComplete();
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    public void setSettingEmptyKey() {
        StepVerifier.create(client.setSetting("", NO_LABEL, "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    public void setSettingEmptyValue() {
        setSettingEmptyValueRunner((setting) -> {
            StepVerifier.create(client.setSetting(setting.getKey(), NO_LABEL, setting.getValue()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(setting.getKey()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    public void setSettingNullKey() {
        assertRunnableThrowsException(() -> client.setSetting(null, NO_LABEL, "A Value").block(), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.setSettingWithResponse(null, false).block(), NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is locked.
     */
    public void getSetting() {
        getSettingRunner((expected) ->
            StepVerifier.create(client.addSetting(expected).then(client.getSetting(expected)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    public void getSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().setKey(key).setValue("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        StepVerifier.create(client.addSetting(neverRetrievedConfiguration))
            .assertNext(response -> assertConfigurationEquals(neverRetrievedConfiguration, response))
            .verifyComplete();

        StepVerifier.create(client.getSetting("myNonExistentKey"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));


        StepVerifier.create(client.getSetting(nonExistentLabel))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    public void deleteSetting() {
        deleteSettingRunner((expected) -> {
            StepVerifier.create(client.addSetting(expected).then(client.getSetting(expected)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            StepVerifier.create(client.deleteSettingWithResponse(expected, false))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(expected))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    public void deleteSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguration = new ConfigurationSetting().setKey(key).setValue("myNeverDeletedValue");

        StepVerifier.create(client.addSetting(neverDeletedConfiguration))
            .assertNext(response -> assertConfigurationEquals(neverDeletedConfiguration, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(new ConfigurationSetting().setKey("myNonExistentKey"), false))
            .assertNext(response -> assertConfigurationEquals(null, response, HttpURLConnection.HTTP_NO_CONTENT))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(new ConfigurationSetting().setKey(neverDeletedConfiguration.getKey()).setLabel("myNonExistentLabel"), false))
            .assertNext(response -> assertConfigurationEquals(null, response, HttpURLConnection.HTTP_NO_CONTENT))
            .verifyComplete();

        StepVerifier.create(client.getSetting(neverDeletedConfiguration.getKey()))
            .assertNext(response -> assertConfigurationEquals(neverDeletedConfiguration, response))
            .verifyComplete();
    }

    /**
     * Tests that when an etag is passed to delete it will only delete if the current representation of the setting has the etag.
     * If the delete etag doesn't match anything the delete won't happen, this will result in a 412.
     */
    public void deleteSettingWithETag() {
        deleteSettingWithETagRunner((initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addSetting(initial).block();
            final ConfigurationSetting updatedConfig = client.setSettingWithResponse(update, true).block().getValue();

            StepVerifier.create(client.getSetting(initial))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();

            StepVerifier.create(client.deleteSettingWithResponse(initiallyAddedConfig, true))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_PRECON_FAILED));

            StepVerifier.create(client.deleteSettingWithResponse(updatedConfig, true))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(initial))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    public void deleteSettingNullKey() {
        assertRunnableThrowsException(() -> client.deleteSetting(null, null).block(), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.deleteSettingWithResponse(null, false).block(), NullPointerException.class);
    }

    /**
     * Tests assert that the setting can not be deleted after lock the setting.
     */
    public void setReadOnly() {
        final String key = getKey();
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("myValue");

        StepVerifier.create(client.addSetting(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.setReadOnly(setting.getKey(), setting.getLabel()))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.getSetting(setting.getKey()))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(setting, false))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, 409));
    }

    /**
     * Tests assert that the setting can be deleted after unlock the setting.
     */
    public void clearReadOnly() {
        final String key = getKey();
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("myValue");

        StepVerifier.create(client.addSetting(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.setReadOnly(setting.getKey(), setting.getLabel()))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(setting, false))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, 409));

        StepVerifier.create(client.clearReadOnly(setting.getKey(), setting.getLabel()))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(setting, false))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();
    }

    /**
     * Tests assert that the setting can not be deleted after lock the setting.
     */
    public void setReadOnlyWithConfigurationSetting() {
        final String key = getKey();
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("myValue");

        StepVerifier.create(client.addSetting(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.setReadOnlyWithResponse(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.getSetting(setting.getKey()))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(setting, false))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, 409));
    }

    /**
     * Tests assert that the setting can be deleted after unlock the setting.
     */
    public void clearReadOnlyWithConfigurationSetting() {
        final String key = getKey();
        final ConfigurationSetting setting = new ConfigurationSetting().setKey(key).setValue("myValue");

        StepVerifier.create(client.addSetting(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.setReadOnlyWithResponse(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(setting, false))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, 409));

        StepVerifier.create(client.clearReadOnlyWithResponse(setting))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();

        StepVerifier.create(client.deleteSettingWithResponse(setting, false))
            .assertNext(response -> assertConfigurationEquals(setting, response))
            .verifyComplete();
    }

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */
    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = testResourceNamer.randomName(keyPrefix, 16);
        final String label = testResourceNamer.randomName("lbl", 8);
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue(value).setLabel(label);

        StepVerifier.create(client.setSettingWithResponse(expected, false))
            .assertNext(response -> assertConfigurationEquals(expected, response))
            .verifyComplete();

        StepVerifier.create(client.listSettings(new SettingSelector().setKeys(key).setLabels(label)))
            .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
            .verifyComplete();

        StepVerifier.create(client.listSettings(new SettingSelector().setKeys(key)))
            .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
            .verifyComplete();
    }

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    public void listWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listSettings(new SettingSelector().setKeys(key, key2)))
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
    public void listWithMultipleLabels() {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listWithMultipleLabelsRunner(key, label, label2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(setting))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(setting2))
                .assertNext(response -> assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listSettings(new SettingSelector().setKeys(key).setLabels(label, label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    public void listSettingsSelectFields() {
        listSettingsSelectFieldsRunner((settings, selector) -> {
            final List<Mono<Response<ConfigurationSetting>>> settingsBeingAdded = new ArrayList<>();
            for (ConfigurationSetting setting : settings) {
                settingsBeingAdded.add(client.setSettingWithResponse(setting, false));
            }

            // Waiting for all the settings to be added.
            Flux.merge(settingsBeingAdded).blockLast();

            List<ConfigurationSetting> settingsReturned = new ArrayList<>();
            StepVerifier.create(client.listSettings(selector))
                .assertNext(settingsReturned::add)
                .assertNext(settingsReturned::add)
                .verifyComplete();

            return settingsReturned;
        });
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    public void listSettingsAcceptDateTime() {
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSettingWithResponse(original, false))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSettingWithResponse(updated, false).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSettingWithResponse(updated2, false))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().setKeys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        StepVerifier.create(client.listSettings(options))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    public void listRevisions() {
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSettingWithResponse(original, false))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSettingWithResponse(updated, false))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSettingWithResponse(updated2, false))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Get all revisions for a key, they are listed in descending order.
        StepVerifier.create(client.listSettingRevisions(new SettingSelector().setKeys(keyName)))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();

        // Verifies that we can select specific fields.
        StepVerifier.create(client.listSettingRevisions(new SettingSelector().setKeys(keyName).setFields(SettingFields.KEY, SettingFields.ETAG)))
                .assertNext(response -> validateListRevisions(updated2, response))
                .assertNext(response -> validateListRevisions(updated, response))
                .assertNext(response -> validateListRevisions(original, response))
                .verifyComplete();
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    public void listRevisionsWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(testInput.get(0)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.setSettingWithResponse(testInput.get(1), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(testInput.get(2)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.setSettingWithResponse(testInput.get(3), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(3), response))
                .verifyComplete();

            StepVerifier.create(client.listSettingRevisions(new SettingSelector().setKeys(key, key2)))
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
    public void listRevisionsWithMultipleLabels() {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listRevisionsWithMultipleLabelsRunner(key, label, label2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(testInput.get(0)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.setSettingWithResponse(testInput.get(1), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(testInput.get(2)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.setSettingWithResponse(testInput.get(3), false))
                .assertNext(response -> assertConfigurationEquals(testInput.get(3), response))
                .verifyComplete();

            StepVerifier.create(client.listSettingRevisions(new SettingSelector().setKeys(key).setLabels(label, label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    /**
     * Verifies that the range header for revision selections returns the expected values.
     */
    public void listRevisionsWithRange() {
        final String key = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(key).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        StepVerifier.create(client.addSetting(original))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();

        StepVerifier.create(client.setSettingWithResponse(updated, false))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .verifyComplete();

        StepVerifier.create(client.setSettingWithResponse(updated2, false))
            .assertNext(response -> assertConfigurationEquals(updated2, response))
            .verifyComplete();

        StepVerifier.create(client.listSettingRevisions(new SettingSelector().setKeys(key).setRange(new Range(1, 2))))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();
    }

    /**
     * Verifies that an exception will be thrown from the service if it cannot satisfy the range request.
     */
    public void listRevisionsInvalidRange() {
        final String key = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(key).setValue("myValue");

        StepVerifier.create(client.addSetting(original))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();

        StepVerifier.create(client.listSettingRevisions(new SettingSelector().setKeys(key).setRange(new Range(0, 10))))
            .verifyErrorSatisfies(exception -> assertRestException(exception, 416)); // REQUESTED_RANGE_NOT_SATISFIABLE
    }

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    public void listRevisionsAcceptDateTime() {
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSettingWithResponse(original, false))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSettingWithResponse(updated, false).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSettingWithResponse(updated2, false))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().setKeys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        StepVerifier.create(client.listSettingRevisions(options))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listRevisionsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix));
        }

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listSettingRevisions(filter))
            .expectNextCount(numberExpected)
            .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination and stream is invoked multiple times.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listRevisionsWithPaginationAndRepeatStream() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (int value = 0; value < numberExpected; value++) {
            ConfigurationSetting setting = new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix);
            settings.add(setting);
            results.add(client.setSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        Flux.merge(results).blockLast();

        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        PagedFlux<ConfigurationSetting> configurationSettingPagedFlux = client.listSettingRevisions(filter);
        configurationSettingPagedFlux.toStream().forEach(configurationSetting -> configurationSettingList1.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedFlux.toStream().forEach(configurationSetting -> configurationSettingList2.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList2.size());
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination and stream is invoked multiple times.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listRevisionsWithPaginationAndRepeatIterator() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (int value = 0; value < numberExpected; value++) {
            ConfigurationSetting setting = new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix);
            settings.add(setting);
            results.add(client.setSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        Flux.merge(results).blockLast();

        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        PagedFlux<ConfigurationSetting> configurationSettingPagedFlux = client.listSettingRevisions(filter);
        configurationSettingPagedFlux.toIterable().forEach(configurationSetting -> configurationSettingList1.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedFlux.toIterable().forEach(configurationSetting -> configurationSettingList2.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList2.size());
    }
    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.
     */
    public void listSettingsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix));
        }

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setSettingWithResponse(setting, false));
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix + "-*").setLabels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listSettings(filter))
            .expectNextCount(numberExpected)
            .verifyComplete();
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the etag provided does not match the one of the current setting.
     */
    public void getSettingWhenValueNotUpdated() {
        final String key = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting block = client.addSetting(expected).single().block();

        assertNotNull(block);
        assertConfigurationEquals(expected, block);

        StepVerifier.create(client.setSettingWithResponse(newExpected, false))
            .assertNext(response -> assertConfigurationEquals(newExpected, response))
            .verifyComplete();
    }

    public void deleteAllSettings() {
        client.listSettings(new SettingSelector().setKeys("*"))
            .flatMap(configurationSetting -> {
                logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isLocked());
                return client.deleteSettingWithResponse(configurationSetting, false);
            }).blockLast();
    }
}

