// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.rest;

import com.azure.core.implementation.http.rest.SwaggerInterfaceParser;
import com.azure.core.implementation.http.rest.SwaggerMethodParser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.core.implementation.ImplUtils.MAX_CACHE_SIZE;

public class SwaggerInterfaceParserExperimental extends SwaggerInterfaceParser {
    private static final Map<Class<?>, SwaggerInterfaceParser> INTERFACE_PARSERS = new ConcurrentHashMap<>();

    private final Map<Method, SwaggerMethodParser> methodParsers = new ConcurrentHashMap<>();

    protected SwaggerInterfaceParserExperimental(Class<?> swaggerInterface) {
        super(swaggerInterface);
    }

    public static SwaggerInterfaceParser getInstance(Class<?> swaggerInterface) {
        if (INTERFACE_PARSERS.size() >= MAX_CACHE_SIZE) {
            INTERFACE_PARSERS.clear();
        }

        return INTERFACE_PARSERS.computeIfAbsent(swaggerInterface, SwaggerInterfaceParserExperimental::new);
    }

    @Override
    public SwaggerMethodParser getMethodParser(Method swaggerMethod) {
        return methodParsers.computeIfAbsent(swaggerMethod, sm -> new SwaggerMethodParserExperimental(this, sm));
    }
}
