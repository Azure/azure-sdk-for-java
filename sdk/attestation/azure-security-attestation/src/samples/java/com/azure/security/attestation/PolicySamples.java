// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResult;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;

public class PolicySamples {

    public static void getCurrentPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");

        //BEGIN: readme-sample-create-admin-client
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        //END: readme-sample-create-admin-client

        // BEGIN: readme-sample-getCurrentPolicy
        String currentPolicy = client.getAttestationPolicy(AttestationType.OPEN_ENCLAVE);
        System.out.printf("Current policy for OpenEnclave is: %s\n", currentPolicy);
        // END: readme-sample-getCurrentPolicy

    }

    public static void getCurrentPolicyAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // BEGIN: readme-sample-getCurrentPolicyAsync
        client.getAttestationPolicy(AttestationType.OPEN_ENCLAVE)
                .subscribe(policy -> System.out.printf("Current policy for OpenEnclave is: %s\n", policy));
        // END: readme-sample-getCurrentPolicyAsync

    }

    public static void setCurrentPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: readme-sample-set-unsigned-policy
        // Set the listed policy on an attestation instance. Please note that this particular policy will deny all
        // attestation requests and should not be used in production.
        PolicyResult policyResult = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, "version=1.0; authorizationrules{=> deny();}; issuancerules{};");
        System.out.printf("Policy set for OpenEnclave result: %s\n", policyResult.getPolicyResolution());
        // END: readme-sample-set-unsigned-policy
    }

    public static void setCurrentPolicyAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        System.out.println("Setting an async policy");
        // BEGIN: readme-sample-set-unsigned-policy-async
        // Set the listed policy on an attestation instance. Please note that this particular policy will deny all
        // attestation requests and should not be used in production.
        client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, "version=1.0; authorizationrules{=> deny();}; issuancerules{};")
                .subscribe(result -> System.out.printf("Async policy set for OpenEnclave result: %s\n", result.getPolicyResolution()));
        // END: readme-sample-set-unsigned-policy-async
    }

    public static void setSignedPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        X509Certificate certificate = SampleCollateral.getSigningCertificate();
        PrivateKey privateKey = SampleCollateral.getSigningKey();

        // BEGIN: readme-sample-set-signed-policy
        // Set the listed policy on an attestation instance using a signed policy token.
        PolicyResult policyResult = client.setAttestationPolicy(AttestationType.SGX_ENCLAVE,
            new AttestationPolicySetOptions()
                .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                    .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)));
        System.out.printf("Policy set for Sgx result: %s\n", policyResult.getPolicyResolution());
        // END: readme-sample-set-signed-policy
    }

    public static void setSignedPolicyAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        X509Certificate certificate = SampleCollateral.getSigningCertificate();
        PrivateKey privateKey = SampleCollateral.getSigningKey();


        System.out.println("Setting an async policy");
        // BEGIN: readme-sample-set-signed-policy-async
        // Set the listed policy on an attestation instance. Please note that this particular policy will deny all
        // attestation requests and should not be used in production.
        client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, new AttestationPolicySetOptions()
                .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)))
            .subscribe(result -> System.out.printf("Async policy set for OpenEnclave result: %s\n", result.getPolicyResolution()));
        // END: readme-sample-set-signed-policy-async
    }


    public static void resetCurrentPolicy() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder()
            .tokenValidationOptions(new AttestationTokenValidationOptions()
                .setValidationSlack(Duration.ofSeconds(10)));
        // Note that the "policy" calls require authentication.
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: readme-sample-resetCurrentPolicy
        PolicyResult policyResult = client.resetAttestationPolicy(AttestationType.OPEN_ENCLAVE);
        System.out.printf("Policy Reset for OpenEnclave result: %s\n", policyResult.getPolicyResolution());
        // END: readme-sample-resetCurrentPolicy
    }

    public static void resetCurrentPolicyAsync() {
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        //BEGIN: readme-sample-create-async-admin-client
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        //END: readme-sample-create-async-admin-client

        // BEGIN: readme-sample-resetCurrentPolicyAsync
        client.resetAttestationPolicy(AttestationType.OPEN_ENCLAVE)
                .subscribe(result -> System.out.printf("Policy Reset for OpenEnclave result: %s\n", result.getPolicyResolution()));

        // END: readme-sample-resetCurrentPolicyAsync
    }


    static void executeSamples() {
        getCurrentPolicy();
        getCurrentPolicyAsync();
        setCurrentPolicy();
        setCurrentPolicyAsync();
        setSignedPolicy();
        setSignedPolicyAsync();
        resetCurrentPolicy();
        resetCurrentPolicyAsync();
    }

}
