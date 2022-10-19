// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.test;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A utility and extension to dump threads after 30 minutes from starting tests.
 * It can be used as standalone utility via {@link #initialize()} as well as hooked up with JUnit
 * by implementing {@link BeforeAllCallback#beforeAll(ExtensionContext)}.
 */
public class ThreadDumper implements BeforeAllCallback {

    private static final ClientLogger LOGGER = new ClientLogger(ThreadDumper.class);
    private static volatile ExecutorService executorService;

    // Assume that after 30 minutes test job hangs.
    private static final int INITIAL_DELAY_IN_MINUTES = 30;
    // Log every minute after initial interval passes.
    private static final int RATE_IN_MINUTES = 2;

    /**
     * Initializes the singleton dumper. Can be called multiple times safely.
     */
    public static void initialize() {
        if (executorService == null) {
            synchronized (ThreadDumper.class) {
                if (executorService == null) {
                    executorService = createExecutorService();
                }
            }
        }
    }

    private static ExecutorService createExecutorService() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        Runtime.getRuntime().addShutdownHook(new Thread(service::shutdown));

        service.scheduleAtFixedRate(
            ThreadDumper::printThreadStacks,
            INITIAL_DELAY_IN_MINUTES,
            RATE_IN_MINUTES,
            TimeUnit.MINUTES
        );

        return service;
    }

    private static void printThreadStacks() {
        final StringBuilder dump = new StringBuilder("============= THREAD DUMP START =========");
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);
        for (ThreadInfo threadInfo : threadInfos) {
            dump.append('"');
            dump.append(threadInfo.getThreadName());
            dump.append("\" ");
            final Thread.State state = threadInfo.getThreadState();
            dump.append("\n   java.lang.Thread.State: ");
            dump.append(state);
            final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                dump.append("\n        at ");
                dump.append(stackTraceElement);
            }
            dump.append("\n\n");
        }
        dump.append("============= THREAD DUMP END =========");
        String output = dump.toString();

        // Log to both console and logs
        System.err.println(output);
        LOGGER.info(output);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        initialize();
    }
}
