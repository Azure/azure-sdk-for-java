// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationData;
import com.azure.security.attestation.models.AttestationDataInterpretation;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResult;
import org.bouncycastle.util.encoders.Hex;
import reactor.core.publisher.Mono;

import java.util.List;

public class AttestationClientJavaDocCodeSnippets {
    public static AttestationClient createSyncClient() {
        String endpoint = "https://sharedcus.cus.attest.azure.net";
        // BEGIN: com.azure.security.attestation.AttestationClientBuilder.buildClient
        AttestationClient client = new AttestationClientBuilder()
            .endpoint(endpoint)
            .buildClient();
        // END: com.azure.security.attestation.AttestationClientBuilder.buildClient

        // BEGIN: com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient
        AttestationAsyncClient asyncClient = new AttestationClientBuilder()
            .endpoint(endpoint)
            .buildAsyncClient();
        // END: com.azure.security.attestation.AttestationClientBuilder.buildAsyncClient

        return client;
    }

    public static AttestationAdministrationClient createAdminSyncClient() {
        String endpoint = "https://attestcus.cus.attest.azure.net";
        // BEGIN: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient
        AttestationAdministrationClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildClient();
        // END: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildClient

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient
        AttestationAdministrationAsyncClient asyncClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildAsyncClient();
        // END: com.azure.security.attestation.AttestationAdministrationClientBuilder.buildAsyncClient

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

        // BEGIN: com.azure.security.attestation.models.AttestationOptions.getRunTimeData
        AttestationOptions attestationOptions = new AttestationOptions(openEnclaveReport)
            .setRunTimeData(new AttestationData(runtimeData, AttestationDataInterpretation.JSON));

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

    public static void attestAsync1() {
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
        // END: com.azure.security.attestation.AttestationAsyncClient.getAttestationSignersWithResponse
    }

    public static void setPolicyCheckHashAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationAsyncClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildAsyncClient();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicy
        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        Mono<PolicyResult> resultMono = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, policyToSet);
        PolicyResult result = resultMono.block();
        // END: com.azure.security.attestation.AttestationAdministrationAsyncClient.setPolicy

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

    public static void setPolicyCheckHash() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClient client = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder()
                .build())
            .buildClient();

        // BEGIN: com.azure.security.attestation.AttestationAdministrationClient.setPolicy
        String policyToSet = "version=1.0; authorizationrules{=> permit();}; issuancerules{};";
        PolicyResult result = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, policyToSet);
        // END: com.azure.security.attestation.AttestationAdministrationClient.setPolicy

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
    static void executeSamples() {
        attestAsync1();
        attestationOptionsSnippets();
        attestationOptionsSnippets2();
        attestationOptionsSnippets3();
        createAdminSyncClient();
        createSyncClient();
        setPolicyCheckHash();
        setPolicyCheckHashAsync();
    }

}
