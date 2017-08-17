/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.Host;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for creating individual Swagger interface method parsers from a Swagger
 * interface.
 */
class SwaggerInterfaceParser {
    private final Class<?> swaggerInterface;
    private final Map<String, SwaggerMethodProxyDetails> methodDetails = new HashMap<>();

    private String host;

    /**
     * Create a new SwaggerInterfaceParser object with the provided fully qualified interface
     * name.
     * @param swaggerInterface The interface that will be parsed.
     */
    SwaggerInterfaceParser(Class<?> swaggerInterface) {
        this.swaggerInterface = swaggerInterface;
    }

    /**
     * Parse the desired host that the provided Swagger interface will target with its REST API
     * calls. This value is retrieved from the @Host annotation placed on the Swagger interface. If
     * no @Host annotation exists on the Swagger interface, then null will be returned.
     * @return The value of the @Host annotation, or null if no @Host annotation exists.
     */
    public String host() {
        if (host == null) {
            final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
            if (hostAnnotation != null) {
                host = hostAnnotation.value();
            }
        }
        return host;
    }

    /**
     * Create and return a SwaggerMethodProxyDetails object that is associated with the provided
     * methodName. If a SwaggerMethodProxyDetails object is already associated with the provided
     * methodName, then the existing object will be returned.
     * @param methodName The name of the method.
     * @return The SwaggerMethodProxyDetails object that is associated with the provided methodName.
     */
    public SwaggerMethodProxyDetails getMethodProxyDetails(String methodName) {
        SwaggerMethodProxyDetails result = methodDetails.get(methodName);
        if (result == null) {
            final String fullyQualifiedMethodName = swaggerInterface.getCanonicalName() + "." + methodName;
            result = new SwaggerMethodProxyDetails(fullyQualifiedMethodName);
            methodDetails.put(methodName, result);
        }
        return result;
    }
}
