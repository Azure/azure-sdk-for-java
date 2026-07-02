// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrating the two authentication methods supported by the VoiceLive client builder.
 *
 * <p><strong>This sample only constructs clients to illustrate the credential APIs — it does not
 * open a session or send any traffic.</strong> For an end-to-end conversation, see
 * {@link BasicVoiceConversationSample}; the rest of the samples in this package all use
 * {@link DefaultAzureCredentialBuilder}.</p>
 *
 * <p>The VoiceLive client builder supports two credential types:</p>
 * <ol>
 *   <li><strong>{@link TokenCredential} (recommended)</strong> — Microsoft Entra ID. Use
 *       {@link DefaultAzureCredentialBuilder} so the same code works for managed identity,
 *       environment variables, the Azure CLI, Visual Studio Code, etc.</li>
 *   <li><strong>{@link KeyCredential}</strong> — A static API key. Convenient for local
 *       experimentation; not recommended for production because keys cannot be rotated, scoped,
 *       or audited per-user.</li>
 * </ol>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 *   <li>{@code AZURE_VOICELIVE_ENDPOINT} - (Required) The VoiceLive service endpoint URL</li>
 *   <li>{@code AZURE_VOICELIVE_API_KEY} - (Required only for the API-key example) The API key</li>
 * </ul>
 *
 * <p><strong>Command Line Arguments:</strong></p>
 * <ul>
 *   <li>{@code --use-api-key} - Build the client with {@link KeyCredential} (reads
 *       {@code AZURE_VOICELIVE_API_KEY}). Without this flag, the sample uses
 *       {@link DefaultAzureCredentialBuilder} (Entra ID).</li>
 * </ul>
 *
 * <p><strong>How to Run:</strong></p>
 * <p>With Entra ID (default):</p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.AuthenticationMethodsSample" -Dexec.classpathScope=test
 * }</pre>
 * <p>With API key:</p>
 * <pre>{@code
 * export AZURE_VOICELIVE_API_KEY="<your-key>"
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.AuthenticationMethodsSample" -Dexec.classpathScope=test -Dexec.args="--use-api-key"
 * }</pre>
 */
public final class AuthenticationMethodsSample {

    private AuthenticationMethodsSample() {
    }

    /**
     * Main entry point. Builds a {@link VoiceLiveAsyncClient} with either {@link KeyCredential} or
     * {@link DefaultAzureCredentialBuilder}, selected via the {@code --use-api-key} command line flag.
     *
     * @param args Pass {@code --use-api-key} to build with an API key; otherwise the sample uses
     *             {@code DefaultAzureCredential} (Entra ID).
     */
    public static void main(String[] args) {
        boolean useApiKey = false;
        for (String arg : args) {
            if ("--use-api-key".equals(arg)) {
                useApiKey = true;
            }
        }

        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        if (endpoint == null || endpoint.isEmpty()) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT environment variable");
            return;
        }

        if (useApiKey) {
            String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("AZURE_VOICELIVE_API_KEY environment variable is required when --use-api-key is passed");
                return;
            }
            createClientWithApiKey(endpoint, apiKey);
        } else {
            createClientWithDefaultAzureCredential(endpoint);
        }
    }

    /**
     * Build a {@link VoiceLiveAsyncClient} with {@link DefaultAzureCredentialBuilder} (Entra ID).
     *
     * <p>{@code DefaultAzureCredential} tries a chain of credential sources in order: environment
     * variables, workload identity, managed identity, the Azure CLI, Azure PowerShell, and so on.
     * The same code therefore works in production (managed identity) and locally (after
     * {@code az login}) without changes.</p>
     */
    private static void createClientWithDefaultAzureCredential(String endpoint) {
        // BEGIN: com.azure.ai.voicelive.AuthenticationMethodsSample.tokenCredential
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.ai.voicelive.AuthenticationMethodsSample.tokenCredential

        System.out.println("✓ Created VoiceLiveAsyncClient using DefaultAzureCredential: " + client);
    }

    /**
     * Build a {@link VoiceLiveAsyncClient} with a static API key ({@link KeyCredential}).
     *
     * <p>API keys are convenient for quick local experiments but should be avoided in production:
     * they cannot be scoped per-user, are hard to rotate, and grant full access to the resource.
     * Prefer {@code DefaultAzureCredential} whenever possible.</p>
     */
    private static void createClientWithApiKey(String endpoint, String apiKey) {
        // BEGIN: com.azure.ai.voicelive.AuthenticationMethodsSample.keyCredential
        KeyCredential credential = new KeyCredential(apiKey);

        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.ai.voicelive.AuthenticationMethodsSample.keyCredential

        System.out.println("✓ Created VoiceLiveAsyncClient using KeyCredential: " + client);
    }
}
