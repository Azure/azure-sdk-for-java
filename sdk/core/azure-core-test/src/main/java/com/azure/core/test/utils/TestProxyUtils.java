// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.utils;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;

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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.azure.core.test.implementation.TestingHelpers.jsonWriteHelper;
import static com.azure.core.test.models.TestProxySanitizerType.BODY_KEY;
import static com.azure.core.test.models.TestProxySanitizerType.BODY_REGEX;
import static com.azure.core.test.models.TestProxySanitizerType.HEADER;
import static com.azure.core.test.policy.TestProxyRecordPolicy.RECORD_MODE;

/**
 * Utility functions for interaction with the test proxy.
 */
public class TestProxyUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyUtils.class);
    private static final HttpHeaderName X_RECORDING_SKIP = HttpHeaderName.fromString("x-recording-skip");
    private static final String REDACTED_VALUE = "REDACTED";
    private static final String URL_REGEX = "(?<=http://|https://)([^/?]+)";

    public static final String HOST_NAME_REGEX = "(?<=http://|https://)(?<host>[^/?\\\\.]+)";
    private static final List<TestProxySanitizer> HEADER_KEY_REGEX_TO_REDACT = Arrays.asList(
        new TestProxySanitizer("ServiceBusDlqSupplementaryAuthorization",
            "(?:(sv|sig|se|srt|ss|sp)=)(?<secret>[^&\\\"]+)", REDACTED_VALUE, TestProxySanitizerType.HEADER)
                .setGroupForReplace("secret"),
        new TestProxySanitizer("ServiceBusSupplementaryAuthorization", "(?:(sv|sig|se|srt|ss|sp)=)(?<secret>[^&\\\"]+)",
            REDACTED_VALUE, TestProxySanitizerType.HEADER).setGroupForReplace("secret"),
        new TestProxySanitizer("operation-location", HOST_NAME_REGEX, REDACTED_VALUE, TestProxySanitizerType.HEADER)
            .setGroupForReplace("host"),
        new TestProxySanitizer("Location", HOST_NAME_REGEX, REDACTED_VALUE, TestProxySanitizerType.HEADER)
            .setGroupForReplace("host"));

    private static final HttpHeaderName X_RECORDING_UPSTREAM_BASE_URI
        = HttpHeaderName.fromString("x-recording-upstream-base-uri");
    private static final HttpHeaderName X_RECORDING_MODE = HttpHeaderName.fromString("x-recording-mode");
    private static final HttpHeaderName X_REQUEST_MISMATCH_ERROR
        = HttpHeaderName.fromString("x-request-mismatch-error");
    private static final HttpHeaderName X_REQUEST_KNOWN_EXCEPTION_ERROR
        = HttpHeaderName.fromString("x-request-known-exception-error");
    private static final HttpHeaderName X_REQUEST_EXCEPTION_EXCEPTION_ERROR
        = HttpHeaderName.fromString("x-request-exception-exception-error");
    private static final HttpHeaderName X_ABSTRACTION_IDENTIFIER
        = HttpHeaderName.fromString("x-abstraction-identifier");

    private static final List<TestProxySanitizer> JSON_BODY_KEYS_TO_REDACT;
    private static final List<TestProxySanitizer> BODY_REGEXES_TO_REDACT;
    private static final List<TestProxySanitizer> HEADER_KEYS_TO_REDACT;

    static {
        // These are prepended with "$.." creating a Jsonpath expression.
        JSON_BODY_KEYS_TO_REDACT = Stream
            .of("authHeader", "accountKey", "accessToken", "accountName", "applicationId", "apiKey", "client_secret",
                "connectionString", "url", "host", "password", "userName", "applicationSecret",
                "aliasSecondaryConnectionString", "aliasPrimaryConnectionString", "primaryKey", "secondaryKey",
                "adminPassword.value", "administratorLoginPassword", "runAsPassword", "adminPassword", "accessSAS",
                "WEBSITE_AUTH_ENCRYPTION_KEY", "decryptionKey", "primaryMasterKey", "primaryReadonlyMasterKey",
                "secondaryMasterKey", "secondaryReadonlyMasterKey", "certificatePassword", "clientSecret",
                "keyVaultClientSecret", "authHeader", "httpHeader", "encryptedCredential", "functionKey",
                "atlasKafkaPrimaryEndpoint", "atlasKafkaSecondaryEndpoint", "certificatePassword",
                "storageAccountPrimaryKey", "privateKey", "fencingClientPassword", "acrToken", "scriptUrlSasToken",
                "azureBlobSource.containerUrl", "properties.DOCKER_REGISTRY_SEVER_PASSWORD")
            .map(jsonProperty -> new TestProxySanitizer("$.." + jsonProperty, null, REDACTED_VALUE, BODY_KEY))
            .collect(Collectors.toList());

        // Values in this list must have a capture group named "secret" for the redaction to work.
        BODY_REGEXES_TO_REDACT = Stream
            .of("(?:(Password|User ID)=)(?<secret>.*)(?:;)", "client_secret=(?<secret>[^&]+)",
                "(?:(access|refresh))_token=(?<secret>.*?)(?=&|$)",
                "<(?:(Primary|Secondary))Key>(?<secret>.*?)</(?:(Primary|Secondary))Key>",
                "<UserDelegationKey>.*?<Signed(O|T)id>(?<secret>.*?)</Signed(O|T)id>.*?</UserDelegationKey>",
                "<UserDelegationKey>.*?<Value>(?<secret>.*?)</Value>.*?</UserDelegationKey>",
                "(?:(Access|Account|SharedAccess))Key=(?<secret>[^;\\\"]+)", "accesskey=(?<secret>[^;\\\"]+)",
                "Secret=(?<secret>[^;\\\"]+)")
            .map(regex -> new TestProxySanitizer(regex, REDACTED_VALUE, BODY_REGEX).setGroupForReplace("secret"))
            .collect(Collectors.toList());

        HEADER_KEYS_TO_REDACT = Stream
            .of("Ocp-Apim-Subscription-Key", "api-key", "x-api-key", "subscription-key", "x-ms-encryption-key",
                "sshPassword")
            .map(headerKey -> new TestProxySanitizer(headerKey, null, REDACTED_VALUE, HEADER))
            .collect(Collectors.toList());
    }

    /**
     * Adds headers required for communication with the test proxy.
     *
     * @param request The request to add headers to.
     * @param xRecordingId The x-recording-id value for the current session.
     * @param mode The current test proxy mode.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies when tests run in Record mode.
     * @throws RuntimeException Construction of one of the URLs failed.
     */
    public static void changeHeaders(HttpRequest request, String xRecordingId, String mode,
        boolean skipRecordingRequestBody) {
        HttpHeader upstreamUri = request.getHeaders().get(X_RECORDING_UPSTREAM_BASE_URI);

        URL proxyUrl = createProxyRequestUrl("");
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
        return Files.exists(
            Paths.get(String.valueOf(TestUtils.getRepoRootResolveUntil(testClassPath, "target")), "assets.json"));
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
            URL originalUrl = UrlBuilder.parse(requestHeaders.getValue(X_RECORDING_UPSTREAM_BASE_URI)).toUrl();
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
            throw LOGGER.logExceptionAsError(new RuntimeException(
                "Test proxy exception: " + new String(Base64.getDecoder().decode(error), StandardCharsets.UTF_8)));
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
        Path overrideVersionFile = Paths.get("eng", "target_proxy_version.txt");
        Path versionFile = Paths.get("eng", "common", "testproxy", "target_version.txt");

        // if a long-lived override exists, use it.
        if (Files.exists(rootPath.resolve(overrideVersionFile))) {
            versionFile = overrideVersionFile;
        }

        Path versionFilePath = rootPath.resolve(versionFile);
        try {
            return Files.readAllLines(versionFilePath).get(0).replace(System.lineSeparator(), "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Creates the URL for a request being sent to Test Proxy.
     *
     * @param path The path to send the request.
     * @return The {@link URL} for a request being sent to the Test Proxy.
     * @throws RuntimeException The URL could not be constructed.
     */
    public static URL createProxyRequestUrl(String path) {
        try {
            return new UrlBuilder().setScheme("http")
                .setHost("localhost")
                .setPort(TestProxyManager.proxyPort)
                .setPath(path)
                .toUrl();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers the default set of sanitizers for sanitizing request and responses
     * @return the list of default sanitizers to be added.
     */
    public static List<TestProxySanitizer> loadSanitizers() {
        List<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.addAll(BODY_REGEXES_TO_REDACT);
        sanitizers.addAll(HEADER_KEY_REGEX_TO_REDACT);
        sanitizers.add(new TestProxySanitizer(URL_REGEX, REDACTED_VALUE, TestProxySanitizerType.URL));
        sanitizers.addAll(JSON_BODY_KEYS_TO_REDACT);
        sanitizers.addAll(HEADER_KEYS_TO_REDACT);
        return sanitizers;
    }

    private static String createCustomMatcherRequestBody(CustomMatcher customMatcher) {
        return jsonWriteHelper(jsonWriter -> jsonWriter.writeStartObject()
            .writeStringField("ignoredHeaders", getCommaSeperatedString(customMatcher.getHeadersKeyOnlyMatch()))
            .writeStringField("excludedHeaders", getCommaSeperatedString(customMatcher.getExcludedHeaders()))
            .writeBooleanField("compareBodies", customMatcher.isComparingBodies())
            .writeStringField("ignoredQueryParameters",
                getCommaSeperatedString(customMatcher.getIgnoredQueryParameters()))
            .writeBooleanField("ignoreQueryOrdering", customMatcher.isQueryOrderingIgnored())
            .writeEndObject());
    }

    private static String getCommaSeperatedString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        return stringList.stream().filter(s -> !CoreUtils.isNullOrEmpty(s)).collect(Collectors.joining(","));
    }

    private static String createBodyJsonKeyRequestBody(String jsonKey, String regex, String redactedValue) {
        return jsonWriteHelper(jsonWriter -> jsonWriter.writeStartObject()
            .writeStringField("value", redactedValue)
            .writeStringField("jsonPath", jsonKey)
            // writeStringField skips fields with null values, no need to null check anymore
            .writeStringField("regex", regex)
            .writeEndObject());
    }

    private static String createRegexRequestBody(String key, String regex, String value, String groupForReplace) {
        return jsonWriteHelper(jsonWriter -> jsonWriter.writeStartObject()
            .writeStringField("key", key)
            .writeStringField("regex", regex)
            .writeStringField("value", value)
            .writeStringField("groupForReplace", groupForReplace)
            .writeEndObject());
    }

    /**
     * Creates a list of sanitizer requests to be sent to the test proxy server.
     *
     * @param sanitizers the list of sanitizers to be added.
     * @param proxyUrl The proxyUrl to use when constructing requests.
     * @return the list of sanitizer {@link HttpRequest requests} to be sent.
     * @throws RuntimeException if {@link TestProxySanitizerType} is not supported.
     * @deprecated Use {@link #createAddSanitizersRequest(List)} instead as this will create a bulk HttpRequest
     * for setting the sanitizers for a test proxy session instead of a request per sanitizer.
     */
    @Deprecated
    public static List<HttpRequest> getSanitizerRequests(List<TestProxySanitizer> sanitizers, URL proxyUrl) {
        return sanitizers.stream().map(testProxySanitizer -> {
            String requestBody;
            String sanitizerType;
            switch (testProxySanitizer.getType()) {
                case URL:
                    sanitizerType = TestProxySanitizerType.URL.getName();
                    requestBody = createRegexRequestBody(null, testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);

                case BODY_REGEX:
                    sanitizerType = BODY_REGEX.getName();
                    requestBody = createRegexRequestBody(null, testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);

                case BODY_KEY:
                    sanitizerType = BODY_KEY.getName();
                    requestBody = createBodyJsonKeyRequestBody(testProxySanitizer.getKey(),
                        testProxySanitizer.getRegex(), testProxySanitizer.getRedactedValue());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);

                case HEADER:
                    sanitizerType = HEADER.getName();
                    if (testProxySanitizer.getKey() == null && testProxySanitizer.getRegex() == null) {
                        throw new RuntimeException(
                            "Missing regexKey and/or headerKey for sanitizer type {" + sanitizerType + "}");
                    }
                    requestBody = createRegexRequestBody(testProxySanitizer.getKey(), testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUrl);

                default:
                    throw new RuntimeException("Sanitizer type {" + testProxySanitizer.getType() + "} not supported");
            }
        }).collect(Collectors.toList());
    }

    /**
     * Creates a request to bulk add sanitizers to the test proxy server.
     * <p>
     * For more information about adding bulk sanitizers see the
     * <a href="https://github.com/Azure/azure-sdk-tools/tree/feature/add-bulk-sanitizers/tools/test-proxy/Azure.Sdk.Tools.TestProxy#passing-sanitizers-in-bulk">Passing Sanitizers in Bulk</a>
     * wiki.
     *
     * @param sanitizers The list of sanitizers to be added.
     * @return The {@link HttpRequest request} to be sent.
     * @throws RuntimeException if {@link TestProxySanitizerType} is not supported.
     */
    public static HttpRequest createAddSanitizersRequest(List<TestProxySanitizer> sanitizers) {
        String requestBody = jsonWriteHelper(jsonWriter -> jsonWriter.writeArray(sanitizers, (writer, sanitizer) -> {
            switch (sanitizer.getType()) {
                case URL:
                    writer.writeStartObject()
                        .writeStringField("Name", TestProxySanitizerType.URL.getName())
                        .writeRawField("Body",
                            createRegexRequestBody(null, sanitizer.getRegex(), sanitizer.getRedactedValue(),
                                sanitizer.getGroupForReplace()))
                        .writeEndObject();
                    break;

                case BODY_REGEX:
                    writer.writeStartObject()
                        .writeStringField("Name", BODY_REGEX.getName())
                        .writeRawField("Body",
                            createRegexRequestBody(null, sanitizer.getRegex(), sanitizer.getRedactedValue(),
                                sanitizer.getGroupForReplace()))
                        .writeEndObject();
                    break;

                case BODY_KEY:
                    writer.writeStartObject()
                        .writeStringField("Name", BODY_KEY.getName())
                        .writeRawField("Body",
                            createBodyJsonKeyRequestBody(sanitizer.getKey(), sanitizer.getRegex(),
                                sanitizer.getRedactedValue()))
                        .writeEndObject();
                    break;

                case HEADER:
                    if (sanitizer.getKey() == null && sanitizer.getRegex() == null) {
                        throw new RuntimeException(
                            "Missing regexKey and/or headerKey for sanitizer type {" + HEADER + "}");
                    }

                    writer.writeStartObject()
                        .writeStringField("Name", HEADER.getName())
                        .writeRawField("Body",
                            createRegexRequestBody(sanitizer.getKey(), sanitizer.getRegex(),
                                sanitizer.getRedactedValue(), sanitizer.getGroupForReplace()))
                        .writeEndObject();
                    break;

                default:
                    throw new RuntimeException("Sanitizer type {" + sanitizer.getType() + "} not supported");
            }
        }));
        return new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/Admin/AddSanitizers")).setBody(requestBody);
    }

    /**
     * Creates a request to remove sanitizers from the request.
     * @return The {@link HttpRequest request} to be sent.
     */
    public static HttpRequest getRemoveSanitizerRequest() {
        return new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/Admin/RemoveSanitizers"))
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json");
    }

    private static HttpRequest createHttpRequest(String requestBody, String sanitizerType, URL proxyUrl) {
        return new HttpRequest(HttpMethod.POST, proxyUrl + "/Admin/AddSanitizer").setBody(requestBody)
            .setHeader(X_ABSTRACTION_IDENTIFIER, sanitizerType);
    }

    /**
     * Creates a {@link List} of {@link HttpRequest} to be sent to the test proxy to register matchers.
     * @param matchers The {@link TestProxyRequestMatcher}s to encode into requests.
     * @return The {@link HttpRequest}s to send to the proxy.
     * @throws RuntimeException The {@link TestProxyRequestMatcher.TestProxyRequestMatcherType} is unsupported.
     */
    public static List<HttpRequest> getMatcherRequests(List<TestProxyRequestMatcher> matchers) {
        return matchers.stream().map(testProxyMatcher -> {
            HttpRequest request = new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/Admin/setmatcher"));
            String matcherType;
            switch (testProxyMatcher.getType()) {
                case HEADERLESS:
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.HEADERLESS.getName();
                    break;

                case BODILESS:
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.BODILESS.getName();
                    break;

                case CUSTOM:
                    CustomMatcher customMatcher = (CustomMatcher) testProxyMatcher;
                    String requestBody = createCustomMatcherRequestBody(customMatcher);
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName();
                    request.setBody(requestBody);
                    break;

                default:
                    throw new RuntimeException("Matcher type {" + testProxyMatcher.getType() + "} not supported");
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
        HttpRequest request
            = new HttpRequest(HttpMethod.POST, createProxyRequestUrl("/Admin/setmatcher")).setBody(requestBody);

        request.setHeader(X_ABSTRACTION_IDENTIFIER,
            TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName());
        return request;
    }
}
