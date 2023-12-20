// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.test.models;

public class ClassB {

    public void methodB() {
        // calls through to ClassA.methodA(), which is annotated
        new ClassA().methodA();
    }
}
