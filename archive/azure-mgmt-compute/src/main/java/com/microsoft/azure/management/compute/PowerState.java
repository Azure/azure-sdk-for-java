/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import java.util.Collection;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Possible power states of a virtual machine.
 */
public final class PowerState extends ExpandableStringEnum<PowerState> {
    /**
     * Static value PowerState/running for PowerState.
     */
    public static final PowerState RUNNING = fromString("PowerState/running");

    /**
     * Static value PowerState/deallocating for PowerState.
     */
    public static final PowerState DEALLOCATING = fromString("PowerState/deallocating");

    /**
     * Static value PowerState/deallocated for PowerState.
     */
    public static final PowerState DEALLOCATED = fromString("PowerState/deallocated");

    /**
     * Static value PowerState/starting for PowerState.
     */
    public static final PowerState STARTING = fromString("PowerState/starting");

    /**
     * Static value PowerState/stopped for PowerState.
     */
    public static final PowerState STOPPED = fromString("PowerState/stopped");

    /**
     * Static value PowerState/stopping for PowerState.
     */
    public static final PowerState STOPPING = fromString("PowerState/stopping");

    /**
     * Static value PowerState/unknown for PowerState.
     */
    public static final PowerState UNKNOWN = fromString("PowerState/unknown");

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
                    return fromString(status.code());
                }
            }
        }
        return null;
    }

    /**
     * @return all known power states
     */
    public static Collection<PowerState> values() {
        return values(PowerState.class);
    }

    /**
     * Finds or creates a PowerState value.
     * @param name the value of the power state
     * @return a PowerState instance
     */
    public static PowerState fromString(String name) {
        return fromString(name, PowerState.class);
    }
}