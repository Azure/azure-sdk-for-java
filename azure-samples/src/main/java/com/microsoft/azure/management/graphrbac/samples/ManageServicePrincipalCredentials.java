/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.graphrbac.samples;

import com.google.common.io.ByteStreams;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import org.joda.time.Duration;

import java.io.File;

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
     * @param authenticated instance of Authenticated
     * @param defaultSubscription default subscription id
     * @param environment the environment the sample is running in
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated, String defaultSubscription, AzureEnvironment environment) {
        final String spName         = Utils.createRandomName("sp");
        final String appName        = SdkContext.randomResourceName("app", 20);
        final String appUrl         = "https://" + appName;
        final String passwordName1  = SdkContext.randomResourceName("password", 20);
        final String password1      = "P@ssw0rd";
        final String passwordName2  = SdkContext.randomResourceName("password", 20);
        final String password2      = "StrongP@ss!12";
        final String certName1      = SdkContext.randomResourceName("cert", 20);
        final String raName         = SdkContext.randomUuid();
        String applicationId = "";
        try {
            // ============================================================
            // Create application

            System.out.println("Creating an Active Directory application " + appName + "...");

            ActiveDirectoryApplication application = authenticated.activeDirectoryApplications()
                    .define(appName)
                    .withSignOnUrl(appUrl)
                    .definePasswordCredential(passwordName1)
                        .withPasswordValue(password1)
                        .attach()
                    .definePasswordCredential(passwordName2)
                        .withPasswordValue(password2)
                        .attach()
                    .defineCertificateCredential(certName1)
                        .withAsymmetricX509Certificate()
                        .withPublicKey(ByteStreams.toByteArray(ManageServicePrincipalCredentials.class.getResourceAsStream("/myTest.cer")))
                        .withDuration(Duration.standardDays(1))
                        .attach()
                    .create();

            System.out.println("Created Active Directory application " + appName + ".");
            Utils.print(application);

            applicationId = application.id();

            // ============================================================
            // Create service principal

            System.out.println("Creating an Active Directory service principal " + spName + "...");

            ServicePrincipal servicePrincipal = authenticated.servicePrincipals()
                    .define(spName)
                    .withExistingApplication(application)
                    .create();

            System.out.println("Created service principal " + spName + ".");
            Utils.print(servicePrincipal);

            // ============================================================
            // Create role assignment

            System.out.println("Creating a Contributor role assignment " + raName + " for the service principal...");

            Thread.sleep(15000);

            RoleAssignment roleAssignment = authenticated.roleAssignments()
                    .define(raName)
                    .forServicePrincipal(servicePrincipal)
                    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                    .withSubscriptionScope(defaultSubscription)
                    .create();

            System.out.println("Created role assignment " + raName + ".");
            Utils.print(roleAssignment);

            // ============================================================
            // Verify the credentials are valid

            System.out.println("Verifying password credential " + passwordName1 + " is valid...");

            ApplicationTokenCredentials testCredential = new ApplicationTokenCredentials(
                    servicePrincipal.applicationId(), authenticated.tenantId(), password1, environment);
            try {
                Azure.authenticate(testCredential).withDefaultSubscription();

                System.out.println("Verified " + passwordName1 + " is valid.");
            } catch (Exception e) {
                System.out.println("Failed to verify " + passwordName1 + " is valid.");
            }

            System.out.println("Verifying password credential " + passwordName2 + " is valid...");

            testCredential = new ApplicationTokenCredentials(
                    servicePrincipal.applicationId(), authenticated.tenantId(), password2, environment);
            try {
                Azure.authenticate(testCredential).withDefaultSubscription();

                System.out.println("Verified " + passwordName2 + " is valid.");
            } catch (Exception e) {
                System.out.println("Failed to verify " + passwordName2 + " is valid.");
            }

            System.out.println("Verifying certificate credential " + certName1 + " is valid...");

            testCredential = new ApplicationTokenCredentials(
                    servicePrincipal.applicationId(),
                    authenticated.tenantId(),
                    ByteStreams.toByteArray(ManageServicePrincipalCredentials.class.getResourceAsStream("/myTest.pfx")),
                    "Abc123",
                    environment);
            try {
                Azure.authenticate(testCredential).withDefaultSubscription();

                System.out.println("Verified " + certName1 + " is valid.");
            } catch (Exception e) {
                System.out.println("Failed to verify " + certName1 + " is valid.");
            }

            // ============================================================
            // Revoke access of the 1st password credential
            System.out.println("Revoking access for password credential " + passwordName1 + "...");

            application.update()
                    .withoutCredential(passwordName1)
                    .apply();

            Thread.sleep(15000);

            System.out.println("Credential revoked.");

            // ============================================================
            // Verify the revoked password credential is no longer valid

            System.out.println("Verifying password credential " + passwordName1 + " is revoked...");

            testCredential = new ApplicationTokenCredentials(
                    servicePrincipal.applicationId(), authenticated.tenantId(), password1, environment);
            try {
                Azure.authenticate(testCredential).withDefaultSubscription();

                System.out.println("Failed to verify " + passwordName1 + " is revoked.");
            } catch (Exception e) {
                System.out.println("Verified " + passwordName1 + " is revoked.");
            }

            // ============================================================
            // Revoke the role assignment

            System.out.println("Revoking role assignment " + raName + "...");

            authenticated.roleAssignments().deleteById(roleAssignment.id());

            Thread.sleep(5000);

            // ============================================================
            // Verify the revoked password credential is no longer valid

            System.out.println("Verifying password credential " + passwordName2 + " has no access to subscription...");

            testCredential = new ApplicationTokenCredentials(
                    servicePrincipal.applicationId(), authenticated.tenantId(), password2, environment);
            try {
                Azure.authenticate(testCredential).withDefaultSubscription()
                        .resourceGroups().list();

                System.out.println("Failed to verify " + passwordName2 + " has no access to subscription.");
            } catch (Exception e) {
                System.out.println("Verified " + passwordName2 + " has no access to subscription.");
            }


            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting application: " + appName);
                authenticated.activeDirectoryApplications().deleteById(applicationId);
                System.out.println("Deleted application: " + appName);
            }
            catch (Exception e) {
                System.out.println("Did not create applications in Azure. No clean up is necessary");
            }
        }
        return false;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            ApplicationTokenCredentials credentials = ApplicationTokenCredentials.fromFile(credFile);
            Azure.Authenticated authenticated = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
                    .authenticate(credentials);

            runSample(authenticated, credentials.defaultSubscriptionId(), credentials.environment());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageServicePrincipalCredentials() {
    }
}
