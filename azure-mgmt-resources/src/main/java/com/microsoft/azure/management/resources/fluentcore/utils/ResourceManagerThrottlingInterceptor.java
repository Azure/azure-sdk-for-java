/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * An interceptor for automatic retry when Azure Resource Manager is throttling because of too many read/write requests.
 * <p>
 * For each subscription and tenant, Azure Resource Manager limits read requests to 15,000 per hour and
 *   write requests to 1,200 per hour. These limits apply to each Azure Resource Manager instance.
 */
public class ResourceManagerThrottlingInterceptor implements Interceptor {
    private static final String LOGGING_HEADER = "x-ms-logging-context";
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Gate control
        try {
            synchronized (REENTRANT_LOCK) {
                if (REENTRANT_LOCK.isLocked()) {
                    REENTRANT_LOCK.wait();
                }
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        Response response = chain.proceed(chain.request());
        if (response.code() != 429) {
            return response;
        }

        try {
            synchronized (REENTRANT_LOCK) {
                if (REENTRANT_LOCK.isLocked()) {
                    REENTRANT_LOCK.wait();
                    return chain.proceed(chain.request());
                } else {
                    REENTRANT_LOCK.lock();
                }
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        try {
            String retryAfterHeader = response.header("Retry-After");
            int retryAfter = 0;
            if (retryAfterHeader != null) {
                retryAfter = Integer.parseInt(retryAfterHeader);
            }
            if (retryAfter <= 0) {
                Pattern pattern = Pattern.compile("try again after '([0-9]*)' minutes", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content(response.body()));
                if (matcher.find()) {
                    retryAfter = (int) TimeUnit.MINUTES.toSeconds(Integer.parseInt(matcher.group(1)));
                }
            }
            if (retryAfter > 0) {
                String context = chain.request().header(LOGGING_HEADER);
                if (context == null) {
                    context = "";
                }
                LoggerFactory.getLogger(context)
                    .info("Azure Resource Manager read/write per hour limit reached. Will retry in: " + retryAfter + " seconds");
                Thread.sleep(TimeUnit.SECONDS.toMillis(retryAfter) + 100);
            }
            return chain.proceed(chain.request());
        } catch (Throwable t) {
            throw new IOException(t);
        } finally {
            synchronized (REENTRANT_LOCK) {
                REENTRANT_LOCK.unlock();
                REENTRANT_LOCK.notifyAll();
            }
        }
    }

    private String content(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            return null;
        }
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        return buffer.readUtf8();
    }
}
