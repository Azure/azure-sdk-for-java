// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test.utils;

import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.models.TestProxySanitizerType;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.StringBinaryData;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.UriBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
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

import static com.azure.v2.core.test.implementation.TestingHelpers.X_RECORDING_ID;
import static com.azure.v2.core.test.models.TestProxySanitizerType.HEADER;
import static com.azure.v2.core.test.policy.TestProxyRecordPolicy.RECORD_MODE;

/**
 * Utility functions for interaction with the test proxy.
 */
public final class TestProxyUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestProxyUtils.class);
    private static final HttpHeaderName X_RECORDING_SKIP = HttpHeaderName.fromString("x-recording-skip");
    private static final String REDACTED_VALUE = "REDACTED";
    private static final String URI_REGEX = "(?<=http://|https://)([^/?]+)";

    // These are prepended with "$.." creating a Jsonpath expression.
    private static final List<String> JSON_BODY_KEYS_TO_REDACT = Arrays.asList("authHeader", "accountKey",
        "accessToken", "accountName", "applicationId", "apiKey", "client_secret", "connectionString", "url", "host",
        "password", "userName", "applicationSecret", "aliasSecondaryConnectionString", "aliasPrimaryConnectionString",
        "primaryKey", "secondaryKey", "adminPassword.value", "administratorLoginPassword", "runAsPassword",
        "adminPassword", "accessSAS", "WEBSITE_AUTH_ENCRYPTION_KEY", "decryptionKey", "primaryMasterKey",
        "primaryReadonlyMasterKey", "secondaryMasterKey", "secondaryReadonlyMasterKey", "certificatePassword",
        "clientSecret", "keyVaultClientSecret", "authHeader", "httpHeader", "encryptedCredential", "functionKey",
        "atlasKafkaPrimaryEndpoint", "atlasKafkaSecondaryEndpoint", "certificatePassword", "storageAccountPrimaryKey",
        "privateKey", "fencingClientPassword", "acrToken", "scriptUrlSasToken", "azureBlobSource.containerUrl",
        "properties.DOCKER_REGISTRY_SEVER_PASSWORD");

    private static final String HOST_NAME_REGEX = "(?<=http://|https://)(?<host>[^/?\\\\.]+)";
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

    // Values in this list must have a capture group named "secret" for the redaction to work.
    private static final List<String> BODY_REGEXES_TO_REDACT
        = Arrays.asList("(?:(Password|User ID)=)(?<secret>.*)(?:;)", "client_secret=(?<secret>[^&]+)",
            "<PrimaryKey>(?<secret>.*?)</PrimaryKey>", "<SecondaryKey>(?<secret>.*?)</SecondaryKey>",
            "<UserDelegationKey>.*?<SignedOid>(?<secret>.*?)</SignedOid>.*?</UserDelegationKey>",
            "<UserDelegationKey>.*?<SignedTid>(?<secret>.*?)</SignedTid>.*?</UserDelegationKey>",
            "<UserDelegationKey>.*?<Value>(?<secret>.*?)</Value>.*?</UserDelegationKey>",
            "SharedAccessKey=(?<secret>[^;\\\"]+)", "AccountKey=(?<secret>[^;\\\"]+)", "accesskey=(?<secret>[^;\\\"]+)",
            "AccessKey=(?<secret>[^;\\\"]+)", "Secret=(?<secret>[^;\\\"]+)", "access_token=(?<secret>.*?)(?=&|$)",
            "refresh_token=(?<secret>.*?)(?=&|$)");

    private static final List<String> HEADER_KEYS_TO_REDACT = Arrays.asList("Ocp-Apim-Subscription-Key", "api-key",
        "x-api-key", "subscription-key", "x-ms-encryption-key", "sshPassword");

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

    private static volatile URI proxyUri;

    /**
     * Adds headers required for communication with the test proxy.
     *
     * @param request The request to add headers to.
     * @param proxyUri The {@link URI} the proxy lives at.
     * @param xRecordingId The x-recording-id value for the current session.
     * @param mode The current test proxy mode.
     * @param skipRecordingRequestBody Flag indicating to skip recording request bodies when tests run in Record mode.
     * @throws RuntimeException Construction of one of the URIs failed.
     */
    public static void changeHeaders(HttpRequest request, URI proxyUri, String xRecordingId, String mode,
        boolean skipRecordingRequestBody) {
        HttpHeader upstreamUri = request.getHeaders().get(X_RECORDING_UPSTREAM_BASE_URI);

        UriBuilder proxyUriBuilder = UriBuilder.parse(request.getUri());
        proxyUriBuilder.setScheme(proxyUri.getScheme());
        proxyUriBuilder.setHost(proxyUri.getHost());
        if (proxyUri.getPort() != -1) {
            proxyUriBuilder.setPort(proxyUri.getPort());
        }

        UriBuilder originalUriBuilder = UriBuilder.parse(request.getUri());
        originalUriBuilder.setPath("");
        originalUriBuilder.setQuery("");

        try {
            URI originalUri = originalUriBuilder.toUri();

            HttpHeaders headers = request.getHeaders();
            if (upstreamUri == null) {
                headers.set(X_RECORDING_UPSTREAM_BASE_URI, originalUri.toString());
                headers.set(X_RECORDING_MODE, mode);
                headers.set(X_RECORDING_ID, xRecordingId);
                if (mode.equals(RECORD_MODE) && skipRecordingRequestBody) {
                    headers.set(X_RECORDING_SKIP, "request-body");
                }
            }

            request.setUri(proxyUriBuilder.toUri());
        } catch (URISyntaxException e) {
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
     * Sets the response URI back to the original URI before returning it through the pipeline.
     * @param response The {@link Response} to modify.
     * @return The modified response.
     * @throws RuntimeException Construction of one of the URIs failed.
     */
    public static Response<BinaryData> resetTestProxyData(Response<BinaryData> response) {
        HttpRequest responseRequest = response.getRequest();
        HttpHeaders requestHeaders = responseRequest.getHeaders();
        try {
            URI originalUri = UriBuilder.parse(requestHeaders.getValue(X_RECORDING_UPSTREAM_BASE_URI)).toUri();
            UriBuilder currentUri = UriBuilder.parse(responseRequest.getUri());
            currentUri.setScheme(originalUri.getScheme());
            currentUri.setHost(originalUri.getHost());
            int port = originalUri.getPort();
            if (port == -1) {
                currentUri.setPort(""); // empty string is no port.
            } else {
                currentUri.setPort(port);
            }
            responseRequest.setUri(currentUri.toUri());

            requestHeaders.remove(X_RECORDING_UPSTREAM_BASE_URI);
            requestHeaders.remove(X_RECORDING_MODE);
            requestHeaders.remove(X_RECORDING_SKIP);
            requestHeaders.remove(X_RECORDING_ID);
            return response;
        } catch (URISyntaxException e) {
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
     * @param response The {@link Response} from the test proxy.
     */
    public static void checkForTestProxyErrors(Response<BinaryData> response) {
        String error = response.getHeaders().getValue(X_REQUEST_MISMATCH_ERROR);
        if (error == null) {
            error = response.getHeaders().getValue(X_REQUEST_KNOWN_EXCEPTION_ERROR);
        }
        if (error == null) {
            error = response.getHeaders().getValue(X_REQUEST_EXCEPTION_EXCEPTION_ERROR);
        }
        if (error != null) {
            throw LOGGER.throwableAtError()
                .addKeyValue("error", new String(Base64.getDecoder().decode(error), StandardCharsets.UTF_8))
                .log("Test proxy exception", RuntimeException::new);
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
     * Gets the current URI for the test proxy.
     * @return The {@link URI} location of the test proxy.
     * @throws RuntimeException The URI could not be constructed.
     */
    public static URI getProxyUri() {
        if (proxyUri != null) {
            return proxyUri;
        }
        UriBuilder builder = new UriBuilder();
        builder.setHost("localhost");
        builder.setScheme("http");
        builder.setPort(5000);
        try {
            proxyUri = builder.toUri();
            return proxyUri;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers the default set of sanitizers for sanitizing request and responses
     * @return the list of default sanitizers to be added.
     */
    public static List<TestProxySanitizer> loadSanitizers() {
        List<TestProxySanitizer> sanitizers = addDefaultRegexSanitizers();
        sanitizers.add(addDefaultUriSanitizer());
        sanitizers.addAll(addDefaultBodySanitizers());
        sanitizers.addAll(addDefaultHeaderKeySanitizers());
        return sanitizers;
    }

    private static String createCustomMatcherRequestBody(CustomMatcher customMatcher) {
        return String.format(
            "{\"ignoredHeaders\":\"%s\",\"excludedHeaders\":\"%s\",\"compareBodies\":%s,\"ignoredQueryParameters\":\"%s\",\"ignoreQueryOrdering\":%s}",
            getCommaSeperatedString(customMatcher.getHeadersKeyOnlyMatch()),
            getCommaSeperatedString(customMatcher.getExcludedHeaders()), customMatcher.isComparingBodies(),
            getCommaSeperatedString(customMatcher.getIgnoredQueryParameters()), customMatcher.isQueryOrderingIgnored());
    }

    private static String getCommaSeperatedString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        return stringList.stream().filter(s -> !CoreUtils.isNullOrEmpty(s)).collect(Collectors.joining(","));
    }

    private static String createBodyJsonKeyRequestBody(String jsonKey, String regex, String redactedValue) {
        if (regex == null) {
            return String.format("{\"value\":\"%s\",\"jsonPath\":\"%s\"}", redactedValue, jsonKey);
        } else {
            return String.format("{\"value\":\"%s\",\"jsonPath\":\"%s\",\"regex\":\"%s\"}", redactedValue, jsonKey,
                regex);
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
     * @param proxyUri The proxyUri to use when constructing requests.
     * @return the list of sanitizer {@link HttpRequest requests} to be sent.
     * @throws RuntimeException if {@link TestProxySanitizerType} is not supported.
     * @deprecated Use {@link #createAddSanitizersRequest(List, URI)} instead as this will create a bulk HttpRequest
     * for setting the sanitizers for a test proxy session instead of a request per sanitizer.
     */
    @Deprecated
    public static List<HttpRequest> getSanitizerRequests(List<TestProxySanitizer> sanitizers, URI proxyUri) {
        return sanitizers.stream().map(testProxySanitizer -> {
            String requestBody;
            String sanitizerType;
            switch (testProxySanitizer.getType()) {
                case URI:
                    sanitizerType = TestProxySanitizerType.URI.getName();
                    requestBody = createRegexRequestBody(null, testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUri);

                case BODY_REGEX:
                    sanitizerType = TestProxySanitizerType.BODY_REGEX.getName();
                    requestBody = createRegexRequestBody(null, testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUri);

                case BODY_KEY:
                    sanitizerType = TestProxySanitizerType.BODY_KEY.getName();
                    requestBody = createBodyJsonKeyRequestBody(testProxySanitizer.getKey(),
                        testProxySanitizer.getRegex(), testProxySanitizer.getRedactedValue());
                    return createHttpRequest(requestBody, sanitizerType, proxyUri);

                case HEADER:
                    sanitizerType = HEADER.getName();
                    if (testProxySanitizer.getKey() == null && testProxySanitizer.getRegex() == null) {
                        throw new RuntimeException(
                            "Missing regexKey and/or headerKey for sanitizer type {" + sanitizerType + "}");
                    }
                    requestBody = createRegexRequestBody(testProxySanitizer.getKey(), testProxySanitizer.getRegex(),
                        testProxySanitizer.getRedactedValue(), testProxySanitizer.getGroupForReplace());
                    return createHttpRequest(requestBody, sanitizerType, proxyUri);

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
     * @param proxyUri The proxyUri to use when constructing requests.
     * @return The {@link HttpRequest request} to be sent.
     * @throws RuntimeException if {@link TestProxySanitizerType} is not supported.
     */
    public static HttpRequest createAddSanitizersRequest(List<TestProxySanitizer> sanitizers, URI proxyUri) {
        List<String> sanitizersJsonPayloads = new ArrayList<>(sanitizers.size());

        for (TestProxySanitizer sanitizer : sanitizers) {
            String requestBody;
            String sanitizerType;
            switch (sanitizer.getType()) {
                case URI:
                    sanitizerType = TestProxySanitizerType.URI.getName();
                    requestBody = createRegexRequestBody(null, sanitizer.getRegex(), sanitizer.getRedactedValue(),
                        sanitizer.getGroupForReplace());
                    break;

                case BODY_REGEX:
                    sanitizerType = TestProxySanitizerType.BODY_REGEX.getName();
                    requestBody = createRegexRequestBody(null, sanitizer.getRegex(), sanitizer.getRedactedValue(),
                        sanitizer.getGroupForReplace());
                    break;

                case BODY_KEY:
                    sanitizerType = TestProxySanitizerType.BODY_KEY.getName();
                    requestBody = createBodyJsonKeyRequestBody(sanitizer.getKey(), sanitizer.getRegex(),
                        sanitizer.getRedactedValue());
                    break;

                case HEADER:
                    sanitizerType = HEADER.getName();
                    if (sanitizer.getKey() == null && sanitizer.getRegex() == null) {
                        throw new RuntimeException(
                            "Missing regexKey and/or headerKey for sanitizer type {" + sanitizerType + "}");
                    }
                    requestBody = createRegexRequestBody(sanitizer.getKey(), sanitizer.getRegex(),
                        sanitizer.getRedactedValue(), sanitizer.getGroupForReplace());
                    break;

                default:
                    throw new RuntimeException("Sanitizer type {" + sanitizer.getType() + "} not supported");
            }

            sanitizersJsonPayloads.add("{\"Name\":\"" + sanitizerType + "\",\"Body\":" + requestBody + "}");
        }

        String requestBody = "[" + CoreUtils.stringJoin(",", sanitizersJsonPayloads) + "]";
        return new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(proxyUri + "/Admin/AddSanitizers")
            .setBody(new StringBinaryData(requestBody));
    }

    /**
     * Creates a request to remove sanitizers from the request.
     * @return The {@link HttpRequest request} to be sent.
     */
    public static HttpRequest getRemoveSanitizerRequest() {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST).setUri(proxyUri + "/Admin/RemoveSanitizers");
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        return request;
    }

    private static HttpRequest createHttpRequest(String requestBody, String sanitizerType, URI proxyUri) {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(proxyUri + "/Admin/RemoveSanitizers")
            .setBody(new StringBinaryData(requestBody));
        request.getHeaders().set(X_ABSTRACTION_IDENTIFIER, sanitizerType);
        return request;
    }

    /**
     * Creates a {@link List} of {@link HttpRequest} to be sent to the test proxy to register matchers.
     * @param matchers The {@link TestProxyRequestMatcher}s to encode into requests.
     * @param proxyUri The proxyUri to use when constructing requests.
     * @return The {@link HttpRequest}s to send to the proxy.
     * @throws RuntimeException The {@link TestProxyRequestMatcher.TestProxyRequestMatcherType} is unsupported.
     */
    public static List<HttpRequest> getMatcherRequests(List<TestProxyRequestMatcher> matchers, URI proxyUri) {
        return matchers.stream().map(testProxyMatcher -> {
            HttpRequest request;
            String matcherType;
            switch (testProxyMatcher.getType()) {
                case HEADERLESS:
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.HEADERLESS.getName();
                    request = new HttpRequest().setMethod(HttpMethod.POST).setUri(proxyUri + "/Admin/setmatcher");
                    break;

                case BODILESS:
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.BODILESS.getName();
                    request = new HttpRequest().setMethod(HttpMethod.POST).setUri(proxyUri + "/Admin/setmatcher");
                    break;

                case CUSTOM:
                    CustomMatcher customMatcher = (CustomMatcher) testProxyMatcher;
                    String requestBody = createCustomMatcherRequestBody(customMatcher);
                    matcherType = TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName();
                    request = new HttpRequest().setMethod(HttpMethod.POST)
                        .setUri(proxyUri + "/Admin/setmatcher")
                        .setBody(new StringBinaryData(requestBody));
                    break;

                default:
                    throw new RuntimeException("Matcher type {" + testProxyMatcher.getType() + "} not supported");
            }

            request.getHeaders().set(X_ABSTRACTION_IDENTIFIER, matcherType);
            return request;
        }).collect(Collectors.toList());
    }

    /**
     * Set comparing bodies to false when running in playback and RecordWithoutRequestBody is set for the test.
     * @return the HttpRequest for setting compare bodies matcher to false.
     */
    public static HttpRequest setCompareBodiesMatcher() {
        String requestBody = createCustomMatcherRequestBody(new CustomMatcher().setComparingBodies(false));
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST)
            .setUri(proxyUri + "/Admin/setmatcher")
            .setBody(new StringBinaryData(requestBody));

        request.getHeaders()
            .set(X_ABSTRACTION_IDENTIFIER, TestProxyRequestMatcher.TestProxyRequestMatcherType.CUSTOM.getName());
        return request;
    }

    private static TestProxySanitizer addDefaultUriSanitizer() {
        return new TestProxySanitizer(URI_REGEX, REDACTED_VALUE, TestProxySanitizerType.URI);
    }

    private static List<TestProxySanitizer> addDefaultBodySanitizers() {
        return JSON_BODY_KEYS_TO_REDACT.stream()
            .map(jsonProperty -> new TestProxySanitizer("$.." + jsonProperty, null, REDACTED_VALUE,
                TestProxySanitizerType.BODY_KEY))
            .collect(Collectors.toList());
    }

    private static List<TestProxySanitizer> addDefaultRegexSanitizers() {
        List<TestProxySanitizer> regexSanitizers = new ArrayList<>();

        regexSanitizers.addAll(BODY_REGEXES_TO_REDACT.stream()
            .map(regex -> new TestProxySanitizer(regex, REDACTED_VALUE, TestProxySanitizerType.BODY_REGEX)
                .setGroupForReplace("secret"))
            .collect(Collectors.toList()));

        regexSanitizers.addAll(HEADER_KEY_REGEX_TO_REDACT);

        return regexSanitizers;
    }

    private static List<TestProxySanitizer> addDefaultHeaderKeySanitizers() {
        return HEADER_KEYS_TO_REDACT.stream()
            .map(headerKey -> new TestProxySanitizer(headerKey, null, REDACTED_VALUE, HEADER))
            .collect(Collectors.toList());
    }

    private TestProxyUtils() {
    }
}
