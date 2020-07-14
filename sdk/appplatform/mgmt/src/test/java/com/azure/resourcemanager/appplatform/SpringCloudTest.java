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
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
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

public class SpringCloudTest extends AppPlatformTest {
    private static final String PIGGYMETRICS_CONFIG_URL = "https://github.com/Azure-Samples/piggymetrics-config";
    private static final String GATEWAY_JAR_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/gateway.jar";
    private static final String PIGGYMETRICS_TAR_GZ_URL = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/piggymetrics.tar.gz";

    private static final String SPRING_CLOUD_SERVICE_PRINCIPAL = "03b39d0f-4213-4864-a245-b1476ec03169";

    SpringCloudTest() {
        super(RunCondition.LIVE_ONLY); // need storage data-plane and url check
    }

    @Test
    public void canCRUDSpringAppWithDeployment() throws IOException {
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        String deploymentName = generateRandomResourceName("deploy", 15);
        String deploymentName1 = generateRandomResourceName("deploy", 15);
        Region region = Region.US_EAST;

        Assertions.assertTrue(appPlatformManager.springServices().checkNameAvailability(serviceName, region).nameAvailable());

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withSku("B0")
            .create();

        Assertions.assertEquals("B0", service.sku().name());

        service.update()
            .withSku("S0")
            .apply();

        Assertions.assertEquals("S0", service.sku().name());

        service.update()
            .withGitUri(PIGGYMETRICS_CONFIG_URL)
            .apply();
        Assertions.assertEquals(PIGGYMETRICS_CONFIG_URL, service.serverProperties().configServer().gitProperty().uri());

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
            .withDefaultPublicEndpoint()
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertNotNull(app.activeDeployment());

        Assertions.assertTrue(requestSuccess(app.url()));

        app.update()
            .withoutDeployment(app.activeDeployment())
            .deployJar(deploymentName, jarFile)
            .apply();

        Assertions.assertNotNull(app.url());
        Assertions.assertEquals(deploymentName, app.activeDeployment());
        Assertions.assertEquals(1, app.deployments().list().stream().count());

        Assertions.assertTrue(requestSuccess(app.url()));

        SpringAppDeployment deployment = app.deployments().getByName(app.activeDeployment());
        deployment.update()
            .withCpu(2)
            .withMemory(4)
            .withRuntime(RuntimeVersion.JAVA_11)
            .withInstance(2)
            .apply();

        Assertions.assertEquals(2, deployment.settings().cpu());
        Assertions.assertEquals(4, deployment.settings().memoryInGB());
        Assertions.assertEquals(RuntimeVersion.JAVA_11, deployment.settings().runtimeVersion());
        Assertions.assertEquals(2, deployment.instances().size());

        File sourceCodeFolder = new File("piggymetrics");
        if (!sourceCodeFolder.exists() || sourceCodeFolder.isFile()) {
            if (sourceCodeFolder.isFile() && !sourceCodeFolder.delete()) {
                Assertions.fail();
            }
            extraTarGzSource(sourceCodeFolder, new URL(PIGGYMETRICS_TAR_GZ_URL));
        }

        deployment = app.deployments().define(deploymentName1)
            .withSourceCodeFolder(sourceCodeFolder)
            .withTargetModule("gateway")
            .withSettingsFromActiveDeployment()
            .withActivation()
            .create();
        app.refresh();

        Assertions.assertEquals(deploymentName1, app.activeDeployment());
        Assertions.assertEquals(2, deployment.settings().cpu());
        Assertions.assertNotNull(deployment.getLogFileUrl());

        Assertions.assertTrue(requestSuccess(app.url()));

        app.update()
            .withoutDefaultPublicEndpoint()
            .apply();
        Assertions.assertFalse(app.isPublic());
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

        Assertions.assertTrue(appPlatformManager.springServices().checkNameAvailability(serviceName, region).nameAvailable());

        SpringService service = appPlatformManager.springServices().define(serviceName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withCertificate("test", vault.vaultUri(), certName)
            .create();

        service.apps().define(appName).withDefaultPublicEndpoint().create();
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

    private boolean checkRedirect(String url) throws IOException {
        for (int i = 0; i < 60; ++i) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            try {
                connection.connect();
                if (200 <= connection.getResponseCode() && connection.getResponseCode() < 400) {
                    connection.getInputStream().close();
                    if (connection.getResponseCode() / 100 == 3) {
                        return true;
                    }
                    System.out.printf("Do request to %s with response code %d%n", url, connection.getResponseCode());
                }
            }  finally {
                connection.disconnect();
            }
            SdkContext.sleep(5000);
        }
        return false;
    }

    private boolean requestSuccess(String url) throws IOException {
        for (int i = 0; i < 60; ++i) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            try {
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    connection.getInputStream().close();
                    return true;
                }
                System.out.printf("Do request to %s with response code %d%n", url, connection.getResponseCode());
            } finally {
                connection.disconnect();
            }
            SdkContext.sleep(5000);
        }
        return false;
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
        }
        connection.disconnect();
    }
}
