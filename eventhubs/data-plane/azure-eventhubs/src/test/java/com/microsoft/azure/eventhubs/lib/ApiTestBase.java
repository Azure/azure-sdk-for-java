/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.lib;

import org.junit.Assume;
import org.junit.BeforeClass;

public class ApiTestBase extends TestBase {

    @BeforeClass
    public static void skipIfNotConfigured() {

        Assume.assumeTrue(TestContext.isTestConfigurationSet());
    }
}
