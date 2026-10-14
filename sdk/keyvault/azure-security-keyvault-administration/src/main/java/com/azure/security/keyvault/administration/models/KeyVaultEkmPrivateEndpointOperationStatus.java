// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * The status of a long-running External Key Manager (EKM) proxy private endpoint operation.
 */
public final class KeyVaultEkmPrivateEndpointOperationStatus
    extends ExpandableStringEnum<KeyVaultEkmPrivateEndpointOperationStatus> {
    /**
     * The operation has not started.
     */
    public static final KeyVaultEkmPrivateEndpointOperationStatus NOT_STARTED = fromString("NotStarted");

    /**
     * The operation is in progress.
     */
    public static final KeyVaultEkmPrivateEndpointOperationStatus RUNNING = fromString("Running");

    /**
     * The operation has completed successfully.
     */
    public static final KeyVaultEkmPrivateEndpointOperationStatus SUCCEEDED = fromString("Succeeded");

    /**
     * The operation has failed.
     */
    public static final KeyVaultEkmPrivateEndpointOperationStatus FAILED = fromString("Failed");

    /**
     * The operation has been canceled.
     */
    public static final KeyVaultEkmPrivateEndpointOperationStatus CANCELED = fromString("Canceled");

    /**
     * Creates a new instance of {@link KeyVaultEkmPrivateEndpointOperationStatus} value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public KeyVaultEkmPrivateEndpointOperationStatus() {
    }

    /**
     * Creates or finds a {@link KeyVaultEkmPrivateEndpointOperationStatus} from its string representation.
     *
     * @param name The name to look for.
     * @return The corresponding {@link KeyVaultEkmPrivateEndpointOperationStatus}.
     */
    public static KeyVaultEkmPrivateEndpointOperationStatus fromString(String name) {
        return fromString(name, KeyVaultEkmPrivateEndpointOperationStatus.class);
    }

    /**
     * Gets known {@link KeyVaultEkmPrivateEndpointOperationStatus} values.
     *
     * @return Known {@link KeyVaultEkmPrivateEndpointOperationStatus} values.
     */
    public static Collection<KeyVaultEkmPrivateEndpointOperationStatus> values() {
        return values(KeyVaultEkmPrivateEndpointOperationStatus.class);
    }
}
