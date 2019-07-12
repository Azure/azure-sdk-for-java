// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.implementation.annotation.Host;
import com.azure.core.implementation.annotation.ServiceInterface;
import com.azure.core.implementation.exception.MissingRequiredAnnotationException;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.util.ImplUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The type responsible for creating individual Swagger interface method parsers from a Swagger
 * interface.
 */
public class SwaggerInterfaceParser {
    private final String host;
    private final String serviceName;
    private final Map<Method, SwaggerMethodParser> methodParsers = new HashMap<>();

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     * @param serializer The serializer that will be used to serialize non-String header values and query values.
     */
    public SwaggerInterfaceParser(Class<?> swaggerInterface, SerializerAdapter serializer) {
        this(swaggerInterface, serializer, null);
    }

    /**
     * Create a SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     * @param serializer The serializer that will be used to serialize non-String header values and query values.
     * @param host The host of URLs that this Swagger interface targets.
     */
    public SwaggerInterfaceParser(Class<?> swaggerInterface, SerializerAdapter serializer, String host) {
        if (!ImplUtils.isNullOrEmpty(host)) {
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
    public SwaggerMethodParser methodParser(Method swaggerMethod) {
        SwaggerMethodParser result = methodParsers.get(swaggerMethod);
        if (result == null) {
            result = new SwaggerMethodParser(swaggerMethod, host());
            methodParsers.put(swaggerMethod, result);
        }
        return result;
    }

    /**
     * Get the desired host that the provided Swagger interface will target with its REST API
     * calls. This value is retrieved from the @Host annotation placed on the Swagger interface.
     * @return The value of the @Host annotation.
     */
    String host() {
        return host;
    }

    String serviceName() {
        return serviceName;
    }
}
