package com.azure.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.servicebus.PeekMessageAsync;
import com.azure.android.servicebus.RecieveMessage;
import com.azure.android.servicebus.SendMessageBatch;
import com.azure.android.servicebus.SendSessionMessageAsync;
import com.azure.android.servicebus.ServiceBusSessionProcessor;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AppConfigurationSampleTests {
    final String serviceBusQueueName= "android-sb-queue";
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
    public void peekMessageAsync() {
        try {
            PeekMessageAsync.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | IOException e) {
            fail();
        }
    }

    @Test
    public void receiveMessage() {
        try {
            ReceiveMessage.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | IOException e) {
            fail();
        }
    }

    @Test
    public void sendMessageBatch() {
        try {
            SendMessageBatch.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | IOException e) {
            fail();
        }
    }

    @Test
    public void sendSessionMessageAsync() {
        try {
            SendSessionMessageAsync.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | IOException e) {
            fail();
        }
    }

    @Test
    public void serviceBusSessionProcessor() {
        try {
            ServiceBusSessionProcessor.main(serviceBusQueueName, clientSecretCredential);
        } catch (RuntimeException | IOException e) {
            fail();
        }
    }

}
