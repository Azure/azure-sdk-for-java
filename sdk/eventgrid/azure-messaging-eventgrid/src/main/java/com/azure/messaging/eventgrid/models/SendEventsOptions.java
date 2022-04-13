// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.models;

import com.azure.core.annotation.Fluent;

/**
 * Options to configure request to send events.
 */
@Fluent
public final class SendEventsOptions {
    private String channelName;

    /**
     * Returns the channel name to send to Event Grid service. This is only applicable for sending Cloud Events to a
     * partner topic in partner namespace. For more details, refer to
     * <a href=https://docs.microsoft.com/azure/event-grid/partner-events-overview>Partner Events Overview.</a>
     *
     * @return The channel name.
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Sets the channel name to send to Event Grid service. This is only applicable for sending Cloud Events to a
     * partner topic in partner namespace. For more details, refer to
     * <a href=https://docs.microsoft.com/azure/event-grid/partner-events-overview>Partner Events Overview.</a>
     *
     * @param channelName The channel name.
     * @return The updated {@link SendEventsOptions} instance.
     */
    public SendEventsOptions setChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }
}
