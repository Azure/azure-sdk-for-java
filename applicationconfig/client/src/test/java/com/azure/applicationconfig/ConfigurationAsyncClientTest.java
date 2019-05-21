// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.Range;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigurationAsyncClientTest extends ConfigurationClientTestBase {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationAsyncClientTest.class);

    private ConfigurationAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> ConfigurationAsyncClient.builder()
                    .credentials(credentials)
                    .httpClient(interceptorManager.getPlaybackClient())
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .buildAsyncClient());
        } else {
            client = clientSetup(credentials -> ConfigurationAsyncClient.builder()
                    .credentials(credentials)
                    .httpClient(HttpClient.createDefault().wiretap(true))
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .addPolicy(new RetryPolicy())
                    .buildAsyncClient());
        }
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listSettings(new SettingSelector().keys(keyPrefix + "*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());
                    return client.deleteSetting(configurationSetting);
                })
                .blockLast();

        logger.info("Finished cleaning up values.");
    }

    @Override
    public void addSetting() {
        addSettingRunner((expected) ->
            StepVerifier.create(client.addSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @Override
    public void addSettingEmptyKey() {
        StepVerifier.create(client.addSetting("", "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseStatus.METHOD_NOT_ALLOWED.code()));
    }

    @Override
    public void addSettingEmptyValue() {
        addSettingEmptyValueRunner((setting) -> {
            StepVerifier.create(client.addSetting(setting.key(), setting.value()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(setting.key()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    @Override
    public void addSettingNullKey() {
        assertRunnableThrowsException(() -> client.addSetting(null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addSetting(null), NullPointerException.class);
    }

    @Override
    public void addExistingSetting() {
        addExistingSettingRunner((expected) ->
            StepVerifier.create(client.addSetting(expected).then(client.addSetting(expected)))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpResponseStatus.PRECONDITION_FAILED.code())));
    }

    @Override
    public void setSetting() {
        setSettingRunner((expected, update) ->
            StepVerifier.create(client.setSetting(expected))
                    .assertNext(response -> assertConfigurationEquals(expected, response))
                    .verifyComplete());
    }

    @Override
    public void setSettingIfEtag() {
        setSettingIfEtagRunner((initial, update) -> {
            // This etag is not the correct format. It is not the correct hash that the service is expecting.
            StepVerifier.create(client.setSetting(initial.etag("badEtag")))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.PRECONDITION_FAILED.code()));

            final String etag = client.addSetting(initial).block().etag();

            StepVerifier.create(client.setSetting(update.etag(etag)))
                    .assertNext(response -> assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.setSetting(initial))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.PRECONDITION_FAILED.code()));

            StepVerifier.create(client.getSetting(update))
                    .assertNext(response -> assertConfigurationEquals(update, response))
                    .verifyComplete();
        });
    }

    @Override
    public void setSettingEmptyKey() {
        StepVerifier.create(client.setSetting("", "A value"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseStatus.METHOD_NOT_ALLOWED.code()));
    }

    @Override
    public void setSettingEmptyValue() {
        setSettingEmptyValueRunner((setting) -> {
            StepVerifier.create(client.setSetting(setting.key(), setting.value()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(setting.key()))
                .assertNext(response -> assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    @Override
    public void setSettingNullKey() {
        assertRunnableThrowsException(() -> client.setSetting(null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.setSetting(null), NullPointerException.class);
    }

    @Override
    public void updateNoExistingSetting() {
        updateNoExistingSettingRunner((expected) ->
            StepVerifier.create(client.updateSetting(expected))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.PRECONDITION_FAILED.code())));
    }

    @Override
    public void updateSetting() {
        updateSettingRunner((initial, update) ->
            StepVerifier.create(client.addSetting(initial))
                    .assertNext(response -> assertConfigurationEquals(initial, response))
                    .verifyComplete());
    }

    @Override
    public void updateSettingOverload() {
        updateSettingOverloadRunner((original, updated) -> {
            StepVerifier.create(client.addSetting(original.key(), original.value()))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(updated.key(), updated.value()))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        });
    }

    @Override
    public void updateSettingNullKey() {
        assertRunnableThrowsException(() -> client.updateSetting(null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.updateSetting(null), NullPointerException.class);
    }

    @Override
    public void updateSettingIfEtag() {
        updateSettingIfEtagRunner(settings -> {
            final ConfigurationSetting initial = settings.get(0);
            final ConfigurationSetting update = settings.get(1);
            final ConfigurationSetting last = settings.get(2);

            final String initialEtag = client.addSetting(initial).block().etag();
            final String updateEtag = client.updateSetting(update).block().etag();

            // The setting does not exist in the service yet, so we cannot update it.
            StepVerifier.create(client.updateSetting(new ConfigurationSetting().key(last.key()).label(last.label()).value(last.value()).etag(initialEtag)))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.PRECONDITION_FAILED.code()));

            StepVerifier.create(client.getSetting(update))
                .assertNext(response -> assertConfigurationEquals(update, response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(new ConfigurationSetting().key(last.key()).label(last.label()).value(last.value()).etag(updateEtag)))
                .assertNext(response -> assertConfigurationEquals(last, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(last))
                .assertNext(response -> assertConfigurationEquals(last, response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(new ConfigurationSetting().key(initial.key()).label(initial.label()).value(initial.value()).etag(updateEtag)))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.PRECONDITION_FAILED.code()));
        });
    }

    @Override
    public void getSetting() {
        getSettingRunner((expected) ->
            StepVerifier.create(client.addSetting(expected).then(client.getSetting(expected)))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    @Override
    public void getSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().key(key).value("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().key(key).label("myNonExistentLabel");

        StepVerifier.create(client.addSetting(neverRetrievedConfiguration))
                .assertNext(response -> assertConfigurationEquals(neverRetrievedConfiguration, response))
                .verifyComplete();

        StepVerifier.create(client.getSetting("myNonExistentKey"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));


        StepVerifier.create(client.getSetting(nonExistentLabel))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
    }

    @Override
    public void deleteSetting() {
        deleteSettingRunner((expected) -> {
            StepVerifier.create(client.addSetting(expected).then(client.getSetting(expected)))
                    .assertNext(response -> assertConfigurationEquals(expected, response))
                    .verifyComplete();

            StepVerifier.create(client.deleteSetting(expected))
                    .assertNext(response -> assertConfigurationEquals(expected, response))
                    .verifyComplete();

            StepVerifier.create(client.getSetting(expected))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
        });
    }

    @Override
    public void deleteSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguation = new ConfigurationSetting().key(key).value("myNeverDeletedValue");

        StepVerifier.create(client.addSetting(neverDeletedConfiguation))
                .assertNext(response -> assertConfigurationEquals(neverDeletedConfiguation, response))
                .verifyComplete();

        StepVerifier.create(client.deleteSetting("myNonExistentKey"))
                .assertNext(response -> assertConfigurationEquals(ConfigurationSetting.EMPTY_SETTING, response))
                .verifyComplete();

        StepVerifier.create(client.deleteSetting(new ConfigurationSetting().key(neverDeletedConfiguation.key()).label("myNonExistentLabel")))
                .assertNext(response -> assertConfigurationEquals(ConfigurationSetting.EMPTY_SETTING, response))
                .verifyComplete();

        StepVerifier.create(client.getSetting(neverDeletedConfiguation.key()))
                .assertNext(response -> assertConfigurationEquals(neverDeletedConfiguation, response))
                .verifyComplete();
    }

    @Override
    public void deleteSettingWithETag() {
        deleteSettingWithETagRunner((initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addSetting(initial).block();
            final ConfigurationSetting updatedConfig = client.updateSetting(update).block();

            StepVerifier.create(client.getSetting(initial))
                    .assertNext(response -> assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.deleteSetting(initiallyAddedConfig))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.PRECONDITION_FAILED.code()));

            StepVerifier.create(client.deleteSetting(updatedConfig))
                    .assertNext(response -> assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.getSetting(initial))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
        });
    }

    @Override
    public void deleteSettingNullKey() {
        assertRunnableThrowsException(() -> client.deleteSetting((String) null), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.deleteSetting((ConfigurationSetting) null), NullPointerException.class);
    }

    @Override
    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = testResourceNamer.randomName(keyPrefix, 16);
        final String label = testResourceNamer.randomName("lbl", 8);
        final ConfigurationSetting expected = new ConfigurationSetting().key(key).value(value).label(label);

        StepVerifier.create(client.setSetting(expected))
                .assertNext(response -> assertConfigurationEquals(expected, response))
                .verifyComplete();

        StepVerifier.create(client.listSettings(new SettingSelector().keys(key).labels(label)))
                .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
                .verifyComplete();

        StepVerifier.create(client.listSettings(new SettingSelector().keys(key)))
                .assertNext(configurationSetting -> assertConfigurationEquals(expected, configurationSetting))
                .verifyComplete();
    }

    @Override
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

            StepVerifier.create(client.listSettings(new SettingSelector().keys(key, key2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    @Override
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

            StepVerifier.create(client.listSettings(new SettingSelector().keys(key).labels(label, label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    @Override
    public void listSettingsSelectFields() {
        listSettingsSelectFieldsRunner((settings, selector) -> {
            final List<Mono<ConfigurationSetting>> settingsBeingAdded = new ArrayList<>();
            for (ConfigurationSetting setting : settings) {
                settingsBeingAdded.add(client.setSetting(setting));
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

    @Override
    public void listSettingsAcceptDateTime() {
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().key(keyName).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().key(original.key()).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().key(original.key()).value("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSetting(original))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated2))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().keys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().keys(keyName).acceptDatetime(revisions.get(1).lastModified());
        StepVerifier.create(client.listSettings(options))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
    }

    @Override
    public void listRevisions() {
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().key(keyName).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().key(original.key()).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().key(original.key()).value("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSetting(original))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated2))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Get all revisions for a key, they are listed in descending order.
        StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(keyName)))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();

        // Verifies that we can select specific fields.
        StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(keyName).fields(SettingFields.KEY, SettingFields.ETAG)))
                .assertNext(response -> validateListRevisions(updated2, response))
                .assertNext(response -> validateListRevisions(updated, response))
                .assertNext(response -> validateListRevisions(original, response))
                .verifyComplete();
    }

    @Override
    public void listRevisionsWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(testInput.get(0)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(1)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(testInput.get(2)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(3)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(3), response))
                .verifyComplete();

            StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(key, key2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    @Override
    public void listRevisionsWithMultipleLabels() {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        listRevisionsWithMultipleLabelsRunner(key, label, label2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(testInput.get(0)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(1)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(testInput.get(2)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(3)))
                .assertNext(response -> assertConfigurationEquals(testInput.get(3), response))
                .verifyComplete();

            StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(key).labels(label, label2)))
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .consumeNextWith(selected::add)
                .verifyComplete();

            return selected;
        });
    }

    @Override
    public void listRevisionsWithRange() {
        final String key = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().key(key).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().key(original.key()).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().key(original.key()).value("anotherValue2");

        StepVerifier.create(client.addSetting(original))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();

        StepVerifier.create(client.updateSetting(updated))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .verifyComplete();

        StepVerifier.create(client.updateSetting(updated2))
            .assertNext(response -> assertConfigurationEquals(updated2, response))
            .verifyComplete();

        StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(key).range(new Range(1, 2))))
            .assertNext(response -> assertConfigurationEquals(updated, response))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();
    }

    @Override
    public void listRevisionsInvalidRange() {
        final String key = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().key(key).value("myValue");

        StepVerifier.create(client.addSetting(original))
            .assertNext(response -> assertConfigurationEquals(original, response))
            .verifyComplete();

        StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(key).range(new Range(0, 10))))
            .verifyErrorSatisfies(exception -> assertRestException(exception, HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE.code()));
    }

    @Override
    public void listRevisionsAcceptDateTime() {
        final String keyName = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().key(keyName).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().key(original.key()).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().key(original.key()).value("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSetting(original))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated2))
                .assertNext(response -> assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().keys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().keys(keyName).acceptDatetime(revisions.get(1).lastModified());
        StepVerifier.create(client.listSettingRevisions(options))
                .assertNext(response -> assertConfigurationEquals(updated, response))
                .assertNext(response -> assertConfigurationEquals(original, response))
                .verifyComplete();
    }

    @Override
    public void listRevisionsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().key(keyPrefix).value("myValue" + value).label(labelPrefix));
        }

        List<Mono<ConfigurationSetting>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setSetting(setting));
        }

        SettingSelector filter = new SettingSelector().keys(keyPrefix).labels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listSettingRevisions(filter))
                .expectNextCount(numberExpected)
                .verifyComplete();
    }

    @Override
    public void listSettingsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = new ArrayList<>(numberExpected);
        for (int value = 0; value < numberExpected; value++) {
            settings.add(new ConfigurationSetting().key(keyPrefix + "-" + value).value("myValue").label(labelPrefix));
        }

        List<Mono<ConfigurationSetting>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setSetting(setting));
        }

        SettingSelector filter = new SettingSelector().keys(keyPrefix + "-*").labels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listSettings(filter))
                .expectNextCount(numberExpected)
                .verifyComplete();
    }

    @Override
    public void getSettingWhenValueNotUpdated() {
        final String key = testResourceNamer.randomName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().key(key).value("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting block = client.addSetting(expected).single().block();

        assertNotNull(block);
        assertConfigurationEquals(expected, block);

        StepVerifier.create(client.setSetting(newExpected))
                .assertNext(response -> assertConfigurationEquals(newExpected, response))
                .verifyComplete();
    }

    @Override
    public void deleteAllSettings() {
        client.listSettings(new SettingSelector().keys("*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());
                    return client.deleteSetting(configurationSetting);
                }).blockLast();
    }
}
