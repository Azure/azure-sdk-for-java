/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Response containing the primary and secondary admin API keys for a given Azure Search service.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface WebhookEventInfo {
    /**
     * Gets the event request object.
     *
     * @return the event request message value
     */
    EventRequestMessage eventRequestMessage();

    /**
     * Gets the event response object.
     *
     * @return the event response message value
     */
    EventResponseMessage eventResponseMessage();

}
