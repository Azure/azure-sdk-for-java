// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.http.HttpClient;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.rest.Response;
import com.azure.common.test.TestBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationAsyncClientTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationAsyncClientTest.class);

    private ConfigurationAsyncClient client;
    private String keyPrefix;
    private String labelPrefix;

    @Rule
    public TestName testName = new TestName();

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        final String connectionString = interceptorManager.isPlaybackMode()
            ? "Endpoint=http://localhost:8080;Id=0000000000000;Secret=MDAwMDAw"
            : System.getenv("AZCONFIG_CONNECTION_STRING");

        Objects.requireNonNull(connectionString, "AZCONFIG_CONNECTION_STRING expected to be set.");

        final ConfigurationClientCredentials credentials;
        try {
            credentials = new ConfigurationClientCredentials(connectionString);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            logger.error("Could not create an configuration client credentials.", e);
            fail();
            return;
        }

        if (interceptorManager.isPlaybackMode()) {
            client = ConfigurationAsyncClient.builder()
                    .credentials(credentials)
                    .httpClient(interceptorManager.getPlaybackClient())
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .build();
        } else {
            client = ConfigurationAsyncClient.builder()
                    .credentials(credentials)
                    .httpClient(HttpClient.createDefault().wiretap(true))
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .build();
        }

        keyPrefix = sdkContext.randomResourceName("key", 8);
        labelPrefix = sdkContext.randomResourceName("label", 8);
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listSettings(new SettingSelector().keys(keyPrefix + "*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());
                    return client.deleteSetting(configurationSetting).retryBackoff(3, Duration.ofSeconds(10));
                })
                .blockLast();

        logger.info("Finished cleaning up values.");
    }

    String getKey() {
        return sdkContext.randomResourceName(keyPrefix, ConfigurationClientTestBase.RESOURCE_LENGTH);
    }

    String getLabel() {
        return sdkContext.randomResourceName(labelPrefix, ConfigurationClientTestBase.RESOURCE_LENGTH);
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    @Test
    public void addSetting() {
        ConfigurationClientTestBase.addSetting(getKey(), getLabel(), (expected) ->
            StepVerifier.create(client.addSetting(expected))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @Test
    public void addSettingEmptyKey() {
        StepVerifier.create(client.addSetting("", "A value"))
            .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.METHOD_NOT_ALLOWED.code()));
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @Test
    public void addSettingEmptyValue() {
        ConfigurationClientTestBase.addSettingEmptyValue(getKey(), (setting) -> {
            StepVerifier.create(client.addSetting(setting.key(), setting.value()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(setting.key()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void addSettingNullKey() {
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.addSetting(null, "A Value"), IllegalArgumentException.class);
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.addSetting(null), NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @Test
    public void addExistingSetting() {
        ConfigurationClientTestBase.addExistingSetting(getKey(), getLabel(), (expected) ->
            StepVerifier.create(client.addSetting(expected).then(client.addSetting(expected)))
                .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code())));
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public void setSetting() {
        ConfigurationClientTestBase.setSetting(getKey(), getLabel(), (expected, update) ->
            StepVerifier.create(client.setSetting(expected))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(expected, response))
                    .verifyComplete());
    }

    /**
     * Tests that when an etag is passed to set it will only set if the current representation of the setting has the
     * etag. If the set etag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    @Test
    public void setSettingIfEtag() {
        ConfigurationClientTestBase.setSettingIfEtag(getKey(), getLabel(), (initial, update) -> {
            // This etag is not the correct format. It is not the correct hash that the service is expecting.
            StepVerifier.create(client.setSetting(initial.etag("badEtag")))
                    .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code()));

            final String etag = client.addSetting(initial).block().value().etag();

            StepVerifier.create(client.setSetting(update.etag(etag)))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.setSetting(initial))
                    .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code()));

            StepVerifier.create(client.getSetting(update))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(update, response))
                    .verifyComplete();
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    @Test
    public void setSettingEmptyKey() {
        StepVerifier.create(client.setSetting("", "A value"))
            .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.METHOD_NOT_ALLOWED.code()));
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @Test
    public void setSettingEmptyValue() {
        ConfigurationClientTestBase.setSettingEmptyValue(getKey(), (setting) -> {
            StepVerifier.create(client.setSetting(setting.key(), setting.value()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.getSetting(setting.key()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void setSettingNullKey() {
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.setSetting(null, "A Value"), IllegalArgumentException.class);
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.setSetting(null), NullPointerException.class);
    }

    /**
     * Tests that update cannot be done to a non-existent configuration, this will result in a 412.
     * Unlike set update isn't able to create the configuration.
     */
    @Test
    public void updateNoExistingSetting() {
        ConfigurationClientTestBase.updateNoExistingSetting(getKey(), getLabel(), (expected) ->
            StepVerifier.create(client.updateSetting(expected))
                    .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code())));
    }

    /**
     * Tests that a configuration is able to be updated when it exists.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public void updateSetting() {
        ConfigurationClientTestBase.updateSetting(getKey(), getLabel(), (initial, update) ->
            StepVerifier.create(client.addSetting(initial))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(initial, response))
                    .verifyComplete());
    }

    /**
     * Tests that a configuration is able to be updated when it exists with the convenience overload.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public void updateSettingOverload() {
        ConfigurationClientTestBase.updateSettingOverload(getKey(), (original, updated) -> {
            StepVerifier.create(client.addSetting(original.key(), original.value()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(original, response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(updated.key(), updated.value()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void updateSettingNullKey() {
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.updateSetting(null, "A Value"), IllegalArgumentException.class);
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.updateSetting(null), NullPointerException.class);
    }

    /**
     * Tests that when an etag is passed to update it will only update if the current representation of the setting has the etag.
     * If the update etag doesn't match anything the update won't happen, this will result in a 412.
     */
    @Test
    public void updateSettingIfEtag() {
        final String key = sdkContext.randomResourceName(keyPrefix, 16);
        final String label = sdkContext.randomResourceName(labelPrefix, 16);
        final ConfigurationSetting newConfiguration = new ConfigurationSetting().key(key).value("myNewValue");
        final ConfigurationSetting updateConfiguration = new ConfigurationSetting().key(key).value("myUpdateValue");
        final ConfigurationSetting finalConfiguration = new ConfigurationSetting().key(key).value("myFinalValue");

        updateSettingIfEtagHelper(newConfiguration, updateConfiguration, finalConfiguration);
        updateSettingIfEtagHelper(newConfiguration.label(label), updateConfiguration.label(label), finalConfiguration.label(label));
    }

    private void updateSettingIfEtagHelper(ConfigurationSetting initial, ConfigurationSetting update, ConfigurationSetting last) {
        final String initialEtag = client.addSetting(initial).block().value().etag();
        final String updateEtag = client.updateSetting(update).block().value().etag();

        // The setting does not exist in the service yet, so we cannot update it.
        StepVerifier.create(client.updateSetting(new ConfigurationSetting(last).etag(initialEtag)))
                .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code()));

        StepVerifier.create(client.getSetting(update))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(update, response))
                .verifyComplete();

        StepVerifier.create(client.updateSetting(new ConfigurationSetting(last).etag(updateEtag)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(last, response))
                .verifyComplete();

        StepVerifier.create(client.getSetting(last))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(last, response))
                .verifyComplete();

        StepVerifier.create(client.updateSetting(new ConfigurationSetting(initial).etag(updateEtag)))
                .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code()));
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is locked.
     */
    @Test
    public void getSetting() {
        ConfigurationClientTestBase.getSetting(getKey(), (expected) ->
            StepVerifier.create(client.addSetting(expected).then(client.getSetting(expected)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @Test
    public void getSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().key(key).value("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().key(key).label("myNonExistentLabel");

        StepVerifier.create(client.addSetting(neverRetrievedConfiguration))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(neverRetrievedConfiguration, response))
                .verifyComplete();

        StepVerifier.create(client.getSetting("myNonExistentKey"))
                .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.NOT_FOUND.code()));


        StepVerifier.create(client.getSetting(nonExistentLabel))
                .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.NOT_FOUND.code()));
    }

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    @Test
    public void deleteSetting() {
        ConfigurationClientTestBase.deleteSetting(getKey(), getLabel(), (expected) -> {
            StepVerifier.create(client.addSetting(expected).then(client.getSetting(expected)))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(expected, response))
                    .verifyComplete();

            StepVerifier.create(client.deleteSetting(expected))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(expected, response))
                    .verifyComplete();

            StepVerifier.create(client.getSetting(expected))
                    .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.NOT_FOUND.code()));
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @Test
    public void deleteSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguation = new ConfigurationSetting().key(key).value("myNeverDeletedValue");

        StepVerifier.create(client.addSetting(neverDeletedConfiguation))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(neverDeletedConfiguation, response))
                .verifyComplete();

        StepVerifier.create(client.deleteSetting("myNonExistentKey"))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(null, response, HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

        StepVerifier.create(client.deleteSetting(new ConfigurationSetting().key(neverDeletedConfiguation.key()).label("myNonExistentLabel")))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(null, response, HttpResponseStatus.NO_CONTENT.code()))
                .verifyComplete();

        StepVerifier.create(client.getSetting(neverDeletedConfiguation.key()))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(neverDeletedConfiguation, response))
                .verifyComplete();
    }

    /**
     * Tests that when an etag is passed to delete it will only delete if the current representation of the setting has the etag.
     * If the delete etag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @Test
    public void deleteSettingWithETag() {
        ConfigurationClientTestBase.deleteSettingWithETag(getKey(), getLabel(), (initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addSetting(initial).block().value();
            final ConfigurationSetting updatedConfig = client.updateSetting(update).block().value();

            StepVerifier.create(client.getSetting(initial))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.deleteSetting(initiallyAddedConfig))
                    .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.PRECONDITION_FAILED.code()));

            StepVerifier.create(client.deleteSetting(updatedConfig))
                    .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(update, response))
                    .verifyComplete();

            StepVerifier.create(client.getSetting(initial))
                    .verifyErrorSatisfies(ex -> ConfigurationClientTestBase.assertRestException(ex, HttpResponseStatus.NOT_FOUND.code()));
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    @Test
    public void deleteSettingNullKey() {
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.deleteSetting((String) null), IllegalArgumentException.class);
        ConfigurationClientTestBase.assertRunnableThrowsException(() -> client.deleteSetting((ConfigurationSetting) null), NullPointerException.class);
    }

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */
    @Test
    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = sdkContext.randomResourceName(keyPrefix, 16);
        final String label = sdkContext.randomResourceName("lbl", 8);
        final ConfigurationSetting expected = new ConfigurationSetting().key(key).value(value).label(label);

        StepVerifier.create(client.setSetting(expected))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(expected, response))
                .verifyComplete();

        StepVerifier.create(client.listSettings(new SettingSelector().keys(key).labels(label)))
                .assertNext(configurationSetting -> ConfigurationClientTestBase.assertConfigurationEquals(expected, configurationSetting))
                .verifyComplete();

        StepVerifier.create(client.listSettings(new SettingSelector().keys(key)))
                .assertNext(configurationSetting -> ConfigurationClientTestBase.assertConfigurationEquals(expected, configurationSetting))
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

        ConfigurationClientTestBase.listWithMultipleKeys(key, key2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(setting))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(setting2))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listSettings(new SettingSelector().keys(key, key2)))
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

        ConfigurationClientTestBase.listWithMultipleLabels(key, label, label2, (setting, setting2) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(setting))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting, response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(setting2))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(setting2, response))
                .verifyComplete();

            StepVerifier.create(client.listSettings(new SettingSelector().keys(key).labels(label, label2)))
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
    public void listSettingsSelectFields() {
        final String label = "my-first-mylabel";
        final String label2 = "my-second-mylabel";
        final int numberToCreate = 8;
        final Map<String, String> tags = new HashMap<>();
        tags.put("tag1", "value1");
        tags.put("tag2", "value2");

        final SettingSelector secondLabelOptions = new SettingSelector()
                .labels("*-second*")
                .keys(keyPrefix + "-fetch-*")
                .fields(SettingFields.KEY, SettingFields.ETAG, SettingFields.CONTENT_TYPE, SettingFields.TAGS);
        final List<ConfigurationSetting> settings = IntStream.range(0, numberToCreate)
                .mapToObj(value -> {
                    String key = value % 2 == 0 ? keyPrefix + "-" + value : keyPrefix + "-fetch-" + value;
                    String lbl = value / 4 == 0 ? label : label2;
                    return new ConfigurationSetting().key(key).value("myValue2").label(lbl).tags(tags);
                })
                .collect(Collectors.toList());

        final List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setSetting(setting));
        }

        // Waiting for all the settings to be added.
        Flux.merge(results).blockLast();

        StepVerifier.create(client.listSettings(secondLabelOptions))
                .assertNext(setting -> {
                    // These are the fields we chose in our filter.
                    assertNotNull(setting.etag());
                    assertNotNull(setting.key());
                    assertTrue(setting.key().contains(keyPrefix));
                    assertNotNull(setting.tags());
                    assertEquals(tags.size(), setting.tags().size());

                    assertNull(setting.lastModified());
                    assertNull(setting.contentType());
                    assertNull(setting.label());
                })
                .assertNext(setting -> {
                    // These are the fields we chose in our filter.
                    assertNotNull(setting.etag());
                    assertNotNull(setting.key());
                    assertTrue(setting.key().contains(keyPrefix));
                    assertNotNull(setting.tags());
                    assertEquals(tags.size(), setting.tags().size());

                    assertNull(setting.lastModified());
                    assertNull(setting.contentType());
                    assertNull(setting.label());
                })
                .verifyComplete();
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @Test
    public void listSettingsAcceptDateTime() {
        final String keyName = sdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().key(keyName).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting(original).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting(original).value("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSetting(original).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated2))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().keys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().keys(keyName).acceptDatetime(revisions.get(1).lastModified());
        StepVerifier.create(client.listSettings(options))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .verifyComplete();
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @Test
    public void listRevisions() {
        final String keyName = sdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().key(keyName).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting(original).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting(original).value("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSetting(original))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated2))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Get all revisions for a key, they are listed in descending order.
        StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(keyName)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated2, response))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(original, response))
                .verifyComplete();

        // Verifies that we can select specific fields.
        StepVerifier.create(client.listSettingRevisions(new SettingSelector().keys(keyName).fields(SettingFields.KEY, SettingFields.ETAG)))
                .assertNext(response -> {
                    assertEquals(updated2.key(), response.key());
                    assertNotNull(response.etag());
                    assertNull(response.value());
                    assertNull(response.lastModified());
                })
                .assertNext(response -> {
                    assertEquals(updated.key(), response.key());
                    assertNotNull(response.etag());
                    assertNull(response.value());
                    assertNull(response.lastModified());
                })
                .assertNext(response -> {
                    assertEquals(original.key(), response.key());
                    assertNotNull(response.etag());
                    assertNull(response.value());
                    assertNull(response.lastModified());
                })
                .verifyComplete();
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @Test
    public void listRevisionsWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        ConfigurationClientTestBase.listRevisionsWithMultipleKeys(key, key2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(testInput.get(0)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(1)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(testInput.get(2)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(3)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(3), response))
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

    /**
     * Verifies that we can get all revisions for all settings with the specified labels.
     */
    @Test
    public void listRevisionsWithMultipleLabels() {
        String key = getKey();
        String label = getLabel();
        String label2 = getLabel();

        ConfigurationClientTestBase.listRevisionsWithMultipleLabels(key, label, label2, (testInput) -> {
            List<ConfigurationSetting> selected = new ArrayList<>();

            StepVerifier.create(client.addSetting(testInput.get(0)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(0), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(1)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(1), response))
                .verifyComplete();

            StepVerifier.create(client.addSetting(testInput.get(2)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(2), response))
                .verifyComplete();

            StepVerifier.create(client.updateSetting(testInput.get(3)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(testInput.get(3), response))
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

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    @Test
    public void listRevisionsAcceptDateTime() {
        final String keyName = sdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting original = new ConfigurationSetting().key(keyName).value("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting(original).value("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting(original).value("anotherValue2");

        // Create 3 revisions of the same key.
        StepVerifier.create(client.setSetting(original).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(original, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated).delayElement(Duration.ofSeconds(2)))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .verifyComplete();
        StepVerifier.create(client.setSetting(updated2))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated2, response))
                .verifyComplete();

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().keys(keyName)).collectList().block();

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().keys(keyName).acceptDatetime(revisions.get(1).lastModified());
        StepVerifier.create(client.listSettingRevisions(options))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(updated, response))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(original, response))
                .verifyComplete();
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     *
     * TODO (conniey): Remove the manual retry when issue is fixed: https://github.com/azure/azure-sdk-for-java/issues/3183
     */
    @Test
    public void listRevisionsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = IntStream.range(0, numberExpected)
                .mapToObj(value -> new ConfigurationSetting()
                        .key(keyPrefix)
                        .value("myValue" + value)
                        .label(labelPrefix))
                .collect(Collectors.toList());

        for (ConfigurationSetting setting : settings) {
            client.setSetting(setting).retryBackoff(3, Duration.ofSeconds(30)).block();
        }

        SettingSelector filter = new SettingSelector().keys(keyPrefix).labels(labelPrefix);
        StepVerifier.create(client.listSettingRevisions(filter))
                .expectNextCount(numberExpected)
                .verifyComplete();
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     *
     * TODO (conniey): Remove the manual retry when issue is fixed: https://github.com/azure/azure-sdk-for-java/issues/3183
     */
    @Test
    public void listSettingsWithPagination() {
        final int numberExpected = 50;
        List<ConfigurationSetting> settings = IntStream.range(0, numberExpected)
                .mapToObj(value -> new ConfigurationSetting()
                        .key(keyPrefix + "-" + value)
                        .value("myValue")
                        .label(labelPrefix))
                .collect(Collectors.toList());

        List<Mono<Response<ConfigurationSetting>>> results = new ArrayList<>();
        for (ConfigurationSetting setting : settings) {
            results.add(client.setSetting(setting).retryBackoff(3, Duration.ofSeconds(30)));
        }

        SettingSelector filter = new SettingSelector().keys(keyPrefix + "-*").labels(labelPrefix);

        Flux.merge(results).blockLast();
        StepVerifier.create(client.listSettings(filter))
                .expectNextCount(numberExpected)
                .verifyComplete();
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the etag provided does not match the one of the current setting.
     */
    @Ignore("Getting a configuration setting only when the value has changed is not a common scenario.")
    @Test
    public void getSettingWhenValueNotUpdated() {
        final String key = sdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting expected = new ConfigurationSetting().key(key).value("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().key(key).value("myNewValue");
        final Response<ConfigurationSetting> block = client.addSetting(expected).single().block();

        assertNotNull(block);
        ConfigurationClientTestBase.assertConfigurationEquals(expected, block);

        StepVerifier.create(client.setSetting(newExpected))
                .assertNext(response -> ConfigurationClientTestBase.assertConfigurationEquals(newExpected, response))
                .verifyComplete();
    }

    @Ignore("This test exists to clean up resources missed due to 429s.")
    @Test
    public void deleteAllSettings() {
        client.listSettings(new SettingSelector().keys("*"))
                .flatMap(configurationSetting -> {
                    logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());
                    return client.deleteSetting(configurationSetting);
                }).blockLast();
    }
}
