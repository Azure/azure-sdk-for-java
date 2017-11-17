/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.containerregistry.implementation.RegistryNameStatusInner;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * The result of checking for container registry name availability.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface CheckNameAvailabilityResult extends HasInner<RegistryNameStatusInner> {
    /**
     * @return true if the specified name is valid and available for use, otherwise false
     */
    boolean isAvailable();
    /**
     * @return the reason why the user-provided name for the container registry could not be used
     */
    String unavailabilityReason();

    /**
     * @return the error message that provides more detail for the reason why the name is not available
     */
    String unavailabilityMessage();
}
