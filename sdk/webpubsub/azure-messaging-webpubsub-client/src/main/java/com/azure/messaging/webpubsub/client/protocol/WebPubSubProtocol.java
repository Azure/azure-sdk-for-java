// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.protocol;

public abstract class WebPubSubProtocol {

    public abstract boolean isReliable();

    public abstract String getName();
}
