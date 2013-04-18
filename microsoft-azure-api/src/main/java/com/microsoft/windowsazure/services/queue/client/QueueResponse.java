/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
