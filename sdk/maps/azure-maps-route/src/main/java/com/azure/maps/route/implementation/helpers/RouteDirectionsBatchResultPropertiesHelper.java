package com.azure.maps.route.implementation.helpers;

import com.azure.maps.route.implementation.models.RouteDirectionsBatchResultPrivate;
import com.azure.maps.route.models.RouteDirectionsBatchResult;

/**
 * The helper class to set the non-public properties of an {@link RouteDirectionsBatchResult} instance.
 */
public final class RouteDirectionsBatchResultPropertiesHelper {
    private static RouteDirectionsBatchResultAccessor accessor;

    private RouteDirectionsBatchResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RouteDirectionsBatchResult} instance.
     */
    public interface RouteDirectionsBatchResultAccessor {
        void setFromRouteDirectionsBatchResultPrivate(RouteDirectionsBatchResult result, RouteDirectionsBatchResultPrivate privateResult);
    }

    /**
     * The method called from {@link RouteDirectionsBatchResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RouteDirectionsBatchResultAccessor routeMatrixAccessor) {
        accessor = routeMatrixAccessor;
    }

    /**
     * Sets properties of an {@link RouteDirectionsBatchResult} using a private model.
     *
     * @param result
     * @param privateResult
     */
    public static void setFromRouteDirectionsBatchResultPrivate(RouteDirectionsBatchResult result,
            RouteDirectionsBatchResultPrivate privateResult) {
        accessor.setFromRouteDirectionsBatchResultPrivate(result, privateResult);
    }
}
