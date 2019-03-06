// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.PageImpl;
import com.azure.applicationconfig.implementation.RestPagedResponseImpl;
import com.azure.applicationconfig.models.ETagFilter;
import com.azure.applicationconfig.models.Key;
import com.azure.applicationconfig.models.KeyLabelFilter;
import com.azure.applicationconfig.models.KeyValueFilter;
import com.azure.applicationconfig.models.KeyValue;
import com.azure.applicationconfig.models.KeyValueCreateUpdateParameters;
import com.azure.applicationconfig.models.KeyValueListFilter;
import com.azure.applicationconfig.models.Label;
import com.azure.applicationconfig.models.RevisionFilter;
import com.microsoft.azure.v3.CloudException;
import com.microsoft.rest.v3.RestPagedResponse;
import com.microsoft.rest.v3.RestProxy;
import com.microsoft.rest.v3.RestResponse;
import com.microsoft.rest.v3.ServiceClient;
import com.microsoft.rest.v3.Validator;
import com.microsoft.rest.v3.annotations.BodyParam;
import com.microsoft.rest.v3.annotations.DELETE;
import com.microsoft.rest.v3.annotations.ExpectedResponses;
import com.microsoft.rest.v3.annotations.GET;
import com.microsoft.rest.v3.annotations.HeaderParam;
import com.microsoft.rest.v3.annotations.Host;
import com.microsoft.rest.v3.annotations.HostParam;
import com.microsoft.rest.v3.annotations.PUT;
import com.microsoft.rest.v3.annotations.PathParam;
import com.microsoft.rest.v3.annotations.QueryParam;
import com.microsoft.rest.v3.annotations.UnexpectedResponseExceptionType;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.HttpLoggingPolicy;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Client that contains all the operations for KeyValues in Azure Configuration Store.
 */
public final class AzConfigClient extends ServiceClient {
    static final String SDK_NAME = "Azure-Configuration";
    static final String SDK_VERSION = "1.0.0-SNAPSHOT";

    private URL baseUri;
    private AzConfigService service;

    /**
     * Create a new instance of AzConfigClient that uses connectionString for authentication.
     * @param connectionString connection string in the format "Endpoint=_endpoint_;Id=_id_;Secret=_secret_"
     * @return an instance of AzConfigClient
     */
    public static AzConfigClient create(String connectionString) {
        return create(connectionString, new PipelineOptions());
    }

    /**
     * Create a new instance of AzConfigClient with pipeline options that uses connectionString for authentication.
     * @param connectionString connection string in the format "Endpoint=_endpoint_;Id=_id_;Secret=_secret_"
     * @param pipelineOptions pipeline options
     * @return an instance of AzConfigClient
     */
    public static AzConfigClient create(String connectionString, PipelineOptions pipelineOptions) {
        AzConfigCredentials credentials = AzConfigCredentials.parseConnectionString(connectionString);
        return new AzConfigClient(credentials, pipelineOptions);
    }

    /**
     * Create a new instance of AzConfigClient with pipeline  that uses credentials for authentication.
     * @param credentials AzConfigCredentials to authenticate
     * @param pipeline pre-defined pipeline
     * @return an instance of AzConfigClient
     */

    public static AzConfigClient create(AzConfigCredentials credentials, HttpPipeline pipeline) {
        return new AzConfigClient(credentials, pipeline);
    }

    private AzConfigClient(AzConfigCredentials credentials, PipelineOptions pipelineOptions) {
        this(credentials, createPipeline(credentials, pipelineOptions));
    }

    private AzConfigClient(AzConfigCredentials credentials, HttpPipeline pipeline) {
        super(pipeline);
        this.service = RestProxy.create(AzConfigService.class, pipeline);
        baseUri = credentials.baseUri;
    }

    /**
     * The interface defining all the services for GeneratedQueues to be used
     * by the proxy service to perform REST calls.
     */
    @Host("{url}")
    private interface AzConfigService {
        @GET("kv/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, KeyValue>> getKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                                      @QueryParam("fields") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime,
                                                                      @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

        @PUT("kv/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, KeyValue>> setKey(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label, @BodyParam("application/json") KeyValueCreateUpdateParameters keyValueParameters,
                                @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

        @GET("kv")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<KeyValue>>> listKeyValues(@HostParam("url") String url, @QueryParam("key") String key, @QueryParam("label") String label,
                                                               @QueryParam("fields") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

        @GET("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<KeyValue>>> listKeyValuesNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);

        @DELETE("kv/{key}")
        @ExpectedResponses({200, 204})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, KeyValue>> delete(@HostParam("url") String url, @PathParam("key") String key,  @QueryParam("label") String label,
                                @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

        @PUT("locks/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, KeyValue>> lockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                      @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

        @DELETE("locks/{key}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, KeyValue>> unlockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                        @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

        @GET("revisions")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<KeyValue>>> listKeyValueRevisions(@HostParam("url") String url,
                                                                     @QueryParam("key") String key, @QueryParam("label") String label, @QueryParam("fields") String fields,
                                                                     @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

        @GET("labels")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<Label>>> listLabels(@HostParam("url") String url, @QueryParam("name") String name, @QueryParam("fields") String fields,
                                                         @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

        @GET("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<Label>>> listLabelsNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);

        @GET("keys")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<Key>>> listKeys(@HostParam("url") String url, @QueryParam("name") String name, @QueryParam("fields") String fields,
                                                  @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

        @GET("{nextUrl}")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(CloudException.class)
        Mono<RestResponse<Map<String, String>, PageImpl<Key>>> listKeysNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);
    }

    /**
     * Sets key value. Label value for the keyValue is optional, if not specified or label=%00 it implies null label.
     * @param keyValue key and value to set
     * @return KeyValue that was created or updated
     */
    public Mono<RestResponse<Map<String, String>, KeyValue>> setKeyValue(KeyValue keyValue) {
        return setKeyValue(keyValue, null);
    }

    /**
     * Sets key value. Label value for the keyValue is optional, if not specified or label=%00 it implies null label.
     * @param keyValue key and value to set
     * @return KeyValue that was created or updated
     */
    public Mono<RestResponse<Map<String, String>, KeyValue>> setKeyValue(KeyValue keyValue, ETagFilter filter) {
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
     * @param key the key being retrievd
     * @param filter options for the request
     * @return KeyValue object
     */
    public Mono<RestResponse<Map<String, String>, KeyValue>> getKeyValue(String key, KeyValueFilter filter) {
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
    public Mono<RestResponse<Map<String, String>, KeyValue>> deleteKeyValue(String key) {
        return deleteKeyValue(key, null, null);
    }

    /**
     * Deletes the KeyValue.
     * @param key key of the keyValue to delete
     * @param filter eTag filter to add to If-Match or If-None-Match header
     * @return the deleted KeyValue or none if didn't exist.
     */
    public Mono<RestResponse<Map<String, String>, KeyValue>> deleteKeyValue(String key, String label, ETagFilter filter) {
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

    /**
     * Gets all KeyValue settings.
     *
     * @return the Flux&lt;RestPagedResponse&lt;KeyValue&gt;&gt; object if successful.
     */
    private Flux<RestPagedResponse<KeyValue>> listSinglePageAsync(KeyValueListFilter filter) {
        Mono<RestResponse<Map<String, String>, PageImpl<KeyValue>>> result;
        if (filter != null) {
            result = service.listKeyValues(baseUri.toString(), filter.key(), filter.label(), filter.fields(), filter.acceptDateTime(), filter.range());
        } else {
            result = service.listKeyValues(baseUri.toString(), null, null, null, null, null);
        }
        return result.flatMapMany(p -> Flux.just(new RestPagedResponseImpl<>(p.body().items(), p.body().nextPageLink(), p.request(), p.headers(), p.statusCode())));
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

    private Flux<RestPagedResponseImpl<KeyValue>> listNextSinglePageAsync(@NonNull String nextPageLink) {
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
    public Mono<RestResponse<Map<String, String>, KeyValue>> lockKeyValue(String key) {
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
    public Mono<RestResponse<Map<String, String>, KeyValue>> lockKeyValue(String key, String label, ETagFilter filter) {
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
    public Mono<RestResponse<Map<String, String>, KeyValue>> unlockKeyValue(String key) {
        return unlockKeyValue(key, null, null);
    }

    /**
     * Unlocks KeyValue. If present, label must be explicit label value (not a wildcard).
     * For all operations it's an optional parameter. If omitted it implies null label.
     * @param key key name
     * @return KeyValue
     */
    public Mono<RestResponse<Map<String, String>, KeyValue>> unlockKeyValue(String key, String label, ETagFilter filter) {
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
        Mono<RestResponse<Map<String, String>, PageImpl<KeyValue>>> result;
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
        Mono<RestResponse<Map<String, String>, PageImpl<Label>>> result;
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
       return  receiver.apply(p);
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
        Mono<RestResponse<Map<String, String>, PageImpl<Key>>> result;
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

    /**
     * Creates an pipeline to process the HTTP requests and Responses.
     *
     * @param credentials credentials the pipeline will use to authenticate the requests
     * @return the pipeline
     */
    private static HttpPipeline createPipeline(AzConfigCredentials credentials, PipelineOptions pipelineOptions) {
        if (pipelineOptions == null) {
            throw new IllegalArgumentException("pipelineOptions cannot be null.");
        }
        // Closest to API goes first, closest to wire goes last.
//        ArrayList<RequestPolicyFactory> factories = new ArrayList<>();
        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();

        policies.add(new UserAgentPolicy(String.format("Azure-SDK-For-Java/%s (%s)", SDK_NAME, SDK_VERSION)));
        policies.add(new RequestIdPolicy());
        policies.add(new AzConfigCredentialsPolicy(credentials));
        policies.add(new RetryPolicy());
//        policies.add(new RequestRetryPolicyFactory()); // todo - do we really need custom retry policy here?
        policies.add(new HttpLoggingPolicy(pipelineOptions.httpLogDetailLevel()));

        return new HttpPipeline(policies.toArray(new HttpPipelinePolicy[policies.size()]));
    }

    static class AzConfigCredentials {
        private URL baseUri;
        private String credential;
        private byte[] secret;

        URL baseUri() {
            return baseUri;
        }

        String credential() {
            return credential;
        }

        byte[] secret() {
            return secret;
        }

        static AzConfigCredentials parseConnectionString(String connectionString) {
            if (connectionString == null || connectionString.isEmpty()) {
                throw new IllegalArgumentException(connectionString);
            }

            // Parse connection string
            String[] args = connectionString.split(";");
            if (args.length < 3) {
                throw new IllegalArgumentException("invalid connection string segment count");
            }

            String endpointString = "endpoint=";
            String idString = "id=";
            String secretString = "secret=";

            AzConfigCredentials credentials = new AzConfigCredentials();

            for (String arg : args) {
                String segment = arg.trim();
                try {
                    if (segment.toLowerCase().startsWith(endpointString)) {
                        credentials.baseUri = new URL(segment.substring(segment.indexOf('=') + 1));
                    } else if (segment.toLowerCase().startsWith(idString)) {
                        credentials.credential = segment.substring(segment.indexOf('=') + 1);
                    } else if (segment.toLowerCase().startsWith(secretString)) {
                        String secretBase64 = segment.substring(segment.indexOf('=') + 1);
                        credentials.secret = Base64.getDecoder().decode(secretBase64);
                    }
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
            return credentials;
        }
    }
}
