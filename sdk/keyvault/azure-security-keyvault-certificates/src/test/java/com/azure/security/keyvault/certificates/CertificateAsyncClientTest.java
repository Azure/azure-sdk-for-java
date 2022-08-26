// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.polling.*;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
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
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
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
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
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

        StepVerifier.create(certificateAsyncClient.backupCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
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

            byte[] backupBytes = certificateAsyncClient.backupCertificate(certificateName).block();

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            deleteAndPurgeCertificate(certificateName);

            sleepInRecordMode(40000);


            StepVerifier.create(certificateAsyncClient.restoreCertificateBackup(backupBytes))
                    .assertNext(restoredCertificate -> {
                        assertEquals(certificateName, restoredCertificate.getName());
                        validatePolicy(restoredCertificate.getPolicy(), createdCert.getPolicy());
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificateOperationRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, setupPolicy());

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastCertResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> retrievePoller =
                certificateAsyncClient.getCertificateOperation(certificateName);


            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastRetrieveResponse =
                retrievePoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            KeyVaultCertificateWithPolicy expectedCert = lastCertResponse.getFinalResult().block();
            StepVerifier.create(lastRetrieveResponse.getFinalResult())
                .assertNext(retrievedCert -> {
                    validateCertificate(expectedCert, retrievedCert);
                    validatePolicy(expectedCert.getPolicy(), retrievedCert.getPolicy());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        cancelCertificateOperationRunner((certName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.IN_PROGRESS)
                    .blockLast();

            StepVerifier.create(lastResponse.cancelOperation())
                .assertNext(certificateOperation -> {
                    assertTrue(certificateOperation.getCancellationRequested());
                }).verifyComplete();

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastCancelResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.USER_CANCELLED)
                    .blockLast();

            StepVerifier.create(lastCancelResponse.getFinalResult())
                    .assertNext(certificate -> {
                        assertFalse(certificate.getProperties().isEnabled());
                    }).verifyComplete();

            certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);
        deleteCertificateOperationRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, CertificatePolicy.getDefault());

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(certificateAsyncClient.deleteCertificateOperation(certificateName))
                    .assertNext(certificateOperation -> {
                        assertEquals("completed", certificateOperation.getStatus());
                    }).verifyComplete();


            StepVerifier.create(certificateAsyncClient.deleteCertificateOperation(certificateName))
                    .verifyErrorSatisfies(e -> {
                        assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
                    });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificatePolicyRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, setupPolicy());

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(lastResponse.getFinalResult())
                    .assertNext(certificate -> {
                        validatePolicy(setupPolicy(), certificate.getPolicy());
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateCertificatePolicyRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, setupPolicy());

            AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            KeyVaultCertificateWithPolicy certificate = lastResponse.getFinalResult().block();

            StepVerifier.create(certificateAsyncClient.updateCertificatePolicy(certificateName, certificate.getPolicy()))
                .assertNext(certificatePolicy ->
                    validatePolicy(certificate.getPolicy(), certificatePolicy)).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        StepVerifier.create(certificateAsyncClient.restoreCertificateBackup(keyBackupBytes))
            .verifyErrorSatisfies(e -> {
                assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
            });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                    certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

                AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                    certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                        .blockLast();
            }

            sleepInRecordMode(90000);

            List<CertificateProperties> output = new ArrayList<>();
            StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificates().map(certificate -> {
                certificates.remove(certificate.getName());
                return Mono.empty();
            })).assertNext(ignore -> {
                assertEquals(0, certificates.size());
            });
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listPropertiesOfCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listPropertiesOfCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                    certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

                AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                    certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                        .blockLast();
            }

            sleepInRecordMode(90000);

            certificateAsyncClient.listPropertiesOfCertificates(false).map(certificate -> {
                certificates.remove(certificate.getName());
                return Mono.empty();
            }).blockLast();

            assertEquals(0, certificates.size());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        createIssuerRunner((issuer) -> {
            StepVerifier.create(certificateAsyncClient.createIssuer(issuer))
                    .assertNext(createdIssuer -> {
                        assertTrue(issuerCreatedCorrectly(issuer, createdIssuer));
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(new CertificateIssuer("", "")))
                .verifyErrorSatisfies(e ->
                    assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNullProvider(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(new CertificateIssuer("", null)))
            .verifyErrorSatisfies(e ->
                assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(null))
            .verifyErrorSatisfies(e ->
                assertEquals(NullPointerException.class, e.getClass()));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = certificateAsyncClient.createIssuer(issuer).block();
            StepVerifier.create(certificateAsyncClient.getIssuer(issuer.getName()))
                    .assertNext(retrievedIssuer -> {
                        assertTrue(issuerCreatedCorrectly(createdIssuer, retrievedIssuer));
                    });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.backupCertificate("non-existing"))
            .verifyErrorSatisfies(e -> {
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
            });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        deleteCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = certificateAsyncClient.createIssuer(issuer).block();
            StepVerifier.create(certificateAsyncClient.deleteIssuer(issuer.getName()))
                    .assertNext(deletedIssuer -> {
                        assertTrue(issuerCreatedCorrectly(createdIssuer, deletedIssuer));
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.backupCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateIssuers(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listCertificateIssuersRunner((certificateIssuers) -> {
            HashMap<String, CertificateIssuer> certificateIssuersToList = new HashMap<>(certificateIssuers);

            for (CertificateIssuer issuer : certificateIssuersToList.values()) {
                CertificateIssuer certificateIssuer = certificateAsyncClient.createIssuer(issuer).block();

                assertTrue(issuerCreatedCorrectly(issuer, certificateIssuer));
            }

            List<IssuerProperties> output = new ArrayList<>();

            StepVerifier.create(certificateAsyncClient.listPropertiesOfIssuers()
                .map(issuerProperties -> {
                    output.add(issuerProperties);
                    return Mono.empty();
                })).assertNext(ignore -> assertEquals(certificateIssuersToList.size(), output.size()));

            for (CertificateIssuer issuer : certificateIssuers.values()) {
                certificateAsyncClient.deleteIssuer(issuer.getName()).block();
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateIssuerRunner((issuerToCreate, issuerToUpdate) -> {
            CertificateIssuer createdIssuer = certificateAsyncClient.createIssuer(issuerToCreate).block();
            StepVerifier.create(certificateAsyncClient.updateIssuer(issuerToUpdate))
                    .assertNext(updatedIssuer ->
                        assertTrue(issuerUpdatedCorrectly(issuerToCreate, updatedIssuer))).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void setContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        List<CertificateContact> contacts = Arrays.asList(setupContact());

        StepVerifier.create(certificateAsyncClient.setContacts(contacts))
            .assertNext(contact -> validateContact(setupContact(), contact))
            .verifyComplete();
        certificateAsyncClient.deleteContacts().blockLast();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void listContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        List<CertificateContact> contacts = Arrays.asList(setupContact());

        StepVerifier.create(certificateAsyncClient.setContacts(contacts))
            .assertNext(contact -> validateContact(setupContact(), contact))
            .verifyComplete();

        sleepInRecordMode(6000);

        StepVerifier.create(certificateAsyncClient.listContacts())
            .assertNext(contact ->
                validateContact(setupContact(), contact))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void deleteContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        List<CertificateContact> contacts = Arrays.asList(setupContact());

        StepVerifier.create(certificateAsyncClient.setContacts(contacts))
            .assertNext(contact -> validateContact(setupContact(), contact))
            .verifyComplete();

        StepVerifier.create(certificateAsyncClient.deleteContacts())
                .assertNext(contact -> {
                    validateContact(setupContact(), contact);
                }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperationNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.getCertificateOperation("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.getCertificatePolicy("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        String certName = testResourceNamer.randomName("testListCertVersion", 25);
        int versionsToCreate = 5;

        for (int i = 0; i < versionsToCreate; i++) {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()).getSyncPoller();

            certPoller.waitForCompletion();
        }

        AtomicInteger createdVersions = new AtomicInteger();

        certificateAsyncClient.listPropertiesOfCertificateVersions(certName)
            .flatMap(certificateProperties -> {
                createdVersions.getAndIncrement();
                return Mono.empty();
            }).blockLast();

        assertEquals(versionsToCreate, createdVersions.get());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        // Skip when running against the service to avoid having pipeline runs take longer than they have to.
        if (interceptorManager.isLiveMode()) {
            return;
        }

        listDeletedCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToDelete = new HashSet<>(certificates);

            for (String certName : certificatesToDelete) {
                PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                    certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

                AsyncPollResponse<CertificateOperation, KeyVaultCertificateWithPolicy> lastResponse =
                    certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                        .blockLast();
            }

            for (String certName : certificates) {
                PollerFlux<DeletedCertificate, Void> poller = certificateAsyncClient.beginDeleteCertificate(certName);

                AsyncPollResponse<DeletedCertificate, Void> lastResponse =
                    poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                        .blockLast();
                assertNotNull(lastResponse.getValue());
            }

            certificateAsyncClient.listDeletedCertificates().map(deletedCertificate -> {
                    certificatesToDelete.remove(deletedCertificate.getName());
                    assertNotNull(deletedCertificate.getRecoveryId());
                    assertNotNull(deletedCertificate.getDeletedOn());
                    return Mono.empty();
                }).blockLast();

            assertEquals(0, certificatesToDelete.size());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        importCertificateRunner((importCertificateOptions) -> {

            StepVerifier.create(certificateAsyncClient.importCertificate(importCertificateOptions))
                    .assertNext(importedCertificate -> {
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
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.mergeCertificate(
            new MergeCertificateOptions(testResourceNamer.randomName("testCert", 20),
                Arrays.asList("test".getBytes()))))
                .verifyErrorSatisfies(e ->
                    assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importPemCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) throws IOException {
        createCertificateAsyncClient(httpClient, serviceVersion);

        importPemCertificateRunner((importCertificateOptions) -> {
            StepVerifier.create(certificateAsyncClient.importCertificate(importCertificateOptions))
                    .assertNext(importedCertificate -> {
                        assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());
                        assertEquals(CertificateContentType.PEM, importedCertificate.getPolicy().getContentType());
                    });
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
