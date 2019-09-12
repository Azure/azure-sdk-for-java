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
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.CertificateBase;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


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
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RetryPolicy()).build();
        CertificateAsyncClient keyClient = new CertificateClientBuilder()
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(new RecordNetworkCallPolicy(networkData))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.withhttpclient.instantiation
        return keyClient;
    }

    /**
     * Implementation for async CertificateAsyncClient
     * @return sync CertificateAsyncClient
     */
    private CertificateAsyncClient getCertificateAsyncClient() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation
        CertificateAsyncClient secretAsyncClient = new CertificateClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.instantiation
        return secretAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link CertificateAsyncClient}
     * @return An instance of {@link CertificateAsyncClient}
     */
    public CertificateAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.pipeline.instantiation
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();
        CertificateAsyncClient secretAsyncClient = new CertificateClientBuilder()
            .pipeline(pipeline)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.pipeline.instantiation
        return secretAsyncClient;
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificatePolicy(String)}
     */
    public void getCertiificatePolicyCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policy ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n",
                    policy.issuerName(), policy.subjectName()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicyWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policyResponse ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n",
                    policyResponse.getValue().issuerName(), policyResponse.getValue().subjectName()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string
    }



    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificateWithPolicy(String)}
     */
    public void getCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithPolicy#String
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.name(),
                    certificateResponse.secretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithPolicy#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#string-string
        String certificateVersion = "6A385B124DEF4096AF1361A85B16C204";
        certificateAsyncClient.getCertificateWithResponse("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s \n",
                    certificateWithVersion.getValue().name(), certificateWithVersion.getValue().secretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#string-string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String-String
        certificateAsyncClient.getCertificate("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s \n",
                    certificateWithVersion.name(), certificateWithVersion.secretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateBase
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.name(),
                    certificateResponse.secretId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#createCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy-Map
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
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy-Map

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy
        CertificatePolicy certPolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        certificateAsyncClient.createCertificate("certificateName", certPolicy)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().status());
                System.out.println(pollResponse.getValue().statusDetails());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#createCertificateIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificateIssuer#String-String
        certificateAsyncClient.createCertificateIssuer("issuerName", "providerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> {
                System.out.printf("Issuer created with %s and %s", issuer.name(), issuer.provider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificateIssuer#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificateIssuer#issuer
        Issuer issuer = new Issuer("issuerName", "providerName")
            .accountId("keyvaultuser")
            .password("temp2");
        certificateAsyncClient.createCertificateIssuer(issuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.name(), issuerResponse.provider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificateIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificateIssuerWithResponse#issuer
        Issuer newIssuer = new Issuer("issuerName", "providerName")
            .accountId("keyvaultuser")
            .password("temp2");
        certificateAsyncClient.createCertificateIssuerWithResponse(newIssuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.getValue().name(),
                    issuerResponse.getValue().provider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificateIssuerWithResponse#issuer
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getCertificateIssuer(String)}
     */
    public void getCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuer#string
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> {
                System.out.printf("Issuer returned with %s and %s", issuer.name(), issuer.provider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuerWithResponse#string
        certificateAsyncClient.getCertificateIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.getValue().name(),
                    issuerResponse.getValue().provider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuerWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuer#issuerBase
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getCertificateIssuer(issuerBase)
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.name(), issuerResponse.provider());
            }));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuer#issuerBase


        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuerWithResponse#issuerBase
        certificateAsyncClient.getCertificateIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getCertificateIssuerWithResponse(issuerBase.getValue())
                .subscribe(issuerResponse -> {
                    System.out.printf("Issuer returned with %s and %s", issuerResponse.getValue().name(),
                        issuerResponse.getValue().provider());
                }));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateIssuerWithResponse#issuerBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificate(CertificateBase)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificate#CertificateBase
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
                //Update enabled status of the certificate
                certificate.enabled(false);
                certificateAsyncClient.updateCertificate(certificate)
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s \n",
                            certificateResponse.enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificate#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateIssuer#IssuerBase
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
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateIssuer#IssuerBase

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateIssuerWithResponse#IssuerBase
        certificateAsyncClient.getCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                Issuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.enabled(false);
                certificateAsyncClient.updateCertificateIssuerWithResponse(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s \n",
                            issuerResponse.getValue().enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateIssuerWithResponse#IssuerBase
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
                certificatePolicy.certificateTransparency(true);
                certificateAsyncClient.updateCertificatePolicy("certificateName", certificatePolicy)
                    .subscribe(updatedPolicy ->
                        System.out.printf("Certificate policy's updated transparency status %s \n",
                            updatedPolicy.certificateTransparency().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificatePolicyResponseValue -> {
                CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
                // Update transparency
                certificatePolicy.certificateTransparency(true);
                certificateAsyncClient.updateCertificatePolicyWithResponse("certificateName",
                    certificatePolicy)
                    .subscribe(updatedPolicyResponse ->
                        System.out.printf("Certificate policy's updated transparency status %s \n",
                            updatedPolicyResponse.getValue().certificateTransparency().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificate(CertificateBase)}
     */
    public void updateCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateWithResponse#CertificateBase
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
                //Update the enabled status of the certificate.
                certificate.enabled(false);
                certificateAsyncClient.updateCertificateWithResponse(certificate)
                    .subscribe(certificateResponse ->
                        System.out.printf("Certificate's enabled status %s \n",
                            certificateResponse.getValue().enabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateWithResponse#CertificateBase
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificate#string
        certificateAsyncClient.deleteCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.recoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateWithResponse#string
        certificateAsyncClient.deleteCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n",
                    deletedSecretResponse.getValue().recoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteCertificateIssuer(String)}
     */
    public void deleteCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateIssuerWithResponse#string
        certificateAsyncClient.deleteCertificateIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s \n", deletedIssuerResponse.getValue().name()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateIssuerWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateIssuer#string
        certificateAsyncClient.deleteCertificateIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedIssuerResponse ->
                System.out.printf("Deleted issuer with name %s \n", deletedIssuerResponse.name()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateIssuer#string
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
                System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.recoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string
        certificateAsyncClient.getDeletedCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s \n",
                    deletedSecretResponse.getValue().recoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#purgeDeletedCertificate(String)}
     */
    public void purgeDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string
        certificateAsyncClient.purgeDeletedCertificate("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d \n", purgeResponse.getStatusCode()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.purgeDeletedCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#recoverDeletedCertificate(String)}
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificate#string
        certificateAsyncClient.recoverDeletedCertificate("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Certificate with name %s \n", recoveredSecretResponse.name()));
        // END: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificateWithResponse#string
        certificateAsyncClient.recoverDeletedCertificateWithResponse("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Certificate with name %s \n", recoveredSecretResponse.getValue().name()));
        // END: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificateWithResponse#string
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
                System.out.printf("Certificate's Backup Byte array's length %s \n", certificateBackupResponse.length));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string
        certificateAsyncClient.backupCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBackupResponse ->
                System.out.printf("Certificate's Backup Byte array's length %s \n",
                    certificateBackupResponse.getValue().length));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.backupCertificateWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#restoreCertificate(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte
        byte[] certificateBackupByteArray = {};
        certificateAsyncClient.restoreCertificate(certificateBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s \n",
                certificateResponse.name(), certificateResponse.keyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte
        byte[] certificateBackup = {};
        certificateAsyncClient.restoreCertificate(certificateBackup)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s \n",
                certificateResponse.name(), certificateResponse.keyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificates()}
     */
    public void listCertificatesCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates
        certificateAsyncClient.listCertificates()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
                    certificateResponse.name(), certificateResponse.keyId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificateIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateIssuers
        certificateAsyncClient.listCertificateIssuers()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getCertificateIssuer(issuerBase)
                .subscribe(issuerResponse -> System.out.printf("Received issuer with name %s and provider %s",
                    issuerResponse.name(), issuerResponse.provider())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateIssuers
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listDeletedCertificates()}
     */
    public void listDeletedCertificatesCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates
        certificateAsyncClient.listDeletedCertificates()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedCertificateResponse ->  System.out.printf("Deleted Certificate's Recovery Id %s \n",
                deletedCertificateResponse.recoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listDeletedCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions
        certificateAsyncClient.listCertificateVersions("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
                .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
                    certificateResponse.name(), certificateResponse.keyId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#setCertificateContacts(List)}
     */
    public void contactsOperationsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.setCertificateContacts#contacts
        Contact oontactToAdd = new Contact("user", "useremail@exmaple.com");
        certificateAsyncClient.setCertificateContacts(Arrays.asList(oontactToAdd)).subscribe(contact ->
            System.out.printf("Contact name %s and email %s", contact.name(), contact.emailAddress())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.setCertificateContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateContacts
        certificateAsyncClient.listCertificateContacts().subscribe(contact ->
            System.out.printf("Contact name %s and email %s", contact.name(), contact.emailAddress())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateContacts
        certificateAsyncClient.listCertificateContacts().subscribe(contact ->
            System.out.printf("Deleted Contact name %s and email %s", contact.name(), contact.emailAddress())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateContacts
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#cancelCertificateOperation(String)} and
     * {@link CertificateAsyncClient#cancelCertificateOperationWithResponse(String)}
     */
    public void certificateOperationCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string
        certificateAsyncClient.cancelCertificateOperation("certificateName")
            .subscribe(certificateOperation -> System.out.printf("Certificate operation status %s",
                certificateOperation.status()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string
        certificateAsyncClient.cancelCertificateOperationWithResponse("certificateName")
            .subscribe(certificateOperationResponse -> System.out.printf("Certificate operation status %s",
                certificateOperationResponse.getValue().status()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string
        certificateAsyncClient.deleteCertificateOperationWithResponse("certificateName")
            .subscribe(certificateOperationResponse -> System.out.printf("Deleted Certificate operation's last"
                + " status %s", certificateOperationResponse.getValue().status()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperationWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string
        certificateAsyncClient.deleteCertificateOperation("certificateName")
            .subscribe(certificateOperation -> System.out.printf("Deleted Certificate operation last status %s",
                certificateOperation.status()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateOperation#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#getPendingCertificateSigningRequest(String)}
     * and {@link CertificateAsyncClient#getPendingCertificateSigningRequestWithResponse(String)}
     */
    public void getPendingCertificateSigningRequestCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getPendingCertificateSigningRequest#string
        certificateAsyncClient.getPendingCertificateSigningRequest("certificateName")
            .subscribe(signingRequest -> System.out.printf("Received Signing request blob of length %s",
                signingRequest.length));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getPendingCertificateSigningRequest#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getPendingCertificateSigningRequestWithResponse#string
        certificateAsyncClient.getPendingCertificateSigningRequestWithResponse("certificateName")
            .subscribe(signingRequestResponse -> System.out.printf("Received Signing request blob of length %s",
                signingRequestResponse.getValue().length));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getPendingCertificateSigningRequestWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#mergeCertificate(MergeCertificateOptions)}
     * and {@link CertificateAsyncClient#mergeCertificate(String, List)}
     */
    public void mergeCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#String-List
        List<byte[]> x509Certs = new ArrayList<>();
        certificateAsyncClient.mergeCertificate("certificateName", x509Certs)
            .subscribe(certificate -> System.out.printf("Received Certificate with name %s and key id %s",
                certificate.name(), certificate.keyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#String-List

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#String-List
        List<byte[]> x509Certificates = new ArrayList<>();
        certificateAsyncClient.mergeCertificateWithResponse("certificateName", x509Certificates)
            .subscribe(certificateResponse -> System.out.printf("Received Certificate with name %s and key id %s",
                certificateResponse.getValue().name(), certificateResponse.getValue().keyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#String-List

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config
        List<byte[]> x509CertificatesToMerge = new ArrayList<>();
        MergeCertificateOptions config = new MergeCertificateOptions("certificateName", x509CertificatesToMerge)
            .enabled(false);
        certificateAsyncClient.mergeCertificate(config)
            .subscribe(certificate -> System.out.printf("Received Certificate with name %s and key id %s",
                certificate.name(), certificate.keyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#config

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config
        List<byte[]> x509CertsToMerge = new ArrayList<>();
        MergeCertificateOptions mergeConfig = new MergeCertificateOptions("certificateName", x509CertsToMerge)
            .enabled(false);
        certificateAsyncClient.mergeCertificateWithResponse(mergeConfig)
            .subscribe(certificateResponse -> System.out.printf("Received Certificate with name %s and key id %s",
                certificateResponse.getValue().name(), certificateResponse.getValue().keyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#config
    }
}
