package com.azure.android;

import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.keyvault.secrets.HelloWorldKeyvaultSecrets;
import com.azure.android.keyvault.secrets.ListOperationsAsyncKeyvaultSecrets;
import com.azure.android.keyvault.secrets.ManagingDeletedSecretsKeyvaultSecrets;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KeyvaultSecretsSampleTests {
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
            HelloWorldKeyvaultSecrets.main(keyvaultEndpoint, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail();
        }
    }
    @Test
    public void listOperationsAsync() {
        try {
            ListOperationsAsyncKeyvaultSecrets.main(keyvaultEndpoint, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail();
        }
    }
    @Test
    public void managingDeletedSecrets() {
        try {
            ManagingDeletedSecretsKeyvaultSecrets.main(keyvaultEndpoint, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail();
        }
    }
}
