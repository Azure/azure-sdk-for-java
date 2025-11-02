// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base test class for Azure Planetary Computer tests.
 * Provides common test infrastructure, client setup, and sanitization configuration.
 */
public class PlanetaryComputerTestBase extends TestProxyTestBase {
    // UUID pattern for various IDs
    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    // Replacement values - matching Python/C# SDK patterns
    private static final String SANITIZED_ZERO_UUID = "00000000-0000-0000-0000-000000000000";
    private static final String SANITIZED_ZERO_UUID_32 = "00000000000000000000000000000000";
    private static final String SANITIZED_HOST = "SANITIZED";
    private static final String REDACTED = "REDACTED";
    private static final String SANITIZED_DATE = "Mon, 01 Jan 2024 00:00:00 GMT";
    private static final String SANITIZED_TRACEPARENT = "00-00000000000000000000000000000000-0000000000000000-00";
    private static final String SANITIZED_COOKIE = "cookie;";
    private static final String SANITIZED_SET_COOKIE = "[set-cookie;]";
    private static final String SANITIZED_SERVER_TIMING = "total;dur=0.0";
    private static final String SANITIZED_ACCESS_TOKEN = "access_token";

    protected IngestionClient ingestionClient;
    protected StacClient stacClient;
    protected DataClient dataClient;
    protected SharedAccessSignatureClient sharedAccessSignatureClient;
    protected PlanetaryComputerTestEnvironment testEnvironment;
    private boolean sanitizersApplied = false;

    @Override
    protected void beforeTest() {
        testEnvironment = new PlanetaryComputerTestEnvironment();

        String endpoint = testEnvironment.getEndpoint();

        // Build Ingestion Client
        PlanetaryComputerProClientBuilder ingestionClientBuilder
            = new PlanetaryComputerProClientBuilder().endpoint(endpoint)
                .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        configureClientAuthentication(ingestionClientBuilder);
        ingestionClient = ingestionClientBuilder.buildIngestionClient();

        // Build STAC Client
        PlanetaryComputerProClientBuilder stacClientBuilder = new PlanetaryComputerProClientBuilder().endpoint(endpoint)
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        configureClientAuthentication(stacClientBuilder);
        stacClient = stacClientBuilder.buildStacClient();

        // Build Data Client
        PlanetaryComputerProClientBuilder dataClientBuilder = new PlanetaryComputerProClientBuilder().endpoint(endpoint)
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        configureClientAuthentication(dataClientBuilder);
        dataClient = dataClientBuilder.buildDataClient();

        // Build SAS Client
        PlanetaryComputerProClientBuilder sasClientBuilder = new PlanetaryComputerProClientBuilder().endpoint(endpoint)
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

        configureClientAuthentication(sasClientBuilder);
        sharedAccessSignatureClient = sasClientBuilder.buildSharedAccessSignatureClient();
    }

    private void configureClientAuthentication(PlanetaryComputerProClientBuilder builder) {
        TestMode mode = getTestMode();

        // Apply sanitizers BEFORE configuring playback/record (matching OpenAI SDK pattern)
        if (mode != TestMode.LIVE && !sanitizersApplied) {
            applySanitizers();
            applyMatchers();
            sanitizersApplied = true;
        }

        if (mode == TestMode.PLAYBACK) {
            builder.credential(new MockTokenCredential());
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else if (mode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
            builder.credential(new DefaultAzureCredentialBuilder().build());
        } else if (mode == TestMode.LIVE) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
    }

    /**
     * Apply all sanitizers for test recordings.
     * This centralizes all sanitization patterns to match C# test patterns.
     */
    private void applySanitizers() {
        // Remove default AZSDK sanitizers that are too aggressive for public data
        // These sanitizers remove "name" and "id" fields which are public collection/item identifiers
        // Note: Apply unconditionally like C# implementation - the interceptorManager will only apply them
        // when recording/playback is active (not in live mode)

        // AZSDK3493 removes "name" fields from JSON bodies
        interceptorManager.removeSanitizers("AZSDK3493");
        // AZSDK3430 removes "id" fields from JSON bodies
        interceptorManager.removeSanitizers("AZSDK3430");
        // AZSDK2003 is the default hostname sanitizer that reduces URLs to "Sanitized.com"
        interceptorManager.removeSanitizers("AZSDK2003");
        // AZSDK2030 is related to operation-location and other headers
        interceptorManager.removeSanitizers("AZSDK2030");
        // AZSDK4001 replaces entire host name in URL
        interceptorManager.removeSanitizers("AZSDK4001");
        // AZSDK3447 sanitizes path segments - collection IDs are public
        interceptorManager.removeSanitizers("AZSDK3447");
        // AZSDK3492 sanitizes "url" body key - THIS IS THE KEY ONE for provider URLs!
        interceptorManager.removeSanitizers("AZSDK3492");
        // AZSDK3491 might sanitize "host" body key
        interceptorManager.removeSanitizers("AZSDK3491");
        // Try removing all common body key sanitizers
        interceptorManager.removeSanitizers("AZSDK3494", "AZSDK3495", "AZSDK3496", "AZSDK3497", "AZSDK3498");

        List<TestProxySanitizer> customSanitizers = new ArrayList<>();

        // Header sanitizers - when no regex, use 4-param constructor with null regex to replace entire header value
        customSanitizers
            .add(new TestProxySanitizer("Set-Cookie", null, SANITIZED_SET_COOKIE, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("Cookie", null, SANITIZED_COOKIE, TestProxySanitizerType.HEADER));
        customSanitizers
            .add(new TestProxySanitizer("X-Request-ID", null, SANITIZED_ZERO_UUID_32, TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("Date", null, SANITIZED_DATE, TestProxySanitizerType.HEADER));
        customSanitizers
            .add(new TestProxySanitizer("Server-Timing", null, SANITIZED_SERVER_TIMING, TestProxySanitizerType.HEADER));
        customSanitizers
            .add(new TestProxySanitizer("traceparent", null, SANITIZED_TRACEPARENT, TestProxySanitizerType.HEADER));

        // UUID-based headers with regex
        customSanitizers.add(new TestProxySanitizer("apim-request-id", UUID_PATTERN, SANITIZED_ZERO_UUID,
            TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("x-ms-client-request-id", UUID_PATTERN, SANITIZED_ZERO_UUID,
            TestProxySanitizerType.HEADER));
        customSanitizers.add(new TestProxySanitizer("Authorization", "Bearer\\s+.+", "Bearer " + REDACTED,
            TestProxySanitizerType.HEADER));

        // Additional Azure-specific headers
        customSanitizers.add(new TestProxySanitizer("x-azure-ref", null, "Sanitized", TestProxySanitizerType.HEADER));
        customSanitizers.add(
            new TestProxySanitizer("mise-correlation-id", null, SANITIZED_ZERO_UUID, TestProxySanitizerType.HEADER));

        // JSON Body Key sanitizers for credentials (using JSONPath)
        customSanitizers
            .add(new TestProxySanitizer("$..access_token", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers
            .add(new TestProxySanitizer("$..refresh_token", null, REDACTED, TestProxySanitizerType.BODY_KEY));
        customSanitizers.add(
            new TestProxySanitizer("$..subscription_id", null, SANITIZED_ZERO_UUID, TestProxySanitizerType.BODY_KEY));
        customSanitizers
            .add(new TestProxySanitizer("$..tenant_id", null, SANITIZED_ZERO_UUID, TestProxySanitizerType.BODY_KEY));
        customSanitizers
            .add(new TestProxySanitizer("$..client_id", null, SANITIZED_ZERO_UUID, TestProxySanitizerType.BODY_KEY));
        customSanitizers
            .add(new TestProxySanitizer("$..client_secret", null, REDACTED, TestProxySanitizerType.BODY_KEY));

        // Operation location header sanitizers
        customSanitizers.add(new TestProxySanitizer("operation-location",
            "/operations/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
            "/operations/" + SANITIZED_ZERO_UUID, TestProxySanitizerType.HEADER));

        customSanitizers.add(new TestProxySanitizer("Location",
            "/ingestion-sources/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
            "/ingestion-sources/" + SANITIZED_ZERO_UUID, TestProxySanitizerType.HEADER));

        customSanitizers.add(new TestProxySanitizer("Location",
            "/ingestions/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
            "/ingestions/" + SANITIZED_ZERO_UUID, TestProxySanitizerType.HEADER));

        // URI sanitizers - geocatalog endpoints
        customSanitizers
            .add(new TestProxySanitizer("(?<=https://)[^.]+\\.[^.]+\\.[^.]+(?=\\.geocatalog\\.spatio\\.azure\\.com)",
                "Sanitized.sanitized_label.sanitized_location", TestProxySanitizerType.URL));

        // Storage account sanitizers
        customSanitizers.add(new TestProxySanitizer("(?<=https://)([^.]+)(?=\\.blob\\.core\\.windows\\.net)",
            SANITIZED_HOST, TestProxySanitizerType.URL));

        // URL-encoded blob storage URLs
        customSanitizers.add(new TestProxySanitizer("https%3A%2F%2F[a-z0-9]+\\.blob\\.core\\.windows\\.net",
            "https%3A%2F%2F" + SANITIZED_HOST + ".blob.core.windows.net", TestProxySanitizerType.URL));

        // Operation/Ingestion/Source IDs in URLs
        customSanitizers
            .add(new TestProxySanitizer("(?<=/operations/)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
                SANITIZED_ZERO_UUID, TestProxySanitizerType.URL));

        customSanitizers.add(new TestProxySanitizer(
            "(?<=/ingestion-sources/)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", SANITIZED_ZERO_UUID,
            TestProxySanitizerType.URL));

        customSanitizers
            .add(new TestProxySanitizer("(?<=/ingestions/)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
                SANITIZED_ZERO_UUID, TestProxySanitizerType.URL));

        customSanitizers
            .add(new TestProxySanitizer("(?<=/runs/)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
                SANITIZED_ZERO_UUID, TestProxySanitizerType.URL));

        // Subscription/tenant/client IDs in URLs
        customSanitizers.add(
            new TestProxySanitizer("(?<=/subscriptions/)([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})",
                SANITIZED_ZERO_UUID, TestProxySanitizerType.URL));

        customSanitizers.add(new TestProxySanitizer("(?<=subscription_id=)([0-9a-f-]+)", SANITIZED_ZERO_UUID,
            TestProxySanitizerType.URL));

        customSanitizers.add(
            new TestProxySanitizer("(?<=tenant_id=)([0-9a-f-]+)", SANITIZED_ZERO_UUID, TestProxySanitizerType.URL));

        customSanitizers.add(
            new TestProxySanitizer("(?<=client_id=)([0-9a-f-]+)", SANITIZED_ZERO_UUID, TestProxySanitizerType.URL));

        // Secret parameters in URLs
        customSanitizers
            .add(new TestProxySanitizer("(?<=client_secret=)([^&]+)", REDACTED, TestProxySanitizerType.URL));

        customSanitizers.add(new TestProxySanitizer("(?<=access_token=)([^&]+)", REDACTED, TestProxySanitizerType.URL));

        customSanitizers
            .add(new TestProxySanitizer("(?<=refresh_token=)([^&]+)", REDACTED, TestProxySanitizerType.URL));

        // Body sanitizers - geocatalog endpoints
        customSanitizers.add(new TestProxySanitizer("https://[^.]+\\.[^.]+\\.[^.]+\\.geocatalog\\.spatio\\.azure\\.com",
            "https://Sanitized.sanitized_label.sanitized_location.geocatalog.spatio.azure.com",
            TestProxySanitizerType.BODY_REGEX));

        // Body sanitizers - blob storage URLs
        customSanitizers.add(new TestProxySanitizer("https://[a-z0-9]+\\.blob\\.core\\.windows\\.net",
            "https://" + SANITIZED_HOST + ".blob.core.windows.net", TestProxySanitizerType.BODY_REGEX));

        // Body sanitizers - UUID in JSON id fields
        customSanitizers.add(
            new TestProxySanitizer("\"id\"\\s*:\\s*\"[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\"",
                "\"id\": \"" + SANITIZED_ZERO_UUID + "\"", TestProxySanitizerType.BODY_REGEX));

        // Body sanitizers - container URLs
        customSanitizers.add(new TestProxySanitizer("(?<=\"containerUrl\":\")[^\"]+",
            "https://" + SANITIZED_HOST + ".blob.core.windows.net/" + SANITIZED_HOST,
            TestProxySanitizerType.BODY_REGEX));

        customSanitizers.add(new TestProxySanitizer("(?<=\"containerUri\":\")[^\"]+",
            "https://" + SANITIZED_HOST + ".blob.core.windows.net/" + SANITIZED_HOST,
            TestProxySanitizerType.BODY_REGEX));

        // Body sanitizers - access tokens and SAS tokens
        customSanitizers.add(new TestProxySanitizer("\"access_token\"\\s*:\\s*\"[^\"]+\"",
            "\"access_token\": \"" + SANITIZED_ACCESS_TOKEN + "\"", TestProxySanitizerType.BODY_REGEX));

        customSanitizers.add(new TestProxySanitizer("\"sasToken\"\\s*:\\s*\"[^\"]+\"",
            "\"sasToken\": \"sv=2021-01-01&st=2020-01-01T00:00:00Z&se=2099-12-31T23:59:59Z&sr=c&sp=rl&sig=Sanitized\"",
            TestProxySanitizerType.BODY_REGEX));

        // Collection ID sanitizers (hash suffix only)
        customSanitizers.add(new TestProxySanitizer("([a-z0-9]+-[a-z]+-[a-z0-9]+)-[0-9a-f]{8}", "$1-00000000",
            TestProxySanitizerType.URL));

        customSanitizers.add(new TestProxySanitizer("\"([a-z0-9]+-[a-z]+-[a-z0-9]+)-[0-9a-f]{8}\"", "\"$1-00000000\"",
            TestProxySanitizerType.BODY_REGEX));

        // Add all sanitizers (unconditional like C#)
        interceptorManager.addSanitizers(customSanitizers);
    }

    /**
     * Apply custom matchers for playback mode.
     */
    private void applyMatchers() {
        if (interceptorManager.isPlaybackMode()) {
            // Match only on specific headers (ignore dynamic request IDs)
            interceptorManager.addMatchers(
                Arrays.asList(new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("x-ms-client-request-id"))));
        }
    }

    /**
     * Validate that a response is not null and has a successful status code.
     */
    protected void validateResponse(Response<?> response, String operationName) {
        assertNotNull(response, operationName + " response should not be null");
        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300,
            operationName + " should return 2xx status code, got: " + response.getStatusCode());
    }

    /**
     * Validate that a string is not null or empty.
     */
    protected void validateNotNullOrEmpty(String value, String fieldName) {
        assertNotNull(value, fieldName + " should not be null");
        assertTrue(!value.isEmpty(), fieldName + " should not be empty");
    }

    /**
     * Get the current test mode.
     */
    public TestMode getTestMode() {
        return testContextManager.getTestMode();
    }

    /**
     * Check if running in live mode.
     */
    protected boolean isLiveMode() {
        return testContextManager.getTestMode() == TestMode.LIVE;
    }

    /**
     * Check if running in playback mode.
     */
    protected boolean isPlaybackMode() {
        return testContextManager.getTestMode() == TestMode.PLAYBACK;
    }

    /**
     * Check if running in record mode.
     */
    protected boolean isRecordMode() {
        return testContextManager.getTestMode() == TestMode.RECORD;
    }

    /**
     * Get a configured StacClient for testing.
     */
    protected StacClient getStacClient() {
        PlanetaryComputerProClientBuilder builder
            = new PlanetaryComputerProClientBuilder().endpoint(testEnvironment.getEndpoint())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        configureClientAuthentication(builder);
        return builder.buildStacClient();
    }

    /**
     * Get a configured IngestionClient for testing.
     */
    protected IngestionClient getIngestionClient() {
        PlanetaryComputerProClientBuilder builder
            = new PlanetaryComputerProClientBuilder().endpoint(testEnvironment.getEndpoint())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        configureClientAuthentication(builder);
        return builder.buildIngestionClient();
    }

    /**
     * Get a configured DataClient for testing.
     */
    protected DataClient getDataClient() {
        PlanetaryComputerProClientBuilder builder
            = new PlanetaryComputerProClientBuilder().endpoint(testEnvironment.getEndpoint())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        configureClientAuthentication(builder);
        return builder.buildDataClient();
    }

    /**
     * Get a configured SharedAccessSignatureClient for testing.
     */
    protected SharedAccessSignatureClient getSasClient() {
        PlanetaryComputerProClientBuilder builder
            = new PlanetaryComputerProClientBuilder().endpoint(testEnvironment.getEndpoint())
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        configureClientAuthentication(builder);
        return builder.buildSharedAccessSignatureClient();
    }
}
