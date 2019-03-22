// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.Page;
import com.azure.applicationconfig.implementation.RestPagedResponseImpl;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.KeyValueCreateUpdateParameters;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.RevisionFilter;
import com.azure.common.ServiceClient;
import com.azure.common.http.HttpPipeline;
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
import java.util.List;

/**
 * Client that contains all the operations for KeyValues in Azure Configuration Store.
 */
public final class ConfigurationClient extends ServiceClient {
    private static final String ETAG_ANY = "*";

    static final String SDK_NAME = "Azure-Configuration";
    static final String SDK_VERSION = "1.0.0-SNAPSHOT";

    private final URL baseUri;
    private final ApplicationConfigService service;

    /**
     * Create a new instance of ConfigurationClient that uses connectionString for authentication.
     *
     * @param connectionString connection string in the format "Endpoint=_endpoint_;Id=_id_;Secret=_secret_"
     */
    public ConfigurationClient(String connectionString) {
        super(new HttpPipeline(getDefaultPolicies(connectionString)));

        this.service = RestProxy.create(ApplicationConfigService.class, this);
        this.baseUri = new ApplicationConfigCredentials(connectionString).baseUri();
    }

    public ConfigurationClient(String connectionString, HttpPipeline pipeline) {
        super(pipeline);

        this.service = RestProxy.create(ApplicationConfigService.class, this);
        this.baseUri = new ApplicationConfigCredentials(connectionString).baseUri();
    }

    /**
     * Gets the default pipeline policies.
     * TODO (conniey): This is going to change when we move to a builder pattern.
     */
    public static List<HttpPipelinePolicy> getDefaultPolicies(String connectionString) {
        final ApplicationConfigCredentials credentials = new ApplicationConfigCredentials(connectionString);
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION)));
        policies.add(new RequestIdPolicy());
        policies.add(new RetryPolicy());
        policies.add(new ConfigurationCredentialsPolicy(credentials));

        return policies;
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
        KeyValueCreateUpdateParameters parameters = new KeyValueCreateUpdateParameters()
                .withValue(configurationSetting.value())
                .withContentType(configurationSetting.contentType())
                .withTags(configurationSetting.tags());

        return service.setKey(baseUri.toString(), configurationSetting.key(), configurationSetting.label(), parameters, null, getETagValue(ETAG_ANY));
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
        KeyValueCreateUpdateParameters parameters = new KeyValueCreateUpdateParameters()
                .withValue(configurationSetting.value())
                .withContentType(configurationSetting.contentType())
                .withTags(configurationSetting.tags());

        return service.setKey(baseUri.toString(), configurationSetting.key(), configurationSetting.label(), parameters, getETagValue(configurationSetting.etag()), null);
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
        KeyValueCreateUpdateParameters parameters = new KeyValueCreateUpdateParameters()
                .withValue(configurationSetting.value())
                .withContentType(configurationSetting.contentType())
                .withTags(configurationSetting.tags());

        String etag = configurationSetting.etag() == null ? ETAG_ANY : configurationSetting.etag();

        return service.setKey(baseUri.toString(), configurationSetting.key(), configurationSetting.label(), parameters, getETagValue(etag), null);
    }

    /**
     * Gets a ConfigurationSetting that matches the {@param key} and {@param label}.
     *
     * @param key The key being retrieved
     * @return The configuration value in the service.
     * @throws com.azure.common.http.rest.RestException with status code of 404 if the {@param key} and {@param label} does
     *                                               not exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> get(String key) {
        return get(key, null);
    }

    /**
     * Gets the ConfigurationSetting given the {@param key}, optional {@param label}.
     *
     * @param key   The key being retrieved
     * @param label Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used.
     * @return The configuration value in the service.
     * @throws com.azure.common.http.rest.RestException with status code of 404 if the {@param key} and {@param label} does
     *                                               not exist. If {@param etag} was specified, returns status code of
     *                                               304 if the key has not been modified.
     */
    public Mono<RestResponse<ConfigurationSetting>> get(String key, String label) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'key' is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.getKeyValue(baseUri.toString(), key, label, null, null, null, null);
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

        return service.delete(baseUri.toString(), key, label, getETagValue(etag), null);
    }

    /**
     * Places a lock on ConfigurationSetting.
     *
     * @param key The key to lock.
     * @return ConfigurationSetting that was locked
     * @throws com.azure.common.http.rest.RestException with status code 404 if the {@param key} does not exist.
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

        return service.lockKeyValue(baseUri.toString(), key, label, null, null);
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
     * Unlocks a ConfigurationSetting with a matching {@param key}, optional {@param label}. If present, {@param label}
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

        return service.unlockKeyValue(baseUri.toString(), key, label, null, null);
    }

    /**
     * Lists the ConfigurationSettings.
     *
     * @param filter query options
     * @return KeyValues
     */
    public Flux<ConfigurationSetting> listKeyValues(KeyValueListFilter filter) {
        Mono<RestResponse<Page<ConfigurationSetting>>> result;
        if (filter != null) {
            result = service.listKeyValues(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDateTime(), filter.range());
        } else {
            result = service.listKeyValues(baseUri.toString(), null, null, null, null, null);
        }

        return getPagedConfigurationSettings(result);
    }

    /**
     * Gets all ConfigurationSetting settings.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @return the observable to the Page&lt;ConfigurationSetting&gt; object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    private Flux<ConfigurationSetting> listKeyValues(@NonNull String nextPageLink) {
        Mono<RestResponse<Page<ConfigurationSetting>>> result = service.listKeyValuesNext(baseUri.toString(), nextPageLink);
        return getPagedConfigurationSettings(result);
    }

    /**
     * Lists chronological/historical representation of ConfigurationSetting resource(s). Revisions eventually expire (default 30 days).
     * For all operations key is optional parameter. If ommited it implies any key.
     * For all operations label is optional parameter. If ommited it implies any label.
     *
     * @param filter query options
     * @return Revisions of the ConfigurationSetting
     */
    public Flux<ConfigurationSetting> listKeyValueRevisions(RevisionFilter filter) {
        Mono<RestResponse<Page<ConfigurationSetting>>> result;
        if (filter != null) {
            result = service.listKeyValueRevisions(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listKeyValueRevisions(baseUri.toString(), null, null, null, null, null);
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
        return Flux.fromIterable(page.items()).concatWith(listKeyValues(nextPageLink));
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
}
