// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.annotation.Host;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.CoreUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type responsible for creating individual Swagger interface method parsers from a Swagger
 * interface.
 */
class SwaggerInterfaceParser {
    private final String host;
    private final String serviceName;
    private static final Map<Method, SwaggerMethodParser> METHOD_PARSERS = new ConcurrentHashMap<>();

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     * @param serializer The serializer that will be used to serialize non-String header values and query values.
     */
    SwaggerInterfaceParser(Class<?> swaggerInterface, SerializerAdapter serializer) {
        this(swaggerInterface, serializer, null);
    }

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     * @param serializer The serializer that will be used to serialize non-String header values and query values.
     * @param host The host of URLs that this Swagger interface targets.
     * @throws MissingRequiredAnnotationException When an expected annotation on the interface is not provided.
     */
    SwaggerInterfaceParser(Class<?> swaggerInterface, SerializerAdapter serializer, String host) {
        if (!CoreUtils.isNullOrEmpty(host)) {
            this.host = host;
        } else {
            final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
            if (hostAnnotation != null && !hostAnnotation.value().isEmpty()) {
                this.host = hostAnnotation.value();
            } else {
                throw new MissingRequiredAnnotationException(Host.class, swaggerInterface);
            }
        }

        ServiceInterface serviceAnnotation = swaggerInterface.getAnnotation(ServiceInterface.class);
        if (serviceAnnotation != null && !serviceAnnotation.name().isEmpty()) {
            serviceName = serviceAnnotation.name();
        } else {
            throw new MissingRequiredAnnotationException(ServiceInterface.class, swaggerInterface);
        }
    }

    /**
     * Get the method parser that is associated with the provided swaggerMethod. The method parser
     * can be used to get details about the Swagger REST API call.
     *
     * @param swaggerMethod the method to generate a parser for
     * @return the SwaggerMethodParser associated with the provided swaggerMethod
     */
    SwaggerMethodParser getMethodParser(Method swaggerMethod) {
        SwaggerMethodParser result = METHOD_PARSERS.get(swaggerMethod);
        if (result == null) {
            result = new SwaggerMethodParser(swaggerMethod, getHost());
            METHOD_PARSERS.put(swaggerMethod, result);
        }
        return result;
    }

    /**
     * Get the desired host that the provided Swagger interface will target with its REST API
     * calls. This value is retrieved from the @Host annotation placed on the Swagger interface.
     * @return The value of the @Host annotation.
     */
    String getHost() {
        return host;
    }

    String getServiceName() {
        return serviceName;
    }
}
