// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link RecoveryKind#classify(Throwable)}.
 */
class RecoveryKindTest {

    @Test
    void nullErrorReturnsNone() {
        assertEquals(RecoveryKind.NONE, RecoveryKind.classify(null));
    }

    @Test
    void timeoutExceptionReturnsNone() {
        assertEquals(RecoveryKind.NONE, RecoveryKind.classify(new TimeoutException("timed out")));
    }

    @Test
    void serverBusyReturnsNone() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "server busy", null);
        assertEquals(RecoveryKind.NONE, RecoveryKind.classify(error));
    }

    @Test
    void timeoutErrorConditionReturnsNone() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR, "timeout", null);
        assertEquals(RecoveryKind.NONE, RecoveryKind.classify(error));
    }

    @Test
    void linkDetachForcedReturnsLink() {
        final AmqpException error
            = new AmqpException(true, AmqpErrorCondition.LINK_DETACH_FORCED, "detach forced", null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void linkStolenReturnsLink() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.LINK_STOLEN, "link stolen", null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void transientAmqpErrorWithoutConditionReturnsLink() {
        final AmqpException error = new AmqpException(true, "transient error", null, null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void connectionForcedReturnsConnection() {
        final AmqpException error
            = new AmqpException(true, AmqpErrorCondition.CONNECTION_FORCED, "connection forced", null);
        assertEquals(RecoveryKind.CONNECTION, RecoveryKind.classify(error));
    }

    @Test
    void connectionFramingErrorReturnsConnection() {
        final AmqpException error
            = new AmqpException(true, AmqpErrorCondition.CONNECTION_FRAMING_ERROR, "framing error", null);
        assertEquals(RecoveryKind.CONNECTION, RecoveryKind.classify(error));
    }

    @Test
    void internalErrorReturnsConnection() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.INTERNAL_ERROR, "internal error", null);
        assertEquals(RecoveryKind.CONNECTION, RecoveryKind.classify(error));
    }

    @Test
    void protonIoReturnsConnection() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.PROTON_IO, "io error", null);
        assertEquals(RecoveryKind.CONNECTION, RecoveryKind.classify(error));
    }

    @Test
    void connectionRedirectReturnsConnection() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.CONNECTION_REDIRECT, "redirect", null);
        assertEquals(RecoveryKind.CONNECTION, RecoveryKind.classify(error));
    }

    @Test
    void linkRedirectReturnsLink() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.LINK_REDIRECT, "redirect", null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void transferLimitExceededReturnsLink() {
        final AmqpException error
            = new AmqpException(true, AmqpErrorCondition.TRANSFER_LIMIT_EXCEEDED, "transfer limit", null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void argumentErrorReturnsFatal() {
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.ARGUMENT_ERROR, "bad argument", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void notFoundReturnsFatal() {
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.NOT_FOUND, "not found", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void unauthorizedAccessReturnsFatal() {
        final AmqpException error
            = new AmqpException(false, AmqpErrorCondition.UNAUTHORIZED_ACCESS, "unauthorized", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void payloadSizeExceededReturnsFatal() {
        final AmqpException error
            = new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, "too large", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void notAllowedReturnsFatal() {
        final AmqpException error = new AmqpException(false, AmqpErrorCondition.NOT_ALLOWED, "not allowed", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void nonTransientAmqpErrorReturnsFatal() {
        final AmqpException error = new AmqpException(false, "permanent error", null, null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void requestResponseChannelClosedReturnsLink() {
        final RequestResponseChannelClosedException error = new RequestResponseChannelClosedException("channel closed");
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void unknownExceptionReturnsFatal() {
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(new RuntimeException("unknown")));
    }

    @Test
    void sessionLockLostReturnsLink() {
        final AmqpException error
            = new AmqpException(true, AmqpErrorCondition.SESSION_LOCK_LOST, "session lock lost", null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void messageLockLostReturnsFatal() {
        final AmqpException error
            = new AmqpException(false, AmqpErrorCondition.MESSAGE_LOCK_LOST, "message lock lost", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void storeLockLostReturnsFatal() {
        final AmqpException error
            = new AmqpException(false, AmqpErrorCondition.STORE_LOCK_LOST_ERROR, "store lock lost", null);
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(error));
    }

    @Test
    void operationCancelledReturnsLink() {
        final AmqpException error = new AmqpException(true, AmqpErrorCondition.OPERATION_CANCELLED, "cancelled", null);
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(error));
    }

    @Test
    void resourceLimitExceededReturnsNone() {
        final AmqpException error
            = new AmqpException(true, AmqpErrorCondition.RESOURCE_LIMIT_EXCEEDED, "resource limit", null);
        assertEquals(RecoveryKind.NONE, RecoveryKind.classify(error));
    }

    @Test
    void illegalStateExceptionDisposedMessageReturnsLink() {
        // Matches ReactorSender.send() message: "connectionId[%s] linkName[%s] Cannot publish message when disposed."
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(
            new IllegalStateException("connectionId[abc] linkName[xyz] Cannot publish message when disposed.")));
    }

    @Test
    void illegalStateExceptionDisposedDataBatchReturnsLink() {
        // Matches ReactorSender.send(List) message: "connectionId[%s] linkName[%s] Cannot publish data batch when disposed."
        assertEquals(RecoveryKind.LINK, RecoveryKind.classify(
            new IllegalStateException("connectionId[abc] linkName[xyz] Cannot publish data batch when disposed.")));
    }

    @Test
    void illegalStateExceptionUnrelatedToDisposedReturnsFatal() {
        // Non-disposed IllegalStateException must remain FATAL (genuine application or SDK bug).
        assertEquals(RecoveryKind.FATAL, RecoveryKind.classify(new IllegalStateException("some unexpected state")));
    }
}
