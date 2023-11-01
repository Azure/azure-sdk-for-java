package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.core.util.logging.ClientLogger;

public class RouterValueAdapter {
    private static final ClientLogger LOGGER = new ClientLogger(RouterValueAdapter.class);

    public static Object getValue(RouterValue routerValue) {
        try {
            return routerValue.getValueAsInteger();
        } catch (IllegalStateException ex) {
            LOGGER.info("value is not an Integer.");
        }
        try {
            return routerValue.getValueAsDouble();
        } catch (IllegalStateException ex) {
            LOGGER.info("value is not a Double.");
        }

        try {
            return routerValue.getValueAsBoolean();
        } catch (IllegalStateException ex) {
            LOGGER.info("value is not a Boolean.");
        }

        try {
            return routerValue.getValueAsString();
        } catch (IllegalStateException ex) {
            LOGGER.info("value is not a String.");
        }

        throw new IllegalStateException("Object is not of types supported in RouterValue");
    }
}
