// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.SettingSelector;
import com.azure.common.ServiceClient;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.AsyncCredentialsPolicy;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpLoggingPolicy;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.RetryPolicy;
import com.azure.common.http.policy.UserAgentPolicy;
import com.azure.common.http.rest.PagedResponse;
import com.azure.common.http.rest.Response;
import com.azure.common.implementation.RestProxy;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Client that contains all the operations for KeyValues in Azure Configuration Store.
 */
public final class ConfigurationClient extends ServiceClient {
    private static final String ETAG_ANY = "*";

    static final String SDK_NAME = "Azure-Configuration";
    static final String SDK_VERSION = "1.0.0-SNAPSHOT";

    private final String serviceEndpoint;
    private final ApplicationConfigService service;

    /**
     * Creates a ConfigurationClient that uses {@code credentials} to authorize with Azure and {@code pipeline} to
     * service requests
     *
     * @param serviceEndpoint URL for the Application configuration service.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    private ConfigurationClient(URL serviceEndpoint, HttpPipeline pipeline) {
        super(pipeline);

        this.service = RestProxy.create(ApplicationConfigService.class, this);
        this.serviceEndpoint = serviceEndpoint.toString();
    }

    /**
     * Creates a builder that can configure options for the ConfigurationClient before creating an instance of it.
     *
     * @return A new Builder to create a ConfigurationClient from.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist.
     *
     * <p>
     * The label value for the ConfigurationSetting is optional.
     *
     * @param setting The setting to add to the configuration service.
     * @return ConfigurationSetting that was created or updated.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key()} is {@code null} or an empty string.
     * @throws ServiceRequestException If a ConfigurationSetting with the same key and label exists.
     */
    public Mono<Response<ConfigurationSetting>> addSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);

        return service.setKey(serviceEndpoint, result.key(), result.label(), result, null, getETagValue(ETAG_ANY));
    }

    /**
     * Creates or updates a configuration value in the service.
     *
     * <p>
     * If {@link ConfigurationSetting#etag()} is specified, the configuration value is updated if the current setting's
     * etag matches. If the etag's value is equal to {@link ConfigurationClient#ETAG_ANY}, the setting will always be
     * updated.
     *
     * <p>
     * The label value for the ConfigurationSetting is optional.
     *
     * @param setting The configuration setting to create or update.
     * @return ConfigurationSetting that was created or updated.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key()} is {@code null} or an empty string.
     * @throws ServiceRequestException If the {@link ConfigurationSetting#etag()} was specified, is not
     * {@link ConfigurationClient#ETAG_ANY}, and the current configuration
     * value's etag does not match.
     */
    public Mono<Response<ConfigurationSetting>> setSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);

        return service.setKey(serviceEndpoint, result.key(), result.label(), result, getETagValue(result.etag()), null);
    }

    /**
     * Updates an existing configuration value in the service. The setting must already exist.
     *
     * <p>
     * The label value for the ConfigurationSetting is optional.
     *
     * <p>
     * If {@link ConfigurationSetting#etag()} is specified, the configuration value is only updated if it matches.
     *
     * @param setting The setting to add or update in the service.
     * @return ConfigurationSetting that was updated.
     * @throws NullPointerException If {@code setting} is {@code null}.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key()} is {@code null} or an empty string.
     * @throws ServiceRequestException If a ConfigurationSetting with the same key and label does not
     * exist or the configuration value is locked.
     */
    public Mono<Response<ConfigurationSetting>> updateSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);
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
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key()} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code of 404 if the {@code key} and {@code label} does
     * not exist. If {@code etag} was specified, returns status code of
     * 304 if the key has not been modified.
     */
    public Mono<Response<ConfigurationSetting>> getSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);

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
     * {@link ConfigurationSetting#etag()} is specified, the setting is <b>only</b> deleted if the etag matches the
     * current etag; this means that no one has updated the ConfigurationSetting yet.
     *
     * @param setting The ConfigurationSetting to delete.
     * @return The deleted ConfigurationSetting or {@link null} if didn't exist.
     * @throws IllegalArgumentException If {@link ConfigurationSetting#key()} is {@code null} or an empty string.
     * @throws NullPointerException When {@code setting} is {@code null}.
     */
    public Mono<Response<ConfigurationSetting>> deleteSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);

        return service.delete(serviceEndpoint, result.key(), result.label(), getETagValue(result.etag()), null);
    }

    /**
     * Places a lock on the ConfigurationSetting so that its contents cannot be changed.
     *
     * @param key The key of the ConfigurationSetting.
     * @return ConfigurationSetting that was locked.
     * @throws IllegalArgumentException If {@code key} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code 404 if the {@code key} does not exist.
     */
    public Mono<Response<ConfigurationSetting>> lockSetting(String key) {
        return lockSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Places a lock on the provided ConfigurationSetting so that its contents cannot be changed. Label is optional. If
     * present, label must be an explicit value (not a wildcard).
     *
     * @param setting The ConfigurationSetting to lock.
     * @return ConfigurationSetting that was locked.
     * @throws IllegalArgumentException If {@code key} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code 404 if the {@code key} does not exist.
     */
    public Mono<Response<ConfigurationSetting>> lockSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);

        return service.lockKeyValue(serviceEndpoint, result.key(), result.label(), null, null);
    }

    /**
     * Unlocks a ConfigurationSetting with the given key.
     *
     * @param key The key of the setting to unlock.
     * @return The ConfigurationSetting that was unlocked.
     * @throws IllegalArgumentException If {@code key} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code 404 if the {@code key} does not exist.
     */
    public Mono<Response<ConfigurationSetting>> unlockSetting(String key) {
        return unlockSetting(new ConfigurationSetting().key(key));
    }

    /**
     * Unlocks a ConfigurationSetting with a matching {@code key} and optional {@code label}. If present, {@code label}
     * must be explicit label value (not a wildcard).
     *
     * @param setting The configuration setting to unlock.
     * @return The ConfigurationSetting that was unlocked.
     * @throws IllegalArgumentException If {@code key} is {@code null} or an empty string.
     * @throws ServiceRequestException with status code 404 if the {@code key} does not exist.
     */
    public Mono<Response<ConfigurationSetting>> unlockSetting(ConfigurationSetting setting) {
        ConfigurationSetting result = validateSetting(setting);

        return service.unlockKeyValue(serviceEndpoint, result.key(), result.label(), null, null);
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all the
     * {@link ConfigurationSetting}s are fetched in their current state with default fields.
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
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided in
     * descending order from their last_modified date.
     * <p>
     * Revisions expire after a period of time. (The default is 30 days.)
     *
     * <p>
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting}s are fetched in their current
     * state with default fields. Otherwise, the results returned match the parameters given in {@code options}.
     * </p>
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

    /**
     * Provides configuration options for instances of {@link ConfigurationClient}.
     */
    public static final class Builder {
        private final List<HttpPipelinePolicy> policies;
        private ConfigurationClientCredentials credentials;
        private HttpClient httpClient;
        private HttpLogDetailLevel httpLogDetailLevel;
        private RetryPolicy retryPolicy;
        private String userAgent;

        private Builder() {
            userAgent = String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION);
            retryPolicy = new RetryPolicy();
            httpLogDetailLevel = HttpLogDetailLevel.NONE;
            policies = new ArrayList<>();
        }

        /**
         * Creates a {@link ConfigurationClient} based on options set in the Builder.
         * <p>
         * Every time {@code build()} is called, a new instance of {@link ConfigurationClient} is created.
         *
         * @return A ConfigurationClient with the options set from the builder.
         * @throws IllegalStateException If {@link Builder#credentials(ConfigurationClientCredentials)} has not been set.
         */
        public ConfigurationClient build() {
            if (credentials == null) {
                throw new IllegalStateException("'credentials' is required.");
            }

            // Closest to API goes first, closest to wire goes last.
            final List<HttpPipelinePolicy> policies = new ArrayList<>();

            policies.add(new UserAgentPolicy(userAgent));
            policies.add(new RequestIdPolicy());
            policies.add(retryPolicy);
            policies.add(new ConfigurationCredentialsPolicy());
            policies.add(new AsyncCredentialsPolicy(credentials));

            policies.addAll(this.policies);

            policies.add(new HttpLoggingPolicy(httpLogDetailLevel));

            HttpPipeline pipeline = httpClient == null
                ? new HttpPipeline(policies)
                : new HttpPipeline(httpClient, policies);

            return new ConfigurationClient(credentials.baseUri(), pipeline);
        }

        /**
         * Sets the credentials to use when authenticating HTTP requests.
         *
         * @param credentials The credentials to use for authenticating HTTP requests.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code credentials} is {@code null}.
         */
        public Builder credentials(ConfigurationClientCredentials credentials) {
            Objects.requireNonNull(credentials);
            this.credentials = credentials;
            return this;
        }

        /**
         * Sets the logging level for HTTP requests and responses.
         *
         * @param logLevel The amount of logging output when sending and receiving HTTP requests/responses.
         * @return The updated Builder object.
         */
        public Builder httpLogDetailLevel(HttpLogDetailLevel logLevel) {
            httpLogDetailLevel = logLevel;
            return this;
        }

        /**
         * Adds a policy to the set of existing policies that are executed after
         * {@link com.azure.applicationconfig.ConfigurationClient} required policies.
         *
         * @param policy The retry policy for service requests.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code policy} is {@code null}.
         */
        public Builder addPolicy(HttpPipelinePolicy policy) {
            Objects.requireNonNull(policy);
            policies.add(policy);
            return this;
        }

        /**
         * Sets the HTTP client to use for sending and receiving requests to and from the service.
         *
         * @param client The HTTP client to use for requests.
         * @return The updated Builder object.
         * @throws NullPointerException if {@code client} is {@code null}.
         */
        public Builder httpClient(HttpClient client) {
            this.httpClient = client;
            return this;
        }
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

    private static String getSelectQuery(EnumSet<SettingFields> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }

        return set.stream().map(item -> item.toString().toLowerCase(Locale.US))
            .collect(Collectors.joining(","));
    }

    private static ConfigurationSetting validateSetting(ConfigurationSetting setting) {
        Objects.requireNonNull(setting);

        if (setting.key() == null || setting.key().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        }

        return setting;
    }
}
