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

        client.getAttestationPolicy(AttestationType.OPEN_ENCLAVE)
                .subscribe(policy -> System.out.printf("Current policy for OpenEnclave is: %s\n", policy));
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
        PolicyResult policyResult = client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE,
            "version=1.0; authorizationrules{=> deny();}; issuancerules{};");
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
        // Set the listed policy on an attestation instance. Please note that this particular policy will deny all
        // attestation requests and should not be used in production.
        client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE,
            "version=1.0; authorizationrules{=> deny();}; issuancerules{};")
            .subscribe(result -> System.out.printf("Async policy set for OpenEnclave result: %s\n",
                result.getPolicyResolution()));
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
        // Set the listed policy on an attestation instance. Please note that this particular policy will deny all
        // attestation requests and should not be used in production.
        client.setAttestationPolicy(AttestationType.OPEN_ENCLAVE, new AttestationPolicySetOptions()
                .setAttestationPolicy("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
                .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)))
            .subscribe(result -> System.out.printf("Async policy set for OpenEnclave result: %s\n",
                result.getPolicyResolution()));
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
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        // Note that the "policy" calls require authentication.
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        client.resetAttestationPolicy(AttestationType.OPEN_ENCLAVE)
                .subscribe(result -> System.out.printf("Policy Reset for OpenEnclave result: %s\n",
                    result.getPolicyResolution()));

    }

    /**
     * Reset all attestation policies to their default value, for both the AAD and Isolated instance.
     */
    public static void resetAllPolicies() {
        System.out.println("Reset all attestation policies.\n");
        String endpoint = System.getenv("ATTESTATION_AAD_URL");
        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        X509Certificate certificate = SampleCollateral.getIsolatedSigningCertificate();
        PrivateKey privateKey = SampleCollateral.getIsolatedSigningKey();

        // Note that the "policy" calls require authentication.
        AttestationAdministrationClient aadClient = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        endpoint = System.getenv("ATTESTATION_ISOLATED_URL");
        AttestationAdministrationClient isolatedClient = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        for (AttestationType attestationType : AttestationType.values()) {
            System.out.printf("Reset AAD attestation policy %s\n", attestationType.toString());
            aadClient.resetAttestationPolicy(attestationType);

            System.out.printf("Reset Isolated attestation policy %s\n", attestationType.toString());
            isolatedClient.resetAttestationPolicy(attestationType,
                new AttestationPolicySetOptions()
                    .setAttestationSigner(new AttestationSigningKey(certificate, privateKey)));
        }
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
