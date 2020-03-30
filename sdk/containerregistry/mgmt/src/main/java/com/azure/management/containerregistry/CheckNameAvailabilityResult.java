/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry;

import com.azure.core.annotation.Fluent;
import com.azure.management.containerregistry.models.RegistryNameStatusInner;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * The result of checking for container registry name availability.
 */
@Fluent
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
