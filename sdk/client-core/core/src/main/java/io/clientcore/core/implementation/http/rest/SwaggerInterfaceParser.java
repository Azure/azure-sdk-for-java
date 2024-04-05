// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.annotation.ServiceInterface;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type responsible for creating individual Swagger interface method parsers from a Swagger interface.
 */
public final class SwaggerInterfaceParser {
    private static final Map<Class<?>, SwaggerInterfaceParser> INTERFACE_PARSERS = new ConcurrentHashMap<>();

    private final String host;
    private final String serviceName;
    private final Map<Method, SwaggerMethodParser> methodParsers = new ConcurrentHashMap<>();

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface name.
     *
     * @param swaggerInterface The interface that will be parsed.
     *
     * @return The {@link SwaggerInterfaceParser} for the passed interface.
     */
    public static SwaggerInterfaceParser getInstance(Class<?> swaggerInterface) {
        //if (INTERFACE_PARSERS.size() >= MAX_CACHE_SIZE) {
        //    INTERFACE_PARSERS.clear();
        //}

        return INTERFACE_PARSERS.computeIfAbsent(swaggerInterface, SwaggerInterfaceParser::new);
    }

    SwaggerInterfaceParser(Class<?> swaggerInterface) {
        ServiceInterface serviceAnnotation = swaggerInterface.getAnnotation(ServiceInterface.class);

        if (serviceAnnotation != null && !serviceAnnotation.name().isEmpty()) {
            serviceName = serviceAnnotation.name();
            host = serviceAnnotation.host();
        } else {
            throw new MissingRequiredAnnotationException(ServiceInterface.class, swaggerInterface);
        }
    }

    /**
     * Get the method parser that is associated with the provided {@code swaggerMethod}. The method parser can be used
     * to get details about the Swagger REST API call.
     *
     * @param swaggerMethod The method to generate a parser for.
     *
     * @return The {@link SwaggerMethodParser} associated with the provided {@code swaggerMethod}.
     */
    public SwaggerMethodParser getMethodParser(Method swaggerMethod) {
        return methodParsers.computeIfAbsent(swaggerMethod, sm -> new SwaggerMethodParser(this, sm));
    }

    /**
     * Get the desired host that the provided Swagger interface will target with its REST API calls. This value is
     * retrieved from the {@link ServiceInterface#host()} annotation placed on the Swagger interface.
     *
     * @return The value of the {@link ServiceInterface#host()} annotation.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the name of the service. This value is retrieved from the {@link ServiceInterface#name()} annotation placed
     * on the Swagger interface.
     *
     * @return The value of the {@link ServiceInterface#name()} annotation.
     */
    public String getServiceName() {
        return serviceName;
    }
}
