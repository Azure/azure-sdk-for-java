// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClientState;

/**
 * A {@link ConnectionObserver} that is used to apply timeout handlers to the Reactor Netty/Netty connection.
 */
public final class AzureTimeoutConnectionObserver implements ConnectionObserver {
    private final long writeTimeout;
    private final long responseTimeout;
    private final long readTimeout;

    public AzureTimeoutConnectionObserver(long writeTimeout, long responseTimeout,
        long readTimeout) {
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public void onStateChange(Connection connection, State newState) {
        if (newState == HttpClientState.REQUEST_PREPARED) {
            if (writeTimeout > 0) {
                connection.addHandlerLast(WriteTimeoutHandler.HANDLER_NAME, new WriteTimeoutHandler(writeTimeout));
            }

            AzureNettyHttpClientAttr attr = connection.channel().attr(AzureNettyHttpClientAttr.ATTRIBUTE_KEY).get();
            if (attr != null && attr.getProgressReporter() != null) {
                connection.addHandlerLast(RequestProgressReportingHandler.HANDLER_NAME,
                    new RequestProgressReportingHandler(attr.getProgressReporter()));
            }
        } else if (newState == HttpClientState.REQUEST_SENT) {
            if (writeTimeout > 0) {
                connection.removeHandler(WriteTimeoutHandler.HANDLER_NAME);
            }

            AzureNettyHttpClientAttr attr = connection.channel().attr(AzureNettyHttpClientAttr.ATTRIBUTE_KEY).get();
            if (attr != null && attr.getProgressReporter() != null) {
                connection.removeHandler(RequestProgressReportingHandler.HANDLER_NAME);
            }

            long responseTimeout = (attr != null && attr.getResponseTimeoutOverride() != null)
                ? attr.getResponseTimeoutOverride()
                : this.responseTimeout;

            connection.addHandlerLast(ResponseTimeoutHandler.HANDLER_NAME, new ResponseTimeoutHandler(responseTimeout));
        } else if (newState == HttpClientState.RESPONSE_RECEIVED) {
            connection.removeHandler(ResponseTimeoutHandler.HANDLER_NAME);
            connection.addHandlerLast(ReadTimeoutHandler.HANDLER_NAME, new ReadTimeoutHandler(readTimeout));
        } else if (newState == HttpClientState.RESPONSE_COMPLETED) {
            connection.removeHandler(ReadTimeoutHandler.HANDLER_NAME);
        }
    }
}
