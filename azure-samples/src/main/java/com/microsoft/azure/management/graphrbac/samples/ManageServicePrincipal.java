/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.RoleAssignment;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.trafficmanager.samples.ManageTrafficManager;
import com.microsoft.rest.LogLevel;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Azure Service Principal sample for managing storage accounts -
 *  - Create a storage account
 *  - Get | regenerate storage account access keys
 *  - Create another storage account
 *  - List storage accounts
 *  - Delete a storage account.
 */

public final class ManageServicePrincipal {
    /**
     * Main function which runs the actual sample.
     * @param authenticated instance of Authenticated
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated) {
        String name = SdkContext.randomResourceName("javasdkapp", 20);
        ActiveDirectoryApplication activeDirectoryApplication = null;
        RoleAssignment roleAssignment = null;

        try {
            activeDirectoryApplication = createActiveDirectoryApplication(authenticated);
            ServicePrincipal servicePrincipal = createServicePrincipalForApplication(authenticated, activeDirectoryApplication);

            roleAssignment = assignRoleForServicePrincipal(authenticated, servicePrincipal);
            manageApplication(authenticated, activeDirectoryApplication);

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (roleAssignment != null) {
                authenticated.roleAssignments().deleteById(roleAssignment.id());
            }
            if (activeDirectoryApplication != null) {
                // this will delete Service Principal as well
                authenticated.activeDirectoryApplications().deleteById(activeDirectoryApplication.id());
            }
        }

        return false;
    }

    private static RoleAssignment assignRoleForServicePrincipal(Azure.Authenticated authenticated, ServicePrincipal servicePrincipal) {
        Subscription subscription = authenticated.subscriptions().list().get(0);
        return authenticated.roleAssignments()
                .define("myassignment")
                    .forServicePrincipal("anotherapp12")
                    .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                    .withSubscriptionScope(subscription.subscriptionId())
                    .create();
    }

    private static void manageApplication(Azure.Authenticated authenticated, ActiveDirectoryApplication activeDirectoryApplication) {
        activeDirectoryApplication.update()
                // delete password credential
                .withoutCredential("password")
                // add another password credentials
                .definePasswordCredential("password-1")
                    .withPasswordValue("P@ssw0rd-1")
                    .withDuration(Duration.standardDays(700))
                    .attach()
                // add reply url
                .withReplyUrl("http://localhost:8080")
                .apply();
    }

    private static void manageCertificatesForServicePrincipal(Azure.Authenticated authenticated, ServicePrincipal servicePrincipal) {

    }

    private static ActiveDirectoryApplication createActiveDirectoryApplication(Azure.Authenticated authenticated) throws Exception {
        String name = SdkContext.randomResourceName("ad-application-sample", 20);

        //create a self-sighed certificate
        String domainName = name + ".com";
        String pfxPath = ManageTrafficManager.class.getResource("/").getPath() + domainName + ".pfx";
        String cerPath = ManageTrafficManager.class.getResource("/").getPath() + domainName + ".cer";
        String certPassword = "StrongPass!12";
        System.out.println("Creating a self-signed certificate " + pfxPath + "...");
        Utils.createCertificate(cerPath, pfxPath, domainName, certPassword, "*." + domainName);

        // create Active Directory application
        ActiveDirectoryApplication activeDirectoryApplication = authenticated.activeDirectoryApplications()
                .define(name)
                    .withSignOnUrl("https://github.com/Azure/azure-sdk-for-java/" + name)
                    // password credentials definition
                    .definePasswordCredential("password")
                        .withPasswordValue("P@ssw0rd")
                        .withDuration(Duration.standardDays(700))
                        .attach()
                    // certificate credentials definition
                    .defineCertificateCredential("cert")
                        .withAsymmetricX509Certificate()
                        .withPublicKey(Files.readAllBytes(Paths.get(cerPath)))
                        .withDuration(Duration.standardDays(100))
                        .attach()
                    .create();
        System.out.println(activeDirectoryApplication.id() + " - " + activeDirectoryApplication.applicationId());
        return activeDirectoryApplication;
    }

    private static ServicePrincipal createServicePrincipalForApplication(Azure.Authenticated authenticated, ActiveDirectoryApplication activeDirectoryApplication) throws Exception {
        String name = SdkContext.randomResourceName("service-principal-sample", 20);

        //create a self-sighed certificate
        String domainName = name + ".com";
        String pfxPath = ManageTrafficManager.class.getResource("/").getPath() + domainName + ".pfx";
        String cerPath = ManageTrafficManager.class.getResource("/").getPath() + domainName + ".cer";
        String certPassword = "StrongPass!12";
        System.out.println("Creating a self-signed certificate " + pfxPath + "...");
        Utils.createCertificate(cerPath, pfxPath, domainName, certPassword, "*." + domainName);

        String authFile = "myCredFile.azureauth";

        // create  a Service Principal and assign it to a subscription with the role Contributor
        return authenticated.servicePrincipals()
                .define("name")
                    .withExistingApplication(activeDirectoryApplication)
                    // password credentials definition
                    .definePasswordCredential("ServicePrincipalAzureSample")
                    .withPasswordValue("StrongPass!12")
                    .withAuthFileToExport(new FileOutputStream(authFile))
                    .attach()
                // certificate credentials definition
                .defineCertificateCredential("spcert")
                    .withAsymmetricX509Certificate()
                    .withPublicKey(Files.readAllBytes(Paths.get(cerPath)))
                    .withDuration(Duration.standardDays(7))
                    .withAuthFileToExport(new FileOutputStream(authFile))
                    .withPrivateKeyFile(pfxPath)
                    .withPrivateKeyPassword("StrongPass!123")
                    .attach()
                .create();
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure.Authenticated authenticated = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credFile);

            runSample(authenticated);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageServicePrincipal() {
    }
}
