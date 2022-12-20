// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

public class GroupMessageEvent {

    private final GroupDataMessage message;

    public GroupMessageEvent(GroupDataMessage message) {
        this.message = message;
    }

    public GroupDataMessage getMessage() {
        return message;
    }
}
