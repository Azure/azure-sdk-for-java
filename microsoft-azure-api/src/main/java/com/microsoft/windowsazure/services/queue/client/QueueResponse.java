package com.microsoft.windowsazure.services.queue.client;

import java.net.HttpURLConnection;

import com.microsoft.windowsazure.services.core.storage.utils.implementation.BaseResponse;

/**
 * RESERVED FOR INTERNAL USE. A class for parsing various responses from the queue service
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
final class QueueResponse extends BaseResponse {

    /**
     * Gets the approximate messages count from the request header.
     * 
     * @param request
     *            The response from server.
     * @return the lease id from the request header.
     */
    public static long getApproximateMessageCount(final HttpURLConnection request) {
        return Long.parseLong(request.getHeaderField("x-ms-approximate-messages-count"));
    }
}
