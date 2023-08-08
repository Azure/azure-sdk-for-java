// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests.models;

public final class NoAnnotationsGetters {
    private final int age;
    private final String name;

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public NoAnnotationsGetters(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public void notAGetter() {
    }

    public int alsoNotAGetter(String parameter) {
        return 0;
    }
}
