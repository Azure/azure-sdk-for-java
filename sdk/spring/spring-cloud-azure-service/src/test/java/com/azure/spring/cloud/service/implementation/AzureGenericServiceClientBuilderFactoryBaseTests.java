// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation;

import com.azure.spring.cloud.core.implementation.factory.AzureServiceClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.AzureSdkProperties;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.core.implementation.util.ClassUtils.isPrimitive;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AzureGenericServiceClientBuilderFactoryBaseTests<P extends AzureSdkProperties,
    F extends AzureServiceClientBuilderFactory<?>> {

    protected abstract P createMinimalServiceProperties();

    protected abstract F createClientBuilderFactoryWithMockBuilder(P properties);

    private static final Set<Class<?>> IGNORED_CLASS = Set.of(Object.class, Class.class, Enum.class, String.class,
        Boolean.class, Integer.class, Long.class, Duration.class);
    private static final Set<String> IGNORED_METHOD_NAMES =
        Arrays.stream(Object.class.getMethods()).map(Method::getName).collect(Collectors.toSet());
    private static final Set<Class<?>> BUILDER_IGNORED_PARAMETER_TYPES = Set.of(Consumer.class);
    private static final Set<String> BUILDER_IGNORED_METHOD_NAME_PREFIX = Set.of("build", "process");
    private static final Function<String, String> EXTRACT_METHOD_NAME = methodName -> {
        if (methodName.startsWith("is")) {
            return methodName.substring("is".length());
        } else {
            return methodName.substring("set".length());
        }
    };

    public static Set<String> listSupportedProperties(Class<?> propertiesClass) {
        Set<Method> classMethodSet = new HashSet<>();
        listClassMethods(classMethodSet, method -> Set.of(method.getReturnType()), propertiesClass,
            method -> method.getName().startsWith("is") || method.getName().startsWith("get"));
        return classMethodSet.stream()
                             .map(Method::getName)
                             .map(EXTRACT_METHOD_NAME)
                             .collect(Collectors.toSet());
    }

    public static Set<String> listBuilderProperties(Class<?> builderClass) {
        Set<Method> classMethodSet = new HashSet<>();
        listClassMethods(classMethodSet,
            method -> Arrays.stream(method.getParameters())
                            .map(Parameter::getType)
                            .filter(type -> !BUILDER_IGNORED_PARAMETER_TYPES.contains(type))
                            .collect(Collectors.toSet()),
            builderClass, method -> BUILDER_IGNORED_METHOD_NAME_PREFIX.stream()
                                                                      .noneMatch(prefix -> method.getName().startsWith(prefix)));
        return classMethodSet.stream().map(Method::getName).collect(Collectors.toSet());
    }

    public static void listClassMethods(Set<Method> classMethodSet, Function<Method, Set<Class<?>>> iterationType,
                                 Class<?> propertiesClass, Predicate<Method> filter) {
        if (isPrimitive(propertiesClass) || propertiesClass.isEnum() || IGNORED_CLASS.contains(propertiesClass)) {
            return;
        }

        Method[] propertiesMethods = propertiesClass.getMethods();
        Set<Method> methodSet = Arrays.stream(propertiesMethods)
                                      .filter(filter)
                                      .filter(method -> !IGNORED_METHOD_NAMES.contains(method.getName()))
                                      .collect(Collectors.toSet());
        if (iterationType != null) {
            methodSet.forEach(method -> {
                for (Class<?> subClass : iterationType.apply(method)) {
                    listClassMethods(classMethodSet, iterationType, subClass, filter);
                }
            });
        }
        System.out.println("[" + propertiesClass.getSimpleName() + "] class property names: \n"
            + methodSet.stream().map(Method::getName).map(EXTRACT_METHOD_NAME).collect(Collectors.toSet()));
        classMethodSet.addAll(methodSet);

        Class<?> superclass = propertiesClass.getSuperclass();
        if (superclass != null) {
            listClassMethods(classMethodSet, iterationType, superclass, filter);
        }
    }

    protected F factoryWithMinimalSettings() {
        P properties = createMinimalServiceProperties();
        return createClientBuilderFactoryWithMockBuilder(properties);
    }

    protected F factoryWithClientSecretTokenCredentialConfigured(P properties) {
        P credentialProperties = createClientSecretTokenCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    protected F factoryWithClientCertificateTokenCredentialConfigured(P properties) {
        P credentialProperties = createClientCertificateTokenCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    protected F factoryWithUsernamePasswordTokenCredentialConfigured(P properties) {
        P credentialProperties = createUsernamePasswordTokenCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    protected F factoryWithManagedIdentityTokenCredentialConfigured(P properties) {
        P credentialProperties = createManagedIdentityCredentialAwareServiceProperties(properties);
        return createClientBuilderFactoryWithMockBuilder(credentialProperties);
    }

    private P createClientSecretTokenCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createClientCertificateTokenCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createUsernamePasswordTokenCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setClientId("test-client");
        properties.getCredential().setUsername("test-username");
        properties.getCredential().setPassword("test-password");
        properties.getProfile().setTenantId("test-tenant");
        return properties;
    }

    private P createManagedIdentityCredentialAwareServiceProperties(P properties) {
        properties.getCredential().setManagedIdentityEnabled(true);
        return properties;
    }

    @Test
    void supportSdkBuilderAllProperties() {
        verifyNoUnsupportedPropertiesFromBuilderClass();
    }

    public PropertiesIntegrityParameters getParametersForPropertiesIntegrity() {
        // override by sub builder factory class
        return null;
    }

    public void verifyNoUnsupportedPropertiesFromBuilderClass() {
        PropertiesIntegrityParameters parameters = getParametersForPropertiesIntegrity();
        if (parameters == null) {
            return;
        }

        Set<String> supportedProperties = listSupportedProperties(parameters.propertiesClass());
        Set<String> builderProperties = listBuilderProperties(parameters.sdkBinderClass());
        Set<String> lowCaseSupportedProperties =
            supportedProperties.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Map<String, String> namingFromBinderToProperties = parameters.propertyNameMappingForBinder();
        Set<String> unsupportedPropertyNames =
            builderProperties.stream()
                             .map(String::toLowerCase)
                             .filter(builderPropertyName -> {
                                 String targetName = builderPropertyName.toLowerCase();
                                 return (!lowCaseSupportedProperties.contains(targetName) && !namingFromBinderToProperties.containsKey(targetName))
                                     || (namingFromBinderToProperties.containsKey(targetName) && !lowCaseSupportedProperties.contains(namingFromBinderToProperties.get(targetName)));
                             })
                             .collect(Collectors.toSet());
        System.out.println("Properties class supported property names: \n" + supportedProperties);
        System.out.println("Builder class owned property names: \n" + builderProperties);
        System.out.println("Unsupported property names: \n" + unsupportedPropertyNames);
        assertTrue(unsupportedPropertyNames.isEmpty());
    }

    public record PropertiesIntegrityParameters(Class<?> propertiesClass, Class<?> sdkBinderClass,
                                                Map<String, String> propertyNameMappingForBinder) {

    }
}
