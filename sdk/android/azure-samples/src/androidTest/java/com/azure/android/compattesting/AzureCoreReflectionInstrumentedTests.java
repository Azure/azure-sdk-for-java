package com.azure.android.compattesting;

//import static com.azure.core.implementation.ReflectionUtils.getConstructorInvoker;
//import com.azure.core.implementation.ReflectionUtils;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.translation.text.TextTranslationClient;
import com.azure.ai.translation.text.TextTranslationClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

import com.azure.identity.ClientSecretCredentialBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test cases for ReflectionUtils usages.
 * When an emulator's API and an app's targetSdk are both greater than 27, ExceptionInInitializer
 * would occur because of the constructor for MethodHandles.Lookup being missing. These three test
 * cases replicated user-reported cases where ExceptionInInitializer would occur, and were used to
 * test if a new PR could resolve these issues by providing two different ReflectionUtil versions.
 */
@RunWith(AndroidJUnit4.class)
public class AzureCoreReflectionInstrumentedTests {
    // Tests were run with placeholder credentials but did trigger ExceptionInInitializerError.
    /* Testing issue 1540 at https://github.com/microsoftgraph/msgraph-sdk-java/issues/1540.
     * Results for current azure-core version (1.42.0):
     * - API level = 26, 27: IllegalArgumentException (invalid TENANT_ID)
     * - API level > 27: ExceptionInInitializerError (MethodHandles.Lookup)
     *
     * Results for PR version:
     * - API level >= 26: IllegalArgumentException (invalid TENANT_ID)
     * This indicates that the PR has managed to resolve the ExceptionInInitializerError.
     */
    @Test
    public void identityTest() {
        // Context of the app under test.
        String TENANT_ID = "<redacted>";
        String CLIENT_ID = "<redacted>";
        String CLIENT_SECRET = "<redacted>";

        assertThrows(IllegalArgumentException.class, () -> {
            new ClientSecretCredentialBuilder().clientId(CLIENT_ID).tenantId(TENANT_ID)
                    .clientSecret(CLIENT_SECRET).build();
        });
    }

    /* Testing issue 35756, https://github.com/Azure/azure-sdk-for-java/issues/35756
     * Results for current azure-core version (1.42.0):
     * - API level = 26, 27: no errors thrown
     * - API level > 27: ExceptionInInitializerError thrown
     *
     * Results for PR version:
     * - API >= 26: NoClassDefFoundError, missing dependencies that are already in the pom.xml.
     * This NoClassDefFoundError occurs about 30 lines after the ExceptionInInitializerError in
     * the createHttpPipeline() method of TextTranslationClientBuilder, so that indicates the PR has
     * resolved the issue.
     */
    @Test
    public void aiTranslationTest() {
        String credential = "<redacted>";
        String region = "<redacted>";
        TextTranslationClient client = new TextTranslationClientBuilder()
                .credential(new AzureKeyCredential(credential))
                .region(region)
                .buildClient();
        assertNotNull(client);
    }

    /* Testing issue 35719, https://github.com/Azure/azure-sdk-for-java/issues/35719
     * Results for current azure-core version (1.42.0):
     * Emulator and targetSdk API level = 26 or 27: no errors thrown
     * Emulator and targetSdk API level > 27: ExceptionInInitializerError thrown.
     *
     * Results for PR version:
     * NoClassDefFoundError for API >= 26, missing dependencies like Netty that are in the pom.xml.
     * Tracing the logs, this error appears to occur further along than the
     * ExceptionInInitializerError in the buildClient() of OpenAIClientBuilder, which seems to
     * indicate that it's resolved the issue.
     */
    @Test
    public void aiTest() {
        String AZURE_OPEN_AI_KEY = "<redacted>";
        String AZURE_OPEN_AI_ENDPOINT = "<redacted>";

        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_OPEN_AI_KEY))
                .endpoint(AZURE_OPEN_AI_ENDPOINT)
                .buildClient();
        assertNotNull(client);
    }
}
