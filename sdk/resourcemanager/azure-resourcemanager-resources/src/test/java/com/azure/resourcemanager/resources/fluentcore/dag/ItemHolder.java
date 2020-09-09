// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

class ItemHolder extends DAGNode<String, ItemHolder> {
    ItemHolder(String taskId, String taskItem) {
        super(taskId, taskItem);
    }
}
