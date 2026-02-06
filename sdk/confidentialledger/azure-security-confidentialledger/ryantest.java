/*
 * ACL Java SDK Redirect Behavior Test
 *
 * Tests whether the azure-security-confidentialledger Java SDK automatically
 * follows HTTP redirects for write operations (POST transactions).
 *
 * FINDING: Java SDK has TWO issues with redirects:
 *   1. RedirectPolicy is NOT added by default to the pipeline
 *   2. DefaultRedirectStrategy only allows GET/HEAD (not POST)
 *   3. RedirectPolicy STRIPS the Authorization header on redirect (security measure)
 *
 * This test demonstrates:
 *   - Test 1: Default behavior - FAIL (no redirect policy)
 *   - Test 2: With RedirectPolicy (GET/HEAD only) - FAIL (POST not allowed)
 *   - Test 3: With POST-enabled RedirectPolicy - FAIL (auth header stripped -> 401)
 *   - Test 4: With Auth-Preserving Policy - PASS
 */
package com.acl.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.DefaultRedirectStrategy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RedirectPolicy;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.confidentialledger.ConfidentialLedgerClient;
import com.azure.security.confidentialledger.ConfidentialLedgerClientBuilder;
import com.azure.security.confidentialledger.certificate.ConfidentialLedgerCertificateClient;
import com.azure.security.confidentialledger.certificate.ConfidentialLedgerCertificateClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestRedirect {
    // Default ACL endpoint
    private static final String DEFAULT_ENDPOINT = "ap-redirect-test-4398.confidential-ledger.azure.com";
    private static final int NUM_TRANSACTIONS = 5;

    private static String endpoint;
    private static boolean verbose;
    private static String ledgerTlsCertificate;

    public static void main(String[] args) {
        parseArgs(args);

        System.out.println("=".repeat(60));
        System.out.println("ACL Java SDK Redirect Behavior Test");
        System.out.println("=".repeat(60));
        System.out.println("Endpoint: https://" + endpoint);
        System.out.println("Transactions per test: " + NUM_TRANSACTIONS);
        System.out.println();

        try {
            // Get identity service URL based on environment
            String identityServiceUrl = endpoint.contains("staging")
                    ? "https://identity.confidential-ledger-staging.core.azure.com"
                    : "https://identity.confidential-ledger.core.azure.com";

            // Get ledger certificate
            String ledgerName = endpoint.split("\\.")[0];
            System.out.println("Retrieving ledger certificate from identity service...");

            ConfidentialLedgerCertificateClient certClient = new ConfidentialLedgerCertificateClientBuilder()
                    .certificateEndpoint(identityServiceUrl)
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .httpClient(HttpClient.createDefault())
                    .buildClient();

            Response<BinaryData> certResponse = certClient.getLedgerIdentityWithResponse(ledgerName, null);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(certResponse.getValue().toBytes());
            ledgerTlsCertificate = jsonNode.get("ledgerTlsCertificate").asText();

            // Track test results
            TestResult[] results = new TestResult[4];

            // --- Test 1: Default Configuration (no RedirectPolicy) ---
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("TEST 1: Default Configuration (no RedirectPolicy)");
            System.out.println("Expected: FAIL (redirect not followed)");
            System.out.println("NOTE: If requests hit primary node directly, this may PASS.");
            System.out.println("=".repeat(60));
            results[0] = runTransactionTest(TestMode.DEFAULT, "Default (no redirect policy)");

            // --- Test 2: With default RedirectPolicy (GET/HEAD only) ---
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("TEST 2: With Default RedirectPolicy (GET/HEAD only)");
            System.out.println("Expected: FAIL (POST method not allowed for redirect)");
            System.out.println("NOTE: If requests hit primary node directly, this may PASS.");
            System.out.println("=".repeat(60));
            results[1] = runTransactionTest(TestMode.REDIRECT_DEFAULT, "RedirectPolicy (GET/HEAD)");

            // --- Test 3: With POST-enabled RedirectPolicy ---
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("TEST 3: With POST-enabled RedirectPolicy");
            System.out.println("Expected: FAIL (auth header stripped on redirect -> 401)");
            System.out.println("NOTE: If requests hit primary node directly, this may PASS.");
            System.out.println("=".repeat(60));
            results[2] = runTransactionTest(TestMode.REDIRECT_POST_ENABLED, "RedirectPolicy (POST enabled)");

            // --- Test 4: With Auth-Preserving Policy ---
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("TEST 4: With Auth-Preserving Redirect Policy");
            System.out.println("Expected: PASS (auth header preserved on redirect)");
            System.out.println("=".repeat(60));
            results[3] = runTransactionTest(TestMode.AUTH_PRESERVING, "Auth-Preserving Policy");

            // --- Final Summary ---
            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("FINAL SUMMARY");
            System.out.println("=".repeat(60));
            
            boolean[] expectedFails = {true, true, true, false}; // Test 1-3 should fail, Test 4 should pass
            String[] testNames = {
                "Default (no redirect policy)",
                "RedirectPolicy (GET/HEAD only)",
                "RedirectPolicy (POST enabled)",
                "Auth-Preserving Policy"
            };
            String[] expectedResults = {"FAIL", "FAIL", "FAIL", "PASS"};
            
            boolean allCorrect = true;
            for (int i = 0; i < 4; i++) {
                boolean shouldFail = expectedFails[i];
                boolean actuallyFailed = results[i].successCount < NUM_TRANSACTIONS;
                boolean isCorrect = (shouldFail == actuallyFailed);
                if (!isCorrect) allCorrect = false;
                
                System.out.println();
                System.out.println("Test " + (i + 1) + " - " + testNames[i] + ":");
                System.out.println("  Expected: " + expectedResults[i]);
                System.out.println("  Actual:   " + (actuallyFailed ? "FAIL" : "PASS") + 
                        " (" + results[i].successCount + "/" + NUM_TRANSACTIONS + ")");
                if (!actuallyFailed && shouldFail) {
                    System.out.println("  Note:     PASS likely means requests went directly to primary node (no redirect)");
                }
                System.out.println("  Status:   " + (isCorrect ? "✓ CORRECT" : "✗ UNEXPECTED (see note above)"));
            }
            
            System.out.println();
            System.out.println("=".repeat(60));
            
            // Check if all tests passed (likely no redirects triggered)
            boolean allPassed = true;
            for (TestResult result : results) {
                if (result.successCount < NUM_TRANSACTIONS) {
                    allPassed = false;
                    break;
                }
            }
            
            if (allCorrect) {
                System.out.println("OVERALL: ✓ TEST VALIDATED SUCCESSFULLY");
                System.out.println("=".repeat(60));
                System.out.println();
                System.out.println("Conclusion:");
                System.out.println("  - Java SDK does NOT include RedirectPolicy by default");
                System.out.println("  - DefaultRedirectStrategy only allows GET/HEAD, not POST");
                System.out.println("  - RedirectPolicy strips Authorization header (by design)");
                System.out.println("  - Workaround: Custom auth-preserving redirect policy");
            } else if (allPassed) {
                System.out.println("OVERALL: ⚠ NO REDIRECTS TRIGGERED");
                System.out.println("=".repeat(60));
                System.out.println();
                System.out.println("All tests passed - no redirects were triggered.");
                System.out.println("This can happen if all requests went directly to the primary node.");
                System.out.println("The redirect issue may still exist but wasn't exercised in this run.");
            } else {
                System.out.println("OVERALL: ✗ TEST RESULTS UNEXPECTED");
                System.out.println("=".repeat(60));
            }

            // Exit with success if all tests passed or validated correctly
            System.exit((allCorrect || allPassed) ? 0 : 1);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseArgs(String[] args) {
        endpoint = System.getenv("ACL_ENDPOINT");
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = DEFAULT_ENDPOINT;
        }
        verbose = false;

        for (int i = 0; i < args.length; i++) {
            if ("--endpoint".equals(args[i]) && i + 1 < args.length) {
                endpoint = args[++i];
            } else if ("--verbose".equals(args[i]) || "-v".equals(args[i])) {
                verbose = true;
            }
        }
    }

    private enum TestMode {
        DEFAULT,                  // No redirect policy
        REDIRECT_DEFAULT,         // RedirectPolicy with default GET/HEAD
        REDIRECT_POST_ENABLED,    // RedirectPolicy with POST allowed
        AUTH_PRESERVING           // Custom auth-preserving policy
    }

    private static TestResult runTransactionTest(TestMode mode, String testName) {
        try {
            ConfidentialLedgerClient client = createClient(mode);
            int successCount = 0;

            for (int i = 1; i <= NUM_TRANSACTIONS; i++) {
                System.out.print("  [" + i + "/" + NUM_TRANSACTIONS + "] Posting transaction... ");
                Result result = postTransaction(client, i);

                if (result.success) {
                    System.out.println("✓ (" + result.message + ")");
                    successCount++;
                } else {
                    System.out.println("✗ (" + result.message + ")");
                }
            }

            String status = successCount == NUM_TRANSACTIONS ? "PASS" : "FAIL";
            System.out.println();
            System.out.println("  Result: " + status + " (" + successCount + "/" + NUM_TRANSACTIONS + " transactions)");
            
            return new TestResult(successCount, testName);
        } catch (Exception e) {
            System.out.println("  Error creating client: " + e.getMessage());
            return new TestResult(0, testName);
        }
    }

    private static ConfidentialLedgerClient createClient(TestMode mode) throws Exception {
        // Build SSL context with ACL certificate
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(new ByteArrayInputStream(ledgerTlsCertificate.getBytes(StandardCharsets.UTF_8)))
                .build();

        reactor.netty.http.client.HttpClient reactorClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        HttpClient httpClient = new NettyAsyncHttpClientBuilder(reactorClient)
                .wiretap(verbose)
                .build();

        var builder = new ConfidentialLedgerClientBuilder()
                .ledgerEndpoint("https://" + endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .httpClient(httpClient);

        switch (mode) {
            case REDIRECT_DEFAULT:
                // Add default RedirectPolicy (only GET/HEAD allowed)
                builder.addPolicy(new RedirectPolicy());
                break;
                
            case REDIRECT_POST_ENABLED:
                // Add RedirectPolicy with POST method allowed
                builder.addPolicy(new RedirectPolicy(new DefaultRedirectStrategy(
                        3, // maxAttempts
                        "Location", // locationHeader
                        EnumSet.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST) // allowed methods
                )));
                break;
                
            case AUTH_PRESERVING:
                // Add custom auth-preserving redirect policy
                builder.addPolicy(new AuthPreservingRedirectPolicy());
                break;
                
            case DEFAULT:
            default:
                // No redirect policy added
                break;
        }

        if (verbose) {
            builder.httpLogOptions(new HttpLogOptions()
                    .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        }

        return builder.buildClient();
    }

    private static Result postTransaction(ConfidentialLedgerClient client, int index) {
        try {
            Map<String, Object> content = new HashMap<>();
            content.put("test_id", UUID.randomUUID().toString());
            content.put("timestamp", Instant.now().toString());
            content.put("purpose", "redirect_behavior_test");
            content.put("index", index);

            Map<String, Object> entry = new HashMap<>();
            entry.put("contents", content.toString());

            BinaryData requestBody = BinaryData.fromObject(entry);
            RequestOptions requestOptions = new RequestOptions()
                    .addQueryParam("collectionId", "redirect-test");

            Response<BinaryData> response = client.createLedgerEntryWithResponse(
                    requestBody,
                    requestOptions
            );

            int statusCode = response.getStatusCode();
            if (statusCode == 200 || statusCode == 201) {
                // Parse transaction ID from headers
                String transactionId = response.getHeaders().getValue(HttpHeaderName.fromString("x-ms-ccf-transaction-id"));
                if (transactionId == null) {
                    transactionId = "unknown";
                }
                return new Result(true, "transaction_id: " + transactionId);
            } else {
                return new Result(false, "HTTP " + statusCode);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && (message.contains("307") || message.contains("308") || message.contains("redirect"))) {
                return new Result(false, "307/308 redirect not followed");
            }
            if (message != null && message.contains("401")) {
                return new Result(false, "401 Unauthorized (auth header stripped)");
            }
            return new Result(false, "Error: " + (message != null ? message : e.getClass().getSimpleName()));
        }
    }

    /**
     * Custom redirect policy that preserves the Authorization header on redirects.
     * 
     * The default RedirectPolicy strips the Authorization header when following redirects
     * (for security). This policy manually handles redirects while preserving the auth header.
     * 
     * NOTE: Only use this when you trust the redirect target (same-origin or trusted domain).
     */
    private static class AuthPreservingRedirectPolicy implements HttpPipelinePolicy {
        private static final int MAX_REDIRECTS = 20;

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            return attemptRedirect(context, next, 0);
        }

        private Mono<HttpResponse> attemptRedirect(HttpPipelineCallContext context, 
                                                    HttpPipelineNextPolicy next, 
                                                    int redirectCount) {
            // Store the Authorization header before the request
            HttpRequest request = context.getHttpRequest();
            String authHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);

            return next.clone().process().flatMap(response -> {
                int statusCode = response.getStatusCode();
                String locationHeader = response.getHeaderValue(HttpHeaderName.LOCATION);

                // Check if this is a redirect we should follow
                if (isRedirectStatusCode(statusCode) && locationHeader != null && redirectCount < MAX_REDIRECTS) {
                    if (verbose) {
                        System.out.println("    [AuthPreservingRedirectPolicy] Following redirect " + 
                                (redirectCount + 1) + ": " + statusCode + " -> " + locationHeader);
                    }

                    // Close the redirect response
                    response.close();

                    // Update the request URL
                    request.setUrl(locationHeader);

                    // RE-ADD the Authorization header (this is the key fix!)
                    if (authHeader != null) {
                        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, authHeader);
                        if (verbose) {
                            System.out.println("    [AuthPreservingRedirectPolicy] Re-added Authorization header");
                        }
                    }

                    // Follow the redirect
                    return attemptRedirect(context, next, redirectCount + 1);
                }

                // Not a redirect, return the response
                return Mono.just(response);
            });
        }

        private boolean isRedirectStatusCode(int statusCode) {
            return statusCode == HttpURLConnection.HTTP_MOVED_PERM ||    // 301
                   statusCode == HttpURLConnection.HTTP_MOVED_TEMP ||    // 302
                   statusCode == 307 ||                                   // Temporary Redirect
                   statusCode == 308;                                     // Permanent Redirect
        }
    }

    private static class Result {
        final boolean success;
        final String message;

        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    private static class TestResult {
        final int successCount;
        final String testName;

        TestResult(int successCount, String testName) {
            this.successCount = successCount;
            this.testName = testName;
        }
    }
}
