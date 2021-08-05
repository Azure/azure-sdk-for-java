// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

public enum RntbdChannelState {
    OK, CLOSED, NULL_REQUEST_MANAGER, PENDING_LIMIT, CONTEXT_NEGOTIATION_PENDING
}
