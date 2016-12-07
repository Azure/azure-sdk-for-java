package com.microsoft.azure.management.resources.fluentcore.dag;

class ItemHolder extends DAGNode<String, ItemHolder> {
    ItemHolder(String taskId, String taskItem) {
        super(taskId,taskItem);
    }
}