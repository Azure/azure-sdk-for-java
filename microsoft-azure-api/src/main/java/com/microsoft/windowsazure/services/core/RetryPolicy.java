package com.microsoft.windowsazure.services.core;

import com.microsoft.windowsazure.services.core.ServiceFilter.Response;

public abstract class RetryPolicy {
    public static final int DEFAULT_CLIENT_BACKOFF = 1000 * 30;
    public static final int DEFAULT_CLIENT_RETRY_COUNT = 3;
    public static final int DEFAULT_MAX_BACKOFF = 1000 * 90;
    public static final int DEFAULT_MIN_BACKOFF = 100 * 3;

    public abstract boolean shouldRetry(int retryCount, Response response, Exception error);

    public abstract int calculateBackoff(int retryCount, Response response, Exception error);
}
