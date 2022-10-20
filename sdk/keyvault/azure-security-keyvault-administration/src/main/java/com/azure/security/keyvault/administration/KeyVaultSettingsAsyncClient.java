// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;
import reactor.core.publisher.Mono;

/**
 * The {@link KeyVaultSettingsAsyncClient} provides asynchronous methods to create, update, get and list
 * {@link KeyVaultSetting settings} for the Azure Key Vault.
 *
 * <p>Instances of this client are obtained by calling the {@link KeyVaultSettingsClientBuilder#buildAsyncClient()}
 * method on a {@link KeyVaultSettingsClientBuilder} object.</p>
 *
 * @see KeyVaultSettingsClientBuilder
 */
@ServiceClient(builder = KeyVaultSettingsClientBuilder.class, isAsync = true, serviceInterfaces =
    KeyVaultSettingsClientImpl.KeyVaultSettingsClientService.class)
public final class KeyVaultSettingsAsyncClient {
    private final String vaultUrl;
    private final KeyVaultSettingsClientImpl implClient;

    /**
     * Creates a {@link KeyVaultSettingsAsyncClient} that uses a {@link KeyVaultSettingsClientImpl} to service requests.
     *
     * @param vaultUrl The URL of the key vault this client will act on.
     * @param implClient The implementation client used to service requests.
     */
    KeyVaultSettingsAsyncClient(String vaultUrl, KeyVaultSettingsClientImpl implClient) {
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
    public Mono<KeyVaultSetting> createOrUpdateSetting(String name, String value) {
        return this.implClient.createOrUpdateSettingAsync(vaultUrl, name, value)
            .map(setting -> new KeyVaultSetting(setting.getName(), setting.getValue(),
                KeyVaultSettingType.fromString(setting.getType().toString())));
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
    public Mono<Response<KeyVaultSetting>> createOrUpdateSettingWithResponse(String name, String value) {
        return this.implClient.createOrUpdateSettingWithResponseAsync(vaultUrl, name, value)
            .map(response -> {
                Setting setting = response.getValue();
                KeyVaultSetting keyVaultSetting = new KeyVaultSetting(setting.getName(), setting.getValue(),
                    KeyVaultSettingType.fromString(setting.getType().toString()));

                return new SimpleResponse<>(response, keyVaultSetting);
            });
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
    public Mono<KeyVaultSetting> getSetting(String name) {
        return this.implClient.getSettingAsync(vaultUrl, name)
            .map(setting -> new KeyVaultSetting(setting.getName(), setting.getValue(),
                KeyVaultSettingType.fromString(setting.getType().toString())));
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
    public Mono<Response<KeyVaultSetting>> getSettingWithResponse(String name) {
        return this.implClient.getSettingWithResponseAsync(vaultUrl, name)
            .map(response -> {
                Setting setting = response.getValue();
                KeyVaultSetting keyVaultSetting = new KeyVaultSetting(setting.getName(), setting.getValue(),
                    KeyVaultSettingType.fromString(setting.getType().toString()));

                return new SimpleResponse<>(response, keyVaultSetting);
            });
    }

    /**
     * List account settings.
     *
     * @return The settings list result on successful completion of {@link Mono}.
     *
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyVaultSetting> listSettings() {
        return this.implClient.getSettingsAsync(vaultUrl);
    }
}
