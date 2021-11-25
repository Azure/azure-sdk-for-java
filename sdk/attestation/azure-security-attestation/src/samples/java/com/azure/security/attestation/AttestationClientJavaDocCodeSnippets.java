// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationData;
import com.azure.security.attestation.models.AttestationDataInterpretation;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResult;
import org.bouncycastle.util.encoders.Hex;
import reactor.core.publisher.Mono;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;

public class AttestationClientJavaDocCodeSnippets {
    public static AttestationClient createSyncClient() {
        String endpoint = "https://sharedcus.cus.attest.azure.net";
        // BEGIN: com.azure.security.attestation.AttestationClientBuilder.buildClient
        AttestationClient client = new AttestationClientBuilder()
            .endpoint(endpoint)
            .buildClient();
        // END: com.azure.security.attestation.AttestationClientBuilder.buildClient

        // BEGIN: com.azure.security.attestation.AttestationClientBuilder.buildClientWithValidation
        AttestationClient validatedClient = new AttestationClientBuilder()
            .endpoint(endpoint)
            .tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidationSlack(Duration.ofSeconds(10)) // Allow 10 seconds of clock drift between attestation service and client.
                .setValidationCallback((token, signer) -> { // Perform custom validation steps.
                    System.out.printf("Validate token signed by signer %s", signer.getCertificates().get(0).getSubjectDN().toString());
                }))
            .buildClient();
        // END: com.azure.security.attestation.AttestationClientBuilder.buildClientWithValidation

        // BEGIN: com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient
        AttestationAsyncClient asyncClient = new AttestationClientBuilder()
            .endpoint(endpoint)
            .buildAsyncClient();
        // END: com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient

        // BEGIN: com.azure.security.attestation.AttestationClientBuilder.buildAsyncClientForTpm
        AttestationAsyncClient asyncClientForTpm = new AttestationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.attestation.AttestationClientBuilder.buildAsyncClientForTpm


        return client;
    }

    public static AttestationAdministrationClient createAdminSyncClient() {
        String endpoint = "https://attestcus.cus.attest.azure.net";
        // BEGIN: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient
        AttestationAdministrationClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient
        AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClientWithValidation
        AttestationAdministrationClient validatedClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidationSlack(Duration.ofSeconds(10)) // Allow 10 seconds of clock drift between attestation service and client.
                .setValidationCallback((token, signer) -> { // Perform custom validation steps.
                    System.out.printf("Validate token signed by signer %s", signer.getCertificates().get(0).getSubjectDN().toString());
                }))
            .buildClient();
        // END: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClientWithValidation


        return client;
    }


    public static void attestationOptionsSnippets() {

        BinaryData sgxQuote = null;
        // BEGIN: com.azure.security.attestation.models..fromEvidence#byte
        AttestationOptions options = new AttestationOptions(sgxQuote);
        // END: com.azure.security.attestation.models..fromEvidence#byte

        BinaryData runtimeData = null;
        // BEGIN: com.azure.security.attestation.AttestationOptionsSetRunTimeData
        AttestationOptions optionsWithRunTimeData = new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.BINARY));
        // END: com.azure.security.attestation.AttestationOptionsSetRunTimeData

        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = null;
        // BEGIN: com.azure.security.attestation.models..setInitTimeData#byte
        AttestationOptions optionsWithInitTimeData = new AttestationOptions(openEnclaveReport)
            .setInitTimeData(new AttestationData(inittimeData, AttestationDataInterpretation.BINARY));
        // END: com.azure.security.attestation.models..setInitTimeData#byte

        // BEGIN: com.azure.security.attestation.models..interpretRunTimeDataAsBinary
        AttestationOptions optionsWithRunTimeDataInitialized = new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.BINARY));
        // END: com.azure.security.attestation.models..interpretRunTimeDataAsBinary

        // BEGIN: com.azure.security.attestation.models..interpretRunTimeDataAsJson
        AttestationOptions optionsWithRunTimeDataInitializedJson = new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON));
        // END: com.azure.security.attestation.models..interpretRunTimeDataAsJson
    }

    public static void attestationOptionsSnippets2() {
        BinaryData runtimeData = null;
        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = null;

        AttestationOptions attestationOptions = new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON));

        // BEGIN: com.azure.security.attestation.models.AttestationOptions.getRunTimeData
        AttestationData existingRuntimeData = attestationOptions.getRunTimeData();
        // END: com.azure.security.attestation.models.AttestationOptions.getRunTimeData

    }
    public static void attestationOptionsSnippets3() {
        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());

        // BEGIN: com.azure.security.attestation.models.AttestationOptions.getInitTimeData
        AttestationOptions attestationOptions = new AttestationOptions(openEnclaveReport)
            .setInitTimeData(new AttestationData(inittimeData, AttestationDataInterpretation.JSON));

        AttestationData existingRuntimeData = attestationOptions.getInitTimeData();
        // END: com.azure.security.attestation.models.AttestationOptions.getInitTimeData

        // BEGIN: com.azure.security.attestation.models.AttestationOptions.setDraftPolicyForAttestation#String
        AttestationOptions request = new AttestationOptions(openEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};");
        // END: com.azure.security.attestation.models.AttestationOptions.setDraftPolicyForAttestation#String

        // BEGIN: com.azure.security.attestation.models.AttestationOptions.getDraftPolicyForAttestation
        AttestationOptions getOptions = new AttestationOptions(openEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};");

        String draftPolicy = getOptions.getDraftPolicyForAttestation();
        // END: com.azure.security.attestation.models.AttestationOptions.getDraftPolicyForAttestation

    }

    public static void attestOpenEnclaveSync1() {
        BinaryData runtimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        AttestationClient client = new AttestationClientBuilder()
            .endpoint("https://sharedcus.cus.attest.azure.net")
            .buildClient();

        // BEGIN: com.azure.security.attestation.AttestationClient.getOpenIdMetadataWithResponse
        Response<AttestationOpenIdMetadata> response = client.getOpenIdMetadataWithResponse(Context.NONE);
        // END: com.azure.security.attestation.AttestationClient.getOpenIdMetadataWithResponse

        // BEGIN: com.azure.security.attestation.AttestationClient.getOpenIdMetadata
        AttestationOpenIdMetadata openIdMetadata = client.getOpenIdMetadata();
        // END: com.azure.security.attestation.AttestationClient.getOpenIdMetadata

        // BEGIN: com.azure.security.attestation.AttestationClient.getAttestationSigners
        List<AttestationSigner> signers = client.listAttestationSigners();
        signers.forEach(cert -> {
            System.out.println("Found certificate.");
            if (cert.getKeyId() != null) {
                System.out.println("    Certificate Key ID: " + cert.getKeyId());
            } else {
                System.out.println("    Signer does not have a Key ID");
            }
            cert.getCertificates().forEach(chainElement -> {
                System.out.println("        Cert Subject: " + chainElement.getSubjectDN().getName());
                System.out.println("        Cert Issuer: " + chainElement.getIssuerDN().getName());
            });
        });
        // END: com.azure.security.attestation.AttestationClient.getAttestationSigners

        // BEGIN: com.azure.security.attestation.AttestationClient.getAttestationSignersWithResponse
        Response<List<AttestationSigner>> responseOfSigners = client.listAttestationSignersWithResponse(Context.NONE);
        // END: com.azure.security.attestation.AttestationClient.getAttestationSignersWithResponse

        // BEGIN: com.azure.security.attestation.AttestationClient.attestOpenEnclaveWithReport
        AttestationResult resultWithReport = client.attestOpenEnclave(openEnclaveReport);
        // END: com.azure.security.attestation.AttestationClient.attestOpenEnclaveWithReport

        // BEGIN: com.azure.security.attestation.AttestationClient.attestOpenEnclaveWithResponseWithReport
        Response<AttestationResult> responseWithReport = client.attestOpenEnclaveWithResponse(openEnclaveReport, Context.NONE);
        // END: com.azure.security.attestation.AttestationClient.attestOpenEnclaveWithResponseWithReport


        // BEGIN: com.azure.security.attestation.AttestationClient.attestOpenEnclave
        AttestationResult result = client.attestOpenEnclave(new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.BINARY)));

        // END: com.azure.security.attestation.AttestationClient.attestOpenEnclave

        // BEGIN: com.azure.security.attestation.AttestationClient.attestOpenEnclaveWithResponse
        Response<AttestationResult> openEnclaveResponse = client.attestOpenEnclaveWithResponse(new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON)), Context.NONE);

        // END: com.azure.security.attestation.AttestationClient.attestOpenEnclaveWithResponse

    }

    public static void attestOpenEnclaveAsync1() {
        BinaryData runtimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        AttestationAsyncClient client = new AttestationClientBuilder()
            .endpoint("https://sharedcus.cus.attest.azure.net")
            .buildAsyncClient();

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadataWithResponse
        Mono<Response<AttestationOpenIdMetadata>> response = client.getOpenIdMetadataWithResponse();
        // END: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadataWithResponse

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadata
        Mono<AttestationOpenIdMetadata> openIdMetadata = client.getOpenIdMetadata();
        // END: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadata

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getAttestationSigners
        Mono<List<AttestationSigner>> signers = client.listAttestationSigners();
        signers.block().forEach(cert -> {
            System.out.println("Found certificate.");
            if (cert.getKeyId() != null) {
                System.out.println("    Certificate Key ID: " + cert.getKeyId());
            } else {
                System.out.println("    Signer does not have a Key ID");
            }
            cert.getCertificates().forEach(chainElement -> {
                System.out.println("        Cert Subject: " + chainElement.getSubjectDN().getName());
                System.out.println("        Cert Issuer: " + chainElement.getIssuerDN().getName());
            });
        });
        // END: com.azure.security.attestation.AttestationAsyncClient.getAttestationSigners

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getAttestationSignersWithResponse
        Mono<Response<List<AttestationSigner>>> responseOfSigners = client.listAttestationSignersWithResponse();
        responseOfSigners.subscribe();
        // END: com.azure.security.attestation.AttestationAsyncClient.getAttestationSignersWithResponse

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclaveWithReport
        Mono<AttestationResult> resultWithReport = client.attestOpenEnclave(openEnclaveReport);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclaveWithReport

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclaveWithResponseWithReport
        Mono<Response<AttestationResult>> responseWithReport = client.attestOpenEnclaveWithResponse(openEnclaveReport);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclaveWithResponseWithReport


        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclave
        Mono<AttestationResult> result = client.attestOpenEnclave(new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.BINARY)));

        // END: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclave

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclaveWithResponse
        Mono<Response<AttestationResult>> openEnclaveResponse = client.attestOpenEnclaveWithResponse(new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON)), Context.NONE);

        // END: com.azure.security.attestation.AttestationAsyncClient.attestOpenEnclaveWithResponse


    }

    public static void attestSgxEnclaveSync1() {
        BinaryData runtimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData inittimeData = null;
        BinaryData sgxEnclaveReport = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        AttestationClient client = new AttestationClientBuilder()
            .endpoint("https://sharedcus.cus.attest.azure.net")
            .buildClient();

        // BEGIN: com.azure.security.attestation.AttestationClient.attestSgxEnclaveWithReport
        AttestationResult resultWithReport = client.attestSgxEnclave(sgxEnclaveReport);
        // END: com.azure.security.attestation.AttestationClient.attestSgxEnclaveWithReport

        // BEGIN: com.azure.security.attestation.AttestationClient.attestSgxEnclaveWithResponseWithReport
        Response<AttestationResult> responseWithReport = client.attestSgxEnclaveWithResponse(sgxQuote, Context.NONE);
        // END: com.azure.security.attestation.AttestationClient.attestSgxEnclaveWithResponseWithReport


        // BEGIN: com.azure.security.attestation.AttestationClient.attestSgxEnclave
        AttestationResult result = client.attestSgxEnclave(new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.BINARY)));

        // END: com.azure.security.attestation.AttestationClient.attestSgxEnclave

        // BEGIN: com.azure.security.attestation.AttestationClient.attestSgxEnclaveWithResponse
        Response<AttestationResult> openEnclaveResponse = client.attestSgxEnclaveWithResponse(new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON)), Context.NONE);

        // END: com.azure.security.attestation.AttestationClient.attestSgxEnclaveWithResponse
    }

    public static void attestSgxEnclaveAsync1() {
        BinaryData runtimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        AttestationAsyncClient client = new AttestationClientBuilder()
            .endpoint("https://sharedcus.cus.attest.azure.net")
            .buildAsyncClient();

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclaveWithReport
        Mono<AttestationResult> resultWithReport = client.attestSgxEnclave(sgxQuote);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclaveWithReport

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclaveWithResponseWithReport
        Mono<Response<AttestationResult>> responseWithReport = client.attestSgxEnclaveWithResponse(sgxQuote);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclaveWithResponseWithReport


        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclave
        Mono<AttestationResult> result = client.attestSgxEnclave(new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.BINARY)));

        // END: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclave

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclaveWithResponse
        Mono<Response<AttestationResult>> openEnclaveResponse = client.attestSgxEnclaveWithResponse(new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON)), Context.NONE);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestSgxEnclaveWithResponse


    }

    public static void getPolicySync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildClient();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.getPolicy
        String policy = client.getAttestationPolicy(AttestationType.SGX_ENCLAVE);
        // END: com.azure.security.attestation.AttestationAdministrationClient.getPolicy
        System.out.printf("Current SGX policy: %s", policy);

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.getPolicyWithResponse
        Response<String> response = client.getAttestationPolicyWithResponse(AttestationType.SGX_ENCLAVE, null, Context.NONE);
        // END: com.azure.security.attestation.AttestationAdministrationClient.getPolicyWithResponse
        System.out.printf("Current SGX policy: %s", response.getValue());
    }

    public static void getPolicyAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationAsyncClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildAsyncClient();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.getPolicy
        Mono<String> policyMono = client.getAttestationPolicy(AttestationType.SGX_ENCLAVE);
        String policy = policyMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.getPolicy
        System.out.printf("Current SGX policy: %s", policy);

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.getPolicyWithResponse
        Mono<Response<String>> responseMono = client.getAttestationPolicyWithResponse(AttestationType.SGX_ENCLAVE, null);
        Response<String> response = responseMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.getPolicyWithResponse
        System.out.printf("Current SGX policy: %s", response.getValue());
    }

    public static void setPolicySimpleCheckHashAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationAsyncClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildAsyncClient();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicySimple
        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        Mono<PolicyResult> resultMono = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, policyToSet);
        PolicyResult result = resultMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicySimple

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicyWithResponseSimple
        Mono<Response<PolicyResult>> resultWithResponseMono = client.setAttestationPolicyWithResponse(
            AttestationType.OPEN_ENCLAVE, "version=1.0; authorizationrules{=> permit();}; issuancerules{};");
        Response<PolicyResult> response = resultWithResponseMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicyWithResponseSimple


        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.checkPolicyTokenHash
        BinaryData expectedHash = client.calculatePolicyTokenHash(policyToSet, null);
        BinaryData actualHash = result.getPolicyTokenHash();
        String expectedString = Hex.toHexString(expectedHash.toBytes());
        String actualString = Hex.toHexString(actualHash.toBytes());
        if (!expectedString.equals(actualString)) {
            throw new RuntimeException("Policy was set but not received!!!");
        }
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.checkPolicyTokenHash

    }

    public static void setPolicySimpleCheckHash() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildClient();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.setPolicySimple
        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        PolicyResult result = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, policyToSet);
        // END: com.azure.security.attestation.AttestationAdministrationClient.setPolicySimple

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.setPolicySimpleWithResponse
        Response<PolicyResult> response = client.setAttestationPolicyWithResponse(AttestationType.OPEN_ENCLAVE,
            "version=1.0; authorizationrules{=> permit();}; issuancerules{};", Context.NONE);
        // END: com.azure.security.attestation.AttestationAdministrationClient.setPolicySimpleWithResponse


        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.checkPolicyTokenHash
        BinaryData expectedHash = client.calculatePolicyTokenHash(policyToSet, null);
        BinaryData actualHash = result.getPolicyTokenHash();
        String expectedString = Hex.toHexString(expectedHash.toBytes());
        String actualString = Hex.toHexString(actualHash.toBytes());
        if (!expectedString.equals(actualString)) {
            throw new RuntimeException("Policy was set but not received!!!");
        }
        // END: com.azure.security.attestation.AttestationAdministrationClient.checkPolicyTokenHash
    }

    public static void setPolicyAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationAsyncClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildAsyncClient();

        X509Certificate certificate = SampleCollateral.getSigningCertificate();
        PrivateKey privateKey = SampleCollateral.getSigningKey();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicy
        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        Mono<PolicyResult> resultMono = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE,
            new AttestationPolicySetOptions()
                .setAttestationPolicy(policyToSet)
                .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)));
        PolicyResult result = resultMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicy

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicyWithResponse
        Mono<Response<PolicyResult>> resultWithResponseMono = client.setAttestationPolicyWithResponse(AttestationType.OPEN_ENCLAVE,
            new AttestationPolicySetOptions()
                .setAttestationPolicy(policyToSet)
                .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)));
        Response<PolicyResult> response = resultWithResponseMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicyWithResponse
    }

    public static void setPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildClient();

        X509Certificate certificate = SampleCollateral.getSigningCertificate();
        PrivateKey privateKey = SampleCollateral.getSigningKey();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.setPolicy
        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        PolicyResult result = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE,
            new AttestationPolicySetOptions()
                .setAttestationPolicy(policyToSet)
                .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)));
        // END: com.azure.security.attestation.AttestationAdministrationClient.setPolicy

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.setPolicyWithResponse
        Response<PolicyResult> response = client.setAttestationPolicyWithResponse(AttestationType.OPEN_ENCLAVE,
            new AttestationPolicySetOptions()
                .setAttestationPolicy(policyToSet)
                .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)), Context.NONE);

        // END: com.azure.security.attestation.AttestationAdministrationClient.setPolicyWithResponse


    }


    static void executeSamples() {
        createAdminSyncClient();
        createSyncClient();

        attestOpenEnclaveAsync1();
        attestOpenEnclaveSync1();

        attestSgxEnclaveSync1();
        attestSgxEnclaveAsync1();

        attestationOptionsSnippets();
        attestationOptionsSnippets2();
        attestationOptionsSnippets3();

        getPolicySync();
        getPolicyAsync();

        setPolicySimpleCheckHash();
        setPolicySimpleCheckHashAsync();

        setPolicy();
        setPolicyAsync();
    }

}
