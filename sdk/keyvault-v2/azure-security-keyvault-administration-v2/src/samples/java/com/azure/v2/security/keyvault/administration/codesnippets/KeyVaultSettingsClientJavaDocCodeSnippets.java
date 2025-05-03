// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration.codesnippets;

import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient;
import com.azure.v2.security.keyvault.administration.KeyVaultSettingsClientBuilder;
import com.azure.v2.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSetting;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions.HttpLogLevel;

import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultSettingsClient}.
 */
public class KeyVaultSettingsClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultSettingsClient}.
     *
     * @return An instance of {@link KeyVaultSettingsClient}.
     */
    public KeyVaultSettingsClient createClient() {
        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.instantiation
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.instantiation

        return keyVaultSettingsClient;
    }

    /**
     * Generates code sample for creating a {@link KeyVaultSettingsClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyVaultSettingsClient}.
     */
    public KeyVaultSettingsClient createClientWithHttpClient() {
        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.instantiation.withHttpClient
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions().setHttpLogLevel(HttpLogLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.getSharedInstance())
            .buildClient();
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.instantiation.withHttpClient

        return keyVaultSettingsClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#updateSetting(KeyVaultSetting)}.
     */
    public void updateSetting() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting
        KeyVaultSetting settingToUpdate = new KeyVaultSetting("<setting-name>", true);
        KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting(settingToUpdate);

        System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(), updatedSetting.asBoolean());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultSettingsClient#updateSettingWithResponse(KeyVaultSetting, RequestContext)}.
     */
    public void updateSettingWithResponse() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-RequestContext
        KeyVaultSetting settingToUpdate = new KeyVaultSetting("<setting-name>", true);
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultSetting> response =
            keyVaultSettingsClient.updateSettingWithResponse(settingToUpdate, requestContext);

        System.out.printf("Response successful with status code: %d. Updated setting '%s' to '%s'.%n",
            response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#getSetting(String)}.
     */
    public void getSetting() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String
        KeyVaultSetting setting = keyVaultSettingsClient.getSetting("<setting-name>");

        System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#getSettingWithResponse(String, RequestContext)}.
     */
    public void getSettingWithResponse() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultSetting> response =
            keyVaultSettingsClient.getSettingWithResponse("<setting-name>", requestContext);

        System.out.printf("Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n",
            response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#getSettings()}.
     */
    public void getSettings() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings
        KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings();
        List<KeyVaultSetting> settings = getSettingsResult.getSettings();

        settings.forEach(setting ->
            System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                setting.asBoolean()));
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#getSettingsWithResponse(RequestContext)}.
     */
    public void getSettingsWithResponse() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingsWithResponse#RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultGetSettingsResult> response =
            keyVaultSettingsClient.getSettingsWithResponse(requestContext);

        System.out.printf("Response successful with status code: %d.", response.getStatusCode());

        KeyVaultGetSettingsResult myGetSettingsResult = response.getValue();
        List<KeyVaultSetting> mySettings = myGetSettingsResult.getSettings();

        mySettings.forEach(setting ->
            System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                setting.asBoolean()));
        // END: com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingsWithResponse#RequestContext
    }
}
