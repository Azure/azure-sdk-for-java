/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.google.common.base.Joiner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServicePrincipalsTests extends GraphRbacManagementTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canCRUDSerivcePrincipal() throws Exception {
        ServicePrincipal servicePrincipal = graphRbacManager.servicePrincipals().define("anothersp13")
                .withNewApplication("http://easycreate.azure.com/anotherapp/13")
                .defineKey("sppass")
                    .withPassword("StrongPass!12")
                    .attach()
                .create();
        System.out.println(servicePrincipal.id() + " - " + Joiner.on(", ").join(servicePrincipal.servicePrincipalNames()));
    }

}
