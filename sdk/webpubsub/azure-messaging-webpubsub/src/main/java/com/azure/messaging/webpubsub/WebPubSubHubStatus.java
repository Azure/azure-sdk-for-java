// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.http.rest.Response;

/**
 * A support class used to indicate status of an Azure Web Pub Sub hub, such as whether it is available. This can be
 * retrieved through the {@code getStatus()} methods on {@link WebPubSubClient#getStatus() WebPubSubClient} and
 * {@link WebPubSubAsyncClient#getStatus() WebPubSubAsyncClient}.
 */
public final class WebPubSubHubStatus {
    private final boolean isAvailable;

    // package-private
    WebPubSubHubStatus(Response<?> response) {
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
