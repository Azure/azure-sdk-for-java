// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.azure.core.amqp.implementation.handler.DeliverySettleMode.ACCEPT_AND_SETTLE_ON_DELIVERY;
import static com.azure.core.amqp.implementation.handler.DeliverySettleMode.SETTLE_ON_DELIVERY;
import static com.azure.core.amqp.implementation.handler.DeliverySettleMode.SETTLE_VIA_DISPOSITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReceiverDeliveryHandlerTest {
    private static final String HOSTNAME = "hostname";
    private static final String ENTITY_PATH = "/orders";
    private static final String RECEIVER_LINK_NAME = "orders-link";
    private static final UUID DELIVERY_TAG_UUID = UUID.fromString("b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d");
    private static final byte[] DELIVERY_TAG_BYTES = {
        (byte) 112,
        (byte) 74,
        (byte) 220,
        (byte) 181,
        (byte) 93,
        (byte) 172,
        (byte) 179,
        (byte) 67,
        (byte) 177,
        50,
        (byte) 236,
        (byte) 143,
        (byte) 205,
        (byte) 172,
        (byte) 58,
        (byte) 157 };
    private static final byte[] DELIVERY_CONTENT_BYTES = {
        0,
        83,
        115,
        -64,
        15,
        13,
        64,
        64,
        64,
        64,
        64,
        83,
        1,
        64,
        64,
        64,
        64,
        64,
        64,
        64,
        0,
        83,
        116,
        -63,
        49,
        4,
        -95,
        11,
        115,
        116,
        97,
        116,
        117,
        115,
        45,
        99,
        111,
        100,
        101,
        113,
        0,
        0,
        0,
        -54,
        -95,
        18,
        115,
        116,
        97,
        116,
        117,
        115,
        45,
        100,
        101,
        115,
        99,
        114,
        105,
        112,
        116,
        105,
        111,
        110,
        -95,
        8,
        65,
        99,
        99,
        101,
        112,
        116,
        101,
        100 };

    private final ClientLogger logger = new ClientLogger(ReceiverUnsettledDeliveriesTest.class);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
    private AutoCloseable mocksCloseable;
    @Mock
    private ReactorDispatcher reactorDispatcher;
    @Mock
    private Receiver receiver;
    @Mock
    private Delivery delivery;

    @BeforeEach
    public void setup() throws IOException {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void shouldIgnorePartialDelivery() {
        when(delivery.isPartial()).thenReturn(true);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .expectNextCount(0)
                .verifyComplete();

        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldIgnoreSettledDelivery() {
        when(delivery.isSettled()).thenReturn(true);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .expectNextCount(0)
                .verifyComplete();
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldSettleDeliveryOnClosedLink() {
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.getLocalState()).thenReturn(EndpointState.CLOSED);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .expectNextCount(0)
                .verifyComplete();

            verify(delivery).disposition(argThat(deliveryState -> {
                return deliveryState.getType() == DeliveryState.DeliveryStateType.Modified;
            }));
            verify(delivery).settle();
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldReadDeliveryTag() {
        final boolean includeDeliveryTag = true;

        when(delivery.getTag()).thenReturn(DELIVERY_TAG_BYTES);
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(__ -> 0);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler(includeDeliveryTag);
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .assertNext(message -> {
                    Assertions.assertInstanceOf(MessageWithDeliveryTag.class, message);
                    final MessageWithDeliveryTag messageWithTag = (MessageWithDeliveryTag) message;
                    Assertions.assertEquals(DELIVERY_TAG_UUID, (messageWithTag.getDeliveryTag()));
                })
                .verifyComplete();

            verify(delivery).getTag();
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldNotReadDeliveryTag() {
        final boolean includeDeliveryTag = false;

        when(delivery.getTag()).thenReturn(DELIVERY_TAG_BYTES);
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(__ -> 0);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler(includeDeliveryTag);
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .assertNext(message -> {
                    Assertions.assertFalse(message instanceof MessageWithDeliveryTag);
                })
                .verifyComplete();

            verify(delivery, never()).getTag();
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldSettleOnDelivery() {
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(__ -> 0);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler(SETTLE_ON_DELIVERY);
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .expectNextCount(1)
                .verifyComplete();

            verify(delivery, never()).disposition(any());
            verify(delivery).settle();
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldAcceptAndSettleOnDelivery() {
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(__ -> 0);

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler(ACCEPT_AND_SETTLE_ON_DELIVERY);
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .expectNextCount(1)
                .verifyComplete();

            verify(delivery).disposition(eq(Accepted.getInstance()));
            verify(delivery).settle();
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldAdvanceWhenSettleViaDisposition() {
        when(delivery.getTag()).thenReturn(DELIVERY_TAG_BYTES);
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(__ -> 0);

        try (ReceiverUnsettledDeliveries unsettledDeliveries = createUnsettledDeliveries()) {
            final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler(unsettledDeliveries);
            try {
                StepVerifier.create(handler.getMessages())
                    .then(() -> handler.onDelivery(delivery))
                    .then(() -> handler.close(""))
                    .expectNextCount(1)
                    .verifyComplete();

                verify(receiver).advance();
                verify(delivery, never()).disposition(any());
                verify(delivery, never()).settle();
                Assertions.assertTrue(unsettledDeliveries.containsDelivery(DELIVERY_TAG_UUID));
            } finally {
                handler.close("");
            }
        }
    }

    @Test
    public void shouldReadDecodeDeliveryContent() {
        final int contentLength = DELIVERY_CONTENT_BYTES.length;

        when(delivery.pending()).thenReturn(contentLength);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(contentLength))).thenAnswer(invocation -> {
            final byte[] buffer = invocation.getArgument(0);
            System.arraycopy(DELIVERY_CONTENT_BYTES, 0, buffer, 0, contentLength);
            return contentLength;
        });

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onDelivery(delivery))
                .then(() -> handler.close(""))
                .assertNext(message -> {
                    Assertions.assertFalse(message instanceof MessageWithDeliveryTag);
                    final Map<String, Object> applicationProperties = message.getApplicationProperties().getValue();
                    Assertions.assertTrue(applicationProperties.containsKey("status-code"));
                    Assertions.assertEquals(202, applicationProperties.get("status-code"));
                    Assertions.assertTrue(applicationProperties.containsKey("status-description"));
                    Assertions.assertEquals("Accepted", applicationProperties.get("status-description"));
                })
                .verifyComplete();
        } finally {
            handler.close("");
        }
    }

    /**
     * The below git_tickets describes a problem we had with the receiver recovery when there was
     * a race between decode delivery and receiver closure. While reactor thread hopping played
     * a part in it, and now that we're removed thread hopping, nullifying the likelihood of it,
     * we don't want change the behavior.
     * @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/27482">27482</a>
     * @see <a href="https://github.com/Azure/azure-sdk-for-java/issues/27716">27716</a>
     * Same with {@link ReceiverDeliveryHandlerTest#shouldEmitErrorIfDeliveryContentReadFailsOnClose}.
     */
    @Test
    public void shouldEmitErrorIfDeliveryContentReadFailsOnTerminalEndpointState() {
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(invocation -> {
            throw new IllegalStateException("no current delivery");
        });

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.onLinkError())
                .then(() -> handler.onDelivery(delivery))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(IllegalStateException.class, error);
                    Assertions.assertNotNull(error.getCause());
                    Assertions.assertInstanceOf(IllegalStateException.class, error.getCause());
                });
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldEmitErrorIfDeliveryContentReadFailsOnClose() {
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(invocation -> {
            throw new IllegalStateException("no current delivery");
        });

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.preClose())
                .then(() -> handler.onDelivery(delivery))
                .verifyErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(IllegalStateException.class, error);
                    Assertions.assertNotNull(error.getCause());
                    Assertions.assertInstanceOf(IllegalStateException.class, error.getCause());
                });
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldEmitAndThrowErrorIfDeliveryContentReadFails() {
        when(delivery.pending()).thenReturn(0);
        when(delivery.getLink()).thenReturn(receiver);
        when(receiver.recv(any(), eq(0), eq(0))).thenAnswer(invocation -> {
            throw new UnsupportedOperationException();
        });

        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            // Any "unexpected" failure case on decode except "IllegalStateException combined with
            // receiver closure" must be rethrown, so that it gets bubbled to the ProtonJ Reactor
            // thread to recover / log to investigate what really happened.
            Assertions.assertThrows(UnsupportedOperationException.class, () -> {
                StepVerifier.create(handler.getMessages()).then(() -> handler.onDelivery(delivery)).verifyComplete();
            });
        } finally {
            handler.close("");
        }
    }

    @Test
    public void shouldCompleteOnClose() {
        final ReceiverDeliveryHandler handler = createReceiverDeliveryHandler();
        try {
            StepVerifier.create(handler.getMessages())
                .then(() -> handler.close(""))
                .expectNextCount(0)
                .verifyComplete();

        } finally {
            handler.close("");
        }
    }

    private ReceiverDeliveryHandler createReceiverDeliveryHandler() {
        return new ReceiverDeliveryHandler(ENTITY_PATH, RECEIVER_LINK_NAME, SETTLE_ON_DELIVERY,
            mock(ReceiverUnsettledDeliveries.class), false, logger);
    }

    private ReceiverDeliveryHandler createReceiverDeliveryHandler(boolean includeDeliveryTag) {
        return new ReceiverDeliveryHandler(ENTITY_PATH, RECEIVER_LINK_NAME, SETTLE_ON_DELIVERY,
            mock(ReceiverUnsettledDeliveries.class), includeDeliveryTag, logger);
    }

    private ReceiverDeliveryHandler createReceiverDeliveryHandler(DeliverySettleMode settleMode) {
        Assertions.assertTrue(settleMode == SETTLE_ON_DELIVERY || settleMode == ACCEPT_AND_SETTLE_ON_DELIVERY);
        return new ReceiverDeliveryHandler(ENTITY_PATH, RECEIVER_LINK_NAME, settleMode,
            mock(ReceiverUnsettledDeliveries.class), false, logger);
    }

    private ReceiverDeliveryHandler createReceiverDeliveryHandler(ReceiverUnsettledDeliveries unsettledDeliveries) {
        return new ReceiverDeliveryHandler(ENTITY_PATH, RECEIVER_LINK_NAME, SETTLE_VIA_DISPOSITION, unsettledDeliveries,
            true, logger);
    }

    private ReceiverUnsettledDeliveries createUnsettledDeliveries() {
        return new ReceiverUnsettledDeliveries(HOSTNAME, ENTITY_PATH, RECEIVER_LINK_NAME, reactorDispatcher,
            retryOptions, logger);
    }
}
