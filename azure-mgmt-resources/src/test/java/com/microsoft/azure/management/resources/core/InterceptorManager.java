package com.microsoft.azure.management.resources.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import com.microsoft.rest.http.*;
import com.microsoft.rest.http.HttpClient.Configuration;
import com.microsoft.rest.policy.RequestPolicy;
import com.microsoft.rest.policy.RequestPolicy.Factory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by vlashch on 7/13/2017.
 */
public class InterceptorManager {

    private final static String RECORD_FOLDER = "session-records/";

    private Map<String, String> textReplacementRules = new HashMap<>();
    // Stores a map of all the HTTP properties in a session
    // A state machine ensuring a test is always reset before another one is setup

    protected RecordedData recordedData;

    private final String testName;

    private final TestBase.TestMode testMode;

    private InterceptorManager(String testName, TestBase.TestMode testMode) {
        this.testName = testName;
        this.testMode = testMode;
    }

    public void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }

    // factory method
    public static InterceptorManager create(String testName, TestBase.TestMode testMode) throws IOException {
        InterceptorManager interceptorManager = new InterceptorManager(testName, testMode);
        SdkContext.setResourceNamerFactory(new TestResourceNamerFactory(interceptorManager));
        SdkContext.setDelayProvider(new TestDelayProvider(interceptorManager.isRecordMode()));
        SdkContext.setRxScheduler(Schedulers.trampoline());

        return interceptorManager;
    }

    public boolean isRecordMode() {
        return testMode == TestBase.TestMode.RECORD;
    }

    public boolean isPlaybackMode() {
        return testMode == TestBase.TestMode.PLAYBACK;
    }

    public RecordPolicyFactory initRecordPolicy() {
        recordedData = new RecordedData();
        return new RecordPolicyFactory();
    }

    public HttpClient.Factory initPlaybackFactory() throws IOException {
        readDataFromFile();
        return new PlaybackClientFactory();
    }

    public void finalizeInterceptor() throws IOException {
        switch (testMode) {
            case RECORD:
                writeDataToFile();
                break;
            case PLAYBACK:
                // Do nothing
                break;
            default:
                System.out.println("==> Unknown AZURE_TEST_MODE: " + testMode);
        };
    }


    class RecordPolicyFactory implements RequestPolicy.Factory {
        @Override
        public RequestPolicy create(RequestPolicy next) {
            return new RecordPolicy(next);
        }
    }

    class RecordPolicy implements RequestPolicy {

        final RequestPolicy next;
        private RecordPolicy(RequestPolicy next) {
            this.next = next;
        }

        public Single<HttpResponse> sendAsync(HttpRequest request) {
            final NetworkCallRecord networkCallRecord = new NetworkCallRecord();

            networkCallRecord.Headers = new HashMap<>();

            if (request.headers().value("Content-Type") != null) {
                networkCallRecord.Headers.put("Content-Type", request.headers().value("Content-Type"));
            }
            if (request.headers().value("x-ms-version") != null) {
                networkCallRecord.Headers.put("x-ms-version", request.headers().value("x-ms-version"));
            }
            if (request.headers().value("User-Agent") != null) {
                networkCallRecord.Headers.put("User-Agent", request.headers().value("User-Agent"));
            }

            networkCallRecord.Method = request.httpMethod();
            networkCallRecord.Uri = applyReplacementRule(request.url().replaceAll("\\?$", ""));

            return next.sendAsync(request).flatMap(new Func1<HttpResponse, Single<HttpResponse>>() {
                @Override
                public Single<HttpResponse> call(HttpResponse response) {
                    networkCallRecord.Response = new HashMap<>();
                    networkCallRecord.Response.put("StatusCode", Integer.toString(response.statusCode()));
                    return extractResponseData(networkCallRecord.Response, response);
                }
            }).doOnSuccess(new Action1<HttpResponse>() {
                @Override
                public void call(HttpResponse httpResponse) {
                    // remove pre-added header if this is a waiting or redirection
                    if (networkCallRecord.Response.get("Body").contains("<Status>InProgress</Status>")
                            || Integer.parseInt(networkCallRecord.Response.get("StatusCode")) == HttpStatus.SC_TEMPORARY_REDIRECT) {
                        // Do nothing
                    } else {
                        synchronized (recordedData.getNetworkCallRecords()) {
                            recordedData.getNetworkCallRecords().add(networkCallRecord);
                        }
                    }
                }
            });
        }
    }

    final class PlaybackClientFactory implements HttpClient.Factory {
        @Override
        public HttpClient create(Configuration configuration) {
            return new PlaybackClient(configuration.policyFactories());
        }
    }

    final class PlaybackClient extends HttpClient{
        PlaybackClient(List<RequestPolicy.Factory> policyFactories) {
            super(policyFactories);
        }

        @Override
        public Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
            String incomingUrl = applyReplacementRule(request.url());
            String incomingMethod = request.httpMethod();

            incomingUrl = removeHost(incomingUrl);
            NetworkCallRecord networkCallRecord = null;
            synchronized (recordedData) {
                for (Iterator<NetworkCallRecord> iterator = recordedData.getNetworkCallRecords().iterator(); iterator.hasNext(); ) {
                    NetworkCallRecord record = iterator.next();
                    if (record.Method.equalsIgnoreCase(incomingMethod) && removeHost(record.Uri).equalsIgnoreCase(incomingUrl)) {
                        networkCallRecord = record;
                        iterator.remove();
                        break;
                    }
                }
            }

            if (networkCallRecord == null) {
                System.out.println("NOT FOUND - " + incomingMethod + " " + incomingUrl);
                System.out.println("Remaining records " + recordedData.getNetworkCallRecords().size());
                return Single.error(new IOException("==> Unexpected request: " + incomingMethod + " " + incomingUrl));
            }

            int recordStatusCode = Integer.parseInt(networkCallRecord.Response.get("StatusCode"));
            HttpHeaders headers = new HttpHeaders();

            for (Map.Entry<String, String> pair : networkCallRecord.Response.entrySet()) {
                if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body") && !pair.getKey().equals("Content-Length")) {
                    String rawHeader = pair.getValue();
                    for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                        if (rule.getValue() != null) {
                            rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                        }
                    }
                    headers.add(pair.getKey(), rawHeader);
                }
            }

            String rawBody = networkCallRecord.Response.get("Body");
            if (rawBody != null) {
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                    }
                }

                try {
                    byte[] bytes = rawBody.getBytes("UTF-8");
                    headers.add("Content-Length", String.valueOf(bytes.length));
                } catch (IOException e) {
                    return Single.error(e);
                }
            }

            HttpResponse response = new MockHttpResponse(recordStatusCode, headers, rawBody);
            return Single.just(response);
        }
    }

    private Single<HttpResponse> extractResponseData(final Map<String, String> responseData, final HttpResponse response) {
        HttpHeaders headers = response.headers();
        boolean addedRetryAfter = false;
        for (HttpHeader header : headers) {
            String headerValueToStore = header.value();

            if (header.name().equalsIgnoreCase("location") || header.name().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyReplacementRule(headerValueToStore);
            }
            if (header.name().equalsIgnoreCase("retry-after")) {
                headerValueToStore = "0";
                addedRetryAfter = true;
            }
            responseData.put(header.name().toLowerCase(), headerValueToStore);
        }

        if (!addedRetryAfter) {
            responseData.put("retry-after", "0");
        }

        Single<HttpResponse> result;
        if (response.headerValue("Content-Encoding") == null) {
            result = response.bodyAsStringAsync().map(new Func1<String, HttpResponse>() {
                @Override
                public HttpResponse call(String content) {
                    content = applyReplacementRule(content);
                    responseData.put("Body", content);
                    return new MockHttpResponse(response.statusCode(), response.headers(), content);
                }
            });
        } else {
            result = response.bodyAsInputStreamAsync().map(new Func1<InputStream, HttpResponse>() {
                @Override
                public HttpResponse call(InputStream inputStream) {
                    try {
                        GZIPInputStream gis = new GZIPInputStream(inputStream);
                        String content = IOUtils.toString(gis);
                        responseData.remove("Content-Encoding".toLowerCase());
                        responseData.put("Content-Length".toLowerCase(), Integer.toString(content.length()));

                        content = applyReplacementRule(content);
                        responseData.put("Body", content);
                        return new MockHttpResponse(response.statusCode(), response.headers(), content);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                }
            });
        }

        return result;
    }

    private void readDataFromFile() throws IOException {
        File recordFile = getRecordFile(testName);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        recordedData = mapper.readValue(recordFile, RecordedData.class);
        System.out.println("Total records " + recordedData.getNetworkCallRecords().size());
    }

    private void writeDataToFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        File recordFile = getRecordFile(testName);
        recordFile.createNewFile();
        mapper.writeValue(recordFile, recordedData);
    }

    private File getRecordFile(String testName) {
        URL folderUrl = InterceptorManager.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        String filePath = folderFile.getPath() + "/" + testName + ".json";
        System.out.println("==> Playback file path: " + filePath);
        return new File(filePath);
    }

    private String applyReplacementRule(String text) {
        for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
            if (rule.getValue() != null) {
                text = text.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        return text;
    }

    private String removeHost(String url) {
        URI uri = URI.create(url);
        return String.format("%s?%s", uri.getPath(), uri.getQuery());
    }

    public void pushVariable(String variable) {
        if (isRecordMode()) {
            synchronized (recordedData.getVariables()) {
                recordedData.getVariables().add(variable);
            }
        }
    }

    public String popVariable() {
        synchronized (recordedData.getVariables()) {
            return recordedData.getVariables().remove();
        }
    }
}
