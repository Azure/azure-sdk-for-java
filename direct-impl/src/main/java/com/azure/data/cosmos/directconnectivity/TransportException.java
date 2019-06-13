/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.directconnectivity;

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
