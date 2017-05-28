/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.google.common.base.Joiner;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

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
        String authFile = "/Users/jianghlu/Downloads/graphtestapp.azureauth";
        String subscription = "0b1f6471-1bf0-4dda-aec3-cb9272f09590";
        try {
            servicePrincipal = graphRbacManager.servicePrincipals().define("ansp43")
                    .withNewApplication("http://easycreate.azure.com/ansp/43")
                    .definePasswordCredential("sppass")
                        .withPasswordValue("StrongPass!12")
                        .withAuthFileToExport(new FileOutputStream(authFile))
                        .attach()
                    .withNewRoleInSubscription(BuiltInRole.CONTRIBUTOR, subscription)
                    .create();
            System.out.println(servicePrincipal.id() + " - " + Joiner.on(", ").join(servicePrincipal.servicePrincipalNames()));
            Assert.assertNotNull(servicePrincipal.id());
            Assert.assertNotNull(servicePrincipal.applicationId());
            Assert.assertEquals(2, servicePrincipal.servicePrincipalNames().size());
            Assert.assertEquals(1, servicePrincipal.passwordCredentials().size());

            Thread.sleep(10000);
            ResourceManager resourceManager = ResourceManager.authenticate(
                    ApplicationTokenCredentials.fromFile(new File(authFile))).withSubscription(subscription);
            Assert.assertNotNull(resourceManager.resourceGroups().list());
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
