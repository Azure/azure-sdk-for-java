// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.utils;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

public class FaceDisplayNameGenerator implements DisplayNameGenerator {
    private static final DisplayNameGenerator STANDARD_DISPLAY_NAME_GENERATOR =
        DisplayNameGenerator.getDisplayNameGenerator(Standard.class);

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        return STANDARD_DISPLAY_NAME_GENERATOR.generateDisplayNameForClass(testClass);
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return STANDARD_DISPLAY_NAME_GENERATOR.generateDisplayNameForNestedClass(nestedClass);
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        return STANDARD_DISPLAY_NAME_GENERATOR.generateDisplayNameForMethod(testClass, testMethod);
    }
}
