// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;


import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.Contact;
import com.azure.security.keyvault.certificates.models.Issuer;
import com.azure.security.keyvault.certificates.models.MergeCertificateOptions;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
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
            .pipeline(pipeline)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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
                    policy.getIssuerName(), policy.getSubjectName()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicyWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(policyResponse ->
                System.out.printf("Certificate policy is returned with issuer name %s and subject name %s %n",
                    policyResponse.getValue().getIssuerName(), policyResponse.getValue().getSubjectName()));
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
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateResponse.getProperties().getName(), certificateResponse.getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithPolicy#String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#string-string
        String certificateVersion = "6A385B124DEF4096AF1361A85B16C204";
        certificateAsyncClient.getCertificateWithResponse("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateWithVersion.getValue().getProperties().getName(),
                    certificateWithVersion.getValue().getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificateWithResponse#string-string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String-String
        certificateAsyncClient.getCertificate("certificateName", certificateVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateWithVersion ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateWithVersion.getProperties().getName(), certificateWithVersion.getSecretId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateProperties
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificate -> certificateAsyncClient.getCertificate(certificate.getProperties())
            .subscribe(certificateResponse ->
                System.out.printf("Certificate is returned with name %s and secretId %s %n",
                    certificateResponse.getProperties().getName(), certificateResponse.getSecretId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getCertificate#CertificateProperties
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#beginCreateCertificate(String, CertificatePolicy)}
     */
    public void createCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy-Boolean-Map
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        certificateAsyncClient.beginCreateCertificate("certificateName", policy, true, tags)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy-Boolean-Map

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy
        CertificatePolicy certPolicy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
        certificateAsyncClient.beginCreateCertificate("certificateName", certPolicy)
            .getObserver().subscribe(pollResponse -> {
                System.out.println("---------------------------------------------------------------------------------");
                System.out.println(pollResponse.getStatus());
                System.out.println(pollResponse.getValue().getStatus());
                System.out.println(pollResponse.getValue().getStatusDetails());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createCertificate#String-CertificatePolicy
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#createIssuer(String, String)}
     */
    public void createCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#String-String
        certificateAsyncClient.createIssuer("issuerName", "providerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> {
                System.out.printf("Issuer created with %s and %s", issuer.getName(),
                    issuer.getProperties().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#String-String

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#issuer
        Issuer issuer = new Issuer("issuerName", "providerName")
            .setAccountId("keyvaultuser")
            .setPassword("temp2");
        certificateAsyncClient.createIssuer(issuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.getName(),
                    issuerResponse.getProperties().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuer#issuer

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#issuer
        Issuer newIssuer = new Issuer("issuerName", "providerName")
            .setAccountId("keyvaultuser")
            .setPassword("temp2");
        certificateAsyncClient.createIssuerWithResponse(newIssuer)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer created with %s and %s", issuerResponse.getValue().getName(),
                    issuerResponse.getValue().getProperties().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.createIssuerWithResponse#issuer
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
                    issuer.getProperties().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string
        certificateAsyncClient.getIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.getValue().getName(),
                    issuerResponse.getValue().getProperties().getProvider());
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#issuerProperties
        certificateAsyncClient.getIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> certificateAsyncClient.getIssuer(issuer.getProperties())
            .subscribe(issuerResponse -> {
                System.out.printf("Issuer returned with %s and %s", issuerResponse.getName(),
                    issuerResponse.getProperties().getProvider());
            }));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuer#issuerProperties


        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#issuerProperties
        certificateAsyncClient.getIssuerWithResponse("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuer -> certificateAsyncClient.getIssuerWithResponse(issuer.getValue().getProperties())
                .subscribe(issuerResponse -> {
                    System.out.printf("Issuer returned with %s and %s", issuerResponse.getValue().getName(),
                        issuerResponse.getValue().getProperties().getProvider());
                }));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.getIssuerWithResponse#issuerProperties
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificateProperties#CertificateProperties
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
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
     * Method to insert code snippets for {@link CertificateAsyncClient#updateIssuer(Issuer)}
     */
    public void updateCertificateIssuerCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#IssuerProperties
        certificateAsyncClient.getIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                Issuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.setEnabled(false);
                certificateAsyncClient.updateIssuer(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s %n",
                            issuerResponse.isEnabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuer#IssuerProperties

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuerWithResponse#IssuerProperties
        certificateAsyncClient.getIssuer("issuerName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerResponseValue -> {
                Issuer issuer = issuerResponseValue;
                //Update the enabled status of the issuer.
                issuer.setEnabled(false);
                certificateAsyncClient.updateIssuerWithResponse(issuer)
                    .subscribe(issuerResponse ->
                        System.out.printf("Issuer's enabled status %s %n",
                            issuerResponse.getValue().isEnabled().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateIssuerWithResponse#IssuerProperties
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
                certificatePolicy.setCertificateTransparency(true);
                certificateAsyncClient.updateCertificatePolicy("certificateName", certificatePolicy)
                    .subscribe(updatedPolicy ->
                        System.out.printf("Certificate policy's updated transparency status %s %n",
                            updatedPolicy.isCertificateTransparency().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicy#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string
        certificateAsyncClient.getCertificatePolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificatePolicyResponseValue -> {
                CertificatePolicy certificatePolicy = certificatePolicyResponseValue;
                // Update transparency
                certificatePolicy.setCertificateTransparency(true);
                certificateAsyncClient.updateCertificatePolicyWithResponse("certificateName",
                    certificatePolicy)
                    .subscribe(updatedPolicyResponse ->
                        System.out.printf("Certificate policy's updated transparency status %s %n",
                            updatedPolicyResponse.getValue().isCertificateTransparency().toString()));
            });
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePolicyWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#updateCertificateProperties(CertificateProperties)}
     */
    public void updateCertificateWithResponseCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.updateCertificatePropertiesWithResponse#CertificateProperties
        certificateAsyncClient.getCertificateWithPolicy("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponseValue -> {
                Certificate certificate = certificateResponseValue;
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
     * Method to insert code snippets for {@link CertificateAsyncClient#deleteCertificate(String)}
     */
    public void deleteCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificate#string
        certificateAsyncClient.deleteCertificate("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s %n", deletedSecretResponse.getRecoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificate#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateWithResponse#string
        certificateAsyncClient.deleteCertificateWithResponse("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Certificate's Recovery Id %s %n",
                    deletedSecretResponse.getValue().getRecoveryId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteCertificateWithResponse#string
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
     * Method to insert code snippets for {@link CertificateAsyncClient#recoverDeletedCertificate(String)}
     */
    public void recoverDeletedCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificate#string
        certificateAsyncClient.recoverDeletedCertificate("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Certificate with name %s %n",
                    recoveredSecretResponse.getProperties().getName()));
        // END: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificate#string

        // BEGIN: com.azure.security.certificatevault.certificates.CertificateAsyncClient.recoverDeletedCertificateWithResponse#string
        certificateAsyncClient.recoverDeletedCertificateWithResponse("deletedCertificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Certificate with name %s %n",
                    recoveredSecretResponse.getValue().getProperties().getName()));
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
     * Method to insert code snippets for {@link CertificateAsyncClient#restoreCertificate(byte[])}
     */
    public void restoreCertificateCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte
        byte[] certificateBackupByteArray = {};
        certificateAsyncClient.restoreCertificate(certificateBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s %n",
                certificateResponse.getProperties().getName(), certificateResponse.getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificate#byte

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.restoreCertificateWithResponse#byte
        byte[] certificateBackup = {};
        certificateAsyncClient.restoreCertificate(certificateBackup)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateResponse -> System.out.printf("Restored Certificate with name %s and key id %s %n",
                certificateResponse.getProperties().getName(), certificateResponse.getKeyId()));
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
                    certificateResponse.getProperties().getName(), certificateResponse.getKeyId())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificates
    }

    /**
     * Method to insert code snippets for {@link CertificateAsyncClient#listIssuers()}
     */
    public void listCertificateIssuersCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listIssuers
        certificateAsyncClient.listIssuers()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(issuerBase -> certificateAsyncClient.getIssuer(issuerBase)
                .subscribe(issuerResponse -> System.out.printf("Received issuer with name %s and provider %s",
                    issuerResponse.getName(), issuerResponse.getProperties().getProvider())));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listIssuers
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
     * Method to insert code snippets for {@link CertificateAsyncClient#listCertificateVersions(String)}
     */
    public void listCertificateVersionsCodeSnippets() {
        CertificateAsyncClient certificateAsyncClient = getCertificateAsyncClient();
        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listCertificateVersions
        certificateAsyncClient.listCertificateVersions("certificateName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(certificateBase -> certificateAsyncClient.getCertificate(certificateBase)
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
        Contact oontactToAdd = new Contact("user", "useremail@exmaple.com");
        certificateAsyncClient.setContacts(Arrays.asList(oontactToAdd)).subscribe(contact ->
            System.out.printf("Contact name %s and email %s", contact.getName(), contact.getEmailAddress())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.setContacts#contacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts
        certificateAsyncClient.listContacts().subscribe(contact ->
            System.out.printf("Contact name %s and email %s", contact.getName(), contact.getEmailAddress())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.listContacts

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts
        certificateAsyncClient.listContacts().subscribe(contact ->
            System.out.printf("Deleted Contact name %s and email %s", contact.getName(), contact.getEmailAddress())
        );
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.deleteContacts
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
                certificateOperation.getStatus()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperation#string

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string
        certificateAsyncClient.cancelCertificateOperationWithResponse("certificateName")
            .subscribe(certificateOperationResponse -> System.out.printf("Certificate operation status %s",
                certificateOperationResponse.getValue().getStatus()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.cancelCertificateOperationWithResponse#string

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
                certificate.getProperties().getName(), certificate.getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificate#String-List

        // BEGIN: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#String-List
        List<byte[]> x509Certificates = new ArrayList<>();
        certificateAsyncClient.mergeCertificateWithResponse("certificateName", x509Certificates)
            .subscribe(certificateResponse -> System.out.printf("Received Certificate with name %s and key id %s",
                certificateResponse.getValue().getProperties().getName(), certificateResponse.getValue().getKeyId()));
        // END: com.azure.security.keyvault.certificates.CertificateAsyncClient.mergeCertificateWithResponse#String-List

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
}
