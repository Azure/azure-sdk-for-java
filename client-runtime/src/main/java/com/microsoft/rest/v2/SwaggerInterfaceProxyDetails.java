/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import java.util.HashMap;
import java.util.Map;

/**
 * Details that are associated with an interface that is generated from a Swagger specification.
 * This is generally used by a proxy implementation of the Swagger interface.
 */
class SwaggerInterfaceProxyDetails {
    private final String fullyQualifiedInterfaceName;
    private final Map<String, SwaggerMethodProxyDetails> methodDetails = new HashMap<>();

    /**
     * Create a new SwaggerInterfaceProxyDetails object with the provided fully qualified interface
     * name.
     * @param fullyQualifiedInterfaceName The fully qualified interface name.
     */
    SwaggerInterfaceProxyDetails(String fullyQualifiedInterfaceName) {
        this.fullyQualifiedInterfaceName = fullyQualifiedInterfaceName;
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
            final String fullyQualifiedMethodName = fullyQualifiedInterfaceName + "." + methodName;
            result = new SwaggerMethodProxyDetails(fullyQualifiedMethodName);

            methodDetails.put(methodName, result);
        }
        return result;
    }
}
