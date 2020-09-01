/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.servicebus.implementation.CheckNameAvailabilityResultInner;

/**
 * The result of checking for Service Bus namespace name availability.
 */
@Fluent
public interface CheckNameAvailabilityResult extends HasInner<CheckNameAvailabilityResultInner> {
    /**
     * @return a boolean value that indicates whether the name is available for
     * you to use. If true, the name is available. If false, the name has
     * already been taken or invalid and cannot be used.
     */
    boolean isAvailable();
    /**
     * @return the unavailabilityReason that a namespace name could not be used. The
     * Reason element is only returned if NameAvailable is false.
     */
    UnavailableReason unavailabilityReason();
    /**
     * @return an error message explaining the Reason value in more detail
     */
    String unavailabilityMessage();
}
