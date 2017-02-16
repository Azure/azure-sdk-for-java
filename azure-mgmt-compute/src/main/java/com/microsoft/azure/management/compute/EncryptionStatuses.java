/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

/**
 * Defines values for EncryptionStatuses.
 */
public class EncryptionStatuses {
    /** Static value Encrypted for EncryptionInProgress. */
    public static final EncryptionStatuses ENCRYPTION_INPROGRESS = new EncryptionStatuses("EncryptionInProgress");

    /** Static value Encrypted for EncryptionStatuses. */
    public static final EncryptionStatuses ENCRYPTED = new EncryptionStatuses("Encrypted");

    /** Static value NotEncrypted for EncryptionStatuses. */
    public static final EncryptionStatuses NOT_ENCRYPTED = new EncryptionStatuses("NotEncrypted");

    /** Static value VMRestartPending for EncryptionStatuses. */
    public static final EncryptionStatuses VM_RESTART_PENDING = new EncryptionStatuses("VMRestartPending");

    /** Static value NotMounted for EncryptionStatuses. */
    public static final EncryptionStatuses NOT_MOUNTED = new EncryptionStatuses("NotMounted");

    /** Static value Unknown for EncryptionStatuses. */
    public static final EncryptionStatuses UNKNOWN = new EncryptionStatuses("Unknown");

    private String value;

    /**
     * Creates a custom value for EncryptionStatuses.
     * @param value the custom value
     */
    public EncryptionStatuses(String value) {
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
        if (!(obj instanceof VirtualMachineSizeTypes)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        EncryptionStatuses rhs = (EncryptionStatuses) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equalsIgnoreCase(rhs.value);
        }
    }
}
