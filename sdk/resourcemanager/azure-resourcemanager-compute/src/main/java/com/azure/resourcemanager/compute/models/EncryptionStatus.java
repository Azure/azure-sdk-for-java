// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for EncryptionStatuses. */
public final class EncryptionStatus extends ExpandableStringEnum<EncryptionStatus> {
    /** Static value Encrypted for EncryptionInProgress. */
    public static final EncryptionStatus ENCRYPTION_INPROGRESS = fromString("EncryptionInProgress");

    /** Static value Encrypted for EncryptionStatuses. */
    public static final EncryptionStatus ENCRYPTED = fromString("Encrypted");

    /** Static value NotEncrypted for EncryptionStatuses. */
    public static final EncryptionStatus NOT_ENCRYPTED = fromString("NotEncrypted");

    /** Static value VMRestartPending for EncryptionStatuses. */
    public static final EncryptionStatus VM_RESTART_PENDING = fromString("VMRestartPending");

    /** Static value NotMounted for EncryptionStatuses. */
    public static final EncryptionStatus NOT_MOUNTED = fromString("NotMounted");

    /** Static value Unknown for EncryptionStatuses. */
    public static final EncryptionStatus UNKNOWN = fromString("Unknown");

    /**
     * Creates of finds an encryption status based on its name.
     *
     * @param name a name to look for
     * @return an EncryptionStatus
     */
    public static EncryptionStatus fromString(String name) {
        return fromString(name, EncryptionStatus.class);
    }

    /** @return known encryption statuses */
    public static Collection<EncryptionStatus> values() {
        return values(EncryptionStatus.class);
    }
}
