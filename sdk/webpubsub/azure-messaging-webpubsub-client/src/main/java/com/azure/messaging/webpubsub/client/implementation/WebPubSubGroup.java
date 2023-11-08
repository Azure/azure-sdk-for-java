// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public final class WebPubSubGroup {

    private final String name;

    private boolean joined = false;

    public WebPubSubGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isJoined() {
        return joined;
    }

    public WebPubSubGroup setJoined(boolean joined) {
        this.joined = joined;
        return this;
    }
}
