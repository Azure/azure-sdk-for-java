// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample demonstrating different authentication methods for VoiceLive service.
 *
 * <p>This sample shows two authentication approaches:</p>
 * <ul>
 *   <li>API Key authentication (simplest for development)</li>
 *   <li>Token Credential authentication (recommended for production)</li>
 * </ul>
 *
 * <p><strong>Related Samples:</strong></p>
 * <ul>
 *   <li>{@link BasicVoiceConversationSample} - Start with the basics first</li>
 *   <li>{@link MicrophoneInputSample} - Add microphone input after understanding authentication</li>
 *   <li>{@link AudioPlaybackSample} - Add audio playback capability</li>
 *   <li>{@link VoiceAssistantSample} - Complete voice assistant using both auth methods</li>
 * </ul>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL (always required)</li>
 *   <li>AZURE_VOICELIVE_API_KEY - The API key (required only for API key authentication)</li>
 * </ul>
 *
 * <p><strong>Token Credential Authentication:</strong></p>
 * <p>This sample uses {@link DefaultAzureCredentialBuilder} by default, which automatically tries
 * multiple authentication methods in order: Environment variables, Managed Identity, Azure CLI,
 * Azure PowerShell, and Interactive browser.</p>
 *
 * <p>You can use other TokenCredential implementations such as:</p>
 * <ul>
 *   <li>AzureCliCredential - Uses 'az login' session</li>
 *   <li>ManagedIdentityCredential - For Azure-hosted applications</li>
 *   <li>ClientSecretCredential - For service principal authentication</li>
 *   <li>And more...</li>
 * </ul>
 *
 * <p>See <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">
 * Azure Identity client library for Java</a> for all available credential types.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * # With API Key (default):
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.AuthenticationMethodsSample" -Dexec.classpathScope=test
 *
 * # With Token Credential:
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.AuthenticationMethodsSample" -Dexec.classpathScope=test -Dexec.args="--token-credential"
 * }</pre>
 */
public final class AuthenticationMethodsSample {

    /**
     * Main method demonstrating different authentication methods.
     *
     * @param args Command line arguments:
     *             --api-key: Use API key authentication (default)
     *             --token-credential: Use DefaultAzureCredential for token-based authentication
     */
    public static void main(String[] args) {
        // Parse authentication method from command line
        String authMethod = "api-key"; // Default
        for (String arg : args) {
            if ("--token-credential".equals(arg)) {
                authMethod = "token-credential";
            } else if ("--api-key".equals(arg)) {
                authMethod = "api-key";
            }
        }

        // Get endpoint (required for all methods)
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        if (endpoint == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT environment variable");
            printUsage();
            return;
        }

        System.out.println("🔐 Authentication Methods Sample");
        System.out.println("Selected method: " + authMethod);
        System.out.println();

        try {
            switch (authMethod) {
                case "api-key":
                    demonstrateApiKeyAuth(endpoint);
                    break;
                case "token-credential":
                    demonstrateTokenCredentialAuth(endpoint);
                    break;
                default:
                    System.err.println("Unknown authentication method: " + authMethod);
                    printUsage();
            }
        } catch (Exception e) {
            System.err.println("❌ Authentication failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demonstrate API Key authentication.
     * Best for: Development, testing, simple scenarios.
     *
     * @param endpoint The VoiceLive service endpoint
     */
    private static void demonstrateApiKeyAuth(String endpoint) {
        System.out.println("📝 Method 1: API Key Authentication");
        System.out.println("   - Simplest authentication method");
        System.out.println("   - Best for development and testing");
        System.out.println("   - Requires AZURE_VOICELIVE_API_KEY environment variable");
        System.out.println();

        String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");
        if (apiKey == null) {
            System.err.println("❌ AZURE_VOICELIVE_API_KEY environment variable is required");
            return;
        }

        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new com.azure.core.credential.KeyCredential(apiKey))
            .buildAsyncClient();

        runSampleSession(client, "API Key");
    }

    /**
     * Demonstrate Token Credential authentication using DefaultAzureCredential.
     * Best for: Production deployments, automatic credential detection.
     *
     * <p>DefaultAzureCredential automatically tries multiple authentication methods in order:</p>
     * <ol>
     *   <li>Environment variables (AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET)</li>
     *   <li>Managed Identity (for Azure-hosted applications)</li>
     *   <li>Azure CLI (if 'az login' has been run)</li>
     *   <li>Azure PowerShell</li>
     *   <li>Interactive browser login</li>
     * </ol>
     *
     * <p>You can also use other TokenCredential types such as:</p>
     * <ul>
     *   <li>AzureCliCredential - Uses 'az login' session</li>
     *   <li>ManagedIdentityCredential - For Azure-hosted applications</li>
     *   <li>ClientSecretCredential - For service principal authentication</li>
     *   <li>ClientCertificateCredential - For certificate-based authentication</li>
     *   <li>UsernamePasswordCredential - For username/password authentication</li>
     * </ul>
     *
     * <p>See <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">
     * Azure Identity library documentation</a> for all available credential types.</p>
     *
     * @param endpoint The VoiceLive service endpoint
     */
    private static void demonstrateTokenCredentialAuth(String endpoint) {
        System.out.println("🔐 Method 2: Token Credential Authentication (DefaultAzureCredential)");
        System.out.println("   - Recommended for production deployments");
        System.out.println("   - Automatically tries multiple authentication methods");
        System.out.println("   - Credential chain:");
        System.out.println("     1. Environment variables");
        System.out.println("     2. Managed Identity");
        System.out.println("     3. Azure CLI (run 'az login' first)");
        System.out.println("     4. Azure PowerShell");
        System.out.println("     5. Interactive browser");
        System.out.println();
        System.out.println("   You can also use other TokenCredential types:");
        System.out.println("   - AzureCliCredential, ManagedIdentityCredential, ClientSecretCredential, etc.");
        System.out.println("   - See: https://learn.microsoft.com/java/api/overview/azure/identity-readme");
        System.out.println();

        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        runSampleSession(client, "DefaultAzureCredential");
    }

    /**
     * Run a simple session to verify authentication.
     *
     * @param client The VoiceLive client
     * @param authMethodName The authentication method name for display
     */
    private static void runSampleSession(VoiceLiveAsyncClient client, String authMethodName) {
        System.out.println("🚀 Testing authentication with " + authMethodName + "...");

        // Configure basic session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a test assistant.")
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(24000);

        // Start session to verify authentication
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                System.out.println("✅ Authentication successful!");
                System.out.println("✓ Session started successfully with " + authMethodName);

                // Subscribe to receive events
                session.receiveEvents()
                    .subscribe(
                        event -> handleEvent(event),
                        error -> System.err.println("Error: " + error.getMessage())
                    );

                // Send session configuration
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent)
                    .doOnSuccess(v -> System.out.println("✓ Session configured successfully"))
                    .then(Mono.delay(java.time.Duration.ofSeconds(2))) // Wait briefly
                    .then(Mono.fromRunnable(() -> System.out.println("✓ Authentication test completed successfully\n")));
            })
            .doOnError(error -> {
                System.err.println("❌ Authentication failed!");
                System.err.println("   Error: " + error.getMessage());
                System.err.println();
            })
            .block(); // Block for demo purposes
    }

    /**
     * Handle incoming server events.
     *
     * @param event The server event
     */
    private static void handleEvent(SessionUpdate event) {
        ServerEventType eventType = event.getType();

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("  → Session created");
        } else if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("  → Session updated");
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("  → Session error");
        }
    }

    /**
     * Print usage information.
     */
    private static void printUsage() {
        System.err.println("\nUsage:");
        System.err.println("  java AuthenticationMethodsSample [--api-key|--token-credential]");
        System.err.println();
        System.err.println("Options:");
        System.err.println("  --api-key              Use API key authentication (default)");
        System.err.println("  --token-credential     Use DefaultAzureCredential for token-based authentication");
        System.err.println();
        System.err.println("Environment Variables:");
        System.err.println("  AZURE_VOICELIVE_ENDPOINT    Required for all methods");
        System.err.println("  AZURE_VOICELIVE_API_KEY     Required only for --api-key");
        System.err.println();
        System.err.println("For --token-credential, ensure one of these is configured:");
        System.err.println("  - Azure CLI: Run 'az login'");
        System.err.println("  - Managed Identity: Enabled on Azure-hosted applications");
        System.err.println("  - Environment variables: AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET");
        System.err.println("  - See: https://learn.microsoft.com/java/api/overview/azure/identity-readme");
        System.err.println();
        System.err.println("Examples:");
        System.err.println("  # Using API key (PowerShell)");
        System.err.println("  $env:AZURE_VOICELIVE_ENDPOINT='https://your-endpoint.com'");
        System.err.println("  $env:AZURE_VOICELIVE_API_KEY='your-api-key'");
        System.err.println("  mvn exec:java -D exec.mainClass=com.azure.ai.voicelive.AuthenticationMethodsSample -D exec.classpathScope=test");
        System.err.println();
        System.err.println("  # Using Token Credential with Azure CLI");
        System.err.println("  az login");
        System.err.println("  mvn exec:java -D exec.mainClass=com.azure.ai.voicelive.AuthenticationMethodsSample -D exec.classpathScope=test -D exec.args='--token-credential'");
    }

    // Private constructor to prevent instantiation
    private AuthenticationMethodsSample() {
    }
}
