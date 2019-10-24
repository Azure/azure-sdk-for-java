// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting by conditional request.
 */
public class ConditionalRequest {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting by conditional
     * request asynchronously
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        ConfigurationSetting setting = new ConfigurationSetting().setKey("key").setLabel("label").setValue("value");

        // If you want to use conditional request, then you should set the boolean value of 'ifUnchanged' to true, so
        // the API will use the ETag value from the given setting to make a conditional request.
        Response<ConfigurationSetting> settingResponse = client.setConfigurationSettingWithResponse(setting, true, Context.NONE);
        System.out.println(String.format("Status code: %s, Key: %s, Value: %s", settingResponse.getStatusCode(),
            settingResponse.getValue().getKey(), settingResponse.getValue().getValue()));

        // If you want to use conditional request, then you should set the boolean value of ifChanged' to true, so the
        // API will use the ETag value from the given setting to make a conditional request.
        settingResponse = client.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
        System.out.println(String.format("Status code: %s, Key: %s, Value: %s", settingResponse.getStatusCode(),
            settingResponse.getValue().getKey(), settingResponse.getValue().getValue()));

        // If you want to use conditional request, then you should set the boolean value of 'ifUnchanged' to true, so
        // the API will use the ETag value from the given setting to make a conditional request.
        client.deleteConfigurationSettingWithResponse(settingResponse.getValue(), true, Context.NONE);
        System.out.println(String.format("Status code: %s, Key: %s, Value: %s", settingResponse.getStatusCode(),
            settingResponse.getValue().getKey(), settingResponse.getValue().getValue()));
    }
}
