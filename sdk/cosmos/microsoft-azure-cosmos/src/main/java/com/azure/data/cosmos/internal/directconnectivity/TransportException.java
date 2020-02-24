// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

// TODO: DANOBLE: Use a TransportException derivative wherever CorruptFrameException is thrown in RntbdTransportClient
//   * Continue to throw IllegalArgumentException, IllegalStateException, and NullPointerException.
//   * Continue to complete all pending requests with a GoneException.
//     Customers should then expect to see these causes for GoneException errors originating in RntbdTransportClient:
//     - TransportException
//     - ReadTimeoutException
//     - WriteTimeoutException
//     These causes for GoneException errors will be logged as issues because they indicate a problem in the
//     RntbdTransportClient code:
//     - IllegalArgumentException
//     - IllegalStateException
//     - NullPointerException
//     Any other exceptions caught by the RntbdTransportClient code will also be logged as issues because they
//     indicate something unexpected happened.
//   NOTES:
//   We throw a derivative in one place: RntbdContextException in RntbdContext.decode. This is a special case
//   that is handled by RntbdRequestManager.userEventTriggered.

public class TransportException extends RuntimeException {
    public TransportException(String message, Throwable cause) {
        super(message, cause, /* enableSuppression */ true, /* writableStackTrace */ false);
    }
}
