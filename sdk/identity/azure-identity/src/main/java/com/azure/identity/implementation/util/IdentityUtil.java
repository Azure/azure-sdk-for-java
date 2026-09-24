// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AuthenticationRecord;
import com.azure.identity.BrowserCustomizationOptions;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class IdentityUtil {
    /**
     * The message of the {@link IllegalStateException} the JVM raises from {@code Runtime.addShutdownHook} once
     * shutdown has begun. azure-core's shared executor hits it when a token request arrives while the JVM is
     * shutting down.
     */
    private static final String JVM_SHUTDOWN_IN_PROGRESS = "Shutdown in progress";

    /**
     * Whether the throwable, or any cause in its chain, is an {@link InterruptedException}.
     * <p>
     * An interruption is a cooperative cancellation of the calling thread, for example a Reactor scheduler disposing
     * the worker that is waiting for a token. It is not an authentication failure and should not be reported as one.
     *
     * @param throwable The throwable to inspect.
     * @return {@code true} if the throwable was caused by the calling thread being interrupted.
     */
    public static boolean isInterruption(Throwable throwable) {
        return hasCause(throwable, IdentityUtil::isInterrupted);
    }

    /**
     * Rethrows {@code failure} when it is a shutdown signal, and does nothing otherwise.
     * <p>
     * Credentials wrap their token-cache lookup in a catch-all so that a cache miss falls through to a full
     * acquisition. A cancellation is not a cache miss: falling through would ignore it and, for the interactive
     * credentials, prompt the user for a request that is already cancelled.
     *
     * @param failure The failure raised while acquiring a token.
     * @throws RuntimeException If {@code failure} is a shutdown signal.
     */
    public static void rethrowIfShutdownSignal(Exception failure) {
        if (failure instanceof RuntimeException && isShutdownSignal(failure)) {
            throw (RuntimeException) failure;
        }
    }

    /**
     * Whether the throwable, or any cause in its chain, is a shutdown signal rather than an authentication failure.
     * <p>
     * That is either an interruption of the calling thread (see {@link #isInterruption(Throwable)}) or the
     * {@link IllegalStateException} the JVM raises when a shutdown hook is registered after shutdown has begun,
     * which a token request runs into when it reaches azure-core's shared executor while the JVM is shutting down.
     * Neither says anything about the credential, so neither should be reported as an authentication error.
     *
     * @param throwable The throwable to inspect.
     * @return {@code true} if the throwable was caused by the calling thread being interrupted or by the JVM
     * shutting down.
     */
    public static boolean isShutdownSignal(Throwable throwable) {
        return hasCause(throwable, throwable1 -> isInterrupted(throwable1) || isJvmShuttingDown(throwable1));
    }

    private static boolean isInterrupted(Throwable throwable) {
        return throwable instanceof InterruptedException;
    }

    private static boolean isJvmShuttingDown(Throwable throwable) {
        return throwable instanceof IllegalStateException && JVM_SHUTDOWN_IN_PROGRESS.equals(throwable.getMessage());
    }

    /**
     * Whether {@code throwable} or any throwable in its causal chain matches {@code predicate}.
     * <p>
     * A causal chain can be made cyclic through {@link Throwable#initCause(Throwable)}, so the chain is walked with
     * two cursors advancing at different speeds; they meet only if the chain loops, which ends the walk. The chain is
     * otherwise followed to its end, as {@code RetryPolicy} does when it inspects a chain for retriable causes.
     *
     * @param throwable The throwable to inspect, which may be null.
     * @param predicate The test to apply to each throwable in the chain.
     * @return Whether any throwable in the chain matches.
     */
    private static boolean hasCause(Throwable throwable, Predicate<Throwable> predicate) {
        Throwable slow = throwable;
        Throwable fast = throwable;
        while (fast != null) {
            if (predicate.test(fast)) {
                return true;
            }
            fast = fast.getCause();
            if (fast == null) {
                return false;
            }
            if (predicate.test(fast)) {
                return true;
            }
            fast = fast.getCause();
            slow = slow.getCause();
            if (fast == slow) {
                // The chain loops back on itself; everything reachable has been tested.
                return false;
            }
        }
        return false;
    }

    public static final Path VSCODE_AUTH_RECORD_PATH = Paths.get(System.getProperty("user.home"), ".azure",
        "ms-azuretools.vscode-azureresourcegroups", "authRecord.json");
    private static final ClientLogger LOGGER = new ClientLogger(IdentityUtil.class);
    public static final String AZURE_ADDITIONALLY_ALLOWED_TENANTS = "AZURE_ADDITIONALLY_ALLOWED_TENANTS";
    public static final String ALL_TENANTS = "*";
    public static final String DEFAULT_TENANT = "organizations";
    public static final HttpHeaderName X_TFS_FED_AUTH_REDIRECT = HttpHeaderName.fromString("X-TFS-FedAuthRedirect");
    public static final HttpHeaderName X_VSS_E2EID = HttpHeaderName.fromString("x-vss-e2eid");
    public static final HttpHeaderName X_MSEDGE_REF = HttpHeaderName.fromString("x-msedge-ref");

    public static final File NULL_FILE
        = new File((System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null"));

    private IdentityUtil() {
    }

    /**
     * Resolve the Tenant Id to be used in the authentication requests.
     * @param currentTenantId the current tenant Id.
     * @param requestContext the user passed in {@link TokenRequestContext}
     * @param options the identity client options bag.
     * on the credential or not.
     */
    public static String resolveTenantId(String currentTenantId, TokenRequestContext requestContext,
        IdentityClientOptions options) {

        String contextTenantId = requestContext.getTenantId();

        if (contextTenantId != null && currentTenantId != null && !currentTenantId.equalsIgnoreCase(contextTenantId)) {
            if (options.isMultiTenantAuthenticationDisabled()) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The Multi Tenant Authentication "
                    + "is disabled. An updated Tenant Id provided via TokenRequestContext cannot be used in this "
                    + "scenario. To resolve this issue, set the env var AZURE_IDENTITY_DISABLE_MULTITENANTAUTH"
                    + " to false ", null));
            } else if ("adfs".equals(currentTenantId)) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The credential is configured with"
                    + "`adfs` tenant id and it cannot be replaced with a tenant id challenge provided via "
                    + "TokenRequestContext class. ", null));
            }
            String resolvedTenantId = CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId : contextTenantId;

            if (!resolvedTenantId.equalsIgnoreCase(currentTenantId)
                && !options.getAdditionallyAllowedTenants().contains(ALL_TENANTS)
                && !options.getAdditionallyAllowedTenants().contains(resolvedTenantId)) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(
                    "The current credential is not configured to acquire tokens for tenant " + resolvedTenantId
                        + ". To enable acquiring tokens for this tenant add it to the AdditionallyAllowedTenants on the credential options, "
                        + "or add \"*\" to AdditionallyAllowedTenants to allow acquiring tokens for any tenant. See the troubleshooting guide for more information. https://aka.ms/azsdk/java/identity/multitenant/troubleshoot",
                    null));
            }
            return resolvedTenantId;
        }

        return currentTenantId;

    }

    public static List<String> resolveAdditionalTenants(List<String> additionallyAllowedTenants) {
        if (additionallyAllowedTenants == null) {
            return Collections.emptyList();
        }

        if (additionallyAllowedTenants.contains(ALL_TENANTS)) {
            return Collections.singletonList(ALL_TENANTS);
        }

        return additionallyAllowedTenants;
    }

    public static List<String> getAdditionalTenantsFromEnvironment(Configuration configuration) {
        String additionalTenantsFromEnv = configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS);
        if (!CoreUtils.isNullOrEmpty(additionalTenantsFromEnv)) {
            return resolveAdditionalTenants(
                Arrays.asList(configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS).split(";")));
        } else {
            return Collections.emptyList();
        }
    }

    public static boolean browserCustomizationOptionsPresent(BrowserCustomizationOptions browserCustomizationOptions) {
        return !CoreUtils.isNullOrEmpty(browserCustomizationOptions.getErrorMessage())
            || !CoreUtils.isNullOrEmpty(browserCustomizationOptions.getSuccessMessage());
    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        try {
            int read = inputStream.read(buffer, 0, buffer.length);
            while (read != -1) {
                outputStream.write(buffer, 0, read);
                read = inputStream.read(buffer, 0, buffer.length);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return outputStream.toByteArray();
    }

    /**
     * Parses the "access_token" field out of a response body.
     * @param json the response body to parse.
     * @return the access_token value
     * @throws IOException
     */
    public static String getAccessToken(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != com.azure.json.JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("access_token".equals(fieldName)) {
                        return reader.getString();
                    }
                }
                return null;
            });
        }
    }

    /**
     * Parses a json string into a key:value map. Doesn't do anything smart for nested objects or arrays.
     * @param json
     * @return a map of the json fields
     * @throws IOException
     */
    public static Map<String, String> parseJsonIntoMap(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {

            return jsonReader.readObject(reader -> jsonReader.readMap(mapReader -> {
                if (mapReader.currentToken() == JsonToken.START_ARRAY
                    || mapReader.currentToken() == JsonToken.START_OBJECT) {
                    return mapReader.readChildren();
                } else {
                    return mapReader.getString();
                }
            }));
        }
    }

    public static boolean isWindowsPlatform() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static boolean isLinuxPlatform() {
        return System.getProperty("os.name").contains("Linux");
    }

    public static boolean isVsCodeBrokerAuthAvailable() {
        // Check if VS Code broker auth record file exists
        File authRecordFile = VSCODE_AUTH_RECORD_PATH.toFile();
        return isBrokerAvailable() && authRecordFile.exists() && authRecordFile.isFile();
    }

    public static boolean isBrokerAvailable() {
        try {
            // 1. Check if Broker dependency is available
            Class.forName("com.azure.identity.broker.InteractiveBrowserBrokerCredentialBuilder");
            return true;
        } catch (ClassNotFoundException e) {
            return false; // Broker not present
        }
    }

    public static AuthenticationRecord loadVSCodeAuthRecord() throws IOException {
        // Resolve the full path to authRecord.json
        File file = VSCODE_AUTH_RECORD_PATH.toFile();
        if (!file.exists()) {
            return null;
        }
        // Read file content
        InputStream json = Files.newInputStream(VSCODE_AUTH_RECORD_PATH);
        // Deserialize to AuthenticationRecord
        return AuthenticationRecord.deserialize(json);
    }

    /**
     * Checks if the GNOME Keyring is accessible.
     * @return true if accessible, false otherwise.
     */
    public static boolean isKeyRingAccessible() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("secret-tool", "lookup", "test", "test");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true; // Keyring is accessible
            } else {
                LOGGER.verbose("GNOME Keyring is unavailable or inaccessible.");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted state if InterruptedException occurs
            LOGGER.verbose("Error while checking GNOME Keyring availability: " + e.getMessage());
            return false;
        } catch (IOException e) {
            LOGGER.verbose("Error while checking GNOME Keyring availability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ensures the claims string is base64 encoded.
     * 
     * @param claims The claims string to encode if needed
     * @return Base64 encoded claims string
     */
    public static String ensureBase64Encoded(String claims) {
        if (claims == null || claims.trim().isEmpty()) {
            return claims;
        }
        return java.util.Base64.getEncoder().encodeToString(claims.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Determines if IMDS probing should be performed for ManagedIdentityCredential.
     * Only probe if: chained AND using IMDS AND managed identity is not configured for DAC
     *
     * @param options the identity client options
     * @return true if IMDS probing should be performed, false otherwise
     */
    public static boolean shouldProbeImds(IdentityClientOptions options) {
        String dacEnvConfiguredCredential = options.getDACEnvConfiguredCredential();
        boolean isManagedIdentityConfiguredForDac
            = "managedidentitycredential".equalsIgnoreCase(dacEnvConfiguredCredential);

        // Only probe if: chained AND using IMDS AND managed identity is not configured for DAC
        return options.isChained()
            && !isManagedIdentityConfiguredForDac
            && ManagedIdentitySourceType.DEFAULT_TO_IMDS.equals(ManagedIdentityApplication.getManagedIdentitySource());
    }

}
