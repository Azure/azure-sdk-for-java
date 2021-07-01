// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.provider.DelayProvider;
import com.azure.core.management.provider.IdentifierProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.models.Subscription;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Defines a few utilities.
 */
public final class ResourceManagerUtils {
    private ResourceManagerUtils() {
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
     * Wrapper for thread sleep.
     *
     * @param duration the duration value for which thread should put on sleep.
     */
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(InternalRuntimeContext.getDelayDuration(duration).toMillis());
        } catch (InterruptedException e) {
        }
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
     * Gets the only subscription as the default one in the tenant if applicable.
     *
     * @param subscriptions the list of subscriptions
     * @throws IllegalStateException when no subscription or more than one subscription found
     * @return the only subscription existing in the tenant
     */
    public static String getDefaultSubscription(PagedIterable<Subscription> subscriptions) {
        List<Subscription> subscriptionList = new ArrayList<>();
        subscriptions.forEach(subscription -> {
            subscriptionList.add(subscription);
        });
        if (subscriptionList.size() == 0) {
            throw new ClientLogger(ResourceManagerUtils.class).logExceptionAsError(
                new IllegalStateException("Please create a subscription before you start resource management. "
                + "To learn more, see: https://azure.microsoft.com/en-us/free/."));
        } else if (subscriptionList.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("More than one subscription found in your tenant. "
                + "Please specify which one below is desired for resource management.");
            subscriptionList.forEach(subscription -> {
                stringBuilder.append("\n" + subscription.displayName() + " : " + subscription.subscriptionId());
            });
            throw new ClientLogger(ResourceManagerUtils.class).logExceptionAsError(
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
    static String getDefaultScopeFromUrl(String url, AzureEnvironment environment) {
        String resource = environment.getManagementEndpoint();
        for (Map.Entry<String, String> endpoint : environment.getEndpoints().entrySet()) {
            if (url.contains(endpoint.getValue())) {
                if (endpoint.getKey().equals(AzureEnvironment.Endpoint.KEYVAULT.identifier())) {
                    resource = String.format("https://%s/", endpoint.getValue().replaceAll("^\\.*", ""));
                    resource = removeTrailingSlash(resource);
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.GRAPH.identifier())) {
                    resource = environment.getGraphEndpoint();
                    resource = removeTrailingSlash(resource);
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.MICROSOFT_GRAPH.identifier())) {
                    resource = environment.getMicrosoftGraphEndpoint();
                    resource = removeTrailingSlash(resource);
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.LOG_ANALYTICS.identifier())) {
                    resource = environment.getLogAnalyticsEndpoint();
                    resource = removeTrailingSlash(resource);
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.APPLICATION_INSIGHTS.identifier())) {
                    resource = environment.getApplicationInsightsEndpoint();
                    resource = removeTrailingSlash(resource);
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_STORE.identifier())
                    || endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_ANALYTICS.identifier())) {
                    resource = environment.getDataLakeEndpointResourceId();
                    resource = removeTrailingSlash(resource);
                    break;
                }
            }
        }
        return resource + "/.default";
    }

    /**
     * Removes the trailing slash of the string.
     * @param s the string
     * @return the string without trailing slash
     */
    private static String removeTrailingSlash(String s) {
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
     * The class provides the common methods required for SDK framework.
     *
     * RESERVED FOR INTERNAL USE.
     */
    public static class InternalRuntimeContext {
        private Function<String, IdentifierProvider> identifierFunction = ResourceNamer::new;
        private static DelayProvider delayProvider = new ResourceDelayProvider();
        private static Scheduler reactorScheduler = Schedulers.parallel();

        /**
         * Sets the resource namer
         *
         * @param identifierFunction the function.
         */
        public void setIdentifierFunction(Function<String, IdentifierProvider> identifierFunction) {
            this.identifierFunction = identifierFunction;
        }

        /**
         * Creates a resource namer
         *
         * @param name the name value.
         * @return the new resource namer
         */
        public IdentifierProvider createIdentifierProvider(String name) {
            return identifierFunction.apply(name);
        }

        /**
         * Gets a random name.
         *
         * @param prefix the prefix to be used if possible
         * @param maxLen the maximum length for the random generated name
         * @return the random name
         */
        public String randomResourceName(String prefix, int maxLen) {
            return identifierFunction.apply("").getRandomName(prefix, maxLen);
        }

        /**
         * Gets a random UUID.
         *
         * @return the random UUID.
         */
        public String randomUuid() {
            return identifierFunction.apply("").getRandomUuid();
        }

        /**
         * Function to override the DelayProvider.
         *
         * @param delayProvider delayProvider to override.
         */
        public static void setDelayProvider(DelayProvider delayProvider) {
            InternalRuntimeContext.delayProvider = delayProvider;
        }

        /**
         * Wrapper for the duration for delay, based on delayProvider.
         *
         * @param delay the duration of proposed delay.
         * @return the duration of delay.
         */
        public static Duration getDelayDuration(Duration delay) {
            return delayProvider.getDelayDuration(delay);
        }

        /**
         * Gets the current Rx Scheduler for the SDK framework.
         *
         * @return current rx scheduler.
         */
        public static Scheduler getReactorScheduler() {
            return reactorScheduler;
        }

        /**
         * Sets the Rx Scheduler for SDK framework, by default is Scheduler.io().
         *
         * @param reactorScheduler current Rx Scheduler to be used in SDK framework.
         */
        public static void setReactorScheduler(Scheduler reactorScheduler) {
            InternalRuntimeContext.reactorScheduler = reactorScheduler;
        }
    }
}
