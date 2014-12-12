package com.microsoft.windowsazure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.mortbay.log.Log;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.UrlMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.apache.HttpServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.apache.HttpServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

public class MockIntegrationTestBase {
    protected final static Boolean IS_MOCKED = System.getenv(ManagementConfiguration.AZURE_TEST_MODE) != null ?
    		System.getenv(ManagementConfiguration.AZURE_TEST_MODE).equals("playback") : true;
    protected final static Boolean IS_RECORD = System.getenv(ManagementConfiguration.AZURE_TEST_MODE) != null ?
    		System.getenv(ManagementConfiguration.AZURE_TEST_MODE).equals("record") : false;

    private final static String RECORD_FOLDER = "session-records/";
    private final static String SUBSCRIPTION_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    protected final static String CLEANUP_SUFFIX = "Cleanup";
    
    private static List<ServiceClient<?>> clients = new ArrayList<ServiceClient<?>>();
    private static List<Callable<?>> funcs = new ArrayList<Callable<?>>();
    private static Map<String, String> regexRules = new HashMap<String, String>();
    private static LinkedList<Map<String, String>> context;
    private static String currentTestName = null;
    
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8043);
    @Rule
    public WireMockClassRule instanceRule = wireMockRule;
    @Rule
    public TestName name = new TestName();
    
    protected static void addClient(ServiceClient<?> client, Callable<?> func) {
        for (int i = 0; i < clients.size(); i++) {
            // Only one client of each class is allowed
            if (clients.get(i).getClass().equals(client.getClass())) {
                clients.set(i, client);
                funcs.set(i, func);
                return;
            }
        }
        clients.add(client);
        funcs.add(func);
    }
    
    protected static void addRegexRule(String regex) {
        regexRules.put(regex, null);
    }
    
    protected void setupTest() throws Exception {
        setupTest(name.getMethodName());
    }
    
    /**
     * Only change the current test name if there's no other tests running.
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
    protected static void setupTest(String testName) throws Exception {
        if (currentTestName == null) {
            currentTestName = testName;
        } else {
            return;
        }
        
        WireMock.reset();
        for (Callable<?> func : funcs) {
            func.call();
        }
        
        ServiceRequestFilter requestFilter = null;
        ServiceResponseFilter responseFilter = null;
        
        regexRules.put(SUBSCRIPTION_REGEX, null);
        
        if (IS_MOCKED) {
            File recordFile = getRecordFile();
            ObjectMapper mapper = new ObjectMapper();
            context = mapper.readValue(recordFile, new TypeReference<LinkedList<Map<String, String>>>() {});
            mockBaseUri();

            requestFilter = new ServiceRequestFilter() {
                @Override
                public void filter(ServiceRequestContext request) {                    
                    try {
                        String url = request.getURI().toString();
                        for (Entry<String, String> rule : regexRules.entrySet()) {
                            Matcher m = Pattern.compile(rule.getKey()).matcher(url);
                            if (m.find()) {
                                regexRules.put(rule.getKey(), m.group());
                            }
                        }
                        registerStub();
                    } catch (Exception e) {
                        Log.warn("Fail to register mock. " + e.getMessage());
                    }
                }
            };
        }
        
        if (IS_RECORD) {
            context = new LinkedList<Map<String, String>>();
            requestFilter = new ServiceRequestFilter() {
                @Override
                public void filter(ServiceRequestContext request) {
                    Map<String, String> requestHeader = new HashMap<String, String>();
                    requestHeader.put("Xmsversion", request.getHeader("x-ms-version"));
                    requestHeader.put("Method", request.getMethod());
                    requestHeader.put("Uri", request.getURI().toString().replaceAll("\\?$", ""));
                    context.add(requestHeader);
                }
            };
            responseFilter = new ServiceResponseFilter() {
                @Override
                public void filter(ServiceRequestContext request, ServiceResponseContext response) {
                    Map<String, String> responseData = new HashMap<String, String>();
                    try {
                        responseData.put("StatusCode", Integer.toString(response.getStatus()));
                        extractResponseData(responseData, response);
                        
                        // remove pre-added header if this is a waiting or redirection
                        if (responseData.get("Body").contains("<Status>InProgress</Status>") ||
                                Integer.parseInt(responseData.get("StatusCode")) == HttpStatus.SC_TEMPORARY_REDIRECT) {
                            context.removeLast();
                        } else {
                            context.add(responseData);
                        }
                    } catch (Exception e) {
                        Log.warn("Fail to register recorder. " + e.getMessage());
                    }
                }
            };
        }
        attachFilters(requestFilter, responseFilter);
    }
    
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
            File recordFile = getRecordFile();
            recordFile.createNewFile();
            mapper.writeValue(recordFile, context);
        }
        
        WireMock.reset();
        currentTestName = null;
        for (Callable<?> func : funcs) {
            func.call();
        }
    }
    
    private static void extractResponseData(Map<String, String> responseData, ServiceResponseContext response) throws Exception {
        Field f = response.getClass().getDeclaredField("clientResponse");
        f.setAccessible(true);
        HttpResponse clientResponse = (HttpResponse) f.get(response);
        for (Header header : clientResponse.getAllHeaders()) {
            responseData.put(header.getName(), header.getValue());
        }
        
        BufferedHttpEntity entity = new BufferedHttpEntity(clientResponse.getEntity());
        responseData.put("Body", IOUtils.toString(entity.getContent()));
        
        // Set entity to be buffered otherwise it can't be consumed again
        clientResponse.setEntity(entity);
    }
    
    private static File getRecordFile() {
        URL folderUrl = MockIntegrationTestBase.class.getClassLoader().getResource(".");
        File folderFile = new File(folderUrl.getPath() + RECORD_FOLDER);
        if (!folderFile.exists()) folderFile.mkdir();
        String filePath = folderFile.getPath() + "/" + currentTestName + ".json";
        return new File(filePath);
    }
    
    private static void mockBaseUri() throws Exception {
        for (int i = 0; i < clients.size(); i++) {
            Field f = clients.get(i).getClass().getDeclaredField("baseUri");
            f.setAccessible(true);
            f.set(clients.get(i), new URI("http://localhost:8043"));
        }
    }
    
    private static void attachFilters(ServiceRequestFilter requestFilter, ServiceResponseFilter responseFilter) throws Exception {
        for (int i = 0; i < clients.size(); i++) {
            Field f = clients.get(i).getClass().getSuperclass().getDeclaredField("httpClientBuilder");
            f.setAccessible(true);
            HttpClientBuilder httpClientBuilder = (HttpClientBuilder) f.get(clients.get(i));
            if (requestFilter != null) {
                httpClientBuilder.addInterceptorFirst(new MockHttpRequestInspector(requestFilter));
            }
            if (responseFilter != null) {
                httpClientBuilder.addInterceptorFirst(new MockHttpResponseInspector(responseFilter));
            }
        }
    }
    
    private static void registerStub() throws Exception {
        Map<String, String> requestHeader = context.remove();
        Map<String, String> responseData = context.remove();
        String url = requestHeader.get("Uri");
        for (Entry<String, String> rule : regexRules.entrySet()) {
            if (rule.getValue() != null) {
                url = url.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        UrlMatchingStrategy urlStrategy = urlEqualTo(url);
        String method = requestHeader.get("Method");
        MappingBuilder mBuilder = null;
        if (method.equals("GET")) {
            mBuilder = get(urlStrategy);
        } else if (method.equals("POST")) {
            mBuilder = post(urlStrategy);
        } else if (method.equals("PUT")) {
            mBuilder = put(urlStrategy);
        } else if (method.equals("DELETE")) {
            mBuilder = delete(urlStrategy);
        } else if (method.equals("PATCH")) {
            mBuilder = patch(urlStrategy);
        } else {
            throw new Exception("Invalid HTTP method.");
        }
        mBuilder.withHeader("x-ms-version", equalTo(requestHeader.get("Xmsversion")));
        
        ResponseDefinitionBuilder rBuilder = aResponse().withStatus(Integer.parseInt(responseData.get("StatusCode")));
        for (Entry<String, String> header : responseData.entrySet()) {
            if (!header.getKey().equals("StatusCode") && !header.getKey().equals("Body")) {
                rBuilder.withHeader(header.getKey(), header.getValue());
            }
        }
        
        String rawBody = responseData.get("Body");
        for (Entry<String, String> rule : regexRules.entrySet()) {
            if (rule.getValue() != null) {
                rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        rBuilder.withBody(rawBody);
        
        mBuilder.willReturn(rBuilder);
        stubFor(mBuilder);
    }
    
    private static class MockHttpRequestInspector implements HttpRequestInterceptor {
        private ServiceRequestFilter filter;

        public MockHttpRequestInspector(ServiceRequestFilter filter) {
            this.filter = filter;
        }

        @Override
        public void process(HttpRequest request, HttpContext context) {
            filter.filter(new HttpServiceRequestContext(request, context));
        }
    }
    
    private static class MockHttpResponseInspector implements HttpResponseInterceptor {
        private ServiceResponseFilter filter;

        public MockHttpResponseInspector(ServiceResponseFilter filter) {
            this.filter = filter;
        }

        @Override
        public void process(HttpResponse response, HttpContext context) {
            filter.filter(null, new HttpServiceResponseContext(response, context));
        }
    }
}
