package com.azure.android;

import static com.azure.android.appconfiguration.SecretReferenceConfigurationSettingSample.printSecretReferenceConfigurationSetting;
import static com.azure.android.appconfiguration.WatchFeature.refresh;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.util.Log;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AppConfigurationSampleTests {

    private static final String HELLO_TAG = "AppConfigHelloWorldOutput";
    private static final String WATCH_TAG = "AppConfigWatchFeatureOutput";
    private static final String SECRET_TAG = "AppConfigSecretReferenceConfigurationSettingOutput";
    private static final String READONLY_TAG = "AppConfigReadOnlyOutput";
    private static final String CONDITION_TAG = "AppConfigConditionRequestAsyncOutput";

    final String appConfigEndpoint = "https://android-app-configuration.azconfig.io";
    ClientSecretCredential clientSecretCredential;
    @Before
    public void setup() {
        // These are obtained by setting system environment variables
        // on the computer emulating the app
        clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(BuildConfig.AZURE_CLIENT_ID)
            .clientSecret(BuildConfig.AZURE_CLIENT_SECRET)
            .tenantId(BuildConfig.AZURE_TENANT_ID)
            .build();
    }

    @Test
    public void helloWorld() {
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(clientSecretCredential)
            .endpoint(appConfigEndpoint)
            .buildClient();

        assertNotNull(client);

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        // Sometimes the client can be set to read only from previous tests.
        client.setReadOnly(key, null, false);

        Log.i(HELLO_TAG, "Beginning of synchronous sample...");

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        Log.i(HELLO_TAG, String.format("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        assertNull(setting.getLabel());
        assertNotNull(setting.getValue());

        setting = client.getConfigurationSetting(key, null, null);
        Log.i(HELLO_TAG, String.format("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.deleteConfigurationSetting(key, null);
        Log.i(HELLO_TAG, String.format("[DeleteConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        Log.i(HELLO_TAG, "End of synchronous sample.");

        assertEquals("hello", setting.getKey());
        assertEquals("world", setting.getValue());

    }

    @Test
    public void watchFeature() {

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(clientSecretCredential)
            .endpoint(appConfigEndpoint)
            .buildClient();

        assertNotNull(client);

        // Prepare a list of watching settings and update one same setting value to the service.
        String prodDBConnectionKey = "prodDBConnection";
        String prodDBConnectionLabel = "prodLabel";

        // Assume we have a list of watching setting that stored somewhere.
        List<ConfigurationSetting> watchingSettings = Arrays.asList(
            client.addConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, "prodValue"),
            client.addConfigurationSetting("stageDBConnection", "stageLabel", "stageValue")
        );

        Log.i(WATCH_TAG, "Watching settings:");
        for (ConfigurationSetting setting : watchingSettings) {
            Log.i(WATCH_TAG, String.format("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag()));
        }

        assertEquals(client.getConfigurationSetting(watchingSettings.get(0)).getValue(), "prodValue");

        // One of the watching settings is been updated by someone in other place.
        ConfigurationSetting updatedSetting = client.setConfigurationSetting(
            prodDBConnectionKey, prodDBConnectionLabel, "updatedProdValue");
        Log.i(WATCH_TAG, "Updated settings:");
        Log.i(WATCH_TAG, String.format("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
            updatedSetting.getKey(), updatedSetting.getLabel(), updatedSetting.getValue(), updatedSetting.getETag()));

        assertEquals(client.getConfigurationSetting(watchingSettings.get(0)).getValue(), "updatedProdValue");

        // Updates the watching settings if needed, and only returns a list of updated settings.
        List<ConfigurationSetting> refreshedSettings = refresh(client, watchingSettings);

        Log.i(WATCH_TAG, "Refreshed settings:");
        for (ConfigurationSetting setting : refreshedSettings) {
            Log.i(WATCH_TAG, String.format("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag()));
        }

        assertEquals(client.getConfigurationSetting(watchingSettings.get(0)).getValue(), "updatedProdValue");

        // Cleaning up after ourselves by deleting the values.
        Log.i(WATCH_TAG, "Deleting settings:");
        watchingSettings.forEach(setting -> {
            client.deleteConfigurationSetting(setting.getKey(), setting.getLabel());
            Log.i(WATCH_TAG, String.format("\tkey: %s, value: %s.%n", setting.getKey(), setting.getValue()));
        });

    }

    @Test
    public void secretReferenceConfigurationSettingSample() {
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(clientSecretCredential)
            .endpoint(appConfigEndpoint)
            .buildClient();

        assertNotNull(client);

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String secretIdValue = "{the-keyVault-secret-id-uri}";

        Log.i(SECRET_TAG, "Beginning of synchronous sample...");

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, secretIdValue);

        // setConfigurationSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can
        // call addConfigurationSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call setConfigurationSetting to update a setting that is already present in the store.
        Log.i(SECRET_TAG, "[Set-SecretReferenceConfigurationSetting]");
        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting) client.setConfigurationSetting(referenceConfigurationSetting);
        printSecretReferenceConfigurationSetting(setting);

        assertEquals(setting.getKey(), "hello");

        Log.i(SECRET_TAG, "[Get-SecretReferenceConfigurationSetting]");
        setting = (SecretReferenceConfigurationSetting) client.getConfigurationSetting(setting);
        printSecretReferenceConfigurationSetting(setting);

        assertEquals(setting.getKey(), "hello");

        Log.i(SECRET_TAG, "[List-SecretReferenceConfigurationSetting]");
        final PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector());
        for (ConfigurationSetting configurationSetting : configurationSettings) {
            if (configurationSetting instanceof SecretReferenceConfigurationSetting) {
                Log.i(SECRET_TAG, "-Listing-SecretReferenceConfigurationSetting");
                printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) configurationSetting);
            } else {
                Log.i(SECRET_TAG, "-Listing-non-SecretReferenceConfigurationSetting");
                Log.i(SECRET_TAG, String.format("Key: %s, Value: %s%n", configurationSetting.getKey(), configurationSetting.getValue()));
            }
        }

        Log.i(SECRET_TAG, "[Delete-SecretReferenceConfigurationSetting");
        setting = (SecretReferenceConfigurationSetting) client.deleteConfigurationSetting(setting);
        printSecretReferenceConfigurationSetting(setting);

        assertEquals(setting.getKey(), "hello");

        Log.i(SECRET_TAG, "End of synchronous sample.");
    }

    @Test
    public void readOnly() {
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(clientSecretCredential)
            .endpoint(appConfigEndpoint)
            .buildClient();

        assertNotNull(client);

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        final ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        // Read-Only
        final ConfigurationSetting readOnlySetting = client.setReadOnly(setting.getKey(), setting.getLabel(), true);
        Log.i(READONLY_TAG, String.format("Setting is read-only now, Key: %s, Value: %s",
            readOnlySetting.getKey(), readOnlySetting.getValue()));

        assertTrue(readOnlySetting.isReadOnly());

        // Clear Read-Only
        final ConfigurationSetting clearedReadOnlySetting = client.setReadOnly(setting.getKey(), setting.getLabel(), false);
        Log.i(READONLY_TAG, String.format("Setting is no longer read-only, Key: %s, Value: %s",
            clearedReadOnlySetting.getKey(), clearedReadOnlySetting.getValue()));

        assertFalse(clearedReadOnlySetting.isReadOnly());
    }


    @Test
    public void conditionalRequestAsync() {
        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .credential(clientSecretCredential)
            .endpoint(appConfigEndpoint)
            .buildAsyncClient();

        assertNotNull(client);

        ConfigurationSetting setting = new ConfigurationSetting().setKey("key").setLabel("label").setValue("value");

        // If you want to conditionally update the setting, set `ifUnchanged` to true. If the ETag of the
        // given setting matches the one in the service, then the setting is updated. Otherwise, it is
        // not updated.
        // If the given setting is not exist in the service, the setting will be added to the service.
        client.setConfigurationSettingWithResponse(setting, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                assertEquals(200, statusCode);
                Log.i(CONDITION_TAG, String.format("Set config with status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> Log.e(CONDITION_TAG, "There was an error while setting the setting: " + error));




        // If you want to conditionally retrieve the setting, set `ifChanged` to true. If the ETag of the
        // given setting matches the one in the service, then 304 status code with null value returned in the response.
        // Otherwise, a setting with new ETag returned, which is the latest setting retrieved from the service.
        client.getConfigurationSettingWithResponse(setting, null, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                assertEquals(200, statusCode);
                Log.i(CONDITION_TAG, String.format("Get config with status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> Log.e(CONDITION_TAG, "There was an error while getting the setting: " + error));


        // If you want to conditionally delete the setting, set `ifUnchanged` to true. If the ETag of the
        // given setting matches the one in the service, then the setting is deleted. Otherwise, it is
        // not deleted.
        client.deleteConfigurationSettingWithResponse(setting, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                assertEquals(200, statusCode);
                Log.i(CONDITION_TAG, String.format("Deleted config with status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> Log.e(CONDITION_TAG, "There was an error while deleting the setting: " + error));
    }

}
