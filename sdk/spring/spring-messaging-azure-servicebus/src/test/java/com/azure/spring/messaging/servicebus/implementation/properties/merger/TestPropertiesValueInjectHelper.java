// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

class TestPropertiesValueInjectHelper {

    private static final Random RANDOM = new Random();

    static <T> void injectPseudoPropertyValues(T target, String... ignoredVariableNames) {
        Set<String> ignoredMemberVariableNames = new HashSet<>(Arrays.asList(ignoredVariableNames));
        Class<?> targetClass = target.getClass();

        List<Method> setters = Arrays.stream(targetClass.getDeclaredMethods()).filter(TestPropertiesValueInjectHelper::isSetter).toList();
        List<Method> getters = Arrays.stream(targetClass.getDeclaredMethods()).filter(TestPropertiesValueInjectHelper::isGetter).toList();

        setters.forEach(setter -> {
            String varName = setter.getName().substring("set".length());
            if (ignoredMemberVariableNames.contains(varName.toLowerCase())) {
                return;
            }

            Class<?>[] parameterTypes = setter.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException("Found multiple parameters of the setter method:" + setter.getName());
            }
            Class<?> parameterType = parameterTypes[0];

            Optional<Method> optionalGetter =
                getters.stream()
                    .filter(getter -> isMatchedGetter(getter, varName))
                    .findFirst();
            if (optionalGetter.isEmpty()) {
                throw new RuntimeException("Not found the getter method: " + varName);
            }

            Object testValue;
            int randomValue = RANDOM.nextInt(100, 1000);
            if (parameterType == String.class) {
                testValue = varName + "-" + randomValue;
            } else if (parameterType == boolean.class || parameterType == Boolean.class) {
                testValue = randomValue % 2 == 0;
            } else if (parameterType == int.class || parameterType == Integer.class) {
                testValue = randomValue;
            } else if (parameterType == Duration.class) {
                testValue = Duration.ofSeconds(randomValue);
            } else {
                // skip complex property
                return;
            }

            try {
                setter.invoke(target, testValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        Class<?> parentClass = targetClass.getSuperclass();
        if (parentClass != null) {
            injectPseudoPropertyValues(parentClass, ignoredVariableNames);
        }
    }

    private static boolean isMatchedGetter(Method getter, String varName) {
        return getter.getName().equals("get" + varName) || getter.getName().equals("is" + varName);
    }

    private static boolean isGetter(Method method) {
        if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }

        return !void.class.equals(method.getReturnType());
    }

    private static boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }

        return void.class.equals(method.getReturnType());
    }
}
