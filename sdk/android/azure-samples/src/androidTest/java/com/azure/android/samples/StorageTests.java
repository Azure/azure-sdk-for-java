package com.azure.android.samples;

import static org.junit.Assert.fail;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.BuildConfig;
import com.azure.android.storage.BasicExample;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class StorageTests {
    final String storageAccountName = "androidazsdkstorage";

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
    public void basicExample() {
        try {
            BasicExample.main(storageAccountName, clientSecretCredential);
        } catch (RuntimeException | IOException e) {
            fail();
        }
    }
}
