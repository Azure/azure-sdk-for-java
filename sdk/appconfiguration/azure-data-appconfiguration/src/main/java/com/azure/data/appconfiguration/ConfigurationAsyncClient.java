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
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.AzureAppConfigurationImpl;
import com.azure.data.appconfiguration.implementation.SyncTokenPolicy;
import com.azure.data.appconfiguration.implementation.models.GetKeyValueHeaders;
import com.azure.data.appconfiguration.implementation.models.KeyValue;
import com.azure.data.appconfiguration.implementation.models.KeyValueFields;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.data.appconfiguration.implementation.Utility.APP_CONFIG_TRACING_NAMESPACE_VALUE;
import static com.azure.data.appconfiguration.implementation.Utility.addTracingNamespace;
import static com.azure.data.appconfiguration.implementation.Utility.getIfMatchETag;
import static com.azure.data.appconfiguration.implementation.Utility.getIfNoneMatchETag;
import static com.azure.data.appconfiguration.implementation.Utility.toConfigurationSetting;
import static com.azure.data.appconfiguration.implementation.Utility.toKeyValue;
import static com.azure.data.appconfiguration.implementation.Utility.toKeyValueFieldsList;
import static com.azure.data.appconfiguration.implementation.Utility.validateSetting;

/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, deleting, set read-only
 * status ConfigurationSettings, and listing settings or revision of a setting based on a
 * {@link SettingSelector filter}.
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
 * @see ConfigurationClientBuilder
 */
@ServiceClient(builder = ConfigurationClientBuilder.class, isAsync = true,
    serviceInterfaces = AzureAppConfigurationImpl.AzureAppConfigurationService.class)
public final class ConfigurationAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationAsyncClient.class);
    private final AzureAppConfigurationImpl serviceClient;
    private final SyncTokenPolicy syncTokenPolicy;

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
     *
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting -->
     * <pre>
     * client.addConfigurationSetting&#40;
     *     new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;.setValue&#40;&quot;db_connection&quot;&#41;&#41;
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
     *
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting -->
     * <pre>
     * client.addConfigurationSettingWithResponse&#40;
     *     new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;.setValue&#40;&quot;db_connection&quot;&#41;&#41;
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
        try {
            // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
            validateSetting(setting);
            return withContext(
                context -> serviceClient.putKeyValueWithResponseAsync(setting.getKey(), setting.getLabel(),
                    null, null, toKeyValue(setting),
                    addTracingNamespace(context))
            .map(response -> new SimpleResponse<>(response, toConfigurationSetting(response.getValue()))));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
     *
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
     * client.setConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;
     * client.setConfigurationSetting&#40;
     *     new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;.setValue&#40;&quot;updated_db_connection&quot;&#41;&#41;
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
     *
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
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
     * client.setConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;
     *     .setValue&#40;&quot;db_connection&quot;&#41;, false&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ConfigurationSetting result = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *             result.getKey&#40;&#41;, result.getLabel&#40;&#41;, result.getValue&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#47;&#47; Update the value of the setting to &quot;updated_db_connection&quot;
     * client.setConfigurationSettingWithResponse&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;
     *     .setValue&#40;&quot;updated_db_connection&quot;&#41;, false&#41;
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
        try {
            // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
            validateSetting(setting);
            return withContext(
                context -> serviceClient.putKeyValueWithResponseAsync(setting.getKey(), setting.getLabel(),
                    getIfMatchETag(ifUnchanged, setting), null, toKeyValue(setting),
                    addTracingNamespace(context))
                               .map(response -> new SimpleResponse<>(response,
                                   toConfigurationSetting(response.getValue()))));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
     * client.getConfigurationSetting&#40;&quot;prodDBConnection&quot;, null&#41;
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
     *     &quot;prodDBConnection&quot;, null, OffsetDateTime.now&#40;ZoneOffset.UTC&#41;.minusMinutes&#40;1&#41;&#41;
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
     * client.getConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;&#41;
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
     * client.getConfigurationSettingWithResponse&#40;
     *     new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, null, false&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ConfigurationSetting result = response.getValue&#40;&#41;;
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
        try {
            // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
            validateSetting(setting);
            return withContext(
                context -> serviceClient.getKeyValueWithResponseAsync(
                    setting.getKey(),
                    setting.getLabel(),
                    acceptDateTime == null ? null : acceptDateTime.toString(),
                    null,
                    getIfNoneMatchETag(ifChanged, setting),
                    null,
                    addTracingNamespace(context))
                               .onErrorResume(HttpResponseException.class,
                                   (Function<Throwable, Mono<ResponseBase<GetKeyValueHeaders, KeyValue>>>)
                                    throwable -> {
                                        HttpResponseException e = (HttpResponseException) throwable;
                                        HttpResponse httpResponse = e.getResponse();
                                        if (httpResponse.getStatusCode() == 304) {
                                            return Mono.just(new ResponseBase<GetKeyValueHeaders, KeyValue>(
                                                httpResponse.getRequest(),
                                                httpResponse.getStatusCode(),
                                                httpResponse.getHeaders(),
                                                null,
                                                null));
                                        }
                                        return Mono.error(throwable);
                                    })
                               .map(response ->
                                        new SimpleResponse<>(response, toConfigurationSetting(response.getValue())))
            );
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
     * client.deleteConfigurationSetting&#40;&quot;prodDBConnection&quot;, null&#41;
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
     *
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
     * client.deleteConfigurationSetting&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;&#41;
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
     *
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
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
     * client.deleteConfigurationSettingWithResponse&#40;
     *     new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, false&#41;
     *     .contextWrite&#40;Context.of&#40;key1, value1, key2, value2&#41;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ConfigurationSetting responseSetting = response.getValue&#40;&#41;;
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
        try {
            // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
            validateSetting(setting);
            return withContext(
                context -> serviceClient.deleteKeyValueWithResponseAsync(setting.getKey(), setting.getLabel(),
                    getIfMatchETag(ifUnchanged, setting),
                    context.addData(AZ_TRACING_NAMESPACE_KEY, APP_CONFIG_TRACING_NAMESPACE_VALUE))
                               .map(response -> new SimpleResponse<>(response,
                                   toConfigurationSetting(response.getValue()))));
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
     *
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean -->
     * <pre>
     * client.setReadOnly&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, true&#41;
     *     .subscribe&#40;response -&gt; System.out.printf&#40;&quot;Key: %s, Label: %s, Value: %s&quot;,
     *         response.getKey&#40;&#41;, response.getLabel&#40;&#41;, response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean -->
     *
     * <p>Clear read-only of the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly -->
     * <pre>
     * client.setReadOnly&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, false&#41;
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
     *
     * For more configuration setting types, see {@link FeatureFlagConfigurationSetting} and
     * {@link SecretReferenceConfigurationSetting}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Set the setting to read-only with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <!-- src_embed com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean -->
     * <pre>
     * client.setReadOnlyWithResponse&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, true&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         final ConfigurationSetting result = response.getValue&#40;&#41;;
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
     * client.setReadOnlyWithResponse&#40;new ConfigurationSetting&#40;&#41;.setKey&#40;&quot;prodDBConnection&quot;&#41;.setLabel&#40;&quot;westUS&quot;&#41;, false&#41;
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
        try {
            // Validate that setting and key is not null. The key is used in the service URL, so it cannot be null.
            validateSetting(setting);
            return withContext(
                context -> {
                    final String key = setting.getKey();
                    final String label = setting.getLabel();
                    context = addTracingNamespace(context);
                    return (isReadOnly
                                ? serviceClient.putLockWithResponseAsync(key, label, null, null, context)
                                : serviceClient.deleteLockWithResponseAsync(key, label, null, null, context))
                               .map(response -> new SimpleResponse<>(response,
                                   toConfigurationSetting(response.getValue())));
                });
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
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
        try {
            final String keyFilter = selector == null ? null : selector.getKeyFilter();
            final String labelFilter = selector == null ? null : selector.getLabelFilter();
            final String acceptDateTime = selector == null ? null : selector.getAcceptDateTime();
            final List<KeyValueFields> keyValueFields = selector == null ? null
                                                            : toKeyValueFieldsList(selector.getFields());
            return new PagedFlux<>(
                () -> withContext(
                    context -> serviceClient.getKeyValuesSinglePageAsync(
                        keyFilter,
                        labelFilter,
                        null,
                        acceptDateTime,
                        keyValueFields,
                        null,
                        addTracingNamespace(context))
                                   .map(pagedResponse -> new PagedResponseBase<>(
                                       pagedResponse.getRequest(),
                                       pagedResponse.getStatusCode(),
                                       pagedResponse.getHeaders(),
                                       pagedResponse.getValue()
                                           .stream()
                                           .map(keyValue -> toConfigurationSetting(keyValue))
                                           .collect(Collectors.toList()),
                                       pagedResponse.getContinuationToken(),
                                       null))),
                nextLink -> withContext(
                    context -> serviceClient.getKeyValuesNextSinglePageAsync(
                        nextLink,
                        acceptDateTime,
                        addTracingNamespace(context))
                                   .map(pagedResponse -> new PagedResponseBase<>(
                                       pagedResponse.getRequest(),
                                       pagedResponse.getStatusCode(),
                                       pagedResponse.getHeaders(),
                                       pagedResponse.getValue()
                                           .stream()
                                           .map(keyValue -> toConfigurationSetting(keyValue)).collect(Collectors.toList()),
                                       pagedResponse.getContinuationToken(),
                                       null)))
            );
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#getLastModified() lastModified} date.
     * Revisions expire after a period of time, see <a href="https://azure.microsoft.com/pricing/details/app-configuration/">Pricing</a>
     * for more information.
     *
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
        try {
            final String keyFilter = selector == null ? null : selector.getKeyFilter();
            final String labelFilter = selector == null ? null : selector.getLabelFilter();
            final String acceptDateTime = selector == null ? null : selector.getAcceptDateTime();
            final List<KeyValueFields> keyValueFields = selector == null ? null
                                                            : toKeyValueFieldsList(selector.getFields());
            return new PagedFlux<>(
                () -> withContext(
                    context -> serviceClient.getRevisionsSinglePageAsync(
                        keyFilter,
                        labelFilter,
                        null,
                        acceptDateTime,
                        keyValueFields,
                        addTracingNamespace(context))
                                   .map(pagedResponse -> new PagedResponseBase<>(
                                       pagedResponse.getRequest(),
                                       pagedResponse.getStatusCode(),
                                       pagedResponse.getHeaders(),
                                       pagedResponse.getValue()
                                           .stream()
                                           .map(keyValue -> toConfigurationSetting(keyValue))
                                           .collect(Collectors.toList()),
                                       pagedResponse.getContinuationToken(),
                                       null))),
                nextLink -> withContext(
                    context ->
                        serviceClient.getRevisionsNextSinglePageAsync(nextLink, acceptDateTime,
                            addTracingNamespace(context))
                            .map(pagedResponse -> new PagedResponseBase<>(
                                pagedResponse.getRequest(),
                                pagedResponse.getStatusCode(),
                                pagedResponse.getHeaders(),
                                pagedResponse.getValue()
                                    .stream()
                                    .map(keyValue -> toConfigurationSetting(keyValue)).collect(Collectors.toList()),
                                pagedResponse.getContinuationToken(), null))));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(LOGGER, ex));
        }
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
