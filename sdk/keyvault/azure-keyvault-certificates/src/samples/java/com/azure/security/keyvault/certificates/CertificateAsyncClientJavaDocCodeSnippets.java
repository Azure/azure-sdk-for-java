// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.Issuer;
import reactor.util.context.Context;

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
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.withhttpclient.instantiation
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).build();
        CertificateAsyncClient keyClient = new CertificateClientBuilder()
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(new RecordNetworkCallPolicy(networkData))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.async.certificateclient.withhttpclient.instantiation
        return keyClient;
    }

    /**
     * Implementation for async CertificateAsyncClient
     * @return sync CertificateAsyncClient
     */
    private CertificateAsyncClient getCertificateAsyncClient() {
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.instantiation
        CertificateAsyncClient secretAsyncClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.async.certificateclient.instantiation
        return secretAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link CertificateAsyncClient}
     * @return An instance of {@link CertificateAsyncClient}
     */
    public CertificateAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.pipeline.instantiation
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();
        CertificateAsyncClient secretAsyncClient = new CertificateClientBuilder()
            .pipeline(pipeline)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.async.certificateclient.pipeline.instantiation
        return secretAsyncClient;
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificatePolicy(String)}
     */
    public void getCertiificatePolicyCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificatePolicy#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policy ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n", policy.issuerName(),
                    policy.subjectName()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicyWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policyResponse ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n", policyResponse.value().issuerName(),
                    policyResponse.value().subjectName()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificateWithPolicy(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithPolicy
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
           .subscriberContext(Context.of(key1, value1, key2, value2))
           .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.name(),
                    certificateResponse.secretId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithPolicy

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithResponse#string-string
        String certificateVersion = "6A385B124DEF4096AF1361A85B16C204";
        certificateAsyncClient.getCertificateWithResponse("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s \n",
                    certificateWithVersion.value().name(), certificateWithVersion.value().secretId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateWithResponse#string-string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificate
        certificateAsyncClient.getCertificate("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s \n",
                    certificateWithVersion.name(), certificateWithVersion.secretId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificate

        //BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificate#CertificateBase
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.name(),
                    certificateResponse.secretId())));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#createCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.createCertificate#tags
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        certificateAsyncClient.createCertificate("certificateName", policy, tags)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().status());
                System.out.println(pollResponse.getValue().statusDetails());
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.createCertificate#tags

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.createCertificate
        CertificatePolicy certPolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        tags.put("foo", "bar");
        certificateAsyncClient.createCertificate("certificateName", certPolicy, tags)
            .getObserver().subscribe(pollResponse -> {
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(pollResponse.getStatus());
            System.out.println(pollResponse.getValue().status());
            System.out.println(pollResponse.getValue().statusDetails());
        });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.createCertificate
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#createCertificateIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.createCertificateIssuer
        certificateAsyncClient.createCertificateIssuer("issuerName", "providerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> {
                System.out.printf("Issuer created with %s and %s", issuer.name(), issuer.provider());
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.createCertificateIssuer

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.createCertificateIssuer#issuer
        Issuer issuer = new Issuer("issuerName", "providerName")
            .accountId("keyvaultuser")
            .password("temp2");
        certificateAsyncClient.createCertificateIssuer(issuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.name(), issuerResponse.provider());
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.createCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.createCertificateIssuerWithResponse#issuer
        Issuer newIssuer = new Issuer("issuerName", "providerName")
            .accountId("keyvaultuser")
            .password("temp2");
        certificateAsyncClient.createCertificateIssuerWithResponse(newIssuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.value().name(), issuerResponse.value().provider());
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.createCertificateIssuerWithResponse#issuer
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificateIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuer#string
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> {
                System.out.printf("Issuer returned with %s and %s", issuer.name(), issuer.provider());
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuerWithResponse#string
        certificateAsyncClient.getCertificateIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.value().name(), issuerResponse.value().provider());
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuerWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuer#issuerBase
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getCertificateIssuer(issuerBase)
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.name(), issuerResponse.provider());
            }));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuer#issuerBase


        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuerWithResponse#issuerBase
        certificateAsyncClient.getCertificateIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getCertificateIssuerWithResponse(issuerBase.value())
                .subscribe(issuerResponse -> {
                    System.out.printf("Issuer returned with %s and %s", issuerResponse.value().name(), issuerResponse.value().provider());
                }));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getCertificateIssuerWithResponse#issuerBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificate(CertificateBase)} (CertificateBase)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificate#CertificateBase
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
                //Update enabled status of the issuer
                certificate.enabled(false);
                certificateAsyncClient.updateCertificate(certificate)
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s \n",
                            certificateResponse.enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificateIssuer#IssuerBase
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                Issuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.enabled(false);
                certificateAsyncClient.updateCertificateIssuer(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s \n",
                            issuerResponse.enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificateIssuer#IssuerBase

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificateIssuerWithResponse#IssuerBase
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                Issuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.enabled(false);
                certificateAsyncClient.updateCertificateIssuerWithResponse(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s \n",
                            issuerResponse.value().enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificateIssuerWithResponse#IssuerBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificatePolicy(String, CertificatePolicy)}
     */
    public void updateCertificatePolicyCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificatePolicy#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificatePolicyResponseValue -> {
                CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
                // Update transparency
                certificatePolicy.certificateTransparency(true);
                certificateAsyncClient.updateCertificatePolicy("certificateName", certificatePolicy)
                    .subscribe(updatedPolicy ->
                        System.out.printf("Certificate policy's updated transparency status %s \n",
                            updatedPolicy.certificateTransparency().toString()));
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificatePolicyResponseValue -> {
                CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
                // Update transparency
                certificatePolicy.certificateTransparency(true);
                certificateAsyncClient.updateCertificatePolicyWithResponse("certificateName", certificatePolicy)
                    .subscribe(updatedPolicyResponse ->
                        System.out.printf("Certificate policy's updated transparency status %s \n",
                            updatedPolicyResponse.value().certificateTransparency().toString()));
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificate(CertificateBase)} (CertificateBase)}
     */
    public void updateCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificateWithResponse#CertificateBase
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
                //Update the enabled status of the certificate.
                certificate.enabled(false);
                certificateAsyncClient.updateCertificateWithResponse(certificate)
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s \n",
                            certificateResponse.value().enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.async.certificateclient.updateCertificateWithResponse#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificate#string
        certificateAsyncClient.deleteCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.recoveryId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateWithResponse#string
        certificateAsyncClient.deleteCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteCertificateIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateIssuerWithResponse#string
        certificateAsyncClient.deleteCertificateIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s \n", deletedIssuerResponse.value().name()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateIssuerWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateIssuer#string
        certificateAsyncClient.deleteCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s \n", deletedIssuerResponse.name()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateIssuer#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getDeletedCertificate(String)}
     */
    public void getDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificate#string
        certificateAsyncClient.getDeletedCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.recoveryId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificateWithResponse#string}
        certificateAsyncClient.getDeletedCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.getDeletedCertificateWithResponse#string}
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#purgeDeletedCertificate(String)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.purgeDeletedCertificate#string
        certificateAsyncClient.purgeDeletedCertificate("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.purgeDeletedCertificate#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#recoverDeletedCertificate(String)} (String)}
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.certificatevault.certificates.async.certificateclient.recoverDeletedCertificate#string
        certificateAsyncClient.recoverDeletedCertificate("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Certificate with name %s \n", recoveredSecretResponse.name()));
        // END: com.azure.security.certificatevault.certificates.async.certificateclient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.async.certificateclient.recoverDeletedCertificateWithResponse#string
        certificateAsyncClient.recoverDeletedCertificateWithResponse("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Certificate with name %s \n", recoveredSecretResponse.value().name()));
        // END: com.azure.security.certificatevault.certificates.async.certificateclient.recoverDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#backupCertificate(String)}
     */
    public void backupCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.backupCertificate#string
        certificateAsyncClient.backupCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBackupResponse ->
                System.out.printf("Certificate's Backup Byte array's length %s \n", certificateBackupResponse.length));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.backupCertificateWithResponse#string
        certificateAsyncClient.backupCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBackupResponse ->
                System.out.printf("Certificate's Backup Byte array's length %s \n", certificateBackupResponse.value().length));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.backupCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#restoreCertificate(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.restoreCertificate#byte
        byte[] certificateBackupByteArray = {};
        certificateAsyncClient.restoreCertificate(certificateBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s \n",
                certificateResponse.name(), certificateResponse.keyId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.restoreCertificateWithResponse#byte
        byte[] certificateBackup = {};
        certificateAsyncClient.restoreCertificate(certificateBackup)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s \n",
                certificateResponse.name(), certificateResponse.keyId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.restoreCertificateWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.listCertificates
        certificateAsyncClient.listCertificates()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
                    certificateResponse.name(), certificateResponse.keyId())));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.listCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificateIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.listCertificateIssuers
        certificateAsyncClient.listCertificateIssuers()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getCertificateIssuer(issuerBase)
                .subscribe(issuerResponse -> System.out.printf("Received issuer with name %s and provider %s",
                    issuerResponse.name(), issuerResponse.provider())));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.listCertificateIssuers
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.listDeletedCertificates
        certificateAsyncClient.listDeletedCertificates()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedCertificateResponse ->  System.out.printf("Deleted Certificate's Recovery Id %s \n",
                deletedCertificateResponse.recoveryId()));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.listDeletedCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.listCertificateVersions
        certificateAsyncClient.listCertificateVersions("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
                    certificateResponse.name(), certificateResponse.keyId())));
        // END: com.azure.security.keyvault.certificates.async.certificateclient.listCertificateVersions
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#setCertificateContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.setCertificateContacts#contacts

        // END: com.azure.security.keyvault.certificates.async.certificateclient.setCertificateContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.listCertificateContacts

        // END: com.azure.security.keyvault.certificates.async.certificateclient.listCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateContacts

        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateContacts
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#cancelCertificateOperation(String)} and
     * {@link CertificateAsyncClient#cancelCertificateOperationWithResponse(String)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.cancelCertificateOperation#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.cancelCertificateOperationWithResponse#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.cancelCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateOperationWithResponse#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateOperation#string

        // END: com.azure.security.keyvault.certificates.async.certificateclient.deleteCertificateOperation#string
    }


}
