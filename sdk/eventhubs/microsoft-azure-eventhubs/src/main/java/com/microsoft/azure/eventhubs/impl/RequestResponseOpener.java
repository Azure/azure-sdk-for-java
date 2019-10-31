// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;

public class RequestResponseOpener implements Operation<RequestResponseChannel> {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RequestResponseOpener.class);

    private final SessionProvider sessionProvider;
    private final String clientId;
    private final String sessionName;
    private final String linkName;
    private final String endpointAddress;
    private final AmqpConnection eventDispatcher;
    private final ScheduledExecutorService executor;

    private boolean isOpened;

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
        if (this.isOpened) {
            return;
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
            return;
        }
        final RequestResponseChannel requestResponseChannel = new RequestResponseChannel(
                this.linkName,
                this.endpointAddress,
                session,
                this.executor);

        requestResponseChannel.open(
                new OperationResult<Void, Exception>() {
                    @Override
                    public void onComplete(Void result) {
                        eventDispatcher.registerForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.registerForConnectionError(requestResponseChannel.getReceiveLink());

                        operationCallback.onComplete(requestResponseChannel);

                        isOpened = true;

                        if (TRACE_LOGGER.isInfoEnabled()) {
                            TRACE_LOGGER.info(String.format(Locale.US, "requestResponseChannel.onOpen complete clientId[%s], session[%s], link[%s], endpoint[%s]",
                                    clientId, sessionName, linkName, endpointAddress));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        operationCallback.onError(error);

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

                        isOpened = false;

                        if (TRACE_LOGGER.isInfoEnabled()) {
                            TRACE_LOGGER.info(String.format(Locale.US, "requestResponseChannel.onClose complete clientId[%s], session[%s], link[%s], endpoint[%s]",
                                    clientId, sessionName, linkName, endpointAddress));
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getSendLink());
                        eventDispatcher.deregisterForConnectionError(requestResponseChannel.getReceiveLink());

                        isOpened = false;

                        if (TRACE_LOGGER.isWarnEnabled()) {
                            TRACE_LOGGER.warn(String.format(Locale.US, "requestResponseChannel.onClose error clientId[%s], session[%s], link[%s], endpoint[%s], error %s",
                                    clientId, sessionName, linkName, endpointAddress, error));
                        }
                    }
                });
    }
}
