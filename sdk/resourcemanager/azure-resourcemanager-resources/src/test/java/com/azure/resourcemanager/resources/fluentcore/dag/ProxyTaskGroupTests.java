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
        TaskGroup group = createSampleTaskGroup("A", "B",
                "C", "D",
                "E", "F",
                groupItems);

        // Invocation of group should invoke all the tasks
        //
        group.invokeAsync(group.newInvocationContext())
                .subscribe(value -> {
                    StringIndexable stringIndexable = toStringIndexable(value);
                    Assertions.assertTrue(groupItems.contains(stringIndexable.str()));
                    groupItems.remove(stringIndexable.str());
                });

        Assertions.assertEquals(0, groupItems.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();

        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{}));

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
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F"}));
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B",
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
        final TaskGroup group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

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
        group1.invokeAsync(group1.newInvocationContext())
                .subscribe(value -> {
                    StringIndexable stringIndexable = toStringIndexable(value);
                    Assertions.assertTrue(group1Items.contains(stringIndexable.str()));
                    group1Items.remove(stringIndexable.str());
                });

        Assertions.assertEquals(0, group1Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();

        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{}));

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
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F"}));
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B",
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
        final TaskGroup group2 = createSampleTaskGroup("G", "H",
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
        group2.addDependencyTaskGroup(group1);

        group2Items.addAll(group1Items);

        // Invocation of group-2 should invoke group-2 and group-1
        //
        group2.invokeAsync(group2.newInvocationContext())
                .subscribe(value -> {
                    StringIndexable stringIndexable = toStringIndexable(value);
                    Assertions.assertTrue(group2Items.contains(stringIndexable.str()));
                    group2Items.remove(stringIndexable.str());
                });

        Assertions.assertEquals(0, group2Items.size());


        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{}));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[]{"H", "I", "J", "K", "L"}));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[]{"K", "L"}));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[]{"K", "L"}));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[]{}));

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
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"}));
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B",
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
        final TaskGroup group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

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

        group1Items.addAll(group2Items);

        // Invocation of group-1 should run group-1 and it's "post run" dependent group-2
        //
        group1.invokeAsync(group1.newInvocationContext())
            .subscribe(
                value -> {
                    StringIndexable stringIndexable = toStringIndexable(value);
                    Assertions.assertTrue(group1Items.contains(stringIndexable.str()));
                    group1Items.remove(stringIndexable.str());
                }, throwable -> {
                }
            );

        Assertions.assertEquals(0, group1Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F", "proxy-F"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F", "proxy-F"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F", "proxy-F"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{"proxy-F"}));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[]{"H", "I", "J", "K", "L", "proxy-F"}));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[]{"L", "proxy-F"}));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[]{"K", "L", "proxy-F"}));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[]{"K", "L", "proxy-F"}));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[]{"L", "proxy-F"}));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[]{"proxy-F"}));
        // NotSeen entries for proxies
        shouldNotSee.put("proxy-F", new HashSet<String>());
        shouldNotSee.get("proxy-F").addAll(Arrays.asList(new String[]{}));

        Set<String> seen = new HashSet<>();
        // Test invocation order for "group-1 proxy"
        //
        group1.proxyTaskGroupWrapper.taskGroup().prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group1.proxyTaskGroupWrapper.taskGroup().getNext();
             entry != null;
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
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "G", "H",
            "I", "J", "K", "L",
            "proxy-F"}));
//        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());


        group1.invokeAsync(group1.newInvocationContext())
                .subscribe(indexable -> System.out.println(indexable.key()));
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B",
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
        final TaskGroup group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

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

        group2Items.addAll(group1Items);

        // Invocation of group-2 should run group-2 and group-1
        //
        group2.invokeAsync(group2.newInvocationContext())
                .subscribe(value -> {
                    StringIndexable stringIndexable = toStringIndexable(value);
                    Assertions.assertTrue(group2Items.contains(stringIndexable.str()));
                    group2Items.remove(stringIndexable.str());
                });

        Assertions.assertEquals(0, group2Items.size());

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F", "proxy-F"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F", "proxy-F"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F", "proxy-F"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{"proxy-F"}));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[]{"H", "I", "J", "K", "L", "proxy-F"}));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[]{"L", "proxy-F"}));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[]{"K", "L", "proxy-F"}));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[]{"K", "L", "proxy-F"}));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[]{"L", "proxy-F"}));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[]{"proxy-F"}));
        // NotSeen entries for proxies
        shouldNotSee.put("proxy-F", new HashSet<String>());
        shouldNotSee.get("proxy-F").addAll(Arrays.asList(new String[]{}));

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
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "G", "H",
            "I", "J", "K", "L"}));
//        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
    }

    @Test
    public void testParentReassignmentUponProxyTaskGroupActivation() {
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B",
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
        final TaskGroup group2 = createSampleTaskGroup("G", "H",
                "I", "J",
                "K", "L",
                group2Items);

        // Make group-2 as group-1's parent by adding group-1 as group-2's dependency.
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

        // Check parent
        Assertions.assertEquals(1, group1.parentDAGs.size());
        Assertions.assertTrue(group1.parentDAGs.contains(group2));

        // Prepare group-3
        //
        /**
         *
         *   |------------------->N------------|
         *   |                                 |
         *   |                                 ↓
         *   R            ------->O----------->M
         *   |            |                    ^            [group-3]
         *   |            |                    |
         *   |----------->Q                    |
         *                |                    |
         *                |                    |
         *                ------->P-------------
         */

        final LinkedList<String> group3Items = new LinkedList<>();
        final TaskGroup group3 = createSampleTaskGroup("M", "N",
                "O", "P",
                "Q", "R",
                group3Items);

        // Make group-3 as group-1's 'post-run" dependent. This activate proxy group, should do parent re-assignment
        // i.e. the parent "group-2" of "group-1" will become parent of "group-1's proxy".
        //
        /**
         *                [group-2]
         *
         *           |------------------->H------------|
         *           |                                 |
         *     ------L            |------->I---------->G                             |------------------->B-----------|
         *     |     |            |                    ^                             |                                |
         *     |     |            |                    |                             |                                ↓
         *     |     |------------>K                   |                             |             ------>C---------->A
         *     |                  |                    |        |------------------->F             |                  ^      [group-1]
         *     |                  |                    |        |            ------->F             |                  |
         *     |                  ------->J-------------        |           |        |------------>E                  |
         *     |                                                |           |                      |                  |
         *     |                                                |           |                      |                  |
         *     |                                                |           |                      ------->D-----------
         *     |-------------------------------------------->Proxy F"       |
         *                                                      |           |       |------------------->N------------|
         *                                                      |           |       |                                 |
         *                                                      |           |       |                                 ↓
         *                                                      |           --------R            ------->O----------->M
         *                                                      |------------------>R            |                    ^      [group-3]
         *                                                                          |            |                    |
         *                                                                          |----------->Q                    |
         *                                                                                       |                    |
         *                                                                                       |                    |
         *                                                                                        ------->P-----------
         *
         */

        group1.addPostRunDependentTaskGroup(group3);

        // Check parent reassignment
        //
        Assertions.assertEquals(2, group1.parentDAGs.size());
        Assertions.assertTrue(group1.parentDAGs.contains(group3));
        Assertions.assertTrue(group1.parentDAGs.contains(group1.proxyTaskGroupWrapper.taskGroup()));
        Assertions.assertEquals(1, group1.proxyTaskGroupWrapper.taskGroup().parentDAGs.size());
        Assertions.assertTrue(group1.proxyTaskGroupWrapper.taskGroup().parentDAGs.contains(group2));

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for group-1
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F", "proxy-F", "L"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F", "proxy-F", "L"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F", "L"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F", "L"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F", "proxy-F", "L"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{"proxy-F", "L"}));
        // NotSeen entries for nodes in Group-3
        //
        shouldNotSee.put("M", new HashSet<String>());
        shouldNotSee.get("M").addAll(Arrays.asList(new String[]{"N", "O", "P", "Q", "R", "proxy-F", "L"}));

        shouldNotSee.put("N", new HashSet<String>());
        shouldNotSee.get("N").addAll(Arrays.asList(new String[]{"R", "proxy-F", "L"}));

        shouldNotSee.put("O", new HashSet<String>());
        shouldNotSee.get("O").addAll(Arrays.asList(new String[]{"Q", "R", "L"}));

        shouldNotSee.put("P", new HashSet<String>());
        shouldNotSee.get("P").addAll(Arrays.asList(new String[]{"Q", "R", "proxy-F", "L"}));

        shouldNotSee.put("Q", new HashSet<String>());
        shouldNotSee.get("Q").addAll(Arrays.asList(new String[]{"R", "proxy-F", "L"}));

        shouldNotSee.put("R", new HashSet<String>());
        shouldNotSee.get("R").addAll(Arrays.asList(new String[]{"proxy-F", "L"}));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[]{"H", "I", "J", "K", "L"}));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[]{"K", "L"}));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[]{"K", "L"}));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[]{}));
        // NotSeen entries for proxies
        shouldNotSee.put("proxy-F", new HashSet<String>());
        shouldNotSee.get("proxy-F").addAll(Arrays.asList(new String[]{"L"}));

        Set<String> seen = new HashSet<>();
        // Test invocation order for "group-2"
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

        Assertions.assertEquals(19, seen.size()); // 3 groups each with 6 nodes + one proxy (proxy-F)
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "G", "H",
            "I", "J", "K", "L",
            "M", "N", "O", "P",
            "Q", "proxy-F", "R"}));
//        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());

        // Test invocation order for "group-1 proxy"
        //
        seen.clear();
        TaskGroup group1Proxy = group1.proxyTaskGroupWrapper.taskGroup();
        group1Proxy.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group1Proxy.getNext(); entry != null; entry = group1Proxy.getNext()) {
            Assertions.assertTrue(shouldNotSee.containsKey(entry.key()));
            Assertions.assertFalse(seen.contains(entry.key()));
//            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group1Proxy.reportCompletion(entry);
        }

        Assertions.assertEquals(13, seen.size()); // 2 groups each with 6 nodes + one proxy (proxy-F)
        expectedToSee.clear();
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "M", "N",
            "O", "P", "Q", "proxy-F",
            "R"}));
    }

    @Test
    public void testParentProxyReassignmentUponProxyTaskGroupActivation() {
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
        final TaskGroup group1 = createSampleTaskGroup("A", "B",
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
        final TaskGroup group2 = createSampleTaskGroup("G", "H",
            "I", "J",
            "K", "L",
            group2Items);

        // Make group-2 as group-1's parent by adding group-1 as group-2's dependency.
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

        // Check parent
        Assertions.assertEquals(1, group1.parentDAGs.size());
        Assertions.assertTrue(group1.parentDAGs.contains(group2));

        // Prepare group-3
        //
        /**
         *
         *   |------------------->N------------|
         *   |                                 |
         *   |                                 ↓
         *   R            ------->O----------->M
         *   |            |                    ^            [group-3]
         *   |            |                    |
         *   |----------->Q                    |
         *                |                    |
         *                |                    |
         *                ------->P-------------
         */

        final LinkedList<String> group3Items = new LinkedList<>();
        final TaskGroup group3 = createSampleTaskGroup("M", "N",
            "O", "P",
            "Q", "R",
            group3Items);

        // Make group-3 (Root-R) as group-1's (Root-F) 'post-run" dependent. This activate "group-1 proxy group",
        // should do parent re-assignment i.e. the parent "group-2" of "group-1" will become parent of "group-1's proxy".
        //
        /**
         *                [group-2]
         *
         *           |------------------->H------------|
         *           |                                 |
         *     ------L            |------->I---------->G                             |------------------->B-----------|
         *     |     |            |                    ^                             |                                |
         *     |     |            |                    |                             |                                ↓
         *     |     |------------>K                   |                             |             ------>C---------->A
         *     |                  |                    |        |------------------->F             |                  ^      [group-1]
         *     |                  |                    |        |            ------->F             |                  |
         *     |                  ------->J-------------        |           |        |------------>E                  |
         *     |                                                |           |                      |                  |
         *     |                                                |           |                      |                  |
         *     |                                                |           |                      ------->D-----------
         *     |-------------------------------------------->Proxy F"       |
         *                                                      |           |       |------------------->N------------|
         *                                                      |           |       |                                 |
         *                                                      |           |       |                                 ↓
         *                                                      |           --------R            ------->O----------->M
         *                                                      |------------------>R            |                    ^      [group-3]
         *                                                                          |            |                    |
         *                                                                          |----------->Q                    |
         *                                                                                       |                    |
         *                                                                                       |                    |
         *                                                                                        ------->P-----------
         *
         */

        group1.addPostRunDependentTaskGroup(group3);

        // Check parent reassignment
        //
        Assertions.assertEquals(2, group1.parentDAGs.size());
        Assertions.assertTrue(group1.parentDAGs.contains(group3));
        Assertions.assertTrue(group1.parentDAGs.contains(group1.proxyTaskGroupWrapper.taskGroup()));
        Assertions.assertEquals(1, group1.proxyTaskGroupWrapper.taskGroup().parentDAGs.size());
        Assertions.assertTrue(group1.proxyTaskGroupWrapper.taskGroup().parentDAGs.contains(group2));


        // Prepare group-4
        //
        /**
         *
         *   |------------------->T------------|
         *   |                                 |
         *   |                                 ↓
         *   X            ------->U----------->S
         *   |            |                    ^            [group-4]
         *   |            |                    |
         *   |----------->W                    |
         *                |                    |
         *                |                    |
         *                ------->V-------------
         */

        final LinkedList<String> group4Items = new LinkedList<>();
        final TaskGroup group4 = createSampleTaskGroup("S", "T",
            "U", "V",
            "W", "X",
            group4Items);

        // Prepare group-5
        //
        /**
         *
         *   |------------------->2------------|
         *   |                                 |
         *   |                                 ↓
         *   6            ------->3----------->1
         *   |            |                    ^            [group-5]
         *   |            |                    |
         *   |----------->5                    |
         *                |                    |
         *                |                    |
         *                ------->4-------------
         */

        final LinkedList<String> group5Items = new LinkedList<>();
        final TaskGroup group5 = createSampleTaskGroup("1", "2",
            "3", "4",
            "5", "6",
            group5Items);

        // Make group-5 as group-4's 'post-run" dependent. This activates "group-4 proxy group".

        /**
         *
         *                         |------------------->2------------|
         *                         |                                 |
         *         --------------->6                                 ↓
         *         |           |---6            |------>3----------->1
         *         |           |   |            |                    ^        [group-5]
         *         |           |   |            |                    |
         *         |           |   |------------>5                   |
         *         |           |                |                    |
         *         |           |                |                    |
         *   Proxy X"          |                ------->4-------------
         *         |           |
         *         |           |        |------------------->T------------|
         *         |           |        |                                 |
         *         |           |        |                                 ↓
         *         |           |------->X            ------->U----------->S
         *         |------------------->X            |                    ^   [group-4]
         *                              |            |                    |
         *                              |------------>W                   |
         *                                            |                   |
         *                                            |                   |
         *                                            ------->V------------
         */

        group4.addPostRunDependentTaskGroup(group5);

        // Make group-4 (Root-x) as group-1's (Root-F) 'post-run" dependent.
        //
        /**
         *
         *
         *                                                                                          |------------------->2------------|
         *                                                                                          |                                 |
         *                                                                          --------------->6                                 ↓
         *                                                                          |           |---6            |------>3----------->1
         *                                                                          |           |   |            |                    ^        [group-5]
         *                                                                          |           |   |            |                    |
         *                                                                          |           |   |------------>5                   |
         *                                                                          |           |                |                    |
         *                                                                          |           |                |                    |
         *                                                      ------------->Proxy X"          |                ------->4-------------
         *                                                      |                   |           |
         *                                                      |                   |           |        |------------------->T------------|
         *                                                      |                   |           |        |                                 |
         *                                                      |                   |           |        |                                 ↓
         *                                                      |                   |           |------->X            ------->U----------->S
         *                                                      |                    ------------------->X            |                    ^   [group-4]
         *                                                      |           -----------------------------X            |                    |
         *                                                      |           |                            |------------>W                   |
         *                                                      |           |                                         |                    |
         *                                                      |           |                                         |                    |
         *                                                      |           |                                         ------->V-------------
         *                                                      |           |
         *                                                      |           |
         *                                                      |           |
         *                                                      |           |
         *                                                      |           |
         *                                                      |           |
         *                [group-2]                             |           |
         *                                                      |           |
         *           |------------------->H------------|        |           |
         *           |                                 ↓        |           |
         *     ------L            |------->I---------->G        |           |        |-------------------->B----------|
         *     |     |            |                    ^        |           |        |                                |
         *     |     |            |                    |        |           |        |                                ↓
         *     |     |------------>K                   |        |           -------->F             ------->C--------->A
         *     |                  |                    |        |------------------->F             |                  ^      [group-1]
         *     |                  |                    |        |            ------->F             |                  |
         *     |                  ------->J-------------        |           |        |------------>E                  |
         *     |                                                |           |                      |                  |
         *     |                                                |           |                      |                  |
         *     |                                                |           |                      ------->D-----------
         *     |-------------------------------------------->Proxy F"       |
         *                                                      |           |       |--------------------->N----------|
         *                                                      |           |       |                                 |
         *                                                      |           |       |                                 ↓
         *                                                      |           --------R            --------->O--------->M
         *                                                      |------------------>R            |                    ^      [group-3]
         *                                                                          |            |                    |
         *                                                                          |----------->Q                    |
         *                                                                                       |                    |
         *                                                                                       |                    |
         *                                                                                        -------->P----------
         *
         */

        group1.addPostRunDependentTaskGroup(group4);

        Map<String, Set<String>> shouldNotSee = new HashMap<>();
        // NotSeen entries for nodes in Group-1
        //
        shouldNotSee.put("A", new HashSet<String>());
        shouldNotSee.get("A").addAll(Arrays.asList(new String[]{"B", "C", "D", "E", "F", "proxy-F", "L"}));

        shouldNotSee.put("B", new HashSet<String>());
        shouldNotSee.get("B").addAll(Arrays.asList(new String[]{"F", "proxy-F", "L"}));

        shouldNotSee.put("C", new HashSet<String>());
        shouldNotSee.get("C").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F", "L"}));

        shouldNotSee.put("D", new HashSet<String>());
        shouldNotSee.get("D").addAll(Arrays.asList(new String[]{"E", "F", "proxy-F", "L"}));

        shouldNotSee.put("E", new HashSet<String>());
        shouldNotSee.get("E").addAll(Arrays.asList(new String[]{"F", "proxy-F", "L"}));

        shouldNotSee.put("F", new HashSet<String>());
        shouldNotSee.get("F").addAll(Arrays.asList(new String[]{"proxy-F", "L"}));
        // NotSeen entries for nodes in Group-3
        //
        shouldNotSee.put("M", new HashSet<String>());
        shouldNotSee.get("M").addAll(Arrays.asList(new String[]{"N", "O", "P", "Q", "R", "proxy-F", "L"}));

        shouldNotSee.put("N", new HashSet<String>());
        shouldNotSee.get("N").addAll(Arrays.asList(new String[]{"R", "proxy-F", "L"}));

        shouldNotSee.put("O", new HashSet<String>());
        shouldNotSee.get("O").addAll(Arrays.asList(new String[]{"Q", "R", "L"}));

        shouldNotSee.put("P", new HashSet<String>());
        shouldNotSee.get("P").addAll(Arrays.asList(new String[]{"Q", "R", "proxy-F", "L"}));

        shouldNotSee.put("Q", new HashSet<String>());
        shouldNotSee.get("Q").addAll(Arrays.asList(new String[]{"R", "proxy-F", "L"}));

        shouldNotSee.put("R", new HashSet<String>());
        shouldNotSee.get("R").addAll(Arrays.asList(new String[]{"proxy-F", "L"}));
        // NotSeen entries for nodes in Group-4
        //
        shouldNotSee.put("S", new HashSet<String>());
        shouldNotSee.get("S").addAll(Arrays.asList(new String[]{"T", "U", "V", "W", "X", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("T", new HashSet<String>());
        shouldNotSee.get("T").addAll(Arrays.asList(new String[]{"X", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("U", new HashSet<String>());
        shouldNotSee.get("U").addAll(Arrays.asList(new String[]{"W", "X", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("V", new HashSet<String>());
        shouldNotSee.get("V").addAll(Arrays.asList(new String[]{"W", "X", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("W", new HashSet<String>());
        shouldNotSee.get("W").addAll(Arrays.asList(new String[]{"X", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("X", new HashSet<String>());
        shouldNotSee.get("X").addAll(Arrays.asList(new String[]{"proxy-X", "proxy-F", "L"}));
        // NotSeen entries for nodes in Group-5
        //
        shouldNotSee.put("1", new HashSet<String>());
        shouldNotSee.get("1").addAll(Arrays.asList(new String[]{"2", "3", "4", "5", "6", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("2", new HashSet<String>());
        shouldNotSee.get("2").addAll(Arrays.asList(new String[]{"6", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("3", new HashSet<String>());
        shouldNotSee.get("3").addAll(Arrays.asList(new String[]{"5", "6", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("4", new HashSet<String>());
        shouldNotSee.get("4").addAll(Arrays.asList(new String[]{"5", "6", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("5", new HashSet<String>());
        shouldNotSee.get("5").addAll(Arrays.asList(new String[]{"6", "proxy-X", "proxy-F", "L"}));

        shouldNotSee.put("6", new HashSet<String>());
        shouldNotSee.get("6").addAll(Arrays.asList(new String[]{"proxy-X", "proxy-F", "L"}));
        // NotSeen entries for nodes in Group-2
        //
        shouldNotSee.put("G", new HashSet<String>());
        shouldNotSee.get("G").addAll(Arrays.asList(new String[]{"H", "I", "J", "K", "L"}));

        shouldNotSee.put("H", new HashSet<String>());
        shouldNotSee.get("H").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("I", new HashSet<String>());
        shouldNotSee.get("I").addAll(Arrays.asList(new String[]{"K", "L"}));

        shouldNotSee.put("J", new HashSet<String>());
        shouldNotSee.get("J").addAll(Arrays.asList(new String[]{"K", "L"}));

        shouldNotSee.put("K", new HashSet<String>());
        shouldNotSee.get("K").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("L", new HashSet<String>());
        shouldNotSee.get("L").addAll(Arrays.asList(new String[]{}));

        // NotSeen entries for proxies
        shouldNotSee.put("proxy-F", new HashSet<String>());
        shouldNotSee.get("proxy-F").addAll(Arrays.asList(new String[]{"L"}));

        shouldNotSee.put("proxy-X", new HashSet<String>());
        shouldNotSee.get("proxy-X").addAll(Arrays.asList(new String[]{"proxy-F", "L"}));

        // Test invocation order for group-1 (which gets delegated to group-1's proxy)
        //
        //
        Set<String> seen = new HashSet<>();
        TaskGroup group1Proxy = group1.proxyTaskGroupWrapper.taskGroup();
        group1Proxy.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group1Proxy.getNext(); entry != null; entry = group1Proxy.getNext()) {
            Assertions.assertTrue(shouldNotSee.containsKey(entry.key()));
            Assertions.assertFalse(seen.contains(entry.key()));
//            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group1Proxy.reportCompletion(entry);
        }

        Assertions.assertEquals(26, seen.size()); // 4 groups each with 6 nodes + two proxy (proxy-F and proxy-X)
        Set<String> expectedToSee = new HashSet<>();
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "M", "N",
            "O", "P", "Q", "R",
            "S", "T", "U", "V",
            "W", "X", "proxy-X",
            "1", "proxy-F", "2",
            "3", "4", "5", "6"}));
//        Sets.SetView<String> diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());

        // Test invocation order for group-1 (which gets delegated to group-1's proxy).
        // This cause -> group-1, group-4 and group-5 to invoked
        //
        seen.clear();
        TaskGroup group4Proxy = group4.proxyTaskGroupWrapper.taskGroup();
        group4Proxy.prepareForEnumeration();
        for (TaskGroupEntry<TaskItem> entry = group4Proxy.getNext(); entry != null; entry = group4Proxy.getNext()) {
            Assertions.assertTrue(shouldNotSee.containsKey(entry.key()));
            Assertions.assertFalse(seen.contains(entry.key()));
//            Sets.SetView<String> common = Sets.intersection(shouldNotSee.get(entry.key()), seen);
            Set<String> common = shouldNotSee.get(entry.key());
            common.retainAll(seen);
            if (common.size() > 0) {
                Assertions.assertTrue(false, "The entries " + common + " must be emitted before " + entry.key());
            }
            seen.add(entry.key());
            group4Proxy.reportCompletion(entry);
        }
        Assertions.assertEquals(19, seen.size()); // 3 groups each with 6 nodes + one proxy

        expectedToSee.clear();
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "S", "T",
            "U", "V", "W", "X",
            "proxy-X", "1", "2",
            "3", "4", "5", "6"}));

//        diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());

        // Test invocation order for group-2.
        // This cause -> all groups to be invoked, group-1, group-3, group-4 and group-5 to invoked
        //
        seen.clear();
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

        expectedToSee.clear();
        expectedToSee.addAll(Arrays.asList(new String[]{"A", "B", "C", "D",
            "E", "F", "G", "H",
            "I", "J", "K", "L",
            "M", "N", "O", "P",
            "Q", "R", "S", "T",
            "U", "V", "W", "X",
            "proxy-X", "1", "proxy-F", "2", "3",
            "4", "5", "6"}));

//        diff = Sets.difference(seen, expectedToSee);
        seen.removeAll(expectedToSee);
        Assertions.assertEquals(0, seen.size());
    }

    @Test
    public void canHandleDependenciesAndPostRunDependentsInBeforeGroupInvoke() throws InterruptedException {
        final IndexableTaskItem itiA = new IndexableTaskItem("A") {
            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
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
        };

        /**
         *            C" ---------> C
         *            |             ^
         *            |             |
         *            |             |
         *            |-----------> B ----> A
         */

        final ArrayList<String> seen = new ArrayList<>();
        CountDownLatch down = new CountDownLatch(1);
        itiC.taskGroup()
            .invokeAsync(itiC.taskGroup().newInvocationContext())
            .subscribe(
                indexable -> seen.add(indexable.key()),
                throwable -> down.countDown(),
                () -> down.countDown());
        down.await();

        boolean b1 = seen.equals(new ArrayList<>(Arrays.asList(new String[]{"A", "C", "B", "C"})));
        boolean b2 = seen.equals(new ArrayList<>(Arrays.asList(new String[]{"C", "A", "B", "C"})));

        if (!b1 && !b2) {
            Assertions.assertTrue(false, "Emission order should be either [A, C, B, C] or [C, A, B, C] but got " + seen);
        }

        Assertions.assertEquals(beforeGroupInvokeCntB[0], 1);
        Assertions.assertEquals(beforeGroupInvokeCntC[0], 1);

        // ------ //

        final IndexableTaskItem itiD = new IndexableTaskItem("D") {
            @Override
            protected Mono<Indexable> invokeTaskAsync(TaskGroup.InvocationContext context) {
                return this.voidPublisher();
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
        };

        /**
         *  F-------->E" ---------> E
         *            |             ^
         *            |             |
         *            |             |
         *            |-----------> D
         */

        final CountDownLatch monitor = new CountDownLatch(1);
        seen.clear();
        itiF.taskGroup()
            .invokeAsync(itiC.taskGroup().newInvocationContext())
            .subscribe(indexable -> seen.add(indexable.key()),
                throwable -> monitor.countDown(),
                () -> monitor.countDown());

        monitor.await();

        b1 = seen.equals(new ArrayList<>(Arrays.asList(new String[]{"E", "D", "E", "F"})));
        Assertions.assertTrue(b1, "Emission order should be [E, D, E, F] but got " + seen);

        Assertions.assertEquals(beforeGroupInvokeCntE[0], 1);
        Assertions.assertEquals(beforeGroupInvokeCntF[0], 1);
    }

    private TaskGroup createSampleTaskGroup(String vertex1,
                                            String vertex2,
                                            String vertex3,
                                            String vertex4,
                                            String vertex5,
                                            String vertex6,
                                            List<String> verticesNames) {
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

        TaskGroup group1 = new TaskGroup(vertex1, new StringTaskItem(vertex1));
        TaskGroup group2 = new TaskGroup(vertex2, new StringTaskItem(vertex2));
        TaskGroup group3 = new TaskGroup(vertex3, new StringTaskItem(vertex3));
        TaskGroup group4 = new TaskGroup(vertex4, new StringTaskItem(vertex4));
        TaskGroup group5 = new TaskGroup(vertex5, new StringTaskItem(vertex5));
        TaskGroup group6 = new TaskGroup(vertex6, new StringTaskItem(vertex6));

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

        StringTaskItem(String name) {
            this.name = name;
        }

        @Override
        public Indexable result() {
            return this.producedValue;
        }

        @Override
        public void beforeGroupInvoke() {
            // NOP
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
