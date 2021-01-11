// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ServicePrincipalsTests extends GraphRbacManagementTest {

    @Test
    public void canCRUDServicePrincipal() throws Exception {
        String name = generateRandomResourceName("ssp", 21);
        ServicePrincipal servicePrincipal = null;
        try {
            // Create
            servicePrincipal =
                authorizationManager
                    .servicePrincipals()
                    .define(name)
                    .withNewApplication()
                    .definePasswordCredential("sppass")
                    .attach()
                    .create();
            System
                .out
                .println(servicePrincipal.id() + " - " + String.join(",", servicePrincipal.servicePrincipalNames()));
            Assertions.assertNotNull(servicePrincipal.id());
            Assertions.assertNotNull(servicePrincipal.applicationId());
            Assertions.assertEquals(1, servicePrincipal.servicePrincipalNames().size());
            Assertions.assertEquals(1, servicePrincipal.passwordCredentials().size());
            Assertions.assertEquals(0, servicePrincipal.certificateCredentials().size());

            // Get
            servicePrincipal = authorizationManager.servicePrincipals().getByName(name);
            Assertions.assertNotNull(servicePrincipal);
            Assertions.assertNotNull(servicePrincipal.applicationId());
            Assertions.assertEquals(1, servicePrincipal.servicePrincipalNames().size());
            Assertions.assertEquals(1, servicePrincipal.passwordCredentials().size());
            Assertions.assertEquals(0, servicePrincipal.certificateCredentials().size());

            // Update
            servicePrincipal
                .update()
                .withoutCredential("sppass")
                .defineCertificateCredential("spcert")
                .withAsymmetricX509Certificate()
                .withPublicKey(readAllBytes(ServicePrincipalsTests.class.getResourceAsStream("/myTest.cer")))
                .withDuration(Duration.ofDays(1))
                .attach()
                .apply();
            Assertions.assertNotNull(servicePrincipal);
            Assertions.assertNotNull(servicePrincipal.applicationId());
            Assertions.assertEquals(1, servicePrincipal.servicePrincipalNames().size());
            Assertions.assertEquals(0, servicePrincipal.passwordCredentials().size());
            Assertions.assertEquals(1, servicePrincipal.certificateCredentials().size());
        } finally {
            if (servicePrincipal != null) {
                authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
                authorizationManager
                    .applications()
                    .deleteById(authorizationManager.applications().getByName(servicePrincipal.applicationId()).id());
            }
        }
    }

    @Test
    @DoNotRecord
    public void canConsumeServicePrincipalPassword() {
        if (skipInPlayback()) {
            return;
        }

        String name = generateRandomResourceName("ssp", 16);
        String passwordName = generateRandomResourceName("ps", 16);
        String rgName = generateRandomResourceName("rg", 16);
        List<String> clientSecret = new ArrayList<>();
        ServicePrincipal servicePrincipal = null;

        try {
            ResourceGroup resourceGroup = resourceManager.resourceGroups().define(rgName)
                .withRegion(Region.US_EAST)
                .create();

            servicePrincipal = authorizationManager.servicePrincipals().define(name)
                .withNewApplication()
                .definePasswordCredential(passwordName)
                    .withPasswordConsumer(password -> clientSecret.add(password.value()))
                    .attach()
                .withNewRoleInResourceGroup(BuiltInRole.READER, resourceGroup)
                .create();

            Assertions.assertEquals(1, clientSecret.size());

            TokenCredential credential = new ClientSecretCredentialBuilder()
                .clientId(servicePrincipal.applicationId())
                .clientSecret(clientSecret.get(0))
                .tenantId(profile().getTenantId())
                .authorityHost(profile().getEnvironment().getActiveDirectoryEndpoint())
                .httpClient(generateHttpClientWithProxy(null, null))
                .build();

            ResourceManager resourceManager1 = ResourceManager.authenticate(credential, profile())
                .withDefaultSubscription();

            Assertions.assertEquals(resourceGroup.id(), resourceManager1.resourceGroups().getByName(resourceGroup.name()).id());
        } finally {
            try {
                if (servicePrincipal != null) {
                    try {
                        authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
                    } finally {
                        authorizationManager.applications().deleteById(
                            authorizationManager.applications().getByName(servicePrincipal.applicationId()).id()
                        );
                    }
                }
            } finally {
                resourceManager.resourceGroups().beginDeleteByName(rgName);
            }
        }
    }

    @Test
    @Disabled("Do not record - recorded JSON may contain auth info")
    public void canCRUDServicePrincipalWithRole() throws Exception {
        String name = generateRandomResourceName("ssp", 21);
        String rgName = generateRandomResourceName("rg", 22);
        ServicePrincipal servicePrincipal = null;
        String authFile = "/Users/jianghlu/Downloads/graphtestapp.azureauth";
        String subscription = "0b1f6471-1bf0-4dda-aec3-cb9272f09590";
        try {
            // Create
            servicePrincipal =
                authorizationManager
                    .servicePrincipals()
                    .define(name)
                    .withNewApplication()
                    .definePasswordCredential("sppass")
                    .attach()
                    .defineCertificateCredential("spcert")
                    .withAsymmetricX509Certificate()
                    .withPublicKey(Files.readAllBytes(Paths.get("/Users/jianghlu/Documents/code/certs/myserver.crt")))
                    .withDuration(Duration.ofDays(7))
                    .withAuthFileToExport(new FileOutputStream(authFile))
                    .withPrivateKeyFile("/Users/jianghlu/Documents/code/certs/myserver.pfx")
                    .withPrivateKeyPassword("StrongPass!123")
                    .attach()
                    .withNewRoleInSubscription(BuiltInRole.CONTRIBUTOR, subscription)
                    .create();
            System
                .out
                .println(servicePrincipal.id() + " - " + String.join(",", servicePrincipal.servicePrincipalNames()));
            Assertions.assertNotNull(servicePrincipal.id());
            Assertions.assertNotNull(servicePrincipal.applicationId());
            Assertions.assertEquals(2, servicePrincipal.servicePrincipalNames().size());

            ResourceManagerUtils.sleep(Duration.ofSeconds(10));
            ResourceManager resourceManager =
                ResourceManager
                    .authenticate(credentialFromFile(), profile())
                    .withSubscription(subscription);
            ResourceGroup group = resourceManager.resourceGroups().define(rgName).withRegion(Region.US_WEST).create();

            // Update
            RoleAssignment ra = servicePrincipal.roleAssignments().iterator().next();
            servicePrincipal
                .update()
                .withoutRole(ra)
                .withNewRoleInResourceGroup(BuiltInRole.CONTRIBUTOR, group)
                .apply();

            ResourceManagerUtils.sleep(Duration.ofMinutes(2));
            Assertions.assertNotNull(resourceManager.resourceGroups().getByName(group.name()));
            try {
                resourceManager.resourceGroups().define(rgName + "2").withRegion(Region.US_WEST).create();
                Assertions.fail();
            } catch (Exception e) {
                // expected
            }
        } finally {
            try {
                authorizationManager.servicePrincipals().deleteById(servicePrincipal.id());
            } catch (Exception e) {
            }
            try {
                authorizationManager
                    .applications()
                    .deleteById(authorizationManager.applications().getByName(servicePrincipal.applicationId()).id());
            } catch (Exception e) {
            }
        }
    }
}
