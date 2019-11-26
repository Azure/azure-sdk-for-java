// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceExistsException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient());
        } else {
            client = clientSetup(credentials -> new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .httpClient(new NettyAsyncHttpClientBuilder().wiretap(true).build())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .buildClient());
        }
    }

    @Override
    protected void afterTest() {
        logger.info("Cleaning up created key values.");
        client.listConfigurationSettings(new SettingSelector().setKeys(keyPrefix + "*")).forEach(configurationSetting -> {
            logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
            if (configurationSetting.isReadOnly()) {
                client.setReadOnlyWithResponse(configurationSetting, false, Context.NONE);
            }
            client.deleteConfigurationSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });

        logger.info("Finished cleaning up values.");
    }

    /**
     * Tests that a configuration is able to be added, these are differentiate from each other using a key or key-label identifier.
     */
    @Test
    public void addConfigurationSetting() {
        addConfigurationSettingRunner((expected) -> assertConfigurationEquals(expected, client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue()));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @Test
    public void addConfigurationSettingEmptyKey() {
        assertRestException(() -> client.addConfigurationSetting("", null, "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @Test
    public void addConfigurationSettingEmptyValue() {
        addConfigurationSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting, client.addConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getConfigurationSetting(setting.getKey(), setting.getLabel()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void addConfigurationSettingNullKey() {
        assertRunnableThrowsException(() -> client.addConfigurationSetting(null, null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addConfigurationSettingWithResponse(null, Context.NONE), NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @Test
    public void addExistingSetting() {
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
    @Test
    public void setConfigurationSetting() {
        setConfigurationSettingRunner((expected, update) -> assertConfigurationEquals(expected, client.setConfigurationSettingWithResponse(expected, false, Context.NONE).getValue()));
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
    @Test
    public void setConfigurationSettingEmptyKey() {
        assertRestException(() -> client.setConfigurationSetting("", null, "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @Test
    public void setConfigurationSettingEmptyValue() {
        setConfigurationSettingEmptyValueRunner((setting) -> {
            assertConfigurationEquals(setting, client.setConfigurationSetting(setting.getKey(), setting.getLabel(), setting.getValue()));
            assertConfigurationEquals(setting, client.getConfigurationSetting(setting.getKey(), setting.getLabel()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void setConfigurationSettingNullKey() {
        assertRunnableThrowsException(() -> client.setConfigurationSetting(null, null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.setConfigurationSettingWithResponse(null, false, Context.NONE).getValue(), NullPointerException.class);
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

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @Test
    public void getConfigurationSettingNotFound() {
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
    @Test
    public void deleteConfigurationSetting() {
        deleteConfigurationSettingRunner((expected) -> {
            client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();
            assertConfigurationEquals(expected, client.getConfigurationSetting(expected.getKey(), expected.getLabel()));

            assertConfigurationEquals(expected, client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
            assertRestException(() -> client.getConfigurationSetting(expected.getKey(), expected.getLabel()), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @Test
    public void deleteConfigurationSettingNotFound() {
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
    @Test
    public void deleteConfigurationSettingWithETag() {
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
    @Test
    public void deleteConfigurationSettingNullKey() {
        assertRunnableThrowsException(() -> client.deleteConfigurationSetting(null, null), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.deleteConfigurationSettingWithResponse(null, false, Context.NONE).getValue(), NullPointerException.class);
    }

    /**
     * Tests assert that the setting can not be deleted after set the setting to read-only.
     */
    @Test
    public void setReadOnly() {

        lockUnlockRunner((expected) -> {
            // read-only setting
            client.addConfigurationSettingWithResponse(expected, Context.NONE);
            client.setReadOnly(expected.getKey(), expected.getLabel(), true);

            // unsuccessfully delete
            assertRestException(() ->
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE),
                HttpResponseException.class, 409);
        });
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
     * Tests assert that the setting can not be deleted after lock the setting.
     */
    @Test
    public void setReadOnlyWithConfigurationSetting() {
        lockUnlockRunner((expected) -> {
            // lock setting
            client.addConfigurationSettingWithResponse(expected, Context.NONE);
            client.setReadOnlyWithResponse(expected, true, Context.NONE);

            // unsuccessfully delete
            assertRestException(() ->
                client.deleteConfigurationSettingWithResponse(expected, false, Context.NONE),
                HttpResponseException.class, 409);
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

        assertConfigurationEquals(expected, client.setConfigurationSettingWithResponse(expected, false, Context.NONE).getValue());
        assertConfigurationEquals(expected, client.listConfigurationSettings(new SettingSelector().setKeys(key).setLabels(label)).iterator().next());
        assertConfigurationEquals(expected, client.listConfigurationSettings(new SettingSelector().setKeys(key)).iterator().next());
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
            assertConfigurationEquals(setting, client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2, client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            return client.listConfigurationSettings(new SettingSelector().setKeys(key, key2));
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
            assertConfigurationEquals(setting, client.addConfigurationSettingWithResponse(setting, Context.NONE).getValue());
            assertConfigurationEquals(setting2, client.addConfigurationSettingWithResponse(setting2, Context.NONE).getValue());

            return client.listConfigurationSettings(new SettingSelector().setKeys(key).setLabels(label, label2));
        });
    }

    /**
     * Verifies that we can select filter results by key, label, and select fields using SettingSelector.
     */
    @Test
    public void listConfigurationSettingsSelectFields() {
        listConfigurationSettingsSelectFieldsRunner((settings, selector) -> {
            settings.forEach(setting -> client.setConfigurationSettingWithResponse(setting, false, Context.NONE).getValue());
            return client.listConfigurationSettings(selector);
        });
    }

    /**
     * Verifies that we can get a ConfigurationSetting at the provided accept datetime
     */
    @Test
    public void listConfigurationSettingsAcceptDateTime() {
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
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeys(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
        assertConfigurationEquals(updated, (client.listConfigurationSettings(options).stream().collect(Collectors.toList())).get(0));
    }

    /**
     * Verifies that we can get all of the revisions for this ConfigurationSetting. Then verifies that we can select
     * specific fields.
     */
    @Test
    public void listRevisions() {
        final String keyName = getKey();
        final ConfigurationSetting original = new ConfigurationSetting().setKey(keyName).setValue("myValue");
        final ConfigurationSetting updated = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue");
        final ConfigurationSetting updated2 = new ConfigurationSetting().setKey(original.getKey()).setValue("anotherValue2");

        // Create 3 revisions of the same key.
        assertConfigurationEquals(original, client.setConfigurationSettingWithResponse(original, false, Context.NONE).getValue());
        assertConfigurationEquals(updated, client.setConfigurationSettingWithResponse(updated, false, Context.NONE).getValue());
        assertConfigurationEquals(updated2, client.setConfigurationSettingWithResponse(updated2, false, Context.NONE).getValue());

        // Get all revisions for a key, they are listed in descending order.
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeys(keyName)).stream().collect(Collectors.toList());
        assertConfigurationEquals(updated2, revisions.get(0));
        assertConfigurationEquals(updated, revisions.get(1));
        assertConfigurationEquals(original, revisions.get(2));

        // Verifies that we can select specific fields.
        revisions = client.listRevisions(new SettingSelector().setKeys(keyName).setFields(SettingFields.KEY, SettingFields.ETAG)).stream().collect(Collectors.toList());
        validateListRevisions(updated2, revisions.get(0));
        validateListRevisions(updated, revisions.get(1));
        validateListRevisions(original, revisions.get(2));
    }

    /**
     * Verifies that we can get all the revisions for all settings with the specified keys.
     */
    @Test
    public void listRevisionsWithMultipleKeys() {
        String key = getKey();
        String key2 = getKey();

        listRevisionsWithMultipleKeysRunner(key, key2, (testInput) -> {
            assertConfigurationEquals(testInput.get(0), client.addConfigurationSettingWithResponse(testInput.get(0), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(1), client.setConfigurationSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2), client.addConfigurationSettingWithResponse(testInput.get(2), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(3), client.setConfigurationSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listRevisions(new SettingSelector().setKeys(key, key2));
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
            assertConfigurationEquals(testInput.get(0), client.addConfigurationSettingWithResponse(testInput.get(0), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(1), client.setConfigurationSettingWithResponse(testInput.get(1), false, Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(2), client.addConfigurationSettingWithResponse(testInput.get(2), Context.NONE).getValue());
            assertConfigurationEquals(testInput.get(3), client.setConfigurationSettingWithResponse(testInput.get(3), false, Context.NONE).getValue());

            return client.listRevisions(new SettingSelector().setKeys(key).setLabels(label, label2));
        });
    }

    /**
     * Verifies that we can get a subset of revisions based on the "acceptDateTime"
     */
    @Test
    public void listRevisionsAcceptDateTime() {
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
        List<ConfigurationSetting> revisions = client.listRevisions(new SettingSelector().setKeys(keyName)).stream().collect(Collectors.toList());

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().setKeys(keyName).setAcceptDatetime(revisions.get(1).getLastModified());
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
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);
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
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);
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
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix).setValue("myValue" + value).setLabel(labelPrefix), false, Context.NONE).getValue();
        }

        SettingSelector filter = new SettingSelector().setKeys(keyPrefix).setLabels(labelPrefix);

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
    @Test
    public void listConfigurationSettingsWithPagination() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey(keyPrefix + "-" + value).setValue("myValue").setLabel(labelPrefix), false, Context.NONE).getValue();
        }
        SettingSelector filter = new SettingSelector().setKeys(keyPrefix + "-*").setLabels(labelPrefix);

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
        final ConfigurationSetting block = client.addConfigurationSettingWithResponse(expected, Context.NONE).getValue();

        assertNotNull(block);
        assertConfigurationEquals(expected, block);
        // conditional get, now the setting has not be updated yet, resulting 304 and null value
        assertConfigurationEquals(null, client.getConfigurationSettingWithResponse(block, null, true, Context.NONE), 304);
        assertConfigurationEquals(newExpected, client.setConfigurationSettingWithResponse(newExpected, false, Context.NONE).getValue());
        // conditional get, now the setting is updated and we are able to get a new setting with 200 code
        assertConfigurationEquals(newExpected, client.getConfigurationSettingWithResponse(newExpected, null, true, Context.NONE).getValue());
    }

    @Test
    @Disabled
    public void deleteAllSettings() {

        client.listConfigurationSettings(new SettingSelector().setKeys("*")).forEach(configurationSetting -> {
            logger.info("Deleting key:label [{}:{}]. isReadOnly? {}", configurationSetting.getKey(), configurationSetting.getLabel(), configurationSetting.isReadOnly());
            client.deleteConfigurationSettingWithResponse(configurationSetting, false, Context.NONE).getValue();
        });
    }
}
