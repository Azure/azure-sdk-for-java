package com.microsoft.windowsazure.management;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
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
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.apache.HttpServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.apache.HttpServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestFilter;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseFilter;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

public class MockIntegrationTestBase {
    protected static Boolean isMocked = Boolean.parseBoolean(System.getenv(ManagementConfiguration.MOCKED));
    protected static Boolean isRecording = Boolean.parseBoolean(System.getenv(ManagementConfiguration.RECORDING));
    
    private static List<ServiceClient<?>> clients = new ArrayList<ServiceClient<?>>();
    private static List<Callable<?>> funcs = new ArrayList<Callable<?>>();
    
    private static Map<String, String> regexRules = new HashMap<String, String>();
    private static List<Map<String, String>> context;
    
    @ClassRule
    @Rule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8043);
    @Rule
    public static TestName name = new TestName();
    private static String recordFolder = "__records/";
    private static String currentTestName = null;
    private static int responseCount;
    
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
    
    protected static void setupTest() throws Exception {
        setupTest(null);
    }
    
    protected static void setupTest(String testName) throws Exception {
        if (currentTestName == null) {
            currentTestName = testName;
            responseCount = 0;
        }
        ServiceRequestFilter requestFilter = null;
        ServiceResponseFilter responseFilter = null;
        
        if (isMocked) {
            requestFilter = new ServiceRequestFilter() {
                @Override
                public void filter(ServiceRequestContext request) {
                    if (currentTestName == null) {
                        currentTestName = name.getMethodName();
                    }
                    
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
            mockBaseUri();
        }
        
        if (isRecording) {
            requestFilter = new ServiceRequestFilter() {
                @Override
                public void filter(ServiceRequestContext request) {
                    if (currentTestName == null) {
                        currentTestName = name.getMethodName();
                    }

                    context = new ArrayList<Map<String, String>>();
                    Map<String, String> requestHeader = new HashMap<String, String>();
                    requestHeader.put("Xmsversion", request.getHeader("x-ms-version"));
                    requestHeader.put("Method", request.getMethod());
                    requestHeader.put("Uri", request.getURI().toString());
                    context.add(requestHeader);
                }
            };
            responseFilter = new ServiceResponseFilter() {
                @Override
                public void filter(ServiceRequestContext request, ServiceResponseContext response) {
                    Map<String, String> responseHeader = new HashMap<String, String>();
                    try {
                        Field f = response.getClass().getDeclaredField("clientResponse");
                        f.setAccessible(true);
                        HttpResponse clientResponse = (HttpResponse) f.get(response);
                        responseHeader.put("StatusCode", Integer.toString(response.getStatus()));
                        for (Header header : clientResponse.getAllHeaders()) {
                            responseHeader.put(header.getName(), header.getValue());
                        }
                        context.add(responseHeader);
                        
                        ObjectMapper mapper = new ObjectMapper();
                        URL folderUrl = MockIntegrationTestBase.class.getClassLoader().getResource(recordFolder);
                        File folderFile = new File(folderUrl.getPath());
                        if (!folderFile.exists()) folderFile.mkdir();
                        String headerFilePath = folderUrl.getPath() + "/" + currentTestName + "_" + responseCount + ".json";
                        File headerFile = new File(headerFilePath);
                        headerFile.createNewFile();
                        mapper.writeValue(headerFile, context);
                        
                        File bodyFile = new File(headerFilePath.replaceAll(".json", ".xml"));
                        BufferedHttpEntity entity = new BufferedHttpEntity(clientResponse.getEntity());
                        entity.writeTo(new FileOutputStream(bodyFile));
                        clientResponse.setEntity(entity);
                        
                        responseCount++;
                    } catch (Exception e) {
                        Log.warn("Fail to register recorder. " + e.getMessage());
                    }
                }
            };
        }
        attachFilters(requestFilter, responseFilter);
    }
    
    protected static void resetTest() throws Exception {
        WireMock.reset();
        currentTestName = null;
        for (Callable<?> func : funcs) {
            func.call();
        }
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
        URL headerUrl = MockIntegrationTestBase.class.getClassLoader().getResource(recordFolder + currentTestName + "_" + responseCount + ".json");
        File headerFile = new File(headerUrl.getPath());
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> headers = mapper.readValue(headerFile, new TypeReference<List<Map<String, String>>>() {});
        String url = headers.get(0).get("Uri");
        for (Entry<String, String> rule : regexRules.entrySet()) {
            if (rule.getValue() != null) {
                url.replaceAll(rule.getKey(), rule.getValue());
            }
        }
        UrlMatchingStrategy urlStrategy = urlEqualTo(url);
        String method = headers.get(0).get("Method");
        MappingBuilder mBuilder = null;
        if (method.equals("GET")) {
            mBuilder = get(urlStrategy);
        } else if (method.equals("POST")) {
            mBuilder = post(urlStrategy);
        } else if (method.equals("PUT")) {
            mBuilder = put(urlStrategy);
        } else if (method.equals("DELETE")) {
            mBuilder = delete(urlStrategy);
        } else {
            throw new Exception("Invalid HTTP method.");
        }
        mBuilder.withHeader("x-ms-version", equalTo(headers.get(0).get("Xmsversion")));
        
        ResponseDefinitionBuilder rBuilder = aResponse().withStatus(Integer.parseInt(headers.get(1).get("StatusCode")));
        for (Entry<String, String> header : headers.get(1).entrySet()) {
            if (!header.getKey().equals("StatusCode")) {
                rBuilder.withHeader(header.getKey(), header.getValue());
            }
        }
        
        File bodyFile = new File(headerUrl.getPath().replaceAll(".json", ".xml"));
        rBuilder.withBody(Files.toString(bodyFile, Charsets.UTF_8));
        
        mBuilder.willReturn(rBuilder);
        stubFor(mBuilder);
        
        responseCount++;
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
