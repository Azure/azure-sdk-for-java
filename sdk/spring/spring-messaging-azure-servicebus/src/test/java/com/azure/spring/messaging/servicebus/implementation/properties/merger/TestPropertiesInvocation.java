// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPropertiesInvocation {

    private final Map<String, Object> memberVariables = new HashMap<>();
    private final Map<String, TestGetterSetterAndValue> setterAndValues = new HashMap<>();
    private final Map<String, Object> targetMemberVariables = new HashMap<>();
    private final Object target;
    private final Set<String> ignoreMemberVariableNames = new HashSet<>();
    private final List<String> lowPriorityMemberVariableNames = new ArrayList<>();

    TestPropertiesInvocation(Object target) {
        this.target = target;
    }

    void extractMethods() {
        extractMethods(target.getClass());
    }

    void extractMethodsAndInvokeSetters() {
        extractMethods(target.getClass());
        invokeSetter();
    }

    private void extractMethods(Class<?> targetClass) {
        Method[] declaredMethods = targetClass.getDeclaredMethods();
        List<Method> setterMethods = Arrays.stream(declaredMethods).filter(this::isSetter).toList();
        List<Method> getterMethods = Arrays.stream(declaredMethods).filter(this::isGetter).toList();
        Random random = new Random();
        setterMethods.forEach(setter -> {
            Class<?>[] parameterTypes = setter.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException("Found multiple parameters of the setter method:" + setter.getName());
            }

            Class<?> parameterType = parameterTypes[0];

            String varName = setter.getName().substring("set".length());
            if (ignoreMemberVariableNames.contains(varName.toLowerCase())) {
                return;
            }

            Optional<Method> optionalGetter =
                getterMethods.stream()
                             .filter(getter -> isMatchedGetter(getter, varName))
                             .findFirst();
            if (optionalGetter.isEmpty()) {
                throw new RuntimeException("Not found the getter method: " + varName);
            }

            Object testValue;
            int randomValue = random.nextInt(100, 1000);
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

            setterAndValues.put(varName, new TestGetterSetterAndValue(optionalGetter.get(), setter, testValue));
            memberVariables.put(varName, testValue);
        });

        Class<?> parentClass = targetClass.getSuperclass();
        if (parentClass != null) {
            extractMethods(parentClass);
        }
    }

    private static boolean isMatchedGetter(Method getter, String varName) {
        return getter.getName().equals("get" + varName) || getter.getName().equals("is" + varName);
    }

    void invokeSetter() {
        setterAndValues.keySet()
                       .stream()
                       .sorted((String o1, String o2) -> {
                          String o1Value = o1.toLowerCase();
                          String o2Value = o2.toLowerCase();
                          if (lowPriorityMemberVariableNames.contains(o1Value) && !lowPriorityMemberVariableNames.contains(o2Value)) {
                              return 1;
                          }

                          if (!lowPriorityMemberVariableNames.contains(o1Value) && lowPriorityMemberVariableNames.contains(o2Value)) {
                              return -1;
                          }

                          if (lowPriorityMemberVariableNames.contains(o1Value) && lowPriorityMemberVariableNames.contains(o2Value)) {
                              return lowPriorityMemberVariableNames.indexOf(o1Value) - lowPriorityMemberVariableNames.indexOf(o2Value);
                          }

                          return o1Value.compareTo(o2Value);
                      })
                       .forEach(envVar -> {
                          TestGetterSetterAndValue property = setterAndValues.get(envVar);
                          try {
                              property.getSetMethod().invoke(target, property.getTestValue());
                          } catch (IllegalAccessException | InvocationTargetException e) {
                              throw new RuntimeException(e);
                          }
                      });
    }

    void assertTargetMemberVariablesValues() {
        assertAllTargetMemberVariablesInPropertyGroups();
        targetMemberVariables.keySet().forEach(envVarName -> {
            TestGetterSetterAndValue property = setterAndValues.get(envVarName);
            String getMethodName = property.getGetMethod().getName();
            String memberVariable;
            if (getMethodName.startsWith("get")) {
                memberVariable = getMethodName.substring("get".length());
            } else if (getMethodName.startsWith("is")) {
                memberVariable = getMethodName.substring("is".length());
            } else {
                throw new RuntimeException("Not a common getter method");
            }

            try {
                Object gotValue = property.getGetMethod().invoke(target);
                assertEquals(targetMemberVariables.get(memberVariable), gotValue,
                    () -> "The value of member variable " + memberVariable + " mismatched.");
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void assertAllTargetMemberVariablesInPropertyGroups() {
        assertFalse(targetMemberVariables.isEmpty());
        List<String> notFoundInTargetMemberVariables = new ArrayList<>();
        setterAndValues.keySet().forEach(envVarName -> {
            if (!targetMemberVariables.containsKey(envVarName)) {
                notFoundInTargetMemberVariables.add(envVarName);
            }
        });
        if (!notFoundInTargetMemberVariables.isEmpty()) {
            System.out.println("Below member variables are not found in target Class: \n"
                + String.join(",", notFoundInTargetMemberVariables) + "\n");
        }

        List<String> notFoundInCurrentMemberVariables = new ArrayList<>();
        targetMemberVariables.keySet().forEach(envVarName -> {
            if (!setterAndValues.containsKey(envVarName)) {
                notFoundInCurrentMemberVariables.add(envVarName);
            }
        });
        assertTrue(notFoundInCurrentMemberVariables.isEmpty(),
            () -> "Member variables [" + String.join(",", notFoundInCurrentMemberVariables)
                + "] not found in Class " + target.getClass().getSimpleName());
    }

    private boolean isGetter(Method method) {
        if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
            return false;
        }
        if (method.getParameterCount() != 0) {
            return false;
        }

        return !void.class.equals(method.getReturnType());
    }

    private boolean isSetter(Method method) {
        if (!method.getName().startsWith("set")) {
            return false;
        }
        if (method.getParameterCount() != 1) {
            return false;
        }

        return void.class.equals(method.getReturnType());
    }

    Map<String, Object> getMemberVariables() {
        return memberVariables;
    }

    void setTargetMemberVariables(Map<String, Object> targetMemberVariables) {
        this.targetMemberVariables.putAll(targetMemberVariables);
    }

    /**
     * Add to skip the member variables invocation.
     *
     * @param ignores the ignore member variables
     */
    void addIgnoreMemberVariableNames(String... ignores) {
        this.ignoreMemberVariableNames.addAll(Arrays.stream(ignores)
                                                    .map(String::toLowerCase)
                                                    .collect(Collectors.toSet()));
    }

    /**
     * Add to low priority for the member variables, which means the correspond setter methods will be invoked later.
     *
     * @param lowPriorities the low priority member variables
     */
    void addLowPriorityMemberVariableNames(String... lowPriorities) {
        this.lowPriorityMemberVariableNames.addAll(Arrays.stream(lowPriorities)
                                                         .map(String::toLowerCase)
                                                         .toList());
    }
}
