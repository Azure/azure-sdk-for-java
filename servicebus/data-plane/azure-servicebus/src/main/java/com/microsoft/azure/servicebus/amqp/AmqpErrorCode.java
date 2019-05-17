// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpErrorCode {

    public static final Symbol NotFound = Symbol.getSymbol("amqp:not-found");
    public static final Symbol UnauthorizedAccess = Symbol.getSymbol("amqp:unauthorized-access");
    public static final Symbol ResourceLimitExceeded = Symbol.getSymbol("amqp:resource-limit-exceeded");
    public static final Symbol NotAllowed = Symbol.getSymbol("amqp:not-allowed");
    public static final Symbol InternalError = Symbol.getSymbol("amqp:internal-error");
    public static final Symbol IllegalState = Symbol.getSymbol("amqp:illegal-state");
    public static final Symbol NotImplemented = Symbol.getSymbol("amqp:not-implemented");
    public static final Symbol DecodeError = Symbol.getSymbol("amqp:decode-error");

    // link errors
    public static final Symbol Stolen = Symbol.getSymbol("amqp:link:stolen");
    public static final Symbol PayloadSizeExceeded = Symbol.getSymbol("amqp:link:message-size-exceeded");
    public static final Symbol AmqpLinkDetachForced = Symbol.getSymbol("amqp:link:detach-forced");

    // connection errors
    public static final Symbol ConnectionForced = Symbol.getSymbol("amqp:connection:forced");
    public static final Symbol FramingError = Symbol.getSymbol("amqp:connection:framing-error");
    // proton library IOExceptions while performing operations on SocketChannel (in IOHandler.java)
    public static final Symbol ProtonIOError = Symbol.getSymbol("proton:io");
}
