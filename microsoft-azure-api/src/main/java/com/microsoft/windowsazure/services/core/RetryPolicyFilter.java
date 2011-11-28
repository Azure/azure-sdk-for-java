package com.microsoft.windowsazure.services.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RetryPolicyFilter implements ServiceFilter {
    private static final Log log = LogFactory.getLog(RetryPolicyFilter.class);
    private final RetryPolicy retryPolicy;

    public RetryPolicyFilter(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public Response handle(Request request, Next next) throws Exception {
        // Only the last added retry policy should be active
        if (request.getProperties().containsKey("RetryPolicy"))
            return next.handle(request);
        request.getProperties().put("RetryPolicy", this);

        // Retry the operation as long as retry policy tells us to do so
        for (int retryCount = 0;; ++retryCount) {
            Response response = null;
            Exception error = null;
            try {
                response = next.handle(request);
            }
            catch (Exception e) {
                error = e;
            }

            boolean shouldRetry = retryPolicy.shouldRetry(retryCount, response, error);
            if (!shouldRetry) {
                if (error != null)
                    throw error;
                return response;
            }

            int backoffTime = retryPolicy.calculateBackoff(retryCount, response, error);
            log.info(String.format("Request failed. Backing off for %1s milliseconds before retrying (retryCount=%2d)", backoffTime, retryCount));
            backoff(backoffTime);
        }
    }

    private void backoff(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
        }
    }
}
