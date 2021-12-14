// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A collection of "Post Run" tasks.
 */
public final class PostRunTaskCollection {
    private final List<IndexableTaskItem> collection;
    private final TaskGroup dependsOnTaskGroup;

    /**
     * Creates PostRunTaskCollection.
     *
     * @param dependsOnTaskGroup the task group in which "Post Run" tasks in the collection depends on
     */
    public PostRunTaskCollection(final TaskGroup dependsOnTaskGroup) {
        Objects.requireNonNull(dependsOnTaskGroup);
        this.collection = new ArrayList<>();
        this.dependsOnTaskGroup = dependsOnTaskGroup;
    }

    /**
     * Adds a "Post Run" task to the collection.
     *
     * @param taskItem the "Post Run" task
     */
    public void add(final IndexableTaskItem taskItem) {
        this.dependsOnTaskGroup.addPostRunDependentTaskGroup(taskItem.taskGroup());
        this.collection.add(taskItem);
    }

    /**
     * Adds a "Post Run" task to the collection.
     *
     * @param taskItem the "Post Run" task
     */
    public void add(final FunctionalTaskItem taskItem) {
        add(IndexableTaskItem.create(taskItem));
    }

    /**
     * Clears the result produced by all "Post Run" tasks in the collection.
     */
    public void clear() {
        for (IndexableTaskItem item : collection) {
            item.clear();
        }
    }
}
