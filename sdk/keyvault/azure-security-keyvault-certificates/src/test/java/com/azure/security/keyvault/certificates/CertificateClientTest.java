// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CertificateClientTest extends CertificateClientTestBase {
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
        HttpPipeline httpPipeline = getHttpPipeline(httpClient, testTenantId);
        CertificateAsyncClient asyncClient = spy(new CertificateClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline)
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(asyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }

        certificateClient = new CertificateClient(asyncClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 25);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, certificatePolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();

            assertEquals(certName, expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateWithMultipleTenants(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, certificatePolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();

            assertEquals(certName, expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
        });

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, certificatePolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();

            assertEquals(certName, expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
        });
    }

    private void deleteAndPurgeCertificate(String certName) {
        SyncPoller<DeletedCertificate, Void> deletePoller = certificateClient.beginDeleteCertificate(certName);

        deletePoller.poll();
        deletePoller.waitForCompletion();

        certificateClient.purgeDeletedCertificate(certName);

        pollOnCertificatePurge(certName);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.beginCreateCertificate("", CertificatePolicy.getDefault()),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRunnableThrowsException(
            () -> certificateClient.beginCreateCertificate(testResourceNamer.randomName("tempCert", 20), null),
            NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRunnableThrowsException(() -> certificateClient.beginCreateCertificate(null, null),
            NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), true, originalTags);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate = certificateClient.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
            Map<String, String> returnedTags = keyVaultCertificate.getProperties().getTags();

            validateMapResponse(updatedTags, returnedTags);
        });
    }

    private void validateMapResponse(Map<String, String> expected, Map<String, String> returned) {
        for (String key : expected.keySet()) {
            String val = returned.get(key);
            String expectedVal = expected.get(key);

            assertEquals(expectedVal, val);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateDisabledCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), false, originalTags);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate =
                certificateClient.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
            Map<String, String> returnedTags = keyVaultCertificate.getProperties().getTags();

            validateMapResponse(updatedTags, returnedTags);

            assertFalse(keyVaultCertificate.getProperties().isEnabled());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificateWithPolicy getCertificate = certificateClient.getCertificate(certificateName);

            validatePolicy(certificate.getPolicy(), getCertificate.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateSpecificVersionRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate getCertificate =
                certificateClient.getCertificateVersion(certificateName, certificate.getProperties().getVersion());

            validateCertificate(certificate, getCertificate);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.getCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        deleteCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            SyncPoller<DeletedCertificate, Void> deletedKeyPoller =
                certificateClient.beginDeleteCertificate(certificateName);
            PollResponse<DeletedCertificate> pollResponse = deletedKeyPoller.poll();
            DeletedCertificate deletedCertificate = pollResponse.getValue();

            deletedKeyPoller.waitForCompletion();

            assertNotNull(deletedCertificate.getDeletedOn());
            assertNotNull(deletedCertificate.getRecoveryId());
            assertNotNull(deletedCertificate.getScheduledPurgeDate());
            assertEquals(certificateName, deletedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.beginDeleteCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getDeletedCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            SyncPoller<DeletedCertificate, Void> deletedKeyPoller =
                certificateClient.beginDeleteCertificate(certificateName);

            deletedKeyPoller.waitForCompletion();

            DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate(certificateName);

            assertNotNull(deletedCertificate.getDeletedOn());
            assertNotNull(deletedCertificate.getRecoveryId());
            assertNotNull(deletedCertificate.getScheduledPurgeDate());
            assertEquals(certificateName, deletedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.getDeletedCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificate createdCertificate = certPoller.getFinalResult();
            SyncPoller<DeletedCertificate, Void> deletedKeyPoller =
                certificateClient.beginDeleteCertificate(certificateName);

            deletedKeyPoller.waitForCompletion();

            SyncPoller<KeyVaultCertificateWithPolicy, Void> recoverPoller =
                certificateClient.beginRecoverDeletedCertificate(certificateName);
            PollResponse<KeyVaultCertificateWithPolicy> recoverPollResponse = recoverPoller.poll();

            KeyVaultCertificate recoveredCert = recoverPollResponse.getValue();

            recoverPoller.waitForCompletion();

            assertEquals(certificateName, recoveredCert.getName());

            validateCertificate(createdCertificate, recoveredCert);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.beginRecoverDeletedCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        backupCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            byte[] backupBytes = (certificateClient.backupCertificate(certificateName));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.backupCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        restoreCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, initialPolicy);

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy createdCert = certPoller.getFinalResult();
            byte[] backupBytes = (certificateClient.backupCertificate(certificateName));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            deleteAndPurgeCertificate(certificateName);

            sleepInRecordMode(40000);

            KeyVaultCertificateWithPolicy restoredCertificate = certificateClient.restoreCertificateBackup(backupBytes);

            assertEquals(certificateName, restoredCertificate.getName());

            validatePolicy(restoredCertificate.getPolicy(), createdCert.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateOperationRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, setupPolicy());
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> retrievePoller =
                certificateClient.getCertificateOperation(certificateName);

            retrievePoller.waitForCompletion();
            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy retrievedCert = retrievePoller.getFinalResult();
            KeyVaultCertificateWithPolicy expectedCert = certPoller.getFinalResult();

            validateCertificate(expectedCert, retrievedCert);
            validatePolicy(expectedCert.getPolicy(), retrievedCert.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        cancelCertificateOperationRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

            certPoller.poll();
            certPoller.cancelOperation();
            certPoller.waitUntil(LongRunningOperationStatus.USER_CANCELLED);

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();

            assertFalse(certificate.getProperties().isEnabled());

            certPoller.waitForCompletion();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        deleteCertificateOperationRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, CertificatePolicy.getDefault());

            certPoller.waitForCompletion();

            CertificateOperation certificateOperation = certificateClient.deleteCertificateOperation(certificateName);

            assertEquals("completed", certificateOperation.getStatus());
            assertRestException(() -> certificateClient.deleteCertificateOperation(certificateName),
                ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificatePolicyRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, setupPolicy());

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();

            validatePolicy(setupPolicy(), certificate.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateCertificatePolicyRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certificateName, setupPolicy());

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();

            certificate.getPolicy().setExportable(false);

            CertificatePolicy policy =
                certificateClient.updateCertificatePolicy(certificateName, certificate.getPolicy());

            validatePolicy(certificate.getPolicy(), policy);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        assertRestException(() -> certificateClient.restoreCertificateBackup(keyBackupBytes),
            ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        listCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                    certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

                certPoller.waitForCompletion();
            }

            sleepInRecordMode(90000);

            for (CertificateProperties actualKey : certificateClient.listPropertiesOfCertificates()) {
                certificates.remove(actualKey.getName());
            }

            assertEquals(0, certificates.size());
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listPropertiesOfCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        listPropertiesOfCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                    certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

                certPoller.waitForCompletion();
            }

            sleepInRecordMode(90000);

            for (CertificateProperties actualKey : certificateClient.listPropertiesOfCertificates(false, Context.NONE)) {
                certificates.remove(actualKey.getName());
            }

            assertEquals(0, certificates.size());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        createIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = certificateClient.createIssuer(issuer);

            validateIssuer(issuer, createdIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.createIssuer(new CertificateIssuer("", "")),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNullProvider(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.createIssuer(new CertificateIssuer("", null)),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRunnableThrowsException(() -> certificateClient.createIssuer(null), NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = certificateClient.createIssuer(issuer);
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(issuer.getName());

            validateIssuer(issuer, retrievedIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.backupCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        deleteCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = certificateClient.createIssuer(issuer);
            CertificateIssuer deletedIssuer = certificateClient.deleteIssuer(issuer.getName());

            validateIssuer(issuer, deletedIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.backupCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateIssuers(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        listCertificateIssuersRunner((certificateIssuers) -> {
            HashMap<String, CertificateIssuer> certificateIssuersToList = new HashMap<>(certificateIssuers);

            for (CertificateIssuer issuer : certificateIssuersToList.values()) {
                CertificateIssuer certificateIssuer = certificateClient.createIssuer(issuer);

                validateIssuer(issuer, certificateIssuer);
            }

            for (IssuerProperties issuerProperties : certificateClient.listPropertiesOfIssuers()) {
                certificateIssuersToList.remove(issuerProperties.getName());
            }

            assertEquals(0, certificateIssuersToList.size());

            for (CertificateIssuer issuer : certificateIssuers.values()) {
                certificateClient.deleteIssuer(issuer.getName());
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void setContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        List<CertificateContact> contacts = Arrays.asList(setupContact());

        certificateClient.setContacts(contacts)
            .forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        certificateClient.deleteContacts();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void listContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        List<CertificateContact> contacts = Arrays.asList(setupContact());

        certificateClient.setContacts(contacts)
            .forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));

        sleepInRecordMode(6000);

        certificateClient.listContacts().stream()
            .forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void deleteContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        List<CertificateContact> contacts = Arrays.asList(setupContact());

        certificateClient.setContacts(contacts)
            .forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));

        PagedIterable<CertificateContact> certificateContacts = certificateClient.deleteContacts();

        validateContact(setupContact(), certificateContacts.iterator().next());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperationNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.getCertificateOperation("non-existing").poll(),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() -> certificateClient.getCertificatePolicy("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        String certName = testResourceNamer.randomName("testListCertVersion", 25);
        int versionsToCreate = 5;

        for (int i = 0; i < versionsToCreate; i++) {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

            certPoller.waitForCompletion();
        }

        int createdVersions = 0;

        for (CertificateProperties properties : certificateClient.listPropertiesOfCertificateVersions(certName)) {
            createdVersions++;

            assertEquals(properties.getName(), certName);
        }

        assertEquals(versionsToCreate, createdVersions);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        // Skip when running against the service to avoid having pipeline runs take longer than they have to.
        if (interceptorManager.isLiveMode()) {
            return;
        }

        listDeletedCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToDelete = new HashSet<>(certificates);

            for (String certName : certificatesToDelete) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                    certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());
                PollResponse<CertificateOperation> pollResponse = certPoller.poll();

                while (!pollResponse.getStatus().isComplete()) {
                    sleepInRecordMode(1000);

                    pollResponse = certPoller.poll();
                }
            }

            for (String certName : certificates) {
                SyncPoller<DeletedCertificate, Void> poller = certificateClient.beginDeleteCertificate(certName);
                PollResponse<DeletedCertificate> pollResponse = poller.poll();

                while (!pollResponse.getStatus().isComplete()) {
                    sleepInRecordMode(1000);

                    pollResponse = poller.poll();
                }

                assertNotNull(pollResponse.getValue());
            }

            sleepInRecordMode(90000);

            Iterable<DeletedCertificate> deletedCertificates = certificateClient.listDeletedCertificates();

            assertTrue(deletedCertificates.iterator().hasNext());

            for (DeletedCertificate deletedCertificate : deletedCertificates) {
                assertNotNull(deletedCertificate.getDeletedOn());
                assertNotNull(deletedCertificate.getRecoveryId());

                certificatesToDelete.remove(deletedCertificate.getName());
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        importCertificateRunner((importCertificateOptions) -> {
            KeyVaultCertificateWithPolicy importedCertificate =
                certificateClient.importCertificate(importCertificateOptions);

            assertTrue(toHexString(importedCertificate.getProperties().getX509Thumbprint())
                .equalsIgnoreCase("7cb8b7539d87ba7215357b9b9049dff2d3fa59ba"));
            assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());

            // Load the CER part into X509Certificate object
            X509Certificate x509Certificate = null;

            try {
                x509Certificate = loadCerToX509Certificate(importedCertificate);
            } catch (CertificateException | IOException e) {
                e.printStackTrace();
                fail();
            }

            assertEquals("CN=KeyVaultTest", x509Certificate.getSubjectX500Principal().getName());
            assertEquals("CN=Root Agency", x509Certificate.getIssuerX500Principal().getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertRestException(() ->
                certificateClient.mergeCertificate(
                    new MergeCertificateOptions(testResourceNamer.randomName("testCert", 20),
                        Arrays.asList("test".getBytes()))),
            HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importPemCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) throws IOException {
        createCertificateClient(httpClient, serviceVersion);

        importPemCertificateRunner((importCertificateOptions) -> {
            KeyVaultCertificateWithPolicy importedCertificate =
                certificateClient.importCertificate(importCertificateOptions);

            assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());
            assertEquals(CertificateContentType.PEM, importedCertificate.getPolicy().getContentType());
        });
    }

    private void pollOnCertificatePurge(String certificateName) {
        int pendingPollCount = 0;

        while (pendingPollCount < 10) {
            DeletedCertificate deletedCertificate = null;

            try {
                deletedCertificate = certificateClient.getDeletedCertificate(certificateName);
            } catch (ResourceNotFoundException ignored) {
            }

            if (deletedCertificate != null) {
                sleepInRecordMode(2000);

                pendingPollCount += 1;
            } else {
                return;
            }
        }

        System.err.printf("Deleted Key %s was not purged \n", certificateName);
    }

}
