// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Provides strongly-typed access to Foundry platform environment variables
 * injected by the Azure AI Foundry hosting infrastructure.
 * <p>
 * All values are read once at class load and cached for the lifetime of the process.
 * This class is a static utility and cannot be instantiated.
 * <p>
 * Equivalent to the C# {@code FoundryEnvironment}.
 */
public final class FoundryEnvironment {

    /**
     * The agent name. Sourced from {@code FOUNDRY_AGENT_NAME}.
     */
    public static final String AGENT_NAME = System.getenv("FOUNDRY_AGENT_NAME");
    /**
     * The agent version. Sourced from {@code FOUNDRY_AGENT_VERSION}.
     */
    public static final String AGENT_VERSION = System.getenv("FOUNDRY_AGENT_VERSION");
    /**
     * The Foundry project endpoint. Sourced from {@code FOUNDRY_PROJECT_ENDPOINT},
     * with fallback to {@code AZURE_AI_PROJECT_ENDPOINT}.
     */
    public static final String PROJECT_ENDPOINT = resolveProjectEndpoint();
    /**
     * The full ARM ID of the Foundry project. Sourced from {@code FOUNDRY_PROJECT_ARM_ID}.
     */
    public static final String PROJECT_ARM_ID = System.getenv("FOUNDRY_PROJECT_ARM_ID");
    /**
     * The session ID. Sourced from {@code FOUNDRY_AGENT_SESSION_ID}.
     */
    public static final String SESSION_ID = System.getenv("FOUNDRY_AGENT_SESSION_ID");
    /**
     * The model deployment name. Sourced from {@code MODEL_DEPLOYMENT_NAME}.
     * Default: {@code "gpt-4.1-mini"}.
     */
    public static final String MODEL_DEPLOYMENT_NAME = resolveWithDefault("MODEL_DEPLOYMENT_NAME", "gpt-4.1-mini");
    /**
     * The Azure managed identity client ID. Sourced from {@code AZURE_CLIENT_ID}.
     * <p>
     * When set, prefer {@code ManagedIdentityCredential} with this client ID.
     * When absent, {@code DefaultAzureCredential} should be used.
     */
    public static final String AZURE_CLIENT_ID = System.getenv("AZURE_CLIENT_ID");
    /**
     * The Azure OpenAI endpoint derived from the project endpoint (scheme + host),
     * with fallback to {@code AZURE_OPENAI_ENDPOINT} or {@code AZURE_ENDPOINT}.
     * <p>
     * For example, if {@link #PROJECT_ENDPOINT} is
     * {@code "https://account.services.ai.azure.com/api/projects/proj"},
     * this resolves to {@code "https://account.services.ai.azure.com"}.
     */
    public static final String OPENAI_ENDPOINT = resolveOpenAiEndpoint();
    /**
     * The HTTP listen port. Sourced from {@code PORT}. Default: 8088.
     */
    public static final int PORT = resolvePort();
    /**
     * The OTLP exporter endpoint. Sourced from {@code OTEL_EXPORTER_OTLP_ENDPOINT}.
     */
    public static final String OTLP_ENDPOINT = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT");
    /**
     * The Application Insights connection string. Sourced from {@code APPLICATIONINSIGHTS_CONNECTION_STRING}.
     * <p>
     * <b>Security note:</b> This value is a credential. Avoid logging or exposing it in diagnostics.
     */
    public static final String APP_INSIGHTS_CONNECTION_STRING = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
    /**
     * Indicates whether the process is running in a Foundry hosted environment.
     * Returns {@code true} when the {@code FOUNDRY_HOSTING_ENVIRONMENT} environment variable
     * is set to a non-empty value.
     */
    public static final boolean IS_HOSTED = isNonEmpty(System.getenv("FOUNDRY_HOSTING_ENVIRONMENT"));
    private static final Logger LOGGER = LoggerFactory.getLogger(FoundryEnvironment.class);

    private FoundryEnvironment() {
        // Static utility class
    }

    /**
     * Resolves the appropriate {@link TokenCredential} for the current environment.
     * <p>
     * When {@link #AZURE_CLIENT_ID} is set (typical in hosted environments with managed identity),
     * returns a {@code ManagedIdentityCredential} scoped to that client ID.
     * Otherwise, returns a {@code DefaultAzureCredential} for local development and
     * environments without an explicit managed identity.
     *
     * @return a {@link TokenCredential} suitable for authenticating with Azure services
     */
    public static TokenCredential resolveCredential() {
        if (isNonEmpty(AZURE_CLIENT_ID)) {
            LOGGER.debug("Using ManagedIdentityCredential with client ID: {}", AZURE_CLIENT_ID);
            return new ManagedIdentityCredentialBuilder()
                .clientId(AZURE_CLIENT_ID)
                .build();
        }
        LOGGER.debug("Using DefaultAzureCredential");
        return new DefaultAzureCredentialBuilder().build();
    }

    private static String resolveProjectEndpoint() {
        String endpoint = System.getenv("FOUNDRY_PROJECT_ENDPOINT");
        if (isNonEmpty(endpoint)) {
            return endpoint;
        }
        return System.getenv("AZURE_AI_PROJECT_ENDPOINT");
    }

    private static String resolveOpenAiEndpoint() {
        // Derive from project endpoint (scheme + host)
        if (isNonEmpty(PROJECT_ENDPOINT)) {
            try {
                URI uri = URI.create(PROJECT_ENDPOINT);
                return uri.getScheme() + "://" + uri.getHost();
            } catch (IllegalArgumentException ignored) {
                // fall through to explicit env vars
            }
        }
        // Fallback to explicit env vars
        String explicit = System.getenv("AZURE_OPENAI_ENDPOINT");
        if (isNonEmpty(explicit)) {
            return explicit;
        }
        return System.getenv("AZURE_ENDPOINT");
    }

    private static String resolveWithDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return isNonEmpty(value) ? value : defaultValue;
    }

    private static int resolvePort() {
        String portEnv = System.getenv("PORT");
        if (portEnv == null || portEnv.isEmpty()) {
            return 8088;
        }
        try {
            int port = Integer.parseInt(portEnv);
            if (port < 1 || port > 65535) {
                throw new IllegalStateException(
                    "The PORT environment variable value '" + portEnv + "' is not a valid port number (1–65535).");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                "The PORT environment variable value '" + portEnv + "' is not a valid port number (1–65535).", e);
        }
    }

    private static boolean isNonEmpty(String value) {
        return value != null && !value.isEmpty();
    }
}

