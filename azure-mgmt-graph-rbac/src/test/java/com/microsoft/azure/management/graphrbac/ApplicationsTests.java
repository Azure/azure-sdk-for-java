/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ApplicationsTests extends GraphRbacManagementTestBase {
    private static final String RG_NAME = "javacsmrg350";
    private static final String APP_NAME = "app-javacsm350";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canCreateApplication() throws Exception {
        Application application = graphRbacManager.applications().define("anotherapp")
                .withSignOnUrl("http://easycreate.azure.com/anotherapp")
                .withIdentifierUrl("http://easycreate.azure.com/anotherapp")
                .create();
        System.out.println(application.id() + " - " + application.appId());
    }

}
