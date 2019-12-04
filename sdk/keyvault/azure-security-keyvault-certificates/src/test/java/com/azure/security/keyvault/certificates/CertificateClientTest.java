// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.certificates.models.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CertificateClientTest extends CertificateClientTestBase {
    private CertificateClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
        if (interceptorManager.isPlaybackMode()) {

            client = clientSetup(pipeline -> new CertificateClientBuilder()
                .vaultUrl(getEndpoint())
                .pipeline(pipeline)
                .buildClient());
        } else {
            client = clientSetup(pipeline -> new CertificateClientBuilder()
                .vaultUrl(getEndpoint())
                .pipeline(pipeline)
                .buildClient());
        }
    }


    @Test
    public void createCertificate() {
        createCertificateRunner((policy) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate("testCer",
                policy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy expected = certPoller.getFinalResult();
            assertEquals("testCer", expected.getName());
            assertNotNull(expected.getProperties().getCreatedOn());
            deleteAndPurgeCertificate("testCer");
        });
    }

    private void deleteAndPurgeCertificate(String certName) {
        SyncPoller<DeletedCertificate, Void> deletePoller = client.beginDeleteCertificate(certName);
        deletePoller.poll();
        deletePoller.waitForCompletion();
        client.purgeDeletedCertificate(certName);
        pollOnCertificatePurge(certName);
    }

    @Test
    public void createCertificateEmptyName() {
        assertRestException(() -> client.beginCreateCertificate("", CertificatePolicy.getDefault()),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @Test
    public void createCertificateNullPolicy() {
        assertRunnableThrowsException(() -> client.beginCreateCertificate("tempCert", null),
            NullPointerException.class);
    }

    @Test
    public void createCertoificateNull() {
        assertRunnableThrowsException(() -> client.beginCreateCertificate(null, null),
            NullPointerException.class);
    }

    @Test
    public void updateCertificate() {
        updateCertificateRunner((tags, updatedTags) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate("testCertificate2",
                    CertificatePolicy.getDefault(), true, tags);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate = client.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
            Map<String, String> returnedTags = keyVaultCertificate.getProperties().getTags();
            validateMapResponse(updatedTags, returnedTags);
            deleteAndPurgeCertificate("testCertificate2");
        });
    }

    private void validateMapResponse(Map<String, String> expected, Map<String, String> returned) {
        for (String key : expected.keySet()) {
            String val = returned.get(key);
            String expectedVal = expected.get(key);
            assertEquals(expectedVal, val);
        }
    }

    @Test
    public void updateDisabledCertificate() {
        updateDisabledCertificateRunner((tags, updatedTags) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate("testCertificate3",
                CertificatePolicy.getDefault(), false, tags);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate keyVaultCertificate = client.updateCertificateProperties(certificate.getProperties().setTags(updatedTags));
            Map<String, String> returnedTags = keyVaultCertificate.getProperties().getTags();
            validateMapResponse(updatedTags, returnedTags);
            assertFalse(keyVaultCertificate.getProperties().isEnabled());
            deleteAndPurgeCertificate("testCertificate3");
        });
    }

    @Test
    public void getCertificate() {
        getCertificateRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificateWithPolicy getCertificate = client.getCertificate(certificateName);
            validatePolicy(certificate.getCertificatePolicy(), getCertificate.getCertificatePolicy());
            deleteAndPurgeCertificate(certificateName);
        });
    }

    @Test
    public void getCertificateSpecificVersion() {
        getCertificateSpecificVersionRunner((certificateName) -> {
            CertificatePolicy initialPolicy = setupPolicy();
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certificateName,
                initialPolicy);
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            KeyVaultCertificate getCertificate = client.getCertificateVersion(certificateName, certificate.getProperties().getVersion());
            validateCertificate(certificate, getCertificate);
            deleteAndPurgeCertificate(certificateName);
        });
    }

    @Test
    public void getCertificateNotFound() {
        assertRestException(() -> client.getCertificate("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void deleteCertificate() {
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
            client.purgeDeletedCertificate(certificateName);
            pollOnCertificatePurge(certificateName);
        });
    }

    @Test
    public void deleteCertificateNotFound() {
        assertRestException(() -> client.beginDeleteCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void getDeletedCertificate() {
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
            client.purgeDeletedCertificate(certificateName);
            pollOnCertificatePurge(certificateName);
        });
    }

    @Test
    public void getDeletedCertificateNotFound() {
        assertRestException(() -> client.getDeletedCertificate("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void recoverDeletedCertificate() {
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
            deleteAndPurgeCertificate(certificateName);
        });
    }

    @Test
    public void recoverDeletedCertificateNotFound() {
        assertRestException(() -> client.beginRecoverDeletedCertificate("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void backupCertificate() {
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

    @Test
    public void backupCertificateNotFound() {
        assertRestException(() -> client.backupCertificate("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void restoreCertificate() {
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
            validatePolicy(restoredCertificate.getCertificatePolicy(), createdCert.getCertificatePolicy());
            deleteAndPurgeCertificate(certificateName);
        });
    }

    @Test
    public void getCertificateOperation() {
        getCertificateOperationRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, setupPolicy());
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> retrievePoller = client.getCertificateOperation(certName);
            retrievePoller.waitForCompletion();
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy reteievedCert = retrievePoller.getFinalResult();
            KeyVaultCertificateWithPolicy expectedCert = certPoller.getFinalResult();
            validateCertificate(expectedCert, reteievedCert);
            validatePolicy(expectedCert.getCertificatePolicy(),
                reteievedCert.getCertificatePolicy());
            deleteAndPurgeCertificate(certName);
        });
    }

    @Test
    public void cancelCertificateOperation() {
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

    @Test
    public void deleteCertificateOperation() {
        deleteCertificateOperationRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, CertificatePolicy.getDefault());
            certPoller.waitForCompletion();
            CertificateOperation certificateOperation = client.deleteCertificateOperation(certName);
            assertEquals("completed", certificateOperation.getStatus());
            assertRestException(() -> client.deleteCertificateOperation(certName), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        });
    }

    @Test
    public void getCertificatePolicy() {
        getCertificatePolicyRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, setupPolicy());
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            validatePolicy(setupPolicy(), certificate.getCertificatePolicy());
            deleteAndPurgeCertificate(certName);
        });
    }

    @Test
    public void updateCertificatePolicy() {
        updateCertificatePolicyRunner((certName) -> {
            SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller =
                client.beginCreateCertificate(certName, setupPolicy());
            certPoller.waitForCompletion();
            KeyVaultCertificateWithPolicy certificate = certPoller.getFinalResult();
            certificate.getCertificatePolicy().setExportable(false);
            CertificatePolicy policy = client.updateCertificatePolicy(certName, certificate.getCertificatePolicy());
            validatePolicy(certificate.getCertificatePolicy(), policy);
            deleteAndPurgeCertificate(certName);
        });
    }

    @Test
    public void restoreCertificateFromMalformedBackup() {
        byte[] keyBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreCertificateBackup(keyBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);

    }

    @Test
    public void listCertificates() {
        listCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToList = new HashSet<>(certificates);
            for (String certName :  certificatesToList) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault());
                certPoller.waitForCompletion();
            }

            for (CertificateProperties actualKey : client.listPropertiesOfCertificates()) {
                if (certificatesToList.contains(actualKey.getName())) {
                    certificatesToList.remove(actualKey.getName());
                }
            }
            assertEquals(0, certificatesToList.size());
            for (String certName : certificates) {
                deleteAndPurgeCertificate(certName);
            }
        });
    }

    @Test
    public void createIssuer() {
        createIssuereRunner((issuer) -> {
            CertificateIssuer createdIssuer = client.createIssuer(issuer);
            validateIssuer(issuer, createdIssuer);
        });
    }

    @Test
    public void createIssuerEmptyName() {
        assertRestException(() -> client.createIssuer("", ""),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @Test
    public void createIssuerNullProvider() {
        assertRestException(() -> client.createIssuer("", null),
            HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
    }

    @Test
    public void createIssuerNull() {
        assertRunnableThrowsException(() -> client.createIssuer(null), NullPointerException.class);
    }

    @Test
    public void getCertificateIssuer() {
        getCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = client.createIssuer(issuer);
            CertificateIssuer retrievedIssuer = client.getIssuer(issuer.getName());
            validateIssuer(issuer, retrievedIssuer);
        });
    }

    @Test
    public void getCertificateIssuerNotFound() {
        assertRestException(() -> client.backupCertificate("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void deleteCertificateIssuer() {
        deleteCertificateIssuerRunner((issuer) -> {
            CertificateIssuer createdIssuer = client.createIssuer(issuer);
            CertificateIssuer deletedIssuer = client.deleteIssuer(issuer.getName());
            validateIssuer(issuer, deletedIssuer);
        });
    }

    @Test
    public void deleteCertificateIssuerNotFound() {
        assertRestException(() -> client.backupCertificate("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void listCertificateIssuers() {
        listCertificateIssuersRunner((certificateIssuers) -> {
            HashMap<String, CertificateIssuer> certificateIssuersToList = new HashMap<>(certificateIssuers);
            for (CertificateIssuer issuer :  certificateIssuersToList.values()) {
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

    @Test
    public void setContacts() {
        List<CertificateContact> contacts = Arrays.asList(setupContact());
        client.setContacts(contacts).forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        client.deleteContacts();
    }

    @Test
    public void listContacts() {
        List<CertificateContact> contacts = Arrays.asList(setupContact());
        client.setContacts(contacts).forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        sleepInRecordMode(6000);
        client.listContacts().stream().forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
    }

    @Test
    public void deleteContacts() {
        List<CertificateContact> contacts = Arrays.asList(setupContact());
        client.setContacts(contacts).forEach((retrievedContact) -> validateContact(setupContact(), retrievedContact));
        PagedIterable<CertificateContact> certificateContacts  = client.deleteContacts();
        validateContact(setupContact(), certificateContacts.iterator().next());
    }

    @Test
    public void getCertificateOperatioNotFound() {
        assertRestException(() -> client.getCertificateOperation("non-existing").poll(),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void getCertificatePolicyNotFound() {
        assertRestException(() -> client.getCertificatePolicy("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void listCertificateVersions() {
        String certName = "testListCertVersion";
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
        deleteAndPurgeCertificate(certName);
    }

    @Test
    public void listDeletedCertificates() {
        listDeletedCertificatesRunner((certificates) -> {
            HashSet<String> certificatesToDelete = new HashSet<>(certificates);
            for (String certName :  certificatesToDelete) {
                SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = client.beginCreateCertificate(certName,
                    CertificatePolicy.getDefault());
                certPoller.waitForCompletion();
            }

            for (String certName : certificates) {
                SyncPoller<DeletedCertificate, Void> poller = client.beginDeleteCertificate(certName);
                PollResponse<DeletedCertificate> pollResponse = poller.poll();
                poller.waitForCompletion();
            }
            Iterable<DeletedCertificate> deletedCertificates =  client.listDeletedCertificates();
            for (DeletedCertificate deletedCertificate : deletedCertificates) {
                if (certificatesToDelete.contains(deletedCertificate.getName())) {
                    assertNotNull(deletedCertificate.getDeletedOn());
                    assertNotNull(deletedCertificate.getRecoveryId());
                    certificatesToDelete.remove(deletedCertificate.getName());
                }
            }

            assertEquals(0, certificatesToDelete.size());

            for (DeletedCertificate deletedCertificate : deletedCertificates) {
                client.purgeDeletedCertificate(deletedCertificate.getName());
                pollOnCertificatePurge(deletedCertificate.getName());
            }
            sleepInRecordMode(10000);
        });
    }

    @Test
    public void importCertificate() {
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
            deleteAndPurgeCertificate(importCertificateOptions.getName());
        });
    }

    @Test
    public void mergeCertificateNotFound() {
        assertRestException(() -> client.mergeCertificate(new MergeCertificateOptions("testCert16", Arrays.asList("test".getBytes()))),
            HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
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
