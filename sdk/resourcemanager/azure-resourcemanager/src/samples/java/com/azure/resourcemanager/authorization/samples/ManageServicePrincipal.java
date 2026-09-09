// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.PasswordCredential;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.SampleUtils;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Azure service principal sample for managing service principals.
 *  - Create an Active Directory application
 *  - Create a service principal for the application and assign it the Reader role on the subscription
 *  - Use the service principal's credential to list the subscription's virtual machines
 *  - Update the application (add a reply URL)
 *  - Update the service principal to revoke its password credential and role
 *  - Delete the application (which also deletes the service principal)
 */
public final class ManageServicePrincipal {
    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of AzureResourceManager
     * @param profile the profile the sample is running in
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager, AzureProfile profile) {
        final String appName = SampleUtils.randomResourceName(azureResourceManager, "adapp", 20);
        final String passwordName = SampleUtils.randomResourceName(azureResourceManager, "password", 20);
        final PasswordHolder passwordHolder = new PasswordHolder();
        ActiveDirectoryApplication application = null;
        try {
            // Create an Active Directory application.
            application = azureResourceManager.accessManagement()
                .activeDirectoryApplications()
                .define(appName)
                .withSignOnUrl("https://github.com/Azure/azure-sdk-for-java/" + appName)
                .create();

            // Create a service principal for the application, add a password credential, and assign the
            // Reader role on the subscription (least privilege: the sample only needs to read resources below).
            // The service principal must share the application's display name.
            ServicePrincipal servicePrincipal = azureResourceManager.accessManagement()
                .servicePrincipals()
                .define(appName)
                .withExistingApplication(application)
                .definePasswordCredential(passwordName)
                .withPasswordConsumer(passwordHolder)
                .attach()
                .withNewRoleInSubscription(BuiltInRole.READER, azureResourceManager.subscriptionId())
                .create();

            // Wait for the service principal and role assignment to propagate.
            ResourceManagerUtils.sleep(Duration.ofSeconds(15));

            // Use the service principal's credential to access the subscription and list its virtual machines.
            TokenCredential spCredential
                = new ClientSecretCredentialBuilder().tenantId(azureResourceManager.tenantId())
                    .clientId(servicePrincipal.applicationId())
                    .clientSecret(passwordHolder.password)
                    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                    .build();
            long vmCount = AzureResourceManager.authenticate(spCredential, profile)
                .withDefaultSubscription()
                .virtualMachines()
                .list()
                .stream()
                .count();
            System.out.println("Service principal listed " + vmCount + " virtual machine(s) in the subscription.");

            // Update the application: add a reply URL.
            application.update().withReplyUrl("https://github.com/Azure/azure-sdk-for-java").apply();

            // Update the service principal: revoke its password credential and role assignment.
            ServicePrincipal.Update spUpdate = servicePrincipal.update().withoutCredential(passwordName);
            for (RoleAssignment roleAssignment : servicePrincipal.roleAssignments()) {
                spUpdate.withoutRole(roleAssignment);
            }
            spUpdate.apply();

            System.out.println("Revoked the service principal's password credential and role assignment.");
            return true;
        } finally {
            if (application != null) {
                // Deleting the application also deletes its service principal.
                azureResourceManager.accessManagement().activeDirectoryApplications().deleteById(application.id());
            }
        }
    }

    private static final class PasswordHolder implements Consumer<PasswordCredential> {
        private String password;

        @Override
        public void accept(PasswordCredential passwordCredential) {
            this.password = passwordCredential.value();
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ManageServicePrincipal() {
    }
}
