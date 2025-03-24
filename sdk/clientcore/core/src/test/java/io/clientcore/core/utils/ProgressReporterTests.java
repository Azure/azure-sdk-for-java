// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.LongConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class ProgressReporterTests {

    private final ListProgressListener listener = new ListProgressListener();

    @Test
    public void listenerMustNotBeNull() {
        assertThrows(NullPointerException.class, () -> ProgressReporter.withProgressListener(null));
    }

    @Test
    public void whenNothingHappens() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);

        assertEquals(Collections.emptyList(), listener.getProgresses());
    }

    @Test
    public void canReportProgress() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);

        progressReporter.reportProgress(1);
        progressReporter.reportProgress(3);
        progressReporter.reportProgress(5);

        assertEquals(Arrays.asList(1L, 4L, 9L), listener.getProgresses());
    }

    @Test
    public void canResetProgress() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);

        progressReporter.reportProgress(5);
        progressReporter.reset();

        assertEquals(Arrays.asList(5L, 0L), listener.getProgresses());
    }

    @Test
    public void canResetProgressInTheMiddle() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);

        progressReporter.reportProgress(5);
        progressReporter.reset();
        progressReporter.reportProgress(4);

        assertEquals(Arrays.asList(5L, 0L, 4L), listener.getProgresses());
    }

    @Test
    public void childrenCanReportProgress() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);
        ProgressReporter child1 = progressReporter.createChild();
        ProgressReporter child2 = progressReporter.createChild();

        progressReporter.reportProgress(1);
        child1.reportProgress(7);
        progressReporter.reportProgress(3);
        child2.reportProgress(11);
        progressReporter.reportProgress(5);

        assertEquals(Arrays.asList(1L, 8L, 11L, 22L, 27L), listener.getProgresses());
    }

    @Test
    public void childrenCanResetProgress() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);
        ProgressReporter child1 = progressReporter.createChild();
        ProgressReporter child2 = progressReporter.createChild();

        progressReporter.reportProgress(1);
        child1.reportProgress(7);
        child1.reset();
        progressReporter.reportProgress(3);
        child2.reportProgress(11);
        progressReporter.reportProgress(5);
        child2.reset();

        assertEquals(Arrays.asList(1L, 8L, 1L, 4L, 15L, 20L, 9L), listener.getProgresses());
    }

    @Test
    public void testConcurrentReporting() throws ExecutionException, InterruptedException {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(listener);

        ForkJoinPool pool = new ForkJoinPool(10);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tasks.add(() -> {
                    ProgressReporter child = progressReporter.createChild();
                    child.reportProgress(1L);
                    child.reportProgress(3L);
                    child.reportProgress(5L);
                    child.reportProgress(7L);
                    child.reportProgress(11L);
                    return null;
                });
            }

            List<Future<Void>> asyncs = pool.invokeAll(tasks);
            for (Future<Void> async : asyncs) {
                async.get();
            }
        } finally {
            pool.shutdown();
        }

        List<Long> progresses = listener.getProgresses();
        assertEquals(2700L, progresses.get(progresses.size() - 1));
        for (int i = 0; i < progresses.size() - 1; i++) {
            if (progresses.get(i + 1) <= progresses.get(i)) {
                fail("Progresses are not in raising order. progress[" + (i + 1) + "]=" + progresses.get(i + 1)
                    + " progress[" + i + "]=" + progresses.get(i));
            }
        }
    }

    private static class ListProgressListener implements LongConsumer {
        private final List<Long> progresses = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void accept(long value) {
            progresses.add(value);
        }

        public List<Long> getProgresses() {
            return Collections.unmodifiableList(progresses);
        }
    }
}
