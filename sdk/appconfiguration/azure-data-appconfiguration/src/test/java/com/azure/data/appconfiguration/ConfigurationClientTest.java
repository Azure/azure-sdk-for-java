// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.Range;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class ConfigurationClientTest extends ConfigurationClientTestBase {
    private final ClientLogger logger = new ClientLogger(ConfigurationClientTest.class);

    private ConfigurationClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                .credential(credentials)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildClient());
        } else {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                .credential(credentials)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .buildClient());
        }
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listSettings(new SettingSelector().setKeys(keyPrefix + "*")).forEach(configurationSetting -> {
            logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isLocked());
            client.deleteSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });

        logger.info("Finished cleaning up values.");
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    public void addSetting() {
        addSettingRunner((expected) -> assertConfigurationEquals(expected, client.addSetting(expected)));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    public void addSettingEmptyKey() {
        assertRestException(() -> client.addSetting("", "A value", null), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    public void addSettingEmptyValue() {
        addSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting, client.addSetting(setting.getKey(), setting.getValue(), setting.getLabel()));
            assertConfigurationEquals(setting, client.getSetting(setting.getKey()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    public void addSettingNullKey() {
        assertRunnableThrowsException(() -> client.addSetting(null, "A Value", null), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addSetting(null), NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    public void addExistingSetting() {
        addExistingSettingRunner((expected) -> {
            client.addSetting(expected);
            assertRestException(() -> client.addSetting(expected), ResourceModifiedException.class, HttpURLConnection.HTTP_PRECON_FAILED);
        });
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    public void setSetting() {
        setSettingRunner((expected, update) -> assertConfigurationEquals(expected, client.setSettingWithResponse(expected, false, Context.NONE).getValue()));
    }

    /**
     * Tests that when an etag is passed to set it will only set if the current representation of the setting has the
     * etag. If the set etag doesn't match anything the update won't happen, this will result in a 412. This will
     * prevent set from doing an add as well.
     */
    public void setSettingIfEtag() {
        setSettingIfEtagRunner((initial, update) -> {
            // This etag is not the correct format. It is not the correct hash that the service is expecting.
            assertRestException(() -> client.setSettingWithResponse(initial.setETag("badEtag"), true, Context.NONE).getValue(), ResourceNotFoundException.class, HttpURLConnection.HTTP_PRECON_FAILED);

            final String etag = client.addSetting(initial).getETag();

            assertConfigurationEquals(update, client.setSettingWithResponse(update.setETag(etag), true, Context.NONE));
            assertRestException(() -> client.setSettingWithResponse(initial, true, Context.NONE).getValue(), ResourceNotFoundException.class, HttpURLConnection.HTTP_PRECON_FAILED);
            assertConfigurationEquals(update, client.getSetting(update));
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    public void setSettingEmptyKey() {
        assertRestException(() -> client.setSetting("", null, "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    public void setSettingEmptyValue() {
        setSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting, client.setSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getSetting(setting.getKey()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    public void setSettingNullKey() {
        assertRunnableThrowsException(() -> client.setSetting(null, null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.setSettingWithResponse(null, false, Context.NONE).getValue(), NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is locked.
     */
    public void getSetting() {
        getSettingRunner((expected) -> {
            client.addSetting(expected);
            assertConfigurationEquals(expected, client.getSetting(expected));
        });
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    public void getSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().setKey(key).setValue("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        assertConfigurationEquals(neverRetrievedConfiguration, client.addSetting(neverRetrievedConfiguration));

        assertRestException(() -> client.getSetting("myNonExistentKey"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        assertRestException(() -> client.getSetting(nonExistentLabel), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    public void deleteSetting() {
        deleteSettingRunner((expected) -> {
            client.addSetting(expected);
            assertConfigurationEquals(expected, client.getSetting(expected));

            assertConfigurationEquals(expected, client.deleteSettingWithResponse(expected, false, Context.NONE).getValue());
            assertRestException(() -> client.getSetting(expected), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    public void deleteSettingNotFound() {
        final String key = getKey();
        final ConfigurationSetting neverDeletedConfiguation = new ConfigurationSetting().setKey(key).setValue("myNeverDeletedValue");
        final ConfigurationSetting notFoundDelete = new ConfigurationSetting().setKey(key).setLabel("myNonExistentLabel");

        assertConfigurationEquals(neverDeletedConfiguation, client.addSetting(neverDeletedConfiguation));

        assertConfigurationEquals(null, client.deleteSetting("myNonExistentKey", null));
        assertConfigurationEquals(null, client.deleteSettingWithResponse(notFoundDelete, false, Context.NONE), HttpURLConnection.HTTP_NO_CONTENT);

        assertConfigurationEquals(neverDeletedConfiguation, client.getSetting(neverDeletedConfiguation.getKey()));
    }

    /**
     * Tests that when an etag is passed to delete it will only delete if the current representation of the setting has the etag.
     * If the delete etag doesn't match anything the delete won't happen, this will result in a 412.
     */
    public void deleteSettingWithETag() {
        deleteSettingWithETagRunner((initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addSetting(initial);
            final ConfigurationSetting updatedConfig = client.setSettingWithResponse(update, false, Context.NONE).getValue();

            assertConfigurationEquals(update, client.getSetting(initial));
            assertRestException(() -> client.deleteSettingWithResponse(initiallyAddedConfig, true, Context.NONE).getValue(), ResourceNotFoundException.class, HttpURLConnection.HTTP_PRECON_FAILED);
            assertConfigurationEquals(update, client.deleteSettingWithResponse(updatedConfig, true, Context.NONE).getValue());
            assertRestException(() -> client.getSetting(initial), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    public void deleteSettingNullKey() {
        assertRunnableThrowsException(() -> client.deleteSetting(null, null), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.deleteSettingWithResponse(null, false, Context.NONE).getValue(), NullPointerException.class);
    }

    /**
     * Verifies that a ConfigurationSetting can be added with a label, and that we can fetch that ConfigurationSetting
     * from the service when filtering by either its label or just its key.
     */

    public void listWithKeyAndLabel() {
        final String value = "myValue";
        final String key = getKey();
        final String label = getLabel();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue(value).setLabel(label);

        assertConfigurationEquals(expected, client.setSettingWithResponse(expected, false, Context.NONE).getValue());
        assertConfigurationEquals(expected, client.listSettings(new SettingSelector().setKeys(key).setLabels(label)).iterator().next());
        assertConfigurationEquals(expected, client.listSettings(new SettingSelector().setKeys(key)).iterator().next());
    }

    /**
     * Verifies that ConfigurationSettings can be added and that we can fetch those ConfigurationSettings from the
     * service when filtering by their keys.
     */
    public void listWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listWithMultipleKeysRunner(key, key2, (setting, setting2) -> {
            assertConfigurationEquals(setting, client.addSetting(setting));
            assertConfigurationEquals(setting2, client.addSetting(setting2));

            return client.listSettings(new SettingSelector().setKeys(key, key2));
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
            assertConfigurationEquals(setting, client.addSetting(setting));
            assertConfigurationEquals(setting2, client.addSetting(setting2));

            return client.listSettings(new SettingSelector().setKeys(key).setLabels(label, label2));
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    public void listSettingsSelectFields() {
        listSettingsSelectFieldsRunner((settings, selector) -> {
            settings.forEach(setting -> client.setSettingWithResponse(setting, false, Context.NONE).getValue());
            return client.listSettings(selector);
        });
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    public void listSettingsAcceptDateTime() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        try {
            assertConfigurationEquals(original, client.setSettingWithResponse(original, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated, client.setSettingWithResponse(updated, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated2, client.setSettingWithResponse(updated2, false, Context.NONE).getValue());
        } catch (InterruptedException ex) {
            // Do nothing.
        }

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().setKeys(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        assertConfigurationEquals(updated, (client.listSettings(options).stream().collect(Collectors.toList())).get(0));
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    public void listRevisions() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        assertConfigurationEquals(original, client.setSettingWithResponse(original, false, Context.NONE).getValue());
        assertConfigurationEquals(updated, client.setSettingWithResponse(updated, false, Context.NONE).getValue());
        assertConfigurationEquals(updated2, client.setSettingWithResponse(updated2, false, Context.NONE).getValue());

        // Get all revisions for a key, they are listed in descending order.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().setKeys(keyName)).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated2, revisions.get(0));
        assertConfigurationEquals(updated, revisions.get(1));
        assertConfigurationEquals(original, revisions.get(2));

        // Verifies that we can select specific fields.
        revisions = client.listSettingRevisions(new SettingSelector().setKeys(keyName).setFields(SettingFields.KEY, SettingFields.ETAG)).stream().collect(Collectors.toList());
        validateListRevisions(updated2, revisions.get(0));
        validateListRevisions(updated, revisions.get(1));
        validateListRevisions(original, revisions.get(2));
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    public void listRevisionsWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            assertConfigurationEquals(testInput.get(0), client.addSetting(testInput.get(0)));
            assertConfigurationEquals(testInput.get(1), client.setSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2), client.addSetting(testInput.get(2)));
            assertConfigurationEquals(testInput.get(3), client.setSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listSettingRevisions(new SettingSelector().setKeys(key, key2));
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
            assertConfigurationEquals(testInput.get(0), client.addSetting(testInput.get(0)));
            assertConfigurationEquals(testInput.get(1), client.setSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2), client.addSetting(testInput.get(2)));
            assertConfigurationEquals(testInput.get(3), client.setSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listSettingRevisions(new SettingSelector().setKeys(key).setLabels(label, label2));
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

        assertConfigurationEquals(original, client.addSetting(original));
        assertConfigurationEquals(updated, client.setSettingWithResponse(updated, false, Context.NONE).getValue());
        assertConfigurationEquals(updated2, client.setSettingWithResponse(updated2, false, Context.NONE).getValue());

        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().setKeys(key).setRange(new Range(1, 2))).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated, revisions.get(0));
        assertConfigurationEquals(original, revisions.get(1));
    }

    /**
     * Verifies that an exception will be thrown from the service if it cannot satisfy the range request.
     */
    @Override
    public void listRevisionsInvalidRange() {
        final String key = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(key).setValue("myValue");

        assertConfigurationEquals(original, client.addSetting(original));
        assertRestException(() -> client.listSettingRevisions(new SettingSelector().setKeys(key).setRange(new Range(0, 10))),
            416); // REQUESTED_RANGE_NOT_SATISFIABLE
    }

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    public void listRevisionsAcceptDateTime() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        try {
            assertConfigurationEquals(original, client.setSettingWithResponse(original, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated, client.setSettingWithResponse(updated, false, Context.NONE).getValue());
            Thread.sleep(2000);
            assertConfigurationEquals(updated2, client.setSettingWithResponse(updated2, false, Context.NONE).getValue());
        } catch (InterruptedException ex) {
            // Do nothing.
        }

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().setKeys(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        revisions = client.listSettingRevisions(options).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated, revisions.get(0));
        assertConfigurationEquals(original, revisions.get(1));
    }

    /**
     * Verifies that, given a ton of revisions, we can list the revisions ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listRevisionsWithPagination() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);
        assertEquals(numberExpected, client.listSettingRevisions(filter).stream().collect(Collectors.toList()).size());
    }

    /**
     * Verifies that, given a ton of revisions, we can process {@link java.util.stream.Stream} multiple time and get same result.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listRevisionsWithPaginationAndRepeatStream() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);
        PagedIterable<ConfigurationSetting> configurationSettingPagedIterable = client.listSettingRevisions(filter);
        assertEquals(numberExpected, configurationSettingPagedIterable.stream().collect(Collectors.toList()).size());

        assertEquals(numberExpected, configurationSettingPagedIterable.stream().collect(Collectors.toList()).size());
    }

    /**
     * Verifies that, given a ton of revisions, we can iterate over multiple time and get same result.
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listRevisionsWithPaginationAndRepeatIterator() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

        PagedIterable<ConfigurationSetting> configurationSettingPagedIterable = client.listSettingRevisions(filter);
        List<ConfigurationSetting> configurationSettingList1 = new ArrayList<>();
        List<ConfigurationSetting> configurationSettingList2 = new ArrayList<>();

        configurationSettingPagedIterable.iterator().forEachRemaining(configurationSetting -> configurationSettingList1.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList1.size());

        configurationSettingPagedIterable.iterator().forEachRemaining(configurationSetting -> configurationSettingList2.add(configurationSetting));
        assertEquals(numberExpected, configurationSettingList2.size());

        assertArrayEquals(configurationSettingList1.toArray(), configurationSettingList2.toArray());
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    public void listSettingsWithPagination() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix), false, Context.NONE).getValue();
        }
        SettingSelector filter = new SettingSelector().setKeys(keyPrefix + "-*").setLabels(labelPrefix);

        assertEquals(numberExpected, client.listSettings(filter).stream().count());
    }

    /**
     * Verifies the conditional "GET" scenario where the setting has yet to be updated, resulting in a 304. This GET
     * scenario will return a setting when the etag provided does not match the one of the current setting.
     */
    public void getSettingWhenValueNotUpdated() {
        final String key = getKey();
        final ConfigurationSetting expected = new ConfigurationSetting().setKey(key).setValue("myValue");
        final ConfigurationSetting newExpected = new ConfigurationSetting().setKey(key).setValue("myNewValue");
        final ConfigurationSetting block = client.addSetting(expected);

        assertNotNull(block);
        assertConfigurationEquals(expected, block);
        assertConfigurationEquals(newExpected, client.setSettingWithResponse(newExpected, false, Context.NONE).getValue());
    }

    public void deleteAllSettings() {

        client.listSettings(new SettingSelector().setKeys("*")).forEach(configurationSetting -> {
            logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isLocked());
            client.deleteSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });
    }
}
