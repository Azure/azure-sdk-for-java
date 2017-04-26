/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

class ItemHolder extends DAGNode<String, ItemHolder> {
    ItemHolder(String taskId, String taskItem) {
        super(taskId,taskItem);
    }
}