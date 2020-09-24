package com.azure.digitaltwins.core.implementation.converters;

import com.azure.digitaltwins.core.models.EventRoute;
import com.azure.digitaltwins.core.models.EventRoutesListOptions;

/**
 * A converter between {@link com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions} and
 * {@link EventRoutesListOptions}.
 */
public final class EventRouteListOptionsConverter {

    /**
     * Maps from {@link com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions} to
     * {@link EventRoutesListOptions}.
     */
    public static EventRoutesListOptions map(com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions input) {
        EventRoutesListOptions mappedEventRouteListOptions = new EventRoutesListOptions();
        mappedEventRouteListOptions.setMaxItemCount(input.getMaxItemCount());
        return mappedEventRouteListOptions;
    }

    /**
     * Maps from {@link EventRoutesListOptions} to
     * {@link com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions}.
     */
    public static com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions map(EventRoutesListOptions input) {
        com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions mappedEventRoutesListOptions = new com.azure.digitaltwins.core.implementation.models.EventRoutesListOptions();
        mappedEventRoutesListOptions.setMaxItemCount(input.getMaxItemCount());
        return mappedEventRoutesListOptions;
    }

    private EventRouteListOptionsConverter() {}
}
