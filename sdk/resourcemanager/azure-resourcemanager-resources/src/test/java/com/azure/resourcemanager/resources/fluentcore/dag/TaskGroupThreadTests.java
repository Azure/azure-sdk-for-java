// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TaskGroupThreadTests {
    @Test
    public void verifyTaskExecuteOnDefaultThread() throws InterruptedException {
        String outerThread = "outer";
        CountDownLatch latch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor(new DefaultThreadFactory(outerThread)).execute(() ->{
            Set<String> items = new HashSet<>();
            Consumer<Indexable> itemConsumer = indexable -> {
                TestTaskItem taskItem = (TestTaskItem) indexable;
                items.add(taskItem.key());
                // verify it runs on ForkJoinPool, not current thread
                Assertions.assertTrue(Thread.currentThread().getName().startsWith("Fork"));
            };
            TestTaskItem taskItem1 = new TestTaskItem("1", itemConsumer);
            TestTaskItem taskItem2 = new TestTaskItem("2", itemConsumer);
            TestTaskItem taskItem3 = new TestTaskItem("3", itemConsumer);
            TestTaskItem taskItem4 = new TestTaskItem("4", itemConsumer);
            TestTaskItem taskItem5 = new TestTaskItem("5", itemConsumer);

            taskItem1.addDependency(taskItem2);
            taskItem1.addPostRunDependent(taskItem3);
            taskItem4.addDependency(taskItem1);
            taskItem4.addPostRunDependent(taskItem5);

            taskItem4.taskGroup().invoke();
            Assertions.assertEquals(new HashSet<>(Arrays.asList("1", "2", "3", "4", "5")), items);
            latch.countDown();
        });
        latch.await();
    }

    @Test
    public void verifyTaskExecuteOnCurrentThread() throws InterruptedException {
        String outerThread = "outer";
        Executor currentThreadExecutor = Runnable::run;
        CountDownLatch latch = new CountDownLatch(1);
        // Tasks will all be executed on the calling thread.
        Executors.newSingleThreadExecutor(new DefaultThreadFactory(outerThread)).execute(() -> {

            Set<String> items = new HashSet<>();
            Consumer<Indexable> itemConsumer = indexable -> {
                TestTaskItem taskItem = (TestTaskItem) indexable;
                items.add(taskItem.key());
                // verify it runs on current thread
                Assertions.assertTrue(Thread.currentThread().getName().contains(outerThread));
            };
            TestTaskItem taskItem1 = new TestTaskItem("1", itemConsumer);
            TestTaskItem taskItem2 = new TestTaskItem("2", itemConsumer);
            TestTaskItem taskItem3 = new TestTaskItem("3", itemConsumer);
            TestTaskItem taskItem4 = new TestTaskItem("4", itemConsumer);
            TestTaskItem taskItem5 = new TestTaskItem("5", itemConsumer);

            taskItem1.addDependency(taskItem2);
            taskItem1.addPostRunDependent(taskItem3);
            taskItem4.addDependency(taskItem1);
            taskItem4.addPostRunDependent(taskItem5);

            taskItem4.taskGroup().invoke(taskItem4.taskGroup().newInvocationContext().withSyncExecutor(currentThreadExecutor));
            Assertions.assertEquals(new HashSet<>(Arrays.asList("1", "2", "3", "4", "5")), items);
            latch.countDown();
        });

        latch.await();
    }

    class TestTaskItem extends IndexableTaskItem {
        private Consumer<Indexable> postTaskSyncInvocation;

        TestTaskItem(String name, Consumer<Indexable> postTaskSyncInvocation) {
            super(name);
            this.postTaskSyncInvocation = postTaskSyncInvocation;
        }

        @Override
        protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
            return Mono.just(this).map(r -> r);
        }

        @Override
        public Indexable invokeTask(TaskGroup.InvocationContext context) {
            if (postTaskSyncInvocation != null) {
                postTaskSyncInvocation.accept(this);
            }
            return this;
        }
    }
}
