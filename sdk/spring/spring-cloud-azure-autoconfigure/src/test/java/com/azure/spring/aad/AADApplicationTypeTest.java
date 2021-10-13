// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.ClassUtils;

import static com.azure.spring.aad.AADApplicationType.RESOURCE_SERVER;
import static com.azure.spring.aad.AADApplicationType.RESOURCE_SERVER_WITH_OBO;
import static com.azure.spring.aad.AADApplicationType.SPRING_SECURITY_OAUTH2_CLIENT_CLASS_NAME;
import static com.azure.spring.aad.AADApplicationType.SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_CLASS_NAME;
import static com.azure.spring.aad.AADApplicationType.WEB_APPLICATION;
import static com.azure.spring.aad.AADApplicationType.inferApplicationTypeByDependencies;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADApplicationTypeTest {

    @Test
    public void noneApplicationType() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, false, false);
            assertNull(inferApplicationTypeByDependencies());
        }
    }

    @Test
    public void webApplication() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, false, true);
            assertSame(WEB_APPLICATION, inferApplicationTypeByDependencies());
        }
    }

    @Test
    public void resourceServer() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, false);
            assertSame(RESOURCE_SERVER, inferApplicationTypeByDependencies());
        }
    }

    @Test
    public void resourceServerWithObo() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, true);
            assertSame(RESOURCE_SERVER_WITH_OBO, inferApplicationTypeByDependencies());
        }
    }

    private void filterClassLoader(MockedStatic<ClassUtils> classUtils,
                                   boolean expectedTokenClassPresent,
                                   boolean expectedRegistrationClassPresent) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        classUtils.when(ClassUtils::getDefaultClassLoader).thenReturn(classLoader);
        classUtils.when(() -> ClassUtils.isPresent(SPRING_SECURITY_OAUTH2_RESOURCE_SERVER_CLASS_NAME, classLoader))
                  .thenReturn(expectedTokenClassPresent);
        classUtils.when(() -> ClassUtils.isPresent(SPRING_SECURITY_OAUTH2_CLIENT_CLASS_NAME, classLoader))
                  .thenReturn(expectedRegistrationClassPresent);
    }
}
