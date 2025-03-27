// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.implementation.models.SettingsListResult;
import com.azure.security.keyvault.administration.implementation.models.UpdateSettingRequest;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.EMPTY_OPTIONS;

/**
 * The {@link KeyVaultSettingsClient} provides synchronous methods to create, update, get and list
 * {@link KeyVaultSetting settings} for an Azure Key Vault account.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultSettingsClient} class, a vault url and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally,
 * we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">
 * Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Synchronous Backup Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultSettingsClient}, using the
 * {@link KeyVaultSettingsClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.instantiation -->
 * <pre>
 * KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get All Settings</h2>
 * The {@link KeyVaultSettingsClient} can be used to list all the settings for an Azure Key Vault account.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously back up an entire collection of keys using, using the
 * {@link KeyVaultSettingsClient#getSettings()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
 * <pre>
 * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
 * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
 *
 * settings.forEach&#40;setting -&gt;
 *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
 *         setting.asBoolean&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultSettingsAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Retrieve a Specific Setting</h2>
 * The {@link KeyVaultSettingsClient} can be used to retrieve a specific setting.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously restore an entire collection of keys from a backup,
 * using the {@link KeyVaultSettingsClient#getSetting(String)} (String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
 * <pre>
 * KeyVaultSetting setting = keyVaultSettingsClient.getSetting&#40;settingName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultSettingsAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update a Specific Setting</h2>
 * The {@link KeyVaultSettingsClient} can be used to restore a specific key from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to synchronously restore a specific key from a backup, using
 * the {@link KeyVaultSettingsClient#updateSetting(KeyVaultSetting)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
 * <pre>
 * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
 * KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting&#40;settingToUpdate&#41;;
 *
 * System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;, updatedSetting.asBoolean&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
 *
 * <p><strong>Note:</strong> For the asynchronous sample, refer to {@link KeyVaultSettingsAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultSettingsClientBuilder
 */
@ServiceClient(builder = KeyVaultSettingsClientBuilder.class)
public final class KeyVaultSettingsClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsClient.class);
    private final KeyVaultAdministrationClientImpl implClient;

    /**
     * Initializes an instance of {@link KeyVaultSettingsClient} class.
     *
     * @param implClient The implementation client used to service requests.
     */
    KeyVaultSettingsClient(KeyVaultAdministrationClientImpl implClient) {
        this.implClient = implClient;
    }

    /**
     * Updates a given {@link KeyVaultSetting account setting}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates a given {@link KeyVaultSetting setting}. Prints out the details of the updated
     * {@link KeyVaultRoleDefinition setting}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
     * <pre>
     * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     * KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting&#40;settingToUpdate&#41;;
     *
     * System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;, updatedSetting.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return The updated {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting updateSetting(KeyVaultSetting setting) {
        Objects.requireNonNull(setting, String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(implClient
                .updateSettingWithResponse(setting.getName(),
                    BinaryData.fromObject(new UpdateSettingRequest(settingValue)), EMPTY_OPTIONS)
                .getValue()
                .toObject(Setting.class));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-Context -->
     * <pre>
     * KeyVaultSetting mySettingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     * Response&lt;KeyVaultSetting&gt; response =
     *     keyVaultSettingsClient.updateSettingWithResponse&#40;mySettingToUpdate, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Updated setting '%s' to '%s'.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-Context -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> updateSettingWithResponse(KeyVaultSetting setting, Context context) {
        Objects.requireNonNull(setting, String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            Response<BinaryData> response = implClient.updateSettingWithResponse(setting.getName(),
                BinaryData.fromObject(new UpdateSettingRequest(settingValue)),
                new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(response.getValue().toObject(Setting.class)));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
     * <pre>
     * KeyVaultSetting setting = keyVaultSettingsClient.getSetting&#40;settingName&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return The {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting getSetting(String name) {
        try {
            return KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(
                implClient.getSettingWithResponse(name, EMPTY_OPTIONS).getValue().toObject(Setting.class));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-Context -->
     * <pre>
     * Response&lt;KeyVaultSetting&gt; response =
     *     keyVaultSettingsClient.getSettingWithResponse&#40;settingName, new Context&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-Context -->
     *
     * @param name The name of setting to retrieve the value of.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> getSettingWithResponse(String name, Context context) {
        try {
            Response<BinaryData> response
                = implClient.getSettingWithResponse(name, new RequestOptions().setContext(context));

            return new SimpleResponse<>(response,
                KeyVaultSettingsAsyncClient.transformToKeyVaultSetting(response.getValue().toObject(Setting.class)));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     * <pre>
     * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
     * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
     *
     * settings.forEach&#40;setting -&gt;
     *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *         setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     *
     * @return A {@link KeyVaultGetSettingsResult result object} wrapping the list of
     * {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultGetSettingsResult getSettings() {
        try {
            List<KeyVaultSetting> keyVaultSettings = implClient.getSettingsWithResponse(EMPTY_OPTIONS)
                .getValue()
                .toObject(SettingsListResult.class)
                .getSettings()
                .stream()
                .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting)
                .collect(Collectors.toList());

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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettingsWithResponse#Context -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettingsWithResponse#Context -->
     *
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains a
     * {@link KeyVaultGetSettingsResult result object} wrapping the list of {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultGetSettingsResult> getSettingsWithResponse(Context context) {
        try {
            Response<BinaryData> response
                = implClient.getSettingsWithResponse(new RequestOptions().setContext(context));
            List<KeyVaultSetting> keyVaultSettings = response.getValue()
                .toObject(SettingsListResult.class)
                .getSettings()
                .stream()
                .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting)
                .collect(Collectors.toList());

            return new SimpleResponse<>(response, new KeyVaultGetSettingsResult(keyVaultSettings));
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }
}
