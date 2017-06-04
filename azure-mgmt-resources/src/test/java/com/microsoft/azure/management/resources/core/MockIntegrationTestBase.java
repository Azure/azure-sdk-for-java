/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MockIntegrationTestBase {
    public static Boolean IS_MOCKED = IsMocked();
    protected static Boolean IS_RECORD = !IS_MOCKED;
    final static String MOCK_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    final static String MOCK_TENANT = "00000000-0000-0000-0000-000000000000";
    private final static String HOST = "localhost";
    private final static String RECORD_FOLDER = "session-records/";

    protected WireMock wireMock;

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());
    @Rule
    public WireMockRule instanceRule = wireMockRule;

    private Map<String, String> textReplacementRules = new HashMap<String, String>();
    // Stores a map of all the HTTP properties in a session
    protected TestRecord testRecord;
    // A state machine ensuring a test is always reset before another one is setup
    private String currentTestName = null;

    @Rule
    public TestName name = new TestName();
    private Interceptor interceptor;

    protected Interceptor interceptor() {
        return this.interceptor;
    }

    protected String mockUri() {
        return "http://" + HOST + ":" + this.instanceRule.port();
    }

    protected void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }

    protected void setupTest(final String testName) throws Exception {
        if (currentTestName == null) {
            currentTestName = testName;
        } else {
            throw new Exception("Setting up another test in middle of a test");
        }
        SdkContext.setResourceNamerFactory(new TestResourceNamerFactory(this));
        SdkContext.setDelayProvider(new TestDelayProvider());
        SdkContext.setRxScheduler(Schedulers.trampoline());

        int retries = 10;
        while (retries > 0) {
            retries--;
            try {
                wireMock = new WireMock(HOST, wireMockRule.port());
                wireMock.resetMappings();
                break;
            }
            catch (Exception ex) {
                Thread.sleep(3000);
            }
        }
        if (IS_MOCKED) {
            File recordFile = getRecordFile();
            ObjectMapper mapper = new ObjectMapper();
            testRecord = mapper.readValue(recordFile, TestRecord.class);
            System.out.println("Total records " + testRecord.networkCallRecords.size());
        } else {
            testRecord = new TestRecord();
        }

        this.interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (IS_MOCKED) {
                    return registerRecordedResponse(chain);
                }

                return recordRequestAndResponse(chain);
            }
        };
    }

    private synchronized Response registerRecordedResponse(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        url = applyRegex(url);
        try {
            synchronized (testRecord.networkCallRecords) {
                registerStub(request.method(), url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chain.proceed(chain.request());
    }

    private Response recordRequestAndResponse(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        NetworkCallRecord networkCallRecord = new NetworkCallRecord();

        networkCallRecord.Headers = new HashMap<>();

        try {
            if (request.header("Content-Type") != null) {
                networkCallRecord.Headers.put("Content-Type", request.header("Content-Type"));
            }
            if (request.header("x-ms-version") != null) {
                networkCallRecord.Headers.put("x-ms-version", request.header("x-ms-version"));
            }
            if (request.header("User-Agent") != null) {
                networkCallRecord.Headers.put("User-Agent", request.header("User-Agent"));
            }

            networkCallRecord.Method = request.method();
            networkCallRecord.Uri = applyRegex(request.url().toString().replaceAll("\\?$", ""));

        } catch (Exception e) {
        }
        Response response = chain.proceed(chain.request());

        networkCallRecord.Response = new HashMap<>();
        try {
            networkCallRecord.Response.put("StatusCode", Integer.toString(response.code()));
            extractResponseData(networkCallRecord.Response, response);

            // remove pre-added header if this is a waiting or redirection
            if (networkCallRecord.Response.get("Body").contains("<Status>InProgress</Status>") ||
                    Integer.parseInt(networkCallRecord.Response.get("StatusCode")) == HttpStatus.SC_TEMPORARY_REDIRECT) {
            } else {
                synchronized (testRecord.networkCallRecords) {
                    testRecord.networkCallRecords.add(networkCallRecord);
                }
            }
        } catch (Exception e) {
        }
        return response;
    }

    /**
     * Resets the test with name @testName.
     * This reset call is only valid for tests setup earlier with the same testName.
     * @param testName
     * @throws Exception
     */
    protected void resetTest(String testName) throws Exception {
        if (!currentTestName.equals(testName)) {
            return;
        }

        if (IS_RECORD) {
            // Write current context to file
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            File recordFile = getRecordFile();
            recordFile.createNewFile();
            mapper.writeValue(recordFile, testRecord);
        }

        wireMock.resetMappings();
        testRecord = null;
        currentTestName = null;
    }

    private void extractResponseData(Map<String, String> responseData, Response response) throws Exception {
        Map<String, List<String>> headers = response.headers().toMultimap();
        boolean addedRetryAfter = false;
        for (Entry<String, List<String>> header : headers.entrySet()) {
            String headerValueToStore = header.getValue().get(0);

            if (header.getKey().equalsIgnoreCase("location") || header.getKey().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyRegex(headerValueToStore);
            }
            if (header.getKey().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.getKey().toLowerCase(), headerValueToStore);
        }
        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        BufferedSource bufferedSource = response.body().source();
        bufferedSource.request(9223372036854775807L);
        Buffer buffer = bufferedSource.buffer().clone();
        String content = null;

        if (response.header("Content-Encoding") == null) {
            content = new String(buffer.readString(Util.UTF_8));
        }
        else if (response.header("Content-Encoding").equalsIgnoreCase("gzip")) {
            GZIPInputStream gis = new GZIPInputStream(buffer.inputStream());
            content = IOUtils.toString(gis);
            responseData.remove("Content-Encoding".toLowerCase());
            responseData.put("Content-Length".toLowerCase(), Integer.toString(content.length()));
        }
        if (content != null) {
            content = applyRegex(content);
            responseData.put("Body", content);
        }
    }

    private File getRecordFile() {
        URL folderUrl = MockIntegrationTestBase.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        String filePath = folderFile.getPath() + "/" + currentTestName + ".json";
        return new File(filePath);
    }

    private String applyRegex(String text) {
        for (Entry<String, String> rule : textReplacementRules.entrySet()) {
            if (rule.getValue() != null) {
                text = text.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        return text;
    }

    private String removeHost(String url) {
        url = url.replace("http://" + HOST + ":", "");
        url = url.substring(url.indexOf("/"));

        return url;
    }

    private void registerStub(String incomingMethod, String incomingUrl) throws Exception {
        int index = 0;
        incomingUrl = removeHost(incomingUrl);
        for (NetworkCallRecord record : testRecord.networkCallRecords) {
            if (record.Method.equalsIgnoreCase(incomingMethod) && removeHost(record.Uri).equalsIgnoreCase(incomingUrl)) {
                break;
            }
            index++;
        }

        if (index >= testRecord.networkCallRecords.size()) {
            System.out.println("NOT FOUND - " + incomingMethod + " " + incomingUrl);
            System.out.println("Remaining records " + testRecord.networkCallRecords.size());
            return;
        }

        NetworkCallRecord networkCallRecord = testRecord.networkCallRecords.remove(index);
        String url = removeHost(networkCallRecord.Uri);

        UrlPattern urlPattern = urlEqualTo(url);
        String method = networkCallRecord.Method;
        MappingBuilder mBuilder;
        if (method.equals("GET")) {
            mBuilder = get(urlPattern);
        } else if (method.equals("POST")) {
            mBuilder = post(urlPattern);
        } else if (method.equals("PUT")) {
            mBuilder = put(urlPattern);
        } else if (method.equals("DELETE")) {
            mBuilder = delete(urlPattern);
        } else if (method.equals("PATCH")) {
            mBuilder = patch(urlPattern);
        } else if (method.equals("HEAD")) {
            mBuilder = head(urlPattern);
        } else {
            throw new Exception("Invalid HTTP method.");
        }

        ResponseDefinitionBuilder rBuilder = aResponse().withStatus(Integer.parseInt(networkCallRecord.Response.get("StatusCode")));
        for (Entry<String, String> header : networkCallRecord.Response.entrySet()) {
            if (!header.getKey().equals("StatusCode") && !header.getKey().equals("Body") && !header.getKey().equals("Content-Length")) {
                String rawHeader = header.getValue();
                for (Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                    }
                }
                rBuilder.withHeader(header.getKey(), rawHeader);
            }
        }

        String rawBody = networkCallRecord.Response.get("Body");
        if (rawBody != null) {
            for (Entry<String, String> rule : textReplacementRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }
            rBuilder.withBody(rawBody);
            rBuilder.withHeader("Content-Length", String.valueOf(rawBody.getBytes("UTF-8").length));
        }

        mBuilder.willReturn(rBuilder);
        wireMock.register(mBuilder);
    }

    public void pushVariable(String variable) {
        if (IS_RECORD) {
            synchronized (testRecord.variables) {
                testRecord.variables.add(variable);
            }
        }
    }

    public String popVariable() {
        synchronized (testRecord.variables) {
            return testRecord.variables.remove();
        }
    }

    private static Boolean IsMocked() {
        String azureTestMode = System.getenv("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            if (azureTestMode.equalsIgnoreCase("RECORD")) {
                return false;
            }
        }

        // Return false to record.
        return true;
    }
}
