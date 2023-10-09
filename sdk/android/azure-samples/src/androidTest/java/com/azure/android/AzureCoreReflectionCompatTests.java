package com.azure.android;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.android.BuildConfig;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test cases for ReflectionUtils usages.
 * At Azure Core versions < 1.44, the old ReflectionUtils would cause ExceptionInInitializerErrors
 * at APIs > 27. The new version of ReflectionUtils resolves this issue.
 */
@RunWith(AndroidJUnit4.class)
public class AzureCoreReflectionCompatTests {
    ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(BuildConfig.AZURE_CLIENT_ID)
            .clientSecret(BuildConfig.AZURE_CLIENT_SECRET)
            .tenantId(BuildConfig.AZURE_TENANT_ID)
            .build();

    // Testing issue 35756, https://github.com/Azure/azure-sdk-for-java/issues/35756
    @Test
    public void aiTranslationTest() {
        String region = "<redacted>";
        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(clientSecretCredential)
                .region(region)
                .buildClient();
        assertNotNull(client);
    }

    // Testing issue 35719, https://github.com/Azure/azure-sdk-for-java/issues/35719
    @Test
    public void aiTest() {
        String AZURE_OPEN_AI_ENDPOINT = "<redacted>";

        OpenAIClient client = new OpenAIClientBuilder()
                .credential(clientSecretCredential)
                .endpoint(AZURE_OPEN_AI_ENDPOINT)
                .buildClient();
        assertNotNull(client);
    }
}
