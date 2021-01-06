// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

class Edge<T extends Node<T>> {
    T sourceNode;
    T targetNode;

    Edge(T sourceNode, T targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }


}
