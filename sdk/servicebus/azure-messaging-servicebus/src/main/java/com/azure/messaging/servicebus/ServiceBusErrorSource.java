// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * Represent the scenario in which user was when the error happened.
 */
public enum ServiceBusErrorSource {
    SEND,
    RECEIVE,
    APPEND,
    COMPLETE,
    DEFER,
    DEAD_LETTER,
    PEEK;
}
