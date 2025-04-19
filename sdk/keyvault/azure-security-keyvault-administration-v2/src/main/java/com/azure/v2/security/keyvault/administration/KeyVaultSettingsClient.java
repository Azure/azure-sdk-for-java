// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.security.keyvault.administration.implementation.KeyVaultAdministrationClientImpl;
import com.azure.v2.security.keyvault.administration.implementation.models.Setting;
import com.azure.v2.security.keyvault.administration.implementation.models.SettingsListResult;
import com.azure.v2.security.keyvault.administration.implementation.models.UpdateSettingRequest;
import com.azure.v2.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSettingType;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides methods to create, update, get, and list {@link KeyVaultSetting settings} for an Azure Key Vault
 * or Managed HSM account.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault or Managed HSM service, you will need to create an instance of the
 * {@link KeyVaultAccessControlClient} class, an Azure Key Vault or Managed HSM endpoint and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named {@code DefaultAzureCredential} for
 * authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Settings Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link KeyVaultSettingsClient}, using the
 * {@link KeyVaultSettingsClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.instantiation -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Get All Settings</h2>
 * The {@link KeyVaultSettingsClient} can be used to list all the settings for an Azure Key Vault account.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to back up an entire collection of keys using, using the
 * {@link KeyVaultSettingsClient#getSettings()} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Retrieve a Specific Setting</h2>
 * The {@link KeyVaultSettingsClient} can be used to retrieve a specific setting.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to restore an entire collection of keys from a backup, using the
 * {@link KeyVaultSettingsClient#getSetting(String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
 *
 * <br/>
 * <hr/>
 *
 * <h2>Update a Specific Setting</h2>
 * The {@link KeyVaultSettingsClient} can be used to update a specific setting.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to update a specific setting, using the
 * {@link KeyVaultSettingsClient#updateSetting(KeyVaultSetting)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
 * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
 *
 * @see com.azure.v2.security.keyvault.administration
 * @see KeyVaultSettingsClientBuilder
 */
@ServiceClient(builder = KeyVaultSettingsClientBuilder.class)
public final class KeyVaultSettingsClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsClient.class);
    private final KeyVaultAdministrationClientImpl clientImpl;

    /**
     * Creates an instance of {@link KeyVaultSettingsClient} that sends requests to the service through the provided
     * {@link KeyVaultAdministrationClientImpl}.
     *
     * @param clientImpl The implementation client.
     */
    KeyVaultSettingsClient(KeyVaultAdministrationClientImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    /**
     * Updates an account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates a given setting and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
     *
     * @param setting The setting to update.
     *
     * @return The updated setting.
     *
     * @throws HttpResponseException If the provided {@code setting} is malformed.
     * @throws NullPointerException If {@code setting} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting updateSetting(KeyVaultSetting setting) {
        try {
            Objects.requireNonNull(setting, String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL, "'setting'"));

            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            return transformToKeyVaultSetting(
                clientImpl.updateSetting(setting.getName(), new UpdateSettingRequest(settingValue)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates an account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates a given setting. Prints out the details of the response returned by the service and the updated
     * setting.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-RequestContext -->
     *
     * @param setting The setting to update.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the updated setting.
     *
     * @throws HttpResponseException If the provided {@code setting} is malformed.
     * @throws NullPointerException if {@code setting} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> updateSettingWithResponse(KeyVaultSetting setting, RequestContext requestContext) {
        try {
            Objects.requireNonNull(setting, String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL, "'setting'"));

            String settingValue = null;

            if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
                settingValue = Boolean.toString(setting.asBoolean());
            }

            Response<Setting> response = clientImpl.updateSettingWithResponse(setting.getName(),
                new UpdateSettingRequest(settingValue), requestContext);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                transformToKeyVaultSetting(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the value of a specific account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves a specific setting and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
     *
     * @param name The name of setting to retrieve.
     *
     * @return The requested setting.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string or if the
     * setting type is not supported by this client.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultSetting getSetting(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException(
                    String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL_OR_EMPTY, "'name'"));
            }

            return transformToKeyVaultSetting(clientImpl.getSetting(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the value of a specific account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves a specific setting. Prints out the details of the response returned by the service and the retrieved
     * setting.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-RequestContext -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-RequestContext -->
     *
     * @param name The name of setting to retrieve.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the requested setting.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string or if the
     * setting type is not supported by this client.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultSetting> getSettingWithResponse(String name, RequestContext requestContext) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException(
                    String.format(KeyVaultAdministrationUtil.CANNOT_BE_NULL_OR_EMPTY, "'name'"));
            }

            Response<Setting> response = clientImpl.getSettingWithResponse(name, requestContext);

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                transformToKeyVaultSetting(response.getValue()));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the account's settings.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves all the settings for an account and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     *
     * @return A result object containing a list of the account's settings.
     *
     * @throws IllegalArgumentException If a setting type in the list is not supported by this client.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultGetSettingsResult getSettings() {
        try {
            List<KeyVaultSetting> keyVaultSettings = clientImpl.getSettings()
                .getSettings()
                .stream()
                .map(KeyVaultSettingsClient::transformToKeyVaultSetting)
                .collect(Collectors.toList());

            return new KeyVaultGetSettingsResult(keyVaultSettings);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the account's settings.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves all the settings for an account and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     *
     * @return A result object containing a list of the account's settings.
     *
     * @throws IllegalArgumentException If a setting type in the list is not supported by this client.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultGetSettingsResult> getSettingsWithResponse(RequestContext requestContext) {
        try {
            Response<SettingsListResult> response = clientImpl.getSettingsWithResponse(requestContext);
            List<KeyVaultSetting> keyVaultSettings = response.getValue()
                .getSettings()
                .stream()
                .map(KeyVaultSettingsClient::transformToKeyVaultSetting)
                .collect(Collectors.toList());

            return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                new KeyVaultGetSettingsResult(keyVaultSettings));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    private static KeyVaultSetting transformToKeyVaultSetting(Setting setting) {
        if (KeyVaultSettingType.BOOLEAN.toString().equalsIgnoreCase(setting.getType().toString())) {
            return new KeyVaultSetting(setting.getName(), Boolean.parseBoolean(setting.getValue()));
        } else {
            throw new IllegalArgumentException(
                String.format("Could not deserialize setting with name '%s'. Type '%s' is not supported.",
                    setting.getName(), setting.getType()));
        }
    }
}
