// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationData;
import com.azure.security.attestation.models.AttestationDataInterpretation;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyModification;
import com.azure.security.attestation.models.PolicyResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;

/**
 * The AttestationSamples class contains samples demonstrating how to use the attestation SDK to attest evidence
 * created by an enclave.
 */
public class AttestationSamples {

    /**
     * Attest evidence from an OpenEnclave enclave when the "runtime data" (data included in the
     * OpenEnclave report) should be interpreted as binary data.
     */
    static void attestOpenEnclaveBinary() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        //BEGIN: readme-sample-create-synchronous-client
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Note that the "attest" calls do not require authentication.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();
        //END: readme-sample-create-synchronous-client

        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());

        // Attest evidence from an OpenEnclave enclave specifying runtime data which should be
        // interpreted as binary data.
        AttestationResult result = client.attestOpenEnclave(new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(
                new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY)));

        String issuer = result.getIssuer();
        System.out.println("Attest OpenEnclave completed. Issuer: " + issuer);
    }

    /**
     * Attest evidence from an OpenEnclave enclave when the "runtime data" (data included in the
     * OpenEnclave report) should be interpreted as json data.
     */
    static void attestOpenEnclaveJson() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        //BEGIN: readme-sample-create-asynchronous-client
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Note that the "attest" calls do not require authentication.
        AttestationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .buildAsyncClient();
        //END: readme-sample-create-asynchronous-client

        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(SampleCollateral.getOpenEnclaveReport());

        // Attest evidence from an OpenEnclave enclave specifying runtime data which should be
        // interpreted as binary data.
        client.attestOpenEnclave(new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(
                new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON)))
            .subscribe(result -> {

                String issuer = result.getIssuer();

                System.out.println("Attest OpenEnclave completed. Issuer: " + issuer);

                // Now dump the returned set of runtime claims. The MAA service has verified that the
                // claims in the resulting token all were generated within the enclave.
                if (result.getRuntimeClaims() instanceof LinkedHashMap) {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> runtimeClaims = (LinkedHashMap) result.getRuntimeClaims();
                    for (String key : runtimeClaims.keySet()) {
                        System.out.printf("Claim %s:", key);
                        JacksonAdapter serializer = new JacksonAdapter();

                        try {
                            String serializedObject = serializer.serialize(runtimeClaims.get(key), SerializerEncoding.JSON);
                            System.out.printf("%s", serializedObject);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    }

    /**
     * Attest evidence from an OpenEnclave enclave when the "runtime data" (data included in the
     * OpenEnclave report) should be interpreted as binary data.
     */
    static void attestSgxEnclaveBinary() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Note that the "attest" calls do not require authentication.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();

        //BEGIN: readme-sample-attest-sgx-enclave
        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        // Attest evidence from an OpenEnclave enclave specifying runtime data which should be
        // interpreted as binary data.
        AttestationResult result = client.attestSgxEnclave(new AttestationOptions(sgxQuote)
            .setRunTimeData(
                new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY)));

        String issuer = result.getIssuer();

        System.out.println("Attest Sgx Enclave completed. Issuer: " + issuer);
        System.out.printf("Runtime Data Length: %d\n", result.getEnclaveHeldData().getLength());
        //END: readme-sample-attest-sgx-enclave
    }

    /**
     * Attest evidence from an OpenEnclave enclave when the "runtime data" (data included in the
     * OpenEnclave report) should be interpreted as json data.
     */
    static void attestSgxEnclaveJson() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Note that the "attest" calls do not require authentication.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        // Attest evidence from an OpenEnclave enclave specifying runtime data which should be
        // interpreted as binary data.
        AttestationResult result = client.attestSgxEnclave(new AttestationOptions(sgxQuote)
            .setRunTimeData(
                new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON)));

        String issuer = result.getIssuer();
        System.out.println("Attest Sgx Enclave completed. Issuer: " + issuer);

        // Now dump the returned set of runtime claims. The MAA service has verified that the
        // claims in the resulting token all were generated within the enclave.
        if (result.getRuntimeClaims() instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> runtimeClaims = (LinkedHashMap) result.getRuntimeClaims();
            for (String key : runtimeClaims.keySet()) {
                System.out.printf("Claim %s:", key);
                JacksonAdapter serializer = new JacksonAdapter();

                try {
                    String serializedObject = serializer.serialize(runtimeClaims.get(key), SerializerEncoding.JSON);
                    System.out.printf("%s\n", serializedObject);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * The attestation service enables a mechanism which can be used by customers to test
     * attestation policy documents before they are set on a live instance.
     */
    static void attestSgxEnclaveWithBadDraftPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Note that the "attest" calls do not require authentication.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        // Attest with a draft policy. THis policy document will always fail a request, so catch
        // the exception
        try {
            AttestationResult result = client.attestSgxEnclave(new AttestationOptions(sgxQuote)
                .setRunTimeData(
                    new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY))
                .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> deny();}; issuancerules{};"));
        } catch (HttpResponseException e) {
            if (e.getValue() instanceof ResponseError) {
                ResponseError response = (ResponseError) e.getValue();
                if (!response.getCode().equals("PolicyEvaluationError")) {
                    e.printStackTrace();

                }
                System.out.println("Received expected policy evaluation error.");
            } else {
                e.printStackTrace();
            }
        }
    }
    /**
     * The attestation service enables a mechanism which can be used by customers to test
     * attestation policy documents before they are set on a live instance.
     */
    static void attestSgxEnclaveWithDraftPolicy() {
        String endpoint = "https://sharedcuse.cuse.attest.azure.net";
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Note that the "attest" calls do not require authentication.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .tokenValidationOptions(new AttestationTokenValidationOptions().setValidationSlack(Duration.ofSeconds(10)))
            .buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(SampleCollateral.getRunTimeData());
        BinaryData sgxQuote = BinaryData.fromBytes(SampleCollateral.getSgxEnclaveQuote());

        // Attest with a draft policy. THis policy document will always fail a request, so catch
        // the exception
        Response<AttestationResult> response = client.attestSgxEnclaveWithResponse(new AttestationOptions(sgxQuote)
                .setRunTimeData(
                    new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY))
                .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{ => issue(type=\"ClaimType\", value=\"SgxClaim\");};"),
            Context.NONE);
        String issuer = response.getValue().getIssuer();

        // The attestation policy emits a claim named "ClaimType" with a value of "SgxClaim".
        // Print out the policy generated claim names.
        if (response.getValue().getPolicyClaims() instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> policyClaims = (LinkedHashMap) response.getValue().getPolicyClaims();
            for (String key : policyClaims.keySet()) {
                System.out.printf("Policy Generated Claim %s:", key);
                JacksonAdapter serializer = new JacksonAdapter();

                try {
                    String serializedObject = serializer.serialize(policyClaims.get(key), SerializerEncoding.JSON);
                    System.out.printf("%s\n", serializedObject);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static void attestTpmInitialExchange() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Set the TPM attestation policy to a default value.
        AttestationAdministrationClient adminClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM,
            "version=1.0; authorizationrules{=>permit();};issuancerules{};");

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            System.out.printf("Unexpected resolution setting TPM policy: %s", result.getPolicyResolution().toString());
            return;
        }

        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .tokenValidationOptions(new AttestationTokenValidationOptions().setValidationSlack(Duration.ofSeconds(10)))
            .buildClient();


        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.

        // BEGIN: com.azure.security.attestation.AttestationClient.attestTpm
        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        String tpmResponse = client.attestTpm(attestInitialPayload);
        // END: com.azure.security.attestation.AttestationClient.attestTpm
    }

    static void attestTpmInitialExchangeWithResponse() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Set the TPM attestation policy to a default value.
        AttestationAdministrationClient adminClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM,
            "version=1.0; authorizationrules{=>permit();};issuancerules{};");

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            System.out.printf("Unexpected resolution setting TPM policy: %s", result.getPolicyResolution().toString());
            return;
        }

        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .tokenValidationOptions(new AttestationTokenValidationOptions().setValidationSlack(Duration.ofSeconds(10)))
            .buildClient();


        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.

        // BEGIN: com.azure.security.attestation.AttestationClient.attestTpmWithResponse
        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        Response<String> tpmResponse = client.attestTpmWithResponse(attestInitialPayload, Context.NONE);
        // END: com.azure.security.attestation.AttestationClient.attestTpmWithResponse
    }

    static void attestTpmInitialExchangeAsync() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Set the TPM attestation policy to a default value.
        AttestationAdministrationClient adminClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM,
            "version=1.0; authorizationrules{=>permit();};issuancerules{};");

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            System.out.printf("Unexpected resolution setting TPM policy: %s", result.getPolicyResolution().toString());
            return;
        }

        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .tokenValidationOptions(new AttestationTokenValidationOptions().setValidationSlack(Duration.ofSeconds(10)))
            .buildAsyncClient();


        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestTpm
        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        Mono<String> tpmResponse = client.attestTpm(attestInitialPayload);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestTpm
        tpmResponse.subscribe();
    }

    static void attestTpmInitialExchangeWithResponseAsync() {

        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationClientBuilder attestationBuilder = new AttestationClientBuilder();
        // Set the TPM attestation policy to a default value.
        AttestationAdministrationClient adminClient = new AttestationAdministrationClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM,
            "version=1.0; authorizationrules{=>permit();};issuancerules{};");

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            System.out.printf("Unexpected resolution setting TPM policy: %s", result.getPolicyResolution().toString());
            return;
        }

        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .tokenValidationOptions(new AttestationTokenValidationOptions().setValidationSlack(Duration.ofSeconds(10)))
            .buildAsyncClient();


        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.

        // BEGIN: com.azure.security.attestation.AttestationAsyncClient.attestTpmWithResponse
        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        Mono<Response<String>> responseMono = client.attestTpmWithResponse(attestInitialPayload);
        // END: com.azure.security.attestation.AttestationAsyncClient.attestTpmWithResponse
        responseMono.subscribe();
    }

    static void executeSamples() {
        attestOpenEnclaveBinary();
        attestOpenEnclaveJson();
        attestSgxEnclaveBinary();
        attestSgxEnclaveJson();
        attestSgxEnclaveWithBadDraftPolicy();
        attestSgxEnclaveWithDraftPolicy();
        attestTpmInitialExchange();
        attestTpmInitialExchangeWithResponse();
        attestTpmInitialExchangeAsync();
        attestTpmInitialExchangeWithResponseAsync();
    }
}
