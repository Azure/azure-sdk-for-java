package com.azure.applicationconfig.hello_world;

import com.azure.applicationconfig.ConfigurationClient;
import com.azure.applicationconfig.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.http.rest.RestResponse;
import reactor.core.publisher.Mono;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HelloWorld {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Retrieve the connection string from the configuration store.
        // You can get the string from your Azure portal.
        final String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = ConfigurationClient.builder()
            .credentials(new ConfigurationClientCredentials(connectionString))
            .build();

        // Create a setting to be stored by the configuration service.
        final String theKey = "hello";
        final ConfigurationSetting setting = new ConfigurationSetting().key(theKey).value("world");

        // setSetting adds or updates a setting to Azure Application Configuration store.
        // Alternatively, you can call addSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call updateSetting to update a setting that is already present in the store.
        RestResponse<ConfigurationSetting> add = client.setSetting(setting).block();

        // Retrieve a previously stored setting by calling getSetting.
        RestResponse<ConfigurationSetting> getSetting = client.getSetting(theKey).block();
    }
}
