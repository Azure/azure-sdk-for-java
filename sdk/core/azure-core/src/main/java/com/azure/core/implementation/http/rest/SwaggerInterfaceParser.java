// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.implementation.ImplUtils.MAX_CACHE_SIZE;

/**
 * The type responsible for creating individual Swagger interface method parsers from a Swagger interface.
 */
public final class SwaggerInterfaceParser {
    private static final Map<Class<?>, SwaggerInterfaceParser> INTERFACE_PARSERS = new HashMap<>();
    private static final Object INTERFACE_PARSERS_WRITE_LOCK = new Object();

    private final String host;
    private final String serviceName;
    private final MethodAndSwaggerMethodParser[] methodParsers;

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface name.
     *
     * @param swaggerInterface The interface that will be parsed.
     * @return The {@link SwaggerInterfaceParser} for the passed interface.
     */
    public static SwaggerInterfaceParser getInstance(Class<?> swaggerInterface) {
        SwaggerInterfaceParser parser = INTERFACE_PARSERS.get(swaggerInterface);
        if (parser != null) {
            return parser;
        }

        synchronized (INTERFACE_PARSERS_WRITE_LOCK) {
            if (INTERFACE_PARSERS.size() >= MAX_CACHE_SIZE) {
                INTERFACE_PARSERS.clear();
            }

            return INTERFACE_PARSERS.computeIfAbsent(swaggerInterface, SwaggerInterfaceParser::new);
        }
    }

    SwaggerInterfaceParser(Class<?> swaggerInterface) {
        final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
        if (hostAnnotation != null && !hostAnnotation.value().isEmpty()) {
            this.host = hostAnnotation.value();
        } else {
            throw new MissingRequiredAnnotationException(Host.class, swaggerInterface);
        }

        ServiceInterface serviceAnnotation = swaggerInterface.getAnnotation(ServiceInterface.class);
        if (serviceAnnotation != null && !serviceAnnotation.name().isEmpty()) {
            serviceName = serviceAnnotation.name();
        } else {
            throw new MissingRequiredAnnotationException(ServiceInterface.class, swaggerInterface);
        }

        Method[] methods = swaggerInterface.getDeclaredMethods();
        this.methodParsers = new MethodAndSwaggerMethodParser[methods.length];
        for (int i = 0; i < methods.length; i++) {
            methodParsers[i] = new MethodAndSwaggerMethodParser(methods[i], host, serviceName);
        }
        Arrays.sort(methodParsers);
    }

    /**
     * Get the method parser that is associated with the provided swaggerMethod. The method parser can be used to get
     * details about the Swagger REST API call.
     *
     * @param swaggerMethod the method to generate a parser for
     * @return the SwaggerMethodParser associated with the provided swaggerMethod
     */
    public SwaggerMethodParser getMethodParser(Method swaggerMethod) {
        int index = Arrays.binarySearch(methodParsers, new MethodAndSwaggerMethodParser(swaggerMethod));
        return methodParsers[index].methodParser;
    }

    /**
     * Get the desired host that the provided Swagger interface will target with its REST API calls. This value is
     * retrieved from the @Host annotation placed on the Swagger interface.
     *
     * @return The value of the @Host annotation.
     */
    public String getHost() {
        return host;
    }

    public String getServiceName() {
        return serviceName;
    }

    private static final class MethodAndSwaggerMethodParser implements Comparable<MethodAndSwaggerMethodParser> {
        private final SwaggerMethodParser methodParser;
        private final String methodString;

        private MethodAndSwaggerMethodParser(Method method) {
            this.methodParser = null;
            this.methodString = method.toString();
        }

        private MethodAndSwaggerMethodParser(Method method, String rawHost, String serviceName) {
            this.methodParser = new SwaggerMethodParser(rawHost, serviceName, method);
            this.methodString = method.toString();
        }

        @Override
        public int hashCode() {
            return methodString.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodAndSwaggerMethodParser)) {
                return false;
            }

            return Objects.equals(methodString, ((MethodAndSwaggerMethodParser) obj).methodString);
        }

        @Override
        public int compareTo(MethodAndSwaggerMethodParser o) {
            return methodString.compareTo(o.methodString);
        }
    }
}
