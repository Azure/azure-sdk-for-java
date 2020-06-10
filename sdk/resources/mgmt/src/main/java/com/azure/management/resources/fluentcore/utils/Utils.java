// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.utils;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.PathParam;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.management.resources.Subscription;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.model.Indexable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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
     * @param retrofit the retrofit client
     * @return an Observable pointing to the content of the file
     */
    /**
     * Download a file asynchronously.
     *
     * @param url the URL pointing to the file
     * @param retrofit the retrofit client
     * @return an Observable pointing to the content of the file
     */
    public static Mono<byte[]> downloadFileAsync(String url, HttpPipeline retrofit) {
        FileService service = RestProxy.create(FileService.class, retrofit);
        Mono<HttpResponse> response = service.download(url);
        return response.flatMap(httpResponse -> httpResponse.getBodyAsByteArray());
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
    private interface FileService {
        @Get("{url}")
        Mono<HttpResponse> download(@PathParam("url") String url);
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
}
