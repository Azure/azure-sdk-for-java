/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql;


import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.sql.models.CheckNameAvailabilityResponseInner;

/**
 * The result of checking for the SQL server name availability.
 */
@Fluent
public interface CheckNameAvailabilityResult extends HasInner<CheckNameAvailabilityResponseInner> {
    /**
     * @return true if the specified name is valid and available for use, otherwise false
     */
    boolean isAvailable();
    /**
     * @return the reason why the user-provided name for the SQL server could not be used
     */
    String unavailabilityReason();

    /**
     * @return the error message that provides more detail for the reason why the name is not available
     */
    String unavailabilityMessage();
}
