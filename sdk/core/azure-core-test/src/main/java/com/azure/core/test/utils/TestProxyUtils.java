// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Utility functions for interaction with the test proxy.
 */
public class TestProxyUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyUtils.class);
    private static final String PROXY_URL_SCHEME = "http";
    private static final String PROXY_URL_HOST = "localhost";
    private static final int PROXY_URL_PORT = 5000;
    private static final String PROXY_URL =
        String.format("%s://%s:%d", PROXY_URL_SCHEME, PROXY_URL_HOST, PROXY_URL_PORT);
    private static final List<String> JSON_PROPERTIES_TO_REDACT
        = new ArrayList<String>(
        Arrays.asList("authHeader", "accountKey", "accessToken", "accountName", "applicationId", "apiKey",
            "connectionString", "url", "host", "password", "userName"));

    private static final List<String> BODY_REGEX_TO_REDACT
        = new ArrayList<>(Arrays.asList("(?:<Value>)(?<secret>.*)(?:</Value>)", "(?:Password=)(?<secret>.*)(?:;)",
        "(?:User ID=)(?<secret>.*)(?:;)", "(?:<PrimaryKey>)(?<secret>.*)(?:</PrimaryKey>)",
        "(?:<SecondaryKey>)(?<secret>.*)(?:</SecondaryKey>)"));

    private static final String URL_REGEX = "(?<=http://|https://)([^/?]+)";
    private static final List<String> HEADERS_TO_REDACT = new ArrayList<>(Arrays.asList("Ocp-Apim-Subscription-Key", "Operation-Location"));
    private static final String REDACTED_VALUE = "REDACTED";

    private static final String DELEGATION_KEY_CLIENTID_REGEX = "(?:<SignedOid>)(?<secret>.*)(?:</SignedOid>)";
    private static final String DELEGATION_KEY_TENANTID_REGEX = "(?:<SignedTid>)(?<secret>.*)(?:</SignedTid>)";

    /**
     * Get the proxy URL.
     *
     * @return A string containing the proxy URL.
     */
    public static String getProxyUrl() {
        return PROXY_URL;
    }

    /**
     * Adds headers required for communication with the test proxy.
     *
     * @param request The request to add headers to.
     * @param xRecordingId The x-recording-id value for the current session.
     * @param mode The current test proxy mode.
     * @throws RuntimeException Construction of one of the URLs failed.
     */
    public static void changeHeaders(HttpRequest request, String xRecordingId, String mode) {
        UrlBuilder proxyUrlBuilder = UrlBuilder.parse(request.getUrl());
        proxyUrlBuilder.setScheme(PROXY_URL_SCHEME);
        proxyUrlBuilder.setHost(PROXY_URL_HOST);
        proxyUrlBuilder.setPort(PROXY_URL_PORT);

        UrlBuilder originalUrlBuilder = UrlBuilder.parse(request.getUrl());
        originalUrlBuilder.setPath("");
        originalUrlBuilder.setQuery("");

        try {
            URL originalUrl = originalUrlBuilder.toUrl();

            HttpHeaders headers = request.getHeaders();

            headers.add("x-recording-upstream-base-uri", originalUrl.toString());
            headers.add("x-recording-mode", mode);
            headers.add("x-recording-id", xRecordingId);
            request.setUrl(proxyUrlBuilder.toUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the process name of the test proxy binary.
     * @return The platform specific process name.
     * @throws UnsupportedOperationException The current OS is not recognized.
     */
    public static String getProxyProcessName() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (osName.contains("windows")) {
            return "test-proxy.exe";
        } else if (osName.contains("linux")) {
            return "test-proxy";
        } else if (osName.contains("mac os x")) {
            return "test-proxy";
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Checks the return from a request through the test proxy for special error headers.
     * @param httpResponse The {@link HttpResponse} from the test proxy.
     */
    public static void checkForTestProxyErrors(HttpResponse httpResponse) {
        String error = httpResponse.getHeaderValue("x-request-mismatch-error");
        if (error == null) {
            error = httpResponse.getHeaderValue("x-request-known-exception-error");
        }
        if (error == null) {
            error = httpResponse.getHeaderValue("x-request-exception-exception-error");
        }
        if (error != null) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Test proxy exception: " + new String(Base64.getDecoder().decode(error), StandardCharsets.UTF_8)));
        }
    }

    /**
     * Registers the default set of sanitizers for sanitizing request and responses
     * @return the list of default sanitizers to be added.
     */
    public static List<TestProxySanitizer> loadSanitizers() {
        List<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.addAll(addDefaultRegexSanitizers());
        sanitizers.add(addDefaultUrlSanitizer());
        sanitizers.addAll(addDefaultBodySanitizers());
        sanitizers.addAll(addDefaultHeaderSanitizers());
        return sanitizers;
    }

    private static String createCustomMatcherRequestBody(CustomMatcher customMatcher) {
        return String.format("{\"ignoredHeaders\":\"%s\",\"excludedHeaders\":\"%s\",\"compareBodies\":%s,\"ignoredQueryParameters\":\"%s\", \"ignoreQueryOrdering\":%s}",
            getCommaSeperatedString(customMatcher.getHeadersKeyOnlyMatch()),
            getCommaSeperatedString(customMatcher.getExcludedHeaders()),
            customMatcher.isComparingBodies(),
            getCommaSeperatedString(customMatcher.getIgnoredQueryParameters()),
            customMatcher.isQueryOrderingIgnored());
    }

    private static String getCommaSeperatedString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        return stringList.stream()
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(","));
    }

    private static String createUrlRegexRequestBody(String regexValue, String redactedValue) {
        return String.format("{\"value\":\"%s\",\"regex\":\"%s\"}", redactedValue, regexValue);
    }

    private static String createBodyJsonKeyRequestBody(String regexValue, String redactedValue) {
        return String.format("{\"value\":\"%s\",\"jsonPath\":\"%s\"}", redactedValue, regexValue);
    }


    private static String createBodyRegexRequestBody(String regexValue, String redactedValue, String groupForReplace) {
        return String.format("{\"value\":\"%s\",\"regex\":\"%s\",\"groupForReplace\":\"%s\"}", redactedValue, regexValue, groupForReplace);
    }

    private static String createHeaderRegexRequestBody(String regexValue, String redactedValue) {
        return String.format("{\"value\":\"%s\",\"key\":\"%s\"}", redactedValue, regexValue);
    }

    /**
     * Creates a list of sanitizer requests to be sent to the test proxy server.
     *
     * @param sanitizers the list of sanitizers to be added
     * @return the list of sanitizer {@link HttpRequest requests} to be sent.
     * @throws RuntimeException if {@link TestProxySanitizerType} is not supported.
     */
    public static List<HttpRequest> getSanitizerRequests(List<TestProxySanitizer> sanitizers) {
        return sanitizers.stream().map(testProxySanitizer -> {
            String requestBody;
            String sanitizerType;
            switch (testProxySanitizer.getType()) {
                case URL:
                    requestBody =
                        createUrlRegexRequestBody(testProxySanitizer.getRegex(), testProxySanitizer.getRedactedValue());
                    sanitizerType = TestProxySanitizerType.URL.getName();
                    break;
                case BODY_REGEX:
                    requestBody = createBodyRegexRequestBody(testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    sanitizerType = TestProxySanitizerType.BODY_REGEX.getName();
                    break;
                case BODY_KEY:
                    requestBody = createBodyJsonKeyRequestBody(testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue());
                    sanitizerType = TestProxySanitizerType.BODY_KEY.getName();
                    break;
                case HEADER:
                    requestBody = createHeaderRegexRequestBody(testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue());
                    sanitizerType = TestProxySanitizerType.HEADER.getName();
                    break;
                default:
                    throw new RuntimeException(String.format("Sanitizer type {%s} not supported", testProxySanitizer.getType()));
            }
            HttpRequest request
                = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/AddSanitizer", TestProxyUtils.getProxyUrl()))
                .setBody(requestBody);
            request.setHeader("x-abstraction-identifier", sanitizerType);
            return request;
        }).collect(Collectors.toList());
    }

    /**
     * Creates a {@link List} of {@link HttpRequest} to be sent to the test proxy to register matchers.
     * @param matchers The {@link TestProxyRequestMatcher}s to encode into requests.
     * @return The {@link HttpRequest}s to send to the proxy.
     * @throws RuntimeException The {@link TestProxyRequestMatcher.TestProxyRequestMatcherType} is unsupported.
     */
    public static List<HttpRequest> getMatcherRequests(List<TestProxyRequestMatcher> matchers) {
        return matchers.stream().map(testProxyMatcher -> {
            HttpRequest request;
            String matcherType;
            switch (testProxyMatcher.getType()) {
                case HEADERLESS:
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.HEADERLESS.getName();
                    request
                        = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", TestProxyUtils.getProxyUrl()));
                    break;
                case BODILESS:
                    request
                        = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", TestProxyUtils.getProxyUrl()));
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.BODILESS.getName();
                    break;
                case CUSTOM:
                    CustomMatcher customMatcher = (CustomMatcher) testProxyMatcher;
                    String requestBody = createCustomMatcherRequestBody(customMatcher);
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName();
                    request
                        = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", TestProxyUtils.getProxyUrl())).setBody(requestBody);
                    break;
                default:
                    throw new RuntimeException(String.format("Matcher type {%s} not supported", testProxyMatcher.getType()));
            }

            request.setHeader("x-abstraction-identifier", matcherType);
            return request;
        }).collect(Collectors.toList());
    }

    private static TestProxySanitizer addDefaultUrlSanitizer() {
        return new TestProxySanitizer(URL_REGEX, REDACTED_VALUE, TestProxySanitizerType.URL);
    }

    private static List<TestProxySanitizer> addDefaultBodySanitizers() {
        return JSON_PROPERTIES_TO_REDACT.stream()
            .map(jsonProperty ->
                new TestProxySanitizer(String.format("$..%s", jsonProperty), REDACTED_VALUE,
                    TestProxySanitizerType.BODY_KEY))
            .collect(Collectors.toList());
    }

    private static List<TestProxySanitizer> addDefaultRegexSanitizers() {
        List<TestProxySanitizer> userDelegationSanitizers = getUserDelegationSanitizers();

        userDelegationSanitizers.addAll(BODY_REGEX_TO_REDACT.stream()
            .map(bodyRegex -> new TestProxySanitizer(bodyRegex, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"))
            .collect(Collectors.toList()));

        // can add default url and header regex sanitizer same way
        return userDelegationSanitizers;

    }

    private static List<TestProxySanitizer> addDefaultHeaderSanitizers() {
        return HEADERS_TO_REDACT.stream()
            .map(headerProperty ->
                new TestProxySanitizer(headerProperty, REDACTED_VALUE, TestProxySanitizerType.HEADER))
            .collect(Collectors.toList());
    }

    private static List<TestProxySanitizer> getUserDelegationSanitizers() {
        List<TestProxySanitizer> userDelegationSanitizers = new ArrayList<>();
        userDelegationSanitizers.add(new TestProxySanitizer(DELEGATION_KEY_CLIENTID_REGEX, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
        userDelegationSanitizers.add(new TestProxySanitizer(DELEGATION_KEY_TENANTID_REGEX, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
        return userDelegationSanitizers;
    }
}
