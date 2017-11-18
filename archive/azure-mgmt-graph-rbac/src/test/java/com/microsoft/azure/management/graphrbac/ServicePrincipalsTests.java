/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class ServicePrincipalsTests extends GraphRbacManagementTest {

    @Test
    public void canCRUDServicePrincipal() throws Exception {
        String name = SdkContext.randomResourceName("ssp", 21);
        ServicePrincipal servicePrincipal = null;
        try {
            // Create
            servicePrincipal = graphRbacManager.servicePrincipals().define(name)
                    .withNewApplication("http://easycreate.azure.com/" + name)
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

            // Get
            servicePrincipal = graphRbacManager.servicePrincipals().getByName(name);
            Assert.assertNotNull(servicePrincipal);
            Assert.assertNotNull(servicePrincipal.applicationId());
            Assert.assertEquals(2, servicePrincipal.servicePrincipalNames().size());
            Assert.assertEquals(1, servicePrincipal.passwordCredentials().size());
            Assert.assertEquals(0, servicePrincipal.certificateCredentials().size());

            // Update
            servicePrincipal.update()
                    .withoutCredential("sppass")
                    .defineCertificateCredential("spcert")
                        .withAsymmetricX509Certificate()
                        .withPublicKey(ByteStreams.toByteArray(ServicePrincipalsTests.class.getResourceAsStream("/myTest.cer")))
                        .withDuration(Duration.standardDays(1))
                        .attach()
                    .apply();
            Assert.assertNotNull(servicePrincipal);
            Assert.assertNotNull(servicePrincipal.applicationId());
            Assert.assertEquals(2, servicePrincipal.servicePrincipalNames().size());
            Assert.assertEquals(0, servicePrincipal.passwordCredentials().size());
            Assert.assertEquals(1, servicePrincipal.certificateCredentials().size());
        } finally {
            if (servicePrincipal != null) {
                graphRbacManager.servicePrincipals().deleteById(servicePrincipal.id());
                graphRbacManager.applications().deleteById(graphRbacManager.applications().getByName(servicePrincipal.applicationId()).id());
            }
        }
    }

    @Test
    @Ignore("Do not record - recorded JSON may contain auth info")
    public void canCRUDServicePrincipalWithRole() throws Exception {
        String name = SdkContext.randomResourceName("ssp", 21);
        String rgName = SdkContext.randomResourceName("rg", 22);
        ServicePrincipal servicePrincipal = null;
        String authFile = "/Users/jianghlu/Downloads/graphtestapp.azureauth";
        String subscription = "0b1f6471-1bf0-4dda-aec3-cb9272f09590";
        try {
            // Create
            servicePrincipal = graphRbacManager.servicePrincipals().define(name)
                    .withNewApplication("http://easycreate.azure.com/ansp/" + name)
                    .definePasswordCredential("sppass")
                        .withPasswordValue("StrongPass!12")
                        .attach()
                    .defineCertificateCredential("spcert")
                        .withAsymmetricX509Certificate()
                        .withPublicKey(Files.readAllBytes(Paths.get("/Users/jianghlu/Documents/code/certs/myserver.crt")))
                        .withDuration(Duration.standardDays(7))
                        .withAuthFileToExport(new FileOutputStream(authFile))
                        .withPrivateKeyFile("/Users/jianghlu/Documents/code/certs/myserver.pfx")
                        .withPrivateKeyPassword("StrongPass!123")
                        .attach()
                    .withNewRoleInSubscription(BuiltInRole.CONTRIBUTOR, subscription)
                    .create();
            System.out.println(servicePrincipal.id() + " - " + Joiner.on(", ").join(servicePrincipal.servicePrincipalNames()));
            Assert.assertNotNull(servicePrincipal.id());
            Assert.assertNotNull(servicePrincipal.applicationId());
            Assert.assertEquals(2, servicePrincipal.servicePrincipalNames().size());

            SdkContext.sleep(10000);
            ResourceManager resourceManager = ResourceManager.authenticate(
                    ApplicationTokenCredentials.fromFile(new File(authFile))).withSubscription(subscription);
            ResourceGroup group = resourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.US_WEST).create();

            // Update
            RoleAssignment ra = servicePrincipal.roleAssignments().iterator().next();
            servicePrincipal.update()
                    .withoutRole(ra)
                    .withNewRoleInResourceGroup(BuiltInRole.CONTRIBUTOR, group)
                    .apply();

            SdkContext.sleep(120000);
            Assert.assertNotNull(resourceManager.resourceGroups().getByName(group.name()));
            try {
                resourceManager.resourceGroups().define(rgName + "2")
                        .withRegion(Region.US_WEST).create();
                fail();
            } catch (Exception e) {
                // expected
            }
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
