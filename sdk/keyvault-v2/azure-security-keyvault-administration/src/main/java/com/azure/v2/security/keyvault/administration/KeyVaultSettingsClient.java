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
 * <pre>
 * KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
 * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
 *
 * settings.forEach&#40;setting -&gt;
 *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
 *         setting.asBoolean&#40;&#41;&#41;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultSetting setting = keyVaultSettingsClient.getSetting&#40;&quot;&lt;setting-name&gt;&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;&quot;&lt;setting-name&gt;&quot;, true&#41;;
 * KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting&#40;settingToUpdate&#41;;
 *
 * System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;, updatedSetting.asBoolean&#40;&#41;&#41;;
 * </pre>
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
     * <pre>
     * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;&quot;&lt;setting-name&gt;&quot;, true&#41;;
     * KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting&#40;settingToUpdate&#41;;
     *
     * System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;, updatedSetting.asBoolean&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(setting, "'setting' cannot be null.");

        String settingValue = null;

        if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
            settingValue = Boolean.toString(setting.asBoolean());
        }

        return transformToKeyVaultSetting(clientImpl
            .updateSettingWithResponse(setting.getName(), new UpdateSettingRequest(settingValue), RequestContext.none())
            .getValue());
    }

    /**
     * Updates an account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates a given setting. Prints out the details of the response returned by the service and the updated
     * setting.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-RequestContext -->
     * <pre>
     * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;&quot;&lt;setting-name&gt;&quot;, true&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultSetting&gt; response =
     *     keyVaultSettingsClient.updateSettingWithResponse&#40;settingToUpdate, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Updated setting '%s' to '%s'.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(setting, "'setting' cannot be null.");

        String settingValue = null;

        if (setting.getType() == KeyVaultSettingType.BOOLEAN) {
            settingValue = Boolean.toString(setting.asBoolean());
        }

        Response<Setting> response = clientImpl.updateSettingWithResponse(setting.getName(),
            new UpdateSettingRequest(settingValue), requestContext);

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            transformToKeyVaultSetting(response.getValue()));
    }

    /**
     * Gets the value of a specific account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves a specific setting and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
     * <pre>
     * KeyVaultSetting setting = keyVaultSettingsClient.getSetting&#40;&quot;&lt;setting-name&gt;&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return transformToKeyVaultSetting(clientImpl.getSettingWithResponse(name, RequestContext.none()).getValue());
    }

    /**
     * Gets the value of a specific account setting.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves a specific setting. Prints out the details of the response returned by the service and the retrieved
     * setting.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettingWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultSetting&gt; response =
     *     keyVaultSettingsClient.getSettingWithResponse&#40;&quot;&lt;setting-name&gt;&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n&quot;,
     *     response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;, response.getValue&#40;&#41;.asBoolean&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        Response<Setting> response = clientImpl.getSettingWithResponse(name, requestContext);

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            transformToKeyVaultSetting(response.getValue()));
    }

    /**
     * Gets the account's settings.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves all the settings for an account and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     * <pre>
     * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
     * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
     *
     * settings.forEach&#40;setting -&gt;
     *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *         setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     *
     * @return A result object containing a list of the account's settings.
     *
     * @throws IllegalArgumentException If a setting type in the list is not supported by this client.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultGetSettingsResult getSettings() {
        List<KeyVaultSetting> keyVaultSettings = clientImpl.getSettingsWithResponse(RequestContext.none())
            .getValue()
            .getSettings()
            .stream()
            .map(KeyVaultSettingsClient::transformToKeyVaultSetting)
            .collect(Collectors.toList());

        return new KeyVaultGetSettingsResult(keyVaultSettings);
    }

    /**
     * Gets the account's settings.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves all the settings for an account and prints out each one's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     * <pre>
     * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
     * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
     *
     * settings.forEach&#40;setting -&gt;
     *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
     *         setting.asBoolean&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
     *
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A result object containing a list of the account's settings.
     *
     * @throws IllegalArgumentException If a setting type in the list is not supported by this client.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultGetSettingsResult> getSettingsWithResponse(RequestContext requestContext) {
        Response<SettingsListResult> response = clientImpl.getSettingsWithResponse(requestContext);
        List<KeyVaultSetting> keyVaultSettings = response.getValue()
            .getSettings()
            .stream()
            .map(KeyVaultSettingsClient::transformToKeyVaultSetting)
            .collect(Collectors.toList());

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new KeyVaultGetSettingsResult(keyVaultSettings));
    }

    private static KeyVaultSetting transformToKeyVaultSetting(Setting setting) {
        if (KeyVaultSettingType.BOOLEAN.toString().equalsIgnoreCase(setting.getType().toString())) {
            return new KeyVaultSetting(setting.getName(), Boolean.parseBoolean(setting.getValue()));
        } else {
            throw LOGGER.throwableAtError()
                .addKeyValue("settingName", setting.getName())
                .addKeyValue("settingType", setting.getType().getValue())
                .log("Could not deserialize setting.", IllegalArgumentException::new);
        }
    }
}
