// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.AdministratorContact;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains code samples for generating javadocs through doclets for {@link CertificateClient}
 */
public final class CertificateClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Implementation for.CertificateClient
     *
     * @return sync CertificateClient
     */
    private CertificateClient getCertificateClient() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.instantiation
        CertificateClient certificateClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .vaultUrl("https://myvault.vault.azure.net/")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.security.keyvault.certificates.CertificateClient.instantiation
        return certificateClient;
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificatePolicy(String)}
     */
    public void getCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string
        CertificatePolicy policy = certificateClient.getCertificatePolicy("certificateName");
        System.out.printf("Received policy with subject name %s%n", policy.getSubject());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
        Response<CertificatePolicy> returnedPolicyWithResponse = certificateClient.getCertificatePolicyWithResponse(
            "certificateName", new Context(key1, value1));
        System.out.printf("Received policy with subject name %s%n",
            returnedPolicyWithResponse.getValue().getSubject());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificate(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String
        KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate("certificateName");
        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            certificate.getProperties().getName(),
            certificate.getProperties().getVersion(), certificate.getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-Context
        Response<KeyVaultCertificateWithPolicy> certificateWithResponse = certificateClient
            .getCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            certificateWithResponse.getValue().getProperties().getName(),
            certificateWithResponse.getValue().getProperties().getVersion(), certificate.getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-Context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersionWithResponse#String-String-Context
        Response<KeyVaultCertificate> returnedCertificateWithResponse = certificateClient
            .getCertificateVersionWithResponse("certificateName", "certificateVersion",
                new Context(key1, value1));
        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            returnedCertificateWithResponse.getValue().getProperties().getName(),
            returnedCertificateWithResponse.getValue().getProperties().getVersion(),
            returnedCertificateWithResponse.getValue().getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersionWithResponse#String-String-Context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String
        KeyVaultCertificate returnedCertificate = certificateClient.getCertificateVersion("certificateName",
            "certificateVersion");
        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            returnedCertificate.getProperties().getName(), returnedCertificate.getProperties().getVersion(),
            returnedCertificate.getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateVersion#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties
        for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate with name %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#beginCreateCertificate(String, CertificatePolicy,
     * Boolean, Map)} and {@link CertificateClient#beginCreateCertificate(String, CertificatePolicy)}.
     */
    public void createCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map
        CertificatePolicy certificatePolicyPkcsSelf = new CertificatePolicy("Self",
            "CN=SelfSignedJavaPkcs12");
        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certificateSyncPoller = certificateClient
            .beginCreateCertificate("certificateName", certificatePolicyPkcsSelf, true, new HashMap<>());
        certificateSyncPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        KeyVaultCertificate createdCertificate = certificateSyncPoller.getFinalResult();
        System.out.printf("Certificate created with name %s%n", createdCertificate.getName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy
        CertificatePolicy certPolicy = new CertificatePolicy("Self",
            "CN=SelfSignedJavaPkcs12");
        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = certificateClient
            .beginCreateCertificate("certificateName", certPolicy);
        certPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        KeyVaultCertificate cert = certPoller.getFinalResult();
        System.out.printf("Certificate created with name %s%n", cert.getName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateOperation(String)}.
     */
    public void getCertificateOperation() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateOperation#String
        SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> getCertPoller = certificateClient
            .getCertificateOperation("certificateName");
        getCertPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
        KeyVaultCertificate cert = getCertPoller.getFinalResult();
        System.out.printf("Certificate created with name %s%n", cert.getName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateOperation#String
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#createIssuer(CertificateIssuer)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer
        CertificateIssuer issuerToCreate = new CertificateIssuer("myissuer", "myProvider")
            .setAccountId("testAccount")
            .setAdministratorContacts(Collections.singletonList(new AdministratorContact().setFirstName("test")
                .setLastName("name").setEmail("test@example.com")));
        CertificateIssuer returnedIssuer = certificateClient.createIssuer(issuerToCreate);
        System.out.printf("Created Issuer with name %s provider %s%n", returnedIssuer.getName(),
            returnedIssuer.getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-Context
        CertificateIssuer issuer = new CertificateIssuer("issuerName", "myProvider")
            .setAccountId("testAccount")
            .setAdministratorContacts(Collections.singletonList(new AdministratorContact().setFirstName("test")
                .setLastName("name").setEmail("test@example.com")));
        Response<CertificateIssuer> issuerResponse = certificateClient.createIssuerWithResponse(issuer,
            new Context(key1, value1));
        System.out.printf("Created Issuer with name %s provider %s%n", issuerResponse.getValue().getName(),
            issuerResponse.getValue().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string
        CertificateIssuer returnedIssuer = certificateClient.getIssuer("issuerName");
        System.out.printf("Retrieved issuer with name %s and provider %s%n", returnedIssuer.getName(),
            returnedIssuer.getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context
        Response<CertificateIssuer> issuerResponse = certificateClient.getIssuerWithResponse("issuerName",
            new Context(key1, value1));
        System.out.printf("Retrieved issuer with name %s and provider %s%n", issuerResponse.getValue().getName(),
            issuerResponse.getValue().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties
        KeyVaultCertificate certificate = certificateClient.getCertificate("certificateName");
        // Update certificate enabled status
        certificate.getProperties().setEnabled(false);
        KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());
        System.out.printf("Updated Certificate with name %s and enabled status %s%n",
            updatedCertificate.getProperties().getName(), updatedCertificate.getProperties().isEnabled());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context
        KeyVaultCertificate certificateToUpdate = certificateClient.getCertificate("certificateName");
        // Update certificate enabled status
        certificateToUpdate.getProperties().setEnabled(false);
        Response<KeyVaultCertificate> updatedCertificateResponse = certificateClient.
            updateCertificatePropertiesWithResponse(certificateToUpdate.getProperties(), new Context(key1, value1));
        System.out.printf("Updated Certificate with name %s and enabled status %s%n",
            updatedCertificateResponse.getValue().getProperties().getName(),
            updatedCertificateResponse.getValue().getProperties().isEnabled());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateIssuer(CertificateIssuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer
        CertificateIssuer returnedIssuer = certificateClient.getIssuer("issuerName");
        returnedIssuer.setAccountId("newAccountId");
        CertificateIssuer updatedIssuer = certificateClient.updateIssuer(returnedIssuer);
        System.out.printf("Updated issuer with name %s, provider %s and account Id %s%n", updatedIssuer.getName(),
            updatedIssuer.getProvider(), updatedIssuer.getAccountId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-Context
        CertificateIssuer issuer = certificateClient.getIssuer("issuerName");
        returnedIssuer.setAccountId("newAccountId");
        Response<CertificateIssuer> updatedIssuerWithResponse = certificateClient.updateIssuerWithResponse(issuer,
            new Context(key1, value1));
        System.out.printf("Updated issuer with name %s, provider %s and account Id %s%n",
            updatedIssuerWithResponse.getValue().getName(),
            updatedIssuerWithResponse.getValue().getProvider(),
            updatedIssuerWithResponse.getValue().getAccountId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string
        CertificatePolicy certificatePolicy = certificateClient.getCertificatePolicy("certificateName");
        //Update the certificate policy cert transparency property.
        certificatePolicy.setCertificateTransparent(true);
        CertificatePolicy updatedCertPolicy = certificateClient.updateCertificatePolicy("certificateName",
            certificatePolicy);
        System.out.printf("Updated Certificate Policy transparency status %s%n",
            updatedCertPolicy.isCertificateTransparent());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
        CertificatePolicy certificatePolicyToUpdate = certificateClient.getCertificatePolicy("certificateName");
        //Update the certificate policy cert transparency property.
        certificatePolicyToUpdate.setCertificateTransparent(true);
        Response<CertificatePolicy> updatedCertPolicyWithResponse = certificateClient
            .updateCertificatePolicyWithResponse("certificateName", certificatePolicyToUpdate,
                new Context(key1, value1));
        System.out.printf("Updated Certificate Policy transparency status %s%n", updatedCertPolicyWithResponse
            .getValue().isCertificateTransparent());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#beginDeleteCertificate(String)}.
     */
    public void deleteCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String
        SyncPoller<DeletedCertificate, Void> deleteCertPoller =
            certificateClient.beginDeleteCertificate("certificateName");
        // Deleted Certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> deleteCertPollResponse = deleteCertPoller.poll();
        System.out.printf("Deleted certificate with name %s and recovery id %s%n",
            deleteCertPollResponse.getValue().getName(), deleteCertPollResponse.getValue().getRecoveryId());
        deleteCertPoller.waitForCompletion();
        // END: com.azure.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteIssuer(String)}
     */
    public void deleteCertificateIssuefrCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context
        CertificateIssuer deletedIssuer = certificateClient.deleteIssuer("certificateName");
        System.out.printf("Deleted certificate issuer with name %s and provider id %s%n", deletedIssuer.getName(),
            deletedIssuer.getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string
        Response<CertificateIssuer> deletedIssuerWithResponse = certificateClient.
            deleteIssuerWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certificate issuer with name %s and provider id %s%n",
            deletedIssuerWithResponse.getValue().getName(),
            deletedIssuerWithResponse.getValue().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string
        DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate("certificateName");
        System.out.printf("Deleted certificate with name %s and recovery id %s%n", deletedCertificate.getName(),
            deletedCertificate.getRecoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context
        Response<DeletedCertificate> deletedCertificateWithResponse = certificateClient
            .getDeletedCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certificate with name %s and recovery id %s%n",
            deletedCertificateWithResponse.getValue().getName(),
            deletedCertificateWithResponse.getValue().getRecoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#purgeDeletedCertificateWithResponse(String,
     * Context)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string
        certificateClient.purgeDeletedCertificate("certificateName");
        // END: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#purgeDeletedCertificateWithResponse(String,
     * Context)}
     */
    public void purgeDeletedCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context
        Response<Void> purgeResponse = certificateClient.purgeDeletedCertificateWithResponse("certificateName",
            new Context(key1, value1));
        System.out.printf("Purged Deleted certificate with status %d%n", purgeResponse.getStatusCode());
        // END: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#beginRecoverDeletedCertificate(String)}.
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.certificatevault.certificates.CertificateClient.beginRecoverDeletedCertificate#String
        SyncPoller<KeyVaultCertificateWithPolicy, Void> recoverDeletedCertPoller = certificateClient
            .beginRecoverDeletedCertificate("deletedCertificateName");
        // Recovered certificate is accessible as soon as polling beings
        PollResponse<KeyVaultCertificateWithPolicy> recoverDeletedCertPollResponse = recoverDeletedCertPoller.poll();
        System.out.printf(" Recovered Deleted certificate with name %s and id %s%n",
            recoverDeletedCertPollResponse.getValue().getProperties().getName(),
            recoverDeletedCertPollResponse.getValue().getProperties().getId());
        recoverDeletedCertPoller.waitForCompletion();
        // END: com.azure.security.certificatevault.certificates.CertificateClient.beginRecoverDeletedCertificate#String
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string
        byte[] certificateBackup = certificateClient.backupCertificate("certificateName");
        System.out.printf("Backed up certificate with back up blob length %d%n", certificateBackup.length);
        // END: com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context
        Response<byte[]> certificateBackupWithResponse = certificateClient
            .backupCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Backed up certificate with back up blob length %d%n",
            certificateBackupWithResponse.getValue().length);
        // END: com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#restoreCertificateBackup(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte
        byte[] certificateBackupBlob = {};
        KeyVaultCertificate certificate = certificateClient.restoreCertificateBackup(certificateBackupBlob);
        System.out.printf(" Restored certificate with name %s and id %s%n",
            certificate.getProperties().getName(), certificate.getProperties().getId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context
        byte[] certificateBackupBlobArray = {};
        Response<KeyVaultCertificateWithPolicy> certificateResponse = certificateClient
            .restoreCertificateBackupWithResponse(certificateBackupBlobArray, new Context(key1, value1));
        System.out.printf(" Restored certificate with name %s and id %s%n",
            certificateResponse.getValue().getProperties().getName(),
            certificateResponse.getValue().getProperties().getId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listPropertiesOfCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificates
        for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate with name %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificates

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context
        for (CertificateProperties certificateProperties : certificateClient
            .listPropertiesOfCertificates(true, new Context(key1, value1))) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate with name %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listPropertiesOfIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers
        for (IssuerProperties issuer : certificateClient.listPropertiesOfIssuers()) {
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(issuer.getName());
            System.out.printf("Received issuer with name %s and provider %s%n", retrievedIssuer.getName(),
                retrievedIssuer.getProvider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#context
        for (IssuerProperties issuer : certificateClient.listPropertiesOfIssuers(new Context(key1, value1))) {
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(issuer.getName());
            System.out.printf("Received issuer with name %s and provider %s%n", retrievedIssuer.getName(),
                retrievedIssuer.getProvider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates
        for (DeletedCertificate deletedCertificate : certificateClient.listDeletedCertificates()) {
            System.out.printf("Deleted certificate's recovery Id %s%n", deletedCertificate.getRecoveryId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context
        for (DeletedCertificate deletedCertificate : certificateClient
            .listDeletedCertificates(true, new Context(key1, value1))) {
            System.out.printf("Deleted certificate's recovery Id %s%n", deletedCertificate.getRecoveryId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listPropertiesOfCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions
        for (CertificateProperties certificateProperties : certificateClient
            .listPropertiesOfCertificateVersions("certificateName")) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate's version with name %s, version %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getProperties().getVersion(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context
        for (CertificateProperties certificateProperties : certificateClient
            .listPropertiesOfCertificateVersions("certificateName")) {
            KeyVaultCertificate certificateWithAllProperties = certificateClient
                .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
            System.out.printf("Received certificate's version with name %s, version %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getProperties().getVersion(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#setContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts
        CertificateContact contactToAdd = new CertificateContact().setName("user").setEmail("useremail@example.com");
        for (CertificateContact contact : certificateClient.setContacts(Collections.singletonList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context
        CertificateContact sampleContact = new CertificateContact().setName("user").setEmail("useremail@example.com");
        for (CertificateContact contact : certificateClient.setContacts(Collections.singletonList(sampleContact),
            new Context(key1, value1))) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listContacts
        for (CertificateContact contact : certificateClient.listContacts()) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listContacts#context
        for (CertificateContact contact : certificateClient.listContacts(new Context(key1, value1))) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listContacts#context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts
        for (CertificateContact contact : certificateClient.deleteContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context
        for (CertificateContact contact : certificateClient.deleteContacts(new Context(key1, value1))) {
            System.out.printf("Deleted contact with name %s and email %s from key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificateOperation(String)} (String)} and
     * {@link CertificateClient#deleteCertificateOperationWithResponse(String, Context)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string
        CertificateOperation deletedCertificateOperation = certificateClient
            .deleteCertificateOperation("certificateName");
        System.out.printf("Deleted Certificate Operation's last status %s%n", deletedCertificateOperation.getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string
        Response<CertificateOperation> deletedCertificateOperationWithResponse = certificateClient
            .deleteCertificateOperationWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted Certificate Operation's last status %s%n",
            deletedCertificateOperationWithResponse.getValue().getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string
        CertificateOperation certificateOperation = certificateClient
            .cancelCertificateOperation("certificateName");
        System.out.printf("Certificate Operation status %s%n", certificateOperation.getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string
        Response<CertificateOperation> certificateOperationWithResponse = certificateClient
            .cancelCertificateOperationWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Certificate Operation status %s%n", certificateOperationWithResponse.getValue().getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#mergeCertificate(MergeCertificateOptions)}
     */
    public void mergeCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config
        List<byte[]> x509CertificatesToMerge = new ArrayList<>();
        MergeCertificateOptions config =
            new MergeCertificateOptions("certificateName", x509CertificatesToMerge)
                .setEnabled(false);
        KeyVaultCertificate mergedCertificate = certificateClient.mergeCertificate(config);
        System.out.printf("Received Certificate with name %s and key id %s%n",
            mergedCertificate.getProperties().getName(), mergedCertificate.getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
        List<byte[]> x509CertsToMerge = new ArrayList<>();
        MergeCertificateOptions mergeConfig =
            new MergeCertificateOptions("certificateName", x509CertsToMerge)
                .setEnabled(false);
        Response<KeyVaultCertificateWithPolicy> mergedCertificateWithResponse =
            certificateClient.mergeCertificateWithResponse(mergeConfig, new Context(key2, value2));
        System.out.printf("Received Certificate with name %s and key id %s%n",
            mergedCertificateWithResponse.getValue().getProperties().getName(),
            mergedCertificateWithResponse.getValue().getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#importCertificate(ImportCertificateOptions)}
     */
    public void importCertificate() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.importCertificate#options
        byte[] certificateToImport = new byte[100];
        ImportCertificateOptions config =
            new ImportCertificateOptions("certificateName", certificateToImport).setEnabled(false);
        KeyVaultCertificate importedCertificate = certificateClient.importCertificate(config);
        System.out.printf("Received Certificate with name %s and key id %s%n",
            importedCertificate.getProperties().getName(), importedCertificate.getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.importCertificate#options

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options
        byte[] certToImport = new byte[100];
        ImportCertificateOptions importCertificateOptions =
            new ImportCertificateOptions("certificateName", certToImport).setEnabled(false);
        Response<KeyVaultCertificateWithPolicy> importedCertificateWithResponse =
            certificateClient.importCertificateWithResponse(importCertificateOptions, new Context(key2, value2));
        System.out.printf("Received Certificate with name %s and key id %s%n",
            importedCertificateWithResponse.getValue().getProperties().getName(),
            importedCertificateWithResponse.getValue().getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options
    }
}
