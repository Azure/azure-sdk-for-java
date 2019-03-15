// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.PageImpl;
import com.azure.applicationconfig.implementation.RestPagedResponseImpl;
import com.azure.applicationconfig.models.ETagFilter;
import com.azure.applicationconfig.models.Key;
import com.azure.applicationconfig.models.KeyLabelFilter;
import com.azure.applicationconfig.models.KeyValue;
import com.azure.applicationconfig.models.KeyValueCreateUpdateParameters;
import com.azure.applicationconfig.models.KeyValueFilter;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.Label;
import com.azure.applicationconfig.models.RevisionFilter;
import com.microsoft.rest.v3.ServiceClient;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import com.microsoft.rest.v3.http.rest.RestPagedResponse;
import com.microsoft.rest.v3.http.rest.RestResponse;
import com.microsoft.rest.v3.implementation.RestProxy;
import com.microsoft.rest.v3.implementation.Validator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Client that contains all the operations for KeyValues in Azure Configuration Store.
 */
public final class AzConfigClient extends ServiceClient {
    static final String SDK_NAME = "Azure-Configuration";
    static final String SDK_VERSION = "1.0.0-SNAPSHOT";

    private final URL baseUri;
    private final ApplicationConfigService service;

    /**
     * Create a new instance of AzConfigClient that uses connectionString for authentication.
     * @param connectionString connection string in the format "Endpoint=_endpoint_;Id=_id_;Secret=_secret_"
     */
    public AzConfigClient(String connectionString) {
        super(new HttpPipeline(getDefaultPolicies(connectionString)));

        this.service = RestProxy.create(ApplicationConfigService.class, this);
        this.baseUri = new ApplicationConfigCredentials(connectionString).baseUri();
    }

    public AzConfigClient(String connectionString, HttpPipeline pipeline) {
        super(pipeline);

        this.service = RestProxy.create(ApplicationConfigService.class, this);
        this.baseUri = new ApplicationConfigCredentials(connectionString).baseUri();
    }

    /**
     * Sets key value. Label value for the keyValue is optional, if not specified or label=%00 it implies null label.
     * @param keyValue key and value to set
     * @return KeyValue that was created or updated
     */
    public Mono<RestResponse<KeyValue>> setKeyValue(KeyValue keyValue) {
        return setKeyValue(keyValue, null);
    }

    /**
     * Sets key value. Label value for the keyValue is optional, if not specified or label=%00 it implies null label.
     * @param keyValue key and value to set
     * @return KeyValue that was created or updated
     */
    public Mono<RestResponse<KeyValue>> setKeyValue(KeyValue keyValue, ETagFilter filter) {
        Validator.validate(keyValue);
        KeyValueCreateUpdateParameters parameters = new KeyValueCreateUpdateParameters().withValue(keyValue.value())
                .withContentType(keyValue.contentType())
                .withTags(keyValue.tags());

        if (filter != null) {
            return service.setKey(baseUri.toString(), keyValue.key(), keyValue.label(), parameters, filter.ifMatch(), filter.ifNoneMatch());
        }
        return service.setKey(baseUri.toString(), keyValue.key(), keyValue.label(), parameters, null, null);
    }

    /**
     * Gets the KeyValue object for the specified key and KeyValueFilter2 parameters.
     * @param key the key being retrieved
     * @param filter options for the request
     * @return KeyValue object
     */
    public Mono<RestResponse<KeyValue>> getKeyValue(String key, KeyValueFilter filter) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        }
        if (filter != null) {
            return service.getKeyValue(baseUri.toString(), key, filter.label(), filter.fields(),
                    filter.acceptDateTime(), filter.ifMatch(), filter.ifNoneMatch());
        }
        return service.getKeyValue(baseUri.toString(), key, null, null, null, null, null);
    }

    /**
     * Deletes the KeyValue.
     * @param key keyValue to delete
     * @return the deleted KeyValue or none if didn't exist.
     */
    public Mono<RestResponse<KeyValue>> deleteKeyValue(String key) {
        return deleteKeyValue(key, null, null);
    }

    /**
     * Deletes the KeyValue.
     * @param key key of the keyValue to delete
     * @param filter eTag filter to add to If-Match or If-None-Match header
     * @return the deleted KeyValue or none if didn't exist.
     */
    public Mono<RestResponse<KeyValue>> deleteKeyValue(String key, String label, ETagFilter filter) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        }
        if (filter != null) {
            return service.delete(baseUri.toString(), key, label, filter.ifMatch(), filter.ifNoneMatch());
        }
        return service.delete(baseUri.toString(), key, label, null, null);
    }

    /**
     * Lists the KeyValues.
     * @param filter query options
     * @return KeyValues
     */
    public Flux<KeyValue> listKeyValues(KeyValueListFilter filter) {
        return listKeyValues(filter, pageResponseFlux -> pageResponseFlux.map(r -> r.items())
                                                            .flatMapIterable(i -> i));
    }

    public <T> Flux<T> listKeyValues(KeyValueListFilter filter, Function<Flux<RestPagedResponse<KeyValue>>, ? extends Flux<T>> receiver) {
        Flux<RestPagedResponse<KeyValue>> p = listSinglePageAsync(filter)
                       .concatMap(page -> {
                           String nextPageLink = page.nextLink();
                           if (nextPageLink == null) {
                               return Flux.just(page);
                           }
                           return Flux.just(page).concatWith(listNextAsync(nextPageLink));
                       });
        return receiver.apply(p);
    }

    public static List<HttpPipelinePolicy> getDefaultPolicies(String connectionString) {
        final ApplicationConfigCredentials credentials = new ApplicationConfigCredentials(connectionString);
        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION)));
        policies.add(new RequestIdPolicy());
        policies.add(new RetryPolicy());
        policies.add(new AzConfigCredentialsPolicy(credentials));

        return policies;
    }

    /**
     * Gets all KeyValue settings.
     *
     * @return the Flux&lt;RestPagedResponse&lt;KeyValue&gt;&gt; object if successful.
     */
    private Flux<RestPagedResponse<KeyValue>> listSinglePageAsync(KeyValueListFilter filter) {
        Mono<RestResponse<PageImpl<KeyValue>>> result;
        if (filter != null) {
            result = service.listKeyValues(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDateTime(), filter.range());
        } else {
            result = service.listKeyValues(baseUri.toString(), null, null, null, null, null);
        }

        return result.flatMapMany(p ->
                Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(),
                        p.headers(), p.statusCode())));
    }

    /**
     * Gets all KeyValue settings.
     *
     * @param nextPageLink The NextLink from the previous successful call to List operation.
     * @return the observable to the Page&lt;KeyValue&gt; object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    private Flux<RestPagedResponse<KeyValue>> listNextAsync(@NonNull String nextPageLink) {
        return listNextSinglePageAsync(nextPageLink)
                       .concatMap(page -> {
                           String nextPageLink1 = page.nextLink();
                           if (nextPageLink1 == null) {
                               return Flux.just(page);
                           }
                           return Flux.just(page).concatWith(p -> listNextAsync(nextPageLink1));
                       });
    }

    private Flux<RestPagedResponse<KeyValue>> listNextSinglePageAsync(@NonNull String nextPageLink) {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        return service.listKeyValuesNext(baseUri.toString(), nextPageLink)
                       .flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
    }

    /**
     * Places a lock on KeyValue.
     * @param key key name
     * @return KeyValue
     */
    public Mono<RestResponse<KeyValue>> lockKeyValue(String key) {
        return lockKeyValue(key, null, null);
    }

    /**
     * Places a lock on KeyValue. If present, label must be explicit label value (not a wildcard).
     * For all operations it's an optional parameter. If omitted it implies null label.
     * @param key key name
     * @param label label
     * @param filter eTagFilter
     * @return KeyValue
     */
    public Mono<RestResponse<KeyValue>> lockKeyValue(String key, String label, ETagFilter filter) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        }
        if (filter != null) {
            return service.lockKeyValue(baseUri.toString(), key, label, filter.ifMatch(), filter.ifNoneMatch());
        }
        return service.lockKeyValue(baseUri.toString(), key, label, null, null);
    }

    /**
     * Unlocks KeyValue. If present, label must be explicit label value (not a wildcard).
     * For all operations it's an optional parameter. If omitted it implies null label.
     * @param key key name
     * @return KeyValue
     */
    public Mono<RestResponse<KeyValue>> unlockKeyValue(String key) {
        return unlockKeyValue(key, null, null);
    }

    /**
     * Unlocks KeyValue. If present, label must be explicit label value (not a wildcard).
     * For all operations it's an optional parameter. If omitted it implies null label.
     * @param key key name
     * @return KeyValue
     */
    public Mono<RestResponse<KeyValue>> unlockKeyValue(String key, String label, ETagFilter filter) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Parameter key is required and cannot be null or empty");
        }
        if (filter != null) {
            return service.unlockKeyValue(baseUri.toString(), key, label, filter.ifMatch(), filter.ifNoneMatch());
        }
        return service.unlockKeyValue(baseUri.toString(), key, label, null, null);
    }

    /**
     * Lists chronological/historical representation of KeyValue resource(s). Revisions eventually expire (default 30 days).
     * For all operations key is optional parameter. If ommited it implies any key.
     * For all operations label is optional parameter. If ommited it implies any label.
     * @param filter query options
     * @return Revisions of the KeyValue
     */
    public Flux<KeyValue> listKeyValueRevisions(RevisionFilter filter) {
        return listKeyValueRevisions(filter, pageResponseFlux -> pageResponseFlux.map(r -> r.items())
                                                              .flatMapIterable(i -> i));
    }

    public <T> Flux<T> listKeyValueRevisions(RevisionFilter filter, Function<Flux<RestPagedResponse<KeyValue>>, ? extends Flux<T>> receiver) {
        Flux<RestPagedResponse<KeyValue>> p = listRevisionsSinglePageAsync(filter)
                       .concatMap(page -> {
                           String nextPageLink = page.nextLink();
                           if (nextPageLink == null) {
                               return Flux.just(page);
                           }
                           return Flux.just(page).concatWith(listNextAsync(nextPageLink));
                       });
        return receiver.apply(p);
    }

    /**
     * Gets all Revisions for KeyValue(s).
     *
     * @return the Single&lt;Page&lt;KeyValue&gt;&gt; object if successful.
     */
    private Flux<RestPagedResponse<KeyValue>> listRevisionsSinglePageAsync(RevisionFilter filter) {
        Mono<RestResponse<PageImpl<KeyValue>>> result;
        if (filter != null) {
            result = service.listKeyValueRevisions(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listKeyValueRevisions(baseUri.toString(), null, null, null, null, null);
        }
        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
    }

    /**
     * List all Labels.
     *
     * @return labels
     */
    public Flux<Label> listLabels(KeyLabelFilter filter) {
        return listLabels(filter, pageResponseFlux -> pageResponseFlux.map(r -> r.items())
                                                            .flatMapIterable(i -> i));
    }

    public <T> Flux<T> listLabels(KeyLabelFilter filter, Function<Flux<RestPagedResponse<Label>>, ? extends Flux<T>> receiver) {
        Flux<RestPagedResponse<Label>> p = listLabelsSinglePageAsync(filter)
                       .concatMap(page -> {
                           String nextPageLink = page.nextLink();
                           if (nextPageLink == null) {
                               return Flux.just(page);
                           }
                           return Flux.just(page).concatWith(listLabelsNextAsync(nextPageLink));
                       });
        return receiver.apply(p);
    }

    /**
     * Gets all Labels.
     *
     * @param nextPageLink The nextPageLink from the previous successful call to List operation.
     * @return the observable to the Page&lt;Label&gt; object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    private Flux<RestPagedResponse<Label>> listLabelsNextAsync(@NonNull String nextPageLink) {
        return listLabelsNextSinglePageAsync(nextPageLink)
                       .concatMap(page -> {
                           String nextPageLink1 = page.nextLink();
                           if (nextPageLink1 == null) {
                               return Flux.just(page);
                           }
                           return Flux.just(page).concatWith(p -> listLabelsNextAsync(nextPageLink1));
                       });
    }

    /**
     * Gets all Labels.
     *
     * @return the Flux&lt;RestPagedResponse&lt;Label&gt;&gt; object if successful.
     */
    private Flux<RestPagedResponse<Label>> listLabelsSinglePageAsync(KeyLabelFilter filter) {
        Mono<RestResponse<PageImpl<Label>>> result;
        if (filter != null) {
            result = service.listLabels(baseUri.toString(), filter.name(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listLabels(baseUri.toString(), null, null, null, null);
        }
        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
    }

    private Flux<RestPagedResponse<Label>> listLabelsNextSinglePageAsync(@NonNull String nextPageLink) {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        String nextUrl = String.format("%s", nextPageLink);
        return service.listLabelsNext(baseUri.toString(), nextUrl)
                       .flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
    }

    /**
     * List all Keys.
     *
     * @return keys
     */
    public Flux<Key> listKeys(KeyLabelFilter filter) {
        return listKeys(filter, pageResponseFlux -> pageResponseFlux.map(r -> r.items())
                                                            .flatMapIterable(i -> i));
    }

    public <T> Flux<T> listKeys(KeyLabelFilter filter, Function<Flux<RestPagedResponse<Key>>, ? extends Flux<T>> receiver) {
        Flux<RestPagedResponse<Key>> p = listKeysSinglePageAsync(filter)
                       .concatMap(page -> {
                           String nextPageLink = page.nextLink();
                           if (nextPageLink == null) {
                               return Flux.just(page);
                           }
                           return Flux.just(page).concatWith(listKeysNextAsync(nextPageLink));
                       });
        return receiver.apply(p);
    }

    /**
     * Gets all Keys.
     *
     * @param nextPageLink The nextPageLink from the previous successful call to List operation.
     * @return the observable to the Page&lt;Key&gt; object.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     */
    private Flux<RestPagedResponse<Key>> listKeysNextAsync(@NonNull String nextPageLink) {
        return listKeysNextSinglePageAsync(nextPageLink)
                .concatMap(page -> {
                    String nextPageLink1 = page.nextLink();
                    if (nextPageLink1 == null) {
                        return Flux.just(page);
                    }
                    return Flux.just(page).concatWith(listKeysNextAsync(nextPageLink1));
                });
    }

    /**
     * Gets all Keys.
     *
     * @return the Flux&lt;RestPagedResponse&lt;Key&gt;&gt; object if successful.
     */
    private Flux<RestPagedResponse<Key>> listKeysSinglePageAsync(KeyLabelFilter filter) {
        Mono<RestResponse<PageImpl<Key>>> result;
        if (filter != null) {
            result = service.listKeys(baseUri.toString(), filter.name(), filter.fields(), filter.acceptDatetime(), filter.range());
        } else {
            result = service.listKeys(baseUri.toString(), null, null, null, null);
        }
        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
    }

    private Flux<RestPagedResponse<Key>> listKeysNextSinglePageAsync(String nextPageLink) {
        if (nextPageLink == null) {
            throw new IllegalArgumentException("Parameter nextPageLink is required and cannot be null.");
        }
        return service.listKeysNext(baseUri.toString(), nextPageLink)
                       .flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
    }
}
