// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.models.Subscription;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines a few utilities.
 */
public final class Utils {
    private Utils() {
    }

    /**
     * Converts an object Boolean to a primitive boolean.
     *
     * @param value the Boolean value
     * @return false if the given Boolean value is null or false else true
     */
    public static boolean toPrimitiveBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value.booleanValue();
    }

    /**
     * Converts an object Integer to a primitive int.
     *
     * @param value the Integer value
     * @return 0 if the given Integer value is null else integer value
     */
    public static int toPrimitiveInt(Integer value) {
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    /**
     * Converts an object Long to a primitive int.
     *
     * @param value the Long value
     * @return 0 if the given Long value is null else integer value
     */
    public static int toPrimitiveInt(Long value) {
        if (value == null) {
            return 0;
        }
        return Math.toIntExact(value);
    }

    /**
     * Converts an object Long to a primitive long.
     *
     * @param value the Long value
     * @return 0 if the given Long value is null else long value
     */
    public static long toPrimitiveLong(Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Creates an Odata filter string that can be used for filtering list results by tags.
     *
     * @param tagName the name of the tag. If not provided, all resources will be returned.
     * @param tagValue the value of the tag. If not provided, only tag name will be filtered.
     * @return the Odata filter to pass into list methods
     */
    public static String createOdataFilterForTags(String tagName, String tagValue) {
        if (tagName == null) {
            return null;
        } else if (tagValue == null) {
            return String.format("tagname eq '%s'", tagName);
        } else {
            return String.format("tagname eq '%s' and tagvalue eq '%s'", tagName, tagValue);
        }
    }

    /**
     * Gets a Mono of type {@code U}, where U extends {@link Indexable}, that emits only the root
     * resource from a given Mono of {@link Indexable}.
     *
     * @param stream the input Mono of {@link Indexable}
     * @param <U> the specialized type of last item in the input stream
     * @return a Mono that emits last item
     */
    @SuppressWarnings("unchecked")
    public static <U extends Indexable> Mono<U> rootResource(Mono<Indexable> stream) {
        return stream.map(indexable -> (U) indexable);
    }

    /**
     * Download a file asynchronously.
     *
     * @param url the URL pointing to the file
     * @param httpPipeline the http pipeline
     * @return an Observable pointing to the content of the file
     */
    public static Mono<byte[]> downloadFileAsync(String url, HttpPipeline httpPipeline) {
        FileService service = RestProxy.create(FileService.class, httpPipeline);
        try {
            return service.download(getHost(url), getPathAndQuery(url))
                .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue()));
        } catch (MalformedURLException ex) {
            return Mono.error(() -> ex);
        }
    }

    /**
     * Get host from url.
     *
     * @param urlString the url string
     * @return the host
     * @throws MalformedURLException when url is invalid format
     */
    public static String getHost(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String protocol = url.getProtocol();
        String host = url.getAuthority();
        return protocol + "://" + host;
    }

    /**
     * Get path from url.
     *
     * @param urlString the url string
     * @return the path
     * @throws MalformedURLException when the url is invalid format
     */
    public static String getPathAndQuery(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        String path = url.getPath();
        String query = url.getQuery();
        if (query != null && !query.isEmpty()) {
            path = path + "?" + query;
        }
        return path;
    }

    /**
     * Adds a value to the list if does not already exists.
     *
     * @param list the list
     * @param value value to add if not exists in the list
     */
    public static void addToListIfNotExists(List<String> list, String value) {
        boolean found = false;
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) {
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(value);
        }
    }

    /**
     * Removes a value from the list.
     *
     * @param list the list
     * @param value value to remove
     */
    public static void removeFromList(List<String> list, String value) {
        int foundIndex = -1;
        int i = 0;
        for (String id : list) {
            if (id.equalsIgnoreCase(value)) {
                foundIndex = i;
                break;
            }
            i++;
        }
        if (foundIndex != -1) {
            list.remove(foundIndex);
        }
    }

    /**
     * @param id resource id
     * @return resource group id for the resource id provided
     */
    public static String resourceGroupId(String id) {
        final ResourceId resourceId = ResourceId.fromString(id);
        return String.format("/subscriptions/%s/resourceGroups/%s",
                resourceId.subscriptionId(),
                resourceId.resourceGroupName());
    }

    /**
     * A Retrofit service used to download a file.
     */
    @Host("{$host}")
    @ServiceInterface(name = "FileService")
    private interface FileService {
        @Get("{path}")
        Mono<SimpleResponse<Flux<ByteBuffer>>> download(
            @HostParam("$host") String host, @PathParam(value = "path", encoded = true) String path);
    }

    /**
     * Gets the only subscription as the default one in the tenant if applicable.
     *
     * @param subscriptions the list of subscriptions
     * @throws IllegalStateException when no subscription or more than one subscription found
     * @return the only subscription existing in the tenant
     */
    public static String defaultSubscription(PagedIterable<Subscription> subscriptions) {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptions.forEach(subscription -> {
            subscriptionList.add(subscription);
        });
        if (subscriptionList.size() == 0) {
            throw new ClientLogger(Utils.class).logExceptionAsError(
                new IllegalStateException("Please create a subscription before you start resource management. "
                + "To learn more, see: https://azure.microsoft.com/en-us/free/."));
        } else if (subscriptionList.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("More than one subscription found in your tenant. "
                + "Please specify which one below is desired for resource management.");
            subscriptionList.forEach(subscription -> {
                stringBuilder.append("\n" + subscription.displayName() + " : " + subscription.subscriptionId());
            });
            throw new ClientLogger(Utils.class).logExceptionAsError(
                new IllegalStateException(stringBuilder.toString()));
        }
        return subscriptionList.get(0).subscriptionId();
    }

    /**
     * Generates default scope for oauth2 from the specific request
     * @param request a http request
     * @param environment the azure environment with current request
     * @return the default scope
     */
    public static String getDefaultScopeFromRequest(HttpRequest request, AzureEnvironment environment) {
        return getDefaultScopeFromUrl(request.getUrl().toString().toLowerCase(Locale.ROOT), environment);
    }

    /**
     * Generates default scope for oauth2 from the specific request
     * @param url the url in lower case of a http request
     * @param environment the azure environment with current request
     * @return the default scope
     */
    public static String getDefaultScopeFromUrl(String url, AzureEnvironment environment) {
        String resource = environment.getManagementEndpoint();
        for (Map.Entry<String, String> endpoint : environment.endpoints().entrySet()) {
            if (url.contains(endpoint.getValue())) {
                if (endpoint.getKey().equals(AzureEnvironment.Endpoint.KEYVAULT.identifier())) {
                    resource = String.format("https://%s/", endpoint.getValue().replaceAll("^\\.*", ""));
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.GRAPH.identifier())) {
                    resource = environment.getGraphEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.LOG_ANALYTICS.identifier())) {
                    resource = environment.getLogAnalyticsEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.APPLICATION_INSIGHTS.identifier())) {
                    resource = environment.getApplicationInsightsEndpoint();
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_STORE.identifier())
                    || endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_ANALYTICS.identifier())) {
                    resource = environment.getDataLakeEndpointResourceId();
                    break;
                }
            }
        }
        return removeTrailingSlash(resource) + "/.default";
    }

    /**
     * Removes the trailing slash of the string.
     * @param s the string
     * @return the string without trailing slash
     */
    public static String removeTrailingSlash(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * Get the Azure storage account connection string.
     * @param accountName storage account name
     * @param accountKey storage account key
     * @param environment the Azure environment
     * @return the storage account connection string.
     */
    public static String getStorageConnectionString(String accountName, String accountKey,
                                                    AzureEnvironment environment) {
        if (environment == null || environment.getStorageEndpointSuffix() == null) {
            environment = AzureEnvironment.AZURE;
        }
        String suffix = environment.getStorageEndpointSuffix().replaceAll("^\\.*", "");
        return String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=%s",
            accountName, accountKey, suffix);
    }

    /**
     * Combine {@link Flux#flatMap(Function, Function, Supplier)} with {@link Flux#flatMapSequential(Function)}.
     * @param flux the original flux
     * @param mapperOnNext the {@link Function} to call on next data and returning a sequence to merge
     * @param mapperOnError the {@link Function} to call on error signal and returning a sequence to merge
     * @param mapperOnComplete the {@link Supplier} to call on complete signal and returning a sequence to merge
     * @param <T> the original data type
     * @param <R> the return data type
     * @return a new flux
     */
    public static <T, R> Flux<R> flatMapSequential(Flux<T> flux,
        Function<? super T, ? extends Publisher<? extends R>> mapperOnNext,
        Function<? super Throwable, ? extends Publisher<? extends R>> mapperOnError,
        Supplier<? extends Publisher<? extends R>> mapperOnComplete) {
        return flux.materialize()
            .flatMapSequential(signal -> {
                if ((signal.isOnNext() ? 1 : 0) + (signal.isOnComplete() ? 1 : 0) + (signal.isOnError() ? 1 : 0) != 1) {
                    return Mono.error(new ClientLogger(Utils.class).logExceptionAsError(new IllegalStateException(
                        "Unexpected signal type, signal could only be one of the onNext, onComplete, onError"
                    )));
                }
                if (signal.isOnNext()) {
                    if (mapperOnNext != null) {
                        return mapperOnNext.apply(signal.get());
                    }
                    return Mono.empty();
                }
                if (signal.isOnComplete()) {
                    if (mapperOnComplete != null) {
                        return mapperOnComplete.get();
                    }
                    return Mono.empty();
                }
                Throwable exception = signal.getThrowable();
                if (mapperOnError != null) {
                    return mapperOnError.apply(exception);
                }
                if (exception != null) {
                    return Mono.error(exception);
                } else {
                    throw new ClientLogger(Utils.class).logExceptionAsError(new IllegalStateException());
                }
            });
    }

    /**
     * Combine {@link Flux#flatMap(Function, Function, Supplier)}
     * with {@link Flux#flatMapSequentialDelayError(Function, int, int)}.
     * @param flux the original flux
     * @param mapperOnNext the {@link Function} to call on next data and returning a sequence to merge
     * @param mapperOnError the {@link Function} to call on error signal and returning a sequence to merge
     * @param mapperOnComplete the {@link Supplier} to call on complete signal and returning a sequence to merge
     * @param maxConcurrency the maximum number of in-flight inner sequences
     * @param prefetch the maximum in-flight elements from each inner {@link Publisher} sequence
     * @param <T> the original data type
     * @param <R> the return data type
     * @return a new flux
     */
    public static <T, R> Flux<R> flatMapSequentialDelayError(Flux<T> flux,
        Function<? super T, ? extends Publisher<? extends R>> mapperOnNext,
        Function<? super Throwable, ? extends Publisher<? extends R>> mapperOnError,
        Supplier<? extends Publisher<? extends R>> mapperOnComplete,
        int maxConcurrency, int prefetch) {
        return flux.materialize()
            .flatMapSequentialDelayError(signal -> {
                if ((signal.isOnNext() ? 1 : 0) + (signal.isOnComplete() ? 1 : 0) + (signal.isOnError() ? 1 : 0) != 1) {
                    return Mono.error(new ClientLogger(Utils.class).logExceptionAsError(new IllegalStateException(
                        "Unexpected signal type, signal could only be one of the onNext, onComplete, onError"
                    )));
                }
                if (signal.isOnNext()) {
                    if (mapperOnNext != null) {
                        return mapperOnNext.apply(signal.get());
                    }
                    return Mono.empty();
                }
                if (signal.isOnComplete()) {
                    if (mapperOnComplete != null) {
                        return mapperOnComplete.get();
                    }
                    return Mono.empty();
                }
                Throwable exception = signal.getThrowable();
                if (mapperOnError != null) {
                    return mapperOnError.apply(exception);
                }
                if (exception != null) {
                    return Mono.error(exception);
                } else {
                    throw new ClientLogger(Utils.class).logExceptionAsError(new IllegalStateException());
                }
            }, maxConcurrency, prefetch);
    }
}
