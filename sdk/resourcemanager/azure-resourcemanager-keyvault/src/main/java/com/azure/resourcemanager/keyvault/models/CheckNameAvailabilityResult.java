// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.keyvault.fluent.inner.CheckNameAvailabilityResultInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** The CheckNameAvailability operation response wrapper. */
@Fluent
public interface CheckNameAvailabilityResult extends HasInner<CheckNameAvailabilityResultInner> {

    /**
     * Get the nameAvailable value.
     *
     * @return the nameAvailable value
     */
    Boolean nameAvailable();

    /**
     * Get the reason value.
     *
     * @return the reason value
     */
    Reason reason();

    /**
     * Get the message value.
     *
     * @return the message value
     */
    String message();
}
