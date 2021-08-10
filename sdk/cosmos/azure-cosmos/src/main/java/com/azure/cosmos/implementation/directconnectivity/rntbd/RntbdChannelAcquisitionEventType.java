// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

public enum RntbdChannelAcquisitionEventType {
    ATTEMPT_TO_POLL_CHANNEL("poll"),
    ADD_TO_PENDING_QUEUE("pending"),
    ATTEMPT_TO_CREATE_NEW_CHANNEL("startNew"),
    ATTEMPT_TO_CREATE_NEW_CHANNEL_COMPLETE("completeNew");

    private String name;
    RntbdChannelAcquisitionEventType(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
