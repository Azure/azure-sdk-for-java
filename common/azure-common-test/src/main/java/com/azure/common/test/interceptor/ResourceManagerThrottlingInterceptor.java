// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.interceptor;

import com.azure.common.test.utils.ResourceUtils;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.common.test.utils.TestConstant.TOO_MANY_REQUEST_CODE;

/**
 * The interceptor for automatic retry when Azure Resource Manager is throttling because of too many read/write requests.
 *
 * For each subscription and tenant, Azure Resource Manager limits read requests to 15,000 per hour and
 *   write requests to 1,200 per hour. These limits apply to each Azure Resource Manager instance.
 */
public class ResourceManagerThrottlingInterceptor implements Interceptor {
    private static final String LOGGING_HEADER = "x-ms-logging-context";
    private static final ConcurrentMap<String, ReentrantLock> REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
    private static final Object LOCK = new Object();

    /**
     * Empty constructor.
     */
    public ResourceManagerThrottlingInterceptor() {
    }

    /**
     * Get response from Interceptor chain request.
     * Automatic retry when Azure Resource Manager is throttling.
     *
     * @param chain The network call chain with rules
     * @return The response after interception
     * @throws IOException Throw IOException if no response received from new network request call.
     */
    public Response intercept(Chain chain) throws IOException {
        String subscriptionId = ResourceUtils.extractFromResourceId(chain.request().url().url().getPath(), "subscriptions");
        if (subscriptionId == null) {
            subscriptionId = "global";
        }
        REENTRANT_LOCK_MAP.putIfAbsent(subscriptionId, new ReentrantLock());
        ReentrantLock requestLock = REENTRANT_LOCK_MAP.get(subscriptionId);
        try {
            synchronized (LOCK) {
                while (requestLock.isLocked()) {
                    requestLock.wait();
                }
            }
        } catch (InterruptedException var27) {
            throw new IOException(var27);
        }

        Response response = chain.proceed(chain.request());
        if (response.code() != TOO_MANY_REQUEST_CODE) {
            return response;
        } else {
            try {
                synchronized (LOCK) {
                    while (requestLock.isLocked()) {
                        requestLock.wait();
                        return chain.proceed(chain.request());
                    }
                    requestLock.lock();
                }
            } catch (InterruptedException var25) {
                throw new IOException(var25);
            }
            Response var29;
            try {
                String retryAfterHeader = response.header("Retry-After");
                int retryAfter = 0;
                if (retryAfterHeader != null) {
                    retryAfter = Integer.parseInt(retryAfterHeader);
                }

                if (retryAfter <= 0) {
                    Pattern pattern = Pattern.compile("try again after '([0-9]*)' minutes", 2);
                    Matcher matcher = pattern.matcher(this.content(response.body()));
                    if (matcher.find()) {
                        retryAfter = (int) TimeUnit.MINUTES.toSeconds((long) Integer.parseInt(matcher.group(1)));
                    }
                }

                if (retryAfter > 0) {
                    String context = chain.request().header("x-ms-logging-context");
                    if (context == null) {
                        context = "";
                    }

                    LoggerFactory.getLogger(context).info("Azure Resource Manager read/write per hour limit reached. Will retry in: " + retryAfter + " seconds");
                    Thread.sleep(TimeUnit.SECONDS.toMillis((long) retryAfter) + 100L);
                }

                var29 = chain.proceed(chain.request());
            } catch (Throwable var22) {
                throw new IOException(var22);
            } finally {
                synchronized (LOCK) {
                    requestLock.unlock();
                    requestLock.notifyAll();
                }
            }
            return var29;
        }
    }

    private String content(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        } else {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            return buffer.readUtf8();
        }
    }
}
