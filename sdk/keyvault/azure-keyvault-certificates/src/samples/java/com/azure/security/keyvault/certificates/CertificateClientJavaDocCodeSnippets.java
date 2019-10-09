// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.IssuerProperties;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import com.azure.security.keyvault.certificates.models.Administrator;
import com.azure.security.keyvault.certificates.models.CertificateProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
     * @return sync CertificateClient
     */
    private CertificateClient getCertificateClient() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.instantiation
        CertificateClient certificateClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.security.keyvault.certificates.CertificateClient.instantiation
        return certificateClient;
    }


    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificatePolicy(String)}
     */
    public void getCertiificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string
        CertificatePolicy policy = certificateClient.getCertificatePolicy("certificateName");
        System.out.printf("Received policy with subject name %s", policy.getSubjectName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
        Response<CertificatePolicy> returnedPolicyWithResponse = certificateClient.getCertificatePolicyWithResponse(
            "certificateName", new Context(key1, value1));
        System.out.printf("Received policy with subject name %s",
            returnedPolicyWithResponse.getValue().getSubjectName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateWithPolicy(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithPolicy#String
        Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
        System.out.printf("Recevied certificate with name %s and version %s and secret id",
            certificate.getProperties().getName(),
            certificate.getProperties().getVersion(), certificate.getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithPolicy#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-Context
        Response<Certificate> returnedCertificateWithResponse = certificateClient.getCertificateWithResponse(
            "certificateName", "certificateVersion", new Context(key1, value1));
        System.out.printf("Recevied certificate with name %s and version %s and secret id",
            returnedCertificateWithResponse.getValue().getProperties().getName(),
            returnedCertificateWithResponse.getValue().getProperties().getVersion(),
            returnedCertificateWithResponse.getValue().getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-Context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String-String
        Certificate returnedCertificate = certificateClient.getCertificate("certificateName",
            "certificateVersion");
        System.out.printf("Recevied certificate with name %s and version %s and secret id",
            returnedCertificate.getProperties().getName(), returnedCertificate.getProperties().getVersion(),
            returnedCertificate.getSecretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties
        for (CertificateProperties cert : certificateClient.listCertificates()) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(cert);
            System.out.printf("Received certificate with name %s and secret id %s",
                certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateProperties
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#beginCreateCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map
        CertificatePolicy certificatePolicyPkcsSelf = new CertificatePolicy("Self",
            "CN=SelfSignedJavaPkcs12");
        Poller<CertificateOperation, Certificate> certPoller = certificateClient
            .beginCreateCertificate("certificateName", certificatePolicyPkcsSelf);
        certPoller.blockUntil(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);
        Certificate cert = certPoller.result().block();
        System.out.printf("Certificate created with name %s", cert.getName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy
        CertificatePolicy certificatePolicy = new CertificatePolicy("Self",
            "CN=SelfSignedJavaPkcs12");
        Poller<CertificateOperation, Certificate> certificatePoller = certificateClient
            .beginCreateCertificate("certificateName", certificatePolicy);
        certificatePoller.blockUntil(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);
        Certificate certificate = certificatePoller.result().block();
        System.out.printf("Certificate created with name %s", certificate.getName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createIssuer#String-String
        Issuer createdIssuer = certificateClient.createIssuer("myIssuer", "myProvider");
        System.out.printf("Created Issuer with name %s provider %s", createdIssuer.getName(),
            createdIssuer.getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createIssuer#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createIssuer#issuer
        Issuer issuerToCreate = new Issuer("myissuer", "myProvider")
            .setAccountId("testAccount")
            .setAdministrators(Arrays.asList(new Administrator("test", "name",
                "test@example.com")));
        Issuer returnedIssuer = certificateClient.createIssuer(issuerToCreate);
        System.out.printf("Created Issuer with name %s provider %s", returnedIssuer.getName(),
            returnedIssuer.getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#Issuer-Context
        Issuer issuer = new Issuer("issuerName", "myProvider")
            .setAccountId("testAccount")
            .setAdministrators(Arrays.asList(new Administrator("test", "name",
                "test@example.com")));
        Response<Issuer> issuerResponse = certificateClient.createIssuerWithResponse(issuer,
            new Context(key1, value1));
        System.out.printf("Created Issuer with name %s provider %s", issuerResponse.getValue().getName(),
            issuerResponse.getValue().getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createIssuerWithResponse#Issuer-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string
        Issuer returnedIssuer = certificateClient.getIssuer("issuerName");
        System.out.printf("Retrieved issuer with name %s and prodier %s", returnedIssuer.getName(),
            returnedIssuer.getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context
        Response<Issuer> issuerResponse = certificateClient.getIssuerWithResponse("issuerName",
            new Context(key1, value1));
        System.out.printf("Retrieved issuer with name %s and prodier %s", issuerResponse.getValue().getName(),
            issuerResponse.getValue().getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getIssuer#issuerProperties
        for (IssuerProperties issuer : certificateClient.listIssuers()) {
            Issuer retrievedIssuer = certificateClient.getIssuer(issuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.getName(),
                retrievedIssuer.getProperties().getProvider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getIssuer#issuerProperties

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#issuerProperties-context
        for (IssuerProperties issuer : certificateClient.listIssuers()) {
            Response<Issuer> retrievedIssuerResponse = certificateClient.getIssuerWithResponse(issuer,
                new Context(key1, value1));
            System.out.printf("Received issuer with name %s and provider %s",
                retrievedIssuerResponse.getValue().getName(),
                retrievedIssuerResponse.getValue().getProperties().getProvider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getIssuerWithResponse#issuerProperties-context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties
        Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        // Update certificate enabled status
        certificate.getProperties().setEnabled(false);
        Certificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());
        System.out.printf("Updated Certificate with name %s and enabled status %s",
            updatedCertificate.getProperties().getName(), updatedCertificate.getProperties().isEnabled());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateProperties#CertificateProperties

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context
        Certificate certificateToUpdate = certificateClient.getCertificateWithPolicy("certificateName");
        // Update certificate enabled status
        certificateToUpdate.getProperties().setEnabled(false);
        Response<Certificate> updatedCertificateResponse = certificateClient.
            updateCertificatePropertiesWithResponse(certificateToUpdate.getProperties(), new Context(key1, value1));
        System.out.printf("Updated Certificate with name %s and enabled status %s",
            updatedCertificateResponse.getValue().getProperties().getName(),
            updatedCertificateResponse.getValue().getProperties().isEnabled());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePropertiesWithResponse#CertificateProperties-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#issuer
        Issuer returnedIssuer = certificateClient.getIssuer("issuerName");
        returnedIssuer.setAccountId("newAccountId");
        Issuer updatedIssuer = certificateClient.updateIssuer(returnedIssuer);
        System.out.printf("Updated issuer with name %s, provider %s and account Id %s", updatedIssuer.getName(),
            updatedIssuer.getProperties().getProvider(), updatedIssuer.getAccountId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#Issuer-Context
        Issuer issuer = certificateClient.getIssuer("issuerName");
        returnedIssuer.setAccountId("newAccountId");
        Response<Issuer> updatedIssuerWithResponse = certificateClient.updateIssuerWithResponse(issuer,
            new Context(key1, value1));
        System.out.printf("Updated issuer with name %s, provider %s and account Id %s",
            updatedIssuerWithResponse.getValue().getName(),
            updatedIssuerWithResponse.getValue().getProperties().getProvider(),
            updatedIssuerWithResponse.getValue().getAccountId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateIssuerWithResponse#Issuer-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string
        CertificatePolicy certificatePolicy = certificateClient.getCertificatePolicy("certificateName");
        //Update the certificate policy cert transparency property.
        certificatePolicy.setCertificateTransparency(true);
        CertificatePolicy updatedCertPolicy = certificateClient.updateCertificatePolicy("certificateName",
            certificatePolicy);
        System.out.printf("Updated Certificate Policy transparency status %s",
            updatedCertPolicy.isCertificateTransparency());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
        CertificatePolicy certificatePolicyToUpdate = certificateClient.getCertificatePolicy("certificateName");
        //Update the certificate policy cert transparency property.
        certificatePolicyToUpdate.setCertificateTransparency(true);
        Response<CertificatePolicy> updatedCertPolicyWithResponse = certificateClient
            .updateCertificatePolicyWithResponse("certificateName", certificatePolicyToUpdate,
                new Context(key1, value1));
        System.out.printf("Updated Certificate Policy transparency status %s", updatedCertPolicyWithResponse
            .getValue().isCertificateTransparency());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificate#string
        DeletedCertificate deletedCertificate = certificateClient.deleteCertificate("certificateName");
        System.out.printf("Deleted certitifcate with name %s and recovery id %s", deletedCertificate.getName(),
            deletedCertificate.getRecoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateWithResponse#String-Context
        Response<DeletedCertificate> deletedCertificateResponse = certificateClient
            .deleteCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certitifcate with name %s and recovery id %s",
            deletedCertificateResponse.getValue().getName(), deletedCertificateResponse.getValue().getRecoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context
        Issuer deletedIssuer = certificateClient.deleteIssuer("certificateName");
        System.out.printf("Deleted certificate issuer with name %s and provider id %s", deletedIssuer.getName(),
            deletedIssuer.getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string
        Response<Issuer> deletedIssuerWithResponse = certificateClient.
            deleteIssuerWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certificate issuer with name %s and provider id %s",
            deletedIssuerWithResponse.getValue().getName(),
            deletedIssuerWithResponse.getValue().getProperties().getProvider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string
        DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate("certificateName");
        System.out.printf("Deleted certificate with name %s and recovery id %s", deletedCertificate.getName(),
            deletedCertificate.getRecoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context
        Response<DeletedCertificate> deletedCertificateWithResponse = certificateClient
            .getDeletedCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certificate with name %s and recovery id %s",
            deletedCertificateWithResponse.getValue().getName(),
            deletedCertificateWithResponse.getValue().getRecoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#purgeDeletedCertificateWithResponse(String, Context)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string
        certificateClient.purgeDeletedCertificate("certificateName");
        // END: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificate#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#purgeDeletedCertificateWithResponse(String, Context)}
     */
    public void purgeDeletedCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context
        Response<Void> purgeResponse = certificateClient.purgeDeletedCertificateWithResponse("certificateName",
            new Context(key1, value1));
        System.out.printf("Purged Deleted certificate with status %d %n", purgeResponse.getStatusCode());
        // END: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#recoverDeletedCertificate(String)} (String)}
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificate#string
        Certificate certificate = certificateClient.recoverDeletedCertificate("deletedCertificateName");
        System.out.printf(" Recovered Deleted certificate with name %s and id %s", certificate.getProperties().getName(),
            certificate.getProperties().getId());
        // END: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificateWithResponse#String-Context
        Response<Certificate> recoveredCertificate = certificateClient
            .recoverDeletedCertificateWithResponse("deletedCertificateName", new Context(key1, value1));
        System.out.printf(" Recovered Deleted certificate with name %s and id %s",
            recoveredCertificate.getValue().getProperties().getName(),
            recoveredCertificate.getValue().getProperties().getId());
        // END: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string
        byte[] certificateBackup = certificateClient.backupCertificate("certificateName");
        System.out.printf("Backed up certificate with back up blob length %d", certificateBackup.length);
        // END: com.azure.security.keyvault.certificates.CertificateClient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context
        Response<byte[]> certificateBackupWithResponse = certificateClient
            .backupCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Backed up certificate with back up blob length %d",
            certificateBackupWithResponse.getValue().length);
        // END: com.azure.security.keyvault.certificates.CertificateClient.backupCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#restoreCertificate(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte
        byte[] certificateBackupBlob = {};
        Certificate certificate = certificateClient.restoreCertificate(certificateBackupBlob);
        System.out.printf(" Restored certificate with name %s and id %s",
            certificate.getProperties().getName(), certificate.getProperties().getId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context
        byte[] certificateBackupBlobArray = {};
        Response<Certificate> certificateResponse = certificateClient
            .restoreCertificateWithResponse(certificateBackupBlobArray, new Context(key1, value1));
        System.out.printf(" Restored certificate with name %s and id %s",
            certificateResponse.getValue().getProperties().getName(),
            certificateResponse.getValue().getProperties().getId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificates
        for (CertificateProperties certificate : certificateClient.listCertificates()) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and secret id %s",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificates

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context
        for (CertificateProperties certificate : certificateClient.listCertificates(true,
            new Context(key1, value1))) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and secret id %s",
                certificateWithAllProperties.getProperties().getName(),
                certificateWithAllProperties.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listIssuers
        for (IssuerProperties issuer : certificateClient.listIssuers()) {
            Issuer retrievedIssuer = certificateClient.getIssuer(issuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.getName(),
                retrievedIssuer.getProperties().getProvider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listIssuers

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listIssuers#context
        for (IssuerProperties issuer : certificateClient.listIssuers(new Context(key1, value1))) {
            Issuer retrievedIssuer = certificateClient.getIssuer(issuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.getName(),
                retrievedIssuer.getProperties().getProvider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listIssuers#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates
        for (DeletedCertificate deletedCertificate : certificateClient.listDeletedCertificates()) {
            System.out.printf("Deleted certificate's recovery Id %s", deletedCertificate.getRecoveryId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context
        for (DeletedCertificate deletedCertificate : certificateClient
            .listDeletedCertificates(new Context(key1, value1))) {
            System.out.printf("Deleted certificate's recovery Id %s", deletedCertificate.getRecoveryId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions
        for (CertificateProperties certificate : certificateClient.listCertificateVersions("certificateName")) {
            Certificate certificateWithAllProperites  = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate's version with name %s, version %s and secret id %s",
                certificateWithAllProperites.getProperties().getName(),
                certificateWithAllProperites.getProperties().getVersion(), certificateWithAllProperites.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context
        for (CertificateProperties certificate : certificateClient.listCertificateVersions("certificateName")) {
            Certificate certificateWithAllProperites  = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate's version with name %s, version %s and secret id %s",
                certificateWithAllProperites.getProperties().getName(),
                certificateWithAllProperites.getProperties().getVersion(), certificateWithAllProperites.getSecretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#setContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts
        Contact contactToAdd = new Contact("user", "useremail@exmaple.com");
        for (Contact contact : certificateClient.setContacts(Arrays.asList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.getName(),
                contact.getEmailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context
        Contact sampleContact = new Contact("user", "useremail@exmaple.com");
        for (Contact contact : certificateClient.setContacts(Arrays.asList(sampleContact),
            new Context(key1, value1))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.getName(),
                contact.getEmailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.setContacts#contacts-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listContacts
        for (Contact contact : certificateClient.listContacts()) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.getName(),
                contact.getEmailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listContacts#context
        for (Contact contact : certificateClient.listContacts(new Context(key1, value1))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.getName(),
                contact.getEmailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listContacts#context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts
        for (Contact contact : certificateClient.deleteContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.getName(),
                contact.getEmailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context
        for (Contact contact : certificateClient.deleteContacts(new Context(key1, value1))) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.getName(),
                contact.getEmailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteContacts#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#cancelCertificateOperation(String)} and
     * {@link CertificateClient#cancelCertificateOperationWithResponse(String, Context)} (String)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string
        CertificateOperation certificateOperation = certificateClient
            .cancelCertificateOperation("certificateName");
        System.out.printf("Certificate Operation status %s", certificateOperation.getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string
        Response<CertificateOperation> certificateOperationWithResponse = certificateClient
            .cancelCertificateOperationWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Certificate Operation status %s", certificateOperationWithResponse.getValue().getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string
        CertificateOperation deletedCertificateOperation = certificateClient
            .deleteCertificateOperation("certificateName");
        System.out.printf("Deleted Certificate Operation's last status %s", deletedCertificateOperation.getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string
        Response<CertificateOperation> deletedCertificateOperationWithResponse = certificateClient
            .deleteCertificateOperationWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted Certificate Operation's last status %s",
            deletedCertificateOperationWithResponse.getValue().getStatus());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getPendingCertificateSigningRequest(String)}
     * and {@link CertificateAsyncClient#getPendingCertificateSigningRequestWithResponse(String)}
     */
    public void getPendingCertificateSigningRequestCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getPendingCertificateSigningRequest#String
        byte[] signingRequest = certificateClient.getPendingCertificateSigningRequest("certificateName");
        System.out.printf("Received Signing request blob of length %s", signingRequest.length);
        // END: com.azure.security.keyvault.certificates.CertificateClient.getPendingCertificateSigningRequest#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getPendingCertificateSigningRequestWithResponse#String-Context
        Response<byte[]> signingRequestResponse =
            certificateClient.getPendingCertificateSigningRequestWithResponse("certificateName",
                new Context(key2, value2));
        System.out.printf("Received Signing request blob of length %s", signingRequestResponse.getValue().length);
        // END: com.azure.security.keyvault.certificates.CertificateClient.getPendingCertificateSigningRequestWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#mergeCertificate(MergeCertificateOptions)}
     * and {@link CertificateAsyncClient#mergeCertificate(String, List)}
     */
    public void mergeCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#String-List
        List<byte[]> x509Certs = new ArrayList<>();
        Certificate certificate = certificateClient.mergeCertificate("certificateName", x509Certs);
        System.out.printf("Received Certificate with name %s and key id %s",
            certificate.getProperties().getName(), certificate.getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#String-List

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#String-List-Context
        List<byte[]> x509Certificates = new ArrayList<>();
        Response<Certificate> certificateResponse =
            certificateClient.mergeCertificateWithResponse("certificateName", x509Certs,
                new Context(key1, value1));
        System.out.printf("Received Certificate with name %s and key id %s",
            certificateResponse.getValue().getProperties().getName(), certificateResponse.getValue().getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#String-List-Context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config
        List<byte[]> x509CertificatesToMerge = new ArrayList<>();
        MergeCertificateOptions config =
            new MergeCertificateOptions("certificateName", x509CertificatesToMerge)
                .setEnabled(false);
        Certificate mergedCertificate = certificateClient.mergeCertificate(config);
        System.out.printf("Received Certificate with name %s and key id %s",
            mergedCertificate.getProperties().getName(), mergedCertificate.getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
        List<byte[]> x509CertsToMerge = new ArrayList<>();
        MergeCertificateOptions mergeConfig =
            new MergeCertificateOptions("certificateName", x509CertsToMerge)
                .setEnabled(false);
        Response<Certificate> mergedCertificateWithResponse =
            certificateClient.mergeCertificateWithResponse(mergeConfig, new Context(key2, value2));
        System.out.printf("Received Certificate with name %s and key id %s",
            mergedCertificateWithResponse.getValue().getProperties().getName(),
            mergedCertificateWithResponse.getValue().getKeyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
    }
}
