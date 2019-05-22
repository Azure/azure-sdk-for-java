// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.exception.ErrorCondition;
import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpErrorCode {
    public static final Symbol NOT_FOUND = Symbol.getSymbol(ErrorCondition.NOT_FOUND.getErrorCondition());
    public static final Symbol UNAUTHORIZED_ACCESS = Symbol.getSymbol(ErrorCondition.UNAUTHORIZED_ACCESS.getErrorCondition());
    public static final Symbol RESOURCE_LIMIT_EXCEEDED = Symbol.getSymbol(ErrorCondition.RESOURCE_LIMIT_EXCEEDED.getErrorCondition());
    public static final Symbol NOT_ALLOWED = Symbol.getSymbol(ErrorCondition.NOT_ALLOWED.getErrorCondition());
    public static final Symbol INTERNAL_ERROR = Symbol.getSymbol(ErrorCondition.INTERNAL_ERROR.getErrorCondition());
    public static final Symbol ILLEGAL_STATE = Symbol.getSymbol(ErrorCondition.ILLEGAL_STATE.getErrorCondition());
    public static final Symbol NOT_IMPLEMENTED = Symbol.getSymbol(ErrorCondition.NOT_IMPLEMENTED.getErrorCondition());

    // link errors
    public static final Symbol LINK_STOLEN = Symbol.getSymbol(ErrorCondition.LINK_STOLEN.getErrorCondition());
    public static final Symbol LINK_PAYLOAD_SIZE_EXCEEDED = Symbol.getSymbol(ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED.getErrorCondition());
    public static final Symbol LINK_DETACH_FORCED = Symbol.getSymbol(ErrorCondition.LINK_DETACH_FORCED.getErrorCondition());

    // connection errors
    public static final Symbol CONNECTION_FORCED = Symbol.getSymbol(ErrorCondition.CONNECTION_FORCED.getErrorCondition());

    // proton library introduced this AMQP symbol in their code-base to communicate IOExceptions
    // while performing operations on SocketChannel (in IOHandler.java)
    public static final Symbol PROTON_IO_ERROR = Symbol.getSymbol("proton:io");
}
