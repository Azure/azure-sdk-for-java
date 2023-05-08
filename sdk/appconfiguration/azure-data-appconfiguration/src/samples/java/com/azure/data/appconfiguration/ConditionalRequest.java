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
     * Runs the sample program and demonstrates how to add, get, and delete a configuration setting by conditional request.
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

        // If you want to conditionally update the setting, set `ifUnchanged` to true. If the ETag of the
        // given setting matches the one in the service, then the setting is updated. Otherwise, it is
        // not updated.
        // If the given setting is not exist in the service, the setting will be added to the service.
        Response<ConfigurationSetting> settingResponse = client.setConfigurationSettingWithResponse(setting, true, Context.NONE);
        System.out.printf(String.format("Status code: %s, Key: %s, Value: %s", settingResponse.getStatusCode(),
            settingResponse.getValue().getKey(), settingResponse.getValue().getValue()));

        // If you want to conditionally retrieve the setting, set `ifChanged` to true. If the ETag of the
        // given setting matches the one in the service, then 304 status code with null value returned in the response.
        // Otherwise, a setting with new ETag returned, which is the latest setting retrieved from the service.
        settingResponse = client.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
        System.out.printf(String.format("Status code: %s, Key: %s, Value: %s", settingResponse.getStatusCode(),
            settingResponse.getValue().getKey(), settingResponse.getValue().getValue()));

        // If you want to conditionally delete the setting, set `ifUnchanged` to true. If the ETag of the
        // given setting matches the one in the service, then the setting is deleted. Otherwise, it is
        // not deleted.
        client.deleteConfigurationSettingWithResponse(settingResponse.getValue(), true, Context.NONE);
        System.out.printf(String.format("Status code: %s, Key: %s, Value: %s", settingResponse.getStatusCode(),
            settingResponse.getValue().getKey(), settingResponse.getValue().getValue()));
    }
}
