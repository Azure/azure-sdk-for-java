package com.microsoft.azure.management.resources.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import okhttp3.*;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by vlashch on 7/13/2017.
 */
public class InterceptorManager {

//    private final static String MOCK_SUBSCRIPTION = "00000000-0000-0000-0000-000000000000";
//    private final static String MOCK_TENANT = "00000000-0000-0000-0000-000000000000";
    //private final static String HOST = "localhost";
    private final static String RECORD_FOLDER = "session-records/";

    private Map<String, String> textReplacementRules = new HashMap<>();
    // Stores a map of all the HTTP properties in a session
    // A state machine ensuring a test is always reset before another one is setup

    protected RecordedData recordedData;

    private final String testName;

    private final TestMode testMode;

    public enum TestMode {
        PLAYBACK,
        RECORD
    }

//    public static InterceptorManager getInstance() throws IOException {
//        if (instance == null) {
//            instance = create();
//        }
//        return instance;
//    }
//
//    private static InterceptorManager instance = null;

    private InterceptorManager(String testName, TestMode testMode) {
        this.testName = testName;
        this.testMode = testMode;
    }

    public void addTextReplacementRule(String regex, String replacement) {
        textReplacementRules.put(regex, replacement);
    }

    // factory method
    public static InterceptorManager create(String testName, TestMode testMode) throws IOException {
        InterceptorManager interceptorManager = new InterceptorManager(testName, testMode);
        //interceptorManager.initMode();
        SdkContext.setResourceNamerFactory(new TestResourceNamerFactory(interceptorManager));
        SdkContext.setDelayProvider(new TestDelayProvider(interceptorManager.isRecordMode()));
        SdkContext.setRxScheduler(Schedulers.trampoline());

        return interceptorManager;
    }

    public boolean isRecordMode() {
        return testMode == TestMode.RECORD;
    }

    public boolean isPlaybackMode() {
        return testMode == TestMode.PLAYBACK;
    }

//    private void initMode() throws IOException{
//        String azureTestMode = System.getenv("AZURE_TEST_MODE");
//        if (azureTestMode != null) {
//            if (azureTestMode.equalsIgnoreCase("Record")) {
//                testMode = TestMode.RECORD;
//            } else if (azureTestMode.equalsIgnoreCase("Playback")) {
//                testMode = TestMode.PLAYBACK;
//            } else {
//                throw new IOException("Unknown AZURE_TEST_MODE: " + azureTestMode);
//            }
//        } else {
//            System.out.print("Environment variable 'AZURE_TEST_MODE' has not been set yet. Use 'Playback' mode.");
//            testMode = TestMode.PLAYBACK;
//        }
//    }

 //   private Map<String, Integer> callList = new HashMap<>();

    public Interceptor initInterceptor() throws IOException {
        switch (testMode) {
            case RECORD:
                recordedData = new RecordedData();
                return new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return record(chain);
                    }
                };
            case PLAYBACK:
                readDataFromFile();
//                if (callList.size() > 0)
//                    callList.clear();
                return new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return playback(chain);
                    }
                };
            default:
                System.out.println("==> Unknown AZURE_TEST_MODE: " + testMode);
        };
        return null;
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

    private Response record(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        NetworkCallRecord networkCallRecord = new NetworkCallRecord();

        networkCallRecord.Headers = new HashMap<>();

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
        networkCallRecord.Uri = applyReplacementRule(request.url().toString().replaceAll("\\?$", ""));

        Response response = chain.proceed(request);

        networkCallRecord.Response = new HashMap<>();
        networkCallRecord.Response.put("StatusCode", Integer.toString(response.code()));
        extractResponseData(networkCallRecord.Response, response);

        // remove pre-added header if this is a waiting or redirection
        if (networkCallRecord.Response.get("Body").contains("<Status>InProgress</Status>")
                || Integer.parseInt(networkCallRecord.Response.get("StatusCode")) == HttpStatus.SC_TEMPORARY_REDIRECT) {
            // Do nothing
        } else {
            synchronized (recordedData.getNetworkCallRecords()) {
                recordedData.getNetworkCallRecords().add(networkCallRecord);
            }
        }

        return response;
    }

    private Response playback(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String incomingUrl = applyReplacementRule(request.url().toString());
        String incomingMethod = request.method();

        int index = 0;
        incomingUrl = removeHost(incomingUrl);


//        String key = String.format("%s %s", incomingMethod, incomingUrl);
//        if(callList.containsKey(key)) {
//            Integer count = callList.get(key);
//            callList.put(key, count+1);
//        } else {
//            callList.put(key, 1);
//        }

        //List<NetworkCallRecord> rl = new ArrayList<>();

//        NetworkCallRecord networkCallRecord = null;
//        for (NetworkCallRecord record : recordedData.getNetworkCallRecords()) {
//            if (record.Method.equalsIgnoreCase(incomingMethod) && removeHost(record.Uri).equalsIgnoreCase(incomingUrl)) {
//                networkCallRecord = record;
//                //rl.add(record);
//                break;
//            }
//            index++;
//        }
        NetworkCallRecord networkCallRecord = null;
        synchronized (recordedData) {
            for (Iterator<NetworkCallRecord> iterator = recordedData.getNetworkCallRecords().iterator(); iterator.hasNext(); ) {
                NetworkCallRecord record = iterator.next();
                if (record.Method.equalsIgnoreCase(incomingMethod) && removeHost(record.Uri).equalsIgnoreCase(incomingUrl)) {
                    networkCallRecord = record;
                    iterator.remove();
                    //rl.add(record);
                    break;
                }
            }
        }


//        int foundQnty = rl.size();
//        if (foundQnty > 1) {
//            int i = 0;
//        }

        if (networkCallRecord == null) {
            System.out.println("NOT FOUND - " + incomingMethod + " " + incomingUrl);
            System.out.println("Remaining records " + recordedData.getNetworkCallRecords().size());
            throw new IOException("==> Unexpected request: " + incomingMethod + " " + incomingUrl);
        }

        //NetworkCallRecord networkCallRecord = recordedData.getNetworkCallRecords().remove(index);
        //recordedData.getNetworkCallRecords().remove(networkCallRecord);
        //recordedData.getNetworkCallRecords().remove(index);

        String url = removeHost(networkCallRecord.Uri);
        String method = networkCallRecord.Method;



        int recordStatusCode = Integer.parseInt(networkCallRecord.Response.get("StatusCode"));

        //        Response.Builder responseBuilder = new Response.Builder()
//                .code(recordStatusCode)
//                .protocol(Protocol.HTTP_1_1)
//                .request(request);


        Response originalResponse = chain.proceed(request);
        Response.Builder responseBuilder = originalResponse.newBuilder()
                .code(recordStatusCode);

        for (Map.Entry<String, String> pair : networkCallRecord.Response.entrySet()) {
            if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body") && !pair.getKey().equals("Content-Length")) {
                String rawHeader = pair.getValue();
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                    }
                }
                responseBuilder.addHeader(pair.getKey(), rawHeader);
            }
        }

        String rawBody = networkCallRecord.Response.get("Body");
        if (rawBody != null) {
            for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }

            String rawContentType = networkCallRecord.Response.get("content-type");
            String contentType =  rawContentType == null
                    ? "application/json; charset=utf-8"
                    : rawContentType;

            ResponseBody responseBody = ResponseBody.create(MediaType.parse(contentType), rawBody.getBytes());
            responseBuilder.body(responseBody);
            responseBuilder.addHeader("Content-Length", String.valueOf(rawBody.getBytes("UTF-8").length));
        }

        Response newResponce = responseBuilder.build();

        //chain.proceed(request);
        return newResponce;
    }

    private void extractResponseData(Map<String, String> responseData, Response response) throws IOException {
        Map<String, List<String>> headers = response.headers().toMultimap();
        boolean addedRetryAfter = false;
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            String headerValueToStore = header.getValue().get(0);

            if (header.getKey().equalsIgnoreCase("location") || header.getKey().equalsIgnoreCase("azure-asyncoperation")) {
                headerValueToStore = applyReplacementRule(headerValueToStore);
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
        } else if (response.header("Content-Encoding").equalsIgnoreCase("gzip")) {
            GZIPInputStream gis = new GZIPInputStream(buffer.inputStream());
            content = IOUtils.toString(gis);
            responseData.remove("Content-Encoding".toLowerCase());
            responseData.put("Content-Length".toLowerCase(), Integer.toString(content.length()));
        }

        if (content != null) {
            content = applyReplacementRule(content);
            responseData.put("Body", content);
        }
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
//        url = url.replace(replacementUrl, "");
//        url = url.substring(url.indexOf("/"));
//        return url;
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
