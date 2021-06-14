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
    private final String clientId;
    private final String sessionName;
    private final String linkName;
    private final String endpointAddress;
    private final AmqpConnection eventDispatcher;
    private final ScheduledExecutorService executor;

    private RequestResponseChannel currentChannel = null;
    private final Object isOpenedSynchronizer = new Object();
    private volatile boolean isOpening = false;

    public RequestResponseOpener(final SessionProvider sessionProvider, final String clientId, final String sessionName, final String linkName,
                                 final String endpointAddress, final AmqpConnection eventDispatcher, final ScheduledExecutorService executor) {
        this.sessionProvider = sessionProvider;
        this.clientId = clientId;
        this.sessionName = sessionName;
        this.linkName = linkName;
        this.endpointAddress = endpointAddress;
        this.eventDispatcher = eventDispatcher;
        this.executor = executor;
    }

    @Override
    public synchronized void run(OperationResult<RequestResponseChannel, Exception> operationCallback) {
        synchronized (this.isOpenedSynchronizer) {
            if (this.currentChannel != null) {
                if ((this.currentChannel.getState() == IOObjectState.OPENED) || (this.currentChannel.getState() == IOObjectState.OPENING)) {
                    if (TRACE_LOGGER.isInfoEnabled()) {
                        TRACE_LOGGER.info("inner channel currently open, no need to recreate");
                    }
                    return;
                }
            }

            // Inner channel doesn't exist or it is closing/closed. Do we need to start creation of a new one,
            // or is that already in progress?
            if (this.isOpening) {
                if (TRACE_LOGGER.isInfoEnabled()) {
                    TRACE_LOGGER.info("inner channel creation already in progress");
                }
                return;
            }

            // Need to start creating an inner channel.
            this.isOpening = true;
            if (TRACE_LOGGER.isInfoEnabled()) {
                TRACE_LOGGER.info("opening inner channel client draft2");
            }
        }

        final Session session = this.sessionProvider.getSession(
            this.sessionName,
            null,
            (error, exception) -> {
                if (error != null) {
                    operationCallback.onError(ExceptionUtil.toException(error));
                } else if (exception != null) {
                    operationCallback.onError(exception);
                }
            });

        if (session == null) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.error("got a null session, inner channel recreation cannot continue");
            }
            return;
        }
        final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                this.linkName,
                this.endpointAddress,
                session,
                this.executor);
        this.currentChannel = requestResponseChannel;
        requestResponseChannel.open(
                new OperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        eventDispatcher.registerForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.registerForConnectionError(requestResponseChannel.getReceiveLink());

                        operationCallback.onComplete(requestResponseChannel);

                        synchronized (RequestResponseOpener.this.isOpenedSynchronizer) {
                            // Inner channel creation complete.
                            isOpening = false;
                        }

                        if (TRACE_LOGGER.isInfoEnabled()) {
                            TRACE_LOGGER.info(String.format(Locale.US, "requestResponseChannel.onOpen complete clientId[%s], session[%s], link[%s], endpoint[%s]",
                                    clientId, sessionName, linkName, endpointAddress));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        operationCallback.onError(error);

                        synchronized (RequestResponseOpener.this.isOpenedSynchronizer) {
                            // Inner channel creation failed.
                            // The next time run() is called should try again.
                            isOpening = false;
                        }

                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "requestResponseChannel.onOpen error clientId[%s], session[%s], link[%s], endpoint[%s], error %s",
                                    clientId, sessionName, linkName, endpointAddress, error));
                        }
                    }
                },
                new OperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());

                        if (TRACE_LOGGER.isInfoEnabled()) {
                            TRACE_LOGGER.info(String.format(Locale.US, "requestResponseChannel.onClose complete clientId[%s], session[%s], link[%s], endpoint[%s]",
                                    clientId, sessionName, linkName, endpointAddress));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());

                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "requestResponseChannel.onClose error clientId[%s], session[%s], link[%s], endpoint[%s], error %s",
                                    clientId, sessionName, linkName, endpointAddress, error));
                        }
                    }
                });
    }
}
