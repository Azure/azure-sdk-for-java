// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.implementation.models.SettingsListResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;
import reactor.core.publisher.Mono;

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
     * Creates or updates a given setting with the provided value.
     *
     * @param name The name of the account setting. Must be a valid settings option.
     * @param value The value to set.
     *
     * @return The response body on successful completion of {@link Mono}.
     *
     * @throws IllegalArgumentException thrown if {@code name} or {@code value} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting createOrUpdateSetting(String name, String value) {
        Setting setting = this.implClient.createOrUpdateSetting(vaultUrl, name, value);

        return new KeyVaultSetting(setting.getName(), setting.getValue(),
            KeyVaultSettingType.fromString(setting.getType().toString()));
    }

    /**
     * Creates or updates a given setting with the provided value.
     *
     * @param name The name of the setting. Must be a valid settings option.
     * @param value The value to set.
     *
     * @return The response body along with {@link Response} on successful completion of {@link Mono}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> createOrUpdateSettingWithResponse(String name, String value, Context context) {
        Response<Setting> response = this.implClient.createOrUpdateSettingWithResponse(vaultUrl, name, value, context);
        Setting setting = response.getValue();
        KeyVaultSetting keyVaultSetting = new KeyVaultSetting(setting.getName(), setting.getValue(),
            KeyVaultSettingType.fromString(setting.getType().toString()));

        return new SimpleResponse<>(response, keyVaultSetting);
    }

    /**
     * Get the value of a specific setting.
     *
     * @param name The name of setting to retrieve the value of. Must be a valid settings option.
     *
     * @return The response body on successful completion of {@link Mono}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting getSetting(String name) {
        Setting setting = this.implClient.getSetting(vaultUrl, name);

        return new KeyVaultSetting(setting.getName(), setting.getValue(),
            KeyVaultSettingType.fromString(setting.getType().toString()));
    }

    /**
     * Get the value of a specific setting.
     *
     * @param name The name of setting to retrieve the value of. Must be a valid settings option.
     *
     * @return The response body along with {@link Response} on successful completion of {@link Mono}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> getSettingWithResponse(String name, Context context) {
        Response<Setting> response = this.implClient.getSettingWithResponse(vaultUrl, name, context);
        Setting setting = response.getValue();
        KeyVaultSetting keyVaultSetting = new KeyVaultSetting(setting.getName(), setting.getValue(),
            KeyVaultSettingType.fromString(setting.getType().toString()));

        return new SimpleResponse<>(response, keyVaultSetting);
    }

    /**
     * List account settings.
     *
     * @return The settings list result on successful completion of {@link Mono}.
     *
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyVaultSetting> lisSettings() {
        return this.implClient.getSettings(vaultUrl);
    }
}
