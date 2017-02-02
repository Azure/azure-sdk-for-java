/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

/**
 * Defines values for AvailabilitySetSkuTypes.
 */
public final class AvailabilitySetSkuTypes {
    /** Static value Aligned for AvailabilitySetSkuTypes. */
    public static final AvailabilitySetSkuTypes MANAGED = new AvailabilitySetSkuTypes("Aligned");

    /** Static value Classic for AvailabilitySetSkuTypes. */
    public static final AvailabilitySetSkuTypes UNMANAGED = new AvailabilitySetSkuTypes("Classic");


    /** The actual serialized value for a AvailabilitySetSkuTypes instance. */
    private String value;

    /**
     * Creates a custom value for AvailabilitySetSkuTypes.
     * @param value the custom value
     */
    public AvailabilitySetSkuTypes(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AvailabilitySetSkuTypes)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        AvailabilitySetSkuTypes rhs = (AvailabilitySetSkuTypes) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equalsIgnoreCase(rhs.value);
        }
    }
}
