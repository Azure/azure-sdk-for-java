package com.azure.android;

import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.keyvault.certificates.HelloWorldKeyvaultCertificates;
import com.azure.android.keyvault.certificates.ListOperationsKeyvaultCertificates;
import com.azure.android.keyvault.certificates.ManagingDeletedCertificatesAsyncKeyvaultCertificates;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KeyvaultCertificatesSampleTests {
    final String keyvaultEndpoint = "https://android-key-vault.vault.azure.net/";

    ClientSecretCredential clientSecretCredential;
    @Before
    public void setup() {
        // These are obtained by setting system environment variables
        // on the computer emulating the app
        clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(BuildConfig.AZURE_CLIENT_ID)
            .clientSecret(BuildConfig.AZURE_CLIENT_SECRET)
            .tenantId(BuildConfig.AZURE_TENANT_ID)
            .build();
    }

    @Test
    public void helloWorld() {
        try {
            HelloWorldKeyvaultCertificates.main(keyvaultEndpoint, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail();
        }
    }
    @Test
    public void listOperations() {
        try {
            ListOperationsKeyvaultCertificates.main(keyvaultEndpoint, clientSecretCredential);
        } catch (RuntimeException e) {
            fail();
        }
    }
    @Test
    public void managingDeletedCertificatesAsync() {
        try {
            ManagingDeletedCertificatesAsyncKeyvaultCertificates.main(keyvaultEndpoint, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail();
        }
    }
}
