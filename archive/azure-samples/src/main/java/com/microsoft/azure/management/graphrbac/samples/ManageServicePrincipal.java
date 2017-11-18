/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryApplication;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import org.joda.time.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Azure Service Principal sample for managing Service Principal -
 *  - Create an Active Directory application
 *  - Create a Service Principal for the application and assign a role
 *  - Export the Service Principal to an authentication file
 *  - Use the file to list subcription virtual machines
 *  - Update the application
 *  - Update the service principal to revoke the password credential and the role
 *  - Delete the application and Service Principal.
 */

public final class ManageServicePrincipal {
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
            String defaultSubscriptionId = authenticated.subscriptions().list().get(0).subscriptionId();
            runSample(authenticated, defaultSubscriptionId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main function which runs the actual sample.
     * @param authenticated instance of Authenticated
     * @param defaultSubscriptionId default subscription id
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure.Authenticated authenticated, String defaultSubscriptionId) {
        ActiveDirectoryApplication activeDirectoryApplication = null;

        try {
            String authFileName = "myAuthFile.azureauth";
            String authFilePath = Paths.get(getBasePath(), authFileName).toString();

            activeDirectoryApplication =
                    createActiveDirectoryApplication(authenticated);

            ServicePrincipal servicePrincipal =
                    createServicePrincipalWithRoleForApplicationAndExportToFile(
                            authenticated,
                            activeDirectoryApplication,
                            BuiltInRole.CONTRIBUTOR,
                            defaultSubscriptionId,
                            authFilePath);

            SdkContext.sleep(15000);

            useAuthFile(authFilePath);

            manageApplication(authenticated, activeDirectoryApplication);

            manageServicePrincipal(authenticated, servicePrincipal);

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (activeDirectoryApplication != null) {
                // this will delete Service Principal as well
                authenticated.activeDirectoryApplications().deleteById(activeDirectoryApplication.id());
            }
        }

        return false;
    }

    private static ActiveDirectoryApplication createActiveDirectoryApplication(Azure.Authenticated authenticated) throws Exception {
        String name = SdkContext.randomResourceName("adapp-sample", 20);
        //create a self-sighed certificate
        String domainName = name + ".com";
        String certPassword = "StrongPass!12";
        Certificate certificate = Certificate.createSelfSigned(domainName, certPassword);

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
                        .withPublicKey(Files.readAllBytes(Paths.get(certificate.getCerPath())))
                        .withDuration(Duration.standardDays(100))
                        .attach()
                    .create();
        System.out.println(activeDirectoryApplication.id() + " - " + activeDirectoryApplication.applicationId());
        return activeDirectoryApplication;
    }

    private static ServicePrincipal createServicePrincipalWithRoleForApplicationAndExportToFile(
            Azure.Authenticated authenticated,
            ActiveDirectoryApplication activeDirectoryApplication,
            BuiltInRole role,
            String subscriptionId,
            String authFilePath) throws Exception {

        String name = SdkContext.randomResourceName("sp-sample", 20);
        //create a self-sighed certificate
        String domainName = name + ".com";
        String certPassword = "StrongPass!12";
        Certificate certificate = Certificate.createSelfSigned(domainName, certPassword);

        // create  a Service Principal and assign it to a subscription with the role Contributor
        return authenticated.servicePrincipals()
                .define("name")
                    .withExistingApplication(activeDirectoryApplication)
                    // password credentials definition
                    .definePasswordCredential("ServicePrincipalAzureSample")
                        .withPasswordValue("StrongPass!12")
                        .attach()
                    // certificate credentials definition
                    .defineCertificateCredential("spcert")
                        .withAsymmetricX509Certificate()
                        .withPublicKey(Files.readAllBytes(Paths.get(certificate.getCerPath())))
                        .withDuration(Duration.standardDays(7))
                        // export the credentials to the file
                        .withAuthFileToExport(new FileOutputStream(authFilePath))
                        .withPrivateKeyFile(certificate.getPfxPath())
                        .withPrivateKeyPassword(certPassword)
                        .attach()
                .withNewRoleInSubscription(role, subscriptionId)
                .create();
    }

    private static void useAuthFile(String authFilePath) throws IOException {
        // use just created auth file to sign in.
        Azure azure = Azure.configure()
                .withLogLevel(LogLevel.BODY)
                .authenticate(new File(authFilePath))
                .withDefaultSubscription();
        // list virtualMachines, if any.
        List<VirtualMachine> resourceGroups = azure.virtualMachines().list();
        for (VirtualMachine vm : resourceGroups) {
            Utils.print(vm);
        }
    }

    private static void manageApplication(Azure.Authenticated authenticated, ActiveDirectoryApplication activeDirectoryApplication) {
        activeDirectoryApplication.update()
                // add another password credentials
                .definePasswordCredential("password-1")
                .withPasswordValue("P@ssw0rd-1")
                .withDuration(Duration.standardDays(700))
                .attach()
                // add a reply url
                .withReplyUrl("http://localhost:8080")
                .apply();
    }

    private static void manageServicePrincipal(Azure.Authenticated authenticated, ServicePrincipal servicePrincipal) {
        servicePrincipal.update()
                .withoutCredential("ServicePrincipalAzureSample")
                .withoutRole(servicePrincipal.roleAssignments().iterator().next())
                .apply();
    }

    private ManageServicePrincipal() {
    }

    private static String basePath = null;
    private static String getBasePath() throws URISyntaxException {
        if (basePath == null) {
            basePath = Paths.get(ManageServicePrincipal.class.getResource("/").toURI()).toString();
        }
        return basePath;
    }

    private static final class Certificate {
        String pfxPath;
        String cerPath;

        public static Certificate createSelfSigned(String domainName, String password) throws Exception {
           return new Certificate(domainName, password);
        }

        public String getPfxPath() {
            return pfxPath;
        }

        public String getCerPath() {
            return cerPath;
        }

        private Certificate(String domainName, String password) throws Exception {
            pfxPath = Paths.get(getBasePath(), domainName + ".pfx").toString();
            cerPath = Paths.get(getBasePath(), domainName + ".cer").toString();

            System.out.println("Creating a self-signed certificate " + pfxPath + "...");
            Utils.createCertificate(cerPath, pfxPath, domainName, password, "*." + domainName);
        }
    }
}
