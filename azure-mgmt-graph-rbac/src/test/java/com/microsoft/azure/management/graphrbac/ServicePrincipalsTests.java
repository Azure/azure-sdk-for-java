/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class ServicePrincipalsTests extends GraphRbacManagementTestBase {
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
    @Ignore("Doesn't work when logged as a service principal")
    public void getServicePrincipal() throws Exception {
        List<ServicePrincipal> servicePrincipals = graphRbacManager.servicePrincipals().list();
        Assert.assertNotNull(servicePrincipals);
    }

}
