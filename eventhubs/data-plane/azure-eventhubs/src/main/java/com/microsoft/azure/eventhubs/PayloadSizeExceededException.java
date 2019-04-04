// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

/**
 * this exception is thrown when user attempts to send a event data or brokered message that has exceeded the
 * allowed payload size as defined by the service. Note that in a batch send scenario the limit can include possible
 * batch overhead.
 *
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class PayloadSizeExceededException extends EventHubException {

    private static final long serialVersionUID = 3627182744252750014L;

    PayloadSizeExceededException() {
        super(false);
    }

    public PayloadSizeExceededException(final String message) {
        super(false, message);
    }

    PayloadSizeExceededException(final Throwable cause) {
        super(false, cause);
    }

    public PayloadSizeExceededException(final String message, final Throwable cause) {
        super(false, message, cause);
    }
}
