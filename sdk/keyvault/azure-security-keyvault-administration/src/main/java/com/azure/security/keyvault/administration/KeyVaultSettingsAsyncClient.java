// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils;
import com.azure.security.keyvault.administration.implementation.models.Setting;
import com.azure.security.keyvault.administration.implementation.models.SettingsListResult;
import com.azure.security.keyvault.administration.implementation.models.UpdateSettingRequest;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.EMPTY_OPTIONS;

/**
 * The {@link KeyVaultSettingsAsyncClient} provides asynchronous methods to create, update, get and list
 * {@link KeyVaultSetting settings} for an Azure Key Vault account.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault service, you will need to create an instance of the
 * {@link KeyVaultSettingsAsyncClient} class, a vault url and a credential object.</p>
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
 * <p><strong>Sample: Construct Asynchronous Backup Client</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultSettingsAsyncClient}, using the
 * {@link KeyVaultSettingsClientBuilder} to configure it.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.instantiation -->
 * <pre>
 * KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = new KeyVaultSettingsClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get All Settings</h2>
 * The {@link KeyVaultSettingsAsyncClient} can be used to list all the settings for an Azure Key Vault account.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously back up an entire collection of keys using, using the
 * {@link KeyVaultSettingsAsyncClient#getSettings()} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettings -->
 * <pre>
 * keyVaultSettingsAsyncClient.getSettings&#40;&#41;.subscribe&#40;getSettingsResult -&gt;
 *     getSettingsResult.getSettings&#40;&#41;.forEach&#40;setting -&gt;
 *         System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
 *             setting.asBoolean&#40;&#41;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettings -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultSettingsClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Retrieve a Specific Setting</h2>
 * The {@link KeyVaultSettingsClient} can be used to retrieve a specific setting.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously restore an entire collection of keys from a backup,
 * using the {@link KeyVaultSettingsClient#getSetting(String)} (String, String)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSetting#String -->
 * <pre>
 * keyVaultSettingsAsyncClient.getSetting&#40;settingName&#41;
 *     .subscribe&#40;setting -&gt;
 *         System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSetting#String -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultSettingsClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update a Specific Setting</h2>
 * The {@link KeyVaultSettingsAsyncClient} can be used to restore a specific key from a backup.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to asynchronously restore a specific key from a backup, using
 * the {@link KeyVaultSettingsAsyncClient#updateSetting(KeyVaultSetting)} API.</p>
 *
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting -->
 * <pre>
 * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
 *
 * keyVaultSettingsAsyncClient.updateSetting&#40;settingToUpdate&#41;
 *     .subscribe&#40;updatedSetting -&gt;
 *         System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;,
 *             updatedSetting.asBoolean&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting -->
 *
 * <p><strong>Note:</strong> For the synchronous sample, refer to {@link KeyVaultSettingsClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * @see com.azure.security.keyvault.administration
 * @see KeyVaultSettingsClientBuilder
 */
@ServiceClient(
    builder = KeyVaultSettingsClientBuilder.class,
    isAsync = true,
    serviceInterfaces = KeyVaultAdministrationClientImpl.KeyVaultAdministrationClientService.class)
public final class KeyVaultSettingsAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsAsyncClient.class);
    private final KeyVaultAdministrationClientImpl implClient;

    /**
     * Creates a {@link KeyVaultSettingsAsyncClient} that uses a {@link KeyVaultAdministrationClientImpl} to service requests.
     *
     * @param implClient The implementation client used to service requests.
     */
    KeyVaultSettingsAsyncClient(KeyVaultAdministrationClientImpl implClient) {
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting -->
     * <pre>
     * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     *
     * keyVaultSettingsAsyncClient.updateSetting&#40;settingToUpdate&#41;
     *     .subscribe&#40;updatedSetting -&gt;
     *         System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;,
     *             updatedSetting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return A {@link Mono} containing the updated {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSetting> updateSetting(KeyVaultSetting setting) {
        Objects.requireNonNull(setting, String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return implClient
                .updateSettingWithResponseAsync(setting.getName(),
                    BinaryData.fromObject(new UpdateSettingRequest(settingValue)), EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultSetting(response.getValue().toObject(Setting.class)));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting -->
     * <pre>
     * KeyVaultSetting mySettingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
     *
     * keyVaultSettingsAsyncClient.updateSettingWithResponse&#40;mySettingToUpdate&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Updated setting '%s' to '%s'.%n&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting -->
     *
     * @param setting The {@link KeyVaultSetting account setting} to update.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the updated
     * {@link KeyVaultSetting account setting}.
     *
     * @throws NullPointerException if {@code setting} is {@code null}.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSetting>> updateSettingWithResponse(KeyVaultSetting setting) {
        Objects.requireNonNull(setting, String.format(KeyVaultAdministrationUtil.PARAMETER_REQUIRED, "'setting'"));

        try {
            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return implClient
                .updateSettingWithResponseAsync(setting.getName(),
                    BinaryData.fromObject(new UpdateSettingRequest(settingValue)), EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultSetting(response.getValue().toObject(Setting.class))));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSetting#String -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSetting&#40;settingName&#41;
     *     .subscribe&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSetting#String -->
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return A {@link Mono} containing the {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultSetting> getSetting(String name) {
        try {
            return implClient.getSettingWithResponseAsync(name, EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> transformToKeyVaultSetting(response.getValue().toObject(Setting.class)));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingWithResponse#String -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSettingWithResponse&#40;settingName&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n&quot;,
     *             response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingWithResponse#String -->
     *
     * @param name The name of setting to retrieve the value of.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultSetting account setting}.
     *
     * @throws IllegalArgumentException thrown if the setting type is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultSetting>> getSettingWithResponse(String name) {
        try {
            return implClient.getSettingWithResponseAsync(name, EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> new SimpleResponse<>(response,
                    transformToKeyVaultSetting(response.getValue().toObject(Setting.class))));
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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettings -->
     * <pre>
     * keyVaultSettingsAsyncClient.getSettings&#40;&#41;.subscribe&#40;getSettingsResult -&gt;
     *     getSettingsResult.getSettings&#40;&#41;.forEach&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *             setting.asBoolean&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettings -->
     *
     * @return A {@link Mono} containing a {@link KeyVaultGetSettingsResult result object} wrapping the list of
     * {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultGetSettingsResult> getSettings() {
        try {
            return implClient.getSettingsWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> {
                    List<KeyVaultSetting> keyVaultSettings = response.getValue()
                        .toObject(SettingsListResult.class)
                        .getSettings()
                        .stream()
                        .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting)
                        .collect(Collectors.toList());

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
     * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingsWithResponse -->
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
     * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingsWithResponse -->
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains a
     * {@link KeyVaultGetSettingsResult result object} wrapping the list of {@link KeyVaultSetting account settings}.
     *
     * @throws IllegalArgumentException thrown if a setting type in the list is not supported.
     * @throws KeyVaultAdministrationException thrown if the request is rejected by the server.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultGetSettingsResult>> getSettingsWithResponse() {
        try {
            return implClient.getSettingsWithResponseAsync(EMPTY_OPTIONS)
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> {
                    List<KeyVaultSetting> keyVaultSettings = response.getValue()
                        .toObject(SettingsListResult.class)
                        .getSettings()
                        .stream()
                        .map(KeyVaultSettingsAsyncClient::transformToKeyVaultSetting)
                        .collect(Collectors.toList());

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
