// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.util;

import org.springframework.boot.kafka.autoconfigure.KafkaProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class TestCompatibilityUtils {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> invokeBuildKafkaProperties(KafkaProperties kafkaProperties, String buildMethodName) {
        try {
            try {
                Method buildPropertiesMethod = KafkaProperties.class.getDeclaredMethod(buildMethodName,
                    Class.forName("org.springframework.boot.ssl.SslBundles"));
                //noinspection unchecked
                return (Map<String, Object>) buildPropertiesMethod.invoke(kafkaProperties, (Object) null);
            } catch (NoSuchMethodException | ClassNotFoundException ignored) {

            }
            // The following logic is to be compatible with Spring Boot 3.0 and 3.1.
            try {
                //noinspection unchecked
                return (Map<String, Object>) KafkaProperties.class.getDeclaredMethod(buildMethodName).invoke(kafkaProperties);
            } catch (NoSuchMethodException ignored) {

            }
        } catch (InvocationTargetException | IllegalAccessException ignored) {

        }
        throw new IllegalStateException("Failed to call " + buildMethodName + " method of KafkaProperties.");
    }
}
