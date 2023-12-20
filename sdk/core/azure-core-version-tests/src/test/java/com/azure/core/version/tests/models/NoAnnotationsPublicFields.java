// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests.models;

public final class NoAnnotationsPublicFields {
    final int age;
    final String name;

    public NoAnnotationsPublicFields(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public void notAGetter() {
    }

    public int alsoNotAGetter(String parameter) {
        return 0;
    }
}
