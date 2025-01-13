// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger.util;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesUtils.GETTER_METHOD;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesUtils.IGNORED_CLASSES;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesUtils.SETTER_METHOD;
import static com.azure.spring.messaging.servicebus.implementation.properties.merger.util.TestPropertiesUtils.NO_SETTER_PROPERTIES_CLASSES;

public class TestPropertiesValueInjectHelper {

    private static final Random RANDOM = new Random();

    public static <T> void injectPseudoPropertyValues(T target,
                                               String... ignoredMemberVariableNames) {
        injectPseudoPropertyValues(target, null, ignoredMemberVariableNames);
    }

    public static <T> void injectPseudoPropertyValues(T target,
                                               List<String> highPriorityVariables,
                                               String... ignoredMemberVariableNames) {
        List<String> priorities = Optional.ofNullable(highPriorityVariables).orElseGet(Collections::emptyList);
        Set<String> ignored = Arrays.stream(ignoredMemberVariableNames)
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toSet());
        doInjectPseudoPropertyValues(target, target.getClass(), priorities, ignored);
    }

    private static <T> void doInjectPseudoPropertyValues(T target,
                                                         Class<?> targetClass,
                                                         List<String> priorities,
                                                         Set<String> ignored) {
        if (IGNORED_CLASSES.contains(targetClass) || targetClass.isInterface()) {
            return;
        }

        Map<String, List<Method>> methods = Arrays.stream(targetClass.getDeclaredMethods())
                                                  .collect(Collectors.groupingBy(TestPropertiesUtils::groupMethodName));
        if (!methods.isEmpty()) {
            Optional<List<Method>> getters = Optional.ofNullable(methods.get(GETTER_METHOD));
            getters.stream()
                   .flatMap(Collection::stream)
                   .sorted(new HighPriorityMethodComparator(priorities))
                   .forEach(getter -> invokeSetter(target, priorities, ignored, methods.get(SETTER_METHOD), getter));
        }
        Class<?> parentClass = targetClass.getSuperclass();
        if (parentClass != null) {
            doInjectPseudoPropertyValues(target, parentClass, priorities, ignored);
        }
    }

    private static <T> void invokeSetter(T target,
                                         List<String> priorities,
                                         Set<String> ignored,
                                         List<Method> setters,
                                         Method getter) {
        String varName;
        if (getter.getName().contains("get")) {
            varName = getter.getName().substring("get".length());
        } else {
            varName = getter.getName().substring("is".length());
        }

        if (ignored.contains(varName.toLowerCase())) {
            return;
        }

        Optional<Method> optionalSetter = Optional.ofNullable(setters)
                                                  .stream().flatMap(Collection::stream)
                                                  .filter(setter -> setter.getName().equals("set" + varName))
                                                  .findFirst();
        if (optionalSetter.isEmpty()) {
            Class<?> getterReturnType = getter.getReturnType();
            if (getterReturnType.isInterface()) {
                return;
            }

            if (Arrays.stream(NO_SETTER_PROPERTIES_CLASSES).anyMatch(cls -> cls == getterReturnType)) {
                Object subTarget;
                try {
                    subTarget = getter.invoke(target);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                if (subTarget != null) {
                    doInjectPseudoPropertyValues(subTarget, subTarget.getClass(), priorities, ignored);
                }
            } else {
                System.out.println("Return type not support: " + getterReturnType.getName());
            }
            return;
        }

        Method setter = optionalSetter.get();
        Class<?> parameterType = setter.getParameterTypes()[0];

        Object testValue;
        int randomValue = RANDOM.nextInt(100, 1000);
        switch (parameterType.getSimpleName()) {
            case "AmqpTransportType" -> testValue = AmqpTransportType.AMQP;
            case "boolean" -> testValue = true;
            case "Boolean" -> testValue = randomValue % 2 == 0;
            case "CloudType" -> testValue = AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
            case "Duration" -> testValue = Duration.ofSeconds(randomValue);
            case "int", "Integer", "long" -> testValue = randomValue;
            case "Long" -> testValue = RANDOM.nextLong(100, 1000);
            case "RetryMode" -> testValue = RetryOptionsProvider.RetryMode.EXPONENTIAL;
            case "ServiceBusEntityType" -> testValue = ServiceBusEntityType.QUEUE;
            case "ServiceBusReceiveMode" -> testValue = ServiceBusReceiveMode.RECEIVE_AND_DELETE;
            case "SubQueue" -> testValue = SubQueue.DEAD_LETTER_QUEUE;
            case "String" -> testValue = varName + "-" + randomValue;
            default -> {
                System.out.println("Not support the setter parameter type: " + parameterType.getName());
                return;
            }
        }

        try {
            setter.invoke(target, testValue);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Member variable " + varName, e);
        }
    }

    static class HighPriorityMethodComparator implements Comparator<Method> {

        private final List<String> priorities;

        HighPriorityMethodComparator(List<String> priorities) {
            this.priorities = priorities;
        }

        @Override
        public int compare(Method o1, Method o2) {
            String o1Value = o1.getName().toLowerCase();
            String o2Value = o2.getName().toLowerCase();
            if (priorities.contains(o1Value) && !priorities.contains(o2Value)) {
                return 1;
            }

            if (!priorities.contains(o1Value) && priorities.contains(o2Value)) {
                return -1;
            }

            if (priorities.contains(o1Value) && priorities.contains(o2Value)) {
                return priorities.indexOf(o1Value) - priorities.indexOf(o2Value);
            }

            return o1Value.compareTo(o2Value);
        }
    }

}
