// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.implementation;

import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.util.serializer.ObjectSerializer;
import io.clientcore.tools.codegen.TestInterfaceServiceVersion;

import java.lang.reflect.InvocationTargetException;

@ServiceInterface(name = "myService", host = "https://somecloud.com")
public interface TestInterfaceClientService {
    static TestInterfaceClientService getInstance(HttpPipeline pipeline, ObjectSerializer serializer,
        TestInterfaceServiceVersion serviceVersion) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class.forName("io.clientcore.tools.codegen.implementation.TestInterfaceClientServiceImpl");
            return (TestInterfaceClientService) clazz
                .getMethod("getInstance", HttpPipeline.class, ObjectSerializer.class, TestInterfaceServiceVersion.class)
                .invoke(null, pipeline, serializer, serviceVersion);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static void reset() {
        try {
            Class<?> clazz = Class.forName("io.clientcore.tools.codegen.implementation.TestInterfaceClientServiceImpl");
            clazz.getMethod("reset").invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @HttpRequestInformation(method = HttpMethod.HEAD, path = "my/uri/path", expectedStatusCodes = { 200 })
    void testHeadMethod();
}
