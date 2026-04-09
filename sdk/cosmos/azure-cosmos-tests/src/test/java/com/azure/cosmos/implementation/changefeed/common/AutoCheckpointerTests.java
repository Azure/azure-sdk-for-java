// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.CheckpointFrequency;
import com.azure.cosmos.implementation.changefeed.Lease;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

public class AutoCheckpointerTests {

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static Mono<Void> invokeCheckpointIfIntervalElapsed(AutoCheckpointer<?> autoCheckpointer) throws Exception {
        Method method = AutoCheckpointer.class.getDeclaredMethod("checkpointIfIntervalElapsed");
        method.setAccessible(true);
        return (Mono<Void>) method.invoke(autoCheckpointer);
    }

    @Test(groups = {"unit"})
    @SuppressWarnings("unchecked")
    public void everyBatchCheckpointStrategyCheckpointsAfterEachBatch() {
        ChangeFeedObserver<String> observer = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedObserverContext<String> context = Mockito.mock(ChangeFeedObserverContext.class);

        Mockito.when(observer.processChanges(any(), any())).thenReturn(Mono.empty());
        Mockito.when(context.checkpoint()).thenReturn(Mono.empty());

        AutoCheckpointer<String> autoCheckpointer = new AutoCheckpointer<>(new CheckpointFrequency(), observer);
        autoCheckpointer.open(context);

        autoCheckpointer.processChanges(context, Collections.singletonList("doc")).block();

        Mockito.verify(context, times(1)).checkpoint();
        autoCheckpointer.close(context, ChangeFeedObserverCloseReason.SHUTDOWN);
    }

    @Test(groups = {"unit"})
    @SuppressWarnings("unchecked")
    public void timeIntervalCheckpointStrategyUsesBackgroundTimer() throws InterruptedException {
        ChangeFeedObserver<String> observer = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedObserverContext<String> context = Mockito.mock(ChangeFeedObserverContext.class);

        Mockito.when(observer.processChanges(any(), any())).thenReturn(Mono.empty());
        Mockito.when(context.checkpoint()).thenReturn(Mono.empty());

        CheckpointFrequency checkpointFrequency = new CheckpointFrequency().withTimeInterval(Duration.ofMillis(50));
        AutoCheckpointer<String> autoCheckpointer = new AutoCheckpointer<>(checkpointFrequency, observer);
        autoCheckpointer.open(context);

        autoCheckpointer.processChanges(context, Collections.singletonList("doc")).block();
        Thread.sleep(150);

        Mockito.verify(context, Mockito.atLeastOnce()).checkpoint();
        autoCheckpointer.close(context, ChangeFeedObserverCloseReason.SHUTDOWN);
    }

    @Test(groups = {"unit"})
    @SuppressWarnings("unchecked")
    public void openFailureDoesNotStartIntervalTimer() throws Exception {
        ChangeFeedObserver<String> observer = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedObserverContext<String> context = Mockito.mock(ChangeFeedObserverContext.class);

        Mockito.doThrow(new IllegalStateException("open failed")).when(observer).open(any());

        AutoCheckpointer<String> autoCheckpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withTimeInterval(Duration.ofMillis(20)), observer);

        Assert.assertThrows(IllegalStateException.class, () -> autoCheckpointer.open(context));

        Disposable intervalDisposable = getField(autoCheckpointer, "intervalCheckpointDisposable");
        Assert.assertNull(intervalDisposable);
    }

    @Test(groups = {"unit"})
    @SuppressWarnings("unchecked")
    public void intervalCheckpointFailureTerminatesIntervalStream() throws InterruptedException {
        ChangeFeedObserver<String> observer = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedObserverContext<String> context = Mockito.mock(ChangeFeedObserverContext.class);

        Mockito.when(observer.processChanges(any(), any())).thenReturn(Mono.empty());
        Mockito.when(context.checkpoint()).thenReturn(Mono.error(new IllegalStateException("checkpoint failure")));

        AutoCheckpointer<String> autoCheckpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withTimeInterval(Duration.ofMillis(40)), observer);
        autoCheckpointer.open(context);

        autoCheckpointer.processChanges(context, Collections.singletonList("doc")).block();
        Thread.sleep(200);

        Mockito.verify(context, times(1)).checkpoint();
        autoCheckpointer.close(context, ChangeFeedObserverCloseReason.SHUTDOWN);
    }

    @Test(groups = {"unit"})
    @SuppressWarnings("unchecked")
    public void checkpointSuccessDoesNotClearNewerProgressArrivingDuringInFlightCheckpoint() throws Exception {
        ChangeFeedObserver<String> observer = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedObserverContext<String> context = Mockito.mock(ChangeFeedObserverContext.class);

        Mockito.when(observer.processChanges(any(), any())).thenReturn(Mono.empty());

        Sinks.One<Lease> firstCheckpoint = Sinks.one();
        Mockito.when(context.checkpoint())
            .thenReturn(firstCheckpoint.asMono(), Mono.just(Mockito.mock(Lease.class)));

        AutoCheckpointer<String> autoCheckpointer = new AutoCheckpointer<>(
            new CheckpointFrequency().withTimeInterval(Duration.ofSeconds(1)), observer);

        setField(autoCheckpointer, "lastCheckpointTime", Instant.now().minusSeconds(2));

        autoCheckpointer.processChanges(context, Collections.singletonList("batch-1")).subscribe();
        autoCheckpointer.processChanges(context, Collections.singletonList("batch-2")).subscribe();

        Mockito.verify(context, times(1)).checkpoint();

        firstCheckpoint.tryEmitValue(Mockito.mock(Lease.class));
        Thread.sleep(30);

        setField(autoCheckpointer, "lastCheckpointTime", Instant.now().minusSeconds(2));
        invokeCheckpointIfIntervalElapsed(autoCheckpointer).block();

        Mockito.verify(context, times(2)).checkpoint();
    }
}
