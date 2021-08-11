// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationSigner;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class AttestationClientJavaDocCodeSnippets {
    public static AttestationClient createSyncClient() {
        String endpoint = null;
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

    public static void attestationOptionsSnippets() {

        byte[] sgxQuote = null;
        // BEGIN: com.azure.security.attestation.models..fromEvidence#byte
        AttestationOptions options = AttestationOptions
            .fromEvidence(sgxQuote);
        // END: com.azure.security.attestation.models..fromEvidence#byte

        byte[] runtimeData = null;
        // BEGIN: com.azure.security.attestation.AttestationOptionsSetRunTimeData
        AttestationOptions optionsWithRunTimeData = AttestationOptions
            .fromEvidence(sgxQuote)
            .setRunTimeData(runtimeData)
            .interpretRunTimeDataAsBinary();
        // END: com.azure.security.attestation.AttestationOptionsSetRunTimeData

        byte[] inittimeData = null;
        byte[] openEnclaveReport = null;
        // BEGIN: com.azure.security.attestation.models..setInitTimeData#byte
        AttestationOptions optionsWithInitTimeData = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setInitTimeData(inittimeData)
            .interpretInitTimeDataAsBinary();
        // END: com.azure.security.attestation.models..setInitTimeData#byte

        // BEGIN: com.azure.security.attestation.models..interpretRunTimeDataAsBinary
        AttestationOptions optionsWithRunTimeDataInitialized = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setRunTimeData(runtimeData)
            .interpretRunTimeDataAsBinary();
        // END: com.azure.security.attestation.models..interpretRunTimeDataAsBinary

        // BEGIN: com.azure.security.attestation.models..interpretRunTimeDataAsJson
        AttestationOptions optionsWithRunTimeDataInitializedJson = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setRunTimeData(runtimeData)
            .interpretRunTimeDataAsJson();
        // END: com.azure.security.attestation.models..interpretRunTimeDataAsJson
    }

    public static void attestationOptionsSnippets2() {
        byte[] runtimeData = null;
        byte[] inittimeData = null;
        byte[] openEnclaveReport = null;

        // BEGIN: com.azure.security.attestation.models..getRunTimeData
        AttestationOptions attestationOptions = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setRunTimeData(runtimeData)
            .interpretRunTimeDataAsJson();

        byte[] existingRuntimeData = attestationOptions.getRunTimeData();
        // END: com.azure.security.attestation.models..getRunTimeData

        // BEGIN: com.azure.security.attestation.models..interpretInitTimeDataAsBinary
        AttestationOptions options = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setInitTimeData(inittimeData)
            .interpretInitTimeDataAsBinary();
        // END: com.azure.security.attestation.models..interpretInitTimeDataAsBinary


    }
    public static void attestationOptionsSnippets3() {
        byte[] inittimeData = null;
        byte[] openEnclaveReport = null;

        // BEGIN: com.azure.security.attestation.models..getInitTimeData
        AttestationOptions attestationOptions = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setInitTimeData(inittimeData)
            .interpretInitTimeDataAsJson();

        byte[] existingRuntimeData = attestationOptions.getInitTimeData();
        // END: com.azure.security.attestation.models..getInitTimeData

        // BEGIN: com.azure.security.attestation.models..setDraftPolicyForAttestation#String
        AttestationOptions request = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};");
        // END: com.azure.security.attestation.models..setDraftPolicyForAttestation#String

        // BEGIN: com.azure.security.attestation.models..getDraftPolicyForAttestation
        AttestationOptions getOptions = AttestationOptions
            .fromEvidence(openEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};");

        String draftPolicy = getOptions.getDraftPolicyForAttestation();
        // END: com.azure.security.attestation.models..getDraftPolicyForAttestation

    }

    public static void attestAsync1() {
        byte[] runtimeData = null;
        byte[] inittimeData = null;
        byte[] openEnclaveReport = null;
        byte[] sgxQuote = null;

        AttestationAsyncClient client = null;

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadataWithResponse
        Mono<Response<AttestationOpenIdMetadata>> response = client.getOpenIdMetadataWithResponse();
        // END: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadataWithResponse

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadata
        Mono<AttestationOpenIdMetadata> openIdMetadata = client.getOpenIdMetadata();
        // END: com.azure.security.attestation.AttestationAsyncClient.getOpenIdMetadata

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getAttestationSigners
        Mono<AttestationSigner[]> signers = client.getAttestationSigners();
        Arrays.stream(signers.block()).forEach(cert -> {
            System.out.println("Found certificate.");
            if (cert.getKeyId() != null) {
                System.out.println("    Certificate Key ID: " + cert.getKeyId());
            } else {
                System.out.println("    Signer does not have a Key ID");
            }
            Arrays.stream(cert.getCertificates()).forEach(chainElement -> {
                System.out.println("        Cert Subject: " + chainElement.getSubjectDN().getName());
                System.out.println("        Cert Issuer: " + chainElement.getIssuerDN().getName());
            });
        });
        // END: com.azure.security.attestation.AttestationAsyncClient.getAttestationSigners

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.getAttestationSignersWithResponse
        Mono<Response<AttestationSigner[]>> responseOfSigners = client.getAttestationSignersWithResponse();
        // END: com.azure.security.attestation.AttestationAsyncClient.getAttestationSignersWithResponse
    }

}
