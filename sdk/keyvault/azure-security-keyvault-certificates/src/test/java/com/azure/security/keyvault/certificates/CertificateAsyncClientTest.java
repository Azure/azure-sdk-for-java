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
import com.azure.core.util.polling.*;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CertificateAsyncClientTest extends CertificateClientTestBase {
    private CertificateAsyncClient certificateAsyncClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createCertificateAsyncClient(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion, null);
    }

    private void createCertificateAsyncClient(HttpClient httpClient, CertificateServiceVersion serviceVersion,
                                         String testTenantId) {
        HttpPipeline httpPipeline = getHttpPipeline(httpClient, testTenantId);
        certificateAsyncClient = spy(new CertificateClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline)
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(certificateAsyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);


//        deleteSecretRunner((secretToDelete) -> {
//            StepVerifier.create(secretAsyncClient.setSecret(secretToDelete))
//                .assertNext(response -> assertSecretEquals(secretToDelete, response))
//                .verifyComplete();
//
//            PollerFlux<DeletedSecret, Void> poller = secretAsyncClient.beginDeleteSecret(secretToDelete.getName());
//            AsyncPollResponse<DeletedSecret, Void> lastResponse =
//                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
//                    .blockLast();
//            DeletedSecret deletedSecretResponse = lastResponse.getValue();
//
//            assertNotNull(deletedSecretResponse.getDeletedOn());
//            assertNotNull(deletedSecretResponse.getRecoveryId());
//            assertNotNull(deletedSecretResponse.getScheduledPurgeDate());
//            assertEquals(secretToDelete.getName(), deletedSecretResponse.getName());
//        });

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 25);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult())
                .assertNext(expected -> {
                    assertEquals(certName, expected.getName());
                    assertNotNull(expected.getProperties().getCreatedOn());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateWithMultipleTenants(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult())
                .assertNext(expected -> {
                    assertEquals(certName, expected.getName());
                    assertNotNull(expected.getProperties().getCreatedOn());
                }).verifyComplete();
        });

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult())
                .assertNext(expected -> {
                    assertEquals(certName, expected.getName());
                    assertNotNull(expected.getProperties().getCreatedOn());
                }).verifyComplete();
        });
    }

    private void deleteAndPurgeCertificate(String certName) {
        SyncPoller<DeletedCertificate, Void> deletePoller = certificateAsyncClient.beginDeleteCertificate(certName)
            .getSyncPoller();

        deletePoller.poll();
        deletePoller.waitForCompletion();

        certificateAsyncClient.purgeDeletedCertificate(certName).block();

        pollOnCertificatePurge(certName);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginCreateCertificate("", CertificatePolicy.getDefault()))
            .verifyErrorSatisfies(e ->
                assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginCreateCertificate(testResourceNamer.randomName("tempCert", 20), null))
            .verifyErrorSatisfies(e ->
                assertEquals(NullPointerException.class, e.getClass()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginCreateCertificate(null, null))
            .verifyErrorSatisfies(e ->
                assertEquals(NullPointerException.class, e.getClass()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), true, originalTags);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult()
                    .flatMap(cert -> certificateAsyncClient
                        .updateCertificateProperties(cert.getProperties().setTags(updatedTags))))
                .assertNext(cert -> {
                    validateMapResponse(updatedTags, cert.getProperties().getTags());
                }).verifyComplete();
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
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateDisabledCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), false, originalTags);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult()
                    .flatMap(cert -> certificateAsyncClient
                        .updateCertificateProperties(cert.getProperties().setTags(updatedTags))))
                .assertNext(cert -> {
                    validateMapResponse(updatedTags, cert.getProperties().getTags());
                    assertFalse(cert.getProperties().isEnabled());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult())
                .assertNext(expectedCert -> certificateAsyncClient.getCertificate(certificateName)
                    .map(returnedCert -> validatePolicy(expectedCert.getPolicy(), returnedCert.getPolicy())))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificateSpecificVersionRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult())
                .assertNext(expectedCert -> certificateAsyncClient.getCertificateVersion(certificateName, expectedCert.getProperties().getVersion())
                    .map(returnedCert -> validateCertificate(expectedCert, returnedCert)))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.getCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        deleteCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            PollerFlux<DeletedCertificate, Void> deletedCertPoller =
                certificateAsyncClient.beginDeleteCertificate(certificateName);

            AsyncPollResponse<DeletedCertificate, Void> lastDeletedCertPollResponse =
                deletedCertPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();


            DeletedCertificate deletedCertificate = lastDeletedCertPollResponse.getValue();

            assertNotNull(deletedCertificate.getDeletedOn());
            assertNotNull(deletedCertificate.getRecoveryId());
            assertNotNull(deletedCertificate.getScheduledPurgeDate());
            assertEquals(certificateName, deletedCertificate.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginDeleteCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getDeletedCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            PollerFlux<DeletedCertificate, Void> deleteCertPoller =
                certificateAsyncClient.beginDeleteCertificate(certificateName);

            AsyncPollResponse<DeletedCertificate, Void> lastDeletedCertPollResponse =
                deleteCertPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(certificateAsyncClient.getDeletedCertificate(certificateName))
                .assertNext(deletedCertificate -> {
                    assertNotNull(deletedCertificate.getDeletedOn());
                    assertNotNull(deletedCertificate.getRecoveryId());
                    assertNotNull(deletedCertificate.getScheduledPurgeDate());
                    assertEquals(certificateName, deletedCertificate.getName());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.getDeletedCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            KeyVaultCertificate createdCertificate = lastResponse.getFinalResult().block();

            PollerFlux<DeletedCertificate, Void> deletedCertPoller =
                certificateAsyncClient.beginDeleteCertificate(certificateName);

            AsyncPollResponse<DeletedCertificate, Void> lastDeletedCertPollResponse =
                deletedCertPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            PollerFlux<KeyVaultCertificateWithPolicy, Void> recoverPoller =
                certificateAsyncClient.beginRecoverDeletedCertificate(certificateName);

            AsyncPollResponse<KeyVaultCertificateWithPolicy, Void> recoverPollResponse =
                recoverPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            KeyVaultCertificate recoveredCert = recoverPollResponse.getValue();

            assertEquals(certificateName, recoveredCert.getName());

            validateCertificate(createdCertificate, recoveredCert);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginRecoverDeletedCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        backupCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(certificateAsyncClient.backupCertificate(certificateName))
                    .assertNext(backupBytes -> {
                        assertNotNull(backupBytes);
                        assertTrue(backupBytes.length > 0);
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        assertRestException(() -> certificateAsyncClient.backupCertificate("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        restoreCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            KeyVaultCertificateWithPolicy createdCert = lastResponse.getFinalResult().block();

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

            assertTrue(issuerCreatedCorrectly(issuer, createdIssuer));
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

            assertTrue(issuerCreatedCorrectly(issuer, retrievedIssuer));
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

            assertTrue(issuerCreatedCorrectly(issuer, deletedIssuer));
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

                assertTrue(issuerCreatedCorrectly(issuer, certificateIssuer));
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
    public void updateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);

        updateIssuerRunner((issuerToCreate, issuerToUpdate) -> {
            CertificateIssuer createdIssuer = certificateClient.createIssuer(issuerToCreate);
            CertificateIssuer updatedIssuer = certificateClient.updateIssuer(issuerToUpdate);

            assertTrue(issuerUpdatedCorrectly(issuerToCreate, updatedIssuer));
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
                deletedCertificate = certificateAsyncClient.getDeletedCertificate(certificateName).block();
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
