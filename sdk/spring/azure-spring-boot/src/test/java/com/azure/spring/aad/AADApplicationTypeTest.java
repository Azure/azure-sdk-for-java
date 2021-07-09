// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.ClassUtils;

import static com.azure.spring.aad.AADApplicationType.BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME;
import static com.azure.spring.aad.AADApplicationType.CLIENT_REGISTRATION_CLASS_NAME;
import static com.azure.spring.aad.AADApplicationType.ENABLE_WEB_SECURITY_CLASS_NAME;
import static com.azure.spring.aad.AADApplicationType.WEB_APPLICATION;
import static com.azure.spring.aad.AADApplicationType.RESOURCE_SERVER;
import static com.azure.spring.aad.AADApplicationType.RESOURCE_SERVER_WITH_OBO;
import static com.azure.spring.aad.AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER;
import static com.azure.spring.aad.AADApplicationType.defaultApplicationType;
import static com.azure.spring.aad.AADApplicationType.validateApplicationType;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADApplicationTypeTest {

    private AADAuthenticationProperties getProperties(boolean isConfiguredClientId,
                                                      AADApplicationType applicationType) {
        AADAuthenticationProperties properties = new AADAuthenticationProperties();
        properties.setApplicationType(applicationType);
        if (isConfiguredClientId) {
            properties.setClientId("fake-client-id");
        }
        return properties;
    }

    @Test
    public void noneApplicationType() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, false, false, false);
            assertThrows(IllegalStateException.class,
                () -> defaultApplicationType(getProperties(false, null)));
        }
    }

    @Test
    public void webApplication() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, false, true, true);
            assertSame(WEB_APPLICATION, defaultApplicationType(getProperties(true, null)));
        }
    }

    @Test
    public void resourceServer() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, false, false);
            assertSame(RESOURCE_SERVER, defaultApplicationType(getProperties(false, null)));
        }
    }

    @Test
    public void resourceServerWithObo() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, true, true);
            assertSame(RESOURCE_SERVER_WITH_OBO, defaultApplicationType(getProperties(true, null)));
        }
    }

    @Test
    public void validateWebApplication() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, false, true, true);
            assertTrue(validateApplicationType(getProperties(true, null)));
        }
    }

    @Test
    public void validateResourceServer() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, false, false);
            assertTrue(validateApplicationType(getProperties(false, null)));
        }
    }

    @Test
    public void validateResourceServerWithObo() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, true, true);
            assertTrue(validateApplicationType(getProperties(true, null)));
        }
    }

    @Test
    public void validateWebApplicationAndResourceServer() {
        try (MockedStatic<ClassUtils> classUtils =
                 mockStatic(ClassUtils.class, Mockito.CALLS_REAL_METHODS)) {
            filterClassLoader(classUtils, true, true, true);
            assertTrue(validateApplicationType(getProperties(true, WEB_APPLICATION_AND_RESOURCE_SERVER)));
        }
    }

    private void filterClassLoader(MockedStatic<ClassUtils> classUtils,
                                   boolean expectedTokenClassPresent,
                                   boolean expectedSecurityClassPresent,
                                   boolean expectedRegistrationClassPresent) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        classUtils.when(ClassUtils::getDefaultClassLoader).thenReturn(classLoader);
        classUtils.when(() -> ClassUtils.isPresent(BEARER_TOKEN_AUTHENTICATION_TOKEN_CLASS_NAME, classLoader))
                  .thenReturn(expectedTokenClassPresent);
        classUtils.when(() -> ClassUtils.isPresent(ENABLE_WEB_SECURITY_CLASS_NAME, classLoader))
                  .thenReturn(expectedSecurityClassPresent);
        classUtils.when(() -> ClassUtils.isPresent(CLIENT_REGISTRATION_CLASS_NAME, classLoader))
                  .thenReturn(expectedRegistrationClassPresent);
    }
}
