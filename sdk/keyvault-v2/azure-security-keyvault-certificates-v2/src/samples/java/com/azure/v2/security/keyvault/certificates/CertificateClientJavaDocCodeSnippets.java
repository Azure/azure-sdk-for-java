// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.certificates.models.AdministratorContact;
import com.azure.v2.security.keyvault.certificates.models.CertificateContact;
import com.azure.v2.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.v2.security.keyvault.certificates.models.CertificateOperation;
import com.azure.v2.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.v2.security.keyvault.certificates.models.CertificateProperties;
import com.azure.v2.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.v2.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.v2.security.keyvault.certificates.models.IssuerProperties;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.v2.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.v2.security.keyvault.certificates.models.MergeCertificateOptions;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains code samples for generating javadocs through doclets for {@link CertificateClient}
 */
public final class CertificateClientJavaDocCodeSnippets {
    /**
     * Implementation for CertificateClient
     *
     * @return sync CertificateClient
     */
    private CertificateClient getCertificateClient() {
        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.instantiation
        CertificateClient certificateClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-key-vault-url>")
            .httpInstrumentationOptions(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.instantiation

        return certificateClient;
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificatePolicy(String)}
     */
    public void getCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string
        CertificatePolicy policy = certificateClient.getCertificatePolicy("certificateName");

        System.out.printf("Received policy with subject name %s%n", policy.getSubject());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificatePolicy> returnedPolicyWithResponse =
            certificateClient.getCertificatePolicyWithResponse("certificateName", requestContext);

        System.out.printf("Received policy with subject name %s%n",
            returnedPolicyWithResponse.getValue().getSubject());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificate(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String
        KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate("certificateName");

        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            certificate.getProperties().getName(),
            certificate.getProperties().getVersion(), certificate.getSecretId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        // Passing a null or empty version retrieves the latest certificate version.
        Response<KeyVaultCertificateWithPolicy> certificateWithResponse =
            certificateClient.getCertificateWithResponse("certificateName", null, requestContext);

        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            certificateWithResponse.getValue().getProperties().getName(),
            certificateWithResponse.getValue().getProperties().getVersion(), certificate.getSecretId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultCertificateWithPolicy> returnedCertificateWithResponse =
            certificateClient.getCertificateWithResponse("certificateName", "certificateVersion",
                reqContext);

        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            returnedCertificateWithResponse.getValue().getProperties().getName(),
            returnedCertificateWithResponse.getValue().getProperties().getVersion(),
            returnedCertificateWithResponse.getValue().getSecretId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String-String
        KeyVaultCertificateWithPolicy returnedCertificate =
            certificateClient.getCertificate("certificateName", "certificateVersion");

        System.out.printf("Received certificate with name %s and version %s and secret id %s%n",
            returnedCertificate.getProperties().getName(), returnedCertificate.getProperties().getVersion(),
            returnedCertificate.getSecretId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#String-String

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties
        for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
            KeyVaultCertificateWithPolicy certificateWithAllProperties = null;
                certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());

            System.out.printf("Received certificate with name %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties
    }

    /**
     * Method to insert code snippets for {/@link CertificateClient#beginCreateCertificate(String, CertificatePolicy,
     * Boolean, Map)} and {/@link CertificateClient#beginCreateCertificate(String, CertificatePolicy)}.
     */
    public void createCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy
        CertificatePolicy certPolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");

        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> certPoller = null;
            //certificateClient.beginCreateCertificate("certificateName", certPolicy);

        certPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate cert = certPoller.getFinalResult();

        System.out.printf("Certificate created with name %s%n", cert.getName());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map
        CertificatePolicy policy = new CertificatePolicy("Self",
            "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();

        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> certificateSyncPoller = null;
            //certificateClient.beginCreateCertificate("certificateName", policy, true, tags);

        certificateSyncPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate createdCertificate = certificateSyncPoller.getFinalResult();

        System.out.printf("Certificate created with name %s%n", createdCertificate.getName());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map-Boolean
        CertificatePolicy certificatePolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> certTags = new HashMap<>();

        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> poller = null;
            //certificateClient.beginCreateCertificate("certificateName", certificatePolicy, true, certTags, true);

        poller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate certificate = poller.getFinalResult();

        System.out.printf("Certificate created with name %s%n", certificate.getName());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map-Boolean
    }

    /**
     * Method to insert code snippets for {/@link CertificateClient#getCertificateOperation(String)}.
     */
    public void getCertificateOperation() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String
        Poller<CertificateOperation, KeyVaultCertificateWithPolicy> getCertPoller = null;
            //certificateClient.getCertificateOperation("certificateName");

        getCertPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);

        KeyVaultCertificate cert = getCertPoller.getFinalResult();

        System.out.printf("Certificate created with name %s%n", cert.getName());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getCertificateOperation#String
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#createIssuer(CertificateIssuer)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer
        CertificateIssuer issuerToCreate = new CertificateIssuer("myissuer", "myProvider")
            .setAccountId("testAccount")
            .setAdministratorContacts(Collections.singletonList(new AdministratorContact().setFirstName("test")
                .setLastName("name").setEmail("test@example.com")));

        CertificateIssuer returnedIssuer = certificateClient.createIssuer(issuerToCreate);

        System.out.printf("Created Issuer with name %s provider %s%n", returnedIssuer.getName(),
            returnedIssuer.getProvider());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuer#CertificateIssuer

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-RequestContext
        CertificateIssuer issuer = new CertificateIssuer("issuerName", "myProvider")
            .setAccountId("testAccount")
            .setAdministratorContacts(Collections.singletonList(
                new AdministratorContact()
                    .setFirstName("test")
                    .setLastName("name")
                    .setEmail("test@example.com")));
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificateIssuer> issuerResponse = certificateClient.createIssuerWithResponse(issuer, requestContext);

        System.out.printf("Created Issuer with name %s provider %s%n", issuerResponse.getValue().getName(),
            issuerResponse.getValue().getProvider());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#CertificateIssuer-RequestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#string
        CertificateIssuer returnedIssuer = certificateClient.getIssuer("issuerName");

        System.out.printf("Retrieved issuer with name %s and provider %s%n", returnedIssuer.getName(),
            returnedIssuer.getProvider());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuer#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-requestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificateIssuer> issuerResponse =
            certificateClient.getIssuerWithResponse("issuerName", requestContext);

        System.out.printf("Retrieved issuer with name %s and provider %s%n", issuerResponse.getValue().getName(),
            issuerResponse.getValue().getProvider());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-requestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties
        KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate("certificateName");

        // Update certificate enabled status
        certificate.getProperties().setEnabled(false);

        KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());

        System.out.printf("Updated Certificate with name %s and enabled status %s%n",
            updatedCertificate.getProperties().getName(), updatedCertificate.getProperties().isEnabled());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-RequestContext
        KeyVaultCertificateWithPolicy certificateToUpdate = certificateClient.getCertificate("certificateName");

        // Update certificate enabled status
        certificateToUpdate.getProperties().setEnabled(false);

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultCertificate> updatedCertificateResponse =
            certificateClient.updateCertificatePropertiesWithResponse(certificateToUpdate.getProperties(),
                requestContext);

        System.out.printf("Updated Certificate with name %s and enabled status %s%n",
            updatedCertificateResponse.getValue().getProperties().getName(),
            updatedCertificateResponse.getValue().getProperties().isEnabled());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-RequestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateIssuer(CertificateIssuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer
        CertificateIssuer returnedIssuer = certificateClient.getIssuer("issuerName");

        returnedIssuer.setAccountId("newAccountId");

        CertificateIssuer updatedIssuer = certificateClient.updateIssuer(returnedIssuer);

        System.out.printf("Updated issuer with name %s, provider %s and account Id %s%n", updatedIssuer.getName(),
            updatedIssuer.getProvider(), updatedIssuer.getAccountId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuer#CertificateIssuer

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-RequestContext
        CertificateIssuer issuer = certificateClient.getIssuer("issuerName");

        returnedIssuer.setAccountId("newAccountId");

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificateIssuer> updatedIssuerWithResponse =
            certificateClient.updateIssuerWithResponse(issuer, requestContext);

        System.out.printf("Updated issuer with name %s, provider %s and account Id %s%n",
            updatedIssuerWithResponse.getValue().getName(),
            updatedIssuerWithResponse.getValue().getProvider(),
            updatedIssuerWithResponse.getValue().getAccountId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#CertificateIssuer-RequestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string
        CertificatePolicy certificatePolicy = certificateClient.getCertificatePolicy("certificateName");

        //Update the certificate policy cert transparency property.
        certificatePolicy.setCertificateTransparent(true);

        CertificatePolicy updatedCertPolicy =
            certificateClient.updateCertificatePolicy("certificateName", certificatePolicy);

        System.out.printf("Updated Certificate Policy transparency status %s%n",
            updatedCertPolicy.isCertificateTransparent());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
        CertificatePolicy certificatePolicyToUpdate = certificateClient.getCertificatePolicy("certificateName");

        //Update the certificate policy cert transparency property.
        certificatePolicyToUpdate.setCertificateTransparent(true);

        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificatePolicy> updatedCertPolicyWithResponse = null;
            certificateClient.updateCertificatePolicyWithResponse("certificateName",
                certificatePolicyToUpdate, requestContext);

        System.out.printf("Updated Certificate Policy transparency status %s%n", updatedCertPolicyWithResponse
            .getValue().isCertificateTransparent());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {/@link CertificateClient#beginDeleteCertificate(String)}.
     */
    public void deleteCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String
        Poller<DeletedCertificate, Void> deleteCertPoller = null;
            //certificateClient.beginDeleteCertificate("certificateName");

        // Deleted Certificate is accessible as soon as polling beings.
        PollResponse<DeletedCertificate> deleteCertPollResponse = deleteCertPoller.poll();

        System.out.printf("Deleted certificate with name %s and recovery id %s%n",
            deleteCertPollResponse.getValue().getName(), deleteCertPollResponse.getValue().getRecoveryId());
        deleteCertPoller.waitForCompletion();
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.beginDeleteCertificate#String
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteIssuer(String)}
     */
    public void deleteCertificateIssuefrCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-requestContext
        CertificateIssuer deletedIssuer = certificateClient.deleteIssuer("issuerName");

        System.out.printf("Deleted certificate issuer with name %s and provider id %s%n", deletedIssuer.getName(),
            deletedIssuer.getProvider());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-requestContext

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#string
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificateIssuer> deletedIssuerWithResponse =
            certificateClient.deleteIssuerWithResponse("issuerName", requestContext);

        System.out.printf("Deleted certificate issuer with name %s and provider id %s%n",
            deletedIssuerWithResponse.getValue().getName(),
            deletedIssuerWithResponse.getValue().getProvider());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string
        DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate("certificateName");

        System.out.printf("Deleted certificate with name %s and recovery id %s%n", deletedCertificate.getName(),
            deletedCertificate.getRecoveryId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<DeletedCertificate> deletedCertificateWithResponse =
            certificateClient.getDeletedCertificateWithResponse("certificateName", requestContext);

        System.out.printf("Deleted certificate with name %s and recovery id %s%n",
            deletedCertificateWithResponse.getValue().getName(),
            deletedCertificateWithResponse.getValue().getRecoveryId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-RequestContext
    }

    /**
     * Method to insert code snippets for {/@link CertificateClient#purgeDeletedCertificateWithResponse(String,
     * RequestContext)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string
        certificateClient.purgeDeletedCertificate("certificateName");
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string
    }

    /**
     * Method to insert code snippets for {/@link CertificateClient#purgeDeletedCertificateWithResponse(String,
     * RequestContext)}
     */
    public void purgeDeletedCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<Void> purgeResponse =
            certificateClient.purgeDeletedCertificateWithResponse("certificateName", requestContext);

        System.out.printf("Purged Deleted certificate with status %d%n", purgeResponse.getStatusCode());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-RequestContext
    }

    /**
     * Method to insert code snippets for {/@link CertificateClient#beginRecoverDeletedCertificate(String)}.
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String
        Poller<KeyVaultCertificateWithPolicy, Void> recoverDeletedCertPoller = null;
            //certificateClient.beginRecoverDeletedCertificate("deletedCertificateName");

        // Recovered certificate is accessible as soon as polling beings
        PollResponse<KeyVaultCertificateWithPolicy> recoverDeletedCertPollResponse = recoverDeletedCertPoller.poll();

        System.out.printf(" Recovered Deleted certificate with name %s and id %s%n",
            recoverDeletedCertPollResponse.getValue().getProperties().getName(),
            recoverDeletedCertPollResponse.getValue().getProperties().getId());

        recoverDeletedCertPoller.waitForCompletion();
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.beginRecoverDeletedCertificate#String
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#string
        byte[] certificateBackup = certificateClient.backupCertificate("certificateName");

        System.out.printf("Backed up certificate with back up blob length %d%n", certificateBackup.length);
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificate#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<byte[]> certificateBackupWithResponse =
            certificateClient.backupCertificateWithResponse("certificateName", requestContext);

        System.out.printf("Backed up certificate with back up blob length %d%n",
            certificateBackupWithResponse.getValue().length);
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-RequestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#restoreCertificateBackup(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificate#byte
        byte[] certificateBackupBlob = {};

        KeyVaultCertificate certificate = certificateClient.restoreCertificateBackup(certificateBackupBlob);

        System.out.printf(" Restored certificate with name %s and id %s%n",
            certificate.getProperties().getName(), certificate.getProperties().getId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificate#byte

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-RequestContext
        byte[] certificateBackupBlobArray = {};
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultCertificateWithPolicy> certificateResponse =
            certificateClient.restoreCertificateBackupWithResponse(certificateBackupBlobArray, requestContext);

        System.out.printf(" Restored certificate with name %s and id %s%n",
            certificateResponse.getValue().getProperties().getName(),
            certificateResponse.getValue().getProperties().getId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-RequestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listPropertiesOfCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates
        for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
            KeyVaultCertificateWithPolicy certificateWithAllProperties =
                certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());

            System.out.printf("Received certificate with name %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates#requestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        for (CertificateProperties certificateProperties : certificateClient
            .listPropertiesOfCertificates(true, requestContext)) {

            KeyVaultCertificateWithPolicy certificateWithAllProperties =
                certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());

            System.out.printf("Received certificate with name %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificates#requestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listPropertiesOfIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers
        for (IssuerProperties issuer : certificateClient.listPropertiesOfIssuers()) {
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(issuer.getName());

            System.out.printf("Received issuer with name %s and provider %s%n", retrievedIssuer.getName(),
                retrievedIssuer.getProvider());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#requestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        for (IssuerProperties issuer : certificateClient.listPropertiesOfIssuers(requestContext)) {
            CertificateIssuer retrievedIssuer = certificateClient.getIssuer(issuer.getName());

            System.out.printf("Received issuer with name %s and provider %s%n", retrievedIssuer.getName(),
                retrievedIssuer.getProvider());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listPropertiesOfIssuers#requestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates
        for (DeletedCertificate deletedCertificate : certificateClient.listDeletedCertificates()) {
            System.out.printf("Deleted certificate's recovery Id %s%n", deletedCertificate.getRecoveryId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#requestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        for (DeletedCertificate deletedCertificate : certificateClient
            .listDeletedCertificates(true, requestContext)) {

            System.out.printf("Deleted certificate's recovery Id %s%n", deletedCertificate.getRecoveryId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listDeletedCertificates#requestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listPropertiesOfCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions
        for (CertificateProperties certificateProperties : certificateClient
            .listPropertiesOfCertificateVersions("certificateName")) {

            KeyVaultCertificateWithPolicy certificateWithAllProperties =
                certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());

            System.out.printf("Received certificate's version with name %s, version %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getProperties().getVersion(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions#requestContext
        for (CertificateProperties certificateProperties : certificateClient
            .listPropertiesOfCertificateVersions("certificateName")) {

            KeyVaultCertificateWithPolicy certificateWithAllProperties =
                certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());

            System.out.printf("Received certificate's version with name %s, version %s and secret id %s%n",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getProperties().getVersion(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listCertificateVersions#requestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#setContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts
        CertificateContact contactToAdd = new CertificateContact().setName("user").setEmail("useremail@example.com");

        for (CertificateContact contact : certificateClient.setContacts(Collections.singletonList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts-requestContext
        CertificateContact sampleContact = new CertificateContact().setName("user").setEmail("useremail@example.com");
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        for (CertificateContact contact : certificateClient.setContacts(Collections.singletonList(sampleContact),
            requestContext)) {

            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.setContacts#contacts-requestContext

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts
        for (CertificateContact contact : certificateClient.listContacts()) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#requestContext
        for (CertificateContact contact : certificateClient.listContacts(requestContext)) {
            System.out.printf("Added contact with name %s and email %s to key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.listContacts#requestContext

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts
        for (CertificateContact contact : certificateClient.deleteContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts#requestContext
        for (CertificateContact contact : certificateClient.deleteContacts(requestContext)) {
            System.out.printf("Deleted contact with name %s and email %s from key vault%n", contact.getName(),
                contact.getEmail());
        }
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteContacts#requestContext
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificateOperation(String)} and
     * {@link CertificateClient#deleteCertificateOperationWithResponse(String, RequestContext)}.
     */
    public void certificateOperationCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string
        CertificateOperation deletedCertificateOperation =
            certificateClient.deleteCertificateOperation("certificateName");

        System.out.printf("Deleted Certificate Operation's last status %s%n", deletedCertificateOperation.getStatus());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<CertificateOperation> deletedCertificateOperationWithResponse =
            certificateClient.deleteCertificateOperationWithResponse("certificateName", requestContext);

        System.out.printf("Deleted Certificate Operation's last status %s%n",
            deletedCertificateOperationWithResponse.getValue().getStatus());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string
        CertificateOperation certificateOperation =
            certificateClient.cancelCertificateOperation("certificateName");

        System.out.printf("Certificate Operation status %s%n", certificateOperation.getStatus());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string
        Response<CertificateOperation> certificateOperationWithResponse =
            certificateClient.cancelCertificateOperationWithResponse("certificateName", requestContext);

        System.out.printf("Certificate Operation status %s%n", certificateOperationWithResponse.getValue().getStatus());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#mergeCertificate(MergeCertificateOptions)}
     */
    public void mergeCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#config
        List<byte[]> x509CertificatesToMerge = new ArrayList<>();
        MergeCertificateOptions config =
            new MergeCertificateOptions("certificateName", x509CertificatesToMerge)
                .setEnabled(false);

        KeyVaultCertificate mergedCertificate = certificateClient.mergeCertificate(config);

        System.out.printf("Received Certificate with name %s and key id %s%n",
            mergedCertificate.getProperties().getName(), mergedCertificate.getKeyId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificate#config

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
        List<byte[]> x509CertsToMerge = new ArrayList<>();
        MergeCertificateOptions mergeConfig =
            new MergeCertificateOptions("certificateName", x509CertsToMerge)
                .setEnabled(false);
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultCertificateWithPolicy> mergedCertificateWithResponse =
            certificateClient.mergeCertificateWithResponse(mergeConfig, requestContext);

        System.out.printf("Received Certificate with name %s and key id %s%n",
            mergedCertificateWithResponse.getValue().getProperties().getName(),
            mergedCertificateWithResponse.getValue().getKeyId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#importCertificate(ImportCertificateOptions)}
     */
    public void importCertificate() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#options
        byte[] certificateToImport = new byte[100];
        ImportCertificateOptions config =
            new ImportCertificateOptions("certificateName", certificateToImport).setEnabled(false);

        KeyVaultCertificate importedCertificate = certificateClient.importCertificate(config);

        System.out.printf("Received Certificate with name %s and key id %s%n",
            importedCertificate.getProperties().getName(), importedCertificate.getKeyId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificate#options

        // BEGIN: com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options
        byte[] certToImport = new byte[100];
        ImportCertificateOptions importCertificateOptions =
            new ImportCertificateOptions("certificateName", certToImport).setEnabled(false);
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultCertificateWithPolicy> importedCertificateWithResponse =
            certificateClient.importCertificateWithResponse(importCertificateOptions, requestContext);

        System.out.printf("Received Certificate with name %s and key id %s%n",
            importedCertificateWithResponse.getValue().getProperties().getName(),
            importedCertificateWithResponse.getValue().getKeyId());
        // END: com.azure.v2.security.keyvault.certificates.CertificateClient.importCertificateWithResponse#options
    }
}
