// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.models.KeyVaultListSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;

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
    private final ClientLogger logger = new ClientLogger(KeyVaultSettingsAsyncClient.class);

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
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return A {@link Mono} containing the updated {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws IllegalArgumentException thrown if {@code name} or {@code value} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSetting> updateSetting(KeyVaultSetting setting) {
        Objects.requireNonNull(setting,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'setting'"));

        if (CoreUtils.isNullOrEmpty(setting.getName())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Setting name cannot be null or empty"));
        }

        try {
            return this.implClient.updateSettingAsync(vaultUrl, setting.getName(), setting.asString())
                .doOnRequest(ignored -> logger.verbose("Updating account setting - {}", setting.getName()))
                .doOnSuccess(response -> logger.verbose("Updated account setting - {}", setting.getName()))
                .doOnError(error -> logger.warning("Failed updating account setting - {}", setting.getName(), error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSetting>> updateSettingWithResponse(KeyVaultSetting setting) {
        Objects.requireNonNull(setting,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'setting'"));


        if (CoreUtils.isNullOrEmpty(setting.getName())) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        try {
            return this.implClient.updateSettingWithResponseAsync(vaultUrl, setting.getName(), setting.asString())
                .doOnRequest(ignored -> logger.verbose("Updating account setting - {}", setting.getName()))
                .doOnSuccess(response -> logger.verbose("Updated account setting - {}", setting.getName()))
                .doOnError(error -> logger.warning("Failed updating account setting - {}", setting.getName(), error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response, transformToKeyVaultSetting(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Get the value of a specific account setting.
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return A {@link Mono} containing the {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSetting> getSetting(String name) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        try {
            return this.implClient.getSettingAsync(vaultUrl, name)
                .doOnRequest(ignored -> logger.verbose("Retrieving account setting - {}", name))
                .doOnSuccess(response -> logger.verbose("Retrieved account setting - {}", name))
                .doOnError(error -> logger.warning("Failed retrieving account setting - {}", name, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Get the value of a specific account setting.
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if {@code name} is {@code null} or empty.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSetting>> getSettingWithResponse(String name) {
        if (CoreUtils.isNullOrEmpty(name)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'name' cannot be empty or null"));
        }

        try {
            return this.implClient.getSettingWithResponseAsync(vaultUrl, name)
                .doOnRequest(ignored -> logger.verbose("Retrieving account setting - {}", name))
                .doOnSuccess(response -> logger.verbose("Retrieved account setting - {}", name))
                .doOnError(error -> logger.warning("Failed retrieving account setting - {}", name, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response, transformToKeyVaultSetting(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * List the account's settings.
     *
     * @return A {@link Mono} containing a {@link KeyVaultListSettingsResult result object} wrapping the list of
     * {@link KeyVaultSetting account settings}.
     *
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultListSettingsResult> listSettings() {
        try {
            return this.implClient.getSettingsAsync(vaultUrl)
                .doOnRequest(ignored -> logger.verbose("Listing account settings"))
                .doOnSuccess(response -> logger.verbose("Listed account settings successfully"))
                .doOnError(error -> logger.warning("Failed listing account settings", error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(settingsListResult -> {
                    List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

                    settingsListResult.getSettings().forEach(setting ->
                        keyVaultSettings.add(transformToKeyVaultSetting(setting)));

                    return new KeyVaultListSettingsResult(keyVaultSettings);
                });
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * List the account's settings.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link KeyVaultListSettingsResult result object} wrapping the list of {@link KeyVaultSetting account settings}.
     *
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultListSettingsResult>> listSettingsWithResponse() {
        try {
            return this.implClient.getSettingsWithResponseAsync(vaultUrl)
                .doOnRequest(ignored -> logger.verbose("Listing account settings"))
                .doOnSuccess(response -> logger.verbose("Listed account settings successfully"))
                .doOnError(error -> logger.warning("Failed listing account settings", error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> {
                    List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

                    response.getValue().getSettings().forEach(setting ->
                        keyVaultSettings.add(transformToKeyVaultSetting(setting)));

                    return new SimpleResponse<>(response, new KeyVaultListSettingsResult(keyVaultSettings));
                });
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    static KeyVaultSetting transformToKeyVaultSetting(Setting setting) {
        return new KeyVaultSetting(setting.getName(), setting.getValue(),
            KeyVaultSettingType.fromString(setting.getType().toString()));
    }
}
