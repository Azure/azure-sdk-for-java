package com.microsoft.windowsazure.services.queue.client;

import java.net.HttpURLConnection;

import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;

/**
 * RESERVED FOR INTERNAL USE. A class for parsing various responses from the queue service.
 */
final class QueueResponse extends BaseResponse {

    /**
     * Gets the approximate messages count from the headers of a server response to a get metadata request.
     * 
     * @param request
     *            The <code>HttpURLConnection</code> containing a web request with a response from the server.
     * @return The <code>long</code> value of the approximate messages count header field.
     */
    public static long getApproximateMessageCount(final HttpURLConnection request) {
        return Long.parseLong(request.getHeaderField("x-ms-approximate-messages-count"));
    }
}
