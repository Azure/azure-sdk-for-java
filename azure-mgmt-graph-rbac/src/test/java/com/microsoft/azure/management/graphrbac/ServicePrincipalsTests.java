/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.google.common.base.Joiner;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
    @Ignore("Need to login as user to run")
    public void canCRUDServicePrincipal() throws Exception {
        ServicePrincipal servicePrincipal = null;
        try {
            servicePrincipal = graphRbacManager.servicePrincipals().define("anothersp40")
                    .withNewApplication("http://easycreate.azure.com/anotherapp/40")
                    .definePasswordCredential("sppass")
                        .withPasswordValue("StrongPass!12")
                        .attach()
                    .create();
            System.out.println(servicePrincipal.id() + " - " + Joiner.on(", ").join(servicePrincipal.servicePrincipalNames()));
            Assert.assertNotNull(servicePrincipal.id());
            Assert.assertNotNull(servicePrincipal.applicationId());
            Assert.assertEquals(2, servicePrincipal.servicePrincipalNames().size());
            Assert.assertEquals(1, servicePrincipal.passwordCredentials().size());
            Assert.assertEquals(0, servicePrincipal.certificateCredentials().size());
        } finally {
            if (servicePrincipal != null) {
                graphRbacManager.servicePrincipals().deleteById(servicePrincipal.id());
                graphRbacManager.applications().deleteById(graphRbacManager.applications().getByName(servicePrincipal.applicationId()).id());
            }
        }
    }

    @Test
    @Ignore("Need to login as user to run")
    public void canCRUDServicePrincipalWithRole() throws Exception {
        ServicePrincipal servicePrincipal = null;
        try {
            servicePrincipal = graphRbacManager.servicePrincipals().define("anothersp40")
                    .withNewApplication("http://easycreate.azure.com/anothersp/40")
                    .definePasswordCredential("sppass")
                        .withPasswordValue("StrongPass!12")
                        .attach()
                    .withNewRoleInSubscription(BuiltInRole.CONTRIBUTOR, "ec0aa5f7-9e78-40c9-85cd-535c6305b380")
                    .withNewRoleInSubscription(BuiltInRole.CONTRIBUTOR, "db1ab6f0-4769-4b27-930e-01e2ef9c123c")
                    .create();
            System.out.println(servicePrincipal.id() + " - " + Joiner.on(", ").join(servicePrincipal.servicePrincipalNames()));
            Assert.assertNotNull(servicePrincipal.id());
            Assert.assertNotNull(servicePrincipal.applicationId());
            Assert.assertEquals(2, servicePrincipal.servicePrincipalNames().size());
            Assert.assertEquals(1, servicePrincipal.passwordCredentials().size());
            Assert.assertEquals(0, servicePrincipal.certificateCredentials().size());
        } finally {
            try {
                graphRbacManager.servicePrincipals().deleteById(servicePrincipal.id());
            } catch (Exception e) { }
            try {
                graphRbacManager.applications().deleteById(graphRbacManager.applications().getByName(servicePrincipal.applicationId()).id());
            } catch (Exception e) { }
        }
    }

}
