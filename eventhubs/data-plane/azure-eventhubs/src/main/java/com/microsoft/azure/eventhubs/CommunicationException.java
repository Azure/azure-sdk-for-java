/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

/**
 * This exception is thrown when there is a client side connectivity issue. When receiving this exception user should
 * check client connectivity settings to the service:
 * <ul>
 * <li> Check for correct hostname and port number used in endpoint.
 * <li> Check for any possible proxy settings that can block amqp ports
 * <li> Check for any firewall settings that can block amqp ports
 * <li> Check for any general network connectivity issues, as well as network latency.
 * </ul>
 *
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class CommunicationException extends EventHubException {
    private static final long serialVersionUID = 7968596830506494332L;

    CommunicationException() {
        super(true);
    }

    CommunicationException(final String message) {
        super(true, message);
    }

    CommunicationException(final Throwable cause) {
        super(true, cause);
    }

    public CommunicationException(final String message, final Throwable cause) {
        super(true, message, cause);
    }
}
