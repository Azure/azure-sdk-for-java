/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.utils;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.PathParam;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.AzureEnvironment;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Defines a few utilities.
 */
public final class Utils {
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
     * @param tagName  the name of the tag. If not provided, all resources will be returned.
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
     * @param <U>    the specialized type of last item in the input stream
     * @return a Mono that emits last item
     */
    @SuppressWarnings("unchecked")
    public static <U extends Indexable> Mono<U> rootResource(Mono<Indexable> stream) {
        return stream.map(indexable -> (U) indexable);
    }

    /**
     * Download a file asynchronously.
     *
     * @param url      the URL pointing to the file
     * @param retrofit the retrofit client
     * @return an Observable pointing to the content of the file
     */
    /**
     * Download a file asynchronously.
     *
     * @param url      the URL pointing to the file
     * @param retrofit the retrofit client
     * @return an Observable pointing to the content of the file
     */
    public static Mono<byte[]> downloadFileAsync(String url, HttpPipeline retrofit) {
        FileService service = RestProxy.create(FileService.class, retrofit);
        Mono<HttpResponse> response = service.download(url);
        return response.flatMap(httpResponse -> httpResponse.getBodyAsByteArray());
    }

//    /**
//     * Converts the given list of a type to paged list of a different type.
//     *
//     * @param list   the list to convert to paged list
//     * @param mapper the mapper to map type in input list to output list
//     * @param <OutT> the type of items in output paged list
//     * @param <InT>  the type of items in input paged list
//     * @return the paged list
//     */
//    public static <OutT, InT> PagedIterable<OutT> toPagedList(List<InT> list, final Function<InT, OutT> mapper) {
//        PageImpl<InT> page = new PageImpl<>();
//        page.setItems(list);
//        page.setNextPageLink(null);
//        PagedIterable<InT> pagedList = new PagedIterable<InT>(page) {
//            @Override
//            public Page<InT> nextPage(String nextPageLink) {
//                return null;
//            }
//        };
//        PagedListConverter<InT, OutT> converter = new PagedListConverter<InT, OutT>() {
//            @Override
//            public Mono<OutT> typeConvertAsync(InT inner) {
//                return Mono.just(mapper.apply(inner));
//            }
//        };
//        return converter.convert(pagedList);
//    }

    /**
     * Adds a value to the list if does not already exists.
     *
     * @param list  the list
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
     * @param list  the list
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
     * Try to extract the environment the client is authenticated to based
     * on the information on the rest client.
     *
     * @param restClient the RestClient instance
     * @return the non-null AzureEnvironment
     */
    public static AzureEnvironment extractAzureEnvironment(RestClient restClient) {
        AzureEnvironment environment = null;
        if (restClient.getCredential() instanceof AzureTokenCredential) {
            environment = ((AzureTokenCredential) restClient.getCredential()).getEnvironment();
        } else {
            String baseUrl = restClient.getBaseUrl().toString();
            for (AzureEnvironment env : AzureEnvironment.knownEnvironments()) {
                if (env.getResourceManagerEndpoint().toLowerCase().contains(baseUrl.toLowerCase())) {
                    environment = env;
                    break;
                }
            }
            if (environment == null) {
                throw new IllegalArgumentException("Unknown resource manager endpoint " + baseUrl);
            }
        }
        return environment;
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

//    /**
//     * Get the response body as string.
//     *
//     * @param responseBody response body object
//     * @return response body in string
//     * @throws IOException throw IOException
//     */
//    public static String getResponseBodyInString(ResponseBody responseBody) throws IOException {
//        if (responseBody == null) {
//            return null;
//        }
//        BufferedSource source = responseBody.source();
//        source.request(Long.MAX_VALUE); // Buffer the entire body.
//        Buffer buffer = source.buffer();
//        return buffer.clone().readUtf8();
//    }

    /**
     * A Retrofit service used to download a file.
     */
    private interface FileService {
        @Get("{url}")
        Mono<HttpResponse> download(@PathParam("url") String url);
    }

    private Utils() {
    }
}
