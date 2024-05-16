// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationSignerCollection;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.PolicyCertificatesModificationResult;
import com.azure.security.attestation.models.PolicyManagementCertificateOptions;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class PolicyManagementCertificatesSamples {

    /**
     * Enumerate the current set of policy management certificates for the specified attestation instance.
     */
    public static void listPolicyCertificates() {
        String endpoint = System.getenv("ATTESTATION_ISOLATED_URL");

        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // BEGIN: readme-sample-listPolicyCertificates
        AttestationSignerCollection signers = client.listPolicyManagementCertificates();
        System.out.printf("Instance %s contains %d signers.\n", endpoint, signers.getAttestationSigners().size());
        for (AttestationSigner signer : signers.getAttestationSigners()) {
            System.out.printf("Certificate Subject: %s", signer.getCertificates().get(0).getSubjectDN().toString());
        }
        // END: readme-sample-listPolicyCertificates

    }


    public static void listPolicyCertificatesAsync() {
        String endpoint = System.getenv("ATTESTATION_ISOLATED_URL");

        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        client.listPolicyManagementCertificates()
                .subscribe(signers -> {
                    System.out.printf("Instance %s contains %d signers.\n", endpoint, signers.getAttestationSigners().size());
                    for (AttestationSigner signer : signers.getAttestationSigners()) {
                        System.out.printf("Certificate Subject: %s\n", signer.getCertificates().get(0).getSubjectDN());
                    }
                });
    }


    /**
     * Add a new certificate to the list of policy management certificates.
     */
    public static void addPolicyManagementCertificate() {
        String endpoint = System.getenv("ATTESTATION_ISOLATED_URL");

        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        X509Certificate certificateToAdd = SampleCollateral.getSigningCertificate();
        PrivateKey isolatedKey = SampleCollateral.getIsolatedSigningKey();
        X509Certificate isolatedCertificate = SampleCollateral.getIsolatedSigningCertificate();

        // BEGIN: readme-sample-addPolicyManagementCertificate
        System.out.printf("Adding new certificate %s\n", certificateToAdd.getSubjectDN().toString());
        PolicyCertificatesModificationResult modificationResult = client.addPolicyManagementCertificate(
            new PolicyManagementCertificateOptions(certificateToAdd,
                new AttestationSigningKey(isolatedCertificate, isolatedKey)));
        System.out.printf("Updated policy certificate, certificate add result: %s\n",
            modificationResult.getCertificateResolution());
        System.out.printf("Added certificate thumbprint: %s\n", modificationResult.getCertificateThumbprint());
        // END: readme-sample-addPolicyManagementCertificate

        // Dump the list of policy certificates.
        listPolicyCertificates();
    }

    public static void addPolicyManagementCertificateAsync() {
        String endpoint = System.getenv("ATTESTATION_ISOLATED_URL");

        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        X509Certificate certificateToAdd = SampleCollateral.getSigningCertificate();
        PrivateKey isolatedKey = SampleCollateral.getIsolatedSigningKey();
        X509Certificate isolatedCertificate = SampleCollateral.getIsolatedSigningCertificate();

        // Note: It is not an error to add the same certificate twice. The second addition is ignored.
        System.out.printf("Adding new certificate %s\n", certificateToAdd.getSubjectDN().toString());
        client.addPolicyManagementCertificate(new PolicyManagementCertificateOptions(certificateToAdd,
                new AttestationSigningKey(isolatedCertificate, isolatedKey)))
            .subscribe(modificationResult -> {
                System.out.printf("Updated policy certificate, certificate add result: %s\n",
                    modificationResult.getCertificateResolution());
                System.out.printf("Added certificate thumbprint: %s\n", modificationResult.getCertificateThumbprint());
            });
        // Dump the list of policy certificates.
        listPolicyCertificates();
    }

    /**
     * Add a new certificate to the list of policy management certificates.
     */
    public static void removePolicyManagementCertificate() {
        String endpoint = System.getenv("ATTESTATION_ISOLATED_URL");

        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        AttestationAdministrationClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        X509Certificate certificateToRemove = SampleCollateral.getSigningCertificate();
        PrivateKey isolatedKey = SampleCollateral.getIsolatedSigningKey();
        X509Certificate isolatedCertificate = SampleCollateral.getIsolatedSigningCertificate();

        // BEGIN: readme-sample-removePolicyManagementCertificate
        System.out.printf("Removing existing certificate %s\n", certificateToRemove.getSubjectDN().toString());
        PolicyCertificatesModificationResult modificationResult = client.deletePolicyManagementCertificate(
            new PolicyManagementCertificateOptions(certificateToRemove,
                new AttestationSigningKey(isolatedCertificate, isolatedKey)));
        System.out.printf("Updated policy certificate, certificate remove result: %s\n",
            modificationResult.getCertificateResolution());
        System.out.printf("Removed certificate thumbprint: %s\n", modificationResult.getCertificateThumbprint());
        // END: readme-sample-removePolicyManagementCertificate

        // Dump the list of policy certificates.
        listPolicyCertificates();
    }

    public static void removePolicyManagementCertificateAsync() {
        String endpoint = System.getenv("ATTESTATION_ISOLATED_URL");

        AttestationAdministrationClientBuilder attestationBuilder = new AttestationAdministrationClientBuilder();
        AttestationAdministrationAsyncClient client = attestationBuilder
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        X509Certificate certificateToRemove = SampleCollateral.getSigningCertificate();
        PrivateKey isolatedKey = SampleCollateral.getIsolatedSigningKey();
        X509Certificate isolatedCertificate = SampleCollateral.getIsolatedSigningCertificate();

        // Note: It is not an error to remove a non-existent certificate. The second removal is ignored.
        System.out.printf("Removing an existing certificate %s\n", certificateToRemove.getSubjectDN().toString());
        client.deletePolicyManagementCertificate(new PolicyManagementCertificateOptions(certificateToRemove,
                new AttestationSigningKey(isolatedCertificate, isolatedKey)))
            .subscribe(modificationResult -> {
                System.out.printf("Updated policy certificate, certificate removal result: %s\n",
                    modificationResult.getCertificateResolution());
                System.out.printf("Removed certificate thumbprint: %s\n", modificationResult.getCertificateThumbprint());
            });

        // Dump the list of policy certificates.
        listPolicyCertificates();
    }


    static void executeSamples() {
        listPolicyCertificates();
        listPolicyCertificatesAsync();
        addPolicyManagementCertificate();
        addPolicyManagementCertificateAsync();
        removePolicyManagementCertificate();
        removePolicyManagementCertificateAsync();
    }
}
