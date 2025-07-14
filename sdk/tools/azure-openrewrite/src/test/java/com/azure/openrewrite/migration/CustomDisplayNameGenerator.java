package com.azure.openrewrite.migration;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class CustomDisplayNameGenerator extends DisplayNameGenerator.Standard {

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        Parameter[] parameters = testMethod.getParameters();
        String param = "test";
        String shortParam = param.length() > 10 ? param.substring(0, 10) + "..." : param;
        return testMethod.getName() + " - " + shortParam;
    }
}
