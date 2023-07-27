package com.azure;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestContextManager;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestResourceNamer;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.annotation.Nullable;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {Application.class, TelemetrySpringNativeTest.TestConfiguration.class}
    , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"applicationinsights.connection.string=InstrumentationKey=00000000-0000-0000-0000-0FEEDDADBEEF;IngestionEndpoint=https://test.in.applicationinsights.azure.com/;LiveEndpoint=https://test.livediagnostics.monitor.azure.com/"
    }
)
// TestConfiguration is loaded before the execution of the @BeforeEach setupTest method. This is the reason why static fields are used and the method name is hardcoded in the
// httpPipeline Spring bean.
public class TelemetrySpringNativeTest extends TestBase {

    private static CountDownLatch countDownLatch;

    private static CustomValidationPolicy customValidationPolicy;

    private static InterceptorManager interceptorManagerFromSpringBean;

    private static TestContextManager testContextManagerFromSpringBean;

    private static TestMode TEST_MODE = TestMode.PLAYBACK;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Override
    @BeforeEach
    public void setupTest(TestInfo testInfo) {
        this.interceptorManager = interceptorManagerFromSpringBean;
        this.testContextManager = testContextManagerFromSpringBean;
        this.testResourceNamer = new TestResourceNamer(testContextManagerFromSpringBean, interceptorManagerFromSpringBean.getRecordedData());
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {

        @Bean
        HttpPipeline httpPipeline() throws NoSuchMethodException {
            Method testMethod = TelemetrySpringNativeTest.class.getMethod("should_send_telemetry");

            testContextManagerFromSpringBean = new TestContextManager(testMethod, TestMode.PLAYBACK);
            boolean doNotRecord = testContextManagerFromSpringBean.doNotRecordTest();

            interceptorManagerFromSpringBean = new InterceptorManager(testMethod.getName(), new HashMap<>(), doNotRecord, "springNativeTelemetry");
            countDownLatch = new CountDownLatch(1);
            customValidationPolicy = new CustomValidationPolicy(countDownLatch);

            return getHttpPipeline(customValidationPolicy);
        }

        HttpPipeline getHttpPipeline(@Nullable HttpPipelinePolicy policy) {
            HttpClient httpClient;
            if (TEST_MODE == TestMode.RECORD || TEST_MODE == TestMode.LIVE) {
                httpClient = HttpClient.createDefault();
            } else {
                httpClient = interceptorManagerFromSpringBean.getPlaybackClient();
            }

            List<HttpPipelinePolicy> policies = new ArrayList<>();
            if (policy != null) {
                policies.add(policy);
            }
            policies.add(interceptorManagerFromSpringBean.getRecordPolicy());

            return new HttpPipelineBuilder().httpClient(httpClient).policies(policies.toArray(new HttpPipelinePolicy[0])).build();
        }

    }

    @Test
    public void should_send_telemetry() throws InterruptedException, MalformedURLException {
        String response = restTemplate.getForObject(Controller.URL, String.class);

        countDownLatch.await(10, SECONDS);

        assertThat(customValidationPolicy.url).isEqualTo(new URL("https://test.in.applicationinsights.azure.com/v2.1/track"));


        assertThat(customValidationPolicy.actualTelemetryItems.size()).isEqualTo(1);

        TelemetryItem telemetry = customValidationPolicy.actualTelemetryItems.get(0);
        assertThat(telemetry.getName()).isEqualTo("Request");

        assertThat(response).isEqualTo("OK!");
    }

}
