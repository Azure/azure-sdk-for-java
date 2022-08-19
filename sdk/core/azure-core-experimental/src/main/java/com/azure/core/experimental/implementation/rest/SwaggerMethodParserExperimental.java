// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.rest;

import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.http.rest.SwaggerInterfaceParser;
import com.azure.core.implementation.http.rest.SwaggerMethodParser;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.DefaultJsonReader;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class SwaggerMethodParserExperimental extends SwaggerMethodParser {
    /**
     * Create a SwaggerMethodParser object using the provided fully qualified method name.
     *
     * @param swaggerMethod the Swagger method to parse.
     */
    public SwaggerMethodParserExperimental(Method swaggerMethod) {
        this(SwaggerInterfaceParserExperimental.getInstance(swaggerMethod.getDeclaringClass()), swaggerMethod);
    }

    /**
     * Creates a SwaggerMethodParser object using the provided SwaggerInterfaceParser and Swagger method.
     *
     * @param interfaceParser The SwaggerInterfaceParser.
     * @param swaggerMethod The Swagger method.
     */
    protected SwaggerMethodParserExperimental(SwaggerInterfaceParser interfaceParser, Method swaggerMethod) {
        super(interfaceParser, swaggerMethod);
    }

    @Override
    public Object deserializeBody(SerializerAdapter serializer, byte[] data, Type bodyType, SerializerEncoding encoding)
        throws IOException {
        if (FromJsonCache.isJsonCapable(bodyType)) {
            JsonReader jsonReader = DefaultJsonReader.fromBytes(data);
            return FromJsonCache.fromJson(TypeUtil.getRawClass(bodyType), jsonReader);
        } else {
            return super.deserializeBody(serializer, data, bodyType, encoding);
        }
    }
}
