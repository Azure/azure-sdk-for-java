// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.test.shared;

import com.azure.core.util.logging.ClientLogger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadDumper {

    private static final ClientLogger LOGGER = new ClientLogger(StorageSpec.class);
    private static volatile ScheduledExecutorService executorService;

    // Assume that after 30 minutes test job hangs.
    private static final int INITIAL_DELAY_IN_MINUTES = 30;
    // Log every minute after initial interval passes.
    private static final int RATE_IN_MINUTES = 1;

    public static void initialize() {
        if (executorService == null) {
            synchronized (ThreadDumper.class) {
                if (executorService == null) {
                    executorService = Executors.newScheduledThreadPool(1, r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });
                    Runtime.getRuntime().addShutdownHook(new Thread(
                        () -> executorService.shutdown()
                    ));

                    executorService.scheduleAtFixedRate(
                        ThreadDumper::printThreadStacks,
                        INITIAL_DELAY_IN_MINUTES,
                        RATE_IN_MINUTES,
                        TimeUnit.MINUTES
                    );
                }
            }
        }
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
        System.out.println(output);
        LOGGER.info(output);
    }
}
