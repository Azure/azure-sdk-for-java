// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Sync-stack variant of {@link ProxyTaskGroupTests}.
 */
public class SyncProxyTaskGroupTests {

    @Test
    public void testSimpleTaskGroupSanity() {
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
        TaskGroup group = createSampleTaskGroup("A", "B", "C", "D", "E", "F", groupItems);

        // Invocation of group should invoke all the tasks
        //
        group.invoke(group.newInvocationContext().withSyncTaskExecutor(Runnable::run));

        Assertions.assertEquals(0, groupItems.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();

        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[] { "B", "C", "D", "E", "F" }));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[] { "F" }));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[] { "E", "F" }));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[] { "E", "F" }));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[] { "F" }));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[] { }));

        Set<String> seen = new HashSet<>();
        // Test invocation order for group
        //
        group.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group.getNext(); entry != null; entry = group.getNext()) {
            //            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group.reportCompletion(entry);
        }

        Assertions.assertEquals(6, seen.size()); // 1 groups with 6 nodes
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee.addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F" }));
        //        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B", "C", "D", "E", "F", group1Items);

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
        final TaskGroup group2 = createSampleTaskGroup("G", "H", "I", "J", "K", "L", group2Items);

        // Expand group-2 by adding group-1 as it's dependency
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
        group2.addDependencyTaskGroup(group1);

        // Invocation of group-1 should not invoke group-2
        //
        group1.invoke(group1.newInvocationContext().withSyncTaskExecutor(Runnable::run));

        Assertions.assertEquals(0, group1Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();

        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[] { "B", "C", "D", "E", "F" }));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[] { "F" }));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[] { "E", "F" }));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[] { "E", "F" }));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[] { "F" }));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[] { }));

        Set<String> seen = new HashSet<>();
        // Test invocation order for group-1
        //
        group1.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group1.getNext(); entry != null; entry = group1.getNext()) {
            //            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group1.reportCompletion(entry);
        }

        Assertions.assertEquals(6, seen.size()); // 1 groups with 6 nodes
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee.addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F" }));
        //        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B", "C", "D", "E", "F", group1Items);

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
        final TaskGroup group2 = createSampleTaskGroup("G", "H", "I", "J", "K", "L", group2Items);

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
        group2.addDependencyTaskGroup(group1);

        // Invocation of group-2 should invoke group-2 and group-1
        //
        group2.invoke(group2.newInvocationContext().withSyncTaskExecutor(Runnable::run));

        Assertions.assertEquals(0, group1Items.size());
        Assertions.assertEquals(0, group2Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[] { "B", "C", "D", "E", "F" }));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[] { "F" }));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[] { "E", "F" }));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[] { "E", "F" }));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[] { "F" }));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[] { }));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[] { "H", "I", "J", "K", "L" }));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[] { "L" }));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[] { "K", "L" }));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[] { "K", "L" }));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[] { "L" }));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[] { }));

        Set<String> seen = new HashSet<>();
        // Test invocation order for group-2
        //
        group2.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group2.getNext(); entry != null; entry = group2.getNext()) {
            Assertions.assertTrue(shouldNotSee.containsKey(entry.key()));
            Assertions.assertFalse(seen.contains(entry.key()));
            //            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group2.reportCompletion(entry);
        }

        Assertions.assertEquals(12, seen.size()); // 2 groups each with 6 nodes
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee
            .addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" }));
        //        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B", "C", "D", "E", "F", group1Items);

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
        final TaskGroup group2 = createSampleTaskGroup("G", "H", "I", "J", "K", "L", group2Items);

        // Add group-2 as group-1's "post run" dependent
        //
        /**
         *
         *                         |------------------->H------------|
         *                         |                                 |
         *         --------------->L                                 |
         *         |               |                                 ↓
         *         |           |---L            |------->I---------->G
         *         |           |   |            |                    ^
         *         |           |   |            |                    |             [group-2]
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
         *                              |            |                    |        [group-1]
         *                              |------------>E                   |
         *                                            |                   |
         *                                            |                   |
         *                                            ------->D------------
         */

        group1.addPostRunDependentTaskGroup(group2);

        // Invocation of group-1 should run group-1 and it's "post run" dependent group-2
        //
        group1.invoke(group1.newInvocationContext().withSyncTaskExecutor(Runnable::run));

        Assertions.assertEquals(0, group1Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[] { "B", "C", "D", "E", "F", "proxy-F" }));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[] { "F", "proxy-F" }));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[] { "E", "F", "proxy-F" }));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[] { "E", "F", "proxy-F" }));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[] { "F", "proxy-F" }));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[] { "proxy-F" }));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[] { "H", "I", "J", "K", "L", "proxy-F" }));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[] { "L", "proxy-F" }));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[] { "K", "L", "proxy-F" }));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[] { "K", "L", "proxy-F" }));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[] { "L", "proxy-F" }));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[] { "proxy-F" }));
        // NotSeen entries for proxies
        shouldNotSee.put("proxy-F", new HashSet<String>());
        shouldNotSee.get("proxy-F").addAll(Arrays.asList(new String[] { }));

        Set<String> seen = new HashSet<>();
        // Test invocation order for "group-1 proxy"
        //
        group1.proxyTaskGroupWrapper.taskGroup().prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group1.proxyTaskGroupWrapper.taskGroup().getNext(); entry != null;
            entry = group1.proxyTaskGroupWrapper.taskGroup().getNext()) {
            Assertions.assertTrue(shouldNotSee.containsKey(entry.key()));
            Assertions.assertFalse(seen.contains(entry.key()));
            //            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group1.proxyTaskGroupWrapper.taskGroup().reportCompletion(entry);
        }

        Assertions.assertEquals(13, seen.size()); // 2 groups each with 6 nodes + 1 proxy (proxy-F)
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee.addAll(
            Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "proxy-F" }));
        //        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
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
         *   |            |                    ^    [group-1]
         *   |            |                    |
         *   |------------>E                   |
         *                |                    |
         *                |                    |
         *                ------->D-------------
         */
        final LinkedList<String> group1Items = new LinkedList<>();
        final TaskGroup group1 = createSampleTaskGroup("A", "B", "C", "D", "E", "F", group1Items);

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
        final TaskGroup group2 = createSampleTaskGroup("G", "H", "I", "J", "K", "L", group2Items);

        // Add group-2 as group-1's "post run" dependent
        //
        /**
         *
         *                         |------------------->H------------|
         *                         |                                 |
         *         --------------->L                                 |
         *         |               |                                 ↓
         *         |           |---L            |------->I---------->G
         *         |           |   |            |                    ^
         *         |           |   |            |                    |            [group-2]
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
         *                              |            |                    |        [group-1]
         *                              |------------>E                   |
         *                                            |                   |
         *                                            |                   |
         *                                            ------->D------------
         */

        group1.addPostRunDependentTaskGroup(group2);

        // Invocation of group-2 should run group-2 and group-1
        //
        group2.invoke(group2.newInvocationContext().withSyncTaskExecutor(Runnable::run));

        Assertions.assertEquals(0, group2Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[] { "B", "C", "D", "E", "F", "proxy-F" }));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[] { "F", "proxy-F" }));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[] { "E", "F", "proxy-F" }));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[] { "E", "F", "proxy-F" }));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[] { "F", "proxy-F" }));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[] { "proxy-F" }));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[] { "H", "I", "J", "K", "L", "proxy-F" }));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[] { "L", "proxy-F" }));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[] { "K", "L", "proxy-F" }));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[] { "K", "L", "proxy-F" }));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[] { "L", "proxy-F" }));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[] { "proxy-F" }));
        // NotSeen entries for proxies
        shouldNotSee.put("proxy-F", new HashSet<String>());
        shouldNotSee.get("proxy-F").addAll(Arrays.asList(new String[] { }));

        Set<String> seen = new HashSet<>();
        // Test invocation order for "group-2 proxy"
        //
        group2.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group2.getNext(); entry != null; entry = group2.getNext()) {
            Assertions.assertTrue(shouldNotSee.containsKey(entry.key()));
            Assertions.assertFalse(seen.contains(entry.key()));
            //            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group2.reportCompletion(entry);
        }

        Assertions.assertEquals(12, seen.size()); // 2 groups each with 6 nodes no proxy
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee
            .addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" }));
        //        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
    }

    @Test
    public void canHandleDependenciesAndPostRunDependentsInBeforeGroupInvoke() throws InterruptedException {
        final ArrayList<String> seen = new ArrayList<>();
        CountDownLatch down = new CountDownLatch(1);
        final IndexableTaskItem itiA = new IndexableTaskItem("A") {
            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
            }

            @Override
            public Indexable invoke(TaskGroup.InvocationContext context) {
                seen.add(key());
                down.countDown();
                return null;
            }

            @Override
            public void invokeAfterPostRun(boolean isGroupFaulted) {
                // should not be called
                seen.add(key());
            }
        };

        final int[] beforeGroupInvokeCntB = new int[1];
        final IndexableTaskItem itiB = new IndexableTaskItem("B") {
            @Override
            public void beforeGroupInvoke() {
                beforeGroupInvokeCntB[0]++;
                this.addDependency(itiA);
            }

            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
            }

            @Override
            public Indexable invoke(TaskGroup.InvocationContext context) {
                seen.add(key());
                down.countDown();
                return null;
            }

            @Override
            public void invokeAfterPostRun(boolean isGroupFaulted) {
                // should not be called
                seen.add(key());
            }
        };

        final int[] beforeGroupInvokeCntC = new int[1];
        final IndexableTaskItem itiC = new IndexableTaskItem("C") {
            @Override
            public void beforeGroupInvoke() {
                beforeGroupInvokeCntC[0]++;
                this.addPostRunDependent(itiB);
            }

            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
            }

            @Override
            public Indexable invoke(TaskGroup.InvocationContext context) {
                seen.add(key());
                down.countDown();
                return null;
            }

            @Override
            public void invokeAfterPostRun(boolean isGroupFaulted) {
                seen.add(key());
                down.countDown();
            }
        };

        /**
         *            C" ---------> C
         *            |             ^
         *            |             |
         *            |             |
         *            |-----------> B ----> A
         */
        itiC.taskGroup().invoke(itiC.taskGroup().newInvocationContext().withSyncTaskExecutor(Runnable::run));

        down.await();

        boolean b1 = seen.equals(new ArrayList<>(Arrays.asList(new String[] { "A", "C", "B", "C" })));
        boolean b2 = seen.equals(new ArrayList<>(Arrays.asList(new String[] { "C", "A", "B", "C" })));

        if (!b1 && !b2) {
            Assertions.fail("Emission order should be either [A, C, B, C] or [C, A, B, C] but got " + seen);
        }

        Assertions.assertEquals(beforeGroupInvokeCntB[0], 1);
        Assertions.assertEquals(beforeGroupInvokeCntC[0], 1);

        // ------ //

        final CountDownLatch monitor = new CountDownLatch(1);
        final IndexableTaskItem itiD = new IndexableTaskItem("D") {
            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
            }

            @Override
            public Indexable invoke(TaskGroup.InvocationContext context) {
                seen.add(key());
                monitor.countDown();
                return null;
            }

            @Override
            public void invokeAfterPostRun(boolean isGroupFaulted) {
                // should not be called
                seen.add(key());
            }
        };
        final int[] beforeGroupInvokeCntE = new int[1];
        final IndexableTaskItem itiE = new IndexableTaskItem("E") {
            @Override
            public void beforeGroupInvoke() {
                beforeGroupInvokeCntE[0]++;
                this.addPostRunDependent(itiD);
            }

            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
            }

            @Override
            public Indexable invoke(TaskGroup.InvocationContext context) {
                seen.add(key());
                monitor.countDown();
                return null;
            }

            @Override
            public void invokeAfterPostRun(boolean isGroupFaulted) {
                seen.add(key());
                down.countDown();
            }
        };

        final int[] beforeGroupInvokeCntF = new int[1];
        final IndexableTaskItem itiF = new IndexableTaskItem("F") {
            @Override
            public void beforeGroupInvoke() {
                beforeGroupInvokeCntF[0]++;
                this.addDependency(itiE);
            }

            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
            }

            @Override
            public Indexable invoke(TaskGroup.InvocationContext context) {
                seen.add(key());
                monitor.countDown();
                return null;
            }

            @Override
            public void invokeAfterPostRun(boolean isGroupFaulted) {
                // should not be called
                seen.add(key());
            }
        };

        /**
         *  F-------->E" ---------> E
         *            |             ^
         *            |             |
         *            |             |
         *            |-----------> D
         */

        seen.clear();
        itiF.taskGroup().invoke(itiC.taskGroup().newInvocationContext().withSyncTaskExecutor(Runnable::run));

        monitor.await();

        b1 = seen.equals(new ArrayList<>(Arrays.asList(new String[] { "E", "D", "E", "F" })));
        Assertions.assertTrue(b1, "Emission order should be [E, D, E, F] but got " + seen);

        Assertions.assertEquals(beforeGroupInvokeCntE[0], 1);
        Assertions.assertEquals(beforeGroupInvokeCntF[0], 1);
    }

    private TaskGroup createSampleTaskGroup(String vertex1, String vertex2, String vertex3, String vertex4,
        String vertex5, String vertex6, List<String> verticesNames) {
        verticesNames.add(vertex6);
        verticesNames.add(vertex5);
        verticesNames.add(vertex4);
        verticesNames.add(vertex3);
        verticesNames.add(vertex2);
        verticesNames.add(vertex1);

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
        Consumer<String> vertexConsumer = vertex -> Assertions.assertTrue(() -> {
            synchronized (verticesNames) {
                return verticesNames.remove(vertex);
            }
        });

        TaskGroup group1 = new TaskGroup(vertex1, new StringTaskItem(vertex1, vertexConsumer));
        TaskGroup group2 = new TaskGroup(vertex2, new StringTaskItem(vertex2, vertexConsumer));
        TaskGroup group3 = new TaskGroup(vertex3, new StringTaskItem(vertex3, vertexConsumer));
        TaskGroup group4 = new TaskGroup(vertex4, new StringTaskItem(vertex4, vertexConsumer));
        TaskGroup group5 = new TaskGroup(vertex5, new StringTaskItem(vertex5, vertexConsumer));
        TaskGroup group6 = new TaskGroup(vertex6, new StringTaskItem(vertex6, vertexConsumer));

        group2.addDependencyTaskGroup(group1);
        group3.addDependencyTaskGroup(group1);
        group4.addDependencyTaskGroup(group1);

        group5.addDependencyTaskGroup(group3);
        group5.addDependencyTaskGroup(group4);

        group6.addDependencyTaskGroup(group2);
        group6.addDependencyTaskGroup(group5);

        return group6;
    }

    private StringIndexable toStringIndexable(Indexable indexable) {
        return (StringIndexable) indexable;
    }

    private static class StringTaskItem implements TaskItem {
        private final String name;
        private StringIndexable producedValue = null;
        private Consumer<String> syncPostRun = null;

        StringTaskItem(String name) {
            this.name = name;
        }

        StringTaskItem(String name, Consumer<String> syncPostRun) {
            this.name = name;
            this.syncPostRun = syncPostRun;
        }

        @Override
        public Indexable result() {
            return this.producedValue;
        }

        @Override
        public void beforeGroupInvoke() {
            // NO-OP
        }

        @Override
        public boolean isHot() {
            return false;
        }

        @Override
        public Mono<Indexable> invokeAsync(final TaskGroup.InvocationContext context) {
            this.producedValue = new StringIndexable(this.name);
            return Mono.just(this.producedValue).map(stringIndexable -> stringIndexable);
        }

        @Override
        public Mono<Void> invokeAfterPostRunAsync(boolean isGroupFaulted) {
            return Mono.empty();
        }

        @Override
        public Indexable invoke(TaskGroup.InvocationContext context) {
            this.producedValue = new StringIndexable(this.name);
            if (syncPostRun != null) {
                syncPostRun.accept(this.name);
            }
            return this.producedValue;
        }

        @Override
        public void invokeAfterPostRun(boolean isGroupFaulted) {
        }
    }

    private static class StringIndexable implements Indexable {
        private final String str;

        StringIndexable(String str) {
            this.str = str;
        }

        public String str() {
            return this.str;
        }

        @Override
        public String key() {
            return this.str;
        }
    }
}
