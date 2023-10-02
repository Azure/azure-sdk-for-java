package com.azure.android.samples;

import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.BuildConfig;
import com.azure.android.keyvault.keys.HelloWorldKeyvaultKeys;
import com.azure.android.keyvault.keys.KeyRotationAsyncKeyvaultKeys;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class KeyvaultKeysTests {
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
            HelloWorldKeyvaultKeys.main(keyvaultEndpoint, clientSecretCredential);
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    public void keyRotationAsync() {
        try {
            KeyRotationAsyncKeyvaultKeys.main(keyvaultEndpoint, clientSecretCredential);
        } catch (InterruptedException e) {
            fail();
        }
    }

    /* commented out pending key-id being obtained to put in this
    @Test
    public void keyWrapUnwrapOperations() {
        try {
            KeyWrapUnwrapOperationsKeyvaultKeys.main(keyvaultEndpoint, clientSecretCredential);
        } catch (InterruptedException e) {
            fail();
        }

    }
    */
}
