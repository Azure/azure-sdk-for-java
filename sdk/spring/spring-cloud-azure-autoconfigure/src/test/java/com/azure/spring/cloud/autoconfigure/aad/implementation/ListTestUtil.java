// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import java.util.List;

class ListTestUtil {

    static boolean hasItemOfClass(List<?> list, Class<?> clazz) {
        return list.stream()
                .anyMatch(item -> item.getClass().equals(clazz));
    }

}
