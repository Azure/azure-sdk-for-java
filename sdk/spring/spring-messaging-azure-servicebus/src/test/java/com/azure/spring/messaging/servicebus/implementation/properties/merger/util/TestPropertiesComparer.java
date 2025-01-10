// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.core.implementation.util.ClassUtils.isPrimitiveDefaultValue;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesUtils.BUILT_IN_MEMBER_VARIABLE_NAMES;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesUtils.IGNORED_CLASSES;

public class TestPropertiesComparer {

    public static <T, S> boolean isMergedPropertiesCorrect(T parent,
                                                    S child,
                                                    S result,
                                                    String... ignoredMemberVariableNames) {
        Set<String> ignored = Arrays.stream(ignoredMemberVariableNames)
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toSet());
        AtomicInteger mismatchedCounter = new AtomicInteger();
        return isMergedPropertiesCorrect(parent, child, result, ignored, mismatchedCounter, result.getClass());
    }

    private static <T, S> boolean isMergedPropertiesCorrect(T parent,
                                                            S child,
                                                            S result,
                                                            Set<String> ignored,
                                                            AtomicInteger counter,
                                                            Class<?> targetClass) {
        if (IGNORED_CLASSES.contains(targetClass)) {
            return true;
        }

        Arrays.stream(targetClass.getDeclaredMethods())
              .filter(TestPropertiesUtils::isGetter)
              .forEach(getter -> checkGetter(parent, child, result, ignored, counter, getter));
        Class<?> parentClass = targetClass.getSuperclass();
        if (parentClass != null) {
            isMergedPropertiesCorrect(parent, child, result, ignored, counter, parentClass);
        }
        return counter.get() == 0;
    }

    private static <T, S> void checkGetter(T parent,
                                           S child,
                                           S result,
                                           Set<String> ignored,
                                           AtomicInteger counter,
                                           Method getter) {
        String varName = getVariableNameByGetter(getter);

        String varNameLowerCase = varName.toLowerCase();
        if (ignored.contains(varNameLowerCase)) {
            return;
        }

        try {
            Object gotValue = getter.invoke(result);
            if (gotValue == null) {
                return;
            }

            Class<?> returnType = getter.getReturnType();
            if (isPrimitiveDefaultValue(returnType, gotValue)) {
                System.out.println("Found the property that has a primitive default value: "
                    + varName + "=" + gotValue);
                counter.getAndIncrement();
                return;
            }

            if (BUILT_IN_MEMBER_VARIABLE_NAMES.contains(varNameLowerCase)) {
                Object builtInParent = findBuiltInNestedMemberVariable(varNameLowerCase, parent, parent.getClass());
                Object builtInChild = findBuiltInNestedMemberVariable(varNameLowerCase, child, child.getClass());
                isMergedPropertiesCorrect(builtInParent, builtInChild, gotValue, ignored, counter, gotValue.getClass());
                return;
            }

            Boolean matched = isMatchedInOriginProperties(gotValue, getter.getName(), returnType, child, child.getClass());
            if (matched == null) {
                matched = isMatchedInOriginProperties(gotValue, getter.getName(), returnType, parent, parent.getClass());
                if (matched == null) {
                    System.out.println("Found the property that doesn't exist in child and parent "
                        + "properties: " + varName + "=" + gotValue);
                    counter.getAndIncrement();
                    return;
                }
            }
            if (!matched) {
                System.out.println("Found the property that mismatch in child and parent"
                    + "properties: " + varName + "=" + gotValue);
                counter.getAndIncrement();
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Check getter failed: " + getter.getName(), e);
        }
    }

    private static <T> Object findBuiltInNestedMemberVariable(String memberVariableName, T target,
                                                              Class<?> targetClass) {
        try {
            Field builtInMemberVariable = targetClass.getDeclaredField(memberVariableName);
            builtInMemberVariable.setAccessible(true);
            return builtInMemberVariable.get(target);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Class<?> parentClass = targetClass.getSuperclass();
            if (parentClass != null) {
                return findBuiltInNestedMemberVariable(memberVariableName, target, parentClass);
            }
        }

        throw new RuntimeException("Not found the built-in member variable: " + memberVariableName);
    }

    private static <S> Boolean isMatchedInOriginProperties(Object usedValue,
                                                           String getterMethodName,
                                                           Class<?> returnType,
                                                           S origin,
                                                           Class<?> targetClass) {
        try {
            Method getter = targetClass.getDeclaredMethod(getterMethodName);
            Object gotValue = getter.invoke(origin);
            if (gotValue == null) {
                return null;
            }

            if (isPrimitiveDefaultValue(returnType, gotValue)) {
                return null;
            }

            boolean matched = false;
            switch (returnType.getSimpleName()) {
                case "boolean", "Boolean", "Duration", "int",
                    "Integer", "long", "Long", "String" ->
                    matched = usedValue.equals(gotValue);
                case "AmqpTransportType", "CloudType", "ServiceBusEntityType",
                    "ServiceBusReceiveMode", "SubQueue", "RetryMode" ->
                    matched = usedValue == gotValue;
                default -> System.out.println("Not support the getter parameter type: " + returnType.getName());
            }
            return matched;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Class<?> parentClass = targetClass.getSuperclass();
            if (parentClass != null) {
                return isMatchedInOriginProperties(usedValue, getterMethodName, returnType, origin, parentClass);
            }
            return null;
        }
    }

    private static String getVariableNameByGetter(Method getter) {
        String varName;
        String getterName = getter.getName();
        if (getterName.contains("get")) {
            varName = getterName.substring("get".length());
        } else {
            varName = getterName.substring("is".length());
        }
        return varName;
    }
}
