package com.azure.communication.jobrouter.implementation.converters;

import com.azure.communication.jobrouter.models.RouterValue;

public class RouterValueAdapter {
    public static Object getValue(RouterValue routerValue) {
        if (routerValue.getValueAsBoolean()) {
            return routerValue.getValueAsBoolean();
        } else if (routerValue.getValueAsDouble() != null) {
            return routerValue.getValueAsDouble();
        } else if (routerValue.getValueAsInteger() != null) {
            return routerValue.getValueAsInteger();
        } else if (routerValue.getValueAsString() != null) {
            return routerValue.getValueAsString();
        }
        return null;
    }
}
