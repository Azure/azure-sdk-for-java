// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;

import com.microsoft.azure.eventhubs.impl.IOObject.IOObjectState;

public class RequestResponseOpener implements Operation<RequestResponseChannel> {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RequestResponseOpener.class);

    private final SessionProvider sessionProvider;
    private final OpenerContext context;
    private final ScheduledExecutorService executor;

    private RequestResponseChannel innerChannel;

    public RequestResponseOpener(final SessionProvider sessionProvider, final String clientId, final String sessionName, final String linkName,
                                 final String endpointAddress, final AmqpConnection eventDispatcher, final ScheduledExecutorService executor) {
        this.sessionProvider = sessionProvider;
        this.context = new OpenerContext(clientId, sessionName, linkName, endpointAddress, eventDispatcher);
        this.executor = executor;
    }

    @Override
    public synchronized void run(OperationResult<RequestResponseChannel, Exception> operationCallback) {
        if (isInnerChannelValid()) {
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info("isOpened is true, immediate return from run()");
            }
            return;
        }

        final Session session = this.sessionProvider.getSession(
            this.context.sessionName,
            null,
            (error, exception) -> {
                if (error != null) {
                    operationCallback.onError(ExceptionUtil.toException(error));
                } else if (exception != null) {
                    operationCallback.onError(exception);
                }
            });

        if (session == null) {
            return;
        }
        this.innerChannel = new RequestResponseChannel(
                this.context.linkName,
                this.context.endpointAddress,
                session,
                this.executor);

        this.innerChannel.open(
                new OperationResult<Void, Exception>() {
                    final RequestResponseChannel capturedRequestResponseChannel = RequestResponseOpener.this.innerChannel;
                    final OperationResult<RequestResponseChannel, Exception> capturedOperationCallback = operationCallback;
                    final OpenerContext capturedContext = RequestResponseOpener.this.context;

                    @Override
                    public void onComplete(Void result) {
                        this.capturedContext.eventDispatcher.registerForConnectionError(this.capturedRequestResponseChannel.getSendLink());
                        this.capturedContext.eventDispatcher.registerForConnectionError(this.capturedRequestResponseChannel.getReceiveLink());

                        this.capturedOperationCallback.onComplete(this.capturedRequestResponseChannel);

                        if (TRACE_LOGGER.isInfoEnabled()) {
                            TRACE_LOGGER.info(String.format(Locale.US, "requestResponseChannel.onOpen complete clientId[%s], session[%s], link[%s], endpoint[%s]",
                                this.capturedContext.clientId, this.capturedContext.sessionName,
                                this.capturedContext.linkName, this.capturedContext.endpointAddress));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        this.capturedOperationCallback.onError(error);

                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "requestResponseChannel.onOpen error clientId[%s], session[%s], link[%s], endpoint[%s], error %s",
                                this.capturedContext.clientId, this.capturedContext.sessionName,
                                this.capturedContext.linkName, this.capturedContext.endpointAddress, error));
                        }
                    }
                },
                new OperationResult<Void, Exception>() {
                    final RequestResponseChannel capturedRequestResponseChannel = RequestResponseOpener.this.innerChannel;
                    final OpenerContext capturedContext = RequestResponseOpener.this.context;

                    @Override
                    public void onComplete(Void result) {
                        this.capturedContext.eventDispatcher.deregisterForConnectionError(this.capturedRequestResponseChannel.getSendLink());
                        this.capturedContext.eventDispatcher.deregisterForConnectionError(this.capturedRequestResponseChannel.getReceiveLink());

                        if (TRACE_LOGGER.isInfoEnabled()) {
                            TRACE_LOGGER.info(String.format(Locale.US, "requestResponseChannel.onClose complete clientId[%s], session[%s], link[%s], endpoint[%s]",
                                this.capturedContext.clientId, this.capturedContext.sessionName,
                                this.capturedContext.linkName, this.capturedContext.endpointAddress));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        this.capturedContext.eventDispatcher.deregisterForConnectionError(this.capturedRequestResponseChannel.getSendLink());
                        this.capturedContext.eventDispatcher.deregisterForConnectionError(this.capturedRequestResponseChannel.getReceiveLink());

                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "requestResponseChannel.onClose error clientId[%s], session[%s], link[%s], endpoint[%s], error %s",
                                this.capturedContext.clientId, this.capturedContext.sessionName,
                                this.capturedContext.linkName, this.capturedContext.endpointAddress, error));
                        }
                    }
                });
    }

    private boolean isInnerChannelValid() {
        if ((this.innerChannel != null) &&
            ((this.innerChannel.getState() == IOObjectState.OPENING) || (this.innerChannel.getState() == IOObjectState.OPENED))) {
                return true;
        }
        return false;
    }

    private class OpenerContext {
        public final String clientId;
        public final String sessionName;
        public final String linkName;
        public final String endpointAddress;
        public final AmqpConnection eventDispatcher;

        public OpenerContext(final String clientId,
                final String sessionName,
                final String linkName,
                final String endpointAddress,
                final AmqpConnection eventDispatcher) {
            this.clientId = clientId;
            this.sessionName = sessionName;
            this.linkName = linkName;
            this.endpointAddress = endpointAddress;
            this.eventDispatcher = eventDispatcher;
        }
    }
}
