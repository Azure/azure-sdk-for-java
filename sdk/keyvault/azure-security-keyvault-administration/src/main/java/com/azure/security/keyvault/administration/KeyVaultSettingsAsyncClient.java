// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
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
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsAsyncClient.class);
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
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.implClient.getHttpPipeline();
    }

    /**
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a given {@link KeyVaultSetting setting}. Prints out the details of the updated
     * {@link KeyVaultRoleDefinition setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting -->
     * <pre>
     * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     *
     * keyVaultSettingsAsyncClient.updateSetting&#40;settingToUpdate&#41;
     *     .subscribe&#40;updatedSetting -&gt;
     *         System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;,
     *             updatedSetting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return A {@link Mono} containing the updated {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSetting> updateSetting(KeyVaultSetting setting) {
        Objects.requireNonNull(setting, String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return implClient.updateSettingAsync(vaultUrl, setting.getName(), settingValue)
                .doOnRequest(ignored -> LOGGER.verbose("Updating account setting - {}", setting.getName()))
                .doOnSuccess(response -> LOGGER.verbose("Updated account setting - {}", setting.getName()))
                .doOnError(error -> LOGGER.warning("Failed updating account setting - {}", setting.getName(), error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a given {@link KeyVaultSetting setting}. Prints out the details of the {@link Response HTTP response}
     * and the updated {@link KeyVaultSetting setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting -->
     * <pre>
     * KeyVaultSetting mySettingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     *
     * keyVaultSettingsAsyncClient.updateSettingWithResponse&#40;mySettingToUpdate&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Updated setting '%s' to '%s'.%n&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSetting>> updateSettingWithResponse(KeyVaultSetting setting) {
        Objects.requireNonNull(setting, String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return implClient.updateSettingWithResponseAsync(vaultUrl, setting.getName(), settingValue)
                .doOnRequest(ignored -> LOGGER.verbose("Updating account setting - {}", setting.getName()))
                .doOnSuccess(response -> LOGGER.verbose("Updated account setting - {}", setting.getName()))
                .doOnError(error -> LOGGER.warning("Failed updating account setting - {}", setting.getName(), error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response, transformToKeyVaultSetting(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Get the value of a specific account setting.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves a specific {@link KeyVaultSetting setting}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSetting#String -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSetting&#40;settingName&#41;
     *     .subscribe&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSetting#String -->
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return A {@link Mono} containing the {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSetting> getSetting(String name) {
        try {
            return implClient.getSettingAsync(vaultUrl, name)
                .doOnRequest(ignored -> LOGGER.verbose("Retrieving account setting - {}", name))
                .doOnSuccess(response -> LOGGER.verbose("Retrieved account setting - {}", name))
                .doOnError(error -> LOGGER.warning("Failed retrieving account setting - {}", name, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Get the value of a specific account setting.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves a specific {@link KeyVaultSetting setting}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultSetting setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingWithResponse#String -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSettingWithResponse&#40;settingName&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingWithResponse#String -->
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSetting>> getSettingWithResponse(String name) {
        try {
            return implClient.getSettingWithResponseAsync(vaultUrl, name)
                .doOnRequest(ignored -> LOGGER.verbose("Retrieving account setting - {}", name))
                .doOnSuccess(response -> LOGGER.verbose("Retrieved account setting - {}", name))
                .doOnError(error -> LOGGER.warning("Failed retrieving account setting - {}", name, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response, transformToKeyVaultSetting(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Get the account's settings.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves all the {@link KeyVaultSetting settings} for an account. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition settings}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettings -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSettings&#40;&#41;.subscribe&#40;getSettingsResult -&gt;
     *     getSettingsResult.getSettings&#40;&#41;.forEach&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *             setting.asBoolean&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettings -->
     *
     * @return A {@link Mono} containing a {@link KeyVaultGetSettingsResult result object} wrapping the list of
     * {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultGetSettingsResult> getSettings() {
        try {
            return implClient.getSettingsAsync(vaultUrl)
                .doOnRequest(ignored -> LOGGER.verbose("Listing account settings"))
                .doOnSuccess(response -> LOGGER.verbose("Listed account settings successfully"))
                .doOnError(error -> LOGGER.warning("Failed retrieving account settings", error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(settingsListResult -> {
                    List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

                    settingsListResult.getSettings().forEach(setting ->
                        keyVaultSettings.add(transformToKeyVaultSetting(setting)));

                    return new KeyVaultGetSettingsResult(keyVaultSettings);
                });
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Get the account's settings.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves all {@link KeyVaultSetting settings for an account}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultSetting settings}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingsWithResponse -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSettingsWithResponse&#40;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response successful with status code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     *         KeyVaultGetSettingsResult getSettingsResult = response.getValue&#40;&#41;;
     *         List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
     *
     *         settings.forEach&#40;setting -&gt;
     *             System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *                 setting.asBoolean&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingsWithResponse -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link KeyVaultGetSettingsResult result object} wrapping the list of {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultGetSettingsResult>> getSettingsWithResponse() {
        try {
            return implClient.getSettingsWithResponseAsync(vaultUrl)
                .doOnRequest(ignored -> LOGGER.verbose("Listing account settings"))
                .doOnSuccess(response -> LOGGER.verbose("Listed account settings successfully"))
                .doOnError(error -> LOGGER.warning("Failed retrieving account settings", error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> {
                    List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

                    response.getValue().getSettings().forEach(setting ->
                        keyVaultSettings.add(transformToKeyVaultSetting(setting)));

                    return new SimpleResponse<>(response, new KeyVaultGetSettingsResult(keyVaultSettings));
                });
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    static KeyVaultSetting transformToKeyVaultSetting(Setting setting) {
        if (KeyVaultSettingType.BOOLEAN.toString().equalsIgnoreCase(setting.getType().toString())) {
            return new KeyVaultSetting(setting.getName(), Boolean.parseBoolean(setting.getValue()));
        } else {
            throw new IllegalArgumentException(
                String.format("Could not deserialize setting with name '%s'. Type '%s' is not supported.",
                    setting.getName(), setting.getType()));
        }
    }
}
