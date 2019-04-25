// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.exception.ServiceRequestException;
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationClientTest extends TestBase {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationClientTest.class);

    private ConfigurationClient client;
    private String keyPrefix;
    private String labelPrefix;

    @Rule
    public TestName testName = new TestName();

    @Override
    public String testName() {
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
            client = ConfigurationClient.builder()
                .credentials(credentials)
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .build();
        } else {
            client = ConfigurationClient.builder()
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

        // TODO (alzimmer): Remove the try/catch sleep when issue is fixed: https://github.com/azure/azure-sdk-for-java/issues/3183
        for (ConfigurationSetting configurationSetting : client.listSettings(new SettingSelector().key(keyPrefix + "*"))) {
            logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());
            try {
                client.deleteSetting(configurationSetting);
            } catch (Throwable e) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {

                }

                client.deleteSetting(configurationSetting);
            }

        }

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
        ConfigurationClientTestBase.addSetting(getKey(), getLabel(), (expected) -> assertConfigurationEquals(expected, client.addSetting(expected)));
    }

    /**
     * Tests that we cannot add a configuration setting when the key is an empty string.
     */
    @Test
    public void addSettingEmptyKey() {
        assertRestException(() -> client.addSetting("", "A value"), HttpResponseStatus.METHOD_NOT_ALLOWED.code());
    }

    /**
     * Tests that we can add configuration settings when value is not null or an empty string.
     */
    @Test
    public void addSettingEmptyValue() {
        ConfigurationClientTestBase.addSettingEmptyValue(getKey(), (setting) -> {
            assertConfigurationEquals(setting, client.addSetting(setting.key(), setting.value()));
            assertConfigurationEquals(setting, client.getSetting(setting.key()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void addSettingNullKey() {
        assertRunnableThrowsException(() -> client.addSetting(null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.addSetting(null), NullPointerException.class);
    }

    /**
     * Tests that a configuration cannot be added twice with the same key. This should return a 412 error.
     */
    @Test
    public void addExistingSetting() {
        ConfigurationClientTestBase.addExistingSetting(getKey(), getLabel(), (expected) -> {
            client.addSetting(expected);
            assertRestException(() -> client.addSetting(expected), HttpResponseStatus.PRECONDITION_FAILED.code());
        });
    }

    /**
     * Tests that a configuration is able to be added or updated with set.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public void setSetting() {
        ConfigurationClientTestBase.setSetting(getKey(), getLabel(), (expected, update) -> assertConfigurationEquals(expected, client.setSetting(expected)));
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
            assertRestException(() -> client.setSetting(initial.etag("badEtag")), HttpResponseStatus.PRECONDITION_FAILED.code());

            final String etag = client.addSetting(initial).value().etag();

            assertConfigurationEquals(update, client.setSetting(update.etag(etag)));
            assertRestException(() -> client.setSetting(initial), HttpResponseStatus.PRECONDITION_FAILED.code());
            assertConfigurationEquals(update, client.getSetting(update));
        });
    }

    /**
     * Tests that we cannot set a configuration setting when the key is an empty string.
     */
    @Test
    public void setSettingEmptyKey() {
        assertRestException(() -> client.setSetting("", "A value"), HttpResponseStatus.METHOD_NOT_ALLOWED.code());
    }

    /**
     * Tests that we can set configuration settings when value is not null or an empty string.
     * Value is not a required property.
     */
    @Test
    public void setSettingEmptyValue() {
        ConfigurationClientTestBase.setSettingEmptyValue(getKey(), (setting) -> {
            assertConfigurationEquals(setting, client.setSetting(setting.key(), setting.value()));
            assertConfigurationEquals(setting, client.getSetting(setting.key()));
        });
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void setSettingNullKey() {
        assertRunnableThrowsException(() -> client.setSetting(null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.setSetting(null), NullPointerException.class);
    }

    /**
     * Tests that update cannot be done to a non-existent configuration, this will result in a 412.
     * Unlike set update isn't able to create the configuration.
     */
    @Test
    public void updateNoExistingSetting() {
        ConfigurationClientTestBase.updateNoExistingSetting(getKey(), getLabel(), (expected) -> {
            assertRestException(() -> client.updateSetting(expected), HttpResponseStatus.PRECONDITION_FAILED.code());
        });
    }

    /**
     * Tests that a configuration is able to be updated when it exists.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public void updateSetting() {
        ConfigurationClientTestBase.updateSetting(getKey(), getLabel(), (initial, update) -> assertConfigurationEquals(initial, client.addSetting(initial)));
    }

    /**
     * Tests that a configuration is able to be updated when it exists with the convenience overload.
     * When the configuration is locked updates cannot happen, this will result in a 409.
     */
    @Test
    public void updateSettingOverload() {
        ConfigurationClientTestBase.updateSettingOverload(getKey(), (original, updated) -> {
            assertConfigurationEquals(original, client.addSetting(original.key(), original.value()));
            assertConfigurationEquals(updated, client.updateSetting(updated.key(), updated.value()));
        });
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
        final String initialEtag = client.addSetting(initial).value().etag();
        final String updateEtag = client.updateSetting(update).value().etag();

        // The setting does not exist in the service yet, so we cannot update it.
        assertRestException(() -> client.updateSetting(new ConfigurationSetting(last).etag(initialEtag)), HttpResponseStatus.PRECONDITION_FAILED.code());

        assertConfigurationEquals(update, client.getSetting(update));
        assertConfigurationEquals(last, client.updateSetting(new ConfigurationSetting(last).etag(updateEtag)));
        assertConfigurationEquals(last, client.getSetting(last));

        assertRestException(() -> client.updateSetting(new ConfigurationSetting(initial).etag(updateEtag)), HttpResponseStatus.PRECONDITION_FAILED.code());
    }

    /**
     * Verifies that an exception is thrown when null key is passed.
     */
    @Test
    public void updateSettingNullKey() {
        assertRunnableThrowsException(() -> client.updateSetting(null, "A Value"), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.updateSetting(null), NullPointerException.class);
    }

    /**
     * Tests that a configuration is able to be retrieved when it exists, whether or not it is locked.
     */
    @Test
    public void getSetting() {
        ConfigurationClientTestBase.getSetting(getKey(), (expected) -> {
            client.addSetting(expected);
            assertConfigurationEquals(expected, client.getSetting(expected));
        });
    }

    /**
     * Tests that attempting to retrieve a non-existent configuration doesn't work, this will result in a 404.
     */
    @Test
    public void getSettingNotFound() {
        final String key = sdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting neverRetrievedConfiguration = new ConfigurationSetting().key(key).value("myNeverRetreivedValue");
        final ConfigurationSetting nonExistentLabel = new ConfigurationSetting().key(key).label("myNonExistentLabel");

        assertConfigurationEquals(neverRetrievedConfiguration, client.addSetting(neverRetrievedConfiguration));

        assertRestException(() -> client.getSetting("myNonExistentKey"), HttpResponseStatus.NOT_FOUND.code());
        assertRestException(() -> client.getSetting(nonExistentLabel), HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that configurations are able to be deleted when they exist.
     * After the configuration has been deleted attempting to get it will result in a 404, the same as if the
     * configuration never existed.
     */
    @Test
    public void deleteSetting() {
        ConfigurationClientTestBase.deleteSetting(getKey(), getLabel(), (expected) -> {
            client.addSetting(expected);
            assertConfigurationEquals(expected, client.getSetting(expected));

            assertConfigurationEquals(expected, client.deleteSetting(expected));
            assertRestException(() -> client.getSetting(expected), HttpResponseStatus.NOT_FOUND.code());
        });
    }

    /**
     * Tests that attempting to delete a non-existent configuration will return a 204.
     */
    @Test
    public void deleteSettingNotFound() {
        final String key = sdkContext.randomResourceName(keyPrefix, 16);
        final ConfigurationSetting neverDeletedConfiguation = new ConfigurationSetting().key(key).value("myNeverDeletedValue");
        final ConfigurationSetting notFoundDelete = new ConfigurationSetting().key(key).label("myNonExistentLabel");

        assertConfigurationEquals(neverDeletedConfiguation, client.addSetting(neverDeletedConfiguation));

        assertConfigurationEquals(null, client.deleteSetting("myNonExistentKey"), HttpResponseStatus.NO_CONTENT.code());
        assertConfigurationEquals(null, client.deleteSetting(notFoundDelete), HttpResponseStatus.NO_CONTENT.code());

        assertConfigurationEquals(neverDeletedConfiguation, client.getSetting(neverDeletedConfiguation.key()));
    }

    /**
     * Tests that when an etag is passed to delete it will only delete if the current representation of the setting has the etag.
     * If the delete etag doesn't match anything the delete won't happen, this will result in a 412.
     */
    @Test
    public void deleteSettingWithETag() {
        ConfigurationClientTestBase.deleteSettingWithETag(getKey(), getLabel(), (initial, update) -> {
            final ConfigurationSetting initiallyAddedConfig = client.addSetting(initial).value();
            final ConfigurationSetting updatedConfig = client.updateSetting(update).value();

            assertConfigurationEquals(update, client.getSetting(initial));
            assertRestException(() -> client.deleteSetting(initiallyAddedConfig), HttpResponseStatus.PRECONDITION_FAILED.code());
            assertConfigurationEquals(update, client.deleteSetting(updatedConfig));
            assertRestException(() -> client.getSetting(initial), HttpResponseStatus.NOT_FOUND.code());
        });
    }

    /**
     * Test the API will not make a delete call without having a key passed, an IllegalArgumentException should be thrown.
     */
    @Test
    public void deleteSettingNullKey() {
        assertRunnableThrowsException(() -> client.deleteSetting((String) null), IllegalArgumentException.class);
        assertRunnableThrowsException(() -> client.deleteSetting((ConfigurationSetting) null), NullPointerException.class);
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

        assertConfigurationEquals(expected, client.setSetting(expected));
        assertConfigurationEquals(expected, client.listSettings(new SettingSelector().key(key).label(label)).get(0));
        assertConfigurationEquals(expected, client.listSettings(new SettingSelector().key(key)).get(0));
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
            .label("*-second*")
            .key(keyPrefix + "-fetch-*")
            .fields(SettingFields.KEY, SettingFields.ETAG, SettingFields.CONTENT_TYPE, SettingFields.TAGS);

        for (int value = 0; value < numberToCreate; value++) {
            String key = value % 2 == 0 ? keyPrefix + "-" + value : keyPrefix + "-fetch-" + value;
            String lbl = value / 4 == 0 ? label : label2;
            client.setSetting(new ConfigurationSetting().key(key).value("myValue2").label(lbl).tags(tags));
        }

        for (ConfigurationSetting setting : client.listSettings(secondLabelOptions)) {
            // These are the fields we chose in our filter.
            assertNotNull(setting.etag());
            assertNotNull(setting.key());
            assertTrue(setting.key().contains(keyPrefix));
            assertNotNull(setting.tags());
            assertEquals(tags.size(), setting.tags().size());

            assertNull(setting.lastModified());
            assertNull(setting.contentType());
            assertNull(setting.label());
        }
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
        try {
            assertConfigurationEquals(original, client.setSetting(original));
            Thread.sleep(2000);
            assertConfigurationEquals(updated, client.setSetting(updated));
            Thread.sleep(2000);
            assertConfigurationEquals(updated2, client.setSetting(updated2));
        } catch (InterruptedException e) {

        }

        // Gets all versions of this value so we can get the one we want at that particular date.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().key(keyName));

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch the configuration setting when we first updated its value.
        SettingSelector options = new SettingSelector().key(keyName).acceptDatetime(revisions.get(1).lastModified());
        assertConfigurationEquals(updated, client.listSettings(options).get(0));
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
        assertConfigurationEquals(original, client.setSetting(original));
        assertConfigurationEquals(updated, client.setSetting(updated));
        assertConfigurationEquals(updated2, client.setSetting(updated2));

        // Get all revisions for a key, they are listed in descending order.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().key(keyName));
        assertConfigurationEquals(updated2, revisions.get(0));
        assertConfigurationEquals(updated, revisions.get(1));
        assertConfigurationEquals(original, revisions.get(2));

        // Verifies that we can select specific fields.
        revisions = client.listSettingRevisions(new SettingSelector().key(keyName).fields(SettingFields.KEY, SettingFields.ETAG));
        ConfigurationSetting revision = revisions.get(0);
        assertEquals(updated2.key(), revision.key());
        assertNotNull(revision.etag());
        assertNull(revision.value());
        assertNull(revision.lastModified());

        revision = revisions.get(1);
        assertEquals(updated.key(), revision.key());
        assertNotNull(revision.etag());
        assertNull(revision.value());
        assertNull(revision.lastModified());

        revision = revisions.get(2);
        assertEquals(original.key(), revision.key());
        assertNotNull(revision.etag());
        assertNull(revision.value());
        assertNull(revision.lastModified());
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
        try {
            assertConfigurationEquals(original, client.setSetting(original));
            Thread.sleep(2000);
            assertConfigurationEquals(updated, client.setSetting(updated));
            Thread.sleep(2000);
            assertConfigurationEquals(updated2, client.setSetting(updated2));
        } catch (InterruptedException e) {

        }

        // Gets all versions of this value.
        List<ConfigurationSetting> revisions = client.listSettingRevisions(new SettingSelector().key(keyName));

        assertNotNull(revisions);
        assertEquals(3, revisions.size());

        // We want to fetch all the revisions that existed up and including when the first revision was created.
        // Revisions are returned in descending order from creation date.
        SettingSelector options = new SettingSelector().key(keyName).acceptDatetime(revisions.get(1).lastModified());
        revisions = client.listSettingRevisions(options);
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
            client.setSetting(new ConfigurationSetting().key(keyPrefix).value("myValue" + value).label(labelPrefix));
        }

        SettingSelector filter = new SettingSelector().key(keyPrefix).label(labelPrefix);
        assertEquals(numberExpected, client.listSettingRevisions(filter).size());
    }

    /**
     * Verifies that, given a ton of existing settings, we can list the ConfigurationSettings using pagination
     * (ie. where 'nextLink' has a URL pointing to the next page of results.)
     */
    @Test
    public void listSettingsWithPagination() {
        final int numberExpected = 50;
        for (int value = 0; value < numberExpected; value++) {
            client.setSetting(new ConfigurationSetting().key(keyPrefix + "-" + value).value("myValue").label(labelPrefix));
        }

        SettingSelector filter = new SettingSelector().key(keyPrefix + "-*").label(labelPrefix);
        assertEquals(numberExpected, client.listSettings(filter).size());
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
        final Response<ConfigurationSetting> block = client.addSetting(expected);

        assertNotNull(block);
        assertConfigurationEquals(expected, block);
        assertConfigurationEquals(newExpected, client.setSetting(newExpected));
    }

    @Ignore("This test exists to clean up resources missed due to 429s.")
    @Test
    public void deleteAllSettings() {
        for (ConfigurationSetting configurationSetting : client.listSettings(new SettingSelector().key("*"))) {
            logger.info("Deleting key:label [{}:{}]. isLocked? {}", configurationSetting.key(), configurationSetting.label(), configurationSetting.isLocked());
            client.deleteSetting(configurationSetting);
        }
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected. This method assumes a response status of 200.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned by the service, the body should contain a ConfigurationSetting
     */
    private static void assertConfigurationEquals(ConfigurationSetting expected, Response<ConfigurationSetting> response) {
        assertConfigurationEquals(expected, response, 200);
    }

    /**
     * Helper method to verify that the RestResponse matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param response RestResponse returned from the service, the body should contain a ConfigurationSetting
     * @param expectedStatusCode Expected HTTP status code returned by the service
     */
    private static void assertConfigurationEquals(ConfigurationSetting expected, Response<ConfigurationSetting> response, final int expectedStatusCode) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.statusCode());

        assertConfigurationEquals(expected, response.value());
    }

    /**
     * Helper method to verify that the returned ConfigurationSetting matches what was expected.
     *
     * @param expected ConfigurationSetting expected to be returned by the service
     * @param actual ConfigurationSetting contained in the RestResponse body
     */
    private static void assertConfigurationEquals(ConfigurationSetting expected, ConfigurationSetting actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.key(), actual.key());

        // This is because we have the no label which is deciphered in the service as "\0".
        if (ConfigurationSetting.NO_LABEL.equals(expected.label())) {
            assertNull(actual.label());
        } else {
            assertEquals(expected.label(), actual.label());
        }

        assertEquals(expected.value(), actual.value());
        assertEquals(expected.contentType(), actual.contentType());

        if (expected.tags() != null) {
            assertEquals(expected.tags().size(), actual.tags().size());

            expected.tags().forEach((key, value) -> {
                assertTrue(actual.tags().containsKey(key));
                assertEquals(value, actual.tags().get(key));
            });
        }
    }

    private static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a RestException and it has a specific HTTP response code.
     *
     * @param ex Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    private static void assertRestException(Throwable ex, int expectedStatusCode) {
        assertTrue(ex instanceof ServiceRequestException);
        assertEquals(expectedStatusCode, ((ServiceRequestException) ex).response().statusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    private static void assertRunnableThrowsException(Runnable exceptionThrower, Class exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }
}
