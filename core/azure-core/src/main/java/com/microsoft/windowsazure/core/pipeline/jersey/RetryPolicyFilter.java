/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.core.pipeline.jersey;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

public class RetryPolicyFilter implements ServiceFilter {
    private static final Log LOG = LogFactory.getLog(RetryPolicyFilter.class);
    private final RetryPolicy retryPolicy;

    public RetryPolicyFilter(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @Override
    public ServiceResponseContext handle(ServiceRequestContext request,
            Next next) throws Exception {
        // Only the last added retry policy should be active
        if (request.getProperty("RetryPolicy") != null) {
            return next.handle(request);
        }

        request.setProperty("RetryPolicy", this);

        // Retry the operation as long as retry policy tells us to do so
        for (int retryCount = 0;; ++retryCount) {
            // Mark the stream before passing the request through
            if (getEntityStream(request) != null) {
                getEntityStream(request).mark(Integer.MAX_VALUE);
            }

            // Pass the request to the next handler
            ServiceResponseContext response = null;
            Exception error = null;
            try {
                response = next.handle(request);
            } catch (Exception e) {
                error = e;
            }

            // Determine if we should retry according to retry policy
            boolean shouldRetry = retryPolicy.shouldRetry(retryCount, response,
                    error);
            if (!shouldRetry) {
                if (error != null) {
                    throw error;
                }

                return response;
            }

            // Reset the stream before retrying
            if (getEntityStream(request) != null) {
                getEntityStream(request).reset();
            }

            // Backoff for some time according to retry policy
            int backoffTime = retryPolicy.calculateBackoff(retryCount,
                    response, error);
            LOG.info(String
                    .format("Request failed. Backing off for %1s milliseconds before retrying (retryCount=%2d)",
                            backoffTime, retryCount));
            backoff(backoffTime);
        }
    }

    private InputStream getEntityStream(ServiceRequestContext request) {
        if (request.getEntity() == null) {
            return null;
        }

        if (!(request.getEntity() instanceof InputStream)) {
            return null;
        }

        InputStream entityStream = (InputStream) request.getEntity();

        // If the entity is an InputStream that doesn't support "mark/reset", we
        // can't
        // implement a retry logic, so we simply throw.
        if (!entityStream.markSupported()) {
            throw new IllegalArgumentException(
                    "The input stream for the request entity must support 'mark' and "
                            + "'reset' to be compatible with a retry policy filter.");
        }

        return entityStream;
    }

    private void backoff(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }
}
