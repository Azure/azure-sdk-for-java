// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.certificates.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        certificateAsyncClient = getCertificateClientBuilder(buildAsyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), testTenantId,
            getEndpoint(), serviceVersion)
            .buildAsyncClient();
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 25);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult))
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult))
                .assertNext(expected -> {
                    assertEquals(certName, expected.getName());
                    assertNotNull(expected.getProperties().getCreatedOn());
                }).verifyComplete();
        });

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        createCertificateRunner((certificatePolicy) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certName, certificatePolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult))
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
                assertResponseException(e, KeyVaultErrorException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginCreateCertificate(testResourceNamer.randomName("tempCert", 20), null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.beginCreateCertificate(null, null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault(), true, originalTags));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(cert -> certificateAsyncClient
                        .updateCertificateProperties(cert.getProperties().setTags(updatedTags))))
                .assertNext(cert -> validateMapResponse(updatedTags, cert.getProperties().getTags()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateDisabledCertificateRunner((originalTags, updatedTags) -> {
            String certName = testResourceNamer.randomName("testCert", 20);
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault(), false, originalTags));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(cert -> certificateAsyncClient.updateCertificateProperties(
                        cert.getProperties().setTags(updatedTags))))
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(expectedCert -> certificateAsyncClient.getCertificate(certificateName)
                        .map(returnedCert -> Tuples.of(expectedCert, returnedCert))))
                .assertNext(certTuple -> assertPolicy(certTuple.getT1().getPolicy(), certTuple.getT2().getPolicy()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificateSpecificVersionRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                .flatMap(expectedCert -> certificateAsyncClient.getCertificateVersion(certificateName,
                        expectedCert.getProperties().getVersion())
                    .map(returnedCert -> Tuples.of(expectedCert, returnedCert))))
                .assertNext(certTuple -> assertCertificate(certTuple.getT1(), certTuple.getT2()))
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginDeleteCertificate(certificateName))
                        .last().map(AsyncPollResponse::getValue)))
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginDeleteCertificate(certificateName))
                        .last().map(AsyncPollResponse::getValue)))
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            AtomicReference<KeyVaultCertificateWithPolicy> createdCertificate = new AtomicReference<>();

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(keyVaultCertificateWithPolicy -> {
                        createdCertificate.set(keyVaultCertificateWithPolicy);
                        return setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginDeleteCertificate(certificateName))
                            .last().map(AsyncPollResponse::getValue);
                    })
                    .flatMap(ignored -> setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginRecoverDeletedCertificate(certificateName))
                        .last().map(AsyncPollResponse::getValue)))
                .assertNext(recoveredCert -> {
                    assertEquals(certificateName, recoveredCert.getName());
                    assertCertificate(createdCertificate.get(), recoveredCert);
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, initialPolicy));

            AtomicReference<KeyVaultCertificateWithPolicy> createdCertificate = new AtomicReference<>();
            AtomicReference<byte[]> backup = new AtomicReference<>();

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(keyVaultCertificateWithPolicy -> {
                        createdCertificate.set(keyVaultCertificateWithPolicy);
                        return certificateAsyncClient.backupCertificate(certificateName)
                            .flatMap(backupBytes -> {
                                backup.set(CoreUtils.clone(backupBytes));
                                return Mono.just(backupBytes);
                            });
                    }))
                .assertNext(backupBytes -> {
                    assertNotNull(backupBytes);
                    assertTrue(backupBytes.length > 0);
                }).verifyComplete();

            StepVerifier.create(setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginDeleteCertificate(certificateName))
                    .last().then(Mono.defer(() -> certificateAsyncClient.purgeDeletedCertificate(certificateName))))
                .verifyComplete();

            sleepIfRunningAgainstService(40000);

            StepVerifier.create(certificateAsyncClient.restoreCertificateBackup(CoreUtils.clone(backup.get())))
                .assertNext(restoredCertificate -> {
                    assertEquals(certificateName, restoredCertificate.getName());
                    assertPolicy(restoredCertificate.getPolicy(), createdCertificate.get().getPolicy());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificateOperationRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, setupPolicy()));

            AtomicReference<KeyVaultCertificateWithPolicy> expectedCert = new AtomicReference<>();

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                .flatMap(keyVaultCertificateWithPolicy -> {
                    expectedCert.set(keyVaultCertificateWithPolicy);
                    return setPlaybackPollerFluxPollInterval(certificateAsyncClient.getCertificateOperation(certificateName))
                        .last().flatMap(AsyncPollResponse::getFinalResult);
                }))
                .assertNext(retrievedCert -> {
                    assertCertificate(expectedCert.get(), retrievedCert);
                    assertPolicy(expectedCert.get().getPolicy(), retrievedCert.getPolicy());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        cancelCertificateOperationRunner((certName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault())
                    .setPollInterval(Duration.ofMillis(250));

            StepVerifier.create(certPoller
                    .takeUntil(asyncPollResponse -> asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS)
                    .flatMap(AsyncPollResponse::cancelOperation))
                .assertNext(certificateOperation -> assertTrue(certificateOperation.getCancellationRequested()))
                .verifyComplete();

            StepVerifier.create(certPoller
                    .takeUntil(asyncPollResponse ->
                        "cancelled".equalsIgnoreCase(asyncPollResponse.getStatus().toString()))
                    .map(asyncPollResponse -> asyncPollResponse.getStatus().toString())
                    .zipWith(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)))
                .assertNext(tuple -> {
                    if ("cancelled".equalsIgnoreCase(tuple.getT1())) {
                        assertFalse(tuple.getT2().getPolicy().isEnabled());
                    }
                    // Else, the operation did not reach the expected status, either because it was completed before it
                    // could be canceled or there was a service timing issue when attempting to cancel the operation.
                })
                .verifyComplete();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);
        deleteCertificateOperationRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, CertificatePolicy.getDefault()));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                    .flatMap(ignored -> certificateAsyncClient.deleteCertificateOperation(certificateName)))
                .assertNext(certificateOperation -> assertEquals("completed", certificateOperation.getStatus()))
                .verifyComplete();


            StepVerifier.create(certificateAsyncClient.deleteCertificateOperation(certificateName))
                .verifyErrorSatisfies(e ->
                    assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        getCertificatePolicyRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, setupPolicy()));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult))
                .assertNext(certificate -> assertPolicy(setupPolicy(), certificate.getPolicy()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateCertificatePolicyRunner((certificateName) -> {
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certificateName, setupPolicy()));
            AtomicReference<KeyVaultCertificateWithPolicy> createdCert = new AtomicReference<>();

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult)
                .flatMap(keyVaultCertificateWithPolicy -> {
                    keyVaultCertificateWithPolicy.getPolicy().setExportable(false);
                    createdCert.set(keyVaultCertificateWithPolicy);
                    return certificateAsyncClient.updateCertificatePolicy(certificateName, keyVaultCertificateWithPolicy.getPolicy());
                }))
                .assertNext(certificatePolicy -> assertPolicy(createdCert.get().getPolicy(), certificatePolicy))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        byte[] keyBackupBytes = "non-existing".getBytes();

        StepVerifier.create(certificateAsyncClient.restoreCertificateBackup(keyBackupBytes))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                StepVerifier.create(setPlaybackPollerFluxPollInterval(
                    certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault())).last())
                    .assertNext(response -> assertNotNull(response.getValue()))
                    .verifyComplete();
            }
            StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificates()
                    .map(certificate -> {
                        certificates.remove(certificate.getName());
                        return Mono.empty();
                    }).last())
                .assertNext(ignore -> assertEquals(0, certificates.size()))
                .verifyComplete();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listPropertiesOfCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listPropertiesOfCertificatesRunner((certificatesToList) -> {
            HashSet<String> certificates = new HashSet<>(certificatesToList);

            for (String certName : certificates) {
                StepVerifier.create(setPlaybackPollerFluxPollInterval(
                    certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault())).last())
                    .assertNext(response -> assertNotNull(response.getValue()))
                    .verifyComplete();
            }

            StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificates(false)
                    .map(certificate -> {
                        certificates.remove(certificate.getName());
                        return Mono.empty();
                    }).last())
                .assertNext(ignore -> assertEquals(0, certificates.size()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        createIssuerRunner((issuer) -> StepVerifier.create(certificateAsyncClient.createIssuer(issuer))
            .assertNext(createdIssuer -> assertIssuerCreatedCorrectly(issuer, createdIssuer))
            .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(new CertificateIssuer("", "")))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, KeyVaultErrorException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNullProvider(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(new CertificateIssuer("", null)))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, KeyVaultErrorException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.createIssuer(null)).verifyError(NullPointerException.class);
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
                .assertNext(retrievedIssuer -> assertIssuerCreatedCorrectly(certificateIssuer.get(), retrievedIssuer))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.backupCertificate("non-existing"))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
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
                .assertNext(deletedIssuer -> assertIssuerCreatedCorrectly(createdIssuer.get(), deletedIssuer))
                .verifyComplete();
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

            for (CertificateIssuer issuer : certificateIssuers.values()) {
                StepVerifier.create(certificateAsyncClient.createIssuer(issuer))
                    .assertNext(certificateIssuer -> assertIssuerCreatedCorrectly(issuer, certificateIssuer))
                    .verifyComplete();
            }

            StepVerifier.create(certificateAsyncClient.listPropertiesOfIssuers()
                    .doOnNext(issuerProperties -> certificateIssuersToList.remove(issuerProperties.getName()))
                    .last())
                .assertNext(ignore -> assertEquals(0, certificateIssuersToList.size()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        updateIssuerRunner((issuerToCreate, issuerToUpdate) ->
            StepVerifier.create(certificateAsyncClient.createIssuer(issuerToCreate)
                .flatMap(createdIssuer -> certificateAsyncClient.updateIssuer(issuerToUpdate)))
            .assertNext(updatedIssuer -> assertIssuerUpdatedCorrectly(issuerToCreate, updatedIssuer))
                .verifyComplete());
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

        sleepIfRunningAgainstService(6000);

        StepVerifier.create(certificateAsyncClient.listContacts())
            .assertNext(contact -> validateContact(setupContact(), contact))
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
            .assertNext(contact -> validateContact(setupContact(), contact))
            .verifyComplete();
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
            PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()));

            StepVerifier.create(certPoller.last().flatMap(AsyncPollResponse::getFinalResult))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        }

        AtomicInteger createdVersions = new AtomicInteger();

        StepVerifier.create(certificateAsyncClient.listPropertiesOfCertificateVersions(certName)
            .map(certificateProperties -> {
                createdVersions.getAndIncrement();
                return Mono.just("complete");
            }).last()).assertNext(ignored -> assertEquals(versionsToCreate, createdVersions.get()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        listDeletedCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToDelete = new HashSet<>(certificates);

            for (String certName : certificatesToDelete) {
                PollerFlux<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = setPlaybackPollerFluxPollInterval(
                    certificateAsyncClient.beginCreateCertificate(certName, CertificatePolicy.getDefault()));

                StepVerifier.create(certPoller.last())
                    .assertNext(Assertions::assertNotNull)
                    .verifyComplete();
            }

            for (String certName : certificates) {
                PollerFlux<DeletedCertificate, Void> poller = setPlaybackPollerFluxPollInterval(
                    certificateAsyncClient.beginDeleteCertificate(certName));

                StepVerifier.create(poller.last())
                    .assertNext(asyncPollResponse -> assertNotNull(asyncPollResponse.getValue()))
                    .verifyComplete();
            }

            sleepIfRunningAgainstService(30000);

            StepVerifier.create(certificateAsyncClient.listDeletedCertificates()
                    .doOnNext(deletedCertificate -> certificatesToDelete.remove(deletedCertificate.getName()))
                    .last())
                .assertNext(ignored -> assertEquals(0, certificatesToDelete.size()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        importCertificateRunner((importCertificateOptions) ->
            StepVerifier.create(certificateAsyncClient.importCertificate(importCertificateOptions))
                .assertNext(importedCertificate -> {
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
                })
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    @DisabledForJreRange(min = JRE.JAVA_17) // Access to sun.security.* classes used here is not possible on Java 17+.
    public void mergeCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        String certificateName = testResourceNamer.randomName("testCert", 25);
        String issuer = "Unknown";
        String subject = "CN=MyCert";

        StepVerifier.create(setPlaybackPollerFluxPollInterval(certificateAsyncClient.beginCreateCertificate(certificateName,
            new CertificatePolicy(issuer, subject).setCertificateTransparent(false)))
            .takeUntil(asyncPollResponse ->
                asyncPollResponse.getStatus() == LongRunningOperationStatus.IN_PROGRESS)
            .map(asyncPollResponse -> {
                MergeCertificateOptions mergeCertificateOptions = null;

                try {
                    CertificateOperation certificateOperation = asyncPollResponse.getValue();
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

                    mergeCertificateOptions =
                        new MergeCertificateOptions(certificateName, Collections.singletonList(certificate.getEncoded()));
                } catch (GeneralSecurityException | IOException | OperatorCreationException e) {
                    fail(e);
                }

                return mergeCertificateOptions;
            })
            .flatMap(mergeCertificateOptions -> certificateAsyncClient.mergeCertificate(mergeCertificateOptions))
            .flatMap(mergedCertificate ->
                setPlaybackPollerFluxPollInterval(certificateAsyncClient.getCertificateOperation(mergedCertificate.getName())))
                .last())
            .assertNext(pollResponse ->
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus()))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(certificateAsyncClient.mergeCertificate(
                new MergeCertificateOptions(testResourceNamer.randomName("testCert", 20),
                    Collections.singletonList("test".getBytes()))))
            .verifyErrorSatisfies(e ->
                assertResponseException(e, KeyVaultErrorException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importPemCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) throws IOException {
        createCertificateAsyncClient(httpClient, serviceVersion);

        importPemCertificateRunner((importCertificateOptions) ->
            StepVerifier.create(certificateAsyncClient.importCertificate(importCertificateOptions))
                .assertNext(importedCertificate -> {
                    assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());
                    assertEquals(CertificateContentType.PEM, importedCertificate.getPolicy().getContentType());
                })
                .verifyComplete());
    }
}

