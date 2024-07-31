// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.PollerFlux;
import com.azure.data.appconfiguration.implementation.AzureAppConfigurationImpl;
import com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper;
import com.azure.data.appconfiguration.implementation.CreateSnapshotUtilClient;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.implementation.Utility;
import com.azure.data.appconfiguration.implementation.models.GetKeyValueHeaders;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import com.azure.data.appconfiguration.models.ConfigurationSnapshotStatus;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingLabelSelector;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingFields;
import com.azure.data.appconfiguration.models.SettingLabel;
import com.azure.data.appconfiguration.models.SettingLabelFields;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.models.SnapshotFields;
import com.azure.data.appconfiguration.models.SnapshotSelector;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.data.appconfiguration.implementation.Utility.ETAG_ANY;
import static com.azure.data.appconfiguration.implementation.Utility.getETag;
import static com.azure.data.appconfiguration.implementation.Utility.getPageETag;
import static com.azure.data.appconfiguration.implementation.Utility.toKeyValue;
import static com.azure.data.appconfiguration.implementation.Utility.toSettingFieldsList;
import static com.azure.data.appconfiguration.implementation.Utility.updateSnapshotAsync;
import static com.azure.data.appconfiguration.implementation.Utility.validateSettingAsync;

/**
 * <p>This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings},
 * {@link FeatureFlagConfigurationSetting FeatureFlagConfigurationSetting} or
 * {@link SecretReferenceConfigurationSetting SecretReferenceConfigurationSetting} in Azure App Configuration Store.
 * Operations allowed by the client are adding, retrieving, deleting, set read-only status ConfigurationSettings, and
 * listing settings or revision of a setting based on a {@link SettingSelector filter}.</p>
 *
 * <p> Additionally, this class allows to add an external synchronization token to ensure service requests receive
 * up-to-date values. Use the {@link #updateSyncToken(String) updateSyncToken} method.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the App Configuration service you'll need to create an instance of the
 * {@link com.azure.data.appconfiguration.ConfigurationAsyncClient} class. To make this possible you'll need the
 * connection string of the configuration store. Alternatively, you can use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable"> Azure Identity</a>
 * to connect to the service.</p>
 * <ol>
 *   <li>Connection string, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#connectionString(java.lang.String) connectionString}.</li>
 *   <li>Azure Active Directory, see {@link com.azure.data.appconfiguration.ConfigurationClientBuilder#credential(com.azure.core.credential.TokenCredential) TokenCredential}.</li>
 * </ol>
 *
 * <p><strong>Instantiating an asynchronous Configuration Client</strong></p>
 *
 * <!-- src_embed com.azure.data.applicationconfig.async.configurationclient.instantiation -->
 * <pre>
 * ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.data.applicationconfig.async.configurationclient.instantiation -->
 *
 * <p>View {@link ConfigurationClientBuilder this} for additional ways to construct the client.</p>
 *
 * <p>App Configuration support multiple operations, such as create, update, retrieve, and delete a configuration setting.
 * See methods in client level class below to explore all capabilities that library provides.</p>
 *
 * <p>For more configuration setting types, see
 * {@link com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting} and
 * {@link com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Add Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#addConfigurationSetting(ConfigurationSetting)}
 * method can be used to add a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to add a setting with the key "prodDBConnection", label "westUS" and value
 * "db_connection" using {@link com.azure.data.appconfiguration.ConfigurationAsyncClient}.</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * client.addConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;
 *         .setValue&#40;&quot;db_connection&quot;&#41;&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
 *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Update Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#setConfigurationSetting(ConfigurationSetting)}
 * method can be used to update a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to update setting's value "db_connection" to "updated_db_connection"</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * client.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
 *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;
 * client.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;
 *         .setValue&#40;&quot;updated_db_connection&quot;&#41;&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
 *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Get Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#getConfigurationSetting(ConfigurationSetting)}
 * method can be used to get a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to retrieve the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * client.getConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
 *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Delete Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#deleteConfigurationSetting(ConfigurationSetting)}
 * method can be used to delete a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to delete the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting -->
 * <pre>
 * client.deleteConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
 *         response.getKey&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Set the Configuration Setting to read-only</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#setReadOnly(ConfigurationSetting, boolean)}
 * method can be used to conditionally set a configuration setting to read-only in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to conditionally set the setting to read-only with the key "prodDBConnection".</p>

 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean -->
 * <pre>
 * client.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;,
 *         true&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
 *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Clear read-only of the Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#setReadOnly(ConfigurationSetting, boolean)}
 * method can be used to conditionally clear read-only of the setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to conditionally clear read-only of the setting with the key "prodDBConnection".</p>
 *
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
 * <pre>
 * client.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
 *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
 *         .setLabel&#40;&quot;westUS&quot;&#41;,
 *         false&#41;
 *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, response.getKey&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>List Configuration Settings</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#listConfigurationSettings(SettingSelector)}
 * method can be used to list configuration settings in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to list all settings that use the key "prodDBConnection".</p>
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettings -->
 * <pre>
 * client.listConfigurationSettings&#40;new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;&#41;
 *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
 *     .subscribe&#40;setting -&gt;
 *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettings -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>List revisions of a Configuration Setting</h2>
 *
 * <p>The {@link com.azure.data.appconfiguration.ConfigurationAsyncClient#listRevisions(SettingSelector)}
 * method can be used to list all revisions of a configuration setting in the Azure App Configuration.</p>
 *
 * <p>The sample below shows how to list all revision of a setting that use the key "prodDBConnection".</p>
 * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions -->
 * <pre>
 * client.listRevisions&#40;new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;&#41;
 *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
 *     .subscribe&#40;setting -&gt;
 *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions -->
 *
 * <p><strong>Note:</strong> For synchronous sample, refer to {@link com.azure.data.appconfiguration.ConfigurationClient}.</p>
 *
 * @see ConfigurationClientBuilder
 * @see ConfigurationSetting
 */
@ServiceClient(builder = ConfigurationClientBuilder.class, isAsync = true,
    serviceInterfaces = AzureAppConfigurationImpl.AzureAppConfigurationService.class)
public final class ConfigurationAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationAsyncClient.class);
    private final AzureAppConfigurationImpl serviceClient;
    private final SyncTokenPolicy syncTokenPolicy;

    final CreateSnapshotUtilClient createSnapshotUtilClient;

    /**
     * Creates a ConfigurationAsyncClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param serviceClient The {@link AzureAppConfigurationImpl} that the client routes its request through.
     * @param syncTokenPolicy {@link SyncTokenPolicy} to be used to update the external synchronization token to ensure
     * service requests receive up-to-date values.
     */
    ConfigurationAsyncClient(AzureAppConfigurationImpl serviceClient, SyncTokenPolicy syncTokenPolicy) {
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string -->
     * <pre>
     * client.addConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, &quot;db_connection&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string -->
     *
     * @param key The key of the configuration setting to add.
     * @param label The label of the configuration setting to add. If {@code null} no label will be used.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addConfigurationSetting(String key, String label, String value) {
        return addConfigurationSetting(new ConfigurationSetting().setKey(key).setLabel(label).setValue(value));
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * client.addConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;db_connection&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to add based on its key and optional label combination.
     *
     * @return The {@link ConfigurationSetting} that was created, or {@code null} if a key collision occurs or the key
     * is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> addConfigurationSetting(ConfigurationSetting setting) {
        return addConfigurationSettingWithResponse(setting).map(Response::getValue);
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting -->
     * <pre>
     * client.addConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;db_connection&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ConfigurationSetting responseSetting = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             responseSetting.getKey&#40;&#41;, responseSetting.getLabel&#40;&#41;, responseSetting.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting -->
     *
     * @param setting The setting to add based on its key and optional label combination.
     * @return A REST response containing the {@link ConfigurationSetting} that was created, if a key collision occurs
     * or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> addConfigurationSettingWithResponse(ConfigurationSetting setting) {
        // This service method call is similar to setConfigurationSetting except we're passing If-Not-Match = "*".
        // If the service finds any existing configuration settings, then its e-tag will match and the service will
        // return an error.
        return withContext(
            context -> validateSettingAsync(setting).flatMap(
                settingInternal -> serviceClient.putKeyValueWithResponseAsync(settingInternal.getKey(),
                    settingInternal.getLabel(), null, ETAG_ANY, toKeyValue(settingInternal), context)
                    .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithResponse)));
    }

    /**
     * Creates or updates a configuration value in the service with the given key. the {@code label} is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", "westUS" and value "db_connection"</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string -->
     * <pre>
     * client.setConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, &quot;db_connection&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;
     * client.setConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, &quot;updated_db_connection&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string -->
     *
     * @param key The key of the configuration setting to create or update.
     * @param label The label of the configuration setting to create or update, If {@code null} no label will be used.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or an empty Mono if the key is an invalid
     * value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setConfigurationSetting(String key, String label, String value) {
        return setConfigurationSetting(new ConfigurationSetting().setKey(key).setLabel(label).setValue(value));
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
     * <p>Add a setting with the key "prodDBConnection", "westUS" and value "db_connection"</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * client.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;
     * client.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;updated_db_connection&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to add based on its key and optional label combination.
     *
     * @return The {@link ConfigurationSetting} that was created or updated, or an empty Mono if the key is an invalid
     * value (which will also throw HttpResponseException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setConfigurationSetting(ConfigurationSetting setting) {
        return setConfigurationSettingWithResponse(setting, false).map(Response::getValue);
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
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     * <p>Update setting's value "db_connection" to "updated_db_connection"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean -->
     * <pre>
     * client.setConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;db_connection&quot;&#41;,
     *         false&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ConfigurationSetting result = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;
     * client.setConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;
     *         .setValue&#40;&quot;updated_db_connection&quot;&#41;,
     *         false&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ConfigurationSetting responseSetting = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             responseSetting.getKey&#40;&#41;, responseSetting.getLabel&#40;&#41;, responseSetting.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean -->
     *
     * @param setting The setting to create or update based on its key, optional label and optional ETag combination.
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * IF-MATCH header.
     * @return A REST response containing the {@link ConfigurationSetting} that was created or updated, if the key is an
     * invalid value, the setting is read-only, or an ETag was provided but does not match the service's current ETag
     * value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#getETag() ETag} was specified, is not the
     * wildcard character, and the current configuration value's ETag does not match, or the setting exists and is
     * read-only.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> setConfigurationSettingWithResponse(ConfigurationSetting setting,
        boolean ifUnchanged) {
        return withContext(
            context -> validateSettingAsync(setting).flatMap(
                settingInternal -> serviceClient.putKeyValueWithResponseAsync(settingInternal.getKey(),
                    settingInternal.getLabel(), getETag(ifUnchanged, settingInternal), null,
                        toKeyValue(settingInternal), context)
                    .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithResponse)));
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, and the optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string -->
     * <pre>
     * client.getConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string -->

     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to retrieve. If {@code null} no label will be used.
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label) {
        return getConfigurationSetting(key, label, null);
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}, the optional {@code label}, and the optional
     * {@code acceptDateTime} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection" and a time that one minute before now at UTC-Zone</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime -->
     * <pre>
     * client.getConfigurationSetting&#40;
     *     &quot;prodDBConnection&quot;, &quot;westUS&quot;, OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.minusMinutes&#40;1&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime -->
     *
     * @param key The key of the setting to retrieve.
     * @param label The label of the configuration setting to retrieve. If {@code null} no label will be used.
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null}
     * then the current state of the configuration setting will be returned.
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label, OffsetDateTime acceptDateTime) {
        return getConfigurationSettingWithResponse(new ConfigurationSetting().setKey(key).setLabel(label),
            acceptDateTime, false).map(Response::getValue);
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code acceptDateTime} and optional ETag combination.
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection" and a time that one minute before now at UTC-Zone</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * client.getConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to retrieve.
     *
     * @return The {@link ConfigurationSetting} stored in the service, or an empty Mono if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> getConfigurationSetting(ConfigurationSetting setting) {
        return getConfigurationSettingWithResponse(setting, null, false).map(Response::getValue);
    }

    /**
     * Attempts to get the ConfigurationSetting with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label}, optional {@code acceptDateTime} and optional ETag combination.
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean -->
     * <pre>
     * client.getConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         null,
     *         false&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ConfigurationSetting result = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean -->
     *
     * @param setting The setting to retrieve.
     * @param acceptDateTime Datetime to access a past state of the configuration setting. If {@code null}
     * then the current state of the configuration setting will be returned.
     * @param ifChanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * If-None-Match header.
     * @return A REST response containing the {@link ConfigurationSetting} stored in the service, or {@code null} if
     * didn't exist. {@code null} is also returned if the configuration value does not exist or the key is an invalid
     * value (which will also throw HttpResponseException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpResponseException If the {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> getConfigurationSettingWithResponse(ConfigurationSetting setting,
        OffsetDateTime acceptDateTime, boolean ifChanged) {
        return withContext(context -> validateSettingAsync(setting).flatMap(settingInternal ->
            serviceClient.getKeyValueWithResponseAsync(settingInternal.getKey(), settingInternal.getLabel(),
                    acceptDateTime == null ? null : acceptDateTime.toString(), null,
                    getETag(ifChanged, settingInternal), null, context)
                .onErrorResume(HttpResponseException.class,
                    (Function<Throwable, Mono<ResponseBase<GetKeyValueHeaders, KeyValue>>>) throwable -> {
                        HttpResponseException e = (HttpResponseException) throwable;
                        HttpResponse httpResponse = e.getResponse();
                        if (httpResponse.getStatusCode() == 304) {
                            return Mono.just(new ResponseBase<GetKeyValueHeaders, KeyValue>(httpResponse.getRequest(),
                                httpResponse.getStatusCode(), httpResponse.getHeaders(), null, null));
                        }
                        return Mono.error(throwable);
                    }).map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithResponse)));
    }

    /**
     * Deletes the ConfigurationSetting with a matching {@code key} and optional {@code label} combination.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string -->
     * <pre>
     * client.deleteConfigurationSetting&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string -->
     *
     * @param key The key of configuration setting to delete.
     * @param label The label of configuration setting to delete. If {@code null} no label will be used.
     * @return The deleted ConfigurationSetting or an empty Mono is also returned if the {@code key} is an invalid value
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteConfigurationSetting(String key, String label) {
        return deleteConfigurationSetting(new ConfigurationSetting().setKey(key).setLabel(label));
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination from the service.
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * client.deleteConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting -->
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     *
     * @return The deleted ConfigurationSetting or an empty Mono is also returned if the {@code key} is an invalid value
     * (which will also throw HttpResponseException described below).
     *
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> deleteConfigurationSetting(ConfigurationSetting setting) {
        return deleteConfigurationSettingWithResponse(setting, false).map(Response::getValue);
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching {@link ConfigurationSetting#getKey() key}, and optional
     * {@link ConfigurationSetting#getLabel() label} and optional ETag combination from the service.
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
     * <p>Delete the setting with the key-label "prodDBConnection"-"westUS"</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean -->
     * <pre>
     * client.deleteConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         false&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ConfigurationSetting responseSetting = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             responseSetting.getKey&#40;&#41;, responseSetting.getLabel&#40;&#41;, responseSetting.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean -->
     *
     * @param setting The setting to delete based on its key, optional label and optional ETag combination.
     * @param ifUnchanged Flag indicating if the {@code setting} {@link ConfigurationSetting#getETag ETag} is used as a
     * IF-MATCH header.
     * @return A REST response containing the deleted ConfigurationSetting or {@code null} if didn't exist. {@code null}
     * is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value or
     * {@link ConfigurationSetting#getETag() ETag} is set but does not match the current ETag
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If {@code setting} is read-only.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#getETag() ETag} is specified, not the wildcard
     * character, and does not match the current ETag value.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> deleteConfigurationSettingWithResponse(ConfigurationSetting setting,
        boolean ifUnchanged) {
        return withContext(context -> validateSettingAsync(setting).flatMap(
            settingInternal -> serviceClient.deleteKeyValueWithResponseAsync(settingInternal.getKey(),
                settingInternal.getLabel(), getETag(ifUnchanged, settingInternal), context)
                .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithResponse)));
    }

    /**
     * Sets the read-only status for the {@link ConfigurationSetting} that matches the {@code key}, the optional
     * {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean -->
     * <pre>
     * client.setReadOnly&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, true&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly -->
     * <pre>
     * client.setReadOnly&#40;&quot;prodDBConnection&quot;, &quot;westUS&quot;, false&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, response.getKey&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly -->
     *
     * @param key The key of configuration setting to set to be read-only.
     * @param label The label of configuration setting to read-only. If {@code null} no label will be used.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     * @return The {@link ConfigurationSetting} that is read-only, or an empty Mono if a key collision occurs or the
     * key is an invalid value (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpResponseException If {@code key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setReadOnly(String key, String label, boolean isReadOnly) {
        return setReadOnly(new ConfigurationSetting().setKey(key).setLabel(label), isReadOnly);
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean -->
     * <pre>
     * client.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         true&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
     * <pre>
     * client.setReadOnly&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         false&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, response.getKey&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
     *
     * @param setting The configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     *
     * @return The {@link ConfigurationSetting} that is read-only, or an empty Mono if a key collision occurs or the
     * key is an invalid value (which will also throw HttpResponseException described below).
     *
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSetting> setReadOnly(ConfigurationSetting setting, boolean isReadOnly) {
        return setReadOnlyWithResponse(setting, isReadOnly).map(Response::getValue);
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean -->
     * <pre>
     * client.setReadOnlyWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         true&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ConfigurationSetting result = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly -->
     * <pre>
     * client.setReadOnlyWithResponse&#40;new ConfigurationSetting&#40;&#41;
     *         .setKey&#40;&quot;prodDBConnection&quot;&#41;
     *         .setLabel&#40;&quot;westUS&quot;&#41;,
     *         false&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         ConfigurationSetting result = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, result.getKey&#40;&#41;, result.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly -->
     *
     * @param setting The configuration setting to set to read-only or not read-only based on the {@code isReadOnly}.
     * @param isReadOnly Flag used to set the read-only status of the configuration. {@code true} will put the
     * configuration into a read-only state, {@code false} will clear the state.
     * @return A REST response containing the read-only or not read-only ConfigurationSetting if {@code isReadOnly}
     * is true or null, or false respectively. Or return {@code null} if the setting didn't exist.
     * {@code null} is also returned if the {@link ConfigurationSetting#getKey() key} is an invalid value.
     * (which will also throw HttpResponseException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#getKey() key} is {@code null}.
     * @throws HttpResponseException If {@link ConfigurationSetting#getKey() key} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSetting>> setReadOnlyWithResponse(ConfigurationSetting setting,
        boolean isReadOnly) {
        return withContext(context -> validateSettingAsync(setting).flatMap(settingInternal -> {
            final String key = settingInternal.getKey();
            final String label = settingInternal.getLabel();
            return (isReadOnly
                ? serviceClient.putLockWithResponseAsync(key, label, null, null, context)
                : serviceClient.deleteLockWithResponseAsync(key, label, null, null, context))
                .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithResponse);
        }));
    }

    /**
     * Fetches the configuration settings that match the {@code selector}. If {@code selector} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettings -->
     * <pre>
     * client.listConfigurationSettings&#40;new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettings -->
     *
     * @param selector Optional. Selector to filter configuration setting results from the service.
     * @return A Flux of ConfigurationSettings that matches the {@code selector}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listConfigurationSettings(SettingSelector selector) {
        final String keyFilter = selector == null ? null : selector.getKeyFilter();
        final String labelFilter = selector == null ? null : selector.getLabelFilter();
        final String acceptDateTime = selector == null ? null : selector.getAcceptDateTime();
        final List<SettingFields> settingFields = selector == null ? null : toSettingFieldsList(selector.getFields());
        final List<MatchConditions> matchConditionsList = selector == null ? null : selector.getMatchConditions();
        final List<String> tagsFilter = selector == null ? null : selector.getTagsFilter();
        AtomicInteger pageETagIndex = new AtomicInteger(0);
        return new PagedFlux<>(() -> withContext(context -> serviceClient.getKeyValuesSinglePageAsync(keyFilter,
                labelFilter, null, acceptDateTime, settingFields, null, null,
                getPageETag(matchConditionsList, pageETagIndex), tagsFilter, context)
            .onErrorResume(HttpResponseException.class,
                (Function<HttpResponseException, Mono<PagedResponse<KeyValue>>>)
                    Utility::handleNotModifiedErrorToValidResponse)
            .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithPagedResponse)),
                nextLink -> withContext(context -> serviceClient.getKeyValuesNextSinglePageAsync(nextLink,
                        acceptDateTime, null, getPageETag(matchConditionsList, pageETagIndex), context)
                    .onErrorResume(HttpResponseException.class,
                        (Function<HttpResponseException, Mono<PagedResponse<KeyValue>>>)
                            Utility::handleNotModifiedErrorToValidResponse)
                    .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithPagedResponse))
        );
    }

    /**
     * Fetches the configuration settings in a snapshot that matches the {@code snapshotName}. If {@code snapshotName}
     * is {@code null}, then all the {@link ConfigurationSetting configuration settings} are fetched with their
     * current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshot -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * client.listConfigurationSettingsForSnapshot&#40;snapshotName&#41;
     *     .subscribe&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshot -->
     *
     * @param snapshotName Optional. A filter used get {@link ConfigurationSetting}s for a snapshot. The value should
     * be the name of the snapshot.
     * @return A Flux of ConfigurationSettings that matches the {@code selector}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listConfigurationSettingsForSnapshot(String snapshotName) {
        return listConfigurationSettingsForSnapshot(snapshotName, null);
    }

    /**
     * Fetches the configuration settings in a snapshot that matches the {@code snapshotName}. If {@code snapshotName}
     * is {@code null}, then all the {@link ConfigurationSetting configuration settings} are fetched with their
     * current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshotMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * List&lt;SettingFields&gt; fields = Arrays.asList&#40;SettingFields.KEY&#41;;
     * client.listConfigurationSettingsForSnapshot&#40;snapshotName, fields&#41;
     *     .subscribe&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listConfigurationSettingsForSnapshotMaxOverload -->
     *
     * @param snapshotName Optional. A filter used get {@link ConfigurationSetting}s for a snapshot. The value should
     * be the name of the snapshot.
     * @param fields Optional. The fields to select for the query response. If none are set, the service will return the
     * ConfigurationSettings with a default set of properties.
     * @return A Flux of ConfigurationSettings that matches the {@code selector}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listConfigurationSettingsForSnapshot(String snapshotName,
        List<SettingFields> fields) {
        return new PagedFlux<>(() -> withContext(context -> serviceClient.getKeyValuesSinglePageAsync(null, null, null,
                null, fields, snapshotName, null, null, null, context)
            .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithPagedResponse)),
            nextLink -> withContext(context -> serviceClient.getKeyValuesNextSinglePageAsync(nextLink, null, null, null,
                    context)
                .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithPagedResponse)));
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions -->
     * <pre>
     * client.listRevisions&#40;new SettingSelector&#40;&#41;.setKeyFilter&#40;&quot;prodDBConnection&quot;&#41;&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;setting -&gt;
     *         System.out.printf&#40;&quot;Key: %s, Value: %s&quot;, setting.getKey&#40;&#41;, setting.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions -->
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return Revisions of the ConfigurationSetting
     * @throws HttpResponseException If a client or service error occurs, such as a 404, 409, 429 or 500.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSetting> listRevisions(SettingSelector selector) {
        final String keyFilter = selector == null ? null : selector.getKeyFilter();
        final String labelFilter = selector == null ? null : selector.getLabelFilter();
        final String acceptDateTime = selector == null ? null : selector.getAcceptDateTime();
        final List<SettingFields> settingFields = selector == null ? null : toSettingFieldsList(selector.getFields());
        List<String> tags = selector == null ? null : selector.getTagsFilter();
        return new PagedFlux<>(() -> withContext(context -> serviceClient.getRevisionsSinglePageAsync(keyFilter,
                labelFilter, null, acceptDateTime, settingFields, tags, context)
            .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithPagedResponse)),
            nextLink -> withContext(context -> serviceClient.getRevisionsNextSinglePageAsync(nextLink, acceptDateTime,
                    context)
                .map(ConfigurationSettingDeserializationHelper::toConfigurationSettingWithPagedResponse)));
    }

    /**
     * Create a {@link ConfigurationSnapshot} by providing a snapshot name and a
     * {@link ConfigurationSnapshot}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.beginCreateSnapshotMaxOverload -->
     * <pre>
     * List&lt;ConfigurationSettingsFilter&gt; filters = new ArrayList&lt;&gt;&#40;&#41;;
     * &#47;&#47; Key Name also supports RegExp but only support prefix end with &quot;*&quot;, such as &quot;k*&quot; and is case-sensitive.
     * filters.add&#40;new ConfigurationSettingsFilter&#40;&quot;&#123;keyName&#125;&quot;&#41;&#41;;
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * client.beginCreateSnapshot&#40;snapshotName, new ConfigurationSnapshot&#40;filters&#41;
     *         .setRetentionPeriod&#40;Duration.ofHours&#40;1&#41;&#41;&#41;
     *     .flatMap&#40;result -&gt; result.getFinalResult&#40;&#41;&#41;
     *     .subscribe&#40;
     *         snapshot -&gt; System.out.printf&#40;&quot;Snapshot name=%s is created at %s%n&quot;,
     *             snapshot.getName&#40;&#41;, snapshot.getCreatedAt&#40;&#41;&#41;,
     *         ex -&gt; System.out.printf&#40;&quot;Error on creating a snapshot=%s, with error=%s.%n&quot;, snapshotName,
     *             ex.getMessage&#40;&#41;&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Successfully created a snapshot.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.beginCreateSnapshotMaxOverload -->
     *
     * @param snapshotName The name of the {@link ConfigurationSnapshot} to create.
     * @param snapshot The {@link ConfigurationSnapshot} to create.
     * @return A {@link PollerFlux} that polls the creating snapshot operation until it has completed or
     * has failed. The completed operation returns a {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PollOperationDetails, ConfigurationSnapshot> beginCreateSnapshot(
        String snapshotName, ConfigurationSnapshot snapshot) {
        return createSnapshotUtilClient.beginCreateSnapshot(snapshotName, snapshot);
    }

    /**
     * Get a {@link ConfigurationSnapshot} by given the snapshot name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByName -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * client.getSnapshot&#40;snapshotName&#41;.subscribe&#40;
     *     getSnapshot -&gt; &#123;
     *         System.out.printf&#40;&quot;Snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *             getSnapshot.getName&#40;&#41;, getSnapshot.getCreatedAt&#40;&#41;, getSnapshot.getStatus&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByName -->
     *
     * @param snapshotName the snapshot name.
     * @return A {@link Mono} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSnapshot> getSnapshot(String snapshotName) {
        return getSnapshotWithResponse(snapshotName, null).map(Response::getValue);
    }

    /**
     * Get a {@link ConfigurationSnapshot} by given the snapshot name.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByNameMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     *
     * client.getSnapshotWithResponse&#40;snapshotName, Arrays.asList&#40;SnapshotFields.NAME, SnapshotFields.CREATED_AT,
     *     SnapshotFields.STATUS, SnapshotFields.FILTERS&#41;&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123;
     *             ConfigurationSnapshot getSnapshot = response.getValue&#40;&#41;;
     *             &#47;&#47; Only properties `name`, `createAt`, `status` and `filters` have value, and expect null or
     *             &#47;&#47; empty value other than the `fields` specified in the request.
     *             System.out.printf&#40;&quot;Snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *                 getSnapshot.getName&#40;&#41;, getSnapshot.getCreatedAt&#40;&#41;, getSnapshot.getStatus&#40;&#41;&#41;;
     *             List&lt;ConfigurationSettingsFilter&gt; filters = getSnapshot.getFilters&#40;&#41;;
     *             for &#40;ConfigurationSettingsFilter filter : filters&#41; &#123;
     *                 System.out.printf&#40;&quot;Snapshot filter key=%s, label=%s.%n&quot;, filter.getKey&#40;&#41;, filter.getLabel&#40;&#41;&#41;;
     *             &#125;
     *         &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.getSnapshotByNameMaxOverload -->
     *
     * @param snapshotName The snapshot name.
     * @param fields Used to select what fields are present in the returned resource(s).
     * @return A {@link Mono} of {@link Response} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSnapshot>> getSnapshotWithResponse(String snapshotName,
        List<SnapshotFields> fields) {
        return serviceClient.getSnapshotWithResponseAsync(snapshotName, null, null, fields, Context.NONE)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Update a snapshot status from {@link ConfigurationSnapshotStatus#READY} to {@link ConfigurationSnapshotStatus#ARCHIVED}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotByName -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * client.archiveSnapshot&#40;snapshotName&#41;.subscribe&#40;
     *     archivedSnapshot -&gt; &#123;
     *         System.out.printf&#40;&quot;Archived snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *             archivedSnapshot.getName&#40;&#41;, archivedSnapshot.getCreatedAt&#40;&#41;, archivedSnapshot.getStatus&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotByName -->
     *
     * @param snapshotName The snapshot name.
     * @return A {@link Mono} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSnapshot> archiveSnapshot(String snapshotName) {
        return updateSnapshotAsync(snapshotName, null, ConfigurationSnapshotStatus.ARCHIVED, serviceClient)
            .map(Response::getValue);
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * MatchConditions matchConditions = new MatchConditions&#40;&#41;.setIfMatch&#40;&quot;&#123;etag&#125;&quot;&#41;;
     * client.archiveSnapshotWithResponse&#40;snapshotName, matchConditions&#41;
     *     .subscribe&#40;
     *         response -&gt; &#123;
     *             ConfigurationSnapshot archivedSnapshot = response.getValue&#40;&#41;;
     *             System.out.printf&#40;&quot;Archived snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *                 archivedSnapshot.getName&#40;&#41;, archivedSnapshot.getCreatedAt&#40;&#41;, archivedSnapshot.getStatus&#40;&#41;&#41;;
     *         &#125;
     *     &#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.archiveSnapshotMaxOverload -->
     *
     * @param snapshotName The snapshot name.
     * @param matchConditions Specifies HTTP options for conditional requests.
     * @return A {@link Mono} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSnapshot>> archiveSnapshotWithResponse(String snapshotName,
        MatchConditions matchConditions) {
        return updateSnapshotAsync(snapshotName, matchConditions, ConfigurationSnapshotStatus.ARCHIVED, serviceClient);
    }

    /**
     * Update a snapshot status from {@link ConfigurationSnapshotStatus#ARCHIVED} to {@link ConfigurationSnapshotStatus#READY}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotByName -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * client.recoverSnapshot&#40;snapshotName&#41;.subscribe&#40;
     *     recoveredSnapshot -&gt; &#123;
     *         System.out.printf&#40;&quot;Recovered snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *             recoveredSnapshot.getName&#40;&#41;, recoveredSnapshot.getCreatedAt&#40;&#41;, recoveredSnapshot.getStatus&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotByName -->
     *
     * @param snapshotName The snapshot name.
     * @return A {@link Mono} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConfigurationSnapshot> recoverSnapshot(String snapshotName) {
        return updateSnapshotAsync(snapshotName, null, ConfigurationSnapshotStatus.READY, serviceClient)
            .map(Response::getValue);
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
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotMaxOverload -->
     * <pre>
     * String snapshotName = &quot;&#123;snapshotName&#125;&quot;;
     * MatchConditions matchConditions = new MatchConditions&#40;&#41;.setIfMatch&#40;&quot;&#123;etag&#125;&quot;&#41;;
     * client.recoverSnapshotWithResponse&#40;snapshotName, matchConditions&#41;.subscribe&#40;
     *     response -&gt; &#123;
     *         ConfigurationSnapshot recoveredSnapshot = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Recovered snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *             recoveredSnapshot.getName&#40;&#41;, recoveredSnapshot.getCreatedAt&#40;&#41;, recoveredSnapshot.getStatus&#40;&#41;&#41;;
     *     &#125;
     * &#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.recoverSnapshotMaxOverload -->
     *
     * @param snapshotName The snapshot name.
     * @param matchConditions Specifies HTTP options for conditional requests.
     * @return A {@link Mono} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConfigurationSnapshot>> recoverSnapshotWithResponse(
        String snapshotName, MatchConditions matchConditions) {
        return updateSnapshotAsync(snapshotName, matchConditions, ConfigurationSnapshotStatus.READY, serviceClient);
    }

    /**
     * List snapshots by given {@link SnapshotSelector}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listSnapshots -->
     * <pre>
     * String snapshotNameFilter = &quot;&#123;snapshotNamePrefix&#125;*&quot;;
     * client.listSnapshots&#40;new SnapshotSelector&#40;&#41;.setNameFilter&#40;snapshotNameFilter&#41;&#41;
     *     .subscribe&#40;recoveredSnapshot -&gt; &#123;
     *         System.out.printf&#40;&quot;Recovered snapshot name=%s is created at %s, snapshot status is %s.%n&quot;,
     *             recoveredSnapshot.getName&#40;&#41;, recoveredSnapshot.getCreatedAt&#40;&#41;, recoveredSnapshot.getStatus&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listSnapshots -->
     *
     * @param selector Optional. Used to filter {@link ConfigurationSnapshot} from the service.
     * @return A {@link PagedFlux} of {@link ConfigurationSnapshot}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ConfigurationSnapshot> listSnapshots(SnapshotSelector selector) {
        try {
            return new PagedFlux<>(() -> withContext(context -> serviceClient.getSnapshotsSinglePageAsync(
                selector == null ? null : selector.getNameFilter(), null,
                selector == null ? null : selector.getFields(), selector == null ? null : selector.getStatus(),
                context)), nextLink -> withContext(context ->
                serviceClient.getSnapshotsNextSinglePageAsync(nextLink, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    /**
     * Gets a list of labels by given {@link SettingLabelSelector}
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.listLabels -->
     * <pre>
     * String labelNameFilter = &quot;&#123;labelNamePrefix&#125;*&quot;;
     * client.listLabels&#40;new SettingLabelSelector&#40;&#41;.setNameFilter&#40;labelNameFilter&#41;&#41;
     *         .subscribe&#40;label -&gt; &#123;
     *             System.out.println&#40;&quot;label name = &quot; + label&#41;;
     *         &#125;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.listLabels -->
     *
     * @param selector Optional. Selector to filter labels from the service.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a list of labels as paginated response with {@link PagedFlux}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<SettingLabel> listLabels(SettingLabelSelector selector) {
        final String labelNameFilter = selector == null ? null : selector.getNameFilter();
        final String acceptDatetime = selector == null ? null
            : selector.getAcceptDateTime() == null ? null : selector.getAcceptDateTime().toString();
        final List<SettingLabelFields> labelFields = selector == null ? null : selector.getFields();
        return serviceClient.getLabelsAsync(labelNameFilter, null, acceptDatetime, labelFields);
    }

    /**
     * Adds an external synchronization token to ensure service requests receive up-to-date values.
     *
     * @param token an external synchronization token to ensure service requests receive up-to-date values.
     * @throws NullPointerException if the given token is null.
     */
    public void updateSyncToken(String token) {
        Objects.requireNonNull(token, "'token' cannot be null.");
        syncTokenPolicy.updateSyncToken(token);
    }
}
