/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Possible power states of a virtual machine.
 */
public class PowerState {
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
     * Static value PowerState/unknown for PowerState.
     */
    public static final PowerState UNKNOWN = new PowerState("PowerState/unknown");

    private String value;

    /**
     * Creates a custom value for PowerState.
     * @param value the custom value
     */
    public PowerState(String value) {
        this.value = value;
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
                if (status.code() != null && status.code().startsWith("PowerState")) {
                    return new PowerState(status.code());
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
        }
        if (obj == this) {
            return true;
        }
        PowerState rhs = (PowerState) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}