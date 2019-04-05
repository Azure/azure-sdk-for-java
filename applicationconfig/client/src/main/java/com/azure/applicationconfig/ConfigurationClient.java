// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.ServiceClient;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.rest.PagedResponse;
import com.azure.common.http.rest.Response;
import com.azure.common.implementation.RestProxy;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Client that contains all the operations for {@link ConfigurationSetting ConfigurationSettings} in Azure Configuration Store.
 *
 * @see ConfigurationClientBuilder
 * @see ConfigurationClientCredentials
 */
public final class ConfigurationClient extends ServiceClient {
    private static final String ETAG_ANY = "*";

    private final String serviceEndpoint;
    private final ApplicationConfigService service;

    /**
     * Creates a ConfigurationClient that uses {@code credentials} to authorize with Azure and {@code pipeline} to
     * service requests
     *
     * @param serviceEndpoint URL for the Application configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    ConfigurationClient(URL serviceEndpoint, HttpPipeline pipeline) {
        super(pipeline);

        this.service = RestProxy.create(ApplicationConfigService.class, this);
        this.serviceEndpoint = serviceEndpoint.toString();
    }

    /**
     * Creates a builder that can configure options for the ConfigurationClient before creating an instance of it.
     *
     * @return A new ConfigurationClientBuilder to create a ConfigurationClient from.
     */
    public static ConfigurationClientBuilder builder() {
        return new ConfigurationClientBuilder();
    }

    /**
     * Adds a configuration value in the service if that key does not exist.
     *
     * @param key The key for the configuration setting to add.
     * @param value The value associated with this configuration setting key.
     * @return ConfigurationSetting that was created.
     * @throws IllegalArgumentException If {@code key} or {@code value} are {@code null} or an empty string.
     * @throws ServiceRequestException If a ConfigurationSetting with the same key exists.
     */
    public Mono<Response<ConfigurationSetting>> addSetting(String key, String value) {
        return addSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist.
     *
     * <p>
     * The label value for the ConfigurationSetting is optional.
     *
     * @param setting The setting to add to the configuration service.
     * @return ConfigurationSetting that was created.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} or {@link ConfigurationSetting#value() value}
     * are {@code null} or an empty string.
     * @throws ServiceRequestException If a ConfigurationSetting with the same key and label exists.
     */
    public Mono<Response<ConfigurationSetting>> addSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting, true);
        return service.setKey(serviceEndpoint, result.key(), result.label(), result, null, getETagValue(ETAG_ANY));
    }

    /**
     * Creates or updates a configuration value in the service with the given key. Partial updates are not supported.
     *
     * @param key The key for the configuration setting to create or update.
     * @param value The value of this configuration setting.
     * @return ConfigurationSetting that was created or updated.
     * @throws IllegalArgumentException If {@code key} or {@code value} are {@code null} or an empty string.
     */
    public Mono<Response<ConfigurationSetting>> setSetting(String key, String value) {
        return setSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Creates or updates a configuration value in the service. Partial updates are not supported.
     *
     * <p>
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is updated if the current setting's
     * etag matches. If the etag's value is equal to {@link ConfigurationClient#ETAG_ANY}, the setting will always be
     * updated.
     *
     * @param setting The configuration setting to create or update.
     * @return ConfigurationSetting that was created or updated.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} or {@link ConfigurationSetting#value() value}
     * are {@code null} or an empty string.
     * @throws ServiceRequestException If the {@link ConfigurationSetting#etag() etag} was specified, is not
     * {@link ConfigurationClient#ETAG_ANY}, and the current configuration value's etag does not match.
     */
    public Mono<Response<ConfigurationSetting>> setSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting, true);

        return service.setKey(serviceEndpoint, result.key(), result.label(), result, getETagValue(result.etag()), null);
    }

    /**
     * Updates an existing configuration value in the service with the given key. The setting must already exist.
     *
     * <p>
     * Partial updates are not supported.
     *
     * @param key The key of the configuration setting to update.
     * @param value The updated value of this configuration setting.
     * @return ConfigurationSetting that was updated.
     * @throws IllegalArgumentException If {@code key} or {@code value} are {@code null} or an empty string.
     * @throws ServiceRequestException If a ConfigurationSetting with the key does not exist or the configuration value
     * is locked.
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(String key, String value) {
        return updateSetting(new ConfigurationSetting().key(key).value(value));
    }

    /**
     * Updates an existing configuration value in the service. The setting must already exist.
     *
     * <p>
     * Partial updates are not supported.
     *
     * <p>
     * If {@link ConfigurationSetting#etag() etag} is specified, the configuration value is only updated if it matches.
     *
     * @param setting The setting to add or update in the service.
     * @return ConfigurationSetting that was updated.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} or {@link ConfigurationSetting#value() value}
     * are {@code null} or an empty string.
     * @throws ServiceRequestException If a ConfigurationSetting with the same key and label does not
     * exist or the configuration value is locked.
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting, true);
        String etag = result.etag() == null ? ETAG_ANY : result.etag();

        return service.setKey(serviceEndpoint, result.key(), result.label(), result, getETagValue(etag), null);
    }

    /**
     * Attempts to get a ConfigurationSetting that matches the {@code key}.
     *
     * @param key The key of the setting to retrieve.
     * @return The configuration setting in the service.
     * @throws IllegalArgumentException If {@code key} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code of 404 if the {@code key} and {@code label} does
     * not exist.
     */
    public Mono<Response<ConfigurationSetting>> getSetting(String key) {
        return getSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Attempts to get the ConfigurationSetting given the {@code key}, optional {@code label}.
     *
     * @param setting The setting to retrieve based on its key and optional label combination.
     * @return The configuration value in the service.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code of 404 if the {@code key} and {@code label} does
     * not exist. If {@code etag} was specified, returns status code of
     * 304 if the key has not been modified.
     */
    public Mono<Response<ConfigurationSetting>> getSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting, false);

        return service.getKeyValue(serviceEndpoint, result.key(), result.label(), null, null, null, null);
    }

    /**
     * Deletes the ConfigurationSetting with a matching {@code key}.
     *
     * @param key The key of the setting to delete.
     * @return The deleted ConfigurationSetting or null if it didn't exist.
     * @throws IllegalArgumentException If {@code key} is {@code null} or an empty string.
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(String key) {
        return deleteSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Deletes the {@link ConfigurationSetting} with a matching key, along with the given label and etag. If the
     * {@link ConfigurationSetting#etag() etag} is specified, the setting is <b>only</b> deleted if the etag matches the
     * current etag; this means that no one has updated the ConfigurationSetting yet.
     *
     * @param setting The ConfigurationSetting to delete.
     * @return The deleted ConfigurationSetting or {@code null} if didn't exist.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key() key} is {@code null} or an empty string.
     * @throws NullPointerException When {@code setting} is {@code null}.
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting, false);

        return service.delete(serviceEndpoint, result.key(), result.label(), getETagValue(result.etag()), null);
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all the
     * {@link ConfigurationSetting configuration settings} are fetched in their current state.
     *
     * @param options Optional. Options to filter configuration setting results from the service.
     * @return A Flux of ConfigurationSettings that matches the {@code options}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     */
    public Flux<ConfigurationSetting> listSettings(SettingSelector options) {
        Mono<PagedResponse<ConfigurationSetting>> result;
        if (options != null) {
            String fields = getSelectQuery(options.fields());
            result = service.listKeyValues(serviceEndpoint, options.key(), options.label(), fields, options.acceptDateTime());
        } else {
            result = service.listKeyValues(serviceEndpoint, null, null, null, null);
        }

        return result.flatMapMany(this::extractAndFetchConfigurationSettings);
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided
     * in descending order from their {@link ConfigurationSetting#lastModified() lastModified} date.
     * <p>
     * Revisions expire after a period of time. (The default is 30 days.)
     *
     * <p>
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting ConfigurationSettings} are fetched
     * in their current state. Otherwise, the results returned match the parameters given in {@code options}.
     *
     * @param selector Optional. Used to filter configuration setting revisions from the service.
     * @return Revisions of the ConfigurationSetting
     */
    public Flux<ConfigurationSetting> listSettingRevisions(SettingSelector selector) {
        Mono<PagedResponse<ConfigurationSetting>> result;
        if (selector != null) {
            String fields = getSelectQuery(selector.fields());
            result = service.listKeyValueRevisions(serviceEndpoint, selector.key(), selector.label(), fields, selector.acceptDateTime(), null);
        } else {
            result = service.listKeyValueRevisions(serviceEndpoint, null, null, null, null, null);
        }

        return result.flatMapMany(this::extractAndFetchConfigurationSettings);
    }

    /*
     * Gets all ConfigurationSetting settings given the {@code nextPageLink} that was retrieved from a call to
     * {@link ConfigurationClient#listSettings(SettingSelector)} or a call from this method.
     *
     * @param nextPageLink The {@link Page#nextPageLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link ConfigurationSetting} from the next page of results.
     */
    private Flux<ConfigurationSetting> listSettings(@NonNull String nextPageLink) {
        Mono<PagedResponse<ConfigurationSetting>> result = service.listKeyValues(serviceEndpoint, nextPageLink);
        return result.flatMapMany(this::extractAndFetchConfigurationSettings);
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(PagedResponse<ConfigurationSetting> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listSettings(nextPageLink));
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

    private static String getSelectQuery(SettingFields[] set) {
        if (set == null || set.length == 0) {
            return null;
        }

        return Arrays.stream(set).map(item -> item.toString().toLowerCase(Locale.US))
            .collect(Collectors.joining(","));
    }

    private static ConfigurationSetting validateSetting(ConfigurationSetting setting, boolean verifyValue) {
        Objects.requireNonNull(setting);

        if (setting.key() == null || setting.key().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        } else if (verifyValue && (setting.value() == null || setting.value().isEmpty())) {
            throw new IllegalArgumentException("Parameter 'value' is required and cannot be null or empty");
        }

        return setting;
    }
}
