// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.exception.AzureException;

/**
 * This exception is thrown when user attempts to send a event data or brokered message that has exceeded the
 * allowed payload size as defined by the service. Note that in a batch send scenario the limit can include possible
 * batch overhead.
 *
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">http://go.microsoft.com/fwlink/?LinkId=761101</a>
 */
public class PayloadSizeExceededException extends AzureException {

    private static final long serialVersionUID = 3627182744252750014L;

    /**
     * Creates a new instance with the {@code message}.
     * @param message The message associated with this exception.
     */
    public PayloadSizeExceededException(final String message) {
        super(message);
    }
}
