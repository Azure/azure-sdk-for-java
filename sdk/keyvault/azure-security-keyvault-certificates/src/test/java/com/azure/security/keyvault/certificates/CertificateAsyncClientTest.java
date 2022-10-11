// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
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

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 25);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy);

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
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

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
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

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
                .assertNext(expected -> {
                    assertEquals(certName, expected.getName());
                    assertNotNull(expected.getProperties().getCreatedOn());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginCreateCertificate("", CertificatePolicy.getDefault()))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
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

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(cert -> certificateAsyncClient
                        .updateCertificateProperties(cert.getProperties().setTags(updatedTags))))
                .assertNext(cert -> {
                    validateMapResponse(updatedTags, cert.getProperties().getTags());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateDisabledCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), false, originalTags);

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
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

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
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

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
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
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        deleteCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> certificateAsyncClient.beginDeleteCertificate(certificateName)
                        .last().flatMap(asyncPollResponse -> Mono.defer(() -> Mono.just(asyncPollResponse.getValue())))
                    ))
                .assertNext(expectedCert -> {
                    assertNotNull(expectedCert.getDeletedOn());
                    assertNotNull(expectedCert.getRecoveryId());
                    assertNotNull(expectedCert.getScheduledPurgeDate());
                    assertEquals(certificateName, expectedCert.getName());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginDeleteCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getDeletedCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> certificateAsyncClient.beginDeleteCertificate(certificateName)
                        .last().flatMap(asyncPollResponse -> Mono.just(asyncPollResponse.getValue()))))
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
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AtomicReference<KeyVaultCertificateWithPolicy> createdCertificate = new AtomicReference<>();

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(keyVaultCertificateWithPolicy -> {
                        createdCertificate.set(keyVaultCertificateWithPolicy);
                        return certificateAsyncClient.beginDeleteCertificate(certificateName)
                            .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                            .last().flatMap(asyncPollResponse -> Mono.just(asyncPollResponse.getValue()));
                    })
                    .flatMap(ignored -> certificateAsyncClient.beginRecoverDeletedCertificate(certificateName)
                        .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                        .last().flatMap(certAsyncResponse -> Mono.just(certAsyncResponse.getValue()))))
                .assertNext(recoveredCert -> {
                    assertEquals(certificateName, recoveredCert.getName());
                    validateCertificate(createdCertificate.get(), recoveredCert);
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginRecoverDeletedCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        backupCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> certificateAsyncClient.backupCertificate(certificateName)))
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
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        restoreCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy);

            AtomicReference<KeyVaultCertificateWithPolicy> createdCertificate = new AtomicReference<>();
            AtomicReference<Byte[]> backup = new AtomicReference<>();

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(keyVaultCertificateWithPolicy -> {
                        createdCertificate.set(keyVaultCertificateWithPolicy);
                        return certificateAsyncClient.backupCertificate(certificateName)
                            .flatMap(backupBytes -> {
                                Byte[] bytes = new Byte[backupBytes.length];
                                int i = 0;
                                for (Byte bt : backupBytes) {
                                    bytes[i] = bt;
                                    i++;
                                }
                                backup.set(bytes);
                                return Mono.just(backupBytes);
                            });
                    }))
                .assertNext(backupBytes -> {
                    assertNotNull(backupBytes);
                    assertTrue(backupBytes.length > 0);
                }).verifyComplete();

            StepVerifier.create(certificateAsyncClient.beginDeleteCertificate(certificateName)
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().then(Mono.defer(() -> certificateAsyncClient.purgeDeletedCertificate(certificateName)))
                    .then(Mono.just("complete")))
                .assertNext(input -> assertEquals("complete", input))
                .verifyComplete();

            sleepInRecordMode(40000);

            StepVerifier.create(Mono.defer(() -> {
                byte[] backupBytes = new byte[backup.get().length];
                for (int i = 0; i < backup.get().length; i++) {
                    backupBytes[i] = backup.get()[i];
                }
                return certificateAsyncClient.restoreCertificateBackup(backupBytes);
            })).assertNext(restoredCertificate -> {
                assertEquals(certificateName, restoredCertificate.getName());
                validatePolicy(restoredCertificate.getPolicy(), createdCertificate.get().getPolicy());
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

            AtomicReference<KeyVaultCertificateWithPolicy> expectedCert = new AtomicReference<>();

            StepVerifier.create(
                    certPoller
                        .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                        .last().flatMap(AsyncPollResponse::getFinalResult)
                        .flatMap(keyVaultCertificateWithPolicy -> {
                            expectedCert.set(keyVaultCertificateWithPolicy);
                            return certificateAsyncClient.getCertificateOperation(certificateName)
                                .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                                .last().flatMap(AsyncPollResponse::getFinalResult);
                        }))
                .assertNext(retrievedCert -> {
                    validateCertificate(expectedCert.get(), retrievedCert);
                    validatePolicy(expectedCert.get().getPolicy(), retrievedCert.getPolicy());
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

            StepVerifier.create(certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.IN_PROGRESS)
                    .last().flatMap(AsyncPollResponse::cancelOperation))
                .assertNext(certificateOperation -> {
                    assertTrue(certificateOperation.getCancellationRequested());
                }).verifyComplete();

            StepVerifier.create(certPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.USER_CANCELLED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
                .assertNext(certificate -> {
                    assertFalse(certificate.getProperties().isEnabled());
                }).verifyComplete();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);
        deleteCertificateOperationRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certificateName, CertificatePolicy.getDefault());

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> certificateAsyncClient.deleteCertificateOperation(certificateName)))
                .assertNext(certificateOperation -> {
                    assertEquals("completed", certificateOperation.getStatus());
                }).verifyComplete();


            StepVerifier.create(certificateAsyncClient.deleteCertificateOperation(certificateName))
                .verifyErrorSatisfies(e -> {
                    assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
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

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult))
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
            AtomicReference<KeyVaultCertificateWithPolicy> createdCert = new AtomicReference<>();

            StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(keyVaultCertificateWithPolicy -> {
                        keyVaultCertificateWithPolicy.getPolicy().setExportable(false);
                        createdCert.set(keyVaultCertificateWithPolicy);
                        return certificateAsyncClient.updateCertificatePolicy(certificateName, keyVaultCertificateWithPolicy.getPolicy());
                    }))
                .assertNext(certificatePolicy ->
                    validatePolicy(createdCert.get().getPolicy(), certificatePolicy)).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        StepVerifier.create(certificateAsyncClient.restoreCertificateBackup(keyBackupBytes))
            .verifyErrorSatisfies(e -> {
                assertResponseException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
            });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);


            for (String certName : certificates) {
                StepVerifier.create(certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault())
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).last())
                    .assertNext(response -> assertNotNull(response.getValue()))
                    .verifyComplete();
            }
            StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificates()
                            .map(certificate -> {
                                certificates.remove(certificate.getName());
                                return Mono.empty();
                            }).last())
                .assertNext(ignore -> {
                    assertEquals(0, certificates.size());
                }).verifyComplete();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listPropertiesOfCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listPropertiesOfCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                StepVerifier.create(certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault())
                        .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).last())
                    .assertNext(response -> assertNotNull(response.getValue()))
                    .verifyComplete();
            }

            StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificates(false)
                            .map(certificate -> {
                                certificates.remove(certificate.getName());
                                return Mono.empty();
                            }).last())
                .assertNext(ignore -> {
                    assertEquals(0, certificates.size());
                }).verifyComplete();
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
                assertResponseException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNullProvider(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(new CertificateIssuer("", null)))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
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
            AtomicReference<CertificateIssuer> certificateIssuer = new AtomicReference<>();
            StepVerifier.create(certificateAsyncClient.createIssuer(issuer)
                    .flatMap(createdIssuer -> {
                        certificateIssuer.set(createdIssuer);
                        return certificateAsyncClient.getIssuer(issuer.getName());
                    }))
                .assertNext(retrievedIssuer -> {
                    assertTrue(issuerCreatedCorrectly(certificateIssuer.get(), retrievedIssuer));
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.backupCertificate("non-existing"))
            .verifyErrorSatisfies(e -> {
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
            });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        deleteCertificateIssuerRunner((issuer) -> {
            AtomicReference<CertificateIssuer> createdIssuer = new AtomicReference<>();
            StepVerifier.create(certificateAsyncClient.createIssuer(issuer)
                    .flatMap(certificateIssuer -> {
                        createdIssuer.set(certificateIssuer);
                        return certificateAsyncClient.deleteIssuer(issuer.getName());
                    }))
                .assertNext(deletedIssuer -> {
                    assertTrue(issuerCreatedCorrectly(createdIssuer.get(), deletedIssuer));
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.backupCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateIssuers(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listCertificateIssuersRunner((certificateIssuers) -> {
            HashMap<String, CertificateIssuer> certificateIssuersToList = new HashMap<>(certificateIssuers);


            AtomicInteger count = new AtomicInteger(0);
            for (CertificateIssuer issuer : certificateIssuers.values()) {
                StepVerifier.create(certificateAsyncClient.createIssuer(issuer))
                    .assertNext(certificateIssuer -> {
                        assertNotNull(certificateIssuer.getName());
                    }).verifyComplete();
            }

            StepVerifier.create(certificateAsyncClient.listPropertiesOfIssuers()
                        .map(issuerProperties -> {
                            if (certificateIssuersToList.containsKey(issuerProperties.getName())) {
                                count.incrementAndGet();
                            }
                            return Mono.empty();
                        }).last())
                .assertNext(ignore -> assertEquals(certificateIssuersToList.size(), count.get()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateIssuerRunner((issuerToCreate, issuerToUpdate) -> {
            StepVerifier.create(certificateAsyncClient.createIssuer(issuerToCreate)
                    .flatMap(createdIssuer -> certificateAsyncClient.updateIssuer(issuerToUpdate)))
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

        StepVerifier.create(certificateAsyncClient.deleteContacts().then(Mono.just("complete")))
            .assertNext(input -> assertEquals("complete", input)).verifyComplete();
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
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.getCertificatePolicy("non-existing"))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        String certName = testResourceNamer.randomName("testListCertVersion", 25);
        int versionsToCreate = 5;

        for (int i = 0; i < versionsToCreate; i++) {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault());

            StepVerifier.create(certPoller
                .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .last().flatMap(AsyncPollResponse::getFinalResult).then(Mono.just("complete"))).assertNext(input -> assertEquals("complete", input)).verifyComplete();

        }

        AtomicInteger createdVersions = new AtomicInteger();

        StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificateVersions(certName)
            .map(certificateProperties -> {
                createdVersions.getAndIncrement();
                return Mono.just("complete");
            }).last()).assertNext(ignored -> assertEquals(versionsToCreate, createdVersions.get())).verifyComplete();
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

                StepVerifier.create(certPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).last()
                    .then(Mono.just("complete"))).assertNext(input -> assertEquals("complete", input)).verifyComplete();
            }

            for (String certName : certificates) {
                PollerFlux<DeletedCertificate, Void> poller = certificateAsyncClient.beginDeleteCertificate(certName);

                StepVerifier.create(poller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .last()).assertNext(asyncPollResponse -> assertNotNull(asyncPollResponse.getValue())).verifyComplete();
            }

            sleepInRecordMode(4000);

            StepVerifier.create(certificateAsyncClient.listDeletedCertificates()
                    .map(deletedCertificate -> {
                        certificatesToDelete.remove(deletedCertificate.getName());
                        return Mono.just("complete");
                    }).last())
                .assertNext(ignored -> {
                    assertEquals(0, certificatesToDelete.size());
                }).verifyComplete();
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
                assertResponseException(e, HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND));
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
                }).verifyComplete();
        });
    }
}

