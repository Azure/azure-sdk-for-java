package com.azure.maps.route.implementation.helpers;

import com.azure.maps.route.implementation.models.RouteMatrixResultPrivate;
import com.azure.maps.route.models.RouteMatrixResult;

/**
 * The helper class to set the non-public properties of an {@link RouteMatrixResult} instance.
 */
public final class RouteMatrixResultPropertiesHelper {
    private static RouteMatrixResultAccessor accessor;

    private RouteMatrixResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RouteMatrixResult} instance.
     */
    public interface RouteMatrixResultAccessor {
        void setFromRouteMatrixResultPrivate(RouteMatrixResult result, RouteMatrixResultPrivate privateResult);
    }

    /**
     * The method called from {@link RouteMatrixResult} to set it's accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(final RouteMatrixResultAccessor routeMatrixAccessor) {
        accessor = routeMatrixAccessor;
    }

    /**
     * Sets properties of an {@link RouteMatrixResult} using a private model.
     *
     * @param result
     * @param privateResult
     */
    public static void setFromRouteMatrixResultPrivate(RouteMatrixResult result,
            RouteMatrixResultPrivate privateResult) {
        accessor.setFromRouteMatrixResultPrivate(result, privateResult);
    }
}
