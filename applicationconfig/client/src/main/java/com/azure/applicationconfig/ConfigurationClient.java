// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.PageImpl;
import com.azure.applicationconfig.implementation.RestPagedResponseImpl;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.Key;
import com.azure.applicationconfig.models.KeyLabelFilter;
import com.azure.applicationconfig.models.KeyValueCreateUpdateParameters;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.Label;
import com.azure.applicationconfig.models.RevisionFilter;
import com.microsoft.rest.v3.ServiceClient;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import com.microsoft.rest.v3.http.rest.RestResponse;
import com.microsoft.rest.v3.implementation.RestProxy;
import com.microsoft.rest.v3.implementation.Validator;
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
    public static final String ETAG_ANY = "*";

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
     * <p>
     * The label value for the ConfigurationSetting is optional. If not specified, the
     * {@link ConfigurationSetting#NULL_LABEL} is used.
     *
     * @param configurationSetting The key, value, and label to set.
     * @return ConfigurationSetting that was created or updated
     * @throws com.microsoft.azure.v3.CloudException when a ConfigurationSetting with the same key and label exists.
     */
    public Mono<RestResponse<ConfigurationSetting>> addKeyValue(ConfigurationSetting configurationSetting) {
        Validator.validate(configurationSetting);
        KeyValueCreateUpdateParameters parameters = new KeyValueCreateUpdateParameters()
                .withValue(configurationSetting.value())
                .withContentType(configurationSetting.contentType())
                .withTags(configurationSetting.tags());

        return service.setKey(baseUri.toString(), configurationSetting.key(), configurationSetting.label(), parameters, null, getETagValue(ETAG_ANY));
    }

    /**
     * Creates or updates a configuration value in the service.
     * <p>
     * If {@link ConfigurationSetting#etag()} is specified, the configuration value is added or updated if the current
     * value's etag matches. If the etag's value is equal to {@link ConfigurationClient#ETAG_ANY}, the setting will
     * always be updated.
     *
     * @param configurationSetting key and value to set
     * @return ConfigurationSetting that was created or updated
     * @throws com.microsoft.azure.v3.CloudException If the {@link ConfigurationSetting#etag()} was specified, is not
     *                                               {@link ConfigurationClient#ETAG_ANY}, and the current configuration value's etag does not match.
     */
    public Mono<RestResponse<ConfigurationSetting>> setKeyValue(ConfigurationSetting configurationSetting) {
        Validator.validate(configurationSetting);
        KeyValueCreateUpdateParameters parameters = new KeyValueCreateUpdateParameters()
                .withValue(configurationSetting.value())
                .withContentType(configurationSetting.contentType())
                .withTags(configurationSetting.tags());

        return service.setKey(baseUri.toString(), configurationSetting.key(), configurationSetting.label(), parameters, getETagValue(configurationSetting.etag()), null);
    }


    /**
     * Updates an existing configuration value in the service. The setting must already exist.
     * <p>
     * The label value for the ConfigurationSetting is optional. If not specified, the
     * {@link ConfigurationSetting#NULL_LABEL} is used.
     *
     * If the {@link ConfigurationSetting#etag()} is specified, the configuration value is only updated if it matches.
     *
     * @param configurationSetting The key, value, and label to set.
     * @return ConfigurationSetting that was created or updated
     * @throws com.microsoft.azure.v3.CloudException when a ConfigurationSetting with the same key and label exists.
     */
    public Mono<RestResponse<ConfigurationSetting>> updateKeyValue(ConfigurationSetting configurationSetting) {
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
     * @throws com.microsoft.azure.v3.CloudException with status code of 404 if the {@param key} and {@param label} does
     *                                               not exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> getKeyValue(String key) {
        return getKeyValue(key, null, null);
    }

    /**
     * Gets the ConfigurationSetting given the {@param key}, optional {@param label} and optional {@param etag}.
     * <p>
     * Supplying {@param etag} will result in a ConfigurationSetting only being returned if the current etag is not the
     * same value. This is to improve the client caching scenario, where they only want the configuration value if it
     * has changed.
     *
     * @param key   The key being retrieved
     * @param label Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used.
     * @param etag  Optional. If specified, will only get the ConfigurationSetting if the current etag does not match.
     * @return The configuration value in the service.
     * @throws com.microsoft.azure.v3.CloudException with status code of 404 if the {@param key} and {@param label} does
     *                                               not exist. If {@param etag} was specified, returns status code of 304 if the key has not been modified.
     */
    public Mono<RestResponse<ConfigurationSetting>> getKeyValue(String key, String label, String etag) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.getKeyValue(baseUri.toString(), key, label, null, null, null, getETagValue(etag));
    }

    /**
     * Deletes the ConfigurationSetting.
     *
     * @param key The key to delete.
     * @return the deleted ConfigurationSetting or none if didn't exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> deleteKeyValue(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        }

        return deleteKeyValue(key, null, null);
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
    public Mono<RestResponse<ConfigurationSetting>> deleteKeyValue(String key, String label, String etag) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
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
     * @throws com.microsoft.azure.v3.CloudException with status code 404 if the {@param key} does not exist.
     */
    public Mono<RestResponse<ConfigurationSetting>> lockKeyValue(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        }

        return lockKeyValue(key, null);
    }

    /**
     * Places a lock on ConfigurationSetting. If present, label must be explicit label value (not a wildcard).
     * For all operations it's an optional parameter. If omitted it implies null label.
     *
     * @param key    key name
     * @param label  Optional. If not specified, {@link ConfigurationSetting#NULL_LABEL} is used.
     * @param filter eTagFilter
     * @return ConfigurationSetting
     */
    public Mono<RestResponse<ConfigurationSetting>> lockKeyValue(String key, String label) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.lockKeyValue(baseUri.toString(), key, label, null, null);
    }

    /**
     * Unlocks ConfigurationSetting.
     * @param key key name
     * @return ConfigurationSetting
     */
    public Mono<RestResponse<ConfigurationSetting>> unlockKeyValue(String key) {
        return unlockKeyValue(key, null);
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
    public Mono<RestResponse<ConfigurationSetting>> unlockKeyValue(String key, String label) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        } else if (label == null) {
            label = ConfigurationSetting.NULL_LABEL;
        }

        return service.unlockKeyValue(baseUri.toString(), key, label, null, null);
    }

    /**
     * Lists the KeyValues.
     *
     * @param filter query options
     * @return KeyValues
     */
    public Flux<ConfigurationSetting> listKeyValues(KeyValueListFilter filter) {
        Mono<RestResponse<PageImpl<ConfigurationSetting>>> result;
        if (filter != null) {
            result = service.listKeyValues(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDateTime(), filter.range());
        } else {
            result = service.listKeyValues(baseUri.toString(), null, null, null, null, null);
        }

        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchConfigurationSettings);
    }

    /**
     * Gets all ConfigurationSetting settings.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @return the observable to the Page&lt;ConfigurationSetting&gt; object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    private Flux<ConfigurationSetting> listKeyValues(@NonNull String nextPageLink) {
        return service.listKeyValuesNext(baseUri.toString(), nextPageLink)
                .flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchConfigurationSettings);
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
        Mono<RestResponse<PageImpl<ConfigurationSetting>>> result;
        if (filter != null) {
            result = service.listKeyValueRevisions(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listKeyValueRevisions(baseUri.toString(), null, null, null, null, null);
        }

        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchConfigurationSettings);
    }

    /**
     * List all Labels.
     *
     * @return labels
     */
    public Flux<Label> listLabels(KeyLabelFilter filter) {
        Mono<RestResponse<PageImpl<Label>>> result;
        if (filter != null) {
            result = service.listLabels(baseUri.toString(), filter.name(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listLabels(baseUri.toString(), null, null, null, null);
        }

        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchLabels);
    }

    /**
     * Gets all Labels.
     *
     * @param nextPageLink The nextPageLink from the previous successful call to List operation.
     * @return the observable to the Page&lt;Label&gt; object.
     */
    private Flux<Label> listLabels(@NonNull String nextPageLink) {
        return service.listLabelsNext(baseUri.toString(), nextPageLink)
                .flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchLabels);
    }

    /**
     * List all Keys.
     *
     * @return keys
     */
    public Flux<Key> listKeys(KeyLabelFilter filter) {
        Mono<RestResponse<PageImpl<Key>>> result;
        if (filter != null) {
            result = service.listKeys(baseUri.toString(), filter.name(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listKeys(baseUri.toString(), null, null, null, null);
        }

        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchKeys);
    }

    /**
     * Gets all Keys.
     *
     * @param nextPageLink The nextPageLink from the previous successful call to List operation.
     * @return A stream of Keys from the nextPageLink
     */
    private Flux<Key> listKeys(@NonNull String nextPageLink) {
        return service.listKeysNext(baseUri.toString(), nextPageLink)
                .flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())))
                .concatMap(this::extractAndFetchKeys);
    }

    private Publisher<ConfigurationSetting> extractAndFetchConfigurationSettings(RestPagedResponseImpl<ConfigurationSetting> page) {
        String nextPageLink = page.nextLink();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listKeyValues(nextPageLink));
    }

    private Publisher<Key> extractAndFetchKeys(RestPagedResponseImpl<Key> page) {
        if (page.nextLink() == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listKeys(page.nextLink()));
    }

    private Publisher<Label> extractAndFetchLabels(RestPagedResponseImpl<Label> page) {
        if (page.nextLink() == null) {
            return Flux.fromIterable(page.items());
        }
        return Flux.fromIterable(page.items()).concatWith(listLabels(page.nextLink()));
    }

    /**
     * Azure Configuration service requires that the etag value is surrounded in quotation marks.
     * @param etag The etag to get the value for. If null is pass in, an empty string is returned.
     * @return The etag surrounded by quotations. (ex. "etag")
     */
    private static String getETagValue(String etag) {
        return etag == null ? "" : "\"" + etag + "\"";
    }
}
