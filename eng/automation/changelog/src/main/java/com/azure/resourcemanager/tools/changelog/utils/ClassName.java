// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.tools.changelog.utils;

import japicmp.model.JApiClass;

public class ClassName {
    public static String namespace(JApiClass apiClass) {
        return namespace(apiClass.getFullyQualifiedName());
    }

    public static String namespace(String name) {
        return name.replaceAll("\\.[^.]+$", "");
    }

    public static String name(JApiClass apiClass) {
        return name(apiClass.getFullyQualifiedName());
    }

    public static String name(String name) {
        return name.replaceAll("[^.]+\\.", "").replaceAll("[^$]+\\$", "");
    }

    public static String parentName(JApiClass apiClass) {
        return parentName(apiClass.getFullyQualifiedName());
    }

    public static String parentName(String name) {
        return name.replaceAll("[^.]+\\.", "").replaceAll("\\$[^$]+$", "");
    }
}
