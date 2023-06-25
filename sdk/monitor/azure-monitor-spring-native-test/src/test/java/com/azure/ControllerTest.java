package com.azure;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.core.util.FluxUtil;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.azure.core.test.TestBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;


@SpringBootTest(classes = {Application.class, ControllerTest.TestConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"applicationinsights.native.spring.non-native.enabled=true"} // To execute the with a JVM and as a GraalVM native executable
)
public class ControllerTest extends TestBase {
//public class ControllerTest {

    private static final String CONNECTION_STRING_ENV = "InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;" + "IngestionEndpoint=https://test.in.applicationinsights.azure.com/;" + "LiveEndpoint=https://test.livediagnostics.monitor.azure.com/";

    private static final String INSTRUMENTATION_KEY = "00000000-0000-0000-0000-000000000000";

    private static CountDownLatch countDownLatch;

    private static CustomValidationPolicy customValidationPolicy;

    private static InterceptorManager INTERCEPTOR_MANAGER;

    @Override
    @BeforeEach
    public void setupTest(TestInfo testInfo)  {
        System.out.println("setup test");
       //Method testMethod = testInfo.getTestMethod().get();
        Method testMethod;

        try {
            testMethod = ControllerTest.class.getMethod("controller_should_return_ok");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        this.testContextManager = new TestContextManager(testMethod, TestMode.PLAYBACK);
        String playbackRecordName = "controller_should_return_ok";
        boolean doNotRecord = testContextManager.doNotRecordTest();
        System.out.println("doNotRecord = " + doNotRecord); // false
        String testName = testContextManager.getTestName(); //controller_should_return_ok
        System.out.println("testName = " + testName);
        interceptorManager = new InterceptorManager(testName, new HashMap<>(), doNotRecord, playbackRecordName);
        INTERCEPTOR_MANAGER = interceptorManager;
        countDownLatch = new CountDownLatch(1);
        customValidationPolicy = new CustomValidationPolicy(countDownLatch);
        testResourceNamer = new TestResourceNamer(testContextManager, interceptorManager.getRecordedData());
        //beforeTest();
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        HttpPipeline httpPipeline() {
            System.out.println("TestConfiguration.httpPipeline");
           return getHttpPipeline(customValidationPolicy);
           //return null;
        }

        HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy) {
            HttpClient httpClient;
            /*if (getTestMode() == TestMode.RECORD || getTestMode() == TestMode.LIVE) {
                httpClient = HttpClient.createDefault();
            } else {
             */
            httpClient = INTERCEPTOR_MANAGER.getPlaybackClient();
            //}

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            if (policy != null) {
                policies.add(policy);
            }
            policies.add(INTERCEPTOR_MANAGER.getRecordPolicy());

            return new HttpPipelineBuilder().httpClient(httpClient).policies(policies.toArray(new HttpPipelinePolicy[0])).build();
        }


    }

    private static class CustomValidationPolicy implements HttpPipelinePolicy {

        private final CountDownLatch countDown;
        private volatile URL url;
        private final List<TelemetryItem> actualTelemetryItems = new CopyOnWriteArrayList<>();

        CustomValidationPolicy(CountDownLatch countDown) {
            this.countDown = countDown;
        }

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            url = context.getHttpRequest().getUrl();
            Mono<String> asyncBytes = FluxUtil.collectBytesInByteBufferStream(context.getHttpRequest().getBody()).map(CustomValidationPolicy::ungzip);
            asyncBytes.subscribe(value -> {
                ObjectMapper objectMapper = createObjectMapper();
                try (MappingIterator<TelemetryItem> i = objectMapper.readerFor(TelemetryItem.class).readValues(value)) {
                    while (i.hasNext()) {
                        actualTelemetryItems.add(i.next());
                    }
                    countDown.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return next.process();
        }

        // decode gzipped request raw bytes back to original request body
        private static String ungzip(byte[] rawBytes) {
            if (rawBytes.length == 0) {
                return "";
            }
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int read;
                while ((read = in.read(data, 0, data.length)) != -1) {
                    baos.write(data, 0, read);
                }
                return new String(baos.toByteArray(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static ObjectMapper createObjectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            // handle JSR-310 (java 8) dates with Jackson by configuring ObjectMapper to use this
            // dependency and not (de)serialize Instant as timestamps that it does by default
            objectMapper.findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return objectMapper;
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void controller_should_return_ok() throws InterruptedException, MalformedURLException {
        String response = restTemplate.getForObject(Controller.URL, String.class);

        countDownLatch.await(10, SECONDS);

       // assertThat(customValidationPolicy.url).isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));
        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(1);

        assertThat(response).isEqualTo("OK!");
    }


}
