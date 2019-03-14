/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation;

import com.azure.common.implementation.exception.MissingRequiredAnnotationException;
import com.azure.common.annotations.Host;
import com.azure.common.implementation.serializer.SerializerAdapter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The type responsible for creating individual Swagger interface method parsers from a Swagger
 * interface.
 */
public class SwaggerInterfaceParser {
    private final SerializerAdapter serializer;
    private final String host;
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
        this.serializer = serializer;

        if (host != null && !host.isEmpty()) {
            this.host = host;
        }
        else {
            final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
            if (hostAnnotation != null && !hostAnnotation.value().isEmpty()) {
                this.host = hostAnnotation.value();
            }
            else {
                throw new MissingRequiredAnnotationException(Host.class, swaggerInterface);
            }
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
            result = new SwaggerMethodParser(swaggerMethod, serializer, host());
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
}
