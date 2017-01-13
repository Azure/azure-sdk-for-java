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
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.resources.fluentcore.utils.SharedSettings;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockIntegrationTestBase {
    protected final static Boolean IS_MOCKED = true;
    protected final static Boolean IS_RECORD = !IS_MOCKED;

    private final static String RECORD_FOLDER = "session-records/";
    private final static String SUBSCRIPTION_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    protected final static String MOCK_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
    protected final static String MOCK_URI = "http://localhost:8043";

    private static Map<String, String> regexRules = new HashMap<String, String>();
    // Stores a map of all the HTTP properties in a session
    private static TestRecord testRecord;
    // A state machine ensuring a test is always reset before another one is setup
    private static String currentTestName = null;
    
    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8043);
    @Rule
    public WireMockRule instanceRule = wireMockRule;
    @Rule
    public TestName name = new TestName();
    public static Interceptor interceptor;

    /**
     * Add a regex rule to filter on URLs and response bodies. The replacement
     * for the pattern should be available in the URL.
     * @param regex the regex pattern that a match will be found in the URL
     */
    protected static void addRegexRule(String regex) {
        addRegexRule(regex, null);
    }

    protected static void addRegexRule(String regex, String replacement) {
        regexRules.put(regex, replacement);
    }

    /**
     * Turn on the test recording / mocking from this point on until
     * resetTest() is called. The current test name will be set as the name of
     * the current test running.
     *
     * Only call this if there's no other tests running. Otherwise the call
     * will be ignored and the traffic will be stored in the record file from
     * the last setupTest() call.
     *
     * @throws Exception
     */
    protected void setupTest() throws Exception {
        setupTest(name.getMethodName());
    }
    
    /**
     * Turn on the test recording / mocking from this point on until
     * resetTest() is called with the file name of the mock record specified.
     *
     * Only call this if there's no other tests running. Otherwise the call
     * will be ignored and the traffic will be stored in the record file from
     * the last setupTest() call.
     * 
     * Example:
     * public void setup() {
     *   setupTest("testA");
     *   cleanup();
     *   resetTest("testA");
     * }
     * 
     * public void cleanup() {
     *   setupTest("testB");
     *   getSomeTraffic();
     *   resetTest("testB");
     * }
     * 
     * The traffic from getSomeTraffic will be stored in testA.json.
     */
    protected static void setupTest(final String testName) throws Exception {
        if (currentTestName == null) {
            currentTestName = testName;
        } else {
            return;
        }
        SharedSettings.setResourceNamerFactory(new TestResourceNamerFactory());

        configureFor(wireMockRule.port());
        reset();

        if (IS_MOCKED) {
            File recordFile = getRecordFile();
            ObjectMapper mapper = new ObjectMapper();
            testRecord = mapper.readValue(recordFile, TestRecord.class);
        } else {
            testRecord = new TestRecord();
        }

        interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (IS_MOCKED) {
                    return registerRecordedResponse(chain);
                }

                return recordRequestAndResponse(chain);
            }
        };
    }

    private static Response registerRecordedResponse(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        for (Entry<String, String> rule : regexRules.entrySet()) {
            Matcher m = Pattern.compile(rule.getKey()).matcher(url);
            if (m.find()) {
                regexRules.put(rule.getKey(), m.group());
            }
        }
        try {
            synchronized (testRecord.networkCallRecords) {
                MockIntegrationTestBase.registerStub(request.method(), url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chain.proceed(chain.request());
    }

    private static Response recordRequestAndResponse(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        NetworkCallRecord networkCallRecord = new NetworkCallRecord();

        networkCallRecord.Headers = new HashMap<String, String>();

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
        networkCallRecord.Response = new HashMap<String, String>();
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
     * Turn off the mocking / recording setup by a previous setupTest() call.
     * @throws Exception
     */
    protected void resetTest() throws Exception {
        resetTest(name.getMethodName());
    }
    
    /**
     * Resets the test with name @testName.
     * This reset call is only valid for tests setup earlier with the same testName.
     * @param testName
     * @throws Exception
     */
    protected static void resetTest(String testName) throws Exception {
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
        
        reset();
        currentTestName = null;
    }
    
    private static void extractResponseData(Map<String, String> responseData, Response response) throws Exception {
        Map<String, List<String>> headers = response.headers().toMultimap();
        for (Entry<String, List<String>> header : headers.entrySet()) {
            String headerValueToStore = header.getValue().get(0);

            if (header.getKey().equalsIgnoreCase("location") || header.getKey().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyRegex(headerValueToStore);
            }
            if (header.getKey().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
            }
            responseData.put(header.getKey().toLowerCase(), headerValueToStore);
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
            responseData.put("Body", content);
        }
    }
    
    private static File getRecordFile() {
        URL folderUrl = MockIntegrationTestBase.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);
        if (!folderFile.exists()) folderFile.mkdir();
        String filePath = folderFile.getPath() + "/" + currentTestName + ".json";
        return new File(filePath);
    }
    private static String applyRegex(String text) {
        for (Entry<String, String> rule : regexRules.entrySet()) {
            if (rule.getValue() != null) {
                text = text.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        return text;
    }

    private static void registerStub(String incomingMethod, String incomingUrl) throws Exception {
        int index = 0;
        for (NetworkCallRecord record : testRecord.networkCallRecords) {
            if (record.Method.equalsIgnoreCase(incomingMethod) && record.Uri.equalsIgnoreCase(incomingUrl)) {
                break;
            }
            index++;
        }

        if (index >= testRecord.networkCallRecords.size()) {
            System.out.println(incomingMethod + " " + incomingUrl);
        }
        NetworkCallRecord networkCallRecord = testRecord.networkCallRecords.remove(index);
        String url = networkCallRecord.Uri.replace(MOCK_URI, "");

        UrlPattern urlPattern = urlEqualTo(url);
        String method = networkCallRecord.Method;
        MappingBuilder mBuilder = null;
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
        } else {
            throw new Exception("Invalid HTTP method.");
        }

        ResponseDefinitionBuilder rBuilder = aResponse().withStatus(Integer.parseInt(networkCallRecord.Response.get("StatusCode")));
        for (Entry<String, String> header : networkCallRecord.Response.entrySet()) {
            if (!header.getKey().equals("StatusCode") && !header.getKey().equals("Body") && !header.getKey().equals("Content-Length")) {
                String rawHeader = header.getValue();
                for (Entry<String, String> rule : regexRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                    }
                }
                rBuilder.withHeader(header.getKey(), rawHeader);
            }
        }
        
        String rawBody = networkCallRecord.Response.get("Body");
        if (rawBody != null) {
            for (Entry<String, String> rule : regexRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }
            rBuilder.withBody(rawBody);
            rBuilder.withHeader("Content-Length", String.valueOf(rawBody.getBytes("UTF-8").length));
        }

        mBuilder.willReturn(rBuilder);
        configureFor(wireMockRule.port());
        stubFor(mBuilder);
    }

    public static String generateRandomResourceName(String prefix, int maxLen) {
        if (IS_MOCKED) {
            return popVariable();
        }
        String randomName = ResourceNamer.randomResourceName(prefix, maxLen);
        pushVariable(randomName);
        return randomName;
    }

    public static void pushVariable(String variable) {
        if (IS_RECORD) {
            testRecord.variables.add(variable);
        }
    }

    public static String popVariable() {
        return testRecord.variables.remove();
    }
}
