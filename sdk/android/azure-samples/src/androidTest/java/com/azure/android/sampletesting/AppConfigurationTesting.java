package com.azure.android.sampletesting;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.azure.android.BuildConfig;
import com.azure.android.appconfiguration.ConditionalRequestAsync;
import com.azure.android.appconfiguration.HelloWorld;
import com.azure.android.appconfiguration.SecretReferenceConfigurationSettingSample;
import com.azure.android.appconfiguration.WatchFeature;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

@RunWith(AndroidJUnit4.class)
public class AppConfigurationTesting {
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
        HelloWorld.main(appconfigEndpoint, clientSecretCredential);
    }

    @Test
    public void watchFeature() {
        WatchFeature.main(appconfigEndpoint, clientSecretCredential);
    }

    @Test
    public void secretReferenceConfigurationSettingSample() {
        SecretReferenceConfigurationSettingSample.main(appconfigEndpoint, clientSecretCredential);
    }

    /*
        I've been unable to get the dependencies for CreateSnapshot, I suspect they're in the preview version
        of the SDK. Once that is figured out this test should be able to be safely uncommented
     */
//    @Test
//    public void createSnapshot() {
//        CreateSnapshot.main(appconfigEndpoint, clientSecretCredential);
//    }

    @Test
    public void conditionalRequestAsync() {
        try {
            ConditionalRequestAsync.main(appconfigEndpoint, clientSecretCredential);
        } catch (InterruptedException e) {
            fail();
        }
    }

}
