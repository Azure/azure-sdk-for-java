// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.Page;
import com.azure.applicationconfig.implementation.RestPagedResponseImpl;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.SettingFields;
import com.azure.applicationconfig.models.RequestOptions;
import com.azure.applicationconfig.models.RevisionOptions;
import com.azure.applicationconfig.models.RevisionRange;
import com.azure.common.ServiceClient;
import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.policy.AsyncCredentialsPolicy;
import com.azure.common.http.policy.HttpLogDetailLevel;
import com.azure.common.http.policy.HttpLoggingPolicy;
import com.azure.common.http.policy.HttpPipelinePolicy;
import com.azure.common.http.policy.RetryPolicy;
import com.azure.common.http.policy.UserAgentPolicy;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.implementation.RestProxy;
import com.azure.common.implementation.Validator;
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
     * @return A new Builder to create a ConfigurationClient from.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Adds a configuration value in the service if that key and label does not exist.
     *
     * <p>
     * The label value for the ConfigurationSetting is optional. If not specified, the
     * {@link ConfigurationSetting#NULL_LABEL} is used.
     * </p>
     *
     * @param configurationSetting The key, value, and label to set.
     * @return ConfigurationSetting that was created or updated
     * @throws com.azure.common.http.rest.RestException when a ConfigurationSetting with the same key and label exists.
     */
    public Mono<RestResponse<ConfigurationSetting>> add(ConfigurationSetting configurationSetting) {
        Validator.validate(configurationSetting);

        return service.setKey(serviceEndpoint, configurationSetting.key(), configurationSetting.label(), configurationSetting, null, getETagValue(ETAG_ANY));
    }

    /**
     * Creates or updates a configuration value in the service.
     *
     * <p>
     * If {@link ConfigurationSetting#etag()} is specified, the configuration value is updated if the current setting's
     * etag matches. If the etag's value is equal to {@link ConfigurationClient#ETAG_ANY}, the setting will always be
     * updated.
     * </p>
     *
     * <p>
     * The label value for the ConfigurationSetting is optional. If not specified, the
     * {@link ConfigurationSetting#NULL_LABEL} is used.
     * </p>
     *
     * @param configurationSetting The configuration setting to create or update.
     * @return ConfigurationSetting that was created or updated.
     * @throws com.azure.common.http.rest.RestException If the {@link ConfigurationSetting#etag()} was specified, is not
     *                                               {@link ConfigurationClient#ETAG_ANY}, and the current configuration
     *                                               value's etag does not match.
     */
    public Mono<RestResponse<ConfigurationSetting>> set(ConfigurationSetting configurationSetting) {
        Validator.validate(configurationSetting);

        return service.setKey(serviceEndpoint, configurationSetting.key(), configurationSetting.label(), configurationSetting, getETagValue(configurationSetting.etag()), null);
    }

    /**
     * Updates an existing configuration value in the service. The setting must already exist.
     *
     * <p>
     * The label value for the ConfigurationSetting is optional. If not specified, the
     * {@link ConfigurationSetting#NULL_LABEL} is used.
     * </p>
     *
     * If the {@link ConfigurationSetting#etag()} is specified, the configuration value is only updated if it matches.
     *
     * @param configurationSetting The key, value, and optional label to set.
     * @return ConfigurationSetting that was updated.
     * @throws com.azure.common.http.rest.RestException When a ConfigurationSetting with the same key and label does not
     *                                               exists or the configuration value is locked.
     */
    public Mono<RestResponse<ConfigurationSetting>> update(ConfigurationSetting configurationSetting) {
        Validator.validate(configurationSetting);

        String etag = configurationSetting.etag() == null ? ETAG_ANY : configurationSetting.etag();

        return service.setKey(serviceEndpoint, configurationSetting.key(), configurationSetting.label(), configurationSetting, getETagValue(etag), null);
    }

    /**
     * Gets a ConfigurationSetting that matches the {@code key} and {@code label}.
     *
     * @param key The key being retrieved
     * @return The configuration value in the service.
     * @throws com.azure.common.http.rest.RestException with status code of 404 if the {@code key} and {@code label} does
     *                                               not exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> get(String key) {
        return get(key, null);
    }

    /**
     * Gets the ConfigurationSetting given the {@code key}, optional {@code label}.
     *
     * @param key   The key being retrieved
     * @param label Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used.
     * @return The configuration value in the service.
     * @throws com.azure.common.http.rest.RestException with status code of 404 if the {@code key} and {@code label} does
     *                                               not exist. If {@code etag} was specified, returns status code of
     *                                               304 if the key has not been modified.
     */
    public Mono<RestResponse<ConfigurationSetting>> get(String key, String label) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.getKeyValue(serviceEndpoint, key, label, null, null, null, null);
    }

    /**
     * Deletes the ConfigurationSetting.
     *
     * @param key The key to delete.
     * @return the deleted ConfigurationSetting or null if didn't exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> delete(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        }

        return delete(key, null, null);
    }

    /**
     * Deletes the ConfigurationSetting.
     *
     * @param key   key of the keyValue to delete
     * @param label Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used.
     * @param etag  Optional. If specified, will only delete the key if its current etag matches. (ie. No one has
     *              changed the value yet.)
     * @return the deleted ConfigurationSetting or none if didn't exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> delete(String key, String label, String etag) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.delete(serviceEndpoint, key, label, getETagValue(etag), null);
    }

    /**
     * Places a lock on ConfigurationSetting.
     *
     * @param key The key to lock.
     * @return ConfigurationSetting that was locked
     * @throws com.azure.common.http.rest.RestException with status code 404 if the {@code key} does not exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> lock(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        }

        return lock(key, null);
    }

    /**
     * Places a lock on ConfigurationSetting. If present, label must be explicit label value (not a wildcard).
     * For all operations it's an optional parameter. If omitted it implies null label.
     *
     * @param key    key name
     * @param label  Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used.
     * @return ConfigurationSetting
     */
    public Mono<RestResponse<ConfigurationSetting>> lock(String key, String label) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.lockKeyValue(serviceEndpoint, key, label, null, null);
    }

    /**
     * Unlocks ConfigurationSetting.
     *
     * @param key key name
     * @return ConfigurationSetting
     */
    public Mono<RestResponse<ConfigurationSetting>> unlock(String key) {
        return unlock(key, null);
    }

    /**
     * Unlocks a ConfigurationSetting with a matching {@code key}, optional {@code label}. If present, {@code label}
     * must be explicit label value (not a wildcard).
     *
     * @param key   key name
     * @param label Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used. If specified, it must
     *              be an explicit value and cannot contain wildcard characters.
     * @return ConfigurationSetting that was unlocked.
     */
    public Mono<RestResponse<ConfigurationSetting>> unlock(String key, String label) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.unlockKeyValue(serviceEndpoint, key, label, null, null);
    }

    /**
     * Fetches the configuration settings that match the {@code options}. If {@code options} is {@code null}, then all the
     * {@link ConfigurationSetting}s are fetched in their current state with default fields.
     *
     * @param options Optional. Options to filter configuration setting results from the service.
     * @return A Flux of ConfigurationSettings that matches the {@code options}. If no options were provided, the Flux
     * contains all of the current settings in the service.
     */
    public Flux<ConfigurationSetting> listKeyValues(RequestOptions options) {
        Mono<RestResponse<Page<ConfigurationSetting>>> result;
        if (options != null) {
            String fields = getSelectQuery(options.fields());
            result = service.listKeyValues(serviceEndpoint, options.key(), options.label(), fields, options.acceptDateTime());
        } else {
            result = service.listKeyValues(serviceEndpoint, null, null, null, null);
        }

        return getPagedConfigurationSettings(result);
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
         *
         * Every time {@code build()} is called, a new instance of {@link ConfigurationClient} is created.
         *
         * @return A ConfigurationClient with the options set from the builder.
         * @throws IllegalStateException If {@link Builder#credentials(ConfigurationClientCredentials)}
         * has not been set.
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

    /**
     * Gets all ConfigurationSetting settings given the {@code nextPageLink} that was retrieved from a call to
     * {@link ConfigurationClient#listKeyValues(KeyValueListFilter)} or {@link ConfigurationClient#listNextPage(String)}.
     *
     * @param nextPageLink The {@link Page#nextPageLink()} from a previous, successful call to one of the list operations.
     * @return A stream of {@link ConfigurationSetting} from the next page of results.
     */
    private Flux<ConfigurationSetting> listKeyValues(@NonNull String nextPageLink) {
        Mono<RestResponse<Page<ConfigurationSetting>>> result = service.listKeyValues(serviceEndpoint, nextPageLink);
        return getPagedConfigurationSettings(result);
    }

    /**
     * Lists chronological/historical representation of {@link ConfigurationSetting} resource(s). Revisions are provided in
     * descending order from their last_modified date.
     *
     * Revisions expire after a period of time. (The default is 30 days.)
     *
     * <p>
     * If {@code options} is {@code null}, then all the {@link ConfigurationSetting}s are fetched in their current
     * state with default fields. Otherwise, the results returned match the parameters given in {@code options}.
     * </p>
     *
     * @param options Optional. Options to filter configuration setting revisions from the service.
     * @return Revisions of the ConfigurationSetting
     */
    public Flux<ConfigurationSetting> listKeyValueRevisions(RevisionOptions options) {
        Mono<RestResponse<Page<ConfigurationSetting>>> result;
        if (options != null) {
            String fields = getSelectQuery(options.fields());
            String range = getItemsRange(options.range());
            result = service.listKeyValueRevisions(serviceEndpoint, options.key(), options.label(), fields, options.acceptDateTime(), range);
        } else {
            result = service.listKeyValueRevisions(serviceEndpoint, null, null, null, null, null);
        }

        return getPagedConfigurationSettings(result);
    }

    private Flux<ConfigurationSetting> getPagedConfigurationSettings(Mono<RestResponse<Page<ConfigurationSetting>>> response) {
        return response.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
            .concatMap(this::extractAndFetchConfigurationSettings);
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(RestPagedResponseImpl<ConfigurationSetting> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listNextPage(nextPageLink));
    }

    /**
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

    private static String getItemsRange(RevisionRange range) {
        if (range == null) {
            return null;
        }

        return range.end() == null
            ? String.format("items=%d-", range.start())
            : String.format("items=%d-%d", range.start(), range.end());
    }
}
