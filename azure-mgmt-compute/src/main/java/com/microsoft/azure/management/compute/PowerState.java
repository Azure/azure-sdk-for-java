package com.microsoft.azure.management.compute;

/**
 * Possible power states of a virtual machine.
 */
public enum PowerState {
    /**
     * Enum value PowerState/running.
     */
    RUNNING("PowerState/running"),

    /**
     * Enum value PowerState/deallocating.
     */
    DEALLOCATING("PowerState/deallocating"),

    /**
     * Enum value PowerState/deallocated.
     */
    DEALLOCATED("PowerState/deallocated"),

    /**
     * Enum value PowerState/starting.
     */
    STARTING("PowerState/starting");

    private String value;

    PowerState(String value) {
        this.value = value;
    }

    /**
     * Parses a string value to a PowerState instance.
     *
     * @param value the string value to parse.
     * @return the parsed PowerState object, or null if unable to parse.
     */
    public static PowerState fromValue(String value) {
        PowerState[] items = PowerState.values();
        for (PowerState item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}