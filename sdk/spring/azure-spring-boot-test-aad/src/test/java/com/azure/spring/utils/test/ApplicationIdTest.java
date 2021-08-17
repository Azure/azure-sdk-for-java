// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils.test;

import com.azure.spring.core.ApplicationId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ApplicationIdTest {

    @Test
    /*
    This test depends on azure-spring-boot-xxx.jar/META-INF/MANIFEST.MF,
    so there are some requirements to run this test:
    1. This test can not put in azure-spring-boot module.
       If put in azure-spring-boot module, azure-spring-boot-xxx.jar/META-INF/MANIFEST.MF can not be used.
    2. This test can not put in the same package with ApplicationId: "com.azure.spring.core".
       If put in that package, then ApplicationId.class.getPackage().getImplementationVersion() will return null.
    3. This test can not run in Intellij with default configuration.
       In Intellij, the default dependency type is "module", not "external library".
       If the "project structure" change to "external library" manually, then the test can pass.
     */
    public void testVersion() {
        Assertions.assertNotEquals("unknown", ApplicationId.VERSION);
    }
}
