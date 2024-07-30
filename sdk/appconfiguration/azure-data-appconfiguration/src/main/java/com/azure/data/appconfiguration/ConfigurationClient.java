// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.implementation.AzureAppConfigurationImpl;
import com.azure.data.appconfiguration.implementation.CreateSnapshotUtilClient;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.implementation.models.DeleteKeyValueHeaders;
import com.azure.data.appconfiguration.implementation.models.GetKeyValueHeaders;
import com.azure.data.appconfiguration.implementation.models.GetSnapshotHeaders;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.PutKeyValueHeaders;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.Label;
import com.azure.data.appconfiguration.models.LabelFields;
import com.azure.data.appconfiguration.models.LabelSelector;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotFields;
import com.azure.data.appconfiguration.models.SnapshotSelector;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.toConfigurationSettingWithPagedResponse;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.toConfigurationSettingWithResponse;
import static com.azure.data.appconfiguration.implementation.Utility.ETAG_ANY;
import static com.azure.data.appconfiguration.implementation.Utility.getETag;
import static com.azure.data.appconfiguration.implementation.Utility.getPageETag;
import static com.azure.data.appconfiguration.implementation.Utility.handleNotModifiedErrorToValidResponse;
import static com.azure.data.appconfiguration.implementation.Utility.toKeyValue;
import static com.azure.data.appconfiguration.implementation.Utility.toSettingFieldsList;
import static com.azure.data.appconfiguration.implementation.Utility.updateSnapshotSync;
import static com.azure.data.appconfiguration.implementation.Utility.validateSetting;

/**
 * <p>This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings},
 * {@link FeatureFlagConfigurationSetting FeatureFlagConfigurationSetting} or
 * {@link SecretReferenceConfigurationSetting SecretReferenceConfigurationSetting} in Azure App Configuration Store.
 * Operations allowed by the client are adding, retrieving, deleting, set read-only status ConfigurationSettings, and
 * listing settings or revision of a setting based on a {@link SettingSelector filter}.</p>
 *
 * <p>Additionally, this class allows to add an external synchronization token to ensure service requests receive
 * up-to-date values. Use the {@link #updateSyncToken(String) updateSyncToken} method.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the App Configuration service you'll need to create an instance of the
 * {@link ConfigurationClient} class. To make this possible you'll need the connection
 * string of the configuration store. Alternatively, you can use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable"> Azure Identity</a>
 * to connect to the service.</p>
 * <ol>
 *   <li>Connection string, see {@link ConfigurationClientBuilder#connectionString(String) connectionString}.</li>
 *   <li>Azure Active Directory, see {@link ConfigurationClientBuilder#credential(TokenCredential) TokenCredential}.</li>
 * </ol>
 *
 * <p><strong>Instantiating a synchronous Configuration Client</strong></p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.instantiation -->
 * <pre>
 * ConfigurationClient configurationClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.instantiation -->
 *
 * <p>View {@link ConfigurationClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>App Configuration support multiple operations, such as create, update, retrieve, and delete a configuration setting.
 * See methods in client level class below to explore all capabilities that library provides.</p>
 *
 * <p>For more configuration setting types, see
 * {@link FeatureFlagConfigurationSetting} and
 * {@link SecretReferenceConfigurationSetting}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Add Configuration Setting</h2>
 *
 * <p>The {@link ConfigurationClient#addConfigurationSetting(ConfigurationSetting)}
 * method can be used to add a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to add a setting with the key "prodDBConnection", label "westUS" and value
 * "db_connection" using {@link ConfigurationClient}.</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.addConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;
 *     .setValue&#40;&quot;db_connection&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update Configuration Setting</h2>
 *
 * <p>The {@link ConfigurationClient#setConfigurationSetting(ConfigurationSetting)}
 * method can be used to update a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to update setting's value "db_connection" to "updated_db_connection"</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;
 *     .setValue&#40;&quot;db_connection&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 *
 * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;.
 * setting = configurationClient.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;
 *     .setValue&#40;&quot;updated_db_connection&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get Configuration Setting</h2>
 *
 * <p>The {@link ConfigurationClient#getConfigurationSetting(ConfigurationSetting)}
 * method can be used to get a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to retrieve the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.getConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete Configuration Setting</h2>
 *
 * <p>The {@link ConfigurationClient#deleteConfigurationSetting(ConfigurationSetting)}
 * method can be used to delete a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to delete the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.deleteConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;&#41;;
 * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Set the Configuration Setting to read-only</h2>
 *
 * <p>The {@link ConfigurationClient#setReadOnly(ConfigurationSetting, boolean)}
 * method can be used to conditionally set a configuration setting to read-only in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to conditionally set the setting to read-only with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;,
 *     true&#41;;
 * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Clear read-only of the Configuration Setting</h2>
 *
 * <p>The {@link ConfigurationClient#setReadOnly(ConfigurationSetting, boolean)}
 * method can be used to conditionally clear read-only of the setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to conditionally clear read-only of the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
 * <pre>
 * ConfigurationSetting setting = configurationClient.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
 *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *     .setLabel&#40;&quot;westUS&quot;&#41;,
 *     false&#41;;
 * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>List Configuration Settings</h2>
 *
 * <p>The {@link ConfigurationClient#listConfigurationSettings(SettingSelector)}
 * method can be used to list configuration settings in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to list all settings that use the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector -->
 * <pre>
 * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
 * configurationClient.listConfigurationSettings&#40;settingSelector&#41;.forEach&#40;setting -&gt; &#123;
 *     System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>List revisions of a Configuration Setting</h2>
 *
 * <p>The {@link ConfigurationClient#listRevisions(SettingSelector)}
 * method can be used to list all revisions of a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to list all revision of a setting that use the key "prodDBConnection".</p>
 * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector -->
 * <pre>
 * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
 * configurationClient.listRevisions&#40;settingSelector&#41;.streamByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
 *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
 *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
 *     resp.getItems&#40;&#41;.forEach&#40;value -&gt; &#123;
 *         System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;;
 *     &#125;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link ConfigurationAsyncClient}.</p>
 *
 * @see ConfigurationClientBuilder
 * @see ConfigurationSetting
 */
@ServiceClient(builder = ConfigurationClientBuilder.class,
    serviceInterfaces = AzureAppConfigurationImpl.AzureAppConfigurationService.class)
public final class ConfigurationClient {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationClient.class);
    private final AzureAppConfigurationImpl serviceClient;
    private final SyncTokenPolicy syncTokenPolicy;

    final CreateSnapshotUtilClient createSnapshotUtilClient;

    /**
     * Creates a ConfigurationClient that sends requests to the configuration service at {@code serviceEndpoint}. Each
     * service call goes through the {@code pipeline}.
     *
     * @param serviceClient The {@link AzureAppConfigurationImpl} that the client routes its request through.
     * @param syncTokenPolicy {@link SyncTokenPolicy} to be used to update the external synchronization token to ensure
     * service requests receive up-to-date values.
     */
    ConfigurationClient(AzureAppConfigurationImpl serviceClient, SyncTokenPolicy syncTokenPolicy) {
        this.serviceClient = serviceClient;
        this.syncTokenPolicy = syncTokenPolicy;
        this.createSnapshotUtilClient = new CreateSnapshotUtilClient(serviceClient);
    }

    /**
     * Gets the service endpoint for the Azure App Configuration instance.
     *
     * @return the service endpoint for the Azure App Configuration instance.
     */
    public String getEndpoint() {
        return serviceClient.getEndpoint();
    }

    /**
     * Adds a configuration value in the service if that key does not exist. The {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS" and value "db_connection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#String-String-String -->
     * <pre>
     * ConfigurationSetting result = configurationClient
     *     .addConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, &quot;db_connection&quot;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#String-String-String -->
     *
     * @param key The key of the configuration setting to add.
     * @param label The label of the configuration setting to create. If {@code null} no label will be used.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting addConfigurationSetting(String key, String label, String value) {
        return addConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey(key).setLabel(label).setValue(value), Context.NONE).getValue();
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS" and value "db_connection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * ConfigurationSetting setting = configurationClient.addConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;
     *     .setValue&#40;&quot;db_connection&quot;&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to add based on its key and optional label combination.
     *
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw ServiceRequestException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting addConfigurationSetting(ConfigurationSetting setting) {
        return addConfigurationSettingWithResponse(setting, Context.NONE).getValue();
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSettingWithResponse#ConfigurationSetting-Context -->
     * <pre>
     * Response&lt;ConfigurationSetting&gt; responseResultSetting = configurationClient
     *     .addConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *             .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *             .setLabel&#40;&quot;westUS&quot;&#41;
     *             .setValue&#40;&quot;db_connection&quot;&#41;,
     *         new Context&#40;key1, value1&#41;&#41;;
     * ConfigurationSetting resultSetting = responseResultSetting.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, resultSetting.getKey&#40;&#41;, resultSetting.getLabel&#40;&#41;,
     *     resultSetting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSettingWithResponse#ConfigurationSetting-Context -->
     *
     * @param setting The setting to add based on its key and optional label combination.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the the {@link ConfigurationSetting} that was created, or {@code null}, if a
     * key collision occurs or the key is an invalid value (which will also throw ServiceRequestException described
     * below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> addConfigurationSettingWithResponse(ConfigurationSetting setting,
        Context context) {
        validateSetting(setting);
        // This service method call is similar to setConfigurationSetting except we're passing If-Not-Match = "*".
        // If the service finds any existing configuration settings, then its e-tag will match and the service will
        // return an error.
        final ResponseBase<PutKeyValueHeaders, KeyValue> response =
            serviceClient.putKeyValueWithResponse(setting.getKey(), setting.getLabel(), null, ETAG_ANY,
                toKeyValue(setting), context);
        return toConfigurationSettingWithResponse(response);
    }

    /**
     * Creates or updates a configuration value in the service with the given key and. the {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", "westUS" and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#String-String-String -->
     * <pre>
     * ConfigurationSetting result = configurationClient
     *     .setConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, &quot;db_connection&quot;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     *
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;.
     * result = configurationClient.setConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, &quot;updated_db_connection&quot;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#String-String-String -->
     *
     * @param key The key of the configuration setting to create or update.
     * @param label The label of the configuration setting to create or update. If {@code null} no label will be used.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null} if the key is an invalid
     * value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting setConfigurationSetting(String key, String label, String value) {
        return setConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey(key).setLabel(label).setValue(value), false, Context.NONE).getValue();
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * ConfigurationSetting setting = configurationClient.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;
     *     .setValue&#40;&quot;db_connection&quot;&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     *
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;.
     * setting = configurationClient.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;
     *     .setValue&#40;&quot;updated_db_connection&quot;&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getLabel&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to create or update based on its key, optional label and optional ETag combination.
     *
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null} if the key is an invalid
     * value (which will also throw ServiceRequestException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#getETag() ETag} was specified, is not the
     * wildcard character, and the current configuration value's ETag does not match, or the setting exists and is
     * read-only.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting setConfigurationSetting(ConfigurationSetting setting) {
        return setConfigurationSettingWithResponse(setting, false, Context.NONE).getValue();
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     * <p>
     * If {@link ConfigurationSetting#getETag() ETag} is specified, the configuration value is updated if the current
     * setting's ETag matches. If the ETag's value is equal to the wildcard character ({@code "*"}), the setting will
     * always be updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context -->
     * <pre>
     * &#47;&#47; Add a setting with the key &quot;prodDBConnection&quot;, label &quot;westUS&quot;, and value &quot;db_connection&quot;
     * Response&lt;ConfigurationSetting&gt; responseSetting = configurationClient.setConfigurationSettingWithResponse&#40;
     *     new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;db_connection&quot;&#41;,
     *     false,
     *     new Context&#40;key2, value2&#41;&#41;;
     * ConfigurationSetting initSetting = responseSetting.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, initSetting.getKey&#40;&#41;, initSetting.getValue&#40;&#41;&#41;;
     *
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;.
     * responseSetting = configurationClient.setConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;updated_db_connection&quot;&#41;,
     *     false,
     *     new Context&#40;key2, value2&#41;&#41;;
     * ConfigurationSetting updatedSetting = responseSetting.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, updatedSetting.getKey&#40;&#41;, updatedSetting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context -->
     *
     * @param setting The setting to create or update based on its key, optional label and optional ETag combination.
     * @param ifUnchanged A boolean indicates if {@code setting} {@link ConfigurationSetting#getETag ETag} is used as an
     * IF-MATCH header.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response contains the {@link ConfigurationSetting} that was created or updated, or {@code null},
     * if the configuration value does not exist or the key is an invalid value (which will also throw
     * ServiceRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#getETag() ETag} was specified, is not the
     * wildcard character, and the current configuration value's ETag does not match, or the setting exists and is
     * read-only.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> setConfigurationSettingWithResponse(ConfigurationSetting setting,
        boolean ifUnchanged, Context context) {
        validateSetting(setting);
        final ResponseBase<PutKeyValueHeaders, KeyValue> response =
            serviceClient.putKeyValueWithResponse(setting.getKey(), setting.getLabel(), getETag(ifUnchanged, setting),
                null, toKeyValue(setting), context);
        return toConfigurationSettingWithResponse(response);
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, and the optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string -->
     * <pre>
     * ConfigurationSetting resultNoDateTime = configurationClient.getConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, resultNoDateTime.getKey&#40;&#41;, resultNoDateTime.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string -->
     *
     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to retrieve. If {@code null} no label will be used.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(String key, String label) {
        return getConfigurationSetting(key, label, null);
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, the optional {@code label}, and the optional
     * {@code acceptDateTime} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string-OffsetDateTime -->
     * <pre>
     * ConfigurationSetting result =
     *     configurationClient.getConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, null&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string-OffsetDateTime -->
     *
     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to create or update. If {@code null} no label will be used.
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null}
     * then the current state of the configuration setting will be returned.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(String key, String label, OffsetDateTime acceptDateTime) {
        return getConfigurationSettingWithResponse(new ConfigurationSetting().setKey(key).setLabel(label),
            acceptDateTime, false, Context.NONE).getValue();
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code acceptDateTime} and optional ETag combination.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * ConfigurationSetting setting = configurationClient.getConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to retrieve.
     *
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw ServiceRequestException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting getConfigurationSetting(ConfigurationSetting setting) {
        return getConfigurationSettingWithResponse(setting, null, false, Context.NONE).getValue();
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code acceptDateTime} and optional ETag combination.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean-Context -->
     * <pre>
     * &#47;&#47; Retrieve the setting with the key-label &quot;prodDBConnection&quot;-&quot;westUS&quot;.
     * Response&lt;ConfigurationSetting&gt; responseResultSetting = configurationClient.getConfigurationSettingWithResponse&#40;
     *     new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *     null,
     *     false,
     *     new Context&#40;key1, value1&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, responseResultSetting.getValue&#40;&#41;.getKey&#40;&#41;,
     *     responseResultSetting.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean-Context -->
     *
     * @param setting The setting to retrieve.
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null}
     * then the current state of the configuration setting will be returned.
     * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as an
     * If-None-Match header.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response contains the {@link ConfigurationSetting} stored in the service, or {@code null}, if the
     * configuration value does not exist or the key is an invalid value (which will also throw ServiceRequestException
     * described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> getConfigurationSettingWithResponse(ConfigurationSetting setting,
        OffsetDateTime acceptDateTime, boolean ifChanged, Context context) {
        validateSetting(setting);
        try {
            final ResponseBase<GetKeyValueHeaders, KeyValue> response =
                serviceClient.getKeyValueWithResponse(setting.getKey(), setting.getLabel(),
                    acceptDateTime == null ? null : acceptDateTime.toString(), null, getETag(ifChanged, setting), null,
                    context);
            return toConfigurationSettingWithResponse(response);
        } catch (HttpResponseException ex) {
            final HttpResponse httpResponse = ex.getResponse();
            if (httpResponse.getStatusCode() == 304) {
                return new ResponseBase<Void, ConfigurationSetting>(httpResponse.getRequest(),
                    httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null);
            }
            throw LOGGER.logExceptionAsError(ex);
        }
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@code key} and optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#string-string -->
     * <pre>
     * ConfigurationSetting result = configurationClient.deleteConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#string-string -->
     *
     * @param key The key of configuration setting to delete.
     * @param label The label of configuration setting to delete. If {@code null} no label will be used.
     * @return The deleted ConfigurationSetting or {@code null} if it didn't exist. {@code null} is also returned if the
     * {@code key} is an invalid value (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting deleteConfigurationSetting(String key, String label) {
        return deleteConfigurationSettingWithResponse(new ConfigurationSetting().setKey(key).setLabel(label),
            false, Context.NONE).getValue();
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * ConfigurationSetting setting = configurationClient.deleteConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     *
     * @return The deleted ConfigurationSetting or {@code null} if it didn't exist. {@code null} is also returned if the
     * {@code key} is an invalid value (which will also throw ServiceRequestException described below).
     *
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting deleteConfigurationSetting(ConfigurationSetting setting) {
        return deleteConfigurationSettingWithResponse(setting, false, Context.NONE).getValue();
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     * <p>
     * If {@link ConfigurationSetting#getETag() ETag} is specified and is not the wildcard character ({@code "*"}), then
     * the setting is <b>only</b> deleted if the ETag matches the current ETag; this means that no one has updated the
     * ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context -->
     * <pre>
     * Response&lt;ConfigurationSetting&gt; responseSetting = configurationClient.deleteConfigurationSettingWithResponse&#40;
     *     new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *     false,
     *     new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;
     *     &quot;Key: %s, Value: %s&quot;, responseSetting.getValue&#40;&#41;.getKey&#40;&#41;, responseSetting.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context -->
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as an
     * IF-MATCH header.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the deleted ConfigurationSetting or {@code null} if didn't exist. {@code null}
     * is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value or
     * {@link ConfigurationSetting#getETag() ETag} is set but does not match the current ETag
     * (which will also throw ServiceRequestException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> deleteConfigurationSettingWithResponse(ConfigurationSetting setting,
        boolean ifUnchanged, Context context) {
        validateSetting(setting);
        final ResponseBase<DeleteKeyValueHeaders, KeyValue> response =
            serviceClient.deleteKeyValueWithResponse(setting.getKey(), setting.getLabel(),
                getETag(ifUnchanged, setting), context);
        return toConfigurationSettingWithResponse(response);
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting} that matches the {@code key}, the optional
     * {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean -->
     * <pre>
     * ConfigurationSetting result = configurationClient.setReadOnly&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, true&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean-clearReadOnly -->
     * <pre>
     * ConfigurationSetting result = configurationClient.setReadOnly&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, false&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean-clearReadOnly -->
     *
     * @param key The key of configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param label The label of configuration setting to set to read-only or not read-only based on the
     * {@code isReadOnly} value, or optionally. If {@code null} no label will be used.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     * @return The {@link ConfigurationSetting} that is read-only, or {@code null} is also returned if a key collision
     * occurs or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting setReadOnly(String key, String label, boolean isReadOnly) {
        return setReadOnlyWithResponse(new ConfigurationSetting().setKey(key).setLabel(label), isReadOnly, Context.NONE)
            .getValue();
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting}.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean -->
     * <pre>
     * ConfigurationSetting setting = configurationClient.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;,
     *     true&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
     * <pre>
     * ConfigurationSetting setting = configurationClient.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
     *     .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *     .setLabel&#40;&quot;westUS&quot;&#41;,
     *     false&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
     *
     * @param setting The configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     *
     * @return The {@link ConfigurationSetting} that is read-only, or {@code null} is also returned if a key collision
     * occurs or the key is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSetting setReadOnly(ConfigurationSetting setting, boolean isReadOnly) {
        return setReadOnlyWithResponse(setting, isReadOnly, Context.NONE).getValue();
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting}.
     * <p>
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-Boolean-Context -->
     * <pre>
     * ConfigurationSetting resultSetting = configurationClient.setReadOnlyWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         true,
     *         Context.NONE&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, resultSetting.getKey&#40;&#41;, resultSetting.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-Boolean-Context -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-Context-ClearReadOnly -->
     * <pre>
     * Response&lt;ConfigurationSetting&gt; responseSetting = configurationClient
     *     .setConfigurationSettingWithResponse&#40;
     *         new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, false,
     *         new Context&#40;key2, value2&#41;&#41;;
     * System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, responseSetting.getValue&#40;&#41;.getKey&#40;&#41;,
     *     responseSetting.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-Context-ClearReadOnly -->
     *
     * @param setting The configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A REST response containing the read-only or not read-only ConfigurationSetting if {@code isReadOnly}
     * is true or null, or false respectively. Or return {@code null} if the setting didn't exist.
     * {@code null} is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value.
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSetting> setReadOnlyWithResponse(ConfigurationSetting setting, boolean isReadOnly,
        Context context) {
        validateSetting(setting);
        final String key = setting.getKey();
        final String label = setting.getLabel();

        return isReadOnly
            ? toConfigurationSettingWithResponse(serviceClient.putLockWithResponse(key, label, null, null, context))
            : toConfigurationSettingWithResponse(serviceClient.deleteLockWithResponse(key, label, null, null, context));
    }

    /**
     * Fetches the configuration settings that match the {@code selector}. If {@code selector} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector -->
     * <pre>
     * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
     * configurationClient.listConfigurationSettings&#40;settingSelector&#41;.forEach&#40;setting -&gt; &#123;
     *     System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector -->
     *
     * @param selector Optional. Selector to filter configuration setting results from the service.
     * @return A {@link PagedIterable} of ConfigurationSettings that matches the {@code selector}. If no options were
     * provided, the List contains all of the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listConfigurationSettings(SettingSelector selector) {
        return listConfigurationSettings(selector, Context.NONE);
    }

    /**
     * Fetches the configuration settings that match the {@code selector}. If {@code selector} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector-context -->
     * <pre>
     * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     * configurationClient.listConfigurationSettings&#40;settingSelector, ctx&#41;.forEach&#40;setting -&gt; &#123;
     *     System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector-context -->
     *
     * @param selector Optional. Selector to filter configuration setting results from the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of ConfigurationSettings that matches the {@code selector}. If no options were
     * provided, the {@link PagedIterable} contains all the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listConfigurationSettings(SettingSelector selector, Context context) {
        final String keyFilter = selector == null ? null : selector.getKeyFilter();
        final String labelFilter = selector == null ? null : selector.getLabelFilter();
        final String acceptDateTime = selector == null ? null : selector.getAcceptDateTime();
        final List<SettingFields> settingFields = selector == null ? null : toSettingFieldsList(selector.getFields());
        final List<MatchConditions> matchConditionsList = selector == null ? null : selector.getMatchConditions();
        final List<String> tagsFilter = selector == null ? null : selector.getTagsFilter();

        AtomicInteger pageETagIndex = new AtomicInteger(0);
        return new PagedIterable<>(
                () -> {
                    PagedResponse<KeyValue> pagedResponse;
                    try {
                        pagedResponse = serviceClient.getKeyValuesSinglePage(keyFilter, labelFilter, null, acceptDateTime,
                            settingFields, null, null, getPageETag(matchConditionsList, pageETagIndex),
                            tagsFilter, context);
                    } catch (HttpResponseException ex) {
                        return handleNotModifiedErrorToValidResponse(ex, LOGGER);
                    }
                    return toConfigurationSettingWithPagedResponse(pagedResponse);
                },
                nextLink -> {
                    PagedResponse<KeyValue> pagedResponse;
                    try {
                        pagedResponse = serviceClient.getKeyValuesNextSinglePage(nextLink, acceptDateTime, null,
                            getPageETag(matchConditionsList, pageETagIndex), context);
                    } catch (HttpResponseException ex) {
                        return handleNotModifiedErrorToValidResponse(ex, LOGGER);
                    }
                    return toConfigurationSettingWithPagedResponse(pagedResponse);
                }
        );
    }

    /**
     * Fetches the configuration settings in a snapshot that matches the {@code snapshotName}. If {@code snapshotName}
     * is {@code null}, then all the {@link ConfigurationSetting configuration settings} are fetched with their current
     * values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listConfigurationSettingsForSnapshot -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * configurationClient.listConfigurationSettingsForSnapshot&#40;snapshotName&#41;.forEach&#40;setting -&gt; &#123;
     *     System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.listConfigurationSettingsForSnapshot -->
     *
     * @param snapshotName Optional. A filter used get {@link ConfigurationSetting}s for a snapshot. The value should
     * be the name of the snapshot.
     * @return A {@link PagedIterable} of ConfigurationSettings that matches the {@code selector}. If no options were
     * provided, the List contains all of the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listConfigurationSettingsForSnapshot(String snapshotName) {
        return listConfigurationSettingsForSnapshot(snapshotName, null, Context.NONE);
    }

    /**
     * Fetches the configuration settings in a snapshot that matches the {@code snapshotName}. If {@code snapshotName}
     * is {@code null}, then all the {@link ConfigurationSetting configuration settings} are fetched with their current
     * values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listConfigurationSettingsForSnapshotMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * List&lt;SettingFields&gt; fields = Arrays.asList&#40;SettingFields.KEY&#41;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     * configurationClient.listConfigurationSettingsForSnapshot&#40;snapshotName, fields, ctx&#41;
     *     .forEach&#40;setting -&gt; System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.listConfigurationSettingsForSnapshotMaxOverload -->
     *
     * @param snapshotName Optional. A filter used get {@link ConfigurationSetting}s for a snapshot. The value should
     * be the name of the snapshot.
     * @param fields Optional. The fields to select for the query response. If none are set, the service will return the
     * ConfigurationSettings with a default set of properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of ConfigurationSettings that matches the {@code selector}. If no options were
     * provided, the {@link PagedIterable} contains all the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listConfigurationSettingsForSnapshot(String snapshotName,
        List<SettingFields> fields, Context context) {
        return new PagedIterable<>(() -> {
            final PagedResponse<KeyValue> pagedResponse = serviceClient.getKeyValuesSinglePage(null, null, null, null,
                fields, snapshotName, null, null, null, context);
            return toConfigurationSettingWithPagedResponse(pagedResponse);
        }, nextLink -> {
            final PagedResponse<KeyValue> pagedResponse = serviceClient.getKeyValuesNextSinglePage(nextLink, null, null,
                null, context);
            return toConfigurationSettingWithPagedResponse(pagedResponse);
        });
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date.
     * Revisions expire after a period of time, see <a href="https://azure.microsoft.com/pricing/details/app-configuration/">Pricing</a>
     * for more information.
     * <p>
     * If {@code selector} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code selector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all revisions of the setting that has the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector -->
     * <pre>
     * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
     * configurationClient.listRevisions&#40;settingSelector&#41;.streamByPage&#40;&#41;.forEach&#40;resp -&gt; &#123;
     *     System.out.printf&#40;&quot;Response headers are %s. Url %s  and status code %d %n&quot;, resp.getHeaders&#40;&#41;,
     *         resp.getRequest&#40;&#41;.getUrl&#40;&#41;, resp.getStatusCode&#40;&#41;&#41;;
     *     resp.getItems&#40;&#41;.forEach&#40;value -&gt; &#123;
     *         System.out.printf&#40;&quot;Response value is %d %n&quot;, value&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector -->
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return {@link PagedIterable} of {@link ConfigurationSetting} revisions.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listRevisions(SettingSelector selector) {
        return listRevisions(selector, Context.NONE);
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date.
     * Revisions expire after a period of time, see <a href="https://azure.microsoft.com/pricing/details/app-configuration/">Pricing</a>
     * for more information.
     * <p>
     * If {@code selector} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code selector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all revisions of the setting that has the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector-context -->
     * <pre>
     * SettingSelector settingSelector = new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     * configurationClient.listRevisions&#40;settingSelector, ctx&#41;.forEach&#40;setting -&gt; &#123;
     *     System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector-context -->
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link ConfigurationSetting} revisions.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSetting> listRevisions(SettingSelector selector, Context context) {
        final String acceptDateTime = selector == null ? null : selector.getAcceptDateTime();
        return new PagedIterable<>(() -> {
            final PagedResponse<KeyValue> pagedResponse = serviceClient.getRevisionsSinglePage(
                selector == null ? null : selector.getKeyFilter(), selector == null ? null : selector.getLabelFilter(),
                null, acceptDateTime, selector == null ? null : toSettingFieldsList(selector.getFields()),
                    selector == null ? null : selector.getTagsFilter(), context);
            return toConfigurationSettingWithPagedResponse(pagedResponse);
        }, nextLink -> {
            final PagedResponse<KeyValue> pagedResponse = serviceClient.getRevisionsNextSinglePage(nextLink,
                acceptDateTime, context);
            return toConfigurationSettingWithPagedResponse(pagedResponse);
        });
    }

    /**
     * Create a {@link ConfigurationSnapshot} by providing a snapshot name and a
     * {@link ConfigurationSnapshot}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.beginCreateSnapshotMaxOverload -->
     * <pre>
     * List&lt;ConfigurationSettingsFilter&gt; filters = new ArrayList&lt;&gt;&#40;&#41;;
     * &#47;&#47; Key Name also supports RegExp but only support prefix end with &quot;*&quot;, such as &quot;k*&quot; and is case-sensitive.
     * filters.add&#40;new ConfigurationSettingsFilter&#40;&quot;&#123;keyName&#125;&quot;&#41;&#41;;
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     *
     * SyncPoller&lt;PollOperationDetails, ConfigurationSnapshot&gt; poller =
     *     client.beginCreateSnapshot&#40;snapshotName,
     *         new ConfigurationSnapshot&#40;filters&#41;.setRetentionPeriod&#40;Duration.ofHours&#40;1&#41;&#41;, ctx&#41;;
     * poller.setPollInterval&#40;Duration.ofSeconds&#40;10&#41;&#41;;
     * poller.waitForCompletion&#40;&#41;;
     * ConfigurationSnapshot snapshot = poller.getFinalResult&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Snapshot name=%s is created at %s%n&quot;, snapshot.getName&#40;&#41;, snapshot.getCreatedAt&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.beginCreateSnapshotMaxOverload -->
     *
     * @param snapshotName The name of the {@link ConfigurationSnapshot} to create.
     * @param snapshot The snapshot to create.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link SyncPoller} that polls the creating snapshot operation until it has completed or
     * has failed. The completed operation returns a {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PollOperationDetails, ConfigurationSnapshot> beginCreateSnapshot(
        String snapshotName, ConfigurationSnapshot snapshot, Context context) {
        return createSnapshotUtilClient.beginCreateSnapshot(snapshotName, snapshot, context);
    }

    /**
     * Get a {@link ConfigurationSnapshot} by given the snapshot name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.getSnapshotByName -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * ConfigurationSnapshot getSnapshot = client.getSnapshot&#40;snapshotName&#41;;
     * System.out.printf&#40;&quot;Snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *     getSnapshot.getName&#40;&#41;, getSnapshot.getCreatedAt&#40;&#41;, getSnapshot.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.getSnapshotByName -->
     *
     * @param snapshotName the snapshot name.
     * @return A {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSnapshot getSnapshot(String snapshotName) {
        return getSnapshotWithResponse(snapshotName, null, Context.NONE).getValue();
    }

    /**
     * Get a {@link ConfigurationSnapshot} by given the snapshot name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.getSnapshotByNameMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     * ConfigurationSnapshot getSnapshot = client.getSnapshotWithResponse&#40;
     *     snapshotName,
     *     Arrays.asList&#40;SnapshotFields.NAME, SnapshotFields.CREATED_AT, SnapshotFields.STATUS, SnapshotFields.FILTERS&#41;,
     *     ctx&#41;
     *     .getValue&#40;&#41;;
     * &#47;&#47; Only properties `name`, `createAt`, `status` and `filters` have value, and expect null or
     * &#47;&#47; empty value other than the `fields` specified in the request.
     * System.out.printf&#40;&quot;Snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *     getSnapshot.getName&#40;&#41;, getSnapshot.getCreatedAt&#40;&#41;, getSnapshot.getStatus&#40;&#41;&#41;;
     * List&lt;ConfigurationSettingsFilter&gt; filters = getSnapshot.getFilters&#40;&#41;;
     * for &#40;ConfigurationSettingsFilter filter : filters&#41; &#123;
     *     System.out.printf&#40;&quot;Snapshot filter key=%s, label=%s.%n&quot;, filter.getKey&#40;&#41;, filter.getLabel&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.getSnapshotByNameMaxOverload -->
     *
     * @param snapshotName the snapshot name.
     * @param fields Used to select what fields are present in the returned resource(s).
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSnapshot> getSnapshotWithResponse(String snapshotName, List<SnapshotFields> fields,
                                                                          Context context) {
        final ResponseBase<GetSnapshotHeaders, ConfigurationSnapshot> response =
            serviceClient.getSnapshotWithResponse(snapshotName, null, null, fields, context);
        return new SimpleResponse<>(response, response.getValue());
    }

    /**
     * Update a snapshot status from {@link ConfigurationSnapshotStatus#READY} to {@link ConfigurationSnapshotStatus#ARCHIVED}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.archiveSnapshotByName -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * ConfigurationSnapshot archivedSnapshot = client.archiveSnapshot&#40;snapshotName&#41;;
     * System.out.printf&#40;&quot;Archived snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *     archivedSnapshot.getName&#40;&#41;, archivedSnapshot.getCreatedAt&#40;&#41;, archivedSnapshot.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.archiveSnapshotByName -->
     *
     * @param snapshotName the snapshot name.
     * @return A {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSnapshot archiveSnapshot(String snapshotName) {
        return updateSnapshotSync(snapshotName, null, ConfigurationSnapshotStatus.ARCHIVED, serviceClient,
            Context.NONE).getValue();
    }

    /**
     * Update a snapshot status from {@link ConfigurationSnapshotStatus#READY} to {@link ConfigurationSnapshotStatus#ARCHIVED}.
     *
     * <p>
     * To turn on using 'if-match' header, set the second parameter 'ifUnchanged' to true.
     * It used to perform an operation only if the targeted resource's ETag matches the value provided.
     * Otherwise, it will throw an exception '412 Precondition Failed'.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.archiveSnapshotByNameMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * MatchConditions matchConditions = new MatchConditions&#40;&#41;.setIfMatch&#40;&quot;&#123;etag&#125;&quot;&#41;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     *
     * ConfigurationSnapshot archivedSnapshot = client.archiveSnapshotWithResponse&#40;snapshotName, matchConditions, ctx&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Archived snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *     archivedSnapshot.getName&#40;&#41;, archivedSnapshot.getCreatedAt&#40;&#41;, archivedSnapshot.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.archiveSnapshotByNameMaxOverload -->
     *
     * @param snapshotName the snapshot name.
     * @param matchConditions Specifies HTTP options for conditional requests.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSnapshot> archiveSnapshotWithResponse(String snapshotName,
        MatchConditions matchConditions, Context context) {
        return updateSnapshotSync(snapshotName, matchConditions, ConfigurationSnapshotStatus.ARCHIVED, serviceClient,
            context);
    }

    /**
     * Update a snapshot status from {@link ConfigurationSnapshotStatus#ARCHIVED} to {@link ConfigurationSnapshotStatus#READY}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.recoverSnapshotByName -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * ConfigurationSnapshot recoveredSnapshot = client.recoverSnapshot&#40;snapshotName&#41;;
     * System.out.printf&#40;&quot;Recovered snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *     recoveredSnapshot.getName&#40;&#41;, recoveredSnapshot.getCreatedAt&#40;&#41;, recoveredSnapshot.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.recoverSnapshotByName -->
     *
     * @param snapshotName the snapshot name.
     * @return A {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ConfigurationSnapshot recoverSnapshot(String snapshotName) {
        return updateSnapshotSync(snapshotName, null, ConfigurationSnapshotStatus.READY, serviceClient,
            Context.NONE).getValue();
    }

    /**
     * Update a snapshot status from {@link ConfigurationSnapshotStatus#ARCHIVED} to {@link ConfigurationSnapshotStatus#READY}.
     *
     * <p>
     * To turn on using 'if-match' header, set the second parameter 'ifUnchanged' to true.
     * It used to perform an operation only if the targeted resource's ETag matches the value provided.
     * Otherwise, it will throw an exception '412 Precondition Failed'.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.recoverSnapshotMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * MatchConditions matchConditions = new MatchConditions&#40;&#41;.setIfMatch&#40;&quot;&#123;etag&#125;&quot;&#41;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     *
     * ConfigurationSnapshot recoveredSnapshot = client.recoverSnapshotWithResponse&#40;snapshotName, matchConditions, ctx&#41;
     *     .getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Recovered snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *     recoveredSnapshot.getName&#40;&#41;, recoveredSnapshot.getCreatedAt&#40;&#41;, recoveredSnapshot.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.recoverSnapshotMaxOverload -->
     *
     * @param snapshotName the snapshot name.
     * @param matchConditions Specifies HTTP options for conditional requests.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ConfigurationSnapshot> recoverSnapshotWithResponse(String snapshotName,
        MatchConditions matchConditions, Context context) {
        return updateSnapshotSync(snapshotName, matchConditions, ConfigurationSnapshotStatus.READY, serviceClient,
            context);
    }

    /**
     * List snapshots by given {@link SnapshotSelector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.listSnapshots -->
     * <pre>
     * String snapshotNameFilter = &quot;&#123;snapshotNamePrefix&#125;*&quot;;
     * client.listSnapshots&#40;new SnapshotSelector&#40;&#41;.setNameFilter&#40;snapshotNameFilter&#41;&#41;
     *     .forEach&#40;snapshotResult -&gt; &#123;
     *         System.out.printf&#40;&quot;Listed Snapshot name = %s is created at %s, snapshot status is %s.%n&quot;,
     *             snapshotResult.getName&#40;&#41;, snapshotResult.getCreatedAt&#40;&#41;, snapshotResult.getStatus&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.listSnapshots -->
     *
     * @param selector Optional. Used to filter {@link ConfigurationSnapshot} from the service.
     * @return A {@link PagedIterable} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSnapshot> listSnapshots(SnapshotSelector selector) {
        return listSnapshots(selector, Context.NONE);
    }

    /**
     * List snapshots by given {@link SnapshotSelector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.listSnapshotsMaxOverload -->
     * <pre>
     * String snapshotNameFilter = &quot;&#123;snapshotNamePrefix&#125;*&quot;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     *
     * client.listSnapshots&#40;new SnapshotSelector&#40;&#41;.setNameFilter&#40;snapshotNameFilter&#41;, ctx&#41;
     *     .forEach&#40;snapshotResult -&gt; &#123;
     *         System.out.printf&#40;&quot;Listed Snapshot name = %s is created at %s, snapshot status is %s.%n&quot;,
     *             snapshotResult.getName&#40;&#41;, snapshotResult.getCreatedAt&#40;&#41;, snapshotResult.getStatus&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.listSnapshotsMaxOverload -->
     *
     * @param selector Optional. Used to filter {@link ConfigurationSnapshot} from the service.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<ConfigurationSnapshot> listSnapshots(SnapshotSelector selector, Context context) {
        return new PagedIterable<>(() -> serviceClient.getSnapshotsSinglePage(
            selector == null ? null : selector.getNameFilter(), null, selector == null ? null : selector.getFields(),
            selector == null ? null : selector.getStatus(), context),
            nextLink -> serviceClient.getSnapshotsNextSinglePage(nextLink, context));
    }

    /**
     * Gets a list of labels by given {@link LabelSelector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.listLabels -->
     * <pre>
     * String labelNameFilter = &quot;&#123;labelNamePrefix&#125;*&quot;;
     * client.listLabels&#40;new LabelSelector&#40;&#41;.setNameFilter&#40;labelNameFilter&#41;&#41;
     *         .forEach&#40;label -&gt; &#123;
     *             System.out.println&#40;&quot;label name = &quot; + label.getName&#40;&#41;&#41;;
     *         &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.listLabels -->
     *
     * @param selector Optional. Selector to filter labels from the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of labels as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Label> listLabels(LabelSelector selector) {
        return listLabels(selector, Context.NONE);
    }

    /**
     * Gets a list of labels by given {@link LabelSelector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationclient.listLabelsMaxOverload -->
     * <pre>
     * String labelNameFilter = &quot;&#123;labelNamePrefix&#125;*&quot;;
     * Context ctx = new Context&#40;key2, value2&#41;;
     *
     * client.listLabels&#40;new LabelSelector&#40;&#41;.setNameFilter&#40;labelNameFilter&#41;, ctx&#41;
     *         .forEach&#40;label -&gt; &#123;
     *             System.out.println&#40;&quot;label name = &quot; + label.getName&#40;&#41;&#41;;
     *         &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationclient.listLabelsMaxOverload -->
     *
     * @param selector Optional. Selector to filter labels from the service.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of labels as paginated response with {@link PagedIterable}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<Label> listLabels(LabelSelector selector, Context context) {
        final String labelNameFilter = selector == null ? null : selector.getNameFilter();
        final String acceptDatetime = selector == null
            ? null : selector.getAcceptDateTime() == null ? null : selector.getAcceptDateTime().toString();
        final List<LabelFields> labelFields = selector == null ? null : selector.getFields();
        return serviceClient.getLabels(labelNameFilter, null, acceptDatetime, labelFields, context);
    }

    /**
     * Adds an external synchronization token to ensure service requests receive up-to-date values.
     *
     * @param token an external synchronization token to ensure service requests receive up-to-date values.
     * @throws NullPointerException if the given token is null.
     */
    public void updateSyncToken(String token) {
        syncTokenPolicy.updateSyncToken(token);
    }
}
