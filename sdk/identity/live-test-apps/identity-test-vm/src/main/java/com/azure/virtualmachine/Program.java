package com.azure.virtualmachine;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.credential.AccessToken;

/**
 * Program to fetch token from Managed Identity Credential.
 */
public class Program {

    public static void main(String[] args) {
        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder().build();
        try {
            AccessToken accessToken = managedIdentityCredential
                .getTokenSync(new TokenRequestContext().addScopes("https://management.azure.com/.default"));
            System.out.println("Successfully retrieved managed identity tokens");
        } catch (Exception ex) {
            System.out.println("Failed to acquire a token from VM ManagedIdentityCredential");
        }
    }
}
