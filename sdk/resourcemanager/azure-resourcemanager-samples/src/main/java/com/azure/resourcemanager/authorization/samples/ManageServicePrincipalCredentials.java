// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.google.common.io.ByteStreams;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;

import java.io.IOException;
import java.time.Duration;

/**
 * Azure service principal sample for managing its credentials.
 * - Create an application with 2 passwords and 1 certificate credentials
 * - Create an associated service principal with contributor role
 * - Verify all password credentials and certificate credentials are valid
 * - Revoke access of a password credential
 * - Verify the password credential is no longer valid
 * - Revoke the role assignment
 * - Verify the remaining password credential is invalid
 */
public final class ManageServicePrincipalCredentials {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of AzureResourceManager
     * @param profile the profile the sample is running in
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, AzureProfile profile) throws IOException {
        final String spName         = Utils.randomResourceName(azureResourceManager, "sp", 20);
        final String appName        = Utils.randomResourceName(azureResourceManager, "app", 20);
        final String passwordName1  = Utils.randomResourceName(azureResourceManager, "password", 20);
        final String password1      = "P@ssw0rd";
        final String passwordName2  = Utils.randomResourceName(azureResourceManager, "password", 20);
        final String password2      = "StrongP@ss!12";
        final String certName1      = Utils.randomResourceName(azureResourceManager, "cert", 20);
        final String raName         = Utils.randomUuid(azureResourceManager);
        String servicePrincipalId = "";
        try {
            // ============================================================
            // Create service principal

            System.out.println("Creating an Active Directory service principal " + spName + "...");

            ServicePrincipal servicePrincipal = azureResourceManager.accessManagement().servicePrincipals()
                    .define(spName)
                    .withNewApplication()
                    .definePasswordCredential(passwordName1)
                        .attach()
                    .definePasswordCredential(passwordName2)
                        .attach()
                    .defineCertificateCredential(certName1)
                        .withAsymmetricX509Certificate()
                        .withPublicKey(ByteStreams.toByteArray(ManageServicePrincipalCredentials.class.getResourceAsStream("/myTest.cer")))
                        .withDuration(Duration.ofDays(1))
                        .attach()
                    .create();

            System.out.println("Created service principal " + spName + ".");
            Utils.print(servicePrincipal);
            servicePrincipalId = servicePrincipal.id();

            // ============================================================
            // Create role assignment

            System.out.println("Creating a Contributor role assignment " + raName + " for the service principal...");

            ResourceManagerUtils.sleep(Duration.ofSeconds(15));

            RoleAssignment roleAssignment = azureResourceManager.accessManagement().roleAssignments()
                    .define(raName)
                    .forServicePrincipal(servicePrincipal)
                    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                    .withSubscriptionScope(profile.getSubscriptionId())
                    .create();

            System.out.println("Created role assignment " + raName + ".");
            Utils.print(roleAssignment);

            // ============================================================
            // Verify the credentials are valid

            System.out.println("Verifying password credential " + passwordName1 + " is valid...");

            TokenCredential testCredential = new ClientSecretCredentialBuilder()
                .tenantId(azureResourceManager.tenantId())
                .clientId(servicePrincipal.applicationId())
                .clientSecret(password1)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
            try {
                AzureResourceManager.authenticate(testCredential, profile).withDefaultSubscription();

                System.out.println("Verified " + passwordName1 + " is valid.");
            } catch (Exception e) {
                System.out.println("Failed to verify " + passwordName1 + " is valid.");
            }

            System.out.println("Verifying password credential " + passwordName2 + " is valid...");

            testCredential = new ClientSecretCredentialBuilder()
                .tenantId(azureResourceManager.tenantId())
                .clientId(servicePrincipal.applicationId())
                .clientSecret(password2)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
            try {
                AzureResourceManager.authenticate(testCredential, profile).withDefaultSubscription();

                System.out.println("Verified " + passwordName2 + " is valid.");
            } catch (Exception e) {
                System.out.println("Failed to verify " + passwordName2 + " is valid.");
            }

            System.out.println("Verifying certificate credential " + certName1 + " is valid...");

            testCredential = new ClientCertificateCredentialBuilder()
                .tenantId(azureResourceManager.tenantId())
                .clientId(servicePrincipal.applicationId())
                .pfxCertificate(ManageServicePrincipalCredentials.class.getResource("/myTest.pfx").toString(), "Abc123")
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
            try {
                AzureResourceManager.authenticate(testCredential, profile).withDefaultSubscription();

                System.out.println("Verified " + certName1 + " is valid.");
            } catch (Exception e) {
                System.out.println("Failed to verify " + certName1 + " is valid.");
            }

            // ============================================================
            // Revoke access of the 1st password credential
            System.out.println("Revoking access for password credential " + passwordName1 + "...");

            servicePrincipal.update()
                    .withoutCredential(passwordName1)
                    .apply();

            ResourceManagerUtils.sleep(Duration.ofSeconds(15));

            System.out.println("Credential revoked.");

            // ============================================================
            // Verify the revoked password credential is no longer valid

            System.out.println("Verifying password credential " + passwordName1 + " is revoked...");

            testCredential = new ClientSecretCredentialBuilder()
                .tenantId(azureResourceManager.tenantId())
                .clientId(servicePrincipal.applicationId())
                .clientSecret(password1)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
            try {
                AzureResourceManager.authenticate(testCredential, profile).withDefaultSubscription();

                System.out.println("Failed to verify " + passwordName1 + " is revoked.");
            } catch (Exception e) {
                System.out.println("Verified " + passwordName1 + " is revoked.");
            }

            // ============================================================
            // Revoke the role assignment

            System.out.println("Revoking role assignment " + raName + "...");

            azureResourceManager.accessManagement().roleAssignments().deleteById(roleAssignment.id());

            ResourceManagerUtils.sleep(Duration.ofSeconds(5));

            // ============================================================
            // Verify the revoked password credential is no longer valid

            System.out.println("Verifying password credential " + passwordName2 + " has no access to subscription...");

            testCredential = new ClientSecretCredentialBuilder()
                .tenantId(azureResourceManager.tenantId())
                .clientId(servicePrincipal.applicationId())
                .clientSecret(password2)
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
            try {
                AzureResourceManager.authenticate(testCredential, profile).withDefaultSubscription()
                        .resourceGroups().list();

                System.out.println("Failed to verify " + passwordName2 + " has no access to subscription.");
            } catch (Exception e) {
                System.out.println("Verified " + passwordName2 + " has no access to subscription.");
            }


            return true;
        } finally {
            try {
                System.out.println("Deleting application: " + appName);
                azureResourceManager.accessManagement().servicePrincipals().deleteById(servicePrincipalId);
                System.out.println("Deleted application: " + appName);
            } catch (Exception e) {
                System.out.println("Did not create applications in Azure. No clean up is necessary");
            }
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager, profile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageServicePrincipalCredentials() {
    }
}
