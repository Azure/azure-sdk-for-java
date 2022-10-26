// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.implementation.models.SettingsListResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingsListResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link KeyVaultSettingsClient} provides asynchronous methods to create, update, get and list
 * {@link KeyVaultSetting settings} for the Azure Key Vault.
 *
 * <p>Instances of this client are obtained by calling the {@link KeyVaultSettingsClientBuilder#buildClient()}
 * method on a {@link KeyVaultSettingsClientBuilder} object.</p>
 *
 * @see KeyVaultSettingsClientBuilder
 */
@ServiceClient(builder = KeyVaultSettingsClientBuilder.class)
public final class KeyVaultSettingsClient {
    private final String vaultUrl;
    private final KeyVaultSettingsClientImpl implClient;
    private final ClientLogger logger = new ClientLogger(KeyVaultSettingsClient.class);

    /**
     * Initializes an instance of {@link KeyVaultSettingsClient} class.
     *
     * @param vaultUrl The URL of the key vault this client will act on.
     * @param implClient The implementation client used to service requests.
     */
    KeyVaultSettingsClient(String vaultUrl, KeyVaultSettingsClientImpl implClient) {
        this.vaultUrl = vaultUrl;
        this.implClient = implClient;
    }

    /**
     * Updates a given account setting with the provided value.
     *
     * @param name The name of the account setting to update.
     * @param value The value to set.
     *
     * @return The updated {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting updateSetting(String name, String value) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        return KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(
            this.implClient.updateSetting(vaultUrl, name, value));
    }

    /**
     * Updates a given account setting with the provided value.
     *
     * @param name The name of the setting to update.
     * @param value The value to set.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> updateSettingWithResponse(String name, String value, Context context) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        Response<Setting> response = this.implClient.updateSettingWithResponse(vaultUrl, name, value, context);

        return new SimpleResponse<>(response, KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(response.getValue()));
    }

    /**
     * Get the value of a specific account setting.
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return The {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting getSetting(String name) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        return KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(this.implClient.getSetting(vaultUrl, name));
    }

    /**
     * Get the value of a specific account setting.
     *
     * @param name The name of setting to retrieve the value of.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> getSettingWithResponse(String name, Context context) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        Response<Setting> response = this.implClient.getSettingWithResponse(vaultUrl, name, context);

        return new SimpleResponse<>(response,
            KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(response.getValue()));
    }

    /**
     * List the account's settings.
     *
     * @return A {@link KeyVaultSettingsListResult result object} wrapping the list of
     * {@link KeyVaultSetting account settings}.
     *
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSettingsListResult listSettings() {
        List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

        this.implClient.getSettings(vaultUrl).getValue()
            .forEach(setting -> keyVaultSettings.add(KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(setting)));

        return new KeyVaultSettingsListResult(keyVaultSettings);
    }

    /**
     * List the account's settings.
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a
     * {@link KeyVaultSettingsListResult result object} wrapping the list of {@link KeyVaultSetting account settings}.
     *
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSettingsListResult> listSettingsWithResponse(Context context) {
        Response<SettingsListResult> response = this.implClient.getSettingsWithResponse(vaultUrl, context);
        List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

        response.getValue().getValue()
            .forEach(setting -> keyVaultSettings.add(KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(setting)));

        return new SimpleResponse<>(response, new KeyVaultSettingsListResult(keyVaultSettings));
    }
}
