// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import java.util.List;

public class Lists {

    public static <V> V firstOrDefault(List<V> list, V defaultValue) {
        return list.isEmpty() ? defaultValue : list.get(0);
    }
}
