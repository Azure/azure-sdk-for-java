// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.KeyVaultSettingsClientImpl;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.implementation.models.SettingsListResult;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsClient.class);
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
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a given {@link KeyVaultSetting setting}. Prints out the details of the updated
     * {@link KeyVaultRoleDefinition setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSetting#KeyVaultSetting -->
     * <pre>
     * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     * KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting&#40;settingToUpdate&#41;;
     *
     * System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;, updatedSetting.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSetting#KeyVaultSetting -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return The updated {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting updateSetting(KeyVaultSetting setting) {
        Objects.requireNonNull(setting, String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(
                implClient.updateSetting(vaultUrl, setting.getName(), settingValue));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a given {@link KeyVaultSetting setting}. Prints out the details of the {@link Response HTTP response}
     * and the updated {@link KeyVaultSetting setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-Context -->
     * <pre>
     * KeyVaultSetting mySettingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     * Response&lt;KeyVaultSetting&gt; response =
     *     keyVaultSettingsClient.updateSettingWithResponse&#40;mySettingToUpdate, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Updated setting '%s' to '%s'.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-Context -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> updateSettingWithResponse(KeyVaultSetting setting, Context context) {
        Objects.requireNonNull(setting, String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            Response<Setting> response =
                implClient.updateSettingWithResponse(vaultUrl, setting.getName(), settingValue, context);

            return new SimpleResponse<>(response,
                KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Get the value of a specific account setting.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves a specific {@link KeyVaultSetting setting}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsClient.getSetting#String -->
     * <pre>
     * KeyVaultSetting setting = keyVaultSettingsClient.getSetting&#40;settingName&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsClient.getSetting#String -->
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return The {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting getSetting(String name) {
        try {
            return KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(this.implClient.getSetting(vaultUrl, name));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Get the value of a specific account setting.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves a specific {@link KeyVaultSetting setting}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultSetting setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingWithResponse#String-Context -->
     * <pre>
     * Response&lt;KeyVaultSetting&gt; response =
     *     keyVaultSettingsClient.getSettingWithResponse&#40;settingName, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingWithResponse#String-Context -->
     *
     * @param name The name of setting to retrieve the value of.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> getSettingWithResponse(String name, Context context) {
        try {
            Response<Setting> response = implClient.getSettingWithResponse(vaultUrl, name, context);

            return new SimpleResponse<>(response,
                KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Get the account's settings.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves all the {@link KeyVaultSetting settings} for an account. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition settings}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettings -->
     * <pre>
     * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
     * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
     *
     * settings.forEach&#40;setting -&gt;
     *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *         setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettings -->
     *
     * @return A {@link KeyVaultGetSettingsResult result object} wrapping the list of
     * {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultGetSettingsResult getSettings() {
        List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

        try {
            implClient.getSettings(vaultUrl).getSettings()
                .forEach(setting -> keyVaultSettings.add(KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(setting)));

            return new KeyVaultGetSettingsResult(keyVaultSettings);
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    /**
     * Get the account's settings.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves all {@link KeyVaultSetting settings for an account}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultSetting settings}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingsWithResponse#Context -->
     * <pre>
     * Response&lt;KeyVaultGetSettingsResult&gt; response =
     *     keyVaultSettingsClient.getSettingsWithResponse&#40;new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d.&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     * KeyVaultGetSettingsResult myGetSettingsResult = response.getValue&#40;&#41;;
     * List&lt;KeyVaultSetting&gt; mySettings = myGetSettingsResult.getSettings&#40;&#41;;
     *
     * mySettings.forEach&#40;setting -&gt;
     *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *         setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingsWithResponse#Context -->
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a
     * {@link KeyVaultGetSettingsResult result object} wrapping the list of {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultErrorException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultGetSettingsResult> getSettingsWithResponse(Context context) {
        try {
            Response<SettingsListResult> response = implClient.getSettingsWithResponse(vaultUrl, context);
            List<KeyVaultSetting> keyVaultSettings = new ArrayList<>();

            response.getValue().getSettings()
                .forEach(setting ->
                    keyVaultSettings.add(KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(setting)));

            return new SimpleResponse<>(response, new KeyVaultGetSettingsResult(keyVaultSettings));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
}
