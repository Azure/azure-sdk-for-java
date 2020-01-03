// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import org.apache.qpid.proton.amqp.Symbol;

/**
 * AMQP error conditions mapped to proton-j symbols.
 */
public final class AmqpErrorCode {
    public static final Symbol NOT_FOUND = Symbol.getSymbol(AmqpErrorCondition.NOT_FOUND.getErrorCondition());
    public static final Symbol UNAUTHORIZED_ACCESS =
        Symbol.getSymbol(AmqpErrorCondition.UNAUTHORIZED_ACCESS.getErrorCondition());
    public static final Symbol RESOURCE_LIMIT_EXCEEDED =
        Symbol.getSymbol(AmqpErrorCondition.RESOURCE_LIMIT_EXCEEDED.getErrorCondition());
    public static final Symbol NOT_ALLOWED = Symbol.getSymbol(AmqpErrorCondition.NOT_ALLOWED.getErrorCondition());
    public static final Symbol INTERNAL_ERROR = Symbol.getSymbol(AmqpErrorCondition.INTERNAL_ERROR.getErrorCondition());
    public static final Symbol ILLEGAL_STATE = Symbol.getSymbol(AmqpErrorCondition.ILLEGAL_STATE.getErrorCondition());
    public static final Symbol NOT_IMPLEMENTED = Symbol.getSymbol(
        AmqpErrorCondition.NOT_IMPLEMENTED.getErrorCondition());

    // link errors
    public static final Symbol LINK_STOLEN = Symbol.getSymbol(AmqpErrorCondition.LINK_STOLEN.getErrorCondition());
    public static final Symbol LINK_PAYLOAD_SIZE_EXCEEDED =
        Symbol.getSymbol(AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED.getErrorCondition());
    public static final Symbol LINK_DETACH_FORCED =
        Symbol.getSymbol(AmqpErrorCondition.LINK_DETACH_FORCED.getErrorCondition());

    // connection errors
    public static final Symbol CONNECTION_FORCED =
        Symbol.getSymbol(AmqpErrorCondition.CONNECTION_FORCED.getErrorCondition());

    // proton library introduced this AMQP symbol in their code-base to communicate IOExceptions
    // while performing operations on SocketChannel (in IOHandler.java)
    public static final Symbol PROTON_IO_ERROR = Symbol.getSymbol("proton:io");

    public static final Symbol SERVER_BUSY_ERROR =
        Symbol.getSymbol(AmqpErrorCondition.SERVER_BUSY_ERROR.getErrorCondition());
    public static final Symbol ARGUMENT_ERROR = Symbol.getSymbol(AmqpErrorCondition.ARGUMENT_ERROR.getErrorCondition());
    public static final Symbol ARGUMENT_OUT_OF_RANGE_ERROR =
        Symbol.getSymbol(AmqpErrorCondition.ARGUMENT_OUT_OF_RANGE_ERROR.getErrorCondition());
    public static final Symbol ENTITY_DISABLED_ERROR =
        Symbol.getSymbol(AmqpErrorCondition.ENTITY_DISABLED_ERROR.getErrorCondition());
    public static final Symbol PARTITION_NOT_OWNED_ERROR =
        Symbol.getSymbol(AmqpErrorCondition.PARTITION_NOT_OWNED_ERROR.getErrorCondition());
    public static final Symbol STORE_LOCK_LOST_ERROR =
        Symbol.getSymbol(AmqpErrorCondition.STORE_LOCK_LOST_ERROR.getErrorCondition());
    public static final Symbol PUBLISHER_REVOKED_ERROR =
        Symbol.getSymbol(AmqpErrorCondition.PUBLISHER_REVOKED_ERROR.getErrorCondition());
    public static final Symbol TIMEOUT_ERROR = Symbol.getSymbol(AmqpErrorCondition.TIMEOUT_ERROR.getErrorCondition());
    public static final Symbol TRACKING_ID_PROPERTY =
        Symbol.getSymbol(AmqpErrorCondition.TRACKING_ID_PROPERTY.getErrorCondition());
}
