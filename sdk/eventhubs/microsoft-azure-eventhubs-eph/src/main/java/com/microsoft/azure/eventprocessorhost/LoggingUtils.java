// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Centralize log message generation
 */
public final class LoggingUtils {
    static CompletionException wrapException(Throwable e, String action) {
        return new CompletionException(new ExceptionWithAction(e, action));
    }

    static CompletionException wrapExceptionWithMessage(Throwable e, String message, String action) {
        return new CompletionException(new ExceptionWithAction(e, message, action));
    }

    // outAction can be null if you don't care about any action string
    static Throwable unwrapException(Throwable wrapped, StringBuilder outAction) {
        Throwable unwrapped = wrapped;

        while ((unwrapped instanceof ExecutionException) || (unwrapped instanceof CompletionException)
                || (unwrapped instanceof ExceptionWithAction)) {
            if ((unwrapped instanceof ExceptionWithAction) && (outAction != null)) {
                // Save the action string from an ExceptionWithAction, if desired.
                outAction.append(((ExceptionWithAction) unwrapped).getAction());
            }

            if ((unwrapped.getCause() != null) && (unwrapped.getCause() instanceof Exception)) {
                unwrapped = unwrapped.getCause();
            } else {
                break;
            }
        }

        return unwrapped;
    }

    static String threadPoolStatusReport(String hostName, ScheduledExecutorService threadPool) {
        String report = "";

        if (threadPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) threadPool;

            StringBuilder builder = new StringBuilder();
            builder.append("Thread pool settings: core: ");
            builder.append(pool.getCorePoolSize());
            builder.append("  active: ");
            builder.append(pool.getActiveCount());
            builder.append("  current: ");
            builder.append(pool.getPoolSize());
            builder.append("  largest: ");
            builder.append(pool.getLargestPoolSize());
            builder.append("  max: ");
            builder.append(pool.getMaximumPoolSize());
            builder.append("  policy: ");
            builder.append(pool.getRejectedExecutionHandler().getClass().toString());
            builder.append("  queue avail: ");
            builder.append(pool.getQueue().remainingCapacity());

            report = builder.toString();
        } else {
            report = "Cannot report on thread pool of type " + threadPool.getClass().toString();
        }

        return report;
    }
}
