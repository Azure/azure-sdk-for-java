// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.http.rest.Response;

/**
 * A support class used to indicate status of a SignalR hub, such as whether it is available. This can be retrieved
 * through the {@code getStatus()} methods on {@link SignalRClient#getStatus() SignalRClient} and
 * {@link SignalRAsyncClient#getStatus() SignalRAsyncClient}.
 */
public class SignalRHubStatus {
    private final boolean isAvailable;

    // package-private
    SignalRHubStatus(Response<?> response) {
        this.isAvailable = response.getStatusCode() == 200;
    }

    /**
     * Returns a boolean to indicate whether the service is available and ready for use.
     * @return Whether the service is available and ready for use.
     */
    public boolean isAvailable() {
        return isAvailable;
    }
}
