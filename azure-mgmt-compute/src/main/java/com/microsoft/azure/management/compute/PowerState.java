/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Possible power states of a virtual machine.
 */
public class PowerState {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, PowerState> VALUES_BY_NAME = new HashMap<>();

    /**
     * Static value PowerState/running for PowerState.
     */
    public static final PowerState RUNNING = new PowerState("PowerState/running");

    /**
     * Static value PowerState/deallocating for PowerState.
     */
    public static final PowerState DEALLOCATING = new PowerState("PowerState/deallocating");

    /**
     * Static value PowerState/deallocated for PowerState.
     */
    public static final PowerState DEALLOCATED = new PowerState("PowerState/deallocated");

    /**
     * Static value PowerState/starting for PowerState.
     */
    public static final PowerState STARTING = new PowerState("PowerState/starting");

    /**
     * Static value PowerState/stopped for PowerState.
     */
    public static final PowerState STOPPED = new PowerState("PowerState/stopped");

    /**
     * Static value PowerState/stopping for PowerState.
     */
    public static final PowerState STOPPING = new PowerState("PowerState/stopping");

    /**
     * Static value PowerState/unknown for PowerState.
     */
    public static final PowerState UNKNOWN = new PowerState("PowerState/unknown");

    private final String value;

    /**
     * @return predefined virtual machine power states
     */
    public static PowerState[] values() {
        Collection<PowerState> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new PowerState[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for PowerState.
     * @param value the custom value
     */
    public PowerState(String value) {
        // TODO: This constructor should be private, but keeping as is for now to keep 1.0.0 back compat
        this.value = value;
        VALUES_BY_NAME.put(value.toLowerCase(), this);
    }

    /**
     * Parses a value into a power state and creates a new PowerState instance if not found among the existing ones.
     *
     * @param value a power state name
     * @return the parsed or created power state
     */
    public static PowerState fromString(String value) {
        if (value == null) {
            return null;
        }

        PowerState powerState = VALUES_BY_NAME.get(value.toLowerCase().replace(" ", ""));
        if (powerState != null) {
            return powerState;
        } else {
            return new PowerState(value);
        }
    }

    /**
     * Creates an instance of PowerState from the virtual machine instance view status entry corresponding
     * to the power state.
     *
     * @param virtualMachineInstanceView the virtual machine instance view
     * @return the PowerState
     */
    public static PowerState fromInstanceView(VirtualMachineInstanceView virtualMachineInstanceView) {
        if (virtualMachineInstanceView != null && virtualMachineInstanceView.statuses() != null) {
            for (InstanceViewStatus status : virtualMachineInstanceView.statuses()) {
                if (status.code() != null && status.code().toLowerCase().startsWith("powerstate")) {
                    return PowerState.fromString(status.code());
                }
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PowerState)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            PowerState rhs = (PowerState) obj;
            if (value == null) {
                return rhs.value == null;
            } else {
                return value.equals(rhs.value);
            }
        }
    }
}