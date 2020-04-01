/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry;

import com.azure.core.annotation.Fluent;

/**
 * Response containing the primary and secondary admin API keys for a given Azure Search service.
 */
@Fluent
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
