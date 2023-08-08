// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

public class ClassNameResolverPredicate {

    boolean resolve(String fullyQualifiedClassName) {
        try {
            if (fullyQualifiedClassName == null) {
                return false;
            }

            Class.forName(fullyQualifiedClassName);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
