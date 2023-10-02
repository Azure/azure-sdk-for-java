// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.http.HttpHeader;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.test.models.CustomMatcher;
import com.typespec.core.test.models.TestProxyRequestMatcher;
import com.typespec.core.test.models.TestProxySanitizer;
import com.typespec.core.test.models.TestProxySanitizerType;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.typespec.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.typespec.core.test.models.TestProxySanitizerType.HEADER;
import static com.typespec.core.test.policy.TestProxyRecordPolicy.RECORD_MODE;

/**
 * Utility functions for interaction with the test proxy.
 */
public class TestProxyUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyUtils.class);
    private static final HttpHeaderName X_RECORDING_SKIP = HttpHeaderName.fromString("x-recording-skip");

    private static final List<String> JSON_PROPERTIES_TO_REDACT
        = new ArrayList<String>(
        Arrays.asList("authHeader", "accountKey", "accessToken", "accountName", "applicationId", "apiKey",
            "connectionString", "url", "host", "password", "userName"));

    private static final Map<String, String> HEADER_KEY_REGEX_TO_REDACT = new HashMap<String, String>() {{
            put("Operation-Location", URL_REGEX);
            put("operation-location", URL_REGEX);
            }};

    private static final List<String> BODY_REGEX_TO_REDACT
        = new ArrayList<>(Arrays.asList("(?:<Value>)(?<secret>.*)(?:</Value>)", "(?:Password=)(?<secret>.*)(?:;)",
        "(?:User ID=)(?<secret>.*)(?:;)", "(?:<PrimaryKey>)(?<secret>.*)(?:</PrimaryKey>)",
        "(?:<SecondaryKey>)(?<secret>.*)(?:</SecondaryKey>)"));

    private static final String URL_REGEX = "(?<=http://|https://)([^/?]+)";
    private static final List<String> HEADER_KEYS_TO_REDACT =
        new ArrayList<>(Arrays.asList("Ocp-Apim-Subscription-Key", "api-key", "x-api-key", "subscription-key"));
    private static final String REDACTED_VALUE = "REDACTED";

    private static final String DELEGATION_KEY_CLIENTID_REGEX = "(?:<SignedOid>)(?<secret>.*)(?:</SignedOid>)";
    private static final String DELEGATION_KEY_TENANTID_REGEX = "(?:<SignedTid>)(?<secret>.*)(?:</SignedTid>)";
    private static final HttpHeaderName X_RECORDING_UPSTREAM_BASE_URI =
        HttpHeaderName.fromString("x-recording-upstream-base-uri");
    private static final HttpHeaderName X_RECORDING_MODE = HttpHeaderName.fromString("x-recording-mode");
    private static final HttpHeaderName X_REQUEST_MISMATCH_ERROR =
        HttpHeaderName.fromString("x-request-mismatch-error");
    private static final HttpHeaderName X_REQUEST_KNOWN_EXCEPTION_ERROR =
        HttpHeaderName.fromString("x-request-known-exception-error");
    private static final HttpHeaderName X_REQUEST_EXCEPTION_EXCEPTION_ERROR =
        HttpHeaderName.fromString("x-request-exception-exception-error");
    private static final HttpHeaderName X_ABSTRACTION_IDENTIFIER =
        HttpHeaderName.fromString("x-abstraction-identifier");

    private static volatile URL proxyUrl;

    /**
     * Adds headers required for communication with the test proxy.
     *
     * @param request The request to add headers to.
     * @param proxyUrl The {@link URL} the proxy lives at.
     * @param xRecordingId The x-recording-id value for the current session.
     * @param mode The current test proxy mode.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies when tests run in Record mode.
     * @throws RuntimeException Construction of one of the URLs failed.
     */
    public static void changeHeaders(HttpRequest request, URL proxyUrl, String xRecordingId, String mode, boolean skipRecordingRequestBody) {
        HttpHeader upstreamUri = request.getHeaders().get(X_RECORDING_UPSTREAM_BASE_URI);

        UrlBuilder proxyUrlBuilder = UrlBuilder.parse(request.getUrl());
        proxyUrlBuilder.setScheme(proxyUrl.getProtocol());
        proxyUrlBuilder.setHost(proxyUrl.getHost());
        if (proxyUrl.getPort() != -1) {
            proxyUrlBuilder.setPort(proxyUrl.getPort());
        }

        UrlBuilder originalUrlBuilder = UrlBuilder.parse(request.getUrl());
        originalUrlBuilder.setPath("");
        originalUrlBuilder.setQuery("");

        try {
            URL originalUrl = originalUrlBuilder.toUrl();

            HttpHeaders headers = request.getHeaders();
            if (upstreamUri == null) {
                headers.set(X_RECORDING_UPSTREAM_BASE_URI, originalUrl.toString());
                headers.set(X_RECORDING_MODE, mode);
                headers.set(X_RECORDING_ID, xRecordingId);
                if (mode.equals(RECORD_MODE) && skipRecordingRequestBody) {
                    headers.set(X_RECORDING_SKIP, "request-body");
                }
            }

            request.setUrl(proxyUrlBuilder.toUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the assets json file path if it exists.
     * @param recordFile the record/playback file
     * @param testClassPath the test class path
     * @return the assets json file path if it exists.
     */
    public static String getAssetJsonFile(File recordFile, Path testClassPath) {
        if (assetJsonFileExists(testClassPath)) {
            // subpath removes nodes "src/test/resources/session-records"
            return Paths.get(recordFile.toPath().subpath(0, 3).toString(), "assets.json").toString();
        } else {
            return null;
        }
    }

    private static boolean assetJsonFileExists(Path testClassPath) {
        return Files.exists(Paths.get(String.valueOf(TestUtils.getRepoRootResolveUntil(testClassPath, "target")),
            "assets.json"));
    }

    /**
     * Sets the response URL back to the original URL before returning it through the pipeline.
     * @param response The {@link HttpResponse} to modify.
     * @return The modified response.
     * @throws RuntimeException Construction of one of the URLs failed.
     */
    public static HttpResponse resetTestProxyData(HttpResponse response) {
        HttpRequest responseRequest = response.getRequest();
        HttpHeaders requestHeaders = responseRequest.getHeaders();
        try {
            URL originalUrl = UrlBuilder.parse(requestHeaders.getValue(X_RECORDING_UPSTREAM_BASE_URI))
                .toUrl();
            UrlBuilder currentUrl = UrlBuilder.parse(responseRequest.getUrl());
            currentUrl.setScheme(originalUrl.getProtocol());
            currentUrl.setHost(originalUrl.getHost());
            int port = originalUrl.getPort();
            if (port == -1) {
                currentUrl.setPort(""); // empty string is no port.
            } else {
                currentUrl.setPort(port);
            }
            responseRequest.setUrl(currentUrl.toUrl());

            requestHeaders.remove(X_RECORDING_UPSTREAM_BASE_URI);
            requestHeaders.remove(X_RECORDING_MODE);
            requestHeaders.remove(X_RECORDING_SKIP);
            requestHeaders.remove(X_RECORDING_ID);
            return response;
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
            return "Azure.Sdk.Tools.TestProxy.exe";
        } else if (osName.contains("linux")) {
            return "Azure.Sdk.Tools.TestProxy";
        } else if (osName.contains("mac os x")) {
            return "Azure.Sdk.Tools.TestProxy";
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Checks the return from a request through the test proxy for special error headers.
     * @param httpResponse The {@link HttpResponse} from the test proxy.
     */
    public static void checkForTestProxyErrors(HttpResponse httpResponse) {
        String error = httpResponse.getHeaderValue(X_REQUEST_MISMATCH_ERROR);
        if (error == null) {
            error = httpResponse.getHeaderValue(X_REQUEST_KNOWN_EXCEPTION_ERROR);
        }
        if (error == null) {
            error = httpResponse.getHeaderValue(X_REQUEST_EXCEPTION_EXCEPTION_ERROR);
        }
        if (error != null) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Test proxy exception: "
                + new String(Base64.getDecoder().decode(error), StandardCharsets.UTF_8)));
        }
    }

    /**
     * Finds the test proxy version in the source tree.
     * @param testClassPath the test class path
     * @return The version string to use.
     * @throws RuntimeException The eng folder could not be located in the repo.
     * @throws UncheckedIOException The version file could not be read properly.
     */
    public static String getTestProxyVersion(Path testClassPath) {
        Path rootPath = TestUtils.getRepoRootResolveUntil(testClassPath, "eng");
        Path versionFile =  Paths.get("eng", "common", "testproxy", "target_version.txt");
        rootPath = rootPath.resolve(versionFile);
        try {
            return Files.readAllLines(rootPath).get(0).replace(System.getProperty("line.separator"), "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets the current URL for the test proxy.
     * @return The {@link URL} location of the test proxy.
     * @throws RuntimeException The URL could not be constructed.
     */
    public static URL getProxyUrl() {
        if (proxyUrl != null) {
            return proxyUrl;
        }
        UrlBuilder builder = new UrlBuilder();
        builder.setHost("localhost");
        builder.setScheme("http");
        builder.setPort(5000);
        try {
            proxyUrl = builder.toUrl();
            return proxyUrl;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers the default set of sanitizers for sanitizing request and responses
     * @return the list of default sanitizers to be added.
     */
    public static List<TestProxySanitizer> loadSanitizers() {
        List<TestProxySanitizer> sanitizers = new ArrayList<>(addDefaultRegexSanitizers());
        sanitizers.add(addDefaultUrlSanitizer());
        sanitizers.addAll(addDefaultBodySanitizers());
        sanitizers.addAll(addDefaultHeaderKeySanitizers());
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

    private static String createBodyJsonKeyRequestBody(String jsonKey, String regex, String redactedValue) {
        if (regex == null) {
            return String.format("{\"value\":\"%s\",\"jsonPath\":\"%s\"}", redactedValue, jsonKey);
        } else {
            return String.format("{\"value\":\"%s\",\"jsonPath\":\"%s\",\"regex\":\"%s\"}", redactedValue, jsonKey, regex);
        }
    }

    private static String createRegexRequestBody(String key, String regex, String value, String groupForReplace) {
        if (key == null) {
            if (groupForReplace == null) {
                // regex pattern and redaction value
                return String.format("{\"value\":\"%s\",\"regex\":\"%s\"}", value, regex);
            } else {
                // regex pattern and redaction value with group replace
                return String.format("{\"value\":\"%s\",\"regex\":\"%s\",\"groupForReplace\":\"%s\"}", value, regex,
                    groupForReplace);
            }
        } else if (regex == null) {
            // header key value
            return String.format("{\"key\":\"%s\",\"value\":\"%s\"}", key, value);
        }
        if (groupForReplace == null) {
            // header key with regex
            return String.format("{\"key\":\"%s\",\"value\":\"%s\",\"regex\":\"%s\"}", key, value, regex);
        } else {
            return String.format("{\"key\":\"%s\",\"value\":\"%s\",\"regex\":\"%s\",\"groupForReplace\":\"%s\"}", key,
                value, regex, groupForReplace);
        }
    }

    /**
     * Creates a list of sanitizer requests to be sent to the test proxy server.
     *
     * @param sanitizers the list of sanitizers to be added.
     * @param proxyUrl The proxyUrl to use when constructing requests.
     * @return the list of sanitizer {@link HttpRequest requests} to be sent.
     * @throws RuntimeException if {@link TestProxySanitizerType} is not supported.
     */
    public static List<HttpRequest> getSanitizerRequests(List<TestProxySanitizer> sanitizers, URL proxyUrl) {
        return sanitizers.stream().map(testProxySanitizer -> {
            String requestBody;
            String sanitizerType;
            switch (testProxySanitizer.getType()) {
                case URL:
                    sanitizerType = TestProxySanitizerType.URL.getName();
                    requestBody =
                        createRegexRequestBody(null, testProxySanitizer.getRegex(),
                            testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);
                case BODY_REGEX:
                    sanitizerType = TestProxySanitizerType.BODY_REGEX.getName();
                    requestBody = createRegexRequestBody(null, testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);
                case BODY_KEY:
                    sanitizerType = TestProxySanitizerType.BODY_KEY.getName();
                    requestBody = createBodyJsonKeyRequestBody(testProxySanitizer.getKey(), testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);
                case HEADER:
                    sanitizerType = HEADER.getName();
                    if (testProxySanitizer.getKey() == null && testProxySanitizer.getRegex() == null) {
                        throw new RuntimeException(
                            String.format("Missing regexKey and/or headerKey for sanitizer type {%s}", sanitizerType));
                    }
                    requestBody = createRegexRequestBody(testProxySanitizer.getKey(),
                        testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);
                default:
                    throw new RuntimeException(
                        String.format("Sanitizer type {%s} not supported", testProxySanitizer.getType()));
            }
        }).collect(Collectors.toList());
    }

    private static HttpRequest createHttpRequest(String requestBody, String sanitizerType, URL proxyUrl) {
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/AddSanitizer", proxyUrl.toString()))
            .setBody(requestBody);
        request.setHeader(X_ABSTRACTION_IDENTIFIER, sanitizerType);
        return request;
    }

    /**
     * Creates a {@link List} of {@link HttpRequest} to be sent to the test proxy to register matchers.
     * @param matchers The {@link TestProxyRequestMatcher}s to encode into requests.
     * @param proxyUrl The proxyUrl to use when constructing requests.
     * @return The {@link HttpRequest}s to send to the proxy.
     * @throws RuntimeException The {@link TestProxyRequestMatcher.TestProxyRequestMatcherType} is unsupported.
     */
    public static List<HttpRequest> getMatcherRequests(List<TestProxyRequestMatcher> matchers, URL proxyUrl) {
        return matchers.stream().map(testProxyMatcher -> {
            HttpRequest request;
            String matcherType;
            switch (testProxyMatcher.getType()) {
                case HEADERLESS:
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.HEADERLESS.getName();
                    request
                        = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", proxyUrl.toString()));
                    break;
                case BODILESS:
                    request
                        = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", proxyUrl.toString()));
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.BODILESS.getName();
                    break;
                case CUSTOM:
                    CustomMatcher customMatcher = (CustomMatcher) testProxyMatcher;
                    String requestBody = createCustomMatcherRequestBody(customMatcher);
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName();
                    request
                        = new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", proxyUrl.toString())).setBody(requestBody);
                    break;
                default:
                    throw new RuntimeException(String.format("Matcher type {%s} not supported", testProxyMatcher.getType()));
            }

            request.setHeader(X_ABSTRACTION_IDENTIFIER, matcherType);
            return request;
        }).collect(Collectors.toList());
    }

    /**
     * Set comparing bodies to false when running in playback and RecordWithoutRequestBody is set for the test.
     * @return the HttpRequest for setting compare bodies matcher to false.
     */
    public static HttpRequest setCompareBodiesMatcher() {
        String requestBody = createCustomMatcherRequestBody(new CustomMatcher().setComparingBodies(false));
        HttpRequest request =
            new HttpRequest(HttpMethod.POST, String.format("%s/Admin/setmatcher", proxyUrl.toString())).setBody(
                requestBody);

        request.setHeader(X_ABSTRACTION_IDENTIFIER,
            TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName());
        return request;
    }

    private static TestProxySanitizer addDefaultUrlSanitizer() {
        return new TestProxySanitizer(URL_REGEX, REDACTED_VALUE, TestProxySanitizerType.URL);
    }

    private static List<TestProxySanitizer> addDefaultBodySanitizers() {
        return JSON_PROPERTIES_TO_REDACT.stream()
            .map(jsonProperty ->
                new TestProxySanitizer(String.format("$..%s", jsonProperty), null, REDACTED_VALUE,
                    TestProxySanitizerType.BODY_KEY))
            .collect(Collectors.toList());
    }

    private static List<TestProxySanitizer> addDefaultRegexSanitizers() {
        List<TestProxySanitizer> regexSanitizers = getUserDelegationSanitizers();

        regexSanitizers.addAll(BODY_REGEX_TO_REDACT.stream()
            .map(bodyRegex -> new TestProxySanitizer(bodyRegex, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"))
            .collect(Collectors.toList()));

        // add body key with regex sanitizers
        List<TestProxySanitizer> keyRegexSanitizers = new ArrayList<>();
        HEADER_KEY_REGEX_TO_REDACT.forEach((key, regex) ->
            keyRegexSanitizers.add(new TestProxySanitizer(key, regex, REDACTED_VALUE, HEADER)));

        regexSanitizers.addAll(keyRegexSanitizers);

        return regexSanitizers;
    }

    private static List<TestProxySanitizer> addDefaultHeaderKeySanitizers() {
        return HEADER_KEYS_TO_REDACT.stream()
            .map(headerKey ->
                new TestProxySanitizer(headerKey, null, REDACTED_VALUE, HEADER))
            .collect(Collectors.toList());
    }

    private static List<TestProxySanitizer> getUserDelegationSanitizers() {
        List<TestProxySanitizer> userDelegationSanitizers = new ArrayList<>();
        userDelegationSanitizers.add(new TestProxySanitizer(DELEGATION_KEY_CLIENTID_REGEX, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
        userDelegationSanitizers.add(new TestProxySanitizer(DELEGATION_KEY_TENANTID_REGEX, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
        return userDelegationSanitizers;
    }
}
