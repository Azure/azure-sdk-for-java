// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.logging.ClientLogger;
import com.azure.v2.security.keyvault.certificates.models.CertificateContact;
import com.azure.v2.security.keyvault.certificates.models.CertificateContentType;
import com.azure.v2.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.CertificateProperties;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.IssuerProperties;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.v2.security.keyvault.certificates.models.MergeCertificateOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CertificateClientTest extends CertificateClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateClientTest.class);

    private CertificateClient certificateClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createCertificateClient(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion, null);
    }

    private void createCertificateClient(HttpClient httpClient, CertificateServiceVersion serviceVersion,
        String testTenantId) {
        certificateClient = getCertificateClientBuilder(httpClient, testTenantId, getEndpoint(), serviceVersion)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        createCertificateRunner((createCertificateOptions) -> {
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> result =
                certificateClient.beginCreateCertificate(createCertificateOptions.getName(), createCertificateOptions.getPolicy())
                    .poll()
                    .getValue();

            assertNotNull(result);
            KeyVaultCertificateWithPolicy certificate = result.join().getValue();
            assertNotNull(certificate);
            assertEquals(createCertificateOptions.getName(), certificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(RuntimeException.class, () -> certificateClient.beginCreateCertificate("", getDefaultCertificatePolicy()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class, () -> certificateClient.beginCreateCertificate("certificateName", null));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        updateCertificateRunner((originalCertificate, updateCertificate) -> {
            String certificateName = originalCertificate.getName();

            // Create the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, originalCertificate.getPolicy())
                    .poll()
                    .getValue();

            KeyVaultCertificateWithPolicy createdCertificate = createResult.join().getValue();

            // Update the certificate
            KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(updateCertificate);

            assertNotNull(updatedCertificate);
            assertEquals(certificateName, updatedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        updateDisabledCertificateRunner((certificate, updateCertificate) -> {
            String certificateName = certificate.getName();

            // Create and disable the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            KeyVaultCertificateWithPolicy createdCertificate = createResult.join().getValue();
            CertificateProperties disabledProperties = new CertificateProperties(certificateName)
                .setEnabled(false);

            certificateClient.updateCertificateProperties(disabledProperties);

            // Update the disabled certificate
            KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(updateCertificate);

            assertNotNull(updatedCertificate);
            assertEquals(certificateName, updatedCertificate.getName());
            assertFalse(updatedCertificate.getProperties().isEnabled());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            // Get the certificate
            KeyVaultCertificateWithPolicy retrievedCertificate = certificateClient.getCertificate(certificateName);

            assertNotNull(retrievedCertificate);
            assertEquals(certificateName, retrievedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getCertificateSpecificVersionRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            KeyVaultCertificateWithPolicy createdCertificate = createResult.join().getValue();

            // Get the specific version
            KeyVaultCertificate retrievedCertificate = certificateClient.getCertificateVersion(certificateName,
                createdCertificate.getProperties().getVersion());

            assertNotNull(retrievedCertificate);
            assertEquals(certificateName, retrievedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(HttpResponseException.class, () -> certificateClient.getCertificate("non-existing"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        deleteCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            // Delete the certificate
            CompletableFuture<Response<DeletedCertificate>> deleteResult =
                certificateClient.beginDeleteCertificate(certificateName)
                    .poll()
                    .getValue();

            DeletedCertificate deletedCertificate = deleteResult.join().getValue();

            assertNotNull(deletedCertificate);
            assertEquals(certificateName, deletedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(HttpResponseException.class, () ->
            certificateClient.beginDeleteCertificate("non-existing"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getDeletedCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create and delete the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            CompletableFuture<Response<DeletedCertificate>> deleteResult =
                certificateClient.beginDeleteCertificate(certificateName)
                    .poll()
                    .getValue();

            // Get the deleted certificate
            DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate(certificateName);

            assertNotNull(deletedCertificate);
            assertEquals(certificateName, deletedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(HttpResponseException.class, () -> certificateClient.getDeletedCertificate("non-existing"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        recoverDeletedCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create and delete the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            CompletableFuture<Response<DeletedCertificate>> deleteResult =
                certificateClient.beginDeleteCertificate(certificateName)
                    .poll()
                    .getValue();

            // Recover the certificate
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> recoverResult =
                certificateClient.beginRecoverDeletedCertificate(certificateName)
                    .poll()
                    .getValue();

            KeyVaultCertificateWithPolicy recoveredCertificate = recoverResult.join().getValue();

            assertNotNull(recoveredCertificate);
            assertEquals(certificateName, recoveredCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(HttpResponseException.class, () ->
            certificateClient.beginRecoverDeletedCertificate("non-existing"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void purgeDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        purgeDeletedCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create and delete the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            CompletableFuture<Response<DeletedCertificate>> deleteResult =
                certificateClient.beginDeleteCertificate(certificateName)
                    .poll()
                    .getValue();

            // Purge the certificate
            assertDoesNotThrow(() -> certificateClient.purgeDeletedCertificate(certificateName));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void purgeDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(HttpResponseException.class, () -> certificateClient.purgeDeletedCertificate("non-existing"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        backupCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            // Backup the certificate
            byte[] backupBytes = certificateClient.backupCertificate(certificateName);

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertThrows(HttpResponseException.class, () -> certificateClient.backupCertificate("non-existing"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        restoreCertificateRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create and backup the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            byte[] backupBytes = certificateClient.backupCertificate(certificateName);

            // Delete the certificate
            CompletableFuture<Response<DeletedCertificate>> deleteResult =
                certificateClient.beginDeleteCertificate(certificateName)
                    .poll()
                    .getValue();

            certificateClient.purgeDeletedCertificate(certificateName);

            // Restore the certificate
            KeyVaultCertificateWithPolicy restoredCertificate = certificateClient.restoreCertificateBackup(backupBytes);

            assertNotNull(restoredCertificate);
            assertEquals(certificateName, restoredCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        byte[] malformedBackup = "not a backup".getBytes();
        assertThrows(HttpResponseException.class, () -> certificateClient.restoreCertificateBackup(malformedBackup));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listCertificatesRunner((certificates) -> {
            for (Map.Entry<String, CertificatePolicy> certEntry : certificates.entrySet()) {
                CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                    certificateClient.beginCreateCertificate(certEntry.getKey(), certEntry.getValue())
                        .poll()
                        .getValue();
            }

            // List certificates
            for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
                assertTrue(certificates.containsKey(certificateProperties.getName()));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listCertificateVersionsRunner((certificate) -> {
            String certificateName = certificate.getName();

            // Create the certificate first
            CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                certificateClient.beginCreateCertificate(certificateName, certificate.getPolicy())
                    .poll()
                    .getValue();

            // List certificate versions
            for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificateVersions(certificateName)) {
                assertEquals(certificateName, certificateProperties.getName());
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listDeletedCertificatesRunner((certificates) -> {
            for (Map.Entry<String, CertificatePolicy> certEntry : certificates.entrySet()) {
                CompletableFuture<Response<KeyVaultCertificateWithPolicy>> createResult =
                    certificateClient.beginCreateCertificate(certEntry.getKey(), certEntry.getValue())
                        .poll()
                        .getValue();

                CompletableFuture<Response<DeletedCertificate>> deleteResult =
                    certificateClient.beginDeleteCertificate(certEntry.getKey())
                        .poll()
                        .getValue();
            }

            // List deleted certificates
            for (DeletedCertificate deletedCertificate : certificateClient.listDeletedCertificates()) {
                assertTrue(certificates.containsKey(deletedCertificate.getName()));
            }
        });
    }
}
