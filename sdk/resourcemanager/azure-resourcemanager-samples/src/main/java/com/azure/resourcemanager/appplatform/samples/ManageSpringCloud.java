// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appservice.models.AppServiceCertificateOrder;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.keyvault.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.models.Secret;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;

/**
 * Azure App Platform sample for managing Spring Cloud.
 *  - Create a Spring Cloud Service
 *  - Create 3 app: gateway, auth-service, account-service from sample
 *  - Open public endpoint for gateway
 *  - Create a custom domain
 *  - Add a CNAME to the gateway fully qualified domain name
 *  - Order a certificate
 *  - Save the certificate to a key vault
 *  - Assign certificate to spring cloud
 *  - Assign custom domain to gateway endpoint
 */
public class ManageSpringCloud {
    private static final String PIGGYMETRICS_TAR_GZ_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/piggymetrics.tar.gz";
    private static final String SPRING_CLOUD_SERVICE_PRINCIPAL = "03b39d0f-4213-4864-a245-b1476ec03169";

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @param clientId the aad client id in azure instance
     * @return true if sample runs successfully
     * @throws IllegalStateException unexcepted state
     */
    public static boolean runSample(Azure azure, String clientId) {
        final String rgName = azure.sdkContext().randomResourceName("rg", 24);
        final String serviceName  = azure.sdkContext().randomResourceName("service", 24);
        final Region region = Region.US_EAST;
        final String domainName = azure.sdkContext().randomResourceName("jsdkdemo-", 20) + ".com";
        final String certOrderName = azure.sdkContext().randomResourceName("cert", 15);
        final String vaultName = azure.sdkContext().randomResourceName("vault", 15);
        final String certName = azure.sdkContext().randomResourceName("cert", 15);

        try {
            azure.resourceGroups().define(rgName)
                .withRegion(region)
                .create();

            //============================================================
            // Create a spring cloud service with 3 apps: gateway, auth-service, account-service

            System.out.printf("Creating spring cloud service %s in resource group %s ...%n", serviceName, rgName);

            SpringService service = azure.springServices().define(serviceName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .create();

            System.out.printf("Created spring cloud service %s%n", service.name());
            Utils.print(service);

            // get source code of a sample project
            File gzFile = new File("piggymetrics.tar.gz");
            if (!gzFile.exists()) {
                HttpURLConnection connection = (HttpURLConnection) new URL(PIGGYMETRICS_TAR_GZ_URL).openConnection();
                connection.connect();
                try (InputStream inputStream = connection.getInputStream();
                     OutputStream outputStream = new FileOutputStream(gzFile)) {
                    IOUtils.copy(inputStream, outputStream);
                }
                connection.disconnect();
            }

            //============================================================
            // Create spring cloud app: gateway

            System.out.printf("Creating spring cloud app gateway in resource group %s ...%n", rgName);
            SpringApp gateway = service.apps().define("gateway")
                .defineActiveDeployment("default")
                    .withSourceCodeTarGzFile(gzFile)
                    .withTargetModule("gateway")
                    .attach()
                .withDefaultPublicEndpoint()
                .withHttpsOnly()
                .create();

            System.out.println("Created spring cloud service gateway");
            Utils.print(gateway);

            //============================================================
            // Create spring cloud app: auth-service

            System.out.printf("Creating spring cloud app auth-service in resource group %s ...%n", rgName);
            SpringApp authService = service.apps().define("auth-service")
                .defineActiveDeployment("default")
                    .withSourceCodeTarGzFile(gzFile)
                    .withTargetModule("auth-service")
                    .attach()
                .create();

            System.out.println("Created spring cloud service auth-service");
            Utils.print(authService);

            //============================================================
            // Create spring cloud app: account-service

            System.out.printf("Creating spring cloud app account-service in resource group %s ...%n", rgName);
            SpringApp accountService = service.apps().define("account-service")
                .defineActiveDeployment("default")
                    .withSourceCodeTarGzFile(gzFile)
                    .withTargetModule("account-service")
                    .attach()
                .create();

            System.out.println("Created spring cloud service account-service");
            Utils.print(accountService);

            //============================================================
            // Create App Service Domain and Certificate

            System.out.println("Purchasing a domain " + domainName + "...");

            AppServiceDomain domain = azure.appServiceDomains().define(domainName)
                .withExistingResourceGroup(rgName)
                .defineRegistrantContact()
                    .withFirstName("Jon")
                    .withLastName("Doe")
                    .withEmail("jondoe@contoso.com")
                    .withAddressLine1("123 4th Ave")
                    .withCity("Redmond")
                    .withStateOrProvince("WA")
                    .withCountry(CountryIsoCode.UNITED_STATES)
                    .withPostalCode("98052")
                    .withPhoneCountryCode(CountryPhoneCode.UNITED_STATES)
                    .withPhoneNumber("4258828080")
                    .attach()
                .withDomainPrivacyEnabled(true)
                .withAutoRenewEnabled(false)
                .create();
            System.out.println("Purchased domain " + domain.name());
            Utils.print(domain);

            DnsZone dnsZone = azure.dnsZones().getById(domain.dnsZoneId());
            gateway.refresh();

            System.out.printf("Updating dns with CNAME ssl.%s to %s%n", domainName, gateway.fqdn());
            dnsZone.update()
                .withCNameRecordSet("ssl", gateway.fqdn())
                .apply();

            System.out.printf("Purchasing a certificate for *.%s and save to %s in key vault named %s ...%n", domainName, certOrderName, vaultName);
            AppServiceCertificateOrder certificateOrder = azure.appServiceCertificateOrders().define(certOrderName)
                .withExistingResourceGroup(rgName)
                .withHostName(String.format("*.%s", domainName))
                .withWildcardSku()
                .withDomainVerification(domain)
                .withNewKeyVault(vaultName, region)
                .withAutoRenew(true)
                .create();
            System.out.printf("Purchased certificate: *.%s ...%n", domain.name());
            Utils.print(certificateOrder);

            System.out.printf("Updating key vault %s with access from %s, %s%n", vaultName, clientId, SPRING_CLOUD_SERVICE_PRINCIPAL);
            Vault vault = azure.vaults().getByResourceGroup(rgName, vaultName);
            vault.update()
                .defineAccessPolicy()
                    .forServicePrincipal(clientId)
                    .allowSecretAllPermissions()
                    .allowCertificateAllPermissions()
                    .attach()
                .defineAccessPolicy()
                    .forServicePrincipal(SPRING_CLOUD_SERVICE_PRINCIPAL)
                    .allowCertificatePermissions(CertificatePermissions.GET, CertificatePermissions.LIST)
                    .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.LIST)
                    .attach()
                .apply();
            System.out.printf("Updated key vault %s%n", vault.name());
            Utils.print(vault);

            Secret secret = vault.secrets().getByName(certOrderName);

            byte[] certificate = Base64.getDecoder().decode(secret.value());

            String thumbprint = secret.tags().get("Thumbprint");
            if (thumbprint == null || thumbprint.isEmpty()) {
                KeyStore store = KeyStore.getInstance("PKCS12");
                store.load(new ByteArrayInputStream(certificate), null);
                String alias = Collections.list(store.aliases()).get(0);
                thumbprint = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(store.getCertificate(alias).getEncoded()));
            }

            System.out.printf("Get certificate: %s%n", secret.value());
            System.out.printf("Certificate Thumbprint: %s%n", thumbprint);

            // upload certificate
            CertificateClient certificateClient = new CertificateClientBuilder()
                .vaultUrl(vault.vaultUri())
                .pipeline(service.manager().httpPipeline())
                .buildClient();

            System.out.printf("Uploading certificate to %s in key vault ...%n", certName);
            certificateClient.importCertificate(
                new ImportCertificateOptions(certName, certificate)
                    .setEnabled(true)
            );

            //============================================================
            // Update Certificate and Custom Domain for Spring Cloud
            System.out.println("Updating Spring Cloud Service with certificate ...");
            service.update()
                .withCertificate(certName, vault.vaultUri(), certName)
                .apply();

            System.out.printf("Updating Spring Cloud App with domain ssl.%s ...%n", domainName);
            gateway.update()
                .withCustomDomain(String.format("ssl.%s", domainName), thumbprint)
                .apply();

            System.out.printf("Successfully expose domain ssl.%s%n", domainName);

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Delete Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure, Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void extraTarGzSource(File folder, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        try (TarArchiveInputStream inputStream = new TarArchiveInputStream(new GzipCompressorInputStream(connection.getInputStream()))) {
            TarArchiveEntry entry;
            while ((entry = inputStream.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File file = new File(folder, entry.getName());
                File parent = file.getParentFile();
                if (parent.exists() || parent.mkdirs()) {
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        IOUtils.copy(inputStream, outputStream);
                    }
                } else {
                    throw new IllegalStateException("Cannot create directory: " + parent.getAbsolutePath());
                }
            }
        } finally {
            connection.disconnect();
        }
    }
}
