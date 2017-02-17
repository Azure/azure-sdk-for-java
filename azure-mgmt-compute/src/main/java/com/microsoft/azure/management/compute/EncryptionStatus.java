/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

/**
 * Defines values for EncryptionStatuses.
 */
public class EncryptionStatus {
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
     * Creates a custom value for EncryptionStatuses.
     * @param value the custom value
     */
    public EncryptionStatus(String value) {
        this.value = value;
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
        }
        if (obj == this) {
            return true;
        }
        EncryptionStatus rhs = (EncryptionStatus) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equalsIgnoreCase(rhs.value);
        }
    }
}
