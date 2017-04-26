/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines values for EncryptionStatuses.
 */
public final class EncryptionStatus {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, EncryptionStatus> VALUES_BY_NAME = new HashMap<>();

    /** Static value Encrypted for EncryptionInProgress. */
    public static final EncryptionStatus ENCRYPTION_INPROGRESS = new EncryptionStatus("EncryptionInProgress");

    /** Static value Encrypted for EncryptionStatuses. */
    public static final EncryptionStatus ENCRYPTED = new EncryptionStatus("Encrypted");

    /** Static value NotEncrypted for EncryptionStatuses. */
    public static final EncryptionStatus NOT_ENCRYPTED = new EncryptionStatus("NotEncrypted");

    /** Static value VMRestartPending for EncryptionStatuses. */
    public static final EncryptionStatus VM_RESTART_PENDING = new EncryptionStatus("VMRestartPending");

    /** Static value NotMounted for EncryptionStatuses. */
    public static final EncryptionStatus NOT_MOUNTED = new EncryptionStatus("NotMounted");

    /** Static value Unknown for EncryptionStatuses. */
    public static final EncryptionStatus UNKNOWN = new EncryptionStatus("Unknown");

    private String value;

    /**
     * @return predefined encryption statuses
     */
    public static EncryptionStatus[] values() {
        Collection<EncryptionStatus> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new EncryptionStatus[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for EncryptionStatuses.
     * @param value the custom value
     */
    public EncryptionStatus(String value) {
        // TODO: This constructor should be private, but keeping as is for now to keep 1.0.0 back compat
        this.value = value;
        VALUES_BY_NAME.put(value.toLowerCase(), this);
    }

    /**
     * Parses a value into an encryption status and creates a new EncryptionStatus instance if not found among the existing ones.
     *
     * @param value a compute usage unit name
     * @return the parsed or created compute usage unit
     */
    public static EncryptionStatus fromString(String value) {
        if (value == null) {
            return null;
        }

        EncryptionStatus result = VALUES_BY_NAME.get(value.toLowerCase());
        if (result != null) {
            return result;
        } else {
            return new EncryptionStatus(value);
        }
    }

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
        if (!(obj instanceof EncryptionStatus)) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (value == null) {
            return ((EncryptionStatus) obj).value == null;
        } else {
            return value.equalsIgnoreCase(((EncryptionStatus) obj).value);
        }
    }
}
