// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.security.attestation;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.security.attestation.models.AttestationData;
import com.azure.security.attestation.models.AttestationDataInterpretation;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationSigner;
import reactor.core.publisher.Mono;

import java.util.List;

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
        BinaryData openEnclaveReport = null;

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
        BinaryData runtimeData = null;
        BinaryData inittimeData = null;
        BinaryData openEnclaveReport = null;
        BinaryData sgxQuote = null;

        AttestationAsyncClient client = null;

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

}
