// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform;

import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.appplatform.models.RuntimeVersion;
import com.azure.resourcemanager.appplatform.models.SpringApp;
import com.azure.resourcemanager.appplatform.models.SpringAppDeployment;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appservice.models.AppServiceDomain;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.resourcemanager.keyvault.models.CertificatePermissions;
import com.azure.resourcemanager.keyvault.models.SecretPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryIsoCode;
import com.azure.resourcemanager.resources.fluentcore.arm.CountryPhoneCode;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SpringCloudLiveOnlyTest extends AppPlatformTest {
    private static final String PIGGYMETRICS_CONFIG_URL = "https://github.com/Azure-Samples/piggymetrics-config";
    private static final String GATEWAY_JAR_URL
        = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/gateway.jar";
    private static final String PIGGYMETRICS_TAR_GZ_URL
        = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/raw/master/spring-cloud/piggymetrics.tar.gz";
    private static final String PETCLINIC_CONFIG_URL
        = "https://github.com/XiaofeiCao/spring-petclinic-microservices-config";
    private static final String PETCLINIC_GATEWAY_JAR_URL
        = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/tree/master/spring-cloud/api-gateway.jar";
    private static final String PETCLINIC_TAR_GZ_URL
        = "https://github.com/weidongxu-microsoft/azure-sdk-for-java-management-tests/tree/master/spring-cloud/petclinic.tar.gz";

    private static final String SPRING_CLOUD_SERVICE_OBJECT_ID = "938df8e2-2b9d-40b1-940c-c75c33494239";

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canCRUDDeployment() throws Exception {
        allowAllSSL();

        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        String deploymentName = generateRandomResourceName("deploy", 15);
        String deploymentName1 = generateRandomResourceName("deploy", 15);
        Region region = Region.US_EAST;

        SpringService service = appPlatformManager.springServices()
            .define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        File jarFile = downloadFile(GATEWAY_JAR_URL);

        SpringApp app = service.apps()
            .define(appName)
            .defineActiveDeployment(deploymentName)
            .withJarFile(jarFile)
            .withInstance(2)
            .withCpu(2)
            .withMemory(4)
            .withRuntime(RuntimeVersion.JAVA_11)
            .withJvmOptions("-Xms512m")
            .attach()
            .withDefaultPublicEndpoint()
            .create();

        Assertions.assertNotNull(app.url());
        Assertions.assertNotNull(app.activeDeploymentName());
        Assertions.assertEquals(1, app.deployments().list().stream().count());

        SpringAppDeployment deployment = app.getActiveDeployment();
        Assertions.assertEquals(RuntimeVersion.JAVA_11, deployment.runtimeVersion());
        Assertions.assertEquals("-Xms512m", deployment.jvmOptions());

        Assertions.assertTrue(requestSuccess(app.url()));

        Assertions.assertEquals("2", deployment.settings().resourceRequests().cpu());
        Assertions.assertEquals("4Gi", deployment.settings().resourceRequests().memory());
        //        Assertions.assertEquals(RuntimeVersion.JAVA_11, deployment.settings().runtimeVersion());
        Assertions.assertEquals(2, deployment.instances().size());

        File gzFile = downloadFile(PIGGYMETRICS_TAR_GZ_URL);

        deployment = app.deployments()
            .define(deploymentName1)
            .withSourceCodeTarGzFile(gzFile)
            .withTargetModule("gateway")
            .withActivation()
            .create();
        app.refresh();

        Assertions.assertEquals(deploymentName1, app.activeDeploymentName());
        Assertions.assertEquals("1", deployment.settings().resourceRequests().cpu());
        Assertions.assertNotNull(deployment.getLogFileUrl());

        Assertions.assertTrue(requestSuccess(app.url()));

        app.update().withoutDefaultPublicEndpoint().apply();
        Assertions.assertFalse(app.isPublic());

        app.deployments().deleteByName(deploymentName);
        Assertions.assertEquals(1, app.deployments().list().stream().count());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canCreateCustomDomainWithSsl() throws Exception {
        String domainName = generateRandomResourceName("jsdkdemo-", 20) + ".com";
        String certOrderName = generateRandomResourceName("cert", 15);
        String vaultName = generateRandomResourceName("vault", 15);
        String certName = generateRandomResourceName("cert", 15);
        String serviceName = generateRandomResourceName("springsvc", 15);
        String appName = "gateway";
        Region region = Region.US_EAST;

        allowAllSSL();
        String cerPassword = password();
        String resourcePath
            = Paths.get(this.getClass().getResource("/junit-platform.properties").toURI()).getParent().toString();
        String cerPath = resourcePath + domainName + ".cer";
        String pfxPath = resourcePath + domainName + ".pfx";
        createCertificate(cerPath, pfxPath, domainName, cerPassword, "ssl." + domainName, "ssl." + domainName);

        byte[] certificate = readAllBytes(new FileInputStream(pfxPath));

        appPlatformManager.resourceManager().resourceGroups().define(rgName).withRegion(region).create();

        // create custom domain and certificate
        DnsZone dnsZone = dnsZoneManager.zones().define(domainName).withExistingResourceGroup(rgName).create();

        AppServiceDomain domain = appServiceManager.domains()
            .define(domainName)
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

        Vault vault = keyVaultManager.vaults()
            .define(vaultName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .defineAccessPolicy()
            .forUser(azureCliSignedInUser().userPrincipalName())
            .allowSecretAllPermissions()
            .allowCertificateAllPermissions()
            .attach()
            .defineAccessPolicy()
            .forObjectId(SPRING_CLOUD_SERVICE_OBJECT_ID)
            .allowCertificatePermissions(CertificatePermissions.GET, CertificatePermissions.LIST)
            .allowSecretPermissions(SecretPermissions.GET, SecretPermissions.LIST)
            .attach()
            .create();

        // upload certificate
        CertificateClient certificateClient = new CertificateClientBuilder().vaultUrl(vault.vaultUri())
            .pipeline(appPlatformManager.httpPipeline())
            .buildClient();

        certificateClient.importCertificate(
            new ImportCertificateOptions(certName, certificate).setPassword(cerPassword).setEnabled(true));

        // get thumbprint
        KeyStore store = KeyStore.getInstance("PKCS12");
        store.load(new ByteArrayInputStream(certificate), cerPassword.toCharArray());
        String alias = Collections.list(store.aliases()).get(0);
        String thumbprint
            = printHexBinary(MessageDigest.getInstance("SHA-1").digest(store.getCertificate(alias).getEncoded()));

        SpringService service = appPlatformManager.springServices()
            .define(serviceName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withCertificate("test", vault.vaultUri(), certName)
            .create();

        service.apps().define(appName).withDefaultActiveDeployment().withDefaultPublicEndpoint().create();
        SpringApp app = service.apps().getByName(appName);

        dnsZone.update().withCNameRecordSet("www", app.fqdn()).withCNameRecordSet("ssl", app.fqdn()).apply();

        app.update()
            .withoutDefaultPublicEndpoint()
            .withCustomDomain(String.format("www.%s", domainName))
            .withCustomDomain(String.format("ssl.%s", domainName), thumbprint)
            .apply();

        Assertions.assertTrue(app.customDomains().validate(String.format("www.%s", domainName)).isValid());
        Assertions.assertTrue(requestSuccess(String.format("http://www.%s", domainName)));
        Assertions.assertTrue(requestSuccess(String.format("https://ssl.%s", domainName)));

        app.update().withHttpsOnly().withoutCustomDomain(String.format("www.%s", domainName)).apply();
        Assertions.assertTrue(checkRedirect(String.format("http://ssl.%s", domainName)));
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canCRUDEnterpriseTierDeployment() throws Exception {
        allowAllSSL();
        File tarGzFile = downloadFile(PETCLINIC_TAR_GZ_URL);
        File jarFile = downloadFile(PETCLINIC_GATEWAY_JAR_URL);

        String serviceName = generateRandomResourceName("springsvc", 15);
        Region region = Region.US_EAST;

        SpringService service = appPlatformManager.springServices()
            .define(serviceName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withEnterpriseTierSku()
            .create();

        String deploymentName = generateRandomResourceName("deploy", 15);

        List<String> apiGatewayConfigFilePatterns = Arrays.asList("api-gateway");
        String appName = "api-gateway";
        SpringApp gatewayApp = service.apps()
            .define(appName)
            .defineActiveDeployment(deploymentName)
            .withJarFile(jarFile)
            .withInstance(2)
            .withCpu(0.5)
            .withMemory(0.5)
            .withJvmOptions("-DskipTests=true")
            .attach()
            .withDefaultPublicEndpoint()
            .withConfigurationServiceBinding()
            .withServiceRegistryBinding()
            .create();

        SpringAppDeployment deployment = gatewayApp.deployments().getByName(deploymentName);
        Assertions.assertTrue(CoreUtils.isNullOrEmpty(deployment.configFilePatterns()));
        String jvmOptions = deployment.jvmOptions();
        Assertions.assertEquals(jvmOptions, "-DskipTests=true");

        List<String> configFilePatterns = Arrays.asList("api-gateway", "customers-service");
        service.update().withDefaultGitRepository(PETCLINIC_CONFIG_URL, "master", configFilePatterns).apply();

        deployment.update().withConfigFilePatterns(apiGatewayConfigFilePatterns).apply();

        deployment.refresh();
        Assertions.assertFalse(CoreUtils.isNullOrEmpty(deployment.configFilePatterns()));

        Assertions.assertNotNull(gatewayApp.url());
        Assertions.assertNotNull(gatewayApp.activeDeploymentName());
        Assertions.assertEquals(1, gatewayApp.deployments().list().stream().count());

        String appName2 = "customers-service";
        String customerServiceModule = "spring-petclinic-customers-service";
        List<String> customerServiceConfigFilePatterns = Arrays.asList("customers-service");
        SpringApp customerServiceApp = service.apps()
            .define(appName2)
            .defineActiveDeployment(deploymentName)
            .withSourceCodeTarGzFile(tarGzFile, customerServiceConfigFilePatterns)
            .withTargetModule(customerServiceModule)
            .attach()
            .withConfigurationServiceBinding()
            .withServiceRegistryBinding()
            .create();

        // no public endpoint
        Assertions.assertNull(customerServiceApp.url());

        SpringAppDeployment customersDeployment = customerServiceApp.deployments().getByName(deploymentName);
        Assertions.assertEquals(customerServiceConfigFilePatterns, customersDeployment.configFilePatterns());
    }

    private File downloadFile(String remoteFileUrl) throws Exception {
        String[] split = remoteFileUrl.split("/");
        String filename = split[split.length - 1];
        File downloaded = new File(filename);
        if (!downloaded.exists()) {
            HttpURLConnection connection = (HttpURLConnection) new URL(remoteFileUrl).openConnection();
            connection.connect();
            try (InputStream inputStream = connection.getInputStream();
                OutputStream outputStream = new FileOutputStream(downloaded)) {
                IOUtils.copy(inputStream, outputStream);
            } finally {
                connection.disconnect();
            }
        }
        return downloaded;
    }

    private void extraTarGzSource(File folder, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        try (TarArchiveInputStream inputStream
            = new TarArchiveInputStream(new GzipCompressorInputStream(connection.getInputStream()))) {
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

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            while (true) {
                int size = inputStream.read(data);
                if (size > 0) {
                    outputStream.write(data, 0, size);
                } else {
                    return outputStream.toByteArray();
                }
            }
        }
    }

    public static void createCertificate(String certPath, String pfxPath, String alias, String password, String cnName,
        String dnsName) throws IOException {
        if (new File(pfxPath).exists()) {
            return;
        }
        String validityInDays = "3650";
        String keyAlg = "RSA";
        String sigAlg = "SHA1withRSA";
        String keySize = "2048";
        String storeType = "pkcs12";
        String command = "keytool";
        String jdkPath = System.getProperty("java.home");
        if (jdkPath != null && !jdkPath.isEmpty()) {
            jdkPath = jdkPath.concat("\\bin");
            if (new File(jdkPath).isDirectory()) {
                command = String.format("%s%s%s", jdkPath, File.separator, command);
            }
        } else {
            return;
        }

        // Create Pfx file
        String[] commandArgs = {
            command,
            "-genkey",
            "-alias",
            alias,
            "-keystore",
            pfxPath,
            "-storepass",
            password,
            "-validity",
            validityInDays,
            "-keyalg",
            keyAlg,
            "-sigalg",
            sigAlg,
            "-keysize",
            keySize,
            "-storetype",
            storeType,
            "-dname",
            "CN=" + cnName,
            "-ext",
            "EKU=1.3.6.1.5.5.7.3.1" };
        if (dnsName != null) {
            List<String> args = new ArrayList<>(Arrays.asList(commandArgs));
            args.add("-ext");
            args.add("san=dns:" + dnsName);
            commandArgs = args.toArray(new String[0]);
        }
        cmdInvocation(commandArgs, true);

        // Create cer file i.e. extract public key from pfx
        File pfxFile = new File(pfxPath);
        if (pfxFile.exists()) {
            String[] certCommandArgs = {
                command,
                "-export",
                "-alias",
                alias,
                "-storetype",
                storeType,
                "-keystore",
                pfxPath,
                "-storepass",
                password,
                "-rfc",
                "-file",
                certPath };
            // output of keytool export command is going to error stream
            // although command is
            // executed successfully, hence ignoring error stream in this case
            cmdInvocation(certCommandArgs, true);

            // Check if file got created or not
            File cerFile = new File(pfxPath);
            if (!cerFile.exists()) {
                throw new IOException("Error occurred while creating certificate" + String.join(" ", certCommandArgs));
            }
        } else {
            throw new IOException("Error occurred while creating certificates" + String.join(" ", commandArgs));
        }
    }

    public static String cmdInvocation(String[] command, boolean ignoreErrorStream) throws IOException {
        String result = "";
        String error = "";

        Process process = new ProcessBuilder(command).start();
        try (InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            BufferedReader ebr = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));) {
            result = br.readLine();
            process.waitFor();
            error = ebr.readLine();
            if (error != null && (!"".equals(error))) {
                // To do - Log error message

                if (!ignoreErrorStream) {
                    throw new IOException(error, null);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while invoking command", e);
        }
        return result;
    }

    private static final char[] HEX_CODE = "0123456789ABCDEF".toCharArray();

    private static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(HEX_CODE[(b >> 4) & 0xF]);
            r.append(HEX_CODE[(b & 0xF)]);
        }
        return r.toString();
    }
}
