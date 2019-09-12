// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.azure.security.keyvault.certificates.models.IssuerBase;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import com.azure.security.keyvault.certificates.models.Administrator;

import java.time.Duration;
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
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
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
        System.out.printf("Received policy with subject name %s", policy.subjectName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
        Response<CertificatePolicy> returnedPolicyWithResponse = certificateClient.getCertificatePolicyWithResponse(
            "certificateName", new Context(key1, value1));
        System.out.printf("Received policy with subject name %s", returnedPolicyWithResponse.getValue().subjectName());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateWithPolicy(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithPolicy#String
        Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
        System.out.printf("Recevied certificate with name %s and version %s and secret id", certificate.name(),
            certificate.version(), certificate.secretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithPolicy#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-Context
        Response<Certificate> returnedCertificateWithResponse = certificateClient.getCertificateWithResponse(
            "certificateName", "certificateVersion", new Context(key1, value1));
        System.out.printf("Recevied certificate with name %s and version %s and secret id",
            returnedCertificateWithResponse.getValue().name(), returnedCertificateWithResponse.getValue().version(),
            returnedCertificateWithResponse.getValue().secretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateWithResponse#String-String-Context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String-String
        Certificate returnedCertificate = certificateClient.getCertificate("certificateName",
            "certificateVersion");
        System.out.printf("Recevied certificate with name %s and version %s and secret id", returnedCertificate.name(),
            returnedCertificate.version(), returnedCertificate.secretId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateBase
        for (CertificateBase cert : certificateClient.listCertificates()) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(cert);
            System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties.name(),
                certificateWithAllProperties.secretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map-Duration
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        try {
            CertificateOperation certificateOperation = certificateClient.createCertificate("certificateName",
                policy, tags, Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s", certificateOperation.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map-Duration

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map
        CertificatePolicy certPolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> metadataTags = new HashMap<>();
        metadataTags.put("foo", "bar");
        CertificateOperation certificateOperation = certificateClient.createCertificate("certificateName",
            certPolicy, metadataTags);
        System.out.printf("Certificate operation status %s", certificateOperation.status());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Map

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Duration
        try {
            CertificatePolicy certificatePolicy = new CertificatePolicy("Self",
                "CN=SelfSignedJavaPkcs12");
            CertificateOperation certificateOp = certificateClient.createCertificate("certificateName",
                certificatePolicy, Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s", certificateOp.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy-Duration

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy
        CertificatePolicy certificatePolicy = new CertificatePolicy("Self",
            "CN=SelfSignedJavaPkcs12");
        CertificateOperation certificateOp = certificateClient.createCertificate("certificateName",
            certificatePolicy);
        System.out.printf("Certificate operation status %s", certificateOp.status());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificate#String-CertificatePolicy
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#createCertificateIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificateIssuer#String-String
        Issuer createdIssuer = certificateClient.createCertificateIssuer("myIssuer", "myProvider");
        System.out.printf("Created Issuer with name %s provider %s", createdIssuer.name(), createdIssuer.provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificateIssuer#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificateIssuer#issuer
        Issuer issuerToCreate = new Issuer("myissuer", "myProvider")
            .accountId("testAccount")
            .administrators(Arrays.asList(new Administrator("test", "name",
                "test@example.com")));
        Issuer returnedIssuer = certificateClient.createCertificateIssuer(issuerToCreate);
        System.out.printf("Created Issuer with name %s provider %s", returnedIssuer.name(), returnedIssuer.provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.createCertificateIssuerWithResponse#Issuer-Context
        Issuer issuer = new Issuer("issuerName", "myProvider")
            .accountId("testAccount")
            .administrators(Arrays.asList(new Administrator("test", "name",
                "test@example.com")));
        Response<Issuer> issuerResponse = certificateClient.createCertificateIssuerWithResponse(issuer,
            new Context(key1, value1));
        System.out.printf("Created Issuer with name %s provider %s", issuerResponse.getValue().name(),
            issuerResponse.getValue().provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.createCertificateIssuerWithResponse#Issuer-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getCertificateIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuer#string
        Issuer returnedIssuer = certificateClient.getCertificateIssuer("issuerName");
        System.out.printf("Retrieved issuer with name %s and prodier %s", returnedIssuer.name(),
            returnedIssuer.provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuerWithResponse#string-context
        Response<Issuer> issuerResponse = certificateClient.getCertificateIssuerWithResponse("issuerName",
            new Context(key1, value1));
        System.out.printf("Retrieved issuer with name %s and prodier %s", issuerResponse.getValue().name(),
            issuerResponse.getValue().provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuer#issuerBase
        for (IssuerBase issuer : certificateClient.listCertificateIssuers()) {
            Issuer retrievedIssuer = certificateClient.getCertificateIssuer(issuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.name(),
                retrievedIssuer.provider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuer#issuerBase

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuerWithResponse#issuerBase-context
        for (IssuerBase issuer : certificateClient.listCertificateIssuers()) {
            Response<Issuer> retrievedIssuerResponse = certificateClient.getCertificateIssuerWithResponse(issuer,
                new Context(key1, value1));
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuerResponse.getValue().name(),
                retrievedIssuerResponse.getValue().provider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.getCertificateIssuerWithResponse#issuerBase-context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificate(CertificateBase)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificate#CertificateBase
        Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        // Update certificate enabled status
        certificate.enabled(false);
        Certificate updatedCertificate = certificateClient.updateCertificate(certificate);
        System.out.printf("Updated Certificate with name %s and enabled status %s", updatedCertificate.name(),
            updatedCertificate.enabled());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificate#CertificateBase

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateWithResponse#CertificateBase-Context
        Certificate certificateToUpdate = certificateClient.getCertificateWithPolicy("certificateName");
        // Update certificate enabled status
        certificateToUpdate.enabled(false);
        Response<Certificate> updatedCertificateResponse = certificateClient.
            updateCertificateWithResponse(certificateToUpdate, new Context(key1, value1));
        System.out.printf("Updated Certificate with name %s and enabled status %s",
            updatedCertificateResponse.getValue().name(), updatedCertificateResponse.getValue().enabled());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateWithResponse#CertificateBase-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateIssuer#issuer
        Issuer returnedIssuer = certificateClient.getCertificateIssuer("issuerName");
        returnedIssuer.accountId("newAccountId");
        Issuer updatedIssuer = certificateClient.updateCertificateIssuer(returnedIssuer);
        System.out.printf("Updated issuer with name %s, provider %s and account Id %s", updatedIssuer.name(),
            updatedIssuer.provider(), updatedIssuer.accountId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateIssuerWithResponse#Issuer-Context
        Issuer issuer = certificateClient.getCertificateIssuer("issuerName");
        returnedIssuer.accountId("newAccountId");
        Response<Issuer> updatedIssuerWithResponse = certificateClient.updateCertificateIssuerWithResponse(issuer,
            new Context(key1, value1));
        System.out.printf("Updated issuer with name %s, provider %s and account Id %s",
            updatedIssuerWithResponse.getValue().name(), updatedIssuerWithResponse.getValue().provider(),
            updatedIssuerWithResponse.getValue().accountId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificateIssuerWithResponse#Issuer-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string
        CertificatePolicy certificatePolicy = certificateClient.getCertificatePolicy("certificateName");
        //Update the certificate policy cert transparency property.
        certificatePolicy.certificateTransparency(true);
        CertificatePolicy updatedCertPolicy = certificateClient.updateCertificatePolicy("certificateName",
            certificatePolicy);
        System.out.printf("Updated Certificate Policy transparency status %s",
            updatedCertPolicy.certificateTransparency());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
        CertificatePolicy certificatePolicyToUpdate = certificateClient.getCertificatePolicy("certificateName");
        //Update the certificate policy cert transparency property.
        certificatePolicyToUpdate.certificateTransparency(true);
        Response<CertificatePolicy> updatedCertPolicyWithResponse = certificateClient
            .updateCertificatePolicyWithResponse("certificateName", certificatePolicyToUpdate,
                new Context(key1, value1));
        System.out.printf("Updated Certificate Policy transparency status %s", updatedCertPolicyWithResponse
            .getValue().certificateTransparency());
        // END: com.azure.security.keyvault.certificates.CertificateClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificate#string
        DeletedCertificate deletedCertificate = certificateClient.deleteCertificate("certificateName");
        System.out.printf("Deleted certitifcate with name %s and recovery id %s", deletedCertificate.name(),
            deletedCertificate.recoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateWithResponse#String-Context
        Response<DeletedCertificate> deletedCertificateResponse = certificateClient
            .deleteCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certitifcate with name %s and recovery id %s",
            deletedCertificateResponse.getValue().name(), deletedCertificateResponse.getValue().recoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#deleteCertificateIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateIssuerWithResponse#string-context
        Issuer deletedIssuer = certificateClient.deleteCertificateIssuer("certificateName");
        System.out.printf("Deleted certificate issuer with name %s and provider id %s", deletedIssuer.name(),
            deletedIssuer.provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateIssuerWithResponse#string-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateIssuer#string
        Response<Issuer> deletedIssuerWithResponse = certificateClient.
            deleteCertificateIssuerWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certificate issuer with name %s and provider id %s",
            deletedIssuerWithResponse.getValue().name(), deletedIssuerWithResponse.getValue().provider());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string
        DeletedCertificate deletedCertificate = certificateClient.getDeletedCertificate("certificateName");
        System.out.printf("Deleted certificate with name %s and recovery id %s", deletedCertificate.name(),
            deletedCertificate.recoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context
        Response<DeletedCertificate> deletedCertificateWithResponse = certificateClient
            .getDeletedCertificateWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted certificate with name %s and recovery id %s",
            deletedCertificateWithResponse.getValue().name(), deletedCertificateWithResponse.getValue().recoveryId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.getDeletedCertificateWithResponse#String-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#purgeDeletedCertificateWithResponse(String, Context)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string
        certificateClient.purgeDeletedCertificateWithResponse("certificateName");
        // END: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context
        VoidResponse purgeResponse = certificateClient.purgeDeletedCertificateWithResponse("certificateName",
            new Context(key1, value1));
        System.out.printf("Purged Deleted certificate with status %s", purgeResponse.getStatusCode());
        // END: com.azure.security.keyvault.certificates.CertificateClient.purgeDeletedCertificateWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#recoverDeletedCertificate(String)} (String)}
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificate#string
        Certificate certificate = certificateClient.recoverDeletedCertificate("deletedCertificateName");
        System.out.printf(" Recovered Deleted certificate with name %s and id %s", certificate.name(),
            certificate.id());
        // END: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.CertificateClient.recoverDeletedCertificateWithResponse#String-Context
        Response<Certificate> recoveredCertificate = certificateClient
            .recoverDeletedCertificateWithResponse("deletedCertificateName", new Context(key1, value1));
        System.out.printf(" Recovered Deleted certificate with name %s and id %s",
            recoveredCertificate.getValue().name(), recoveredCertificate.getValue().id());
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
        System.out.printf(" Restored certificate with name %s and id %s", certificate.name(), certificate.id());
        // END: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context
        byte[] certificateBackupBlobArray = {};
        Response<Certificate> certificateResponse = certificateClient
            .restoreCertificateWithResponse(certificateBackupBlobArray, new Context(key1, value1));
        System.out.printf(" Restored certificate with name %s and id %s", certificateResponse.getValue().name(),
            certificateResponse.getValue().id());
        // END: com.azure.security.keyvault.certificates.CertificateClient.restoreCertificateWithResponse#byte-Context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificates
        for (CertificateBase certificate : certificateClient.listCertificates()) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties.name(),
                certificateWithAllProperties.secretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificates

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context
        for (CertificateBase certificate : certificateClient.listCertificates(true,
            new Context(key1, value1))) {
            Certificate certificateWithAllProperties = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties.name(),
                certificateWithAllProperties.secretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateIssuers
        for (IssuerBase issuer : certificateClient.listCertificateIssuers()) {
            Issuer retrievedIssuer = certificateClient.getCertificateIssuer(issuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.name(),
                retrievedIssuer.provider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateIssuers

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateIssuers#context
        for (IssuerBase issuer : certificateClient.listCertificateIssuers(new Context(key1, value1))) {
            Issuer retrievedIssuer = certificateClient.getCertificateIssuer(issuer);
            System.out.printf("Received issuer with name %s and provider %s", retrievedIssuer.name(),
                retrievedIssuer.provider());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateIssuers#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates
        for (DeletedCertificate deletedCertificate : certificateClient.listDeletedCertificates()) {
            System.out.printf("Deleted certificate's recovery Id %s", deletedCertificate.recoveryId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context
        for (DeletedCertificate deletedCertificate : certificateClient
            .listDeletedCertificates(new Context(key1, value1))) {
            System.out.printf("Deleted certificate's recovery Id %s", deletedCertificate.recoveryId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listDeletedCertificates#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions
        for (CertificateBase certificate : certificateClient.listCertificateVersions("certificateName")) {
            Certificate certificateWithAllProperites  = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate's version with name %s, version %s and secret id %s",
                certificateWithAllProperites.name(),
                certificateWithAllProperites.version(), certificateWithAllProperites.secretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context
        for (CertificateBase certificate : certificateClient.listCertificateVersions("certificateName")) {
            Certificate certificateWithAllProperites  = certificateClient.getCertificate(certificate);
            System.out.printf("Received certificate's version with name %s, version %s and secret id %s",
                certificateWithAllProperites.name(),
                certificateWithAllProperites.version(), certificateWithAllProperites.secretId());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateVersions#context
    }

    /**
     * Method to insert code snippets for {@link CertificateClient#setCertificateContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateClient certificateClient = getCertificateClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.setCertificateContacts#contacts
        Contact contactToAdd = new Contact("user", "useremail@exmaple.com");
        for (Contact contact : certificateClient.setCertificateContacts(Arrays.asList(contactToAdd))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.name(),
                contact.emailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.setCertificateContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.setCertificateContacts#contacts-context
        Contact sampleContact = new Contact("user", "useremail@exmaple.com");
        for (Contact contact : certificateClient.setCertificateContacts(Arrays.asList(sampleContact),
            new Context(key1, value1))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.name(),
                contact.emailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.setCertificateContacts#contacts-context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateContacts
        for (Contact contact : certificateClient.listCertificateContacts()) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.name(),
                contact.emailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.listCertificateContacts#context
        for (Contact contact : certificateClient.listCertificateContacts(new Context(key1, value1))) {
            System.out.printf("Added contact with name %s and email %s to key vault", contact.name(),
                contact.emailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.listCertificateContacts#context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateContacts
        for (Contact contact : certificateClient.deleteCertificateContacts()) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.name(),
                contact.emailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateContacts#context
        for (Contact contact : certificateClient.deleteCertificateContacts(new Context(key1, value1))) {
            System.out.printf("Deleted contact with name %s and email %s from key vault", contact.name(),
                contact.emailAddress());
        }
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateContacts#context
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
        System.out.printf("Certificate Operation status %s", certificateOperation.status());
        // END: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string
        Response<CertificateOperation> certificateOperationWithResponse = certificateClient
            .cancelCertificateOperationWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Certificate Operation status %s", certificateOperationWithResponse.getValue().status());
        // END: com.azure.security.keyvault.certificates.CertificateClient.cancelCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string
        CertificateOperation deletedCertificateOperation = certificateClient
            .deleteCertificateOperation("certificateName");
        System.out.printf("Deleted Certificate Operation's last status %s", deletedCertificateOperation.status());
        // END: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.deleteCertificateOperation#string
        Response<CertificateOperation> deletedCertificateOperationWithResponse = certificateClient
            .deleteCertificateOperationWithResponse("certificateName", new Context(key1, value1));
        System.out.printf("Deleted Certificate Operation's last status %s",
            deletedCertificateOperationWithResponse.getValue().status());
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
        System.out.printf("Received Certificate with name %s and key id %s", certificate.name(), certificate.keyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#String-List

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#String-List-Context
        List<byte[]> x509Certificates = new ArrayList<>();
        Response<Certificate> certificateResponse =
            certificateClient.mergeCertificateWithResponse("certificateName", x509Certs,
                new Context(key1, value1));
        System.out.printf("Received Certificate with name %s and key id %s", certificateResponse.getValue().name(),
            certificateResponse.getValue().keyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#String-List-Context

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config
        List<byte[]> x509CertificatesToMerge = new ArrayList<>();
        MergeCertificateOptions config =
            new MergeCertificateOptions("certificateName", x509CertificatesToMerge)
                .enabled(false);
        Certificate mergedCertificate = certificateClient.mergeCertificate(config);
        System.out.printf("Received Certificate with name %s and key id %s", mergedCertificate.name(),
            mergedCertificate.keyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificate#config

        // BEGIN: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
        List<byte[]> x509CertsToMerge = new ArrayList<>();
        MergeCertificateOptions mergeConfig =
            new MergeCertificateOptions("certificateName", x509CertsToMerge)
                .enabled(false);
        Response<Certificate> mergedCertificateWithResponse =
            certificateClient.mergeCertificateWithResponse(mergeConfig, new Context(key2, value2));
        System.out.printf("Received Certificate with name %s and key id %s",
            mergedCertificateWithResponse.getValue().name(), mergedCertificateWithResponse.getValue().keyId());
        // END: com.azure.security.keyvault.certificates.CertificateClient.mergeCertificateWithResponse#config
    }
}
