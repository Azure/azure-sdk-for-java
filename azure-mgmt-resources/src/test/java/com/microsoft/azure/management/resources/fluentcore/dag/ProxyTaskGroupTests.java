/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ProxyTaskGroupTests {

    @Test
    public void testSampleTaskGroupSanity() {
        // Prepare sample group
        //
        /**
         *
         *   |------------------->B------------|
         *   |                                 |
         *   |                                 ↓
         *   F            ------->C----------->A
         *   |            |                    ^
         *   |            |                    |
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final List<String> groupItems = new ArrayList<>();
        TaskGroup<String, StringTaskItem> group = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                groupItems);



        // Invocation of group should invoke all the tasks
        //
        group.invokeAsync(group.newInvocationContext())
               .subscribe(new Action1<String>() {
                   @Override
                   public void call(String value) {
                       Assert.assertTrue(groupItems.contains(value));
                       groupItems.remove(value);
                   }
               });

        Assert.assertEquals(0, groupItems.size());

        // Test order
        //
        LinkedList<String> expectedOrder = new LinkedList<>();

        expectedOrder.push("F");
        expectedOrder.push("E");
        expectedOrder.push("D");
        expectedOrder.push("C");
        expectedOrder.push("B");
        expectedOrder.push("A");

        group.prepareForEnumeration();
        for (TaskGroupEntry<String, StringTaskItem> entry = group.getNext(); entry != null; entry = group.getNext()) {
            String top = expectedOrder.poll();
            Assert.assertNotNull(top);
            Assert.assertEquals(top, entry.key());
            group.reportCompletion(entry);
        }

        Assert.assertEquals(0, expectedOrder.size());

    }

    @Test
    public void testTaskGroupInvocationShouldNotInvokeDependentTaskGroup() {
        // Prepare group-1
        //
        /**
         *
         *   |------------------->B------------|
         *   |                                 |
         *   |                                 ↓
         *   F            ------->C----------->A
         *   |            |                    ^
         *   |            |                    |    [group-1]
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final List<String> group1Items = new ArrayList<>();
        final TaskGroup<String, StringTaskItem> group1 = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                group1Items);

        // Prepare group-2
        //
        /**
         *
         *   |------------------->H------------|
         *   |                                 |
         *   |                                 ↓
         *   L            ------->I----------->G
         *   |            |                    ^    [group-2]
         *   |            |                    |
         *   |------------>K                   |
         *                |                    |
         *                |                    |
         *                ------->J-------------
         */
        final List<String> group2Items = new ArrayList<>();
        final TaskGroup<String, StringTaskItem> group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

        // Expand group-2 by adding it as group-1's dependent
        //
        /**
         *
         *     |------------------->H------------|
         *     |                                 |
         *     |                                 ↓
         * |---L             ------->I---------->G
         * |   |            |                    ^          [group-2]
         * |   |            |                    |
         * |   |------------>K                   |
         * |                |                    |
         * |                |                    |
         * |                ------->J-------------
         * |
         * |        |------------------->B------------|
         * |        |                                 |
         * |        |                                 ↓
         * |------->F            ------->C----------->A
         *          |            |                    ^     [group-1]
         *          |            |                    |
         *          |------------>E                   |
         *                        |                   |
         *                        |                   |
         *                        ------->D------------
         */
        group1.addDependentTaskGroup(group2);

        // Invocation of group-1 should not invoke group-2
        //
        group1.invokeAsync(group1.newInvocationContext())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        Assert.assertTrue(group1Items.contains(value));
                        group1Items.remove(value);
                    }
                });

        Assert.assertEquals(0, group1Items.size());

        // Test order
        //
        LinkedList<String> expectedOrder = new LinkedList<>();

        expectedOrder.push("F");
        expectedOrder.push("E");
        expectedOrder.push("D");
        expectedOrder.push("C");
        expectedOrder.push("B");
        expectedOrder.push("A");

        group1.prepareForEnumeration();
        for (TaskGroupEntry<String, StringTaskItem> entry = group1.getNext(); entry != null; entry = group1.getNext()) {
            String top = expectedOrder.poll();
            Assert.assertNotNull(top);
            Assert.assertEquals(top, entry.key());
            group1.reportCompletion(entry);
        }

        Assert.assertEquals(0, expectedOrder.size());
    }

    @Test
    public void testTaskGroupInvocationShouldInvokeDependencyTaskGroup() {
        // Prepare group-1
        //
        /**
         *
         *   |------------------->B------------|
         *   |                                 |
         *   |                                 ↓
         *   F            ------->C----------->A
         *   |            |                    ^    [group-1]
         *   |            |                    |
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final List<String> group1Items = new ArrayList<>();
        final TaskGroup<String, StringTaskItem> group1 = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                group1Items);

        // Prepare group-2
        //
        /**
         *
         *   |------------------->H------------|
         *   |                                 |
         *   |                                 ↓
         *   L            ------->I----------->G
         *   |            |                    ^    [group-2]
         *   |            |                    |
         *   |------------>K                   |
         *                |                    |
         *                |                    |
         *                ------->J-------------
         */
        final List<String> group2Items = new ArrayList<>();
        final TaskGroup<String, StringTaskItem> group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

        // Expand group-2 by adding it as group-1's dependent
        //
        /**
         *
         *     |------------------->H------------|
         *     |                                 |
         *     |                                 ↓
         * |---L            ------->I----------->G
         * |   |            |                    ^          [group-2]
         * |   |            |                    |
         * |   |------------>K                   |
         * |                |                    |
         * |                |                    |
         * |                ------->J-------------
         * |
         * |        |------------------->B------------|
         * |        |                                 |
         * |        |                                 ↓
         * |------->F             ------->C---------->A
         *          |             |                   ^
         *          |             |                   |     [group-1]
         *          |------------>E                   |
         *                        |                   |
         *                        |                   |
         *                        ------->D------------
         */
        group1.addDependentTaskGroup(group2);

        group2Items.addAll(group1Items);

        // Invocation of group-2 should invoke group-2 and group-1
        //
        group2.invokeAsync(group2.newInvocationContext())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        Assert.assertTrue(group2Items.contains(value));
                        group2Items.remove(value);
                    }
                });

        Assert.assertEquals(0, group2Items.size());

        // Test order
        //
        LinkedList<String> expectedOrder = new LinkedList<>();

        expectedOrder.push("L");
        expectedOrder.push("F");
        expectedOrder.push("K");
        expectedOrder.push("E");
        expectedOrder.push("J");
        expectedOrder.push("I");
        expectedOrder.push("H");
        expectedOrder.push("D");
        expectedOrder.push("C");
        expectedOrder.push("B");
        expectedOrder.push("G");
        expectedOrder.push("A");

        group2.prepareForEnumeration();
        for (TaskGroupEntry<String, StringTaskItem> entry = group2.getNext(); entry != null; entry = group2.getNext()) {
            String top = expectedOrder.poll();
            Assert.assertNotNull(top);
            Assert.assertEquals(top, entry.key());
            group2.reportCompletion(entry);
        }

        Assert.assertEquals(0, expectedOrder.size());
    }

    @Test
    public void testTaskGroupInvocationShouldInvokePostRunDependentTaskGroup() {
        // Prepare group-1
        //
        /**
         *
         *   |------------------->B------------|
         *   |                                 |
         *   |                                 ↓
         *   F            ------->C----------->A
         *   |            |                    ^    [group-1]
         *   |            |                    |
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final LinkedList<String> group1Items = new LinkedList<>();
        final TaskGroup<String, StringTaskItem> group1 = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                group1Items);

        // Prepare group-2
        //
        /**
         *
         *   |------------------->H------------|
         *   |                                 |
         *   |                                 ↓
         *   L            ------->I----------->G
         *   |            |                    ^    [group-2]
         *   |            |                    |
         *   |------------>K                   |
         *                |                    |
         *                |                    |
         *                ------->J-------------
         */
        final LinkedList<String> group2Items = new LinkedList<>();
        final TaskGroup<String, StringTaskItem> group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

        // Expand group-2 by adding it as group-1's post run dependent
        //
        /**
         *
         *                         |------------------->H------------|
         *                         |                                 |
         *         --------------->L                                 |
         *         |               |                                 ↓
         *         |           |---L            |------->I---------->G
         *         |           |   |            |                    ^
         *         |           |   |            |                    |          [group-1]
         *         |           |   |------------>K                   |
         *         |           |                |                    |
         *         |           |                |                    |
         *   Proxy F"          |                ------->J-------------
         *         |           |
         *         |           |        |------------------->B------------|
         *         |           |        |                                 |
         *         |           |        |                                 |
         *         |           |------->F                                 ↓
         *         |                    |              ------->C--------->A
         *         |------------------->F            |                    ^
         *                              |            |                    |        [group-2]
         *                              |------------>E                   |
         *                                            |                   |
         *                                            |                   |
         *                                            ------->D------------
         */

        group1.addPostRunDependentTaskGroup(group2);

        group1Items.addAll(group2Items);

        // Invocation of group-1 should run group-2 and group-1
        //
        group1.invokeAsync(group1.newInvocationContext())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        Assert.assertTrue(group1Items.contains(value));
                        group1Items.remove(value);
                    }
                });

        Assert.assertEquals(0, group1Items.size());

//        // Test order
//        //
        LinkedList<String> expectedOrder = new LinkedList<>();

        expectedOrder.push("proxy-F");
        expectedOrder.push("L");
        expectedOrder.push("F");
        expectedOrder.push("K");
        expectedOrder.push("E");
        expectedOrder.push("J");
        expectedOrder.push("I");
        expectedOrder.push("H");
        expectedOrder.push("D");
        expectedOrder.push("C");
        expectedOrder.push("B");
        expectedOrder.push("G");
        expectedOrder.push("A");

        group1.proxyTaskGroupWrapper.proxyTaskGroup().prepareForEnumeration();
        for (TaskGroupEntry<String, TaskItem<String>> entry = group1.proxyTaskGroupWrapper.proxyTaskGroup().getNext();
             entry != null;
             entry = group1.proxyTaskGroupWrapper.proxyTaskGroup().getNext()) {
            String top = expectedOrder.poll();
            Assert.assertNotNull(top);
            Assert.assertEquals(top, entry.key());
            group1.proxyTaskGroupWrapper.proxyTaskGroup().reportCompletion(entry);
        }
        Assert.assertEquals(0, expectedOrder.size());
    }

    @Test
    public void testPostRunTaskGroupInvocationShouldInvokeDependencyTaskGroup() {
        // Prepare group-1
        //
        /**
         *
         *   |------------------->B------------|
         *   |                                 |
         *   |                                 ↓
         *   F            ------->C----------->A
         *   |            |                    ^
         *   |            |                    |
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final LinkedList<String> group1Items = new LinkedList<>();
        final TaskGroup<String, StringTaskItem> group1 = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                group1Items);

        // Prepare group-2
        //
        /**
         *
         *   |------------------->H------------|
         *   |                                 |
         *   |                                 ↓
         *   L            ------->I----------->G
         *   |            |                    ^
         *   |            |                    |
         *   |------------>K                   |
         *                |                    |
         *                |                    |
         *                ------->J-------------
         */
        final List<String> group2Items = new ArrayList<>();
        final TaskGroup<String, StringTaskItem> group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

        // Add group-2 as group-1's post run dependent.
        //
        /**
         *
         *                         |------------------->H------------|
         *                         |                                 |
         *         --------------->L                                 |
         *         |               |                                 ↓
         *         |           |---L            |------->I---------->G
         *         |           |   |            |                    ^
         *         |           |   |            |                    |          [group-1]
         *         |           |   |------------>K                   |
         *         |           |                |                    |
         *         |           |                |                    |
         *   Proxy F"          |                ------->J-------------
         *         |           |
         *         |           |        |------------------->B------------|
         *         |           |        |                                 |
         *         |           |        |                                 |
         *         |           |------->F                                 ↓
         *         |                    |              ------->C--------->A
         *         |------------------->F            |                    ^
         *                              |            |                    |        [group-2]
         *                              |------------>E                   |
         *                                            |                   |
         *                                            |                   |
         *                                            ------->D------------
         */

        group1.addPostRunDependentTaskGroup(group2);

        group2Items.addAll(group1Items);

        // Invocation of group-2 should run group-2 and group-1
        //
        group2.invokeAsync(group2.newInvocationContext())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        Assert.assertTrue(group2Items.contains(value));
                        group2Items.remove(value);
                    }
                });

        Assert.assertEquals(0, group2Items.size());

        // Test order
        //
        LinkedList<String> expectedOrder = new LinkedList<>();

        expectedOrder.push("L");
        expectedOrder.push("F");
        expectedOrder.push("K");
        expectedOrder.push("E");
        expectedOrder.push("J");
        expectedOrder.push("I");
        expectedOrder.push("H");
        expectedOrder.push("D");
        expectedOrder.push("C");
        expectedOrder.push("B");
        expectedOrder.push("G");
        expectedOrder.push("A");

        group2.prepareForEnumeration();
        for (TaskGroupEntry<String, StringTaskItem> entry = group2.getNext();
             entry != null;
             entry = group2.getNext()) {
            String top = expectedOrder.poll();
            Assert.assertNotNull(top);
            Assert.assertEquals(top, entry.key());
            group2.reportCompletion(entry);
        }
        Assert.assertEquals(0, expectedOrder.size());
    }

    @Test
    public void testProxyTaskGroupShouldBeUsedAsDependencyInsteadOfActualTaskGroup() {
        // Prepare group-1
        //
        /**
         *
         *   |------------------->B------------|
         *   |                                 |
         *   |                                 ↓
         *   F            ------->C----------->A
         *   |            |                    ^      [group-1]
         *   |            |                    |
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final LinkedList<String> group1Items = new LinkedList<>();
        final TaskGroup<String, StringTaskItem> group1 = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                group1Items);

        // Prepare group-2
        //
        /**
         *
         *   |------------------->H------------|
         *   |                                 |
         *   |                                 ↓
         *   L            ------->I----------->G
         *   |            |                    ^       [group-2]
         *   |            |                    |
         *   |------------>K                   |
         *                |                    |
         *                |                    |
         *                ------->J-------------
         */
        final List<String> group2Items = new ArrayList<>();
        final TaskGroup<String, StringTaskItem> group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

        // Add group-2 as group-1's post run dependent.
        //
        /**
         *
         *                         |------------------->H------------|
         *                         |                                 |
         *         --------------->L                                 ↓
         *         |           |---L            |------->I---------->G
         *         |           |   |            |                    ^        [group-2]
         *         |           |   |            |                    |
         *         |           |   |------------>K                   |
         *         |           |                |                    |
         *         |           |                |                    |
         *   Proxy F"          |                ------->J-------------
         *         |           |
         *         |           |        |------------------->B------------|
         *         |           |        |                                 |
         *         |           |        |                                 ↓
         *         |           |------->F            ------->C----------->A
         *         |------------------->F            |                    ^   [group-1]
         *                              |            |                    |
         *                              |------------>E                   |
         *                                            |                   |
         *                                            |                   |
         *                                            ------->D------------
         */

        group1.addPostRunDependentTaskGroup(group2);
        group2Items.addAll(group1Items);

        final List<String> group1ProxyItems = new ArrayList<>();
        group1ProxyItems.addAll(group2Items);

        // Prepare group-3
        //
        /**
         *
         *   |------------------->N------------|
         *   |                                 |
         *   |                                 ↓
         *   R            ------->O----------->M
         *   |            |                    ^   [group-3]
         *   |            |                    |
         *   |----------->Q                    |
         *                |                    |
         *                |                    |
         *                ------->P-------------
         */

        final LinkedList<String> group3Items = new LinkedList<>();
        final TaskGroup<String, StringTaskItem> group3 = createSampleTaskGroup("M", "N",
                "O", "P",
                "Q", "R",
                group3Items);

        // Add group-3 as group-1's dependent

        /**
         *
         *                         |------------------->H------------|
         *                         |                                 |
         *         --------------->L                                 ↓
         *         |           |---L            |------->I---------->G
         *         |           |   |            |                    ^       [group-2]
         *         |           |   |            |                    |
         *         |           |   |------------>K                   |
         *         |           |                |                    |
         *         |           |                |                    |
         *   Proxy F"          |                ------->J-------------
         *         |           |
         *         |           |        |------------------->B------------|
         *         |           |        |                                 |
         *         |           |        |                                 ↓
         *         |            ------->F            ------->C----------->A
         *         |------------------->F            |                    ^  [group-1]
         *                      ------->F            |                    |
         *                     |        |------------>E                   |
         *                     |                      |                   |
         *                     |                      |                   |
         *                     |                      ------->D------------
         *                     |
         *                     |    |------------------->N------------|
         *                     |    |                                 |
         *                     |    |                                 ↓
         *                     -----R            ------->O----------->M
         *                          |            |                    ^      [group-3]
         *                          |            |                    |
         *                          |----------->Q                    |
         *                                       |                    |
         *                                       |                    |
         *                                        ------->P-----------
         */

        group1.addDependentTaskGroup(group3);
        group3Items.addAll(group1Items);

        // Prepare group-4
        //
        /**
         *
         *   |------------------->T------------|
         *   |                                 |
         *   |                                 ↓
         *   X            ------->U----------->S
         *   |            |                    ^
         *   |            |                    |  [group-4]
         *   |----------->W                    |
         *                |                    |
         *                |                    |
         *                ------->V-------------
         */

        final LinkedList<String> group4Items = new LinkedList<>();
        final TaskGroup<String, StringTaskItem> group4 = createSampleTaskGroup("S", "T",
                "U", "V",
                "W", "X",
                group4Items);

        // Add group-4 as group-1's dependent (takes dependency on proxy task group)
        /**
         *
         *                                                                         |------------------->H------------|
         *                                                                         |                                 |
         *                                                         --------------->L                                 ↓
         *                                                         |           |---L            |------->I---------->G
         *                                                         |           |   |            |                    ^       [group-2]
         *                                                         |           |   |            |                    |
         *                                                         |           |   |------------>K                   |
         *                                                         |           |                |                    |
         *                                                         |           |                |                    |
         *    ---------------------------------------------->Proxy F"          |                ------->J-------------
         *   |                                                     |           |
         *   |                                                     |           |        |------------------->B------------|
         *   |                                                     |           |        |                                 |
         *   |                                                     |           |        |                                 ↓
         *   |                                                     |            ------->F             ------>C----------->A
         *   |                                                     |------------------->F             |                   ^  [group-1]
         *   |     |------------------->T------------|                          ------->F             |                   |
         *   |     |                                 ↓                         |        |------------>E                   |
         *   ------X            ------->U----------->S                         |                      |                   |
         *         |            |                    ^                         |                      |                   |
         *         |            |                    |  [group-4]              |                      ------->D------------
         *         |----------->W                    |                         |
         *                      |                    |                         |    |------------------->N------------|
         *                      |                    |                         |    |                                 |
         *                      ------->V-------------                         |    |                                 ↓
         *                                                                     -----R            ------->O----------->M
         *                                                                          |            |                    ^      [group-3]
         *                                                                          |            |                    |
         *                                                                          |----------->Q                    |
         *                                                                                       |                    |
         *                                                                                       |                    |
         *                                                                                        ------->P-----------
         **/

        group4.addDependencyTaskGroup(group1);
        group4Items.addAll(group1ProxyItems);
        group4Items.add("F");   // Duplicate emitted by Proxy

        // Invocation of group-4 should run group-1, group-2 & group-4
        //
        group4.invokeAsync(group4.newInvocationContext())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        Assert.assertTrue(group4Items.contains(value));
                        group4Items.remove(value);
                    }
                });

        Assert.assertEquals(0, group4Items.size());

        // Test order
        //
        LinkedList<String> expectedOrder = new LinkedList<>();

        expectedOrder.push("X");
        expectedOrder.push("proxy-F");
        expectedOrder.push("L");
        expectedOrder.push("F");
        expectedOrder.push("W");
        expectedOrder.push("K");
        expectedOrder.push("E");
        expectedOrder.push("V");
        expectedOrder.push("U");
        expectedOrder.push("T");
        expectedOrder.push("J");
        expectedOrder.push("I");
        expectedOrder.push("H");
        expectedOrder.push("D");
        expectedOrder.push("C");
        expectedOrder.push("B");
        expectedOrder.push("S");
        expectedOrder.push("G");
        expectedOrder.push("A");

        group4.prepareForEnumeration();
        for (TaskGroupEntry<String, StringTaskItem> entry = group4.getNext(); entry != null; entry = group4.getNext()) {
            String top = expectedOrder.poll();
            Assert.assertNotNull(top);
            Assert.assertEquals(top, entry.key());
            group4.reportCompletion(entry);
        }
    }

    private TaskGroup<String, StringTaskItem> createSampleTaskGroup(String v1,
                                                                    String v2,
                                                                    String v3,
                                                                    String v4,
                                                                    String v5,
                                                                    String v6,
                                                                    List<String> nodes) {
        nodes.add(v6);
        nodes.add(v5);
        nodes.add(v4);
        nodes.add(v3);
        nodes.add(v2);
        nodes.add(v1);

        /**
         * Creates a task group with following shape.
         *
         *   |------------------->group2------------|
         *   |                                      |
         *   |                                      ↓
         * group6         ------->group3--------->group1
         *   |            |                         ^
         *   |            |                         |
         *   |-------->group5                       |
         *                |                         |
         *                |                         |
         *                ------->group4-------------
         */

        TaskGroupTerminateOnErrorStrategy terminateStrategy = TaskGroupTerminateOnErrorStrategy.TERMINATE_ON_INPROGRESS_TASKS_COMPLETION;
        TaskGroup<String, StringTaskItem> group1 = new TaskGroup<>(v1, new StringTaskItem(v1), terminateStrategy);
        TaskGroup<String, StringTaskItem> group2 = new TaskGroup<>(v2, new StringTaskItem(v2), terminateStrategy);
        TaskGroup<String, StringTaskItem> group3 = new TaskGroup<>(v3, new StringTaskItem(v3), terminateStrategy);
        TaskGroup<String, StringTaskItem> group4 = new TaskGroup<>(v4, new StringTaskItem(v4), terminateStrategy);
        TaskGroup<String, StringTaskItem> group5 = new TaskGroup<>(v5, new StringTaskItem(v5), terminateStrategy);
        TaskGroup<String, StringTaskItem> group6 = new TaskGroup<>(v6, new StringTaskItem(v6), terminateStrategy);

        group1.addDependentTaskGroup(group2);
        group1.addDependentTaskGroup(group3);
        group1.addDependentTaskGroup(group4);

        group5.addDependencyTaskGroup(group3);
        group5.addDependencyTaskGroup(group4);

        group2.addDependentTaskGroup(group6);
        group6.addDependencyTaskGroup(group5);

        return group6;
    }


    private static class StringTaskItem implements TaskItem<String> {
        private final String name;
        private String producedValue = null;

        StringTaskItem(String name) {
            this.name = name;
        }

        @Override
        public String result() {
            return this.producedValue;
        }

        @Override
        public void prepare() {
        }

        @Override
        public boolean isHot() {
            return false;
        }

        @Override
        public Observable<String> invokeAsync(final TaskGroup.InvocationContext context) {
            this.producedValue = this.name;
            return Observable.just(this.producedValue);
        }
    }
}
