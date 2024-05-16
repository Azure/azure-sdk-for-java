// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

public interface IOObject {

    // should be run on reactor thread
    IOObjectState getState();

    String getId();

    enum IOObjectState {
        OPENING,
        OPENED,
        CLOSED,
        CLOSING
    }
}
