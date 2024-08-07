// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The deletion recovery level currently in effect for the object. If it contains 'Purgeable', then the object can be
 * permanently deleted by a privileged user; otherwise, only the system can purge the object at the end of the retention
 * interval.
 */
public final class DeletionRecoveryLevel extends ExpandableStringEnum<DeletionRecoveryLevel> {
    /**
     * Static value Purgeable for DeletionRecoveryLevel.
     */
    public static final DeletionRecoveryLevel PURGEABLE = fromString("Purgeable");

    /**
     * Static value Recoverable+Purgeable for DeletionRecoveryLevel.
     */
    public static final DeletionRecoveryLevel RECOVERABLE_PURGEABLE = fromString("Recoverable+Purgeable");

    /**
     * Static value Recoverable for DeletionRecoveryLevel.
     */
    public static final DeletionRecoveryLevel RECOVERABLE = fromString("Recoverable");

    /**
     * Static value Recoverable+ProtectedSubscription for DeletionRecoveryLevel.
     */
    public static final DeletionRecoveryLevel RECOVERABLE_PROTECTED_SUBSCRIPTION
        = fromString("Recoverable+ProtectedSubscription");

    /**
     * Creates a new instance of DeletionRecoveryLevel value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public DeletionRecoveryLevel() {
    }

    /**
     * Creates or finds a DeletionRecoveryLevel from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding DeletionRecoveryLevel.
     */
    public static DeletionRecoveryLevel fromString(String name) {
        return fromString(name, DeletionRecoveryLevel.class);
    }

    /**
     * Gets known DeletionRecoveryLevel values.
     * 
     * @return known DeletionRecoveryLevel values.
     */
    public static Collection<DeletionRecoveryLevel> values() {
        return values(DeletionRecoveryLevel.class);
    }
}
