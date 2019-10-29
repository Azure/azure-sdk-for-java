// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.EventHubException;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestResponseChannel implements IOObject {

    private final Sender sendLink;
    private final Receiver receiveLink;
    private final String replyTo;
    private final HashMap<Object, OperationResult<Message, Exception>> inflightRequests;
    private final AtomicLong requestId;
    private final AtomicInteger openRefCount;
    private final AtomicInteger closeRefCount;

    private OperationResult<Void, Exception> onOpen;
    private OperationResult<Void, Exception> onClose; // handles closeLink due to failures
    private OperationResult<Void, Exception> onGraceFullClose; // handles intentional close

    public RequestResponseChannel(
            final String linkName,
            final String path,
            final Session session,
            final ScheduledExecutorService executor) {

        this.replyTo = path.replace("$", "") + "-client-reply-to";
        this.openRefCount = new AtomicInteger(2);
        this.closeRefCount = new AtomicInteger(2);
        this.inflightRequests = new HashMap<>();
        this.requestId = new AtomicLong(0);

        this.sendLink = session.sender(linkName + ":sender");
        final Target target = new Target();
        target.setAddress(path);
        this.sendLink.setTarget(target);
        sendLink.setSource(new Source());
        this.sendLink.setSenderSettleMode(SenderSettleMode.SETTLED);
        BaseHandler.setHandler(this.sendLink, new SendLinkHandler(new RequestHandler(), linkName, executor));

        this.receiveLink = session.receiver(linkName + ":receiver");
        final Source source = new Source();
        source.setAddress(path);
        this.receiveLink.setSource(source);
        final Target receiverTarget = new Target();
        receiverTarget.setAddress(this.replyTo);
        this.receiveLink.setTarget(receiverTarget);
        this.receiveLink.setSenderSettleMode(SenderSettleMode.SETTLED);
        this.receiveLink.setReceiverSettleMode(ReceiverSettleMode.SECOND);
        BaseHandler.setHandler(this.receiveLink, new ReceiveLinkHandler(new ResponseHandler(), linkName, executor));
    }

    // open should be called only once - we use FaultTolerantObject for that
    public void open(final OperationResult<Void, Exception> onOpen, final OperationResult<Void, Exception> onClose) {

        this.onOpen = onOpen;
        this.onClose = onClose;
        this.sendLink.open();
        this.receiveLink.open();
    }

    // close should be called exactly once - we use FaultTolerantObject for that
    public void close(final OperationResult<Void, Exception> onGraceFullClose) {

        this.onGraceFullClose = onGraceFullClose;
        this.sendLink.close();
        this.receiveLink.close();
    }

    public Sender getSendLink() {
        return this.sendLink;
    }

    public Receiver getReceiveLink() {
        return this.receiveLink;
    }

    // not thread-safe
    // this must be invoked from reactor/dispatcher thread
    // & assumes that this is run on Opened Object
    public void request(
            final Message message,
            final OperationResult<Message, Exception> onResponse) {

        if (message == null) {
            throw new IllegalArgumentException("message cannot be null.");
        }
        if (message.getMessageId() != null) {
            throw new IllegalArgumentException("message.getMessageId() should be null");
        }
        if (message.getReplyTo() != null) {
            throw new IllegalArgumentException("message.getReplyTo() should be null");
        }
        message.setMessageId("request" + UnsignedLong.valueOf(this.requestId.incrementAndGet()).toString());
        message.setReplyTo(this.replyTo);

        this.inflightRequests.put(message.getMessageId(), onResponse);

        sendLink.delivery(UUID.randomUUID().toString().replace("-", StringUtil.EMPTY).getBytes(UTF_8));
        final int payloadSize = AmqpUtil.getDataSerializedSize(message) + 512; // need buffer for headers

        final byte[] bytes = new byte[payloadSize];
        final int encodedSize = message.encode(bytes, 0, payloadSize);

        receiveLink.flow(1);
        sendLink.send(bytes, 0, encodedSize);
        sendLink.advance();
    }

    private void onLinkOpenComplete(final Exception exception) {

        if (openRefCount.decrementAndGet() <= 0 && onOpen != null) {
            if (exception == null && this.sendLink.getRemoteState() == EndpointState.ACTIVE && this.receiveLink.getRemoteState() == EndpointState.ACTIVE) {
                onOpen.onComplete(null);
            } else {
                if (exception != null) {
                    onOpen.onError(exception);
                } else {
                    final ErrorCondition error = (this.sendLink.getRemoteCondition() != null && this.sendLink.getRemoteCondition().getCondition() != null)
                        ? this.sendLink.getRemoteCondition()
                        : this.receiveLink.getRemoteCondition();
                    onOpen.onError(ExceptionUtil.toException(error));
                }
            }
        }
    }

    private void onLinkCloseComplete(final Exception exception) {

        if (closeRefCount.decrementAndGet() <= 0) {
            if (exception == null) {
                if (onClose != null) {
                    onClose.onComplete(null);
                }
                if (onGraceFullClose != null) {
                    onGraceFullClose.onComplete(null);
                }
            } else {
                if (onClose != null) {
                    onClose.onError(exception);
                }
                if (onGraceFullClose != null) {
                    onGraceFullClose.onError(exception);
                }
            }
        }
    }

    @Override
    public IOObjectState getState() {

        if (sendLink.getLocalState() == EndpointState.UNINITIALIZED || receiveLink.getLocalState() == EndpointState.UNINITIALIZED
                || sendLink.getRemoteState() == EndpointState.UNINITIALIZED || receiveLink.getRemoteState() == EndpointState.UNINITIALIZED) {
            return IOObjectState.OPENING;
        }
        if (sendLink.getRemoteState() == EndpointState.ACTIVE && receiveLink.getRemoteState() == EndpointState.ACTIVE
                && sendLink.getLocalState() == EndpointState.ACTIVE && receiveLink.getRemoteState() == EndpointState.ACTIVE) {
            return IOObjectState.OPENED;
        }
        if (sendLink.getRemoteState() == EndpointState.CLOSED && receiveLink.getRemoteState() == EndpointState.CLOSED) {
            return IOObjectState.CLOSED;
        }
        return IOObjectState.CLOSING; // only left cases are if some are active and some are closed
    }

    private class RequestHandler implements AmqpSender {

        @Override
        public void onFlow(int creditIssued) {
        }

        @Override
        public void onSendComplete(Delivery delivery) {
        }

        @Override
        public void onOpenComplete(Exception completionException) {

            onLinkOpenComplete(completionException);
        }

        @Override
        public void onError(Exception exception, String failingLinkName) {

            onLinkCloseComplete(exception);
        }

        @Override
        public void onClose(ErrorCondition condition, String errorContext) {

            if (condition == null || condition.getCondition() == null) {
                onLinkCloseComplete(null);
            } else {
                onError(ExceptionUtil.toException(condition), errorContext);
            }
        }

    }

    private class ResponseHandler implements AmqpReceiver {

        @Override
        public void onReceiveComplete(Delivery delivery) {

            final Message response = Proton.message();
            final int msgSize = delivery.pending();
            final byte[] buffer = new byte[msgSize];

            final int read = receiveLink.recv(buffer, 0, msgSize);

            response.decode(buffer, 0, read);
            delivery.settle();

            final OperationResult<Message, Exception> responseCallback = inflightRequests.remove(response.getCorrelationId());
            if (responseCallback != null) {
                responseCallback.onComplete(response);
            }
        }

        @Override
        public void onOpenComplete(Exception completionException) {

            onLinkOpenComplete(completionException);
        }

        @Override
        public void onError(Exception exception, String failingLinkName) {

            this.cancelPendingRequests(exception);

            if (onClose != null) {
                onLinkCloseComplete(exception);
            }
        }

        @Override
        public void onClose(ErrorCondition condition, String errorContext) {

            if (condition == null || condition.getCondition() == null) {
                this.cancelPendingRequests(
                        new EventHubException(
                                ClientConstants.DEFAULT_IS_TRANSIENT,
                                "The underlying request-response channel closed, recreate the channel and retry the request."));

                if (onClose != null) {
                    onLinkCloseComplete(null);
                }
            } else {
                this.onError(ExceptionUtil.toException(condition), errorContext);
            }
        }

        private void cancelPendingRequests(final Exception exception) {
            for (OperationResult<Message, Exception> responseCallback : inflightRequests.values()) {
                responseCallback.onError(exception);
            }
            inflightRequests.clear();
        }
    }
}
