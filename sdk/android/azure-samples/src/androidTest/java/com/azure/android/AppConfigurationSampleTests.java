package com.azure.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.azure.android.appconfiguration.ConditionalRequestAsync;
import com.azure.android.appconfiguration.HelloWorld;
import com.azure.android.appconfiguration.SecretReferenceConfigurationSettingSample;
import com.azure.android.appconfiguration.WatchFeature;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

@RunWith(AndroidJUnit4.class)
public class AppConfigurationSampleTests {
    final String appconfigEndpoint = "https://android-app-configuration.azconfig.io";
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
            HelloWorld.main(appconfigEndpoint, clientSecretCredential);
        } catch (RuntimeException e) {
            fail();
        }
    }

    @Test
    public void watchFeature() {
        try {
            WatchFeature.main(appconfigEndpoint, clientSecretCredential);
        } catch (RuntimeException e) {
            fail();
        }
    }

    @Test
    public void secretReferenceConfigurationSettingSample() {
        try {
            SecretReferenceConfigurationSettingSample.main(appconfigEndpoint, clientSecretCredential);
        } catch (RuntimeException e) {
            fail();
        }
    }

    @Test
    public void readOnly() {
        try {
            ReadOnly.main(appconfigEndpoint, clientSecretCredential);
        } catch (RuntimeException e) {
            fail();
        }
    }


    @Test
    public void conditionalRequestAsync() {
        try {
            ConditionalRequestAsync.main(appconfigEndpoint, clientSecretCredential);
        } catch (RuntimeException | InterruptedException e) {
            fail();
        }
    }

}
