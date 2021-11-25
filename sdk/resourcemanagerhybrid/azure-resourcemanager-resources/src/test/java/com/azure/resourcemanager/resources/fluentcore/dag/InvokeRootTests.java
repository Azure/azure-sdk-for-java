// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;

public class InvokeRootTests {
    @Test
    public void testIgnoreCachedResultOnRootWithNoProxy() {
        TestTaskItem taskItem1 = new TestTaskItem("A");
        TestTaskItem taskItem2 = new TestTaskItem("B");

        taskItem1.addDependency(taskItem2);

        final HashMap<String, Integer> seen = new HashMap<>();

        taskItem1.taskGroup().invokeAsync(taskItem1.taskGroup().newInvocationContext())
                .map(item -> {
                    SupportCountingAndHasName c = (SupportCountingAndHasName) item;
                    if (seen.containsKey(c.name())) {
                        Integer a = seen.get(c.name()) + 1;
                        seen.put(c.name(), a);
                    } else {
                        seen.put(c.name(), 1);
                    }
                    return item;
                }).blockLast();

        Assertions.assertEquals(2, seen.size());
        Assertions.assertTrue(seen.containsKey("A"));
        Assertions.assertTrue(seen.containsKey("B"));
        Assertions.assertEquals(1, (long) seen.get("A"));
        Assertions.assertEquals(1, (long) seen.get("B"));


        Assertions.assertEquals(1, taskItem1.getCallCount());
        Assertions.assertEquals(1, taskItem2.getCallCount());

        seen.clear();

        taskItem1.taskGroup().invokeAsync(taskItem1.taskGroup().newInvocationContext())
                .map(item -> {
                    SupportCountingAndHasName c = (SupportCountingAndHasName) item;
                    if (seen.containsKey(c.name())) {
                        Integer a = seen.get(c.name()) + 1;
                        seen.put(c.name(), a);
                    } else {
                        seen.put(c.name(), 1);
                    }
                    return item;
                }).blockLast();

        Assertions.assertEquals(2, seen.size());
        Assertions.assertTrue(seen.containsKey("A"));
        Assertions.assertTrue(seen.containsKey("B"));
        Assertions.assertEquals(1, (long) seen.get("A"));
        Assertions.assertEquals(1, (long) seen.get("B"));


        Assertions.assertEquals(2, taskItem1.getCallCount());
        Assertions.assertEquals(1, taskItem2.getCallCount());
    }

    @Test
    public void testIgnoreCachedResultOnRootWithProxy() {
        TestTaskItem taskItem1 = new TestTaskItem("X");
        TestTaskItem taskItem2 = new TestTaskItem("Y");
        TestTaskItem taskItem3 = new TestTaskItem("Z");

        taskItem1.addDependency(taskItem2);
        taskItem1.addPostRunDependent(taskItem3);

        final HashMap<String, Integer> seen = new HashMap<>();

        taskItem1.taskGroup().invokeAsync(taskItem1.taskGroup().newInvocationContext())
                .map(item -> {
                    SupportCountingAndHasName c = (SupportCountingAndHasName) item;
                    if (seen.containsKey(c.name())) {
                        Integer a = seen.get(c.name()) + 1;
                        seen.put(c.name(), a);
                    } else {
                        seen.put(c.name(), 1);
                    }
                    return item;
                }).blockLast();

        Assertions.assertEquals(3, seen.size()); // X, Y, Z

        Assertions.assertTrue(seen.containsKey("X"));
        Assertions.assertTrue(seen.containsKey("Y"));
        Assertions.assertTrue(seen.containsKey("Z"));
        Assertions.assertEquals(2, (long) seen.get("X"));   // Due to proxy two Xs
        Assertions.assertEquals(1, (long) seen.get("Y"));
        Assertions.assertEquals(1, (long) seen.get("Z"));

        Assertions.assertEquals(1, taskItem1.getCallCount());
        Assertions.assertEquals(1, taskItem2.getCallCount());
        Assertions.assertEquals(1, taskItem3.getCallCount());

        seen.clear();

        taskItem1.taskGroup().invokeAsync(taskItem1.taskGroup().newInvocationContext())
                .map(item -> {
                    SupportCountingAndHasName c = (SupportCountingAndHasName) item;
                    if (seen.containsKey(c.name())) {
                        Integer a = seen.get(c.name()) + 1;
                        seen.put(c.name(), a);
                    } else {
                        seen.put(c.name(), 1);
                    }
                    return item;
                }).blockLast();

        Assertions.assertEquals(3, seen.size());

        Assertions.assertTrue(seen.containsKey("X"));
        Assertions.assertTrue(seen.containsKey("Y"));
        Assertions.assertTrue(seen.containsKey("Z"));
        Assertions.assertEquals(2, (long) seen.get("X")); // Due to proxy two Xs
        Assertions.assertEquals(1, (long) seen.get("Y"));
        Assertions.assertEquals(1, (long) seen.get("Z"));

        // Though proxy is the root still actual must be called twice
        //
        Assertions.assertEquals(2, taskItem1.getCallCount());
        Assertions.assertEquals(1, taskItem2.getCallCount());
        Assertions.assertEquals(1, taskItem3.getCallCount());
    }

    @Test
    public void testIgnoreCachedResultOnRootWithProxyWithDescendantProxy() {
        TestTaskItem taskItem1 = new TestTaskItem("1");
        TestTaskItem taskItem2 = new TestTaskItem("2");
        TestTaskItem taskItem3 = new TestTaskItem("3");
        TestTaskItem taskItem4 = new TestTaskItem("4");
        TestTaskItem taskItem5 = new TestTaskItem("5");

        taskItem1.addDependency(taskItem2);
        taskItem1.addPostRunDependent(taskItem3);
        taskItem4.addDependency(taskItem1);
        taskItem4.addPostRunDependent(taskItem5);

        final HashMap<String, Integer> seen = new HashMap<>();

        taskItem4.taskGroup().invokeAsync(taskItem1.taskGroup().newInvocationContext())
                .map(item -> {
                    SupportCountingAndHasName c = (SupportCountingAndHasName) item;
                    if (seen.containsKey(c.name())) {
                        Integer a = seen.get(c.name()) + 1;
                        seen.put(c.name(), a);
                    } else {
                        seen.put(c.name(), 1);
                    }
                    return item;
                }).blockLast();

        Assertions.assertEquals(5, seen.size());

        Assertions.assertTrue(seen.containsKey("1"));
        Assertions.assertTrue(seen.containsKey("2"));
        Assertions.assertTrue(seen.containsKey("3"));
        Assertions.assertTrue(seen.containsKey("4"));
        Assertions.assertTrue(seen.containsKey("5"));

        Assertions.assertEquals(2, (long) seen.get("1")); // Due to proxy two 1s
        Assertions.assertEquals(1, (long) seen.get("2"));
        Assertions.assertEquals(1, (long) seen.get("3"));
        Assertions.assertEquals(2, (long) seen.get("4")); // Due to proxy two 1s
        Assertions.assertEquals(1, (long) seen.get("5"));

        Assertions.assertEquals(1, taskItem1.getCallCount());
        Assertions.assertEquals(1, taskItem2.getCallCount());
        Assertions.assertEquals(1, taskItem3.getCallCount());
        Assertions.assertEquals(1, taskItem4.getCallCount());
        Assertions.assertEquals(1, taskItem5.getCallCount());

        seen.clear();

        taskItem4.taskGroup().invokeAsync(taskItem1.taskGroup().newInvocationContext())
                .map(item -> {
                    SupportCountingAndHasName c = (SupportCountingAndHasName) item;
                    if (seen.containsKey(c.name())) {
                        Integer a = seen.get(c.name()) + 1;
                        seen.put(c.name(), a);
                    } else {
                        seen.put(c.name(), 1);
                    }
                    return item;
                }).blockLast();

        Assertions.assertEquals(5, seen.size());

        Assertions.assertTrue(seen.containsKey("1"));
        Assertions.assertTrue(seen.containsKey("2"));
        Assertions.assertTrue(seen.containsKey("3"));
        Assertions.assertTrue(seen.containsKey("4"));
        Assertions.assertTrue(seen.containsKey("5"));

        Assertions.assertEquals(2, (long) seen.get("1")); // Due to proxy two 1s
        Assertions.assertEquals(1, (long) seen.get("2"));
        Assertions.assertEquals(1, (long) seen.get("3"));
        Assertions.assertEquals(2, (long) seen.get("4")); // Due to proxy two 1s
        Assertions.assertEquals(1, (long) seen.get("5"));

        Assertions.assertEquals(1, taskItem1.getCallCount());
        Assertions.assertEquals(1, taskItem2.getCallCount());
        Assertions.assertEquals(1, taskItem3.getCallCount());
        Assertions.assertEquals(2, taskItem4.getCallCount());   // Only Root must be called twice
        Assertions.assertEquals(1, taskItem5.getCallCount());
    }

    class TestTaskItem extends IndexableTaskItem implements SupportCountingAndHasName {
        private final String name;
        private int callCount = 0;

        TestTaskItem(String name) {
            super(name);
            this.name = name;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public int getCallCount() {
            return this.callCount;
        }

        @Override
        protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
            return Mono.just(this)
                    .map(r -> {
                        callCount++;
                        return r;
                    });
        }
    }

    interface SupportCountingAndHasName extends HasName {
        int getCallCount();
    }
}
