// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

public enum RntbdChannelAcquisitionEventType {
    ATTEMPT_TO_ACQUIRE_CHANNEL,
    ATTEMPT_TO_POLL_CHANNEL,
    ADD_TO_PENDING_QUEUE,
    ATTEMPT_TO_CREATE_NEW_CHANNEL,
    ATTEMPT_TO_CREATE_NEW_CHANNEL_COMPLETE
}
