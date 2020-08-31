// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
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
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

public class SpringCloudLiveOnlyTest extends AppPlatformTest {
    private static final String PIGGYMETRICS_CONFIG_URL = "https://github.com/Azure-Samples/piggymetrics-config";
    private static final String GATEWAY_JAR_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/gateway.jar";
    private static final String PIGGYMETRICS_TAR_GZ_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/piggymetrics.tar.gz";

    private static final String SPRING_CLOUD_SERVICE_PRINCIPAL = "03b39d0f-4213-4864-a245-b1476ec03169";

    SpringCloudLiveOnlyTest() {
        super(RunCondition.LIVE_ONLY); // need storage data-plane and url check
    }

    @Test
    public void canCRUDDeployment() throws Exception {
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        String deploymentName = generateRandomResourceName("deploy", 15);
        String deploymentName1 = generateRandomResourceName("deploy", 15);
        Region region = Region.US_EAST;

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        File jarFile = new File("gateway.jar");
        if (!jarFile.exists()) {
            HttpURLConnection connection = (HttpURLConnection) new URL(GATEWAY_JAR_URL).openConnection();
            connection.connect();
            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = new FileOutputStream(jarFile)) {
                IOUtils.copy(inputStream, outputStream);
            }
            connection.disconnect();
        }

        SpringApp app = service.apps().define(appName)
            .defineActiveDeployment(deploymentName)
            .withJarFile(jarFile)
            .withInstance(2)
            .withCpu(2)
            .withMemory(4)
            .withRuntime(RuntimeVersion.JAVA_11)
            .attach()
            .withDefaultPublicEndpoint()
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertNotNull(app.activeDeploymentName());
        Assertions.assertEquals(1, app.deployments().list().stream().count());

        Assertions.assertTrue(requestSuccess(app.url()));

        SpringAppDeployment deployment = app.getActiveDeployment();

        Assertions.assertEquals(2, deployment.settings().cpu());
        Assertions.assertEquals(4, deployment.settings().memoryInGB());
        Assertions.assertEquals(RuntimeVersion.JAVA_11, deployment.settings().runtimeVersion());
        Assertions.assertEquals(2, deployment.instances().size());

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

        deployment = app.deployments().define(deploymentName1)
            .withSourceCodeTarGzFile(gzFile)
            .withTargetModule("gateway")
            .withActivation()
            .create();
        app.refresh();

        Assertions.assertEquals(deploymentName1, app.activeDeploymentName());
        Assertions.assertEquals(1, deployment.settings().cpu());
        Assertions.assertNotNull(deployment.getLogFileUrl());

        Assertions.assertTrue(requestSuccess(app.url()));

        app.update()
            .withoutDefaultPublicEndpoint()
            .apply();
        Assertions.assertFalse(app.isPublic());

        app.deployments().deleteByName(deploymentName);
        Assertions.assertEquals(1, app.deployments().list().stream().count());
    }

    @Test
    public void canCreateCustomDomainWithSsl() throws Exception {
        String domainName = generateRandomResourceName("jsdkdemo-", 20) + ".com";
        String certOrderName = generateRandomResourceName("cert", 15);
        String vaultName = generateRandomResourceName("vault", 15);
        String certName = generateRandomResourceName("cert", 15);
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        Region region = Region.US_EAST;

        appPlatformManager.resourceManager().resourceGroups().define(rgName)
            .withRegion(region)
            .create();

        // create custom domain and certificate
        DnsZone dnsZone = dnsZoneManager.zones().define(domainName)
            .withExistingResourceGroup(rgName)
            .create();

        AppServiceDomain domain = appServiceManager.domains().define(domainName)
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
            .withExistingDnsZone(dnsZone)
            .create();

        AppServiceCertificateOrder certificateOrder = appServiceManager.certificateOrders().define(certOrderName)
            .withExistingResourceGroup(rgName)
            .withHostName(String.format("*.%s", domainName))
            .withWildcardSku()
            .withDomainVerification(domain)
            .withNewKeyVault(vaultName, region)
            .withAutoRenew(true)
            .create();

        Vault vault = keyVaultManager.vaults().getByResourceGroup(rgName, vaultName);
        vault.update()
            .defineAccessPolicy()
                .forServicePrincipal(clientIdFromFile())
                .allowSecretAllPermissions()
                .allowCertificateAllPermissions()
                .attach()
            .defineAccessPolicy()
                .forServicePrincipal(SPRING_CLOUD_SERVICE_PRINCIPAL)
                .allowCertificatePermissions(CertificatePermissions.GET, CertificatePermissions.LIST)
                .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.LIST)
                .attach()
            .apply();

        Secret secret = vault.secrets().getByName(certOrderName);

        byte[] certificate = Base64.getDecoder().decode(secret.value());

        // upload certificate
        CertificateClient certificateClient = new CertificateClientBuilder()
            .vaultUrl(vault.vaultUri())
            .pipeline(appPlatformManager.httpPipeline())
            .buildClient();

        certificateClient.importCertificate(
            new ImportCertificateOptions(certName, certificate)
                .setEnabled(true)
        );

        // get thumbprint
        KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(new ByteArrayInputStream(certificate), null);
        String alias = Collections.list(store.aliases()).get(0);
        String thumbprint = DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-1").digest(store.getCertificate(alias).getEncoded()));

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withCertificate("test", vault.vaultUri(), certName)
            .create();

        service.apps().define(appName).withDefaultActiveDeployment().withDefaultPublicEndpoint().create();
        SpringApp app = service.apps().getByName(appName);

        dnsZone.update()
            .withCNameRecordSet("www", app.fqdn())
            .withCNameRecordSet("ssl", app.fqdn())
            .apply();

        app.update()
            .withoutDefaultPublicEndpoint()
            .withCustomDomain(String.format("www.%s", domainName))
            .withCustomDomain(String.format("ssl.%s", domainName), thumbprint)
            .apply();

        Assertions.assertTrue(app.customDomains().validate(String.format("www.%s", domainName)).isValid());
        Assertions.assertTrue(requestSuccess(String.format("http://www.%s", domainName)));
        Assertions.assertTrue(requestSuccess(String.format("https://ssl.%s", domainName)));

        app.update()
            .withHttpsOnly()
            .apply();
        Assertions.assertTrue(checkRedirect(String.format("http://ssl.%s", domainName)));
    }

    private void extraTarGzSource(File folder, URL url) throws IOException {
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
