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
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class CertificateClientTest extends CertificateClientTestBase {
    private CertificateClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createCertificateClient(HttpClient httpClient,
                                         CertificateServiceVersion serviceVersion) {
        HttpPipeline httpPipeline = getHttpPipeline(httpClient, serviceVersion);
        CertificateAsyncClient asyncClient = spy(new CertificateClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline)
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(asyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }

        client = new CertificateClient(asyncClient);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        createCertificateRunner((policy) -> {
            String certName = generateResourceId("testCer");
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                policy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();
            assertEquals(certName, expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
        });
    }

    private void deleteAndPurgeCertificate(String certName) {
        SyncPoller<DeletedCertificate, Void> deletePoller = client.beginDeleteCertificate(certName);
        deletePoller.poll();
        deletePoller.waitForCompletion();
        client.purgeDeletedCertificate(certName);
        pollOnCertificatePurge(certName);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.beginCreateCertificate("", CertificatePolicy.getDefault()),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNullPolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.beginCreateCertificate(generateResourceId("tempCert"), null),
            NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createCertificateNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.beginCreateCertificate(null, null),
            NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        updateCertificateRunner((tags, updatedTags) -> {
            String certName = generateResourceId("testCertificate2");
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                CertificatePolicy.getDefault(), true, tags);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate = client.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
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
        updateDisabledCertificateRunner((tags, updatedTags) -> {
            String certName = generateResourceId("testCertificate3");
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                CertificatePolicy.getDefault(), false, tags);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate = client.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
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
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificateWithPolicy getCertificate = client.getCertificate(certificateName);
            validatePolicy(certificate.getPolicy(), getCertificate.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateSpecificVersion(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getCertificateSpecificVersionRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate getCertificate = client.getCertificateVersion(certificateName, certificate.getProperties().getVersion());
            validateCertificate(certificate, getCertificate);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.getCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        deleteCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            SyncPoller<DeletedCertificate, Void> deletedKeyPoller = client.beginDeleteCertificate(certificateName);

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
        assertRestException(() -> client.beginDeleteCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getDeletedCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            SyncPoller<DeletedCertificate, Void> deletedKeyPoller = client.beginDeleteCertificate(certificateName);

            PollResponse<DeletedCertificate> pollResponse = deletedKeyPoller.poll();
            DeletedCertificate deletedCertificate = pollResponse.getValue();

            deletedKeyPoller.waitForCompletion();
            deletedCertificate = client.getDeletedCertificate(certificateName);
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
        assertRestException(() -> client.getDeletedCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        recoverDeletedKeyRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();

            KeyVaultCertificate createdCertificate = certPoller.getFinalResult();

            SyncPoller<DeletedCertificate, Void> deletedKeyPoller = client.beginDeleteCertificate(certificateName);

            PollResponse<DeletedCertificate> pollResponse = deletedKeyPoller.poll();
            deletedKeyPoller.waitForCompletion();

            SyncPoller<KeyVaultCertificateWithPolicy, Void> recoverPoller = client.beginRecoverDeletedCertificate(certificateName);
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
        assertRestException(() -> client.beginRecoverDeletedCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        backupCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            byte[] backupBytes = (client.backupCertificate(certificateName));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.backupCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        restoreCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy createdCert = certPoller.getFinalResult();
            byte[] backupBytes = (client.backupCertificate(certificateName));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            deleteAndPurgeCertificate(certificateName);
            sleepInRecordMode(40000);
            KeyVaultCertificateWithPolicy restoredCertificate = client.restoreCertificateBackup(backupBytes);
            assertEquals(certificateName, restoredCertificate.getName());
            validatePolicy(restoredCertificate.getPolicy(), createdCert.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getCertificateOperationRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, setupPolicy());
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> retrievePoller = client.getCertificateOperation(certName);
            retrievePoller.waitForCompletion();
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy reteievedCert = retrievePoller.getFinalResult();
            KeyVaultCertificateWithPolicy expectedCert = certPoller.getFinalResult();
            validateCertificate(expectedCert, reteievedCert);
            validatePolicy(expectedCert.getPolicy(),
                reteievedCert.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void cancelCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        cancelCertificateOperationRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, CertificatePolicy.getDefault());
            certPoller.poll();
            certPoller.cancelOperation();
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            assertEquals(false, certificate.getProperties().isEnabled());
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateOperation(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        deleteCertificateOperationRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, CertificatePolicy.getDefault());
            certPoller.waitForCompletion();
            CertificateOperation certificateOperation = client.deleteCertificateOperation(certName);
            assertEquals("completed", certificateOperation.getStatus());
            assertRestException(() -> client.deleteCertificateOperation(certName), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getCertificatePolicyRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, setupPolicy());
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            validatePolicy(setupPolicy(), certificate.getPolicy());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateCertificatePolicy(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        updateCertificatePolicyRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, setupPolicy());
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            certificate.getPolicy().setExportable(false);
            CertificatePolicy policy = client.updateCertificatePolicy(certName, certificate.getPolicy());
            validatePolicy(certificate.getPolicy(), policy);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreCertificateFromMalformedBackup(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        byte[] keyBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreCertificateBackup(keyBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToList = new HashSet<>(certificates);
            for (String certName : certificatesToList) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault());
                certPoller.waitForCompletion();
            }

            sleepInRecordMode(90000);
            for (CertificateProperties actualKey : client.listPropertiesOfCertificates()) {
                if (certificatesToList.contains(actualKey.getName())) {
                    certificatesToList.remove(actualKey.getName());
                }
            }
            assertEquals(0, certificatesToList.size());
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listPropertiesOfCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listPropertiesOfCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToList = new HashSet<>(certificates);
            for (String certName : certificatesToList) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault());
                certPoller.waitForCompletion();
            }
            sleepInRecordMode(90000);
            for (CertificateProperties actualKey : client.listPropertiesOfCertificates(false, Context.NONE)) {
                if (certificatesToList.contains(actualKey.getName())) {
                    certificatesToList.remove(actualKey.getName());
                }
            }
            assertEquals(0, certificatesToList.size());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        createIssuereRunner((issuer) -> {
            CertificateIssuer createdIssuer = client.createIssuer(issuer);
            validateIssuer(issuer, createdIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerEmptyName(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.createIssuer(new CertificateIssuer("", "")),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNullProvider(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.createIssuer(new CertificateIssuer("", null)),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createIssuerNull(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.createIssuer(null), NullPointerException.class);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        getCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = client.createIssuer(issuer);
            CertificateIssuer retrievedIssuer = client.getIssuer(issuer.getName());
            validateIssuer(issuer, retrievedIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.backupCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuer(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        deleteCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = client.createIssuer(issuer);
            CertificateIssuer deletedIssuer = client.deleteIssuer(issuer.getName());
            validateIssuer(issuer, deletedIssuer);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteCertificateIssuerNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.backupCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateIssuers(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listCertificateIssuersRunner((certificateIssuers) -> {
            HashMap<String, CertificateIssuer> certificateIssuersToList = new HashMap<>(certificateIssuers);
            for (CertificateIssuer issuer : certificateIssuersToList.values()) {
                CertificateIssuer certificateIssuer = client.createIssuer(issuer);
                validateIssuer(issuer, certificateIssuer);
            }

            for (IssuerProperties issuerProperties : client.listPropertiesOfIssuers()) {
                if (certificateIssuersToList.containsKey(issuerProperties.getName())) {
                    certificateIssuersToList.remove(issuerProperties.getName());
                }
            }
            assertEquals(0, certificateIssuersToList.size());
            for (CertificateIssuer issuer : certificateIssuers.values()) {
                client.deleteIssuer(issuer.getName());
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        List<CertificateContact> contacts = Arrays.asList(setupContact());
        client.setContacts(contacts).forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        client.deleteContacts();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        List<CertificateContact> contacts = Arrays.asList(setupContact());
        client.setContacts(contacts).forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        sleepInRecordMode(6000);
        client.listContacts().stream().forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteContacts(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        List<CertificateContact> contacts = Arrays.asList(setupContact());
        client.setContacts(contacts).forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        PagedIterable<CertificateContact> certificateContacts = client.deleteContacts();
        validateContact(setupContact(), certificateContacts.iterator().next());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificateOperatioNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.getCertificateOperation("non-existing").poll(), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getCertificatePolicyNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.getCertificatePolicy("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listCertificateVersions(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        String certName = generateResourceId("testListCertVersion");
        int counter = 5;
        for (int i = 0; i < counter; i++) {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                CertificatePolicy.getDefault());
            certPoller.waitForCompletion();
        }
        int countRecv = 0;
        for (CertificateProperties certificateProperties : client.listPropertiesOfCertificateVersions(certName)) {
            countRecv++;
            assertEquals(certificateProperties.getName(), certName);
        }
        assertEquals(counter, countRecv);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedCertificates(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        listDeletedCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToDelete = new HashSet<>(certificates);
            for (String certName : certificatesToDelete) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault());
                PollResponse<CertificateOperation> pollResponse = certPoller.poll();
                while (!pollResponse.getStatus().isComplete()) {
                    sleepInRecordMode(1000);
                    pollResponse = certPoller.poll();
                }
            }

            for (String certName : certificates) {
                SyncPoller<DeletedCertificate, Void> poller = client.beginDeleteCertificate(certName);
                PollResponse<DeletedCertificate> pollResponse = poller.poll();
                while (!pollResponse.getStatus().isComplete()) {
                    sleepInRecordMode(1000);
                    pollResponse = poller.poll();
                }
                assertNotNull(pollResponse.getValue());
            }

            sleepInRecordMode(90000);

            Iterable<DeletedCertificate> deletedCertificates = client.listDeletedCertificates();
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
            KeyVaultCertificateWithPolicy importedCertificate = client.importCertificate(importCertificateOptions);
            assertTrue(toHexString(importedCertificate.getProperties().getX509Thumbprint()).equalsIgnoreCase("7cb8b7539d87ba7215357b9b9049dff2d3fa59ba"));
            assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());

            // Load the CER part into X509Certificate object
            X509Certificate x509Certificate = null;
            try {
                x509Certificate = loadCerToX509Certificate(importedCertificate);
            } catch (CertificateException e) {
                e.printStackTrace();
                fail();
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }

            assertTrue(x509Certificate.getSubjectX500Principal().getName().equals("CN=KeyVaultTest"));
            assertTrue(x509Certificate.getIssuerX500Principal().getName().equals("CN=Root Agency"));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void mergeCertificateNotFound(HttpClient httpClient, CertificateServiceVersion serviceVersion) {
        createCertificateClient(httpClient, serviceVersion);
        assertRestException(() -> client.mergeCertificate(new MergeCertificateOptions(generateResourceId("testCert16"), Arrays.asList("test".getBytes()))),
            HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void importPemCertificate(HttpClient httpClient, CertificateServiceVersion serviceVersion) throws IOException {
        createCertificateClient(httpClient, serviceVersion);
        importPemCertificateRunner((importCertificateOptions) -> {
            KeyVaultCertificateWithPolicy importedCertificate = client.importCertificate(importCertificateOptions);
            assertEquals(importCertificateOptions.isEnabled(), importedCertificate.getProperties().isEnabled());
            assertEquals(CertificateContentType.PEM, importedCertificate.getPolicy().getContentType());
        });
    }

    private DeletedCertificate pollOnCertificatePurge(String certificateName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedCertificate deletedCertificate = null;
            try {
                deletedCertificate = client.getDeletedCertificate(certificateName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedCertificate != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedCertificate;
            }
        }
        System.err.printf("Deleted Key %s was not purged \n", certificateName);
        return null;
    }

}
