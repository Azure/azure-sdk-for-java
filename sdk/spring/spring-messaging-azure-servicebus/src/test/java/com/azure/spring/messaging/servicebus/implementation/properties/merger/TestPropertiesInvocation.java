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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPropertiesInvocation {

    private final Map<String, Object> memberVariables = new HashMap<>();
    private final Map<String, TestPropertyGroup> propertyGroups = new HashMap<>();
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
        setterMethods.forEach(set -> {
            Class<?>[] parameterTypes = set.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new RuntimeException("Found multiple parameters of the setter method:" + set.getName());
            }

            Class<?> parameterTypeClass = parameterTypes[0];

            String varName = set.getName().substring(3);
            if (ignoreMemberVariableNames.contains(varName.toLowerCase())) {
                return;
            }

            Optional<Method> getOptional =
                getterMethods.stream()
                             .filter(get -> get.getName().equals("get" + varName))
                             .findFirst();
            if (getOptional.isEmpty()) {
                throw new RuntimeException("Not found the getter method: " + varName);
            }

            Object testValue;
            if (parameterTypeClass == String.class) {
                testValue = varName + "-" + System.currentTimeMillis();
            } else if (parameterTypeClass == boolean.class || parameterTypeClass == Boolean.class) {
                testValue = true;
            } else if (parameterTypeClass == int.class || parameterTypeClass == Integer.class) {
                testValue = 123;
            } else if (parameterTypeClass == Duration.class) {
                testValue = Duration.ofSeconds(10);
            } else {
                // skip complex property
                return;
            }

            propertyGroups.put(varName, new TestPropertyGroup(set, getOptional.get(), testValue));
            memberVariables.put(varName, testValue);
        });

        Class<?> parentClass = targetClass.getSuperclass();
        if (parentClass != null) {
            extractMethods(parentClass);
        }
    }

    void invokeSetter() {
        propertyGroups.keySet()
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
                          TestPropertyGroup pair = propertyGroups.get(envVar);
                          try {
                              pair.getSetMethod().invoke(target, pair.getTestValue());
                          } catch (IllegalAccessException | InvocationTargetException e) {
                              throw new RuntimeException(e);
                          }
                      });
    }

    void assertTargetMemberVariablesValues() {
        assertFalse(targetMemberVariables.isEmpty());
        listNotExistMemberVariables();
        printNotFoundMemberVariablesInTarget();
        targetMemberVariables.keySet().forEach(envVarName -> {
            TestPropertyGroup pair = propertyGroups.get(envVarName);
            String getMethodName = pair.getGetMethod().getName();
            String memberEnv;
            if (getMethodName.startsWith("get")) {
                memberEnv = getMethodName.substring(3);
            } else if (getMethodName.startsWith("is")) {
                memberEnv = getMethodName.substring(2);
            } else {
                throw new RuntimeException("Not a common get method");
            }

            try {
                Object gotValue = pair.getGetMethod().invoke(target);
                assertEquals(targetMemberVariables.get(memberEnv), gotValue,
                    () -> "The value of member variable " + memberEnv + " mismatched.");
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void printNotFoundMemberVariablesInTarget() {
        List<String> memberVariablesNotFoundInTarget = new ArrayList<>();
        propertyGroups.keySet().forEach(envVarName -> {
            if (!targetMemberVariables.containsKey(envVarName)) {
                memberVariablesNotFoundInTarget.add(envVarName);
            }
        });
        if (memberVariablesNotFoundInTarget.isEmpty()) {
            return;
        }

        System.out.println("Below member variables are not found in target Class: \n"
            + String.join(",", memberVariablesNotFoundInTarget) + "\n");
    }

    private void listNotExistMemberVariables() {
        List<String> memberVariablesNotFoundInTarget = new ArrayList<>();
        targetMemberVariables.keySet().forEach(envVarName -> {
            if (!propertyGroups.containsKey(envVarName)) {
                memberVariablesNotFoundInTarget.add(envVarName);
            }
        });
        assertTrue(memberVariablesNotFoundInTarget.isEmpty(),
            () -> "Member variables [" + String.join(",", memberVariablesNotFoundInTarget)
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
