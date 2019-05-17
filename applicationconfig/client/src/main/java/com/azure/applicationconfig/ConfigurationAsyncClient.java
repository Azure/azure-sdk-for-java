// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.credentials.ConfigurationClientCredentials;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.core.ServiceClient;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;

/**
 * This class provides a client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings}
 * in Azure App Configuration Store. Operations allowed by the client are adding, retrieving, updating, and deleting
 * ConfigurationSettings, and listing settings or revision of a setting based on a {@link SettingSelector filter}.
 *
 * <p><strong>Instantiating an Asynchronous Configuration Client</strong></p>
 *
 * <pre>
 * ConfigurationAsyncClient client = ConfigurationAsyncClient.builder()
 *     .credentials(new ConfigurationClientCredentials(connectionString))
 *     .build();
 * </pre>
 *
 * <p>View {@link ConfigurationAsyncClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see ConfigurationAsyncClientBuilder
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationAsyncClient extends ServiceClient {
    private final ServiceLogger logger = new ServiceLogger(ConfigurationAsyncClient.class);

    private static final String ETAG_ANY = "*";
    private static final String RANGE_QUERY = "items=%s";

    private final String serviceEndpoint;
    private final ConfigurationService service;

    /**
     * Creates a ConfigurationAsyncClient that sends requests to the configuration service at {@code serviceEndpoint}.
     * Each service call goes through the {@code pipeline}.
     *
     * @param serviceEndpoint URL for the Application configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    ConfigurationAsyncClient(URL serviceEndpoint, HttpPipeline pipeline) {
        super(pipeline);

        this.service = RestProxy.create(ConfigurationService.class, this);
        this.serviceEndpoint = serviceEndpoint.toString();
    }

    /**
     * Creates a builder that can configure options for the ConfigurationAsyncClient before creating an instance of it.
     *
     * @return A new {@link ConfigurationAsyncClientBuilder} to create a ConfigurationAsyncClient from.
     */
    public static ConfigurationAsyncClientBuilder builder() {
        return new ConfigurationAsyncClientBuilder();
    }

    /**
     * Adds a configuration value in the service if that key does not exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * <pre>
     * client.addSetting("prodDBConnection", "db_connection")
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param key The key of the configuration setting to add.
     * @param value The value associated with this configuration setting key.
     * @return The {@link ConfigurationSetting} that was created, or {@code null}, if a key collision occurs or the key
     * is an invalid value (which will also throw HttpRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key exists.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> addSetting(String key, String value) {
        return addSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist. The label value of the
     * ConfigurationSetting is optional.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * <pre>
     * client.addSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"))
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param setting The setting to add to the configuration service.
     * @return The {@link ConfigurationSetting} that was created, or {@code null}, if a key collision occurs or the key
     * is an invalid value (which will also throw HttpRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label exists.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> addSetting(ConfigurationSetting setting) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        // This service method call is similar to setSetting except we're passing If-Not-Match = "*". If the service
        // finds any existing configuration settings, then its e-tag will match and the service will return an error.
        return service.setKey(serviceEndpoint, setting.key(), setting.label(), setting, null, getETagValue(ETAG_ANY))
            .doOnRequest(ignoredValue -> logger.asInformational().log("Adding ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.asInformational().log("Added ConfigurationSetting - {}", response.value()))
            .onErrorMap(ConfigurationAsyncClient::addSettingExceptionMapper)
            .doOnError(error -> logger.asWarning().log("Failed to add ConfigurationSetting - {}", setting, error));
    }

    /**
     * Creates or updates a configuration value in the service with the given key.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection" and value "db_connection".</p>
     *
     * <pre>
     * client.setSetting("prodDBConnection", "db_connection")
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * <p>Update the value of the setting to "updated_db_connection".</p>
     *
     * <pre>
     * client.setSetting("prodDBConnection", "updated_db_connection")
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param key The key of the configuration setting to create or update.
     * @param value The value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null}, if the key is an invalid
     * value (which will also throw HttpRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the setting exists and is locked.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> setSetting(String key, String value) {
        return setSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported and the entire
     * configuration setting is updated.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is updated if the current
     * setting's etag matches. If the etag's value is equal to the wildcard character ({@code "*"}), the setting
     * will always be updated.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection".</p>
     *
     * <pre>
     * client.setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"))
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * <p>Update the value of the setting to "updated_db_connection".</p>
     *
     * <pre>
     * client.setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("updated_db_connection"))
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param setting The configuration setting to create or update.
     * @return The {@link ConfigurationSetting} that was created or updated, or {@code null}, if the key is an invalid
     * value, the setting is locked, or an etag was provided but does not match the service's current etag value (which
     * will also throw HttpRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If the {@link ConfigurationSetting#etag() etag} was specified, is not the
     * wildcard character, and the current configuration value's etag does not match, or the
     * setting exists and is locked.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> setSetting(ConfigurationSetting setting) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        // This service method call is similar to addSetting except it will create or update a configuration setting.
        // If the user provides an etag value, it is passed in as If-Match = "{etag value}". If the current value in the
        // service has a matching etag then it matches, then its value is updated with what the user passed in.
        // Otherwise, the service throws an exception because the current configuration value was updated and we have an
        // old value locally.
        // If no etag value was passed in, then the value is always added or updated.
        return service.setKey(serviceEndpoint, setting.key(), setting.label(), setting, getETagValue(setting.etag()), null)
            .doOnRequest(ignoredValue -> logger.asInformational().log("Setting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.asInformational().log("Set ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.asWarning().log("Failed to set ConfigurationSetting - {}", setting, error));
    }

    /**
     * Updates an existing configuration value in the service with the given key. The setting must already exist.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update a setting with the key "prodDBConnection" to have the value "updated_db_connection".</p>
     *
     * <pre>
     * client.updateSetting("prodDBConnection", "updated_db_connection")
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param key The key of the configuration setting to update.
     * @param value The updated value of this configuration setting.
     * @return The {@link ConfigurationSetting} that was updated, or {@code null}, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw HttpRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws HttpRequestException If a ConfigurationSetting with the key does not exist or the configuration value
     * is locked.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(String key, String value) {
        return updateSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Updates an existing configuration value in the service. The setting must already exist. Partial updates are not
     * supported, the entire configuration value is replaced.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is only updated if it matches.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update the setting with the key-label pair "prodDBConnection"-"westUS" to have the value "updated_db_connection".</p>
     *
     * <pre>
     * client.updateSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("updated_db_connection"))
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param setting The setting to add or update in the service.
     * @return The {@link ConfigurationSetting} that was updated, or {@code null}, if the configuration value does not
     * exist, is locked, or the key is an invalid value (which will also throw HttpRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceModifiedException If a ConfigurationSetting with the same key and label does not
     * exist, the setting is locked, or {@link ConfigurationSetting#etag() etag} is specified but does not match
     * the current value.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(ConfigurationSetting setting) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        String etag = setting.etag() == null ? ETAG_ANY : setting.etag();

        return service.setKey(serviceEndpoint, setting.key(), setting.label(), setting, getETagValue(etag), null)
            .doOnRequest(ignoredValue -> logger.asInformational().log("Updating ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.asInformational().log("Updated ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.asWarning().log("Failed to update ConfigurationSetting - {}", setting, error));
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key "prodDBConnection".</p>
     *
     * <pre>
     * client.getSetting("prodDBConnection")
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param key The key of the setting to retrieve.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with {@code key} does not exist.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> getSetting(String key) {
        return getSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Attempts to get the ConfigurationSetting given the {@code key}, optional {@code label}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <pre>
     * client.getSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"))
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param setting The setting to retrieve based on its key and optional label combination.
     * @return The {@link ConfigurationSetting} stored in the service, or {@code null}, if the configuration value does
     * not exist or the key is an invalid value (which will also throw HttpRequestException described below).
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws ResourceNotFoundException If a ConfigurationSetting with the same key and label does not exist.
     * @throws HttpRequestException If the {@code} key is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> getSetting(ConfigurationSetting setting) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        return service.getKeyValue(serviceEndpoint, setting.key(), setting.label(), null, null, null, null)
            .doOnRequest(ignoredValue -> logger.asInformational().log("Retrieving ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.asInformational().log("Retrieved ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.asWarning().log("Failed to get ConfigurationSetting - {}", setting, error));
    }

    /**
     * Deletes the ConfigurationSetting with a matching {@code key}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key "prodDBConnection".</p>
     *
     * <pre>
     * client.deleteSetting("prodDBConnection")
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param key The key of the setting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if it didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value (which will also throw HttpRequestException described below).
     * @throws IllegalArgumentException If {@code key} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(String key) {
        return deleteSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching key, along with the given label and etag.
     *
     * If {@link ConfigurationSetting#etag() etag} is specified and is not the wildcard character ({@code "*"}),
     * then the setting is <b>only</b> deleted if the etag matches the current etag; this means that no one has updated
     * the ConfigurationSetting yet.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Delete the setting with the key-label "prodDBConnection"-"westUS".</p>
     *
     * <pre>
     * client.deleteSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"))
     *     .subscribe(response -&gt; {
     *         ConfigurationSetting result = response.value();
     *         System.out.printf("Key: %s, Value: %s", result.key(), result.value());
     *     });</pre>
     *
     * @param setting The ConfigurationSetting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if didn't exist. {@code null} is also returned if
     * the {@code key} is an invalid value or {@link ConfigurationSetting#etag() etag} is set but does not match the
     * current etag (which will also throw HttpRequestException described below).
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null}.
     * @throws NullPointerException When {@code setting} is {@code null}.
     * @throws ResourceModifiedException If the ConfigurationSetting is locked.
     * @throws ResourceNotFoundException If {@link ConfigurationSetting#etag() etag} is specified, not the wildcard
     * character, and does not match the current etag value.
     * @throws HttpRequestException If {@code key} is an empty string.
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(ConfigurationSetting setting) {
        // Validate that setting and key is not null. The key is used in the service URL so it cannot be null.
        validateSetting(setting);

        return service.delete(serviceEndpoint, setting.key(), setting.label(), getETagValue(setting.etag()), null)
            .doOnRequest(ignoredValue -> logger.asInformational().log("Deleting ConfigurationSetting - {}", setting))
            .doOnSuccess(response -> logger.asInformational().log("Deleted ConfigurationSetting - {}", response.value()))
            .doOnError(error -> logger.asWarning().log("Failed to delete ConfigurationSetting - {}", setting, error));
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all
     * the {@link ConfigurationSetting configuration settings} are fetched with their current values.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all settings that use the key "prodDBConnection".</p>
     *
     * <pre>
     * client.listSettings(new SettingSelector().key("prodDBConnection"))
     *     .subscribe(setting -&gt; System.out.printf("Key: %s, Value: %s", setting.key(), setting.value()));</pre>
     *
     * @param options Optional. Options to filter configuration setting results from the service.
     * @return A Flux of ConfigurationSettings that matches the {@code options}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     */
    public Flux<ConfigurationSetting> listSettings(SettingSelector options) {
        Mono<PagedResponse<ConfigurationSetting>> result;
        if (options != null) {
            String fields = ImplUtils.arrayToString(options.fields(), SettingFields::toStringMapper);
            String keys = ImplUtils.arrayToString(options.keys(), key -> key);
            String labels = ImplUtils.arrayToString(options.labels(), label -> label);

            result = service.listKeyValues(serviceEndpoint, keys, labels, fields, options.acceptDateTime())
                .doOnRequest(ignoredValue -> logger.asInformational().log("Listing ConfigurationSettings - {}", options))
                .doOnSuccess(response -> logger.asInformational().log("Listed ConfigurationSettings - {}", options))
                .doOnError(error -> logger.asWarning().log("Failed to list ConfigurationSetting - {}", options, error));
        } else {
            result = service.listKeyValues(serviceEndpoint, null, null, null, null)
                .doOnRequest(ignoredValue -> logger.asInformational().log("Listing all ConfigurationSettings"))
                .doOnSuccess(response -> logger.asInformational().log("Listed all ConfigurationSettings"))
                .doOnError(error -> logger.asWarning().log("Failed to list all ConfigurationSetting", error));
        }

        return result.flatMapMany(this::extractAndFetchConfigurationSettings);
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#lastModified() lastModified} date. Revisions expire
     * after a period of time. The service maintains change history for up to 7 days.
     *
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code options}.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Retrieve all revisions of the setting that has the key "prodDBConnection".</p>
     *
     * <pre>
     * client.listSettingRevisions(new SettingSelector().key("prodDBConnection"))
     *     .subscribe(setting -&gt; System.out.printf("Key: %s, Value: %s", setting.key(), setting.value()));</pre>
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return Revisions of the ConfigurationSetting
     */
    public Flux<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        Mono<PagedResponse<ConfigurationSetting>> result;
        if (selector != null) {
            String fields = ImplUtils.arrayToString(selector.fields(), SettingFields::toStringMapper);
            String keys = ImplUtils.arrayToString(selector.keys(), key -> key);
            String labels = ImplUtils.arrayToString(selector.labels(), label -> label);
            String range = selector.range() != null ? String.format(RANGE_QUERY, selector.range()) : null;

            result = service.listKeyValueRevisions(serviceEndpoint, keys, labels, fields, selector.acceptDateTime(), range)
                .doOnRequest(ignoredValue -> logger.asInformational().log("Listing ConfigurationSetting revisions - {}", selector))
                .doOnSuccess(response -> logger.asInformational().log("Listed ConfigurationSetting revisions - {}", selector))
                .doOnError(error -> logger.asWarning().log("Failed to list ConfigurationSetting revisions - {}", selector, error));
        } else {
            result = service.listKeyValueRevisions(serviceEndpoint, null, null, null, null, null)
                .doOnRequest(ignoredValue -> logger.asInformational().log("Listing ConfigurationSetting revisions"))
                .doOnSuccess(response -> logger.asInformational().log("Listed ConfigurationSetting revisions"))
                .doOnError(error -> logger.asWarning().log("Failed to list all ConfigurationSetting revisions", error));
        }

        return result.flatMapMany(this::extractAndFetchConfigurationSettings);
    }

    /*
     * Gets all ConfigurationSetting settings given the {@code nextPageLink} that was retrieved from a call to
     * {@link ConfigurationAsyncClient#listSettings(SettingSelector)} or a call from this method.
     *
     * @param nextPageLink The {@link Page#nextPageLink()} from a previous, successful call to one of the list
     * operations.
     * @return A stream of {@link ConfigurationSetting} from the next page of results.
     */
    private Flux<ConfigurationSetting> listSettings(String nextPageLink) {
        Mono<PagedResponse<ConfigurationSetting>> result = service.listKeyValues(serviceEndpoint, nextPageLink)
            .doOnRequest(ignoredValue -> logger.asInformational().log("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.asInformational().log("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.asWarning().log("Failed to retrieve the next listing page - Page {}", nextPageLink, error));

        return result.flatMapMany(this::extractAndFetchConfigurationSettings);
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(PagedResponse<ConfigurationSetting> page) {
        return ImplUtils.extractAndFetch(page, this::listSettings);
    }

    /*
     * Azure Configuration service requires that the etag value is surrounded in quotation marks.
     *
     * @param etag The etag to get the value for. If null is pass in, an empty string is returned.
     * @return The etag surrounded by quotations. (ex. "etag")
     */
    private static String getETagValue(String etag) {
        return etag == null ? "" : "\"" + etag + "\"";
    }

    /*
     * Ensure that setting is not null. And, key cannot be null because it is part of the service REST URL.
     */
    private static void validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.key() == null) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null.");
        }
    }

    /**
     * Remaps the exception returned from the service if it is a PRECONDITION_FAILED response. This is performed since
     * add setting returns PRECONDITION_FAILED when the configuration already exists, all other uses of setKey return
     * this status when the configuration doesn't exist.
     * @param throwable Error response from the service.
     * @return Exception remapped to a ResourceModifiedException if the throwable was a ResourceNotFoundException,
     * otherwise the throwable is returned unmodified.
     */
    private static Throwable addSettingExceptionMapper(Throwable throwable) {
        if (!(throwable instanceof ResourceNotFoundException)) {
            return throwable;
        }

        ResourceNotFoundException notFoundException = (ResourceNotFoundException) throwable;
        return new ResourceModifiedException(notFoundException.getMessage(), notFoundException.response());
    }
}
