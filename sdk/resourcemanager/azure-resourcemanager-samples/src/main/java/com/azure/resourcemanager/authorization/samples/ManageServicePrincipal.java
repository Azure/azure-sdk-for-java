// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
//package com.azure.management.graphrbac.samples;
//
//import com.azure.core.credential.TokenCredential;
//import com.azure.core.http.policy.HttpLogDetailLevel;
//import com.azure.core.management.AzureEnvironment;
//import com.azure.identity.DefaultAzureCredentialBuilder;
//import com.azure.management.Azure;
//import com.azure.management.compute.models.VirtualMachine;
//import com.azure.management.graphrbac.ActiveDirectoryApplication;
//import com.azure.management.graphrbac.BuiltInRole;
//import com.azure.management.graphrbac.ServicePrincipal;
//import com.azure.management.resources.fluentcore.profile.AzureProfile;
//import com.azure.management.resources.fluentcore.utils.SdkContext;
//import com.azure.management.samples.Utils;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.Duration;
//import java.util.List;
//
///**
// * Azure Service Principal sample for managing Service Principal -
// *  - Create an Active Directory application
// *  - Create a Service Principal for the application and assign a role
// *  - Export the Service Principal to an authentication file
// *  - Use the file to list subcription virtual machines
// *  - Update the application
// *  - Update the service principal to revoke the password credential and the role
// *  - Delete the application and Service Principal.
// */
//
//public final class ManageServicePrincipal {
//    /**
//     * Main entry point.
//     * @param args the parameters
//     */
//    public static void main(String[] args) {
//        try {
//            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE, false);
//            final TokenCredential credential = new DefaultAzureCredentialBuilder()
//                .build();
//
//            Azure.Authenticated authenticated = Azure
//                .configure()
//                .withLogLevel(HttpLogDetailLevel.BASIC)
//                .authenticate(credential, profile);
//
//            String defaultSubscriptionId = authenticated.subscriptions().list().iterator().next().subscriptionId();
//            String tenantId = authenticated.tenants().list().iterator().next().tenantId();
//
//            authenticated = authenticated.withTenantId(tenantId);
//            runSample(authenticated, defaultSubscriptionId);
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Main function which runs the actual sample.
//     * @param authenticated instance of Authenticated
//     * @param defaultSubscriptionId default subscription id
//     * @return true if sample runs successfully
//     */
//    public static boolean runSample(Azure.Authenticated authenticated, String defaultSubscriptionId) {
//        ActiveDirectoryApplication activeDirectoryApplication = null;
//
//        try {
//            activeDirectoryApplication =
//                    createActiveDirectoryApplication(authenticated);
//
//            ServicePrincipal servicePrincipal =
//                    createServicePrincipalWithRoleForApplicationAndExportToFile(
//                            authenticated,
//                            activeDirectoryApplication,
//                            BuiltInRole.CONTRIBUTOR,
//                            defaultSubscriptionId,
//                            authFilePath);
//
//            SdkContext.sleep(15000);
//
//            useAuthFile(authFilePath);
//
//            manageApplication(authenticated, activeDirectoryApplication);
//
//            manageServicePrincipal(authenticated, servicePrincipal);
//
//            return true;
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        } finally {
//            if (activeDirectoryApplication != null) {
//                // this will delete Service Principal as well
//                authenticated.activeDirectoryApplications().deleteById(activeDirectoryApplication.id());
//            }
//        }
//
//        return false;
//    }
//
//    private static ActiveDirectoryApplication createActiveDirectoryApplication(Azure.Authenticated authenticated) throws Exception {
//        String name = authenticated.sdkContext().randomResourceName("adapp-sample", 20);
//        //create a self-sighed certificate
//        String domainName = name + ".com";
//        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
//        String certPassword = "StrongPass!12";
//        Certificate certificate = Certificate.createSelfSigned(domainName, certPassword);
//
//        // create Active Directory application
//        ActiveDirectoryApplication activeDirectoryApplication = authenticated.activeDirectoryApplications()
//                .define(name)
//                    .withSignOnUrl("https://github.com/Azure/azure-sdk-for-java/" + name)
//                    // password credentials definition
//                    .definePasswordCredential("password")
//                        .withPasswordValue(Utils.password())
//                        .withDuration(Duration.ofDays(700))
//                        .attach()
//                    // certificate credentials definition
//                    .defineCertificateCredential("cert")
//                        .withAsymmetricX509Certificate()
//                        .withPublicKey(Files.readAllBytes(Paths.get(certificate.getCerPath())))
//                        .withDuration(Duration.ofDays(100))
//                        .attach()
//                    .create();
//        System.out.println(activeDirectoryApplication.id() + " - " + activeDirectoryApplication.applicationId());
//        return activeDirectoryApplication;
//    }
//
//    private static ServicePrincipal createServicePrincipalWithRoleForApplicationAndExportToFile(
//            Azure.Authenticated authenticated,
//            ActiveDirectoryApplication activeDirectoryApplication,
//            BuiltInRole role,
//            String subscriptionId) throws Exception {
//
//        String name = authenticated.sdkContext().randomResourceName("sp-sample", 20);
//        //create a self-sighed certificate
//        String domainName = name + ".com";
//        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
//        String certPassword = "StrongPass!12";
//        Certificate certificate = Certificate.createSelfSigned(domainName, certPassword);
//
//        // create  a Service Principal and assign it to a subscription with the role Contributor
//        return authenticated.servicePrincipals()
//                .define("name")
//                    .withExistingApplication(activeDirectoryApplication)
//                    // password credentials definition
//                    .definePasswordCredential("ServicePrincipalAzureSample")
//                        .withPasswordValue(Utils.password())
//                        .attach()
//                    // certificate credentials definition
//                    .defineCertificateCredential("spcert")
//                        .withAsymmetricX509Certificate()
//                        .withPublicKey(Files.readAllBytes(Paths.get(certificate.getCerPath())))
//                        .withDuration(Duration.ofDays(7))
//                        // export the credentials to the file
//                        .withAuthFileToExport(new FileOutputStream(authFilePath))
//                        .withPrivateKeyFile(certificate.getPfxPath())
//                        .withPrivateKeyPassword(certPassword)
//                        .attach()
//                .withNewRoleInSubscription(role, subscriptionId)
//                .create();
//    }
//
//    private static void useAuthFile(String authFilePath) throws IOException {
//        // use just created auth file to sign in.
//        Azure azure = Azure.configure()
//                .withLogLevel(HttpLogDetailLevel.BODY)
//                .authenticate(new File(authFilePath))
//                .withDefaultSubscription();
//        // list virtualMachines, if any.
//        List<VirtualMachine> resourceGroups = azure.virtualMachines().list();
//        for (VirtualMachine vm : resourceGroups) {
//            Utils.print(vm);
//        }
//    }
//
//    private static void manageApplication(Azure.Authenticated authenticated, ActiveDirectoryApplication activeDirectoryApplication) {
//        activeDirectoryApplication.update()
//                // add another password credentials
//                .definePasswordCredential("password-1")
//                .withPasswordValue("P@ssw0rd-1")
//                .withDuration(Duration.ofDays(700))
//                .attach()
//                // add a reply url
//                .withReplyUrl("http://localhost:8080")
//                .apply();
//    }
//
//    private static void manageServicePrincipal(Azure.Authenticated authenticated, ServicePrincipal servicePrincipal) {
//        servicePrincipal.update()
//                .withoutCredential("ServicePrincipalAzureSample")
//                .withoutRole(servicePrincipal.roleAssignments().iterator().next())
//                .apply();
//    }
//
//    private ManageServicePrincipal() {
//    }
//
//    private static String basePath = null;
//    private static String getBasePath() throws URISyntaxException {
//        if (basePath == null) {
//            basePath = Paths.get(ManageServicePrincipal.class.getResource("/").toURI()).toString();
//        }
//        return basePath;
//    }
//
//    private static final class Certificate {
//        String pfxPath;
//        String cerPath;
//
//        public static Certificate createSelfSigned(String domainName, String password) throws Exception {
//           return new Certificate(domainName, password);
//        }
//
//        public String getPfxPath() {
//            return pfxPath;
//        }
//
//        public String getCerPath() {
//            return cerPath;
//        }
//
//        private Certificate(String domainName, String password) throws Exception {
//            pfxPath = Paths.get(getBasePath(), domainName + ".pfx").toString();
//            cerPath = Paths.get(getBasePath(), domainName + ".cer").toString();
//
//            System.out.println("Creating a self-signed certificate " + pfxPath + "...");
//            Utils.createCertificate(cerPath, pfxPath, domainName, password, "*." + domainName);
//        }
//    }
//}
