package com.azure;

import com.azure.identity.WorkloadIdentityCredential;
import com.azure.identity.WorkloadIdentityCredentialBuilder;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.credential.AccessToken;

/**
 * Program to fetch token from Workload Identity Credential.
 */
public class Program {

    public static void main(String[] args) {
        WorkloadIdentityCredential workloadIdentityCredential = new WorkloadIdentityCredentialBuilder().build();
        try {
            AccessToken accessToken = workloadIdentityCredential
                .getTokenSync(new TokenRequestContext().addScopes("https://management.azure.com/.default"));
            System.out.println("Successfully retrieved managed identity tokens");
        } catch (Exception ex) {
            System.out.println("Failed to acquire a token from WorkloadIdentityCredential");
        }
    }
}
