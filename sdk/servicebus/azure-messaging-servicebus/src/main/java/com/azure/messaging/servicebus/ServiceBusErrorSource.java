// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * Represent the operation user was performing when the error happened.
 */
public enum ServiceBusErrorSource {
    /** Error while sending the message(s).*/
    SEND,

    /** Error while receiving the message(s).*/
    RECEIVE,

    /** Error while abandoning the message.*/
    ABANDONED,

    /** Error while completing the message.*/
    COMPLETE,

    /** Error while deferring the message.*/
    DEFER,

    /** Error while dead-lettering the message.*/
    DEAD_LETTER;
}
