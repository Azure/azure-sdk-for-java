// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

/**
 * The result of check name availability.
 */
public interface CheckNameAvailabilityResult {

    /**
     * Gets whether name is available to use.
     *
     * @return whether name is available to use.
     */
    boolean nameAvailable();

    /**
     * Gets the reason that the name could not be used.
     *
     * @return the reason that the name could not be used.
     */
    CheckNameAvailabilityReason reason();

    /**
     * Gets the message explaining the reason of name unavailability in more detail.
     *
     * @return an error message explaining the reason of name unavailability in more detail.
     */
    String message();
}
