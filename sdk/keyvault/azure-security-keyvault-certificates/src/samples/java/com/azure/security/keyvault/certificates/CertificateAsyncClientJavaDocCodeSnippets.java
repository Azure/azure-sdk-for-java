// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificateContact;
import com.azure.security.keyvault.certificates.models.CertificateIssuer;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificate;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class contains code samples for generating javadocs through doclets for {@link CertificateAsyncClient}
 */
public final class CertificateAsyncClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link CertificateAsyncClient}
     * @return An instance of {@link CertificateAsyncClient}
     */
    public CertificateAsyncClient createAsyncClientWithHttpclient() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.withhttpclient.instantiation
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.withhttpclient.instantiation
        return certificateAsyncClient;
    }

    /**
     * Implementation for async CertificateAsyncClient
     * @return sync CertificateAsyncClient
     */
    private CertificateAsyncClient getCertificateAsyncClient() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .vaultUrl("https://myvault.vault.azure.net/")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation
        return certificateAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link CertificateAsyncClient}
     * @return An instance of {@link CertificateAsyncClient}
     */
    public CertificateAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new BearerTokenAuthenticationPolicy(new DefaultAzureCredentialBuilder().build()), new RetryPolicy())
            .build();
        CertificateAsyncClient certificateAsyncClient = new CertificateClientBuilder()
            .pipeline(pipeline)
            .vaultUrl("https://myvault.azure.net/")
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.pipeline.instantiation
        return certificateAsyncClient;
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificatePolicy(String)}
     */
    public void getCertificatePolicyCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policy ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n",
                    policy.getIssuerName(), policy.getSubject()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicyWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policyResponse ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n",
                    policyResponse.getValue().getIssuerName(), policyResponse.getValue().getSubject()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificate(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String
        certificateAsyncClient.getCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateResponse.getProperties().getName(), certificateResponse.getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#String
        certificateAsyncClient.getCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateResponse.getValue().getProperties().getName(),
                    certificateResponse.getValue().getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersionWithResponse#string-string
        String certificateVersion = "6A385B124DEF4096AF1361A85B16C204";
        certificateAsyncClient.getCertificateVersionWithResponse("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateWithVersion.getValue().getProperties().getName(),
                    certificateWithVersion.getValue().getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersionWithResponse#string-string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersion#String-String
        certificateAsyncClient.getCertificateVersion("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateWithVersion.getProperties().getName(), certificateWithVersion.getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateVersion#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateProperties
        certificateAsyncClient.getCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificate -> certificateAsyncClient.getCertificateVersion(certificate.getName(),
                certificate.getProperties().getVersion())
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateResponse.getProperties().getName(), certificateResponse.getSecretId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateProperties
    }

    /**
     * Method to insert code snippets for
     * {@link CertificateAsyncClient#beginCreateCertificate(String, CertificatePolicy, Boolean, Map)} and
     * {@link CertificateAsyncClient#beginCreateCertificate(String, CertificatePolicy)}.
     */
    public void createCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        certificateAsyncClient.beginCreateCertificate("certificateName", policy, true, tags)
            .subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy-Boolean-Map

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy
        CertificatePolicy certPolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        certificateAsyncClient.beginCreateCertificate("certificateName", certPolicy)
            .subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.beginCreateCertificate#String-CertificatePolicy
    }

    /**
     * Method to insert code snippets for
     * {@link CertificateAsyncClient#getCertificateOperation(String)}.
     */
    public void getCertificateOperation() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateOperation#String
        certificateAsyncClient.getCertificateOperation("certificateName")
            .subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateOperation#String
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#createIssuer(CertificateIssuer)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#CertificateIssuer
        CertificateIssuer issuer = new CertificateIssuer("issuerName", "providerName")
            .setAccountId("keyvaultuser")
            .setPassword("temp2");
        certificateAsyncClient.createIssuer(issuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.getName(),
                    issuerResponse.getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#CertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#CertificateIssuer
        CertificateIssuer newIssuer = new CertificateIssuer("issuerName", "providerName")
            .setAccountId("keyvaultuser")
            .setPassword("temp2");
        certificateAsyncClient.createIssuerWithResponse(newIssuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.getValue().getName(),
                    issuerResponse.getValue().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#CertificateIssuer
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#string
        certificateAsyncClient.getIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> {
                System.out.printf("Issuer returned with %s and %s", issuer.getName(),
                    issuer.getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string
        certificateAsyncClient.getIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.getValue().getName(),
                    issuerResponse.getValue().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateProperties#CertificateProperties
        certificateAsyncClient.getCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                KeyVaultCertificate certificate = certificateResponseValue;
                //Update enabled status of the certificate
                certificate.getProperties().setEnabled(false);
                certificateAsyncClient.updateCertificateProperties(certificate.getProperties())
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s %n",
                            certificateResponse.getProperties().isEnabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateProperties#CertificateProperties
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateIssuer(CertificateIssuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer
        certificateAsyncClient.getIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                CertificateIssuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.setEnabled(false);
                certificateAsyncClient.updateIssuer(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s %n",
                            issuerResponse.isEnabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#CertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuerWithResponse#CertificateIssuer
        certificateAsyncClient.getIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                CertificateIssuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.setEnabled(false);
                certificateAsyncClient.updateIssuerWithResponse(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s %n",
                            issuerResponse.getValue().isEnabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuerWithResponse#CertificateIssuer
    }

    /**
     * Method to insert code snippets for
     * {@link CertificateAsyncClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificatePolicyResponseValue -> {
                CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
                // Update transparency
                certificatePolicy.setCertificateTransparent(true);
                certificateAsyncClient.updateCertificatePolicy("certificateName", certificatePolicy)
                    .subscribe(updatedPolicy ->
                        System.out.printf("Certificate policy's updated transparency status %s %n",
                            updatedPolicy.isCertificateTransparent()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificatePolicyResponseValue -> {
                CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
                // Update transparency
                certificatePolicy.setCertificateTransparent(true);
                certificateAsyncClient.updateCertificatePolicyWithResponse("certificateName",
                    certificatePolicy)
                    .subscribe(updatedPolicyResponse ->
                        System.out.printf("Certificate policy's updated transparency status %s %n",
                            updatedPolicyResponse.getValue().isCertificateTransparent()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePropertiesWithResponse#CertificateProperties
        certificateAsyncClient.getCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                KeyVaultCertificate certificate = certificateResponseValue;
                //Update the enabled status of the certificate.
                certificate.getProperties().setEnabled(false);
                certificateAsyncClient.updateCertificatePropertiesWithResponse(certificate.getProperties())
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s %n",
                            certificateResponse.getValue().getProperties().isEnabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePropertiesWithResponse#CertificateProperties
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#beginDeleteCertificate(String)}.
     */
    public void deleteCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String
        certificateAsyncClient.beginDeleteCertificate("certificateName")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Certificate Name: " + pollResponse.getValue().getName());
                System.out.println("Certificate Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.beginDeleteCertificate#String
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuerWithResponse#string
        certificateAsyncClient.deleteIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s %n", deletedIssuerResponse.getValue().getName()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuerWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuer#string
        certificateAsyncClient.deleteIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s %n", deletedIssuerResponse.getName()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificate#string
        certificateAsyncClient.getDeletedCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s %n", deletedSecretResponse.getRecoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string
        certificateAsyncClient.getDeletedCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s %n",
                    deletedSecretResponse.getValue().getRecoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#purgeDeletedCertificate(String)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificate#string
        certificateAsyncClient.purgeDeletedCertificate("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .doOnSuccess(response -> System.out.println("Successfully Purged certificate"));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificate#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#purgeDeletedCertificateWithResponse(String)}
     */
    public void purgeDeletedCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string
        certificateAsyncClient.purgeDeletedCertificateWithResponse("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#beginRecoverDeletedCertificate(String)}.
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.certificatevault.certificates.CertificateAsyncClient.beginRecoverDeletedCertificate#String
        certificateAsyncClient.beginRecoverDeletedCertificate("deletedCertificateName")
            .subscribe(pollResponse -> {
                System.out.println("Recovery Status: " + pollResponse.getStatus().toString());
                System.out.println("Recover Certificate Name: " + pollResponse.getValue().getName());
                System.out.println("Recover Certificate Id: " + pollResponse.getValue().getId());
            });
        // END: com.azure.security.certificatevault.certificates.CertificateAsyncClient.beginRecoverDeletedCertificate#String
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificate#string
        certificateAsyncClient.backupCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBackupResponse ->
                System.out.printf("Certificate's Backup Byte array's length %s %n", certificateBackupResponse.length));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string
        certificateAsyncClient.backupCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBackupResponse ->
                System.out.printf("Certificate's Backup Byte array's length %s %n",
                    certificateBackupResponse.getValue().length));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#restoreCertificateBackup(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte
        byte[] certificateBackupByteArray = {};
        certificateAsyncClient.restoreCertificateBackup(certificateBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s %n",
                certificateResponse.getProperties().getName(), certificateResponse.getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte
        byte[] certificateBackup = {};
        certificateAsyncClient.restoreCertificateBackup(certificateBackup)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s %n",
                certificateResponse.getProperties().getName(), certificateResponse.getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listPropertiesOfCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates
        certificateAsyncClient.listPropertiesOfCertificates()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificate -> certificateAsyncClient.getCertificateVersion(certificate.getName(),
                certificate.getVersion())
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
                    certificateResponse.getName(), certificateResponse.getKeyId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listPropertiesOfIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listPropertiesOfIssuers
        certificateAsyncClient.listPropertiesOfIssuers()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerProperties -> certificateAsyncClient.getIssuer(issuerProperties.getName())
                .subscribe(issuerResponse -> System.out.printf("Received issuer with name %s and provider %s",
                    issuerResponse.getName(), issuerResponse.getProvider())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listPropertiesOfIssuers
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates
        certificateAsyncClient.listDeletedCertificates()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedCertificateResponse ->  System.out.printf("Deleted Certificate's Recovery Id %s %n",
                deletedCertificateResponse.getRecoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listPropertiesOfCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions
        certificateAsyncClient.listPropertiesOfCertificateVersions("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificate -> certificateAsyncClient.getCertificateVersion(certificate.getName(),
                certificate.getVersion())
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
                    certificateResponse.getProperties().getName(), certificateResponse.getKeyId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#setContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts
        CertificateContact contactToAdd = new CertificateContact().setName("user").setEmail("useremail@example.com");
        certificateAsyncClient.setContacts(Collections.singletonList(contactToAdd)).subscribe(contact ->
            System.out.printf("Contact name %s and email %s", contact.getName(), contact.getEmail())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts
        certificateAsyncClient.listContacts().subscribe(contact ->
            System.out.printf("Contact name %s and email %s", contact.getName(), contact.getEmail())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts
        certificateAsyncClient.deleteContacts().subscribe(contact ->
            System.out.printf("Deleted Contact name %s and email %s", contact.getName(), contact.getEmail())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteCertificateOperation(String)} and
     * {@link CertificateAsyncClient#deleteCertificateOperationWithResponse(String, com.azure.core.util.Context)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string
        certificateAsyncClient.deleteCertificateOperationWithResponse("certificateName")
            .subscribe(certificateOperationResponse -> System.out.printf("Deleted Certificate operation's last"
                + " status %s", certificateOperationResponse.getValue().getStatus()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string
        certificateAsyncClient.deleteCertificateOperation("certificateName")
            .subscribe(certificateOperation -> System.out.printf("Deleted Certificate operation last status %s",
                certificateOperation.getStatus()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string
        certificateAsyncClient.cancelCertificateOperation("certificateName")
            .subscribe(certificateOperation -> System.out.printf("Certificate operation status %s",
                certificateOperation.getStatus()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string
        certificateAsyncClient.cancelCertificateOperationWithResponse("certificateName")
            .subscribe(certificateOperationResponse -> System.out.printf("Certificate operation status %s",
                certificateOperationResponse.getValue().getStatus()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#mergeCertificate(MergeCertificateOptions)}
     */
    public void mergeCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config
        List<byte[]> x509CertificatesToMerge = new ArrayList<>();
        MergeCertificateOptions config =
            new MergeCertificateOptions("certificateName", x509CertificatesToMerge).setEnabled(false);
        certificateAsyncClient.mergeCertificate(config)
            .subscribe(certificate -> System.out.printf("Received Certificate with name %s and key id %s",
                certificate.getProperties().getName(), certificate.getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config
        List<byte[]> x509CertsToMerge = new ArrayList<>();
        MergeCertificateOptions mergeConfig =
            new MergeCertificateOptions("certificateName", x509CertsToMerge).setEnabled(false);
        certificateAsyncClient.mergeCertificateWithResponse(mergeConfig)
            .subscribe(certificateResponse -> System.out.printf("Received Certificate with name %s and key id %s",
                certificateResponse.getValue().getProperties().getName(), certificateResponse.getValue().getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config
    }


    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#importCertificate(ImportCertificateOptions)}
     */
    public void importCertificate() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificate#options
        byte[] certificateToImport = new byte[100];
        ImportCertificateOptions config =
            new ImportCertificateOptions("certificateName", certificateToImport).setEnabled(false);
        certificateAsyncClient.importCertificate(config)
            .subscribe(certificate -> System.out.printf("Received Certificate with name %s and key id %s",
                certificate.getProperties().getName(), certificate.getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificate#options

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificateWithResponse#options
        byte[] certToImport = new byte[100];
        ImportCertificateOptions importCertificateOptions  =
            new ImportCertificateOptions("certificateName", certToImport).setEnabled(false);
        certificateAsyncClient.importCertificateWithResponse(importCertificateOptions)
            .subscribe(certificateResponse -> System.out.printf("Received Certificate with name %s and key id %s",
                certificateResponse.getValue().getProperties().getName(), certificateResponse.getValue().getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.importCertificateWithResponse#options
    }
}
