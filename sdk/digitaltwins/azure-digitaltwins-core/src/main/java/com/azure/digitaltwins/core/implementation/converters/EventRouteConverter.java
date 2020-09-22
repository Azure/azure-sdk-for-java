package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.models.EventRoute;

/**
 * A converter between {@link com.azure.digitaltwins.core.implementation.models.EventRoute} and
 * {@link com.azure.digitaltwins.core.models.EventRoute}.
 */
public final class EventRouteConverter {

    /**
     * Maps from {@link com.azure.digitaltwins.core.implementation.models.EventRoute} to
     * {@link com.azure.digitaltwins.core.models.EventRoute}.
     */
    public static com.azure.digitaltwins.core.models.EventRoute map(com.azure.digitaltwins.core.implementation.models.EventRoute input) {
        EventRoute mappedEventRoute = new EventRoute(input.getEndpointName());
        mappedEventRoute.setFilter(input.getFilter());
        mappedEventRoute.setId(input.getId());
        return mappedEventRoute;
    }

    /**
     * Maps from {@link com.azure.digitaltwins.core.models.EventRoute} to
     * {@link com.azure.digitaltwins.core.implementation.models.EventRoute}.
     */
    public static com.azure.digitaltwins.core.implementation.models.EventRoute map(com.azure.digitaltwins.core.models.EventRoute input) {
        com.azure.digitaltwins.core.implementation.models.EventRoute mappedEventRoute = new com.azure.digitaltwins.core.implementation.models.EventRoute(input.getEndpointName());
        mappedEventRoute.setFilter(input.getFilter());
        // Note that eventRoute's Id is only set by the service, so there is no need to map it here since the input is a user-created instance.
        return mappedEventRoute;
    }

    private EventRouteConverter() {}
}
