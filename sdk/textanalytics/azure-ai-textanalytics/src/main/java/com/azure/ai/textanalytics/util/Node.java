// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import java.util.List;

class Node<T> {
    T value;
    List<T> children;
    public Node(T value) {
        this.value = value;
    }


}
