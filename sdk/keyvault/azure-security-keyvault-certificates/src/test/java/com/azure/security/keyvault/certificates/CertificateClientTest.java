// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.implementation.models.KeyVaultErrorException;
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
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
        certificateClient = getCertificateClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), testTenantId,
            getEndpoint(), serviceVersion)
            .buildClient();
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 25);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                setPlaybackSyncPollerPollInterval(certificateClient.beginCreateCertificate(certName, certificatePolicy));

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
                setPlaybackSyncPollerPollInterval(certificateClient.beginCreateCertificate(certName, certificatePolicy));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();

            assertEquals(certName, expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
        });

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                setPlaybackSyncPollerPollInterval(certificateClient.beginCreateCertificate(certName, certificatePolicy));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();

            assertEquals(certName, expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
        });
    }

    private void deleteAndPurgeCertificate(String certName) {
        SyncPoller<DeletedCertificate, Void> deletePoller = setPlaybackSyncPollerPollInterval(
            certificateClient.beginDeleteCertificate(certName));

        deletePoller.poll();
        deletePoller.waitForCompletion();

        certificateClient.purgeDeletedCertificate(certName);

        pollOnCertificatePurge(certName);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.beginCreateCertificate("", CertificatePolicy.getDefault()),
            KeyVaultErrorException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertThrows(NullPointerException.class,
            () -> certificateClient.beginCreateCertificate(testResourceNamer.randomName("tempCert", 20), null));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertThrows(NullPointerException.class, () -> certificateClient.beginCreateCertificate(null, null));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                setPlaybackSyncPollerPollInterval(certificateClient.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault(), true, originalTags));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate = certificateClient.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
            Map<String, String> returnedTags = keyVaultCertificate.getProperties().getTags();

            validateMapResponse(updatedTags, returnedTags);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateDisabledCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), false, originalTags));

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
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificateWithPolicy getCertificate = certificateClient.getCertificate(certificateName);

            assertPolicy(certificate.getPolicy(), getCertificate.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateSpecificVersionRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate getCertificate =
                certificateClient.getCertificateVersion(certificateName, certificate.getProperties().getVersion());

            assertCertificate(certificate, getCertificate);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.getCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        deleteCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

            certPoller.waitForCompletion();

            SyncPoller<DeletedCertificate, Void> deletedKeyPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginDeleteCertificate(certificateName));

            DeletedCertificate deletedCertificate = deletedKeyPoller.waitForCompletion().getValue();

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

        assertResponseException(() -> certificateClient.beginDeleteCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getDeletedCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

            certPoller.waitForCompletion();

            SyncPoller<DeletedCertificate, Void> deletedKeyPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginDeleteCertificate(certificateName));

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

        assertResponseException(() -> certificateClient.getDeletedCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

            certPoller.waitForCompletion();

            KeyVaultCertificate createdCertificate = certPoller.getFinalResult();
            SyncPoller<DeletedCertificate, Void> deletedKeyPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginDeleteCertificate(certificateName));

            deletedKeyPoller.waitForCompletion();

            SyncPoller<KeyVaultCertificateWithPolicy, Void> recoverPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginRecoverDeletedCertificate(certificateName));

            KeyVaultCertificate recoveredCert = recoverPoller.waitForCompletion().getValue();

            assertEquals(certificateName, recoveredCert.getName());

            assertCertificate(createdCertificate, recoveredCert);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.beginRecoverDeletedCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        backupCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

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

        assertResponseException(() -> certificateClient.backupCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        restoreCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, initialPolicy));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy createdCert = certPoller.getFinalResult();
            byte[] backupBytes = (certificateClient.backupCertificate(certificateName));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            deleteAndPurgeCertificate(certificateName);

            sleepIfRunningAgainstService(40000);

            KeyVaultCertificateWithPolicy restoredCertificate = certificateClient.restoreCertificateBackup(backupBytes);

            assertEquals(certificateName, restoredCertificate.getName());

            assertPolicy(restoredCertificate.getPolicy(), createdCert.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateOperationRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, setupPolicy()));
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> retrievePoller = setPlaybackSyncPollerPollInterval(
                certificateClient.getCertificateOperation(certificateName));

            retrievePoller.waitForCompletion();
            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy retrievedCert = retrievePoller.getFinalResult();
            KeyVaultCertificateWithPolicy expectedCert = certPoller.getFinalResult();

            assertCertificate(expectedCert, retrievedCert);
            assertPolicy(expectedCert.getPolicy(), retrievedCert.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        cancelCertificateOperationRunner(certName -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault())
                    .setPollInterval(Duration.ofMillis(100));

            certPoller.waitUntil(LongRunningOperationStatus.IN_PROGRESS);
            certPoller.cancelOperation();

            try {
                certPoller.waitUntil(Duration.ofSeconds(60), LongRunningOperationStatus.fromString("cancelled", true));
            } catch (NoSuchElementException e) {
                // The operation did not reach the expected status, either because it was completed before it could be
                // canceled or there was a service timing issue when attempting to cancel the operation.
                return;
            }

            PollResponse<CertificateOperation> pollResponse = certPoller.poll();

            assertTrue(pollResponse.getValue().getCancellationRequested());

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();

            assertFalse(certificate.getProperties().isEnabled());
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        deleteCertificateOperationRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, CertificatePolicy.getDefault()));

            certPoller.waitForCompletion();

            CertificateOperation certificateOperation = certificateClient.deleteCertificateOperation(certificateName);

            assertEquals("completed", certificateOperation.getStatus());
            assertResponseException(() -> certificateClient.deleteCertificateOperation(certificateName),
                ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificatePolicyRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, setupPolicy()));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();

            assertPolicy(setupPolicy(), certificate.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateCertificatePolicyRunner((certificateName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certificateName, setupPolicy()));

            certPoller.waitForCompletion();

            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();

            certificate.getPolicy().setExportable(false);

            CertificatePolicy policy =
                certificateClient.updateCertificatePolicy(certificateName, certificate.getPolicy());

            assertPolicy(certificate.getPolicy(), policy);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        assertResponseException(() -> certificateClient.restoreCertificateBackup(keyBackupBytes),
            ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        listCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                    certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()));

                certPoller.waitForCompletion();
            }

            sleepIfRunningAgainstService(30000);

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
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                    certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()));

                certPoller.waitForCompletion();
            }

            sleepIfRunningAgainstService(30000);

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

            assertIssuerCreatedCorrectly(issuer, createdIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.createIssuer(new CertificateIssuer("", "")),
            KeyVaultErrorException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNullProvider(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.createIssuer(new CertificateIssuer("", null)),
            KeyVaultErrorException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertThrows(NullPointerException.class, () -> certificateClient.createIssuer(null));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        getCertificateIssuerRunner((issuer) -> {
            certificateClient.createIssuer(issuer);
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(issuer.getName());

            assertIssuerCreatedCorrectly(issuer, retrievedIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.backupCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        deleteCertificateIssuerRunner((issuer) -> {
            certificateClient.createIssuer(issuer);
            CertificateIssuer deletedIssuer = certificateClient.deleteIssuer(issuer.getName());

            assertIssuerCreatedCorrectly(issuer, deletedIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.backupCertificate("non-existing"),
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

                assertIssuerCreatedCorrectly(issuer, certificateIssuer);
            }

            for (IssuerProperties issuerProperties : certificateClient.listPropertiesOfIssuers()) {
                certificateIssuersToList.remove(issuerProperties.getName());
            }

            assertEquals(0, certificateIssuersToList.size());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateIssuerRunner((issuerToCreate, issuerToUpdate) -> {
            certificateClient.createIssuer(issuerToCreate);
            CertificateIssuer updatedIssuer = certificateClient.updateIssuer(issuerToUpdate);

            assertIssuerUpdatedCorrectly(issuerToCreate, updatedIssuer);
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

        sleepIfRunningAgainstService(6000);

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

        assertResponseException(() -> certificateClient.getCertificateOperation("non-existing").poll(),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.getCertificatePolicy("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        String certName = testResourceNamer.randomName("testListCertVersion", 25);
        int versionsToCreate = 5;

        for (int i = 0; i < versionsToCreate; i++) {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()));

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

        listDeletedCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToDelete = new HashSet<>(certificates);

            for (String certName : certificatesToDelete) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackSyncPollerPollInterval(
                    certificateClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()));

                assertNotNull(certPoller.waitForCompletion().getValue());
            }

            for (String certName : certificates) {
                SyncPoller<DeletedCertificate, Void> poller = setPlaybackSyncPollerPollInterval(
                    certificateClient.beginDeleteCertificate(certName));

                assertNotNull(poller.waitForCompletion().getValue());
            }

            sleepIfRunningAgainstService(30000);

            certificateClient.listDeletedCertificates()
                .forEach(deletedCertificate -> certificatesToDelete.remove(deletedCertificate.getName()));

            assertEquals(0, certificatesToDelete.size());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        importCertificateRunner((importCertificateOptions) -> {
            KeyVaultCertificateWithPolicy importedCertificate =
                certificateClient.importCertificate(importCertificateOptions);

            assertTrue("73b4319cdf38e0797084535d9c02fd04d4b2b2e6"
                .equalsIgnoreCase(importedCertificate.getProperties().getX509ThumbprintAsString()));
            assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());

            // Load the CER part into X509Certificate object
            X509Certificate x509Certificate = assertDoesNotThrow(
                () -> loadCerToX509Certificate(importedCertificate.getCer()));

            assertTrue(x509Certificate.getSubjectX500Principal().getName()
                .contains("CN=Test,OU=Test,O=Contoso,L=Redmond,ST=WA,C=US"));
            assertTrue(x509Certificate.getIssuerX500Principal().getName()
                .contains("CN=Test,OU=Test,O=Contoso,L=Redmond,ST=WA,C=US"));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledForJreRange(min = JRE.JAVA_17) // Access to sun.security.* classes used here is not possible on Java 17+.
    public void mergeCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        try {
            createCertificateClient(httpClient, serviceVersion);

            String certificateName = testResourceNamer.randomName("testCert", 25);
            String issuer = "Unknown";
            String subject = "CN=MyCert";
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> createCertificatePoller =
                setPlaybackSyncPollerPollInterval(certificateClient.beginCreateCertificate(certificateName,
                    new CertificatePolicy(issuer, subject).setCertificateTransparent(false)));

            createCertificatePoller.waitUntil(LongRunningOperationStatus.IN_PROGRESS);

            CertificateOperation certificateOperation = createCertificatePoller.poll().getValue();
            byte[] certificateSignRequest = certificateOperation.getCsr();
            PKCS10CertificationRequest pkcs10CertificationRequest =
                new PKCS10CertificationRequest(certificateSignRequest);
            byte[] certificateToMerge = FakeCredentialsForTests.FAKE_PEM_CERTIFICATE_FOR_MERGE.getBytes();
            X509Certificate x509ToMerge = loadCerToX509Certificate(certificateToMerge);
            PrivateKey privateKey = loadPrivateKey("priv8.der");
            Date notBefore = new Date();
            Date notAfter = new Date(notBefore.getTime() + 60 * 86400000L);

            X500Name mergeIssuer = new X500Name(x509ToMerge.getSubjectX500Principal().getName());
            X500Name mergeSubject = pkcs10CertificationRequest.getSubject();
            AlgorithmIdentifier signatureAlgorithmIdentifier =
                new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
            AlgorithmIdentifier digestAlgorithmIdentifier =
                new DefaultDigestAlgorithmIdentifierFinder().find(signatureAlgorithmIdentifier);
            AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(privateKey.getEncoded());
            SubjectPublicKeyInfo publicKeyInfo = pkcs10CertificationRequest.getSubjectPublicKeyInfo();
            X509v3CertificateBuilder x509CertificateBuilder =
                new X509v3CertificateBuilder(mergeIssuer, BigInteger.ONE, notBefore, notAfter, mergeSubject,
                    publicKeyInfo);

            ContentSigner contentSigner =
                new BcRSAContentSignerBuilder(signatureAlgorithmIdentifier, digestAlgorithmIdentifier)
                    .build(asymmetricKeyParameter);

            Certificate certificate = x509CertificateBuilder.build(contentSigner).toASN1Structure();

            MergeCertificateOptions mergeCertificateOptions =
                new MergeCertificateOptions(certificateName, Collections.singletonList(certificate.getEncoded()));

            certificateClient.mergeCertificate(mergeCertificateOptions);

            PollResponse<CertificateOperation> pollResponse = createCertificatePoller.poll();

            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
        } catch (GeneralSecurityException | IOException | OperatorCreationException e) {
            fail(e);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        assertResponseException(() -> certificateClient.mergeCertificate(new MergeCertificateOptions(
            testResourceNamer.randomName("testCert", 20), Collections.singletonList("test".getBytes()))),
            KeyVaultErrorException.class, HttpURLConnection.HTTP_NOT_FOUND);
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
                sleepIfRunningAgainstService(2000);

                pendingPollCount += 1;
            } else {
                return;
            }
        }

        LOGGER.log(LogLevel.VERBOSE, () -> "Deleted Certificate " + certificateName + " was not purged");
    }

}
