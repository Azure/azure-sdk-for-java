// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.security.attestation;

import com.azure.security.attestation.models.AttestationOptions;

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

}
